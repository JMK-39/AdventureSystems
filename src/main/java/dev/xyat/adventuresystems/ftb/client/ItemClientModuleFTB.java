package dev.xyat.adventuresystems.ftb.client;

import dev.xyat.adventuresystems.ftb.data.BlacklistStoreFTB;
import dev.xyat.adventuresystems.ftb.data.BindingStoreFTB;
import dev.xyat.adventuresystems.ftb.data.FavoritesStoreFTB;
import dev.xyat.adventuresystems.ftb.event.ClientEventsFTB;
import dev.xyat.adventuresystems.ftb.util.BridgeFTB;
import dev.xyat.adventuresystems.ftb.util.QuestMatchCacheFTB;

public final class ItemClientModuleFTB {
    private static boolean enabled;

    private ItemClientModuleFTB() {
    }

    public static void register() {
        if (enabled) return;
        FTBClientConfig.register();

        enabled = true;
        BridgeFTB.init();
        BindingStoreFTB.load();
        FavoritesStoreFTB.load();
        BlacklistStoreFTB.load();
        QuestMatchCacheFTB.rebuild();

        KeyMappingsFTB.register();
        ClientEventsFTB.install();
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
