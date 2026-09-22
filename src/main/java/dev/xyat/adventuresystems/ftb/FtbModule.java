package dev.xyat.adventuresystems.ftb;

import com.mojang.logging.LogUtils;
import dev.xyat.adventuresystems.ftb.client.FTBConfigGui;
import dev.xyat.adventuresystems.ftb.client.ItemClientModuleFTB;
import dev.xyat.adventuresystems.ftb.network.FTBSubmitLimitNetwork;
import dev.xyat.kineticcore.api.runtime.KineticPlatform;
import org.slf4j.Logger;

public final class FtbModule {
    public static final String MODID = "adventuresystems";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FtbModule() {
        FTBSubmitLimitNetwork.register();
        KineticPlatform.runOnClient(() -> () -> {
            ItemClientModuleFTB.register();
            FTBConfigGui.load();
        });
    }
}
