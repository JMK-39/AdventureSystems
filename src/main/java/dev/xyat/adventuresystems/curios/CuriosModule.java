package dev.xyat.adventuresystems.curios;

import com.mojang.logging.LogUtils;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.config.CuriosConfigGui;
import dev.xyat.adventuresystems.curios.heartofsteel.init.SoundEvents;
import dev.xyat.adventuresystems.curios.init.CreativeTabs;
import dev.xyat.adventuresystems.curios.init.Items;
import dev.xyat.adventuresystems.curios.wallet.network.Network;
import dev.xyat.kineticcore.config.server.KTServerConfigApi;
import dev.xyat.kineticcore.config.server.KTServerConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

public final class CuriosModule {
    public static final String MODID = "adventuresystems";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CuriosModule() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        CuriosConfig.load();
        KTServerConfigApi.register(KTServerConfigSpec.builder("adventuresystems:mechanics")
                .booleanValue("enable_heart_of_steel", () -> CuriosConfig.enableHeartOfSteel, value -> CuriosConfig.enableHeartOfSteel = value)
                .booleanValue("enable_paradise_lost", () -> CuriosConfig.enableParadiseLost, value -> CuriosConfig.enableParadiseLost = value)
                .booleanValue("enable_levitation_backpack", () -> CuriosConfig.enableLevitationBackpack, value -> CuriosConfig.enableLevitationBackpack = value)
                .booleanValue("enable_currency_wallet", () -> CuriosConfig.enableCurrencyWallet, value -> CuriosConfig.enableCurrencyWallet = value)
                .intValue("hos_growth_interval", () -> CuriosConfig.hosGrowthInterval, value -> CuriosConfig.hosGrowthInterval = value, Integer.MIN_VALUE, Integer.MAX_VALUE)
                .doubleValue("hos_damage_cap", () -> CuriosConfig.hosDamageCap, value -> CuriosConfig.hosDamageCap = value, -Double.MAX_VALUE, Double.MAX_VALUE)
                .intValue("hos_max_health_cap", () -> CuriosConfig.hosMaxHealthCap, value -> CuriosConfig.hosMaxHealthCap = value, Integer.MIN_VALUE, Integer.MAX_VALUE)
                .intValue("hos_stacks_per_hp", () -> CuriosConfig.hosStacksPerHp, value -> CuriosConfig.hosStacksPerHp = value, 1, Integer.MAX_VALUE)
                .doubleValue("hos_base_health", () -> CuriosConfig.hosBaseHealth, value -> CuriosConfig.hosBaseHealth = value, -Double.MAX_VALUE, Double.MAX_VALUE)
                .doubleValue("hos_heal_multiplier", () -> CuriosConfig.hosHealMultiplier, value -> CuriosConfig.hosHealMultiplier = value, -Double.MAX_VALUE, Double.MAX_VALUE)
                .intValue("hos_damage_window_ticks", () -> CuriosConfig.hosDamageWindowTicks, value -> CuriosConfig.hosDamageWindowTicks = value, Integer.MIN_VALUE, Integer.MAX_VALUE)
                .intValue("hos_min_gain", () -> CuriosConfig.hosMinGain, value -> CuriosConfig.hosMinGain = value, Integer.MIN_VALUE, Integer.MAX_VALUE)
                .intValue("hos_max_gain", () -> CuriosConfig.hosMaxGain, value -> CuriosConfig.hosMaxGain = value, Integer.MIN_VALUE, Integer.MAX_VALUE)
                .doubleValue("hos_damage_per_hp", () -> CuriosConfig.hosDamagePerHp, value -> CuriosConfig.hosDamagePerHp = value, -Double.MAX_VALUE, Double.MAX_VALUE)
                .stringList("hos_conflicts", () -> CuriosConfig.hosConflicts, value -> CuriosConfig.hosConflicts = value)
                .intValue("pl_min_nutrition", () -> CuriosConfig.plMinNutrition, value -> CuriosConfig.plMinNutrition = value, Integer.MIN_VALUE, Integer.MAX_VALUE)
                .stringList("pl_conflicts", () -> CuriosConfig.plConflicts, value -> CuriosConfig.plConflicts = value)
                .intValue("wallet_extra_belt_slots", () -> CuriosConfig.walletExtraBeltSlots, value -> CuriosConfig.walletExtraBeltSlots = value, Integer.MIN_VALUE, Integer.MAX_VALUE)
                .intValue("wallet_max_currency_types", () -> CuriosConfig.walletMaxCurrencyTypes, CuriosConfig::setWalletMaxCurrencyTypes, Integer.MIN_VALUE, Integer.MAX_VALUE)
                .doubleValue("wallet_magnet_range", () -> CuriosConfig.walletMagnetRange, value -> CuriosConfig.walletMagnetRange = value, -Double.MAX_VALUE, Double.MAX_VALUE)
                .intValue("wallet_magnet_interval", () -> CuriosConfig.walletMagnetIntervalTicks, value -> CuriosConfig.walletMagnetIntervalTicks = value, Integer.MIN_VALUE, Integer.MAX_VALUE)
                .stringList("wallet_currency_definitions", () -> CuriosConfig.walletCurrencyDefinitions, CuriosConfig::setWalletCurrencyDefinitions)
                .stringList("wallet_exchange_rules", () -> CuriosConfig.walletExchangeRules, CuriosConfig::setWalletExchangeRules)
                .stringList("wallet_manual_only", () -> CuriosConfig.walletManualExchangeOnlyCurrencies, CuriosConfig::setWalletManualExchangeOnlyCurrencies)
                .onSave(CuriosConfig::saveServerSettings)
                .build());
        KTServerConfigApi.registerActionPage("adventuresystems:shop_editor");
        Items.register(modEventBus);
        CreativeTabs.register(modEventBus);
        SoundEvents.register(modEventBus);
        Network.register();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> CuriosConfigGui::load);
    }
}
