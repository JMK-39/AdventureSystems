package dev.xyat.adventuresystems.tips.command;

import dev.xyat.kineticcore.command.KTCommandApi;
import dev.xyat.kineticcore.command.KTCommandExtension;
import dev.xyat.adventuresystems.tips.TipsModule;
import dev.xyat.adventuresystems.tips.config.GeneralConfig;
import net.minecraft.commands.CommandSourceStack;

public final class TipsCommandExtension implements KTCommandExtension {
    private TipsCommandExtension() {
    }

    public static void install() {
        KTCommandApi.register(TipsModule.MODID, new TipsCommandExtension());
    }

    @Override
    public void reload(CommandSourceStack source) {
        GeneralConfig.load();
    }
}
