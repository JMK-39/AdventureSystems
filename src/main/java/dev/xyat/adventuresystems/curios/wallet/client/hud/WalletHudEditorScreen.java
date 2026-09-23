package dev.xyat.adventuresystems.curios.wallet.client.hud;

import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.config.CuriosConfigGui;
import dev.xyat.adventuresystems.curios.wallet.data.CurrencyType;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.kineticcore.api.client.screen.KineticNativeScreen;
import dev.xyat.kineticcore.api.client.selector.HudPositionEditor;
import dev.xyat.kineticcore.api.config.client.KTConfigApi;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import dev.xyat.kineticcore.api.text.KineticI18n;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/** Drag-and-scale editor for the currency wallet HUD. */
public final class WalletHudEditorScreen extends KineticNativeScreen {
    private static final double MIN_SCALE = 0.1D;
    private static final double DEFAULT_SCALE = 0.75D;

    private final Screen parent;
    private final HudPositionEditor editor = new HudPositionEditor();
    private final List<CurrencyType> previewCurrencies;
    private final CompoundTag previewBalances = new CompoundTag();
    private boolean draftConfigured;

    public WalletHudEditorScreen(Screen parent) {
        super(KineticI18n.translatable("screen.adventuresystems.wallet_hud.editor.title"));
        this.parent = parent;
        setParentScreen(parent);
        reserveStandaloneDraft();
        this.previewCurrencies = createPreviewCurrencies();
        for (int i = 0; i < previewCurrencies.size(); i++) {
            previewBalances.putLong(previewCurrencies.get(i).itemId(), (long) (i + 1) * 12_345L);
        }
    }

    @Override
    protected void buildUi() {
        int previewWidth = Hud.contentWidth(previewCurrencies.size());
        int previewHeight = Hud.contentHeight(previewCurrencies.size());
        double initialScale = Math.max(MIN_SCALE, CuriosConfig.walletHudScale);
        int initialHeight = scaledSize(previewHeight, initialScale);
        int defaultHeight = scaledSize(previewHeight, DEFAULT_SCALE);

        editor.initialize(
                width,
                height,
                previewWidth,
                previewHeight,
                Math.max(0, CuriosConfig.walletHudOffsetX),
                height - initialHeight - Math.max(0, CuriosConfig.walletHudOffsetY),
                0,
                height - defaultHeight,
                initialScale,
                DEFAULT_SCALE,
                MIN_SCALE
        );
        if (!draftConfigured) {
            configureStandaloneDraft(editor::snapshot, editor::restore);
            draftConfigured = true;
        }

        editor.addControlButtons(
                button -> addControl(button, null),
                KineticI18n.translatable("gui.kineticcore.hud_editor.save"),
                KineticI18n.translatable("gui.kineticcore.hud_editor.reset"),
                KineticI18n.translatable("gui.kineticcore.hud_editor.cancel"),
                this::save,
                this::closeWithoutSaving
        );
    }

    @Override
    protected void renderNativeBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        editor.render(
                graphics,
                font,
                mouseX,
                mouseY,
                title,
                KineticI18n.translatable("screen.kineticcore.hud_editor.instruction_scale"),
                KineticI18n.translatable(
                        "screen.kineticcore.hud_editor.position_scale",
                        Component.literal(String.valueOf(currentOffsetX())),
                        Component.literal(String.valueOf(currentOffsetY())),
                        Component.literal(String.valueOf(Math.round(editor.getScale() * 100.0D)))
                ),
                (g, x, y, mx, my) -> Hud.renderCells(
                        g, font, previewCurrencies, previewBalances, x, y)
        );
    }

    @Override
    protected boolean nativeMouseClicked(double mouseX, double mouseY, int button) {
        return editor.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected boolean nativeMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return editor.mouseDragged(mouseX, mouseY, button);
    }

    @Override
    protected boolean nativeMouseReleased(double mouseX, double mouseY, int button) {
        return editor.mouseReleased(button);
    }

    @Override
    protected boolean nativeMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        return editor.mouseScrolled(mouseX, mouseY, scrollDelta);
    }

    @Override
    protected boolean nativeKeyPressed(int keyCode, int scanCode, int modifiers) {
        return editor.keyPressed(keyCode, hasShiftDown());
    }

    @Override
    protected boolean handleCloseRequest() {
        closeWithoutSaving();
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void save() {
        CuriosConfig.walletHudOffsetX = currentOffsetX();
        CuriosConfig.walletHudOffsetY = currentOffsetY();
        CuriosConfig.walletHudScale = editor.getScale();
        CuriosConfig.saveClientSettings();
        KTConfigApi.notifySaved(CuriosConfigGui.HUD_PAGE_ID);
        commitDraft();
        KTConfigApi.refreshScreenFromSource(parent);
    }

    private void closeWithoutSaving() {
        discardDraft();
        closeScreen();
    }

    private void closeScreen() {
        navigateBack();
    }

    private int currentOffsetX() {
        return Math.max(0, editor.getX());
    }

    private int currentOffsetY() {
        return Math.max(0, height - editor.getElementHeight() - editor.getY());
    }

    private static List<CurrencyType> createPreviewCurrencies() {
        List<CurrencyType> configured = Data.currencies().stream()
                .filter(CurrencyType::hasItem)
                .limit(10)
                .toList();
        if (!configured.isEmpty()) return configured;

        List<CurrencyType> fallback = new ArrayList<>();
        fallback.add(currency("minecraft:gold_ingot"));
        fallback.add(currency("minecraft:diamond"));
        fallback.add(currency("minecraft:emerald"));
        return List.copyOf(fallback);
    }

    private static CurrencyType currency(String id) {
        ResourceLocation location = KineticResourceIds.parse(id);
        return new CurrencyType(location.toString(), location, 1L);
    }

    private static int scaledSize(int baseSize, double scale) {
        double result = Math.ceil(baseSize * scale);
        if (!Double.isFinite(result) || result >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return Math.max(1, (int) result);
    }
}
