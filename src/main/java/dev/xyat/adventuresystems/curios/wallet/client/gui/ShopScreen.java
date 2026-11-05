package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.adventuresystems.curios.wallet.client.ShopClientPreferences;
import dev.xyat.adventuresystems.curios.wallet.data.CurrencyType;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.adventuresystems.curios.wallet.data.StackCodec;
import dev.xyat.adventuresystems.curios.wallet.network.Network;
import dev.xyat.adventuresystems.curios.wallet.shop.Shop;
import dev.xyat.adventuresystems.ftb.util.BridgeFTB;
import dev.xyat.adventuresystems.text.AdventureText;
import dev.xyat.kineticcore.api.client.gui.input.MouseButton;
import dev.xyat.kineticcore.api.client.gui.input.MouseDragInput;
import dev.xyat.kineticcore.api.client.gui.input.MouseInput;
import dev.xyat.kineticcore.api.client.gui.input.ScrollInput;
import dev.xyat.kineticcore.api.client.gui.layout.KineticLayout;
import dev.xyat.kineticcore.api.client.gui.overlay.KineticOverlays;
import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.scroll.KineticScrollAnimator;
import dev.xyat.kineticcore.api.client.gui.scroll.KineticScrollController;
import dev.xyat.kineticcore.api.client.gui.state.LayerState;
import dev.xyat.kineticcore.api.client.gui.text.KineticText;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.ui.NumberType;
import dev.xyat.kineticcore.api.client.gui.widget.KineticButton;
import dev.xyat.kineticcore.api.client.gui.widget.KineticControl;
import dev.xyat.kineticcore.api.client.gui.widget.KineticNumberField;
import dev.xyat.kineticcore.api.client.gui.widget.KineticTextField;
import dev.xyat.kineticcore.api.client.input.KineticMouseButtons;
import dev.xyat.kineticcore.api.client.search.KineticSearch;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import java.text.NumberFormat;
import java.util.*;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * The wallet shop: a three-column storefront on the 640×360 canvas.
 * <ul>
 *   <li>Left: the shop's pages as a vertical list (favourites, all, then every page with its entry count) and the
 *   wallet balances underneath.</li>
 *   <li>Middle: search, then the products as cards (icon, name, status, price) that fill the width in whole columns.</li>
 *   <li>Right: the selected product – large icon, price, quest requirement, payment sources and limits, reward preview,
 *   description – with the amount and the trade button pinned to the bottom.</li>
 * </ul>
 * The three columns are layout-editor regions, so players can drag the lines between them.
 */
public class ShopScreen extends KineticPage {
    private enum OverlayLayer {
        REWARD_PICKER,
        QUEST_PICKER,
        CHOICE_OVERLAY
    }

    // ---- geometry (canvas pixels) ----------------------------------------------------------------------------------
    private static final int CANVAS_W = 640;
    private static final int CANVAS_H = 360;
    private static final int MARGIN = 4;
    private static final int HEADER_Y = 4;
    private static final int BODY_Y = 28;
    private static final int CONTROL_H = KineticPage.CONTROL_HEIGHT;
    private static final int TOP_BUTTON_W = 40;
    private static final int EDITOR_MENU_W = 44;
    private static final int SOURCE_MENU_W = 64;
    private static final int COLUMN_GAP = 6;
    private static final int SIDEBAR_W = 96;
    private static final int DETAIL_W = 190;
    private static final int PAGE_ROW_H = 16;
    private static final int CURRENCY_ROW_H = 18;
    private static final int MAX_CURRENCIES = 8;
    // Wide enough that the price, quest requirement and limit lines read without scrolling.
    private static final int CELL_MIN_W = 132;
    private static final int CELL_H = 36;
    private static final int CELL_GAP = 2;
    private static final int CELL_PITCH = CELL_H + CELL_GAP;
    private static final int CELL_SLOT = dev.xyat.kineticcore.api.client.gui.theme.KineticSizes.ITEM_SLOT;
    private static final int SCROLLBAR_W = 4;
    private static final int DETAIL_PAD = 6;
    private static final int HERO_SLOT = 36;
    private static final int PRICE_SLOT = 16;
    private static final int STEP_BUTTON_W = 18;
    private static final int AMOUNT_FIELD_W = 32;
    private static final int REWARD_PICKER_VISIBLE_ROWS = 5;
    private static final int REWARD_PICKER_ROW_HEIGHT = 20;
    private static final int QUEST_PICKER_VISIBLE_ROWS = 5;
    private static final int QUEST_PICKER_ROW_HEIGHT = 20;
    private static final int REWARD_PREVIEW_COLUMNS = 2;
    private static final int REWARD_PREVIEW_SLOT_SIZE = 16;
    private static final int REWARD_PREVIEW_ROW_HEIGHT = REWARD_PREVIEW_SLOT_SIZE + 2;
    private static final int REWARD_PREVIEW_MAX_VISIBLE_ROWS = 3;
    private static final int REWARD_PREVIEW_HEADER_HEIGHT = 16;
    private static final int REWARD_PREVIEW_CELL_GAP = 4;
    // Reward choices use the product slot size with 2 px gaps; 14 columns keep the header wide enough for Back, the
    // title and Confirm.
    private static final int CHOICE_OVERLAY_COLUMNS = 14;
    private static final int CHOICE_OVERLAY_SLOT = CELL_SLOT;
    private static final int CHOICE_OVERLAY_GAP = 2;
    private static final int CHOICE_OVERLAY_VISIBLE_ROWS = 5;
    private static final int CHOICE_OVERLAY_BUTTON_WIDTH = 86;
    private static final String FAVORITES_PAGE = "\u0001favorites";
    private static final String ALL_PAGE = "\u0001all";
    private static final ShopMemory SHOP_MEMORY = new ShopMemory();

    private final boolean returnToParent;
    private final LayerState<OverlayLayer> overlayLayers = new LayerState<>();

    private CompoundTag balances;
    private CompoundTag shopTag;
    private boolean editorMode;
    private boolean canEdit;
    private boolean useBackpackSource;
    private boolean useRsSource;
    private Shop.Mode mode = Shop.Mode.BUY;
    private long shopReceivedAt;

    // The three columns; build() resolves them through the layout editor.
    private KineticLayout.Rect sidebar = new KineticLayout.Rect(MARGIN, BODY_Y, SIDEBAR_W, CANVAS_H - MARGIN - BODY_Y);
    private KineticLayout.Rect products = new KineticLayout.Rect(MARGIN + SIDEBAR_W + COLUMN_GAP, BODY_Y,
            CANVAS_W - MARGIN - DETAIL_W - COLUMN_GAP - (MARGIN + SIDEBAR_W + COLUMN_GAP), CANVAS_H - MARGIN - BODY_Y);
    private KineticLayout.Rect detail = new KineticLayout.Rect(CANVAS_W - MARGIN - DETAIL_W, BODY_Y, DETAIL_W, CANVAS_H - MARGIN - BODY_Y);

    private int selectedIndex = -1;
    private int amount = 1;
    private String searchQuery = "";
    private String activePageName = ALL_PAGE;
    private final List<String> pages = new ArrayList<>();
    private final Map<String, Integer> pageCounts = new HashMap<>();
    private int favoriteCount;
    private int allCount;
    private final List<Row> rows = new ArrayList<>();
    private final Map<String, BalanceAnimation> animations = new HashMap<>();
    private final Map<String, Integer> selectedRewardIndices = new HashMap<>();
    private final Map<Long, String> clientQuestTitleCache = new HashMap<>();
    private final Map<String, String> searchTextCache = new HashMap<>();
    private long clientQuestTitleCacheAt;

    private final KineticScrollController productScroller = new KineticScrollController()
            .bindSelection(() -> selectedIndex, index -> Math.max(0, index / gridColumns() - visibleGridRows() / 2));
    private final KineticScrollController pageScroller = new KineticScrollController()
            .bindSelection(this::selectedPageRow, row -> Math.max(0, row - visiblePageRows() / 2));
    private final KineticScrollController rewardPreviewScroller = new KineticScrollController();
    private final KineticScrollController questPickerScroller = new KineticScrollController();
    private final KineticScrollController choiceOverlayScroller = new KineticScrollController();
    private final KineticScrollAnimator rewardPickerScrollSmoothing = new KineticScrollAnimator();
    private double rewardPickerScroll;
    private boolean rewardPreviewExpanded;
    private int choiceOverlaySelectedIndex = -1;

    private KineticButton buyModeButton;
    private KineticButton sellModeButton;
    private KineticButton editorModeButton;
    private KineticButton backpackSourceButton;
    private KineticButton tradeButton;
    private KineticButton questButton;
    private KineticButton questListButton;
    private KineticButton amountMinusButton;
    private KineticButton amountPlusButton;
    private KineticButton amountTenButton;
    private KineticButton amountMaxButton;
    private KineticButton rewardPreviewToggleButton;
    private KineticNumberField amountBox;
    private KineticTextField searchBox;

    private Shop.Entry contextEntry;
    private Shop.Entry dragSourceEntry;
    private Shop.Entry dragTargetEntry;
    private int dragMouseX;
    private int dragMouseY;
    private String pendingSelectKey;
    // Remembered scroll offsets, applied once the first build knows how far the lists can scroll.
    private int restoreProductScroll = -1;
    private int restorePageScroll = -1;

