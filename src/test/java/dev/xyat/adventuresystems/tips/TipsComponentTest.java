package dev.xyat.adventuresystems.tips;

//? if >=1.21 {
/*import dev.xyat.adventuresystems.tips.api.HelpTip;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/^* Focused checks against the real native component map and removed defaults. ^/
public final class TipsComponentTest {
    public static void main(String[] args) {
        if (net.neoforged.fml.loading.LoadingModList.get() == null) {
            net.neoforged.fml.loading.LoadingModList.of(java.util.List.of(), java.util.List.of(), java.util.List.of(),
                    java.util.List.of(), java.util.Map.of());
        }
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        verify();
    }

    /^* Can also run after NeoForge has bootstrapped in the runtime fixture. ^/
    public static void verify() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        DataComponentPatch damage = DataComponentPatch.builder().set(DataComponents.DAMAGE, 3).build();
        var weak = new HelpTip.ItemMatcher("minecraft:diamond_sword", damage, HelpTip.ComponentMode.WEAK);
        check(!TipsUtils.matchNbt(weak, sword), "Weak matching accepted the wrong damage");
        sword.set(DataComponents.DAMAGE, 3);
        check(TipsUtils.matchNbt(weak, sword), "Weak matching rejected required damage");
        var strong = new HelpTip.ItemMatcher("minecraft:diamond_sword", damage, HelpTip.ComponentMode.STRONG);
        check(TipsUtils.matchNbt(strong, sword), "Strong matching rejected an equal patch");
        sword.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Named"));
        check(TipsUtils.matchNbt(weak, sword), "Weak matching rejected an extra component");
        check(!TipsUtils.matchNbt(strong, sword), "Strong matching accepted an extra component");
        DataComponentPatch removed = DataComponentPatch.builder().remove(DataComponents.MAX_DAMAGE).build();
        var absent = new HelpTip.ItemMatcher("minecraft:diamond_sword", removed, HelpTip.ComponentMode.WEAK);
        check(!TipsUtils.matchNbt(absent, sword), "Removal predicate ignored a default component");
        sword.remove(DataComponents.MAX_DAMAGE);
        check(TipsUtils.matchNbt(absent, sword), "Removal predicate rejected an absent component");
        var none = new HelpTip.ItemMatcher("minecraft:diamond_sword", damage, HelpTip.ComponentMode.NONE);
        check(TipsUtils.matchNbt(none, new ItemStack(Items.DIAMOND_SWORD)), "NONE matched component data");
        DataComponentPatch undamaged = DataComponentPatch.builder().set(DataComponents.DAMAGE, 0).build();
        var zeroDamage = new HelpTip.ItemMatcher("minecraft:diamond_sword", undamaged, HelpTip.ComponentMode.WEAK);
        check(!TipsUtils.matchNbt(zeroDamage, sword), "Explicit default damage was dropped from a weak predicate");
        check(TipsUtils.matchNbt(zeroDamage, new ItemStack(Items.DIAMOND_SWORD)), "Default damage failed to match");
        var strongDefault = new HelpTip.ItemMatcher("minecraft:diamond_sword", undamaged, HelpTip.ComponentMode.STRONG);
        check(TipsUtils.matchNbt(strongDefault, new ItemStack(Items.DIAMOND_SWORD)), "Strong matching failed to normalize defaults");
        String nativeJson = "{\"tips\":[{\"text\":\"Native\",\"conditions\":{\"items\":[{\"id\":\"minecraft:diamond_sword\",\"components\":\"[damage=0]\",\"componentMode\":\"WEAK\"}]}}]}";
        var entries = dev.xyat.adventuresystems.tips.config.ConfigLoader.fromJson(nativeJson);
        check(entries != null, "Native component conditions were rejected");
        var parsed = dev.xyat.adventuresystems.tips.config.ConfigLoader.toRuntimeTips(entries).getFirst().requiredItems.getFirst();
        check(!TipsUtils.matchNbt(parsed, sword), "Parsing normalized away an explicit default-valued predicate");
        check(dev.xyat.adventuresystems.tips.config.ConfigLoader.fromJson(nativeJson.replace("[damage=0]", "{Damage:0}")) == null,
                "Legacy item NBT was accepted as native components");
        check(dev.xyat.adventuresystems.tips.config.ConfigLoader.fromJson(nativeJson.replace("components", "nbt")) == null,
                "Old condition fields were silently dropped");
        System.out.println("Tips native component matching checks passed");
    }

    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
*///?} else {
public final class TipsComponentTest {
    public static void main(String[] args) {
        System.out.println("Native component checks apply to NeoForge 1.21.1");
    }
}
//?}
