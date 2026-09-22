package dev.xyat.adventuresystems.ftb.client.gui;

import dev.xyat.adventuresystems.ftb.data.FavoritesStoreFTB;
import dev.xyat.adventuresystems.ftb.data.RefFTB;
import dev.xyat.adventuresystems.ftb.util.BridgeFTB;
import dev.xyat.kineticcore.api.client.screen.KineticScreen;
import dev.xyat.kineticcore.api.client.search.KineticSearch;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.widget.input.KineticTextFields.KineticEditBox;
import dev.xyat.kineticcore.api.client.widget.selection.KineticTabs.ActionItem;
import dev.xyat.kineticcore.api.client.widget.selection.KineticTabs.ScrollableActionList;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import dev.xyat.kineticcore.api.text.KineticI18n;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SelectScreenFTB extends KineticScreen {
    private static final int LIST_X = 24;
    private static final int LIST_Y = 88;
    private static final int LIST_W = 792;
    private static final int LIST_H = 330;

    private final Screen parent;
    private final ItemStack stack;
    private final List<RefFTB> allRefs;
    private final List<RefFTB> filtered = new ArrayList<>();
    private KineticEditBox searchBox;
    private ScrollableActionList listWidget;

    public SelectScreenFTB(Screen parent, ItemStack stack, List<RefFTB> refs) {
        super(KineticI18n.translatable("screen.adventuresystems.ftb.select"));
        this.parent = parent;
        setParentScreen(parent);
        this.stack = stack;
        this.allRefs = new ArrayList<>(refs);
        this.filtered.addAll(refs);
        useCanvas(840f, 470f, 6);
    }

    @Override
    protected void buildUi() {
        searchBox = addTextField(
                LIST_X,
                54,
                520,
                KineticI18n.translatable("gui.adventuresystems.ftb.search"),
                KineticI18n.translatable("placeholder.adventuresystems.ftb.select.search"),
                null,
                KineticI18n.translatable("tip.adventuresystems.ftb.search.desc")
        );
        searchBox.setResponder(value -> applySearch());
        addButton(728, 432, 88, KineticI18n.translatable("gui.done"), null, this::onClose);
        listWidget = addScrollableActionList(
                LIST_X,
                LIST_Y,
                LIST_W,
                LIST_H,
                listItems(),
                -1,
                0,
                116,
                this::openQuest,
                this::setFavorite
        );
    }

    private void applySearch() {
        filtered.clear();
        String query = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase();
        for (RefFTB ref : allRefs) {
            if (KineticSearch.match(ref.searchText(), query)) filtered.add(ref);
        }
        if (listWidget != null) listWidget.setItems(listItems());
    }

    private List<ActionItem> listItems() {
        long favoriteId = resolvedFavoriteId();
        List<ActionItem> items = new ArrayList<>(filtered.size());
        for (RefFTB ref : filtered) {
            Component title = KineticI18n.translatable(
                    "label.adventuresystems.ftb.quest.title",
                    Component.literal(ref.title())
            );
            Component secondary = KineticI18n.translatable(
                    "label.adventuresystems.ftb.quest.meta",
                    Component.literal(ref.chapter() + "  ·  " + ref.source())
            );
            items.add(new ActionItem(
                    title,
                    secondary,
                    KineticI18n.translatable("tip.adventuresystems.ftb.list.open"),
                    true,
                    ref.id() == favoriteId,
                    false,
                    KineticI18n.translatable("tip.adventuresystems.ftb.favorite.title"),
                    KineticI18n.translatable("tip.adventuresystems.ftb.favorite.desc"),
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
    protected void renderCanvasBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        GuiTheme.shadow(graphics, canvasWidth(), canvasHeight());
        GuiTheme.panel(graphics, 10, 10, 820, 450);
        graphics.drawString(font, KineticI18n.translatable("screen.adventuresystems.ftb.select"), 24, 24, GuiTheme.current().text(), false);
        graphics.drawString(font, KineticI18n.translatable("label.adventuresystems.ftb.select.subtitle"), 170, 25, GuiTheme.current().mutedText(), false);
        GuiTheme.itemSlot(graphics, 565, 53);
        GuiTheme.item(graphics, font, stack, 565, 53, 18, 1.0F, false);
        graphics.drawString(
                font,
                KineticI18n.translatable("label.adventuresystems.ftb.item.name", stack.getHoverName().copy()),
                588,
                59,
                GuiTheme.current().text(),
                false
        );
    }

    @Override
    protected boolean handleCloseRequest() {
        navigateBack();
        return true;
    }
}
