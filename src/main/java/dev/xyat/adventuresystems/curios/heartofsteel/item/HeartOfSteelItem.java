//? if >=1.21 {
/*package dev.xyat.adventuresystems.curios.heartofsteel.item;

import dev.xyat.adventuresystems.text.AdventureText;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.kineticcore.api.client.gui.overlay.KineticOverlays;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;
public class HeartOfSteelItem extends Item implements ICurioItem {
    public HeartOfSteelItem() {
        super(dev.xyat.adventuresystems.curios.init.ItemProperties.of("heart_of_steel").stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        List<String> conflicts = CuriosConfig.hosConflicts;
        for (String conflictId : conflicts) {
            Item conflictItem = KineticRegistries.items().get(KineticResourceIds.parse(conflictId));
            if (conflictItem != null && conflictItem != net.minecraft.world.item.Items.AIR) {
                if (dev.xyat.adventuresystems.curios.common.util.CuriosAccess.findFirst(entity, conflictItem).isPresent()) {
                    if (entity instanceof Player player && player.level().isClientSide()) {
                        KineticOverlays.toast(AdventureText.translatable("msg.adventuresystems.curios.equip_conflict",
                                AdventureText.translatable(conflictItem.getDescriptionId())));
                    }
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            CompoundTag nbt = dev.xyat.adventuresystems.data.AdventureItemData.customData(stack);
            boolean isClient = player.level().isClientSide();

            if (!dev.xyat.adventuresystems.data.Nbt.hasUuid(nbt, "adventuresystems_owner_id")) {
                if (!isClient) {
                    dev.xyat.adventuresystems.data.Nbt.putUuid(nbt, "adventuresystems_owner_id", player.getUUID());
                    nbt.putString("owner_name", player.getScoreboardName());
                    dev.xyat.adventuresystems.data.AdventureItemData.updateCustomData(stack, data -> data.merge(nbt));
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.2f);
                } else {
                    KineticOverlays.toast(AdventureText.translatable("msg.adventuresystems.curios.soul_bound"));
                }
            } else {
                UUID ownerId = dev.xyat.adventuresystems.data.Nbt.uuid(nbt, "adventuresystems_owner_id");
                if (!player.getUUID().equals(ownerId)) {
                    if (!isClient) {
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 0.5f);
                    } else {
                        String ownerName = nbt.getString("owner_name");
                        KineticOverlays.toast(AdventureText.translatable("msg.adventuresystems.curios.bound_to_other", ownerName));
                    }
                }
            }
        }
    }
}

*///?} else {
package dev.xyat.adventuresystems.curios.heartofsteel.item;

import dev.xyat.adventuresystems.text.AdventureText;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.kineticcore.api.client.gui.overlay.KineticOverlays;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;
public class HeartOfSteelItem extends Item implements ICurioItem {
    public HeartOfSteelItem() {
        super(dev.xyat.adventuresystems.curios.init.ItemProperties.of("heart_of_steel").stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        List<String> conflicts = CuriosConfig.hosConflicts;
        for (String conflictId : conflicts) {
            Item conflictItem = KineticRegistries.items().get(KineticResourceIds.parse(conflictId));
            if (conflictItem != null && conflictItem != net.minecraft.world.item.Items.AIR) {
                if (dev.xyat.adventuresystems.curios.common.util.CuriosAccess.findFirst(entity, conflictItem).isPresent()) {
                    if (entity instanceof Player player && player.level().isClientSide()) {
                        KineticOverlays.toast(AdventureText.translatable("msg.adventuresystems.curios.equip_conflict",
                                AdventureText.translatable(conflictItem.getDescriptionId())));
                    }
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            CompoundTag nbt = stack.getOrCreateTag();
            boolean isClient = player.level().isClientSide();

            if (!dev.xyat.adventuresystems.data.Nbt.hasUuid(nbt, "adventuresystems_owner_id")) {
                if (!isClient) {
                    dev.xyat.adventuresystems.data.Nbt.putUuid(nbt, "adventuresystems_owner_id", player.getUUID());
                    nbt.putString("owner_name", player.getScoreboardName());
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.2f);
                } else {
                    KineticOverlays.toast(AdventureText.translatable("msg.adventuresystems.curios.soul_bound"));
                }
            } else {
                UUID ownerId = dev.xyat.adventuresystems.data.Nbt.uuid(nbt, "adventuresystems_owner_id");
                if (!player.getUUID().equals(ownerId)) {
                    if (!isClient) {
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 0.5f);
                    } else {
                        String ownerName = nbt.getString("owner_name");
                        KineticOverlays.toast(AdventureText.translatable("msg.adventuresystems.curios.bound_to_other", ownerName));
                    }
                }
            }
        }
    }
}

//?}
