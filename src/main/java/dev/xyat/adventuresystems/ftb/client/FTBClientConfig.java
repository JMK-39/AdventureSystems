package dev.xyat.adventuresystems.ftb.client;

import dev.xyat.kineticcore.api.config.client.KTClientConfigAdapter;
import dev.xyat.kineticcore.api.config.client.KTClientConfigSpec;

public final class FTBClientConfig {
    public static final KTClientConfigSpec SPEC;

    private static final KTClientConfigSpec.BooleanValue ENABLE_TASK_JUMP;
    private static boolean registered;

    static {
        KTClientConfigSpec.Builder builder = KTClientConfigSpec.builder();
        builder.push("ftb_item_task_jump");
        ENABLE_TASK_JUMP = builder.defineBoolean("enableTaskJump", true);
        builder.pop();
        SPEC = builder.build();
    }

    private FTBClientConfig() {
    }

    public static void register() {
        if (registered) return;
        KTClientConfigAdapter.registerSpec(SPEC, "kineticcore/ftb_item_client.toml");
        registered = true;
    }

    public static boolean shouldSkipTaskJump() {
        return !ENABLE_TASK_JUMP.get();
    }

    public static boolean isTaskJumpEnabled() {
        return ENABLE_TASK_JUMP.get();
    }

    public static void setTaskJumpEnabled(boolean enabled) {
        ENABLE_TASK_JUMP.set(enabled);
    }

    public static void save() {
        SPEC.save();
    }
}
