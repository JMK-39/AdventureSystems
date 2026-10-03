//? if >=1.21 {
/*package dev.xyat.adventuresystems.ftb.api;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import net.minecraft.server.level.ServerPlayer;

/^* FTB 2101 dispatches record messages from a static handler and queues all quest mutations. *^/
public final class FTBTaskSubmitInterceptor {
    private FTBTaskSubmitInterceptor() {}

    public static void handle(long taskId, NetworkManager.PacketContext context) {
        if (!(context.getPlayer() instanceof ServerPlayer player)) return;
        context.queue(() -> ServerQuestFile.getInstance().ifPresent(file -> file.getTeamData(player).ifPresent(data -> {
            if (data.isLocked()) return;
            var task = file.getTask(taskId);
            if (task == null || !data.canStartTasks(task.getQuest())) return;
            file.withPlayerContext(player, () -> {
                if (task instanceof ItemTask itemTask && FTBTaskSubmitHelper.isCustomSubmitAllowed(data, itemTask)) {
                    FTBTaskSubmitHelper.submit(player, data, itemTask, 1);
                } else {
                    task.submitTask(data, player);
                }
            });
        })));
    }
}
*///?}
