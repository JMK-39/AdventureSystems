package dev.xyat.adventuresystems.curios.heartofsteel.client.tooltip;

import dev.xyat.kineticcore.api.text.KineticI18n;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

public class HeartOfSteelTooltip {
    private static final String OWNER_KEY = "adventuresystems_owner_id";
    private static final String STACKS_KEY = "adventuresystems_hos_stacks";
    private static final String NEXT_CHARGE_KEY = "adventuresystems_hos_next_charge";

    public static void addBasicStatus(ItemStack stack, List<Component> tooltip) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null || !nbt.hasUUID(OWNER_KEY)) return;

        Player player = KineticClientRuntime.localPlayer();
        if (player == null) return;

        long currentTime = player.level().getGameTime();
        long nextChargeTime = nbt.getLong(NEXT_CHARGE_KEY);

        if (currentTime >= nextChargeTime) {
            tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.heart_of_steel.ready"));
        } else {
            long remainingSeconds = Math.max(0L, (nextChargeTime - currentTime + 19L) / 20L);
            tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.heart_of_steel.cooldown", remainingSeconds));
        }
    }

    public static void addTooltip(ItemStack stack, List<Component> tooltip) {
        Player player = KineticClientRuntime.localPlayer();
        Data data = data(stack, player);
        tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.heart_of_steel.scaling", "1", oneDecimal(CuriosConfig.hosDamagePerHp * 100.0D)));
        tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.heart_of_steel.stacks", data.stacksText(), data.stacksPerHpText()));
        tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.heart_of_steel.health_bonus", data.totalHealthText(), data.baseHealthText(), data.stackHealthText(), data.healthCapText()));
        Object efficiency = data.stackHealth() >= Math.max(0.0D, CuriosConfig.hosMaxHealthCap)
                ? KineticI18n.translatable("tip.adventuresystems.curios.heart_of_steel.max_reached")
                : CuriosConfig.hosMinGain + "~" + CuriosConfig.hosMaxGain;
        tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.heart_of_steel.health_growth_amount", efficiency));
        tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.heart_of_steel.current_damage_bonus", data.damagePercentText(), data.playerMaxHpText(), data.damageCapText()));
        tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.heart_of_steel.healing_bonus", oneDecimal(CuriosConfig.hosHealMultiplier)));
        tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.heart_of_steel.healing_desc"));
        tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.heart_of_steel.growth_interval", Integer.toString(CuriosConfig.hosGrowthInterval)));
    }

    private static Data data(ItemStack stack, Player player) {
        CompoundTag nbt = stack.getTag();
        int stacks = nbt == null ? 0 : Math.max(0, nbt.getInt(STACKS_KEY));
        int stacksPerHp = Math.max(1, CuriosConfig.hosStacksPerHp);
        double healthCap = Math.max(0.0D, CuriosConfig.hosMaxHealthCap);
        double stackHealth = Math.min((double) stacks / stacksPerHp, healthCap);
        double baseHealth = Math.max(0.0D, CuriosConfig.hosBaseHealth);
        double totalHealth = baseHealth + stackHealth;
        double playerMaxHp = player == null ? 20.0D : Math.max(1.0D, player.getMaxHealth());
        double damageCap = Math.max(0.0D, CuriosConfig.hosDamageCap);
        double damageBonus = Math.min(playerMaxHp * Math.max(0.0D, CuriosConfig.hosDamagePerHp), damageCap);
        return new Data(stacks, stacksPerHp, baseHealth, stackHealth, totalHealth, healthCap, playerMaxHp, damageBonus, damageCap);
    }

    private static String oneDecimal(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static String twoDecimal(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private record Data(int stacks, int stacksPerHp, double baseHealth, double stackHealth, double totalHealth, double healthCap, double playerMaxHp, double damageBonus, double damageCap) {
        String stacksText() {
            return Integer.toString(stacks);
        }

        String stacksPerHpText() {
            return Integer.toString(stacksPerHp);
        }

        String baseHealthText() {
            return oneDecimal(baseHealth);
        }

        String stackHealthText() {
            return twoDecimal(stackHealth);
        }

        String totalHealthText() {
            return "+" + twoDecimal(totalHealth);
        }

        String healthCapText() {
            return Integer.toString((int) healthCap);
        }

        String playerMaxHpText() {
            return oneDecimal(playerMaxHp);
        }

        String damagePercentText() {
            return "+" + oneDecimal(damageBonus * 100.0D);
        }

        String damageCapText() {
            return Integer.toString((int) (damageCap * 100.0D));
        }
    }
}
