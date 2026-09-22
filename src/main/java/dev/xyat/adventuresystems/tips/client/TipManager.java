package dev.xyat.adventuresystems.tips.client;

import dev.xyat.adventuresystems.tips.TipsModule;
import dev.xyat.adventuresystems.tips.TipsNetwork;
import dev.xyat.adventuresystems.tips.TipsUtils;
import dev.xyat.adventuresystems.tips.api.HelpTip;
import dev.xyat.adventuresystems.tips.config.ConfigLoader;
import dev.xyat.adventuresystems.tips.config.GeneralConfig;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 提示管理器
 * 负责根据当前环境筛选出合适的提示
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TipManager extends SimplePreparableReloadListener<Void> {
    private final List<HelpTip> allTips = new ArrayList<>();
    private final Random random = new Random();

    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return null;
    }

    @Override
    protected void apply(Void ignored, ResourceManager resourceManager, ProfilerFiller profiler) {
        allTips.clear();
        allTips.addAll(ConfigLoader.loadTipsForLanguage(KineticClientRuntime.selectedLanguage()));
        if (KineticClientRuntime.connected() && KineticClientRuntime.currentLevel() != null) {
            TipsNetwork.requestRuntimeTips();
        }
        TipsModule.LOGGER.info("TipsManager: Loaded {} entries.", allTips.size());
    }

    public void replaceServerEntries(List<HelpTip.JsonModel.Entry> entries) {
        allTips.clear();
        allTips.addAll(ConfigLoader.toRuntimeTips(entries));
        TipsModule.LOGGER.info("TipsManager: Applied {} server entries.", allTips.size());
    }

    public void reloadLocalFallback() {
        allTips.clear();
        allTips.addAll(ConfigLoader.loadTipsForLanguage(KineticClientRuntime.selectedLanguage()));
    }

    @Nullable
    public HelpTip getValidTip(final Screen screen) {
        if (!GeneralConfig.isEnabled() || allTips.isEmpty()) return null;

        final boolean paused = screen instanceof PauseScreen;
        final Player player = KineticClientRuntime.localPlayer();
        final var level = KineticClientRuntime.currentLevel();

        ResourceLocation currentBiome = null;
        ResourceLocation currentDimension = null;
        if (level != null) {
            currentDimension = level.dimension().location();
            if (player != null) {
                currentBiome = level.getBiome(player.blockPosition())
                        .unwrapKey()
                        .map(net.minecraft.resources.ResourceKey::location)
                        .orElse(null);
            }
        }

        final String currentStructure = TipCache.currentStructure;
        List<HelpTip> priorityPool = new ArrayList<>();
        List<HelpTip> generalPool = new ArrayList<>();

        for (HelpTip tip : allTips) {
            if (paused) {
                if (tip.stage == 1) continue;
            } else if (tip.stage == 2) {
                continue;
            }

            boolean matches = true;
            boolean hasCondition = false;

            if (paused && player != null && level != null) {
                if (!tip.requiredDimension.isEmpty()) {
                    hasCondition = true;
                    if (currentDimension == null || !currentDimension.toString().equals(tip.requiredDimension)) {
                        matches = false;
                    }
                }

                if (matches && !tip.requiredBiome.isEmpty()) {
                    hasCondition = true;
                    if (currentBiome == null || !currentBiome.toString().equals(tip.requiredBiome)) {
                        matches = false;
                    }
                }

                if (matches && !tip.requiredStructure.isEmpty()) {
                    hasCondition = true;
                    if (currentStructure == null || !currentStructure.equals(tip.requiredStructure)) {
                        matches = false;
                    }
                }

                if (matches && !tip.requiredAdvancement.isEmpty()) {
                    hasCondition = true;
                    if (!TipCache.isAdvancementDone(tip.requiredAdvancement)) {
                        matches = false;
                    }
                }

                if (matches && !tip.requiredItems.isEmpty()) {
                    hasCondition = true;
                    for (HelpTip.ItemMatcher required : tip.requiredItems) {
                        boolean found = false;
                        for (ItemStack stack : player.getInventory().items) {
                            ResourceLocation itemKey = KineticRegistries.items().id(stack.getItem());
                            if (itemKey != null
                                    && itemKey.toString().equals(required.itemId())
                                    && TipsUtils.matchNbt(required, stack)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            matches = false;
                            break;
                        }
                    }
                }

                if (matches && !tip.requiredCurios.isEmpty()) {
                    hasCondition = true;
                    if (!TipsUtils.hasAllCurios(player, tip.requiredCurios)) {
                        matches = false;
                    }
                }
            }

            if (matches) {
                if (hasCondition) priorityPool.add(tip);
                else generalPool.add(tip);
            }
        }

        if (!priorityPool.isEmpty() && (generalPool.isEmpty() || random.nextFloat() < 0.7F)) {
            return priorityPool.get(random.nextInt(priorityPool.size()));
        }
        return generalPool.isEmpty() ? null : generalPool.get(random.nextInt(generalPool.size()));
    }
}
