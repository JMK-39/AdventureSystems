//? if >=1.21 {
/*package dev.xyat.adventuresystems.ftb.data;

import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import dev.xyat.kineticcore.api.runtime.KineticPlatform;
import dev.xyat.kineticcore.api.runtime.KineticServerRuntime;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/^* Native component patches in SNBT; the existing config field name does not imply legacy item NBT. *^/
public final class FTBItemComponents {
    private FTBItemComponents() {}

    public static HolderLookup.Provider registries() {
        return dev.xyat.adventuresystems.data.AdventureItemData.registryAccess();
    }

    public static CompoundTag encode(ItemStack stack) {
        return encode(stack, registries());
    }

    public static CompoundTag encode(ItemStack stack, HolderLookup.Provider lookup) {
        if (stack == null || stack.isEmpty() || stack.getComponentsPatch().isEmpty()) return new CompoundTag();
        var result = DataComponentPatch.CODEC.encodeStart(lookup.createSerializationContext(NbtOps.INSTANCE), stack.getComponentsPatch());
        return (CompoundTag) result.getOrThrow(error -> new IllegalArgumentException("Invalid FTB item components: " + error));
    }

    public static void apply(ItemStack stack, CompoundTag encoded, HolderLookup.Provider lookup) {
        var patch = DataComponentPatch.CODEC.parse(lookup.createSerializationContext(NbtOps.INSTANCE), encoded)
                .getOrThrow(error -> new IllegalArgumentException("Invalid FTB item components: " + error));
        stack.applyComponents(patch);
    }

    public static boolean hasStoredEnchantments(ItemStack stack) {
        return stack != null && !stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty();
    }

    public static CompoundTag storedEnchantments(ItemStack stack) {
        CompoundTag all = encode(stack);
        CompoundTag clean = new CompoundTag();
        var value = all.get("minecraft:stored_enchantments");
        if (value != null) clean.put("minecraft:stored_enchantments", value.copy());
        return clean;
    }
}
*///?}
