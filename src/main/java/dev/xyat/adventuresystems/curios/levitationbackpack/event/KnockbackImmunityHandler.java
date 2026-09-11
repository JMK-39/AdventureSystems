package dev.xyat.adventuresystems.curios.levitationbackpack.event;

import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.init.Items;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = CuriosModule.MODID)
public class KnockbackImmunityHandler {

    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        if (!CuriosConfig.enableLevitationBackpack) return;

        if (event.getEntity() instanceof Player player) {
            if (player.getAbilities().flying && hasLevitationBackpack(player)) {
                event.setCanceled(true);
            }
        }
    }

    private static boolean hasLevitationBackpack(Player player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(Items.LEVITATION_BACKPACK.get()))
                .isPresent();
    }
}
