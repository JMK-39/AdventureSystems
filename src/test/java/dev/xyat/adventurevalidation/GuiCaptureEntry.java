//? if >=26.1 {
/*package dev.xyat.adventurevalidation;

/^** 26.1 entry of the validation mod: only the GUI capture runs there; RuntimeValidation targets 1.21's APIs. *^/
@net.neoforged.fml.common.Mod("adventuresystems_validation")
public final class GuiCaptureEntry {
    public GuiCaptureEntry() {
        if (Boolean.getBoolean("adventuresystems.guiValidation")) {
            dev.xyat.kineticcore.api.runtime.KineticPlatform.runOnClient(() -> GuiCaptureValidation::install);
        }
        if (Boolean.getBoolean("adventuresystems.walletHoldingsCheck")) {
            dev.xyat.kineticcore.api.runtime.KineticPlatform.runOnClient(() -> WalletHoldingsCheck::install);
        }
    }
}
*///?}
