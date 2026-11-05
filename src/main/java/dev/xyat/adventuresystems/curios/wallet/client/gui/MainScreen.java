package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.adventuresystems.curios.wallet.client.Client;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.adventuresystems.curios.wallet.network.Network;
import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.widget.KineticButton;
import dev.xyat.kineticcore.api.client.gui.widget.list.KineticRowList;
import dev.xyat.adventuresystems.text.AdventureText;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The wallet: one balance of wallet coins, the shop, and a quick deposit that turns the coin items in the inventory
 * into wallet coins. Below the balance, each coin item the wallet takes in, what it is worth and how many the player
 * carries.
 */
public class MainScreen extends KineticPage {
    private CompoundTag balances;
    private boolean hudCurrencyVisible;
    private final List<String> rows = new ArrayList<>();
    private KineticButton hudButton;
    private WalletRows list;
    private int scrollOffset;
    // The panel is as tall as its rows need (up to MAX_VISIBLE_ROWS, then the list scrolls) and sits in the middle of the canvas.
    private static final int MAX_VISIBLE_ROWS = 6;
    private static final int CARD_TOP = 46;
    private static final int CARD_HEIGHT = 32;
    private static final int LIST_LABEL_TOP = 82;
    private static final int LIST_TOP = 94;
    private static final int PANEL_BOTTOM = 11;
    private int top;
    private int listHeight;

    public MainScreen(CompoundTag balances, boolean hudCurrencyVisible) {
        super(AdventureText.translatable("gui.adventuresystems.curios.wallet.title"));
        useCanvas(450, 300, 6);
        setPausesGame(false);
        this.balances = balances == null ? new CompoundTag() : balances.copy();
        this.hudCurrencyVisible = hudCurrencyVisible;
        rebuildRows();
    }

    public void updateBalances(CompoundTag balances, boolean hudCurrencyVisible) {
        this.balances = balances == null ? new CompoundTag() : balances.copy();
        this.hudCurrencyVisible = hudCurrencyVisible;
        if (hudButton != null) hudButton.setText(hudText());
        List<String> previous = List.copyOf(rows);
        rebuildRows();
        if (list == null) return;
        if (!previous.equals(rows)) rebuild();
        else list.setItems(rows);
    }

