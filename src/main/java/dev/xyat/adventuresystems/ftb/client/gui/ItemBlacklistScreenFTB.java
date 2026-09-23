package dev.xyat.adventuresystems.ftb.client.gui;

import dev.xyat.adventuresystems.ftb.client.hud.FTBToastUtil;
import dev.xyat.adventuresystems.ftb.data.BindingStoreFTB;
import dev.xyat.adventuresystems.ftb.data.BlacklistStoreFTB;
import dev.xyat.kineticcore.api.client.screen.KineticScreen;
import dev.xyat.kineticcore.api.client.search.KineticSearch;
import dev.xyat.kineticcore.api.client.selector.KineticSelectors;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.widget.input.KineticTextFields.KineticEditBox;
import dev.xyat.kineticcore.api.client.widget.selection.KineticTabs.ItemActionItem;
import dev.xyat.kineticcore.api.client.widget.selection.KineticTabs.ScrollableItemActionList;
import dev.xyat.kineticcore.api.text.KineticI18n;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ItemBlacklistScreenFTB extends KineticScreen {
    private static final int PANEL_X = 10;
    private static final int PANEL_Y = 10;
    private static final int PANEL_W = 600;
    private static final int PANEL_H = 400;
    private static final int LIST_X = 24;
    private static final int LIST_Y = 88;
    private static final int LIST_W = 572;
    private static final int LIST_H = 300;

    private final Screen parent;
    private final List<String> allEntries = new ArrayList<>();
    private final List<String> filtered = new ArrayList<>();
    private KineticEditBox searchBox;
    private ScrollableItemActionList listWidget;

    public ItemBlacklistScreenFTB(Screen parent) {
        super(KineticI18n.translatable("screen.adventuresystems.ftb_item.blacklist"));
        this.parent = parent;
        setParentScreen(parent);
        useCanvas(620f, 420f, 6);
        configureStandaloneDraft(BlacklistStoreFTB::getAll, BlacklistStoreFTB::replaceAll);
    }

    @Override
    protected void buildUi() {
        reloadEntries();
        searchBox = addTextField(
                LIST_X + 110,
                54,
                350,
                KineticI18n.translatable("gui.adventuresystems.ftb.search"),
                KineticI18n.translatable("placeholder.adventuresystems.ftb_item.binding_search"),
                null,
                null
        );
        searchBox.setResponder(value -> applySearch());
        addButton(LIST_X, 54, 100, KineticI18n.translatable("button.adventuresystems.ftb_item.add_blacklist"), null, this::openSelector);
        addButton(LIST_X + 470, 54, 100, KineticI18n.translatable("button.adventuresystems.ftb.save"), null, this::save);
        listWidget = addScrollableItemActionList(
                LIST_X,
                LIST_Y,
                LIST_W,
                LIST_H,
                listItems(),
                -1,
                0,
                72,
                ignored -> { },
                this::removeEntry
        );
    }

    private void reloadEntries() {
        allEntries.clear();
        allEntries.addAll(BlacklistStoreFTB.getAll());
        applySearch();
    }

    private void applySearch() {
        filtered.clear();
        String query = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase();
        for (String entry : allEntries) {
            if (query.isEmpty() || KineticSearch.match(entry.toLowerCase(), query)) filtered.add(entry);
        }
        if (listWidget != null) listWidget.setItems(listItems());
    }

    private List<ItemActionItem> listItems() {
        List<ItemActionItem> items = new ArrayList<>(filtered.size());
        for (String entry : filtered) {
            ItemStack stack = BindingStoreFTB.createDisplayStack(entry, "");
            items.add(new ItemActionItem(
                    stack,
                    Component.literal(entry),
                    null,
                    Component.literal(entry),
                    true,
                    false,
                    false,
                    KineticI18n.translatable("button.adventuresystems.ftb_item.delete"),
                    KineticI18n.translatable("tip.adventuresystems.ftb.blacklist.remove"),
                    true
            ));
        }
        return items;
    }

    private void openSelector() {
        KineticSelectors.openItemSelector(this, selection -> {
            String target = "";
            if (selection.isMod()) target = "@" + selection.value();
            else if (selection.isTag()) target = "#" + selection.value();
            else if (selection.isItem()) target = BindingStoreFTB.itemKey(selection.stack());
            if (target.isEmpty()) return;
            BlacklistStoreFTB.add(target);
            reloadEntries();
            FTBToastUtil.show("adventuresystems_blacklist_added", KineticI18n.translatable("msg.adventuresystems.ftb_item.blacklist_added"));
        });
    }

    private void removeEntry(int index) {
        if (index < 0 || index >= filtered.size()) return;
        BlacklistStoreFTB.remove(filtered.get(index));
        reloadEntries();
        FTBToastUtil.show("adventuresystems_blacklist_removed", KineticI18n.translatable("msg.adventuresystems.ftb_item.blacklist_removed"));
    }

    @Override
    protected void renderCanvasBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        GuiTheme.shadow(graphics, canvasWidth(), canvasHeight());
        GuiTheme.panel(graphics, PANEL_X, PANEL_Y, PANEL_W, PANEL_H);
        graphics.drawString(font, KineticI18n.translatable("screen.adventuresystems.ftb_item.blacklist"), 24, 24, GuiTheme.current().text(), false);
        graphics.drawString(font, KineticI18n.translatable("label.adventuresystems.ftb_item.blacklist_desc"), 150, 25, GuiTheme.current().mutedText(), false);
        graphics.drawString(
                font,
                KineticI18n.translatable(
                        "label.adventuresystems.ftb_item.blacklist_count",
                        Component.literal(String.valueOf(filtered.size()))
                ),
                480,
                25,
                GuiTheme.current().text(),
                false
        );
    }

    private void save() {
        commitDraft();
    }

    @Override
    protected boolean handleCloseRequest() {
        navigateBack();
        return true;
    }

    public Screen getParent() {
        return parent;
    }
}
