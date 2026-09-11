package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.kineticcore.api.client.overlay.GuiOverlay;
import dev.xyat.adventuresystems.curios.util.ColorText;
import dev.xyat.adventuresystems.curios.wallet.data.CurrencyType;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.adventuresystems.curios.wallet.network.Network;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.screen.KineticScreen;
import dev.xyat.kineticcore.api.client.widget.KineticWidgets.GridScrollController;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class MainScreen extends KineticScreen {
    private static final int PANEL_WIDTH = 430;
    private static final int PANEL_HEIGHT = 270;
    private static final int CURRENCY_ROW_HEIGHT = 40;
    private static final int EXCHANGE_ROW_HEIGHT = 30;
    private static final int PANEL_BG = 0xF0101010;
    private static final int PANEL_INNER = 0xF01A1A1A;
    private static final int GOLD = 0xFFFFAA00;
    private static final int GOLD_DARK = 0xFF8A5A00;
    private static final int CYAN = 0xFFBBBBBB;
    private static final int CYAN_DARK = 0xFF444444;
    private static final int GREEN = 0xFF55FF55;
    private static final int GREEN_DARK = 0xFF2F8B2F;
    private static final int YELLOW = 0xFFFFFF55;
    private static final int YELLOW_DARK = 0xFF9F8A00;
    private static final int HINT = 0xFFB7FF55;
    private static final int ROW_BG = 0xEE171717;
    private static final int ROW_HOVER = 0xEE252525;
    private static final int CHILD_BG = 0xEE111111;
    private static final int CHILD_HOVER = 0xEE1A2026;
    private static final int BUTTON_BG = 0xEE353535;
    private static final int BUTTON_HOVER = 0xEE454545;
    private static final int TEXT_WHITE = 0xFFFFFFFF;
    private static final int TEXT_GOLD = 0xFFFFD35A;
    private static final int TEXT_AQUA = 0xFF55FFFF;
    private static final int TEXT_NUMBER = 0xFF55FF55;
    private static final int TEXT_NUMBER_ALT = 0xFFFFFF55;
    private static final int TEXT_PINK = 0xFFFF55FF;

    private CompoundTag balances;
    private int left;
    private int top;
    private final GridScrollController listScroll =
            new GridScrollController();
    private boolean hudCurrencyVisible;
    private Button hudButton;
    private final List<Row> rows = new ArrayList<>();
    private final Set<String> expandedCurrencies = new HashSet<>();

    public MainScreen(CompoundTag balances, boolean hudCurrencyVisible) {
        super(ColorText.translatable("gui.adventuresystems.curios.wallet.title"));
        useCanvas(
                450f,
                300f,
                6
        );
        this.balances = balances == null ? new CompoundTag() : balances.copy();
        this.hudCurrencyVisible = hudCurrencyVisible;
        if (hudButton != null) hudButton.setMessage(hudText());
        rebuildRows();
    }

    public void updateBalances(CompoundTag balances, boolean hudCurrencyVisible) {
        this.balances = balances == null ? new CompoundTag() : balances.copy();
        this.hudCurrencyVisible = hudCurrencyVisible;
        if (hudButton != null) hudButton.setMessage(hudText());
        rebuildRows();
    }

    @Override
    protected void buildUi() {
        this.left = (this.canvasWidth - PANEL_WIDTH) / 2;
        this.top = (this.canvasHeight - PANEL_HEIGHT) / 2;
        addRenderableWidget(Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.deposit_short"), button -> Network.sendDepositAll())
                .bounds(left + 12, top + 22, 60, 20)
                .build());
        addRenderableWidget(Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.shop"), button -> Network.sendOpenShop())
                .bounds(left + 78, top + 22, 60, 20)
                .build());
        hudButton = addRenderableWidget(Button.builder(hudText(), button -> Network.sendToggleHudCurrency())
                .bounds(left + PANEL_WIDTH - 138, top + 22, 60, 20)
                .build());
        addRenderableWidget(Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.close"), button -> onClose())
                .bounds(left + PANEL_WIDTH - 72, top + 22, 60, 20)
                .build());
    }

    private Component hudText() {
        return ColorText.translatable(hudCurrencyVisible ? "gui.adventuresystems.curios.wallet.hud_visible" : "gui.adventuresystems.curios.wallet.hud_hidden");
    }

    @Override
    protected void renderCanvasBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderPanel(graphics);
        graphics.drawCenteredString(font, title, left + PANEL_WIDTH / 2, top + 8, GOLD);
        drawHintText(graphics, ColorText.translatable("gui.adventuresystems.curios.wallet.expand_hint"), left + 14, top + 44);
    }

    @Override
    protected void renderCanvasForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderRows(graphics, mouseX, mouseY);
        renderScrollbar(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltips(GuiGraphics graphics, int scaledMouseX, int scaledMouseY, int rawMouseX, int rawMouseY) {
        renderHover(graphics, scaledMouseX, scaledMouseY, rawMouseX, rawMouseY);
    }

    private void renderPanel(GuiGraphics graphics) {
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, GOLD_DARK);
        graphics.fill(left + 1, top + 1, left + PANEL_WIDTH - 1, top + PANEL_HEIGHT - 1, GOLD);
        graphics.fill(left + 2, top + 2, left + PANEL_WIDTH - 2, top + PANEL_HEIGHT - 2, PANEL_BG);
        graphics.fill(left + 6, top + 18, left + PANEL_WIDTH - 6, top + PANEL_HEIGHT - 6, PANEL_INNER);
    }

    private void renderRows(GuiGraphics graphics, int mouseX, int mouseY) {
        int listLeft = left + 12;
        int listTop = top + 52;
        int listRight = left + PANEL_WIDTH - 12;
        int listBottom = top + PANEL_HEIGHT - 12;
        enableCanvasScissor(graphics, listLeft, listTop, listRight, listBottom);
        int y = listTop - (int) Math.round(listScroll.smoothOffset());
        for (Row row : rows) {
            if (y + row.height() >= listTop && y <= listBottom) {
                renderRow(graphics, row, listLeft, y, listRight - listLeft, mouseX, mouseY);
            }
            y += row.height();
        }
        graphics.disableScissor();
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

        boolean hover = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + CURRENCY_ROW_HEIGHT - 3;
        int bg = hover ? ROW_HOVER : ROW_BG;

        graphics.fill(x, y, x + width, y + CURRENCY_ROW_HEIGHT - 3, GOLD_DARK);
        graphics.fill(x + 1, y + 1, x + width - 1, y + CURRENCY_ROW_HEIGHT - 4, GOLD);
        graphics.fill(x + 2, y + 2, x + width - 2, y + CURRENCY_ROW_HEIGHT - 5, bg);

        ItemStack stack = stack(row.from);
        GuiTheme.itemSlot(graphics, stack, x + 6, y + 10, 18, 4, hover);
        graphics.renderItem(stack, x + 7, y + 11);

        Component arrow = Component.literal(expandedCurrencies.contains(row.from) ? "▼" : "▶").withStyle(ChatFormatting.GOLD);
        graphics.drawString(font, arrow, x + 29, y + 9, TEXT_GOLD, true);

        Component name = stack.getHoverName();
        graphics.drawString(font, name, x + 44, y + 7, TEXT_WHITE, true);
        drawAmountValue(graphics, ColorText.translatable("gui.adventuresystems.curios.wallet.amount_label"), formatCompact(amount(row.from)), x + 44, y + 22);

        drawClickHint(graphics, ColorText.translatable(expandedCurrencies.contains(row.from) ? "gui.adventuresystems.curios.wallet.click_collapse_short" : "gui.adventuresystems.curios.wallet.click_expand_short"), x + width - 135, y + 6);

        int buttonX = x + width - 74;
        boolean buttonHover = mouseX >= buttonX && mouseX <= buttonX + 62 && mouseY >= y + 9 && mouseY <= y + 29;
        renderWithdrawButton(graphics, buttonX, y + 9, buttonHover, ColorText.translatable("gui.adventuresystems.curios.wallet.withdraw_64"));
    }

    private void renderExchangeRow(GuiGraphics graphics, Row row, int x, int y, int width, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + EXCHANGE_ROW_HEIGHT - 3;
        int bg = hover ? CHILD_HOVER : CHILD_BG;

        graphics.fill(x - 10, y - 2, x - 7, y + EXCHANGE_ROW_HEIGHT - 2, CYAN_DARK);
        graphics.fill(x - 10, y + EXCHANGE_ROW_HEIGHT / 2, x - 1, y + EXCHANGE_ROW_HEIGHT / 2 + 2, CYAN_DARK);

        graphics.fill(x, y, x + width, y + EXCHANGE_ROW_HEIGHT - 3, CYAN_DARK);
        graphics.fill(x + 1, y + 1, x + width - 1, y + EXCHANGE_ROW_HEIGHT - 4, CYAN);
        graphics.fill(x + 2, y + 2, x + width - 2, y + EXCHANGE_ROW_HEIGHT - 5, bg);

        ItemStack fromStack = stack(row.from);
        ItemStack toStack = stack(row.to);

        renderExchangeItem(graphics, fromStack, x + 7, y + 7);
        drawExchangeArrow(graphics, ColorText.translatable("gui.adventuresystems.curios.wallet.exchange_arrow"), x + 25, y + 10);
        renderExchangeItem(graphics, toStack, x + 39, y + 7);

        Component text = ColorText.translatable("gui.adventuresystems.curios.wallet.exchange_to", toStack.getHoverName());
        drawExchangeName(graphics, text, x + 61, y + 4);
        drawRatioValue(graphics, ColorText.translatable("gui.adventuresystems.curios.wallet.ratio_label"), exchangeRatio(row.from, row.to), x + 61, y + 17);

        int oneX = oneButtonX(x, width);
        int allX = allButtonX(x, width);
        int buttonY = y + 5;
        boolean oneHover = mouseX >= oneX && mouseX <= oneX + 38 && mouseY >= buttonY && mouseY <= buttonY + 18;
        boolean allHover = mouseX >= allX && mouseX <= allX + 38 && mouseY >= buttonY && mouseY <= buttonY + 18;

        renderConvertOneButton(graphics, oneX, buttonY, oneHover, ColorText.translatable("gui.adventuresystems.curios.wallet.exchange_one"));
        renderConvertAllButton(graphics, allX, buttonY, allHover, ColorText.translatable("gui.adventuresystems.curios.wallet.exchange_all"));
    }

    private void renderWithdrawButton(GuiGraphics graphics, int x, int y, boolean hover, Component text) {
        renderButton(graphics, x, y, 62, 20, hover, text, GOLD_DARK, TEXT_NUMBER_ALT);
    }

    private void renderConvertOneButton(GuiGraphics graphics, int x, int y, boolean hover, Component text) {
        renderButton(graphics, x, y, 38, 18, hover, text, GREEN_DARK, GREEN);
    }

    private void renderConvertAllButton(GuiGraphics graphics, int x, int y, boolean hover, Component text) {
        renderButton(graphics, x, y, 38, 18, hover, text, YELLOW_DARK, YELLOW);
    }

    private void renderButton(GuiGraphics graphics, int x, int y, int width, int height, boolean hover, Component text, int borderColor, int textColor) {
        graphics.fill(x, y, x + width, y + height, borderColor);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, hover ? BUTTON_HOVER : BUTTON_BG);
        graphics.drawCenteredString(font, text, x + width / 2, y + (height - 8) / 2 - 1, textColor);
    }

    private void renderExchangeItem(GuiGraphics graphics, ItemStack stack, int x, int y) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(0.82f, 0.82f, 1.0f);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();
    }

    private void drawHintText(GuiGraphics graphics, Component text, int x, int y) {
        drawScaledText(graphics, text, x, y, HINT, 0.82f);
    }

    private void drawClickHint(GuiGraphics graphics, Component text, int x, int y) {
        drawScaledText(graphics, text, x, y, TEXT_GOLD, 0.85f);
    }

    private void drawExchangeArrow(GuiGraphics graphics, Component text, int x, int y) {
        drawScaledText(graphics, text, x, y, TEXT_GOLD, 0.82f);
    }

    private void drawExchangeName(GuiGraphics graphics, Component text, int x, int y) {
        drawScaledText(graphics, text, x, y, TEXT_WHITE, 0.88f);
    }

    private void drawScaledText(GuiGraphics graphics, Component text, int x, int y, int color, float scale) {
        float readableScale = Math.max(0.85f, Math.min(1.0f, scale));
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(readableScale, readableScale, 1.0f);
        graphics.drawString(font, text, 0, 0, color, true);
        graphics.pose().popPose();
    }

    private void drawAmountValue(GuiGraphics graphics, Component label, String value, int x, int y) {
        graphics.drawString(font, label, x, y, TEXT_AQUA, true);
        graphics.drawString(font, value, x + font.width(label) + 4, y, TEXT_NUMBER, true);
    }

    private void drawRatioValue(GuiGraphics graphics, Component label, Component value, int x, int y) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(0.85f, 0.85f, 1.0f);
        graphics.drawString(font, label, 0, 0, TEXT_PINK, true);
        graphics.drawString(font, value, font.width(label) + 4, 0, TEXT_NUMBER_ALT, true);
        graphics.pose().popPose();
    }

    private void renderScrollbar(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        updateScrollRange();

        listScroll.renderFramed(
                graphics,
                mouseX,
                mouseY,
                left + PANEL_WIDTH - 8,
                top + 52,
                5,
                visibleHeight(),
                18,
                ShopGuiSupport.SCROLLBAR_BORDER,
                ShopGuiSupport.SCROLLBAR_TRACK,
                ShopGuiSupport.SCROLLBAR_THUMB,
                ShopGuiSupport.SCROLLBAR_HOVER
        );
    }

    private void renderHover(GuiGraphics graphics, int mouseX, int mouseY, int rawMouseX, int rawMouseY) {
        if (mouseX >= left + PANEL_WIDTH - 138 && mouseX <= left + PANEL_WIDTH - 78 && mouseY >= top + 22 && mouseY <= top + 42) {
            List<Component> buttonTooltip = new ArrayList<>();
            buttonTooltip.add(ColorText.translatable(hudCurrencyVisible ? "gui.adventuresystems.curios.wallet.hud_visible" : "gui.adventuresystems.curios.wallet.hud_hidden").withStyle(ChatFormatting.GOLD));
            buttonTooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.hud_button_tip"));
            GuiOverlay.requestTooltip(buttonTooltip, rawMouseX, rawMouseY);
            return;
        }
        Row row = rowAt(mouseX, mouseY);
        if (row == null) return;
        List<Component> tooltip = new ArrayList<>();
        if (!row.exchange()) {
            ItemStack stack = stack(row.from);
            CurrencyType currency = currency(row.from);
            tooltip.add(stack.getHoverName().copy().withStyle(ChatFormatting.GOLD));
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.tooltip_exact", Component.literal(ShopGuiSupport.formatExact(amount(row.from))).withStyle(ChatFormatting.AQUA)));
            if (currency != null) {
                tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.tooltip_value", Component.literal(ShopGuiSupport.formatExact(currency.value())).withStyle(ChatFormatting.YELLOW)));
            }
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.tooltip_expand_click").withStyle(ChatFormatting.GREEN));
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.tooltip_withdraw").withStyle(ChatFormatting.YELLOW));
        } else {
            ItemStack fromStack = stack(row.from);
            ItemStack toStack = stack(row.to);
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.tooltip_convert", fromStack.getHoverName(), toStack.getHoverName()).withStyle(ChatFormatting.AQUA));
            tooltip.add(exchangeRatio(row.from, row.to));
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.tooltip_convert_one").withStyle(ChatFormatting.GREEN));
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.tooltip_convert_all").withStyle(ChatFormatting.YELLOW));
        }
        GuiOverlay.requestTooltip(tooltip, rawMouseX, rawMouseY);
    }

    @Override
    protected boolean canvasMouseClicked(double mouseX, double mouseY, int button) {
        if (super.canvasMouseClicked(mouseX, mouseY, button)) return true;
        if (button != 0) return false;

        updateScrollRange();

        if (listScroll.beginDrag(
                mouseX,
                mouseY,
                left + PANEL_WIDTH - 8,
                top + 52,
                4,
                visibleHeight(),
                18,
                3
        )) {
            return true;
        }

        Row row = rowAt((int) mouseX, (int) mouseY);
        if (row == null) return false;

        int baseX = left + 12;
        int baseWidth = PANEL_WIDTH - 24;

        if (!row.exchange()) {
            int buttonX = baseX + baseWidth - 74;
            if (mouseX >= buttonX && mouseX <= buttonX + 62) {
                Network.sendWithdraw(row.from);
                return true;
            }

            if (expandedCurrencies.contains(row.from)) {
                expandedCurrencies.remove(row.from);
            } else {
                expandedCurrencies.add(row.from);
            }
            rebuildRows();
            return true;
        }

        int childX = baseX + 18;
        int childWidth = baseWidth - 18;
        int oneX = oneButtonX(childX, childWidth);
        int allX = allButtonX(childX, childWidth);
        int buttonY = rowTop(row) + 5;
        boolean inButtonY = mouseY >= buttonY && mouseY <= buttonY + 18;

        if (inButtonY && mouseX >= oneX && mouseX <= oneX + 38) {
            Network.sendConvertOne(row.from, row.to);
            return true;
        }

        if (inButtonY && mouseX >= allX && mouseX <= allX + 38) {
            Network.sendConvertAll(row.from, row.to);
            return true;
        }

        return false;
    }

    @Override
    protected boolean canvasMouseReleased(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (listScroll.release(button)) {
            return true;
        }

        return super.canvasMouseReleased(
                mouseX,
                mouseY,
                button
        );
    }

    @Override
    protected boolean canvasMouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY
    ) {
        if (listScroll.drag(
                mouseY,
                top + 52,
                visibleHeight(),
                18
        )) {
            return true;
        }

        return super.canvasMouseDragged(
                mouseX,
                mouseY,
                button,
                dragX,
                dragY
        );
    }

    @Override
    protected boolean canvasMouseScrolled(
            double mouseX,
            double mouseY,
            double delta
    ) {
        updateScrollRange();

        return listScroll.scroll(delta, 18 / 3.0D)
                || super.canvasMouseScrolled(
                        mouseX,
                        mouseY,
                        delta
                );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }


    private Row rowAt(int mouseX, int mouseY) {
        int listLeft = left + 12;
        int listTop = top + 52;
        int listRight = left + PANEL_WIDTH - 12;
        int listBottom = top + PANEL_HEIGHT - 12;
        if (mouseX < listLeft || mouseX > listRight || mouseY < listTop || mouseY > listBottom) return null;

        int y = listTop - (int) Math.round(listScroll.smoothOffset());
        for (Row row : rows) {
            int rowLeft = row.exchange() ? listLeft + 18 : listLeft;
            if (mouseY >= y && mouseY <= y + row.height() - 3 && mouseX >= rowLeft) {
                return row;
            }
            y += row.height();
        }

        return null;
    }


    private int rowTop(Row target) {
        int y = top + 52 - (int) Math.round(listScroll.smoothOffset());
        for (Row row : rows) {
            if (row == target) return y;
            y += row.height();
        }
        return y;
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
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        if (item == null) item = net.minecraft.world.item.Items.BARRIER;
        return new ItemStack(item);
    }

    private int visibleHeight() {
        return PANEL_HEIGHT - 64;
    }

    private int contentHeight() {
        int height = 0;
        for (Row row : rows) {
            height += row.height();
        }
        return height;
    }

    private void updateScrollRange() {
        listScroll.update(
                contentHeight(),
                visibleHeight()
        );
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

        Component fromAmount;
        Component toAmount;

        if (sourceValue == targetValue) {
            fromAmount = Component.literal("1").withStyle(ChatFormatting.AQUA);
            toAmount = Component.literal("1").withStyle(ChatFormatting.YELLOW);
            return ColorText.translatable("gui.adventuresystems.curios.wallet.exchange_ratio", fromAmount, fromStack.getHoverName(), toAmount, toStack.getHoverName());
        }

        if (targetValue > sourceValue && targetValue % sourceValue == 0) {
            fromAmount = Component.literal(Long.toString(targetValue / sourceValue)).withStyle(ChatFormatting.AQUA);
            toAmount = Component.literal("1").withStyle(ChatFormatting.YELLOW);
            return ColorText.translatable("gui.adventuresystems.curios.wallet.exchange_ratio", fromAmount, fromStack.getHoverName(), toAmount, toStack.getHoverName());
        }

        if (sourceValue > targetValue && sourceValue % targetValue == 0) {
            fromAmount = Component.literal("1").withStyle(ChatFormatting.AQUA);
            toAmount = Component.literal(Long.toString(sourceValue / targetValue)).withStyle(ChatFormatting.YELLOW);
            return ColorText.translatable("gui.adventuresystems.curios.wallet.exchange_ratio", fromAmount, fromStack.getHoverName(), toAmount, toStack.getHoverName());
        }

        return ColorText.translatable("gui.adventuresystems.curios.wallet.exchange_ratio_invalid").withStyle(ChatFormatting.RED);
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

}

