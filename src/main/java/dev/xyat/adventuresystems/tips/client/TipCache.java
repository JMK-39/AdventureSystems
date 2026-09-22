package dev.xyat.adventuresystems.tips.client;

import dev.xyat.adventuresystems.tips.TipsModule;
import dev.xyat.kineticcore.api.client.advancement.KineticClientAdvancements;
import dev.xyat.kineticcore.api.client.event.KineticClientEvents;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public final class TipCache {
    public static String currentStructure = "";
    public static final Set<ResourceLocation> ALL_STRUCTURES = new HashSet<>();
    public static final TipManager TIP_MANAGER = new TipManager();

    public static final Set<ResourceLocation> REGISTRY_STRUCTURES = new HashSet<>();
    public static final Set<ResourceLocation> REGISTRY_BIOMES = new HashSet<>();

    private static final Set<String> DONE_ADVANCEMENTS = new HashSet<>();

    private TipCache() {
    }

    public static void install() {
        KineticClientEvents.registerReloadListener(TIP_MANAGER);
        KineticClientEvents.onTick(KineticClientEvents.TickPhase.END, TipCache::onClientTick);
        KineticClientEvents.onLogin(TipCache::onLogin);
    }

    public static boolean isAdvancementDone(String id) {
        return DONE_ADVANCEMENTS.contains(id);
    }

    private static void onClientTick() {
        var player = KineticClientRuntime.localPlayer();
        if (player != null && player.tickCount % 20 == 0) {
            updateAdvancementCache();
        }
    }

    private static void onLogin() {
        DONE_ADVANCEMENTS.clear();
        REGISTRY_STRUCTURES.clear();
        REGISTRY_BIOMES.clear();
        ALL_STRUCTURES.clear();
        currentStructure = "";

        try {
            REGISTRY_STRUCTURES.addAll(KineticClientRuntime.registryKeys(Registries.STRUCTURE));
            REGISTRY_BIOMES.addAll(KineticClientRuntime.registryKeys(Registries.BIOME));
        } catch (RuntimeException e) {
            TipsModule.LOGGER.error("TipCache: RecipeRegistry sync failed", e);
        }
        updateAdvancementCache();
    }

    private static void updateAdvancementCache() {
        for (ResourceLocation id : KineticClientAdvancements.completedIds()) {
            DONE_ADVANCEMENTS.add(id.toString());
        }
    }
}
