package dev.xyat.adventuresystems.ftb.client.hud;

import dev.xyat.kineticcore.api.text.KineticI18n;
import net.minecraft.network.chat.Component;

public final class FTBSubmitResultClientToast {
    private FTBSubmitResultClientToast() {
    }

    public static void show(int requestedTimes, int completedTimes, long submittedItems) {
        int requested = Math.max(1, requestedTimes);
        int completed = Math.max(0, completedTimes);
        long submitted = Math.max(0L, submittedItems);

        Component message;
        if (completed == 0 && submitted > 0L) {
            message = KineticI18n.translatable("toast.adventuresystems.ftb.submit.partial_items", number(submitted));
        } else if (completed == 0) {
            message = KineticI18n.translatable("toast.adventuresystems.ftb.submit.none", number(requested));
        } else if (completed < requested) {
            message = KineticI18n.translatable("toast.adventuresystems.ftb.submit.partial", number(requested), number(completed));
        } else {
            message = KineticI18n.translatable("toast.adventuresystems.ftb.submit.success", number(completed));
        }

        FTBToastUtil.showLong("adventuresystems.ftb.submit.result", message);
    }

    private static Component number(long value) {
        return Component.literal(String.valueOf(value));
    }
}
