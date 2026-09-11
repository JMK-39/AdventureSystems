package dev.xyat.adventuresystems.curios.common.event;

import dev.xyat.adventuresystems.curios.util.ColorText;
import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.init.Items;
import dev.xyat.kineticcore.api.client.overlay.GuiOverlay;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.util.List;

@Mod.EventBusSubscriber(modid = CuriosModule.MODID)
public class CurioConflictHandler {

    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // 分离双端逻辑，避免跨端异常
        boolean isClient = player.level().isClientSide;

        if (CuriosConfig.enableHeartOfSteel) checkAndUnequip(player, Items.HEART_OF_STEEL.get(), CuriosConfig.hosConflicts, isClient);
        if (CuriosConfig.enableParadiseLost) checkAndUnequip(player, Items.PARADISE_LOST.get(), CuriosConfig.plConflicts, isClient);
    }

    private static void checkAndUnequip(Player player, Item targetItem, List<String> conflicts, boolean isClient) {
        CuriosApi.getCuriosHelper().findFirstCurio(player, targetItem).ifPresent(slotResult -> {
            for (String conflictId : conflicts) {
                Item conflictItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(conflictId));
                if (conflictItem != null && conflictItem != net.minecraft.world.item.Items.AIR) {
                    if (CuriosApi.getCuriosHelper().findFirstCurio(player, conflictItem).isPresent()) {

                        if (isClient) {
                            // 客户端专心负责弹出完美的金边弹窗提示
                            GuiOverlay.toast(ColorText.translatable("msg.adventuresystems.curios.force_unequip",
                                    ColorText.translatable(targetItem.getDescriptionId()),
                                    ColorText.translatable(conflictItem.getDescriptionId())));
                        } else {
                            // 服务端专心负责把冲突的装备强制卸下并掉落
                            SlotContext context = slotResult.slotContext();
                            ItemStack targetStack = slotResult.stack();

                            CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> handler.getStacksHandler(context.identifier()).ifPresent(stacks -> {
                                stacks.getStacks().setStackInSlot(context.index(), ItemStack.EMPTY);
                                if (!player.getInventory().add(targetStack)) {
                                    player.drop(targetStack, false);
                                }
                            }));
                        }
                        return;
                    }
                }
            }
        });
    }
}
