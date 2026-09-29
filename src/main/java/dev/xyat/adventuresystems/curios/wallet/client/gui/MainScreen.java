package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.adventuresystems.curios.wallet.data.CurrencyType;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.adventuresystems.curios.wallet.network.Network;
import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.input.MouseInput;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.widget.KineticButton;
import dev.xyat.kineticcore.api.client.gui.widget.list.KineticRowList;
import dev.xyat.kineticcore.api.text.KineticI18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainScreen extends KineticPage {
    private CompoundTag balances;
    private boolean hudCurrencyVisible;
    private final Set<String> expandedCurrencies = new HashSet<>();
    private final List<Row> rows = new ArrayList<>();
    private final List<KineticButton> rowActions = new ArrayList<>();
    private final List<KineticButton> rowExpanders = new ArrayList<>();
    private KineticButton hudButton;
    private WalletRows list;
    private int scrollOffset;

    public MainScreen(CompoundTag balances, boolean hudCurrencyVisible) {
        super(KineticI18n.translatable("gui.adventuresystems.curios.wallet.title"));
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
        refreshList();
    }

    @Override
    protected void build(KineticUi ui) {
        if (list != null) scrollOffset = list.scrollOffset();
        ui.button(22, 37, 60).text(KineticI18n.translatable("gui.adventuresystems.curios.wallet.deposit_short"))
                .onClick(Network::sendDepositAll).build();
        ui.button(88, 37, 60).text(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop"))
                .onClick(Network::sendOpenShop).build();
        hudButton = ui.button(302, 37, 60).text(hudText())
                .tooltip(KineticI18n.translatable("gui.adventuresystems.curios.wallet.hud_button_tip"))
                .onClick(Network::sendToggleHudCurrency).build();
        ui.button(368, 37, 60).text(KineticI18n.translatable("gui.adventuresystems.curios.wallet.close"))
                .onClick(this::close).build();
        list = ui.add(new WalletRows(22, 68, 406, 206));
        list.setItems(rows);
        list.setScrollOffset(scrollOffset);
        rowActions.clear();
        rowExpanders.clear();
        for (int index = 0; index < rows.size(); index++) {
            Row row = rows.get(index);
            int rowIndex = index;
            if (row.action() == Action.WITHDRAW) {
                rowExpanders.add(ui.button(0, 0, WalletRows.EXPAND_WIDTH).compact()
                        .text(expandLabel(row))
                        .onClick(() -> selectRow(rowIndex))
                        .visible(false).build());
            } else {
                rowExpanders.add(null);
            }
            rowActions.add(ui.button(0, 0, WalletRows.ACTION_WIDTH).compact()
                    .text(actionLabel(row))
                    .tooltip(actionTooltip(row))
                    .onClick(() -> runRowAction(rowIndex))
                    .visible(false).build());
        }
    }

    private Component hudText() {
        return KineticI18n.translatable(hudCurrencyVisible
                ? "gui.adventuresystems.curios.wallet.hud_visible"
                : "gui.adventuresystems.curios.wallet.hud_hidden");
    }

    private void rebuildRows() {
        rows.clear();
        for (CurrencyType currency : Data.currencyMap().values()) {
            rows.add(new Row(currency.itemId(), null, Action.WITHDRAW));
            if (expandedCurrencies.contains(currency.itemId())) {
                for (String[] rule : Data.exchangeRules()) {
                    if (!rule[0].equals(currency.itemId())) continue;
                    rows.add(new Row(rule[0], rule[1], Action.CONVERT_ONE));
                    rows.add(new Row(rule[0], rule[1], Action.CONVERT_ALL));
                }
            }
        }
    }

    private void selectRow(int index) {
        if (index < 0 || index >= rows.size()) return;
        Row row = rows.get(index);
        if (row.action() != Action.WITHDRAW) return;
        if (!expandedCurrencies.add(row.from())) expandedCurrencies.remove(row.from());
        rebuildRows();
        rebuild();
    }

    private void runRowAction(int index) {
        if (index < 0 || index >= rows.size()) return;
        Row row = rows.get(index);
        switch (row.action()) {
            case WITHDRAW -> Network.sendWithdraw(row.from());
            case CONVERT_ONE -> Network.sendConvertOne(row.from(), row.to());
            case CONVERT_ALL -> Network.sendConvertAll(row.from(), row.to());
        }
    }

    private void refreshList() {
        rebuildRows();
        if (list != null) list.setItems(rows);
    }

    private Component expandLabel(Row row) {
        return KineticI18n.translatable(expandedCurrencies.contains(row.from())
                ? "gui.adventuresystems.curios.wallet.collapse" : "gui.adventuresystems.curios.wallet.expand");
    }

    private Component actionLabel(Row row) {
        return KineticI18n.translatable(switch (row.action()) {
            case WITHDRAW -> "gui.adventuresystems.curios.wallet.withdraw_64";
            case CONVERT_ONE -> "gui.adventuresystems.curios.wallet.exchange_one";
            case CONVERT_ALL -> "gui.adventuresystems.curios.wallet.exchange_all";
        });
    }

    private Component actionTooltip(Row row) {
        return KineticI18n.translatable(switch (row.action()) {
            case WITHDRAW -> "gui.adventuresystems.curios.wallet.tooltip_withdraw";
            case CONVERT_ONE -> "gui.adventuresystems.curios.wallet.tooltip_convert_one";
            case CONVERT_ALL -> "gui.adventuresystems.curios.wallet.tooltip_convert_all";
        });
    }

    private Component exchangeRatio(String from, String to) {
        CurrencyType source = Data.currencyMap().get(from);
        CurrencyType target = Data.currencyMap().get(to);
        if (source == null || target == null) return Component.empty();
        long sourceValue = source.value();
        long targetValue = target.value();
        Component fromName = ShopGuiSupport.stackNameComponent(from);
        Component toName = ShopGuiSupport.stackNameComponent(to);
        if (sourceValue == targetValue) {
            return KineticI18n.translatable("gui.adventuresystems.curios.wallet.exchange_ratio",
                    "1", fromName, "1", toName);
        }
        if (targetValue > sourceValue && sourceValue > 0 && targetValue % sourceValue == 0) {
            return KineticI18n.translatable("gui.adventuresystems.curios.wallet.exchange_ratio",
                    Long.toString(targetValue / sourceValue), fromName, "1", toName);
        }
        if (sourceValue > targetValue && targetValue > 0 && sourceValue % targetValue == 0) {
            return KineticI18n.translatable("gui.adventuresystems.curios.wallet.exchange_ratio",
                    "1", fromName, Long.toString(sourceValue / targetValue), toName);
        }
        return KineticI18n.translatable("gui.adventuresystems.curios.wallet.exchange_ratio_invalid");
    }

    private String formatCompact(long value) {
        long safeValue = Math.max(0L, value);
        if (safeValue >= 1_000_000_000_000_000_000L) return String.format(Locale.ROOT, "%dQi+", safeValue / 1_000_000_000_000_000_000L);
        if (safeValue >= 1_000_000_000_000_000L) return String.format(Locale.ROOT, "%dQa+", safeValue / 1_000_000_000_000_000L);
        if (safeValue >= 1_000_000_000_000L) return String.format(Locale.ROOT, "%dT+", safeValue / 1_000_000_000_000L);
        if (safeValue >= 1_000_000_000L) return String.format(Locale.ROOT, "%dB+", safeValue / 1_000_000_000L);
        if (safeValue >= 1_000_000L) return String.format(Locale.ROOT, "%dM+", safeValue / 1_000_000L);
        if (safeValue >= 1_000L) return String.format(Locale.ROOT, "%dK+", safeValue / 1_000L);
        return ShopGuiSupport.formatExact(safeValue);
    }

    private final class WalletRows extends KineticRowList<Row> {
        private static final int ROW_HEIGHT = 28;
        private static final int ACTION_WIDTH = 68;
        private static final int EXPAND_WIDTH = 62;
        private static final int BUTTON_GAP = 4;

        private WalletRows(int x, int y, int width, int height) {
            super(x, y, width, height, ROW_HEIGHT);
        }

        @Override
        protected void renderRowBackground(KineticGraphics graphics, int index, int x, int y, int width,
                                           int height, boolean hovered, boolean selected) {
            KineticTheme.panelAlt(graphics, x, y + 1, width, height - 2);
        }

        @Override
        protected void renderRow(KineticGraphics graphics, Row row, int index, int x, int y, int width,
                                 int height, boolean hovered, boolean selected) {
            int actionX = x + width - ACTION_WIDTH - BUTTON_GAP;
            int expandX = actionX - EXPAND_WIDTH - BUTTON_GAP;
            int slotX = x + 4;
            int slotY = y + 5;
            KineticTheme.itemSlot(graphics, slotX, slotY, 18, hovered && mouseX() < slotX + 18);
            graphics.item(ShopGuiSupport.stack(row.action() == Action.WITHDRAW ? row.from() : row.to()), slotX + 1, slotY + 1);

            boolean currency = row.action() == Action.WITHDRAW;
            int textRight = currency ? expandX : actionX;
            Component name = currency ? ShopGuiSupport.stackNameComponent(row.from())
                    : KineticI18n.translatable("gui.adventuresystems.curios.wallet.exchange_to",
                            ShopGuiSupport.stackNameComponent(row.to()));
            graphics.scrollingText(name, x + 28, y + 3, textRight - x - 34,
                    KineticTheme.current().text(), false);
            Component detail = currency
                    ? KineticI18n.translatable("gui.adventuresystems.curios.wallet.amount_value",
                            formatCompact(Data.readAmount(balances, row.from())))
                    : exchangeRatio(row.from(), row.to());
            graphics.scrollingText(detail, x + 28, y + 15, textRight - x - 34,
                    KineticTheme.current().mutedText(), false);
            boolean fullyVisible = y >= controlY() && y + height <= controlY() + controlHeight();
            KineticButton action = rowActions.get(index);
            action.moveTo(actionX, y + 6);
            action.setControlVisible(fullyVisible);
            KineticButton expander = rowExpanders.get(index);
            if (expander != null) {
                expander.moveTo(expandX, y + 6);
                expander.setControlVisible(fullyVisible);
            }
        }

        @Override
        protected boolean onRowClick(Row row, int index, MouseInput input) {
            if (!input.isLeft()) return false;
            int actionX = controlX() + rowsWidth() - ACTION_WIDTH - BUTTON_GAP;
            int expandX = actionX - EXPAND_WIDTH - BUTTON_GAP;
            if (input.x() >= (row.action() == Action.WITHDRAW ? expandX : actionX)) return false;
            if (row.action() == Action.WITHDRAW) {
                selectRow(index);
                return true;
            }
            return false;
        }

        @Override
        protected Component rowTooltip(Row row, int index) {
            int actionX = controlX() + rowsWidth() - ACTION_WIDTH - BUTTON_GAP;
            int buttonX = row.action() == Action.WITHDRAW ? actionX - EXPAND_WIDTH - BUTTON_GAP : actionX;
            if (mouseX() >= buttonX) return null;
            if (row.action() == Action.WITHDRAW) {
                return KineticI18n.translatable("gui.adventuresystems.curios.wallet.tooltip_exact",
                        ShopGuiSupport.formatExact(Data.readAmount(balances, row.from())));
            }
            return KineticI18n.translatable(row.action() == Action.CONVERT_ONE
                    ? "gui.adventuresystems.curios.wallet.tooltip_convert_one"
                    : "gui.adventuresystems.curios.wallet.tooltip_convert_all");
        }
    }

    @Override
    protected void renderBackground(KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (KineticButton button : rowActions) button.setControlVisible(false);
        for (KineticButton button : rowExpanders) if (button != null) button.setControlVisible(false);
        KineticTheme.shadow(graphics, width(), height());
        KineticTheme.panel(graphics, 10, 15, 430, 270);
        graphics.centeredText(title(), 225, 23, KineticTheme.current().text(), false);
    }

    private enum Action { WITHDRAW, CONVERT_ONE, CONVERT_ALL }

    private record Row(String from, String to, Action action) {
    }
}
