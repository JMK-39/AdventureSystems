package dev.xyat.adventuresystems.curios.paradiselost.client.tooltip;

import dev.xyat.adventuresystems.curios.util.ColorText;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ParadiseLostTooltip {

    public static void addTooltip(ItemStack stack, List<Component> tooltip) {
        CompoundTag nbt = stack.getTag();
        int score = nbt != null ? nbt.getInt("pl_score") : 0;

        tooltip.add(ColorText.translatable("tip.adventuresystems.curios.paradise_lost.title"));
        tooltip.add(ColorText.translatable("tip.adventuresystems.curios.paradise_lost.desc"));

        double bonus = getParadiseBonus(score) * 100.0;

        int nextTarget = 30000;
        int[] stageScores = {1000, 3000, 7000, 15000, 30000};
        for (int s : stageScores) {
            if (s > score) {
                nextTarget = s;
                break;
            }
        }

        double nextBonus = getParadiseBonus(nextTarget) * 100.0;

        tooltip.add(ColorText.translatable("tip.adventuresystems.curios.paradise_lost.score",
                "§b" + score,
                "§6" + nextTarget));

        if (bonus >= 0) {
            tooltip.add(ColorText.translatable("tip.adventuresystems.curios.paradise_lost.damage_bonus_pos",
                    "§a" + String.format("%.2f", bonus)));
        } else {
            tooltip.add(ColorText.translatable("tip.adventuresystems.curios.paradise_lost.damage_bonus_neg",
                    "§c" + String.format("%.2f", Math.abs(bonus))));
        }

        if (score >= 30000) {
            // 满级时调用专用的满级翻译键
            tooltip.add(ColorText.translatable("tip.adventuresystems.curios.paradise_lost.max_stage"));
        } else {
            // 未满级时调用原来的下一阶段翻译键
            tooltip.add(ColorText.translatable("tip.adventuresystems.curios.paradise_lost.next_stage",
                    "§a" + String.format("%.2f", nextBonus)));
        }
    }

    private static double getParadiseBonus(int score) {
        if (score <= 0) return 0.0;
        int[] scores = {0, 1000, 3000, 7000, 15000, 30000};
        double[] bonuses = {0.0, 0.60, 1.20, 1.60, 1.90, 2.00};

        if (score >= scores[scores.length - 1]) return bonuses[bonuses.length - 1];

        for (int i = 0; i < scores.length - 1; i++) {
            if (score >= scores[i] && score < scores[i + 1]) {
                double progress = (double) (score - scores[i]) / (scores[i + 1] - scores[i]);
                double smoothProgress = Math.sin(progress * Math.PI / 2.0);
                return bonuses[i] + (bonuses[i + 1] - bonuses[i]) * smoothProgress;
            }
        }
        return 0.0;
    }
}
