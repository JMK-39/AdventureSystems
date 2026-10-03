package dev.xyat.adventuresystems.tips.api;

//? if >=1.21 {
/*import net.minecraft.core.component.DataComponentPatch;
*///?} else {
import net.minecraft.nbt.CompoundTag;
//?}
import net.minecraft.network.chat.Component;
import java.util.Collections;
import java.util.List;

/**
 * 提示系统的数据模型类
 * 包含运行时使用的 HelpTip 对象以及用于 JSON 解析的 JsonModel
 */
public class HelpTip {
    private final Component textComponent;
    public final int cycleTime;

    public int stage = 0; // 0: any, 1: loading, 2: game
    public String requiredBiome = "";
    public String requiredStructure = "";
    public String requiredDimension = "";
    public String requiredAdvancement = "";

    public List<ItemMatcher> requiredItems = Collections.emptyList();
    public List<ItemMatcher> requiredCurios = Collections.emptyList();

    public HelpTip(Component textComponent, int cycleTime) {
        this.textComponent = textComponent;
        this.cycleTime = cycleTime;
    }

    public Component getText() { return textComponent; }

    // --- 运行时辅助类 ---
    //? if >=1.21 {
    /*public enum ComponentMode {
        NONE, WEAK, STRONG
    }

    public record ItemMatcher(String itemId, DataComponentPatch components, ComponentMode mode, String deferredComponents) {
        public ItemMatcher(String itemId, DataComponentPatch components, ComponentMode mode) { this(itemId,components,mode,null); }
        public DataComponentPatch resolvedComponents() {
            if (deferredComponents == null) return components;
            try {
                var reader=new com.mojang.brigadier.StringReader(itemId + deferredComponents);
                var result=new net.minecraft.commands.arguments.item.ItemParser(dev.xyat.adventuresystems.data.AdventureItemData.registryAccess()).parse(reader);
                if(reader.canRead())throw new IllegalArgumentException("Trailing item component text");
                return result.components();
            } catch(com.mojang.brigadier.exceptions.CommandSyntaxException error) { throw new IllegalArgumentException(error); }
        }
    }
    *///?} else {
    public enum NbtMode {
        NONE, WEAK, STRONG
    }

    public record ItemMatcher(String itemId, CompoundTag tag, NbtMode mode) {
    }
    //?}

    // --- JSON 解析模型 ---
    public static class JsonModel {
        public List<Entry> tips;

        public static class Entry {
            public String stage = "any";
            public String text;
            public int time = 5000;
            public Conditions conditions;
        }

        public static class Conditions {
            public String biome;
            public String structure;
            public String advancement;
            public String dimension;
            public List<ItemCheck> items;
            public List<ItemCheck> curios;
        }

        public static class ItemCheck {
            public String id;
            //? if >=1.21 {
            /*public String components;
            public String componentMode = "NONE";
            // Kept only so incompatible old JSON is rejected rather than silently losing its conditions.
            private String nbt;
            private String nbtMode;

            public boolean hasLegacyItemData() {
                return nbt != null || nbtMode != null;
            }
            *///?} else {
            public String nbt;
            public String nbtMode = "NONE";
            //?}
        }
    }
}
