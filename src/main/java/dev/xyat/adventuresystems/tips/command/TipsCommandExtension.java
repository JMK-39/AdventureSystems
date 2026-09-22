package dev.xyat.adventuresystems.tips.command;

import dev.xyat.kineticcore.api.command.CommandExtension;
import dev.xyat.kineticcore.api.command.KineticCommands;
import dev.xyat.adventuresystems.tips.TipsModule;
import dev.xyat.adventuresystems.tips.config.GeneralConfig;
import net.minecraft.commands.CommandSourceStack;

public final class TipsCommandExtension implements CommandExtension {
    private TipsCommandExtension() {
    }

    public static void install() {
        KineticCommands.registerExtension(TipsModule.MODID, new TipsCommandExtension());
    }

    @Override
    public void reload(CommandSourceStack source) {
        GeneralConfig.load();
    }
}
