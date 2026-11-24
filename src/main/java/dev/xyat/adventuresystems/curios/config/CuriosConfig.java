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
    private static final String CONFIG_FILE = "kineticcore/adventuresystems/curios.toml";
    private static final Path CONFIG_PATH = KineticPaths.configFile(CONFIG_FILE);
    private static CommentedFileConfig configData;

    // Coin items the wallet takes in, with how many wallet coins each is worth. The wallet itself holds one virtual
    // balance (adventuresystems:adventure_coin); coins are turned into it by the wallet's quick deposit.
    public static final List<String> DEFAULT_WALLET_CURRENCY_DEFINITIONS = Arrays.asList(
            "kubejs:gold_coin|1",
            "kubejs:diamond_coin|100",
            "kubejs:netherite_coin|1000"
    );

    public static boolean enableHeartOfSteel = true;
    public static boolean enableParadiseLost = true;
    public static boolean enableLevitationBackpack = true;
    public static boolean enableCurrencyWallet = true;
    public static boolean enableWindThunderWings = true;
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
    public static int walletHudOffsetX = 0;
    public static int walletHudOffsetY = 0;
    public static double walletHudScale = 0.75;
    public static List<String> walletCurrencyDefinitions = DEFAULT_WALLET_CURRENCY_DEFINITIONS;

    // 风雷翅：飞行倍率相对创造模式飞行速度；燃血按最大生命值的比例。
    public static double wtwNormalSpeed = 2.0;
    public static double wtwBoostSpeed = 6.0;
    public static double wtwBurnSpeed = 20.0;
    public static int wtwBurnSeconds = 10;
    public static double wtwBurnHealthCost = 0.30;
    public static double wtwBurnMinHealth = 0.30;
    public static int wtwBurnCooldown = 15;
    // 风雷翅外观（只影响本客户端）。
    public static int wtwWingColor = 0x2FA3AE;
    public static int wtwThunderColor = 0xD6ECFF;
    public static int wtwBoostColor = 0x3CFF8A;
    public static int wtwBurnColor = 0xD8101C;
    public static double wtwTrailSeconds = 0.8;
    public static double wtwTrailWidth = 1.0;
    public static boolean wtwShowWings = true;
    public static boolean wtwSparks = true;
    public static boolean wtwSounds = true;

    static {
        load();
    }

    public static void load() {
        if (configData != null) return;
        try {
            KineticPaths.migrateConfigFile("kineticcore/curios.toml", CONFIG_FILE);
            KineticPaths.ensureConfigDirectory("kineticcore/adventuresystems");
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

        define(old, "general.enable_wind_thunder_wings", true, """
                是否注册风雷翅饰品 (修改后需重启)
                Whether the Wind-Thunder Wings accessory is registered (restart required)""");

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
                可存入钱包的货币物品，以及每个值多少冒险币。格式：'物品ID|冒险币'，数值必须大于0。
                钱包里只有一种冒险币；钱包界面的“快速存入”把背包里的这些物品按面值换成冒险币，村民交易要这些物品时从钱包扣对应的冒险币。
                Coin items the wallet takes in and how many Adventure Coins each is worth. Format: 'item_id|coins', coins > 0.
                The wallet holds one balance of Adventure Coins; its Quick deposit turns these items into Adventure Coins at their value,
                and villager trades that cost these items pay their value from the wallet.
                示例 / Example: ["kubejs:gold_coin|1", "kubejs:diamond_coin|100"]""");

        setupWings(old);
    }

    private static void setupWings(Config old) {
        configData.setComment("wind_thunder_wings", """
                风雷翅：佩戴后可以飞行；遁光加速时化作遁光、人物不可见；燃血消耗生命换取短时间的极速。
                Wind-Thunder Wings: flight while worn; boost turns the wearer into a streak of light; blood burn trades health for a short burst of top speed.""");
        define(old, "wind_thunder_wings.normalSpeed", 2.0, "普通飞行速度倍率（相对创造模式飞行）\nNormal flight speed, times creative flight speed");
        define(old, "wind_thunder_wings.boostSpeed", 6.0, "遁光加速的速度倍率\nBoost speed, times creative flight speed");
        define(old, "wind_thunder_wings.burnSpeed", 20.0, "燃血期间的速度倍率\nBlood burn speed, times creative flight speed");
        define(old, "wind_thunder_wings.burnSeconds", 10, "燃血持续秒数\nBlood burn duration in seconds");
        define(old, "wind_thunder_wings.burnHealthCost", 0.30, "燃血扣除的生命，占最大生命值的比例\nHealth taken by blood burn, as a share of max health");
        define(old, "wind_thunder_wings.burnMinHealth", 0.30, "生命低于最大生命值的这个比例时无法燃血\nBlood burn is refused below this share of max health");
        define(old, "wind_thunder_wings.burnCooldown", 15, "燃血冷却秒数\nBlood burn cooldown in seconds");
        define(old, "wind_thunder_wings.wingColor", 0x2FA3AE, "翅膀颜色（客户端）\nWing colour (client)");
        define(old, "wind_thunder_wings.thunderColor", 0xD6ECFF, "雷光颜色（客户端）\nThunder colour (client)");
        define(old, "wind_thunder_wings.boostColor", 0x3CFF8A, "遁光颜色（客户端）\nBoost light colour (client)");
        define(old, "wind_thunder_wings.burnColor", 0xD8101C, "燃血遁光颜色（客户端）\nBlood burn light colour (client)");
        define(old, "wind_thunder_wings.trailSeconds", 0.8, "尾迹停留秒数（客户端）\nHow long the trail lingers in seconds (client)");
        define(old, "wind_thunder_wings.trailWidth", 1.0, "尾迹宽度倍率（客户端）\nTrail width multiplier (client)");
        define(old, "wind_thunder_wings.showWings", true, "普通飞行时显示翅膀（客户端）\nShow the wings in normal flight (client)");
        define(old, "wind_thunder_wings.sparks", true, "显示雷光电弧（客户端）\nShow thunder sparks (client)");
        define(old, "wind_thunder_wings.sounds", true, "播放风雷翅音效（客户端）\nPlay the wing sounds (client)");
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
        configData.set("currency_wallet.currencyDefinitions", walletCurrencyDefinitions);

        configData.set("general.enable_wind_thunder_wings", enableWindThunderWings);
        configData.set("wind_thunder_wings.normalSpeed", wtwNormalSpeed);
        configData.set("wind_thunder_wings.boostSpeed", wtwBoostSpeed);
        configData.set("wind_thunder_wings.burnSpeed", wtwBurnSpeed);
        configData.set("wind_thunder_wings.burnSeconds", wtwBurnSeconds);
        configData.set("wind_thunder_wings.burnHealthCost", wtwBurnHealthCost);
        configData.set("wind_thunder_wings.burnMinHealth", wtwBurnMinHealth);
        configData.set("wind_thunder_wings.burnCooldown", wtwBurnCooldown);

        configData.save();
        Data.invalidateCurrencyCache();
    }

    public static void saveClientSettings() {
        if (configData == null) return;
        configData.set("currency_wallet.hudOffsetX", walletHudOffsetX);
        configData.set("currency_wallet.hudOffsetY", walletHudOffsetY);
        configData.set("currency_wallet.hudScale", walletHudScale);
        configData.set("wind_thunder_wings.wingColor", wtwWingColor);
        configData.set("wind_thunder_wings.thunderColor", wtwThunderColor);
        configData.set("wind_thunder_wings.boostColor", wtwBoostColor);
        configData.set("wind_thunder_wings.burnColor", wtwBurnColor);
        configData.set("wind_thunder_wings.trailSeconds", wtwTrailSeconds);
        configData.set("wind_thunder_wings.trailWidth", wtwTrailWidth);
        configData.set("wind_thunder_wings.showWings", wtwShowWings);
        configData.set("wind_thunder_wings.sparks", wtwSparks);
        configData.set("wind_thunder_wings.sounds", wtwSounds);
        configData.save();
    }

    public static void setWalletCurrencyDefinitions(List<String> value) {
        walletCurrencyDefinitions = value;
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
        walletHudOffsetX = configData.getOrElse("currency_wallet.hudOffsetX", 0);
        walletHudOffsetY = configData.getOrElse("currency_wallet.hudOffsetY", 0);
        walletHudScale = configData.getOrElse("currency_wallet.hudScale", 0.75);
        walletCurrencyDefinitions = configData.getOrElse("currency_wallet.currencyDefinitions", DEFAULT_WALLET_CURRENCY_DEFINITIONS);
        Data.invalidateCurrencyCache();

        enableWindThunderWings = configData.getOrElse("general.enable_wind_thunder_wings", true);
        wtwNormalSpeed = number("wind_thunder_wings.normalSpeed", 2.0).doubleValue();
        wtwBoostSpeed = number("wind_thunder_wings.boostSpeed", 6.0).doubleValue();
        wtwBurnSpeed = number("wind_thunder_wings.burnSpeed", 20.0).doubleValue();
        // The first release's defaults (5 and 10) move to 3 and 10 times normal flight; speeds someone set stay.
        if (wtwBoostSpeed == 5.0) wtwBoostSpeed = 6.0;
        if (wtwBurnSpeed == 10.0) wtwBurnSpeed = 20.0;
        wtwBurnSeconds = number("wind_thunder_wings.burnSeconds", 10).intValue();
        wtwBurnHealthCost = number("wind_thunder_wings.burnHealthCost", 0.30).doubleValue();
        wtwBurnMinHealth = number("wind_thunder_wings.burnMinHealth", 0.30).doubleValue();
        wtwBurnCooldown = number("wind_thunder_wings.burnCooldown", 15).intValue();
        wtwWingColor = number("wind_thunder_wings.wingColor", 0x2FA3AE).intValue();
        wtwThunderColor = number("wind_thunder_wings.thunderColor", 0xD6ECFF).intValue();
        // The first release's defaults (light cyan wings, violet thunder) move to the roc teal and blue-white lightning;
        // colours someone picked themselves stay.
        if (wtwWingColor == 0x5FD8FF) wtwWingColor = 0x2FA3AE;
        if (wtwThunderColor == 0xB98CFF) wtwThunderColor = 0xD6ECFF;
        wtwBoostColor = number("wind_thunder_wings.boostColor", 0x3CFF8A).intValue();
        wtwBurnColor = number("wind_thunder_wings.burnColor", 0xD8101C).intValue();
        wtwTrailSeconds = number("wind_thunder_wings.trailSeconds", 0.8).doubleValue();
        wtwTrailWidth = number("wind_thunder_wings.trailWidth", 1.0).doubleValue();
        wtwShowWings = configData.getOrElse("wind_thunder_wings.showWings", true);
        wtwSparks = configData.getOrElse("wind_thunder_wings.sparks", true);
        wtwSounds = configData.getOrElse("wind_thunder_wings.sounds", true);
    }

    /** Numbers may come back as Integer or Double whatever the default's type. */
    private static Number number(String path, Number def) {
        Object value = configData.get(path);
        return value instanceof Number number ? number : def;
    }

    private static void define(Config old, String path, Object def, String comment) {
        configData.set(path, old.getOrElse(path, def));
        configData.setComment(path, comment);
    }
}
