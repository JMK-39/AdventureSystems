package dev.xyat.adventuresystems.curios.wallet.client;

import dev.xyat.kineticcore.api.runtime.KineticPaths;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.adventuresystems.curios.wallet.shop.Shop;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ShopClientPreferences {
    private static final String CONFIG_FILE = "kineticcore/currency_wallet_shop_client.toml";
    private static final Path CONFIG_PATH = KineticPaths.configFile(CONFIG_FILE);
    private static final Map<String, Set<String>> FAVORITES = new HashMap<>();
    private static CommentedFileConfig configData;
    private static boolean loaded;

    private ShopClientPreferences() {
    }

    public static boolean isFavorite(Shop.Mode mode, String entryKey) {
        if (mode == null || entryKey == null || entryKey.isBlank()) return false;
        return favoriteKeys(mode).contains(entryKey);
    }

    public static boolean toggleFavorite(Shop.Mode mode, String entryKey) {
        if (mode == null || entryKey == null || entryKey.isBlank()) return false;
        Set<String> favorites = favoriteKeys(mode);
        boolean favorite;
        if (favorites.remove(entryKey)) {
            favorite = false;
        } else {
            favorites.add(entryKey);
            favorite = true;
        }
        saveFavorites(mode, favorites);
        return favorite;
    }

    private static Set<String> favoriteKeys(Shop.Mode mode) {
        ensureLoaded();
        String path = favoritesPath(mode);
        return FAVORITES.computeIfAbsent(path, ShopClientPreferences::readFavorites);
    }

    private static Set<String> readFavorites(String path) {
        Set<String> result = new LinkedHashSet<>();
        if (configData == null) return result;
        Object raw = configData.get(path);
        if (!(raw instanceof List<?> list)) return result;
        for (Object value : list) {
            if (value == null) continue;
            String key = value.toString().trim();
            if (!key.isEmpty()) result.add(key);
        }
        return result;
    }

    private static void saveFavorites(Shop.Mode mode, Set<String> favorites) {
        ensureLoaded();
        if (configData == null) return;
        try {
            configData.set(favoritesPath(mode), new ArrayList<>(favorites));
            configData.save();
        } catch (Exception exception) {
            CuriosModule.LOGGER.error("Failed to save currency wallet shop client preferences", exception);
        }
    }

    private static String favoritesPath(Shop.Mode mode) {
        String modeName = mode.name().toLowerCase(Locale.ROOT);
        return "players." + playerKey() + "." + modeName + "Favorites";
    }

    private static String playerKey() {
        var player = KineticClientRuntime.localPlayer();
        if (player == null) return "local";
        return player.getUUID().toString().replace("-", "");
    }

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        try {
            KineticPaths.ensureConfigDirectory("kineticcore");
            configData = CommentedFileConfig.builder(CONFIG_PATH)
                    .sync()
                    .preserveInsertionOrder()
                    .writingMode(WritingMode.REPLACE)
                    .build();
            if (KineticPaths.configFileExists(CONFIG_FILE)) configData.load();
        } catch (Exception exception) {
            configData = null;
            CuriosModule.LOGGER.error("Failed to load currency wallet shop client preferences", exception);
        }
    }
}
