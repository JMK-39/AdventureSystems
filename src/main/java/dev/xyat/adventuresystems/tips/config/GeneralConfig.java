package dev.xyat.adventuresystems.tips.config;

import dev.xyat.kineticcore.api.config.common.KTCommonConfigApi;
import dev.xyat.kineticcore.api.config.common.KTCommonConfigSpec;

public final class GeneralConfig {
    private static final KTCommonConfigSpec.BooleanValue ENABLE_TIPS;
    private static final KTCommonConfigSpec SPEC;
    private static boolean registered;
    private static boolean dirty;

    public static boolean enableTips = true;

    static {
        KTCommonConfigSpec.Builder builder = KTCommonConfigSpec.builder();
        ENABLE_TIPS = builder
                .comment(
                        "是否启用屏幕提示。开启后将在加载界面、世界转换界面和游戏暂停界面显示自定义的小贴士。",
                        "Whether to enable screen tips. Custom tips will be shown on loading, world transition, and pause screens."
                )
                .define("enableTips", true);
        SPEC = builder.build();
    }

    private GeneralConfig() {
    }

    public static synchronized void load() {
        if (!registered) {
            KTCommonConfigApi.register(SPEC, "kineticcore/tips_settings.toml");
            registered = true;
        }
        syncFromLoadedSpec();
    }

    public static synchronized boolean isEnabled() {
        if (!dirty) {
            syncFromLoadedSpec();
        }
        return enableTips;
    }

    public static synchronized void setEnabled(boolean value) {
        enableTips = value;
        dirty = true;
    }

    public static synchronized void save() {
        if (!SPEC.isLoaded()) {
            return;
        }
        ENABLE_TIPS.set(enableTips);
        SPEC.save();
        enableTips = ENABLE_TIPS.get();
        dirty = false;
    }

    private static void syncFromLoadedSpec() {
        if (SPEC.isLoaded()) {
            enableTips = ENABLE_TIPS.get();
            dirty = false;
        }
    }
}
