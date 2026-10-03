//? if >=1.21 {
/*package dev.xyat.adventuresystems.ftb.data;

import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

/^* Real component checks. A dropped patch, shared CustomData, or count-sensitive key fails these checks. *^/
public final class FTBItemComponentsTest {
    public static void main(String[] args) throws Exception {
        if (net.neoforged.fml.loading.LoadingModList.get() == null) {
            net.neoforged.fml.loading.LoadingModList.of(java.util.List.of(), java.util.List.of(), java.util.List.of(), java.util.List.of(), java.util.Map.of());
        }
        net.neoforged.fml.loading.FMLPaths.loadAbsolutePaths(java.nio.file.Files.createTempDirectory("adventure-ftb-tests-"));
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        verify();
    }

    public static void verify() {
        var lookup = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        ItemStack source = new ItemStack(Items.DIAMOND_SWORD, 17);
        source.set(DataComponents.CUSTOM_NAME, Component.literal("FTB variant"));
        source.set(DataComponents.DAMAGE, 9);
        CompoundTag data = new CompoundTag();
        data.putByte("flag", (byte) 1);
        data.putLong("balance", 9_007_199_254_740_993L);
        source.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        source.remove(DataComponents.ATTRIBUTE_MODIFIERS);

        CompoundTag encoded = FTBItemComponents.encode(source, lookup);
        ItemStack restored = new ItemStack(Items.DIAMOND_SWORD);
        FTBItemComponents.apply(restored, encoded, lookup);
        check(restored.getComponentsPatch().equals(source.getComponentsPatch()),
                "Bindings must retain names, damage, numeric tag types and removed defaults");
        ItemStack otherCount = source.copyWithCount(1);
        check(FTBItemComponents.encode(otherCount, lookup).equals(encoded),
                "Quest/favorite/cache variant keys must ignore stack count");
        CompoundTag changed = restored.get(DataComponents.CUSTOM_DATA).copyTag();
        changed.putLong("balance", 1L);
        restored.set(DataComponents.CUSTOM_DATA, CustomData.of(changed));
        check(source.get(DataComponents.CUSTOM_DATA).copyTag().getLong("balance") == 9_007_199_254_740_993L,
                "Editing a display stack must not mutate the original stack");
        check(!FTBItemComponents.encode(restored, lookup).equals(encoded),
                "Distinct item components must not alias the quest match cache");
        boolean rejected = false;
        try {
            CompoundTag legacy = new CompoundTag();
            legacy.putString("GunId", "old:data");
            FTBItemComponents.apply(restored, legacy, lookup);
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        check(rejected, "Native components must reject legacy item NBT instead of converting it");
        System.out.println("FTB native component checks passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
*///?}
