package dev.xyat.adventuresystems.ftb.client.gui;

import dev.xyat.kineticcore.api.client.gui.input.MouseDragInput;
import dev.xyat.kineticcore.api.client.gui.input.MouseInput;
import dev.xyat.kineticcore.api.client.gui.input.ScrollInput;
import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.scroll.KineticScrollController;
import dev.xyat.kineticcore.api.client.gui.selector.KineticSelectors;
import dev.xyat.kineticcore.api.client.gui.theme.KineticSizes;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.widget.KineticButton;
import dev.xyat.kineticcore.api.client.gui.widget.list.KineticRowList;
import dev.xyat.kineticcore.api.client.search.KineticSearch;
import dev.xyat.kineticcore.api.config.client.KTConfigApi;
import dev.xyat.adventuresystems.text.AdventureText;
import dev.xyat.adventuresystems.ftb.client.hud.FTBToastUtil;
import dev.xyat.adventuresystems.ftb.client.FTBConfigGui;
import dev.xyat.adventuresystems.ftb.data.BindingStoreFTB;
import dev.xyat.adventuresystems.ftb.data.FavoritesStoreFTB;
import dev.xyat.adventuresystems.ftb.data.ItemBindingEntryFTB;
import dev.xyat.adventuresystems.ftb.data.RefFTB;
import dev.xyat.adventuresystems.ftb.util.BridgeFTB;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Binds FTB quests to an item. Left: every quest (live search, click to bind or unbind; bound ones are green).
 * Right: the current item, the items that already have bindings (click one to edit it) and the current item's
 * bound quests (click removes, right-click makes it the default one, marked with a star).
 */
public class FTBItemBindingEditorScreen extends KineticPage {
    private static final int CANVAS_W = 640, CANVAS_H = 360;
    private static final int LIST_Y = 28, ROW_H = 20, SEARCH_W = 200;
    private static final int DIVIDER_X = 330, RIGHT_X = DIVIDER_X + 4, RIGHT_W = CANVAS_W - 4 - RIGHT_X;
    private static final int ITEM_SLOT = KineticSizes.ITEM_SLOT, ITEM_PITCH = KineticSizes.ITEM_PITCH;
    private static final int SCROLL_W = 4;
    // Current item row, then the bound items grid (two rows), then the bound quests list down to the bottom.
    private static final int GRID_LABEL_Y = LIST_Y + ITEM_SLOT + 6, GRID_Y = GRID_LABEL_Y + 11, GRID_ROWS = 2;
    private static final int GRID_COLS = (RIGHT_W - SCROLL_W - 2) / ITEM_PITCH;
    private static final int BOUND_LABEL_Y = GRID_Y + GRID_ROWS * ITEM_PITCH + 4, BOUND_Y = BOUND_LABEL_Y + 11;

    private final List<RefFTB> allTasks = new ArrayList<>();
    private final List<RefFTB> visibleTasks = new ArrayList<>();
    private final List<RefFTB> boundTasks = new ArrayList<>();
    private final List<ItemBindingEntryFTB> explicitEntries = new ArrayList<>();
    private final LinkedHashSet<Long> selectedQuestIds = new LinkedHashSet<>();
    private final Map<Long, RefFTB> taskById = new LinkedHashMap<>();

    private String searchText = "";
    private ItemStack selectedStack = ItemStack.EMPTY;

    private final KineticScrollController itemScroll = new KineticScrollController();
    private TaskRows taskRows;
    private TaskRows boundRows;
    private boolean dirty;
    private long favoriteQuestId;
    private RefFTB selectedTaskForDelete;
    private KineticButton saveButton;

    public FTBItemBindingEditorScreen() {
        super(AdventureText.translatable("screen.adventuresystems.ftb.editor"));
        useCanvas(CANVAS_W, CANVAS_H, 4);
        bindDeleteAction(() -> selectedTaskForDelete != null && selectedQuestIds.contains(selectedTaskForDelete.id()),
                () -> removeTask(selectedTaskForDelete));
        configureStandaloneDraft(this::captureBindingSnapshot, this::restoreBindingSnapshot);
    }

    private record BindingSnapshot(String stackKey, List<Long> questIds, long favoriteQuestId, boolean dirty) {
    }

