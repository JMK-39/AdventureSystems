package dev.xyat.adventuresystems.curios.init;

import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.kineticcore.api.registry.KineticRegistryHandle;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import dev.xyat.kineticcore.api.runtime.KineticCreativeTabs;
import dev.xyat.kineticcore.api.text.KineticI18n;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class CreativeTabs {
    public static final KineticRegistryHandle<CreativeModeTab> MAIN_TAB = KineticCreativeTabs.register(
            KineticResourceIds.of(CuriosModule.MODID, "main"),
            () -> CreativeModeTab.builder()
                    .title(KineticI18n.translatable("itemGroup.adventuresystems"))
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
                        if (CuriosConfig.enableCurrencyWallet) {
                            output.accept(Items.CURRENCY_WALLET.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                        }
                    })
                    .build()
    );

    private CreativeTabs() {
    }

    public static void register() {
    }
}
