//? if >=1.21 {
/*package dev.xyat.adventuresystems.data;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
public final class AdventureItemData {
    private AdventureItemData() {}
    public static CompoundTag customData(ItemStack stack) { return stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag(); }
    public static void updateCustomData(ItemStack stack, java.util.function.Consumer<CompoundTag> action) { net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, action); }
    public static String toItemText(ItemStack stack) { return dev.xyat.kineticcore.api.inventory.KineticItemText.format(stack); }
    public static ItemStack parseItemText(String text) { return dev.xyat.kineticcore.api.inventory.KineticItemText.parse(text); }
    public static boolean hasWorldRegistries() { return registryAccess().lookup(net.minecraft.core.registries.Registries.ENCHANTMENT).isPresent(); }
    public static net.minecraft.core.HolderLookup.Provider registryAccess() {
        var server = dev.xyat.kineticcore.api.runtime.KineticServerRuntime.currentServer();
        if (server != null && server.isSameThread()) return server.registryAccess();
        var lookup = dev.xyat.kineticcore.api.runtime.KineticPlatform.callOnClient(() -> () -> {
            var level=dev.xyat.kineticcore.api.runtime.KineticClientRuntime.currentLevel();
            return level == null ? null : level.registryAccess();
        }, null);
        if (lookup != null) return lookup;
        return server != null ? server.registryAccess() : net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
    }
}

*///?}