    private BindingSnapshot captureBindingSnapshot() {
        return new BindingSnapshot(
                BindingStoreFTB.cacheKeyForStack(selectedStack),
                new ArrayList<>(selectedQuestIds),
                favoriteQuestId,
                dirty
        );
    }

    private void restoreBindingSnapshot(BindingSnapshot snapshot) {
        if (snapshot == null) return;
        selectedStack = stackFromCacheKey(snapshot.stackKey());
        selectedQuestIds.clear();
        selectedQuestIds.addAll(snapshot.questIds());
        favoriteQuestId = snapshot.favoriteQuestId();
        dirty = snapshot.dirty();
        refreshBoundRefs();
        updateSaveButton();
    }

    private ItemStack stackFromCacheKey(String key) {
        if (key == null || key.isBlank()) return ItemStack.EMPTY;
        String[] parts = key.split("\\|nbt:", 2);
        return BindingStoreFTB.createDisplayStack(parts[0], parts.length > 1 ? parts[1] : "");
    }

    @Override
    protected void build(KineticUi ui) {
        this.allTasks.clear();
        this.taskById.clear();
        for (RefFTB ref : BridgeFTB.getAllQuestRefs()) {
            this.allTasks.add(ref);
            this.taskById.put(ref.id(), ref);
        }
        reloadExplicitEntries();

        int saveX = CANVAS_W - 4 - 40;
        int clearX = saveX - 4 - 56;
        int blacklistX = clearX - 4 - 48;
        ui.button(4, 4, 40).text(AdventureText.translatable("gui.back")).onClick(this::close).build();
        ui.textField(48, 4, SEARCH_W)
                .placeholder(AdventureText.translatable("placeholder.adventuresystems.ftb.task.search"))
                .value(searchText).maxLength(128)
                .onChange(value -> {
                    searchText = value;
                    refreshTaskFilter(value);
                    if (taskRows != null) taskRows.setScrollOffset(0);
                }).build();
        ui.button(blacklistX, 4, 48).text(AdventureText.translatable("button.adventuresystems.ftb.blacklist"))
                .onClick(() -> openChild(new FTBBlacklistScreen())).build();
        ui.button(clearX, 4, 56).text(AdventureText.translatable("button.adventuresystems.ftb.clear"))
                .onClick(this::clearSelectedBinding).build();
        this.saveButton = ui.button(saveX, 4, 40).text(AdventureText.translatable("button.adventuresystems.ftb.save"))
                .onClick(this::saveCurrentBinding).build();

        this.taskRows = ui.add(new TaskRows(4, LIST_Y, DIVIDER_X - 8, CANVAS_H - 4 - LIST_Y, false));
        this.boundRows = ui.add(new TaskRows(RIGHT_X, BOUND_Y, RIGHT_W, CANVAS_H - 4 - BOUND_Y, true));

        updateItemScroll();
        refreshTaskFilter(searchText);
        refreshBoundRefs();
        updateSaveButton();
    }

    private void reloadExplicitEntries() {
        explicitEntries.clear();
        for (ItemBindingEntryFTB entry : BindingStoreFTB.getAllEntries()) {
            if (entry == null || BindingStoreFTB.isTagTarget(entry.itemId)) continue;
            ItemStack stack = entry.createDisplayStack();
            if (!stack.isEmpty()) {
                explicitEntries.add(BindingStoreFTB.copyEntry(entry));
            }
        }
        updateItemScroll();
    }


    private void updateItemScroll() {
        int rows = explicitEntries.isEmpty() ? 0 : (int) Math.ceil((double) explicitEntries.size() / GRID_COLS);
        itemScroll.update(rows, GRID_ROWS);
    }

    private void refreshTaskFilter(String raw) {
        String query = raw == null ? "" : raw.toLowerCase(Locale.ROOT).trim();
        visibleTasks.clear();
        if (query.isEmpty()) {
            visibleTasks.addAll(allTasks);
        } else {
            for (RefFTB ref : allTasks) {
                if (KineticSearch.match(ref.searchText(), query)) {
                    visibleTasks.add(ref);
                }
            }
        }
        refreshRows();
    }

    private void refreshBoundRefs() {
        boundTasks.clear();
        for (Long id : selectedQuestIds) {
            RefFTB ref = taskById.get(id);
            if (ref == null) {
                ref = BridgeFTB.getQuestRef(id, "KT绑定");
            }
            if (ref != null) {
                boundTasks.add(ref);
            }
        }
        refreshRows();
    }

