package dev.xyat.adventuresystems.curios.wallet.event;

import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.wallet.compat.rs.RefinedStorageCompat;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.adventuresystems.curios.wallet.network.Network;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CuriosModule.MODID)
public class WalletModule {
    private static final String HAD_WALLET_KEY = "adventuresystems_currency_wallet_had_wallet";

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (serverPlayer.level().isClientSide) return;

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

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!CuriosConfig.enableCurrencyWallet) return;
        Player player = event.player;
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (serverPlayer.level().isClientSide) return;

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


    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!CuriosConfig.enableCurrencyWallet) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        ItemStack stack = event.getItemStack();
        if (!Data.isWalletStack(stack)) return;

        if (!player.isShiftKeyDown()) {
            event.setCanceled(true);
            toggleMagnet(player, stack);
            return;
        }

        BlockPos pos = event.getPos();
        if (!ModList.get().isLoaded("refinedstorage")) return;
        if (!RefinedStorageCompat.isController(level, pos)) return;
        event.setCanceled(true);
        if (!RefinedStorageCompat.canBind(level, pos)) {
            Network.toast(player, "msg.adventuresystems.curios.wallet.rs_bind_fail");
            return;
        }
        Data.bindRsController(stack, level, pos);
        Network.toast(player, "msg.adventuresystems.curios.wallet.rs_bind_success", pos.getX() + " " + pos.getY() + " " + pos.getZ());
        Network.sync(player);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!CuriosConfig.enableCurrencyWallet) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.isShiftKeyDown()) return;
        ItemStack stack = event.getItemStack();
        if (!Data.isWalletStack(stack)) return;
        event.setCanceled(true);
        toggleMagnet(player, stack);
    }

    private static void toggleMagnet(ServerPlayer player, ItemStack walletStack) {
        boolean disabled = Data.toggleMagnetDisabled(walletStack);
        Network.toast(player, disabled ? "msg.adventuresystems.curios.wallet.magnet_off" : "msg.adventuresystems.curios.wallet.magnet_on");
        Network.sync(player);
    }

    @SubscribeEvent
    public static void onPickup(EntityItemPickupEvent event) {
        if (!CuriosConfig.enableCurrencyWallet) return;
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        Optional<ItemStack> wallet = Data.equippedWallet(serverPlayer);
        if (wallet.isEmpty()) return;
        ItemStack walletStack = wallet.get();
        if (Data.isMagnetDisabled(walletStack)) return;
        ItemEntity itemEntity = event.getItem();
        if (!Data.isCurrencyItem(itemEntity.getItem())) return;
        if (Data.depositStack(serverPlayer, walletStack, itemEntity.getItem())) {
            event.setCanceled(true);
            itemEntity.discard();
            serverPlayer.playNotifySound(SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f, 1.6f);
            Network.sync(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        Data.copy(event.getOriginal(), event.getEntity());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        event.getEntity().getPersistentData().putBoolean(HAD_WALLET_KEY, false);
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            Network.hide(serverPlayer);
        }
    }

    public static boolean hasWallet(Player player) {
        return Data.hasWallet(player);
    }
}

