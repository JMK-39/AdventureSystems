package dev.xyat.adventuresystems.curios.common;

import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.init.Items;
import dev.xyat.adventuresystems.curios.paradiselost.data.ParadiseLostCurve;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * The numbers an accessory shows in its tooltip. The server writes them onto the item itself, so a client never
 * reads its own config for them and nothing is sent to other players when an admin changes the settings: each
 * player sees the new numbers once their own items are refreshed. Without server values the tooltip shows a
 * placeholder.
 */
public final class DisplayValues {
    public static final String KEY = "adventuresystems_display";

    private DisplayValues() {
    }

    /** The values written on the item, or an empty tag when the server has not written any yet. */
    public static CompoundTag read(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return new CompoundTag();
        CompoundTag data = customData(stack);
        return data == null ? new CompoundTag() : data.getCompound(KEY);
    }

    // 1.20.5+ keeps custom item data in a component; 1.20.1 in the item's NBT tag.
    private static CompoundTag customData(ItemStack stack) {
        //? if >=1.21 {
        /*return dev.xyat.adventuresystems.data.AdventureItemData.customData(stack);
        *///?} else {
        return stack.getTag();
        //?}
    }

    /** Writes the values when they differ from the item's current ones; returns whether the item changed. */
    public static boolean stamp(ItemStack stack, CompoundTag values) {
        if (stack == null || stack.isEmpty() || values == null) return false;
        if (values.equals(read(stack))) return false;
        //? if >=1.21 {
        /*dev.xyat.adventuresystems.data.AdventureItemData.updateCustomData(stack, data -> data.put(KEY, values.copy()));
        *///?} else {
        stack.getOrCreateTag().put(KEY, values.copy());
        //?}
        return true;
    }

    /** Server side: the Heart of Steel settings its tooltip shows. */
    public static CompoundTag heartOfSteel() {
        CompoundTag values = new CompoundTag();
        values.putDouble("damage_per_hp", CuriosConfig.hosDamagePerHp);
        values.putDouble("damage_cap", CuriosConfig.hosDamageCap);
        values.putBoolean("enabled", CuriosConfig.enableHeartOfSteel);
        values.putInt("max_health_cap", CuriosConfig.hosMaxHealthCap);
        values.putDouble("base_health", CuriosConfig.hosBaseHealth);
        values.putInt("stacks_per_hp", CuriosConfig.hosStacksPerHp);
        values.putInt("min_gain", CuriosConfig.hosMinGain);
        values.putInt("max_gain", CuriosConfig.hosMaxGain);
        values.putDouble("heal_multiplier", CuriosConfig.hosHealMultiplier);
        values.putInt("growth_interval", CuriosConfig.hosGrowthInterval);
        values.put("conflicts", strings(CuriosConfig.hosConflicts));
        return values;
    }

    /** Server side: the Paradise Lost bonus at the item's current score and the next stage. */
    public static CompoundTag paradiseLost(ItemStack stack) {
        CompoundTag data = customData(stack);
        int score = data == null ? 0 : data.getInt("pl_score");
        int nextTarget = ParadiseLostCurve.nextTarget(score);
        CompoundTag values = new CompoundTag();
        values.putBoolean("enabled", CuriosConfig.enableParadiseLost);
        values.putInt("score", score);
        values.putDouble("bonus", ParadiseLostCurve.bonus(score));
        values.putInt("next_target", nextTarget);
        values.putDouble("next_bonus", ParadiseLostCurve.bonus(nextTarget));
        values.putBoolean("maxed", ParadiseLostCurve.isMaxed(score));
        values.put("conflicts", strings(CuriosConfig.plConflicts));
        return values;
    }

    /** Server side: refreshes the values on a player's accessories, in the inventory and in the curio slots. */
    public static void stampPlayer(net.minecraft.world.entity.player.Player player) {
        for (ItemStack stack : player.getInventory().items) stampOne(stack);
        dev.xyat.adventuresystems.curios.common.util.CuriosAccess.findFirst(player, Items.HEART_OF_STEEL.get())
                .ifPresent(result -> stampOne(result.stack()));
        dev.xyat.adventuresystems.curios.common.util.CuriosAccess.findFirst(player, Items.PARADISE_LOST.get())
                .ifPresent(result -> stampOne(result.stack()));
    }

    private static void stampOne(ItemStack stack) {
        if (stack.is(Items.HEART_OF_STEEL.get())) stamp(stack, heartOfSteel());
        else if (stack.is(Items.PARADISE_LOST.get())) stamp(stack, paradiseLost(stack));
    }

    /** Whether the item carries values written by the server. */
    public static boolean known(ItemStack stack) {
        return !read(stack).isEmpty();
    }

    /** A number written on the item; 0 when missing. */
    public static double number(ItemStack stack, String key) {
        CompoundTag tag = read(stack);
        return tag.getDouble(key);
    }

    /** A whole number written on the item; 0 when missing. */
    public static int integer(ItemStack stack, String key) {
        CompoundTag tag = read(stack);
        return tag.getInt(key);
    }

    /** A switch written on the item; false when missing. */
    public static boolean flag(ItemStack stack, String key) {
        CompoundTag tag = read(stack);
        return tag.getBoolean(key);
    }

    /** The conflicting item IDs written on the item. */
    public static List<String> conflicts(ItemStack stack) {
        CompoundTag tag = read(stack);
        List<String> result = new ArrayList<>();
        ListTag list = tag.getList("conflicts", Tag.TAG_STRING);
        for (int index = 0; index < list.size(); index++) result.add(list.getString(index));
        return result;
    }

    private static ListTag strings(List<String> values) {
        ListTag list = new ListTag();
        if (values != null) for (String value : values) list.add(StringTag.valueOf(value));
        return list;
    }
}
