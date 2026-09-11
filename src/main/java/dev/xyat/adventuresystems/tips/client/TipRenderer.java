package dev.xyat.adventuresystems.tips.client;

import dev.xyat.adventuresystems.tips.TipsModule;
import dev.xyat.adventuresystems.tips.api.HelpTip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(modid = TipsModule.MODID, value = Dist.CLIENT)
public class TipRenderer {
    private static HelpTip currentTip;
    private static TipCache currentCache;
    private static long lastSwitchTime;

    // 匹配颜色代码，用于换行时继承颜色
    public static final Pattern COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    private record TipCache(List<String> lines, int totalW, int totalBoxH) {}

    public static void refresh(Screen screen) {
        currentTip = dev.xyat.adventuresystems.tips.client.TipCache.TIP_MANAGER.getValidTip(screen);
        lastSwitchTime = System.currentTimeMillis();
        if (currentTip != null) precomputeLayout();
        else currentCache = null;
    }

    private static void precomputeLayout() {
        Minecraft mc = Minecraft.getInstance();
        String rawText = currentTip.getText().getString();

        // 使用60像素宽度分割文本
        List<String> lines = splitTextKeepFormat(rawText);

        int maxW = mc.font.width(Component.translatable("gui.adventuresystems.tips.tips.title"));
        for (String line : lines) {
            maxW = Math.max(maxW, mc.font.width(line));
        }

        int totalBoxH = 10 + 9 + 5 + (lines.size() * 11) + 5;
        currentCache = new TipCache(lines, maxW, totalBoxH);
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        Screen s = event.getScreen();
        if (s instanceof LevelLoadingScreen || s instanceof PauseScreen) {
            if (currentTip == null || System.currentTimeMillis() - lastSwitchTime > currentTip.cycleTime) refresh(s);
            if (currentTip != null && currentCache != null) draw(event.getGuiGraphics(), s);
        }
    }

    private static void draw(GuiGraphics g, Screen s) {
        Minecraft mc = Minecraft.getInstance();
        int margin = 5;
        int padding = 8;
        int boxW = currentCache.totalW + padding * 2;
        int boxY = s.height - currentCache.totalBoxH - margin;

        g.fill(margin, boxY, margin + boxW, s.height - margin, 0xAA000000);

        int curY = boxY + padding;

        // 标题：默认金色 (0xFFAA00)，如果语言文件里有颜色代码则会覆盖
        g.drawString(mc.font, Component.translatable("gui.adventuresystems.tips.tips.title"), margin + padding, curY, 0xFFAA00, true);
        curY += 13;

        for (String line : currentCache.lines) {
            // 正文：默认白色 (0xFFFFFF)，你的文本中包含的 § 代码会覆盖这个白色
            g.drawString(mc.font, line, margin + padding, curY, 0xFFFFFF, true);
            curY += 11;
        }
    }

    /**
     * 分割文本并保留颜色格式
     */
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

            Matcher m = COLOR_PATTERN.matcher(seg);
            while (m.find()) {
                lastFormat = m.group();
            }
        }
        if (!currentLine.isEmpty()) lines.add(currentLine.toString());
        return lines;
    }
}
