package dev.xyat.adventuresystems.tips.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.xyat.adventuresystems.tips.TipsModule;
import dev.xyat.adventuresystems.tips.api.HelpTip;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import dev.xyat.kineticcore.api.runtime.KineticPaths;
//? if >=1.21 {
/*import dev.xyat.adventuresystems.data.AdventureItemData;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.component.DataComponentPatch;
*///?} else {
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
//?}
import dev.xyat.adventuresystems.text.AdventureText;
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
            if (model != null && areValidEntries(model.tips, false)) {
                return new ArrayList<>(model.tips);
            }
        } catch (Exception exception) {
            TipsModule.LOGGER.error("TipsConfig: Failed to read raw entries", exception);
        }
        return new ArrayList<>();
    }

    public static List<HelpTip.JsonModel.Entry> fromJson(String json) {
        return parseJson(json, true);
    }

    /** Read-only editor snapshots also carry older long entries so players can repair them. */
    public static List<HelpTip.JsonModel.Entry> fromEditorJson(String json) {
        return parseJson(json, false);
    }

    private static List<HelpTip.JsonModel.Entry> parseJson(String json, boolean compactText) {
        try {
            HelpTip.JsonModel model = GSON.fromJson(json == null ? "" : json, HelpTip.JsonModel.class);
            if (model == null || !areValidEntries(model.tips, compactText)) return null;
            return new ArrayList<>(model.tips);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    public static String toJson(List<HelpTip.JsonModel.Entry> entries) {
        return serializeJson(entries, true);
    }

    public static String toEditorJson(List<HelpTip.JsonModel.Entry> entries) {
        return serializeJson(entries, false);
    }

    private static String serializeJson(List<HelpTip.JsonModel.Entry> entries, boolean compactText) {
        if (!areValidEntries(entries, compactText)) throw new IllegalArgumentException("Invalid tip entries");
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
        return areValidEntries(entries, true);
    }

    private static boolean areValidEntries(List<HelpTip.JsonModel.Entry> entries, boolean compactText) {
        if (entries == null || entries.size() > 4096) return false;
        for (HelpTip.JsonModel.Entry entry : entries) {
            if (entry == null || entry.text == null || entry.text.isBlank() || entry.text.length() > 32767
                    || compactText && !TipTextLimits.isValid(entry.text)) return false;
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
        return hasInvalidItemChecks(conditions.items) || hasInvalidItemChecks(conditions.curios);
    }

    private static boolean isInvalidOptionalResourceLocation(String value) {
        return value != null && !value.isBlank() && KineticResourceIds.tryParse(value.trim()) == null;
    }

    private static boolean hasInvalidItemChecks(List<HelpTip.JsonModel.ItemCheck> checks) {
        if (checks == null) return false;
        if (checks.size() > 256) return true;
        for (HelpTip.JsonModel.ItemCheck check : checks) {
            if (check == null || check.id == null) return true;
            ResourceLocation id = KineticResourceIds.tryParse(check.id.trim());
            if (id == null) return true;
            //? if >=1.21 {
            /*if (check.hasLegacyItemData()) return true;
            String mode = check.componentMode == null ? "NONE" : check.componentMode.toUpperCase(Locale.ROOT);
            if (!("NONE".equals(mode) || "WEAK".equals(mode) || "STRONG".equals(mode))) return true;
            if (check.components != null && check.components.length() > 32767) return true;
            if (check.components != null && !check.components.isBlank()) {
                try {
                    String text=check.components.trim();
                    if (!text.startsWith("[") || !text.endsWith("]")) return true;
                    if (AdventureItemData.hasWorldRegistries()) parseComponents(check);
                } catch (RuntimeException exception) {
                    return true;
                }
            }
            *///?} else {
            String mode = check.nbtMode == null ? "NONE" : check.nbtMode.toUpperCase(Locale.ROOT);
            if (!("NONE".equals(mode) || "WEAK".equals(mode) || "STRONG".equals(mode))) return true;
            if (check.nbt != null && check.nbt.length() > 32767) return true;
            if (check.nbt != null && !check.nbt.isBlank()) {
                try {
                    TagParser.parseTag(check.nbt);
                } catch (Exception exception) {
                    return true;
                }
            }
            //?}
        }
        return false;
    }

    public static List<HelpTip> loadTipsForLanguage(String languageCode) {
        return toRuntimeTips(getRawEntriesForLanguage(languageCode));
    }

    public static List<HelpTip> toRuntimeTips(List<HelpTip.JsonModel.Entry> entries) {
        List<HelpTip> result = new ArrayList<>();
        if (entries == null) return result;
        for (HelpTip.JsonModel.Entry entry : entries) {
            if (entry == null || !TipTextLimits.isValid(entry.text)) continue;

            HelpTip tip = new HelpTip(AdventureText.literal(TipTextLimits.normalizeLines(entry.text)), entry.time);
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

    //? if >=1.21 {
    /*private static DataComponentPatch parseComponents(HelpTip.JsonModel.ItemCheck check) {
        if (check.components == null || check.components.isBlank()) return DataComponentPatch.EMPTY;
        String text = check.components.trim();
        if (!text.startsWith("[") || !text.endsWith("]")) {
            throw new IllegalArgumentException("Expected native item component syntax [component=value]");
        }
        try {
            StringReader reader = new StringReader(check.id + text);
            DataComponentPatch patch = new ItemParser(AdventureItemData.registryAccess()).parse(reader).components();
            if (reader.canRead()) throw new IllegalArgumentException("Unexpected text after item components");
            // Keep explicit default-valued predicates (for example damage=0) instead of normalizing through a stack.
            return patch;
        } catch (CommandSyntaxException exception) {
            throw new IllegalArgumentException(exception.getMessage(), exception);
        }
    }

    private static List<HelpTip.ItemMatcher> parseItems(List<HelpTip.JsonModel.ItemCheck> checks) {
        List<HelpTip.ItemMatcher> list = new ArrayList<>();
        if (checks == null) return list;
        for (HelpTip.JsonModel.ItemCheck check : checks) {
            if (check == null || check.id == null || check.id.isEmpty()) continue;
            try {
                HelpTip.ComponentMode mode = check.componentMode == null ? HelpTip.ComponentMode.NONE
                        : HelpTip.ComponentMode.valueOf(check.componentMode.toUpperCase(Locale.ROOT));
                if (!AdventureItemData.hasWorldRegistries() && check.components != null && !check.components.isBlank()) {
                    list.add(new HelpTip.ItemMatcher(check.id, null, mode, check.components.trim()));
                } else list.add(new HelpTip.ItemMatcher(check.id, parseComponents(check), mode));
            } catch (RuntimeException exception) {
                TipsModule.LOGGER.warn("TipsConfig: Invalid native item components for {}", check.id, exception);
            }
        }
        return list;
    }
    *///?} else {
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
    //?}

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
            for(String language:List.of("zh_cn","en_us")){
                String path=CONFIG_DIR+"tips_"+language+".json";
                if(!KineticPaths.configFileExists(path)||TipDefaults.isLegacy(KineticPaths.readConfigText(path),language)){
                    String defaults=TipDefaults.read(language);
                    if(fromJson(defaults)==null)throw new IOException("Invalid bundled tips: "+language);
                    KineticPaths.writeConfigTextsAtomic(Map.of(path,defaults));
                }
            }
        } catch (IOException exception) {
            TipsModule.LOGGER.error("TipsConfig: Failed to init default files", exception);
        }
    }

}
