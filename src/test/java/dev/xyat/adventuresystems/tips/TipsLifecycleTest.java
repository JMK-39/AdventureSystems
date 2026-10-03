//? if >=1.21 {
/*package dev.xyat.adventuresystems.tips;
public class TipsLifecycleTest {
 @org.junit.jupiter.api.BeforeAll static void bootstrap()throws Exception{if(net.neoforged.fml.loading.LoadingModList.get()==null)net.neoforged.fml.loading.LoadingModList.of(java.util.List.of(),java.util.List.of(),java.util.List.of(),java.util.List.of(),java.util.Map.of());net.neoforged.fml.loading.FMLPaths.loadAbsolutePaths(java.nio.file.Files.createTempDirectory("tips-lifecycle-tests-"));net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
 @org.junit.jupiter.api.Test void dynamicConditionsDoNotDiscardLoadingTipsBeforeWorld(){
 String json="{\"tips\":[{\"text\":\"loading\",\"stage\":\"loading\"},{\"text\":\"enchanted\",\"stage\":\"game\",\"conditions\":{\"items\":[{\"id\":\"minecraft:diamond_sword\",\"components\":\"[enchantments={levels:{\\\"minecraft:sharpness\\\":1}}]\",\"componentMode\":\"WEAK\"}]}}]}";
 var entries=dev.xyat.adventuresystems.tips.config.ConfigLoader.fromJson(json);org.junit.jupiter.api.Assertions.assertNotNull(entries,"World registries are not yet available; loading entries must survive");
 var tips=dev.xyat.adventuresystems.tips.config.ConfigLoader.toRuntimeTips(entries);org.junit.jupiter.api.Assertions.assertEquals(2,tips.size());org.junit.jupiter.api.Assertions.assertEquals(1,tips.get(1).requiredItems.size(),"Unresolved condition must not become unconditional");
 org.junit.jupiter.api.Assertions.assertFalse(TipsUtils.matchNbt(tips.get(1).requiredItems.get(0),new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD)),"Unresolved enchantments must not match ordinary items");
 }
 @org.junit.jupiter.api.Test void freshDefaultsIncludeTipsForPauseMenuWithoutConditions() {
  for (String language : java.util.List.of("en_us","zh_cn")) {
   var entries=dev.xyat.adventuresystems.tips.config.ConfigLoader.getRawEntriesForLanguage(language);
   org.junit.jupiter.api.Assertions.assertTrue(
       dev.xyat.adventuresystems.tips.config.ConfigLoader.toRuntimeTips(entries).stream().anyMatch(tip ->
           tip.stage != 1 && tip.requiredDimension.isEmpty() && tip.requiredBiome.isEmpty()
               && tip.requiredStructure.isEmpty() && tip.requiredAdvancement.isEmpty()
               && tip.requiredItems.isEmpty() && tip.requiredCurios.isEmpty()),
       language + ": a fresh installation must offer a pause-menu tip without world/item requirements");
  }
 }
}

*///?}
