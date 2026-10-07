package dev.xyat.adventuresystems.ftb.client.gui;

import dev.xyat.kineticcore.api.client.gui.input.MouseDragInput;
import dev.xyat.kineticcore.api.client.gui.input.MouseInput;
import dev.xyat.kineticcore.api.client.gui.input.ScrollInput;
import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.scroll.KineticScrollController;
import dev.xyat.kineticcore.api.client.gui.selector.KineticSelectors;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.widget.KineticButton;
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
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FTBItemBindingEditorScreen extends KineticPage {
    private static final int PANEL_TOP = 36;
    private static final int PANEL_BOTTOM_PAD = 12;
    private static final int GAP = 10;
    // Two text lines per row at +4 and +13, keeping 3 px above and 2 px below inside the row frame.
    private static final int ROW_H = 26;
    private static final int ITEM_SLOT = 22;
    private static final int SCROLL_W = 4;
    
    private final List<RefFTB> allTasks = new ArrayList<>();
    private final List<RefFTB> visibleTasks = new ArrayList<>();
    private final List<RefFTB> boundTasks = new ArrayList<>();
    private final List<ItemBindingEntryFTB> explicitEntries = new ArrayList<>();
    private final LinkedHashSet<Long> selectedQuestIds = new LinkedHashSet<>();
    private final Map<Long, RefFTB> taskById = new LinkedHashMap<>();

    private String searchText = "";
    private ItemStack selectedStack = ItemStack.EMPTY;

    private int leftX;
    private int rightX;
    private int panelY;
    private int leftW;
    private int rightW;
    private int panelH;
    private int itemGridX;
    private int itemGridY;
    private int itemGridCols;
    private int itemGridRows;
    private int itemGridH;
    private int boundY;
    private int boundH;
    private final KineticScrollController taskScroll = new KineticScrollController();
    private final KineticScrollController boundScroll = new KineticScrollController();
    private final KineticScrollController itemScroll = new KineticScrollController();
    private boolean dirty;
    private long favoriteQuestId;
    private KineticButton saveButton;

    public FTBItemBindingEditorScreen() {
        super(AdventureText.translatable("screen.adventuresystems.ftb.editor"));
        useCanvas(640, 360, 6);
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

        int topY = 10;
        this.leftX = 14;
        int panelTotalW = width() - this.leftX * 2 - GAP;
        this.leftW = Math.max(220, panelTotalW / 2);
        this.rightW = this.leftW;
        this.rightX = this.leftX + this.leftW + GAP;
        this.panelY = PANEL_TOP;
        this.panelH = height() - this.panelY - PANEL_BOTTOM_PAD;

        int buttonW = 70;
        int closeX = width() - 14 - buttonW;
        int clearX = closeX - GAP - 80;
        int blacklistX = clearX - GAP - 70;

        ui.textField(this.leftX, topY, 220)
                .label(AdventureText.translatable("placeholder.adventuresystems.ftb.task.search"))
                .placeholder(AdventureText.translatable("placeholder.adventuresystems.ftb.task.search"))
                .value(searchText).maxLength(128)
                .onChange(value -> {
                    searchText = value;
                    taskScroll.reset();
                    refreshTaskFilter(value);
                }).firstShownTextAsDefault().build();
        ui.button(blacklistX, topY, 70).text(AdventureText.translatable("button.adventuresystems.ftb.blacklist"))
                .onClick(() -> openChild(new FTBBlacklistScreen())).build();
        ui.button(clearX, topY, 80).text(AdventureText.translatable("button.adventuresystems.ftb.clear"))
                .onClick(this::clearSelectedBinding).build();
        ui.button(closeX, topY, buttonW).text(AdventureText.translatable("gui.done"))
                .onClick(this::close).build();

        refreshLayoutValues();

        this.saveButton = ui.button(selectedItemIconX() + ITEM_SLOT + 8, selectedItemIconY() + 1, 52)
                .text(AdventureText.translatable("button.adventuresystems.ftb.save"))
                .onClick(this::saveCurrentBinding).build();

        updateItemScroll();
        refreshTaskFilter(searchText);
        refreshBoundRefs();
        updateSaveButton();
    }

    private void refreshLayoutValues() {
        this.itemGridX = this.rightX + 10;
        this.itemGridY = this.panelY + 46;
        this.itemGridCols = Math.max(1, (this.rightW - 30 - SCROLL_W) / ITEM_SLOT);
        this.itemGridRows = 4;
        this.itemGridH = this.itemGridRows * ITEM_SLOT;
        this.boundY = this.itemGridY + this.itemGridH + 34;
        this.boundH = Math.max(ROW_H, this.panelY + this.panelH - this.boundY - 8);
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
        int rows = explicitEntries.isEmpty() ? 0 : (int) Math.ceil((double) explicitEntries.size() / Math.max(1, itemGridCols));
        itemScroll.update(rows, itemGridRows);
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
        taskScroll.update(visibleTasks.size(), visibleTaskRows());
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
        boundScroll.update(boundTasks.size(), visibleBoundRows());
    }

    private int visibleTaskRows() {
        // The last row keeps 3 px above the panel's bottom line.
        return Math.max(1, (panelH - 26 - 3) / ROW_H);
    }

    private int visibleBoundRows() {
        return Math.max(1, boundH / ROW_H);
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
        KineticTheme.canvasBackground(g, width(), height());
        g.scrollingTextCentered(title(), width() / 2, 15, Math.max(0, Math.min(width() / 2 - (leftX + 220 + 4), width() - 14 - 70 - GAP - 80 - GAP - 70 - 4 - width() / 2) * 2), KineticTheme.current().text(), true);
        KineticTheme.panel(g, leftX, panelY, leftW, panelH);
        KineticTheme.panel(g, rightX, panelY, rightW, panelH);
    }

    @Override
    protected void renderForeground(KineticGraphics g, int mx, int my, float pt) {
        renderLeftTasks(g, mx, my);
        renderRightPanel(g, mx, my);
    }

    private void renderLeftTasks(KineticGraphics g, int mx, int my) {
        int titleX = leftX + 8;
        int titleY = panelY + 8;
        g.scrollingText(AdventureText.translatable("label.adventuresystems.ftb.tasks", number(visibleTasks.size()), number(allTasks.size())), titleX, titleY, leftW - 16, KineticTheme.current().text(), false);

        int listX = leftX + 6;
        int listY = panelY + 26;
        int listW = leftW - 20;
        int rows = visibleTaskRows();
        double smoothTaskScroll = taskScroll.smoothOffset();
        int start = (int) Math.floor(smoothTaskScroll + 1.0E-6D);
        int taskShift = (int) Math.round((smoothTaskScroll - start) * ROW_H);
        int end = Math.min(visibleTasks.size(), start + rows + 1);

        g.scissor(listX, listY, listX + listW, listY + rows * ROW_H);
        for (int i = start; i < end; i++) {
            RefFTB ref = visibleTasks.get(i);
            int row = i - start;
            int y = listY + row * ROW_H - taskShift;
            boolean selected = selectedQuestIds.contains(ref.id());
            boolean hover = mx >= listX && mx < listX + listW && my >= y && my < y + ROW_H;
            KineticTheme.stateSurface(g, listX, y, listW, ROW_H - 1, KineticTheme.Surface.PANEL_ALT, selected, hover, false);
            Component titleText = AdventureText.literal(cleanTaskTitle(ref));
            Component subText = buildTaskChapterLine(ref);
            g.scrollingText(titleText, listX + 4, y + 4, Math.max(0, listW - 8), KineticTheme.current().text(), false);
            g.scrollingText(subText, listX + 4, y + 13, listW - 8, KineticTheme.current().text(), false);
        }

        g.endScissor();
        if (taskScroll.canScroll()) {
            int trackH = rows * ROW_H;
            taskScroll.render(g, mx, my, leftX + leftW - 12, listY, SCROLL_W, trackH, 20);
        }
    }

    private void renderRightPanel(KineticGraphics g, int mx, int my) {
        int headerX = rightX + 8;
        int headerY = panelY + 8;
        Component selectedLabel = AdventureText.translatable("label.adventuresystems.ftb.item");
        g.scrollingText(selectedLabel, headerX, headerY, Math.max(0, selectedItemIconX() - headerX - 4), KineticTheme.current().text(), false);

        drawItemSlot(g, selectedItemIconX(), selectedItemIconY(), selectedStack, mx, my, false);
        if (selectedStack.isEmpty()) {
            g.scrollingText(AdventureText.translatable("tip.adventuresystems.ftb.item.choose"), selectedItemIconX() + ITEM_SLOT + 8 + 52 + 4, selectedItemIconY() + 7, Math.max(0, rightX + rightW - 8 - (selectedItemIconX() + ITEM_SLOT + 8 + 52 + 4)), KineticTheme.current().text(), false);
        }

        g.scrollingText(AdventureText.translatable("label.adventuresystems.ftb.custom.items", number(explicitEntries.size())), headerX, panelY + 30, rightW - 16, KineticTheme.current().text(), false);
        renderExplicitItemGrid(g, mx, my);

        g.scrollingText(AdventureText.translatable("label.adventuresystems.ftb.bound.tasks", number(boundTasks.size())), headerX, boundY - 18, rightW - 16, KineticTheme.current().text(), false);
        renderBoundTasks(g, mx, my);

        if (!selectedStack.isEmpty() && boundTasks.isEmpty() && !dirty) {
            g.scrollingText(AdventureText.translatable("tip.adventuresystems.ftb.default"), rightX + 8, boundY + boundH + 1, rightW - 16, KineticTheme.current().text(), false);
        }
    }

    private void renderExplicitItemGrid(KineticGraphics g, int mx, int my) {
        double smoothItemScroll = itemScroll.smoothOffset();
        int smoothItemRow = (int) Math.floor(smoothItemScroll + 1.0E-6D);
        int itemShift = (int) Math.round((smoothItemScroll - smoothItemRow) * ITEM_SLOT);
        int start = smoothItemRow * itemGridCols;
        int visible = (itemGridRows + 1) * itemGridCols;
        int end = Math.min(explicitEntries.size(), start + visible);
        int gridW = itemGridCols * ITEM_SLOT;
        KineticTheme.panelAlt(g, itemGridX - 2, itemGridY - 2, gridW + SCROLL_W + 7, itemGridH + 4);

        g.scissor(itemGridX, itemGridY, itemGridX + gridW, itemGridY + itemGridH);
        for (int i = start; i < end; i++) {
            ItemBindingEntryFTB entry = explicitEntries.get(i);
            int col = (i - start) % itemGridCols;
            int row = (i - start) / itemGridCols;
            int x = itemGridX + col * ITEM_SLOT;
            int y = itemGridY + row * ITEM_SLOT - itemShift;
            ItemStack stack = entry.createDisplayStack();
            boolean selected = !selectedStack.isEmpty() && BindingStoreFTB.exactKeyForStack(selectedStack).equals(entry.key);
            drawItemSlot(g, x, y, stack, mx, my, selected);
        }

        g.endScissor();
        if (itemScroll.canScroll()) {
            itemScroll.render(g, mx, my, itemGridX + gridW + 3, itemGridY, SCROLL_W, itemGridH, 18);
        }
    }

    private void renderBoundTasks(KineticGraphics g, int mx, int my) {
        int listX = rightX + 10;
        int listW = rightW - 34 - SCROLL_W;
        int rows = visibleBoundRows();
        double smoothBoundScroll = boundScroll.smoothOffset();
        int start = (int) Math.floor(smoothBoundScroll + 1.0E-6D);
        int boundShift = (int) Math.round((smoothBoundScroll - start) * ROW_H);
        int end = Math.min(boundTasks.size(), start + rows + 1);
        KineticTheme.panelAlt(g, listX - 3, boundY - 3, listW + SCROLL_W + 10, rows * ROW_H + 6);

        g.scissor(listX, boundY, listX + listW, boundY + rows * ROW_H);
        for (int i = start; i < end; i++) {
            RefFTB ref = boundTasks.get(i);
            int y = boundY + (i - start) * ROW_H - boundShift;
            boolean hover = mx >= listX && mx < listX + listW && my >= y && my < y + ROW_H;
            KineticTheme.stateSurface(g, listX, y, listW, ROW_H - 1, KineticTheme.Surface.PANEL_ALT, false, hover, false);
            boolean favorite = ref.id() == favoriteQuestId;
            Component star = AdventureText.translatable(favorite
                    ? "label.adventuresystems.ftb.favorite.marker_on"
                    : "label.adventuresystems.ftb.favorite.marker_off");
            // A fixed column for the favourite marker in every language.
            int starW = 24;
            Component titleText = AdventureText.literal(cleanTaskTitle(ref));
            Component subText = buildTaskChapterLine(ref);
            g.scrollingText(titleText, listX + 4, y + 4, Math.max(0, listW - 8 - starW), KineticTheme.current().text(), false);
            g.scrollingText(subText, listX + 4, y + 13, listW - 8, KineticTheme.current().text(), false);
            g.scrollingText(star, listX + listW - starW + 2, y + 4, Math.max(0, starW - 6), KineticTheme.current().text(), false);
        }

        g.endScissor();
        if (boundScroll.canScroll()) {
            int trackH = rows * ROW_H;
            boundScroll.render(g, mx, my, listX + listW + 4, boundY, SCROLL_W, trackH, 20);
        }
    }

    private String cleanTaskTitle(RefFTB ref) {
        String title = ref == null ? "" : cleanFtbText(ref.title());
        return title.isBlank() ? AdventureText.translatable("label.adventuresystems.ftb.task.unnamed").getString() : title;
    }

    private Component buildTaskChapterLine(RefFTB ref) {
        String chapter = ref == null ? "" : cleanFtbText(ref.chapter());
        Component chapterText = chapter.isBlank()
                ? AdventureText.translatable("label.adventuresystems.ftb.chapter.unknown")
                : AdventureText.literal(chapter);
        String code = ref == null ? "" : ref.code();
        return AdventureText.translatable(
                "label.adventuresystems.ftb.quest.chapter.id",
                chapterText,
                Component.literal(code)
        );
    }

    private static Component number(long value) {
        return Component.literal(String.valueOf(value));
    }

    private String cleanFtbText(String value) {
        if (value == null || value.isBlank()) return "";
        return value.replaceAll("(?i)[§&][0-9A-FK-OR]", "").trim();
    }

    private int selectedItemIconX() {
        // The item label has the same room in every language and scrolls when longer.
        int labelEnd = rightX + 8 + 40 + 8;
        // Reserve the item slot, Save button and a readable hint viewport.
        int limit = rightX + rightW - 8 - ITEM_SLOT - 8 - 52 - 4 - 60;
        return Math.max(rightX + 8, Math.min(labelEnd, limit));
    }

    private int selectedItemIconY() {
        return panelY + 4;
    }

    private void drawItemSlot(KineticGraphics g, int x, int y, ItemStack stack, int mx, int my, boolean selected) {
        boolean hovered = mx >= x && mx < x + ITEM_SLOT && my >= y && my < y + ITEM_SLOT;
        KineticTheme.itemSlot(g, x, y, ITEM_SLOT, ITEM_SLOT, 4, selected, hovered, false);
        KineticTheme.item(g, stack, x, y, ITEM_SLOT, 1.0F, false);
    }

    @Override
    protected void renderTooltips(int mx, int my) {
        RefFTB taskRef = taskAt(mx, my);
        if (taskRef != null) {
            Component tip = selectedQuestIds.contains(taskRef.id())
                    ? AdventureText.translatable("tip.adventuresystems.ftb.task.remove")
                    : AdventureText.translatable("tip.adventuresystems.ftb.task.add");
            showTooltip(List.of(tip));
            return;
        }
        RefFTB boundRef = boundAt(mx, my);
        if (boundRef != null) {
            showTooltip(List.of(
                    AdventureText.translatable("tip.adventuresystems.ftb.task.remove"),
                    AdventureText.translatable("tip.adventuresystems.ftb.favorite.desc")
            ));
            return;
        }
        if (isInside(mx, my, selectedItemIconX(), selectedItemIconY(), ITEM_SLOT, ITEM_SLOT)) {
            showTooltip(AdventureText.translatable("button.adventuresystems.ftb.item.select"));
        }
    }

    @Override
    protected boolean onMouseClick(MouseInput input) {
        double mx = input.x();
        double my = input.y();
        if (input.isLeft()) {
            if (clickScrollbars(input)) return true;

            if (isInside(mx, my, selectedItemIconX(), selectedItemIconY(), ITEM_SLOT, ITEM_SLOT)) {
                openItemSelector();
                return true;
            }

            RefFTB task = taskAt((int) mx, (int) my);
            if (task != null) {
                toggleTaskFromLeft(task);
                return true;
            }

            RefFTB bound = boundAt((int) mx, (int) my);
            if (bound != null) {
                removeTask(bound);
                return true;
            }

            ItemBindingEntryFTB entry = itemEntryAt((int) mx, (int) my);
            if (entry != null) {
                selectEntry(entry);
                return true;
            }
        } else if (input.isRight()) {
            RefFTB bound = boundAt((int) mx, (int) my);
            if (bound != null) {
                setFavoriteTask(bound);
                return true;
            }
        }
        return false;
    }

    private boolean clickScrollbars(MouseInput input) {
        int rows = visibleTaskRows();
        int listY = panelY + 26;
        int taskTrackH = rows * ROW_H;
        if (taskScroll.beginDrag(input.x(), input.y(), input.button(), leftX + leftW - 12, listY, SCROLL_W, taskTrackH, 20)) return true;

        int gridW = itemGridCols * ITEM_SLOT;
        if (itemScroll.beginDrag(input.x(), input.y(), input.button(), itemGridX + gridW + 3, itemGridY, SCROLL_W, itemGridH, 18)) return true;

        int boundRows = visibleBoundRows();
        int boundTrackH = boundRows * ROW_H;
        int boundListX = rightX + 10;
        int boundListW = rightW - 34 - SCROLL_W;
        int boundBarX = boundListX + boundListW + 4;
        return boundScroll.beginDrag(input.x(), input.y(), input.button(), boundBarX, boundY, SCROLL_W, boundTrackH, 20);
    }

    @Override
    protected boolean onMouseRelease(MouseInput input) {
        return taskScroll.release(input.button()) | boundScroll.release(input.button()) | itemScroll.release(input.button());
    }

    @Override
    protected boolean onMouseDrag(MouseDragInput input) {
        if (!input.isLeft()) return false;
        return taskScroll.drag(input.y(), panelY + 26, visibleTaskRows() * ROW_H, 20)
                | itemScroll.drag(input.y(), itemGridY, itemGridH, 18)
                | boundScroll.drag(input.y(), boundY, visibleBoundRows() * ROW_H, 20);
    }

    @Override
    protected boolean onMouseScroll(ScrollInput input) {
        if (input.inside(leftX, panelY + 26, leftW, visibleTaskRows() * ROW_H)) return taskScroll.scroll(input.deltaY());
        if (input.inside(itemGridX, itemGridY, itemGridCols * ITEM_SLOT + SCROLL_W + 4, itemGridH)) return itemScroll.scroll(input.deltaY());
        if (input.inside(rightX, boundY, rightW, visibleBoundRows() * ROW_H)) return boundScroll.scroll(input.deltaY());
        return false;
    }

    private RefFTB taskAt(int mx, int my) {
        int listX = leftX + 6;
        int listY = panelY + 26;
        int listW = leftW - 20;
        if (!isInside(mx, my, listX, listY, listW, visibleTaskRows() * ROW_H)) return null;
        double smooth = taskScroll.smoothOffset();
        int start = (int) Math.floor(smooth + 1.0E-6D);
        int shift = (int) Math.round((smooth - start) * ROW_H);
        int idx = start + (my - listY + shift) / ROW_H;
        return idx >= 0 && idx < visibleTasks.size() ? visibleTasks.get(idx) : null;
    }

    private RefFTB boundAt(int mx, int my) {
        int listX = rightX + 10;
        int listW = rightW - 34 - SCROLL_W;
        if (!isInside(mx, my, listX, boundY, listW, visibleBoundRows() * ROW_H)) return null;
        double smooth = boundScroll.smoothOffset();
        int start = (int) Math.floor(smooth + 1.0E-6D);
        int shift = (int) Math.round((smooth - start) * ROW_H);
        int idx = start + (my - boundY + shift) / ROW_H;
        return idx >= 0 && idx < boundTasks.size() ? boundTasks.get(idx) : null;
    }

    private ItemBindingEntryFTB itemEntryAt(int mx, int my) {
        int gridW = itemGridCols * ITEM_SLOT;
        if (!isInside(mx, my, itemGridX, itemGridY, gridW, itemGridH)) return null;
        int col = (mx - itemGridX) / ITEM_SLOT;
        double smooth = itemScroll.smoothOffset();
        int startRow = (int) Math.floor(smooth + 1.0E-6D);
        int shift = (int) Math.round((smooth - startRow) * ITEM_SLOT);
        int row = (my - itemGridY + shift) / ITEM_SLOT;
        int idx = (startRow + row) * itemGridCols + col;
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
}