    /** Pushes the task lists into the two row lists. */
    private void refreshRows() {
        if (taskRows != null) taskRows.setItems(visibleTasks);
        if (boundRows != null) boundRows.setItems(boundTasks);
    }

    private void openItemSelector() {
        KineticSelectors.openItemSelector(selection -> {
            if (selection != null && selection.isItem()) {
                selectStack(selection.stack());
            } else {
                FTBToastUtil.showQuick("adventuresystems_binding_item_only", AdventureText.translatable("msg.adventuresystems.ftb.item.only"));
            }
        });
    }

    private void selectStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        selectedStack = stack.copy();
        selectedQuestIds.clear();
        selectedQuestIds.addAll(BindingStoreFTB.getExactQuestIds(selectedStack));
        favoriteQuestId = FavoritesStoreFTB.getFavorite(selectedStack);
        ensureFavoriteValid();
        dirty = false;
        refreshBoundRefs();
        updateSaveButton();
    }

    private void selectEntry(ItemBindingEntryFTB entry) {
        if (entry == null) return;
        ItemStack stack = entry.createDisplayStack();
        if (stack.isEmpty()) return;
        selectedStack = stack;
        selectedQuestIds.clear();
        if (entry.questIds != null) {
            selectedQuestIds.addAll(entry.questIds);
        }
        favoriteQuestId = FavoritesStoreFTB.getFavorite(selectedStack);
        ensureFavoriteValid();
        dirty = false;
        refreshBoundRefs();
        updateSaveButton();
    }

    private void addTask(RefFTB ref) {
        if (ref == null) return;
        if (selectedStack.isEmpty()) {
            FTBToastUtil.showQuick("adventuresystems_binding_select_item_first", AdventureText.translatable("msg.adventuresystems.ftb.item.first"));
            return;
        }
        if (selectedQuestIds.add(ref.id())) {
            if (selectedQuestIds.size() == 1 || favoriteQuestId == 0L || !selectedQuestIds.contains(favoriteQuestId)) {
                favoriteQuestId = ref.id();
            }
            dirty = true;
            refreshBoundRefs();
            updateSaveButton();
        }
    }

    private void toggleTaskFromLeft(RefFTB ref) {
        if (ref == null) return;
        selectedTaskForDelete = ref;
        if (selectedQuestIds.contains(ref.id())) {
            removeTask(ref);
        } else {
            addTask(ref);
        }
    }

    private void removeTask(RefFTB ref) {
        if (ref == null || selectedStack.isEmpty()) return;
        if (selectedQuestIds.remove(ref.id())) {
            if (favoriteQuestId == ref.id()) {
                favoriteQuestId = firstSelectedQuestId();
            }
            dirty = true;
            refreshBoundRefs();
            updateSaveButton();
        }
    }

    private void setFavoriteTask(RefFTB ref) {
        if (ref == null) return;
        if (selectedStack.isEmpty()) {
            FTBToastUtil.showQuick("adventuresystems_binding_select_item_first", AdventureText.translatable("msg.adventuresystems.ftb.item.first"));
            return;
        }
        boolean changed = selectedQuestIds.add(ref.id());
        if (favoriteQuestId != ref.id()) {
            favoriteQuestId = ref.id();
            changed = true;
        }
        if (changed) {
            dirty = true;
            refreshBoundRefs();
            updateSaveButton();
        }
    }

    private void ensureFavoriteValid() {
        if (favoriteQuestId != 0L && !selectedQuestIds.contains(favoriteQuestId)) {
            favoriteQuestId = firstSelectedQuestId();
        }
    }

    private long firstSelectedQuestId() {
        for (Long id : selectedQuestIds) {
            return id == null ? 0L : id;
        }
        return 0L;
    }

    private void clearSelectedBinding() {
        if (selectedStack.isEmpty()) {
            FTBToastUtil.showQuick("adventuresystems_binding_select_item_first", AdventureText.translatable("msg.adventuresystems.ftb.item.first"));
            return;
        }
        if (!selectedQuestIds.isEmpty()) {
            selectedQuestIds.clear();
            favoriteQuestId = 0L;
            dirty = true;
            refreshBoundRefs();
            updateSaveButton();
        }
    }

    private void saveCurrentBinding() {
        if (selectedStack.isEmpty()) {
            FTBToastUtil.showQuick("adventuresystems_binding_select_item_first", AdventureText.translatable("msg.adventuresystems.ftb.item.first"));
            return;
        }

        BindingStoreFTB.SaveResult result = BindingStoreFTB.saveExactQuestIds(selectedStack, selectedQuestIds);
        if (result == BindingStoreFTB.SaveResult.OK) {
            ensureFavoriteValid();
            FavoritesStoreFTB.setFavorite(selectedStack, selectedQuestIds.isEmpty() ? 0L : favoriteQuestId);
            dirty = false;
            reloadExplicitEntries();
            commitDraft();
            refreshBoundRefs();
            updateSaveButton();
            KTConfigApi.notifySaved(FTBConfigGui.PAGE_ID);
        } else {
            updateSaveButton();
            FTBToastUtil.showQuick("adventuresystems_binding_failed", AdventureText.translatable("msg.adventuresystems.ftb.failed"));
        }
    }

    private void updateSaveButton() {
        if (saveButton == null) return;
        saveButton.setControlVisible(!selectedStack.isEmpty());
        saveButton.setEnabled(!selectedStack.isEmpty() && dirty);
    }

    @Override
    protected void renderBackground(KineticGraphics g, int mx, int my, float pt) {
        KineticTheme.shadow(g, width(), height());
        KineticTheme.panel(g, 0, 0, width(), height());
        KineticTheme.separator(g, 4, 24, CANVAS_W - 8);
        KineticTheme.verticalSeparator(g, DIVIDER_X, LIST_Y, CANVAS_H - 4 - LIST_Y);
        // Centred on the screen, clear of the search field and the header buttons.
        int half = Math.min(CANVAS_W / 2 - (48 + SEARCH_W + 4), CANVAS_W - 4 - 40 - 4 - 56 - 4 - 48 - 4 - CANVAS_W / 2);
        g.scrollingTextCentered(title(), CANVAS_W / 2, 8, Math.max(0, half * 2), KineticTheme.current().text(), true);
    }

    @Override
    protected void renderForeground(KineticGraphics g, int mx, int my, float pt) {
        int textX = RIGHT_X + ITEM_SLOT + 6;
        KineticTheme.itemSlot(g, RIGHT_X, LIST_Y, ITEM_SLOT, isInside(mx, my, RIGHT_X, LIST_Y, ITEM_SLOT, ITEM_SLOT));
        if (selectedStack.isEmpty()) {
            g.scrollingText(AdventureText.translatable("label.adventuresystems.ftb.item.pick"), textX, LIST_Y + 7,
                    RIGHT_W - ITEM_SLOT - 6, KineticTheme.current().mutedText(), false);
        } else {
            KineticTheme.item(g, selectedStack, RIGHT_X, LIST_Y, ITEM_SLOT, 1.0F, false);
            Component name = selectedStack.getHoverName().copy().withStyle(ChatFormatting.GOLD);
            g.scrollingText(name, textX, LIST_Y + 7, RIGHT_W - ITEM_SLOT - 6, KineticTheme.current().text(), false);
        }

        g.scrollingText(AdventureText.translatable("label.adventuresystems.ftb.custom.items", number(explicitEntries.size())),
                RIGHT_X, GRID_LABEL_Y, RIGHT_W, KineticTheme.current().text(), false);
        renderExplicitItemGrid(g, mx, my);
        g.scrollingText(AdventureText.translatable("label.adventuresystems.ftb.bound.tasks", number(boundTasks.size())),
                RIGHT_X, BOUND_LABEL_Y, RIGHT_W, KineticTheme.current().text(), false);
    }

    private void renderExplicitItemGrid(KineticGraphics g, int mx, int my) {
        int gridH = GRID_ROWS * ITEM_PITCH;
        if (explicitEntries.isEmpty()) {
            g.text(AdventureText.translatable("label.adventuresystems.ftb.custom.items.empty"), RIGHT_X, GRID_Y + (gridH - 8) / 2,
                    KineticTheme.current().mutedText(), false);
            return;
        }
        double smoothItemScroll = itemScroll.smoothOffset();
        int firstRow = (int) Math.floor(smoothItemScroll + 1.0E-6D);
        int shift = (int) Math.round((smoothItemScroll - firstRow) * ITEM_PITCH);
        int start = firstRow * GRID_COLS;
        int end = Math.min(explicitEntries.size(), start + (GRID_ROWS + 1) * GRID_COLS);
        String selectedKey = selectedStack.isEmpty() ? "" : BindingStoreFTB.exactKeyForStack(selectedStack);
        g.scissor(RIGHT_X, GRID_Y, RIGHT_X + GRID_COLS * ITEM_PITCH, GRID_Y + gridH);
        g.batchItems(() -> {
            for (int i = start; i < end; i++) {
                ItemBindingEntryFTB entry = explicitEntries.get(i);
                int x = RIGHT_X + ((i - start) % GRID_COLS) * ITEM_PITCH;
                int y = GRID_Y + ((i - start) / GRID_COLS) * ITEM_PITCH - shift;
                boolean hovered = isInside(mx, my, x, y, ITEM_SLOT, ITEM_SLOT) && isInside(mx, my, RIGHT_X, GRID_Y, GRID_COLS * ITEM_PITCH, gridH);
                KineticTheme.itemSlot(g, x, y, ITEM_SLOT, hovered);
                KineticTheme.item(g, entry.createDisplayStack(), x, y, ITEM_SLOT, 1.0F, false);
                // The item being edited is framed in green.
                if (entry.key.equals(selectedKey)) KineticTheme.indicatorOutline(g, x, y, ITEM_SLOT, ITEM_SLOT, KineticTheme.Indicator.SUCCESS);
            }
        });
        g.endScissor();
        if (itemScroll.canScroll()) {
            itemScroll.render(g, mx, my, gridScrollX(), GRID_Y, SCROLL_W, gridH, 12);
        }
    }

    private static int gridScrollX() {
        return RIGHT_X + RIGHT_W - SCROLL_W;
    }

    private String cleanTaskTitle(RefFTB ref) {
        String title = ref == null ? "" : cleanFtbText(ref.title());
        return title.isBlank() ? AdventureText.translatable("label.adventuresystems.ftb.task.unnamed").getString() : title;
    }

    private static Component number(long value) {
        return Component.literal(String.valueOf(value));
    }

    private String cleanFtbText(String value) {
        if (value == null || value.isBlank()) return "";
        return value.replaceAll("(?i)[§&][0-9A-FK-OR]", "").trim();
    }

    @Override
    protected void renderTooltips(int mx, int my) {
        if (isInside(mx, my, RIGHT_X, LIST_Y, ITEM_SLOT, ITEM_SLOT)) {
            Component hint = AdventureText.translatable("button.adventuresystems.ftb.item.select");
            if (selectedStack.isEmpty()) showTooltip(hint);
            else showItemTooltip(selectedStack, List.of(hint));
            return;
        }
        ItemBindingEntryFTB item = itemEntryAt(mx, my);
        if (item != null && !item.createDisplayStack().isEmpty()) showItemTooltip(item.createDisplayStack());
    }

    @Override
    protected boolean onMouseClick(MouseInput input) {
        if (!input.isLeft()) return false;
        int gridH = GRID_ROWS * ITEM_PITCH;
        if (itemScroll.canScroll() && itemScroll.beginDrag(input.x(), input.y(), input.button(), gridScrollX(), GRID_Y, SCROLL_W, gridH, 12)) return true;
        if (isInside(input.x(), input.y(), RIGHT_X, LIST_Y, ITEM_SLOT, ITEM_SLOT)) {
            openItemSelector();
            return true;
        }
        ItemBindingEntryFTB entry = itemEntryAt((int) input.x(), (int) input.y());
        if (entry != null) {
            selectEntry(entry);
            return true;
        }
        return false;
    }

    @Override
    protected boolean onMouseRelease(MouseInput input) {
        return itemScroll.release(input.button());
    }

    @Override
    protected boolean onMouseDrag(MouseDragInput input) {
        return input.isLeft() && itemScroll.drag(input.y(), GRID_Y, GRID_ROWS * ITEM_PITCH, 12);
    }

    @Override
    protected boolean onMouseScroll(ScrollInput input) {
        if (input.inside(RIGHT_X, GRID_Y, RIGHT_W, GRID_ROWS * ITEM_PITCH)) return itemScroll.scroll(input.deltaY());
        return false;
    }

    private ItemBindingEntryFTB itemEntryAt(int mx, int my) {
        if (!isInside(mx, my, RIGHT_X, GRID_Y, GRID_COLS * ITEM_PITCH, GRID_ROWS * ITEM_PITCH)) return null;
        double smooth = itemScroll.smoothOffset();
        int startRow = (int) Math.floor(smooth + 1.0E-6D);
        int shift = (int) Math.round((smooth - startRow) * ITEM_PITCH);
        int col = (mx - RIGHT_X) / ITEM_PITCH;
        int row = (my - GRID_Y + shift) / ITEM_PITCH;
        if ((mx - RIGHT_X) % ITEM_PITCH >= ITEM_SLOT) return null;
        int idx = (startRow + row) * GRID_COLS + col;
        return idx >= 0 && idx < explicitEntries.size() ? explicitEntries.get(idx) : null;
    }

    private boolean isInside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    protected boolean onCloseRequested() {
        navigateBack();
        return true;
    }

    /**
     * One quest per row: title, chapter muted, code grey at the right. In the quest list a bound quest is green;
     * in the bound list the default quest carries a green star.
     */
    private final class TaskRows extends KineticRowList<RefFTB> {
        private final boolean boundList;

        TaskRows(int x, int y, int width, int height, boolean boundList) {
            super(x, y, width, height, ROW_H);
            this.boundList = boundList;
        }

        @Override
        protected void renderRow(KineticGraphics g, RefFTB ref, int index, int x, int y, int width, int height, boolean hovered, boolean selected) {
            int textY = y + (height - 8) / 2;
            int right = x + width - 4;
            if (boundList) {
                boolean favorite = ref.id() == favoriteQuestId;
                Component star = AdventureText.translatable(favorite
                        ? "label.adventuresystems.ftb.favorite.marker_on"
                        : "label.adventuresystems.ftb.favorite.marker_off");
                // A fixed column for the star in every language.
                g.text(star, right - 8, textY, KineticTheme.current().text(), false);
                right -= 14;
            }
            String code = ref.code() == null ? "" : ref.code();
            int codeW = g.textWidth(code);
            g.text(code, right - codeW, textY, KineticTheme.current().mutedText(), false);
            boolean bound = !boundList && selectedQuestIds.contains(ref.id());
            Component label = Component.literal(cleanTaskTitle(ref)).withStyle(bound ? ChatFormatting.GREEN : ChatFormatting.WHITE);
            String chapter = cleanFtbText(ref.chapter());
            if (!chapter.isBlank()) label = label.copy().append(Component.literal("  " + chapter).withStyle(ChatFormatting.GRAY));
            g.scrollingText(label, x + 4, textY, Math.max(0, right - codeW - 6 - x - 4), 0xFFFFFFFF, false);
        }

        @Override
        protected boolean onRowClick(RefFTB ref, int index, MouseInput input) {
            if (boundList) {
                selectedTaskForDelete = ref;
                if (input.isLeft()) removeTask(ref);
                else if (input.isRight()) setFavoriteTask(ref);
                else return false;
            } else {
                if (!input.isLeft()) return false;
                toggleTaskFromLeft(ref);
            }
            playClickSound();
            return true;
        }

        @Override
        protected Component rowTooltip(RefFTB ref, int index) {
            Component title = Component.literal(cleanTaskTitle(ref));
            Component where = Component.literal(cleanFtbText(ref.chapter()) + "  " + ref.code()).withStyle(ChatFormatting.GRAY);
            if (boundList) {
                return title.copy().append("\n").append(where)
                        .append("\n").append(AdventureText.translatable("tip.adventuresystems.ftb.task.remove"))
                        .append("\n").append(AdventureText.translatable("tip.adventuresystems.ftb.favorite.desc"));
            }
            return title.copy().append("\n").append(where).append("\n").append(AdventureText.translatable(selectedQuestIds.contains(ref.id())
                    ? "tip.adventuresystems.ftb.task.remove" : "tip.adventuresystems.ftb.task.add"));
        }

        @Override
        protected Component emptyText() {
            if (!boundList) return AdventureText.translatable("label.adventuresystems.ftb.tasks.empty");
            return selectedStack.isEmpty() ? null : AdventureText.translatable("tip.adventuresystems.ftb.default");
        }
    }
}
