package dev.xyat.adventuresystems.data;

/**
 * Item stacks stored in NBT, empty stacks included. 1.21.1 has save/parse helpers on ItemStack; 26.1 stores them
 * through the optional item codec, in the same format. Not used on 1.20.1, which stores stacks with ItemStack#save.
 */
public final class ItemStacks {
    private ItemStacks() {
    }

    //? if >=26.1 {
    /*public static net.minecraft.nbt.Tag save(net.minecraft.world.item.ItemStack stack) {
        return net.minecraft.world.item.ItemStack.OPTIONAL_CODEC.encodeStart(ops(), stack).getOrThrow();
    }

    public static net.minecraft.world.item.ItemStack parse(net.minecraft.nbt.CompoundTag tag) {
        return net.minecraft.world.item.ItemStack.OPTIONAL_CODEC.parse(ops(), tag).result()
                .orElse(net.minecraft.world.item.ItemStack.EMPTY);
    }

    private static net.minecraft.resources.RegistryOps<net.minecraft.nbt.Tag> ops() {
        return AdventureItemData.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
    }
    *///?} else if >=1.21 {
    /*public static net.minecraft.nbt.Tag save(net.minecraft.world.item.ItemStack stack) {
        return stack.saveOptional(AdventureItemData.registryAccess());
    }

    public static net.minecraft.world.item.ItemStack parse(net.minecraft.nbt.CompoundTag tag) {
        return net.minecraft.world.item.ItemStack.parseOptional(AdventureItemData.registryAccess(), tag);
    }
    *///?}
}
