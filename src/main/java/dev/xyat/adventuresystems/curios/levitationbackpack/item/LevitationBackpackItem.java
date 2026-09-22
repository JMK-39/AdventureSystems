package dev.xyat.adventuresystems.curios.levitationbackpack.item;

import javax.annotation.Nonnull;

import dev.xyat.kineticcore.api.flight.KineticFlightSources;
import dev.xyat.kineticcore.api.text.KineticI18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

public class LevitationBackpackItem extends Item implements ICurioItem {

    public LevitationBackpackItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    /**
     * 当背包放入 Curios 槽位时，计数器 +1
     */
    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        // 我们只在服务端处理逻辑，API 内部其实已经处理了 isClientSide 判断，
        // 但为了严谨，这里直接调用即可。
        KineticFlightSources.addSource(slotContext.entity(), "levitation_backpack");
    }

    /**
     * 当背包从 Curios 槽位移除时，计数器 -1
     */
    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        KineticFlightSources.removeSource(slotContext.entity(), "levitation_backpack");
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        tooltipComponents.add(KineticI18n.translatable("tip.adventuresystems.curios.levitation_backpack.title"));
        tooltipComponents.add(KineticI18n.translatable("tip.adventuresystems.curios.levitation_backpack.desc1"));
        tooltipComponents.add(KineticI18n.translatable("tip.adventuresystems.curios.levitation_backpack.desc2"));
    }
}
