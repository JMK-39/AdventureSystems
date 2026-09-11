package dev.xyat.adventuresystems.curios.wallet.network;

import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.adventuresystems.curios.wallet.client.Client;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.adventuresystems.curios.wallet.shop.Shop;
import dev.xyat.kineticcore.api.KTNetworkProtocol;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class Network {
    private static final String PROTOCOL = "1";
    private static final int MAX_SHOP_ACTIONS_PER_SECOND = 8;
    private static final long SHOP_ACTION_WINDOW_TICKS = 20L;
    private static final Map<ServerPlayer, ShopActionWindow> SHOP_ACTION_WINDOWS = new WeakHashMap<>();
    private static SimpleChannel channel;
    private static int packetId;

    public static void register() {
        channel = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(CuriosModule.MODID, "currency_wallet"),
                () -> PROTOCOL,
                KTNetworkProtocol::acceptsAnyVersion,
                KTNetworkProtocol::acceptsAnyVersion
        );
        channel.registerMessage(packetId++, ClientboundSync.class, ClientboundSync::encode, ClientboundSync::decode,
                ClientboundSync::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(packetId++, ClientboundToast.class, ClientboundToast::encode, ClientboundToast::decode,
                ClientboundToast::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(packetId++, ServerboundAction.class, ServerboundAction::encode, ServerboundAction::decode,
                ServerboundAction::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(packetId++, ClientboundShopOpen.class, ClientboundShopOpen::encode, ClientboundShopOpen::decode,
                ClientboundShopOpen::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(packetId++, ClientboundShopRefresh.class, ClientboundShopRefresh::encode, ClientboundShopRefresh::decode,
                ClientboundShopRefresh::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(packetId++, ServerboundShopAction.class, ServerboundShopAction::encode, ServerboundShopAction::decode,
                ServerboundShopAction::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(packetId++, ServerboundShopEdit.class, ServerboundShopEdit::encode, ServerboundShopEdit::decode,
                ServerboundShopEdit::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    public static void open(ServerPlayer player) {
        if (!Data.hasWallet(player)) return;
        send(player, true, Data.isHudVisible(player), true, Data.snapshot(player));
    }

    public static void openShop(ServerPlayer player) {
        if (channel == null || player == null || !Data.hasWallet(player)) return;
        channel.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundShopOpen(Data.snapshot(player), Shop.clientTag(player), Shop.isEditMode(player)));
    }

    public static void refreshShop(ServerPlayer player) {
        if (channel == null || player == null || !Data.hasWallet(player)) return;
        channel.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundShopRefresh(Data.snapshot(player), Shop.clientTag(player), Shop.isEditMode(player)));
    }

    public static void broadcastShopRefresh(ServerPlayer source) {
        if (channel == null || source == null) return;
        for (ServerPlayer player : source.server.getPlayerList().getPlayers()) {
            refreshShop(player);
        }
    }

    public static void sync(ServerPlayer player) {
        boolean equipped = Data.hasWallet(player);
        boolean visible = equipped && Data.isHudVisible(player);
        send(player, false, visible, equipped, equipped ? Data.snapshot(player) : new CompoundTag());
    }

    public static void hide(ServerPlayer player) {
        send(player, false, false, false, new CompoundTag());
    }

    public static void toast(ServerPlayer player, String translationKey) {
        toast(player, translationKey, new String[0]);
    }

    public static void toast(ServerPlayer player, String translationKey, String... args) {
        if (channel == null || player == null || translationKey == null || translationKey.isBlank()) return;
        channel.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundToast(packToast(translationKey, args)));
    }

    private static String packToast(String translationKey, String... args) {
        StringBuilder builder = new StringBuilder(translationKey);
        if (args != null) {
            for (String arg : args) builder.append("|").append(arg == null ? "" : arg.replace("|", "/"));
        }
        return builder.toString();
    }

    public static void sendDepositAll() {
        if (channel != null) channel.sendToServer(new ServerboundAction(0, "", ""));
    }

    public static void sendWithdraw(String currencyId) {
        if (channel != null) channel.sendToServer(new ServerboundAction(1, currencyId, ""));
    }

    public static void sendConvertAll(String from, String to) {
        if (channel != null) channel.sendToServer(new ServerboundAction(2, from, to));
    }

    public static void sendOpen() {
        if (channel != null) channel.sendToServer(new ServerboundAction(3, "", ""));
    }

    public static void sendConvertOne(String from, String to) {
        if (channel != null) channel.sendToServer(new ServerboundAction(4, from, to));
    }

    public static void sendToggleHudCurrency() {
        if (channel != null) channel.sendToServer(new ServerboundAction(5, "", ""));
    }

    public static void sendOpenShop() {
        if (channel != null) channel.sendToServer(new ServerboundShopAction(0, 0, 1, 0));
    }

    /** Requests the permission-checked server shop editor from the config hub. */
    public static void sendOpenShopEditor() {
        if (channel != null) channel.sendToServer(new ServerboundShopAction(8, 0, 0, 0));
    }

    public static void sendShopBuy(int index, int amount) {
        if (channel != null) channel.sendToServer(new ServerboundShopAction(1, index, amount, 0));
    }

    public static void sendShopSell(int index, int amount) {
        if (channel != null) channel.sendToServer(new ServerboundShopAction(2, index, amount, 0));
    }

    public static void sendRemoveShopEntry(Shop.Mode mode, int index) {
        if (channel != null && mode != null) channel.sendToServer(new ServerboundShopAction(3, index, 1, mode.ordinal()));
    }

    public static void sendMoveShopEntry(Shop.Mode mode, int fromIndex, int toIndex) {
        if (channel != null && mode != null) channel.sendToServer(new ServerboundShopAction(4, fromIndex, toIndex, mode.ordinal()));
    }

    public static void sendToggleShopBackpack() {
        if (channel != null) channel.sendToServer(new ServerboundShopAction(5, 0, 0, 0));
    }

    public static void sendToggleShopRs() {
        if (channel != null) channel.sendToServer(new ServerboundShopAction(6, 0, 0, 0));
    }

    public static void sendToggleShopEditorMode() {
        if (channel != null) channel.sendToServer(new ServerboundShopAction(7, 0, 0, 0));
    }

    public static void sendSaveShopEntry(Shop.Mode mode, int index, String itemId, String currencyId, long price, int count, int dailyLimit, int totalLimit, String questText, boolean gacha, String rewardsText) {
        if (channel != null && mode != null) channel.sendToServer(new ServerboundShopEdit(mode.ordinal(), index, itemId, currencyId, price, count, dailyLimit, totalLimit, questText, gacha, rewardsText));
    }

    private static void send(ServerPlayer player, boolean open, boolean hudVisible, boolean equipped, CompoundTag balances) {
        if (channel == null || player == null) return;
        channel.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundSync(open, hudVisible, equipped, balances));
    }

    private static Shop.Mode modeFromOrdinal(int ordinal) {
        Shop.Mode[] modes = Shop.Mode.values();
        if (ordinal < 0 || ordinal >= modes.length) return Shop.Mode.BUY;
        return modes[ordinal];
    }

    private static boolean rejectUnexpectedDirection(
            NetworkEvent.Context context,
            NetworkDirection expected
    ) {
        if (context.getDirection() == expected) return false;
        context.setPacketHandled(true);
        return true;
    }

    /** Called only from enqueued server-thread packet handlers. */
    private static boolean acquireShopActionBudget(ServerPlayer player) {
        long now = player.serverLevel().getGameTime();
        ShopActionWindow window = SHOP_ACTION_WINDOWS.computeIfAbsent(
                player,
                ignored -> new ShopActionWindow(now)
        );
        if (now < window.startedAt || now - window.startedAt >= SHOP_ACTION_WINDOW_TICKS) {
            window.startedAt = now;
            window.actions = 0;
        }
        if (window.actions >= MAX_SHOP_ACTIONS_PER_SECOND) return false;
        window.actions++;
        return true;
    }

    private static final class ShopActionWindow {
        private long startedAt;
        private int actions;

        private ShopActionWindow(long startedAt) {
            this.startedAt = startedAt;
        }
    }

    public record ClientboundSync(boolean open, boolean hudVisible, boolean equipped, CompoundTag balances) {
        public static void encode(ClientboundSync packet, FriendlyByteBuf buffer) {
            buffer.writeBoolean(packet.open);
            buffer.writeBoolean(packet.hudVisible);
            buffer.writeBoolean(packet.equipped);
            buffer.writeNbt(packet.balances);
        }

        public static ClientboundSync decode(FriendlyByteBuf buffer) {
            boolean open = buffer.readBoolean();
            boolean hudVisible = buffer.readBoolean();
            boolean equipped = buffer.readBoolean();
            CompoundTag balances = buffer.readNbt();
            return new ClientboundSync(open, hudVisible, equipped, balances == null ? new CompoundTag() : balances);
        }

        public static void handle(ClientboundSync packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            if (rejectUnexpectedDirection(context, NetworkDirection.PLAY_TO_CLIENT)) return;
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> Client.handleSync(packet.open, packet.hudVisible, packet.equipped, packet.balances)));
            context.setPacketHandled(true);
        }
    }

    public record ClientboundToast(String translationKey) {
        public static void encode(ClientboundToast packet, FriendlyByteBuf buffer) {
            buffer.writeUtf(packet.translationKey == null ? "" : packet.translationKey, 1024);
        }

        public static ClientboundToast decode(FriendlyByteBuf buffer) {
            return new ClientboundToast(buffer.readUtf(1024));
        }

        public static void handle(ClientboundToast packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            if (rejectUnexpectedDirection(context, NetworkDirection.PLAY_TO_CLIENT)) return;
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> Client.showToast(packet.translationKey)));
            context.setPacketHandled(true);
        }
    }

    public record ServerboundAction(int action, String from, String to) {
        public static void encode(ServerboundAction packet, FriendlyByteBuf buffer) {
            buffer.writeVarInt(packet.action);
            buffer.writeUtf(packet.from == null ? "" : packet.from, 256);
            buffer.writeUtf(packet.to == null ? "" : packet.to, 256);
        }

        public static ServerboundAction decode(FriendlyByteBuf buffer) {
            return new ServerboundAction(buffer.readVarInt(), buffer.readUtf(256), buffer.readUtf(256));
        }

        public static void handle(ServerboundAction packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            if (rejectUnexpectedDirection(context, NetworkDirection.PLAY_TO_SERVER)) return;
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) return;
                if (packet.action == 0) Data.depositInventory(player);
                else if (packet.action == 1) Data.withdraw(player, packet.from);
                else if (packet.action == 2) Data.convertAll(player, packet.from, packet.to);
                else if (packet.action == 3) {
                    Network.open(player);
                    return;
                } else if (packet.action == 4) Data.convertOne(player, packet.from, packet.to);
                else if (packet.action == 5) {
                    boolean hidden = Data.toggleHudHidden(player);
                    Network.toast(player, hidden ? "msg.adventuresystems.curios.wallet.hud_off" : "msg.adventuresystems.curios.wallet.hud_on");
                }
                Network.sync(player);
            });
            context.setPacketHandled(true);
        }
    }

    public record ClientboundShopOpen(CompoundTag balances, CompoundTag shop, boolean editorMode) {
        public static void encode(ClientboundShopOpen packet, FriendlyByteBuf buffer) {
            buffer.writeNbt(packet.balances);
            buffer.writeNbt(packet.shop);
            buffer.writeBoolean(packet.editorMode);
        }

        public static ClientboundShopOpen decode(FriendlyByteBuf buffer) {
            CompoundTag balances = buffer.readNbt();
            CompoundTag shop = buffer.readNbt();
            return new ClientboundShopOpen(balances == null ? new CompoundTag() : balances, shop == null ? new CompoundTag() : shop, buffer.readBoolean());
        }

        public static void handle(ClientboundShopOpen packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            if (rejectUnexpectedDirection(context, NetworkDirection.PLAY_TO_CLIENT)) return;
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> Client.openShop(packet.balances, packet.shop, packet.editorMode)));
            context.setPacketHandled(true);
        }
    }

    public record ClientboundShopRefresh(CompoundTag balances, CompoundTag shop, boolean editorMode) {
        public static void encode(ClientboundShopRefresh packet, FriendlyByteBuf buffer) {
            buffer.writeNbt(packet.balances);
            buffer.writeNbt(packet.shop);
            buffer.writeBoolean(packet.editorMode);
        }

        public static ClientboundShopRefresh decode(FriendlyByteBuf buffer) {
            CompoundTag balances = buffer.readNbt();
            CompoundTag shop = buffer.readNbt();
            return new ClientboundShopRefresh(balances == null ? new CompoundTag() : balances, shop == null ? new CompoundTag() : shop, buffer.readBoolean());
        }

        public static void handle(ClientboundShopRefresh packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            if (rejectUnexpectedDirection(context, NetworkDirection.PLAY_TO_CLIENT)) return;
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> Client.refreshShopIfOpen(packet.balances, packet.shop, packet.editorMode)));
            context.setPacketHandled(true);
        }
    }

    public record ServerboundShopAction(int action, int index, int amount, int modeOrdinal) {
        public static void encode(ServerboundShopAction packet, FriendlyByteBuf buffer) {
            buffer.writeVarInt(packet.action);
            buffer.writeVarInt(packet.index);
            buffer.writeVarInt(packet.amount);
            buffer.writeVarInt(packet.modeOrdinal);
        }

        public static ServerboundShopAction decode(FriendlyByteBuf buffer) {
            return new ServerboundShopAction(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
        }

        public static void handle(ServerboundShopAction packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            if (rejectUnexpectedDirection(context, NetworkDirection.PLAY_TO_SERVER)) return;
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null || !acquireShopActionBudget(player)) return;
                if (packet.action == 0) {
                    Network.openShop(player);
                    return;
                }
                if (packet.action == 1) {
                    int packedAmount = Math.max(1, Math.min(65535, packet.amount));
                    Shop.BuyResult result = Shop.buyDetailed(player, packet.index, packedAmount);
                    Network.toast(player, result.messageKey(), result.args());
                    Network.refreshShop(player);
                    return;
                }
                if (packet.action == 2) {
                    int packedAmount = Math.max(1, Math.min(65535, packet.amount));
                    Shop.SellResult result = Shop.sellDetailed(player, packet.index, packedAmount);
                    Network.toast(player, result.noticeKey());
                    Network.refreshShop(player);
                    return;
                }
                if (packet.action == 5) {
                    boolean enabled = Shop.toggleBackpack(player);
                    Network.toast(player, enabled
                            ? "msg.adventuresystems.curios.wallet.shop_backpack_source_on"
                            : "msg.adventuresystems.curios.wallet.shop_backpack_source_off");
                    Network.refreshShop(player);
                    return;
                }
                if (packet.action == 6) {
                    boolean enabled = Shop.toggleRs(player);
                    Network.toast(player, enabled
                            ? "msg.adventuresystems.curios.wallet.shop_rs_source_on"
                            : "msg.adventuresystems.curios.wallet.shop_rs_source_off");
                    Network.refreshShop(player);
                    return;
                }
                if (packet.action == 7) {
                    if (!Shop.canEdit(player)) {
                        Network.toast(player, "msg.adventuresystems.curios.wallet.shop_editor_mode_no_permission");
                        Network.refreshShop(player);
                        return;
                    }
                    boolean enabled = Shop.toggleEditMode(player);
                    Network.toast(player, enabled
                            ? "msg.adventuresystems.curios.wallet.shop_editor_mode_on"
                            : "msg.adventuresystems.curios.wallet.shop_editor_mode_off");
                    Network.refreshShop(player);
                    return;
                }
                if (packet.action == 8) {
                    if (!Shop.canEdit(player)) {
                        Network.toast(player, "msg.adventuresystems.curios.wallet.shop_editor_mode_no_permission");
                        return;
                    }
                    Shop.setEditMode(player, true);
                    channel.send(
                            PacketDistributor.PLAYER.with(() -> player),
                            new ClientboundShopOpen(Data.snapshot(player), Shop.clientTag(player), true)
                    );
                    return;
                }
                boolean changed;
                if (packet.action == 3) {
                    changed = Shop.remove(player, modeFromOrdinal(packet.modeOrdinal), packet.index);
                } else if (packet.action == 4) {
                    changed = Shop.move(player, modeFromOrdinal(packet.modeOrdinal), packet.index, packet.amount);
                } else {
                    changed = false;
                }
                if (changed) {
                    Network.toast(player, packet.action == 4
                            ? "msg.adventuresystems.curios.wallet.shop_move_success"
                            : "msg.adventuresystems.curios.wallet.shop_remove_success");
                    Network.broadcastShopRefresh(player);
                } else {
                    Network.toast(player, "msg.adventuresystems.curios.wallet.shop_action_fail");
                    Network.openShop(player);
                }
            });
            context.setPacketHandled(true);
        }
    }

    public record ServerboundShopEdit(int modeOrdinal, int index, String itemId, String currencyId, long price, int count, int dailyLimit, int totalLimit, String questText, boolean gacha, String rewardsText) {
        public static void encode(ServerboundShopEdit packet, FriendlyByteBuf buffer) {
            buffer.writeVarInt(packet.modeOrdinal);
            buffer.writeVarInt(packet.index);
            buffer.writeUtf(packet.itemId == null ? "" : packet.itemId, 256);
            buffer.writeUtf(packet.currencyId == null ? "" : packet.currencyId, 256);
            buffer.writeVarLong(packet.price);
            buffer.writeVarInt(packet.count);
            buffer.writeVarInt(packet.dailyLimit);
            buffer.writeVarInt(packet.totalLimit);
            buffer.writeUtf(packet.questText == null ? "" : packet.questText, 4096);
            buffer.writeBoolean(packet.gacha);
            buffer.writeUtf(packet.rewardsText == null ? "" : packet.rewardsText, 8192);
        }

        public static ServerboundShopEdit decode(FriendlyByteBuf buffer) {
            return new ServerboundShopEdit(buffer.readVarInt(), buffer.readVarInt(), buffer.readUtf(256), buffer.readUtf(256), buffer.readVarLong(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readUtf(4096), buffer.readBoolean(), buffer.readUtf(8192));
        }

        public static void handle(ServerboundShopEdit packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            if (rejectUnexpectedDirection(context, NetworkDirection.PLAY_TO_SERVER)) return;
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null || !acquireShopActionBudget(player)) return;
                boolean changed = Shop.saveEntry(player, modeFromOrdinal(packet.modeOrdinal), packet.index, packet.itemId, packet.currencyId, packet.price, Math.max(1, Math.min(64, packet.count)), Math.max(0, packet.dailyLimit), Math.max(0, packet.totalLimit), packet.questText, packet.gacha, packet.rewardsText);
                if (changed) {
                    Network.toast(player, "msg.adventuresystems.curios.wallet.shop_save_success");
                    Network.broadcastShopRefresh(player);
                } else {
                    Network.toast(player, "msg.adventuresystems.curios.wallet.shop_save_fail");
                    Network.openShop(player);
                }
            });
            context.setPacketHandled(true);
        }
    }
}
