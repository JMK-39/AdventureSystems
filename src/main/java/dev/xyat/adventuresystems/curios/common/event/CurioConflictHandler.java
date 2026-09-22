package dev.xyat.adventuresystems.curios.common.event;

import dev.xyat.kineticcore.api.text.KineticI18n;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.init.Items;
import dev.xyat.kineticcore.api.client.overlay.KineticOverlays;
import dev.xyat.kineticcore.api.compat.curios.KineticCuriosEvents;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;

public final class CurioConflictHandler {
    private CurioConflictHandler() {
    }

    public static void install() {
        KineticCuriosEvents.onChange(CurioConflictHandler::onCurioChange);
    }

    private static void onCurioChange(KineticCuriosEvents.ChangeContext event) {
        if (!(event.entity() instanceof Player player)) return;

        boolean isClient = player.level().isClientSide;

        if (CuriosConfig.enableHeartOfSteel) {
            checkAndUnequip(player, Items.HEART_OF_STEEL.get(), CuriosConfig.hosConflicts, isClient);
        }
        if (CuriosConfig.enableParadiseLost) {
            checkAndUnequip(player, Items.PARADISE_LOST.get(), CuriosConfig.plConflicts, isClient);
        }
    }

    private static void checkAndUnequip(Player player, Item targetItem, List<String> conflicts, boolean isClient) {
        CuriosApi.getCuriosHelper().findFirstCurio(player, targetItem).ifPresent(slotResult -> {
            for (String conflictId : conflicts) {
                Item conflictItem = KineticRegistries.items().get(KineticResourceIds.parse(conflictId));
                if (conflictItem != null && conflictItem != net.minecraft.world.item.Items.AIR
                        && CuriosApi.getCuriosHelper().findFirstCurio(player, conflictItem).isPresent()) {
                    if (isClient) {
                        KineticOverlays.toast(KineticI18n.translatable(
                                "msg.adventuresystems.curios.force_unequip",
                                KineticI18n.translatable(targetItem.getDescriptionId()),
                                KineticI18n.translatable(conflictItem.getDescriptionId())
                        ));
                    } else {
                        SlotContext context = slotResult.slotContext();
                        ItemStack targetStack = slotResult.stack();
                        CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler ->
                                handler.getStacksHandler(context.identifier()).ifPresent(stacks -> {
                                    stacks.getStacks().setStackInSlot(context.index(), ItemStack.EMPTY);
                                    if (!player.getInventory().add(targetStack)) {
                                        player.drop(targetStack, false);
                                    }
                                })
                        );
                    }
                    return;
                }
            }
        });
    }
}
