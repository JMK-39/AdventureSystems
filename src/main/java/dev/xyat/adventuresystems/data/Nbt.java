package dev.xyat.adventuresystems.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * NBT access that reads the same on every Minecraft version. 26.1 getters return Optional and UUIDs go through a
 * codec; the stored data is the same. Missing values read as 0, false, "" or an empty compound.
 */
public final class Nbt {
    private Nbt() {
    }

    //? if >=26.1 {
    /*public static Set<String> keys(CompoundTag tag) { return tag.keySet(); }
    public static CompoundTag compound(CompoundTag tag, String key) { return tag.getCompoundOrEmpty(key); }
    public static int intValue(CompoundTag tag, String key) { return tag.getIntOr(key, 0); }
    public static long longValue(CompoundTag tag, String key) { return tag.getLongOr(key, 0L); }
    public static boolean bool(CompoundTag tag, String key) { return tag.getBooleanOr(key, false); }
    public static String string(CompoundTag tag, String key) { return tag.getStringOr(key, ""); }
    public static boolean hasInt(CompoundTag tag, String key) { return tag.get(key) instanceof net.minecraft.nbt.IntTag; }
    public static boolean hasByte(CompoundTag tag, String key) { return tag.get(key) instanceof net.minecraft.nbt.ByteTag; }
    public static boolean hasUuid(CompoundTag tag, String key) { return tag.read(key, net.minecraft.core.UUIDUtil.CODEC).isPresent(); }
    public static @Nullable UUID uuid(CompoundTag tag, String key) { return tag.read(key, net.minecraft.core.UUIDUtil.CODEC).orElse(null); }
    public static void putUuid(CompoundTag tag, String key, UUID value) { tag.store(key, net.minecraft.core.UUIDUtil.CODEC, value); }
    public static long asLong(NumericTag tag) { return tag.longValue(); }
    /^* A string tag's text, or the SNBT of any other tag. ^/
    public static String asString(Tag tag) { return tag.asString().orElse(tag.toString()); }
    *///?} else {
    public static Set<String> keys(CompoundTag tag) { return tag.getAllKeys(); }
    public static CompoundTag compound(CompoundTag tag, String key) { return tag.getCompound(key); }
    public static int intValue(CompoundTag tag, String key) { return tag.getInt(key); }
    public static long longValue(CompoundTag tag, String key) { return tag.getLong(key); }
    public static boolean bool(CompoundTag tag, String key) { return tag.getBoolean(key); }
    public static String string(CompoundTag tag, String key) { return tag.getString(key); }
    public static boolean hasInt(CompoundTag tag, String key) { return tag.contains(key, Tag.TAG_INT); }
    public static boolean hasByte(CompoundTag tag, String key) { return tag.contains(key, Tag.TAG_BYTE); }
    public static boolean hasUuid(CompoundTag tag, String key) { return tag.hasUUID(key); }
    public static @Nullable UUID uuid(CompoundTag tag, String key) { return tag.hasUUID(key) ? tag.getUUID(key) : null; }
    public static void putUuid(CompoundTag tag, String key, UUID value) { tag.putUUID(key, value); }
    public static long asLong(NumericTag tag) { return tag.getAsLong(); }
    /** A string tag's text, or the SNBT of any other tag. */
    public static String asString(Tag tag) { return tag.getAsString(); }
    //?}
}
