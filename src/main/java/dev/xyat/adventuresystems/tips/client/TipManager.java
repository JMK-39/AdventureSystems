package dev.xyat.adventuresystems.tips.client;

import dev.xyat.adventuresystems.tips.TipsModule;
import dev.xyat.adventuresystems.tips.TipsNetwork;
import dev.xyat.adventuresystems.tips.api.HelpTip;
import dev.xyat.adventuresystems.tips.config.GeneralConfig;
import dev.xyat.adventuresystems.tips.TipsUtils;
import dev.xyat.adventuresystems.tips.config.ConfigLoader;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

/**
 * 提示管理器
 * 负责根据当前环境筛选出合适的提示
 */
@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TipManager extends SimplePreparableReloadListener<Void> {
    private final List<HelpTip> allTips = new ArrayList<>();
    private final Random random = new Random();

    @Override
    protected Void prepare(ResourceManager rm, ProfilerFiller pf) { return null; }

    @Override
    protected void apply(Void v, ResourceManager rm, ProfilerFiller pf) {
        allTips.clear();
        Minecraft minecraft = Minecraft.getInstance();
        allTips.addAll(ConfigLoader.loadTipsForLanguage(minecraft.getLanguageManager().getSelected()));
        if (minecraft.getConnection() != null && minecraft.level != null) {
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
        allTips.addAll(ConfigLoader.loadTipsForLanguage(Minecraft.getInstance().getLanguageManager().getSelected()));
    }

    @Nullable
    public HelpTip getValidTip(final Screen screen) {
        if (!GeneralConfig.enableTips) return null;
        if (allTips.isEmpty()) return null;

        final Minecraft mc = Minecraft.getInstance();
        final boolean isPaused = screen instanceof PauseScreen;
        final Player player = mc.player;

        // --- 预取环境 ID 对象 ---
        ResourceLocation currentBiomeRL = null;
        ResourceLocation currentDimRL = null;
        if (mc.level != null) {
            currentDimRL = mc.level.dimension().location();
            if (player != null) {
                currentBiomeRL = mc.level.getBiome(player.blockPosition()).unwrapKey().map(net.minecraft.resources.ResourceKey::location).orElse(null);
            }
        }

        final String currentStruct = TipCache.currentStructure;

        List<HelpTip> priorityPool = new ArrayList<>();
        List<HelpTip> generalPool = new ArrayList<>();

        for (HelpTip tip : allTips) {
            // 1. 阶段检查 (stage 1: 加载中, stage 2: 游戏中)
            if (isPaused) {
                if (tip.stage == 1) continue;
            } else {
                if (tip.stage == 2) continue;
            }

            boolean isMatch = true;
            boolean hasCondition = false;

            // 2. 运行时条件检查
            if (isPaused && player != null && mc.level != null) {

                // 维度检查
                if (!tip.requiredDimension.isEmpty()) {
                    hasCondition = true;
                    if (!currentDimRL.toString().equals(tip.requiredDimension)) {
                        isMatch = false;
                    }
                }

                // 群系检查
                if (isMatch && !tip.requiredBiome.isEmpty()) {
                    hasCondition = true;
                    if (currentBiomeRL == null || !currentBiomeRL.toString().equals(tip.requiredBiome)) {
                        isMatch = false;
                    }
                }

                // 结构检查
                if (isMatch && !tip.requiredStructure.isEmpty()) {
                    hasCondition = true;
                    if (currentStruct == null || !currentStruct.equals(tip.requiredStructure)) {
                        isMatch = false;
                    }
                }

                // 成就检查
                if (isMatch && !tip.requiredAdvancement.isEmpty()) {
                    hasCondition = true;
                    if (!TipCache.isAdvancementDone(tip.requiredAdvancement)) {
                        isMatch = false;
                    }
                }

                // 物品检查 (支持 NBT 模式: NONE/WEAK/STRONG)
                if (isMatch && !tip.requiredItems.isEmpty()) {
                    hasCondition = true;
                    for (HelpTip.ItemMatcher req : tip.requiredItems) {
                        boolean found = false;
                        for (ItemStack stack : player.getInventory().items) {
                            ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(stack.getItem());

                            // ID 匹配
                            if (itemKey != null && itemKey.toString().equals(req.itemId())) {
                                // NBT 匹配 (调用 FlightAPI 公共逻辑)
                                if (TipsUtils.matchNbt(req, stack)) {
                                    found = true;
                                    break;
                                }
                            }
                        }
                        // 只要有一个要求没满足，整个物品条件就失败
                        if (!found) {
                            isMatch = false;
                            break;
                        }
                    }
                }

                // 饰品检查 (FlightAPI 内部已包含 NBT 逻辑)
                if (isMatch && !tip.requiredCurios.isEmpty()) {
                    hasCondition = true;
                    if (!TipsUtils.hasAllCurios(player, tip.requiredCurios)) {
                        isMatch = false;
                    }
                }
            }

            if (isMatch) {
                if (hasCondition) priorityPool.add(tip);
                else generalPool.add(tip);
            }
        }

        // 3. 最终选择逻辑
        if (!priorityPool.isEmpty()) {
            // 如果有满足特定条件的提示，70% 概率优先显示，否则从通用池里取
            if (generalPool.isEmpty() || random.nextFloat() < 0.7f) {
                return priorityPool.get(random.nextInt(priorityPool.size()));
            }
        }
        return generalPool.isEmpty() ? null : generalPool.get(random.nextInt(generalPool.size()));
    }
}
