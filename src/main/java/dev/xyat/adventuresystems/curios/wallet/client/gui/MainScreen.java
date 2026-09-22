package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.adventuresystems.curios.wallet.data.CurrencyType;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.adventuresystems.curios.wallet.network.Network;
import dev.xyat.kineticcore.api.client.input.KineticMouseButtons;
import dev.xyat.kineticcore.api.client.overlay.KineticOverlays;
import dev.xyat.kineticcore.api.client.screen.KineticScreen;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.widget.button.KineticButtons.StateButton;
import dev.xyat.kineticcore.api.client.widget.scroll.KineticScroll.GridScrollController;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import dev.xyat.kineticcore.api.text.KineticI18n;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import dev.xyat.kineticcore.api.client.widget.KineticControl;

public class MainScreen extends KineticScreen {
    private static final int PANEL_WIDTH = 430;
    private static final int PANEL_HEIGHT = 270;
    private static final int CURRENCY_ROW_HEIGHT = 40;
    private static final int EXCHANGE_ROW_HEIGHT = 30;
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int SCROLLBAR_MIN_THUMB = 18;

    private CompoundTag balances;
    private int left;
    private int top;
    private final GridScrollController listScroll = new GridScrollController();
    private boolean hudCurrencyVisible;
    private StateButton hudButton;
    private final List<Row> rows = new ArrayList<>();
    private final List<RowButtons> rowButtons = new ArrayList<>();
    private final Set<String> expandedCurrencies = new HashSet<>();

    public MainScreen(CompoundTag balances, boolean hudCurrencyVisible) {
        super(KineticI18n.translatable("gui.adventuresystems.curios.wallet.title"));
        useCanvas(450f, 300f, 6);
        this.balances = balances == null ? new CompoundTag() : balances.copy();
        this.hudCurrencyVisible = hudCurrencyVisible;
        rebuildRows();
    }

    public void updateBalances(CompoundTag balances, boolean hudCurrencyVisible) {
        this.balances = balances == null ? new CompoundTag() : balances.copy();
        this.hudCurrencyVisible = hudCurrencyVisible;
        if (hudButton != null) hudButton.setText(hudText());
        rebuildRows();
        if (rowButtons.size() != rows.size()) rebuildUi();
    }

    @Override
    protected void buildUi() {
        left = (canvasWidth() - PANEL_WIDTH) / 2;
        top = (canvasHeight() - PANEL_HEIGHT) / 2;

        addButton(
                left + 12,
                top + 22,
                60,
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.deposit_short"),
                null,
                Network::sendDepositAll
        );
        addButton(
                left + 78,
                top + 22,
                60,
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop"),
                null,
                Network::sendOpenShop
        );
        hudButton = addButton(
                left + PANEL_WIDTH - 138,
                top + 22,
                60,
                hudText(),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.hud_button_tip"),
                Network::sendToggleHudCurrency
        );
        addButton(
                left + PANEL_WIDTH - 72,
                top + 22,
                60,
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.close"),
                null,
                this::onClose
        );

        rebuildRowButtons();
    }

    private Component hudText() {
        return KineticI18n.translatable(hudCurrencyVisible
                ? "gui.adventuresystems.curios.wallet.hud_visible"
                : "gui.adventuresystems.curios.wallet.hud_hidden");
    }

    private void rebuildRowButtons() {
        rowButtons.clear();
        for (Row row : rows) {
            if (row.exchange()) {
                StateButton once = addCompactButton(
                        0,
                        0,
                        38,
                        KineticI18n.translatable("gui.adventuresystems.curios.wallet.exchange_one"),
                        KineticI18n.translatable("gui.adventuresystems.curios.wallet.tooltip_convert_one"),
                        () -> Network.sendConvertOne(row.from, row.to)
                );
                StateButton all = addCompactButton(
                        0,
                        0,
                        38,
                        KineticI18n.translatable("gui.adventuresystems.curios.wallet.exchange_all"),
                        KineticI18n.translatable("gui.adventuresystems.curios.wallet.tooltip_convert_all"),
                        () -> Network.sendConvertAll(row.from, row.to)
                );
                setControlVisible(once, false);
                setControlEnabled(once, false);
                setControlVisible(all, false);
                setControlEnabled(all, false);
                rowButtons.add(new RowButtons(once, all));
            } else {
                StateButton withdraw = addCompactButton(
                        0,
                        0,
                        62,
                        KineticI18n.translatable("gui.adventuresystems.curios.wallet.withdraw_64"),
                        KineticI18n.translatable("gui.adventuresystems.curios.wallet.tooltip_withdraw"),
                        () -> Network.sendWithdraw(row.from)
                );
                setControlVisible(withdraw, false);
                setControlEnabled(withdraw, false);
                rowButtons.add(new RowButtons(withdraw, null));
            }
        }
        updateActionButtonPositions();
    }

