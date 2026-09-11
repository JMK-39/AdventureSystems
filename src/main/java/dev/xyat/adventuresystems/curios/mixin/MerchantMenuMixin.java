package dev.xyat.adventuresystems.curios.mixin;

import dev.xyat.adventuresystems.curios.wallet.event.MerchantRefill;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin {
    @Inject(method = "setSelectionHint", at = @At("TAIL"))
    private void adventuresystems_curios$refillWalletCurrencyOnSelectionChanged(int selectionHint, CallbackInfo ci) {
        MerchantRefill.refill((MerchantMenu) (Object) this, selectionHint);
    }

    @Inject(method = "clickMenuButton", at = @At("RETURN"))
    private void adventuresystems_curios$refillWalletCurrencyOnButton(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        MerchantRefill.refill((MerchantMenu) (Object) this, id);
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void adventuresystems_curios$storeWalletCurrencyBeforeReturn(Player player, CallbackInfo ci) {
        MerchantRefill.storePaymentSlotsToWallet((MerchantMenu) (Object) this, player);
    }
}
