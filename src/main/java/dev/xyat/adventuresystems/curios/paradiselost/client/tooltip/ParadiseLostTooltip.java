package dev.xyat.adventuresystems.curios.paradiselost.client.tooltip;

import dev.xyat.kineticcore.api.text.KineticI18n;
import dev.xyat.adventuresystems.curios.paradiselost.data.ParadiseLostCurve;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ParadiseLostTooltip {

    public static void addTooltip(ItemStack stack, List<Component> tooltip) {
        CompoundTag nbt = stack.getTag();
        int score = nbt != null ? nbt.getInt("pl_score") : 0;

        tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.paradise_lost.title"));
        tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.paradise_lost.desc"));

        double bonus = ParadiseLostCurve.bonus(score) * 100.0;
        int nextTarget = ParadiseLostCurve.nextTarget(score);
        double nextBonus = ParadiseLostCurve.bonus(nextTarget) * 100.0;

        tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.paradise_lost.score",
                Integer.toString(score),
                Integer.toString(nextTarget)));

        if (bonus >= 0) {
            tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.paradise_lost.damage_bonus_pos",
                    String.format("%.2f", bonus)));
        } else {
            tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.paradise_lost.damage_bonus_neg",
                    String.format("%.2f", Math.abs(bonus))));
        }

        if (ParadiseLostCurve.isMaxed(score)) {
            tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.paradise_lost.max_stage"));
        } else {
            tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.paradise_lost.next_stage",
                    String.format("%.2f", nextBonus)));
        }
    }

}
