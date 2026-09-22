package dev.xyat.adventuresystems.curios.wallet.network;

import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.adventuresystems.curios.wallet.client.Client;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.adventuresystems.curios.wallet.shop.Shop;
import dev.xyat.kineticcore.api.network.NetworkCodec;
import dev.xyat.kineticcore.api.network.NetworkVersionPolicy;
import dev.xyat.kineticcore.api.network.PacketChannel;
import dev.xyat.kineticcore.api.network.ServerPacketContext;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.WeakHashMap;

public final class Network {
    private static final int MAX_SHOP_ACTIONS_PER_SECOND = 8;
    private static final long SHOP_ACTION_WINDOW_TICKS = 20L;
    private static final Map<ServerPlayer, ShopActionWindow> SHOP_ACTION_WINDOWS = new WeakHashMap<>();
    private static final PacketChannel CHANNEL = PacketChannel.create(
            KineticResourceIds.of(CuriosModule.MODID, "currency_wallet"),
            "1",
            NetworkVersionPolicy.ANY
    );
    private static boolean registered;

    private Network() {
    }

    public static void register() {
        if (registered) return;
        registered = true;

        CHANNEL.registerClientbound(0, ClientboundSync.class,
                NetworkCodec.of(
                        (buffer, packet) -> {
                            buffer.writeBoolean(packet.open());
                            buffer.writeBoolean(packet.hudVisible());
                            buffer.writeBoolean(packet.equipped());
                            buffer.writeNbt(packet.balances());
                        },
                        buffer -> new ClientboundSync(
                                buffer.readBoolean(),
                                buffer.readBoolean(),
                                buffer.readBoolean(),
                                safeNbt(buffer.readNbt())
                        )
                ),
                packet -> Client.handleSync(packet.open(), packet.hudVisible(), packet.equipped(), packet.balances()));

        CHANNEL.registerClientbound(1, ClientboundToast.class,
                NetworkCodec.of(
                        (buffer, packet) -> buffer.writeUtf(packet.translationKey() == null ? "" : packet.translationKey(), 1024),
                        buffer -> new ClientboundToast(buffer.readUtf(1024))
                ),
                packet -> Client.showToast(packet.translationKey()));

        CHANNEL.registerServerbound(2, ServerboundAction.class,
                NetworkCodec.of(
                        (buffer, packet) -> {
                            buffer.writeVarInt(packet.action());
                            buffer.writeUtf(packet.from() == null ? "" : packet.from(), 256);
                            buffer.writeUtf(packet.to() == null ? "" : packet.to(), 256);
                        },
                        buffer -> new ServerboundAction(buffer.readVarInt(), buffer.readUtf(256), buffer.readUtf(256))
                ),
                Network::handleAction);

        CHANNEL.registerClientbound(3, ClientboundShopOpen.class,
                NetworkCodec.of(
                        (buffer, packet) -> {
                            buffer.writeNbt(packet.balances());
                            buffer.writeNbt(packet.shop());
                            buffer.writeBoolean(packet.editorMode());
                        },
                        buffer -> new ClientboundShopOpen(
                                safeNbt(buffer.readNbt()),
                                safeNbt(buffer.readNbt()),
                                buffer.readBoolean()
                        )
                ),
                packet -> Client.openShop(packet.balances(), packet.shop(), packet.editorMode()));

        CHANNEL.registerClientbound(4, ClientboundShopRefresh.class,
                NetworkCodec.of(
                        (buffer, packet) -> {
                            buffer.writeNbt(packet.balances());
                            buffer.writeNbt(packet.shop());
                            buffer.writeBoolean(packet.editorMode());
                        },
                        buffer -> new ClientboundShopRefresh(
                                safeNbt(buffer.readNbt()),
                                safeNbt(buffer.readNbt()),
                                buffer.readBoolean()
                        )
                ),
                packet -> Client.refreshShopIfOpen(packet.balances(), packet.shop(), packet.editorMode()));

        CHANNEL.registerServerbound(5, ServerboundShopAction.class,
                NetworkCodec.of(
                        (buffer, packet) -> {
                            buffer.writeVarInt(packet.action());
                            buffer.writeVarInt(packet.index());
                            buffer.writeVarInt(packet.amount());
                            buffer.writeVarInt(packet.modeOrdinal());
                        },
                        buffer -> new ServerboundShopAction(
                                buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt()
                        )
                ),
                Network::handleShopAction);

        CHANNEL.registerServerbound(6, ServerboundShopEdit.class,
                NetworkCodec.of(
                        (buffer, packet) -> {
                            buffer.writeVarInt(packet.modeOrdinal());
                            buffer.writeVarInt(packet.index());
                            buffer.writeUtf(packet.itemId() == null ? "" : packet.itemId(), 256);
                            buffer.writeUtf(packet.currencyId() == null ? "" : packet.currencyId(), 256);
                            buffer.writeVarLong(packet.price());
                            buffer.writeVarInt(packet.count());
                            buffer.writeVarInt(packet.dailyLimit());
                            buffer.writeVarInt(packet.totalLimit());
                            buffer.writeUtf(packet.questText() == null ? "" : packet.questText(), 4096);
                            buffer.writeBoolean(packet.gacha());
                            buffer.writeUtf(packet.rewardsText() == null ? "" : packet.rewardsText(), 8192);
                        },
                        buffer -> new ServerboundShopEdit(
                                buffer.readVarInt(),
                                buffer.readVarInt(),
                                buffer.readUtf(256),
                                buffer.readUtf(256),
                                buffer.readVarLong(),
                                buffer.readVarInt(),
                                buffer.readVarInt(),
                                buffer.readVarInt(),
                                buffer.readUtf(4096),
                                buffer.readBoolean(),
                                buffer.readUtf(8192)
                        )
                ),
                Network::handleShopEdit);
    }

