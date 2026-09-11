package dev.xyat.adventuresystems.tips;

import com.mojang.logging.LogUtils;
import dev.xyat.adventuresystems.tips.command.TipsCommandExtension;
import dev.xyat.adventuresystems.tips.config.GeneralConfig;
import dev.xyat.adventuresystems.tips.config.TipsConfigGui;
import dev.xyat.kineticcore.config.server.KTServerConfigApi;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

public final class TipsModule {
    public static final String MODID = "adventuresystems";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TipsModule() {
        GeneralConfig.load();
        KTServerConfigApi.registerActionPage(TipsConfigGui.EDITOR_PAGE_ID);
        TipsNetwork.register();
        TipsCommandExtension.install();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> TipsConfigGui::load);
    }
}
