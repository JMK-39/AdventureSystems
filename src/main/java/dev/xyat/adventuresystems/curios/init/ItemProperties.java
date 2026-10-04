package dev.xyat.adventuresystems.curios.init;

import dev.xyat.adventuresystems.curios.CuriosModule;
import net.minecraft.world.item.Item;

/** Item properties for one of the mod's items. Since 1.21.2 an item must know its registry id when it is built. */
public final class ItemProperties {
    private ItemProperties() {
    }

    public static Item.Properties of(String path) {
        Item.Properties properties = new Item.Properties();
        //? if >=26.1 {
        /*properties.setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM,
                dev.xyat.kineticcore.api.resource.KineticResourceIds.of(CuriosModule.MODID, path)));
        *///?}
        return properties;
    }
}
