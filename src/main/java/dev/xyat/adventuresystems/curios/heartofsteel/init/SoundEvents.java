package dev.xyat.adventuresystems.curios.heartofsteel.init;

import dev.xyat.adventuresystems.curios.CuriosModule;
import dev.xyat.kineticcore.api.registry.KineticRegistryHandle;
import dev.xyat.kineticcore.api.registry.KineticSoundEvents;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import net.minecraft.sounds.SoundEvent;

public final class SoundEvents {
    public static final KineticRegistryHandle<SoundEvent> HEART_OF_STEEL = KineticSoundEvents.register(
            KineticResourceIds.of(CuriosModule.MODID, "heart_of_steel"),
            () -> SoundEvent.createVariableRangeEvent(KineticResourceIds.of(CuriosModule.MODID, "heart_of_steel"))
    );

    private SoundEvents() {
    }

    public static void register() {
    }
}
