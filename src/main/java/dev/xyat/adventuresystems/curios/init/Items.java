package dev.xyat.adventuresystems.curios.init;

import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.heartofsteel.item.HeartOfSteelItem;
import dev.xyat.adventuresystems.curios.wallet.item.WalletItem;
import dev.xyat.adventuresystems.curios.common.item.DisabledCurioItem;
import dev.xyat.adventuresystems.curios.levitationbackpack.item.LevitationBackpackItem;
import dev.xyat.adventuresystems.curios.paradiselost.item.ParadiseLostItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Items {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CuriosModule.MODID);


    public static final RegistryObject<Item> HEART_OF_STEEL = ITEMS.register("heart_of_steel",
            () -> CuriosConfig.enableHeartOfSteel ? new HeartOfSteelItem() : new DisabledCurioItem(new Item.Properties()));

    public static final RegistryObject<Item> PARADISE_LOST = ITEMS.register("paradise_lost",
            () -> CuriosConfig.enableParadiseLost ? new ParadiseLostItem() : new DisabledCurioItem(new Item.Properties()));

    public static final RegistryObject<Item> LEVITATION_BACKPACK = ITEMS.register("levitation_backpack",
            () -> CuriosConfig.enableLevitationBackpack ? new LevitationBackpackItem() : new DisabledCurioItem(new Item.Properties()));

    public static final RegistryObject<Item> CURRENCY_WALLET = ITEMS.register("currency_wallet",
            () -> CuriosConfig.enableCurrencyWallet ? new WalletItem() : new DisabledCurioItem(new Item.Properties()));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
