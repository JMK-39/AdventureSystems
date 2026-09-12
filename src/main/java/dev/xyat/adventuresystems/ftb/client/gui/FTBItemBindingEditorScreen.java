package dev.xyat.adventuresystems.ftb.client.gui;

import dev.xyat.kineticcore.api.client.overlay.GuiOverlay;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.xyat.kineticcore.api.client.search.KineticSearch;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.selector.ItemSelectorScreen;
import dev.xyat.kineticcore.api.client.screen.KineticScreen;
import dev.xyat.kineticcore.api.client.widget.KineticWidgets.Scroll;
import dev.xyat.kineticcore.config.client.KTConfigApi;
import dev.xyat.adventuresystems.ftb.client.hud.FTBToastUtil;
import dev.xyat.adventuresystems.ftb.client.FTBConfigGui;
import dev.xyat.adventuresystems.ftb.data.BindingStoreFTB;
import dev.xyat.adventuresystems.ftb.data.FavoritesStoreFTB;
import dev.xyat.adventuresystems.ftb.data.ItemBindingEntryFTB;
import dev.xyat.adventuresystems.ftb.data.RefFTB;
import dev.xyat.adventuresystems.ftb.util.BridgeFTB;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FTBItemBindingEditorScreen extends KineticScreen {
    private static final int PANEL_TOP = 36;
    private static final int PANEL_BOTTOM_PAD = 12;
    private static final int GAP = 10;
    private static final int ROW_H = 22;
    private static final int ITEM_SLOT = 22;
    private static final int SCROLL_W = 4;
    private static final int FAVORITE_ON_COLOR = 0xFF55FF55;
    private static final int FAVORITE_OFF_COLOR = 0xFFCFCFCF;

    private final Screen parent;
    private final List<RefFTB> allTasks = new ArrayList<>();
    private final List<RefFTB> visibleTasks = new ArrayList<>();
    private final List<RefFTB> boundTasks = new ArrayList<>();
    private final List<ItemBindingEntryFTB> explicitEntries = new ArrayList<>();
    private final LinkedHashSet<Long> selectedQuestIds = new LinkedHashSet<>();
    private final Map<Long, RefFTB> taskById = new LinkedHashMap<>();

    private EditBox searchBox;
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
    private double taskScroll;
    private double boundScroll;
    private double itemScroll;
    private int taskMaxScroll;
    private int boundMaxScroll;
    private int itemMaxScroll;
    private boolean taskScrolling;
    private boolean boundScrolling;
    private boolean itemScrolling;
    private final Scroll.State taskScrollState = new Scroll.State();
    private final Scroll.State boundScrollState = new Scroll.State();
    private final Scroll.State itemScrollState = new Scroll.State();
    private boolean dirty;
    private long favoriteQuestId;
    private Button saveButton;

    public FTBItemBindingEditorScreen(Screen parent) {
        super(Component.translatable("screen.adventuresystems.ftb.editor"));
        this.parent = parent;
        useCanvas(
                760f,
                430f,
                6
        );
        this.minScale = 0.5f;
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
    protected void buildUi() {
        this.allTasks.clear();
        this.taskById.clear();
        for (RefFTB ref : BridgeFTB.getAllQuestRefs()) {
            this.allTasks.add(ref);
            this.taskById.put(ref.id(), ref);
        }
        reloadExplicitEntries();

        int topY = 10;
        this.leftX = 14;
        int panelTotalW = this.canvasWidth - this.leftX * 2 - GAP;
        this.leftW = Math.max(220, panelTotalW / 2);
        this.rightW = this.leftW;
        this.rightX = this.leftX + this.leftW + GAP;
        this.panelY = PANEL_TOP;
        this.panelH = this.canvasHeight - this.panelY - PANEL_BOTTOM_PAD;

        int buttonW = 70;
        int closeX = this.canvasWidth - 14 - buttonW;
        int clearX = closeX - GAP - 80;
        int blacklistX = clearX - GAP - 70;

        this.searchBox = new EditBox(this.font, this.leftX, topY, 220, 20, Component.translatable("placeholder.adventuresystems.ftb.task.search"));
        this.searchBox.setMaxLength(128);
        this.searchBox.setResponder(this::refreshTaskFilter);
        this.addRenderableWidget(this.searchBox);

        this.addRenderableWidget(Button.builder(Component.translatable("button.adventuresystems.ftb.blacklist"), button -> {
            if (this.minecraft != null) this.minecraft.setScreen(new FTBBlacklistScreen(this));
        }).bounds(blacklistX, topY, 70, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("button.adventuresystems.ftb.clear"), button -> clearSelectedBinding())
                .bounds(clearX, topY, 80, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(closeX, topY, buttonW, 20).build());

        refreshLayoutValues();

        this.saveButton = Button.builder(Component.translatable("button.adventuresystems.ftb.save"), button -> saveCurrentBinding())
                .bounds(selectedItemIconX() + ITEM_SLOT + 8, selectedItemIconY() + 1, 52, 20)
                .build();
        this.addRenderableWidget(this.saveButton);

        updateItemScroll();
        refreshTaskFilter(this.searchBox.getValue());
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
        itemMaxScroll = Math.max(0, rows - itemGridRows);
        itemScroll = Math.max(0, Math.min(itemScroll, itemMaxScroll));
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
        taskScroll = 0;
        taskMaxScroll = Math.max(0, visibleTasks.size() - visibleTaskRows());
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
        boundScroll = 0;
        boundMaxScroll = Math.max(0, boundTasks.size() - visibleBoundRows());
    }

    private int visibleTaskRows() {
        return Math.max(1, (panelH - 26) / ROW_H);
    }

    private int visibleBoundRows() {
        return Math.max(1, boundH / ROW_H);
    }

    private void openItemSelector() {
        Minecraft.getInstance().setScreen(new ItemSelectorScreen(this, selection -> {
            if (selection != null && selection.isItem()) {
                selectStack(selection.stack());
            } else {
                FTBToastUtil.showQuick("adventuresystems_binding_item_only", Component.translatable("msg.adventuresystems.ftb.item.only"));
            }
            Minecraft.getInstance().setScreen(this);
        }));
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
            FTBToastUtil.showQuick("adventuresystems_binding_select_item_first", Component.translatable("msg.adventuresystems.ftb.item.first"));
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
            FTBToastUtil.showQuick("adventuresystems_binding_select_item_first", Component.translatable("msg.adventuresystems.ftb.item.first"));
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
            FTBToastUtil.showQuick("adventuresystems_binding_select_item_first", Component.translatable("msg.adventuresystems.ftb.item.first"));
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
            FTBToastUtil.showQuick("adventuresystems_binding_select_item_first", Component.translatable("msg.adventuresystems.ftb.item.first"));
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
            FTBToastUtil.showQuick("adventuresystems_binding_failed", Component.translatable("msg.adventuresystems.ftb.failed"));
        }
    }

    private void updateSaveButton() {
        if (saveButton == null) return;
        saveButton.visible = !selectedStack.isEmpty();
        saveButton.active = !selectedStack.isEmpty() && dirty;
    }

    @Override
    protected void renderCanvasBackground(@NotNull GuiGraphics g, int mx, int my, float pt) {
        GuiTheme.shadow(g, canvasWidth, canvasHeight);
        g.fillGradient(0, 0, this.canvasWidth, this.canvasHeight, 0xFF222222, 0xFF111111);
        g.drawCenteredString(font, title, this.canvasWidth / 2, 15, 0xFFFFAA00);
        GuiTheme.panel(g, leftX, panelY, leftW, panelH, 0xEE151515, 0xFF666666);
        GuiTheme.panel(g, rightX, panelY, rightW, panelH, 0xEE151515, 0xFF666666);
    }

    @Override
    protected void renderCanvasForeground(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderSearchPlaceholder(g);
        renderLeftTasks(g, mx, my);
        renderRightPanel(g, mx, my);
    }


    private void renderSearchPlaceholder(GuiGraphics g) {
        if (searchBox == null) return;
        if (!searchBox.getValue().isEmpty() || searchBox.isFocused()) return;
        g.drawString(font, Component.translatable("placeholder.adventuresystems.ftb.task.search"), searchBox.getX() + 5, searchBox.getY() + 6, 0xFFAAAAAA, false);
    }

    private void renderLeftTasks(GuiGraphics g, int mx, int my) {
        int titleX = leftX + 8;
        int titleY = panelY + 8;
        g.drawString(font, Component.translatable("label.adventuresystems.ftb.tasks", number(visibleTasks.size(), ChatFormatting.GREEN), number(allTasks.size(), ChatFormatting.YELLOW)), titleX, titleY, 0xFF55FFFF, false);

        int listX = leftX + 6;
        int listY = panelY + 26;
        int listW = leftW - 20;
        int rows = visibleTaskRows();
        double smoothTaskScroll = taskScrollState.follow(taskScroll, taskMaxScroll, taskScrolling);
        int start = (int) Math.floor(smoothTaskScroll + 1.0E-6D);
        int taskShift = (int) Math.round((smoothTaskScroll - start) * ROW_H);
        int end = Math.min(visibleTasks.size(), start + rows + 1);

        enableCanvasScissor(g, listX, listY, listX + listW, listY + rows * ROW_H);
        for (int i = start; i < end; i++) {
            RefFTB ref = visibleTasks.get(i);
            int row = i - start;
            int y = listY + row * ROW_H - taskShift;
            boolean selected = selectedQuestIds.contains(ref.id());
            boolean hover = mx >= listX && mx < listX + listW && my >= y && my < y + ROW_H;
            int bg = selected ? 0x6633AA33 : hover ? 0x44FFFFFF : 0x22000000;
            g.fill(listX, y, listX + listW, y + ROW_H - 1, bg);
            String titleText = GuiTheme.trim(font, cleanTaskTitle(ref), listW - 8);
            Component subText = buildTaskChapterLine(ref);
            g.drawString(font, titleText, listX + 4, y + 3, 0xFFFFFFFF, false);
            g.drawString(font, subText, listX + 4, y + 13, 0xFFFFAA00, false);
        }

        g.disableScissor();
        if (taskMaxScroll > 0) {
            int trackH = rows * ROW_H;
            int thumbH = Scroll.calculateThumbHeight(trackH, rows, visibleTasks.size(), 20);
            Scroll.renderScrollbar(g, mx, my, leftX + leftW - 12, listY, SCROLL_W, trackH, thumbH, taskMaxScroll, smoothTaskScroll, taskScrolling);
        }
    }

    private void renderRightPanel(GuiGraphics g, int mx, int my) {
        int headerX = rightX + 8;
        int headerY = panelY + 8;
        Component selectedLabel = Component.translatable("label.adventuresystems.ftb.item");
        g.drawString(font, selectedLabel, headerX, headerY, 0xFF55FFFF, false);

        drawItemSlot(g, selectedItemIconX(), selectedItemIconY(), selectedStack, mx, my, 0xFF444444);
        if (selectedStack.isEmpty()) {
            g.drawString(font, Component.translatable("tip.adventuresystems.ftb.item.choose"), selectedItemIconX() + ITEM_SLOT + 8, selectedItemIconY() + 7, 0xFFFFFF55, false);
        }

        g.drawString(font, Component.translatable("label.adventuresystems.ftb.custom.items", number(explicitEntries.size(), ChatFormatting.GREEN)), headerX, panelY + 30, 0xFFFFAA00, false);
        renderExplicitItemGrid(g, mx, my);

        g.drawString(font, Component.translatable("label.adventuresystems.ftb.bound.tasks", number(boundTasks.size(), ChatFormatting.GREEN)), headerX, boundY - 18, 0xFF55FFFF, false);
        renderBoundTasks(g, mx, my);

        if (!selectedStack.isEmpty() && boundTasks.isEmpty() && !dirty) {
            g.drawString(font, Component.translatable("tip.adventuresystems.ftb.default"), rightX + 8, boundY + boundH + 1, 0xFF55FF55, false);
        }
    }

    private void renderExplicitItemGrid(GuiGraphics g, int mx, int my) {
        double smoothItemScroll = itemScrollState.follow(itemScroll, itemMaxScroll, itemScrolling);
        int smoothItemRow = (int) Math.floor(smoothItemScroll + 1.0E-6D);
        int itemShift = (int) Math.round((smoothItemScroll - smoothItemRow) * ITEM_SLOT);
        int start = smoothItemRow * itemGridCols;
        int visible = (itemGridRows + 1) * itemGridCols;
        int end = Math.min(explicitEntries.size(), start + visible);
        int gridW = itemGridCols * ITEM_SLOT;
        g.fill(itemGridX - 2, itemGridY - 2, itemGridX + gridW + SCROLL_W + 5, itemGridY + itemGridH + 2, 0xFF000000);
        g.fill(itemGridX - 1, itemGridY - 1, itemGridX + gridW + SCROLL_W + 4, itemGridY + itemGridH + 1, 0xFF2A2A2A);

        enableCanvasScissor(g, itemGridX, itemGridY, itemGridX + gridW, itemGridY + itemGridH);
        for (int i = start; i < end; i++) {
            ItemBindingEntryFTB entry = explicitEntries.get(i);
            int col = (i - start) % itemGridCols;
            int row = (i - start) / itemGridCols;
            int x = itemGridX + col * ITEM_SLOT;
            int y = itemGridY + row * ITEM_SLOT - itemShift;
            ItemStack stack = entry.createDisplayStack();
            boolean selected = !selectedStack.isEmpty() && BindingStoreFTB.exactKeyForStack(selectedStack).equals(entry.key);
            drawItemSlot(g, x, y, stack, mx, my, selected ? 0xFF55AA55 : 0xFF333333);
        }

        g.disableScissor();
        if (itemMaxScroll > 0) {
            int thumbH = Scroll.calculateThumbHeight(itemGridH, itemGridRows, Math.max(itemGridRows, (int) Math.ceil((double) explicitEntries.size() / itemGridCols)), 18);
            Scroll.renderScrollbar(g, mx, my, itemGridX + gridW + 3, itemGridY, SCROLL_W, itemGridH, thumbH, itemMaxScroll, smoothItemScroll, itemScrolling);
        }
    }

    private void renderBoundTasks(GuiGraphics g, int mx, int my) {
        int listX = rightX + 10;
        int listW = rightW - 34 - SCROLL_W;
        int rows = visibleBoundRows();
        double smoothBoundScroll = boundScrollState.follow(boundScroll, boundMaxScroll, boundScrolling);
        int start = (int) Math.floor(smoothBoundScroll + 1.0E-6D);
        int boundShift = (int) Math.round((smoothBoundScroll - start) * ROW_H);
        int end = Math.min(boundTasks.size(), start + rows + 1);
        g.fill(listX - 2, boundY - 2, listX + listW + SCROLL_W + 6, boundY + rows * ROW_H + 2, 0xFF000000);
        g.fill(listX - 1, boundY - 1, listX + listW + SCROLL_W + 5, boundY + rows * ROW_H + 1, 0xFF2A2A2A);

        enableCanvasScissor(g, listX, boundY, listX + listW, boundY + rows * ROW_H);
        for (int i = start; i < end; i++) {
            RefFTB ref = boundTasks.get(i);
            int y = boundY + (i - start) * ROW_H - boundShift;
            boolean hover = mx >= listX && mx < listX + listW && my >= y && my < y + ROW_H;
            g.fill(listX, y, listX + listW, y + ROW_H - 1, hover ? 0x44FFFFFF : 0x22000000);
            boolean favorite = ref.id() == favoriteQuestId;
            Component star = Component.literal(favorite ? "★" : "☆");
            int starW = font.width(star) + 8;
            String titleText = GuiTheme.trim(font, cleanTaskTitle(ref), listW - 8 - starW);
            Component subText = buildTaskChapterLine(ref);
            g.drawString(font, titleText, listX + 4, y + 3, 0xFFFFFFFF, false);
            g.drawString(font, subText, listX + 4, y + 13, 0xFFFFAA00, false);
            g.drawString(font, star, listX + listW - starW + 2, y + 3, favorite ? FAVORITE_ON_COLOR : FAVORITE_OFF_COLOR, false);
        }

        g.disableScissor();
        if (boundMaxScroll > 0) {
            int trackH = rows * ROW_H;
            int thumbH = Scroll.calculateThumbHeight(trackH, rows, boundTasks.size(), 20);
            Scroll.renderScrollbar(g, mx, my, listX + listW + 4, boundY, SCROLL_W, trackH, thumbH, boundMaxScroll, smoothBoundScroll, boundScrolling);
        }
    }

    private String cleanTaskTitle(RefFTB ref) {
        String title = ref == null ? "" : cleanFtbText(ref.title());
        return title.isBlank() ? Component.translatable("label.adventuresystems.ftb.task.unnamed").getString() : title;
    }

    private Component buildTaskChapterLine(RefFTB ref) {
        String chapter = ref == null ? "" : cleanFtbText(ref.chapter());
        if (chapter.isBlank()) {
            chapter = Component.translatable("label.adventuresystems.ftb.chapter.unknown").getString();
        }
        String code = ref == null ? "" : ref.code();
        return Component.translatable(
                "label.adventuresystems.ftb.quest.chapter.id",
                Component.literal(chapter).withStyle(ChatFormatting.GOLD),
                Component.literal(code).withStyle(ChatFormatting.AQUA)
        );
    }

    private static Component number(long value, ChatFormatting color) {
        return Component.literal(String.valueOf(value)).withStyle(color);
    }

    private String cleanFtbText(String value) {
        if (value == null || value.isBlank()) return "";
        return value.replaceAll("(?i)[§&][0-9A-FK-OR]", "").trim();
    }

    private int selectedItemIconX() {
        return rightX + 8 + font.width(Component.translatable("label.adventuresystems.ftb.item")) + 8;
    }

    private int selectedItemIconY() {
        return panelY + 4;
    }

    private void drawItemSlot(GuiGraphics g, int x, int y, ItemStack stack, int mx, int my, int borderColor) {
        boolean hovered = mx >= x && mx < x + ITEM_SLOT && my >= y && my < y + ITEM_SLOT;
        GuiTheme.itemSlot(g, stack, x, y, ITEM_SLOT, 4, hovered);
        g.renderOutline(x, y, ITEM_SLOT, ITEM_SLOT, borderColor);
        if (!stack.isEmpty()) {
            RenderSystem.enableDepthTest();
            g.renderItem(stack, x + 3, y + 3);
            RenderSystem.disableDepthTest();
        }
    }

    @Override
    protected void renderTooltips(GuiGraphics g, int smx, int smy, int mx, int my) {
        RefFTB taskRef = taskAt(smx, smy);
        if (taskRef != null) {
            Component tip = selectedQuestIds.contains(taskRef.id())
                    ? Component.translatable("tip.adventuresystems.ftb.task.remove")
                    : Component.translatable("tip.adventuresystems.ftb.task.add");
            GuiOverlay.requestTooltip(List.of(tip), mx, my);
            return;
        }
        RefFTB boundRef = boundAt(smx, smy);
        if (boundRef != null) {
            GuiOverlay.requestTooltip(List.of(
                    Component.translatable("tip.adventuresystems.ftb.task.remove"),
                    Component.translatable("tip.adventuresystems.ftb.favorite.desc")
            ), mx, my);
            return;
        }
        if (isInside(smx, smy, selectedItemIconX(), selectedItemIconY(), ITEM_SLOT, ITEM_SLOT)) {
            GuiOverlay.requestTooltip(List.of(Component.translatable("button.adventuresystems.ftb.item.select")), mx, my);
        }
    }

    @Override
    protected boolean canvasMouseClicked(double mx, double my, int btn) {
        if (btn == 0) {
            if (clickScrollbars(mx, my)) return true;

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
        } else if (btn == 1) {
            RefFTB bound = boundAt((int) mx, (int) my);
            if (bound != null) {
                setFavoriteTask(bound);
                return true;
            }
        }
        return super.canvasMouseClicked(mx, my, btn);
    }

    private boolean clickScrollbars(double mx, double my) {
        int rows = visibleTaskRows();
        int listY = panelY + 26;
        int taskTrackH = rows * ROW_H;
        if (taskMaxScroll > 0 && mx >= leftX + leftW - 12 && mx <= leftX + leftW - 12 + SCROLL_W && my >= listY && my < listY + taskTrackH) {
            taskScrolling = true;
            updateTaskScroll(my);
            return true;
        }

        int gridW = itemGridCols * ITEM_SLOT;
        if (itemMaxScroll > 0 && mx >= itemGridX + gridW + 3 && mx <= itemGridX + gridW + 3 + SCROLL_W && my >= itemGridY && my < itemGridY + itemGridH) {
            itemScrolling = true;
            updateItemScroll(my);
            return true;
        }

        int boundRows = visibleBoundRows();
        int boundTrackH = boundRows * ROW_H;
        int boundListX = rightX + 10;
        int boundListW = rightW - 34 - SCROLL_W;
        int boundBarX = boundListX + boundListW + 4;
        if (boundMaxScroll > 0 && mx >= boundBarX && mx <= boundBarX + SCROLL_W && my >= boundY && my < boundY + boundTrackH) {
            boundScrolling = true;
            updateBoundScroll(my);
            return true;
        }
        return false;
    }

    @Override
    protected boolean canvasMouseReleased(double mx, double my, int btn) {
        if (btn == 0) {
            taskScrolling = false;
            boundScrolling = false;
            itemScrolling = false;
        }
        return super.canvasMouseReleased(mx, my, btn);
    }

    @Override
    protected boolean canvasMouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (btn == 0) {
            if (taskScrolling) {
                updateTaskScroll(my);
                return true;
            }
            if (boundScrolling) {
                updateBoundScroll(my);
                return true;
            }
            if (itemScrolling) {
                updateItemScroll(my);
                return true;
            }
        }
        return super.canvasMouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    protected boolean canvasMouseScrolled(double mx, double my, double delta) {
        if (isInside(mx, my, leftX, panelY + 26, leftW, visibleTaskRows() * ROW_H) && taskMaxScroll > 0) {
            taskScroll = taskScrollState.wheel(taskScroll, delta, 1.0D / 3.0D, taskMaxScroll);
            return true;
        }
        if (isInside(mx, my, itemGridX, itemGridY, itemGridCols * ITEM_SLOT + SCROLL_W + 4, itemGridH) && itemMaxScroll > 0) {
            itemScroll = itemScrollState.wheel(itemScroll, delta, 1.0D / 3.0D, itemMaxScroll);
            return true;
        }
        if (isInside(mx, my, rightX, boundY, rightW, visibleBoundRows() * ROW_H) && boundMaxScroll > 0) {
            boundScroll = boundScrollState.wheel(boundScroll, delta, 1.0D / 3.0D, boundMaxScroll);
            return true;
        }
        return super.canvasMouseScrolled(mx, my, delta);
    }

    private void updateTaskScroll(double my) {
        int rows = visibleTaskRows();
        int trackH = rows * ROW_H;
        int thumbH = Scroll.calculateThumbHeight(trackH, rows, visibleTasks.size(), 20);
        taskScroll = Scroll.calculateScrollOffset(my, panelY + 26, trackH, thumbH, taskMaxScroll);
        taskScrollState.snap(taskScroll, taskMaxScroll);
    }

    private void updateBoundScroll(double my) {
        int rows = visibleBoundRows();
        int trackH = rows * ROW_H;
        int thumbH = Scroll.calculateThumbHeight(trackH, rows, boundTasks.size(), 20);
        boundScroll = Scroll.calculateScrollOffset(my, boundY, trackH, thumbH, boundMaxScroll);
        boundScrollState.snap(boundScroll, boundMaxScroll);
    }

    private void updateItemScroll(double my) {
        int totalRows = Math.max(itemGridRows, (int) Math.ceil((double) explicitEntries.size() / Math.max(1, itemGridCols)));
        int thumbH = Scroll.calculateThumbHeight(itemGridH, itemGridRows, totalRows, 18);
        itemScroll = Scroll.calculateScrollOffset(my, itemGridY, itemGridH, thumbH, itemMaxScroll);
        itemScrollState.snap(itemScroll, itemMaxScroll);
    }

    private RefFTB taskAt(int mx, int my) {
        int listX = leftX + 6;
        int listY = panelY + 26;
        int listW = leftW - 20;
        if (!isInside(mx, my, listX, listY, listW, visibleTaskRows() * ROW_H)) return null;
        double smooth = taskScrollState.follow(taskScroll, taskMaxScroll, taskScrolling);
        int start = (int) Math.floor(smooth + 1.0E-6D);
        int shift = (int) Math.round((smooth - start) * ROW_H);
        int idx = start + (my - listY + shift) / ROW_H;
        return idx >= 0 && idx < visibleTasks.size() ? visibleTasks.get(idx) : null;
    }

    private RefFTB boundAt(int mx, int my) {
        int listX = rightX + 10;
        int listW = rightW - 34 - SCROLL_W;
        if (!isInside(mx, my, listX, boundY, listW, visibleBoundRows() * ROW_H)) return null;
        double smooth = boundScrollState.follow(boundScroll, boundMaxScroll, boundScrolling);
        int start = (int) Math.floor(smooth + 1.0E-6D);
        int shift = (int) Math.round((smooth - start) * ROW_H);
        int idx = start + (my - boundY + shift) / ROW_H;
        return idx >= 0 && idx < boundTasks.size() ? boundTasks.get(idx) : null;
    }

    private ItemBindingEntryFTB itemEntryAt(int mx, int my) {
        int gridW = itemGridCols * ITEM_SLOT;
        if (!isInside(mx, my, itemGridX, itemGridY, gridW, itemGridH)) return null;
        int col = (mx - itemGridX) / ITEM_SLOT;
        double smooth = itemScrollState.follow(itemScroll, itemMaxScroll, itemScrolling);
        int startRow = (int) Math.floor(smooth + 1.0E-6D);
        int shift = (int) Math.round((smooth - startRow) * ITEM_SLOT);
        int row = (my - itemGridY + shift) / ITEM_SLOT;
        int idx = (startRow + row) * itemGridCols + col;
        return idx >= 0 && idx < explicitEntries.size() ? explicitEntries.get(idx) : null;
    }

    private boolean isInside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private int clampScroll(int value, int max) {
        return Math.max(0, Math.min(value, max));
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
