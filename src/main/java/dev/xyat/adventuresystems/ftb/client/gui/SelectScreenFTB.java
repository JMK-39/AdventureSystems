package dev.xyat.adventuresystems.ftb.client.gui;

import dev.xyat.adventuresystems.ftb.data.FavoritesStoreFTB;
import dev.xyat.adventuresystems.ftb.data.RefFTB;
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
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SelectScreenFTB extends KineticPage {
    private static final int LIST_X = 4;
    private static final int LIST_Y = 28;
    private static final int LIST_W = 632;
    private static final int LIST_H = 328;

    private final ItemStack stack;
    private final List<RefFTB> allRefs;
    private final List<RefFTB> filtered = new ArrayList<>();
    private KineticActionList listWidget;
    private String searchText = "";
    private int listScroll;

    public SelectScreenFTB(ItemStack stack, List<RefFTB> refs) {
        super(AdventureText.translatable("screen.adventuresystems.ftb.select"));
        this.stack = stack;
        this.allRefs = new ArrayList<>(refs);
        this.filtered.addAll(refs);
        useCanvas(640, 360, 4);
    }

    @Override
    protected void build(KineticUi ui) {
        if (listWidget != null) listScroll = listWidget.scrollOffset();
        listWidget = null;
        applySearch();
        ui.textField(48, 4, 400)
                .label(AdventureText.translatable("gui.adventuresystems.ftb.search"))
                .placeholder(AdventureText.translatable("placeholder.adventuresystems.ftb.select.search"))
                .tooltip(AdventureText.translatable("tip.adventuresystems.ftb.search.desc"))
                .value(searchText)
                .onChange(value -> {
                    searchText = value;
                    applySearch();
                }).firstShownTextAsDefault().build();
        ui.button(4, 4, 40).text(AdventureText.translatable("gui.back"))
                .onClick(this::close).build();
        listWidget = ui.actionList(LIST_X, LIST_Y, LIST_W, LIST_H, listItems())
                .selected(-1).scrollOffset(listScroll).actionWidth(116)
                .onSelect(this::openQuest).onAction(this::setFavorite).build();
    }

    private void applySearch() {
        filtered.clear();
        String query = searchText.trim().toLowerCase();
        for (RefFTB ref : allRefs) {
            if (KineticSearch.match(ref.searchText(), query)) filtered.add(ref);
        }
        if (listWidget != null) listWidget.setItems(listItems());
    }

    private List<ActionItem> listItems() {
        List<ActionItem> items = new ArrayList<>(filtered.size());
        for (RefFTB ref : filtered) {
            Component title = AdventureText.translatable(
                    "label.adventuresystems.ftb.quest.title",
                    AdventureText.literal(ref.title())
            );
            Component secondary = AdventureText.translatable(
                    "label.adventuresystems.ftb.quest.meta",
                    AdventureText.literal(ref.chapter() + "  ·  " + ref.source())
            );
            items.add(new ActionItem(
                    title,
                    secondary,
                    AdventureText.translatable("tip.adventuresystems.ftb.list.open"),
                    true,
                    false,
                    AdventureText.translatable("tip.adventuresystems.ftb.favorite.title"),
                    AdventureText.translatable("tip.adventuresystems.ftb.favorite.desc"),
                    true,
                    false
            ));
        }
        return items;
    }

    private long resolvedFavoriteId() {
        long favoriteId = FavoritesStoreFTB.getFavorite(stack);
        if (allRefs.stream().anyMatch(ref -> ref.id() == favoriteId)) return favoriteId;
        return allRefs.isEmpty() ? favoriteId : allRefs.get(0).id();
    }

    private void openQuest(int index) {
        if (index < 0 || index >= filtered.size()) return;
        RefFTB ref = filtered.get(index);
        navigateBack();
        BridgeFTB.openQuest(ref.id());
    }

    private void setFavorite(int index) {
        if (index < 0 || index >= filtered.size()) return;
        FavoritesStoreFTB.setFavorite(stack, filtered.get(index).id());
        if (listWidget != null) listWidget.setItems(listItems());
    }

    @Override
    protected void renderBackground(KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        KineticTheme.shadow(graphics, width(), height());
        KineticTheme.panel(graphics, 0, 0, width(), height());
        KineticTheme.itemSlot(graphics, 452, 4);
        KineticTheme.item(graphics, stack, 452, 4, 18, 0.75F, false);
        graphics.scrollingText(AdventureText.translatable("label.adventuresystems.ftb.item.name", stack.getHoverName().copy()), 474, 8, 636 - 474, KineticTheme.current().text(), false);
    }

    @Override
    protected void renderTooltips(int mouseX, int mouseY) {
        if (!stack.isEmpty() && KineticTheme.hovering(mouseX, mouseY, 452, 4, 18, 18)) showItemTooltip(stack);
    }

    @Override
    protected boolean onCloseRequested() {
        navigateBack();
        return true;
    }
}
