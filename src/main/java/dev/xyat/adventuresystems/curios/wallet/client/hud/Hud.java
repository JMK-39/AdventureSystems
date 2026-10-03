package dev.xyat.adventuresystems.curios.wallet.client.hud;

import dev.xyat.adventuresystems.text.AdventureText;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.wallet.client.Client;
import dev.xyat.adventuresystems.curios.wallet.data.CurrencyType;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.kineticcore.api.client.event.KineticClientEvents;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

public final class Hud {
    private static final int MAX_PER_ROW = 5;
    private static final int CELL_WIDTH = 50;
    private static final int CELL_HEIGHT = 16;
    private static final int GAP = 1;

    private Hud() {
    }

    public static void install() {
        KineticClientEvents.onHudRender(KineticClientEvents.HudStage.END, Hud::onRenderGui);
    }

    private static void onRenderGui(KineticGraphics graphics, float partialTick) {
        if (KineticClientRuntime.localPlayer() == null
                || KineticClientRuntime.currentLevel() == null
                || !KineticClientRuntime.connected()) {
            Client.clearClientWalletState();
            return;
        }
        if (!Client.hudVisible()) return;
        List<CurrencyType> currencies = Data.currencies();
        if (currencies.isEmpty()) return;
        render(
                graphics,
                KineticClientRuntime.guiScaledWidth(),
                KineticClientRuntime.guiScaledHeight(),
                currencies,
                Client.hudBalances()
        );
    }

    private static void render(KineticGraphics graphics, int screenWidth, int screenHeight, List<CurrencyType> currencies, CompoundTag balances) {
        double scale = Math.max(0.1D, CuriosConfig.walletHudScale);
        int total = currencies.size();
        int contentWidth = contentWidth(total);
        int contentHeight = contentHeight(total);
        double x = Math.max(0, CuriosConfig.walletHudOffsetX) / scale;
        double y = (screenHeight - Math.max(0, CuriosConfig.walletHudOffsetY)) / scale - contentHeight;
        double maxX = screenWidth / scale - contentWidth;
        if (x > maxX) x = Math.max(0, maxX);
        if (y < 0) y = 0;
        graphics.push();
        graphics.scale((float) scale, (float) scale);
        try {
            renderCells(graphics, currencies, balances, (int) x, (int) y);
        } finally {
            graphics.pop();
        }
    }

    static int contentWidth(int total) {
        int columns = Math.min(MAX_PER_ROW, Math.max(0, total));
        return columns * CELL_WIDTH + Math.max(0, columns - 1) * GAP;
    }

    static int contentHeight(int total) {
        int rows = (Math.max(0, total) + MAX_PER_ROW - 1) / MAX_PER_ROW;
        return rows * CELL_HEIGHT + Math.max(0, rows - 1) * GAP;
    }

    static void renderCells(KineticGraphics graphics, List<CurrencyType> currencies, CompoundTag balances, int x, int y) {
        for (int i = 0; i < currencies.size(); i++) {
            int row = i / MAX_PER_ROW;
            int col = i % MAX_PER_ROW;
            int cellX = x + col * (CELL_WIDTH + GAP);
            int cellY = y + row * (CELL_HEIGHT + GAP);
            CurrencyType currency = currencies.get(i);
            renderCell(graphics, cellX, cellY, currency, Data.readAmount(balances, currency.itemId()));
        }
    }

    private static void renderCell(KineticGraphics graphics, int x, int y, CurrencyType currency, long amount) {
        graphics.item(stack(currency.itemId()), x, y);
        Component text = AdventureText.translatable("gui.adventuresystems.curios.wallet.hud_amount", compact(amount));
        graphics.text(text, x + 18, y + 4, KineticTheme.current().text(), true);
    }

    private static ItemStack stack(String id) {
        Item item = KineticRegistries.items().get(KineticResourceIds.parse(id));
        if (item == null) item = net.minecraft.world.item.Items.BARRIER;
        return new ItemStack(item);
    }

    private static String compact(long value) {
        long safeValue = Math.max(0L, value);
        if (safeValue >= 1_000_000_000_000_000_000L) return String.format(Locale.ROOT, "%dQi+", safeValue / 1_000_000_000_000_000_000L);
        if (safeValue >= 1_000_000_000_000_000L) return String.format(Locale.ROOT, "%dQa+", safeValue / 1_000_000_000_000_000L);
        if (safeValue >= 1_000_000_000_000L) return String.format(Locale.ROOT, "%dT+", safeValue / 1_000_000_000_000L);
        if (safeValue >= 1_000_000_000L) return String.format(Locale.ROOT, "%dB+", safeValue / 1_000_000_000L);
        if (safeValue >= 1_000_000L) return String.format(Locale.ROOT, "%dM+", safeValue / 1_000_000L);
        if (safeValue >= 1_000L) return String.format(Locale.ROOT, "%dK+", safeValue / 1_000L);
        return Long.toString(safeValue);
    }
}
