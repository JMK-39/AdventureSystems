package dev.xyat.adventuresystems.curios.wallet.data;

import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class StackCodec {
    private static final String STACK64_MARK = "#stack64:";
    private static final String NBT64_MARK = "#nbt64:";

    private StackCodec() {
    }

    public static ItemStack fromConfigString(String text) {
        if (text == null || text.isBlank()) return ItemStack.EMPTY;
        String raw = text.trim();

        int stackIndex = raw.indexOf(STACK64_MARK);
        if (stackIndex >= 0) {
            String itemText = raw.substring(0, stackIndex).trim();
            ItemStack stack = decodeStack(raw.substring(stackIndex + STACK64_MARK.length()).trim());
            if (!stack.isEmpty()) {
                int count = countFromItemText(itemText);
                stack.setCount(safeCount(count, stack.getMaxStackSize()));
                return stack;
            }
            raw = itemText;
        }

        String itemText = raw;
        CompoundTag tag = null;
        int nbtIndex = raw.indexOf(NBT64_MARK);
        if (nbtIndex >= 0) {
            itemText = raw.substring(0, nbtIndex).trim();
            tag = decodeTag(raw.substring(nbtIndex + NBT64_MARK.length()).trim());
        }

        int count = countFromItemText(itemText);
        String id = idFromItemText(itemText);
        try {
            Item item = KineticRegistries.items().get(KineticResourceIds.parse(id));
            if (item == null || item == Items.AIR) return ItemStack.EMPTY;
            ItemStack stack = new ItemStack(item, safeCount(count, item.getMaxStackSize()));
            if (tag != null && !tag.isEmpty()) stack.setTag(tag.copy());
            return stack;
        } catch (Exception ignored) {
            return ItemStack.EMPTY;
        }
    }

    public static ItemStack fromConfigString(String text, int count) {
        ItemStack stack = fromConfigString(text);
        if (!stack.isEmpty()) stack.setCount(safeCount(count, stack.getMaxStackSize()));
        return stack;
    }

    public static String toConfigString(ItemStack stack, int count) {
        if (stack == null || stack.isEmpty()) return "";
        ResourceLocation id = KineticRegistries.items().id(stack.getItem());
        if (id == null) return "";
        int safeCount = safeCount(count, stack.getMaxStackSize());
        String base = id + "*" + safeCount;
        CompoundTag tag = stack.getTag();
        if (tag == null || tag.isEmpty()) return base;
        ItemStack copy = stack.copy();
        copy.setCount(safeCount);
        return base + STACK64_MARK + encodeStack(copy);
    }

    public static String withCount(String text, int count) {
        ItemStack stack = fromConfigString(text);
        if (stack.isEmpty()) return "";
        return toConfigString(stack, count);
    }

    private static String idFromItemText(String itemText) {
        if (itemText == null) return "";
        String text = itemText.trim();
        int star = text.lastIndexOf('*');
        if (star > 0) return text.substring(0, star).trim();
        return text;
    }

    private static int countFromItemText(String itemText) {
        if (itemText == null) return 1;
        String text = itemText.trim();
        int star = text.lastIndexOf('*');
        if (star <= 0 || star >= text.length() - 1) return 1;
        return parseCount(text.substring(star + 1));
    }


    private static String encodeStack(ItemStack stack) {
        CompoundTag saved = stack.save(new CompoundTag());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(saved.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static ItemStack decodeStack(String encoded) {
        if (encoded == null || encoded.isBlank()) return ItemStack.EMPTY;
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(encoded);
            CompoundTag tag = TagParser.parseTag(new String(bytes, StandardCharsets.UTF_8));
            return ItemStack.of(tag);
        } catch (Exception ignored) {
            return ItemStack.EMPTY;
        }
    }


    private static CompoundTag decodeTag(String encoded) {
        if (encoded == null || encoded.isBlank()) return null;
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(encoded);
            return TagParser.parseTag(new String(bytes, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static int safeCount(int count, int maxStackSize) {
        int max = Math.max(1, maxStackSize);
        return Math.max(1, Math.min(count, max));
    }

    private static int parseCount(String text) {
        try {
            return Math.max(1, Integer.parseInt(text.trim()));
        } catch (Exception ignored) {
            return 1;
        }
    }

}

