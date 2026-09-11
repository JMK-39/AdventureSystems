package dev.xyat.adventuresystems;

import dev.xyat.adventuresystems.tips.TipsModule;
import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.adventuresystems.ftb.FtbModule;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AdventureSystems.MODID)
public final class AdventureSystems {
    public static final String MODID = "adventuresystems";

    public AdventureSystems(FMLJavaModLoadingContext context) {
        new TipsModule();
        new CuriosModule();
        new FtbModule(context);
    }
}
