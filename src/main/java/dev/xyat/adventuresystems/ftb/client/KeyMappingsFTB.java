package dev.xyat.adventuresystems.ftb.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.xyat.adventuresystems.ftb.FtbModule;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public final class KeyMappingsFTB {
    public static final KeyMapping OPEN_QUEST = new KeyMapping(
            "key." + FtbModule.MODID + ".ftb.open",
            KeyConflictContext.GUI,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.adventuresystems.category"
    );

    public static final KeyMapping OPEN_QUEST_MULTI = new KeyMapping(
            "key." + FtbModule.MODID + ".ftb.open.multi",
            KeyConflictContext.GUI,
            KeyModifier.ALT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.adventuresystems.category"
    );

    private KeyMappingsFTB() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_QUEST);
        event.register(OPEN_QUEST_MULTI);
    }
}
