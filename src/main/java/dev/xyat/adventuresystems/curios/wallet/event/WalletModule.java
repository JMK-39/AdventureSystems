package dev.xyat.adventuresystems.curios.wallet.event;

import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.wallet.compat.rs.RefinedStorageCompat;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.adventuresystems.curios.wallet.network.Network;
import dev.xyat.kineticcore.api.event.KineticEventPriority;
import dev.xyat.kineticcore.api.player.event.KineticPlayerEvents;
import dev.xyat.kineticcore.api.runtime.KineticPlatform;
import dev.xyat.kineticcore.api.server.event.KineticServerEvents;
import dev.xyat.kineticcore.api.world.event.KineticWorldEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public final class WalletModule {
    private static final String HAD_WALLET_KEY = "adventuresystems_currency_wallet_had_wallet";

    private WalletModule() {
    }

    public static void install() {
        KineticServerEvents.onPlayerLogin(KineticEventPriority.NORMAL, WalletModule::onLogin);
        KineticServerEvents.onPlayerTick(
                KineticEventPriority.NORMAL,
                KineticServerEvents.TickPhase.END,
                WalletModule::onPlayerTick
        );
        KineticPlayerEvents.onRightClickBlock(KineticEventPriority.NORMAL, WalletModule::onRightClickBlock);
        KineticPlayerEvents.onRightClickItem(KineticEventPriority.NORMAL, WalletModule::onRightClickItem);
        KineticWorldEvents.onItemPickup(KineticEventPriority.NORMAL, WalletModule::onPickup);
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

        if (Data.isMagnetDisabled(walletStack)) return;
        if (CuriosConfig.walletMagnetRange <= 0.0) return;
        int interval = Math.max(1, CuriosConfig.walletMagnetIntervalTicks);
        if (serverPlayer.tickCount % interval != 0) return;

        double range = CuriosConfig.walletMagnetRange;
        List<ItemEntity> items = serverPlayer.level().getEntitiesOfClass(
                ItemEntity.class,
                serverPlayer.getBoundingBox().inflate(range),
                itemEntity -> Data.isCurrencyItem(itemEntity.getItem())
        );
        boolean changed = false;
        for (ItemEntity itemEntity : items) {
            if (Data.depositStack(serverPlayer, walletStack, itemEntity.getItem())) {
                itemEntity.discard();
                changed = true;
            }
        }
        if (changed) {
            serverPlayer.playNotifySound(SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f, 1.6f);
            Network.sync(serverPlayer);
        }
    }

    private static void onRightClickBlock(KineticPlayerEvents.RightClickBlockContext event) {
        if (!CuriosConfig.enableCurrencyWallet) return;
        if (!(event.player() instanceof ServerPlayer player)) return;
        if (!(event.level() instanceof ServerLevel level)) return;
        ItemStack stack = event.stack();
        if (!Data.isWalletStack(stack)) return;

        if (!player.isShiftKeyDown()) {
            event.cancel();
            toggleMagnet(player, stack);
            return;
        }

        BlockPos pos = event.pos();
        if (!KineticPlatform.isModLoaded("refinedstorage")) return;
        if (!RefinedStorageCompat.isController(level, pos)) return;
        event.cancel();
        if (!RefinedStorageCompat.canBind(level, pos)) {
            Network.toast(player, "msg.adventuresystems.curios.wallet.rs_bind_fail");
            return;
        }
        Data.bindRsController(stack, level, pos);
        Network.toast(player, "msg.adventuresystems.curios.wallet.rs_bind_success", pos.getX() + " " + pos.getY() + " " + pos.getZ());
        Network.sync(player);
    }

    private static void onRightClickItem(KineticPlayerEvents.RightClickItemContext event) {
        if (!CuriosConfig.enableCurrencyWallet) return;
        if (!(event.player() instanceof ServerPlayer player)) return;
        if (player.isShiftKeyDown()) return;
        ItemStack stack = event.stack();
        if (!Data.isWalletStack(stack)) return;
        event.cancel();
        toggleMagnet(player, stack);
    }

    private static void toggleMagnet(ServerPlayer player, ItemStack walletStack) {
        boolean disabled = Data.toggleMagnetDisabled(walletStack);
        Network.toast(player, disabled ? "msg.adventuresystems.curios.wallet.magnet_off" : "msg.adventuresystems.curios.wallet.magnet_on");
        Network.sync(player);
    }

    private static void onPickup(KineticWorldEvents.ItemPickupContext event) {
        if (!CuriosConfig.enableCurrencyWallet) return;
        if (!(event.player() instanceof ServerPlayer serverPlayer)) return;
        Optional<ItemStack> wallet = Data.equippedWallet(serverPlayer);
        if (wallet.isEmpty()) return;
        ItemStack walletStack = wallet.get();
        if (Data.isMagnetDisabled(walletStack)) return;
        ItemEntity itemEntity = event.item();
        if (!Data.isCurrencyItem(itemEntity.getItem())) return;
        if (Data.depositStack(serverPlayer, walletStack, itemEntity.getItem())) {
            event.cancel();
            itemEntity.discard();
            serverPlayer.playNotifySound(SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f, 1.6f);
            Network.sync(serverPlayer);
        }
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