    private void updateActionButtonPositions() {
        int listTop = listTop();
        int listBottom = listBottom();
        int baseX = listLeft();
        int baseWidth = listWidth();
        int y = listTop - (int) Math.round(listScroll.smoothOffset());
        for (int i = 0; i < rows.size() && i < rowButtons.size(); i++) {
            Row row = rows.get(i);
            RowButtons buttons = rowButtons.get(i);
            if (row.exchange()) {
                int childX = baseX + 18;
                int childWidth = baseWidth - 18;
                positionButton(buttons.primary(), oneButtonX(childX, childWidth), y + 5, listTop, listBottom);
                positionButton(buttons.secondary(), allButtonX(childX, childWidth), y + 5, listTop, listBottom);
            } else {
                positionButton(buttons.primary(), baseX + baseWidth - 74, y + 9, listTop, listBottom);
            }
            y += row.height();
        }
    }

    private void positionButton(StateButton button, int x, int y, int listTop, int listBottom) {
        if (button == null) return;
        button.setX(x);
        button.setY(y);
        boolean visible = y >= listTop && y + button.getHeight() <= listBottom;
        setControlVisible(button, visible);
        setControlEnabled(button, visible);
    }

    @Override
    protected void renderCanvasBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        GuiTheme.panel(graphics, left, top, PANEL_WIDTH, PANEL_HEIGHT);
        graphics.drawCenteredString(font, title, left + PANEL_WIDTH / 2, top + 8, GuiTheme.current().text());
        graphics.drawString(
                font,
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.expand_hint"),
                left + 14,
                top + 44,
                GuiTheme.current().mutedText(),
                true
        );
        updateActionButtonPositions();
    }

    @Override
    protected void renderCanvasForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderRows(graphics, mouseX, mouseY);
        renderScrollbar(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltips(GuiGraphics graphics, int scaledMouseX, int scaledMouseY, int rawMouseX, int rawMouseY) {
        renderHover(scaledMouseX, scaledMouseY, rawMouseX, rawMouseY);
    }

    private int listLeft() { return left + 12; }
    private int listTop() { return top + 52; }
    private int listRight() { return left + PANEL_WIDTH - 12; }
    private int listBottom() { return top + PANEL_HEIGHT - 12; }
    private int listWidth() { return listRight() - listLeft(); }

    private void renderRows(GuiGraphics graphics, int mouseX, int mouseY) {
        enableCanvasScissor(graphics, listLeft(), listTop(), listRight(), listBottom());
        try {
            int y = listTop() - (int) Math.round(listScroll.smoothOffset());
            for (Row row : rows) {
                if (y + row.height() >= listTop() && y <= listBottom()) {
                    renderRow(graphics, row, listLeft(), y, listWidth(), mouseX, mouseY);
                }
                y += row.height();
            }
        } finally {
            disableCanvasScissor(graphics);
        }
    }

    private void renderRow(GuiGraphics graphics, Row row, int x, int y, int width, int mouseX, int mouseY) {
        if (row.exchange()) {
            renderExchangeRow(graphics, row, x + 18, y, width - 18, mouseX, mouseY);
        } else {
            renderCurrencyRow(graphics, row, x, y, width, mouseX, mouseY);
        }
    }

    private void renderCurrencyRow(GuiGraphics graphics, Row row, int x, int y, int width, int mouseX, int mouseY) {
        CurrencyType currency = currency(row.from);
        if (currency == null) return;
        boolean hover = GuiTheme.hovering(mouseX, mouseY, x, y, width, CURRENCY_ROW_HEIGHT - 3);
        GuiTheme.stateSurface(
                graphics,
                x,
                y,
                width,
                CURRENCY_ROW_HEIGHT - 3,
                GuiTheme.Surface.PANEL_ALT,
                expandedCurrencies.contains(row.from),
                hover,
                false
        );

        ItemStack stack = stack(row.from);
        GuiTheme.itemSlot(graphics, x + 6, y + 10, 18, 4, hover);
        graphics.renderItem(stack, x + 7, y + 11);

        graphics.drawString(
                font,
                KineticI18n.translatable(expandedCurrencies.contains(row.from)
                        ? "gui.adventuresystems.curios.wallet.expand_arrow_open"
                        : "gui.adventuresystems.curios.wallet.expand_arrow_closed"),
                x + 29,
                y + 9,
                GuiTheme.current().text(),
                true
        );
        graphics.drawString(font, stack.getHoverName(), x + 44, y + 7, GuiTheme.current().text(), true);
        graphics.drawString(
                font,
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.amount_value", formatCompact(amount(row.from))),
                x + 44,
                y + 22,
                GuiTheme.current().text(),
                true
        );
        graphics.drawString(
                font,
                KineticI18n.translatable(expandedCurrencies.contains(row.from)
                        ? "gui.adventuresystems.curios.wallet.click_collapse_short"
                        : "gui.adventuresystems.curios.wallet.click_expand_short"),
                x + width - 135,
                y + 6,
                GuiTheme.current().mutedText(),
                true
        );
    }

    private void renderExchangeRow(GuiGraphics graphics, Row row, int x, int y, int width, int mouseX, int mouseY) {
        boolean hover = GuiTheme.hovering(mouseX, mouseY, x, y, width, EXCHANGE_ROW_HEIGHT - 3);
        GuiTheme.stateSurface(
                graphics,
                x,
                y,
                width,
                EXCHANGE_ROW_HEIGHT - 3,
                GuiTheme.Surface.PANEL_ALT,
                false,
                hover,
                false
        );

        ItemStack fromStack = stack(row.from);
        ItemStack toStack = stack(row.to);
        renderExchangeItem(graphics, fromStack, x + 7, y + 7);
        graphics.drawString(font, KineticI18n.translatable("gui.adventuresystems.curios.wallet.exchange_arrow"), x + 25, y + 10, GuiTheme.current().text(), true);
        renderExchangeItem(graphics, toStack, x + 39, y + 7);
        graphics.drawString(
                font,
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.exchange_to", toStack.getHoverName()),
                x + 61,
                y + 4,
                GuiTheme.current().text(),
                true
        );
        graphics.drawString(font, exchangeRatio(row.from, row.to), x + 61, y + 17, GuiTheme.current().text(), true);
    }

    private void renderExchangeItem(GuiGraphics graphics, ItemStack stack, int x, int y) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(0.82f, 0.82f, 1.0f);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();
    }

    private void renderScrollbar(GuiGraphics graphics, int mouseX, int mouseY) {
        updateScrollRange();
        if (!listScroll.canScroll()) return;
        listScroll.render(
                graphics,
                mouseX,
                mouseY,
                left + PANEL_WIDTH - 8,
                top + 52,
                SCROLLBAR_WIDTH,
                visibleHeight(),
                SCROLLBAR_MIN_THUMB
        );
    }

    private void renderHover(int mouseX, int mouseY, int rawMouseX, int rawMouseY) {
        Row row = rowAt(mouseX, mouseY);
        if (row == null) return;
        List<Component> tooltip = new ArrayList<>();
        if (!row.exchange()) {
            ItemStack stack = stack(row.from);
            CurrencyType currency = currency(row.from);
            tooltip.add(KineticI18n.translatable("gui.adventuresystems.curios.wallet.tooltip_name", stack.getHoverName()));
            tooltip.add(KineticI18n.translatable("gui.adventuresystems.curios.wallet.tooltip_exact", ShopGuiSupport.formatExact(amount(row.from))));
            if (currency != null) {
                tooltip.add(KineticI18n.translatable("gui.adventuresystems.curios.wallet.tooltip_value", ShopGuiSupport.formatExact(currency.value())));
            }
            tooltip.add(KineticI18n.translatable("gui.adventuresystems.curios.wallet.tooltip_expand_click"));
            tooltip.add(KineticI18n.translatable("gui.adventuresystems.curios.wallet.tooltip_withdraw"));
        } else {
            ItemStack fromStack = stack(row.from);
            ItemStack toStack = stack(row.to);
            tooltip.add(KineticI18n.translatable("gui.adventuresystems.curios.wallet.tooltip_convert", fromStack.getHoverName(), toStack.getHoverName()));
            tooltip.add(exchangeRatio(row.from, row.to));
            tooltip.add(KineticI18n.translatable("gui.adventuresystems.curios.wallet.tooltip_convert_one"));
            tooltip.add(KineticI18n.translatable("gui.adventuresystems.curios.wallet.tooltip_convert_all"));
        }
        KineticOverlays.requestTooltip(tooltip, rawMouseX, rawMouseY);
    }

    @Override
    protected boolean canvasMouseClicked(double mouseX, double mouseY, int button) {
        if (super.canvasMouseClicked(mouseX, mouseY, button)) return true;
        if (!KineticMouseButtons.isPrimary(button)) return false;

        updateScrollRange();
        if (listScroll.beginDrag(
                mouseX,
                mouseY,
                left + PANEL_WIDTH - 8,
                top + 52,
                SCROLLBAR_WIDTH,
                visibleHeight(),
                SCROLLBAR_MIN_THUMB,
                3
        )) {
            return true;
        }

        Row row = rowAt((int) mouseX, (int) mouseY);
        if (row == null || row.exchange()) return false;
        if (expandedCurrencies.contains(row.from)) {
            expandedCurrencies.remove(row.from);
        } else {
            expandedCurrencies.add(row.from);
        }
        rebuildRows();
        rebuildUi();
        return true;
    }

    @Override
    protected boolean canvasMouseReleased(double mouseX, double mouseY, int button) {
        if (listScroll.release(button)) return true;
        return super.canvasMouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected boolean canvasMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (listScroll.drag(mouseY, top + 52, visibleHeight(), SCROLLBAR_MIN_THUMB)) {
            updateActionButtonPositions();
            return true;
        }
        return super.canvasMouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected boolean canvasMouseScrolled(double mouseX, double mouseY, double delta) {
        updateScrollRange();
        if (listScroll.scroll(delta, SCROLLBAR_MIN_THUMB)) {
            updateActionButtonPositions();
            return true;
        }
        return super.canvasMouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private Row rowAt(int mouseX, int mouseY) {
        if (!GuiTheme.hovering(mouseX, mouseY, listLeft(), listTop(), listWidth(), visibleHeight())) return null;
        int y = listTop() - (int) Math.round(listScroll.smoothOffset());
        for (Row row : rows) {
            int rowLeft = row.exchange() ? listLeft() + 18 : listLeft();
            if (mouseY >= y && mouseY <= y + row.height() - 3 && mouseX >= rowLeft) return row;
            y += row.height();
        }
        return null;
    }

    private void rebuildRows() {
        rows.clear();
        Map<String, CurrencyType> currencies = Data.currencyMap();
        for (CurrencyType currency : currencies.values()) {
            rows.add(new Row(currency.itemId(), null, CURRENCY_ROW_HEIGHT));
            if (expandedCurrencies.contains(currency.itemId())) {
                for (String[] rule : Data.exchangeRules()) {
                    if (rule[0].equals(currency.itemId())) {
                        rows.add(new Row(rule[0], rule[1], EXCHANGE_ROW_HEIGHT));
                    }
                }
            }
        }
        updateScrollRange();
    }

    private CurrencyType currency(String id) {
        return Data.currencyMap().get(id);
    }

    private long amount(String id) {
        return Data.readAmount(balances, id);
    }

    private ItemStack stack(String id) {
        Item item = KineticRegistries.items().get(KineticResourceIds.parse(id));
        if (item == null) item = net.minecraft.world.item.Items.BARRIER;
        return new ItemStack(item);
    }

    private int visibleHeight() {
        return PANEL_HEIGHT - 64;
    }

    private int contentHeight() {
        int height = 0;
        for (Row row : rows) height += row.height();
        return height;
    }

    private void updateScrollRange() {
        listScroll.update(contentHeight(), visibleHeight());
    }

    private int oneButtonX(int x, int width) {
        return x + width - 88;
    }

    private int allButtonX(int x, int width) {
        return x + width - 44;
    }

    private MutableComponent exchangeRatio(String from, String to) {
        CurrencyType source = currency(from);
        CurrencyType target = currency(to);
        if (source == null || target == null) return Component.empty();

        long sourceValue = source.value();
        long targetValue = target.value();
        ItemStack fromStack = stack(from);
        ItemStack toStack = stack(to);

        if (sourceValue == targetValue) {
            return KineticI18n.translatable(
                    "gui.adventuresystems.curios.wallet.exchange_ratio",
                    "1",
                    fromStack.getHoverName(),
                    "1",
                    toStack.getHoverName()
            );
        }
        if (targetValue > sourceValue && targetValue % sourceValue == 0) {
            return KineticI18n.translatable(
                    "gui.adventuresystems.curios.wallet.exchange_ratio",
                    Long.toString(targetValue / sourceValue),
                    fromStack.getHoverName(),
                    "1",
                    toStack.getHoverName()
            );
        }
        if (sourceValue > targetValue && sourceValue % targetValue == 0) {
            return KineticI18n.translatable(
                    "gui.adventuresystems.curios.wallet.exchange_ratio",
                    "1",
                    fromStack.getHoverName(),
                    Long.toString(sourceValue / targetValue),
                    toStack.getHoverName()
            );
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

    private record Row(String from, String to, int height) {
        boolean exchange() {
            return to != null;
        }
    }

    private record RowButtons(StateButton primary, StateButton secondary) {
    }
    private static boolean isControlVisible(KineticControl control) {
        return control != null && control.isVisible();
    }

    private static boolean isControlEnabled(KineticControl control) {
        return control != null && control.isEnabled();
    }

    private static void setControlVisible(KineticControl control, boolean visible) {
        if (control != null) control.setVisible(visible);
    }

    private static void setControlEnabled(KineticControl control, boolean enabled) {
        if (control != null) control.setEnabled(enabled);
    }

}
