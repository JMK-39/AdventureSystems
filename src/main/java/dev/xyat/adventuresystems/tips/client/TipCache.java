package dev.xyat.adventuresystems.tips.client;

import dev.xyat.adventuresystems.tips.TipsModule;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = TipsModule.MODID, value = Dist.CLIENT)
public class TipCache {

    public static String currentStructure = "";
    public static final Set<ResourceLocation> ALL_STRUCTURES = new HashSet<>();
    public static final TipManager TIP_MANAGER = new TipManager();

    public static final Set<ResourceLocation> REGISTRY_STRUCTURES = new HashSet<>();
    public static final Set<ResourceLocation> REGISTRY_BIOMES = new HashSet<>();

    private static final Set<String> DONE_ADVANCEMENTS = new HashSet<>();
    private static Field progressField = null;
    private static boolean reflectionFailed = false;

    @Mod.EventBusSubscriber(modid = TipsModule.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(TIP_MANAGER);
        }
    }

    public static boolean isAdvancementDone(String id) {
        return DONE_ADVANCEMENTS.contains(id);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().player != null) {
            if (Minecraft.getInstance().player.tickCount % 20 == 0) {
                updateAdvancementCache();
            }
        }
    }

    @SubscribeEvent
    public static void onLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        DONE_ADVANCEMENTS.clear();
        REGISTRY_STRUCTURES.clear();
        REGISTRY_BIOMES.clear();
        ALL_STRUCTURES.clear();
        currentStructure = "";
        reflectionFailed = false;

        var access = event.getPlayer().connection.registryAccess();
        try {
            access.registry(Registries.STRUCTURE).ifPresent(reg -> REGISTRY_STRUCTURES.addAll(reg.keySet()));
            access.registry(Registries.BIOME).ifPresent(reg -> REGISTRY_BIOMES.addAll(reg.keySet()));
        } catch (Exception e) {
            TipsModule.LOGGER.error("TipCache: RecipeRegistry sync failed", e);
        }
    }

    private static void updateAdvancementCache() {
        if (reflectionFailed) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) return;
        ClientAdvancements manager = mc.getConnection().getAdvancements();

        try {
            if (progressField == null) {
                progressField = ObfuscationReflectionHelper.findField(ClientAdvancements.class, "f_104390_");
                progressField.setAccessible(true);
            }

            Object value = progressField.get(manager);
            if (value instanceof Map<?, ?> progressMap) {
                for (Map.Entry<?, ?> entry : progressMap.entrySet()) {
                    if (entry.getKey() instanceof Advancement advancement
                            && entry.getValue() instanceof AdvancementProgress progress
                            && progress.isDone()) {
                        DONE_ADVANCEMENTS.add(advancement.getId().toString());
                    }
                }
            }
        } catch (Exception e) {
            reflectionFailed = true;
        }
    }
}
