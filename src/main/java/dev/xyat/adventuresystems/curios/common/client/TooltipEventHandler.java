package dev.xyat.adventuresystems.curios.common.client;

import dev.xyat.adventuresystems.text.AdventureText;
import dev.xyat.adventuresystems.curios.common.client.tooltip.TooltipHelper;
import dev.xyat.adventuresystems.curios.common.DisplayValues;
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

        boolean heartOfSteel = stack.is(Items.HEART_OF_STEEL.get());
        boolean paradiseLost = stack.is(Items.PARADISE_LOST.get());
        if (!heartOfSteel && !paradiseLost) return;
        // The numbers come from the server, written on the item; until then only a placeholder line is shown.
        if (!DisplayValues.known(stack)) {
            TooltipHelper.addBindingTooltip(stack, tooltip);
            tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.global.values_pending"));
            return;
        }
        if (!DisplayValues.flag(stack, "enabled")) return;

        if (heartOfSteel) {
            TooltipHelper.addBindingTooltip(stack, tooltip);
            HeartOfSteelTooltip.addBasicStatus(stack, tooltip);
            HeartOfSteelTooltip.addTooltip(stack, tooltip);
            addConflictHintOrTitle(tooltip);
            return;
        }

        if (paradiseLost) {
            TooltipHelper.addBindingTooltip(stack, tooltip);
            ParadiseLostTooltip.addTooltip(stack, tooltip);
            addConflictHintOrTitle(tooltip);
        }
    }

    private static void addConflictHintOrTitle(List<Component> tooltip) {
        tooltip.add(AdventureText.translatable(KineticClientRuntime.altModifierDown()
                ? "tip.adventuresystems.curios.global.conflicts_title"
                : "tip.adventuresystems.curios.global.hold_alt"));
    }

    private static void onGatherTooltipComponents(KineticItemTooltips.GatherContext event) {
        ItemStack stack = event.stack();
        if (stack.isEmpty() || !KineticClientRuntime.altModifierDown()) return;

        if ((stack.is(Items.HEART_OF_STEEL.get()) || stack.is(Items.PARADISE_LOST.get()))
                && DisplayValues.flag(stack, "enabled")) {
            TooltipHelper.appendConflictIcons(event, DisplayValues.conflicts(stack));
        }
    }
}
