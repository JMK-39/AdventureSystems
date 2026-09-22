package dev.xyat.adventuresystems.tips.client;

import dev.xyat.adventuresystems.tips.TipsNetwork;
import dev.xyat.kineticcore.api.client.event.KineticClientEvents;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import net.minecraft.client.gui.screens.PauseScreen;

public final class TipEvents {
    private static String requestedLanguage = "";

    private TipEvents() {
    }

    public static void install() {
        KineticClientEvents.onTick(KineticClientEvents.TickPhase.END, TipEvents::onClientTick);
        KineticClientEvents.onScreenInitAfter(TipEvents::onScreenInit);
    }

    private static void onClientTick() {
        if (!KineticClientRuntime.connected() || KineticClientRuntime.currentLevel() == null) {
            if (!requestedLanguage.isEmpty()) {
                requestedLanguage = "";
                TipCache.TIP_MANAGER.reloadLocalFallback();
            }
            return;
        }

        String language = KineticClientRuntime.selectedLanguage();
        if (!language.equals(requestedLanguage)) {
            requestedLanguage = language;
            TipsNetwork.requestRuntimeTips();
        }
    }

    private static void onScreenInit(KineticClientEvents.ScreenInitContext event) {
        if (event.screen() instanceof PauseScreen) {
            TipsNetwork.sendToServer(new TipsNetwork.RequestStructure(false));
        }
    }
}
