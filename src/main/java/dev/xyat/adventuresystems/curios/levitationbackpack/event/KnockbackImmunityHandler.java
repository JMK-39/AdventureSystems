package dev.xyat.adventuresystems.curios.levitationbackpack.event;

import dev.xyat.adventuresystems.curios.config.CuriosConfig;
import dev.xyat.adventuresystems.curios.init.Items;
import dev.xyat.kineticcore.api.entity.event.KineticLivingEvents;
import dev.xyat.kineticcore.api.event.KineticEventPriority;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;

public final class KnockbackImmunityHandler {
    private KnockbackImmunityHandler() {
    }

    public static void install() {
        KineticLivingEvents.onKnockback(KineticEventPriority.NORMAL, KnockbackImmunityHandler::onKnockback);
    }

    private static void onKnockback(KineticLivingEvents.KnockbackContext event) {
        if (!CuriosConfig.enableLevitationBackpack || !CuriosConfig.levitationKnockbackImmunity) return;
        if (event.entity() instanceof Player player && player.getAbilities().flying && hasLevitationBackpack(player)) {
            event.cancel();
        }
    }

    private static boolean hasLevitationBackpack(Player player) {
        return CuriosApi.getCuriosHelper()
                .findFirstCurio(player, stack -> stack.is(Items.LEVITATION_BACKPACK.get()))
                .isPresent();
    }
}
