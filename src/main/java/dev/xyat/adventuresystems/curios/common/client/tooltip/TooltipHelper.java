package dev.xyat.adventuresystems.curios.common.client.tooltip;

import dev.xyat.kineticcore.api.text.KineticI18n;
import dev.xyat.kineticcore.api.client.tooltip.KineticItemTooltips;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TooltipHelper {

    /** 添加基础操作提示 */
    public static void addHints(List<Component> tooltip) {
        tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.global.hold_shift"));
        tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.global.hold_alt"));
    }

    /** 添加灵魂绑定状态 */
    public static void addBindingTooltip(ItemStack stack, List<Component> tooltip) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null || !nbt.hasUUID("adventuresystems_owner_id")) {
            tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.global.unbound"));
            return;
        }

        UUID ownerId = nbt.getUUID("adventuresystems_owner_id");
        String ownerName = nbt.getString("owner_name");
        if (ownerName.isEmpty()) ownerName = "Unknown";

        Player player = KineticClientRuntime.localPlayer();
        if (player != null) {
            if (player.getUUID().equals(ownerId)) {
                tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.global.bound_to", ownerName));
            } else {
                tooltip.add(KineticI18n.translatable("tip.adventuresystems.curios.global.void_binding"));
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

    public static class ClientConflictTooltip implements ClientTooltipComponent {
        private final List<ItemStack> icons;
        public ClientConflictTooltip(ConflictTooltipData data) { this.icons = data.icons(); }
        @Override public int getHeight() { return 20; }
        @Override public int getWidth(@NotNull Font font) { return icons.size() * 18; }
        @Override public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics graphics) {
            for (int i = 0; i < icons.size(); i++) {
                graphics.renderItem(icons.get(i), x + i * 18, y);
            }
        }
    }
}