    public ShopScreen(boolean returnToParent, CompoundTag balances, CompoundTag shopTag, boolean editorMode) {
        super(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_title"));
        this.returnToParent = returnToParent;
        setPausesGame(false);
        useCanvas(CANVAS_W, CANVAS_H, MARGIN);
        this.balances = balances == null ? new CompoundTag() : balances.copy();
        this.shopTag = shopTag == null ? new CompoundTag() : shopTag.copy();
        this.editorMode = editorMode;
        this.canEdit = dev.xyat.adventuresystems.data.Nbt.bool(this.shopTag, "CanEdit");
        this.useBackpackSource = dev.xyat.adventuresystems.data.Nbt.bool(this.shopTag, "UseBackpack");
        this.useRsSource = dev.xyat.adventuresystems.data.Nbt.bool(this.shopTag, "UseRs");
        this.shopReceivedAt = System.currentTimeMillis();
        applySessionMemory();
        bindDeleteAction(() -> editorMode && canEdit && overlayLayers.activeLayer() == null && selectedEntry() != null, () -> {
            contextEntry = selectedEntry();
            deleteContextEntry();
        });
    }

    // ---- session memory and server updates ----------------------------------------------------------------------

    private void applySessionMemory() {
        if (!SHOP_MEMORY.valid) return;
        this.mode = SHOP_MEMORY.mode == null ? Shop.Mode.BUY : SHOP_MEMORY.mode;
        this.activePageName = SHOP_MEMORY.activePageName == null || SHOP_MEMORY.activePageName.isBlank()
                ? ALL_PAGE
                : SHOP_MEMORY.activePageName;
        this.searchQuery = SHOP_MEMORY.searchQuery == null ? "" : SHOP_MEMORY.searchQuery;
        this.restoreProductScroll = Math.max(0, SHOP_MEMORY.scroll);
        this.restorePageScroll = Math.max(0, SHOP_MEMORY.pageScroll);
        this.selectedIndex = Math.max(-1, SHOP_MEMORY.selectedIndex);
        this.amount = Math.max(1, Math.min(64, SHOP_MEMORY.amount));
    }

    private void rememberSessionPosition() {
        SHOP_MEMORY.valid = true;
        SHOP_MEMORY.mode = this.mode;
        SHOP_MEMORY.activePageName = this.activePageName;
        SHOP_MEMORY.searchQuery = this.searchQuery;
        SHOP_MEMORY.scroll = this.productScroller.offset();
        SHOP_MEMORY.pageScroll = this.pageScroller.offset();
        SHOP_MEMORY.selectedIndex = this.selectedIndex;
        SHOP_MEMORY.amount = this.amount;
    }

    @Override
    protected void onRemoved() {
        rememberSessionPosition();
    }

    public void updateShop(CompoundTag balances, CompoundTag shopTag, boolean editorMode) {
        CompoundTag nextShopTag = shopTag == null ? new CompoundTag() : shopTag.copy();
        boolean nextCanEdit = dev.xyat.adventuresystems.data.Nbt.bool(nextShopTag, "CanEdit");
        boolean changed = this.editorMode != editorMode || this.canEdit != nextCanEdit;
        Shop.Entry selected = selectedEntry();
        String selectedKey = selected == null ? "" : selected.key();
        updateBalances(balances);
        this.shopTag = nextShopTag;
        this.editorMode = editorMode;
        this.canEdit = nextCanEdit;
        this.useBackpackSource = dev.xyat.adventuresystems.data.Nbt.bool(this.shopTag, "UseBackpack");
        this.useRsSource = dev.xyat.adventuresystems.data.Nbt.bool(this.shopTag, "UseRs");
        this.shopReceivedAt = System.currentTimeMillis();
        this.searchTextCache.clear();
        closeShopContextMenu();
        clearDragState();
        if (isAttached()) {
            rebuildRows();
            selectRowByKey(selectedKey);
            if (selectedKey.startsWith("local:")) selectNewestRow();
            updateDetailWidgetState();
        }
        if (changed && isAttached()) rebuild();
    }

    public void updateBalances(CompoundTag balances) {
        CompoundTag next = balances == null ? new CompoundTag() : balances.copy();
        long now = System.currentTimeMillis();
        for (CurrencyType currency : Data.currencies()) {
            long oldValue = Data.readAmount(this.balances, currency.itemId());
            long newValue = Data.readAmount(next, currency.itemId());
            if (oldValue != newValue) animations.put(currency.itemId(), new BalanceAnimation(oldValue, newValue, newValue - oldValue, now));
        }
        this.balances = next;
    }

    // ---- build ------------------------------------------------------------------------------------------------------

    /** Every shop page, then the first entry of the open page selected, for screenshot tools. */
    @Override
    public List<ReviewView> reviewViews() {
        List<ReviewView> views = new ArrayList<>();
        for (int index = 0; index < pages.size(); index++) {
            String page = pages.get(index);
            views.add(new ReviewView("page-" + (index + 1), () -> selectPage(page)));
        }
        views.add(new ReviewView("first-entry", () -> selectRow(0)));
        return views;
    }

    @Override
    protected void build(KineticUi ui) {
        int bodyH = CANVAS_H - MARGIN - BODY_Y;
        int productsX = MARGIN + SIDEBAR_W + COLUMN_GAP;
        int detailX = CANVAS_W - MARGIN - DETAIL_W;
        sidebar = clampRegion(region("pages", MARGIN, BODY_Y, SIDEBAR_W, bodyH), 64);
        products = clampRegion(region("products", productsX, BODY_Y, detailX - COLUMN_GAP - productsX, bodyH), CELL_MIN_W + SCROLLBAR_W + 4);
        detail = clampRegion(region("detail", detailX, BODY_Y, DETAIL_W, bodyH), 150);

        addButton(MARGIN, HEADER_Y, TOP_BUTTON_W, AdventureText.translatable("gui.adventuresystems.curios.wallet.back"),
                this::returnToPreviousScreen);
        buyModeButton = ui.button(MARGIN + TOP_BUTTON_W + 4, HEADER_Y, TOP_BUTTON_W)
                .text(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_buy_tab"))
                .tooltip(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_buy_tab"))
                .onClick(() -> switchMode(Shop.Mode.BUY)).build();
        sellModeButton = ui.button(MARGIN + TOP_BUTTON_W * 2 + 6, HEADER_Y, TOP_BUTTON_W)
                .text(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_sell_tab"))
                .tooltip(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_sell_tab"))
                .onClick(() -> switchMode(Shop.Mode.SELL)).build();
        int closeX = CANVAS_W - MARGIN - TOP_BUTTON_W;
        ui.button(closeX, HEADER_Y, TOP_BUTTON_W).text(AdventureText.translatable("gui.adventuresystems.curios.wallet.close"))
                .onClick(this::close).build();
        // The payment-source switches live in "Item sources ▾"; editing actions in "Edit ▾" (editors only).
        backpackSourceButton = ui.button(closeX - 4 - SOURCE_MENU_W, HEADER_Y, SOURCE_MENU_W)
                .text(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_menu_source"))
                .tooltip(() -> Component.empty().append(sourceButtonText(true)).append("\n").append(sourceButtonText(false))
                        .append("\n").append(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_source_priority_tip")))
                .expands().onClick(this::openSourceMenu).build();
        editorModeButton = null;
        if (canEdit) {
            editorModeButton = ui.button(closeX - 8 - SOURCE_MENU_W - EDITOR_MENU_W, HEADER_Y, EDITOR_MENU_W)
                    .text(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_menu_editor"))
                    .tooltip(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_menu_editor_tip"))
                    .expands().onClick(this::openEditorMenu).build();
        }

        searchBox = ui.textField(products.x(), products.y(), products.width())
                .label(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_search"))
                .placeholder(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_search_hint"))
                .firstShownTextAsDefault().build();
        searchBox.limitTextLength(64);
        searchBox.setTextValue(searchQuery);
        searchBox.onTextChange(value -> {
            searchQuery = value == null ? "" : value;
            productScroller.reset();
            selectedIndex = -1;
            closeShopContextMenu();
            clearDragState();
            rebuildRows();
            updateDetailWidgetState();
        });

        int stepY = amountRowY();
        amountMinusButton = addCompactButton(contentX(), stepY, STEP_BUTTON_W,
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_amount_minus"), () -> adjustAmount(-1));
        amountBox = ui.numberField(contentX() + STEP_BUTTON_W + 2, stepY, AMOUNT_FIELD_W, NumberType.INT)
                .label(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_trade_amount_input"))
                .allowNegative(false).range(1, 64).firstShownTextAsDefault().build();
        amountBox.limitTextLength(2);
        amountBox.setIntValue(amount);
        amountBox.onTextChange(this::onAmountInput);
        amountPlusButton = addCompactButton(contentX() + STEP_BUTTON_W + AMOUNT_FIELD_W + 4, stepY, STEP_BUTTON_W,
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_amount_plus"), () -> adjustAmount(1));
        amountTenButton = addCompactButton(quickButtonX(0), stepY, quickButtonWidth(),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_amount_ten"),
                () -> setAmount(Math.min(64, Math.max(1, amount) * 10)));
        amountMaxButton = addCompactButton(quickButtonX(1), stepY, quickButtonWidth(),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_amount_max"),
                () -> setAmount(maxUsefulTradeAmount()));
        tradeButton = addButton(contentX(), tradeButtonY(), contentWidth(), Component.empty(), this::tradeSelectedEntry);
        questButton = addButton(contentX(), questButtonY(), contentWidth(), Component.empty(), this::handleQuestButtonClick);
        questListButton = ui.button(contentX(), questButtonY(), contentWidth()).text(Component.empty()).expands()
                .onClick(this::handleQuestButtonClick).build();
        rewardPreviewToggleButton = addButton(contentX(), 0, CONTROL_H, sectionToggleButtonText(rewardPreviewExpanded),
                this::toggleRewardPreviewSection);

        rebuildRows();
        if (pendingSelectKey != null) {
            String key = pendingSelectKey;
            pendingSelectKey = null;
            selectedIndex = -1;
            selectRowByKey(key);
            if (key.startsWith("local:") && selectedEntry() == null) selectNewestRow();
        }
        if (restoreProductScroll >= 0) {
            renderedScroll();
            productScroller.setOffset(restoreProductScroll);
            restoreProductScroll = -1;
        }
        if (restorePageScroll >= 0) {
            pageListOffset();
            pageScroller.setOffset(restorePageScroll);
            restorePageScroll = -1;
        }
        updateDetailWidgetState();
    }

    /** Keeps a region usable (a minimum width) and inside the body, whatever the stored layout says. */
    private static KineticLayout.Rect clampRegion(KineticLayout.Rect rect, int minWidth) {
        int x = Math.max(MARGIN, Math.min(CANVAS_W - MARGIN - minWidth, rect.x()));
        int y = Math.max(BODY_Y, Math.min(CANVAS_H - MARGIN - 80, rect.y()));
        int width = Math.max(minWidth, Math.min(CANVAS_W - MARGIN - x, rect.width()));
        int height = Math.max(80, Math.min(CANVAS_H - MARGIN - y, rect.height()));
        return new KineticLayout.Rect(x, y, width, height);
    }

    @Override
    protected boolean onCloseRequested() {
        navigateBack();
        return true;
    }

    private void returnToPreviousScreen() {
        if (returnToParent) {
            navigateBack();
            return;
        }
        Network.sendOpen();
    }

    private Component sectionToggleButtonText(boolean expanded) {
        return AdventureText.translatable(expanded
                ? "gui.adventuresystems.curios.wallet.expand_arrow_open"
                : "gui.adventuresystems.curios.wallet.expand_arrow_closed");
    }

    private void switchMode(Shop.Mode mode) {
        this.mode = mode;
        productScroller.reset();
        this.selectedIndex = -1;
        this.activePageName = ALL_PAGE;
        pageScroller.reset();
        this.rewardPreviewExpanded = false;
        this.rewardPickerScroll = 0;
        this.questPickerScroller.reset();
        this.choiceOverlayScroller.reset();
        this.choiceOverlaySelectedIndex = -1;
        closeAllOverlays();
        closeShopContextMenu();
        clearDragState();
        rebuildRows();
        updateDetailWidgetState();
    }

    private boolean isRewardPickerOpen() {
        return overlayLayers.isOpen(OverlayLayer.REWARD_PICKER);
    }

    private boolean isQuestPickerOpen() {
        return overlayLayers.isOpen(OverlayLayer.QUEST_PICKER);
    }

    private boolean isChoiceOverlayOpen() {
        return overlayLayers.isOpen(OverlayLayer.CHOICE_OVERLAY);
    }

    private void closeAllOverlays() {
        overlayLayers.closeAll();
        questPickerScroller.release(MouseButton.LEFT);
        choiceOverlayScroller.release(MouseButton.LEFT);
    }

    /** Places and shows the detail controls for the selected entry; called every frame and after every change. */
    private void updateDetailWidgetState() {
        Shop.Entry entry = selectedEntry();
        if (buyModeButton != null) buyModeButton.setSelected(mode == Shop.Mode.BUY);
        if (sellModeButton != null) sellModeButton.setSelected(mode == Shop.Mode.SELL);
        if (editorModeButton != null) editorModeButton.setSelected(editorMode);
        boolean choiceBuy = entry != null && mode == Shop.Mode.BUY && entry.selectable();
        boolean amountActive = detailTradeControlsVisible(entry);
        if (entry != null && !amountActive && !choiceBuy && amount != 1) {
            amount = 1;
            if (amountBox != null) amountBox.setIntValue(1);
        }
        int stepY = amountRowY();
        place(amountMinusButton, contentX(), stepY, STEP_BUTTON_W, amountActive);
        place(amountBox, contentX() + STEP_BUTTON_W + 2, stepY, AMOUNT_FIELD_W, amountActive);
        place(amountPlusButton, contentX() + STEP_BUTTON_W + AMOUNT_FIELD_W + 4, stepY, STEP_BUTTON_W, amountActive);
        place(amountTenButton, quickButtonX(0), stepY, quickButtonWidth(), amountActive);
        place(amountMaxButton, quickButtonX(1), stepY, quickButtonWidth(), amountActive);
        if (amountBox != null) {
            int enteredAmount = parseCount(amountBox.textValue());
            amountBox.setValidationError(amountActive && (enteredAmount < 1 || !choiceBuy && !canTrade(entry, enteredAmount)));
        }
        if (tradeButton != null) {
            place(tradeButton, contentX(), tradeButtonY(), contentWidth(), entry != null);
            setControlEnabled(tradeButton, entry != null && !entry.locked() && (choiceBuy || canTrade(entry, amount)));
            tradeButton.setText(AdventureText.translatable(tradeActionKey(entry)));
        }
        if (rewardPreviewToggleButton != null) {
            boolean visible = entry != null && entry.gacha() && rewardPreviewY() >= 0;
            place(rewardPreviewToggleButton, contentX(), visible ? rewardPreviewY() : 0, CONTROL_H, visible);
            rewardPreviewToggleButton.setText(sectionToggleButtonText(rewardPreviewExpanded));
        }
        if (questButton != null && questListButton != null) {
            // One quest: the button opens it. Several: the "▾" button lists them.
            boolean single = hasQuestList(entry) && !hasMultipleQuests(entry);
            place(questButton, contentX(), questButtonY(), contentWidth(), single);
            place(questListButton, contentX(), questButtonY(), contentWidth(), hasMultipleQuests(entry));
            questButton.setText(questButtonText(entry));
            questListButton.setText(questButtonText(entry));
            // The button carries its own tooltip: a control without one hides the page's tooltip behind it.
            questButton.setTooltip(single
                    ? AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_quest_click_tip").copy().append("\n")
                            .append(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_quest_title", questTitleById(entry, entry.requiredQuestId())))
                    : Component.empty());
        }
    }

    private String tradeActionKey(Shop.Entry entry) {
        if (entry != null && mode == Shop.Mode.BUY && entry.selectable()) return "gui.adventuresystems.curios.wallet.shop_choice_open_button";
        return mode == Shop.Mode.BUY ? "gui.adventuresystems.curios.wallet.shop_buy" : "gui.adventuresystems.curios.wallet.shop_sell";
    }

    private static void place(KineticControl control, int x, int y, int width, boolean visible) {
        if (control == null) return;
        control.moveControlX(x);
        control.moveControlY(y);
        control.resizeControlWidth(width);
        control.setControlVisible(visible);
        control.setEnabled(visible);
    }

    // Controls can draw above the page-drawn pickers and the reward dialog, so the controls under an open one are
    // hidden while it shows. They are shown again at the start of the next frame, before the per-frame state decides.
    private final List<KineticControl> hiddenUnderOverlay = new ArrayList<>();

    private void restoreControlsUnderOverlay() {
        for (KineticControl control : hiddenUnderOverlay) control.setControlVisible(true);
        hiddenUnderOverlay.clear();
    }

    private void hideControlsUnderOverlay() {
        Shop.Entry entry = selectedEntry();
        boolean choice = isChoiceOverlayOpen();
        boolean quests = isQuestPickerOpen() && hasQuestList(entry);
        boolean rewards = isRewardPickerOpen() && isSelectableRewardEntry(entry);
        if (!choice && !quests && !rewards) return;
        for (KineticControl control : new KineticControl[] {tradeButton, questButton, questListButton, amountMinusButton, amountPlusButton,
                amountTenButton, amountMaxButton, buyModeButton, sellModeButton, editorModeButton, backpackSourceButton,
                amountBox, searchBox, rewardPreviewToggleButton}) {
            if (control == null || !control.controlVisible()) continue;
            if (control == questListButton && quests) continue;
            boolean covered = choice && overlaps(control, choiceOverlayX(), choiceOverlayY(), choiceOverlayWidth(), choiceOverlayHeight())
                    || quests && overlaps(control, contentX(), questPickerY(), contentWidth(), questPickerHeight(entry))
                    || rewards && overlaps(control, contentX(), rewardPickerY(), contentWidth(), rewardPickerHeight(entry));
            if (!covered) continue;
            control.setControlVisible(false);
            hiddenUnderOverlay.add(control);
        }
    }

    private static boolean overlaps(KineticControl control, int x, int y, int width, int height) {
        return control.controlX() < x + width && control.controlX() + control.controlWidth() > x
                && control.controlY() < y + height && control.controlY() + control.controlHeight() > y;
    }

    // ---- rendering ----------------------------------------------------------------------------------------------

    @Override
    protected void renderBackground(@NotNull KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        restoreControlsUnderOverlay();
        updateDetailWidgetState();
        hideControlsUnderOverlay();
        KineticTheme.panel(graphics, 0, 0, CANVAS_W, CANVAS_H);
        renderHeader(graphics);
        renderSidebar(graphics, mouseX, mouseY);
        KineticTheme.verticalSeparator(graphics, products.x() - COLUMN_GAP / 2 - 1, BODY_Y, CANVAS_H - MARGIN - BODY_Y);
        KineticTheme.surface(graphics, detail.x(), detail.y(), detail.width(), detail.height(), KineticTheme.Surface.PANEL_ALT);
        renderEmptyListHint(graphics);
    }

    @Override
    protected void renderForeground(@NotNull KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderProducts(graphics, mouseX, mouseY);
        renderDetail(graphics, mouseX, mouseY);
        renderDragPreview(graphics);
        if (isChoiceOverlayOpen()) renderChoiceOverlay(graphics, mouseX, mouseY);
    }

    private void renderHeader(KineticGraphics graphics) {
        int left = MARGIN + TOP_BUTTON_W * 3 + 10;
        int right = (editorModeButton != null ? editorModeButton.controlX() : backpackSourceButton.controlX()) - 4;
        int centre = CANVAS_W / 2;
        int room = Math.max(0, Math.min(centre - left, right - centre) * 2);
        Component title = editorMode
                ? title().copy().append(" ").append(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_badge"))
                : title();
        graphics.scrollingTextCentered(title, centre, HEADER_Y + 4, room, KineticTheme.current().text(), true);
        KineticTheme.separator(graphics, MARGIN, BODY_Y - 4, CANVAS_W - MARGIN * 2);
    }

    // ---- sidebar: pages and balances ------------------------------------------------------------------------------

    private List<String> sidebarPages() {
        List<String> list = new ArrayList<>(pages.size() + 2);
        list.add(FAVORITES_PAGE);
        list.add(ALL_PAGE);
        list.addAll(pages);
        return list;
    }

    private int pageListTop() {
        return sidebar.y() + 12;
    }

    private int pageListBottom() {
        return walletTop() - 6;
    }

    private int visiblePageRows() {
        return Math.max(1, (pageListBottom() - pageListTop()) / PAGE_ROW_H);
    }

    private int selectedPageRow() {
        return sidebarPages().indexOf(activePageName);
    }

    private double pageListOffset() {
        int viewport = pageListBottom() - pageListTop();
        pageScroller.updateContent(sidebarPages().size() * PAGE_ROW_H, viewport, PAGE_ROW_H);
        return pageScroller.smoothOffset() * PAGE_ROW_H;
    }

    private boolean pageListScrolls() {
        return sidebarPages().size() * PAGE_ROW_H > pageListBottom() - pageListTop();
    }

    private int pageRowWidth() {
        return sidebar.width() - (pageListScrolls() ? SCROLLBAR_W + 2 : 0);
    }

    private int walletRows() {
        return Math.max(1, Math.min(MAX_CURRENCIES, Data.currencies().size()));
    }

    private int walletTop() {
        return sidebar.y() + sidebar.height() - 12 - walletRows() * CURRENCY_ROW_H;
    }

    private void renderSidebar(KineticGraphics graphics, int mouseX, int mouseY) {
        int x = sidebar.x();
        int width = sidebar.width();
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_pages_title"), x + 2, sidebar.y() + 2,
                width - 4, KineticTheme.current().text(), true);
        int top = pageListTop();
        int bottom = pageListBottom();
        double offset = pageListOffset();
        int rowWidth = pageRowWidth();
        List<String> list = sidebarPages();
        graphics.scissor(x, top, x + width, bottom);
        try {
            for (int i = 0; i < list.size(); i++) {
                int rowY = top + i * PAGE_ROW_H - (int) Math.round(offset);
                if (rowY + PAGE_ROW_H <= top || rowY >= bottom) continue;
                String page = list.get(i);
                boolean active = Objects.equals(page, activePageName);
                boolean hovered = mouseY >= top && mouseY < bottom && isHover(mouseX, mouseY, x, rowY, rowWidth, PAGE_ROW_H - 1);
                if (active || hovered) {
                    KineticTheme.stateSurface(graphics, x, rowY, rowWidth, PAGE_ROW_H - 1, KineticTheme.Surface.PANEL_ALT, active, hovered, false);
                }
                String count = Integer.toString(pageCount(page));
                int countWidth = graphics.textWidth(count);
                int textColor = active ? KineticTheme.current().translatedText() : KineticTheme.current().text();
                graphics.scrollingText(pageLabel(page), x + 4, rowY + 4, Math.max(0, rowWidth - 12 - countWidth), textColor, true);
                graphics.text(count, x + rowWidth - 3 - countWidth, rowY + 4, 0xFF8C8C8C, false);
            }
        } finally {
            graphics.endScissor();
        }
        if (pageListScrolls()) {
            pageScroller.render(graphics, mouseX, mouseY, x + width - SCROLLBAR_W, top, SCROLLBAR_W, bottom - top, 12);
        }
        renderWallet(graphics);
    }

    private int pageCount(String page) {
        if (Objects.equals(page, FAVORITES_PAGE)) return favoriteCount;
        if (Objects.equals(page, ALL_PAGE)) return allCount;
        return pageCounts.getOrDefault(page, 0);
    }

    private String pageAt(int mouseX, int mouseY) {
        int top = pageListTop();
        int bottom = pageListBottom();
        if (mouseX < sidebar.x() || mouseX >= sidebar.x() + pageRowWidth() || mouseY < top || mouseY >= bottom) return null;
        int index = (int) Math.floor((mouseY - top + pageListOffset()) / PAGE_ROW_H);
        List<String> list = sidebarPages();
        return index >= 0 && index < list.size() ? list.get(index) : null;
    }

    private void renderWallet(KineticGraphics graphics) {
        int x = sidebar.x();
        int width = sidebar.width();
        int y = walletTop();
        KineticTheme.separator(graphics, x, y - 4, width);
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.balance_title"), x + 2, y + 1, width - 4,
                KineticTheme.current().text(), true);
        List<CurrencyType> currencies = sortedCurrenciesByValueDesc();
        for (int i = 0; i < Math.min(currencies.size(), MAX_CURRENCIES); i++) {
            CurrencyType currency = currencies.get(i);
            int rowY = y + 12 + i * CURRENCY_ROW_H;
            KineticTheme.itemSlot(graphics, x, rowY, 16, 4, false);
            KineticTheme.item(graphics, stack(currency.itemId()), x, rowY, 16, 0.75F, false);
            int textX = x + 20;
            int available = x + width - 2 - textX;
            BalanceAnimation animation = animations.get(currency.itemId());
            String delta = animation != null && animation.deltaVisible() ? formatDelta(animation.delta()) : "";
            int deltaWidth = delta.isEmpty() ? 0 : graphics.textWidth(delta) + 3;
            graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_balance_amount",
                    formatCompact(displayAmount(currency.itemId()))), textX, rowY + 4, Math.max(0, available - deltaWidth), KineticTheme.current().text(), true);
            if (!delta.isEmpty()) {
                graphics.scrollingTextRight(AdventureText.translatable(animation.delta() > 0L
                                ? "gui.adventuresystems.curios.wallet.shop_balance_delta_positive"
                                : "gui.adventuresystems.curios.wallet.shop_balance_delta_negative", delta),
                        x + width - 2, rowY + 4, deltaWidth, KineticTheme.current().text(), true);
            }
        }
    }

    private CurrencyType currencyAt(int mouseX, int mouseY) {
        int y = walletTop() + 12;
        if (mouseX < sidebar.x() || mouseX >= sidebar.x() + sidebar.width() || mouseY < y) return null;
        int index = (mouseY - y) / CURRENCY_ROW_H;
        List<CurrencyType> currencies = sortedCurrenciesByValueDesc();
        return index < Math.min(currencies.size(), MAX_CURRENCIES) ? currencies.get(index) : null;
    }

    // ---- products ---------------------------------------------------------------------------------------------------

    private int gridX() {
        return products.x();
    }

    private int gridTop() {
        return products.y() + CONTROL_H + 4;
    }

    private int gridHeight() {
        return Math.max(CELL_H, products.y() + products.height() - gridTop());
    }

    private int gridWidth() {
        return products.width() - SCROLLBAR_W - 4;
    }

    private int gridColumns() {
        return Math.max(1, (gridWidth() + CELL_GAP) / (CELL_MIN_W + CELL_GAP));
    }

    /** Cards share the width in whole columns, so the grid always reaches the scrollbar. */
    private int cellWidth() {
        int columns = gridColumns();
        return (gridWidth() - (columns - 1) * CELL_GAP) / columns;
    }

    private int visibleGridRows() {
        return Math.max(1, gridHeight() / CELL_PITCH);
    }

    private int contentHeight() {
        int columns = gridColumns();
        int rowCount = (rows.size() + columns - 1) / columns;
        return rowCount <= 0 ? 0 : rowCount * CELL_PITCH - CELL_GAP;
    }

    private int renderedScroll() {
        productScroller.updateContent(contentHeight(), gridHeight(), CELL_PITCH);
        return (int) Math.round(productScroller.smoothOffset() * CELL_PITCH);
    }

    private Cell cellForIndex(int index) {
        int columns = gridColumns();
        int x = gridX() + (index % columns) * (cellWidth() + CELL_GAP);
        int y = gridTop() + (index / columns) * CELL_PITCH - renderedScroll();
        return new Cell(x, y);
    }

    private int visibleIndexStart() {
        int row = Math.max(0, renderedScroll() / CELL_PITCH);
        return Math.min(rows.size(), row * gridColumns());
    }

    private int visibleIndexEnd() {
        int row = (renderedScroll() + gridHeight()) / CELL_PITCH + 1;
        return Math.min(rows.size(), row * gridColumns());
    }

    private void renderEmptyListHint(KineticGraphics graphics) {
        if (!rows.isEmpty()) return;
        Component text = AdventureText.translatable(Objects.equals(activePageName, FAVORITES_PAGE)
                ? "gui.adventuresystems.curios.wallet.shop_favorites_empty"
                : "gui.adventuresystems.curios.wallet.shop_page_empty");
        graphics.scrollingTextCentered(text, gridX() + gridWidth() / 2, gridTop() + gridHeight() / 2 - 4, gridWidth() - 8,
                KineticTheme.current().text(), true);
    }

    private void renderProducts(KineticGraphics graphics, int mouseX, int mouseY) {
        int top = gridTop();
        int bottom = top + gridHeight();
        int width = cellWidth();
        graphics.scissor(gridX(), top, gridX() + gridWidth(), bottom);
        try {
            for (int index = visibleIndexStart(); index < visibleIndexEnd(); index++) {
                Cell cell = cellForIndex(index);
                Shop.Entry entry = rows.get(index).entry();
                if (entry == null) continue;
                if (dragSourceEntry != null && entry.index() == dragSourceEntry.index()) continue;
                boolean hovered = mouseY >= top && mouseY < bottom && isHover(mouseX, mouseY, cell.x(), cell.y(), width, CELL_H);
                renderCard(graphics, entry, cell, width, index == selectedIndex, hovered);
                productScroller.renderSelectionFlash(graphics, index, cell.x(), cell.y(), width, CELL_H);
            }
        } finally {
            graphics.endScissor();
        }
        productScroller.render(graphics, mouseX, mouseY, gridX() + gridWidth() + 4, top, SCROLLBAR_W, gridHeight(), 20);
    }

    /**
     * One product card: the item on the left; the name, a status line (quest, cooldown, limit or reward kind) and the
     * price with its currency on the right; a star in the corner for favourites. Only a product that cannot be had at
     * all (quest missing, sold out) gets the red frame; one the wallet cannot pay for yet shows its price in red.
     */
    private void renderCard(KineticGraphics graphics, Shop.Entry entry, Cell cell, int width, boolean selected, boolean hovered) {
        boolean canTrade = canTrade(entry, 1);
        boolean blocked = entry.locked() || mode == Shop.Mode.BUY && entry.totalLimit() > 0 && totalRemaining(entry) <= 0;
        int x = cell.x();
        int y = cell.y();
        KineticTheme.stateSurface(graphics, x, y, width, CELL_H, KineticTheme.Surface.PANEL_ALT, selected, hovered, blocked);
        graphics.clipped(x + 1, y + 1, x + width - 1, y + CELL_H - 1, () -> {
            int slotY = y + (CELL_H - CELL_SLOT) / 2;
            KineticTheme.itemSlot(graphics, x + 3, slotY, CELL_SLOT, 4, false);
            ItemStack display = cellDisplayStack(entry);
            KineticTheme.item(graphics, display, x + 3, slotY, CELL_SLOT, 1.0F, !hasMultipleRewards(entry));
            if (hasMultipleRewards(entry)) renderSmallPlus(graphics, display, x + 3, slotY, CELL_SLOT);
            int textX = x + CELL_SLOT + 7;
            boolean favorite = isFavorite(entry);
            int nameWidth = x + width - 3 - textX - (favorite ? 9 : 0);
            graphics.scrollingText(entryDisplayName(entry, cellContentStack(entry)), textX, y + 3, Math.max(0, nameWidth),
                    KineticTheme.current().text(), true);
            if (favorite) graphics.text("★", x + width - 9, y + 3, 0xFFFFD34D, true);
            renderCellStatus(graphics, entry, textX, y + 14, x + width - 3 - textX);
            KineticTheme.item(graphics, stack(entry.currencyId()), textX - 1, y + 24, 10, 0.625F, false);
            graphics.scrollingText(cellPriceText(formatCompact(entry.price()), canTrade), textX + 11, y + 25,
                    Math.max(0, x + width - 3 - textX - 11), KineticTheme.current().text(), false);
        });
    }

    private void renderCellStatus(KineticGraphics graphics, Shop.Entry entry, int x, int y, int available) {
        Component primary = cellPrimaryStatusText(entry);
        Component limit = cellLimitStatusText(entry);
        if (primary != null && limit != null) {
            // Fixed halves, so the limit text starts at the same place in every language; each half scrolls.
            int half = Math.max(8, (available - 3) / 2);
            drawCellString(graphics, primary, x, y, half);
            drawCellString(graphics, limit, x + half + 3, y, Math.max(1, available - half - 3));
            return;
        }
        Component line = primary != null ? primary : limit;
        if (line == null && entry.gacha()) line = AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_gacha_marker");
        if (line == null && entry.selectable()) line = AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_choice_marker");
        if (line != null) drawCellString(graphics, line, x, y, available);
    }

    private Component cellPrimaryStatusText(Shop.Entry entry) {
        if (entry.requiredQuestIds() != null && !entry.requiredQuestIds().isEmpty()) {
            if (!entry.locked()) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_cell_quest_done");
            return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_cell_quest_need", missingRequiredQuestCount(entry));
        }
        if (entry.timedLimitSeconds() > 0) {
            long remaining = timedRemaining(entry);
            if (remaining <= 0L) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_cell_timed_ready");
            return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_cell_timed_wait", cellDurationText(remaining));
        }
        return null;
    }

    private Component cellLimitStatusText(Shop.Entry entry) {
        if (entry.totalLimit() <= 0) return null;
        int remaining = totalRemaining(entry);
        if (remaining <= 0) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_cell_limit_sold_out");
        return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_cell_limit_left", remaining, entry.totalLimit());
    }

    private void drawCellString(KineticGraphics graphics, Component text, int x, int y, int available) {
        if (text == null || text.getString().isBlank()) return;
        graphics.scrollingText(text, x, y, Math.max(0, available), KineticTheme.current().text(), false);
    }

    private Component cellPriceText(String price, boolean available) {
        return AdventureText.translatable(available
                ? "gui.adventuresystems.curios.wallet.shop_cell_price_ready"
                : "gui.adventuresystems.curios.wallet.shop_cell_price_blocked", price);
    }

    /** The multi-reward mark sits in the bottom-right corner; the count stays left of it and durability bars remain. */
    private void renderSmallPlus(KineticGraphics graphics, ItemStack stack, int x, int y, int slotSize) {
        graphics.push();
        graphics.raise(1);
        graphics.translate(x + (slotSize - 14) / 2F, y + (slotSize - 14) / 2F);
        graphics.scale(0.875F, 0.875F);
        graphics.itemDecorations(stack, 0, 0, "");
        graphics.pop();

        Component plus = AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_multi_reward_plus");
        int plusX = x + slotSize - 1 - graphics.textWidth(plus);
        int textY = y + slotSize - 1 - graphics.lineHeight();
        graphics.push();
        graphics.raise(1);
        graphics.text(plus, plusX, textY, KineticTheme.current().translatedText(), true);
        if (stack.getCount() > 1) {
            String count = Integer.toString(stack.getCount());
            int countRight = plusX - 2;
            int available = Math.max(1, countRight - (x + 1));
            int countWidth = graphics.textWidth(count);
            float countScale = Math.min(1F, available / (float) Math.max(1, countWidth));
            graphics.push();
            graphics.translate(countRight - countWidth * countScale, textY + graphics.lineHeight() * (1F - countScale));
            graphics.scale(countScale, countScale);
            graphics.text(count, 0, 0, KineticTheme.current().text(), true);
            graphics.pop();
        }
        graphics.pop();
    }

    private RowClick rowClickAt(int mouseX, int mouseY) {
        int top = gridTop();
        if (mouseX < gridX() || mouseX >= gridX() + gridWidth() || mouseY < top || mouseY >= top + gridHeight()) return null;
        int width = cellWidth();
        for (int i = visibleIndexStart(); i < visibleIndexEnd(); i++) {
            Cell cell = cellForIndex(i);
            if (isHover(mouseX, mouseY, cell.x(), cell.y(), width, CELL_H)) return new RowClick(rows.get(i), i);
        }
        return null;
    }

    private Row rowAt(int mouseX, int mouseY) {
        RowClick click = rowClickAt(mouseX, mouseY);
        return click == null ? null : click.row();
    }

    // ---- detail panel ---------------------------------------------------------------------------------------------

    private int contentX() {
        return detail.x() + DETAIL_PAD;
    }

    private int contentWidth() {
        return detail.width() - DETAIL_PAD * 2;
    }

    private int contentRight() {
        return contentX() + contentWidth();
    }

    private int heroY() {
        return detail.y() + DETAIL_PAD;
    }

    private int heroTextX() {
        return contentX() + HERO_SLOT + 6;
    }

    private int priceRowY() {
        return heroY() + HERO_SLOT + 4;
    }

    private int questButtonY() {
        return priceRowY() + 22;
    }

    private int infoY(Shop.Entry entry) {
        return questButtonY() + (hasQuestList(entry) ? CONTROL_H + 6 : 0);
    }

    private int tradeButtonY() {
        return detail.y() + detail.height() - DETAIL_PAD - CONTROL_H;
    }

    private int amountRowY() {
        return tradeButtonY() - CONTROL_H - 4;
    }

    private int amountLabelY() {
        return amountRowY() - PRICE_SLOT - 4;
    }

    private int quickButtonWidth() {
        int used = STEP_BUTTON_W * 2 + AMOUNT_FIELD_W + 4 + 6;
        return Math.max(20, (contentWidth() - used - 2) / 2);
    }

    private int quickButtonX(int slot) {
        int start = contentX() + STEP_BUTTON_W * 2 + AMOUNT_FIELD_W + 4 + 6;
        return start + slot * (quickButtonWidth() + 2);
    }

    /** The first pixel row the trade controls use; reward previews and descriptions end above it. */
    private int tradeBlockTop(Shop.Entry entry) {
        return detailTradeControlsVisible(entry) ? amountLabelY() - 4 : tradeButtonY() - 4;
    }

    private boolean detailTradeControlsVisible(Shop.Entry entry) {
        if (entry == null || mode == Shop.Mode.BUY && entry.selectable()) return false;
        return entry.totalLimit() <= 0 || totalRemaining(entry) > 1;
    }

    private void renderDetail(KineticGraphics graphics, int mouseX, int mouseY) {
        int x = detail.x();
        int y = detail.y();
        Shop.Entry entry = selectedEntry();
        graphics.scissor(x + 1, y + 1, x + detail.width() - 1, y + detail.height() - 1);
        try {
            if (entry == null) {
                renderNoSelection(graphics);
                return;
            }
            int cx = contentX();
            int width = contentWidth();
            ItemStack item = detailDisplayStack(entry);
            KineticTheme.itemSlot(graphics, cx, heroY(), HERO_SLOT, 4, isSelectableRewardIcon(mouseX, mouseY));
            KineticTheme.item(graphics, item, cx, heroY(), HERO_SLOT, 2.0F, false);
            if (hasMultipleRewards(entry)) {
                Component plus = AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_multi_reward_plus");
                graphics.push();
                graphics.raise(2);
                graphics.text(plus, cx + HERO_SLOT - 2 - graphics.textWidth(plus), heroY() + HERO_SLOT - 10, KineticTheme.current().translatedText(), true);
                graphics.pop();
            }
            int textX = heroTextX();
            int textWidth = contentRight() - textX;
            graphics.scrollingText(AdventureText.translatable(entry.locked()
                            ? "gui.adventuresystems.curios.wallet.shop_detail_name_locked"
                            : "gui.adventuresystems.curios.wallet.shop_detail_name_ready",
                    entryDisplayName(entry, item)), textX, heroY() + 2, textWidth, KineticTheme.current().text(), true);
            graphics.scrollingText(entryCountText(entry), textX, heroY() + 14, textWidth, KineticTheme.current().text(), true);
            graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_detail_page", pageLabel(entry)),
                    textX, heroY() + 26, textWidth, KineticTheme.current().text(), true);

            // Price: text scrolls before a fixed currency slot at the right.
            int iconX = contentRight() - PRICE_SLOT;
            graphics.scrollingText(entryPriceIconText(entry), cx, priceRowY() + 4, Math.max(0, iconX - cx - 3), KineticTheme.current().text(), false);
            KineticTheme.itemSlot(graphics, iconX, priceRowY(), PRICE_SLOT, 4, false);
            KineticTheme.item(graphics, stack(entry.currencyId()), iconX, priceRowY(), PRICE_SLOT, 0.625F, false);
            KineticTheme.separator(graphics, cx, priceRowY() + PRICE_SLOT + 3, width);

            int infoY = infoY(entry);
            if (mode == Shop.Mode.BUY) {
                renderBuyPaymentSourceLines(graphics, entry, cx, infoY);
                renderBuyLimitStatusLines(graphics, entry, cx, infoY + 35);
            } else {
                renderStatusLines(graphics, entry, cx, infoY);
            }
            int descriptionTop = detailStatusEndY(entry) + 4;
            if (entry.gacha()) {
                renderRewardPreview(graphics, entry, cx, rewardPreviewY(), mouseX, mouseY);
                descriptionTop = (rewardPreviewExpanded ? rewardPreviewBottomY() : rewardPreviewY() + REWARD_PREVIEW_HEADER_HEIGHT) + 4;
            }
            renderDescription(graphics, entry, cx, descriptionTop, tradeBlockTop(entry));
            if (detailTradeControlsVisible(entry)) renderTradeCostPreview(graphics, entry);
            if (isRewardPickerOpen() && isSelectableRewardEntry(entry)) {
                graphics.isolated(() -> { graphics.raise(3); renderRewardPicker(graphics, entry, mouseX, mouseY); });
            }
            if (isQuestPickerOpen() && hasQuestList(entry)) {
                graphics.isolated(() -> { graphics.raise(3); renderQuestPicker(graphics, entry, mouseX, mouseY); });
            }
        } finally {
            graphics.endScissor();
        }
    }

    /** Nothing selected: a pointer to the products and how this mode works, wrapped to the panel. */
    private void renderNoSelection(KineticGraphics graphics) {
        int cx = contentX();
        int width = contentWidth();
        int y = detail.y() + detail.height() / 2 - 24;
        graphics.scrollingTextCentered(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_select_entry_hint"),
                cx + width / 2, y, width, KineticTheme.current().text(), true);
        y += 16;
        for (FormattedCharSequence line : KineticText.wrap(AdventureText.translatable(shopHintKey()), width)) {
            graphics.text(line, cx, y, KineticTheme.current().text(), true);
            y += 10;
            if (y > detail.y() + detail.height() - 12) break;
        }
    }

    private String shopHintKey() {
        if (mode == Shop.Mode.SELL) return editorMode ? "gui.adventuresystems.curios.wallet.shop_editor_sell_hint_modal" : "gui.adventuresystems.curios.wallet.shop_sell_hint";
        return editorMode ? "gui.adventuresystems.curios.wallet.shop_editor_hint_modal" : "gui.adventuresystems.curios.wallet.shop_hint";
    }

    /** The entry's description, wrapped, in whatever room is left between the details and the trade controls. */
    private void renderDescription(KineticGraphics graphics, Shop.Entry entry, int x, int top, int bottom) {
        if (entry.description() == null || entry.description().isBlank() || bottom - top < 14) return;
        KineticTheme.separator(graphics, x, top, contentWidth());
        int y = top + 4;
        List<FormattedCharSequence> lines = KineticText.wrap(AdventureText.literal(entry.description()), contentWidth());
        for (int i = 0; i < lines.size(); i++) {
            if (y + 9 > bottom) break;
            boolean lastFitting = y + 19 > bottom && i < lines.size() - 1;
            graphics.text(lastFitting ? FormattedCharSequence.composite(lines.get(i), Component.literal("…").getVisualOrderText()) : lines.get(i),
                    x, y, 0xFFBFBFBF, false);
            y += 10;
        }
    }

    private void renderTradeCostPreview(KineticGraphics graphics, Shop.Entry entry) {
        int y = amountLabelY();
        int iconX = contentRight() - PRICE_SLOT;
        int labelWidth = Math.min(70, contentWidth() / 2);
        graphics.scrollingText(tradeAmountLabel(), contentX(), y + 4, labelWidth, KineticTheme.current().text(), true);
        Component cost = AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_trade_cost_preview", formatCompact(tradeCostAmount(entry)));
        graphics.scrollingTextRight(cost, iconX - 3, y + 4, Math.max(0, iconX - 3 - (contentX() + labelWidth + 4)), KineticTheme.current().text(), false);
        ItemStack stack = tradeCostStack(entry);
        if (!stack.isEmpty()) {
            KineticTheme.itemSlot(graphics, iconX, y, PRICE_SLOT, 4, false);
            KineticTheme.item(graphics, stack, iconX, y, PRICE_SLOT, 0.625F, false);
        }
    }

    private long tradeCostAmount(Shop.Entry entry) {
        if (entry == null) return 0L;
        long perTrade = mode == Shop.Mode.BUY ? entry.price() : Math.max(1, sellTradeStack(entry).getCount());
        return safeMultiply(perTrade, Math.max(1, Math.min(64, amount)));
    }

    private ItemStack tradeCostStack(Shop.Entry entry) {
        if (entry == null) return ItemStack.EMPTY;
        return mode == Shop.Mode.BUY ? stack(entry.currencyId()) : sellTradeStack(entry);
    }

    private void renderStatusLines(KineticGraphics graphics, Shop.Entry entry, int x, int y) {
        int width = contentWidth();
        int lineY = y;
        if (isSelectableRewardEntry(entry)) {
            graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_sell_choice_hint"), x, lineY, width, KineticTheme.current().text(), true);
            lineY += 11;
        }
        long inventoryCount = sellInventoryCount(entry);
        graphics.scrollingText(AdventureText.translatable(inventoryCount > 0L
                ? "gui.adventuresystems.curios.wallet.shop_sell_inventory_count_ready"
                : "gui.adventuresystems.curios.wallet.shop_sell_inventory_count_missing", materialCompact(inventoryCount)), x, lineY, width, KineticTheme.current().text(), true);
        lineY += 11;
        graphics.scrollingText(backpackMaterialText(entry), x, lineY, width, KineticTheme.current().text(), true);
        lineY += 11;
        graphics.scrollingText(rsMaterialText(entry), x, lineY, width, KineticTheme.current().text(), true);
        lineY += 11;
        if (sellProgress(entry) > 0L) {
            graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_sell_progress_detail",
                    materialCompact(sellProgress(entry)), materialCompact(sellTradeStack(entry).getCount())), x, lineY, width, KineticTheme.current().text(), true);
        }
    }

    private void renderBuyLimitStatusLines(KineticGraphics graphics, Shop.Entry entry, int x, int y) {
        int lineY = y;
        if (entry.timedLimitSeconds() > 0) {
            graphics.scrollingText(timedLimitText(entry, timedRemaining(entry)), x, lineY, contentWidth(), KineticTheme.current().text(), true);
            lineY += 11;
        }
        if (entry.totalLimit() > 0) {
            int remaining = totalRemaining(entry);
            graphics.scrollingText(AdventureText.translatable(remaining > 0
                            ? "gui.adventuresystems.curios.wallet.shop_total_limit_status"
                            : "gui.adventuresystems.curios.wallet.shop_total_limit_status_sold_out",
                    entry.totalBought(), entry.totalLimit(), remaining), x, lineY, contentWidth(), KineticTheme.current().text(), true);
        }
    }

    /** Payment sources in two fixed columns (same start in every language; each value scrolls), then the total. */
    private void renderBuyPaymentSourceLines(KineticGraphics graphics, Shop.Entry entry, int x, int y) {
        int half = contentWidth() / 2;
        int rightX = x + half;
        int rightWidth = contentRight() - rightX;
        long inventoryCount = buyInventoryCount(entry);
        long walletCount = buyWalletCount(entry);
        long total = buyPaymentTotal(entry);
        graphics.scrollingText(AdventureText.translatable(inventoryCount > 0L
                ? "gui.adventuresystems.curios.wallet.shop_buy_source_inventory_ready"
                : "gui.adventuresystems.curios.wallet.shop_buy_source_inventory_empty", materialCompact(inventoryCount)), x, y, half - 4, KineticTheme.current().text(), true);
        graphics.scrollingText(AdventureText.translatable(walletCount > 0L
                ? "gui.adventuresystems.curios.wallet.shop_buy_source_wallet_ready"
                : "gui.adventuresystems.curios.wallet.shop_buy_source_wallet_empty", materialCompact(walletCount)), rightX, y, rightWidth, KineticTheme.current().text(), true);
        graphics.scrollingText(buyBackpackMaterialText(entry), x, y + 11, half - 4, KineticTheme.current().text(), true);
        graphics.scrollingText(buyRsMaterialText(entry), rightX, y + 11, rightWidth, KineticTheme.current().text(), true);
        graphics.scrollingText(AdventureText.translatable(total >= entry.price()
                ? "gui.adventuresystems.curios.wallet.shop_buy_source_total_ready"
                : "gui.adventuresystems.curios.wallet.shop_buy_source_total_missing", materialCompact(total)), x, y + 22, contentWidth(), KineticTheme.current().text(), true);
    }

    private MutableComponent buyBackpackMaterialText(Shop.Entry entry) {
        if (!useBackpackSource) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_buy_source_backpack_disabled");
        if (!entry.backpackLoaded() || !entry.hasBackpack()) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_buy_source_backpack_missing");
        return AdventureText.translatable(entry.backpackCount() > 0L
                ? "gui.adventuresystems.curios.wallet.shop_buy_source_backpack_ready"
                : "gui.adventuresystems.curios.wallet.shop_buy_source_backpack_empty", materialCompact(entry.backpackCount()));
    }

    private MutableComponent buyRsMaterialText(Shop.Entry entry) {
        if (!useRsSource) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_buy_source_rs_disabled");
        if ("BOUND".equals(entry.rsState())) return AdventureText.translatable(entry.rsCount() > 0L
                ? "gui.adventuresystems.curios.wallet.shop_buy_source_rs_ready"
                : "gui.adventuresystems.curios.wallet.shop_buy_source_rs_empty", materialCompact(entry.rsCount()));
        return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_buy_source_rs_unbound");
    }

    /** Where the detail text above the reward preview ends. */
    private int detailStatusEndY(Shop.Entry entry) {
        int y = infoY(entry);
        if (mode == Shop.Mode.BUY) {
            y += 33;
            if (entry.timedLimitSeconds() > 0) y += 11;
            if (entry.totalLimit() > 0) y += 11;
            return y;
        }
        if (isSelectableRewardEntry(entry)) y += 11;
        y += 33;
        if (sellProgress(entry) > 0L) y += 11;
        return y;
    }

    // ---- reward preview (gacha) -------------------------------------------------------------------------------------

    private int rewardPreviewY() {
        Shop.Entry entry = selectedEntry();
        if (entry == null || !entry.gacha()) return -1;
        return detailStatusEndY(entry) + 4;
    }

    private void renderRewardPreview(KineticGraphics graphics, Shop.Entry entry, int x, int y, int mouseX, int mouseY) {
        if (y < 0 || tradeBlockTop(entry) - y < CONTROL_H) return;
        graphics.scrollingText(AdventureText.translatable(entry.selectable()
                        ? (mode == Shop.Mode.SELL
                        ? "gui.adventuresystems.curios.wallet.shop_sell_choice_title"
                        : "gui.adventuresystems.curios.wallet.shop_choice_title")
                        : "gui.adventuresystems.curios.wallet.shop_reward_probability_title"), x + CONTROL_H + 4, y + 4,
                Math.max(0, contentWidth() - CONTROL_H - 4), KineticTheme.current().translatedText(), true);
        if (!rewardPreviewExpanded) return;
        List<Shop.Reward> rewards = sortedRewardPreviewRewards(entry);
        if (rewards.isEmpty()) return;
        int listY = rewardPreviewListY();
        int listHeight = rewardPreviewListHeight();
        if (listHeight < REWARD_PREVIEW_ROW_HEIGHT) return;
        KineticTheme.surface(graphics, x, listY - 3, contentWidth(), rewardPreviewBottomY() - (listY - 3), KineticTheme.Surface.PANEL_ALT);
        double visual = rewardPreviewVisualScroll(entry);
        int start = Math.max(0, (int) Math.floor(visual));
        int shift = (int) Math.round((visual - start) * REWARD_PREVIEW_ROW_HEIGHT);
        boolean scrollbar = rewardPreviewMaxScroll(entry) > 0;
        int listWidth = contentWidth() - 4 - (scrollbar ? SCROLLBAR_W + 4 : 0);
        int cellWidth = (listWidth - REWARD_PREVIEW_CELL_GAP) / REWARD_PREVIEW_COLUMNS;
        int displaySlotCount = rewardPreviewDisplaySlotCount(entry);
        graphics.scissor(x + 2, listY, x + 2 + listWidth, listY + listHeight);
        for (int row = 0; row < rewardPreviewVisibleRows() + 2; row++) {
            int rowY = listY + row * REWARD_PREVIEW_ROW_HEIGHT - shift;
            if (rowY + REWARD_PREVIEW_ROW_HEIGHT <= listY || rowY >= listY + listHeight) continue;
            for (int column = 0; column < REWARD_PREVIEW_COLUMNS; column++) {
                int rewardIndex = rewardPreviewRewardIndexAtDisplaySlot(rewards.size(), (start + row) * REWARD_PREVIEW_COLUMNS + column, displaySlotCount);
                if (rewardIndex < 0) continue;
                renderRewardPreviewCell(graphics, rewards.get(rewardIndex), x + 2 + column * (cellWidth + REWARD_PREVIEW_CELL_GAP), rowY, cellWidth);
            }
        }
        graphics.endScissor();
        if (scrollbar) {
            rewardPreviewScroller.render(graphics, mouseX, mouseY, contentRight() - 2 - SCROLLBAR_W, listY, SCROLLBAR_W, listHeight, 12);
        }
    }

    private void renderRewardPreviewCell(KineticGraphics graphics, Shop.Reward reward, int x, int y, int width) {
        KineticTheme.itemSlot(graphics, x, y, REWARD_PREVIEW_SLOT_SIZE, 4, false);
        ItemStack stack = reward.empty() ? new ItemStack(net.minecraft.world.item.Items.BARRIER) : reward.stack();
        KineticTheme.item(graphics, stack, x, y, REWARD_PREVIEW_SLOT_SIZE, 0.625F, true);
        graphics.scrollingText(Component.literal(percent(reward.chance())), x + REWARD_PREVIEW_SLOT_SIZE + 4, y + 4,
                Math.max(0, width - REWARD_PREVIEW_SLOT_SIZE - 6), KineticTheme.current().translatedText(), true);
    }

    private List<Shop.Reward> sortedRewardPreviewRewards(Shop.Entry entry) {
        if (entry == null || entry.rewards() == null || entry.rewards().isEmpty()) return Collections.emptyList();
        List<Shop.Reward> rewards = new ArrayList<>(entry.rewards());
        rewards.sort(Comparator.comparingDouble((Shop.Reward reward) -> reward == null ? Double.MAX_VALUE : reward.chance()));
        return rewards;
    }

    private int rewardPreviewDisplaySlotCount(Shop.Entry entry) {
        int rewardCount = sortedRewardPreviewRewards(entry).size();
        if (rewardCount == 0) return 0;
        return (rewardCount & 1) == 0 ? rewardCount : rewardCount + 1;
    }

    private int rewardPreviewRewardIndexAtDisplaySlot(int rewardCount, int displaySlot, int displaySlotCount) {
        if (rewardCount <= 0 || displaySlot < 0 || displaySlot >= displaySlotCount) return -1;
        if ((rewardCount & 1) == 1) {
            if (displaySlot == displaySlotCount - 2) return -1;
            if (displaySlot == displaySlotCount - 1) return rewardCount - 1;
        }
        return displaySlot < rewardCount ? displaySlot : -1;
    }

    private int rewardPreviewListY() {
        return rewardPreviewY() + REWARD_PREVIEW_HEADER_HEIGHT + 5;
    }

    private int rewardPreviewTotalRows(Shop.Entry entry) {
        return rewardPreviewDisplaySlotCount(entry) / REWARD_PREVIEW_COLUMNS;
    }

    /** Where the expanded preview ends: its rows (at most three) or the trade controls, whichever comes first. */
    private int rewardPreviewBottomY() {
        Shop.Entry entry = selectedEntry();
        if (entry == null) return detail.y() + detail.height();
        int maximum = tradeBlockTop(entry);
        int rows = Math.min(REWARD_PREVIEW_MAX_VISIBLE_ROWS, rewardPreviewTotalRows(entry));
        return Math.min(maximum, rewardPreviewListY() + rows * REWARD_PREVIEW_ROW_HEIGHT + 2);
    }

    private int rewardPreviewListHeight() {
        return Math.max(0, rewardPreviewBottomY() - 2 - rewardPreviewListY());
    }

    private int rewardPreviewVisibleRows() {
        return Math.min(REWARD_PREVIEW_MAX_VISIBLE_ROWS, Math.max(0, rewardPreviewListHeight() / REWARD_PREVIEW_ROW_HEIGHT));
    }

    private int rewardPreviewMaxScroll(Shop.Entry entry) {
        return Math.max(0, rewardPreviewTotalRows(entry) - rewardPreviewVisibleRows());
    }

    private double rewardPreviewVisualScroll(Shop.Entry entry) {
        rewardPreviewScroller.updateRange(rewardPreviewMaxScroll(entry), rewardPreviewTotalRows(entry), rewardPreviewVisibleRows());
        return rewardPreviewScroller.smoothOffset();
    }

    private boolean rewardPreviewShown() {
        Shop.Entry entry = selectedEntry();
        return rewardPreviewExpanded && entry != null && entry.gacha() && rewardPreviewY() >= 0 && rewardPreviewListHeight() > 0;
    }

    private boolean isInsideRewardPreview(int mouseX, int mouseY) {
        return rewardPreviewShown() && isHover(mouseX, mouseY, contentX(), rewardPreviewListY(), contentWidth(), rewardPreviewListHeight());
    }

    private boolean isInsideRewardPreviewScrollbar(int mouseX, int mouseY) {
        return rewardPreviewShown() && rewardPreviewMaxScroll(selectedEntry()) > 0
                && isHover(mouseX, mouseY, contentRight() - 4 - SCROLLBAR_W, rewardPreviewListY(), SCROLLBAR_W + 4, rewardPreviewListHeight());
    }

    private ItemStack hoveredRewardPreviewItem(int mouseX, int mouseY, Shop.Entry entry) {
        if (!rewardPreviewShown()) return ItemStack.EMPTY;
        int listY = rewardPreviewListY();
        int listHeight = rewardPreviewListHeight();
        if (mouseY < listY || mouseY >= listY + listHeight) return ItemStack.EMPTY;
        List<Shop.Reward> rewards = sortedRewardPreviewRewards(entry);
        boolean scrollbar = rewardPreviewMaxScroll(entry) > 0;
        int listWidth = contentWidth() - 4 - (scrollbar ? SCROLLBAR_W + 4 : 0);
        int cellWidth = (listWidth - REWARD_PREVIEW_CELL_GAP) / REWARD_PREVIEW_COLUMNS;
        double visual = rewardPreviewVisualScroll(entry);
        int start = Math.max(0, (int) Math.floor(visual + 1.0E-6D));
        int shift = (int) Math.round((visual - start) * REWARD_PREVIEW_ROW_HEIGHT);
        int displaySlotCount = rewardPreviewDisplaySlotCount(entry);
        for (int row = 0; row < rewardPreviewVisibleRows() + 2; row++) {
            int rowY = listY + row * REWARD_PREVIEW_ROW_HEIGHT - shift;
            for (int column = 0; column < REWARD_PREVIEW_COLUMNS; column++) {
                int rewardIndex = rewardPreviewRewardIndexAtDisplaySlot(rewards.size(), (start + row) * REWARD_PREVIEW_COLUMNS + column, displaySlotCount);
                if (rewardIndex < 0) continue;
                int cellX = contentX() + 2 + column * (cellWidth + REWARD_PREVIEW_CELL_GAP);
                if (isHover(mouseX, mouseY, cellX, rowY, REWARD_PREVIEW_SLOT_SIZE, REWARD_PREVIEW_SLOT_SIZE)) {
                    Shop.Reward reward = rewards.get(rewardIndex);
                    return reward.empty() ? new ItemStack(net.minecraft.world.item.Items.BARRIER) : reward.stack();
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private void toggleRewardPreviewSection() {
        Shop.Entry entry = selectedEntry();
        if (entry == null || !entry.gacha()) return;
        rewardPreviewExpanded = !rewardPreviewExpanded;
        rewardPreviewScroller.reset();
        updateDetailWidgetState();
    }

    // ---- reward picker (switch the item of a multi-item entry) -----------------------------------------------------

    private boolean isSelectableRewardIcon(int mouseX, int mouseY) {
        return isSelectableRewardEntry(selectedEntry()) && isHover(mouseX, mouseY, contentX(), heroY(), HERO_SLOT, HERO_SLOT);
    }

    private int rewardPickerX() {
        return contentX();
    }

    private int rewardPickerWidth() {
        return contentWidth();
    }

    private int rewardPickerY() {
        // Below the hero icon; below the quest button (2 px clear) when the entry shows one.
        int y = heroY() + HERO_SLOT + 2;
        return hasQuestList(selectedEntry()) ? Math.max(y, questButtonY() + CONTROL_H + 2) : y;
    }

    private int rewardPickerHeight(Shop.Entry entry) {
        int rows = Math.min(REWARD_PICKER_VISIBLE_ROWS, entry == null || entry.rewards() == null ? 0 : entry.rewards().size());
        return 18 + rows * REWARD_PICKER_ROW_HEIGHT + 6;
    }

    private int rewardPickerMaxScroll(Shop.Entry entry) {
        if (!isSelectableRewardEntry(entry)) return 0;
        return Math.max(0, entry.rewards().size() - REWARD_PICKER_VISIBLE_ROWS);
    }

    private boolean isInsideRewardPicker(int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        if (!isRewardPickerOpen() || !isSelectableRewardEntry(entry)) return false;
        return isHover(mouseX, mouseY, rewardPickerX(), rewardPickerY(), rewardPickerWidth(), rewardPickerHeight(entry));
    }

    private void renderRewardPicker(KineticGraphics graphics, Shop.Entry entry, int mouseX, int mouseY) {
        int x = rewardPickerX();
        int y = rewardPickerY();
        int width = rewardPickerWidth();
        int height = rewardPickerHeight(entry);
        // The picker covers the detail text under it, so it gets an opaque backing.
        graphics.fill(x, y, x + width, y + height, 0xFF161616);
        KineticTheme.panelAlt(graphics, x, y, width, height);
        graphics.scrollingText(AdventureText.translatable(mode == Shop.Mode.SELL
                ? "gui.adventuresystems.curios.wallet.shop_sell_choice_picker_title"
                : "gui.adventuresystems.curios.wallet.shop_choice_picker_title"), x + 5, y + 5, Math.max(0, width - 10), KineticTheme.current().text(), true);
        int max = rewardPickerMaxScroll(entry);
        rewardPickerScroll = Math.max(0D, Math.min(rewardPickerScroll, max));
        double visual = rewardPickerScrollSmoothing.follow(rewardPickerScroll, max);
        int start = Math.max(0, Math.min((int) Math.floor(visual), max));
        int shift = (int) Math.round((visual - start) * REWARD_PICKER_ROW_HEIGHT);
        int listY = y + 18;
        int listHeight = Math.min(REWARD_PICKER_VISIBLE_ROWS, entry.rewards().size()) * REWARD_PICKER_ROW_HEIGHT;
        int selected = selectedRewardIndex(entry);
        graphics.scissor(x + 2, listY, x + width - 2, listY + listHeight);
        try {
            for (int row = 0; row < REWARD_PICKER_VISIBLE_ROWS + 2; row++) {
                int index = start + row;
                if (index >= entry.rewards().size()) break;
                int rowY = listY + row * REWARD_PICKER_ROW_HEIGHT - shift;
                if (rowY + REWARD_PICKER_ROW_HEIGHT <= listY || rowY >= y + height) continue;
                boolean hover = isHover(mouseX, mouseY, x + 2, listY, width - 4, listHeight)
                        && isHover(mouseX, mouseY, x + 2, rowY, width - 4, REWARD_PICKER_ROW_HEIGHT);
                boolean active = index == selected;
                KineticTheme.stateSurface(graphics, x + 2, rowY, width - 4, REWARD_PICKER_ROW_HEIGHT - 1,
                        KineticTheme.Surface.PANEL_ALT, active, hover, false);
                Shop.Reward reward = entry.rewards().get(index);
                KineticTheme.itemSlot(graphics, x + 4, rowY + 1, 18, 4, hover);
                if (!reward.empty()) KineticTheme.item(graphics, reward.stack(), x + 4, rowY + 1, 18, 0.75F, true);
                int color = active ? KineticTheme.current().translatedText() : KineticTheme.current().text();
                graphics.scrollingText(rewardName(reward), x + 25, rowY + 6, Math.max(0, width - 34), color, true);
            }
        } finally {
            graphics.endScissor();
        }
    }

    private int rewardPickerIndexAt(int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        if (!isRewardPickerOpen() || !isSelectableRewardEntry(entry)) return -1;
        int listY = rewardPickerY() + 18;
        int listHeight = Math.min(REWARD_PICKER_VISIBLE_ROWS, entry.rewards().size()) * REWARD_PICKER_ROW_HEIGHT;
        if (mouseX < rewardPickerX() + 2 || mouseX >= rewardPickerX() + rewardPickerWidth() - 2 || mouseY < listY || mouseY >= listY + listHeight) return -1;
        double visual = rewardPickerScrollSmoothing.follow(rewardPickerScroll, rewardPickerMaxScroll(entry));
        int index = (mouseY - listY + (int) Math.round(visual * REWARD_PICKER_ROW_HEIGHT)) / REWARD_PICKER_ROW_HEIGHT;
        return index >= 0 && index < entry.rewards().size() ? index : -1;
    }

    private ItemStack hoveredRewardPickerItem(int mouseX, int mouseY) {
        int index = rewardPickerIndexAt(mouseX, mouseY);
        if (index < 0) return ItemStack.EMPTY;
        Shop.Reward reward = selectedEntry().rewards().get(index);
        return reward.empty() ? ItemStack.EMPTY : reward.stack();
    }

    private boolean handleRewardPickerClick(int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        if (!isRewardPickerOpen() || !isSelectableRewardEntry(entry) || !isInsideRewardPicker(mouseX, mouseY)) return false;
        int index = rewardPickerIndexAt(mouseX, mouseY);
        if (index >= 0) {
            selectedRewardIndices.put(entry.key(), index);
            overlayLayers.close(OverlayLayer.REWARD_PICKER);
        }
        return true;
    }

    // ---- reward choice dialog (buy one reward of a choice entry) ----------------------------------------------------

    private void openChoiceOverlay(Shop.Entry entry) {
        if (!isSelectableRewardEntry(entry) || mode != Shop.Mode.BUY || entry.locked()) return;
        closeShopContextMenu();
        overlayLayers.open(OverlayLayer.CHOICE_OVERLAY);
        choiceOverlayVisualScroll(entry);
        choiceOverlaySelectedIndex = -1;
    }

    private void closeChoiceOverlay() {
        overlayLayers.close(OverlayLayer.CHOICE_OVERLAY);
        choiceOverlayScroller.reset();
    }

    private int choiceOverlayWidth() {
        return CHOICE_OVERLAY_COLUMNS * CHOICE_OVERLAY_SLOT + (CHOICE_OVERLAY_COLUMNS - 1) * CHOICE_OVERLAY_GAP + 34;
    }

    private int choiceOverlayHeight() {
        return 32 + CHOICE_OVERLAY_VISIBLE_ROWS * CHOICE_OVERLAY_SLOT + (CHOICE_OVERLAY_VISIBLE_ROWS - 1) * CHOICE_OVERLAY_GAP + 14;
    }

    private int choiceOverlayX() {
        return (CANVAS_W - choiceOverlayWidth()) / 2;
    }

    private int choiceOverlayY() {
        return Math.max(BODY_Y, (CANVAS_H - choiceOverlayHeight()) / 2);
    }

    private int choiceGridX() {
        return choiceOverlayX() + 14;
    }

    private int choiceGridY() {
        return choiceOverlayY() + 32;
    }

    private int choiceGridHeight() {
        return CHOICE_OVERLAY_VISIBLE_ROWS * CHOICE_OVERLAY_SLOT + (CHOICE_OVERLAY_VISIBLE_ROWS - 1) * CHOICE_OVERLAY_GAP;
    }

    private int choiceOverlayMaxScroll(Shop.Entry entry) {
        if (!isSelectableRewardEntry(entry)) return 0;
        int rows = (entry.rewards().size() + CHOICE_OVERLAY_COLUMNS - 1) / CHOICE_OVERLAY_COLUMNS;
        return Math.max(0, rows - CHOICE_OVERLAY_VISIBLE_ROWS);
    }

    private int choiceOverlayButtonY() {
        return choiceOverlayY() + 7;
    }

    private int choiceOverlayCancelX() {
        return choiceOverlayX() + 12;
    }

    private int choiceOverlayConfirmX() {
        return choiceOverlayX() + choiceOverlayWidth() - CHOICE_OVERLAY_BUTTON_WIDTH - 12;
    }

    private int choiceScrollbarX() {
        return choiceOverlayX() + choiceOverlayWidth() - 14;
    }

    private boolean isOutsideChoiceOverlay(int mouseX, int mouseY) {
        return !isChoiceOverlayOpen() || !isHover(mouseX, mouseY, choiceOverlayX(), choiceOverlayY(), choiceOverlayWidth(), choiceOverlayHeight());
    }

    private int choiceOverlayIndexAt(int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        if (!isChoiceOverlayOpen() || !isSelectableRewardEntry(entry)) return -1;
        int localX = mouseX - choiceGridX();
        int localY = mouseY - choiceGridY();
        if (localX < 0 || localY < 0 || localY >= choiceGridHeight()) return -1;
        int pitch = CHOICE_OVERLAY_SLOT + CHOICE_OVERLAY_GAP;
        int column = localX / pitch;
        if (column >= CHOICE_OVERLAY_COLUMNS || localX % pitch >= CHOICE_OVERLAY_SLOT) return -1;
        double visualRow = localY / (double) pitch + choiceOverlayVisualScroll(entry);
        int row = (int) Math.floor(visualRow);
        if ((visualRow - row) * pitch >= CHOICE_OVERLAY_SLOT) return -1;
        int index = row * CHOICE_OVERLAY_COLUMNS + column;
        return index < entry.rewards().size() ? index : -1;
    }

    private void handleChoiceOverlayClick(int mouseX, int mouseY, int button) {
        if (!KineticMouseButtons.isPrimary(button)) return;
        Shop.Entry entry = selectedEntry();
        if (!isSelectableRewardEntry(entry)) {
            closeChoiceOverlay();
            return;
        }
        if (isHover(mouseX, mouseY, choiceOverlayCancelX(), choiceOverlayButtonY(), CHOICE_OVERLAY_BUTTON_WIDTH, CONTROL_H)) {
            closeChoiceOverlay();
            return;
        }
        if (isHover(mouseX, mouseY, choiceOverlayConfirmX(), choiceOverlayButtonY(), CHOICE_OVERLAY_BUTTON_WIDTH, CONTROL_H)) {
            confirmChoiceOverlay();
            return;
        }
        if (choiceOverlayScroller.beginDrag(mouseX, mouseY, MouseButton.of(button), choiceScrollbarX(), choiceGridY(),
                SCROLLBAR_W, choiceGridHeight(), 20, 3)) {
            return;
        }
        int index = choiceOverlayIndexAt(mouseX, mouseY);
        if (index >= 0) {
            choiceOverlaySelectedIndex = index;
            selectedRewardIndices.put(entry.key(), index);
            return;
        }
        if (isOutsideChoiceOverlay(mouseX, mouseY)) closeChoiceOverlay();
    }

    private void confirmChoiceOverlay() {
        Shop.Entry entry = selectedEntry();
        if (entry == null) return;
        if (choiceOverlaySelectedIndex >= 0) confirmChoicePurchase(entry);
        else KineticOverlays.toast("currency_wallet_shop_notice",
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_choice_need_select"),
                KineticOverlays.Position.BOTTOM_CENTER, 2500, 0, -30);
    }

    private void confirmChoicePurchase(Shop.Entry entry) {
        if (!isSelectableRewardEntry(entry)) return;
        int index = Math.max(0, Math.min(choiceOverlaySelectedIndex, entry.rewards().size() - 1));
        selectedRewardIndices.put(entry.key(), index);
        if (!canTrade(entry, amount)) {
            KineticOverlays.toast("currency_wallet_shop_notice", tradeFailText(entry), KineticOverlays.Position.BOTTOM_CENTER, 2500, 0, -30);
            return;
        }
        Network.sendShopBuy(entry.index(), Math.max(1, Math.min(64, amount)) + (index + 1) * 1000);
        closeChoiceOverlay();
    }

    private void renderChoiceOverlay(KineticGraphics graphics, int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        if (!isSelectableRewardEntry(entry)) return;
        int x = choiceOverlayX();
        int y = choiceOverlayY();
        int width = choiceOverlayWidth();
        int height = choiceOverlayHeight();
        graphics.push();
        graphics.raise(5);
        // The page behind the dialog dims, so the choice is the one thing in focus.
        graphics.fill(0, 0, CANVAS_W, CANVAS_H, 0x90000000);
        graphics.fill(x, y, x + width, y + height, 0xFF161616);
        KineticTheme.panel(graphics, x, y, width, height);
        graphics.scrollingTextCentered(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_choice_overlay_title"), x + width / 2,
                choiceOverlayButtonY() + 4, Math.max(0, 2 * (choiceOverlayConfirmX() - 4 - (x + width / 2))), KineticTheme.current().text(), true);
        renderChoiceOverlayGrid(graphics, entry, mouseX, mouseY);
        if (choiceOverlayMaxScroll(entry) > 0) {
            choiceOverlayVisualScroll(entry);
            choiceOverlayScroller.render(graphics, mouseX, mouseY, choiceScrollbarX(), choiceGridY(), SCROLLBAR_W, choiceGridHeight(), 20);
        }
        KineticTheme.button(graphics, choiceOverlayCancelX(), choiceOverlayButtonY(), CHOICE_OVERLAY_BUTTON_WIDTH, CONTROL_H,
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_choice_cancel"),
                isHover(mouseX, mouseY, choiceOverlayCancelX(), choiceOverlayButtonY(), CHOICE_OVERLAY_BUTTON_WIDTH, CONTROL_H), true, false);
        KineticTheme.button(graphics, choiceOverlayConfirmX(), choiceOverlayButtonY(), CHOICE_OVERLAY_BUTTON_WIDTH, CONTROL_H,
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_choice_confirm"),
                isHover(mouseX, mouseY, choiceOverlayConfirmX(), choiceOverlayButtonY(), CHOICE_OVERLAY_BUTTON_WIDTH, CONTROL_H),
                choiceOverlaySelectedIndex >= 0, false);
        graphics.pop();
    }

    private void renderChoiceOverlayGrid(KineticGraphics graphics, Shop.Entry entry, int mouseX, int mouseY) {
        int gridX = choiceGridX();
        int gridY = choiceGridY();
        int pitch = CHOICE_OVERLAY_SLOT + CHOICE_OVERLAY_GAP;
        int selected = choiceOverlaySelectedIndex < 0 ? -1 : Math.min(choiceOverlaySelectedIndex, entry.rewards().size() - 1);
        double visual = choiceOverlayVisualScroll(entry);
        int start = Math.max(0, (int) Math.floor(visual));
        int shift = (int) Math.round((visual - start) * pitch);
        graphics.scissor(gridX, gridY, gridX + CHOICE_OVERLAY_COLUMNS * pitch - CHOICE_OVERLAY_GAP, gridY + choiceGridHeight());
        for (int row = 0; row < CHOICE_OVERLAY_VISIBLE_ROWS + 2; row++) {
            for (int column = 0; column < CHOICE_OVERLAY_COLUMNS; column++) {
                int index = (start + row) * CHOICE_OVERLAY_COLUMNS + column;
                if (index >= entry.rewards().size()) break;
                int slotX = gridX + column * pitch;
                int slotY = gridY + row * pitch - shift;
                if (slotY + CHOICE_OVERLAY_SLOT <= gridY || slotY >= gridY + choiceGridHeight()) continue;
                boolean hover = isHover(mouseX, mouseY, slotX, slotY, CHOICE_OVERLAY_SLOT, CHOICE_OVERLAY_SLOT);
                Shop.Reward reward = entry.rewards().get(index);
                KineticTheme.itemSlot(graphics, slotX, slotY, CHOICE_OVERLAY_SLOT, 4, hover);
                if (index == selected) KineticTheme.stateOutline(graphics, slotX, slotY, CHOICE_OVERLAY_SLOT, CHOICE_OVERLAY_SLOT, true, hover, false);
                if (reward.empty()) {
                    graphics.scrollingTextCentered(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_gacha_marker"),
                            slotX + CHOICE_OVERLAY_SLOT / 2, slotY + (CHOICE_OVERLAY_SLOT - 8) / 2, CHOICE_OVERLAY_SLOT - 4, KineticTheme.current().text(), true);
                } else {
                    KineticTheme.item(graphics, reward.stack(), slotX, slotY, CHOICE_OVERLAY_SLOT, 0.875F, true);
                }
            }
        }
        graphics.endScissor();
    }

    private double choiceOverlayVisualScroll(Shop.Entry entry) {
        int max = choiceOverlayMaxScroll(entry);
        choiceOverlayScroller.updateRange(max, CHOICE_OVERLAY_VISIBLE_ROWS + max, CHOICE_OVERLAY_VISIBLE_ROWS);
        return choiceOverlayScroller.smoothOffset();
    }

    private List<Component> choiceOverlayTooltipAt(int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (!isChoiceOverlayOpen()) return tooltip;
        if (isHover(mouseX, mouseY, choiceOverlayConfirmX(), choiceOverlayButtonY(), CHOICE_OVERLAY_BUTTON_WIDTH, CONTROL_H)) {
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.tooltip_name", AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_choice_confirm")));
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_choice_confirm_tip"));
            return tooltip;
        }
        if (isHover(mouseX, mouseY, choiceOverlayCancelX(), choiceOverlayButtonY(), CHOICE_OVERLAY_BUTTON_WIDTH, CONTROL_H)) {
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.tooltip_name", AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_choice_cancel")));
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_choice_cancel_tip"));
            return tooltip;
        }
        int index = choiceOverlayIndexAt(mouseX, mouseY);
        Shop.Entry entry = selectedEntry();
        if (isSelectableRewardEntry(entry) && index >= 0) {
            Shop.Reward reward = entry.rewards().get(index);
            if (reward.empty() || reward.stack().isEmpty()) {
                tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.tooltip_name", rewardName(reward)));
            }
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_choice_pick_tip"));
            if (reward.commandReward()) tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_reward_command_tooltip"));
        }
        return tooltip;
    }

    // ---- quests -------------------------------------------------------------------------------------------------------

    private boolean hasQuestList(Shop.Entry entry) {
        return entry != null && entry.requiredQuestIds() != null && !entry.requiredQuestIds().isEmpty();
    }

    private boolean hasMultipleQuests(Shop.Entry entry) {
        return hasQuestList(entry) && entry.requiredQuestIds().size() > 1;
    }

    private void handleQuestButtonClick() {
        Shop.Entry entry = selectedEntry();
        if (!hasQuestList(entry)) return;
        if (hasMultipleQuests(entry)) {
            if (isQuestPickerOpen()) overlayLayers.close(OverlayLayer.QUEST_PICKER);
            else overlayLayers.open(OverlayLayer.QUEST_PICKER);
            questPickerVisualScroll(entry);
            return;
        }
        openKtQuest(entry.requiredQuestId());
    }

    private Component questButtonText(Shop.Entry entry) {
        if (!hasQuestList(entry)) return Component.empty();
        Component status = !entry.locked()
                ? AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_task_requirement_done")
                : AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_task_requirement_need", missingRequiredQuestCount(entry));
        Component title = hasMultipleQuests(entry)
                ? AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_task_multiple", entry.requiredQuestIds().size())
                : AdventureText.literal(questTitleById(entry, entry.requiredQuestId()));
        return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_task_button", status, title);
    }

    private int missingRequiredQuestCount(Shop.Entry entry) {
        if (entry == null) return 0;
        return Math.max(1, Math.max(0, entry.requiredQuestNeed()) - Math.max(0, entry.requiredQuestCompleted()));
    }

    private int questPickerY() {
        return questButtonY() + CONTROL_H + 2;
    }

    private int questPickerHeight(Shop.Entry entry) {
        int count = entry == null || entry.requiredQuestIds() == null ? 0 : entry.requiredQuestIds().size();
        return 18 + Math.min(QUEST_PICKER_VISIBLE_ROWS, count) * QUEST_PICKER_ROW_HEIGHT + 6;
    }

    private int questPickerMaxScroll(Shop.Entry entry) {
        if (!hasQuestList(entry)) return 0;
        return Math.max(0, entry.requiredQuestIds().size() - QUEST_PICKER_VISIBLE_ROWS);
    }

    private boolean isInsideQuestPicker(int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        if (!isQuestPickerOpen() || !hasQuestList(entry)) return false;
        return isHover(mouseX, mouseY, contentX(), questPickerY(), contentWidth(), questPickerHeight(entry));
    }

    private int questScrollbarX() {
        return contentRight() - SCROLLBAR_W - 2;
    }

    private int questListTop() {
        return questPickerY() + 18;
    }

    private int questListHeight(Shop.Entry entry) {
        return questPickerHeight(entry) - 18 - 4;
    }

    private boolean isInsideQuestPickerScrollbar(int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        return isQuestPickerOpen() && questPickerMaxScroll(entry) > 0
                && isHover(mouseX, mouseY, questScrollbarX() - 2, questListTop(), SCROLLBAR_W + 4, questListHeight(entry));
    }

    private void renderQuestPicker(KineticGraphics graphics, Shop.Entry entry, int mouseX, int mouseY) {
        int x = contentX();
        int y = questPickerY();
        int width = contentWidth();
        int height = questPickerHeight(entry);
        // The list covers the detail text under it, so it gets an opaque backing under the themed panel.
        graphics.fill(x, y, x + width, y + height, 0xFF161616);
        KineticTheme.panelAlt(graphics, x, y, width, height);
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_task_list_title"), x + 5, y + 5, width - 10, KineticTheme.current().text(), true);
        double visual = questPickerVisualScroll(entry);
        int start = Math.max(0, (int) Math.floor(visual));
        int shift = (int) Math.round((visual - start) * QUEST_PICKER_ROW_HEIGHT);
        int listY = questListTop();
        boolean scrollbar = questPickerMaxScroll(entry) > 0;
        int rowRight = x + width - (scrollbar ? SCROLLBAR_W + 7 : 4);
        graphics.scissor(x + 2, listY, rowRight, y + height);
        try {
            for (int row = 0; row < QUEST_PICKER_VISIBLE_ROWS + 2; row++) {
                int index = start + row;
                if (index >= entry.requiredQuestIds().size()) break;
                long questId = entry.requiredQuestIds().get(index);
                boolean completed = entry.isQuestCompleted(questId);
                int rowY = listY + row * QUEST_PICKER_ROW_HEIGHT - shift;
                if (rowY + QUEST_PICKER_ROW_HEIGHT <= listY || rowY >= y + height) continue;
                boolean hover = isHover(mouseX, mouseY, x + 2, rowY, rowRight - x - 2, QUEST_PICKER_ROW_HEIGHT);
                KineticTheme.stateSurface(graphics, x + 2, rowY, rowRight - x - 2, QUEST_PICKER_ROW_HEIGHT - 1,
                        KineticTheme.Surface.PANEL_ALT, completed, hover, false);
                Component title = AdventureText.translatable(completed
                        ? "gui.adventuresystems.curios.wallet.shop_task_title_done"
                        : hover
                        ? "gui.adventuresystems.curios.wallet.shop_task_title_hover"
                        : "gui.adventuresystems.curios.wallet.shop_task_title_pending", questTitleById(entry, questId));
                graphics.scrollingText(title, x + 6, rowY + 6, Math.max(0, rowRight - 6 - (x + 6)), KineticTheme.current().text(), true);
            }
        } finally {
            graphics.endScissor();
        }
        if (scrollbar) {
            questPickerScroller.render(graphics, mouseX, mouseY, questScrollbarX(), listY, SCROLLBAR_W, questListHeight(entry), 16);
        }
    }

    private Long questAt(int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        if (!isInsideQuestPicker(mouseX, mouseY) || isInsideQuestPickerScrollbar(mouseX, mouseY)) return null;
        int listY = questListTop();
        if (mouseY < listY || mouseY >= listY + questListHeight(entry)) return null;
        int index = (int) Math.floor((mouseY - listY) / (double) QUEST_PICKER_ROW_HEIGHT + questPickerVisualScroll(entry));
        return index >= 0 && index < entry.requiredQuestIds().size() ? entry.requiredQuestIds().get(index) : null;
    }

    private boolean handleQuestPickerClick(int mouseX, int mouseY) {
        if (!isInsideQuestPicker(mouseX, mouseY)) return false;
        Long quest = questAt(mouseX, mouseY);
        if (quest != null) openKtQuest(quest);
        return true;
    }

    private double questPickerVisualScroll(Shop.Entry entry) {
        questPickerScroller.updateRange(questPickerMaxScroll(entry),
                hasQuestList(entry) ? entry.requiredQuestIds().size() : 0, QUEST_PICKER_VISIBLE_ROWS);
        return questPickerScroller.smoothOffset();
    }

    private void openKtQuest(long questId) {
        if (questId == 0L) return;
        try {
            BridgeFTB.openQuest(questId);
        } catch (Throwable ignored) {
            KineticOverlays.toast("currency_wallet_shop_notice", AdventureText.translatable("msg.adventuresystems.curios.wallet.shop_open_task_fail"), KineticOverlays.Position.BOTTOM_CENTER, 2500, 0, -30);
        }
    }

    private String questTitleById(Shop.Entry entry, long questId) {
        if (entry == null || questId == 0L) return "";
        List<Long> ids = entry.requiredQuestIds();
        if (ids != null) {
            for (int i = 0; i < ids.size(); i++) {
                Long id = ids.get(i);
                if (id != null && id == questId) return questTitleAt(entry, i, questId);
            }
        }
        if (entry.requiredQuestId() == questId) {
            String stored = cleanQuestTitle(entry.requiredQuestTitle());
            if (!stored.isBlank()) return stored;
        }
        String resolved = resolveClientQuestTitle(questId);
        return resolved.isBlank() ? String.valueOf(questId) : resolved;
    }

    private String questTitleAt(Shop.Entry entry, int index, long questId) {
        if (entry != null && entry.requiredQuestTitles() != null && index >= 0 && index < entry.requiredQuestTitles().size()) {
            String title = cleanQuestTitle(entry.requiredQuestTitles().get(index));
            if (!title.isBlank()) return title;
        }
        String resolved = resolveClientQuestTitle(questId);
        if (!resolved.isBlank()) return resolved;
        return questId != 0L ? Long.toUnsignedString(questId) : AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_task_fallback_name", index + 1).getString();
    }

    private String requiredQuestTitlesText(Shop.Entry entry) {
        if (entry == null) return "";
        if (entry.requiredQuestIds() != null && !entry.requiredQuestIds().isEmpty()) {
            StringBuilder builder = new StringBuilder();
            int visible = 0;
            for (int i = 0; i < entry.requiredQuestIds().size(); i++) {
                String title = questTitleAt(entry, i, entry.requiredQuestIds().get(i));
                if (title == null || title.isBlank()) continue;
                if (visible >= 3) {
                    builder.append(", ...");
                    break;
                }
                if (!builder.isEmpty()) builder.append(", ");
                builder.append(title);
                visible++;
            }
            if (!builder.isEmpty()) return builder.toString();
        }
        String title = cleanQuestTitle(entry.requiredQuestTitle());
        if (!title.isBlank()) return title;
        long questId = entry.requiredQuestId();
        String resolved = resolveClientQuestTitle(questId);
        if (!resolved.isBlank()) return resolved;
        return questId != 0L ? Long.toUnsignedString(questId) : AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_task_fallback_name", 1).getString();
    }

    private String resolveClientQuestTitle(long questId) {
        if (questId == 0L) return "";
        refreshClientQuestTitleCache(false);
        String title = cleanQuestTitle(clientQuestTitleCache.get(questId));
        if (!title.isBlank()) return title;
        refreshClientQuestTitleCache(true);
        return cleanQuestTitle(clientQuestTitleCache.get(questId));
    }

    private void refreshClientQuestTitleCache(boolean force) {
        long now = System.currentTimeMillis();
        if (!force && now - clientQuestTitleCacheAt < 3000L && !clientQuestTitleCache.isEmpty()) return;
        clientQuestTitleCacheAt = now;
        try {
            for (var ref : BridgeFTB.getAllQuestRefs()) {
                if (ref == null || ref.id() == 0L) continue;
                String title = cleanQuestTitle(ref.title());
                if (title.isBlank()) title = cleanQuestTitle(ref.code());
                if (!title.isBlank()) clientQuestTitleCache.put(ref.id(), title);
            }
        } catch (Throwable ignored) {
        }
    }

    private static String cleanQuestTitle(String text) {
        if (text == null) return "";
        String value = stripQuestFormattingCodes(text).trim();
        return looksLikeQuestId(value) ? "" : value;
    }

    private static String stripQuestFormattingCodes(String text) {
        if (text == null || text.isEmpty()) return "";
        StringBuilder builder = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if ((current == '&' || current == '§') && i + 1 < text.length() && isMinecraftFormatCode(text.charAt(i + 1))) {
                i++;
                continue;
            }
            builder.append(current);
        }
        return builder.toString();
    }

    private static boolean isMinecraftFormatCode(char value) {
        char lower = Character.toLowerCase(value);
        return (lower >= '0' && lower <= '9') || (lower >= 'a' && lower <= 'f') || lower == 'k' || lower == 'l' || lower == 'm' || lower == 'n' || lower == 'o' || lower == 'r';
    }

    private static boolean looksLikeQuestId(String text) {
        if (text == null) return false;
        String value = text.trim();
        if (value.length() < 8) return false;
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) return false;
        }
        return true;
    }

    // ---- tooltips -----------------------------------------------------------------------------------------------------

    @Override
    protected void renderTooltips(int mouseX, int mouseY) {
        showTooltipsAt(mouseX, mouseY);
        anchorTooltip(mouseX, mouseY);
    }

    /** Keeps the tooltip beside the product card or reward slot it describes instead of over it. */
    private void anchorTooltip(int mouseX, int mouseY) {
        if (isChoiceOverlayOpen()) {
            int index = choiceOverlayIndexAt(mouseX, mouseY);
            Shop.Entry entry = selectedEntry();
            if (index < 0 || entry == null) return;
            int pitch = CHOICE_OVERLAY_SLOT + CHOICE_OVERLAY_GAP;
            int x = choiceGridX() + (index % CHOICE_OVERLAY_COLUMNS) * pitch;
            int y = choiceGridY() + (int) Math.round((index / CHOICE_OVERLAY_COLUMNS - choiceOverlayVisualScroll(entry)) * pitch);
            tooltipAnchor(x, y, CHOICE_OVERLAY_SLOT, CHOICE_OVERLAY_SLOT);
            return;
        }
        if (isInsideRewardPicker(mouseX, mouseY)) return;
        Row row = rowAt(mouseX, mouseY);
        if (row == null) return;
        Cell cell = cellForIndex(rows.indexOf(row));
        tooltipAnchor(cell.x(), cell.y(), cellWidth(), CELL_H);
    }

    private void showTooltipsAt(int mouseX, int mouseY) {
        if (isChoiceOverlayOpen()) {
            List<Component> overlayTooltip = choiceOverlayTooltipAt(mouseX, mouseY);
            int index = choiceOverlayIndexAt(mouseX, mouseY);
            Shop.Entry entry = selectedEntry();
            if (isSelectableRewardEntry(entry) && index >= 0) {
                Shop.Reward reward = entry.rewards().get(index);
                if (!reward.empty() && !reward.stack().isEmpty()) {
                    showItemTooltip(reward.stack(), overlayTooltip);
                    return;
                }
            }
            if (!overlayTooltip.isEmpty()) showTooltip(overlayTooltip);
            return;
        }
        if (isInsideRewardPicker(mouseX, mouseY)) {
            ItemStack stack = hoveredRewardPickerItem(mouseX, mouseY);
            if (!stack.isEmpty()) showItemTooltip(stack);
            return;
        }
        ItemStack stack = hoveredItemStackAt(mouseX, mouseY);
        if (!stack.isEmpty()) {
            showItemTooltip(stack, tooltipAt(mouseX, mouseY));
            return;
        }
        List<Component> tooltip = tooltipAt(mouseX, mouseY);
        if (!tooltip.isEmpty()) showTooltip(tooltip);
    }

    private List<Component> tooltipAt(int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        String hoveredPage = pageAt(mouseX, mouseY);
        if (hoveredPage != null) {
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.tooltip_name", pageLabel(hoveredPage)));
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_page_click_tip"));
            return tooltip;
        }
        CurrencyType currency = currencyAt(mouseX, mouseY);
        if (currency != null) {
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_currency_name", ShopGuiSupport.stackNameComponent(currency.itemId())));
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_currency_balance", AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_currency_balance_value", formatExact(amountOf(currency.itemId())))));
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_currency_value", AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_currency_value_value", formatExact(currency.value()))));
            return tooltip;
        }
        Shop.Entry selected = selectedEntry();
        if (selected != null && isQuestPickerOpen()) {
            Long quest = questAt(mouseX, mouseY);
            if (quest != null && quest != 0L) {
                tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_quest_click_tip"));
                tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_quest_title", questTitleById(selected, quest)));
                return tooltip;
            }
            if (isInsideQuestPicker(mouseX, mouseY)) {
                tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_quest_list_click_tip"));
                return tooltip;
            }
        }
        if (detailTradeControlsVisible(selected) && amountBox != null && amountBox.controlVisible()
                && isHover(mouseX, mouseY, amountBox.controlX(), amountBox.controlY(), amountBox.controlWidth(), CONTROL_H)) {
            tooltip.add(tradeAmountTooltipLabel());
            tooltip.add(AdventureText.translatable(mode == Shop.Mode.BUY ? "gui.adventuresystems.curios.wallet.shop_tooltip_amount_input" : "gui.adventuresystems.curios.wallet.shop_tooltip_sell_amount_input"));
            return tooltip;
        }
        if (selected != null && isHover(mouseX, mouseY, contentX(), tradeButtonY(), contentWidth(), CONTROL_H)) {
            Component action = AdventureText.translatable(tradeActionKey(selected));
            if (selected.locked()) {
                tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_action_locked", action));
                tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_need_complete_task", requiredQuestTitlesText(selected)));
            } else {
                tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_action_ready", action));
                tooltip.add(AdventureText.translatable(mode == Shop.Mode.BUY && selected.selectable()
                        ? "gui.adventuresystems.curios.wallet.shop_choice_open_tip"
                        : mode == Shop.Mode.BUY ? "gui.adventuresystems.curios.wallet.shop_tooltip_buy_button" : "gui.adventuresystems.curios.wallet.shop_tooltip_sell_button"));
                long total = mode == Shop.Mode.BUY ? safeMultiply(selected.price(), amount) : safeMultiply(selected.price(), previewSuccessTrades(selected, amount));
                tooltip.add(totalTradeText(total, selected.currencyId()));
                if (mode == Shop.Mode.SELL) tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_sell_inventory_notice"));
            }
            return tooltip;
        }
        if (isSelectableRewardIcon(mouseX, mouseY)) {
            tooltip.add(AdventureText.translatable(mode == Shop.Mode.SELL
                    ? "gui.adventuresystems.curios.wallet.shop_sell_choice_hint"
                    : "gui.adventuresystems.curios.wallet.shop_choice_open_tip"));
        }
        Row row = rowAt(mouseX, mouseY);
        if (row == null || row.entry() == null) return tooltip;
        Shop.Entry entry = row.entry();
        tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_entry_name", entryDisplayName(entry, cellContentStack(entry))));
        appendEntryDescriptionTooltip(tooltip, entry);
        tooltip.add(AdventureText.translatable(mode == Shop.Mode.BUY ? "gui.adventuresystems.curios.wallet.shop_tooltip_click_buy_view" : "gui.adventuresystems.curios.wallet.shop_tooltip_click_sell_view"));
        tooltip.add(AdventureText.translatable(editorMode
                ? "gui.adventuresystems.curios.wallet.shop_tooltip_editor_actions"
                : "gui.adventuresystems.curios.wallet.shop_tooltip_right_favorite"));
        return tooltip;
    }

    private ItemStack hoveredItemStackAt(int mouseX, int mouseY) {
        CurrencyType currency = currencyAt(mouseX, mouseY);
        if (currency != null && mouseX < sidebar.x() + 16) return stack(currency.itemId());
        ItemStack picker = hoveredRewardPickerItem(mouseX, mouseY);
        if (!picker.isEmpty()) return picker;
        Shop.Entry selected = selectedEntry();
        if (selected != null) {
            if (isHover(mouseX, mouseY, contentX(), heroY(), HERO_SLOT, HERO_SLOT)) return detailDisplayStack(selected);
            int iconX = contentRight() - PRICE_SLOT;
            if (isHover(mouseX, mouseY, iconX, priceRowY(), PRICE_SLOT, PRICE_SLOT)) return stack(selected.currencyId());
            if (detailTradeControlsVisible(selected) && isHover(mouseX, mouseY, iconX, amountLabelY(), PRICE_SLOT, PRICE_SLOT)) {
                return tradeCostStack(selected);
            }
            if (selected.gacha()) {
                ItemStack preview = hoveredRewardPreviewItem(mouseX, mouseY, selected);
                if (!preview.isEmpty()) return preview;
            }
        }
        Row row = rowAt(mouseX, mouseY);
        if (row != null && row.entry() != null) {
            Cell cell = cellForIndex(rows.indexOf(row));
            if (isHover(mouseX, mouseY, cell.x() + 3, cell.y() + (CELL_H - CELL_SLOT) / 2, CELL_SLOT, CELL_SLOT)) return cellDisplayStack(row.entry());
        }
        return ItemStack.EMPTY;
    }

    private void appendEntryDescriptionTooltip(List<Component> tooltip, Shop.Entry entry) {
        if (entry == null || entry.description() == null || entry.description().isBlank()) return;
        for (var line : KineticText.wrap(AdventureText.literal(entry.description()), 220)) {
            tooltip.add(AdventureText.fromSequence(line));
        }
    }

    private MutableComponent tradeAmountLabel() {
        return AdventureText.translatable(mode == Shop.Mode.BUY ? "gui.adventuresystems.curios.wallet.shop_trade_amount_label" : "gui.adventuresystems.curios.wallet.shop_sell_times_label");
    }

    private MutableComponent tradeAmountTooltipLabel() {
        return AdventureText.translatable(mode == Shop.Mode.BUY
                ? "gui.adventuresystems.curios.wallet.shop_tooltip_amount_title_buy"
                : "gui.adventuresystems.curios.wallet.shop_tooltip_amount_title_sell");
    }

    private Component entryDisplayName(Shop.Entry entry, ItemStack fallbackStack) {
        if (entry != null && entry.displayName() != null && !entry.displayName().isBlank()) return AdventureText.literal(entry.displayName());
        if (entry != null && entry.command() != null && !entry.command().isBlank()) return ShopGuiSupport.stackNameComponent(entry.stack());
        if (entry != null && entry.gacha() && !entry.selectable()) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_gacha_title");
        return ShopGuiSupport.stackNameComponent(fallbackStack == null || fallbackStack.isEmpty() ? entry == null ? ItemStack.EMPTY : entry.stack() : fallbackStack);
    }

    private MutableComponent entryCountText(Shop.Entry entry) {
        int count = mode == Shop.Mode.SELL ? sellTradeStack(entry).getCount() : entry.stack().getCount();
        return AdventureText.translatable(mode == Shop.Mode.BUY ? "gui.adventuresystems.curios.wallet.shop_single_count" : "gui.adventuresystems.curios.wallet.shop_sell_item_count", count);
    }

    private Component entryPriceIconText(Shop.Entry entry) {
        return AdventureText.translatable(mode == Shop.Mode.BUY
                        ? "gui.adventuresystems.curios.wallet.shop_single_price_icon"
                        : "gui.adventuresystems.curios.wallet.shop_sell_income_single_icon",
                formatExact(entry.price()));
    }

    private MutableComponent totalTradeText(long total, String currencyId) {
        return AdventureText.translatable(mode == Shop.Mode.BUY ? "gui.adventuresystems.curios.wallet.shop_price" : "gui.adventuresystems.curios.wallet.shop_sell_total_income",
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_price_value", formatExact(total)), currencyName(currencyId));
    }

    // ---- input ----------------------------------------------------------------------------------------------------------

    @Override
    protected boolean onMouseClickCapture(MouseInput input) {
        int mouseX = (int) input.x();
        int mouseY = (int) input.y();
        int button = input.rawButton();
        OverlayLayer activeLayer = overlayLayers.activeLayer();
        if (activeLayer != null) {
            // The quest button toggles its own list.
            if (activeLayer == OverlayLayer.QUEST_PICKER && questListButton != null && questListButton.contains(mouseX, mouseY)) return false;
            clearFocus();
            switch (activeLayer) {
                case CHOICE_OVERLAY -> handleChoiceOverlayClick(mouseX, mouseY, button);
                case QUEST_PICKER -> handleQuestOverlayClick(mouseX, mouseY, button);
                case REWARD_PICKER -> {
                    if (KineticMouseButtons.isPrimary(button) && !handleRewardPickerClick(mouseX, mouseY)) overlayLayers.close(OverlayLayer.REWARD_PICKER);
                }
            }
            return true;
        }
        RowClick dragRow = rowClickAt(mouseX, mouseY);
        if (KineticMouseButtons.isPrimary(button) && editorMode && KineticClientRuntime.controlModifierDown() && dragRow != null) {
            beginEntryDrag(dragRow, mouseX, mouseY);
            clearFocus();
            return true;
        }
        return false;
    }

    private void handleQuestOverlayClick(int mouseX, int mouseY, int button) {
        if (!KineticMouseButtons.isPrimary(button)) return;
        if (isInsideQuestPickerScrollbar(mouseX, mouseY)
                && questPickerScroller.beginDrag(mouseX, mouseY, MouseButton.of(button), questScrollbarX(), questListTop(),
                SCROLLBAR_W, questListHeight(selectedEntry()), 16, 2)) {
            return;
        }
        if (handleQuestPickerClick(mouseX, mouseY)) return;
        overlayLayers.close(OverlayLayer.QUEST_PICKER);
        questPickerScroller.release(MouseButton.LEFT);
    }

    @Override
    protected boolean onMouseClick(MouseInput input) {
        int mouseX = (int) input.x();
        int mouseY = (int) input.y();
        int button = input.rawButton();
        boolean primary = KineticMouseButtons.isPrimary(button);
        if (primary && isInsideRewardPreviewScrollbar(mouseX, mouseY)
                && rewardPreviewScroller.beginDrag(mouseX, mouseY, input.button(), contentRight() - 2 - SCROLLBAR_W, rewardPreviewListY(),
                SCROLLBAR_W, rewardPreviewListHeight(), 12, 2)) {
            clearFocus();
            return true;
        }
        Shop.Entry selected = selectedEntry();
        if (primary && selected != null && !selected.locked() && isSelectableRewardIcon(mouseX, mouseY)) {
            if (mode == Shop.Mode.BUY) openChoiceOverlay(selected);
            else overlayLayers.open(OverlayLayer.REWARD_PICKER);
            clearFocus();
            return true;
        }
        if (primary && pageListScrolls()
                && pageScroller.beginDrag(mouseX, mouseY, input.button(), sidebar.x() + sidebar.width() - SCROLLBAR_W, pageListTop(),
                SCROLLBAR_W, pageListBottom() - pageListTop(), 12, 2)) {
            clearFocus();
            return true;
        }
        String page = pageAt(mouseX, mouseY);
        if (primary && page != null) {
            selectPage(page);
            return true;
        }
        if (primary && productScroller.beginDrag(mouseX, mouseY, input.button(), gridX() + gridWidth() + 4, gridTop(),
                SCROLLBAR_W, gridHeight(), 20, 2)) {
            clearFocus();
            return true;
        }
        RowClick rowClick = rowClickAt(mouseX, mouseY);
        if (rowClick == null) {
            clearFocus();
            return false;
        }
        if (KineticMouseButtons.isSecondary(button)) {
            openShopContextMenu(rowClick, mouseX, mouseY);
            clearFocus();
            return true;
        }
        if (primary) {
            selectRow(rowClick.index());
            clearFocus();
            return true;
        }
        return false;
    }

    @Override
    protected boolean onMouseRelease(MouseInput input) {
        if (overlayLayers.activeLayer() != null) {
            questPickerScroller.release(input.button());
            choiceOverlayScroller.release(input.button());
            return true;
        }
        if (KineticMouseButtons.isPrimary(input.rawButton()) && dragSourceEntry != null) {
            finishEntryDrag((int) input.x(), (int) input.y());
            return true;
        }
        boolean released = productScroller.release(input.button());
        released |= pageScroller.release(input.button());
        released |= rewardPreviewScroller.release(input.button());
        return released;
    }

    @Override
    protected boolean onMouseDrag(MouseDragInput input) {
        double mouseX = input.x();
        double mouseY = input.y();
        if (!KineticMouseButtons.isPrimary(input.rawButton())) return false;
        OverlayLayer activeLayer = overlayLayers.activeLayer();
        if (activeLayer != null) {
            if (activeLayer == OverlayLayer.CHOICE_OVERLAY) choiceOverlayScroller.drag(mouseY, choiceGridY(), choiceGridHeight(), 20);
            else if (activeLayer == OverlayLayer.QUEST_PICKER) questPickerScroller.drag(mouseY, questListTop(), questListHeight(selectedEntry()), 16);
            return true;
        }
        if (dragSourceEntry != null) {
            updateEntryDrag((int) mouseX, (int) mouseY);
            return true;
        }
        if (productScroller.drag(mouseY, gridTop(), gridHeight(), 20)) return true;
        if (pageScroller.drag(mouseY, pageListTop(), pageListBottom() - pageListTop(), 12)) return true;
        return rewardPreviewShown() && rewardPreviewScroller.drag(mouseY, rewardPreviewListY(), rewardPreviewListHeight(), 12);
    }

    @Override
    protected boolean onMouseScroll(ScrollInput input) {
        int mouseX = (int) input.x();
        int mouseY = (int) input.y();
        double delta = input.deltaY();
        OverlayLayer activeLayer = overlayLayers.activeLayer();
        if (activeLayer != null) {
            switch (activeLayer) {
                case CHOICE_OVERLAY -> {
                    choiceOverlayVisualScroll(selectedEntry());
                    choiceOverlayScroller.scroll(delta);
                }
                case QUEST_PICKER -> {
                    if (isInsideQuestPicker(mouseX, mouseY)) {
                        questPickerVisualScroll(selectedEntry());
                        questPickerScroller.scroll(delta);
                    }
                }
                case REWARD_PICKER -> {
                    if (isInsideRewardPicker(mouseX, mouseY)) {
                        rewardPickerScroll = rewardPickerScrollSmoothing.wheel(rewardPickerScroll, delta, 1.0D, rewardPickerMaxScroll(selectedEntry()));
                    }
                }
            }
            return true;
        }
        if (isInsideRewardPreview(mouseX, mouseY)) {
            rewardPreviewVisualScroll(selectedEntry());
            rewardPreviewScroller.scroll(delta);
            return true;
        }
        if (isHover(mouseX, mouseY, sidebar.x(), pageListTop(), sidebar.width(), pageListBottom() - pageListTop())) {
            pageListOffset();
            pageScroller.scroll(delta);
            return true;
        }
        if (isHover(mouseX, mouseY, gridX(), gridTop(), gridWidth() + SCROLLBAR_W + 4, gridHeight())) {
            renderedScroll();
            productScroller.scroll(delta);
            return true;
        }
        return false;
    }

    private void tradeSelectedEntry() {
        Shop.Entry entry = selectedEntry();
        if (entry == null) return;
        if (entry.locked()) {
            KineticOverlays.toast("currency_wallet_shop_notice", tradeFailText(entry), KineticOverlays.Position.BOTTOM_CENTER, 2500, 0, -30);
            return;
        }
        if (mode == Shop.Mode.BUY && entry.selectable()) {
            openChoiceOverlay(entry);
            return;
        }
        if (!canTrade(entry, amount)) {
            KineticOverlays.toast("currency_wallet_shop_notice", tradeFailText(entry), KineticOverlays.Position.BOTTOM_CENTER, 2500, 0, -30);
            return;
        }
        if (mode == Shop.Mode.BUY) Network.sendShopBuy(entry.index(), packedSelectedAmount(entry));
        else Network.sendShopSell(entry.index(), packedSelectedAmount(entry));
    }

    private MutableComponent tradeFailText(Shop.Entry entry) {
        if (entry != null && entry.locked()) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_need_complete_task_first", requiredQuestTitlesText(entry));
        if (mode == Shop.Mode.SELL) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_sell_fail_preview", entry == null ? 0L : sellTotalMaterials(entry));
        if (entry != null && timedRemaining(entry) > 0L) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_timed_wait", formatDuration(timedRemaining(entry)));
        if (entry != null && totalRemaining(entry) <= 0) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_limit_reached");
        return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_buy_fail_no_money");
    }

    // ---- menus ------------------------------------------------------------------------------------------------------

    // Switches show their state as "name: on/off" in green/red text, not the orange selection frame (that means "selected").
    private void openEditorMenu() {
        if (editorModeButton == null) return;
        List<KineticOverlays.MenuItem> items = new ArrayList<>();
        items.add(KineticOverlays.MenuItem.action(editorModeButtonText(),
                AdventureText.translatable(editorMode
                        ? "gui.adventuresystems.curios.wallet.shop_editor_mode_on_tip"
                        : "gui.adventuresystems.curios.wallet.shop_editor_mode_off_tip"),
                Network::sendToggleShopEditorMode));
        Component newEntry = AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_new_entry");
        items.add(editorMode
                ? KineticOverlays.MenuItem.action(newEntry, AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_new_entry"), this::openNewEditor)
                : KineticOverlays.MenuItem.disabled(newEntry, AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_menu_editor_need_mode")));
        openContextMenu(editorModeButton.controlX(), editorModeButton.controlY() + CONTROL_H + 2, items);
    }

    private void openSourceMenu() {
        if (backpackSourceButton == null) return;
        List<KineticOverlays.MenuItem> items = new ArrayList<>();
        items.add(KineticOverlays.MenuItem.action(sourceButtonText(true),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_source_backpack_tip"), Network::sendToggleShopBackpack));
        items.add(KineticOverlays.MenuItem.action(sourceButtonText(false),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_source_rs_tip"), Network::sendToggleShopRs));
        openContextMenu(backpackSourceButton.controlX(), backpackSourceButton.controlY() + CONTROL_H + 2, items);
    }

    private Component sourceButtonText(boolean backpack) {
        boolean enabled = backpack ? useBackpackSource : useRsSource;
        String key = backpack
                ? enabled ? "gui.adventuresystems.curios.wallet.shop_source_backpack_on" : "gui.adventuresystems.curios.wallet.shop_source_backpack_off"
                : enabled ? "gui.adventuresystems.curios.wallet.shop_source_rs_on" : "gui.adventuresystems.curios.wallet.shop_source_rs_off";
        return AdventureText.translatable(key);
    }

    private Component editorModeButtonText() {
        if (!canEdit) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_mode_locked");
        return AdventureText.translatable(editorMode
                ? "gui.adventuresystems.curios.wallet.shop_editor_mode_on"
                : "gui.adventuresystems.curios.wallet.shop_editor_mode_off");
    }

    private void openNewEditor() {
        ShopGuiSupport.EditorDraft draft = new ShopGuiSupport.EditorDraft(mode);
        draft.pageName = activeConcretePage();
        openChild(new ShopEntryEditorScreen(this, draft));
    }

    private void openEditEditor(Shop.Entry entry) {
        openChild(new ShopEntryEditorScreen(this, new ShopGuiSupport.EditorDraft(entry)));
    }

    private void openCopyEditor(Shop.Entry entry) {
        if (entry == null) return;
        ShopGuiSupport.EditorDraft draft = new ShopGuiSupport.EditorDraft(entry);
        draft.index = -1;
        openChild(new ShopEntryEditorScreen(this, draft));
    }

    private void openShopContextMenu(RowClick click, int mouseX, int mouseY) {
        if (click == null || click.row().entry() == null) return;
        contextEntry = click.row().entry();
        selectRow(click.index());
        questPickerScroller.release(MouseButton.LEFT);
        choiceOverlayScroller.release(MouseButton.LEFT);
        // Sorting stays inside the current page: front / back and up / down are relative to the page's neighbours.
        List<Shop.Entry> siblings = pageSiblings(contextEntry);
        int position = indexOfEntry(siblings, contextEntry);
        Shop.Entry first = siblings.isEmpty() ? null : siblings.get(0);
        Shop.Entry previous = position > 0 ? siblings.get(position - 1) : null;
        Shop.Entry next = position >= 0 && position < siblings.size() - 1 ? siblings.get(position + 1) : null;
        Shop.Entry last = siblings.isEmpty() ? null : siblings.get(siblings.size() - 1);
        List<KineticOverlays.MenuItem> items = new ArrayList<>();
        items.add(KineticOverlays.MenuItem.action(AdventureText.translatable(isFavorite(contextEntry)
                ? "gui.adventuresystems.curios.wallet.shop_context_unfavorite"
                : "gui.adventuresystems.curios.wallet.shop_context_favorite"), this::toggleContextFavorite));
        if (editorMode) {
            items.add(KineticOverlays.MenuItem.action(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_context_edit"), this::editContextEntry));
            items.add(KineticOverlays.MenuItem.action(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_context_copy"), this::copyContextEntry));
            items.add(contextMenuMoveAction("gui.adventuresystems.curios.wallet.shop_context_move_front", previous != null && first != null, first));
            items.add(contextMenuMoveAction("gui.adventuresystems.curios.wallet.shop_context_move_up", previous != null, previous));
            items.add(contextMenuMoveAction("gui.adventuresystems.curios.wallet.shop_context_move_down", next != null, next));
            items.add(contextMenuMoveAction("gui.adventuresystems.curios.wallet.shop_context_move_end", next != null && last != null, last));
            items.add(KineticOverlays.MenuItem.danger(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_context_delete"), this::deleteContextEntry));
        }
        openContextMenu(mouseX, mouseY, items);
    }

    private KineticOverlays.MenuItem contextMenuMoveAction(String translationKey, boolean enabled, Shop.Entry target) {
        Component label = AdventureText.translatable(translationKey);
        return enabled && target != null
                ? KineticOverlays.MenuItem.action(label, () -> moveContextEntry(target.index()))
                : KineticOverlays.MenuItem.disabled(label);
    }

    private void closeShopContextMenu() {
        super.closeContextMenu();
        contextEntry = null;
    }

    /**
     * The entries of {@code entry}'s mode on the current page (all / favourites / a page), in global order; search does
     * not apply. The server move takes an entry out and inserts it at the target index, so moving to a neighbour's
     * index places it before (up) or after (down) that neighbour.
     */
    private List<Shop.Entry> pageSiblings(Shop.Entry entry) {
        List<Shop.Entry> result = new ArrayList<>();
        if (entry == null) return result;
        for (Shop.Entry candidate : Shop.entriesFromTag(shopTag, entry.mode())) {
            if (!entryExcludedFromActivePage(candidate)) result.add(candidate);
        }
        result.sort(Comparator.comparingInt(Shop.Entry::index));
        return result;
    }

    private static int indexOfEntry(List<Shop.Entry> entries, Shop.Entry entry) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).index() == entry.index()) return i;
        }
        return -1;
    }

    int entryCount(Shop.Mode entryMode) {
        String key = entryMode == Shop.Mode.BUY ? "Buy" : "Sell";
        return shopTag == null ? 0 : shopTag.getList(key, Tag.TAG_COMPOUND).size();
    }

    private void toggleContextFavorite() {
        Shop.Entry entry = contextEntry;
        if (entry == null) {
            closeShopContextMenu();
            return;
        }
        boolean favorite = ShopClientPreferences.toggleFavorite(entry.mode(), entry.key());
        closeShopContextMenu();
        if (!favorite && Objects.equals(activePageName, FAVORITES_PAGE)) selectedIndex = -1;
        rebuildRows();
        if (favorite || !Objects.equals(activePageName, FAVORITES_PAGE)) selectRowByKey(entry.key());
        updateDetailWidgetState();
    }

    private boolean isFavorite(Shop.Entry entry) {
        return entry != null && ShopClientPreferences.isFavorite(entry.mode(), entry.key());
    }

    private void editContextEntry() {
        Shop.Entry entry = contextEntry;
        closeShopContextMenu();
        if (entry != null) openEditEditor(entry);
    }

    private void copyContextEntry() {
        Shop.Entry entry = contextEntry;
        closeShopContextMenu();
        if (entry != null) openCopyEditor(entry);
    }

    private void moveContextEntry(int targetIndex) {
        Shop.Entry entry = contextEntry;
        closeShopContextMenu();
        if (entry == null) return;
        Network.sendMoveShopEntry(entry.mode(), entry.index(), targetIndex);
        applyLocalMove(entry.mode(), entry.index(), targetIndex, entry.key());
    }

    private void deleteContextEntry() {
        Shop.Entry entry = contextEntry;
        closeShopContextMenu();
        if (entry != null) {
            Network.sendRemoveShopEntry(entry.mode(), entry.index());
            applyLocalRemove(entry.mode(), entry.index());
            selectedIndex = -1;
            updateDetailWidgetState();
        }
    }

    // ---- local edits: shown at once, replaced by the server's data when it broadcasts the saved shop ------------

    private ListTag localEntryList(Shop.Mode entryMode, boolean create) {
        if (shopTag == null || entryMode == null) return null;
        String key = entryMode == Shop.Mode.BUY ? "Buy" : "Sell";
        if (!shopTag.contains(key, Tag.TAG_LIST)) {
            if (!create) return null;
            shopTag.put(key, new ListTag());
        }
        return shopTag.getList(key, Tag.TAG_COMPOUND);
    }

    /** Same as the server's {@code Shop.move}: take {@code from} out and insert it at {@code to}. */
    private void applyLocalMove(Shop.Mode entryMode, int from, int to, String selectKey) {
        ListTag list = localEntryList(entryMode, false);
        if (list == null || from == to || from < 0 || to < 0) return;
        List<CompoundTag> tags = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            int index = tag.getInt("Index");
            int moved;
            if (index == from) moved = to;
            else if (index > from && index <= to) moved = index - 1;
            else if (index >= to && index < from) moved = index + 1;
            else moved = index;
            tag.putInt("Index", moved);
            tags.add(tag);
        }
        tags.sort(Comparator.comparingInt(tag -> tag.getInt("Index")));
        list.clear();
        list.addAll(tags);
        refreshAfterLocalEdit(selectKey);
    }

    void applyLocalRemove(Shop.Mode entryMode, int index) {
        ListTag list = localEntryList(entryMode, false);
        if (list == null) return;
        for (int i = list.size() - 1; i >= 0; i--) {
            CompoundTag tag = list.getCompound(i);
            int current = tag.getInt("Index");
            if (current == index) list.remove(i);
            else if (current > index) tag.putInt("Index", current - 1);
        }
        refreshAfterLocalEdit(null);
    }

    /** Called when the editor saves: changes an entry or appends a new one, then shows its page and selects it. */
    void applyLocalSave(ShopGuiSupport.EditorDraft draft) {
        if (draft == null || draft.mode == null) return;
        ListTag list = localEntryList(draft.mode, true);
        if (list == null) return;
        CompoundTag tag = null;
        int maxIndex = -1;
        for (int i = 0; i < list.size(); i++) {
            CompoundTag candidate = list.getCompound(i);
            int index = dev.xyat.adventuresystems.data.Nbt.intValue(candidate, "Index");
            maxIndex = Math.max(maxIndex, index);
            if (draft.index >= 0 && index == draft.index) tag = candidate;
        }
        if (tag == null) {
            tag = new CompoundTag();
            tag.putInt("Index", maxIndex + 1);
            tag.putString("Key", "local:" + UUID.randomUUID());
            tag.putBoolean("Unlocked", true);
            list.add(tag);
        }
        ItemStack stack = ShopGuiSupport.stack(draft.itemId);
        if (!stack.isEmpty()) {
            stack.setCount(Math.max(1, Math.min(64, draft.count)));
//? if >=1.21 {
        /*tag.put("Item", dev.xyat.adventuresystems.data.ItemStacks.save(stack));
        *///?} else {
            tag.put("Item", stack.save(new CompoundTag()));
        //?}
        }
        tag.putString("Currency", draft.currencyId == null ? "" : draft.currencyId);
        tag.putLong("Price", draft.price);
        tag.putInt("TimedLimitSeconds", Math.max(0, draft.dailyLimit));
        tag.putInt("TotalLimit", Math.max(0, draft.totalLimit));
        tag.putString("PageName", draft.pageName == null ? "" : draft.pageName.trim());
        tag.putString("DisplayName", draft.displayName == null ? "" : draft.displayName);
        tag.putString("Description", draft.description == null ? "" : draft.description);
        tag.putString("Command", draft.commandText == null ? "" : draft.commandText);
        tag.putInt("RequiredQuestCount", Math.max(0, draft.requiredQuestCount));
        ListTag questIds = new ListTag();
        for (Long id : draft.questIds) {
            if (id != null) questIds.add(LongTag.valueOf(id));
        }
        tag.put("RequiredQuestIds", questIds);
        tag.putBoolean("Gacha", draft.gacha);
        tag.putBoolean("Selectable", draft.selectable);
        ItemStack icon = draft.iconId == null || draft.iconId.isBlank() ? ItemStack.EMPTY : ShopGuiSupport.stack(draft.iconId);
        if (icon.isEmpty()) tag.remove("Icon");
//? if >=1.21 {
        /*else tag.put("Icon", dev.xyat.adventuresystems.data.ItemStacks.save(icon));
        *///?} else {
        else tag.put("Icon", icon.save(new CompoundTag()));
        //?}

        if (mode != draft.mode) {
            mode = draft.mode;
            productScroller.reset();
        }
        String page = draft.pageName == null ? "" : draft.pageName.trim();
        if (!Objects.equals(activePageName, ALL_PAGE) && !Objects.equals(activePageName, page)) activePageName = page;
        refreshAfterLocalEdit(tag.getString("Key"));
    }

    /** After the server replaced a locally created entry, select the newest entry of the current page again. */
    private void selectNewestRow() {
        int best = -1;
        int bestIndex = -1;
        for (int i = 0; i < rows.size(); i++) {
            Shop.Entry entry = rows.get(i).entry();
            if (entry != null && entry.index() > bestIndex) {
                bestIndex = entry.index();
                best = i;
            }
        }
        if (best >= 0) selectRowByKey(rows.get(best).entry().key());
    }

    private void refreshAfterLocalEdit(String selectKey) {
        searchTextCache.clear();
        if (!isAttached()) {
            pendingSelectKey = selectKey;
            return;
        }
        rebuildRows();
        if (selectKey != null) selectRowByKey(selectKey);
        updateDetailWidgetState();
    }

    /** Page names for the editor's page picker: this mode's pages first, then the other mode's; no default page. */
    List<String> knownPageNames(Shop.Mode preferred) {
        List<String> result = new ArrayList<>();
        Shop.Mode first = preferred == null ? mode : preferred;
        Shop.Mode second = first == Shop.Mode.BUY ? Shop.Mode.SELL : Shop.Mode.BUY;
        for (Shop.Mode entryMode : new Shop.Mode[]{first, second}) {
            for (Shop.Entry entry : Shop.entriesFromTag(shopTag, entryMode)) {
                String page = pageName(entry);
                if (!page.isBlank() && !result.contains(page)) result.add(page);
            }
        }
        return result;
    }

    /** The concrete page being browsed (empty for all / favourites); new entries go there. */
    String activeConcretePage() {
        if (Objects.equals(activePageName, ALL_PAGE) || Objects.equals(activePageName, FAVORITES_PAGE)) return "";
        return activePageName == null ? "" : activePageName;
    }

    // ---- amount -------------------------------------------------------------------------------------------------------

    private void adjustAmount(int delta) {
        setAmount(Math.max(1, Math.min(64, amount + delta)));
    }

    private void setAmount(int value) {
        amount = Math.max(1, Math.min(64, value));
        if (amountBox != null) amountBox.setIntValue(amount);
        updateDetailWidgetState();
    }

    private int maxUsefulTradeAmount() {
        Shop.Entry entry = selectedEntry();
        if (entry == null) return 1;
        return Math.max(1, Math.min(64, previewSuccessTrades(entry, 64)));
    }

    private void onAmountInput(String text) {
        int parsed = parseCount(text);
        if (parsed > 0) {
            amount = parsed;
        } else if (!text.isBlank()) {
            KineticOverlays.toast("currency_wallet_shop_notice", AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_invalid_trade_amount"), KineticOverlays.Position.BOTTOM_CENTER, 2500, 0, -30);
        }
    }

    private int parseCount(String text) {
        try {
            int value = Integer.parseInt(text.trim());
            return value >= 1 && value <= 64 ? value : -1;
        } catch (Exception ignored) {
            return -1;
        }
    }

    // ---- rows, pages and search -----------------------------------------------------------------------------------

    private void rebuildRows() {
        rows.clear();
        List<Shop.Entry> entries = Shop.entriesFromTag(shopTag, mode);
        rebuildPages(entries);
        String query = normalizedSearchQuery();
        for (Shop.Entry entry : entries) {
            if (entryExcludedFromActivePage(entry)) continue;
            if (!query.isEmpty() && failsSearch(entry, query)) continue;
            rows.add(new Row(entry));
        }
        if (selectedIndex >= rows.size()) selectedIndex = -1;
    }

    private boolean entryExcludedFromActivePage(Shop.Entry entry) {
        if (entry == null) return true;
        if (Objects.equals(activePageName, ALL_PAGE)) return false;
        if (Objects.equals(activePageName, FAVORITES_PAGE)) return !isFavorite(entry);
        return !Objects.equals(pageName(entry), activePageName);
    }

    private void rebuildPages(List<Shop.Entry> entries) {
        pages.clear();
        pageCounts.clear();
        favoriteCount = 0;
        allCount = entries.size();
        for (Shop.Entry entry : entries) {
            String page = pageName(entry);
            if (!pages.contains(page)) pages.add(page);
            pageCounts.merge(page, 1, Integer::sum);
            if (isFavorite(entry)) favoriteCount++;
        }
        if (!Objects.equals(activePageName, FAVORITES_PAGE)
                && !Objects.equals(activePageName, ALL_PAGE)
                && !pages.contains(activePageName)) {
            activePageName = ALL_PAGE;
        }
    }

    private void selectPage(String page) {
        if (page == null || page.isBlank()) return;
        activePageName = page;
        searchQuery = "";
        if (searchBox != null) searchBox.setTextValue("");
        selectedIndex = -1;
        overlayLayers.closeAll();
        rewardPickerScroll = 0;
        questPickerScroller.reset();
        choiceOverlayScroller.reset();
        productScroller.reset();
        closeShopContextMenu();
        clearDragState();
        rebuildRows();
        updateDetailWidgetState();
        clearFocus();
    }

    private String normalizedSearchQuery() {
        return searchQuery == null ? "" : searchQuery.trim().toLowerCase(Locale.ROOT);
    }

    private boolean failsSearch(Shop.Entry entry, String query) {
        if (query == null || query.isBlank()) return false;
        String text = searchText(entry);
        for (String token : query.trim().toLowerCase(Locale.ROOT).split("\\s+")) {
            if (token.isBlank()) continue;
            if (token.startsWith("@")) {
                if (failsModToken(entry, token.substring(1))) return true;
                continue;
            }
            if (token.startsWith("#")) {
                if (failsPageToken(entry, token.substring(1))) return true;
                continue;
            }
            if (!KineticSearch.match(text, token)) return true;
        }
        return false;
    }

    private boolean failsModToken(Shop.Entry entry, String token) {
        if (entry == null || token == null || token.isBlank()) return false;
        ResourceLocation itemId = KineticRegistries.items().id(entry.stack().getItem());
        String itemNamespace = itemId == null ? "" : itemId.getNamespace().toLowerCase(Locale.ROOT);
        if (itemNamespace.contains(token) || namespaceOf(entry.currencyId()).contains(token)) return false;
        if (entry.rewards() != null) {
            for (Shop.Reward reward : entry.rewards()) {
                if (reward == null || reward.empty()) continue;
                ResourceLocation rewardId = KineticRegistries.items().id(reward.stack().getItem());
                if (rewardId != null && rewardId.getNamespace().toLowerCase(Locale.ROOT).contains(token)) return false;
            }
        }
        return true;
    }

    private boolean failsPageToken(Shop.Entry entry, String token) {
        if (entry == null || token == null || token.isBlank()) return false;
        String page = pageLabel(entry).getString().toLowerCase(Locale.ROOT);
        return !KineticSearch.match(page + " " + KineticSearch.pinyin(page), token);
    }

    private static String namespaceOf(String id) {
        if (id == null) return "";
        int split = id.indexOf(':');
        return split <= 0 ? "" : id.substring(0, split).toLowerCase(Locale.ROOT);
    }

    private String searchText(Shop.Entry entry) {
        if (entry == null) return "";
        String cacheKey = entry.key();
        if (cacheKey != null && !cacheKey.isBlank()) {
            String cached = searchTextCache.get(cacheKey);
            if (cached != null) return cached;
        }
        ItemStack item = entry.stack();
        String itemName = ShopGuiSupport.stackName(item);
        String currencyName = ShopGuiSupport.stackName(stack(entry.currencyId()));
        StringBuilder builder = new StringBuilder();
        if (entry.displayName() != null && !entry.displayName().isBlank()) {
            builder.append(entry.displayName()).append(' ').append(KineticSearch.pinyin(entry.displayName())).append(' ');
        }
        if (entry.description() != null && !entry.description().isBlank()) {
            builder.append(entry.description()).append(' ').append(KineticSearch.pinyin(entry.description())).append(' ');
        }
        ResourceLocation itemId = KineticRegistries.items().id(item.getItem());
        if (itemId != null) builder.append(itemId).append(' ').append('@').append(itemId.getNamespace()).append(' ');
        if (entry.currencyId() != null && entry.currencyId().contains(":")) builder.append('@').append(namespaceOf(entry.currencyId())).append(' ');
        builder.append('#').append(pageLabel(entry).getString()).append(' ');
        builder.append(itemName).append(' ').append(KineticSearch.pinyin(itemName)).append(' ');
        if (entry.rewards() != null) {
            for (Shop.Reward reward : entry.rewards()) {
                if (reward == null || reward.empty()) continue;
                ResourceLocation rewardId = KineticRegistries.items().id(reward.stack().getItem());
                String rewardName = ShopGuiSupport.stackName(reward.stack());
                if (rewardId != null) builder.append(rewardId).append(' ').append('@').append(rewardId.getNamespace()).append(' ');
                builder.append(rewardName).append(' ').append(KineticSearch.pinyin(rewardName)).append(' ');
            }
        }
        builder.append(entry.currencyId()).append(' ').append(currencyName).append(' ').append(KineticSearch.pinyin(currencyName)).append(' ');
        builder.append(pageLabel(entry).getString()).append(' ');
        if (entry.requiredQuestTitles() != null) {
            for (String title : entry.requiredQuestTitles()) {
                builder.append(title == null ? "" : title).append(' ').append(KineticSearch.pinyin(title)).append(' ');
            }
        }
        String text = builder.toString().toLowerCase(Locale.ROOT);
        if (cacheKey != null && !cacheKey.isBlank()) searchTextCache.put(cacheKey, text);
        return text;
    }

    private String pageName(Shop.Entry entry) {
        if (entry == null) return "";
        String page = entry.safePageName();
        return page == null ? "" : page.trim();
    }

    private Component pageLabel(Shop.Entry entry) {
        return pageLabel(pageName(entry));
    }

    private Component pageLabel(String page) {
        if (Objects.equals(page, FAVORITES_PAGE)) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_page_favorites");
        if (Objects.equals(page, ALL_PAGE)) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_page_all");
        if (page == null || page.isBlank()) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_page_default");
        return AdventureText.literal(page);
    }

    // ---- selection --------------------------------------------------------------------------------------------------

    private Shop.Entry selectedEntry() {
        if (selectedIndex < 0 || selectedIndex >= rows.size()) return null;
        return rows.get(selectedIndex).entry();
    }

    private void selectRow(int index) {
        if (index < 0 || index >= rows.size()) return;
        selectedIndex = index;
        rewardPreviewExpanded = false;
        overlayLayers.closeAll();
        rewardPickerScroll = 0;
        rewardPreviewScroller.reset();
        questPickerScroller.reset();
        choiceOverlayScroller.reset();
        choiceOverlaySelectedIndex = -1;
        amount = 1;
        if (amountBox != null) amountBox.setTextValue("1");
        updateDetailWidgetState();
    }

    private void selectRowByKey(String key) {
        if (key == null || key.isBlank()) return;
        for (int i = 0; i < rows.size(); i++) {
            Shop.Entry entry = rows.get(i).entry();
            if (entry != null && Objects.equals(key, entry.key())) {
                selectedIndex = i;
                rewardPreviewExpanded = false;
                rewardPreviewScroller.reset();
                updateDetailWidgetState();
                return;
            }
        }
    }

    // ---- reordering by Ctrl+drag (editor mode) ----------------------------------------------------------------------

    private void beginEntryDrag(RowClick click, int mouseX, int mouseY) {
        if (click == null || click.row().entry() == null) return;
        closeShopContextMenu();
        selectRow(click.index());
        dragSourceEntry = click.row().entry();
        dragTargetEntry = dragSourceEntry;
        dragMouseX = mouseX;
        dragMouseY = mouseY;
    }

    private void updateEntryDrag(int mouseX, int mouseY) {
        if (dragSourceEntry == null) return;
        dragMouseX = mouseX;
        dragMouseY = mouseY;
        // Near the top or bottom edge the grid scrolls by itself.
        if (mouseY < gridTop() + 18) productScroller.scroll(0.25D);
        else if (mouseY > gridTop() + gridHeight() - 18) productScroller.scroll(-0.25D);
        RowClick target = rowClickAt(mouseX, mouseY);
        if (target != null && target.row().entry() != null) dragTargetEntry = target.row().entry();
    }

    private void finishEntryDrag(int mouseX, int mouseY) {
        updateEntryDrag(mouseX, mouseY);
        Shop.Entry source = dragSourceEntry;
        Shop.Entry target = dragTargetEntry;
        clearDragState();
        if (source == null || target == null || source.index() == target.index()) return;
        Network.sendMoveShopEntry(source.mode(), source.index(), target.index());
        applyLocalMove(source.mode(), source.index(), target.index(), source.key());
    }

    private void clearDragState() {
        dragSourceEntry = null;
        dragTargetEntry = null;
        dragMouseX = 0;
        dragMouseY = 0;
    }

    private void renderDragPreview(KineticGraphics graphics) {
        if (dragSourceEntry == null) return;
        int width = cellWidth();
        if (dragTargetEntry != null) {
            for (int i = visibleIndexStart(); i < visibleIndexEnd(); i++) {
                Shop.Entry entry = rows.get(i).entry();
                if (entry == null || entry.index() != dragTargetEntry.index()) continue;
                Cell target = cellForIndex(i);
                KineticTheme.indicatorOutline(graphics, target.x() - 1, target.y() - 1, width + 2, CELL_H + 2, KineticTheme.Indicator.INFO);
                break;
            }
        }
        int x = Math.max(gridX(), Math.min(dragMouseX - width / 2, gridX() + gridWidth() - width));
        int y = Math.max(gridTop(), Math.min(dragMouseY - CELL_H / 2, gridTop() + gridHeight() - CELL_H));
        graphics.push();
        graphics.raise(6);
        renderCard(graphics, dragSourceEntry, new Cell(x, y), width, true, false);
        graphics.pop();
    }

    // ---- entry contents -------------------------------------------------------------------------------------------

    /** The card icon: the custom icon when set, otherwise the product (rotating through several rewards). */
    private ItemStack cellDisplayStack(Shop.Entry entry) {
        if (entry != null && entry.hasCustomIcon()) return entry.icon();
        return cellContentStack(entry);
    }

    private ItemStack cellContentStack(Shop.Entry entry) {
        if (entry == null) return ItemStack.EMPTY;
        if (hasMultipleRewards(entry)) {
            Shop.Reward reward = cycleReward(entry);
            if (reward != null && !reward.empty()) return reward.stack();
        }
        if (entry.selectable()) {
            Shop.Reward reward = selectedReward(entry);
            if (reward != null && !reward.empty()) return reward.stack();
        }
        return entry.stack();
    }

    private ItemStack detailDisplayStack(Shop.Entry entry) {
        if (entry == null) return ItemStack.EMPTY;
        if (entry.selectable()) {
            Shop.Reward reward = selectedReward(entry);
            if (reward != null && !reward.empty()) return reward.stack();
        }
        return cellContentStack(entry);
    }

    private boolean hasMultipleRewards(Shop.Entry entry) {
        return entry != null && (entry.gacha() || entry.selectable()) && entry.rewards() != null && entry.rewards().size() > 1;
    }

    private boolean isSelectableRewardEntry(Shop.Entry entry) {
        return entry != null && entry.selectable() && entry.rewards() != null && !entry.rewards().isEmpty();
    }

    private Shop.Reward selectedReward(Shop.Entry entry) {
        if (!isSelectableRewardEntry(entry)) return null;
        return entry.rewards().get(selectedRewardIndex(entry));
    }

    private Shop.Reward cycleReward(Shop.Entry entry) {
        if (entry == null || entry.rewards() == null || entry.rewards().isEmpty()) return null;
        int count = entry.rewards().size();
        return entry.rewards().get((int) ((System.currentTimeMillis() / 1000L) % count));
    }

    private int selectedRewardIndex(Shop.Entry entry) {
        if (!isSelectableRewardEntry(entry)) return 0;
        int value = selectedRewardIndices.getOrDefault(entry.key(), 0);
        return Math.max(0, Math.min(value, entry.rewards().size() - 1));
    }

    private int packedSelectedAmount(Shop.Entry entry) {
        if (!isSelectableRewardEntry(entry)) return amount;
        return Math.max(1, Math.min(64, amount)) + (selectedRewardIndex(entry) + 1) * 1000;
    }

    private static Component rewardName(Shop.Reward reward) {
        if (reward == null) return Component.empty();
        if (reward.displayName() != null && !reward.displayName().isBlank()) return AdventureText.literal(reward.displayName());
        if (reward.empty()) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_gacha_empty");
        ItemStack stack = reward.stack();
        return ShopGuiSupport.stackNameComponent(stack).copy().append(" x" + Math.max(1, stack.getCount()));
    }

    private static String percent(double value) {
        return ShopGuiSupport.percent(value);
    }

    // ---- trading rules ------------------------------------------------------------------------------------------------

    private int totalRemaining(Shop.Entry entry) {
        if (entry == null) return 0;
        if (entry.totalLimit() <= 0) return 64;
        return Math.max(0, entry.totalLimit() - entry.totalBought());
    }

    private long timedRemaining(Shop.Entry entry) {
        if (entry == null || entry.timedLimitSeconds() <= 0) return 0L;
        long elapsed = Math.max(0L, (System.currentTimeMillis() - shopReceivedAt) / 1000L);
        return Math.max(0L, entry.timedRemainingSeconds() - elapsed);
    }

    private MutableComponent timedLimitText(Shop.Entry entry, long remaining) {
        if (remaining > 0L) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_timed_limit_wait", formatDuration(remaining), formatDuration(entry.timedLimitSeconds()));
        return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_timed_limit_ready", formatDuration(entry.timedLimitSeconds()));
    }

    private boolean canTrade(Shop.Entry entry, int times) {
        if (entry == null) return false;
        if (mode == Shop.Mode.BUY) return previewSuccessTrades(entry, times) > 0;
        return sellTotalMaterials(entry) > 0L || sellProgress(entry) > 0L;
    }

    private int previewSuccessTrades(Shop.Entry entry, int times) {
        if (entry == null || entry.locked()) return 0;
        int requested = Math.max(1, Math.min(64, times));
        if (mode == Shop.Mode.BUY) {
            if (timedRemaining(entry) > 0L) return 0;
            int limit = Math.min(requested, totalRemaining(entry));
            if (limit <= 0 || entry.price() <= 0L) return 0;
            return (int) Math.max(0, Math.min(limit, buyPaymentTotal(entry) / entry.price()));
        }
        ItemStack sellStack = sellTradeStack(entry);
        long possibleMaterials = safeAdd(sellProgress(entry), sellTotalMaterials(entry));
        int byMaterials = sellStack.getCount() <= 0 ? 0 : (int) Math.min(requested, possibleMaterials / sellStack.getCount());
        long current = amountOf(entry.currencyId());
        long byWallet = entry.price() <= 0L ? 0L : (Long.MAX_VALUE - current) / entry.price();
        return (int) Math.max(0, Math.min(byMaterials, byWallet));
    }

    private ItemStack sellTradeStack(Shop.Entry entry) {
        if (entry == null) return ItemStack.EMPTY;
        if (mode == Shop.Mode.SELL && isSelectableRewardEntry(entry)) {
            Shop.Reward reward = selectedReward(entry);
            if (reward != null && !reward.empty()) return reward.stack();
        }
        return entry.stack();
    }

    private long buyPaymentTotal(Shop.Entry entry) {
        if (entry == null) return 0L;
        return safeAdd(safeAdd(entry.inventoryCount(), entry.walletCount()), safeAdd(entry.backpackCount(), entry.rsCount()));
    }

    private long buyInventoryCount(Shop.Entry entry) {
        return entry == null ? 0L : entry.inventoryCount();
    }

    private long buyWalletCount(Shop.Entry entry) {
        return entry == null ? 0L : entry.walletCount();
    }

    private long sellInventoryCount(Shop.Entry entry) {
        Shop.Reward reward = selectedSellReward(entry);
        return reward == null ? (entry == null ? 0L : entry.inventoryCount()) : reward.inventoryCount();
    }

    private long sellBackpackCount(Shop.Entry entry) {
        Shop.Reward reward = selectedSellReward(entry);
        return reward == null ? (entry == null ? 0L : entry.backpackCount()) : reward.backpackCount();
    }

    private long sellRsCount(Shop.Entry entry) {
        Shop.Reward reward = selectedSellReward(entry);
        return reward == null ? (entry == null ? 0L : entry.rsCount()) : reward.rsCount();
    }

    private boolean sellBackpackNotLoaded(Shop.Entry entry) {
        Shop.Reward reward = selectedSellReward(entry);
        return !(reward == null ? entry != null && entry.backpackLoaded() : reward.backpackLoaded());
    }

    private boolean sellHasNoBackpack(Shop.Entry entry) {
        Shop.Reward reward = selectedSellReward(entry);
        return !(reward == null ? entry != null && entry.hasBackpack() : reward.hasBackpack());
    }

    private boolean sellRsNotLoaded(Shop.Entry entry) {
        Shop.Reward reward = selectedSellReward(entry);
        return !(reward == null ? entry != null && entry.rsLoaded() : reward.rsLoaded());
    }

    private String sellRsState(Shop.Entry entry) {
        Shop.Reward reward = selectedSellReward(entry);
        return reward == null ? (entry == null ? "" : entry.rsState()) : reward.rsState();
    }

    private long sellProgress(Shop.Entry entry) {
        Shop.Reward reward = selectedSellReward(entry);
        return reward == null ? (entry == null ? 0L : entry.sellProgress()) : reward.sellProgress();
    }

    private long sellTotalMaterials(Shop.Entry entry) {
        Shop.Reward reward = selectedSellReward(entry);
        return reward == null ? (entry == null ? 0L : entry.totalMaterials()) : reward.totalMaterials();
    }

    private Shop.Reward selectedSellReward(Shop.Entry entry) {
        if (mode != Shop.Mode.SELL || !isSelectableRewardEntry(entry)) return null;
        return selectedReward(entry);
    }

    private MutableComponent backpackMaterialText(Shop.Entry entry) {
        if (!useBackpackSource) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_sell_backpack_disabled");
        if (entry == null || sellBackpackNotLoaded(entry) || sellHasNoBackpack(entry)) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_sell_backpack_missing");
        long count = sellBackpackCount(entry);
        return AdventureText.translatable(count > 0L
                ? "gui.adventuresystems.curios.wallet.shop_sell_backpack_count_ready"
                : "gui.adventuresystems.curios.wallet.shop_sell_backpack_count_missing", materialCompact(count));
    }

    private MutableComponent rsMaterialText(Shop.Entry entry) {
        if (!useRsSource) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_sell_rs_disabled");
        if (entry == null || sellRsNotLoaded(entry)) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_sell_rs_unbound");
        if ("BOUND".equals(sellRsState(entry))) {
            long count = sellRsCount(entry);
            return AdventureText.translatable(count > 0L
                    ? "gui.adventuresystems.curios.wallet.shop_sell_rs_count_ready"
                    : "gui.adventuresystems.curios.wallet.shop_sell_rs_count_missing", materialCompact(count));
        }
        return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_sell_rs_unbound");
    }

    // ---- balances and formatting ------------------------------------------------------------------------------------

    private List<CurrencyType> sortedCurrenciesByValueDesc() {
        List<CurrencyType> currencies = new ArrayList<>(Data.currencies());
        currencies.sort(Comparator.comparingLong(CurrencyType::value));
        return currencies;
    }

    private long amountOf(String id) {
        return Data.readAmount(balances, id);
    }

    private long displayAmount(String id) {
        BalanceAnimation animation = animations.get(id);
        return animation == null ? amountOf(id) : animation.display();
    }

    private static Component currencyName(String id) {
        return ShopGuiSupport.stackNameComponent(stack(id));
    }

    private static ItemStack stack(String id) {
        ItemStack stack = StackCodec.fromConfigString(id);
        return stack.isEmpty() ? new ItemStack(net.minecraft.world.item.Items.BARRIER) : stack;
    }

    private static long safeAdd(long a, long b) {
        if (a < 0L || b < 0L) return Long.MAX_VALUE;
        if (Long.MAX_VALUE - a < b) return Long.MAX_VALUE;
        return a + b;
    }

    private static long safeMultiply(long a, long b) {
        if (a <= 0L || b <= 0L) return 0L;
        if (a > Long.MAX_VALUE / b) return Long.MAX_VALUE;
        return a * b;
    }

    private static boolean isHover(int mouseX, int mouseY, int x, int y, int width, int height) {
        return KineticTheme.hovering(mouseX, mouseY, x, y, width, height);
    }

    private static String formatCompact(long value) {
        long safeValue = Math.max(0L, value);
        if (safeValue >= 1_000_000_000_000_000_000L) return String.format(Locale.ROOT, "%dQi+", safeValue / 1_000_000_000_000_000_000L);
        if (safeValue >= 1_000_000_000_000_000L) return String.format(Locale.ROOT, "%dQa+", safeValue / 1_000_000_000_000_000L);
        if (safeValue >= 1_000_000_000_000L) return String.format(Locale.ROOT, "%dT+", safeValue / 1_000_000_000_000L);
        if (safeValue >= 1_000_000_000L) return String.format(Locale.ROOT, "%dB+", safeValue / 1_000_000_000L);
        if (safeValue >= 1_000_000L) return String.format(Locale.ROOT, "%dM+", safeValue / 1_000_000L);
        if (safeValue >= 1_000L) return String.format(Locale.ROOT, "%dK+", safeValue / 1_000L);
        return formatExact(safeValue);
    }

    private static String materialCompact(long value) {
        return formatCompact(value);
    }

    private static String formatDelta(long value) {
        long abs = value == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(value);
        return (value >= 0L ? "+" : "-") + formatCompact(abs);
    }

    private static String formatDuration(long seconds) {
        long value = Math.max(0L, seconds);
        if (value < 60L) return value + "s";
        if (value < 3600L) return (value / 60L) + "m " + (value % 60L) + "s";
        if (value < 86400L) return (value / 3600L) + "h " + ((value % 3600L) / 60L) + "m";
        return (value / 86400L) + "d " + ((value % 86400L) / 3600L) + "h";
    }

    private static Component cellDurationText(long seconds) {
        long value = Math.max(0L, seconds);
        if (value < 60L) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_cell_duration_seconds", value);
        if (value < 3600L) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_cell_duration_minutes_plus", Math.max(1L, value / 60L));
        if (value < 86400L) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_cell_duration_hours_plus", Math.max(1L, value / 3600L));
        return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_cell_duration_days_plus", Math.max(1L, value / 86400L));
    }

    private static String formatExact(long value) {
        return NumberFormat.getIntegerInstance(Locale.ROOT).format(Math.max(0L, value));
    }

    private KineticButton addButton(int x, int y, int width, Component label, Runnable action) {
        return ui().button(x, y, width).text(label).onClick(action).build();
    }

    private KineticButton addCompactButton(int x, int y, int width, Component label, Runnable action) {
        return ui().button(x, y, width).compact().text(label).onClick(action).build();
    }

    private static void setControlEnabled(KineticControl control, boolean enabled) {
        if (control != null) control.setEnabled(enabled);
    }

    private record Row(Shop.Entry entry) {}

    private record RowClick(Row row, int index) {}

    private record Cell(int x, int y) {}

    private static final class ShopMemory {
        private boolean valid;
        private Shop.Mode mode = Shop.Mode.BUY;
        private String activePageName = ALL_PAGE;
        private String searchQuery = "";
        private int scroll;
        private int pageScroll;
        private int selectedIndex = -1;
        private int amount = 1;
    }

    private record BalanceAnimation(long from, long to, long delta, long start) {
        long display() {
            long age = System.currentTimeMillis() - start;
            if (age < 1000L) return from;
            long progress = Math.min(350L, age - 1000L);
            return from + (to - from) * progress / 350L;
        }

        boolean deltaVisible() {
            return System.currentTimeMillis() - start < 1000L;
        }
    }
}
