package dev.xyat.adventuresystems.ftb.event;

import dev.xyat.adventuresystems.ftb.client.FTBClientConfig;
import dev.xyat.adventuresystems.ftb.client.ItemClientModuleFTB;
import dev.xyat.adventuresystems.ftb.client.KeyMappingsFTB;
import dev.xyat.adventuresystems.ftb.client.gui.SelectScreenFTB;
import dev.xyat.adventuresystems.ftb.client.hud.FTBToastUtil;
import dev.xyat.adventuresystems.ftb.data.BlacklistStoreFTB;
import dev.xyat.adventuresystems.ftb.data.FavoritesStoreFTB;
import dev.xyat.adventuresystems.ftb.data.RefFTB;
import dev.xyat.adventuresystems.ftb.util.BridgeFTB;
import dev.xyat.adventuresystems.ftb.util.HoveredItemFTB;
import dev.xyat.adventuresystems.ftb.util.ResolverFTB;
import dev.xyat.kineticcore.api.client.tooltip.KineticItemTooltips;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import dev.xyat.kineticcore.api.text.KineticI18n;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ClientEventsFTB {
    private static boolean installed;

    private ClientEventsFTB() {
    }

    public static void install() {
        if (installed) return;
        installed = true;
        KineticItemTooltips.onBuild(ClientEventsFTB::onItemTooltip);
    }

    private static void onItemTooltip(ItemStack stack, List<Component> lines) {
        if (!ItemClientModuleFTB.isEnabled()) return;
        if (stack == null || stack.isEmpty()) return;

        HoveredItemFTB.rememberTooltipStack(stack);

        if (FTBClientConfig.shouldSkipTaskJump()) return;
        if (BlacklistStoreFTB.isBlacklisted(stack)) return;

        List<RefFTB> refs = ResolverFTB.findQuestRefs(stack);
        int count = refs.size();
        if (count == 0) return;

        Component singleKeyName = KeyMappingsFTB.OPEN_QUEST.translatedKeyMessage().copy();
        lines.add(KineticI18n.translatable("tip.adventuresystems.ftb.open", singleKeyName));

        if (count > 1) {
            Component multiKeyName = KeyMappingsFTB.OPEN_QUEST_MULTI.translatedKeyMessage().copy();
            Component countText = Component.literal(String.valueOf(count));
            lines.add(KineticI18n.translatable(
                    "tip.adventuresystems.ftb.open.list",
                    multiKeyName,
                    countText
            ));
        }
    }

    public static boolean handleOpenPress(boolean multi) {
        if (!ItemClientModuleFTB.isEnabled()) return false;
        if (FTBClientConfig.shouldSkipTaskJump()) return false;

        Screen screen = KineticClientRuntime.currentScreen();
        if (screen == null) return false;

        ItemStack hovered = HoveredItemFTB.getHoveredStack(screen);
        if (hovered.isEmpty()) return false;
        if (BlacklistStoreFTB.isBlacklisted(hovered)) return false;

        List<RefFTB> refs = ResolverFTB.findQuestRefs(hovered);
        if (refs.isEmpty()) {
            FTBToastUtil.showQuick(
                    "adventuresystems.ftb.no.quest",
                    KineticI18n.translatable("msg.adventuresystems.ftb.no.quest")
            );
            return true;
        }

        if (multi && refs.size() > 1) {
            KineticClientRuntime.openScreen(new SelectScreenFTB(screen, hovered.copy(), refs));
        } else {
            long favId = FavoritesStoreFTB.getFavorite(hovered);
            long targetId = refs.get(0).id();
            for (RefFTB ref : refs) {
                if (ref.id() == favId) {
                    targetId = favId;
                    break;
                }
            }
            BridgeFTB.openQuest(targetId);
        }
        return true;
    }
}
