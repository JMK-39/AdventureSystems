package dev.xyat.adventuresystems.curios.config;

import dev.xyat.kineticcore.api.config.client.KTConfigApi;
import dev.xyat.kineticcore.api.config.client.KTConfigPage;
import dev.xyat.kineticcore.api.config.client.KTConfigScope;
import dev.xyat.adventuresystems.curios.wallet.client.Client;
import dev.xyat.adventuresystems.curios.paradiselost.data.ParadiseLostCurve;
import dev.xyat.adventuresystems.curios.wallet.client.hud.WalletHudEditorScreen;
import net.minecraft.network.chat.Component;
import dev.xyat.adventuresystems.text.AdventureText;

/** Registers CuriosModule configuration pages in KineticCore's config hub. */
public final class CuriosConfigGui {
    public static final String HUD_PAGE_ID = "adventuresystems:wallet_hud";
    public static final String MECHANICS_PAGE_ID = "adventuresystems:mechanics";
    public static final String SHOP_PAGE_ID = "adventuresystems:shop_editor";
    public static final String WINGS_PAGE_ID = "adventuresystems:wind_thunder_wings";

    private static final String KEY = "cfg.adventuresystems.curios.";

    private CuriosConfigGui() {
    }

    public static void load() {
        registerHudPage();
        registerMechanicsPage();
        registerShopPage();
        registerWingsPage();
    }

    private static void registerHudPage() {
        KTConfigApi.register(KTConfigPage.builder(HUD_PAGE_ID, text("hud.title"))
                .scope(KTConfigScope.CLIENT_LOCAL)
                .pageDescription(text("hud.description"))
                .applyTiming(KTConfigPage.ApplyTiming.IMMEDIATE)
                .divider()
                .action("open_editor", text("hud.open_editor"),
                        KTConfigApi.pageAction(WalletHudEditorScreen::new),
                        tooltip("hud.open_editor"))
                .build());
    }

