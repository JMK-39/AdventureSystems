package dev.xyat.adventuresystems.curios.wallet.client.hud;

import dev.xyat.kineticcore.api.client.selector.HudPositionEditor;
import dev.xyat.kineticcore.config.client.KTConfigScreen;
import dev.xyat.kineticcore.config.client.KTConfigApi;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.config.CuriosConfigGui;
import dev.xyat.adventuresystems.curios.wallet.data.CurrencyType;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import dev.xyat.kineticcore.api.client.screen.KineticNativeScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/** Drag-and-scale editor for the currency wallet HUD. */
@OnlyIn(Dist.CLIENT)
public final class WalletHudEditorScreen extends KineticNativeScreen {
    private static final double MIN_SCALE = 0.1D;
    private static final double DEFAULT_SCALE = 0.75D;

    private final Screen parent;
    private final HudPositionEditor editor = new HudPositionEditor();
    private final List<CurrencyType> previewCurrencies;
    private final CompoundTag previewBalances = new CompoundTag();
    private boolean draftConfigured;

    public WalletHudEditorScreen(Screen parent) {
        super(Component.translatable("screen.adventuresystems.wallet_hud.editor.title"));
        this.parent = parent;
        reserveStandaloneDraft();
        this.previewCurrencies = createPreviewCurrencies();
        for (int i = 0; i < previewCurrencies.size(); i++) {
            previewBalances.putLong(previewCurrencies.get(i).itemId(), (long) (i + 1) * 12_345L);
        }
    }

    @Override
    protected void init() {
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
                this::addRenderableWidget,
                Component.translatable("gui.kineticcore.hud_editor.save"),
                Component.translatable("gui.kineticcore.hud_editor.reset"),
                Component.translatable("gui.kineticcore.hud_editor.cancel"),
                this::saveAndClose,
                this::closeWithoutSaving
        );
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        editor.render(
                graphics,
                font,
                mouseX,
                mouseY,
                title,
                Component.translatable("screen.kineticcore.hud_editor.instruction_scale"),
                Component.translatable(
                        "screen.kineticcore.hud_editor.position_scale",
                        Component.literal(String.valueOf(currentOffsetX())).withStyle(ChatFormatting.AQUA),
                        Component.literal(String.valueOf(currentOffsetY())).withStyle(ChatFormatting.AQUA),
                        Component.literal(String.valueOf(Math.round(editor.getScale() * 100.0D)))
                                .withStyle(ChatFormatting.YELLOW)
                ),
                (g, x, y, mx, my) -> Hud.renderCells(
                        g, font, previewCurrencies, previewBalances, x, y)
        );
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (editor.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (editor.mouseDragged(mouseX, mouseY, button)) return true;
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (editor.mouseReleased(button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        if (editor.mouseScrolled(mouseX, mouseY, scrollDelta)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollDelta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (editor.keyPressed(keyCode, hasShiftDown())) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        closeWithoutSaving();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void saveAndClose() {
        CuriosConfig.walletHudOffsetX = currentOffsetX();
        CuriosConfig.walletHudOffsetY = currentOffsetY();
        CuriosConfig.walletHudScale = editor.getScale();
        CuriosConfig.saveClientSettings();
        KTConfigApi.notifySaved(CuriosConfigGui.HUD_PAGE_ID);
        commitDraft();
        if (parent instanceof KTConfigScreen configScreen) {
            configScreen.refreshFromSource();
        }
        closeScreen();
    }

    private void closeWithoutSaving() {
        closeScreen();
    }

    private void closeScreen() {
        Minecraft.getInstance().setScreen(parent);
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
        ResourceLocation location = new ResourceLocation(id);
        return new CurrencyType(location.toString(), location, 1L);
    }

    private static int scaledSize(int baseSize, double scale) {
        double result = Math.ceil(baseSize * scale);
        if (!Double.isFinite(result) || result >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return Math.max(1, (int) result);
    }
}
