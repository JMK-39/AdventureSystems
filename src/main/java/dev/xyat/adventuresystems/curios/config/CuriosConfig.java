package dev.xyat.adventuresystems.curios.config;

import dev.xyat.kineticcore.api.runtime.KineticPaths;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.adventuresystems.curios.paradiselost.data.ParadiseLostCurve;
import dev.xyat.adventuresystems.curios.wallet.data.Data;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class CuriosConfig {
    private static final String CONFIG_FILE = "kineticcore/curios.toml";
    private static final Path CONFIG_PATH = KineticPaths.configFile(CONFIG_FILE);
    private static CommentedFileConfig configData;

    public static final List<String> DEFAULT_WALLET_CURRENCY_DEFINITIONS = Arrays.asList(
            "kubejs:gold_coin|1",
            "kubejs:diamond_coin|100",
            "kubejs:netherite_coin|1000",
            "kubejs:monster_coin|10000"
    );

    public static final List<String> DEFAULT_WALLET_EXCHANGE_RULES = Arrays.asList(
            "kubejs:gold_coin->kubejs:diamond_coin",
            "kubejs:diamond_coin->kubejs:gold_coin",
            "kubejs:diamond_coin->kubejs:netherite_coin",
            "kubejs:netherite_coin->kubejs:diamond_coin",
            "kubejs:netherite_coin->kubejs:gold_coin",
            "kubejs:monster_coin->kubejs:netherite_coin",
            "kubejs:monster_coin->kubejs:diamond_coin",
            "kubejs:monster_coin->kubejs:gold_coin"
    );

    public static final List<String> DEFAULT_WALLET_MANUAL_EXCHANGE_ONLY_CURRENCIES = Arrays.asList(
            "kubejs:monster_coin"
    );

    public static boolean enableHeartOfSteel = true;
    public static boolean enableParadiseLost = true;
    public static boolean enableLevitationBackpack = true;
    public static boolean enableCurrencyWallet = true;
    public static boolean levitationKnockbackImmunity = true;

    public static int hosGrowthInterval = 3;
    public static double hosDamageCap = 3.0;
    public static int hosMaxHealthCap = 200;
    public static int hosStacksPerHp = 100;
    public static double hosBaseHealth = 10.0;
    public static double hosHealMultiplier = 2.0;
    public static int hosDamageWindowTicks = 5;
    public static int hosMinGain = 2;
    public static int hosMaxGain = 5;
    public static double hosDamagePerHp = 0.005;
    public static double hosHealActivationThreshold = 0.01;
    public static List<String> hosConflicts = Arrays.asList("enigmaticlegacy:cursed_ring");

    public static int plMinNutrition = 0;
    public static double plScoreMultiplier = 1.0;
    public static List<String> plCurvePoints = ParadiseLostCurve.DEFAULT_DEFINITIONS;
    public static String plCurveMode = "sine_ease_out";
    public static double plCurvePower = 2.0;
    public static List<String> plConflicts = Arrays.asList("enigmaticlegacy:cursed_ring");

    public static int walletExtraBeltSlots = 1;
    public static int walletMaxCurrencyTypes = 10;
    public static double walletMagnetRange = 6.0;
    public static int walletMagnetIntervalTicks = 10;
    public static int walletHudOffsetX = 0;
    public static int walletHudOffsetY = 0;
    public static double walletHudScale = 0.75;
    public static List<String> walletCurrencyDefinitions = DEFAULT_WALLET_CURRENCY_DEFINITIONS;
    public static List<String> walletExchangeRules = DEFAULT_WALLET_EXCHANGE_RULES;
    public static List<String> walletManualExchangeOnlyCurrencies = DEFAULT_WALLET_MANUAL_EXCHANGE_ONLY_CURRENCIES;

    static {
        load();
    }

    public static void load() {
        if (configData != null) return;
        try {
            KineticPaths.ensureConfigDirectory("kineticcore");
            Config old = Config.inMemory();
            if (KineticPaths.configFileExists(CONFIG_FILE)) {
                try (FileConfig oldFile = FileConfig.of(CONFIG_PATH)) {
                    oldFile.load();
                    old.putAll(oldFile);
                }
            }
            configData = CommentedFileConfig.builder(CONFIG_PATH).sync().preserveInsertionOrder().writingMode(WritingMode.REPLACE).build();
            setup(old);
            configData.save();
            readValues();
        } catch (Exception e) {
            discardBrokenConfig();
            CuriosModule.LOGGER.error("Failed to load CuriosModule config", e);
        }
    }

    private static void discardBrokenConfig() {
        if (configData == null) return;
        try {
            configData.close();
        } catch (Throwable ignored) {
        }
        configData = null;
    }

    private static void setup(Config old) {
        configData.setComment("general", """
                模块总开关 (修改后需重启游戏生效)
                Module Toggles (Game restart required after changes)""");

        define(old, "general.enable_heart_of_steel", true, """
                启用 心之钢 饰品
                Enable the Heart of Steel accessory""");

        define(old, "general.enable_paradise_lost", true, """
                启用 失乐园 饰品
                Enable the Paradise Lost accessory""");

        define(old, "general.enable_levitation_backpack", true, """
                启用 悬浮背包 饰品
                Enable the Levitation Backpack accessory""");

        define(old, "general.enable_currency_wallet", true, """
                启用 钱包袋子 饰品
                Enable the Currency Wallet accessory""");

        define(old, "levitation_backpack.knockbackImmunity", true, """
                悬浮背包在玩家处于飞行状态时是否提供击退免疫。
                Whether the Levitation Backpack grants knockback immunity while the player is flying.""");

        configData.setComment("heart_of_steel", """
                心之钢机制设定
                Heart of Steel Mechanics Settings""");

        define(old, "heart_of_steel.growthInterval", 3, """
                触发充能攻击的冷却时间（单位：秒）。
                Cooldown time in seconds for charged attacks.
                示例: 3 = 每3秒才能获取一次层数
                Example: 3 = Gain stacks max once every 3s""");

        define(old, "heart_of_steel.damageCap", 3.0, """
                心之钢能够提供的最大额外伤害绝对值上限。
                Absolute maximum limit for bonus damage provided.
                示例: 3.0 = 最多只能为你增加3点最终伤害
                Example: 3.0 = Max +3.0 total damage""");

        define(old, "heart_of_steel.maxHealthCap", 200, """
                通过不断叠加层数，能获取的最大额外生命值上限。
                Maximum bonus health obtainable from stacking.
                示例: 200 = 最多额外增加200点生命值上限
                Example: 200 = Max +200 HP limit""");

        define(old, "heart_of_steel.stacksPerHp", 100, """
                每积累多少层数，转化为 1 点最大生命值。
                Number of stacks required to gain 1 Max HP.
                示例: 100 = 打够100层增加1点生命值
                Example: 100 = 100 stacks = +1 HP""");

        define(old, "heart_of_steel.baseHealth", 10.0, """
                佩戴心之钢时直接提供的基础生命值加成（无需叠层）。
                Base bonus health granted simply by wearing the item.""");

        define(old, "heart_of_steel.healMultiplier", 2.0, """
                触发充能攻击时，基于造成伤害恢复生命值的倍率。
                Healing multiplier based on damage dealt during a charged attack.""");

        define(old, "heart_of_steel.damageWindowTicks", 5, """
                触发充能攻击后，允许享受额外伤害加成的持续时间（单位：Tick，20 Tick = 1秒）。
                Duration of the damage bonus window after a charged attack (20 Ticks = 1s).""");

        define(old, "heart_of_steel.minGain", 2, """
                单次成功触发充能攻击时，随机获取的最小层数。
                Minimum stacks gained on a successful charged attack.""");

        define(old, "heart_of_steel.maxGain", 5, """
                单次成功触发充能攻击时，随机获取的最大层数。
                Maximum stacks gained on a successful charged attack.""");

        define(old, "heart_of_steel.damagePerHp", 0.005, """
                基于玩家最大生命值，转化为额外伤害的比例。
                Percentage of Max HP converted to extra damage.
                示例: 0.005 = 0.5% (若玩家有200血, 200 * 0.5% = 增加1点额外伤害)
                Example: 0.005 = 0.5% (If player has 200 HP, 200 * 0.5% = +1 bonus damage)""");

        define(old, "heart_of_steel.healActivationThreshold", 0.01, """
                治疗加成实际生效所需的最小倍率。
                Minimum healing bonus multiplier required before the bonus is applied.
                示例: 0.01 = 治疗加成至少达到 1% 才生效
                Example: 0.01 = Healing bonus must reach at least 1% to apply""");

        define(old, "heart_of_steel.conflicts", Arrays.asList("enigmaticlegacy:cursed_ring"), """
                排斥的饰品ID列表。如果玩家佩戴了列表中的饰品，心之钢将失效。
                List of incompatible accessory IDs. Heart of Steel won't work if these are equipped.
                示例: ["enigmaticlegacy:cursed_ring", "minecraft:apple"]
                Example: ["enigmaticlegacy:cursed_ring", "minecraft:apple"]""");

        configData.setComment("paradise_lost", """
                失乐园机制设定
                Paradise Lost Mechanics Settings""");

        define(old, "paradise_lost.minNutrition", 0, """
                忽略营养值（恢复的饱食度）低于或等于此值的食物，它们不会被计入多样性。
                Foods restoring this much or less hunger will be ignored for variety calculation.
                示例: 0 = 不恢复饱食度的物品不生效
                Example: 0 = Ignore items that do not restore hunger""");

        define(old, "paradise_lost.scoreMultiplier", 1.0, """
                新食物提供的失乐园积分倍率。实际增加积分 = 食物营养值 × 此倍率，再四舍五入为整数。
                Paradise Lost score multiplier for newly discovered foods. Added score = food nutrition × this multiplier, rounded to an integer.
                默认 1.0 保持原有 1:1 积分规则。
                Default 1.0 preserves the original 1:1 score rule.""");

        define(old, "paradise_lost.curvePoints", ParadiseLostCurve.DEFAULT_DEFINITIONS, """
                失乐园积分到伤害加成的曲线节点，格式为 积分|加成倍率。必须至少两行且包含 0 积分起点，积分不能重复。最后一行同时决定积分上限和最终伤害加成上限。
                Paradise Lost score-to-damage curve points in score|bonus format. At least two points are required and score 0 must exist. Scores must be unique. The final point defines both the score cap and final bonus cap.
                示例: 30000|2.0 = 30000 积分时最终额外伤害为 200%
                Example: 30000|2.0 = +200% final damage at 30000 score""");

        define(old, "paradise_lost.curveMode", "sine_ease_out", """
                曲线节点之间的插值方式。可选 sine_ease_out、linear、smoothstep、power。
                Interpolation mode between curve points: sine_ease_out, linear, smoothstep, or power.""");

        define(old, "paradise_lost.curvePower", 2.0, """
                当曲线模式为 power 时使用的指数。必须大于 0。
                Exponent used when curve mode is power. Must be greater than 0.""");

        define(old, "paradise_lost.conflicts", Arrays.asList("enigmaticlegacy:cursed_ring"), """
                排斥的饰品ID列表。如果玩家佩戴了列表中的饰品，失乐园将失效。
                List of incompatible accessory IDs. Paradise Lost won't work if these are equipped.""");

        configData.setComment("currency_wallet", """
                钱包袋子机制设定
                Currency Wallet Mechanics Settings""");

        define(old, "currency_wallet.extraBeltSlots", 1, """
                佩戴钱包袋子时，为玩家额外提供的腰带 (Belt) 饰品栏位数量。
                Extra Curios belt slots provided when the wallet is equipped.""");

        define(old, "currency_wallet.maxCurrencyTypes", 10, """
                系统支持的最大货币种类数量。数值越大，钱包界面需要显示的货币项目越多。
                Maximum supported currency types. Higher values allow more currencies to appear in the wallet UI.""");

        define(old, "currency_wallet.magnetRange", 6.0, """
                佩戴钱包时，自动收集周围货币掉落物的半径（单位：格子）。
                Radius (in blocks) to automatically collect currency items while equipped.""");

        define(old, "currency_wallet.magnetIntervalTicks", 10, """
                自动收集的检查间隔（单位：Tick）。数值越大越节省服务器性能，但收集会有延迟。
                Interval (Ticks) for automatic collection checks. Higher = better server performance but slower collection.""");

        define(old, "currency_wallet.hudOffsetX", 0, """
                钱包货币实时显示界面距离屏幕左侧的像素偏移量（负数按 0 处理）。
                Currency HUD pixel offset from the left side of the screen (negative values are treated as 0).""");

        define(old, "currency_wallet.hudOffsetY", 0, """
                钱包货币实时显示界面距离屏幕底部的像素偏移量（负数按 0 处理）。
                Currency HUD pixel offset from the bottom of the screen (negative values are treated as 0).""");

        define(old, "currency_wallet.hudScale", 0.75, """
                屏幕上货币数量显示的 UI 缩放比例（最小有效值 0.1，默认 0.75）。
                UI scale for the currency display overlay (minimum effective value 0.1, default 0.75).""");

        define(old, "currency_wallet.currencyDefinitions", DEFAULT_WALLET_CURRENCY_DEFINITIONS, """
                定义哪些物品是货币及其基础价值。格式：'物品ID|价值'。价值必须大于0。
                Defines valid currencies and their base values. Format: 'item_id|value'. Value must be > 0.
                示例: ["kubejs:gold_coin|1", "kubejs:diamond_coin|100"]
                Example: ["kubejs:gold_coin|1", "kubejs:diamond_coin|100"]""");

        define(old, "currency_wallet.exchangeRules", DEFAULT_WALLET_EXCHANGE_RULES, """
                定义货币之间的单向转换规则，用于找零或自动升级。格式：'来源物品ID->目标物品ID'。
                Defines allowed one-way currency conversions. Format: 'source_id->target_id'.
                示例: ["kubejs:gold_coin->kubejs:diamond_coin"] (允许金币向钻石币兑换)
                Example: ["kubejs:gold_coin->kubejs:diamond_coin"] (Allows converting Gold to Diamond coin)""");

        define(old, "currency_wallet.manualExchangeOnlyCurrencies", DEFAULT_WALLET_MANUAL_EXCHANGE_ONLY_CURRENCIES, """
                受保护货币列表。这些货币只允许玩家在钱包界面手动兑换，不会被村民交易、钱包商店或FTB虚拟扣款自动消耗，也不会被自动拆成低级货币。
                Protected currency list. These currencies can only be converted manually in the wallet screen. Villager trades, wallet shop, and FTB virtual payments will not auto-consume or auto-split them.
                示例: ["kubejs:monster_coin"]
                Example: ["kubejs:monster_coin"]""");
    }

    public static void save() {
        saveServerSettings();
    }

    public static void saveServerSettings() {
        if (configData == null) {
            throw new IllegalStateException("Curios server config is not loaded");
        }
        configData.set("general.enable_heart_of_steel", enableHeartOfSteel);
        configData.set("general.enable_paradise_lost", enableParadiseLost);
        configData.set("general.enable_levitation_backpack", enableLevitationBackpack);
        configData.set("general.enable_currency_wallet", enableCurrencyWallet);
        configData.set("levitation_backpack.knockbackImmunity", levitationKnockbackImmunity);

        configData.set("heart_of_steel.growthInterval", hosGrowthInterval);
        configData.set("heart_of_steel.damageCap", hosDamageCap);
        configData.set("heart_of_steel.maxHealthCap", hosMaxHealthCap);
        configData.set("heart_of_steel.stacksPerHp", hosStacksPerHp);
        configData.set("heart_of_steel.baseHealth", hosBaseHealth);
        configData.set("heart_of_steel.healMultiplier", hosHealMultiplier);
        configData.set("heart_of_steel.damageWindowTicks", hosDamageWindowTicks);
        configData.set("heart_of_steel.minGain", hosMinGain);
        configData.set("heart_of_steel.maxGain", hosMaxGain);
        configData.set("heart_of_steel.damagePerHp", hosDamagePerHp);
        configData.set("heart_of_steel.healActivationThreshold", hosHealActivationThreshold);
        configData.set("heart_of_steel.conflicts", hosConflicts);

        configData.set("paradise_lost.minNutrition", plMinNutrition);
        configData.set("paradise_lost.scoreMultiplier", plScoreMultiplier);
        configData.set("paradise_lost.curvePoints", plCurvePoints);
        configData.set("paradise_lost.curveMode", plCurveMode);
        configData.set("paradise_lost.curvePower", plCurvePower);
        configData.set("paradise_lost.conflicts", plConflicts);

        configData.set("currency_wallet.extraBeltSlots", walletExtraBeltSlots);
        configData.set("currency_wallet.maxCurrencyTypes", walletMaxCurrencyTypes);
        configData.set("currency_wallet.magnetRange", walletMagnetRange);
        configData.set("currency_wallet.magnetIntervalTicks", walletMagnetIntervalTicks);
        configData.set("currency_wallet.currencyDefinitions", walletCurrencyDefinitions);
        configData.set("currency_wallet.exchangeRules", walletExchangeRules);
        configData.set("currency_wallet.manualExchangeOnlyCurrencies", walletManualExchangeOnlyCurrencies);

        configData.save();
        Data.invalidateCurrencyCache();
    }

    public static void saveClientSettings() {
        if (configData == null) return;
        configData.set("currency_wallet.hudOffsetX", walletHudOffsetX);
        configData.set("currency_wallet.hudOffsetY", walletHudOffsetY);
        configData.set("currency_wallet.hudScale", walletHudScale);
        configData.save();
    }

    public static void setWalletMaxCurrencyTypes(int value) {
        walletMaxCurrencyTypes = value;
        Data.invalidateCurrencyCache();
    }

    public static void setWalletCurrencyDefinitions(List<String> value) {
        walletCurrencyDefinitions = value;
        Data.invalidateCurrencyCache();
    }

    public static void setWalletExchangeRules(List<String> value) {
        walletExchangeRules = value;
        Data.invalidateCurrencyCache();
    }

    public static void setWalletManualExchangeOnlyCurrencies(List<String> value) {
        walletManualExchangeOnlyCurrencies = value;
        Data.invalidateCurrencyCache();
    }

    private static void readValues() {
        enableHeartOfSteel = configData.getOrElse("general.enable_heart_of_steel", true);
        enableParadiseLost = configData.getOrElse("general.enable_paradise_lost", true);
        enableLevitationBackpack = configData.getOrElse("general.enable_levitation_backpack", true);
        enableCurrencyWallet = configData.getOrElse("general.enable_currency_wallet", true);
        levitationKnockbackImmunity = configData.getOrElse("levitation_backpack.knockbackImmunity", true);

        hosGrowthInterval = configData.getOrElse("heart_of_steel.growthInterval", 3);
        hosDamageCap = configData.getOrElse("heart_of_steel.damageCap", 3.0);
        hosMaxHealthCap = configData.getOrElse("heart_of_steel.maxHealthCap", 200);
        hosStacksPerHp = configData.getOrElse("heart_of_steel.stacksPerHp", 100);
        hosBaseHealth = configData.getOrElse("heart_of_steel.baseHealth", 10.0);
        hosHealMultiplier = configData.getOrElse("heart_of_steel.healMultiplier", 2.0);
        hosDamageWindowTicks = configData.getOrElse("heart_of_steel.damageWindowTicks", 5);
        hosMinGain = configData.getOrElse("heart_of_steel.minGain", 2);
        hosMaxGain = configData.getOrElse("heart_of_steel.maxGain", 5);
        hosDamagePerHp = configData.getOrElse("heart_of_steel.damagePerHp", 0.005);
        hosHealActivationThreshold = configData.getOrElse("heart_of_steel.healActivationThreshold", 0.01);
        hosConflicts = configData.getOrElse("heart_of_steel.conflicts", Arrays.asList("enigmaticlegacy:cursed_ring"));

        plMinNutrition = configData.getOrElse("paradise_lost.minNutrition", 0);
        plScoreMultiplier = configData.getOrElse("paradise_lost.scoreMultiplier", 1.0);
        plCurvePoints = configData.getOrElse("paradise_lost.curvePoints", ParadiseLostCurve.DEFAULT_DEFINITIONS);
        plCurveMode = configData.getOrElse("paradise_lost.curveMode", "sine_ease_out");
        plCurvePower = configData.getOrElse("paradise_lost.curvePower", 2.0);
        plConflicts = configData.getOrElse("paradise_lost.conflicts", Arrays.asList("enigmaticlegacy:cursed_ring"));

        walletExtraBeltSlots = configData.getOrElse("currency_wallet.extraBeltSlots", 1);
        walletMaxCurrencyTypes = configData.getOrElse("currency_wallet.maxCurrencyTypes", 10);
        walletMagnetRange = configData.getOrElse("currency_wallet.magnetRange", 6.0);
        walletMagnetIntervalTicks = configData.getOrElse("currency_wallet.magnetIntervalTicks", 10);
        walletHudOffsetX = configData.getOrElse("currency_wallet.hudOffsetX", 0);
        walletHudOffsetY = configData.getOrElse("currency_wallet.hudOffsetY", 0);
        walletHudScale = configData.getOrElse("currency_wallet.hudScale", 0.75);
        walletCurrencyDefinitions = configData.getOrElse("currency_wallet.currencyDefinitions", DEFAULT_WALLET_CURRENCY_DEFINITIONS);
        walletExchangeRules = configData.getOrElse("currency_wallet.exchangeRules", DEFAULT_WALLET_EXCHANGE_RULES);
        walletManualExchangeOnlyCurrencies = configData.getOrElse("currency_wallet.manualExchangeOnlyCurrencies", DEFAULT_WALLET_MANUAL_EXCHANGE_ONLY_CURRENCIES);
        Data.invalidateCurrencyCache();
    }

    private static void define(Config old, String path, Object def, String comment) {
        configData.set(path, old.getOrElse(path, def));
        configData.setComment(path, comment);
    }
}
