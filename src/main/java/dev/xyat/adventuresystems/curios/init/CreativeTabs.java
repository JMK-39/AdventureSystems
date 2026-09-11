package dev.xyat.adventuresystems.curios.init;

import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CuriosModule.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.adventuresystems"))
            .icon(() -> {
                if (CuriosConfig.enableHeartOfSteel) return new ItemStack(Items.HEART_OF_STEEL.get());
                if (CuriosConfig.enableParadiseLost) return new ItemStack(Items.PARADISE_LOST.get());
                if (CuriosConfig.enableCurrencyWallet) return new ItemStack(Items.CURRENCY_WALLET.get());
                if (CuriosConfig.enableLevitationBackpack) return new ItemStack(Items.LEVITATION_BACKPACK.get());
                return new ItemStack(net.minecraft.world.item.Items.NETHER_STAR);
            })
            .displayItems((parameters, output) -> {
                if (CuriosConfig.enableHeartOfSteel) output.accept(Items.HEART_OF_STEEL.get());
                if (CuriosConfig.enableParadiseLost) output.accept(Items.PARADISE_LOST.get());
                if (CuriosConfig.enableLevitationBackpack) output.accept(Items.LEVITATION_BACKPACK.get());
                if (CuriosConfig.enableCurrencyWallet) output.accept(Items.CURRENCY_WALLET.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            })
            .build());

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
