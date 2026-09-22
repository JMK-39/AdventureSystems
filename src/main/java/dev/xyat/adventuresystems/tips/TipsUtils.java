package dev.xyat.adventuresystems.tips;

import dev.xyat.adventuresystems.tips.api.HelpTip;
import dev.xyat.adventuresystems.tips.client.TipRenderer;
import dev.xyat.kineticcore.api.client.text.KineticText;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import dev.xyat.kineticcore.api.runtime.KineticPlatform;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 提示系统综合工具类
 */
public class TipsUtils {
    private static final Map<String, String> MOD_NAME_CACHE = new HashMap<>();

    public static boolean matchNbt(HelpTip.ItemMatcher req, ItemStack stack) {
        if (req.mode() == HelpTip.NbtMode.NONE) return true;
        if (req.tag() == null) return true;

        CompoundTag stackTag = stack.getTag();
        if (stackTag == null) return false;

        if (req.mode() == HelpTip.NbtMode.STRONG) {
            return req.tag().equals(stackTag);
        }
        return NbtUtils.compareNbt(req.tag(), stackTag, true);
    }

    public static boolean hasAllCurios(Player player, List<HelpTip.ItemMatcher> matchers) {
        if (matchers == null || matchers.isEmpty()) return false;

        return CuriosApi.getCuriosHelper().getEquippedCurios(player).map(handler -> {
            for (HelpTip.ItemMatcher matcher : matchers) {
                boolean found = false;
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (stack.isEmpty()) continue;

                    ResourceLocation stackId = KineticRegistries.items().id(stack.getItem());
                    if (stackId != null && stackId.toString().equals(matcher.itemId()) && matchNbt(matcher, stack)) {
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
            }
            return true;
        }).orElse(false);
    }

    public static String getModName(String modId) {
        return MOD_NAME_CACHE.computeIfAbsent(modId, id -> {
            if ("minecraft".equals(id)) return "Minecraft";
            return KineticPlatform.displayName(id);
        });
    }

    public static String getPrettyName(String type, ResourceLocation key) {
        return KineticPlatform.callOnClient(
                () -> () -> Client.getPrettyName(type, key),
                key == null ? "" : key.getPath()
        );
    }

    public static void refreshPauseMenu() {
        KineticPlatform.runOnClient(() -> Client::refreshPauseMenu);
    }

    public static <T> List<ResourceLocation> getRegistryKeys(ResourceKey<? extends Registry<T>> registryKey) {
        return KineticPlatform.callOnClient(
                () -> () -> Client.getRegistryKeys(registryKey),
                Collections.emptyList()
        );
    }

    private static class Client {
        private static String getPrettyName(String type, ResourceLocation key) {
            if (key == null) return "";
            String transKey = Util.makeDescriptionId(type, key);
            if (KineticText.hasTranslation(transKey)) {
                return KineticText.get(transKey);
            }

            String path = key.getPath().replace('_', ' ');
            if (path.isEmpty()) return path;

            return Arrays.stream(path.split(" "))
                    .filter(s -> !s.isEmpty())
                    .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase())
                    .collect(Collectors.joining(" "));
        }

        private static void refreshPauseMenu() {
            var screen = KineticClientRuntime.currentScreen();
            if (screen instanceof net.minecraft.client.gui.screens.PauseScreen) {
                TipRenderer.refresh(screen);
            }
        }

        private static <T> List<ResourceLocation> getRegistryKeys(ResourceKey<? extends Registry<T>> registryKey) {
            return new ArrayList<>(KineticClientRuntime.registryKeys(registryKey));
        }
    }
}
