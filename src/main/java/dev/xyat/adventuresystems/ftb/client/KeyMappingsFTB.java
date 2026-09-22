package dev.xyat.adventuresystems.ftb.client;

import dev.xyat.adventuresystems.ftb.FtbModule;
import dev.xyat.adventuresystems.ftb.event.ClientEventsFTB;
import dev.xyat.kineticcore.api.client.input.KineticKeyBindings;

public final class KeyMappingsFTB {
    public static KineticKeyBindings.Binding OPEN_QUEST;
    public static KineticKeyBindings.Binding OPEN_QUEST_MULTI;
    private static boolean registered;

    private KeyMappingsFTB() {
    }

    public static void register() {
        if (registered) return;
        registered = true;

        OPEN_QUEST = KineticKeyBindings.builder("key." + FtbModule.MODID + ".ftb.open")
                .category("key.adventuresystems.category")
                .context(KineticKeyBindings.Context.GUI)
                .keyboard(KineticKeyBindings.Key.G)
                .modifier(KineticKeyBindings.Modifier.NONE)
                .exactModifiers(true)
                .onPressed(() -> ClientEventsFTB.handleOpenPress(false))
                .register();

        OPEN_QUEST_MULTI = KineticKeyBindings.builder("key." + FtbModule.MODID + ".ftb.open.multi")
                .category("key.adventuresystems.category")
                .context(KineticKeyBindings.Context.GUI)
                .keyboard(KineticKeyBindings.Key.G)
                .modifier(KineticKeyBindings.Modifier.ALT)
                .exactModifiers(true)
                .onPressed(() -> ClientEventsFTB.handleOpenPress(true))
                .register();
    }
}
