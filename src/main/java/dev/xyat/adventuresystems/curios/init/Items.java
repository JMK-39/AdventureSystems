package dev.xyat.adventuresystems.curios.init;

import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.adventuresystems.curios.common.item.DisabledCurioItem;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.heartofsteel.item.HeartOfSteelItem;
import dev.xyat.adventuresystems.curios.levitationbackpack.item.LevitationBackpackItem;
import dev.xyat.adventuresystems.curios.paradiselost.item.ParadiseLostItem;
import dev.xyat.adventuresystems.curios.wallet.item.WalletItem;
import dev.xyat.kineticcore.api.registry.KineticItems;
import dev.xyat.kineticcore.api.registry.KineticRegistryHandle;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import net.minecraft.world.item.Item;

public final class Items {
    public static final KineticRegistryHandle<Item> HEART_OF_STEEL = KineticItems.register(
            KineticResourceIds.of(CuriosModule.MODID, "heart_of_steel"),
            () -> CuriosConfig.enableHeartOfSteel ? new HeartOfSteelItem() : new DisabledCurioItem(new Item.Properties())
    );

    public static final KineticRegistryHandle<Item> PARADISE_LOST = KineticItems.register(
            KineticResourceIds.of(CuriosModule.MODID, "paradise_lost"),
            () -> CuriosConfig.enableParadiseLost ? new ParadiseLostItem() : new DisabledCurioItem(new Item.Properties())
    );

    public static final KineticRegistryHandle<Item> LEVITATION_BACKPACK = KineticItems.register(
            KineticResourceIds.of(CuriosModule.MODID, "levitation_backpack"),
            () -> CuriosConfig.enableLevitationBackpack ? new LevitationBackpackItem() : new DisabledCurioItem(new Item.Properties())
    );

    public static final KineticRegistryHandle<Item> CURRENCY_WALLET = KineticItems.register(
            KineticResourceIds.of(CuriosModule.MODID, "currency_wallet"),
            () -> CuriosConfig.enableCurrencyWallet ? new WalletItem() : new DisabledCurioItem(new Item.Properties())
    );

    private Items() {
    }

    public static void register() {
    }
}
