//? if >=1.21 {
/*package dev.xyat.adventuresystems.data;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.UUID;
import dev.xyat.adventuresystems.curios.paradiselost.data.ParadiseLostSavedData;
public class AccessoryComponentsTest {
    @BeforeAll static void bootstrap() throws Exception {
        if (net.neoforged.fml.loading.LoadingModList.get() == null) net.neoforged.fml.loading.LoadingModList.of(java.util.List.of(),java.util.List.of(),java.util.List.of(),java.util.List.of(),java.util.Map.of());
        net.neoforged.fml.loading.FMLPaths.loadAbsolutePaths(java.nio.file.Files.createTempDirectory("adventure-tests-"));
        net.minecraft.SharedConstants.tryDetectVersion(); net.minecraft.server.Bootstrap.bootStrap();
    }
    @Test void customDataSnapshotsCannotMutateOriginal() {
        ItemStack stack=new ItemStack(Items.DIAMOND); UUID owner=UUID.randomUUID();
        AdventureItemData.updateCustomData(stack,t->{t.putUUID("adventuresystems_owner_id",owner);t.putInt("adventuresystems_hos_stacks",120);});
        var read=AdventureItemData.customData(stack);read.putInt("adventuresystems_hos_stacks",0);
        assertEquals(120,AdventureItemData.customData(stack).getInt("adventuresystems_hos_stacks"));
        assertEquals(owner,AdventureItemData.customData(stack).getUUID("adventuresystems_owner_id"));
    }
    @Test void copiedAccessoryGrowsIndependently() {
        ItemStack stack=new ItemStack(Items.DIAMOND); AdventureItemData.updateCustomData(stack,t->t.putInt("pl_score",10));
        ItemStack copy=stack.copy(); AdventureItemData.updateCustomData(copy,t->t.putInt("pl_score",30));
        assertEquals(10,AdventureItemData.customData(stack).getInt("pl_score"));assertEquals(30,AdventureItemData.customData(copy).getInt("pl_score"));
    }
    @Test void nativeStackSaveRetainsOwnerGrowthAndTimers() {
        ItemStack stack=new ItemStack(Items.DIAMOND);UUID owner=UUID.randomUUID();
        AdventureItemData.updateCustomData(stack,t->{t.putUUID("adventuresystems_owner_id",owner);t.putInt("adventuresystems_hos_stacks",300);t.putLong("adventuresystems_hos_next_charge",123456789L);});
        ItemStack loaded=ItemStack.parseOptional(AdventureItemData.registryAccess(),(net.minecraft.nbt.CompoundTag) stack.save(AdventureItemData.registryAccess()));
        assertTrue(ItemStack.isSameItemSameComponents(stack,loaded));assertEquals(owner,AdventureItemData.customData(loaded).getUUID("adventuresystems_owner_id"));
    }
    @Test void foodScoreCountsDistinctFoodsAndSurvivesPersistence() {
        ParadiseLostSavedData data=new ParadiseLostSavedData();UUID player=UUID.randomUUID();UUID other=UUID.randomUUID();
        data.addFood(player,"minecraft:apple",4);data.addFood(player,"minecraft:apple",4);data.addFood(player,"minecraft:bread",5);
        assertEquals(9,data.getScore(player));assertEquals(0,data.getScore(other));
        ParadiseLostSavedData loaded=ParadiseLostSavedData.load(data.save(new net.minecraft.nbt.CompoundTag(),AdventureItemData.registryAccess()));
        assertEquals(9,loaded.getScore(player));assertTrue(loaded.hasEaten(player,"minecraft:apple"));assertFalse(loaded.hasEaten(other,"minecraft:apple"));
    }
}

*///?}
