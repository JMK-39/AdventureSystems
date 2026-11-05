package dev.xyat.adventuresystems.curios.windthunderwings;

import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.kineticcore.api.network.NetworkCodec;
import dev.xyat.kineticcore.api.network.NetworkVersionPolicy;
import dev.xyat.kineticcore.api.network.PacketChannel;
import dev.xyat.kineticcore.api.network.ServerPacketContext;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import net.minecraft.server.level.ServerPlayer;

/** 风雷翅的同步：服务端把佩戴者的状态发给自己和附近的玩家，客户端发送遁光加速与燃血请求。 */
public final class WingsNetwork {
    private static final int ACTION_BOOST = 0;
    private static final int ACTION_BURN = 1;
    private static final PacketChannel CHANNEL = PacketChannel.create(
            KineticResourceIds.of(CuriosModule.MODID, "wind_thunder_wings"),
            "2",
            NetworkVersionPolicy.ANY
    );
    private static boolean registered;

    private WingsNetwork() {
    }

    /** 一名佩戴者的状态：是否佩戴、是否在超人飞行、飞行状态、燃血剩余 tick，以及当前状态的速度倍率（相对创造飞行）。 */
    public record State(int entityId, boolean worn, boolean flying, int mode, int burnTicks, float speed) {
    }

    private record Action(int action) {
    }

    static void register() {
        if (registered) return;
        registered = true;
        CHANNEL.registerClientboundLazy(0, State.class,
                NetworkCodec.of(
                        (buffer, packet) -> {
                            buffer.writeVarInt(packet.entityId());
                            buffer.writeBoolean(packet.worn());
                            buffer.writeBoolean(packet.flying());
                            buffer.writeVarInt(packet.mode());
                            buffer.writeVarInt(packet.burnTicks());
                            buffer.writeFloat(packet.speed());
                        },
                        buffer -> new State(buffer.readVarInt(), buffer.readBoolean(), buffer.readBoolean(),
                                buffer.readVarInt(), buffer.readVarInt(), buffer.readFloat())
                ),
                () -> dev.xyat.adventuresystems.curios.windthunderwings.client.WingsClient::applyState);
        CHANNEL.registerServerbound(1, Action.class,
                NetworkCodec.of((buffer, packet) -> buffer.writeVarInt(packet.action()), buffer -> new Action(buffer.readVarInt())),
                WingsNetwork::handleAction);
    }

    static void sendState(ServerPlayer player, boolean worn, boolean flying, WingsMode mode, int burnTicks, float speed) {
        CHANNEL.sendToTrackingAndSelf(player, new State(player.getId(), worn, flying, mode.ordinal(), burnTicks, speed));
    }

    private static void handleAction(Action packet, ServerPacketContext context) {
        ServerPlayer player = context.sender();
        if (player == null) return;
        if (packet.action() == ACTION_BOOST) WingsServer.toggleBoost(player);
        else if (packet.action() == ACTION_BURN) WingsServer.burn(player);
    }

    /** 客户端：切换遁光加速。 */
    public static void requestBoost() {
        CHANNEL.sendToServer(new Action(ACTION_BOOST));
    }

    /** 客户端：燃血。 */
    public static void requestBurn() {
        CHANNEL.sendToServer(new Action(ACTION_BURN));
    }
}
