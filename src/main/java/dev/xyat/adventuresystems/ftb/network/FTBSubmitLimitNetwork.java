package dev.xyat.adventuresystems.ftb.network;

import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.xyat.adventuresystems.ftb.FtbModule;
import dev.xyat.adventuresystems.ftb.api.FTBTaskSubmitHelper;
import dev.xyat.adventuresystems.ftb.api.FTBVirtualItemProviders;
import dev.xyat.adventuresystems.ftb.client.FTBVirtualItemClientState;
import dev.xyat.adventuresystems.ftb.client.hud.FTBSubmitResultClientToast;
import dev.xyat.kineticcore.api.network.NetworkCodec;
import dev.xyat.kineticcore.api.network.NetworkVersionPolicy;
import dev.xyat.kineticcore.api.network.PacketChannel;
import dev.xyat.kineticcore.api.network.ServerPacketContext;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import net.minecraft.server.level.ServerPlayer;

public final class FTBSubmitLimitNetwork {
    private static final PacketChannel CHANNEL = PacketChannel.create(
            KineticResourceIds.of(FtbModule.MODID, "ftb_submit_limit"),
            "1",
            NetworkVersionPolicy.ANY
    );
    private static boolean registered;

    private FTBSubmitLimitNetwork() {
    }

    public static void register() {
        if (registered) return;
        registered = true;

        CHANNEL.registerServerbound(
                0,
                ServerboundSubmitTaskWithCount.class,
                NetworkCodec.of(
                        (buffer, packet) -> {
                            buffer.writeLong(packet.taskId());
                            buffer.writeVarInt(Math.max(1, packet.count()));
                        },
                        buffer -> new ServerboundSubmitTaskWithCount(buffer.readLong(), Math.max(1, buffer.readVarInt()))
                ),
                FTBSubmitLimitNetwork::handleSubmitTask
        );
        CHANNEL.registerClientbound(
                1,
                ClientboundSubmitResult.class,
                NetworkCodec.of(
                        (buffer, packet) -> {
                            buffer.writeVarInt(Math.max(1, packet.requestedTimes()));
                            buffer.writeVarInt(Math.max(0, packet.completedTimes()));
                            buffer.writeVarLong(Math.max(0L, packet.submittedItems()));
                        },
                        buffer -> new ClientboundSubmitResult(
                                Math.max(1, buffer.readVarInt()),
                                Math.max(0, buffer.readVarInt()),
                                Math.max(0L, buffer.readVarLong())
                        )
                ),
                packet -> FTBSubmitResultClientToast.show(
                        packet.requestedTimes(),
                        packet.completedTimes(),
                        packet.submittedItems()
                )
        );
        CHANNEL.registerServerbound(
                2,
                ServerboundVirtualItemCountQuery.class,
                NetworkCodec.of(
                        (buffer, packet) -> buffer.writeLong(packet.taskId()),
                        buffer -> new ServerboundVirtualItemCountQuery(buffer.readLong())
                ),
                FTBSubmitLimitNetwork::handleVirtualItemQuery
        );
        CHANNEL.registerClientbound(
                3,
                ClientboundVirtualItemCount.class,
                NetworkCodec.of(
                        (buffer, packet) -> {
                            buffer.writeLong(packet.taskId());
                            buffer.writeBoolean(packet.supported());
                            buffer.writeVarLong(Math.max(0L, packet.count()));
                        },
                        buffer -> new ClientboundVirtualItemCount(
                                buffer.readLong(),
                                buffer.readBoolean(),
                                Math.max(0L, buffer.readVarLong())
                        )
                ),
                packet -> FTBVirtualItemClientState.accept(packet.taskId(), packet.supported(), packet.count())
        );
    }

    public static void sendSubmit(long taskId, int count) {
        CHANNEL.sendToServer(new ServerboundSubmitTaskWithCount(taskId, count));
    }

    public static void sendVirtualItemCountQuery(long taskId) {
        CHANNEL.sendToServer(new ServerboundVirtualItemCountQuery(taskId));
    }

    private static void sendResult(ServerPlayer player, FTBTaskSubmitHelper.Result result) {
        if (player == null || result == null) return;
        CHANNEL.sendToPlayer(
                player,
                new ClientboundSubmitResult(result.requestedTimes(), result.completedTimes(), result.submittedItems())
        );
    }

    private static void handleSubmitTask(ServerboundSubmitTaskWithCount packet, ServerPacketContext context) {
        ServerPlayer player = context.sender();
        TeamData data = TeamData.get(player);
        if (data.isLocked()) return;

        Task task = data.getFile().getTask(packet.taskId());
        if (!(task instanceof ItemTask itemTask)) return;
        if (!data.canStartTasks(itemTask.getQuest())) return;

        BaseQuestFile file = data.getFile();
        if (!(file instanceof ServerQuestFile sqf)) return;

        sqf.withPlayerContext(player, () -> {
            if (!FTBTaskSubmitHelper.isCustomSubmitAllowed(data, itemTask)) {
                itemTask.submitTask(data, player);
                return;
            }

            FTBTaskSubmitHelper.Result result = FTBTaskSubmitHelper.submit(player, data, itemTask, packet.count());
            sendResult(player, result);
        });
    }

    private static void handleVirtualItemQuery(ServerboundVirtualItemCountQuery packet, ServerPacketContext context) {
        ServerPlayer player = context.sender();
        boolean supported = false;
        long count = 0L;
        TeamData data = TeamData.get(player);
        Task task = data.getFile().getTask(packet.taskId());
        if (task instanceof ItemTask itemTask && data.canStartTasks(itemTask.getQuest())) {
            FTBVirtualItemProviders.QueryResult result = FTBVirtualItemProviders.query(player, itemTask.getItemStack());
            supported = result.supported();
            count = result.count();
        }

        CHANNEL.sendToPlayer(player, new ClientboundVirtualItemCount(packet.taskId(), supported, count));
    }

    public record ServerboundSubmitTaskWithCount(long taskId, int count) {
    }

    public record ClientboundSubmitResult(int requestedTimes, int completedTimes, long submittedItems) {
    }

    public record ServerboundVirtualItemCountQuery(long taskId) {
    }

    public record ClientboundVirtualItemCount(long taskId, boolean supported, long count) {
    }
}
