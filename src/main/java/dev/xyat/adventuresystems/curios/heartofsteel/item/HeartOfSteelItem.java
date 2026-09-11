package dev.xyat.adventuresystems.curios.heartofsteel.item;

import dev.xyat.adventuresystems.curios.util.ColorText;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.kineticcore.api.client.overlay.GuiOverlay;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;
public class HeartOfSteelItem extends Item implements ICurioItem {
    public HeartOfSteelItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        List<String> conflicts = CuriosConfig.hosConflicts;
        for (String conflictId : conflicts) {
            Item conflictItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(conflictId));
            if (conflictItem != null && conflictItem != net.minecraft.world.item.Items.AIR) {
                if (CuriosApi.getCuriosHelper().findFirstCurio(entity, conflictItem).isPresent()) {
                    if (entity instanceof Player player && player.level().isClientSide) {
                        GuiOverlay.toast(ColorText.translatable("msg.adventuresystems.curios.equip_conflict",
                                ColorText.translatable(conflictItem.getDescriptionId())));
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
            boolean isClient = player.level().isClientSide;

            if (!nbt.hasUUID("adventuresystems_owner_id")) {
                if (!isClient) {
                    nbt.putUUID("adventuresystems_owner_id", player.getUUID());
                    nbt.putString("owner_name", player.getScoreboardName());
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.2f);
                } else {
                    GuiOverlay.toast(ColorText.translatable("msg.adventuresystems.curios.soul_bound"));
                }
            } else {
                UUID ownerId = nbt.getUUID("adventuresystems_owner_id");
                if (!player.getUUID().equals(ownerId)) {
                    if (!isClient) {
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 0.5f);
                    } else {
                        String ownerName = nbt.getString("owner_name");
                        GuiOverlay.toast(ColorText.translatable("msg.adventuresystems.curios.bound_to_other", ownerName));
                    }
                }
            }
        }
    }
}
