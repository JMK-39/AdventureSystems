package dev.xyat.adventuresystems.curios.common.client;

import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.adventuresystems.curios.common.client.tooltip.TooltipHelper;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.heartofsteel.client.tooltip.HeartOfSteelTooltip;
import dev.xyat.adventuresystems.curios.init.Items;
import dev.xyat.adventuresystems.curios.paradiselost.client.tooltip.ParadiseLostTooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = CuriosModule.MODID, value = Dist.CLIENT)
public class TooltipEventHandler {

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        List<Component> tooltip = event.getToolTip();

        if (CuriosConfig.enableHeartOfSteel && stack.is(Items.HEART_OF_STEEL.get())) {
            TooltipHelper.addBindingTooltip(stack, tooltip);
            HeartOfSteelTooltip.addBasicStatus(stack, tooltip);
            HeartOfSteelTooltip.addTooltip(stack, tooltip);
            addConflictHintOrTitle(tooltip);
            return;
        }

        if (CuriosConfig.enableParadiseLost && stack.is(Items.PARADISE_LOST.get())) {
            TooltipHelper.addBindingTooltip(stack, tooltip);
            ParadiseLostTooltip.addTooltip(stack, tooltip);
            addConflictHintOrTitle(tooltip);
        }
    }

    private static void addConflictHintOrTitle(List<Component> tooltip) {
        if (Screen.hasAltDown()) {
            tooltip.add(Component.translatable("tip.adventuresystems.curios.global.conflicts_title"));
        } else {
            tooltip.add(Component.translatable("tip.adventuresystems.curios.global.hold_alt"));
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !Screen.hasAltDown()) return;

        if (CuriosConfig.enableHeartOfSteel && stack.is(Items.HEART_OF_STEEL.get())) {
            TooltipHelper.appendConflictIcons(event, CuriosConfig.hosConflicts);
        } else if (CuriosConfig.enableParadiseLost && stack.is(Items.PARADISE_LOST.get())) {
            TooltipHelper.appendConflictIcons(event, CuriosConfig.plConflicts);
        }
    }

    @Mod.EventBusSubscriber(modid = CuriosModule.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerTooltipComponent(RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(TooltipHelper.ConflictTooltipData.class, TooltipHelper.ClientConflictTooltip::new);
        }
    }
}