    private static void registerMechanicsPage() {
        KTConfigApi.register(KTConfigPage.builder(MECHANICS_PAGE_ID, text("mechanics.title"))
                .scope(KTConfigScope.SERVER_AUTHORITATIVE)
                .serverManaged()
                .pageDescription(text("mechanics.description"))
                .applyTiming(KTConfigPage.ApplyTiming.MIXED)
                .applyNotice(text("mechanics.apply_notice"))
                .divider()
                .booleanValue("enable_heart_of_steel", text("general.enable_heart_of_steel"),
                        () -> CuriosConfig.enableHeartOfSteel, value -> CuriosConfig.enableHeartOfSteel = value,
                        true, tooltip("general.enable_heart_of_steel"))
                .booleanValue("enable_paradise_lost", text("general.enable_paradise_lost"),
                        () -> CuriosConfig.enableParadiseLost, value -> CuriosConfig.enableParadiseLost = value,
                        true, tooltip("general.enable_paradise_lost"))
                .booleanValue("enable_levitation_backpack", text("general.enable_levitation_backpack"),
                        () -> CuriosConfig.enableLevitationBackpack, value -> CuriosConfig.enableLevitationBackpack = value,
                        true, tooltip("general.enable_levitation_backpack"))
                .booleanValue("enable_currency_wallet", text("general.enable_currency_wallet"),
                        () -> CuriosConfig.enableCurrencyWallet, value -> CuriosConfig.enableCurrencyWallet = value,
                        true, tooltip("general.enable_currency_wallet"))
                .booleanValue("enable_wind_thunder_wings", text("general.enable_wind_thunder_wings"),
                        () -> CuriosConfig.enableWindThunderWings, value -> CuriosConfig.enableWindThunderWings = value,
                        true, tooltip("general.enable_wind_thunder_wings"))
                .divider()
                .booleanValue("levitation_knockback_immunity", text("levitation_backpack.knockback_immunity"),
                        () -> CuriosConfig.levitationKnockbackImmunity, value -> CuriosConfig.levitationKnockbackImmunity = value,
                        true, tooltip("levitation_backpack.knockback_immunity"))
                .divider()
                .intValueUnbounded("hos_growth_interval", text("heart_of_steel.growth_interval"),
                        () -> CuriosConfig.hosGrowthInterval, value -> CuriosConfig.hosGrowthInterval = value,
                        3, tooltip("heart_of_steel.growth_interval"))
                .doubleValueUnbounded("hos_damage_cap", text("heart_of_steel.damage_cap"),
                        () -> CuriosConfig.hosDamageCap, value -> CuriosConfig.hosDamageCap = value,
                        3.0D, tooltip("heart_of_steel.damage_cap"))
                .intValueUnbounded("hos_max_health_cap", text("heart_of_steel.max_health_cap"),
                        () -> CuriosConfig.hosMaxHealthCap, value -> CuriosConfig.hosMaxHealthCap = value,
                        200, tooltip("heart_of_steel.max_health_cap"))
                .intValue("hos_stacks_per_hp", text("heart_of_steel.stacks_per_hp"),
                        () -> CuriosConfig.hosStacksPerHp, value -> CuriosConfig.hosStacksPerHp = value,
                        100, 1, Integer.MAX_VALUE, tooltip("heart_of_steel.stacks_per_hp"))
                .doubleValueUnbounded("hos_base_health", text("heart_of_steel.base_health"),
                        () -> CuriosConfig.hosBaseHealth, value -> CuriosConfig.hosBaseHealth = value,
                        10.0D, tooltip("heart_of_steel.base_health"))
                .doubleValueUnbounded("hos_heal_multiplier", text("heart_of_steel.heal_multiplier"),
                        () -> CuriosConfig.hosHealMultiplier, value -> CuriosConfig.hosHealMultiplier = value,
                        2.0D, tooltip("heart_of_steel.heal_multiplier"))
                .intValueUnbounded("hos_damage_window_ticks", text("heart_of_steel.damage_window_ticks"),
                        () -> CuriosConfig.hosDamageWindowTicks, value -> CuriosConfig.hosDamageWindowTicks = value,
                        5, tooltip("heart_of_steel.damage_window_ticks"))
                .intValueUnbounded("hos_min_gain", text("heart_of_steel.min_gain"),
                        () -> CuriosConfig.hosMinGain, value -> CuriosConfig.hosMinGain = value,
                        2, tooltip("heart_of_steel.min_gain"))
                .intValueUnbounded("hos_max_gain", text("heart_of_steel.max_gain"),
                        () -> CuriosConfig.hosMaxGain, value -> CuriosConfig.hosMaxGain = value,
                        5, tooltip("heart_of_steel.max_gain"))
                .doubleValueUnbounded("hos_damage_per_hp", text("heart_of_steel.damage_per_hp"),
                        () -> CuriosConfig.hosDamagePerHp, value -> CuriosConfig.hosDamagePerHp = value,
                        0.005D, tooltip("heart_of_steel.damage_per_hp"))
                .doubleValueUnbounded("hos_heal_activation_threshold", text("heart_of_steel.heal_activation_threshold"),
                        () -> CuriosConfig.hosHealActivationThreshold, value -> CuriosConfig.hosHealActivationThreshold = value,
                        0.01D, tooltip("heart_of_steel.heal_activation_threshold"))
                .itemList("hos_conflicts", text("heart_of_steel.conflicts"),
                        () -> CuriosConfig.hosConflicts, value -> CuriosConfig.hosConflicts = value,
                        java.util.List.of("enigmaticlegacy:cursed_ring"), tooltip("heart_of_steel.conflicts"))
                .divider()
                .intValueUnbounded("pl_min_nutrition", text("paradise_lost.min_nutrition"),
                        () -> CuriosConfig.plMinNutrition, value -> CuriosConfig.plMinNutrition = value,
                        0, tooltip("paradise_lost.min_nutrition"))
                .doubleValueUnbounded("pl_score_multiplier", text("paradise_lost.score_multiplier"),
                        () -> CuriosConfig.plScoreMultiplier, value -> CuriosConfig.plScoreMultiplier = value,
                        1.0D, tooltip("paradise_lost.score_multiplier"))
                .stringListValidated("pl_curve_points", text("paradise_lost.curve_points"),
                        () -> CuriosConfig.plCurvePoints, value -> CuriosConfig.plCurvePoints = value,
                        ParadiseLostCurve.DEFAULT_DEFINITIONS, ParadiseLostCurve::isValidDefinitionList,
                        tooltip("paradise_lost.curve_points"))
                .translatedChoice("pl_curve_mode", text("paradise_lost.curve_mode"),
                        () -> CuriosConfig.plCurveMode, value -> CuriosConfig.plCurveMode = value,
                        "sine_ease_out", tooltip("paradise_lost.curve_mode"),
                        KEY + "paradise_lost.curve_mode.option",
                        "sine_ease_out", "linear", "smoothstep", "power")
                .doubleValue("pl_curve_power", text("paradise_lost.curve_power"),
                        () -> CuriosConfig.plCurvePower, value -> CuriosConfig.plCurvePower = value,
                        2.0D, 0.000001D, Double.MAX_VALUE, tooltip("paradise_lost.curve_power"))
                .itemList("pl_conflicts", text("paradise_lost.conflicts"),
                        () -> CuriosConfig.plConflicts, value -> CuriosConfig.plConflicts = value,
                        java.util.List.of("enigmaticlegacy:cursed_ring"), tooltip("paradise_lost.conflicts"))
                .divider()
                .doubleValue("wtw_normal_speed", text("wind_thunder_wings.normal_speed"),
                        () -> CuriosConfig.wtwNormalSpeed, value -> CuriosConfig.wtwNormalSpeed = value,
                        2.0D, 1.0D, 100.0D, tooltip("wind_thunder_wings.normal_speed"))
                .doubleValue("wtw_boost_speed", text("wind_thunder_wings.boost_speed"),
                        () -> CuriosConfig.wtwBoostSpeed, value -> CuriosConfig.wtwBoostSpeed = value,
                        6.0D, 1.0D, 100.0D, tooltip("wind_thunder_wings.boost_speed"))
                .doubleValue("wtw_burn_speed", text("wind_thunder_wings.burn_speed"),
                        () -> CuriosConfig.wtwBurnSpeed, value -> CuriosConfig.wtwBurnSpeed = value,
                        20.0D, 1.0D, 100.0D, tooltip("wind_thunder_wings.burn_speed"))
                .intValue("wtw_burn_seconds", text("wind_thunder_wings.burn_seconds"),
                        () -> CuriosConfig.wtwBurnSeconds, value -> CuriosConfig.wtwBurnSeconds = value,
                        10, 1, 600, tooltip("wind_thunder_wings.burn_seconds"))
                .doubleValue("wtw_burn_health_cost", text("wind_thunder_wings.burn_health_cost"),
                        () -> CuriosConfig.wtwBurnHealthCost, value -> CuriosConfig.wtwBurnHealthCost = value,
                        0.30D, 0.0D, 1.0D, tooltip("wind_thunder_wings.burn_health_cost"))
                .doubleValue("wtw_burn_min_health", text("wind_thunder_wings.burn_min_health"),
                        () -> CuriosConfig.wtwBurnMinHealth, value -> CuriosConfig.wtwBurnMinHealth = value,
                        0.30D, 0.0D, 1.0D, tooltip("wind_thunder_wings.burn_min_health"))
                .intValue("wtw_burn_cooldown", text("wind_thunder_wings.burn_cooldown"),
                        () -> CuriosConfig.wtwBurnCooldown, value -> CuriosConfig.wtwBurnCooldown = value,
                        15, 0, 3600, tooltip("wind_thunder_wings.burn_cooldown"))
                .divider()
                .intValueUnbounded("wallet_extra_belt_slots", text("wallet.extra_belt_slots"),
                        () -> CuriosConfig.walletExtraBeltSlots, value -> CuriosConfig.walletExtraBeltSlots = value,
                        1, tooltip("wallet.extra_belt_slots"))
                // Each coin item as an item slot with its value in Adventure Coins beside it.
                .itemValueList("wallet_currency_definitions", text("wallet.currency_definitions"),
                        () -> CuriosConfig.walletCurrencyDefinitions, CuriosConfig::setWalletCurrencyDefinitions,
                        CuriosConfig.DEFAULT_WALLET_CURRENCY_DEFINITIONS, CuriosConfigGui::validCurrencyDefinitions,
                        tooltip("wallet.currency_definitions"))
                .build());
    }

