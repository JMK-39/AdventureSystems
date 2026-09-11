package dev.xyat.adventuresystems.curios.wallet.event;

import dev.xyat.adventuresystems.curios.mixin.MerchantMenuAccessor;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.adventuresystems.curios.wallet.network.Network;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public final class MerchantRefill {
    private static final int FIRST_PAYMENT_SLOT = 0;
    private static final int SECOND_PAYMENT_SLOT = 1;
    private static final String MANUAL_EXCHANGE_TOAST_COOLDOWN_KEY = "adventuresystems_currency_wallet_merchant_manual_exchange_toast_cooldown";
    private static final int MANUAL_EXCHANGE_TOAST_COOLDOWN_TICKS = 60;

    private MerchantRefill() {
    }

    public static void refill(MerchantMenu menu, int selectionHint) {
        if (!(menu instanceof MerchantMenuAccessor accessor)) return;
        Merchant merchant = accessor.adventuresystems_curios$getTrader();
        if (merchant == null) return;
        Player tradingPlayer = merchant.getTradingPlayer();
        if (!(tradingPlayer instanceof ServerPlayer player)) return;
        if (!Data.hasWallet(player)) return;

        MerchantOffers offers = menu.getOffers();
        if (selectionHint < 0 || selectionHint >= offers.size()) return;
        MerchantOffer offer = offers.get(selectionHint);
        if (offer == null || offer.isOutOfStock()) return;

        boolean changed = refillPaymentSlot(player, menu, FIRST_PAYMENT_SLOT, offer.getCostA());
        changed |= refillPaymentSlot(player, menu, SECOND_PAYMENT_SLOT, offer.getCostB());

        if (changed) {
            menu.broadcastChanges();
            Network.sync(player);
        }
    }

    public static void storePaymentSlotsToWallet(MerchantMenu menu, Player player) {
        if (menu == null) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (!Data.hasWallet(serverPlayer)) return;

        boolean changed = storePaymentSlotToWallet(serverPlayer, menu, FIRST_PAYMENT_SLOT);
        changed |= storePaymentSlotToWallet(serverPlayer, menu, SECOND_PAYMENT_SLOT);

        if (changed) {
            menu.broadcastChanges();
            Network.sync(serverPlayer);
        }
    }

    private static boolean refillPaymentSlot(ServerPlayer player, MerchantMenu menu, int slotIndex, ItemStack cost) {
        if (cost == null || cost.isEmpty()) return false;
        if (!Data.isCurrencyItem(cost)) return false;
        if (slotIndex < 0 || slotIndex >= menu.slots.size()) return false;

        Slot slot = menu.getSlot(slotIndex);
        ItemStack current = slot.getItem();
        if (!current.isEmpty() && !ItemStack.isSameItemSameTags(current, cost)) return false;

        int maxStack = Math.min(cost.getMaxStackSize(), cost.getItem().getDefaultInstance().getMaxStackSize());
        int currentCount = current.isEmpty() ? 0 : current.getCount();
        int missing = maxStack - currentCount;
        if (missing <= 0) return false;

        String currencyId = Data.currencyId(cost);
        if (currencyId.isEmpty()) return false;

        long available = Data.countDirectForAutomaticPayment(player, currencyId);
        int wanted = (int) Math.min(missing, Math.min(maxStack, available));
        if (wanted <= 0) {
            notifyManualExchangeRequired(player, currencyId);
            return false;
        }

        long extracted = Data.extractDirectForAutomaticPayment(player, currencyId, wanted, false);
        if (extracted <= 0L) return false;

        int accepted = (int) Math.min(extracted, missing);
        if (current.isEmpty()) {
            ItemStack inserted = cost.copy();
            inserted.setCount(accepted);
            slot.set(inserted);
        } else {
            current.grow(accepted);
            slot.set(current);
        }
        slot.setChanged();

        long leftover = extracted - accepted;
        if (leftover > 0L) Data.add(player, currencyId, leftover);
        return true;
    }

    private static void notifyManualExchangeRequired(ServerPlayer player, String currencyId) {
        if (!Data.hasManualExchangeSourceFor(player, currencyId)) return;
        long now = player.level().getGameTime();
        CompoundTag data = player.getPersistentData();
        if (data.getLong(MANUAL_EXCHANGE_TOAST_COOLDOWN_KEY) > now) return;
        data.putLong(MANUAL_EXCHANGE_TOAST_COOLDOWN_KEY, now + MANUAL_EXCHANGE_TOAST_COOLDOWN_TICKS);
        Network.toast(player, "msg.adventuresystems.curios.wallet.merchant_manual_exchange_required");
    }

    private static boolean storePaymentSlotToWallet(ServerPlayer player, MerchantMenu menu, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= menu.slots.size()) return false;
        Slot slot = menu.getSlot(slotIndex);
        ItemStack stack = slot.getItem();
        if (stack.isEmpty()) return false;
        if (!Data.isCurrencyItem(stack)) return false;

        String currencyId = Data.currencyId(stack);
        if (currencyId.isEmpty()) return false;

        int count = stack.getCount();
        long accepted = Data.add(player, currencyId, count);
        if (accepted <= 0L) return false;

        stack.shrink((int) accepted);
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.set(stack);
        slot.setChanged();
        return true;
    }
}

