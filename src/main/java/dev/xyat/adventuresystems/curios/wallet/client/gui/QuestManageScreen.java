package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.widget.list.ActionItem;
import dev.xyat.kineticcore.api.client.gui.widget.list.KineticActionList;
import dev.xyat.kineticcore.api.text.KineticI18n;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class QuestManageScreen extends KineticPage {
    private final ShopGuiSupport.EditorDraft draft;
    private final Map<Long, ShopGuiSupport.QuestDisplay> questDisplays = ShopGuiSupport.loadQuestDisplays();
    private KineticActionList list;
    private int scrollOffset;

    QuestManageScreen(ShopGuiSupport.EditorDraft draft) {
        super(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_title"));
        this.draft = draft;
        useCanvas(480, 320, 6);
    }

    @Override
    protected void build(KineticUi ui) {
        if (list != null) scrollOffset = list.scrollOffset();
        ui.button(38, 274, 116)
                .text(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_add"))
                .onClick(() -> openChild(new QuestPickerScreen(id -> {
                    if (id > 0 && !draft.questIds.contains(id)) draft.questIds.add(id);
                    refreshItems();
                }))).build();
        ui.button(362, 274, 80).text(KineticI18n.translatable("gui.done"))
                .onClick(this::navigateBack).build();
        list = ui.actionList(32, 72, 416, 192, listItems())
                .actionWidth(70).scrollOffset(scrollOffset)
                .onSelect(ignored -> { }).onAction(this::removeQuest).build();
    }

    private List<ActionItem> listItems() {
        List<ActionItem> items = new ArrayList<>(draft.questIds.size());
        for (long questId : draft.questIds) {
            ShopGuiSupport.QuestDisplay display = questDisplays.get(questId);
            String name = display == null ? String.valueOf(questId) : display.displayTitle();
            String meta = display == null
                    ? KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_quest_id_scaled", questId).getString()
                    : display.displayMeta();
            items.add(new ActionItem(Component.literal(name), Component.literal(meta),
                    KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_quest_title_value", name),
                    true, false,
                    KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_remove"),
                    KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_remove_tip"),
                    true, false));
        }
        return items;
    }

    private void removeQuest(int index) {
        if (index < 0 || index >= draft.questIds.size()) return;
        draft.questIds.remove(index);
        refreshItems();
    }

    private void refreshItems() {
        if (list != null) list.setItems(listItems());
    }

    @Override
    protected void renderBackground(KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        KineticTheme.shadow(graphics, width(), height());
        KineticTheme.panel(graphics, 20, 12, 440, 296);
        graphics.centeredText(title(), 240, 20, KineticTheme.current().text(), false);
        graphics.text(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_hint_top"),
                38, 43, KineticTheme.current().mutedText(), true);
        if (draft.questIds.isEmpty()) {
            graphics.centeredText(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_empty"),
                    240, 160, KineticTheme.current().mutedText(), false);
        }
    }
}
