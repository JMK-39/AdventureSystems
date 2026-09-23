package dev.xyat.adventuresystems.curios.wallet.storage;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class WalletMaterialMatcher {
    public static final String PICKED_UP_TAG = "kt_picked_up";

    private WalletMaterialMatcher() {
    }

    public static boolean matches(ItemStack first, ItemStack second) {
        if (first == null || second == null || first.isEmpty() || second.isEmpty()) return false;
        return ItemStack.isSameItemSameTags(normalized(first), normalized(second));
    }

    public static List<ItemStack> exactVariants(ItemStack target) {
        if (target == null || target.isEmpty()) return List.of();
        ItemStack clean = normalized(target);
        ItemStack picked = clean.copy();
        picked.getOrCreateTag().putBoolean(PICKED_UP_TAG, true);
        List<ItemStack> result = new ArrayList<>(2);
        result.add(clean);
        if (!ItemStack.isSameItemSameTags(clean, picked)) result.add(picked);
        return List.copyOf(result);
    }

    private static ItemStack normalized(ItemStack source) {
        ItemStack copy = source.copy();
        CompoundTag tag = copy.getTag();
        if (tag == null || !tag.contains(PICKED_UP_TAG)) return copy;
        tag.remove(PICKED_UP_TAG);
        if (tag.isEmpty()) copy.setTag(null);
        return copy;
    }
}