    public static void open(ServerPlayer player) {
        if (!Data.hasWallet(player)) return;
        send(player, true, Data.isHudVisible(player), true, Data.snapshot(player));
    }

    public static void openShop(ServerPlayer player) {
        if (player == null || !Data.hasWallet(player)) return;
        CHANNEL.sendToPlayer(player, new ClientboundShopOpen(Data.snapshot(player), Shop.clientTag(player), Shop.isEditMode(player)));
    }

    public static void refreshShop(ServerPlayer player) {
        if (player == null || !Data.hasWallet(player)) return;
        CHANNEL.sendToPlayer(player, new ClientboundShopRefresh(Data.snapshot(player), Shop.clientTag(player), Shop.isEditMode(player)));
    }

    public static void broadcastShopRefresh(ServerPlayer source) {
        if (source == null) return;
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
        if (player == null || translationKey == null || translationKey.isBlank()) return;
        CHANNEL.sendToPlayer(player, new ClientboundToast(packToast(translationKey, args)));
    }

    private static String packToast(String translationKey, String... args) {
        StringBuilder builder = new StringBuilder(translationKey);
        if (args != null) {
            for (String arg : args) builder.append("|").append(arg == null ? "" : arg.replace("|", "/"));
        }
        return builder.toString();
    }

    public static void sendDepositAll() {
        CHANNEL.sendToServer(new ServerboundAction(0, "", ""));
    }

    public static void sendWithdraw(String currencyId) {
        CHANNEL.sendToServer(new ServerboundAction(1, currencyId, ""));
    }

    public static void sendConvertAll(String from, String to) {
        CHANNEL.sendToServer(new ServerboundAction(2, from, to));
    }

    public static void sendOpen() {
        CHANNEL.sendToServer(new ServerboundAction(3, "", ""));
    }

    public static void sendConvertOne(String from, String to) {
        CHANNEL.sendToServer(new ServerboundAction(4, from, to));
    }

    public static void sendToggleHudCurrency() {
        CHANNEL.sendToServer(new ServerboundAction(5, "", ""));
    }

    public static void sendOpenShop() {
        CHANNEL.sendToServer(new ServerboundShopAction(0, 0, 1, 0));
    }

    public static void sendOpenShopEditor() {
        CHANNEL.sendToServer(new ServerboundShopAction(8, 0, 0, 0));
    }

    public static void sendShopBuy(int index, int amount) {
        CHANNEL.sendToServer(new ServerboundShopAction(1, index, amount, 0));
    }

    public static void sendShopSell(int index, int amount) {
        CHANNEL.sendToServer(new ServerboundShopAction(2, index, amount, 0));
    }

