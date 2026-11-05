package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.adventuresystems.ftb.util.BridgeFTB;
import dev.xyat.adventuresystems.text.AdventureText;
import dev.xyat.kineticcore.api.client.gui.input.MouseInput;
import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.widget.list.KineticRowList;
import dev.xyat.kineticcore.api.client.search.KineticSearch;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The quests a shop entry requires, picked on one page: every quest on the left (search, click to add; the chosen
 * ones are green), the chosen quests on the right (click to remove). Changes go straight into the entry's draft.
 */
final class QuestManageScreen extends KineticPage {
    private static final int ROW_H = 20, SEARCH_W = 200, DIVIDER_X = 400, LIST_Y = 28;
    private final ShopGuiSupport.EditorDraft draft;
    private final Map<Long, Quest> quests = new LinkedHashMap<>();
    private final List<Quest> filtered = new ArrayList<>();
    private String query = "";
    private Rows all, chosen;

    QuestManageScreen(ShopGuiSupport.EditorDraft draft) {
        super(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_title"));
        this.draft = draft;
        useCanvas(640, 360, 4);
        try {
            for (var ref : BridgeFTB.getAllQuestRefs()) {
                if (ref != null && ref.id() > 0L) quests.put(ref.id(), new Quest(ref.id(), safe(ref.code()), safe(ref.title()), safe(ref.chapter())));
            }
        } catch (Throwable ignored) {
            // Without FTB Quests only the stored IDs are shown.
        }
        bindDeleteAction(() -> chosen != null && chosen.selectedIndex() >= 0, () -> remove(chosen.selectedIndex()));
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private Quest quest(long id) {
        Quest known = quests.get(id);
        return known != null ? known : new Quest(id, "", "", "");
    }

    @Override
    protected void build(KineticUi ui) {
        ui.button(4, 4, 40).text(AdventureText.translatable("gui.back")).onClick(this::navigateBack).build();
        ui.textField(48, 4, SEARCH_W).value(query)
                .placeholder(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_task_search"))
                .onChange(value -> { query = value; filter(); }).build();
        all = ui.add(new Rows(4, DIVIDER_X - 4 - 4, false));
        chosen = ui.add(new Rows(DIVIDER_X + 4, 636 - DIVIDER_X - 4, true));
        filter();
        refreshChosen();
    }

    private void filter() {
        filtered.clear();
        String q = query.trim().toLowerCase(Locale.ROOT);
        for (Quest quest : quests.values()) if (KineticSearch.match(quest.searchText(), q)) filtered.add(quest);
        if (all != null) { all.setItems(filtered); all.setScrollOffset(0); }
    }

    private void refreshChosen() {
        if (chosen == null) return;
        List<Quest> rows = new ArrayList<>();
        for (long id : draft.questIds) rows.add(quest(id));
        chosen.setItems(rows);
    }

    private void toggle(Quest quest) {
        if (!draft.questIds.remove(quest.id())) draft.questIds.add(quest.id());
        refreshChosen();
    }

    private void remove(int index) {
        if (index < 0 || index >= draft.questIds.size()) return;
        draft.questIds.remove(index);
        refreshChosen();
    }

    @Override
    protected void renderBackground(KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        KineticTheme.panel(graphics, 0, 0, width(), height());
        KineticTheme.separator(graphics, 4, 24, 632);
        KineticTheme.verticalSeparator(graphics, DIVIDER_X, LIST_Y, 360 - LIST_Y - 4);
        // Centred on the screen; the chosen count follows the title.
        Component heading = title().copy().append(Component.literal("  " + draft.questIds.size()).withStyle(ChatFormatting.GREEN));
        graphics.scrollingTextCentered(heading, 320, 8, 2 * (320 - 48 - SEARCH_W - 4), KineticTheme.current().text(), false);
    }

    /** One quest per row: title (green when chosen), chapter muted, quest code grey at the right. */
    private final class Rows extends KineticRowList<Quest> {
        private final boolean chosenList;

        Rows(int x, int width, boolean chosenList) {
            super(x, LIST_Y, width, 360 - LIST_Y - 4, ROW_H);
            this.chosenList = chosenList;
        }

        @Override
        protected void renderRow(KineticGraphics g, Quest quest, int index, int x, int y, int width, int height, boolean hovered, boolean selected) {
            boolean picked = draft.questIds.contains(quest.id());
            int textY = y + (height - 8) / 2;
            String code = quest.code().isBlank() ? String.valueOf(quest.id()) : quest.code();
            int codeW = chosenList ? 0 : g.textWidth(code);
            if (!chosenList) g.text(code, x + width - 4 - codeW, textY, KineticTheme.current().mutedText(), false);
            Component label = Component.literal(quest.displayTitle()).withStyle(picked && !chosenList ? ChatFormatting.GREEN : ChatFormatting.WHITE);
            if (!quest.chapter().isBlank()) label = label.copy().append(Component.literal("  " + quest.chapter()).withStyle(ChatFormatting.GRAY));
            g.scrollingText(label, x + 4, textY, width - 8 - (codeW == 0 ? 0 : codeW + 6), 0xFFFFFFFF, false);
        }

        @Override
        protected boolean onRowClick(Quest quest, int index, MouseInput input) {
            if (!input.isLeft()) return false;
            if (chosenList) remove(index); else { toggle(quest); }
            playClickSound();
            return true;
        }

        @Override
        protected Component rowTooltip(Quest quest, int index) {
            Component action = AdventureText.translatable(chosenList || draft.questIds.contains(quest.id())
                    ? "gui.adventuresystems.curios.wallet.shop_quest_click_remove" : "gui.adventuresystems.curios.wallet.shop_quest_click_add");
            return Component.literal(quest.displayTitle()).append("\n")
                    .append(Component.literal(quest.chapter().isBlank() ? String.valueOf(quest.id()) : quest.chapter() + "  " + quest.code()).withStyle(ChatFormatting.GRAY))
                    .append("\n").append(action);
        }

        @Override
        protected Component emptyText() {
            return AdventureText.translatable(chosenList ? "gui.adventuresystems.curios.wallet.shop_quest_manage_empty"
                    : "gui.adventuresystems.curios.wallet.shop_task_empty");
        }
    }

    private record Quest(long id, String code, String title, String chapter) {
        String displayTitle() {
            String value = title.isBlank() ? code : title;
            return value.isBlank() ? String.valueOf(id) : value;
        }

        String searchText() {
            return (id + " " + code + " " + title + " " + chapter).toLowerCase(Locale.ROOT);
        }
    }
}
