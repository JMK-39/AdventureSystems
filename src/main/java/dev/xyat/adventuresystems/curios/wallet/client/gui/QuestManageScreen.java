package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.widget.list.ActionItem;
import dev.xyat.kineticcore.api.client.gui.widget.list.KineticActionList;
import dev.xyat.adventuresystems.text.AdventureText;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class QuestManageScreen extends KineticPage {
    private final ShopGuiSupport.EditorDraft draft;
    private final Map<Long, ShopGuiSupport.QuestDisplay> questDisplays = ShopGuiSupport.loadQuestDisplays();
    private KineticActionList list;
    private int scrollOffset;
    // The list is as tall as its quests need (3 to 8 rows) and the panel sits in the middle of the canvas.
    private int top;
    private int listHeight;

    QuestManageScreen(ShopGuiSupport.EditorDraft draft) {
        super(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_title"));
        this.draft = draft;
        useCanvas(480, 320, 6);
    }

    @Override
    protected void build(KineticUi ui) {
        if (list != null) scrollOffset = list.scrollOffset();
        listHeight = Math.max(3, Math.min(8, draft.questIds.size())) * 21 + 24;
        top = Math.max(0, (height() - (listHeight + 104)) / 2);
        int buttonY = top + 60 + listHeight + 10;
        ui.button(38, buttonY, 116)
                .text(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_add"))
                .onClick(() -> openChild(new QuestPickerScreen(id -> {
                    if (id > 0 && !draft.questIds.contains(id)) draft.questIds.add(id);
                    refreshItems();
                }))).build();
        ui.button(362, buttonY, 80).text(AdventureText.translatable("gui.done"))
                .onClick(this::navigateBack).build();
        list = ui.actionList(32, top + 60, 416, listHeight, listItems())
                .actionWidth(70).scrollOffset(scrollOffset)
                .onSelect(ignored -> { }).onAction(this::removeQuest).build();
    }

    private List<ActionItem> listItems() {
        List<ActionItem> items = new ArrayList<>(draft.questIds.size());
        for (long questId : draft.questIds) {
            ShopGuiSupport.QuestDisplay display = questDisplays.get(questId);
            String name = display == null ? String.valueOf(questId) : display.displayTitle();
            Component meta = display == null
                    ? AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_quest_id_scaled", questId)
                    : AdventureText.literal(display.displayMeta());
            items.add(new ActionItem(AdventureText.literal(name), meta,
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_quest_title_value", name),
                    true, false,
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_remove"),
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_remove_tip"),
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
        if (list == null) return;
        if (isAttached() && Math.max(3, Math.min(8, draft.questIds.size())) * 21 + 24 != listHeight) rebuild();
        else list.setItems(listItems());
    }

    @Override
    protected void renderBackground(KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        KineticTheme.shadow(graphics, width(), height());
        KineticTheme.panel(graphics, 20, top, 440, listHeight + 104);
        graphics.scrollingTextCentered(title(), 240, top + 8, 440 - 36, KineticTheme.current().text(), false);
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_hint_top"), 38, top + 31, 440 - 36, KineticTheme.current().text(), true);
        if (draft.questIds.isEmpty()) {
            graphics.scrollingTextCentered(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_empty"), 240, top + 60 + (listHeight - 8) / 2, 440 - 36, KineticTheme.current().text(), false);
        }
    }
}
