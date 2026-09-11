package dev.xyat.adventuresystems.curios.wallet.data;

import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public record CurrencyType(String itemId, ResourceLocation resourceLocation, long value) {
    public static Optional<CurrencyType> parse(String text) {
        if (text == null) return Optional.empty();
        String[] parts = text.trim().split("\\|");
        if (parts.length < 2) return Optional.empty();
        String id = parts[0].trim();
        String valueText = parts[1].trim();
        try {
            ResourceLocation location = new ResourceLocation(id);
            long value = Long.parseLong(valueText);
            if (value <= 0) return Optional.empty();
            return Optional.of(new CurrencyType(location.toString(), location, value));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public Item item() {
        Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
        if (item == null) return net.minecraft.world.item.Items.AIR;
        return item;
    }

    public boolean hasItem() {
        Item item = item();
        return item != net.minecraft.world.item.Items.AIR;
    }
}

