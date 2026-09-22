package dev.xyat.adventuresystems.ftb.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.xyat.adventuresystems.ftb.FtbModule;
import dev.xyat.kineticcore.api.runtime.KineticPaths;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class FavoritesStoreFTB {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, Long> FAVORITES = new HashMap<>();
    private static final String CONFIG_FILE = "kineticcore/ftb_quest_favorites.json";

    private FavoritesStoreFTB() {
    }

    public static void load() {
        FAVORITES.clear();
        try {
            String content = String.join("\n", KineticPaths.readConfigLines(CONFIG_FILE));
            if (content.isBlank()) return;
            JsonObject root = GSON.fromJson(content, JsonObject.class);
            if (root != null && root.has("favorites")) {
                Map<String, Long> loaded = GSON.fromJson(root.get("favorites"), new TypeToken<Map<String, Long>>() {}.getType());
                if (loaded != null) {
                    FAVORITES.putAll(loaded);
                }
            }
        } catch (Exception e) {
            FtbModule.LOGGER.error("[KT-FTB任务物品] 读取收藏文件失败", e);
        }
    }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            root.add("favorites", GSON.toJsonTree(FAVORITES));
            KineticPaths.writeConfigText(CONFIG_FILE, GSON.toJson(root));
        } catch (Exception e) {
            FtbModule.LOGGER.error("[KT-FTB任务物品] 保存收藏文件失败", e);
        }
    }

    public static long getFavorite(ItemStack stack) {
        return FAVORITES.getOrDefault(BindingStoreFTB.itemKey(stack), 0L);
    }

    public static void setFavorite(ItemStack stack, long questId) {
        if (questId == 0L) {
            FAVORITES.remove(BindingStoreFTB.itemKey(stack));
        } else {
            FAVORITES.put(BindingStoreFTB.itemKey(stack), questId);
        }
        save();
    }

}
