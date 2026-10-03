//? if >=1.21 {
/*package dev.xyat.adventuresystems.curios.wallet.compat.rs;
import dev.xyat.kineticcore.api.runtime.KineticPlatform;
import dev.xyat.adventuresystems.curios.wallet.storage.WalletMaterialMatcher;
import com.refinedmods.refinedstorage.common.api.support.network.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.api.core.Action;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class RefinedStorageCompat {
    private RefinedStorageCompat() {}
    public static boolean isController(ServerLevel level, BlockPos pos) {
        return KineticPlatform.isModLoaded("refinedstorage") && level != null && pos != null
                && level.getBlockEntity(pos) instanceof com.refinedmods.refinedstorage.common.controller.ControllerBlockEntity;
    }
    public static boolean canBind(ServerLevel level, BlockPos pos) { return isController(level, pos); }
    public static boolean canBind(ServerPlayer player, ServerLevel level, BlockPos pos) {
        return isController(level, pos) && ((AbstractNetworkNodeContainerBlockEntity<?>) level.getBlockEntity(pos)).getContainerProvider().canBuild(player);
    }
    public static boolean hasNetwork(ServerLevel level, BlockPos pos) { return network(level, pos) != null; }
    public static long count(ServerPlayer player, ServerLevel level, BlockPos pos, ItemStack target) {
        return extract(player, level, pos, target, Long.MAX_VALUE, true);
    }
    public static long extract(ServerPlayer player, ServerLevel level, BlockPos pos, ItemStack target, long amount, boolean simulate) {
        if (player == null || target == null || target.isEmpty() || amount <= 0L) return 0L;
        Network network = network(level, pos);
        if (network == null) return 0L;
        return extractFromNetwork(player,network,target,amount,simulate);
    }
    private static long extractFromNetwork(ServerPlayer player, Network network, ItemStack target, long amount, boolean simulate) {
        if (!com.refinedmods.refinedstorage.common.api.security.SecurityHelper.isAllowed(player, com.refinedmods.refinedstorage.common.security.BuiltinPermission.EXTRACT, network)) return 0L;
        StorageNetworkComponent storage = network.getComponent(StorageNetworkComponent.class);
        if (storage == null) return 0L;
        long remaining = amount;
        for (ItemStack variant : WalletMaterialMatcher.exactVariants(target)) {
            long got = storage.extract(ItemResource.ofItemStack(variant), remaining,
                    simulate ? Action.SIMULATE : Action.EXECUTE, new PlayerActor(player));
            remaining -= Math.max(0L, Math.min(got, remaining));
            if (remaining == 0L) break;
        }
        return amount - remaining;
    }
    private static Network network(ServerLevel level, BlockPos pos) {
        if (!isController(level, pos)) return null;
        var blockEntity = (AbstractNetworkNodeContainerBlockEntity<?>) level.getBlockEntity(pos);
        for (var container : blockEntity.getContainerProvider().getContainers()) {
            var node = container.getNode();
            if (node instanceof AbstractNetworkNode active && active.isActive() && node.getNetwork() != null) return node.getNetwork();
        }
        return null;
    }
}
*///?} else {
package dev.xyat.adventuresystems.curios.wallet.compat.rs;

import dev.xyat.kineticcore.api.runtime.KineticPlatform;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.adventuresystems.curios.wallet.storage.WalletMaterialMatcher;
import com.refinedmods.refinedstorage.api.IRSAPI;
import com.refinedmods.refinedstorage.api.RSAPIInject;
import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.util.Action;
import com.refinedmods.refinedstorage.api.util.IComparer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

public class RefinedStorageCompat {
    @RSAPIInject
    public static IRSAPI API;

    private RefinedStorageCompat() {
    }

    private static boolean unavailable() {
        return !KineticPlatform.isModLoaded("refinedstorage");
    }

    public static boolean isController(ServerLevel level, BlockPos pos) {
        if (unavailable() || level == null || pos == null) return false;
        ResourceLocation id = KineticRegistries.blocks().id(level.getBlockState(pos).getBlock());
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
        long total = 0L;
        for (ItemStack variant : WalletMaterialMatcher.exactVariants(target)) {
            ItemStack extracted = network.extractItem(variant, Integer.MAX_VALUE, IComparer.COMPARE_NBT, Action.SIMULATE);
            if (!extracted.isEmpty() && extracted.getCount() > 0) {
                total = safeAdd(total, extracted.getCount());
            }
        }
        return total;
    }

    public static long extract(ServerLevel level, BlockPos pos, ItemStack target, long amount, boolean simulate) {
        if (target == null || target.isEmpty() || amount <= 0L) return 0L;
        INetwork network = network(level, pos);
        if (network == null || !network.canRun()) return 0L;
        long remaining = amount;
        long extractedTotal = 0L;
        for (ItemStack variant : WalletMaterialMatcher.exactVariants(target)) {
            while (remaining > 0L) {
                int step = (int) Math.min(Integer.MAX_VALUE, remaining);
                ItemStack extracted = network.extractItem(variant, step, IComparer.COMPARE_NBT,
                        simulate ? Action.SIMULATE : Action.PERFORM);
                int got = extracted.isEmpty() ? 0 : extracted.getCount();
                if (got == 0) break;
                extractedTotal = safeAdd(extractedTotal, got);
                remaining -= got;
                if (simulate) break;
            }
            if (remaining <= 0L) break;
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

//?}
