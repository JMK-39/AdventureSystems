package dev.xyat.adventuresystems.tips.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import dev.xyat.adventuresystems.tips.TipsModule;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class GeneralConfig {
    public static boolean enableTips = true;

    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("kineticcore");
    private static final Path CONFIG_PATH = CONFIG_DIR.resolve("tips_settings.toml");
    private static CommentedFileConfig configData;

    public static void load() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
            configData = CommentedFileConfig.builder(CONFIG_PATH)
                    .sync()
                    .preserveInsertionOrder()
                    .writingMode(WritingMode.REPLACE)
                    .build();
            configData.load();
            setupDefaults();
            configData.save();
            readValues();
        } catch (Exception e) {
            TipsModule.LOGGER.error("TipsConfig: Failed to load tips_settings.toml", e);
            if (configData != null) {
                try {
                    configData.close();
                } catch (Exception closeException) {
                    TipsModule.LOGGER.debug("TipsConfig: Failed to close broken config", closeException);
                }
                configData = null;
            }
        }
    }

    private static void setupDefaults() {
        define();
    }

    private static void readValues() {
        enableTips = configData.getOrElse("enableTips", true);
    }

    private static void define() {
        if (!configData.contains("enableTips")) {
            configData.set("enableTips", true);
        }
        configData.setComment("enableTips", " " + "是否启用屏幕提示。开启后将在加载界面、世界转换界面和游戏暂停界面显示自定义的小贴士。\nWhether to enable screen tips. Custom tips will be shown on loading, world transition, and pause screens.");
    }

    public static void save() {
        if (configData == null) {
            throw new IllegalStateException("Tips config is not loaded");
        }

        Path backupPath = CONFIG_PATH.resolveSibling(CONFIG_PATH.getFileName() + ".save-backup");
        boolean hadOriginal = Files.exists(CONFIG_PATH);
        try {
            if (hadOriginal) {
                Files.copy(CONFIG_PATH, backupPath, StandardCopyOption.REPLACE_EXISTING);
            }
            configData.set("enableTips", enableTips);
            configData.save();
            readValues();
            try {
                Files.deleteIfExists(backupPath);
            } catch (Exception cleanupException) {
                TipsModule.LOGGER.debug("TipsConfig: Failed to remove save backup", cleanupException);
            }
        } catch (Exception exception) {
            try {
                if (configData != null) {
                    configData.close();
                }
            } catch (Exception closeException) {
                TipsModule.LOGGER.debug("TipsConfig: Failed to close config before rollback", closeException);
            }
            configData = null;

            try {
                if (hadOriginal && Files.exists(backupPath)) {
                    Files.move(backupPath, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.deleteIfExists(backupPath);
                }
            } catch (Exception restoreFileException) {
                TipsModule.LOGGER.error("TipsConfig: Failed to restore config file after save failure", restoreFileException);
            }

            try {
                configData = CommentedFileConfig.builder(CONFIG_PATH)
                        .sync()
                        .preserveInsertionOrder()
                        .writingMode(WritingMode.REPLACE)
                        .build();
                configData.load();
                readValues();
            } catch (Exception reloadException) {
                TipsModule.LOGGER.error("TipsConfig: Failed to reload config after save failure", reloadException);
                if (configData != null) {
                    try {
                        configData.close();
                    } catch (Exception ignored) {
                    }
                    configData = null;
                }
            }
            throw new IllegalStateException("Failed to save tips config", exception);
        }
    }
}
