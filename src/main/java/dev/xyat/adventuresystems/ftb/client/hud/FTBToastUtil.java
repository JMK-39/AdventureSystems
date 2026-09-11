package dev.xyat.adventuresystems.ftb.client.hud;

import dev.xyat.kineticcore.api.client.overlay.GuiOverlay;
import net.minecraft.network.chat.Component;

public final class FTBToastUtil {
    private static final GuiOverlay.Position POSITION = GuiOverlay.Position.BOTTOM_CENTER;
    private static final int OFFSET_X = 0;
    private static final int OFFSET_Y = -30;

    private FTBToastUtil() {
    }

    public static void show(String id, Component message) {
        GuiOverlay.toast(id, message);
    }

    public static void showShort(String id, Component message) {
        showTimed(id, message, 2500);
    }

    public static void showQuick(String id, Component message) {
        showTimed(id, message, 1800);
    }

    public static void showLong(String id, Component message) {
        showTimed(id, message, 4500);
    }

    public static void showTimed(String id, Component message, int durationMs) {
        GuiOverlay.toast(id, message, POSITION, durationMs, OFFSET_X, OFFSET_Y);
    }
}
