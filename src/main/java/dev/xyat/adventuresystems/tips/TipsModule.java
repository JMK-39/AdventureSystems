package dev.xyat.adventuresystems.tips;

import com.mojang.logging.LogUtils;
import dev.xyat.adventuresystems.tips.command.TipsCommandExtension;
import dev.xyat.adventuresystems.tips.client.TipRenderer;
import dev.xyat.adventuresystems.tips.client.TipEvents;
import dev.xyat.adventuresystems.tips.client.TipCache;
import dev.xyat.adventuresystems.tips.config.GeneralConfig;
import dev.xyat.adventuresystems.tips.config.TipsConfigGui;
import dev.xyat.kineticcore.api.config.server.KTServerConfigApi;
import dev.xyat.kineticcore.api.runtime.KineticPlatform;
import org.slf4j.Logger;

public final class TipsModule {
    public static final String MODID = "adventuresystems";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TipsModule() {
        GeneralConfig.load();
        KTServerConfigApi.registerActionPage(TipsConfigGui.EDITOR_PAGE_ID);
        TipsNetwork.register();
        TipsCommandExtension.install();
        KineticPlatform.runOnClient(() -> () -> {
            TipsConfigGui.load();
            TipCache.install();
            TipEvents.install();
            TipRenderer.install();
        });
    }
}
