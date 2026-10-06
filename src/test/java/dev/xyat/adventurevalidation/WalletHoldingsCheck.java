//? if >=1.21 {
/*package dev.xyat.adventurevalidation;

import dev.xyat.adventuresystems.curios.init.Items;
import dev.xyat.adventuresystems.curios.wallet.client.gui.MainScreen;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.adventuresystems.curios.wallet.network.Network;
import dev.xyat.kineticcore.api.client.event.KineticClientEvents;
import dev.xyat.kineticcore.api.client.gui.KineticGui;
import dev.xyat.kineticcore.api.client.gui.overlay.KineticOverlays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.nio.file.*;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/^*
 * Gives the player a wallet and 64 + 64 + 12 of the first currency, opens the wallet through the server, and logs and
 * captures the currency tooltip that lists where the money is held.
 *^/
public final class WalletHoldingsCheck {
    private static final Logger LOG = LoggerFactory.getLogger(WalletHoldingsCheck.class);
    private static final String ROOT = System.getProperty("adventuresystems.walletHoldingsCheck.output", "D:/IDEAWork/AdventureSystems/.gradle/wallet-holdings/");
    private static boolean installed, prepared, opened, finished;
    private static long due;
    private static String currencyId;
    private static List<Component> lines;

    public static void install() {
        if (installed) return;
        installed = true;
        KineticClientEvents.onTick(KineticClientEvents.TickPhase.END, WalletHoldingsCheck::tick);
    }

    private static void tick() {
        if (finished) return;
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        long now = System.currentTimeMillis();
        try {
            if (!prepared) {
                prepared = true;
                var server = mc.getSingleplayerServer();
                var player = server.getPlayerList().getPlayer(mc.player.getUUID());
                server.submit(() -> {
                    if (!Data.hasWallet(player)) {
                        top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).flatMap(handler -> handler.getStacksHandler("belt"))
                                .ifPresent(belt -> belt.getStacks().setStackInSlot(0, new ItemStack(Items.CURRENCY_WALLET.get())));
                    }
                    var currency = Data.currencies().stream().filter(c -> c.hasItem()).findFirst().orElseThrow();
                    currencyId = currency.itemId();
                    for (int count : new int[] {64, 64, 12}) player.getInventory().add(new ItemStack(currency.item(), count));
                    LOG.info("WALLET_HOLDINGS_PREPARED currency={} wallet={}", currencyId, Data.hasWallet(player));
                }).join();
                due = now + 1000;
                return;
            }
            if (!opened) {
                if (now < due) return;
                opened = true;
                Network.sendOpen();
                due = now + 2500;
                return;
            }
            if (now < due) return;
            var page = KineticGui.currentPage();
            if (!(page instanceof MainScreen)) throw new IllegalStateException("wallet did not open: " + page);
            if (lines == null) {
                var method = MainScreen.class.getDeclaredMethod("holdingLines", String.class);
                method.setAccessible(true);
                @SuppressWarnings("unchecked") List<Component> found = (List<Component>) method.invoke(page, currencyId);
                lines = found;
                for (Component line : lines) LOG.info("WALLET_HOLDINGS_LINE {}", line.getString());
                due = now + 300;
                return;
            }
            KineticOverlays.requestTooltip(lines, mc.getWindow().getGuiScaledWidth() / 2, mc.getWindow().getGuiScaledHeight() / 3);
            if (now < due + 400) return;
            Path path = Path.of(ROOT, "wallet-holdings.png");
            Files.createDirectories(path.getParent());
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(path);}
            LOG.info("WALLET_HOLDINGS_PASS lines={}", lines.size());
            finish();
        } catch (Throwable error) {
            LOG.error("WALLET_HOLDINGS_FAIL", error);
            finish();
        }
    }

    private static void finish() {
        finished = true;
        dev.xyat.kineticcore.api.runtime.KineticClientRuntime.stopClient();
    }
}
*///?}
