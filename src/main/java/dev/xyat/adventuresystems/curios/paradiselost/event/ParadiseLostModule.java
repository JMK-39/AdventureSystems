package dev.xyat.adventuresystems.curios.paradiselost.event;

import dev.xyat.adventuresystems.curios.util.ColorText;
import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.paradiselost.data.ParadiseLostSavedData;
import dev.xyat.adventuresystems.curios.init.Items;
import dev.xyat.kineticcore.api.client.overlay.GuiOverlay;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = CuriosModule.MODID)
public class ParadiseLostModule {

    private static final String NBT_OWNER = "adventuresystems_owner_id";
    private static final String NBT_SCORE = "pl_score";

    private static double getDamageBonus(int score) {
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

    @SubscribeEvent
    public static void onEat(LivingEntityUseItemEvent.Finish event) {
        if (!CuriosConfig.enableParadiseLost) return;
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack foodStack = event.getItem();
        if (!foodStack.isEdible()) return;

        FoodProperties prop = foodStack.getItem().getFoodProperties(foodStack, player);
        int nutritionGain = (prop != null) ? prop.getNutrition() : 0;

        if (nutritionGain <= CuriosConfig.plMinNutrition) return;

        boolean isNewFood = false;
        int newScore = 0;
        double oldBonusPercent = 0;
        double newBonusPercent = 0;

        if (!player.level().isClientSide) {
            String foodId = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(foodStack.getItem())).toString();
            ParadiseLostSavedData data = ParadiseLostSavedData.get(player.level());

            isNewFood = !data.hasEaten(player.getUUID(), foodId);

            if (isNewFood) {
                int oldScore = data.getScore(player.getUUID());
                oldBonusPercent = getDamageBonus(oldScore) * 100.0;

                data.addFood(player.getUUID(), foodId, nutritionGain);

                newScore = data.getScore(player.getUUID());
                newBonusPercent = getDamageBonus(newScore) * 100.0;
            }
        }

        final boolean finalIsNewFood = isNewFood;
        final int finalNewScore = newScore;
        final double finalOldBonusPercent = oldBonusPercent;
        final double finalNewBonusPercent = newBonusPercent;

        CuriosApi.getCuriosHelper().findFirstCurio(player, Items.PARADISE_LOST.get()).ifPresent(slot -> {
            ItemStack ring = slot.stack();
            CompoundTag nbt = ring.getOrCreateTag();

            if (!nbt.hasUUID(NBT_OWNER)) {
                if (!player.level().isClientSide) {
                    nbt.putUUID(NBT_OWNER, player.getUUID());
                }
            }

            if (!player.getUUID().equals(nbt.getUUID(NBT_OWNER))) {
                if (player.level().isClientSide) {
                    GuiOverlay.toast(ColorText.translatable("msg.adventuresystems.curios.not_owner"));
                }
                return;
            }

            if (!player.level().isClientSide && finalIsNewFood) {
                nbt.putInt(NBT_SCORE, finalNewScore);
                player.displayClientMessage(ColorText.translatable("msg.adventuresystems.curios.paradise_lost.boost",
                        nutritionGain, String.format("%.2f", finalOldBonusPercent), String.format("%.2f", finalNewBonusPercent)), true);
            }
        });
    }

    @SubscribeEvent
    public static void onAttack(LivingDamageEvent event) {
        if (!CuriosConfig.enableParadiseLost) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        CuriosApi.getCuriosHelper().findFirstCurio(player, Items.PARADISE_LOST.get()).ifPresent(slot -> {
            ItemStack ring = slot.stack();
            CompoundTag nbt = ring.getTag();

            if (nbt != null && nbt.hasUUID(NBT_OWNER) && player.getUUID().equals(nbt.getUUID(NBT_OWNER))) {
                int score = nbt.getInt(NBT_SCORE);
                float multiplier = (float) (1.0 + getDamageBonus(score));
                if (multiplier > 0) {
                    event.setAmount(event.getAmount() * multiplier);
                }
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            Player player = event.player;
            if (player.tickCount % 20 == 0) {
                CuriosApi.getCuriosHelper().findFirstCurio(player, Items.PARADISE_LOST.get()).ifPresent(slot -> {
                    ItemStack ring = slot.stack();
                    CompoundTag nbt = ring.getOrCreateTag();

                    if (!nbt.hasUUID(NBT_OWNER)) {
                        nbt.putUUID(NBT_OWNER, player.getUUID());
                    }

                    if (player.getUUID().equals(nbt.getUUID(NBT_OWNER))) {
                        ParadiseLostSavedData data = ParadiseLostSavedData.get(player.level());
                        int savedScore = data.getScore(player.getUUID());
                        if (nbt.getInt(NBT_SCORE) != savedScore) {
                            nbt.putInt(NBT_SCORE, savedScore);
                        }
                    }
                });
            }
        }
    }
}
