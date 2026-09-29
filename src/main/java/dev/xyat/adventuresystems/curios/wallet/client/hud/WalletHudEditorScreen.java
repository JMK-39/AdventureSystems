package dev.xyat.adventuresystems.curios.wallet.client.hud;

import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.config.CuriosConfigGui;
import dev.xyat.adventuresystems.curios.wallet.data.CurrencyType;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.selector.KineticHudEditorPage;
import dev.xyat.kineticcore.api.config.client.KTConfigApi;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import dev.xyat.kineticcore.api.text.KineticI18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/** Drag-and-scale editor for the currency wallet HUD. */
public final class WalletHudEditorScreen extends KineticHudEditorPage {
    private static final double MIN_SCALE = 0.1D;
    private static final double DEFAULT_SCALE = 0.75D;

    private final List<CurrencyType> previewCurrencies;
    private final CompoundTag previewBalances = new CompoundTag();

    public WalletHudEditorScreen() {
        super(KineticI18n.translatable("screen.adventuresystems.wallet_hud.editor.title"));
        reserveStandaloneDraft();
        configureStandaloneDraft(
                () -> new HudSettings(CuriosConfig.walletHudOffsetX, CuriosConfig.walletHudOffsetY, CuriosConfig.walletHudScale),
                settings -> {
                    CuriosConfig.walletHudOffsetX = settings.x();
                    CuriosConfig.walletHudOffsetY = settings.y();
                    CuriosConfig.walletHudScale = settings.scale();
                });
        previewCurrencies = createPreviewCurrencies();
        for (int i = 0; i < previewCurrencies.size(); i++) {
            previewBalances.putLong(previewCurrencies.get(i).itemId(), (long) (i + 1) * 12_345L);
        }
    }

    @Override
    protected int elementWidth() {
        return Hud.contentWidth(previewCurrencies.size());
    }

    @Override
    protected int elementHeight() {
        return Hud.contentHeight(previewCurrencies.size());
    }

    @Override
    protected HudLayout initialLayout(int screenWidth, int screenHeight) {
        double scale = Math.max(MIN_SCALE, CuriosConfig.walletHudScale);
        return new HudLayout(Math.max(0, CuriosConfig.walletHudOffsetX),
                screenHeight - scaledSize(elementHeight(), scale) - Math.max(0, CuriosConfig.walletHudOffsetY), scale);
    }

    @Override
    protected HudLayout defaultLayout(int screenWidth, int screenHeight) {
        return new HudLayout(0, screenHeight - scaledSize(elementHeight(), DEFAULT_SCALE), DEFAULT_SCALE);
    }

    @Override
    protected void renderElement(KineticGraphics graphics, int x, int y, int mouseX, int mouseY) {
        Hud.renderCells(graphics, previewCurrencies, previewBalances, x, y);
    }

    @Override
    protected Component positionText(HudLayout layout) {
        return KineticI18n.translatable("screen.kineticcore.hud_editor.position_scale",
                Component.literal(String.valueOf(Math.max(0, layout.x()))),
                Component.literal(String.valueOf(Math.max(0, height() - scaledElementHeight() - layout.y()))),
                Component.literal(String.valueOf(Math.round(layout.scale() * 100.0D))));
    }

    @Override
    protected double minimumScale() {
        return MIN_SCALE;
    }

    @Override
    protected void save(HudLayout layout) {
        CuriosConfig.walletHudOffsetX = Math.max(0, layout.x());
        CuriosConfig.walletHudOffsetY = Math.max(0, height() - scaledElementHeight() - layout.y());
        CuriosConfig.walletHudScale = layout.scale();
        CuriosConfig.saveClientSettings();
        KTConfigApi.notifySaved(CuriosConfigGui.HUD_PAGE_ID);
        commitDraft();
        KTConfigApi.refreshOpenScreens();
    }

    @Override
    protected boolean onCloseRequested() {
        discardDraft();
        navigateBack();
        return true;
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

    private record HudSettings(int x, int y, double scale) {
    }
}
