package dev.xyat.adventuresystems.ftb.client.gui;

import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.xyat.adventuresystems.ftb.api.FTBTaskSubmitHelper;
import dev.xyat.adventuresystems.ftb.client.FTBVirtualItemClientState;
import dev.xyat.adventuresystems.ftb.network.FTBSubmitLimitNetwork;
import dev.xyat.kineticcore.api.client.gui.input.KeyInput;
import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.ui.NumberType;
import dev.xyat.kineticcore.api.client.gui.widget.KineticNumberField;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import dev.xyat.adventuresystems.text.AdventureText;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class FTBSubmitCountScreen extends KineticPage {
    private static final int PANEL_W = 420;
    private static final int PANEL_H = 230;
    private static final int MARGIN_X = 24;
    private static final int BUTTON_W = 150;
    private static final long STATS_REFRESH_MS = 250L;

    private final ItemTask task;
    private KineticNumberField countBox;
    private String countValue = "1";
    private int left;
    private int top;
    private String lastCountValue = "";
    private long lastStatsRefreshMs;
    private int cachedCount = 1;
    private long cachedRequiredItems = 1L;
    private long cachedInventoryItems;
    private long cachedVirtualItems;
    private boolean cachedVirtualSupported;
    private int cachedInventoryEstimatedTimes;
    private int cachedTotalEstimatedTimes;

    public FTBSubmitCountScreen(ItemTask task) {
        super(AdventureText.translatable("screen.adventuresystems.ftb.submit"));
        this.task = task;
        useCanvas(460, 270, 6);
        setPausesGame(false);
    }

    @Override
    protected void build(KineticUi ui) {
        left = (width() - PANEL_W) / 2;
        top = (height() - PANEL_H) / 2;
        countBox = ui.numberField(left + MARGIN_X, top + 52, PANEL_W - MARGIN_X * 2, NumberType.INT)
                .label(AdventureText.translatable("placeholder.adventuresystems.ftb.submit.count"))
                .allowNegative(false)
                .range(1, 1_000_000)
                .onChange(value -> countValue = value)
                .firstShownTextAsDefault().build();
        countBox.setTextValue(countValue);
        int buttonY = top + PANEL_H - 38;
        ui.button(left + MARGIN_X, buttonY, BUTTON_W)
                .text(AdventureText.translatable("button.adventuresystems.ftb.submit.confirm"))
                .onClick(this::submit).build();
        ui.button(left + PANEL_W - MARGIN_X - BUTTON_W, buttonY, BUTTON_W)
                .text(AdventureText.translatable("gui.cancel"))
                .onClick(this::close).build();
        refreshStats(true);
        focus(countBox);
    }

    private void submit() {
        if (FTBTaskSubmitHelper.isCustomSubmitAllowed(task)) {
            refreshStats(true);
            FTBSubmitLimitNetwork.sendSubmit(task.id, cachedCount);
        } else {
            FTBSubmitLimitNetwork.sendSubmit(task.id, 1);
        }
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
        String value = countBox == null ? "" : countBox.textValue().trim();
        long now = System.currentTimeMillis();
        if (!force && value.equals(lastCountValue) && now - lastStatsRefreshMs < STATS_REFRESH_MS) return;
        lastCountValue = value;
        lastStatsRefreshMs = now;
        cachedCount = parseCountValue(value);
        long perExchange = Math.max(1L, task.getMaxProgress());
        cachedRequiredItems = safeMultiply(perExchange, cachedCount);
        cachedInventoryItems = calculateInventoryItems(cachedRequiredItems);
        FTBVirtualItemClientState.Snapshot virtualItems = FTBVirtualItemClientState.request(task.id);
        cachedVirtualSupported = virtualItems.supported();
        cachedVirtualItems = virtualItems.count();
        cachedInventoryEstimatedTimes = estimateTimes(cachedInventoryItems, perExchange, cachedCount);
        cachedTotalEstimatedTimes = estimateTimes(safeAdd(cachedInventoryItems, cachedVirtualItems), perExchange, cachedCount);
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
    protected void renderBackground(KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        refreshStats(false);
        KineticTheme.shadow(graphics, width(), height());
        KineticTheme.canvasBackground(graphics, width(), height());
        KineticTheme.panel(graphics, left, top, PANEL_W, PANEL_H);
        graphics.centeredText(title(), left + PANEL_W / 2, top + 14, KineticTheme.current().text(), true);
        graphics.text(AdventureText.translatable("label.adventuresystems.ftb.submit.desc"), left + MARGIN_X, top + 34, KineticTheme.current().mutedText(), false);
    }

    @Override
    protected void renderForeground(KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int lineX = left + MARGIN_X;
        int y = top + 86;
        int gap = 18;
        drawLine(graphics, AdventureText.translatable("label.adventuresystems.ftb.submit.exchanges", number(cachedCount)), lineX, y);
        y += gap;
        drawLine(graphics, AdventureText.translatable("label.adventuresystems.ftb.submit.required", number(cachedRequiredItems)), lineX, y);
        y += gap;
        drawLine(graphics, AdventureText.translatable("label.adventuresystems.ftb.submit.inventory", number(cachedInventoryItems)), lineX, y);
        y += gap;
        drawLine(graphics, AdventureText.translatable("label.adventuresystems.ftb.submit.inventory.times", number(cachedInventoryEstimatedTimes)), lineX, y);
        y += gap;
        if (cachedVirtualSupported) {
            drawLine(graphics, AdventureText.translatable("label.adventuresystems.ftb.submit.virtual", number(cachedVirtualItems)), lineX, y);
            y += gap;
            drawLine(graphics, AdventureText.translatable("label.adventuresystems.ftb.submit.total", number(cachedTotalEstimatedTimes)), lineX, y);
        }
    }

    private static Component number(long value) {
        return Component.literal(String.valueOf(value));
    }

    private void drawLine(KineticGraphics graphics, Component component, int x, int y) {
        graphics.text(component, x, y, KineticTheme.current().text(), false);
    }

    @Override
    protected boolean onKeyPress(KeyInput input) {
        if (input.isEnter()) {
            submit();
            return true;
        }
        return false;
    }

    @Override
    protected boolean onCloseRequested() {
        navigateBack();
        return true;
    }
}
