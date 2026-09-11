package dev.xyat.adventuresystems.tips.config;

import dev.xyat.kineticcore.config.client.KTConfigApi;
import dev.xyat.kineticcore.config.client.KTConfigPage;
import dev.xyat.kineticcore.config.client.KTConfigScope;
import dev.xyat.adventuresystems.tips.TipsNetwork;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class TipsConfigGui {
    public static final String PAGE_ID = "adventuresystems:tips";
    public static final String EDITOR_PAGE_ID = "adventuresystems:editor";

    private TipsConfigGui() {
    }

    public static void load() {
        KTConfigApi.register(KTConfigPage.builder(
                        PAGE_ID,
                        Component.translatable("cfg.adventuresystems.tips.tips.title")
                )
                .scope(KTConfigScope.CLIENT_LOCAL)
                .applyTiming(KTConfigPage.ApplyTiming.IMMEDIATE)
                .pageDescription(Component.translatable("cfg.adventuresystems.tips.tips.description"))
                .booleanValue(
                        "enable_tips",
                        Component.translatable("cfg.adventuresystems.tips.tips.enable"),
                        () -> GeneralConfig.enableTips,
                        value -> GeneralConfig.enableTips = value,
                        true,
                        Component.translatable("cfg.adventuresystems.tips.tips.enable.tooltip")
                )
                .onSave(GeneralConfig::save)
                .build());

        KTConfigApi.register(KTConfigPage.builder(
                        EDITOR_PAGE_ID,
                        Component.translatable("cfg.adventuresystems.tips.tips.editor.section")
                )
                .scope(KTConfigScope.SERVER_AUTHORITATIVE)
                .serverManaged()
                .applyTiming(KTConfigPage.ApplyTiming.IMMEDIATE)
                .pageDescription(Component.translatable("cfg.adventuresystems.tips.tips.editor.tooltip"))
                .action(
                        "open_editor",
                        Component.translatable("cfg.adventuresystems.tips.tips.editor"),
                        TipsNetwork::requestEditor,
                        Component.translatable("cfg.adventuresystems.tips.tips.editor.tooltip")
                )
                .build());
    }

    public static Screen create(Screen parent) {
        return KTConfigApi.createScreenForOwner(parent, "adventuresystems");
    }
}
