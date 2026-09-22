package dev.xyat.adventuresystems.tips.client;

import dev.xyat.adventuresystems.tips.api.HelpTip;
import dev.xyat.kineticcore.api.client.event.KineticClientEvents;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import dev.xyat.kineticcore.api.text.KineticI18n;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TipRenderer {
    private static HelpTip currentTip;
    private static TipCache currentCache;
    private static long lastSwitchTime;

    public static final Pattern COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    private record TipCache(List<String> lines, int totalW, int totalBoxH) {
    }

    private TipRenderer() {
    }

    public static void install() {
        KineticClientEvents.onScreenRenderAfter(TipRenderer::onScreenRender);
    }

    public static void refresh(Screen screen) {
        currentTip = dev.xyat.adventuresystems.tips.client.TipCache.TIP_MANAGER.getValidTip(screen);
        lastSwitchTime = System.currentTimeMillis();
        if (currentTip != null) precomputeLayout();
        else currentCache = null;
    }

    private static void precomputeLayout() {
        Font font = KineticClientRuntime.font();
        String rawText = currentTip.getText().getString();
        List<String> lines = splitTextKeepFormat(rawText);

        int maxW = font.width(KineticI18n.translatable("gui.adventuresystems.tips.tips.title"));
        for (String line : lines) {
            maxW = Math.max(maxW, font.width(line));
        }

        int totalBoxH = 10 + 9 + 5 + (lines.size() * 11) + 5;
        currentCache = new TipCache(lines, maxW, totalBoxH);
    }

    private static void onScreenRender(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (screen instanceof LevelLoadingScreen || screen instanceof PauseScreen) {
            if (currentTip == null || System.currentTimeMillis() - lastSwitchTime > currentTip.cycleTime) refresh(screen);
            if (currentTip != null && currentCache != null) draw(graphics, screen);
        }
    }

    private static void draw(GuiGraphics graphics, Screen screen) {
        Font font = KineticClientRuntime.font();
        int margin = 5;
        int padding = 8;
        int boxW = currentCache.totalW + padding * 2;
        int boxY = screen.height - currentCache.totalBoxH - margin;

        GuiTheme.panel(graphics, margin, boxY, boxW, currentCache.totalBoxH);

        int curY = boxY + padding;
        graphics.drawString(
                font,
                KineticI18n.translatable("gui.adventuresystems.tips.tips.title"),
                margin + padding,
                curY,
                GuiTheme.current().text(),
                true
        );
        curY += 13;

        for (String line : currentCache.lines) {
            graphics.drawString(font, line, margin + padding, curY, GuiTheme.current().text(), true);
            curY += 11;
        }
    }

    private static List<String> splitTextKeepFormat(String text) {
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        String lastFormat = "";
        int visibleLen = 0;

        String[] segments = text.split("(?<=\\s)");

        for (String seg : segments) {
            String cleanSeg = COLOR_PATTERN.matcher(seg).replaceAll("");

            if (visibleLen + cleanSeg.length() > 60) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder();
                if (!lastFormat.isEmpty()) currentLine.append(lastFormat);
                visibleLen = 0;
            }

            currentLine.append(seg);
            visibleLen += cleanSeg.length();

            Matcher matcher = COLOR_PATTERN.matcher(seg);
            while (matcher.find()) {
                lastFormat = matcher.group();
            }
        }
        if (!currentLine.isEmpty()) lines.add(currentLine.toString());
        return lines;
    }
}
