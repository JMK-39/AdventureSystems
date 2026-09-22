package dev.xyat.adventuresystems.tips.config;

import dev.xyat.adventuresystems.tips.TipsNetwork;
import dev.xyat.kineticcore.api.config.client.KTConfigApi;
import dev.xyat.kineticcore.api.config.client.KTConfigPage;
import dev.xyat.kineticcore.api.config.client.KTConfigScope;
import dev.xyat.kineticcore.api.text.KineticI18n;
import net.minecraft.client.gui.screens.Screen;

public final class TipsConfigGui {
    public static final String PAGE_ID = "adventuresystems:tips";
    public static final String EDITOR_PAGE_ID = "adventuresystems:editor";

    private TipsConfigGui() {
    }

    public static void load() {
        KTConfigApi.register(KTConfigPage.builder(
                        PAGE_ID,
                        KineticI18n.translatable("cfg.adventuresystems.tips.tips.title")
                )
                .scope(KTConfigScope.CLIENT_LOCAL)
                .applyTiming(KTConfigPage.ApplyTiming.IMMEDIATE)
                .pageDescription(KineticI18n.translatable("cfg.adventuresystems.tips.tips.description"))
                .booleanValue(
                        "enable_tips",
                        KineticI18n.translatable("cfg.adventuresystems.tips.tips.enable"),
                        GeneralConfig::isEnabled,
                        GeneralConfig::setEnabled,
                        true,
                        KineticI18n.translatable("cfg.adventuresystems.tips.tips.enable.tooltip")
                )
                .onSave(GeneralConfig::save)
                .build());

        KTConfigApi.register(KTConfigPage.builder(
                        EDITOR_PAGE_ID,
                        KineticI18n.translatable("cfg.adventuresystems.tips.tips.editor.section")
                )
                .scope(KTConfigScope.SERVER_AUTHORITATIVE)
                .serverManaged()
                .applyTiming(KTConfigPage.ApplyTiming.IMMEDIATE)
                .pageDescription(KineticI18n.translatable("cfg.adventuresystems.tips.tips.editor.tooltip"))
                .action(
                        "open_editor",
                        KineticI18n.translatable("cfg.adventuresystems.tips.tips.editor"),
                        TipsNetwork::requestEditor,
                        KineticI18n.translatable("cfg.adventuresystems.tips.tips.editor.tooltip")
                )
                .build());
    }

    public static Screen create(Screen parent) {
        return KTConfigApi.createScreenForOwner(parent, "adventuresystems");
    }
}