    /** The look of the Wind-Thunder Wings on this client: colours, trail, wings, sparks and sounds. */
    private static void registerWingsPage() {
        KTConfigApi.register(KTConfigPage.builder(WINGS_PAGE_ID, text("wind_thunder_wings.look.title"))
                .scope(KTConfigScope.CLIENT_LOCAL)
                .pageDescription(text("wind_thunder_wings.look.description"))
                .applyTiming(KTConfigPage.ApplyTiming.IMMEDIATE)
                .color("wing_color", text("wind_thunder_wings.wing_color"),
                        () -> CuriosConfig.wtwWingColor, value -> CuriosConfig.wtwWingColor = value,
                        0x2FA3AE, tooltip("wind_thunder_wings.wing_color"))
                .color("thunder_color", text("wind_thunder_wings.thunder_color"),
                        () -> CuriosConfig.wtwThunderColor, value -> CuriosConfig.wtwThunderColor = value,
                        0xD6ECFF, tooltip("wind_thunder_wings.thunder_color"))
                .color("boost_color", text("wind_thunder_wings.boost_color"),
                        () -> CuriosConfig.wtwBoostColor, value -> CuriosConfig.wtwBoostColor = value,
                        0x3CFF8A, tooltip("wind_thunder_wings.boost_color"))
                .color("burn_color", text("wind_thunder_wings.burn_color"),
                        () -> CuriosConfig.wtwBurnColor, value -> CuriosConfig.wtwBurnColor = value,
                        0xD8101C, tooltip("wind_thunder_wings.burn_color"))
                .doubleValue("trail_seconds", text("wind_thunder_wings.trail_seconds"),
                        () -> CuriosConfig.wtwTrailSeconds, value -> CuriosConfig.wtwTrailSeconds = value,
                        0.8D, 0.05D, 5.0D, tooltip("wind_thunder_wings.trail_seconds"))
                .doubleValue("trail_width", text("wind_thunder_wings.trail_width"),
                        () -> CuriosConfig.wtwTrailWidth, value -> CuriosConfig.wtwTrailWidth = value,
                        1.0D, 0.1D, 4.0D, tooltip("wind_thunder_wings.trail_width"))
                .booleanValue("show_wings", text("wind_thunder_wings.show_wings"),
                        () -> CuriosConfig.wtwShowWings, value -> CuriosConfig.wtwShowWings = value,
                        true, tooltip("wind_thunder_wings.show_wings"))
                .booleanValue("sparks", text("wind_thunder_wings.sparks"),
                        () -> CuriosConfig.wtwSparks, value -> CuriosConfig.wtwSparks = value,
                        true, tooltip("wind_thunder_wings.sparks"))
                .booleanValue("sounds", text("wind_thunder_wings.sounds"),
                        () -> CuriosConfig.wtwSounds, value -> CuriosConfig.wtwSounds = value,
                        true, tooltip("wind_thunder_wings.sounds"))
                .onSave(CuriosConfig::saveClientSettings)
                .build());
    }

    /** Every row is "itemId|value" with a whole value of at least 1. */
    private static boolean validCurrencyDefinitions(java.util.List<String> rows) {
        for (String row : rows) {
            int bar = row.lastIndexOf('|');
            if (bar <= 0) return false;
            try {
                if (Long.parseLong(row.substring(bar + 1).trim()) < 1L) return false;
            } catch (NumberFormatException invalid) {
                return false;
            }
        }
        return true;
    }

    private static void registerShopPage() {
        KTConfigApi.register(KTConfigPage.builder(SHOP_PAGE_ID, text("shop.title"))
                .displayOrder(-100)
                .scope(KTConfigScope.SERVER_AUTHORITATIVE)
                .serverManaged()
                .pageDescription(text("shop.description"))
                .applyTiming(KTConfigPage.ApplyTiming.IMMEDIATE)
                .action("open_shop_editor", text("shop.open"), Client::requestOpenShopEditor,
                        tooltip("shop.open"))
                .build());
    }

    private static Component text(String path) {
        return AdventureText.translatable(KEY + path);
    }

    private static Component tooltip(String path) {
        return text(path + ".tooltip");
    }
}
