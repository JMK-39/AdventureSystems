//? if >=1.21 {
/*package dev.xyat.adventuresystems.curios.common.client.tooltip;

import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.tooltip.KineticTooltipComponent;
import dev.xyat.adventuresystems.text.AdventureText;
import dev.xyat.kineticcore.api.client.tooltip.KineticItemTooltips;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TooltipHelper {

    /^** 添加基础操作提示 *^/
    public static void addHints(List<Component> tooltip) {
        tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.global.hold_shift"));
        tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.global.hold_alt"));
    }

    /^** 添加灵魂绑定状态 *^/
    public static void addBindingTooltip(ItemStack stack, List<Component> tooltip) {
        CompoundTag nbt = dev.xyat.adventuresystems.data.AdventureItemData.customData(stack);
        if (nbt == null || !dev.xyat.adventuresystems.data.Nbt.hasUuid(nbt, "adventuresystems_owner_id")) {
            tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.global.unbound"));
            return;
        }

        UUID ownerId = dev.xyat.adventuresystems.data.Nbt.uuid(nbt, "adventuresystems_owner_id");
        String ownerName = nbt.getString("owner_name");
        if (ownerName.isEmpty()) ownerName = "Unknown";

        Player player = KineticClientRuntime.localPlayer();
        if (player != null) {
            if (player.getUUID().equals(ownerId)) {
                tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.global.bound_to", ownerName));
            } else {
                tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.global.void_binding"));
            }
        }
    }

    /^** 获取并添加排斥图标 *^/
    public static void appendConflictIcons(KineticItemTooltips.GatherContext event, List<String> conflicts) {
        List<ItemStack> icons = new ArrayList<>();
        for (String id : conflicts) {
            Item item = KineticRegistries.items().get(KineticResourceIds.parse(id));
            if (item != null && item != net.minecraft.world.item.Items.AIR) {
                icons.add(new ItemStack(item));
            }
        }
        if (!icons.isEmpty()) {
            event.addComponent(new ConflictTooltipData(icons));
        }
    }

    // --- 内部渲染逻辑 ---

    public record ConflictTooltipData(List<ItemStack> icons) implements TooltipComponent {}

    public static class ClientConflictTooltip implements KineticTooltipComponent {
        private final List<ItemStack> icons;
        public ClientConflictTooltip(ConflictTooltipData data) { this.icons = data.icons(); }
        @Override public int height() { return 20; }
        @Override public int width() { return icons.size() * 18; }
        @Override public void render(KineticGraphics graphics, int x, int y) {
            for (int i = 0; i < icons.size(); i++) {
                graphics.item(icons.get(i), x + i * 18, y);
            }
        }
    }
}

*///?} else {
package dev.xyat.adventuresystems.curios.common.client.tooltip;

import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.tooltip.KineticTooltipComponent;
import dev.xyat.adventuresystems.text.AdventureText;
import dev.xyat.kineticcore.api.client.tooltip.KineticItemTooltips;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TooltipHelper {

    /** 添加基础操作提示 */
    public static void addHints(List<Component> tooltip) {
        tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.global.hold_shift"));
        tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.global.hold_alt"));
    }

    /** 添加灵魂绑定状态 */
    public static void addBindingTooltip(ItemStack stack, List<Component> tooltip) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null || !dev.xyat.adventuresystems.data.Nbt.hasUuid(nbt, "adventuresystems_owner_id")) {
            tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.global.unbound"));
            return;
        }

        UUID ownerId = dev.xyat.adventuresystems.data.Nbt.uuid(nbt, "adventuresystems_owner_id");
        String ownerName = nbt.getString("owner_name");
        if (ownerName.isEmpty()) ownerName = "Unknown";

        Player player = KineticClientRuntime.localPlayer();
        if (player != null) {
            if (player.getUUID().equals(ownerId)) {
                tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.global.bound_to", ownerName));
            } else {
                tooltip.add(AdventureText.translatable("tip.adventuresystems.curios.global.void_binding"));
            }
        }
    }

    /** 获取并添加排斥图标 */
    public static void appendConflictIcons(KineticItemTooltips.GatherContext event, List<String> conflicts) {
        List<ItemStack> icons = new ArrayList<>();
        for (String id : conflicts) {
            Item item = KineticRegistries.items().get(KineticResourceIds.parse(id));
            if (item != null && item != net.minecraft.world.item.Items.AIR) {
                icons.add(new ItemStack(item));
            }
        }
        if (!icons.isEmpty()) {
            event.addComponent(new ConflictTooltipData(icons));
        }
    }

    // --- 内部渲染逻辑 ---

    public record ConflictTooltipData(List<ItemStack> icons) implements TooltipComponent {}

    public static class ClientConflictTooltip implements KineticTooltipComponent {
        private final List<ItemStack> icons;
        public ClientConflictTooltip(ConflictTooltipData data) { this.icons = data.icons(); }
        @Override public int height() { return 20; }
        @Override public int width() { return icons.size() * 18; }
        @Override public void render(KineticGraphics graphics, int x, int y) {
            for (int i = 0; i < icons.size(); i++) {
                graphics.item(icons.get(i), x + i * 18, y);
            }
        }
    }
}

//?}
