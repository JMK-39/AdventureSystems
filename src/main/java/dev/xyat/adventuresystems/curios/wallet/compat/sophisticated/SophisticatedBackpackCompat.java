package dev.xyat.adventuresystems.curios.wallet.compat.sophisticated;

import dev.xyat.kineticcore.api.runtime.KineticPlatform;
import dev.xyat.adventuresystems.curios.wallet.storage.WalletMaterialMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import top.theillusivec4.curios.api.CuriosApi;

public final class SophisticatedBackpackCompat {
    private SophisticatedBackpackCompat() {
    }

    private static boolean unavailable() {
        return !KineticPlatform.isModLoaded("sophisticatedbackpacks") || !KineticPlatform.isModLoaded("sophisticatedcore");
    }

    public static boolean hasAnyBackpack(ServerPlayer player) {
        return !backpacks(player).isEmpty();
    }

    public static long count(ServerPlayer player, ItemStack target) {
        if (player == null || target == null || target.isEmpty()) return 0L;
        long total = 0L;
        for (ItemStack backpack : backpacks(player)) {
            Optional<IBackpackWrapper> wrapper = wrapper(backpack);
            if (wrapper.isEmpty()) continue;
            InventoryHandler handler = wrapper.get().getInventoryHandler();
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack current = handler.getStackInSlot(slot);
                if (WalletMaterialMatcher.matches(current, target)) {
                    total = safeAdd(total, current.getCount());
                }
            }
        }
        return total;
    }

    public static long extract(ServerPlayer player, ItemStack target, long amount, boolean simulate) {
        if (player == null || target == null || target.isEmpty() || amount <= 0L) return 0L;
        long remaining = amount;
        long removed = 0L;
        for (ItemStack backpack : backpacks(player)) {
            if (remaining <= 0L) break;
            Optional<IBackpackWrapper> wrapper = wrapper(backpack);
            if (wrapper.isEmpty()) continue;
            InventoryHandler handler = wrapper.get().getInventoryHandler();
            for (int slot = 0; slot < handler.getSlots() && remaining > 0L; slot++) {
                ItemStack current = handler.getStackInSlot(slot);
                if (!WalletMaterialMatcher.matches(current, target)) continue;
                int step = (int) Math.min(current.getCount(), Math.min(Integer.MAX_VALUE, remaining));
                ItemStack extracted = handler.extractItem(slot, step, simulate);
                int got = extracted.isEmpty() ? 0 : extracted.getCount();
                if (got == 0) continue;
                removed += got;
                remaining -= got;
            }
        }
        return removed;
    }

    private static List<ItemStack> backpacks(ServerPlayer player) {
        List<ItemStack> result = new ArrayList<>();
        if (unavailable() || player == null) return result;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isBackpack(stack)) result.add(stack);
        }
        try {
            for (top.theillusivec4.curios.api.SlotResult slotResult : CuriosApi.getCuriosHelper().findCurios(player, SophisticatedBackpackCompat::isBackpack)) {
                if (slotResult != null && isBackpack(slotResult.stack())) result.add(slotResult.stack());
            }
        } catch (Throwable ignored) {
        }
        return result;
    }

    private static boolean isBackpack(ItemStack stack) {
        return stack != null && !stack.isEmpty() && wrapper(stack).isPresent();
    }

    private static Optional<IBackpackWrapper> wrapper(ItemStack stack) {
        if (unavailable() || stack == null || stack.isEmpty()) return Optional.empty();
        try {
            return stack.getCapability(CapabilityBackpackWrapper.getCapabilityInstance()).resolve();
        } catch (Throwable ignored) {
            return Optional.empty();
        }
    }

    private static long safeAdd(long a, long b) {
        if (a < 0L || b < 0L) return Long.MAX_VALUE;
        if (Long.MAX_VALUE - a < b) return Long.MAX_VALUE;
        return a + b;
    }
}

