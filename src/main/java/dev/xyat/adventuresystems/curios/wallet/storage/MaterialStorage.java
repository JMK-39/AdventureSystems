package dev.xyat.adventuresystems.curios.wallet.storage;

import dev.xyat.kineticcore.api.runtime.KineticPlatform;
import dev.xyat.adventuresystems.curios.wallet.compat.rs.RefinedStorageCompat;
import dev.xyat.adventuresystems.curios.wallet.compat.sophisticated.SophisticatedBackpackCompat;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class MaterialStorage {
    private MaterialStorage() {
    }

    public static Snapshot snapshot(ServerPlayer player, ItemStack target) {
        if (player == null || target == null || target.isEmpty()) return Snapshot.empty();
        Optional<ItemStack> wallet = Data.equippedWallet(player);
        long inventory = inventoryCount(player.getInventory(), target);
        boolean backpackLoaded = KineticPlatform.isModLoaded("sophisticatedbackpacks") && KineticPlatform.isModLoaded("sophisticatedcore");
        boolean hasBackpack = backpackLoaded && SophisticatedBackpackCompat.hasAnyBackpack(player);
        long backpack = hasBackpack ? SophisticatedBackpackCompat.count(player, target) : 0L;
        boolean rsLoaded = KineticPlatform.isModLoaded("refinedstorage");
        Data.RsBinding binding = wallet.map(Data::rsBinding).orElse(Data.RsBinding.none());
        RsState rsState = RsState.HIDDEN;
        long rs = 0L;
        if (rsLoaded) {
            if (!binding.bound()) {
                rsState = RsState.UNBOUND;
            } else if (!(player.level() instanceof ServerLevel serverLevel)) {
                rsState = RsState.MISSING;
            } else {
                BlockPos pos = binding.pos();
                if (RefinedStorageCompat.hasNetwork(serverLevel, pos)) {
                    rsState = RsState.BOUND;
                    rs = RefinedStorageCompat.count(serverLevel, pos, target);
                } else {
                    rsState = RsState.MISSING;
                }
            }
        }
        return new Snapshot(inventory, backpack, rs, backpackLoaded, hasBackpack, rsLoaded, rsState);
    }

    public static Snapshot snapshot(ServerPlayer player, ItemStack target, boolean useBackpack, boolean useRs) {
        Snapshot snapshot = snapshot(player, target);
        return new Snapshot(
                snapshot.inventoryCount(),
                useBackpack ? snapshot.backpackCount() : 0L,
                useRs ? snapshot.rsCount() : 0L,
                useBackpack && snapshot.backpackLoaded(),
                useBackpack && snapshot.hasBackpack(),
                useRs && snapshot.rsLoaded(),
                useRs ? snapshot.rsState() : RsState.HIDDEN
        );
    }

    public static ConsumeResult consume(ServerPlayer player, ItemStack target, long amount) {
        return consume(player, target, amount, true, true);
    }

    public static ConsumeResult consume(ServerPlayer player, ItemStack target, long amount, boolean useBackpack, boolean useRs) {
        if (player == null || target == null || target.isEmpty() || amount <= 0L) {
            return ConsumeResult.empty(snapshot(player, target, useBackpack, useRs));
        }
        Snapshot before = snapshot(player, target, useBackpack, useRs);
        long remaining = amount;
        long fromBackpack = 0L;
        if (useBackpack && before.hasBackpack()) {
            fromBackpack = SophisticatedBackpackCompat.extract(player, target, remaining, false);
            remaining -= fromBackpack;
        }
        long fromRs = 0L;
        if (useRs && remaining > 0L && before.rsState() == RsState.BOUND && player.level() instanceof ServerLevel serverLevel) {
            Optional<ItemStack> wallet = Data.equippedWallet(player);
            if (wallet.isPresent()) {
                Data.RsBinding binding = Data.rsBinding(wallet.get());
                fromRs = RefinedStorageCompat.extract(serverLevel, binding.pos(), target, remaining, false);
                remaining -= fromRs;
            }
        }
        long fromInventory = remaining > 0L ? removeFromInventory(player.getInventory(), target, remaining) : 0L;
        Snapshot after = snapshot(player, target, useBackpack, useRs);
        return new ConsumeResult(fromInventory, fromBackpack, fromRs, fromInventory + fromBackpack + fromRs, after);
    }


    public static long consumeInventory(ServerPlayer player, ItemStack target, long amount) {
        if (player == null || target == null || target.isEmpty() || amount <= 0L) return 0L;
        return removeFromInventory(player.getInventory(), target, amount);
    }

    public static long consumeBackpack(ServerPlayer player, ItemStack target, long amount) {
        if (player == null || target == null || target.isEmpty() || amount <= 0L) return 0L;
        if (!KineticPlatform.isModLoaded("sophisticatedbackpacks") || !KineticPlatform.isModLoaded("sophisticatedcore")) return 0L;
        if (!SophisticatedBackpackCompat.hasAnyBackpack(player)) return 0L;
        return SophisticatedBackpackCompat.extract(player, target, amount, false);
    }

    public static long consumeRs(ServerPlayer player, ItemStack target, long amount) {
        if (player == null || target == null || target.isEmpty() || amount <= 0L) return 0L;
        if (!KineticPlatform.isModLoaded("refinedstorage")) return 0L;
        Optional<ItemStack> wallet = Data.equippedWallet(player);
        if (wallet.isEmpty()) return 0L;
        Data.RsBinding binding = Data.rsBinding(wallet.get());
        if (!binding.bound() || !(player.level() instanceof ServerLevel serverLevel)) return 0L;
        if (!RefinedStorageCompat.hasNetwork(serverLevel, binding.pos())) return 0L;
        return RefinedStorageCompat.extract(serverLevel, binding.pos(), target, amount, false);
    }

    private static long inventoryCount(Inventory inventory, ItemStack target) {
        long count = 0L;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (WalletMaterialMatcher.matches(stack, target)) count = safeAdd(count, stack.getCount());
        }
        return count;
    }

    private static long removeFromInventory(Inventory inventory, ItemStack target, long amount) {
        long remaining = amount;
        for (int i = 0; i < inventory.getContainerSize() && remaining > 0L; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!WalletMaterialMatcher.matches(stack, target)) continue;
            int remove = (int) Math.min(stack.getCount(), remaining);
            stack.shrink(remove);
            remaining -= remove;
        }
        inventory.setChanged();
        return amount - remaining;
    }

    private static long safeAdd(long a, long b) {
        if (a < 0L || b < 0L) return Long.MAX_VALUE;
        if (Long.MAX_VALUE - a < b) return Long.MAX_VALUE;
        return a + b;
    }

    public enum RsState {
        HIDDEN,
        UNBOUND,
        BOUND,
        MISSING
    }

    public record Snapshot(long inventoryCount, long backpackCount, long rsCount, boolean backpackLoaded, boolean hasBackpack, boolean rsLoaded, RsState rsState) {
        public static Snapshot empty() {
            return new Snapshot(0L, 0L, 0L, false, false, false, RsState.HIDDEN);
        }

        public long total() {
            return safeAdd(safeAdd(inventoryCount, backpackCount), rsCount);
        }
    }

    public record ConsumeResult(long fromInventory, long fromBackpack, long fromRs, long total, Snapshot after) {
        public static ConsumeResult empty(Snapshot snapshot) {
            return new ConsumeResult(0L, 0L, 0L, 0L, snapshot);
        }
    }
}

