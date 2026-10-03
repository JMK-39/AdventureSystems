//? if >=1.21 {
/*package dev.xyat.adventurevalidation;
import dev.xyat.adventuresystems.ftb.api.FTBTaskSubmitHelper;

import dev.architectury.utils.Env;
import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.ItemReward;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.UUID;

/^* Uses actual FTB quest/team state. Removing any gate enables resource consumption for an invalid bulk submit. *^/
public final class FTBSubmitPermissionTest {
    public static void main(String[] args) throws Exception {
        if (net.neoforged.fml.loading.LoadingModList.get() == null) {
            net.neoforged.fml.loading.LoadingModList.of(java.util.List.of(), java.util.List.of(), java.util.List.of(), java.util.List.of(), java.util.Map.of());
        }
        net.neoforged.fml.loading.FMLPaths.loadAbsolutePaths(java.nio.file.Files.createTempDirectory("adventure-ftb-tests-"));
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        run();
    }

    // Invoke from the loader runtime after FTB's access transformers have been applied.
    public static void run() {
        var file = new FixtureFile();
        var chapter = new Chapter(1L, file, file.getDefaultChapterGroup());
        var quest = new Quest(2L, chapter);
        CompoundTag settings = new CompoundTag();
        settings.putBoolean("can_repeat", true);
        settings.putString("progression_mode", "linear");
        quest.readData(settings, file.holderLookup());
        var task = new ItemTask(3L, quest).setStackAndCount(new ItemStack(Items.STONE), 4);
        task.setConsumeItems(Tristate.TRUE);
        quest.addTask(task);
        var reward = new ItemReward(4L, quest, new ItemStack(Items.DIAMOND));
        quest.addReward(reward);
        var data = new TeamData(UUID.randomUUID(), file);
        check(FTBTaskSubmitHelper.isCustomSubmitAllowed(data, task), "Valid single-task repeat must be eligible");
        data.setLocked(true);
        check(!FTBTaskSubmitHelper.isCustomSubmitAllowed(data, task), "Locked teams must never consume items");
        data.setLocked(false);
        data.setRewardsBlocked(true);
        check(!FTBTaskSubmitHelper.isCustomSubmitAllowed(data, task), "Blocked reward must stop before item consumption");
        data.setRewardsBlocked(false);
        task.setConsumeItems(Tristate.FALSE);
        check(!FTBTaskSubmitHelper.isCustomSubmitAllowed(data, task), "Non-consuming tasks must use normal submission");
        task.setConsumeItems(Tristate.TRUE);
        var second = new ItemTask(5L, quest).setStackAndCount(new ItemStack(Items.STONE), 1);
        quest.addTask(second);
        check(!FTBTaskSubmitHelper.isCustomSubmitAllowed(data, task), "Multi-task quests cannot bulk claim rewards");
        quest.removeTask(second);
        var secondReward = new ItemReward(6L, quest, new ItemStack(Items.GOLD_INGOT));
        quest.addReward(secondReward);
        check(!FTBTaskSubmitHelper.isCustomSubmitAllowed(data, task), "Multi-reward quests cannot bulk claim rewards");
        quest.removeReward(secondReward);
        var dependency = new Quest(7L, chapter);
        dependency.addTask(new ItemTask(8L, dependency).setStackAndCount(new ItemStack(Items.DIRT), 1));
        quest.addDependency(dependency);
        data.clearCachedProgress();
        check(!FTBTaskSubmitHelper.isCustomSubmitAllowed(data, task), "Incomplete dependencies must stop submission");
        System.out.println("FTB submission permission checks passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class FixtureFile extends BaseQuestFile {
        @Override public Env getSide() { return Env.CLIENT; }
        @Override public HolderLookup.Provider holderLookup() { return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY); }
        @Override public boolean deleteObjects(List<Long> ids) { return false; }
        @Override public boolean isPlayerOnTeam(Player player, TeamData data) { return false; }
        @Override public String getLocale() { return "en_us"; }
    }
}
*///?}
