package dev.xyat.adventuresystems.curios.common.client;

import dev.xyat.kineticcore.api.text.KineticI18n;
import dev.xyat.adventuresystems.curios.common.client.tooltip.TooltipHelper;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.heartofsteel.client.tooltip.HeartOfSteelTooltip;
import dev.xyat.adventuresystems.curios.init.Items;
import dev.xyat.adventuresystems.curios.paradiselost.client.tooltip.ParadiseLostTooltip;
import dev.xyat.kineticcore.api.client.tooltip.KineticItemTooltips;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class TooltipEventHandler {
    private TooltipEventHandler() {
    }

    public static void install() {
        KineticItemTooltips.onBuild(TooltipEventHandler::onTooltip);
        KineticItemTooltips.onGather(TooltipEventHandler::onGatherTooltipComponents);
        KineticItemTooltips.registerComponentFactory(
                TooltipHelper.ConflictTooltipData.class,
                TooltipHelper.ClientConflictTooltip::new
        );
    }

    private static void onTooltip(ItemStack stack, List<Component> tooltip) {
        if (stack.isEmpty()) return;

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
        tooltip.add(KineticI18n.translatable(KineticClientRuntime.altModifierDown()
                ? "tip.adventuresystems.curios.global.conflicts_title"
                : "tip.adventuresystems.curios.global.hold_alt"));
    }

    private static void onGatherTooltipComponents(KineticItemTooltips.GatherContext event) {
        ItemStack stack = event.stack();
        if (stack.isEmpty() || !KineticClientRuntime.altModifierDown()) return;

        if (CuriosConfig.enableHeartOfSteel && stack.is(Items.HEART_OF_STEEL.get())) {
            TooltipHelper.appendConflictIcons(event, CuriosConfig.hosConflicts);
        } else if (CuriosConfig.enableParadiseLost && stack.is(Items.PARADISE_LOST.get())) {
            TooltipHelper.appendConflictIcons(event, CuriosConfig.plConflicts);
        }
    }
}
