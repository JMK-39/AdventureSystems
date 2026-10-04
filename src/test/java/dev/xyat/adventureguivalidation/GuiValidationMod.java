//? if >=1.21 && <26 {
/*package dev.xyat.adventureguivalidation;
@net.neoforged.fml.common.Mod("adventuresystems_gui_validation")
public final class GuiValidationMod {
    public GuiValidationMod(){
        if(Boolean.getBoolean("adventuresystems.guiValidation"))
            dev.xyat.kineticcore.api.runtime.KineticPlatform.runOnClient(() -> () ->
                dev.xyat.kineticcore.api.runtime.KineticClientRuntime.execute(GuiLongTextValidation::install));
    }
}
*///?}
