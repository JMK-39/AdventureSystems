package dev.xyat.adventuresystems.ftb.client;

import dev.xyat.kineticcore.api.config.client.KTConfigApi;
import dev.xyat.kineticcore.api.config.client.KTConfigPage;
import dev.xyat.kineticcore.api.config.client.KTConfigScope;
import dev.xyat.adventuresystems.ftb.client.gui.FTBItemBindingEditorScreen;
import dev.xyat.adventuresystems.text.AdventureText;

/** FTB Quests contributes this page only while that optional mod is installed. */
public final class FTBConfigGui {
    public static final String PAGE_ID = "adventuresystems:ftb_quests";

    private FTBConfigGui() {
    }

    public static void load() {
        KTConfigApi.register(KTConfigPage.builder(
                        PAGE_ID,
                        AdventureText.translatable("cfg.adventuresystems.ftb.title")
                )
                .scope(KTConfigScope.CLIENT_LOCAL)
                .applyTiming(KTConfigPage.ApplyTiming.IMMEDIATE)
                .booleanValue(
                        "enable_task_jump",
                        AdventureText.translatable("cfg.adventuresystems.ftb.enable_task_jump"),
                        FTBClientConfig::isTaskJumpEnabled,
                        FTBClientConfig::setTaskJumpEnabled,
                        true,
                        AdventureText.translatable("cfg.adventuresystems.ftb.enable_task_jump.tooltip")
                )
                .action(
                        "open_binding_editor",
                        AdventureText.translatable("cfg.adventuresystems.ftb.open_editor"),
                        KTConfigApi.pageAction(FTBItemBindingEditorScreen::new),
                        AdventureText.translatable("cfg.adventuresystems.ftb.open_editor.tooltip")
                )
                .onSave(FTBClientConfig::save)
                .build());
    }

    public static void open() {
        KTConfigApi.openPage(PAGE_ID);
    }
}
