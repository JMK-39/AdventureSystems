package dev.xyat.adventuresystems.tips.client;

import dev.xyat.adventuresystems.tips.api.HelpTip;
import dev.xyat.kineticcore.api.client.event.KineticClientEvents;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.text.KineticText;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.adventuresystems.text.AdventureText;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.util.FormattedCharSequence;

public final class TipRenderer {
    private static HelpTip currentTip;
    private static TipCache currentCache;
    private static long lastSwitchTime;

    public static final Pattern COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    private record TipCache(List<FormattedCharSequence> lines, int totalW, int totalBoxH) {
    }

    private TipRenderer() {
    }

    public static void install() {
        KineticClientEvents.onScreenRenderAfter(TipRenderer::onScreenRender);
    }

    public static void refresh(Screen screen) {
        currentTip = dev.xyat.adventuresystems.tips.client.TipCache.TIP_MANAGER.getValidTip(screen);
        lastSwitchTime = System.currentTimeMillis();
        if (currentTip != null) precomputeLayout(screen);
        else currentCache = null;
    }

    private static void precomputeLayout(Screen screen) {
        List<FormattedCharSequence> lines = KineticText.wrap(currentTip.getText(), Math.max(1, Math.min(360, screen.width - 26)));

        int maxW = KineticText.width(AdventureText.translatable("gui.adventuresystems.tips.tips.title"));
        for (FormattedCharSequence line : lines) {
            maxW = Math.max(maxW, KineticText.width(line));
        }

        int totalBoxH = 10 + 9 + 5 + (lines.size() * 11) + 5;
        currentCache = new TipCache(lines, maxW, totalBoxH);
    }

    private static void onScreenRender(Screen screen, KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (screen instanceof LevelLoadingScreen || screen instanceof PauseScreen) {
            if (currentTip == null || System.currentTimeMillis() - lastSwitchTime > currentTip.cycleTime) refresh(screen);
            if (currentTip != null && currentCache != null) draw(graphics, screen);
        }
    }

    private static void draw(KineticGraphics graphics, Screen screen) {
        int margin = 5;
        int padding = 8;
        int boxW = currentCache.totalW + padding * 2;
        int boxY = screen.height - currentCache.totalBoxH - margin;

        KineticTheme.panel(graphics, margin, boxY, boxW, currentCache.totalBoxH);

        int curY = boxY + padding;
        graphics.text(
                AdventureText.translatable("gui.adventuresystems.tips.tips.title"),
                margin + padding,
                curY,
                KineticTheme.current().text(),
                true
        );
        curY += 13;

        for (FormattedCharSequence line : currentCache.lines) {
            graphics.text(line, margin + padding, curY, KineticTheme.current().text(), true);
            curY += 11;
        }
    }

}
