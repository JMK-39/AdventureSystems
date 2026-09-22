package dev.xyat.adventuresystems.curios.paradiselost.event;

import dev.xyat.kineticcore.api.text.KineticI18n;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.init.Items;
import dev.xyat.adventuresystems.curios.paradiselost.data.ParadiseLostSavedData;
import dev.xyat.adventuresystems.curios.paradiselost.data.ParadiseLostCurve;
import dev.xyat.kineticcore.api.client.overlay.KineticOverlays;
import dev.xyat.kineticcore.api.entity.event.KineticLivingEvents;
import dev.xyat.kineticcore.api.event.KineticEventPriority;
import dev.xyat.kineticcore.api.player.KineticPlayerMessages;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.server.event.KineticServerEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Objects;

public final class ParadiseLostModule {
    private static final String NBT_OWNER = "adventuresystems_owner_id";
    private static final String NBT_SCORE = "pl_score";

    private ParadiseLostModule() {
    }

    public static void install() {
        KineticLivingEvents.onUseItemFinish(KineticEventPriority.NORMAL, ParadiseLostModule::onEat);
        KineticLivingEvents.onDamage(KineticEventPriority.NORMAL, ParadiseLostModule::onDamage);
        KineticServerEvents.onPlayerTick(
                KineticEventPriority.NORMAL,
                KineticServerEvents.TickPhase.END,
                ParadiseLostModule::onPlayerTick
        );
    }

    private static void onEat(KineticLivingEvents.UseItemFinishContext event) {
        if (!CuriosConfig.enableParadiseLost) return;
        if (!(event.entity() instanceof Player player)) return;

        ItemStack foodStack = event.item();
        if (!foodStack.isEdible()) return;

        FoodProperties prop = foodStack.getItem().getFoodProperties(foodStack, player);
        int nutritionGain = prop != null ? prop.getNutrition() : 0;
        if (nutritionGain <= CuriosConfig.plMinNutrition) return;

        boolean isNewFood = false;
        int scoreGain = nutritionGain;
        int newScore = 0;
        double oldBonusPercent = 0;
        double newBonusPercent = 0;

        if (!player.level().isClientSide) {
            String foodId = Objects.requireNonNull(KineticRegistries.items().id(foodStack.getItem())).toString();
            ParadiseLostSavedData data = ParadiseLostSavedData.get(player.level());
            isNewFood = !data.hasEaten(player.getUUID(), foodId);
            if (isNewFood) {
                int oldScore = data.getScore(player.getUUID());
                scoreGain = (int) Math.round(nutritionGain * CuriosConfig.plScoreMultiplier);
                oldBonusPercent = ParadiseLostCurve.bonus(oldScore) * 100.0;
                data.addFood(player.getUUID(), foodId, scoreGain);
                newScore = data.getScore(player.getUUID());
                newBonusPercent = ParadiseLostCurve.bonus(newScore) * 100.0;
            }
        }

        final boolean finalIsNewFood = isNewFood;
        final int finalScoreGain = scoreGain;
        final int finalNewScore = newScore;
        final double finalOldBonusPercent = oldBonusPercent;
        final double finalNewBonusPercent = newBonusPercent;

        CuriosApi.getCuriosHelper().findFirstCurio(player, Items.PARADISE_LOST.get()).ifPresent(slot -> {
            ItemStack ring = slot.stack();
            CompoundTag nbt = ring.getOrCreateTag();

            if (!nbt.hasUUID(NBT_OWNER) && !player.level().isClientSide) {
                nbt.putUUID(NBT_OWNER, player.getUUID());
            }

            if (!player.getUUID().equals(nbt.getUUID(NBT_OWNER))) {
                if (player.level().isClientSide) {
                    KineticOverlays.toast(KineticI18n.translatable("msg.adventuresystems.curios.not_owner"));
                }
                return;
            }

            if (!player.level().isClientSide && finalIsNewFood) {
                nbt.putInt(NBT_SCORE, finalNewScore);
                if (player instanceof ServerPlayer serverPlayer) {
                    KineticPlayerMessages.display(
                            serverPlayer,
                            KineticI18n.translatable(
                                    "msg.adventuresystems.curios.paradise_lost.boost",
                                    finalScoreGain,
                                    String.format("%.2f", finalOldBonusPercent),
                                    String.format("%.2f", finalNewBonusPercent)
                            ),
                            true
                    );
                }
            }
        });
    }

    private static void onDamage(KineticLivingEvents.DamageContext event) {
        if (!CuriosConfig.enableParadiseLost) return;
        if (!(event.source().getEntity() instanceof Player player)) return;

        CuriosApi.getCuriosHelper().findFirstCurio(player, Items.PARADISE_LOST.get()).ifPresent(slot -> {
            CompoundTag nbt = slot.stack().getTag();
            if (nbt != null && nbt.hasUUID(NBT_OWNER) && player.getUUID().equals(nbt.getUUID(NBT_OWNER))) {
                int score = nbt.getInt(NBT_SCORE);
                float multiplier = (float) (1.0 + ParadiseLostCurve.bonus(score));
                if (multiplier > 0) {
                    event.amount(event.amount() * multiplier);
                }
            }
        });
    }

    private static void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;
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
