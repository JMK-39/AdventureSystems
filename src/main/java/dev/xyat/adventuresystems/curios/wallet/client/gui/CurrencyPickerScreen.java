package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.adventuresystems.curios.wallet.data.CurrencyType;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.kineticcore.api.client.gui.input.MouseInput;
import dev.xyat.kineticcore.api.client.gui.input.ScrollInput;
import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.adventuresystems.text.AdventureText;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 货币选择：两列方框，每个方框显示货币图标与名称，点击方框即选择；当前货币橘黄边框，悬停蓝色边框。
 */
final class CurrencyPickerScreen extends KineticPage {
    private static final int PANEL_W = 320;
    private static final int COLUMNS = 2;
    private static final int BOX_GAP = 4;
    private static final int BOX_H = 24;
    private static final int MAX_ROWS = 5;
    private static final int GRID_W = PANEL_W - 20;
    // Title above the grid, Cancel button below it, and the margins around both.
    private static final int TITLE_SPACE = 24;
    private static final int CANCEL_SPACE = 6 + 20 + 10;

    private final ShopGuiSupport.EditorDraft draft;
    private final List<CurrencyType> currencies;
    private int scrollRows;
    // The panel holds as many rows as there are currencies (up to MAX_ROWS) and sits in the middle of the canvas.
    private int panelX, panelY, panelH, gridX, gridY, gridH, cancelY;

    CurrencyPickerScreen(ShopGuiSupport.EditorDraft draft) {
        super(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_currency_picker_title"));
        this.draft = draft;
        this.currencies = new ArrayList<>(Data.currencies());
        this.currencies.sort(Comparator.comparingLong(CurrencyType::value));
        useCanvas(360, 240, 6);
    }

    @Override
    protected void build(KineticUi ui) {
        int rows = Math.max(1, Math.min(MAX_ROWS, (currencies.size() + COLUMNS - 1) / COLUMNS));
        gridH = rows * (BOX_H + BOX_GAP) - BOX_GAP;
        panelH = TITLE_SPACE + gridH + CANCEL_SPACE;
        panelX = (width() - PANEL_W) / 2;
        panelY = Math.max(0, (height() - panelH) / 2);
        gridX = panelX + 10;
        gridY = panelY + TITLE_SPACE;
        cancelY = gridY + gridH + 6;
        Component cancel = AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_cancel");
        // A fixed width in every language; the label scrolls when longer.
        int width = 80;
        ui.button(panelX + (PANEL_W - width) / 2, cancelY, width).text(cancel).onClick(this::navigateBack).build();
    }

    private static int boxWidth() {
        return (GRID_W - BOX_GAP * (COLUMNS - 1)) / COLUMNS;
    }

    private int visibleRows() {
        return Math.max(1, (gridH + BOX_GAP) / (BOX_H + BOX_GAP));
    }

    private int maxScrollRows() {
        int rows = (currencies.size() + COLUMNS - 1) / COLUMNS;
        return Math.max(0, rows - visibleRows());
    }

    private int boxX(int index) {
        return gridX + (index % COLUMNS) * (boxWidth() + BOX_GAP);
    }

    private int boxY(int index) {
        return gridY + (index / COLUMNS - scrollRows) * (BOX_H + BOX_GAP);
    }

    private boolean boxVisible(int index) {
        int row = index / COLUMNS - scrollRows;
        return row >= 0 && row < visibleRows();
    }

    private int boxAt(double mouseX, double mouseY) {
        for (int i = 0; i < currencies.size(); i++) {
            if (boxVisible(i) && KineticTheme.hovering(mouseX, mouseY, boxX(i), boxY(i), boxWidth(), BOX_H)) return i;
        }
        return -1;
    }

    private void selectCurrency(int index) {
        if (index < 0 || index >= currencies.size()) return;
        draft.currencyId = currencies.get(index).itemId();
        navigateBack();
    }

    @Override
    protected void renderBackground(KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        KineticTheme.shadow(graphics, width(), height());
        KineticTheme.panel(graphics, panelX, panelY, PANEL_W, panelH);
        graphics.scrollingTextCentered(title(), panelX + PANEL_W / 2, panelY + 8, PANEL_W - 24, KineticTheme.current().text(), false);
        if (currencies.isEmpty()) {
            graphics.scrollingTextCentered(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_currency_picker_empty"), panelX + PANEL_W / 2, gridY + (gridH - 8) / 2, PANEL_W - 24, KineticTheme.current().text(), false);
            return;
        }
        int hovered = boxAt(mouseX, mouseY);
        for (int i = 0; i < currencies.size(); i++) {
            if (!boxVisible(i)) continue;
            CurrencyType currency = currencies.get(i);
            int x = boxX(i);
            int y = boxY(i);
            boolean selected = Objects.equals(currency.itemId(), draft.currencyId);
            KineticTheme.stateSurface(graphics, x, y, boxWidth(), BOX_H, KineticTheme.Surface.PANEL_ALT,
                    selected, i == hovered, false);
            KineticTheme.itemSlot(graphics, x + 2, y + 2, 20, 4, i == hovered);
            graphics.item(ShopGuiSupport.stack(currency.itemId()), x + 4, y + 4);
            Component name = ShopGuiSupport.stackNameComponent(currency.itemId());
            int textX = x + 24;
            int textWidth = boxWidth() - 28;
            graphics.scrollingText(name, textX, y + 8, textWidth, KineticTheme.current().text(), true);
        }
    }

    @Override
    protected void renderTooltips(int mouseX, int mouseY) {
        int index = boxAt(mouseX, mouseY);
        if (index < 0) return;
        CurrencyType currency = currencies.get(index);
        showTooltip(List.of(
                ShopGuiSupport.stackNameComponent(currency.itemId()),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_currency_value",
                        ShopGuiSupport.formatExact(currency.value())),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_currency_picker_select_tip")));
    }

    @Override
    protected boolean onMouseClick(MouseInput input) {
        if (!input.isLeft()) return false;
        int index = boxAt(input.x(), input.y());
        if (index < 0) return false;
        selectCurrency(index);
        return true;
    }

    @Override
    protected boolean onMouseScroll(ScrollInput input) {
        if (!input.inside(gridX, gridY, GRID_W, gridH) || maxScrollRows() <= 0) return false;
        scrollRows = Math.max(0, Math.min(maxScrollRows(), scrollRows - (int) Math.signum(input.deltaY())));
        return true;
    }
}
