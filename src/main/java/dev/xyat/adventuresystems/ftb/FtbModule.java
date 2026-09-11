package dev.xyat.adventuresystems.ftb;

import com.mojang.logging.LogUtils;
import dev.xyat.adventuresystems.ftb.client.FTBClientConfig;
import dev.xyat.adventuresystems.ftb.client.FTBConfigGui;
import dev.xyat.adventuresystems.ftb.client.ItemClientModuleFTB;
import dev.xyat.adventuresystems.ftb.network.FTBSubmitLimitNetwork;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

public final class FtbModule {
    public static final String MODID = "adventuresystems";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FtbModule(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        FTBSubmitLimitNetwork.register();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FTBClientConfig.register();
            ItemClientModuleFTB.register(modEventBus);
            FTBConfigGui.load();
        });
    }
}
