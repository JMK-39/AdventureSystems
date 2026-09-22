package dev.xyat.adventuresystems.tips.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.xyat.adventuresystems.tips.TipsModule;
import dev.xyat.adventuresystems.tips.api.HelpTip;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import dev.xyat.kineticcore.api.runtime.KineticPaths;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConfigLoader {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String CONFIG_DIR = "kineticcore/tips/";

    public static List<HelpTip.JsonModel.Entry> getRawEntriesForLanguage(String languageCode) {
        ensureDefaultFiles();
        String targetFile = resolveReadFile(languageCode);
        try {
            if (!KineticPaths.configFileExists(targetFile)) return new ArrayList<>();
            HelpTip.JsonModel model = GSON.fromJson(KineticPaths.readConfigText(targetFile), HelpTip.JsonModel.class);
            if (model != null && model.tips != null && areValidEntries(model.tips)) {
                return new ArrayList<>(model.tips);
            }
        } catch (Exception exception) {
            TipsModule.LOGGER.error("TipsConfig: Failed to read raw entries", exception);
        }
        return new ArrayList<>();
    }

    public static List<HelpTip.JsonModel.Entry> fromJson(String json) {
        try {
            HelpTip.JsonModel model = GSON.fromJson(json == null ? "" : json, HelpTip.JsonModel.class);
            if (model == null || model.tips == null || !areValidEntries(model.tips)) return null;
            return new ArrayList<>(model.tips);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    public static String toJson(List<HelpTip.JsonModel.Entry> entries) {
        if (!areValidEntries(entries)) throw new IllegalArgumentException("Invalid tip entries");
        HelpTip.JsonModel model = new HelpTip.JsonModel();
        model.tips = new ArrayList<>(entries);
        return GSON.toJson(model);
    }

    public static boolean saveRawEntriesForLanguage(String languageCode, List<HelpTip.JsonModel.Entry> entries) {
        if (!areValidEntries(entries)) {
            TipsModule.LOGGER.error("TipsConfig: Refusing to save invalid tip entries");
            return false;
        }
        ensureDefaultFiles();
        String targetFile = fileForLanguage(languageCode);
        try {
            KineticPaths.writeConfigTextsAtomic(Map.of(targetFile, toJson(entries)));
            return true;
        } catch (IOException exception) {
            TipsModule.LOGGER.error("TipsConfig: Failed to save config", exception);
            return false;
        }
    }

    public static boolean areValidEntries(List<HelpTip.JsonModel.Entry> entries) {
        if (entries == null || entries.size() > 4096) return false;
        for (HelpTip.JsonModel.Entry entry : entries) {
            if (entry == null || entry.text == null || entry.text.isBlank() || entry.text.length() > 32767) return false;
            if (!("any".equals(entry.stage) || "loading".equals(entry.stage) || "game".equals(entry.stage))) return false;
            if (entry.time < 250 || entry.time > 3_600_000) return false;
            if (hasInvalidConditions(entry.conditions)) return false;
        }
        return true;
    }

    private static boolean hasInvalidConditions(HelpTip.JsonModel.Conditions conditions) {
        if (conditions == null) return false;
        if (isInvalidOptionalResourceLocation(conditions.biome)
                || isInvalidOptionalResourceLocation(conditions.structure)
                || isInvalidOptionalResourceLocation(conditions.advancement)
                || isInvalidOptionalResourceLocation(conditions.dimension)) {
            return true;
        }
        return !isValidItemChecks(conditions.items) || !isValidItemChecks(conditions.curios);
    }

    private static boolean isInvalidOptionalResourceLocation(String value) {
        return value != null && !value.isBlank() && KineticResourceIds.tryParse(value.trim()) == null;
    }

    private static boolean isValidItemChecks(List<HelpTip.JsonModel.ItemCheck> checks) {
        if (checks == null) return true;
        if (checks.size() > 256) return false;
        for (HelpTip.JsonModel.ItemCheck check : checks) {
            if (check == null || check.id == null) return false;
            ResourceLocation id = KineticResourceIds.tryParse(check.id.trim());
            if (id == null) return false;
            String mode = check.nbtMode == null ? "NONE" : check.nbtMode.toUpperCase(Locale.ROOT);
            if (!("NONE".equals(mode) || "WEAK".equals(mode) || "STRONG".equals(mode))) return false;
            if (check.nbt != null && check.nbt.length() > 32767) return false;
            if (check.nbt != null && !check.nbt.isBlank()) {
                try {
                    TagParser.parseTag(check.nbt);
                } catch (Exception exception) {
                    return false;
                }
            }
        }
        return true;
    }

    public static List<HelpTip> loadTipsForLanguage(String languageCode) {
        return toRuntimeTips(getRawEntriesForLanguage(languageCode));
    }

    public static List<HelpTip> toRuntimeTips(List<HelpTip.JsonModel.Entry> entries) {
        List<HelpTip> result = new ArrayList<>();
        if (entries == null) return result;
        for (HelpTip.JsonModel.Entry entry : entries) {
            if (entry == null || entry.text == null || entry.text.isEmpty()) continue;

            HelpTip tip = new HelpTip(Component.literal(entry.text), entry.time);
            if ("loading".equalsIgnoreCase(entry.stage)) tip.stage = 1;
            else if ("game".equalsIgnoreCase(entry.stage)) tip.stage = 2;
            else tip.stage = 0;

            if (entry.conditions != null) {
                tip.requiredBiome = entry.conditions.biome != null ? entry.conditions.biome : "";
                tip.requiredStructure = entry.conditions.structure != null ? entry.conditions.structure : "";
                tip.requiredAdvancement = entry.conditions.advancement != null ? entry.conditions.advancement : "";
                tip.requiredDimension = entry.conditions.dimension != null ? entry.conditions.dimension : "";
                tip.requiredItems = parseItems(entry.conditions.items);
                tip.requiredCurios = parseItems(entry.conditions.curios);
            }
            result.add(tip);
        }
        return result;
    }

    private static List<HelpTip.ItemMatcher> parseItems(List<HelpTip.JsonModel.ItemCheck> checks) {
        List<HelpTip.ItemMatcher> list = new ArrayList<>();
        if (checks == null) return list;
        for (HelpTip.JsonModel.ItemCheck check : checks) {
            if (check.id == null || check.id.isEmpty()) continue;
            CompoundTag tag = null;
            try {
                if (check.nbt != null && !check.nbt.isEmpty()) tag = TagParser.parseTag(check.nbt);
            } catch (Exception ignored) {
            }

            HelpTip.NbtMode mode = HelpTip.NbtMode.NONE;
            if ("WEAK".equalsIgnoreCase(check.nbtMode)) mode = HelpTip.NbtMode.WEAK;
            else if ("STRONG".equalsIgnoreCase(check.nbtMode)) mode = HelpTip.NbtMode.STRONG;
            list.add(new HelpTip.ItemMatcher(check.id, tag, mode));
        }
        return list;
    }

    public static String normalizeLanguageCode(String languageCode) {
        String value = languageCode == null ? "" : languageCode.trim().toLowerCase(Locale.ROOT);
        if (value.length() > 32 || !value.matches("[a-z0-9_-]+")) return "en_us";
        return value.isEmpty() ? "en_us" : value;
    }

    private static String fileForLanguage(String languageCode) {
        return CONFIG_DIR + "tips_" + normalizeLanguageCode(languageCode) + ".json";
    }

    private static String resolveReadFile(String languageCode) {
        String requested = fileForLanguage(languageCode);
        if (KineticPaths.configFileExists(requested)) return requested;
        return CONFIG_DIR + "tips_en_us.json";
    }

    private static void ensureDefaultFiles() {
        try {
            String chinese = CONFIG_DIR + "tips_zh_cn.json";
            String english = CONFIG_DIR + "tips_en_us.json";
            if (!KineticPaths.configFileExists(chinese)) {
                KineticPaths.writeConfigText(chinese, getDefaultJsonCN());
            }
            if (!KineticPaths.configFileExists(english)) {
                KineticPaths.writeConfigText(english, getDefaultJsonEN());
            }
        } catch (IOException exception) {
            TipsModule.LOGGER.error("TipsConfig: Failed to init default files", exception);
        }
    }

    private static String getDefaultJsonCN() {
        return """
        {
          "tips":[
            {
              "stage": "loading",
              "text": "§e[阶段演示] §f这是一条仅在 §b游戏加载阶段 §f显示的提示。适合放背景故事或性能说明。",
              "time": 4000
            },
            {
              "stage": "game",
              "text": "§a§l[群系演示] §f检测到你位于下界荒地！猪灵通常在附近出没，建议穿一件金装。",
              "conditions": {
                "biome": "minecraft:nether_wastes"
              }
            },
            {
              "stage": "game",
              "text": "§b§l[结构演示] §f你发现了一座村庄。记得寻找铁匠铺，那里通常有不错的补给。",
              "conditions": {
                "structure": "minecraft:village_plains"
              }
            },
            {
              "stage": "game",
              "text": "§d§l[物品演示] §f你拿到了鞘翅！配合烟花火箭可以实现跨维度长距离飞行。",
              "conditions": {
                "items":[ { "id": "minecraft:elytra" } ]
              }
            },
            {
              "stage": "game",
              "text": "§c§l[NBT演示] §f你正拿着一把 §6强力武器§f。小心操作，别掉进岩浆了！",
              "conditions": {
                "items":[ { "id": "minecraft:netherite_sword", "nbt": "{Damage:0}" } ]
              }
            },
            {
              "stage": "game",
              "text": "§e§l[饰品演示] §f检测到你装备了精美戒指,至少能加幸运值~",
              "conditions": {
                "curios":[ { "id": "enigmaticlegacy:golden_ring" } ]
              }
            },
            {
              "stage": "game",
              "text": "§6§l[成就演示] §f恭喜完成工业时代！准备好迈向自动化生产了吗？",
              "conditions": {
                "advancement": "minecraft:story/enter_the_end"
              }
            }
          ]
        }""";
    }

    private static String getDefaultJsonEN() {
        return """
        {
          "tips":[
            {
              "stage": "loading",
              "text": "§e[Loading] §fThis tip is only visible while §bLoading the world§f. Useful for performance tips.",
              "time": 4000
            },
            {
              "stage": "game",
              "text": "§a§l[Biome] §fYou are in Nether Wastes! Wear gold armor to stop Piglins from attacking.",
              "conditions": {
                "biome": "minecraft:nether_wastes"
              }
            },
            {
              "stage": "game",
              "text": "§d§l[Item] §fYou found an Elytra! Use fireworks for infinite flight.",
              "conditions": {
                "items":[ { "id": "minecraft:elytra" } ]
              }
            }
          ]
        }""";
    }
}
