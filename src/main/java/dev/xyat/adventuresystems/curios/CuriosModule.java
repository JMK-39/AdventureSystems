package dev.xyat.adventuresystems.curios;

import com.mojang.logging.LogUtils;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.config.CuriosConfigGui;
import dev.xyat.adventuresystems.curios.wallet.client.Client;
import dev.xyat.adventuresystems.curios.wallet.compat.ftb.FTBCompat;
import dev.xyat.adventuresystems.curios.wallet.client.hud.Hud;
import dev.xyat.adventuresystems.curios.common.client.TooltipEventHandler;
import dev.xyat.adventuresystems.curios.common.event.CurioConflictHandler;
import dev.xyat.adventuresystems.curios.heartofsteel.event.HeartOfSteelModule;
import dev.xyat.adventuresystems.curios.levitationbackpack.event.KnockbackImmunityHandler;
import dev.xyat.adventuresystems.curios.paradiselost.event.ParadiseLostModule;
import dev.xyat.adventuresystems.curios.paradiselost.data.ParadiseLostCurve;
import dev.xyat.adventuresystems.curios.wallet.event.WalletModule;
import dev.xyat.adventuresystems.curios.heartofsteel.init.SoundEvents;
import dev.xyat.adventuresystems.curios.init.CreativeTabs;
import dev.xyat.adventuresystems.curios.init.Items;
import dev.xyat.adventuresystems.curios.wallet.network.Network;
import dev.xyat.kineticcore.api.config.server.KTServerConfigApi;
import dev.xyat.kineticcore.api.config.server.KTServerConfigSpec;
import dev.xyat.kineticcore.api.runtime.KineticPlatform;
import org.slf4j.Logger;

public final class CuriosModule {
    public static final String MODID = "adventuresystems";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CuriosModule() {
        CuriosConfig.load();
        KTServerConfigApi.register(KTServerConfigSpec.builder("adventuresystems:mechanics")
                .booleanValue("enable_heart_of_steel", () -> CuriosConfig.enableHeartOfSteel, value -> CuriosConfig.enableHeartOfSteel = value)
                .booleanValue("enable_paradise_lost", () -> CuriosConfig.enableParadiseLost, value -> CuriosConfig.enableParadiseLost = value)
                .booleanValue("enable_levitation_backpack", () -> CuriosConfig.enableLevitationBackpack, value -> CuriosConfig.enableLevitationBackpack = value)
                .booleanValue("enable_currency_wallet", () -> CuriosConfig.enableCurrencyWallet, value -> CuriosConfig.enableCurrencyWallet = value)
                .booleanValue("levitation_knockback_immunity", () -> CuriosConfig.levitationKnockbackImmunity, value -> CuriosConfig.levitationKnockbackImmunity = value)
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
                .doubleValue("hos_heal_activation_threshold", () -> CuriosConfig.hosHealActivationThreshold, value -> CuriosConfig.hosHealActivationThreshold = value, -Double.MAX_VALUE, Double.MAX_VALUE)
                .stringList("hos_conflicts", () -> CuriosConfig.hosConflicts, value -> CuriosConfig.hosConflicts = value)
                .intValue("pl_min_nutrition", () -> CuriosConfig.plMinNutrition, value -> CuriosConfig.plMinNutrition = value, Integer.MIN_VALUE, Integer.MAX_VALUE)
                .doubleValue("pl_score_multiplier", () -> CuriosConfig.plScoreMultiplier, value -> CuriosConfig.plScoreMultiplier = value, -Double.MAX_VALUE, Double.MAX_VALUE)
                .stringListValidated("pl_curve_points", () -> CuriosConfig.plCurvePoints, value -> CuriosConfig.plCurvePoints = value, ParadiseLostCurve::isValidDefinitionList)
                .choiceValue("pl_curve_mode", () -> CuriosConfig.plCurveMode, value -> CuriosConfig.plCurveMode = value, "sine_ease_out", "linear", "smoothstep", "power")
                .doubleValue("pl_curve_power", () -> CuriosConfig.plCurvePower, value -> CuriosConfig.plCurvePower = value, 0.000001D, Double.MAX_VALUE)
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
        Items.register();
        CreativeTabs.register();
        SoundEvents.register();
        Network.register();
        CurioConflictHandler.install();
        HeartOfSteelModule.install();
        KnockbackImmunityHandler.install();
        ParadiseLostModule.install();
        WalletModule.install();
        FTBCompat.install();
        KineticPlatform.runOnClient(() -> () -> {
            CuriosConfigGui.load();
            TooltipEventHandler.install();
            Client.install();
            Hud.install();
        });
    }
}
