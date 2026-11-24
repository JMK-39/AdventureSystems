package dev.xyat.adventuresystems.curios.wallet.event;

import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.wallet.compat.rs.RefinedStorageCompat;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.adventuresystems.curios.wallet.network.Network;
import dev.xyat.kineticcore.api.event.KineticEventPriority;
import dev.xyat.kineticcore.api.player.event.KineticPlayerEvents;
import dev.xyat.kineticcore.api.runtime.KineticPaths;
import dev.xyat.kineticcore.api.runtime.KineticPlatform;
import dev.xyat.kineticcore.api.server.event.KineticServerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class WalletModule {
    public static final String SHOP_CONFIG_FILE = "kineticcore/adventuresystems/currency_wallet_shop.toml";
    public static final String SHOP_CLIENT_CONFIG_FILE = "kineticcore/adventuresystems/currency_wallet_shop_client.toml";
    private static final String HAD_WALLET_KEY = "adventuresystems_currency_wallet_had_wallet";

    private WalletModule() {
    }

    public static void install() {
        KineticPaths.migrateConfigFile("kineticcore/currency_wallet_shop.toml", SHOP_CONFIG_FILE);
        KineticPaths.migrateConfigFile("kineticcore/currency_wallet_shop_client.toml", SHOP_CLIENT_CONFIG_FILE);
        KineticServerEvents.onPlayerLogin(KineticEventPriority.NORMAL, WalletModule::onLogin);
        KineticServerEvents.onPlayerTick(
                KineticEventPriority.NORMAL,
                KineticServerEvents.TickPhase.END,
                WalletModule::onPlayerTick
        );
        KineticPlayerEvents.onRightClickBlock(KineticEventPriority.NORMAL, WalletModule::onRightClickBlock);
        KineticServerEvents.onPlayerClone(KineticEventPriority.NORMAL, WalletModule::onClone);
        KineticServerEvents.onPlayerLogout(KineticEventPriority.NORMAL, WalletModule::onLogout);
    }

    private static void onLogin(ServerPlayer serverPlayer) {
        CompoundTag data = serverPlayer.getPersistentData();
        data.putBoolean(HAD_WALLET_KEY, false);

        if (!CuriosConfig.enableCurrencyWallet) {
            Network.hide(serverPlayer);
            return;
        }

        Optional<ItemStack> wallet = Data.equippedWallet(serverPlayer);
        if (wallet.isPresent()) {
            Data.ensureWalletIdentity(wallet.get());
            data.putBoolean(HAD_WALLET_KEY, true);
            Network.sync(serverPlayer);
        } else {
            Network.hide(serverPlayer);
        }
    }

    private static void onPlayerTick(ServerPlayer serverPlayer) {
        if (!CuriosConfig.enableCurrencyWallet) return;

        Optional<ItemStack> wallet = Data.equippedWallet(serverPlayer);
        boolean equipped = wallet.isPresent();
        CompoundTag data = serverPlayer.getPersistentData();
        boolean hadWallet = data.getBoolean(HAD_WALLET_KEY);
        if (!equipped) {
            if (hadWallet) {
                data.putBoolean(HAD_WALLET_KEY, false);
                Network.hide(serverPlayer);
            }
            return;
        }

        ItemStack walletStack = wallet.get();
        Data.ensureWalletIdentity(walletStack);

        if (!hadWallet) {
            data.putBoolean(HAD_WALLET_KEY, true);
            Network.sync(serverPlayer);
        } else if (serverPlayer.tickCount % 20 == 0) {
            Network.sync(serverPlayer);
        }
    }

    private static void onRightClickBlock(KineticPlayerEvents.RightClickBlockContext event) {
        if (!CuriosConfig.enableCurrencyWallet) return;
        if (!(event.player() instanceof ServerPlayer player)) return;
        if (!(event.level() instanceof ServerLevel level)) return;
        ItemStack stack = event.stack();
        if (!Data.isWalletStack(stack)) return;

        if (!player.isShiftKeyDown()) return;

        BlockPos pos = event.pos();
        if (!KineticPlatform.isModLoaded("refinedstorage")) return;
        if (!RefinedStorageCompat.isController(level, pos)) return;
        event.cancel();
//? if >=1.21 {
        /*if (!RefinedStorageCompat.canBind(player, level, pos)) {
        *///?} else {
        if (!RefinedStorageCompat.canBind(level, pos)) {
        //?}
            Network.toast(player, "msg.adventuresystems.curios.wallet.rs_bind_fail");
            return;
        }
        Data.bindRsController(stack, level, pos);
        Network.toast(player, "msg.adventuresystems.curios.wallet.rs_bind_success", pos.getX() + " " + pos.getY() + " " + pos.getZ());
        Network.sync(player);
    }

    private static void onClone(ServerPlayer original, ServerPlayer current, boolean wasDeath) {
        Data.copy(original, current);
    }

    private static void onLogout(ServerPlayer serverPlayer) {
        serverPlayer.getPersistentData().putBoolean(HAD_WALLET_KEY, false);
        Network.hide(serverPlayer);
    }

    public static boolean hasWallet(Player player) {
        return Data.hasWallet(player);
    }
}
