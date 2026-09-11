package dev.xyat.adventuresystems.ftb.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("hoveredSlot")
    @Nullable
    Slot adventuresystems_ftb$getHoveredSlot();

    @Accessor("leftPos")
    int adventuresystems_ftb$getLeftPos();

    @Accessor("topPos")
    int adventuresystems_ftb$getTopPos();
}
