package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.adventuresystems.curios.wallet.data.CurrencyType;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.kineticcore.api.client.overlay.KineticOverlays;
import dev.xyat.kineticcore.api.client.screen.KineticScreen;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.widget.scroll.KineticScroll.GridScrollController;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import dev.xyat.kineticcore.api.text.KineticI18n;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

final class CurrencyPickerScreen extends KineticScreen {
    private static final int VISIBLE_ROWS = 6;
    private static final int ROW_HEIGHT = 22;
    private final Screen parent;
    private final ShopGuiSupport.EditorDraft draft;
    private final List<CurrencyType> currencies;
    private final GridScrollController listScroll = new GridScrollController();

    CurrencyPickerScreen(Screen parent, ShopGuiSupport.EditorDraft draft) {
        super(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_currency_picker_title"));
        this.parent = parent;
        setParentScreen(parent);
        this.draft = draft;
        this.currencies = new ArrayList<>(Data.currencies());
        this.currencies.sort(Comparator.comparingLong(CurrencyType::value));
        useCanvas(360.0F, 240.0F, 6);
        this.listScroll.update(this.currencies.size(), VISIBLE_ROWS);
    }

    @Override
    protected void buildUi() {
        int left = (canvasWidth() - 300) / 2;
        int top = (canvasHeight() - 200) / 2;
        addButton(
                left + 110,
                top + 170,
                80,
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_cancel"),
                null,
                this::returnToParent
        );
    }

    @Override
    protected void renderCanvasBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int left = (canvasWidth() - 300) / 2;
        int top = (canvasHeight() - 200) / 2;
        GuiTheme.panel(graphics, left, top, 300, 200);
        graphics.drawCenteredString(font, title, left + 150, top + 8, GuiTheme.current().text());

        int listX = left + 10;
        int listY = top + 25;
        int listW = 280;
        int listH = 140;
        GuiTheme.surface(graphics, listX, listY, listW, listH, GuiTheme.Surface.PANEL_ALT);

        if (currencies.isEmpty()) {
            graphics.drawCenteredString(
                    font,
                    KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_currency_picker_empty"),
                    left + 150,
                    top + 80,
                    GuiTheme.current().text()
            );
            return;
        }

        listScroll.update(currencies.size(), VISIBLE_ROWS);
        int start = listScroll.smoothIndexOffset();
        int shift = listScroll.visualShift(ROW_HEIGHT);
        enableCanvasScissor(graphics, listX, listY, listX + listW, listY + listH);
        try {
            for (int i = start; i < Math.min(currencies.size(), start + VISIBLE_ROWS + 1); i++) {
                int y = listY + (i - start) * ROW_HEIGHT - shift;
                boolean hover = GuiTheme.hovering(mouseX, mouseY, listX, y, listW, ROW_HEIGHT);
                GuiTheme.stateSurface(
                        graphics,
                        listX + 1,
                        y + 1,
                        listW - 2,
                        ROW_HEIGHT - 2,
                        GuiTheme.Surface.PANEL_ALT,
                        false,
                        hover,
                        false
                );
                CurrencyType currency = currencies.get(i);
                ItemStack stack = ShopGuiSupport.stack(currency.itemId());
                GuiTheme.itemSlot(graphics, listX + 3, y + 2, 18, 4, hover);
                graphics.renderItem(stack, listX + 4, y + 3);
                graphics.drawString(
                        font,
                        ShopGuiSupport.stackNameComponent(currency.itemId()),
                        listX + 26,
                        y + 7,
                        GuiTheme.current().text(),
                        true
                );
            }
        } finally {
            disableCanvasScissor(graphics);
        }
    }

    @Override
    protected void renderTooltips(GuiGraphics graphics, int scaledMouseX, int scaledMouseY, int rawMouseX, int rawMouseY) {
        ItemStack stack = hoveredCurrencyStack(scaledMouseX, scaledMouseY);
        if (!stack.isEmpty()) {
            KineticOverlays.requestItemTooltip(stack, rawMouseX, rawMouseY);
            return;
        }
        List<Component> tooltip = currencyPickerTooltipAt(scaledMouseX, scaledMouseY);
        if (!tooltip.isEmpty()) KineticOverlays.requestTooltip(tooltip, rawMouseX, rawMouseY);
    }

    private ItemStack hoveredCurrencyStack(int mouseX, int mouseY) {
        int left = (canvasWidth() - 300) / 2;
        int top = (canvasHeight() - 200) / 2;
        int listX = left + 10;
        int listY = top + 25;
        int start = listScroll.smoothIndexOffset();
        int shift = listScroll.visualShift(ROW_HEIGHT);
        for (int i = start; i < Math.min(currencies.size(), start + VISIBLE_ROWS + 1); i++) {
            int y = listY + (i - start) * ROW_HEIGHT - shift;
            if (GuiTheme.hovering(mouseX, mouseY, listX + 4, y + 3, 16, 16)) {
                return ShopGuiSupport.stack(currencies.get(i).itemId());
            }
        }
        return ItemStack.EMPTY;
    }

    private List<Component> currencyPickerTooltipAt(int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        int left = (canvasWidth() - 300) / 2;
        int top = (canvasHeight() - 200) / 2;
        int listX = left + 10;
        int listY = top + 25;
        int listW = 280;
        int listH = 140;
        if (!GuiTheme.hovering(mouseX, mouseY, listX, listY, listW, listH)) return tooltip;
        int row = (mouseY - listY + listScroll.visualShift(ROW_HEIGHT)) / ROW_HEIGHT;
        int index = listScroll.smoothIndexOffset() + row;
        if (index < 0 || index >= currencies.size()) return tooltip;
        CurrencyType currency = currencies.get(index);
        tooltip.add(KineticI18n.translatable(
                "gui.adventuresystems.curios.wallet.shop_tooltip_name",
                ShopGuiSupport.stackNameComponent(currency.itemId())
        ));
        tooltip.add(KineticI18n.translatable(
                "gui.adventuresystems.curios.wallet.shop_tooltip_currency_value",
                ShopGuiSupport.formatExact(currency.value())
        ));
        tooltip.add(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_currency_picker_select_tip"));
        return tooltip;
    }

    @Override
    protected boolean canvasMouseClicked(double mouseX, double mouseY, int button) {
        if (super.canvasMouseClicked(mouseX, mouseY, button)) return true;
        int left = (canvasWidth() - 300) / 2;
        int top = (canvasHeight() - 200) / 2;
        int listX = left + 10;
        int listY = top + 25;
        int listW = 280;
        int listH = 140;
        if (!GuiTheme.hovering(mouseX, mouseY, listX, listY, listW, listH)) return false;
        int row = (int) ((mouseY - listY + listScroll.visualShift(ROW_HEIGHT)) / ROW_HEIGHT);
        int index = listScroll.smoothIndexOffset() + row;
        if (index >= 0 && index < currencies.size()) {
            draft.currencyId = currencies.get(index).itemId();
            returnToParent();
            return true;
        }
        return false;
    }

    @Override
    protected boolean canvasMouseScrolled(double mouseX, double mouseY, double delta) {
        listScroll.update(currencies.size(), VISIBLE_ROWS);
        return listScroll.scroll(delta) || super.canvasMouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    protected boolean handleCloseRequest() {
        returnToParent();
        return true;
    }

    private void returnToParent() {
        navigateBack();
    }
}
