package dev.xyat.adventuresystems.curios.wallet.item;

import dev.xyat.adventuresystems.text.AdventureText;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import dev.xyat.adventuresystems.curios.wallet.client.Client;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.wallet.network.Network;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.kineticcore.api.runtime.KineticPlatform;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
//? if <26.1
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;

public class WalletItem extends Item implements ICurioItem {
    private static final String ITEM_NAME_KEY = "item.adventuresystems.currency_wallet";

    public WalletItem() {
        super(dev.xyat.adventuresystems.curios.init.ItemProperties.of("currency_wallet").stacksTo(1).rarity(Rarity.RARE).fireResistant());
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return AdventureText.translatable(ITEM_NAME_KEY);
    }

//? if >=1.21 {
        /*    @Override
    public @NotNull Multimap<net.minecraft.core.Holder<Attribute>, AttributeModifier> getAttributeModifiers(@NotNull SlotContext slotContext, @NotNull net.minecraft.resources.ResourceLocation id, @NotNull ItemStack stack) {
        Multimap<net.minecraft.core.Holder<Attribute>, AttributeModifier> modifiers = LinkedHashMultimap.create();
        if (CuriosConfig.enableCurrencyWallet && CuriosConfig.walletExtraBeltSlots > 0) {
            CuriosApi.addSlotModifier(modifiers, "belt", id, CuriosConfig.walletExtraBeltSlots, AttributeModifier.Operation.ADD_VALUE);
        }
        return modifiers;
    }
        *///?} else {
    @Override
    public @NotNull Multimap<Attribute, AttributeModifier> getAttributeModifiers(@NotNull SlotContext slotContext, @NotNull UUID uuid, @NotNull ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();
        if (CuriosConfig.enableCurrencyWallet && CuriosConfig.walletExtraBeltSlots > 0) {
            CuriosApi.addSlotModifier(modifiers, "belt", uuid, CuriosConfig.walletExtraBeltSlots, AttributeModifier.Operation.ADDITION);
        }
        return modifiers;
    }

        //?}
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!CuriosConfig.enableCurrencyWallet) return InteractionResultHolder.pass(stack);
        if (player.isShiftKeyDown()) return InteractionResultHolder.pass(stack);

        Data.ensureWalletIdentity(stack);
        boolean disabled = Data.toggleMagnetDisabled(stack);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            Network.toast(serverPlayer, disabled ? "msg.adventuresystems.curios.wallet.magnet_disabled" : "msg.adventuresystems.curios.wallet.magnet_enabled");
            Network.sync(serverPlayer);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    // 26.1 feeds tooltip lines to a consumer; the lines are collected first and handed over at the end.
    //? if >=26.1 {
    /*public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context,
                                @NotNull net.minecraft.world.item.component.TooltipDisplay display,
                                @NotNull java.util.function.Consumer<Component> lines, @NotNull TooltipFlag flag) {
        Level level = KineticPlatform.callOnClient(() -> dev.xyat.kineticcore.api.runtime.KineticClientRuntime::currentLevel, null);
        List<Component> tooltip = new java.util.ArrayList<>();
    *///?} else if >=1.21 {
        /*public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        Level level = KineticPlatform.callOnClient(() -> dev.xyat.kineticcore.api.runtime.KineticClientRuntime::currentLevel, null);
        *///?} else {
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        //?}
        tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.wallet.title"));
        tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.wallet.desc1", openKeyName(level)));
        tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.wallet.desc2"));
        tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.wallet.desc3", Long.toString(Math.round(CuriosConfig.walletMagnetRange))));
        appendClientAmounts(stack, level, tooltip);
        //? if >=26.1
        /*tooltip.forEach(lines);*/
    }

    private String openKeyName(@Nullable Level level) {
        if (level == null || !level.isClientSide()) return "U";
        String name = KineticPlatform.callOnClient(() -> Client::openKeyName, "U");
        if (name == null || name.isEmpty()) return "U";
        return name;
    }

    private void appendClientAmounts(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip) {
        if (level == null || !level.isClientSide()) return;
        KineticPlatform.runOnClient(() -> () -> Client.appendWalletTooltip(stack, tooltip));
    }
}

