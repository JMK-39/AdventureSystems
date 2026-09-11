package dev.xyat.adventuresystems.curios.wallet.compat.rs;

import com.refinedmods.refinedstorage.api.IRSAPI;
import com.refinedmods.refinedstorage.api.RSAPIInject;
import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.util.Action;
import com.refinedmods.refinedstorage.api.util.IComparer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class RefinedStorageCompat {
    @RSAPIInject
    public static IRSAPI API;

    private RefinedStorageCompat() {
    }

    private static boolean unavailable() {
        return !ModList.get().isLoaded("refinedstorage");
    }

    public static boolean isController(ServerLevel level, BlockPos pos) {
        if (unavailable() || level == null || pos == null) return false;
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(level.getBlockState(pos).getBlock());
        return id != null && "refinedstorage".equals(id.getNamespace()) && id.getPath().contains("controller");
    }

    public static boolean canBind(ServerLevel level, BlockPos pos) {
        return isController(level, pos);
    }

    public static boolean hasNetwork(ServerLevel level, BlockPos pos) {
        return network(level, pos) != null;
    }

    public static long count(ServerLevel level, BlockPos pos, ItemStack target) {
        if (target == null || target.isEmpty()) return 0L;
        INetwork network = network(level, pos);
        if (network == null || !network.canRun()) return 0L;
        ItemStack extracted = network.extractItem(target.copy(), Integer.MAX_VALUE, IComparer.COMPARE_NBT, Action.SIMULATE);
        if (extracted.isEmpty()) return 0L;
        int count = extracted.getCount();
        return count < 0 ? 0L : count;
    }

    public static long extract(ServerLevel level, BlockPos pos, ItemStack target, long amount, boolean simulate) {
        if (target == null || target.isEmpty() || amount <= 0L) return 0L;
        INetwork network = network(level, pos);
        if (network == null || !network.canRun()) return 0L;
        long remaining = amount;
        long extractedTotal = 0L;
        while (remaining > 0L) {
            int step = (int) Math.min(Integer.MAX_VALUE, remaining);
            ItemStack extracted = network.extractItem(target.copy(), step, IComparer.COMPARE_NBT, simulate ? Action.SIMULATE : Action.PERFORM);
            int got = extracted.isEmpty() ? 0 : extracted.getCount();
            if (got == 0) break;
            extractedTotal = safeAdd(extractedTotal, got);
            remaining = remaining - got;
            if (simulate) break;
        }
        return extractedTotal;
    }

    private static long safeAdd(long a, long b) {
        if (a < 0L || b < 0L) return Long.MAX_VALUE;
        if (Long.MAX_VALUE - a < b) return Long.MAX_VALUE;
        return a + b;
    }

    private static INetwork network(ServerLevel level, BlockPos pos) {
        if (unavailable() || API == null || level == null || pos == null) return null;
        try {
            return API.getNetworkManager(level).getNetwork(pos);
        } catch (Throwable ignored) {
            return null;
        }
    }
}