    public static void sendRemoveShopEntry(Shop.Mode mode, int index) {
        if (mode != null) CHANNEL.sendToServer(new ServerboundShopAction(3, index, 1, mode.ordinal()));
    }

    public static void sendMoveShopEntry(Shop.Mode mode, int fromIndex, int toIndex) {
        if (mode != null) CHANNEL.sendToServer(new ServerboundShopAction(4, fromIndex, toIndex, mode.ordinal()));
    }

    public static void sendToggleShopBackpack() {
        CHANNEL.sendToServer(new ServerboundShopAction(5, 0, 0, 0));
    }

    public static void sendToggleShopRs() {
        CHANNEL.sendToServer(new ServerboundShopAction(6, 0, 0, 0));
    }

    public static void sendToggleShopEditorMode() {
        CHANNEL.sendToServer(new ServerboundShopAction(7, 0, 0, 0));
    }

    public static void sendSaveShopEntry(
            Shop.Mode mode,
            int index,
            String itemId,
            String currencyId,
            long price,
            int count,
            int dailyLimit,
            int totalLimit,
            String questText,
            boolean gacha,
            String rewardsText
    ) {
        if (mode != null) {
            CHANNEL.sendToServer(new ServerboundShopEdit(
                    mode.ordinal(), index, itemId, currencyId, price, count, dailyLimit, totalLimit, questText, gacha, rewardsText
            ));
        }
    }

    private static void send(ServerPlayer player, boolean open, boolean hudVisible, boolean equipped, CompoundTag balances) {
        if (player == null) return;
        CHANNEL.sendToPlayer(player, new ClientboundSync(open, hudVisible, equipped, balances));
    }

    private static CompoundTag safeNbt(CompoundTag tag) {
        return tag == null ? new CompoundTag() : tag;
    }

    private static Shop.Mode modeFromOrdinal(int ordinal) {
        Shop.Mode[] modes = Shop.Mode.values();
        if (ordinal < 0 || ordinal >= modes.length) return Shop.Mode.BUY;
        return modes[ordinal];
    }

    private static boolean rejectShopActionBudget(ServerPlayer player) {
        long now = player.serverLevel().getGameTime();
        ShopActionWindow window = SHOP_ACTION_WINDOWS.computeIfAbsent(player, ignored -> new ShopActionWindow(now));
        if (now < window.startedAt || now - window.startedAt >= SHOP_ACTION_WINDOW_TICKS) {
            window.startedAt = now;
            window.actions = 0;
        }
        if (window.actions >= MAX_SHOP_ACTIONS_PER_SECOND) return true;
        window.actions++;
        return false;
    }

    private static void handleAction(ServerboundAction packet, ServerPacketContext context) {
        ServerPlayer player = context.sender();
        if (packet.action() == 0) Data.depositInventory(player);
        else if (packet.action() == 1) Data.withdraw(player, packet.from());
        else if (packet.action() == 2) Data.convertAll(player, packet.from(), packet.to());
        else if (packet.action() == 3) {
            open(player);
            return;
        } else if (packet.action() == 4) Data.convertOne(player, packet.from(), packet.to());
        else if (packet.action() == 5) {
            boolean hidden = Data.toggleHudHidden(player);
            toast(player, hidden ? "msg.adventuresystems.curios.wallet.hud_off" : "msg.adventuresystems.curios.wallet.hud_on");
        }
        sync(player);
    }

