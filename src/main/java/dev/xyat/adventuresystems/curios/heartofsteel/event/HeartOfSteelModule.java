package dev.xyat.adventuresystems.curios.heartofsteel.event;

import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.heartofsteel.init.SoundEvents;
import dev.xyat.adventuresystems.curios.init.Items;
import dev.xyat.kineticcore.api.entity.event.KineticLivingEvents;
import dev.xyat.kineticcore.api.event.KineticEventPriority;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

public final class HeartOfSteelModule {
    private static final UUID HOS_HEALTH_UUID = UUID.fromString("a7a3c7a7-1c2c-4b3c-9a3e-1b4e0a9b8c7d");
    private static final String STACKS_KEY = "adventuresystems_hos_stacks";
    private static final String OWNER_KEY = "adventuresystems_owner_id";
    private static final String NEXT_CHARGE_KEY = "adventuresystems_hos_next_charge";
    private static final String BURST_END_KEY = "adventuresystems_hos_burst_end_time";

    private HeartOfSteelModule() {
    }

    public static void install() {
        KineticLivingEvents.onTick(KineticEventPriority.NORMAL, HeartOfSteelModule::onLivingTick);
        KineticLivingEvents.onDamage(KineticEventPriority.NORMAL, HeartOfSteelModule::onDamage);
        KineticLivingEvents.onHeal(KineticEventPriority.NORMAL, HeartOfSteelModule::onHeal);
    }

    private static void onLivingTick(LivingEntity entity) {
        if (!CuriosConfig.enableHeartOfSteel) return;
        if (!(entity instanceof Player player)) return;
        if (player.tickCount % 20 == 1) {
            updatePlayerHealthAttribute(player);
        }
    }

    private static void updatePlayerHealthAttribute(Player player) {
        var maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr == null) return;

        maxHealthAttr.removeModifier(HOS_HEALTH_UUID);

        CuriosApi.getCuriosHelper().findFirstCurio(player, Items.HEART_OF_STEEL.get()).ifPresent(slot -> {
            ItemStack stack = slot.stack();
            CompoundTag nbt = stack.getTag();

            if (nbt == null || !nbt.hasUUID(OWNER_KEY) || !player.getUUID().equals(nbt.getUUID(OWNER_KEY))) return;

            int stacks = nbt.getInt(STACKS_KEY);
            double bonusFromStacks = Math.min((double) stacks / CuriosConfig.hosStacksPerHp, CuriosConfig.hosMaxHealthCap);
            double totalBonus = CuriosConfig.hosBaseHealth + bonusFromStacks;

            maxHealthAttr.addPermanentModifier(new AttributeModifier(
                    HOS_HEALTH_UUID, "CuriosModule_HOS_Health", totalBonus, AttributeModifier.Operation.ADDITION
            ));
        });
    }

    private static void onDamage(KineticLivingEvents.DamageContext event) {
        if (!CuriosConfig.enableHeartOfSteel) return;
        if (!(event.source().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        CuriosApi.getCuriosHelper().findFirstCurio(player, Items.HEART_OF_STEEL.get()).ifPresent(slot -> {
            ItemStack hosStack = slot.stack();
            CompoundTag nbt = hosStack.getOrCreateTag();
            if (!nbt.hasUUID(OWNER_KEY) || !player.getUUID().equals(nbt.getUUID(OWNER_KEY))) return;

            long currentTime = player.level().getGameTime();
            long nextChargeTime = nbt.getLong(NEXT_CHARGE_KEY);
            CompoundTag pData = player.getPersistentData();

            double maxHp = player.getMaxHealth();
            double calculatedDmgBonus = maxHp * CuriosConfig.hosDamagePerHp;
            double finalBonus = Math.min(calculatedDmgBonus, CuriosConfig.hosDamageCap);

            if (currentTime >= nextChargeTime) {
                int min = CuriosConfig.hosMinGain;
                int max = CuriosConfig.hosMaxGain;
                int gain = min + player.getRandom().nextInt(Math.max(1, max - min + 1));

                nbt.putInt(STACKS_KEY, nbt.getInt(STACKS_KEY) + gain);
                int cooldownTicks = CuriosConfig.hosGrowthInterval * 20;
                nbt.putLong(NEXT_CHARGE_KEY, currentTime + cooldownTicks);
                pData.putLong(BURST_END_KEY, currentTime + CuriosConfig.hosDamageWindowTicks);

                player.getCooldowns().addCooldown(Items.HEART_OF_STEEL.get(), cooldownTicks);
                updatePlayerHealthAttribute(player);
                player.playNotifySound(SoundEvents.HEART_OF_STEEL.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            }

            if (currentTime <= pData.getLong(BURST_END_KEY) && finalBonus > 0) {
                event.amount((float) (event.amount() * (1.0 + finalBonus)));
            }
        });
    }

    private static void onHeal(KineticLivingEvents.HealContext event) {
        if (!CuriosConfig.enableHeartOfSteel) return;
        if (!(event.entity() instanceof Player player)) return;

        CuriosApi.getCuriosHelper().findFirstCurio(player, Items.HEART_OF_STEEL.get()).ifPresent(slot -> {
            CompoundTag nbt = slot.stack().getTag();
            if (nbt == null || !nbt.hasUUID(OWNER_KEY) || !player.getUUID().equals(nbt.getUUID(OWNER_KEY))) return;

            float missingPercent = Math.max(0, (player.getMaxHealth() - player.getHealth()) / player.getMaxHealth());
            float bonusMultiplier = missingPercent * (float) CuriosConfig.hosHealMultiplier;
            if (bonusMultiplier > CuriosConfig.hosHealActivationThreshold) {
                event.amount(event.amount() * (1.0f + bonusMultiplier));
            }
        });
    }
}
