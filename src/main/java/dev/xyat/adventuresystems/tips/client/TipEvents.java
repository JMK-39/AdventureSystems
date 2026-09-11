package dev.xyat.adventuresystems.tips.client;

import dev.xyat.adventuresystems.tips.TipsModule;
import dev.xyat.adventuresystems.tips.TipsNetwork;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TipsModule.MODID, value = Dist.CLIENT)
public class TipEvents {
    private static String requestedLanguage = "";

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() == null || minecraft.level == null) {
            if (!requestedLanguage.isEmpty()) {
                requestedLanguage = "";
                TipCache.TIP_MANAGER.reloadLocalFallback();
            }
            return;
        }

        String language = minecraft.getLanguageManager().getSelected();
        if (!language.equals(requestedLanguage)) {
            requestedLanguage = language;
            TipsNetwork.requestRuntimeTips();
        }
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof PauseScreen) {
            TipsNetwork.sendToServer(new TipsNetwork.RequestStructure(false));
        }
    }
}