    private static void handleShopAction(ServerboundShopAction packet, ServerPacketContext context) {
        ServerPlayer player = context.sender();
        if (rejectShopActionBudget(player)) return;
        if (packet.action() == 0) {
            openShop(player);
            return;
        }
        if (packet.action() == 1) {
            int packedAmount = Math.max(1, Math.min(65535, packet.amount()));
            Shop.BuyResult result = Shop.buyDetailed(player, packet.index(), packedAmount);
            toast(player, result.messageKey(), result.args());
            refreshShop(player);
            return;
        }
        if (packet.action() == 2) {
            int packedAmount = Math.max(1, Math.min(65535, packet.amount()));
            Shop.SellResult result = Shop.sellDetailed(player, packet.index(), packedAmount);
            toast(player, result.noticeKey());
            refreshShop(player);
            return;
        }
        if (packet.action() == 5) {
            boolean enabled = Shop.toggleBackpack(player);
            toast(player, enabled
                    ? "msg.adventuresystems.curios.wallet.shop_backpack_source_on"
                    : "msg.adventuresystems.curios.wallet.shop_backpack_source_off");
            refreshShop(player);
            return;
        }
        if (packet.action() == 6) {
            boolean enabled = Shop.toggleRs(player);
            toast(player, enabled
                    ? "msg.adventuresystems.curios.wallet.shop_rs_source_on"
                    : "msg.adventuresystems.curios.wallet.shop_rs_source_off");
            refreshShop(player);
            return;
        }
        if (packet.action() == 7) {
            if (!Shop.canEdit(player)) {
                toast(player, "msg.adventuresystems.curios.wallet.shop_editor_mode_no_permission");
                refreshShop(player);
                return;
            }
            boolean enabled = Shop.toggleEditMode(player);
            toast(player, enabled
                    ? "msg.adventuresystems.curios.wallet.shop_editor_mode_on"
                    : "msg.adventuresystems.curios.wallet.shop_editor_mode_off");
            refreshShop(player);
            return;
        }
        if (packet.action() == 8) {
            if (!Shop.canEdit(player)) {
                toast(player, "msg.adventuresystems.curios.wallet.shop_editor_mode_no_permission");
                return;
            }
            Shop.setEditMode(player, true);
            CHANNEL.sendToPlayer(player, new ClientboundShopOpen(Data.snapshot(player), Shop.clientTag(player), true));
            return;
        }

        boolean changed;
        if (packet.action() == 3) {
            changed = Shop.remove(player, modeFromOrdinal(packet.modeOrdinal()), packet.index());
        } else if (packet.action() == 4) {
            changed = Shop.move(player, modeFromOrdinal(packet.modeOrdinal()), packet.index(), packet.amount());
        } else {
            changed = false;
        }

        if (changed) {
            toast(player, packet.action() == 4
                    ? "msg.adventuresystems.curios.wallet.shop_move_success"
                    : "msg.adventuresystems.curios.wallet.shop_remove_success");
            broadcastShopRefresh(player);
        } else {
            toast(player, "msg.adventuresystems.curios.wallet.shop_action_fail");
            openShop(player);
        }
    }

    private static void handleShopEdit(ServerboundShopEdit packet, ServerPacketContext context) {
        ServerPlayer player = context.sender();
        if (rejectShopActionBudget(player)) return;
        boolean changed = Shop.saveEntry(
                player,
                modeFromOrdinal(packet.modeOrdinal()),
                packet.index(),
                packet.itemId(),
                packet.currencyId(),
                packet.price(),
                Math.max(1, Math.min(64, packet.count())),
                Math.max(0, packet.dailyLimit()),
                Math.max(0, packet.totalLimit()),
                packet.questText(),
                packet.gacha(),
                packet.rewardsText()
        );
        if (changed) {
            toast(player, "msg.adventuresystems.curios.wallet.shop_save_success");
            broadcastShopRefresh(player);
        } else {
            toast(player, "msg.adventuresystems.curios.wallet.shop_save_fail");
            openShop(player);
        }
    }

    private static final class ShopActionWindow {
        private long startedAt;
        private int actions;

        private ShopActionWindow(long startedAt) {
            this.startedAt = startedAt;
        }
    }

    public record ClientboundSync(boolean open, boolean hudVisible, boolean equipped, CompoundTag balances) {
    }

    public record ClientboundToast(String translationKey) {
    }

    public record ServerboundAction(int action, String from, String to) {
    }

    public record ClientboundShopOpen(CompoundTag balances, CompoundTag shop, boolean editorMode) {
    }

    public record ClientboundShopRefresh(CompoundTag balances, CompoundTag shop, boolean editorMode) {
    }

    public record ServerboundShopAction(int action, int index, int amount, int modeOrdinal) {
    }

    public record ServerboundShopEdit(
            int modeOrdinal,
            int index,
            String itemId,
            String currencyId,
            long price,
            int count,
            int dailyLimit,
            int totalLimit,
            String questText,
            boolean gacha,
            String rewardsText
    ) {
    }
}
