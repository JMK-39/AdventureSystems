package dev.xyat.adventuresystems.ftb.client;

import dev.xyat.adventuresystems.ftb.FtbModule;
import dev.xyat.adventuresystems.ftb.data.BlacklistStoreFTB;
import dev.xyat.adventuresystems.ftb.data.BindingStoreFTB;
import dev.xyat.adventuresystems.ftb.data.FavoritesStoreFTB;
import dev.xyat.adventuresystems.ftb.event.ClientEventsFTB;
import dev.xyat.adventuresystems.ftb.util.BridgeFTB;
import dev.xyat.adventuresystems.ftb.util.QuestMatchCacheFTB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

public final class ItemClientModuleFTB {
    private static boolean enabled = false;

    private ItemClientModuleFTB() {
    }

    public static void register(IEventBus modEventBus) {
        FTBClientConfig.register();


        enabled = true;
        BridgeFTB.init();
        BindingStoreFTB.load();
        FavoritesStoreFTB.load();
        BlacklistStoreFTB.load();
        QuestMatchCacheFTB.rebuild();

        modEventBus.addListener(KeyMappingsFTB::register);
        MinecraftForge.EVENT_BUS.register(ClientEventsFTB.class);
    }

    public static boolean isEnabled() {
        return enabled;
    }


}
