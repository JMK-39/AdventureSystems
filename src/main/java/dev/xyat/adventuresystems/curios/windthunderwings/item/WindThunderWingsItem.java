package dev.xyat.adventuresystems.curios.windthunderwings.item;

import dev.xyat.adventuresystems.curios.init.ItemProperties;
import dev.xyat.adventuresystems.curios.windthunderwings.WingsServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** 风雷翅：背部饰品。佩戴后获得飞行能力，其余逻辑见 {@link WingsServer}。 */
public class WindThunderWingsItem extends Item implements ICurioItem {
    public WindThunderWingsItem() {
        super(ItemProperties.of("wind_thunder_wings").stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        WingsServer.equipped(slotContext.entity());
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        WingsServer.unequipped(slotContext.entity());
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        WingsServer.tick(slotContext.entity());
    }
}
