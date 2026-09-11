package dev.xyat.adventuresystems.tips;

import dev.xyat.adventuresystems.tips.api.HelpTip;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 提示系统综合工具类
 */
public class TipsUtils {
    private static final Map<String, String> MOD_NAME_CACHE = new HashMap<>();

    // --- 1. 通用逻辑 (服务端/客户端均可安全调用) ---

    public static boolean matchNbt(HelpTip.ItemMatcher req, ItemStack stack) {
        if (req.mode() == HelpTip.NbtMode.NONE) return true;
        if (req.tag() == null) return true;

        CompoundTag stackTag = stack.getTag();
        if (stackTag == null) return false;

        if (req.mode() == HelpTip.NbtMode.STRONG) {
            return req.tag().equals(stackTag);
        } else {
            return NbtUtils.compareNbt(req.tag(), stackTag, true);
        }
    }

    public static boolean hasAllCurios(Player player, List<HelpTip.ItemMatcher> matchers) {
        if (matchers == null || matchers.isEmpty()) return false;

        return CuriosApi.getCuriosHelper().getEquippedCurios(player).map(handler -> {
            for (HelpTip.ItemMatcher matcher : matchers) {
                boolean found = false;
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (stack.isEmpty()) continue;

                    ResourceLocation stackId = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    if (stackId != null && stackId.toString().equals(matcher.itemId())) {
                        if (matchNbt(matcher, stack)) {
                            found = true;
                            break;
                        }
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
            return ModList.get().getModContainerById(id)
                    .map(c -> c.getModInfo().getDisplayName())
                    .orElse(id);
        });
    }

    // --- 2. 客户端跳转方法 ---

    public static String getPrettyName(String type, ResourceLocation key) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return Client.getPrettyName(type, key);
        }
        return key.getPath();
    }

    public static void refreshPauseMenu() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            Client.refreshPauseMenu();
        }
    }

    public static List<ResourceLocation> getRegistryKeys(ResourceKey<? extends net.minecraft.core.Registry<?>> registryKey) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return Client.getRegistryKeys(registryKey);
        }
        return Collections.emptyList();
    }

    // --- 3. 物理隔离区 ---

    @OnlyIn(Dist.CLIENT)
    private static class Client {
        private static String getPrettyName(String type, ResourceLocation key) {
            String transKey = Util.makeDescriptionId(type, key);
            if (net.minecraft.client.resources.language.I18n.exists(transKey)) {
                return net.minecraft.client.resources.language.I18n.get(transKey);
            }

            String path = key.getPath().replace('_', ' ');
            if (path.isEmpty()) return path;

            return Arrays.stream(path.split(" "))
                    .filter(s -> !s.isEmpty())
                    .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase())
                    .collect(Collectors.joining(" "));
        }

        private static void refreshPauseMenu() {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen) {
                dev.xyat.adventuresystems.tips.client.TipRenderer.refresh(mc.screen);
            }
        }

        private static List<ResourceLocation> getRegistryKeys(ResourceKey<? extends net.minecraft.core.Registry<?>> registryKey) {
            List<ResourceLocation> keys = new ArrayList<>();
            var mc = net.minecraft.client.Minecraft.getInstance();
            try {
                if (mc.level != null) {
                    mc.level.registryAccess().registry(registryKey).ifPresent(r -> keys.addAll(r.keySet()));
                }
            } catch (Exception ignored) {}
            return keys;
        }
    }
}