    @Override
    protected void build(KineticUi ui) {
        if (list != null) scrollOffset = list.scrollOffset();
        listHeight = visibleRows() * WalletRows.ROW_HEIGHT;
        top = Math.max(0, (height() - (LIST_TOP + listHeight + PANEL_BOTTOM)) / 2);
        ui.button(22, top + 22, 40).text(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop"))
                .onClick(Network::sendOpenShop).build();
        ui.button(66, top + 22, 72).text(AdventureText.translatable("gui.adventuresystems.curios.wallet.quick_deposit"))
                .tooltip(AdventureText.translatable("gui.adventuresystems.curios.wallet.quick_deposit_tip"))
                .onClick(Network::sendDepositAll).build();
        hudButton = ui.button(302, top + 22, 60).text(hudText())
                .tooltip(AdventureText.translatable("gui.adventuresystems.curios.wallet.hud_button_tip"))
                .onClick(Network::sendToggleHudCurrency).build();
        ui.button(366, top + 22, 62).text(AdventureText.translatable("gui.adventuresystems.curios.wallet.close"))
                .onClick(this::close).build();
        list = ui.add(new WalletRows());
        list.setItems(rows);
        list.setScrollOffset(scrollOffset);
    }

    private Component hudText() {
        return AdventureText.translatable(hudCurrencyVisible
                ? "gui.adventuresystems.curios.wallet.hud_visible"
                : "gui.adventuresystems.curios.wallet.hud_hidden");
    }

    /** One row per coin item the server lists with the holdings, cheapest first. */
    private void rebuildRows() {
        rows.clear();
        CompoundTag holdings = Client.holdings();
        List<String> ids = new ArrayList<>(dev.xyat.adventuresystems.data.Nbt.keys(holdings));
        ids.remove(Data.VIRTUAL_ID);
        ids.sort(Comparator.comparingLong((String id) -> value(id)).thenComparing(id -> id));
        rows.addAll(ids);
    }

    private static CompoundTag holding(String id) {
        return Client.holdings().get(id) instanceof CompoundTag data ? data : new CompoundTag();
    }

    private static long value(String id) {
        return Math.max(1L, dev.xyat.adventuresystems.data.Nbt.longValue(holding(id), "value"));
    }

    private int visibleRows() {
        return Math.max(1, Math.min(MAX_VISIBLE_ROWS, rows.size()));
    }

    private int holdingsTicks;

    // The open wallet asks the server for fresh holdings every 2 s; the server answers only this player.
    @Override
    protected void onTick() {
        if (++holdingsTicks >= 40) {
            holdingsTicks = 0;
            Network.requestHoldings();
        }
    }

    /** Wallet coins a quick deposit would add now: every carried coin item at its value. */
    private long depositPreview() {
        long total = 0L;
        for (String id : rows) total = safeAdd(total, safeMultiply(dev.xyat.adventuresystems.data.Nbt.longValue(holding(id), "inventory"), value(id)));
        return total;
    }

    /** Where a coin item is held: each inventory stack, Sophisticated Backpacks, RS storage and the total. */
    private List<Component> holdingLines(String coinId) {
        List<Component> lines = new ArrayList<>();
        CompoundTag data = holding(coinId);
        long inventory = dev.xyat.adventuresystems.data.Nbt.longValue(data, "inventory");
        int[] stacks = data.get("stacks") instanceof net.minecraft.nbt.IntArrayTag array ? array.getAsIntArray() : new int[0];
        if (stacks.length > 1) {
            StringBuilder parts = new StringBuilder();
            for (int i = 0; i < Math.min(stacks.length, 8); i++) parts.append(i == 0 ? "" : " + ").append(stacks[i]);
            if (stacks.length > 8) parts.append(" + …");
            lines.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.holdings_inventory_stacks",
                    ShopGuiSupport.formatExact(inventory), parts.toString()));
        } else {
            lines.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.holdings_inventory", ShopGuiSupport.formatExact(inventory)));
        }
        long total = inventory;
        if (dev.xyat.adventuresystems.data.Nbt.bool(data, "backpack_shown")) {
            long backpack = dev.xyat.adventuresystems.data.Nbt.longValue(data, "backpack");
            lines.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.holdings_backpack", ShopGuiSupport.formatExact(backpack)));
            total = safeAdd(total, backpack);
        }
        switch (dev.xyat.adventuresystems.data.Nbt.string(data, "rs_state")) {
            case "BOUND" -> {
                long rs = dev.xyat.adventuresystems.data.Nbt.longValue(data, "rs");
                lines.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.holdings_rs", ShopGuiSupport.formatExact(rs)));
                total = safeAdd(total, rs);
            }
            case "UNBOUND" -> lines.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.holdings_rs_unbound"));
            case "MISSING" -> lines.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.holdings_rs_missing"));
            default -> { }
        }
        lines.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.holdings_total", ShopGuiSupport.formatExact(total)));
        lines.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.holdings_deposit_hint"));
        return lines;
    }

    private static long safeAdd(long a, long b) {
        return b > 0 && a > Long.MAX_VALUE - b ? Long.MAX_VALUE : a + b;
    }

    private static long safeMultiply(long a, long b) {
        if (a <= 0L || b <= 0L) return 0L;
        return a > Long.MAX_VALUE / b ? Long.MAX_VALUE : a * b;
    }

    private final class WalletRows extends KineticRowList<String> {
        private static final int ROW_HEIGHT = 26;

        private WalletRows() {
            super(22, top + LIST_TOP, 406, listHeight, ROW_HEIGHT);
        }

        @Override
        protected void renderRowBackground(KineticGraphics graphics, int index, int x, int y, int width,
                                           int height, boolean hovered, boolean selected) {
            KineticTheme.panelAlt(graphics, x, y + 1, width, height - 2);
        }

        @Override
        protected void renderRow(KineticGraphics graphics, String coin, int index, int x, int y, int width,
                                 int height, boolean hovered, boolean selected) {
            // A standard item slot, the same size in every add-on.
            int slot = dev.xyat.kineticcore.api.client.gui.theme.KineticSizes.ITEM_SLOT;
            int slotX = x + 3;
            int slotY = y + 2;
            KineticTheme.itemSlot(graphics, slotX, slotY, slot, hovered && mouseX() < slotX + slot);
            KineticTheme.item(graphics, ShopGuiSupport.stack(coin), slotX, slotY, slot, 1.0F, false);
            int countWidth = 96;
            int textWidth = width - 28 - countWidth - 8;
            graphics.scrollingText(ShopGuiSupport.stackNameComponent(coin), x + 28, y + 4, textWidth,
                    KineticTheme.current().text(), false);
            graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.coin_value",
                            ShopGuiSupport.formatExact(value(coin)), ShopGuiSupport.stackNameComponent(Data.VIRTUAL_ID)),
                    x + 28, y + 14, textWidth, KineticTheme.current().mutedText(), false);
            graphics.scrollingTextRight(AdventureText.translatable("gui.adventuresystems.curios.wallet.coin_carried",
                            ShopGuiSupport.formatExact(dev.xyat.adventuresystems.data.Nbt.longValue(holding(coin), "inventory"))),
                    x + width - 6, y + 9, countWidth, KineticTheme.current().text(), false);
        }

        @Override
        protected Component rowTooltip(String coin, int index) {
            var lines = holdingLines(coin);
            var stack = ShopGuiSupport.stack(coin);
            if (stack.isEmpty()) {
                List<Component> all = new ArrayList<>();
                all.add(ShopGuiSupport.stackNameComponent(coin));
                all.addAll(lines);
                MainScreen.this.showTooltip(all);
            } else {
                MainScreen.this.showItemTooltip(stack, lines);
            }
            return null;
        }
    }

    @Override
    protected void renderBackground(KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        KineticTheme.shadow(graphics, width(), height());
        KineticTheme.panel(graphics, 10, top, 430, LIST_TOP + listHeight + PANEL_BOTTOM);
        graphics.scrollingTextCentered(title(), 225, top + 8, 430 - 24, KineticTheme.current().text(), false);

        // The balance: the wallet coin's icon, its name and the exact amount.
        int cardY = top + CARD_TOP;
        KineticTheme.panelAlt(graphics, 22, cardY, 406, CARD_HEIGHT);
        KineticTheme.itemSlot(graphics, 26, cardY + 3, 26, false);
        KineticTheme.item(graphics, ShopGuiSupport.stack(Data.VIRTUAL_ID), 26, cardY + 3, 26, 1.25F, false);
        int textRight = 428 - 4 - 170;
        graphics.scrollingText(ShopGuiSupport.stackNameComponent(Data.VIRTUAL_ID), 58, cardY + 5, textRight - 58,
                KineticTheme.current().mutedText(), false);
        graphics.scrollingText(Component.literal(ShopGuiSupport.formatExact(Data.readAmount(balances, Data.VIRTUAL_ID))),
                58, cardY + 17, textRight - 58, KineticTheme.current().text(), false);
        long preview = depositPreview();
        graphics.scrollingTextRight(AdventureText.translatable(preview > 0L
                        ? "gui.adventuresystems.curios.wallet.deposit_preview" : "gui.adventuresystems.curios.wallet.deposit_none",
                        ShopGuiSupport.formatExact(preview)),
                424, cardY + 12, 166, KineticTheme.current().mutedText(), false);

        graphics.scrollingText(AdventureText.translatable(rows.isEmpty()
                        ? "gui.adventuresystems.curios.wallet.coins_none" : "gui.adventuresystems.curios.wallet.coins_title"),
                24, top + LIST_LABEL_TOP, 400, KineticTheme.current().mutedText(), false);
    }
}
