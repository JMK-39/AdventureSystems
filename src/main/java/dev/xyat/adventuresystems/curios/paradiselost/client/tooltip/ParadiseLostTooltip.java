//? if >=1.21 {
/*package dev.xyat.adventuresystems.curios.paradiselost.client.tooltip;

import dev.xyat.adventuresystems.text.AdventureText;
import dev.xyat.adventuresystems.curios.common.DisplayValues;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ParadiseLostTooltip {

    public static void addTooltip(ItemStack stack, List<Component> tooltip) {
        CompoundTag nbt = dev.xyat.adventuresystems.data.AdventureItemData.customData(stack);
        int score = nbt != null ? nbt.getInt("pl_score") : 0;

        tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.paradise_lost.title"));
        tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.paradise_lost.desc"));

        double bonus = DisplayValues.number(stack, "bonus") * 100.0;
        int nextTarget = DisplayValues.integer(stack, "next_target");
        double nextBonus = DisplayValues.number(stack, "next_bonus") * 100.0;

        tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.paradise_lost.score",
                Integer.toString(score),
                Integer.toString(nextTarget)));

        if (bonus >= 0) {
            tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.paradise_lost.damage_bonus_pos",
                    String.format("%.2f", bonus)));
        } else {
            tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.paradise_lost.damage_bonus_neg",
                    String.format("%.2f", Math.abs(bonus))));
        }

        if (DisplayValues.flag(stack, "maxed")) {
            tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.paradise_lost.max_stage"));
        } else {
            tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.paradise_lost.next_stage",
                    String.format("%.2f", nextBonus)));
        }
    }

}

*///?} else {
package dev.xyat.adventuresystems.curios.paradiselost.client.tooltip;

import dev.xyat.adventuresystems.text.AdventureText;
import dev.xyat.adventuresystems.curios.common.DisplayValues;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ParadiseLostTooltip {

    public static void addTooltip(ItemStack stack, List<Component> tooltip) {
        CompoundTag nbt = stack.getTag();
        int score = nbt != null ? nbt.getInt("pl_score") : 0;

        tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.paradise_lost.title"));
        tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.paradise_lost.desc"));

        double bonus = DisplayValues.number(stack, "bonus") * 100.0;
        int nextTarget = DisplayValues.integer(stack, "next_target");
        double nextBonus = DisplayValues.number(stack, "next_bonus") * 100.0;

        tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.paradise_lost.score",
                Integer.toString(score),
                Integer.toString(nextTarget)));

        if (bonus >= 0) {
            tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.paradise_lost.damage_bonus_pos",
                    String.format("%.2f", bonus)));
        } else {
            tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.paradise_lost.damage_bonus_neg",
                    String.format("%.2f", Math.abs(bonus))));
        }

        if (DisplayValues.flag(stack, "maxed")) {
            tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.paradise_lost.max_stage"));
        } else {
            tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.paradise_lost.next_stage",
                    String.format("%.2f", nextBonus)));
        }
    }

}

//?}
