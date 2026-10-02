package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.adventuresystems.curios.wallet.data.CurrencyType;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.kineticcore.api.client.gui.input.MouseInput;
import dev.xyat.kineticcore.api.client.gui.input.ScrollInput;
import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.text.KineticText;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.text.KineticI18n;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 货币选择：两列方框，每个方框显示货币图标与名称，点击方框即选择；当前货币橘黄边框，悬停蓝色边框。
 */
final class CurrencyPickerScreen extends KineticPage {
    private static final int PANEL_X = 20;
    private static final int PANEL_Y = 20;
    private static final int PANEL_W = 320;
    private static final int PANEL_H = 200;
    private static final int COLUMNS = 2;
    private static final int BOX_GAP = 4;
    private static final int BOX_H = 24;
    private static final int GRID_X = PANEL_X + 10;
    private static final int GRID_Y = PANEL_Y + 24;
    private static final int GRID_W = PANEL_W - 20;
    private static final int CANCEL_Y = PANEL_Y + PANEL_H - 30;
    private static final int GRID_H = CANCEL_Y - 6 - GRID_Y;

    private final ShopGuiSupport.EditorDraft draft;
    private final List<CurrencyType> currencies;
    private int scrollRows;

    CurrencyPickerScreen(ShopGuiSupport.EditorDraft draft) {
        super(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_currency_picker_title"));
        this.draft = draft;
        this.currencies = new ArrayList<>(Data.currencies());
        this.currencies.sort(Comparator.comparingLong(CurrencyType::value));
        useCanvas(360, 240, 6);
    }

    @Override
    protected void build(KineticUi ui) {
        Component cancel = KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_cancel");
        int width = KineticText.width(cancel) + 24;
        ui.button(PANEL_X + (PANEL_W - width) / 2, CANCEL_Y, width).text(cancel).onClick(this::navigateBack).build();
    }

    private static int boxWidth() {
        return (GRID_W - BOX_GAP * (COLUMNS - 1)) / COLUMNS;
    }

    private static int visibleRows() {
        return Math.max(1, (GRID_H + BOX_GAP) / (BOX_H + BOX_GAP));
    }

    private int maxScrollRows() {
        int rows = (currencies.size() + COLUMNS - 1) / COLUMNS;
        return Math.max(0, rows - visibleRows());
    }

    private int boxX(int index) {
        return GRID_X + (index % COLUMNS) * (boxWidth() + BOX_GAP);
    }

    private int boxY(int index) {
        return GRID_Y + (index / COLUMNS - scrollRows) * (BOX_H + BOX_GAP);
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
        KineticTheme.panel(graphics, PANEL_X, PANEL_Y, PANEL_W, PANEL_H);
        graphics.centeredText(title(), PANEL_X + PANEL_W / 2, PANEL_Y + 8, KineticTheme.current().text(), false);
        if (currencies.isEmpty()) {
            graphics.centeredText(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_currency_picker_empty"),
                    PANEL_X + PANEL_W / 2, GRID_Y + GRID_H / 2 - 4, KineticTheme.current().text(), false);
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
            graphics.item(ShopGuiSupport.stack(currency.itemId()), x + 4, y + 4);
            Component name = ShopGuiSupport.stackNameComponent(currency.itemId());
            int textX = x + 24;
            int textWidth = boxWidth() - 28;
            graphics.text(KineticText.ellipsize(name.getString(), textWidth), textX, y + 8, KineticTheme.current().text(), true);
        }
    }

    @Override
    protected void renderTooltips(int mouseX, int mouseY) {
        int index = boxAt(mouseX, mouseY);
        if (index < 0) return;
        CurrencyType currency = currencies.get(index);
        showTooltip(List.of(
                ShopGuiSupport.stackNameComponent(currency.itemId()),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_currency_value",
                        ShopGuiSupport.formatExact(currency.value())),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_currency_picker_select_tip")));
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
        if (!input.inside(GRID_X, GRID_Y, GRID_W, GRID_H) || maxScrollRows() <= 0) return false;
        scrollRows = Math.max(0, Math.min(maxScrollRows(), scrollRows - (int) Math.signum(input.deltaY())));
        return true;
    }
}
