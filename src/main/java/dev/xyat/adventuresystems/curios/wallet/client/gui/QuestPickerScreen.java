package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.adventuresystems.ftb.util.BridgeFTB;
import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.widget.list.ActionItem;
import dev.xyat.kineticcore.api.client.gui.widget.list.KineticActionList;
import dev.xyat.kineticcore.api.client.search.KineticSearch;
import dev.xyat.adventuresystems.text.AdventureText;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

final class QuestPickerScreen extends KineticPage {
    private final Consumer<Long> callback;
    private final List<QuestOption> all = new ArrayList<>();
    private final List<QuestOption> filtered = new ArrayList<>();
    private KineticActionList list;
    private String searchText = "";
    private int scrollOffset;

    QuestPickerScreen(Consumer<Long> callback) {
        super(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_pick_task_title"));
        this.callback = callback;
        useCanvas(580, 336, 6);
        loadQuests();
        applySearch();
    }

    @Override
    protected void build(KineticUi ui) {
        if (list != null) scrollOffset = list.scrollOffset();
        ui.textField(24, 42, 428)
                .label(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_task_search"))
                .placeholder(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_task_search"))
                .value(searchText)
                .onChange(value -> {
                    searchText = value;
                    scrollOffset = 0;
                    applySearch();
                }).firstShownTextAsDefault().build();
        ui.button(464, 42, 82).text(AdventureText.translatable("gui.done"))
                .onClick(this::navigateBack).build();
        list = ui.actionList(24, 74, 532, 230, listItems())
                .actionWidth(64).scrollOffset(scrollOffset)
                .onSelect(this::select).onAction(this::select).build();
    }

    private void loadQuests() {
        all.clear();
        try {
            for (var ref : BridgeFTB.getAllQuestRefs()) {
                if (ref == null || ref.id() <= 0L) continue;
                all.add(new QuestOption(ref.id(), safe(ref.code()), safe(ref.title()), safe(ref.chapter())));
            }
        } catch (Throwable ignored) {
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private void applySearch() {
        filtered.clear();
        String query = searchText.trim().toLowerCase(Locale.ROOT);
        for (QuestOption option : all) {
            if (KineticSearch.match(option.searchText(), query)) filtered.add(option);
        }
        if (list != null) {
            list.setItems(listItems());
            list.setScrollOffset(scrollOffset);
        }
    }

    private List<ActionItem> listItems() {
        List<ActionItem> items = new ArrayList<>(filtered.size());
        for (QuestOption option : filtered) {
            items.add(new ActionItem(AdventureText.literal(option.displayTitle()),
                    AdventureText.literal(option.displayMeta()),
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_quest_id_scaled", option.id()),
                    true, false,
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_task_pick_tip"),
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_task_pick_tip"),
                    true, false));
        }
        return items;
    }

    private void select(int index) {
        if (index < 0 || index >= filtered.size()) return;
        callback.accept(filtered.get(index).id());
        navigateBack();
    }

    @Override
    protected void renderBackground(KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        KineticTheme.shadow(graphics, width(), height());
        KineticTheme.panel(graphics, 10, 10, 560, 316);
        graphics.scrollingTextCentered(title(), 290, 20, 560 - 28, KineticTheme.current().text(), false);
        if (all.isEmpty()) {
            graphics.scrollingTextCentered(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_task_empty"), 290, 166, 560 - 28, KineticTheme.current().text(), false);
        }
    }

    private record QuestOption(long id, String code, String title, String chapter) {
        String displayTitle() {
            String value = title.isBlank() ? code : title;
            return value.isBlank() ? String.valueOf(id) : value;
        }

        String displayMeta() {
            if (!chapter.isBlank() && !code.isBlank()) return chapter + "  " + code;
            if (!chapter.isBlank()) return chapter;
            if (!code.isBlank()) return code;
            return String.valueOf(id);
        }

        String searchText() {
            return (id + " " + code + " " + title + " " + chapter).toLowerCase(Locale.ROOT);
        }
    }
}
