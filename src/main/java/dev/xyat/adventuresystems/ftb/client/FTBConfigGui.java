package dev.xyat.adventuresystems.ftb.client;

import dev.xyat.kineticcore.api.config.client.KTConfigApi;
import dev.xyat.kineticcore.api.config.client.KTConfigPage;
import dev.xyat.kineticcore.api.config.client.KTConfigScope;
import dev.xyat.adventuresystems.ftb.client.gui.FTBItemBindingEditorScreen;
import net.minecraft.client.gui.screens.Screen;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import dev.xyat.kineticcore.api.text.KineticI18n;

/** FTB Quests contributes this page only while that optional mod is installed. */
public final class FTBConfigGui {
    public static final String PAGE_ID = "adventuresystems:ftb_quests";

    private FTBConfigGui() {
    }

    public static void load() {
        KTConfigApi.register(KTConfigPage.builder(
                        PAGE_ID,
                        KineticI18n.translatable("cfg.adventuresystems.ftb.title")
                )
                .scope(KTConfigScope.CLIENT_LOCAL)
                .applyTiming(KTConfigPage.ApplyTiming.IMMEDIATE)
                .booleanValue(
                        "enable_task_jump",
                        KineticI18n.translatable("cfg.adventuresystems.ftb.enable_task_jump"),
                        FTBClientConfig::isTaskJumpEnabled,
                        FTBClientConfig::setTaskJumpEnabled,
                        true,
                        KineticI18n.translatable("cfg.adventuresystems.ftb.enable_task_jump.tooltip")
                )
                .action(
                        "open_binding_editor",
                        KineticI18n.translatable("cfg.adventuresystems.ftb.open_editor"),
                        FTBConfigGui::openEditor,
                        KineticI18n.translatable("cfg.adventuresystems.ftb.open_editor.tooltip")
                )
                .onSave(FTBClientConfig::save)
                .build());
    }

    public static Screen create(Screen parent) {
        return KTConfigApi.createScreen(parent, PAGE_ID);
    }

    private static void openEditor() {
        KineticClientRuntime.openScreen(new FTBItemBindingEditorScreen(KineticClientRuntime.currentScreen()));
    }
}
