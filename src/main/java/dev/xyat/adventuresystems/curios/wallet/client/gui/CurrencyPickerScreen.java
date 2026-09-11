package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.kineticcore.api.client.overlay.GuiOverlay;
import dev.xyat.adventuresystems.curios.util.ColorText;
import dev.xyat.adventuresystems.curios.wallet.data.CurrencyType;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.screen.KineticScreen;
import dev.xyat.kineticcore.api.client.widget.KineticWidgets.GridScrollController;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
final class CurrencyPickerScreen extends KineticScreen {
    private final Screen parent;
    private final ShopGuiSupport.EditorDraft draft;
    private final List<CurrencyType> currencies;
    private final GridScrollController listScroll = new GridScrollController();

    CurrencyPickerScreen(Screen parent, ShopGuiSupport.EditorDraft draft) {
        super(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_currency_picker_title"));
        this.parent = parent;
        this.draft = draft;
        this.currencies = new ArrayList<>(Data.currencies());
        this.currencies.sort(Comparator.comparingLong(CurrencyType::value));
        useCanvas(
                360f,
                240f,
                6
        );
        this.listScroll.update(this.currencies.size(), 6);
    }

    @Override
    protected void buildUi() {
        int left = (canvasWidth - 300) / 2;
        int top = (canvasHeight - 200) / 2;
        addRenderableWidget(Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_cancel"), b -> Minecraft.getInstance().setScreen(parent)).bounds(left + 110, top + 170, 80, 20).build());
    }

    @Override
    protected void renderCanvasBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int left = (canvasWidth - 300) / 2;
        int top = (canvasHeight - 200) / 2;
        ShopGuiSupport.renderBox(graphics, left, top, 300, 200, ShopGuiSupport.PANEL_BG);
        graphics.drawCenteredString(font, title, left + 150, top + 8, ShopGuiSupport.GOLD);

        int listX = left + 10;
        int listY = top + 25;
        int listW = 280;
        int listH = 140;
        graphics.fill(listX, listY, listX + listW, listY + listH, 0xAA000000);

        if (currencies.isEmpty()) {
            graphics.drawCenteredString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_currency_picker_empty"), left + 150, top + 80, ShopGuiSupport.RED);
            return;
        }

        int visible = 6;
        listScroll.update(currencies.size(), visible);
        int start = listScroll.smoothIndexOffset();
        int shift = listScroll.visualShift(22);
                enableCanvasScissor(graphics, listX, listY, listX + listW, listY + listH);
        try {
for (int i = start; i < Math.min(currencies.size(), start + visible + 1); i++) {
            int y = listY + (i - start) * 22 - shift;
            boolean hover = GuiTheme.hovering(mouseX, mouseY, listX, y, listW, 22);
            graphics.fill(listX + 1, y + 1, listX + listW - 1, y + 21, hover ? ShopGuiSupport.ROW_HOVER : ShopGuiSupport.ROW_BG);
            CurrencyType c = currencies.get(i);
            ItemStack stack = ShopGuiSupport.stack(c.itemId());
            GuiTheme.itemSlot(graphics, stack, listX + 3, y + 2, 18, 4, hover);
            graphics.renderItem(stack, listX + 4, y + 3);
            graphics.drawString(font, ShopGuiSupport.stackNameComponent(c.itemId()), listX + 26, y + 7, ShopGuiSupport.TEXT_WHITE, true);
        }
        } finally {
            graphics.disableScissor();
        }
    }

    @Override
    protected void renderTooltips(GuiGraphics graphics, int scaledMouseX, int scaledMouseY, int rawMouseX, int rawMouseY) {
        ItemStack stack = hoveredCurrencyStack(scaledMouseX, scaledMouseY);
        if (!stack.isEmpty()) {
            GuiOverlay.requestItemTooltip(stack, rawMouseX, rawMouseY);
            return;
        }
        List<Component> tooltip = currencyPickerTooltipAt(scaledMouseX, scaledMouseY);
        if (!tooltip.isEmpty()) GuiOverlay.requestTooltip(tooltip, rawMouseX, rawMouseY);
    }

    private ItemStack hoveredCurrencyStack(int mouseX, int mouseY) {
        int left = (canvasWidth - 300) / 2;
        int top = (canvasHeight - 200) / 2;
        int listX = left + 10;
        int listY = top + 25;
        int start = listScroll.smoothIndexOffset();
        int shift = listScroll.visualShift(22);
        for (int i = start; i < Math.min(currencies.size(), start + 7); i++) {
            int y = listY + (i - start) * 22 - shift;
            if (GuiTheme.hovering(mouseX, mouseY, listX + 4, y + 3, 16, 16)) return ShopGuiSupport.stack(currencies.get(i).itemId());
        }
        return ItemStack.EMPTY;
    }

    private List<Component> currencyPickerTooltipAt(int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        int left = (canvasWidth - 300) / 2;
        int top = (canvasHeight - 200) / 2;
        int listX = left + 10;
        int listY = top + 25;
        int listW = 280;
        int listH = 140;
        if (!GuiTheme.hovering(mouseX, mouseY, listX, listY, listW, listH)) return tooltip;
        int row = (mouseY - listY + listScroll.visualShift(22)) / 22;
        int index = listScroll.smoothIndexOffset() + row;
        if (index < 0 || index >= currencies.size()) return tooltip;
        CurrencyType currency = currencies.get(index);
        tooltip.add(ShopGuiSupport.stackNameComponent(currency.itemId()).copy().withStyle(ChatFormatting.GOLD));
        tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_currency_value", Component.literal(ShopGuiSupport.formatExact(currency.value())).withStyle(ChatFormatting.AQUA)));
        tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_currency_picker_select_tip"));
        return tooltip;
    }

    @Override
    protected boolean canvasMouseClicked(double mouseX, double mouseY, int button) {
        if (super.canvasMouseClicked(mouseX, mouseY, button)) return true;
        int left = (canvasWidth - 300) / 2;
        int top = (canvasHeight - 200) / 2;
        int listX = left + 10;
        int listY = top + 25;
        int listW = 280;
        int listH = 140;
        if (!GuiTheme.hovering(mouseX, mouseY, listX, listY, listW, listH)) return false;
        int row = (int) ((mouseY - listY + listScroll.visualShift(22)) / 22);
        int index = listScroll.smoothIndexOffset() + row;
        if (index >= 0 && index < currencies.size()) {
            draft.currencyId = currencies.get(index).itemId();
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        return false;
    }

    @Override
    protected boolean canvasMouseScrolled(double mouseX, double mouseY, double delta) {
        listScroll.update(currencies.size(), 6);
        return listScroll.scroll(delta)
                || super.canvasMouseScrolled(mouseX, mouseY, delta);
    }
}

