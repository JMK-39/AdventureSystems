package dev.xyat.adventuresystems.curios.heartofsteel.event;

import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.init.Items;
import dev.xyat.adventuresystems.curios.heartofsteel.init.SoundEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = CuriosModule.MODID)
public class HeartOfSteelModule {
    private static final UUID HOS_HEALTH_UUID = UUID.fromString("a7a3c7a7-1c2c-4b3c-9a3e-1b4e0a9b8c7d");
    private static final String STACKS_KEY = "adventuresystems_hos_stacks";
    private static final String OWNER_KEY = "adventuresystems_owner_id";
    private static final String NEXT_CHARGE_KEY = "adventuresystems_hos_next_charge";
    private static final String BURST_END_KEY = "adventuresystems_hos_burst_end_time";

    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (!CuriosConfig.enableHeartOfSteel) return;
        if (!(event.getEntity() instanceof Player player)) return;

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

    @SubscribeEvent
    public static void onAttack(LivingDamageEvent event) {
        if (!CuriosConfig.enableHeartOfSteel) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        CuriosApi.getCuriosHelper().findFirstCurio(player, Items.HEART_OF_STEEL.get()).ifPresent(slot -> {
            ItemStack hosStack = slot.stack();
            CompoundTag nbt = hosStack.getOrCreateTag();

            // 权限检查
            if (!nbt.hasUUID(OWNER_KEY) || !player.getUUID().equals(nbt.getUUID(OWNER_KEY))) return;

            long currentTime = player.level().getGameTime();
            long nextChargeTime = nbt.getLong(NEXT_CHARGE_KEY);
            CompoundTag pData = player.getPersistentData();

            double maxHp = player.getMaxHealth();
            double calculatedDmgBonus = maxHp * CuriosConfig.hosDamagePerHp;
            double finalBonus = Math.min(calculatedDmgBonus, CuriosConfig.hosDamageCap);

            // --- 核心：充能完毕，本次攻击触发爆发 ---
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

                // --- 播放触发音效 ---
                // 使用 player.playSound 在服务端调用，确保只有玩家自己能听到（私密播放）
                player.playNotifySound(SoundEvents.HEART_OF_STEEL.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            }

            // --- 爆发伤害增幅 ---
            if (currentTime <= pData.getLong(BURST_END_KEY)) {
                if (finalBonus > 0) {
                    event.setAmount((float) (event.getAmount() * (1.0 + finalBonus)));
                }
            }
        });
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        if (!CuriosConfig.enableHeartOfSteel) return;
        if (!(event.getEntity() instanceof Player player)) return;

        CuriosApi.getCuriosHelper().findFirstCurio(player, Items.HEART_OF_STEEL.get()).ifPresent(slot -> {
            CompoundTag nbt = slot.stack().getTag();
            if (nbt == null || !nbt.hasUUID(OWNER_KEY) || !player.getUUID().equals(nbt.getUUID(OWNER_KEY))) return;

            float missingPercent = Math.max(0, (player.getMaxHealth() - player.getHealth()) / player.getMaxHealth());
            float bonusMultiplier = missingPercent * (float) CuriosConfig.hosHealMultiplier;

            if (bonusMultiplier > 0.01f) {
                event.setAmount(event.getAmount() * (1.0f + bonusMultiplier));
            }
        });
    }
}
