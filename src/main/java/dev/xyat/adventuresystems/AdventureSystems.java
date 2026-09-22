package dev.xyat.adventuresystems;

import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.adventuresystems.ftb.FtbModule;
import dev.xyat.adventuresystems.tips.TipsModule;
import net.minecraftforge.fml.common.Mod;

@Mod(AdventureSystems.MODID)
public final class AdventureSystems {
    public static final String MODID = "adventuresystems";

    public AdventureSystems() {
        new TipsModule();
        new CuriosModule();
        new FtbModule();
    }
}
