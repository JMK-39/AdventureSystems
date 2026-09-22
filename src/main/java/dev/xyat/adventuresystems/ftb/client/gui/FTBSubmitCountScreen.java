package dev.xyat.adventuresystems.ftb.client.gui;

import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.xyat.adventuresystems.ftb.api.FTBTaskSubmitHelper;
import dev.xyat.adventuresystems.ftb.client.FTBVirtualItemClientState;
import dev.xyat.adventuresystems.ftb.network.FTBSubmitLimitNetwork;
import dev.xyat.kineticcore.api.client.input.KineticKeyBindings;
import dev.xyat.kineticcore.api.client.screen.KineticScreen;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.widget.input.KineticNumericFields.NumericEditBox;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import dev.xyat.kineticcore.api.text.KineticI18n;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FTBSubmitCountScreen extends KineticScreen {
    private static final int PANEL_W = 420;
    private static final int PANEL_H = 230;
    private static final int MARGIN_X = 24;
    private static final int BUTTON_W = 150;
    private static final long STATS_REFRESH_MS = 250L;

    private final Screen parent;
    private final ItemTask task;
    private NumericEditBox countBox;
    private int left;
    private int top;
    private String lastCountValue = "";
    private long lastStatsRefreshMs;
    private int cachedCount = 1;
    private long cachedPerExchange = 1L;
    private long cachedRequiredItems = 1L;
    private long cachedInventoryItems;
    private long cachedVirtualItems;
    private boolean cachedVirtualSupported;
    private int cachedInventoryEstimatedTimes;
    private int cachedTotalEstimatedTimes;

    public FTBSubmitCountScreen(Screen parent, ItemTask task) {
        super(KineticI18n.translatable("screen.adventuresystems.ftb.submit"));
        this.parent = parent;
        setParentScreen(parent);
        this.task = task;
        useCanvas(460f, 270f, 6);
    }

    @Override
    protected void buildUi() {
        left = (canvasWidth() - PANEL_W) / 2;
        top = (canvasHeight() - PANEL_H) / 2;
        countBox = addIntegerField(
                left + MARGIN_X,
                top + 52,
                PANEL_W - MARGIN_X * 2,
                KineticI18n.translatable("placeholder.adventuresystems.ftb.submit.count"),
                false,
                1,
                1_000_000,
                null
        );
        countBox.setValue("1");
        int buttonY = top + PANEL_H - 38;
        addButton(left + MARGIN_X, buttonY, BUTTON_W, KineticI18n.translatable("button.adventuresystems.ftb.submit.confirm"), null, this::submit);
        addButton(left + PANEL_W - MARGIN_X - BUTTON_W, buttonY, BUTTON_W, KineticI18n.translatable("gui.cancel"), null, this::onClose);
        refreshStats(true);
        focusControl(countBox);
    }

    private void submit() {
        if (!FTBTaskSubmitHelper.isCustomSubmitAllowed(task)) {
            FTBSubmitLimitNetwork.sendSubmit(task.id, 1);
            navigateBack();
            return;
        }
        refreshStats(true);
        FTBSubmitLimitNetwork.sendSubmit(task.id, cachedCount);
        navigateBack();
    }

    private int parseCountValue(String value) {
        try {
            return Math.max(1, Math.min(1_000_000, Integer.parseInt(value.trim())));
        } catch (Exception ignored) {
            return 1;
        }
    }

    private void refreshStats(boolean force) {
        String value = countBox == null ? "" : countBox.getValue().trim();
        long now = System.currentTimeMillis();
        if (!force && value.equals(lastCountValue) && now - lastStatsRefreshMs < STATS_REFRESH_MS) return;
        lastCountValue = value;
        lastStatsRefreshMs = now;
        cachedCount = parseCountValue(value);
        cachedPerExchange = Math.max(1L, task.getMaxProgress());
        cachedRequiredItems = safeMultiply(cachedPerExchange, cachedCount);
        cachedInventoryItems = calculateInventoryItems(cachedRequiredItems);
        FTBVirtualItemClientState.Snapshot virtualItems = FTBVirtualItemClientState.request(task.id);
        cachedVirtualSupported = virtualItems.supported();
        cachedVirtualItems = virtualItems.count();
        cachedInventoryEstimatedTimes = estimateTimes(cachedInventoryItems, cachedPerExchange, cachedCount);
        cachedTotalEstimatedTimes = estimateTimes(safeAdd(cachedInventoryItems, cachedVirtualItems), cachedPerExchange, cachedCount);
    }

    private long calculateInventoryItems(long stopAt) {
        LocalPlayer player = KineticClientRuntime.localPlayer();
        if (player == null) return 0L;
        long total = 0L;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) continue;
            try {
                if (task.test(stack)) {
                    total += stack.getCount();
                    if (total >= stopAt) return total;
                }
            } catch (Throwable ignored) {
            }
        }
        return total;
    }

    private int estimateTimes(long amount, long per, int maxTimes) {
        long times = amount / Math.max(1L, per);
        return (int) Math.max(0L, Math.min(maxTimes, times));
    }

    private long safeMultiply(long a, long b) {
        try {
            return Math.multiplyExact(a, b);
        } catch (ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
    }

    private long safeAdd(long a, long b) {
        try {
            return Math.addExact(a, b);
        } catch (ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
    }

    @Override
    protected void renderCanvasBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        refreshStats(false);
        GuiTheme.shadow(graphics, canvasWidth(), canvasHeight());
        GuiTheme.canvasBackground(graphics, canvasWidth(), canvasHeight());
        GuiTheme.panel(graphics, left, top, PANEL_W, PANEL_H);
        graphics.drawCenteredString(font, title, left + PANEL_W / 2, top + 14, GuiTheme.current().text());
        graphics.drawString(font, KineticI18n.translatable("label.adventuresystems.ftb.submit.desc"), left + MARGIN_X, top + 34, GuiTheme.current().mutedText(), false);
    }

    @Override
    protected void renderCanvasForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int lineX = left + MARGIN_X;
        int y = top + 86;
        int gap = 18;
        drawLine(graphics, KineticI18n.translatable("label.adventuresystems.ftb.submit.exchanges", number(cachedCount)), lineX, y);
        y += gap;
        drawLine(graphics, KineticI18n.translatable("label.adventuresystems.ftb.submit.required", number(cachedRequiredItems)), lineX, y);
        y += gap;
        drawLine(graphics, KineticI18n.translatable("label.adventuresystems.ftb.submit.inventory", number(cachedInventoryItems)), lineX, y);
        y += gap;
        drawLine(graphics, KineticI18n.translatable("label.adventuresystems.ftb.submit.inventory.times", number(cachedInventoryEstimatedTimes)), lineX, y);
        y += gap;
        if (cachedVirtualSupported) {
            drawLine(graphics, KineticI18n.translatable("label.adventuresystems.ftb.submit.virtual", number(cachedVirtualItems)), lineX, y);
            y += gap;
            drawLine(graphics, KineticI18n.translatable("label.adventuresystems.ftb.submit.total", number(cachedTotalEstimatedTimes)), lineX, y);
        }
    }

    private static Component number(long value) {
        return Component.literal(String.valueOf(value));
    }

    private void drawLine(GuiGraphics graphics, Component component, int x, int y) {
        graphics.drawString(font, component, x, y, GuiTheme.current().text(), false);
    }

    @Override
    protected boolean canvasKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (KineticKeyBindings.matchesKeyCode(KineticKeyBindings.Key.ENTER, keyCode)
                || KineticKeyBindings.matchesKeyCode(KineticKeyBindings.Key.KP_ENTER, keyCode)) {
            submit();
            return true;
        }
        return false;
    }

    @Override
    protected boolean handleCloseRequest() {
        navigateBack();
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
