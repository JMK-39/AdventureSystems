package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.kineticcore.api.client.gui.input.MouseButton;
import dev.xyat.kineticcore.api.client.gui.text.KineticText;
import dev.xyat.kineticcore.api.client.input.KineticMouseButtons;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.adventuresystems.curios.wallet.client.ShopClientPreferences;
import dev.xyat.adventuresystems.curios.wallet.data.CurrencyType;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.adventuresystems.curios.wallet.data.StackCodec;
import dev.xyat.adventuresystems.curios.wallet.network.Network;
import dev.xyat.adventuresystems.curios.wallet.shop.Shop;
import dev.xyat.kineticcore.api.client.search.KineticSearch;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.overlay.KineticOverlays;
import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.widget.KineticControl;
import dev.xyat.kineticcore.api.client.gui.widget.KineticButton;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.ui.NumberType;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.input.MouseInput;
import dev.xyat.kineticcore.api.client.gui.input.MouseDragInput;
import dev.xyat.kineticcore.api.client.gui.input.ScrollInput;
import dev.xyat.kineticcore.api.client.gui.widget.KineticTextField;
import dev.xyat.kineticcore.api.client.gui.widget.KineticNumberField;
import dev.xyat.kineticcore.api.client.gui.widget.KineticSlider;
import dev.xyat.kineticcore.api.client.tooltip.KineticItemTooltips;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import dev.xyat.adventuresystems.text.AdventureText;
import dev.xyat.kineticcore.api.client.gui.state.LayerState;
import dev.xyat.kineticcore.api.client.gui.scroll.KineticScrollAnimator;
import dev.xyat.kineticcore.api.client.gui.scroll.KineticScrollController;
import dev.xyat.kineticcore.api.client.gui.scroll.KineticScrollSettings;
import dev.xyat.adventuresystems.ftb.util.BridgeFTB;
import java.text.NumberFormat;
import java.util.*;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShopScreen extends KineticPage {
    private enum OverlayLayer {
        REWARD_PICKER,
        QUEST_PICKER,
        CHOICE_OVERLAY
    }

    private static final int BUTTON_HEIGHT = 20;
    private static final int TOP_BUTTON_WIDTH = 48;
    private static final int BACK_BUTTON_WIDTH = 52;
    private static final int CLOSE_BUTTON_WIDTH = 52;
    private static final int DETAIL_WIDTH = 160;
    private static final int GRID_CELL_WIDTH = 90;
    // Room for a status line 3 px under the top line and a 14 px price badge 2 px above the bottom line.
    private static final int GRID_CELL_HEIGHT = 32;
    private static final int GRID_COLUMN_GAP = 1;
    private static final int GRID_ROW_GAP = 3;
    private static final int GRID_TOP_PADDING = 1;
    private static final int PRODUCT_SLOT_SIZE = 20;
    private static final int DETAIL_AMOUNT_SLIDER_WIDTH = 118;
    private static final int CURRENCY_COLUMNS = 4;
    private static final int TRADE_AMOUNT_LABEL_WIDTH = 60;
    // The balance label has the same room in every language and scrolls when longer, so the currency cells never move.
    private static final int BALANCE_LABEL_W = 40;
    private static final int CURRENCY_ROW_HEIGHT = 20;
    private static final int GRID_MAX_COLUMNS = 5;
    private static final int PAGE_TAB_HEIGHT = 20;
    private static final int PAGE_TAB_GAP = 4;
    private static final int PAGE_TAB_ARROW_WIDTH = 18;
    private static final int PAGE_TAB_EDGE_PADDING = 2;
    private static final int MAX_PAGE_BUTTON_WIDGETS = 16;
    private static final int SEARCH_BOX_WIDTH = 150;
    private static final int REWARD_PICKER_VISIBLE_ROWS = 5;
    private static final int REWARD_PICKER_ROW_HEIGHT = 20;
    private static final int CHOICE_OVERLAY_BUTTON_WIDTH = 86;
    private static final int PAGE_SCROLLBAR_HEIGHT = 4;
    private static final int LIST_SCROLLBAR_WIDTH = 4;
    private static final int DETAIL_GAP = 6;
    private static final int DETAIL_CONTENT_LEFT_PAD = 8;
    private static final int DETAIL_RIGHT_MARGIN = 2;
    private static final int CURRENCY_SLOT_SIZE = 16;
    private static final float CURRENCY_ITEM_SCALE = 0.625F;
    private static final int NUMBER_BAR_HEIGHT = 14;
    private static final int DETAIL_AMOUNT_INPUT_SIZE = 18;
    private static final int DETAIL_SECTION_BUTTON_SIZE = 16;
    private static final int REWARD_PREVIEW_HEADER_HEIGHT = 16;
    private static final int DETAIL_ACTION_BUTTON_WIDTH = 84;
    private static final int DETAIL_QUEST_BUTTON_WIDTH = 220;
    private static final int QUEST_PICKER_VISIBLE_ROWS = 5;
    private static final int QUEST_PICKER_ROW_HEIGHT = 20;
    private static final int CHOICE_OVERLAY_COLUMNS = 10;
    private static final int CHOICE_OVERLAY_SLOT = 28;
    private static final int CHOICE_OVERLAY_GAP = 4;
    private static final int CHOICE_OVERLAY_VISIBLE_ROWS = 5;
    private static final int CHOICE_OVERLAY_SCROLLBAR_WIDTH = 4;
    private static final int REWARD_PREVIEW_COLUMNS = 2;
    private static final int REWARD_PREVIEW_SLOT_SIZE = 16;
    private static final int REWARD_PREVIEW_ITEM_GAP = 2;
    private static final int REWARD_PREVIEW_ROW_HEIGHT = REWARD_PREVIEW_SLOT_SIZE + REWARD_PREVIEW_ITEM_GAP;
    private static final int REWARD_PREVIEW_MAX_VISIBLE_ROWS = 3;
    private static final int REWARD_PREVIEW_TOP_GAP = 2;
    private static final int REWARD_PREVIEW_FRAME_PADDING = 2;
    private static final int DETAIL_PRICE_SLOT_SIZE = 16;
    private static final int REWARD_PREVIEW_SCROLLBAR_WIDTH = 4;
    private static final int REWARD_PREVIEW_CELL_GAP = 4;
    private static final String FAVORITES_PAGE = "\u0001favorites";
    private static final String ALL_PAGE = "\u0001all";
    private static final ShopMemory SHOP_MEMORY = new ShopMemory();

    private final boolean returnToParent;

    private final LayerState<OverlayLayer> overlayLayers =
            new LayerState<>();

    private CompoundTag balances;
    private CompoundTag shopTag;
    private boolean editorMode;
    private boolean canEdit;
    private Shop.Mode mode = Shop.Mode.BUY;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;
    private int scroll;
    private float smoothScroll;
    private float smoothPageScroll;
    private int amount = 1;
    private int selectedIndex = -1;
    private boolean draggingScrollbar;
    private boolean draggingPageScrollbar;
    private int scrollbarGrabOffset;
    private int pageScrollbarGrabOffset;
    private double rewardPickerScroll;
    private final KineticScrollAnimator rewardPickerScrollSmoothing = new KineticScrollAnimator();
    private final KineticScrollController rewardPreviewScroller = new KineticScrollController();
    private boolean rewardPreviewExpanded;
    private final KineticScrollController questPickerScroller = new KineticScrollController();
    private final KineticScrollController choiceOverlayScroller = new KineticScrollController();
    private int choiceOverlaySelectedIndex = -1;
    private KineticButton tradeButton;
    private KineticButton questButton;
    private KineticButton amountMinusButton;
    private KineticButton amountPlusButton;
    private KineticButton amountTenButton;
    private KineticButton amountMaxButton;
    private KineticSlider amountSlider;
    private KineticButton buyModeButton;
    private KineticButton sellModeButton;
    private KineticButton editorModeButton;
    private KineticButton backpackSourceButton;
    private KineticNumberField amountBox;
    private KineticTextField searchBox;
    private KineticButton favoritesPageButton;
    private KineticButton allPageButton;
    private KineticButton pagePrevButton;
    private KineticButton pageNextButton;
    private KineticButton rewardPreviewToggleButton;
    private String searchQuery = "";
    private String activePageName = ALL_PAGE;
    private int pageScroll;
    private long shopReceivedAt;
    private final List<String> pages = new ArrayList<>();
    private final KineticScrollController pageScrollController = new KineticScrollController()
            .bindSelection(this::selectedPageScrollIndex, this::pageJumpOffset);
    private int syncedPageScrollOffset;
    private final KineticScrollController productScrollController = new KineticScrollController()
            .bindSelection(() -> selectedIndex,
                    index -> Math.max(0, (index / gridColumns()) * (GRID_CELL_HEIGHT + GRID_ROW_GAP)
                            - (contentHeightVisible() - GRID_CELL_HEIGHT) / 2));
    private int syncedProductScrollOffset;
    private final List<String> visiblePageButtonPages = new ArrayList<>();
    private final List<KineticButton> pageButtons = new ArrayList<>();
    private final List<Row> rows = new ArrayList<>();
    private final Map<String, BalanceAnimation> animations = new HashMap<>();
    private final Map<String, Integer> selectedRewardIndices = new HashMap<>();
    private final Map<Long, String> clientQuestTitleCache = new HashMap<>();
    private final Map<String, String> searchTextCache = new HashMap<>();
    private long clientQuestTitleCacheAt;
    private Shop.Entry contextEntry;
    private Shop.Entry dragSourceEntry;
    private Shop.Entry dragTargetEntry;
    private int dragMouseX;
    private int dragMouseY;
    private boolean useBackpackSource;
    private boolean useRsSource;
    private String pendingSelectKey;

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

    public ShopScreen(boolean returnToParent, CompoundTag balances, CompoundTag shopTag, boolean editorMode) {
        super(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_title"));
        this.returnToParent = returnToParent;
        setPausesGame(false);
        useCanvas(
                640,
                360,
                6
        );
        this.balances = balances == null ? new CompoundTag() : balances.copy();
        this.shopTag = shopTag == null ? new CompoundTag() : shopTag.copy();
        this.editorMode = editorMode;
        this.canEdit = dev.xyat.adventuresystems.data.Nbt.bool(this.shopTag, "CanEdit");
        this.useBackpackSource = dev.xyat.adventuresystems.data.Nbt.bool(this.shopTag, "UseBackpack");
        this.useRsSource = dev.xyat.adventuresystems.data.Nbt.bool(this.shopTag, "UseRs");
        this.shopReceivedAt = System.currentTimeMillis();
        applySessionMemory();
    }

    private void applySessionMemory() {
        if (!SHOP_MEMORY.valid) return;
        this.mode = SHOP_MEMORY.mode == null ? Shop.Mode.BUY : SHOP_MEMORY.mode;
        this.activePageName = SHOP_MEMORY.activePageName == null || SHOP_MEMORY.activePageName.isBlank()
                ? ALL_PAGE
                : SHOP_MEMORY.activePageName;
        this.searchQuery = SHOP_MEMORY.searchQuery == null ? "" : SHOP_MEMORY.searchQuery;
        this.scroll = Math.max(0, SHOP_MEMORY.scroll);
        this.smoothScroll = this.scroll;
        this.pageScroll = Math.max(0, SHOP_MEMORY.pageScroll);
        this.smoothPageScroll = this.pageScroll;
        this.selectedIndex = Math.max(-1, SHOP_MEMORY.selectedIndex);
        this.amount = Math.max(1, Math.min(64, SHOP_MEMORY.amount));
    }

    private void rememberSessionPosition() {
        SHOP_MEMORY.valid = true;
        SHOP_MEMORY.mode = this.mode;
        SHOP_MEMORY.activePageName = this.activePageName;
        SHOP_MEMORY.searchQuery = this.searchQuery;
        SHOP_MEMORY.scroll = this.scroll;
        SHOP_MEMORY.pageScroll = this.pageScroll;
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

    @Override
    protected void build(KineticUi ui) {
        this.left = 4;
        this.top = 4;
        this.panelWidth = Math.max(620, width() - 8);
        this.panelHeight = Math.max(348, height() - 8);
        this.favoritesPageButton = null;
        this.allPageButton = null;
        this.pagePrevButton = null;
        this.pageNextButton = null;
        this.rewardPreviewToggleButton = null;
        this.editorModeButton = null;
        this.pageButtons.clear();
        this.visiblePageButtonPages.clear();

        buyModeButton = addButton(
                buyButtonX(), top + 6, TOP_BUTTON_WIDTH,
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_buy_tab"),
                () -> switchMode(Shop.Mode.BUY)
        );
        sellModeButton = addButton(
                sellButtonX(), top + 6, TOP_BUTTON_WIDTH,
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_sell_tab"),
                () -> switchMode(Shop.Mode.SELL)
        );
        // 编辑相关操作收进“编辑 ▾”菜单（只有可编辑的玩家才显示），物品来源开关收进“物品来源 ▾”菜单。
        if (canEdit) {
            editorModeButton = addButton(
                    editorModeButtonX(), top + 6, editorMenuButtonWidth(),
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_menu_editor"),
                    this::openEditorMenu
            );
        }
        backpackSourceButton = addButton(
                backpackButtonX(), top + 6, sourceMenuButtonWidth(),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_menu_source"),
                this::openSourceMenu
        );
        addButton(
                backButtonX(), top + 6, BACK_BUTTON_WIDTH,
                AdventureText.translatable("gui.adventuresystems.curios.wallet.back"),
                this::returnToPreviousScreen
        );
        addButton(
                closeButtonX(), top + 6, CLOSE_BUTTON_WIDTH,
                AdventureText.translatable("gui.adventuresystems.curios.wallet.close"),
                this::close
        );
        favoritesPageButton = addButton(
                listLeft(), pageTabsY(), pageButtonWidthForPage(FAVORITES_PAGE),
                pageButtonLabel(FAVORITES_PAGE),
                () -> selectPage(FAVORITES_PAGE)
        );
        allPageButton = addButton(
                listLeft() + pageButtonWidthForPage(FAVORITES_PAGE) + PAGE_TAB_GAP,
                pageTabsY(), pageButtonWidthForPage(ALL_PAGE),
                pageButtonLabel(ALL_PAGE),
                () -> selectPage(ALL_PAGE)
        );

        pagePrevButton = addButton(
                fixedPageTabsEndX(), pageTabsY(), PAGE_TAB_ARROW_WIDTH,
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_page_previous"),
                () -> {
                    pageScroll = Math.max(0, pageScroll - pageScrollStep());
                    clampPageScroll();
                    refreshPageButtons();
                }
        );

        for (int i = 0; i < MAX_PAGE_BUTTON_WIDGETS; i++) {
            int slot = i;
            KineticButton pageButton = addButton(0, 0, 40, Component.empty(), () -> selectVisiblePage(slot));
            setControlVisible(pageButton, false);
            setControlEnabled(pageButton, false);
            pageButtons.add(pageButton);
        }

        pageNextButton = addButton(
                pageRightArrowX(), pageTabsY(), PAGE_TAB_ARROW_WIDTH,
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_page_next"),
                () -> {
                    pageScroll = Math.min(maxPageScroll(), pageScroll + pageScrollStep());
                    clampPageScroll();
                    refreshPageButtons();
                }
        );

        searchBox = ui.textField(searchBoxX(), searchBoxY(), SEARCH_BOX_WIDTH)
                .label(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_search"))
                .placeholder(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_search_hint"))
                .firstShownTextAsDefault().build();
        searchBox.limitTextLength(64);
        searchBox.setTextValue(searchQuery);
        searchBox.onTextChange(value -> {
            searchQuery = value == null ? "" : value;
            resetScrollImmediately();
            selectedIndex = -1;
            closeShopContextMenu();
            clearDragState();
            rebuildRows();
            updateDetailWidgetState();
        });

        amountBox = ui.numberField(detailAmountInputX(), detailAmountInputY(), DETAIL_AMOUNT_INPUT_SIZE, NumberType.INT)
                .label(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_trade_amount_input"))
                .allowNegative(false).range(1, 64).firstShownTextAsDefault().build();
        amountBox.limitTextLength(2);
        amountBox.setIntValue(amount);
        amountBox.onTextChange(this::onAmountInput);

        amountSlider = ui.slider(detailAmountSliderX(), detailAmountSliderY() - 5, DETAIL_AMOUNT_SLIDER_WIDTH)
                .label(Component.empty()).range(1.0D, 64.0D, 1.0D).value(amount)
                .validator(value -> value >= 1.0D && value <= 64.0D)
                .onChange(value -> setAmount((int) Math.round(value))).build();

        rewardPreviewToggleButton = addButton(
                detailContentX(), 0, DETAIL_SECTION_BUTTON_SIZE,
                sectionToggleButtonText(rewardPreviewExpanded),
                this::toggleRewardPreviewSection
        );

        questButton = addButton(
                questButtonX(), questButtonY(), questButtonWidth(), Component.empty(),
                this::handleQuestButtonClick
        );
        amountMinusButton = addCompactButton(
                detailAmountQuickButtonX(0), detailAmountQuickButtonY(), detailAmountQuickButtonWidth(),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_amount_minus"),
                () -> adjustAmount(-1)
        );
        amountPlusButton = addCompactButton(
                detailAmountQuickButtonX(1), detailAmountQuickButtonY(), detailAmountQuickButtonWidth(),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_amount_plus"),
                () -> adjustAmount(1)
        );
        amountTenButton = addCompactButton(
                detailAmountQuickButtonX(2), detailAmountQuickButtonY(), detailAmountQuickButtonWidth(),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_amount_ten"),
                () -> setAmount(Math.min(64, Math.max(1, amount) * 10))
        );
        amountMaxButton = addCompactButton(
                detailAmountQuickButtonX(3), detailAmountQuickButtonY(), detailAmountQuickButtonWidth(),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_amount_max"),
                () -> setAmount(maxUsefulTradeAmount())
        );
        tradeButton = addButton(
                detailTradeButtonX(), detailTradeButtonY(), detailTradeButtonWidth(), Component.empty(),
                this::tradeSelectedEntry
        );
        rebuildRows();
        if (pendingSelectKey != null) {
            String key = pendingSelectKey;
            pendingSelectKey = null;
            selectedIndex = -1;
            selectRowByKey(key);
            if (key.startsWith("local:") && selectedEntry() == null) selectNewestRow();
        }
        updateDetailWidgetState();
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
        resetScrollImmediately();
        this.selectedIndex = -1;
        this.activePageName = ALL_PAGE;
        this.pageScroll = 0;
        this.smoothPageScroll = 0.0F;
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

    private void updateDetailWidgetState() {
        Shop.Entry entry = selectedEntry();
        if (buyModeButton != null) buyModeButton.setSelected(mode == Shop.Mode.BUY);
        if (sellModeButton != null) sellModeButton.setSelected(mode == Shop.Mode.SELL);
        if (editorModeButton != null) editorModeButton.setSelected(editorMode);
        if (backpackSourceButton != null) backpackSourceButton.setSelected(false);
        boolean choiceBuy = entry != null && mode == Shop.Mode.BUY && entry.selectable();
        boolean amountActive = detailTradeControlsVisible(entry);
        if (entry != null && !amountActive && !choiceBuy && amount != 1) {
            amount = 1;
            if (amountBox != null) amountBox.setIntValue(1);
            if (amountSlider != null) amountSlider.setSliderValue(1);
        }
        if (amountBox != null) {
            amountBox.moveControlX(detailAmountInputX());
            amountBox.moveControlY(detailAmountInputY());
            setControlVisible(amountBox, amountActive);
            setControlEnabled(amountBox, amountActive);
            int enteredAmount = parseCount(amountBox.textValue());
            amountBox.setValidationError(amountActive && (enteredAmount < 1
                    || !choiceBuy && !canTrade(entry, enteredAmount)));
        }
        if (amountSlider != null) {
            amountSlider.moveControlX(detailAmountSliderX());
            amountSlider.moveControlY(detailAmountSliderY() - 5);
            amountSlider.resizeControlWidth(DETAIL_AMOUNT_SLIDER_WIDTH);
            amountSlider.setSliderValue(amount);
            amountSlider.setText(Component.empty());
            setControlVisible(amountSlider, amountActive);
            setControlEnabled(amountSlider, amountActive);
        }
        updateAmountButton(amountMinusButton, 0, amountActive);
        updateAmountButton(amountPlusButton, 1, amountActive);
        updateAmountButton(amountTenButton, 2, amountActive);
        updateAmountButton(amountMaxButton, 3, amountActive);
        if (tradeButton != null) {
            tradeButton.moveControlX(detailTradeButtonX());
            tradeButton.moveControlY(detailTradeButtonY());
            tradeButton.resizeControlWidth(detailTradeButtonWidth());
            setControlVisible(tradeButton, entry != null);
            setControlEnabled(tradeButton, entry != null && !entry.locked() && (choiceBuy || canTrade(entry, amount)));
            tradeButton.setText(AdventureText.translatable(choiceBuy
                    ? "gui.adventuresystems.curios.wallet.shop_choice_open_button"
                    : mode == Shop.Mode.BUY
                    ? "gui.adventuresystems.curios.wallet.shop_buy"
                    : "gui.adventuresystems.curios.wallet.shop_sell"));
        }
        if (rewardPreviewToggleButton != null) {
            boolean visible = entry != null && entry.gacha() && rewardPreviewY() >= 0;
            rewardPreviewToggleButton.moveControlX(detailContentX());
            rewardPreviewToggleButton.moveControlY(visible ? rewardPreviewY() : 0);
            rewardPreviewToggleButton.resizeControlWidth(DETAIL_SECTION_BUTTON_SIZE);
            rewardPreviewToggleButton.setText(sectionToggleButtonText(rewardPreviewExpanded));
            setControlVisible(rewardPreviewToggleButton, visible);
            setControlEnabled(rewardPreviewToggleButton, visible);
        }
        if (questButton != null) {
            questButton.moveControlX(questButtonX());
            questButton.moveControlY(questButtonY());
            questButton.resizeControlWidth(questButtonWidth());
            boolean questVisible = hasQuestList(entry);
            setControlVisible(questButton, questVisible);
            setControlEnabled(questButton, questVisible);
            questButton.setText(questButtonText(entry));
        }
    }


    // Controls can draw above the page-drawn reward dialog, reward preview and quest list in some modpacks, so the controls under an
    // open dialog are hidden while it shows. They are shown again at the start of the next frame, before the normal
    // per-frame state decides whether they should be visible.
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
        List<KineticControl> controls = new ArrayList<>();
        for (KineticControl control : new KineticControl[] {tradeButton, questButton, amountMinusButton, amountPlusButton,
                amountTenButton, amountMaxButton, amountSlider, buyModeButton, sellModeButton, editorModeButton,
                backpackSourceButton, amountBox, searchBox, favoritesPageButton, allPageButton, pagePrevButton,
                pageNextButton, rewardPreviewToggleButton}) {
            if (control != null) controls.add(control);
        }
        controls.addAll(pageButtons);
        for (KineticControl control : controls) {
            if (!control.controlVisible()) continue;
            boolean covered = choice && overlaps(control, choiceOverlayX(), choiceOverlayY(), choiceOverlayWidth(), choiceOverlayHeight())
                    || quests && overlaps(control, questPickerX(), questPickerY(), questPickerWidth(), questPickerHeight(entry))
                    || rewards && overlaps(control, rewardPickerX(), rewardPickerY(), rewardPickerWidth(), rewardPickerHeight(entry));
            if (!covered) continue;
            control.setControlVisible(false);
            hiddenUnderOverlay.add(control);
        }
    }

    private static boolean overlaps(KineticControl control, int x, int y, int width, int height) {
        return control.controlX() < x + width && control.controlX() + control.controlWidth() > x
                && control.controlY() < y + height && control.controlY() + control.controlHeight() > y;
    }

    private void updateAmountButton(KineticButton button, int slot, boolean visible) {
        if (button == null) return;
        button.moveControlX(detailAmountQuickButtonX(slot));
        button.moveControlY(detailAmountQuickButtonY());
        button.resizeControlWidth(detailAmountQuickButtonWidth());
        setControlVisible(button, visible);
        setControlEnabled(button, visible);
    }

    @Override
    protected void renderBackground(@NotNull KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updateSmoothScrolling();
        restoreControlsUnderOverlay();
        updateDetailWidgetState();
        hideControlsUnderOverlay();
        renderPanel(graphics);
        graphics.scrollingTextCentered(title(), shopTitleX(), top + 8, Math.max(0, Math.min(shopTitleX() - (editorModeButtonX() + editorMenuButtonWidth() + 4), backpackButtonX() - 4 - shopTitleX()) * 2), KineticTheme.current().text(), true);
        renderBalanceSection(graphics);
        renderPageScrollBar(graphics, mouseX, mouseY);
        renderListBorder(graphics);
        renderEmptyListHint(graphics);
        Component hint = AdventureText.translatable(shopHintKey());
        drawHintText(graphics, hint, listLeft() + 2, contentTop() - 11);
        renderDetailBackground(graphics);
    }

    @Override
    protected void renderForeground(@NotNull KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderPageSelectionFlash(graphics);
        renderProductButtonContents(graphics, mouseX, mouseY);
        renderScrollbar(graphics, mouseX, mouseY);
        renderDetailForeground(graphics, mouseX, mouseY);
        renderDragPreview(graphics);
        if (isChoiceOverlayOpen()) renderChoiceOverlay(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltips(int scaledMouseX, int scaledMouseY) {
        if (isChoiceOverlayOpen()) {
            List<Component> overlayTooltip = choiceOverlayTooltipAt(scaledMouseX, scaledMouseY);
            if (!overlayTooltip.isEmpty()) showTooltip(overlayTooltip);
            return;
        }
        ItemStack stack = hoveredItemStackAt(scaledMouseX, scaledMouseY);
        if (!stack.isEmpty()) {
            showItemTooltip(stack);
            return;
        }
        List<Component> tooltip = tooltipAt(scaledMouseX, scaledMouseY);
        if (!tooltip.isEmpty()) showTooltip(tooltip);
    }

    private void renderPanel(KineticGraphics graphics) {
        // The outer frame owns the shop boundary. Individual balance/list/detail sections
        // render their own frames, so a second full-size inner panel would intersect
        // the toolbar and create stacked/doubled borders.
        KineticTheme.panel(graphics, left, top, panelWidth, panelHeight);
    }

    private void renderBalanceSection(KineticGraphics graphics) {
        int x = left + 14;
        int y = balanceY();
        int width = panelWidth - 28;
        int height = balanceHeight();
        KineticTheme.panelAlt(graphics, x, y, width, height);
        Component label = AdventureText.translatable("gui.adventuresystems.curios.wallet.balance_title");
        int labelX = x + 8;
        int labelY = y + 7;
        graphics.scrollingText(label, labelX, labelY, BALANCE_LABEL_W, KineticTheme.current().text(), true);
        List<CurrencyType> currencies = sortedCurrenciesByValueDesc();
        int cellStartX = labelX + BALANCE_LABEL_W + 8;
        int usableWidth = x + width - 8 - cellStartX;
        int cellWidth = Math.max(92, usableWidth / CURRENCY_COLUMNS);
        for (int i = 0; i < Math.min(currencies.size(), CURRENCY_COLUMNS * 2); i++) {
            CurrencyType currency = currencies.get(i);
            int column = i % CURRENCY_COLUMNS;
            int row = i / CURRENCY_COLUMNS;
            int cellX = cellStartX + column * cellWidth;
            int cellY = y + 7 + row * CURRENCY_ROW_HEIGHT;
            if (cellX + cellWidth > x + width - 4) continue;
            renderCurrencyCell(graphics, currency, cellX, cellY, cellWidth - 4);
        }
    }

    private void renderCurrencyCell(KineticGraphics graphics, CurrencyType currency, int x, int y, int width) {
        ItemStack stack = stack(currency.itemId());
        String text = formatCompact(displayAmount(currency.itemId()));
        renderItemCheckerSlot(graphics, x, y - 5, 18);
        KineticTheme.item(graphics, stack, x, y - 5, 18, 0.75F, false);
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_balance_amount", text), x + 20, y, Math.max(0, Math.min(width - 20, KineticText.width(text))), KineticTheme.current().text(), true);
        BalanceAnimation animation = animations.get(currency.itemId());
        if (animation != null && animation.deltaVisible()) {
            String delta = formatDelta(animation.delta());
            int deltaX = x + 23 + KineticText.width(text);
            if (deltaX + KineticText.width(delta) <= x + width) {
                graphics.scrollingText(AdventureText.translatable(animation.delta() > 0L
                                ? "gui.adventuresystems.curios.wallet.shop_balance_delta_positive"
                                : "gui.adventuresystems.curios.wallet.shop_balance_delta_negative", delta), deltaX, y, Math.max(0, x + width - deltaX), KineticTheme.current().text(), true);
            }
        }
    }

    private void renderPageScrollBar(KineticGraphics graphics, int mouseX, int mouseY) {
        int width = pageScrollBarWidth();
        int max = maxPageScroll();
        if (width <= 0 || max <= 0) return;
        pageScrollController.renderHorizontal(graphics, mouseX, mouseY, listLeft(), pageScrollBarY(),
                width, PAGE_SCROLLBAR_HEIGHT, 24);
    }

    private void renderPageSelectionFlash(KineticGraphics graphics) {
        int selectedPage = selectedPageScrollIndex();
        if (selectedPage < 0) return;
        if (selectedPage <= 1) {
            KineticButton button = selectedPage == 0 ? favoritesPageButton : allPageButton;
            if (button != null && button.controlVisible()) {
                pageScrollController.renderSelectionFlash(graphics, selectedPage,
                        button.controlX(), button.controlY(), button.controlWidth(), button.controlHeight());
            }
            return;
        }
        for (int i = 0; i < visiblePageButtonPages.size() && i < pageButtons.size(); i++) {
            if (!Objects.equals(visiblePageButtonPages.get(i), activePageName)) continue;
            KineticButton button = pageButtons.get(i);
            if (button.controlVisible()) {
                pageScrollController.renderSelectionFlash(graphics, selectedPage,
                        button.controlX(), button.controlY(), button.controlWidth(), button.controlHeight());
            }
            break;
        }
    }

    private int selectedPageScrollIndex() {
        if (Objects.equals(activePageName, FAVORITES_PAGE)) return 0;
        if (Objects.equals(activePageName, ALL_PAGE)) return 1;
        int index = pages.indexOf(activePageName);
        return index < 0 ? -1 : index + 2;
    }

    private int pageJumpOffset(int selectionIndex) {
        if (selectionIndex <= 1) return 0;
        int pageIndex = selectionIndex - 2;
        if (pageIndex >= pages.size()) return pageScroll;
        return categoryPageStart(pageIndex)
                - (categoryViewportWidth() - pageButtonWidthForPage(pages.get(pageIndex))) / 2;
    }

    private void renderListBorder(KineticGraphics graphics) {
        int x = listLeft();
        int y = contentTop();
        int width = listWidth();
        int height = contentHeightVisible();
        KineticTheme.gridFrame(graphics, x - 1, y - 1, width + 2, height + 2);
    }

    private void renderEmptyListHint(KineticGraphics graphics) {
        if (!rows.isEmpty()) return;
        Component text = AdventureText.translatable(Objects.equals(activePageName, FAVORITES_PAGE)
                ? "gui.adventuresystems.curios.wallet.shop_favorites_empty"
                : "gui.adventuresystems.curios.wallet.shop_page_empty");
        graphics.scrollingTextCentered(text, listLeft() + listWidth() / 2, contentTop() + contentHeightVisible() / 2 - 4, listWidth() - 8, KineticTheme.current().text(), true);
    }

    private void renderProductCellContent(KineticGraphics graphics, Row row, Cell cell) {
        Shop.Entry entry = row.entry();
        if (entry == null) return;
        if (dragSourceEntry != null && entry.index() == dragSourceEntry.index()) return;
        boolean canTrade = canTrade(entry, 1);
        int itemSlotX = cellItemSlotX(cell);
        int itemSlotY = cellItemSlotY(cell);
        ItemStack displayStack = cellDisplayStack(entry);
        renderItemCheckerSlot(graphics, itemSlotX, itemSlotY, PRODUCT_SLOT_SIZE);
        renderProductItem(graphics, displayStack, itemSlotX, itemSlotY);
        // “+”放在格子左上角：右下角是物品数量。
        if (hasMultipleRewards(entry)) renderSmallPlus(graphics, itemSlotX + 3, itemSlotY + 3);

        renderCellStatus(graphics, entry, cell, itemSlotX + PRODUCT_SLOT_SIZE + 3);

        ItemStack currency = stack(entry.currencyId());
        String price = formatCompact(entry.price());
        int numberX = cellNumberX(cell);
        int numberY = cellNumberY(cell);
        int numberW = cellNumberWidth(price);
        renderNumberBar(graphics, numberX, numberY, numberW);
        drawCellString(graphics, cellPriceText(price, canTrade), numberX + 3, numberY + (NUMBER_BAR_HEIGHT - 8) / 2, numberW - 6);
        renderCurrencyItem(graphics, currency, numberX + numberW + 2, numberY - 1);
    }

    private void renderProductButtonContents(KineticGraphics graphics, int mouseX, int mouseY) {
        enableShopScissor(
                graphics,
                listLeft(),
                contentTop(),
                listLeft() + listWidth(),
                contentTop() + contentHeightVisible()
        );
        try {
            for (int rowIndex = visibleIndexStart(); rowIndex < visibleIndexEnd(); rowIndex++) {
                Cell cell = cellForIndex(rowIndex);
                Row row = rows.get(rowIndex);
                Shop.Entry entry = row.entry();
                boolean hovered = isHover(mouseX, mouseY, cell.x(), cell.y(), GRID_CELL_WIDTH, GRID_CELL_HEIGHT);
                KineticTheme.stateSurface(graphics, cell.x(), cell.y(), GRID_CELL_WIDTH, GRID_CELL_HEIGHT,
                        KineticTheme.Surface.PANEL_ALT, rowIndex == selectedIndex, hovered,
                        entry != null && !canTrade(entry, 1));
                graphics.clipped(cell.x() + 1, cell.y() + 1, cell.x() + GRID_CELL_WIDTH - 1,
                        cell.y() + GRID_CELL_HEIGHT - 1,
                        () -> renderProductCellContent(graphics, row, cell));
                productScrollController.renderSelectionFlash(graphics, rowIndex,
                        cell.x(), cell.y(), GRID_CELL_WIDTH, GRID_CELL_HEIGHT);
            }
        } finally {
            disableShopScissor(graphics);
        }
    }

    private void renderCellStatus(KineticGraphics graphics, Shop.Entry entry, Cell cell, int minX) {
        Component primary = cellPrimaryStatusText(entry);
        Component limit = cellLimitStatusText(entry);
        int available = Math.max(4, cell.x() + GRID_CELL_WIDTH - 3 - minX);
        int y = cell.y() + 3;
        if (primary != null && limit != null) {
            // Fixed halves, so the limit text starts at the same place in every language; each half scrolls.
            int primaryWidth = Math.max(8, (available - 3) / 2);
            drawCellString(graphics, primary, minX, y, primaryWidth);
            int limitX = minX + primaryWidth + 3;
            drawCellString(graphics, limit, limitX, y, Math.max(1, available - primaryWidth - 3));
            return;
        }
        if (primary != null) {
            drawCellString(graphics, primary, minX, y, available);
            return;
        }
        if (limit != null) {
            drawCellString(graphics, limit, minX, y, available);
            return;
        }
        if (entry.gacha()) {
            drawCellString(graphics, AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_gacha_marker"), minX, y, available);
        }
    }

    private int cellItemSlotX(Cell cell) {
        return cell.x() + 3;
    }

    private int cellItemSlotY(Cell cell) {
        return cell.y() + (GRID_CELL_HEIGHT - PRODUCT_SLOT_SIZE) / 2;
    }

    private int cellNumberX(Cell cell) {
        return cell.x() + PRODUCT_SLOT_SIZE + 5;
    }

    private int cellNumberY(Cell cell) {
        return cell.y() + GRID_CELL_HEIGHT - NUMBER_BAR_HEIGHT - 3;
    }

    private int cellNumberWidth(String price) {
        int startOffset = PRODUCT_SLOT_SIZE + 5;
        int maximum = Math.max(24, GRID_CELL_WIDTH - startOffset - CURRENCY_SLOT_SIZE - 4);
        // Always the full width, so the currency icon after the badge never moves with the price.
        return maximum;
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


    private void renderItemCheckerSlot(KineticGraphics graphics, int x, int y) {
        renderItemCheckerSlot(graphics, x, y, 20);
    }

    private void renderItemCheckerSlot(KineticGraphics graphics, int x, int y, int size) {
        KineticTheme.itemSlot(graphics, x, y, size, 4, false);
    }

    private void renderProductItem(KineticGraphics graphics, ItemStack stack, int x, int y) {
        KineticTheme.item(graphics, stack, x, y, PRODUCT_SLOT_SIZE, 0.875F, true);
    }

    private void renderNumberBar(KineticGraphics graphics, int x, int y, int width) {
        KineticTheme.stateSurface(
                graphics, x, y, width, NUMBER_BAR_HEIGHT,
                KineticTheme.Surface.FIELD, false, false, false
        );
    }

    private void renderCurrencyItem(KineticGraphics graphics, ItemStack stack, int x, int y) {
        renderItemCheckerSlot(graphics, x, y, CURRENCY_SLOT_SIZE);
        KineticTheme.item(graphics, stack, x, y, CURRENCY_SLOT_SIZE, CURRENCY_ITEM_SCALE, false);
    }

    private void drawCellString(KineticGraphics graphics, Component text, int x, int y, int available) {
        if (text == null || text.getString().isBlank()) return;
        Component line = text;
        graphics.scrollingText(line, x, y, Math.max(0, available), KineticTheme.current().text(), false);
    }

    private Component cellPriceText(String price, boolean available) {
        return AdventureText.translatable(available
                ? "gui.adventuresystems.curios.wallet.shop_cell_price_ready"
                : "gui.adventuresystems.curios.wallet.shop_cell_price_blocked", price);
    }

    private void renderDetailBackground(KineticGraphics graphics) {
        int x = detailX();
        int y = detailTop();
        int right = left + panelWidth - 2;
        KineticTheme.panelAlt(graphics, x, y, right - x, detailVisibleHeight());
    }

    private void renderDetailForeground(KineticGraphics graphics, int mouseX, int mouseY) {
        int x = detailX();
        int y = detailTop();
        int contentX = detailContentX();
        int textX = contentX + 24;
        int clipRight = left + panelWidth - 6;
        int scissorLeft = Math.max(x, contentX - 4);
        enableShopScissor(graphics, scissorLeft, y + 2, clipRight, y + detailVisibleHeight() - 2);
        try {
            Shop.Entry entry = selectedEntry();
            if (entry == null) {
                graphics.scrollingTextCentered(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_select_entry_hint"), x + (left + panelWidth - x) / 2, y + 18, Math.max(0, clipRight - x - 4), KineticTheme.current().text(), true);
                return;
            }
            ItemStack item = detailDisplayStack(entry);
            renderItemCheckerSlot(graphics, contentX - 2, y + 4);
            KineticTheme.item(graphics, item, contentX - 2, y + 4, 20, 0.875F, false);
            graphics.itemDecorations(item, contentX, y + 6);
            if (hasMultipleRewards(entry)) renderSmallPlus(graphics, contentX + 8, y + 15);
            graphics.scrollingText(AdventureText.translatable(entry.locked()
                            ? "gui.adventuresystems.curios.wallet.shop_detail_name_locked"
                            : "gui.adventuresystems.curios.wallet.shop_detail_name_ready",
                            entryDisplayName(entry, item)), textX, y + 4, Math.max(0, clipRight - textX - 4), KineticTheme.current().text(), true);
            graphics.scrollingText(entryCountText(entry), textX, y + 18, Math.max(0, clipRight - textX - 4), KineticTheme.current().text(), true);
            renderEntryPriceLine(graphics, entry, contentX, y + 33);
            if (mode == Shop.Mode.BUY) {
                renderBuyPaymentSourceLines(graphics, entry, contentX, detailBuyPaymentY(entry));
            }
            if (mode == Shop.Mode.BUY) {
                renderBuyLimitStatusLines(graphics, entry, contentX, detailStatusY(entry));
            } else {
                renderStatusLines(graphics, entry, contentX, detailStatusY(entry));
            }
            if (entry.gacha()) {
                renderRewardPreview(graphics, entry, contentX, rewardPreviewY(), mouseX, mouseY);
            }
            if (detailTradeControlsVisible(entry)) {
                graphics.scrollingText(tradeAmountLabel(), contentX, detailAmountInputY() + 5, Math.max(0, detailAmountInputX() - contentX - 4), KineticTheme.current().text(), true);
                renderTradeCostPreview(graphics, entry);
            }
            if (isRewardPickerOpen() && isSelectableRewardEntry(entry)) {
                graphics.isolated(() -> { graphics.raise(1); renderRewardPicker(graphics, entry, mouseX, mouseY); });
            }
            if (isQuestPickerOpen() && hasQuestList(entry)) {
                graphics.isolated(() -> { graphics.raise(1); renderQuestPicker(graphics, entry, mouseX, mouseY); });
            }
        } finally {
            disableShopScissor(graphics);
        }
    }


    private void renderTradeCostPreview(KineticGraphics graphics, Shop.Entry entry) {
        long cost = tradeCostAmount(entry);
        Component text = AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_trade_cost_preview", formatCompact(cost));
        int textX = detailTradeCostTextX();
        int textY = detailAmountInputY() + 5;
        graphics.scrollingText(text, textX, textY, Math.max(0, detailTradeCostIconX(entry) - textX - 3), KineticTheme.current().text(), false);
        ItemStack stack = tradeCostStack(entry);
        if (!stack.isEmpty()) {
            int slotX = detailTradeCostIconX(entry);
            int slotY = detailTradeCostIconY();
            renderItemCheckerSlot(graphics, slotX, slotY, DETAIL_PRICE_SLOT_SIZE);
            KineticTheme.item(graphics, stack, slotX, slotY, DETAIL_PRICE_SLOT_SIZE, 0.625F, false);
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
        int lineY = y;
        if (isSelectableRewardEntry(entry)) {
            graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_sell_choice_hint"), x, lineY, Math.max(0, detailContentX() + detailContentWidth() - x - 4), KineticTheme.current().text(), true);
            lineY += 11;
        }
        long inventoryCount = sellInventoryCount(entry);
        graphics.scrollingText(AdventureText.translatable(inventoryCount > 0L
                        ? "gui.adventuresystems.curios.wallet.shop_sell_inventory_count_ready"
                        : "gui.adventuresystems.curios.wallet.shop_sell_inventory_count_missing", materialCompact(inventoryCount)), x, lineY, Math.max(0, detailContentX() + detailContentWidth() - x - 4), KineticTheme.current().text(), true);
        lineY += 11;
        graphics.scrollingText(backpackMaterialText(entry), x, lineY, Math.max(0, detailContentX() + detailContentWidth() - x - 4), KineticTheme.current().text(), true);
        lineY += 11;
        graphics.scrollingText(rsMaterialText(entry), x, lineY, Math.max(0, detailContentX() + detailContentWidth() - x - 4), KineticTheme.current().text(), true);
        lineY += 11;
        if (sellProgress(entry) > 0L) {
            graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_sell_progress_detail", materialCompact(sellProgress(entry)), materialCompact(sellTradeStack(entry).getCount())), x, lineY, Math.max(0, detailContentX() + detailContentWidth() - x - 4), KineticTheme.current().text(), true);
        }
    }

    private void renderBuyLimitStatusLines(KineticGraphics graphics, Shop.Entry entry, int x, int y) {
        int lineY = y;
        if (entry.timedLimitSeconds() > 0) {
            long remaining = timedRemaining(entry);
            graphics.scrollingText(timedLimitText(entry, remaining), x, lineY, Math.max(0, detailContentX() + detailContentWidth() - x - 4), KineticTheme.current().text(), true);
            lineY += 14;
        }
        if (entry.totalLimit() > 0) {
            int remaining = totalRemaining(entry);
            graphics.scrollingText(AdventureText.translatable(remaining > 0
                            ? "gui.adventuresystems.curios.wallet.shop_total_limit_status"
                            : "gui.adventuresystems.curios.wallet.shop_total_limit_status_sold_out",
                            entry.totalBought(), entry.totalLimit(), remaining), x, lineY, Math.max(0, detailContentX() + detailContentWidth() - x - 4), KineticTheme.current().text(), true);
        }
    }



    private void renderBuyPaymentSourceLines(KineticGraphics graphics, Shop.Entry entry, int x, int y) {
        long inventoryCount = buyInventoryCount(entry);
        long walletCount = buyWalletCount(entry);
        long total = buyPaymentTotal(entry);
        graphics.scrollingText(AdventureText.translatable(inventoryCount > 0L
                        ? "gui.adventuresystems.curios.wallet.shop_buy_source_inventory_ready"
                        : "gui.adventuresystems.curios.wallet.shop_buy_source_inventory_empty", materialCompact(inventoryCount)), x, y, 74 - 4, KineticTheme.current().text(), true);
        graphics.scrollingText(AdventureText.translatable(walletCount > 0L
                        ? "gui.adventuresystems.curios.wallet.shop_buy_source_wallet_ready"
                        : "gui.adventuresystems.curios.wallet.shop_buy_source_wallet_empty", materialCompact(walletCount)), x + 74, y, Math.max(0, detailContentX() + detailContentWidth() - (x + 74) - 4), KineticTheme.current().text(), true);
        graphics.scrollingText(buyBackpackMaterialText(entry), x, y + 11, 74 - 4, KineticTheme.current().text(), true);
        graphics.scrollingText(buyRsMaterialText(entry), x + 74, y + 11, Math.max(0, detailContentX() + detailContentWidth() - (x + 74) - 4), KineticTheme.current().text(), true);
        graphics.scrollingText(AdventureText.translatable(total >= entry.price()
                        ? "gui.adventuresystems.curios.wallet.shop_buy_source_total_ready"
                        : "gui.adventuresystems.curios.wallet.shop_buy_source_total_missing", materialCompact(total)), x, y + 22, Math.max(0, detailContentX() + detailContentWidth() - x - 4), KineticTheme.current().text(), true);
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

    private void renderSelectionOutline(KineticGraphics graphics, int x, int y) {
        KineticTheme.indicatorOutline(graphics, x, y, GRID_CELL_WIDTH + 2, GRID_CELL_HEIGHT + 2, KineticTheme.Indicator.INFO);
    }

    private void renderScrollbar(KineticGraphics graphics, int mouseX, int mouseY) {
        Scrollbar scrollbar = scrollbar();
        if (!scrollbar.visible()) return;
        productScrollController.render(graphics, mouseX, mouseY,
                scrollbar.x(), scrollbar.trackTop(), LIST_SCROLLBAR_WIDTH, scrollbar.trackHeight(), 20);
    }

    private List<Component> tooltipAt(int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (isHover(mouseX, mouseY, buyButtonX(), top + 6, TOP_BUTTON_WIDTH, BUTTON_HEIGHT)) {
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.tooltip_name", AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_buy_tab")));
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_buy_tab"));
            return tooltip;
        }
        if (isHover(mouseX, mouseY, sellButtonX(), top + 6, TOP_BUTTON_WIDTH, BUTTON_HEIGHT)) {
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.tooltip_name", AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_sell_tab")));
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_sell_tab"));
            return tooltip;
        }
        if (editorModeButton != null && isHover(mouseX, mouseY, editorModeButton.controlX(), editorModeButton.controlY(), editorModeButton.controlWidth(), BUTTON_HEIGHT)) {
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.tooltip_name", editorModeButtonText()));
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_menu_editor_tip"));
            return tooltip;
        }
        if (backpackSourceButton != null && isHover(mouseX, mouseY, backpackSourceButton.controlX(), backpackSourceButton.controlY(), backpackSourceButton.controlWidth(), BUTTON_HEIGHT)) {
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.tooltip_name", AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_menu_source")));
            tooltip.add(sourceButtonText(true));
            tooltip.add(sourceButtonText(false));
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_source_priority_tip"));
            return tooltip;
        }
        String hoveredPage = pageAt(mouseX, mouseY);
        if (hoveredPage != null) {
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.tooltip_name", pageLabel(hoveredPage)));
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_page_click_tip"));
            return tooltip;
        }
        Long questClick = detailQuestClickAt(mouseX, mouseY);
        if (questClick != null && questClick != 0L) {
            Shop.Entry entry = selectedEntry();
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_quest_click_tip"));
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_quest_title", questTitleById(entry, questClick)));
            return tooltip;
        }
        if (selectedEntry() != null && isInsideQuestPicker(mouseX, mouseY)) {
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_quest_list_click_tip"));
            return tooltip;
        }
        currencyTooltip(mouseX, mouseY, tooltip);
        if (!tooltip.isEmpty()) return tooltip;
        if (detailTradeControlsVisible(selectedEntry()) && isHover(mouseX, mouseY, detailAmountInputX(), detailAmountInputY(), DETAIL_AMOUNT_INPUT_SIZE, DETAIL_AMOUNT_INPUT_SIZE)) {
            tooltip.add(tradeAmountTooltipLabel());
            tooltip.add(AdventureText.translatable(mode == Shop.Mode.BUY ? "gui.adventuresystems.curios.wallet.shop_tooltip_amount_input" : "gui.adventuresystems.curios.wallet.shop_tooltip_sell_amount_input"));
            return tooltip;
        }
        if (detailTradeControlsVisible(selectedEntry()) && isHover(mouseX, mouseY, detailAmountSliderX(), detailAmountSliderY() - 4, DETAIL_AMOUNT_SLIDER_WIDTH, 16)) {
            tooltip.add(tradeAmountTooltipLabel());
            tooltip.add(AdventureText.translatable(mode == Shop.Mode.BUY ? "gui.adventuresystems.curios.wallet.shop_tooltip_amount_slider" : "gui.adventuresystems.curios.wallet.shop_tooltip_sell_amount_slider"));
            return tooltip;
        }
        if (selectedEntry() != null && isHover(mouseX, mouseY, detailTradeButtonX(), detailTradeButtonY(), detailTradeButtonWidth(), BUTTON_HEIGHT)) {
            Shop.Entry entry = selectedEntry();
            if (entry != null) {
                if (entry.locked()) {
                    tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_action_locked", AdventureText.translatable(mode == Shop.Mode.BUY && entry.selectable() ? "gui.adventuresystems.curios.wallet.shop_choice_open_button" : mode == Shop.Mode.BUY ? "gui.adventuresystems.curios.wallet.shop_buy" : "gui.adventuresystems.curios.wallet.shop_sell")));
                    tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_need_complete_task", requiredQuestTitlesText(entry)));
                } else {
                    tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_action_ready", AdventureText.translatable(mode == Shop.Mode.BUY && entry.selectable() ? "gui.adventuresystems.curios.wallet.shop_choice_open_button" : mode == Shop.Mode.BUY ? "gui.adventuresystems.curios.wallet.shop_buy" : "gui.adventuresystems.curios.wallet.shop_sell")));
                    tooltip.add(AdventureText.translatable(mode == Shop.Mode.BUY && entry.selectable() ? "gui.adventuresystems.curios.wallet.shop_choice_open_tip" : mode == Shop.Mode.BUY ? "gui.adventuresystems.curios.wallet.shop_tooltip_buy_button" : "gui.adventuresystems.curios.wallet.shop_tooltip_sell_button"));
                    long previewTotal = mode == Shop.Mode.BUY ? safeMultiply(entry.price(), amount) : safeMultiply(entry.price(), previewSuccessTrades(entry, amount));
                    tooltip.add(totalTradeText(previewTotal, entry.currencyId()));
                    if (mode == Shop.Mode.SELL) tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_sell_inventory_notice"));
                }
                return tooltip;
            }
        }
        Row row = rowAt(mouseX, mouseY);
        if (row == null) return tooltip;
        Shop.Entry entry = row.entry();
        if (entry == null) return tooltip;

        tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_entry_name", entryDisplayName(entry, cellContentStack(entry))));
        appendEntryDescriptionTooltip(tooltip, entry);
        tooltip.add(AdventureText.translatable(mode == Shop.Mode.BUY ? "gui.adventuresystems.curios.wallet.shop_tooltip_click_buy_view" : "gui.adventuresystems.curios.wallet.shop_tooltip_click_sell_view"));
        tooltip.add(AdventureText.translatable(editorMode
                ? "gui.adventuresystems.curios.wallet.shop_tooltip_editor_actions"
                : "gui.adventuresystems.curios.wallet.shop_tooltip_right_favorite"));
        return tooltip;
    }

    private void appendEntryDescriptionTooltip(List<Component> tooltip, Shop.Entry entry) {
        if (entry == null || entry.description() == null || entry.description().isBlank()) return;
        for (var line : KineticText.wrap(AdventureText.literal(entry.description()), 220)) {
            tooltip.add(AdventureText.fromSequence(line));
        }
    }

    private String shopHintKey() {
        if (mode == Shop.Mode.SELL) return editorMode ? "gui.adventuresystems.curios.wallet.shop_editor_sell_hint_modal" : "gui.adventuresystems.curios.wallet.shop_sell_hint";
        return editorMode ? "gui.adventuresystems.curios.wallet.shop_editor_hint_modal" : "gui.adventuresystems.curios.wallet.shop_hint";
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

    private void renderEntryPriceLine(KineticGraphics graphics, Shop.Entry entry, int x, int y) {
        Component text = entryPriceIconText(entry);
        graphics.scrollingText(text, x, y, Math.max(0, detailPriceIconX(entry) - x - 3), KineticTheme.current().text(), false);
        ItemStack currency = stack(entry.currencyId());
        if (currency.isEmpty()) return;
        int slotX = detailPriceIconX(entry);
        int slotY = detailPriceIconY();
        renderItemCheckerSlot(graphics, slotX, slotY, DETAIL_PRICE_SLOT_SIZE);
        KineticTheme.item(graphics, currency, slotX, slotY, DETAIL_PRICE_SLOT_SIZE, 0.625F, false);
    }

    private Component entryPriceIconText(Shop.Entry entry) {
        return AdventureText.translatable(
                mode == Shop.Mode.BUY
                        ? "gui.adventuresystems.curios.wallet.shop_single_price_icon"
                        : "gui.adventuresystems.curios.wallet.shop_sell_income_single_icon",
                formatExact(entry.price())
        );
    }

    private int detailPriceIconX(Shop.Entry entry) {
        // A fixed spot at the right of the detail area, whatever the language; the price text scrolls before it.
        return detailContentX() + detailContentWidth() - DETAIL_PRICE_SLOT_SIZE - 4;
    }

    private int detailPriceIconY() {
        return detailTop() + 28;
    }

    private MutableComponent totalTradeText(long total, String currencyId) {
        return AdventureText.translatable(mode == Shop.Mode.BUY ? "gui.adventuresystems.curios.wallet.shop_price" : "gui.adventuresystems.curios.wallet.shop_sell_total_income", AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_price_value", formatExact(total)), currencyName(currencyId));
    }

    private void currencyTooltip(int mouseX, int mouseY, List<Component> tooltip) {
        int x = left + 14;
        int y = balanceY();
        int width = panelWidth - 28;
        int cellStartX = x + 8 + BALANCE_LABEL_W + 8;
        int usableWidth = x + width - 8 - cellStartX;
        int cellWidth = Math.max(92, usableWidth / CURRENCY_COLUMNS);
        List<CurrencyType> currencies = sortedCurrenciesByValueDesc();
        for (int i = 0; i < Math.min(currencies.size(), CURRENCY_COLUMNS * 2); i++) {
            CurrencyType currency = currencies.get(i);
            int column = i % CURRENCY_COLUMNS;
            int row = i / CURRENCY_COLUMNS;
            int cellX = cellStartX + column * cellWidth;
            int cellY = y + 1 + row * CURRENCY_ROW_HEIGHT;
            if (isHover(mouseX, mouseY, cellX, cellY, cellWidth - 4, CURRENCY_ROW_HEIGHT)) {
                tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_currency_name", ShopGuiSupport.stackNameComponent(currency.itemId())));
                tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_currency_balance", AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_currency_balance_value", formatExact(amountOf(currency.itemId())))));
                tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_currency_value", AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_currency_value_value", formatExact(currency.value()))));
                return;
            }
        }
    }

    @Override
    protected boolean onMouseClickCapture(MouseInput input) {
        double mouseX = input.x();
        double mouseY = input.y();
        int button = input.rawButton();
        OverlayLayer activeLayer =
                overlayLayers.activeLayer();

        if (activeLayer != null) {
            clearTextFocus();
            handleOverlayClick(
                    activeLayer,
                    (int) mouseX,
                    (int) mouseY,
                    button
            );
            return true;
        }

        RowClick ctrlDragRow =
                rowClickAt(
                        (int) mouseX,
                        (int) mouseY
                );

        if (KineticMouseButtons.isPrimary(button)
                && editorMode
                && KineticClientRuntime.controlModifierDown()
                && ctrlDragRow != null) {
            beginEntryDrag(
                    ctrlDragRow,
                    (int) mouseX,
                    (int) mouseY
            );

            clearTextFocus();
            return true;
        }

        return false;
    }

    @Override
    protected boolean onMouseClick(MouseInput input) {
        double mouseX = input.x();
        double mouseY = input.y();
        int button = input.rawButton();

        if (KineticMouseButtons.isPrimary(button)
                && isInsideRewardPreviewScrollbar(
                        (int) mouseX,
                        (int) mouseY
                )) {
            Scrollbar rewardPreviewScrollbar = rewardPreviewScrollbar(selectedEntry(), rewardPreviewY());
            if (rewardPreviewScroller.beginDrag(mouseX, mouseY, input.button(), rewardPreviewScrollbar.x(), rewardPreviewScrollbar.trackTop(),
                    REWARD_PREVIEW_SCROLLBAR_WIDTH, rewardPreviewScrollbar.trackHeight(), 20, 2)) {
                clearTextFocus();
                return true;
            }
        }

        if (KineticMouseButtons.isPrimary(button)
                && selectedEntry() != null
                && !Objects.requireNonNull(
                        selectedEntry()
                ).locked()
                && isSelectableRewardIcon(
                        (int) mouseX,
                        (int) mouseY
                )) {
            openChoiceOverlay(
                    selectedEntry()
            );

            clearTextFocus();
            return true;
        }

        Long questClick =
                detailQuestClickAt(
                        (int) mouseX,
                        (int) mouseY
                );

        if (KineticMouseButtons.isPrimary(button)
                && questClick != null
                && questClick != 0L) {
            openKtQuest(questClick);
            clearTextFocus();
            return true;
        }

        if (KineticMouseButtons.isPrimary(button)
                && isInsidePageScrollBar(
                        mouseX,
                        mouseY
                )) {
            draggingPageScrollbar = true;

            int barWidth =
                    pageScrollBarWidth();

            int thumbWidth =
                    pageScrollThumbWidth(
                            barWidth
                    );

            int thumbX =
                    pageScrollThumbX(
                            listLeft(),
                            barWidth,
                            thumbWidth
                    );

            if (mouseX >= thumbX
                    && mouseX <= thumbX + thumbWidth) {
                pageScrollbarGrabOffset =
                        (int) mouseX - thumbX;
            } else {
                pageScrollbarGrabOffset =
                        thumbWidth / 2;
            }

            updatePageScrollFromMouse(
                    (int) mouseX
            );

            clearTextFocus();
            return true;
        }

        Scrollbar scrollbar =
                scrollbar();

        if (KineticMouseButtons.isPrimary(button)
                && scrollbar.visible()
                && mouseX >= scrollbar.x() - 2
                && mouseX <= scrollbar.x()
                + LIST_SCROLLBAR_WIDTH
                + 2
                && mouseY >= scrollbar.trackTop()
                && mouseY <= scrollbar.trackBottom()) {
            draggingScrollbar = true;

            if (mouseY >= scrollbar.thumbTop()
                    && mouseY <= scrollbar.thumbBottom()) {
                scrollbarGrabOffset =
                        (int) mouseY
                                - scrollbar.thumbTop();
            } else {
                scrollbarGrabOffset =
                        scrollbar.thumbHeight() / 2;

                updateScrollFromMouse(
                        (int) mouseY
                );
            }

            clearTextFocus();
            return true;
        }

        RowClick rowClick =
                rowClickAt(
                        (int) mouseX,
                        (int) mouseY
                );

        if (rowClick == null) {
            clearTextFocus();
            return false;
        }

        if (KineticMouseButtons.isSecondary(button)) {
            openShopContextMenu(
                    rowClick,
                    (int) mouseX,
                    (int) mouseY
            );

            clearTextFocus();
            return true;
        }

        if (KineticMouseButtons.isPrimary(button)) {
            selectRow(rowClick.index());
            clearTextFocus();
            return true;
        }

        return false;
    }

    private void handleOverlayClick(
            OverlayLayer layer,
            int mouseX,
            int mouseY,
            int button
    ) {
        switch (layer) {
            case CHOICE_OVERLAY ->
                    handleChoiceOverlayClick(
                            mouseX,
                            mouseY,
                            button
                    );
            case QUEST_PICKER ->
                    handleQuestOverlayClick(
                            mouseX,
                            mouseY,
                            button
                    );
            case REWARD_PICKER ->
                    handleRewardOverlayClick(
                            mouseX,
                            mouseY,
                            button
                    );
        }
    }

    private void handleQuestOverlayClick(
            int mouseX,
            int mouseY,
            int button
    ) {
        if (KineticMouseButtons.isPrimary(button)
                && isInsideQuestPickerScrollbar(
                        mouseX,
                        mouseY
                )) {
            Scrollbar questScrollbar = questPickerScrollbar(selectedEntry());
            if (questPickerScroller.beginDrag(mouseX, mouseY, MouseButton.of(button), questScrollbar.x(), questScrollbar.trackTop(),
                    LIST_SCROLLBAR_WIDTH, questScrollbar.trackHeight(), 16, 2)) {
                return;
            }
        }

        if (KineticMouseButtons.isPrimary(button)
                && handleQuestPickerClick(
                        mouseX,
                        mouseY
                )) {
            return;
        }

        if (KineticMouseButtons.isPrimary(button)) {
            overlayLayers.close(
                    OverlayLayer.QUEST_PICKER
            );

            questPickerScroller.release(MouseButton.LEFT);
        }
    }

    private void handleRewardOverlayClick(
            int mouseX,
            int mouseY,
            int button
    ) {
        if (KineticMouseButtons.isPrimary(button)
                && handleRewardPickerClick(
                        mouseX,
                        mouseY
                )) {
            return;
        }

        if (KineticMouseButtons.isPrimary(button)) {
            overlayLayers.close(
                    OverlayLayer.REWARD_PICKER
            );
        }
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

    private void openKtQuest(long questId) {
        if (questId == 0L) return;
        try {
            BridgeFTB.openQuest(questId);
        } catch (Throwable ignored) {
            KineticOverlays.toast("currency_wallet_shop_notice", AdventureText.translatable("msg.adventuresystems.curios.wallet.shop_open_task_fail"), KineticOverlays.Position.BOTTOM_CENTER, 2500, 0, -30);
        }
    }

    private MutableComponent tradeFailText(Shop.Entry entry) {
        if (entry != null && entry.locked()) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_need_complete_task_first", requiredQuestTitlesText(entry));
        if (mode == Shop.Mode.SELL) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_sell_fail_preview", entry == null ? 0L : sellTotalMaterials(entry));
        if (entry != null && timedRemaining(entry) > 0L) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_timed_wait", formatDuration(timedRemaining(entry)));
        if (entry != null && totalRemaining(entry) <= 0) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_limit_reached");
        return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_buy_fail_no_money");
    }

    private void renderRewardPreview(KineticGraphics graphics, Shop.Entry entry, int x, int y, int mouseX, int mouseY) {
        if (y < 0 || rewardPreviewBottomY() - y < 12) return;
        graphics.scrollingText(AdventureText.translatable(entry.selectable()
                        ? (mode == Shop.Mode.SELL
                        ? "gui.adventuresystems.curios.wallet.shop_sell_choice_title"
                        : "gui.adventuresystems.curios.wallet.shop_choice_title")
                        : "gui.adventuresystems.curios.wallet.shop_reward_probability_title"), x + DETAIL_SECTION_BUTTON_SIZE + 4, y + 4, Math.max(0, detailContentWidth() - DETAIL_SECTION_BUTTON_SIZE - 8), KineticTheme.current().translatedText(), true);
        if (!rewardPreviewExpanded) return;
        List<Shop.Reward> rewards = sortedRewardPreviewRewards(entry);
        if (rewards.isEmpty()) return;

        int listX = rewardPreviewListX();
        int listY = rewardPreviewListY(y);
        int listHeight = rewardPreviewListHeight(y);
        if (listHeight < REWARD_PREVIEW_ROW_HEIGHT) return;

        renderRewardPreviewFrame(graphics, y);
        int rewardPreviewMax = rewardPreviewMaxScroll(entry, y);
        double visualRewardPreviewScroll = rewardPreviewVisualScroll(entry, y);
        int rewardPreviewStart = Math.max(0, Math.min((int) Math.floor(visualRewardPreviewScroll), rewardPreviewMax));
        int rewardPreviewShift = (int) Math.round((visualRewardPreviewScroll - rewardPreviewStart) * REWARD_PREVIEW_ROW_HEIGHT);
        Scrollbar scrollbar = rewardPreviewScrollbar(entry, y);
        int scrollbarSpace = scrollbar.visible() ? REWARD_PREVIEW_SCROLLBAR_WIDTH + 4 : 0;
        int contentWidth = Math.max(40, detailContentWidth() - 2 - REWARD_PREVIEW_FRAME_PADDING * 2 - scrollbarSpace);
        int cellWidth = Math.max(40, (contentWidth - REWARD_PREVIEW_CELL_GAP) / REWARD_PREVIEW_COLUMNS);
        int visibleRows = rewardPreviewVisibleRows(y);

        int displaySlotCount = rewardPreviewDisplaySlotCount(entry);
        enableShopScissor(graphics, listX, listY, listX + contentWidth, listY + listHeight);
        for (int row = 0; row < visibleRows + 2; row++) {
            int rewardRow = rewardPreviewStart + row;
            int rowY = listY + row * REWARD_PREVIEW_ROW_HEIGHT - rewardPreviewShift;
            if (rowY + REWARD_PREVIEW_ROW_HEIGHT <= listY || rowY >= listY + listHeight) continue;
            for (int column = 0; column < REWARD_PREVIEW_COLUMNS; column++) {
                int displaySlot = rewardRow * REWARD_PREVIEW_COLUMNS + column;
                if (displaySlot >= displaySlotCount) break;
                int rewardIndex = rewardPreviewRewardIndexAtDisplaySlot(rewards.size(), displaySlot, displaySlotCount);
                if (rewardIndex < 0) continue;
                Shop.Reward reward = rewards.get(rewardIndex);
                int cellX = listX + column * (cellWidth + REWARD_PREVIEW_CELL_GAP);
                renderRewardPreviewCell(graphics, reward, cellX, rowY, cellWidth);
            }
        }
        disableShopScissor(graphics);

        renderRewardPreviewScrollbar(graphics, scrollbar, mouseX, mouseY);
    }

    private void renderRewardPreviewFrame(KineticGraphics graphics, int previewY) {
        int x = detailContentX();
        int y = rewardPreviewFrameY(previewY);
        int bottom = rewardPreviewBottomY();
        if (bottom <= y + 1) return;
        KineticTheme.panelAlt(graphics, x, y, detailContentWidth(), bottom - y);
    }

    private void renderRewardPreviewCell(KineticGraphics graphics, Shop.Reward reward, int x, int y, int width) {
        renderItemCheckerSlot(graphics, x, y, REWARD_PREVIEW_SLOT_SIZE);
        ItemStack stack = reward.empty() ? new ItemStack(net.minecraft.world.item.Items.BARRIER) : reward.stack();
        KineticTheme.item(graphics, stack, x, y, REWARD_PREVIEW_SLOT_SIZE, 0.625F, true);
        String chanceText = percent(reward.chance());
        graphics.scrollingText(Component.literal(chanceText), x + REWARD_PREVIEW_SLOT_SIZE + 6, y + 4,
                Math.max(0, width - REWARD_PREVIEW_SLOT_SIZE - 8), KineticTheme.current().translatedText(), true);
    }

    private void renderRewardPreviewScrollbar(KineticGraphics graphics, Scrollbar scrollbar, int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        int previewY = rewardPreviewY();
        if (!scrollbar.visible()) return;
        rewardPreviewVisualScroll(entry, previewY);
        rewardPreviewScroller.render(graphics, mouseX, mouseY, scrollbar.x(), scrollbar.trackTop(),
                REWARD_PREVIEW_SCROLLBAR_WIDTH, scrollbar.trackHeight(), 20);
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

    private int rewardPreviewY() {
        Shop.Entry entry = selectedEntry();
        if (entry == null || !entry.gacha()) return -1;
        return detailStatusEndY(entry);
    }

    private int detailStatusEndY(Shop.Entry entry) {
        int y = detailStatusY(entry);
        if (mode == Shop.Mode.BUY) {
            if (entry.timedLimitSeconds() > 0) y += 14;
            if (entry.totalLimit() > 0) y += 14;
            return y;
        }
        if (isSelectableRewardEntry(entry)) y += 11;
        y += 33;
        if (sellProgress(entry) > 0L) y += 11;
        return y;
    }

    private int rewardPreviewFrameY(int previewY) {
        return previewY + REWARD_PREVIEW_HEADER_HEIGHT + REWARD_PREVIEW_TOP_GAP;
    }

    private int rewardPreviewListX() {
        return detailContentX() + 1 + REWARD_PREVIEW_FRAME_PADDING;
    }

    private int rewardPreviewListY(int previewY) {
        return rewardPreviewFrameY(previewY) + 1 + REWARD_PREVIEW_FRAME_PADDING;
    }

    private int rewardPreviewListHeight(int previewY) {
        int contentBottom = rewardPreviewBottomY() - 1 - REWARD_PREVIEW_FRAME_PADDING;
        return Math.max(0, contentBottom - rewardPreviewListY(previewY));
    }

    private int rewardPreviewVisibleRows(
            int previewY
    ) {
        return Math.min(
                REWARD_PREVIEW_MAX_VISIBLE_ROWS,
                Math.max(
                        0,
                        rewardPreviewListHeight(previewY)
                                / REWARD_PREVIEW_ROW_HEIGHT
                )
        );
    }

    private int rewardPreviewTotalRows(Shop.Entry entry) {
        int displaySlotCount = rewardPreviewDisplaySlotCount(entry);
        return displaySlotCount <= 0 ? 0 : displaySlotCount / REWARD_PREVIEW_COLUMNS;
    }

    private int rewardPreviewMaxScroll(Shop.Entry entry, int previewY) {
        return Math.max(0, rewardPreviewTotalRows(entry) - rewardPreviewVisibleRows(previewY));
    }

    private Scrollbar rewardPreviewScrollbar(Shop.Entry entry, int previewY) {
        if (!rewardPreviewExpanded) return new Scrollbar(false, 0, 0, 0, 0, 0);
        int listY = rewardPreviewListY(previewY);
        int listHeight = rewardPreviewListHeight(previewY);
        int totalRows = rewardPreviewTotalRows(entry);
        int visibleRows = rewardPreviewVisibleRows(previewY);
        if (listHeight < REWARD_PREVIEW_ROW_HEIGHT || visibleRows <= 0 || totalRows <= visibleRows) return new Scrollbar(false, 0, 0, 0, 0, 0);
        int trackBottom = listY + listHeight;
        int barHeight = Math.max(20, listHeight * visibleRows / totalRows);
        int maxScroll = rewardPreviewMaxScroll(entry, previewY);
        double visualScroll = rewardPreviewVisualScroll(entry, previewY);
        int barY = listY + (int) Math.round(visualScroll * (listHeight - barHeight) / maxScroll);
        int barX = detailContentX() + detailContentWidth() - REWARD_PREVIEW_SCROLLBAR_WIDTH - 1 - REWARD_PREVIEW_FRAME_PADDING;
        return new Scrollbar(true, barX, listY, trackBottom, barY, barY + barHeight);
    }

    private boolean isInsideRewardPreview(int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        int previewY = rewardPreviewY();
        if (!rewardPreviewExpanded || entry == null || !entry.gacha() || previewY < 0) return false;
        int listY = rewardPreviewListY(previewY);
        int listHeight = rewardPreviewListHeight(previewY);
        if (listHeight <= 0) return false;
        Scrollbar scrollbar = rewardPreviewScrollbar(entry, previewY);
        int scrollbarSpace = scrollbar.visible() ? REWARD_PREVIEW_SCROLLBAR_WIDTH + 4 : 0;
        int contentWidth = Math.max(40, detailContentWidth() - 2 - REWARD_PREVIEW_FRAME_PADDING * 2 - scrollbarSpace);
        return isHover(mouseX, mouseY, rewardPreviewListX(), listY, contentWidth, listHeight);
    }

    private boolean isInsideRewardPreviewScrollbar(int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        int previewY = rewardPreviewY();
        if (!rewardPreviewExpanded || entry == null || !entry.gacha() || previewY < 0) return false;
        Scrollbar scrollbar = rewardPreviewScrollbar(entry, previewY);
        return scrollbar.visible() && mouseX >= scrollbar.x() - 2 && mouseX <= scrollbar.x() + REWARD_PREVIEW_SCROLLBAR_WIDTH + 2 && mouseY >= scrollbar.trackTop() && mouseY <= scrollbar.trackBottom();
    }

    private double rewardPreviewVisualScroll(Shop.Entry entry, int previewY) {
        rewardPreviewScroller.updateRange(rewardPreviewMaxScroll(entry, previewY), rewardPreviewTotalRows(entry), rewardPreviewVisibleRows(previewY));
        return rewardPreviewScroller.smoothOffset();
    }

    /** 列表格里显示的图标：设置了自定义图标就用它，否则用商品本身（多奖励时轮播）。 */
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
        int index = selectedRewardIndex(entry);
        return entry.rewards().get(index);
    }

    private Shop.Reward cycleReward(Shop.Entry entry) {
        if (entry == null || entry.rewards() == null || entry.rewards().isEmpty()) return null;
        int count = entry.rewards().size();
        int index = (int) ((System.currentTimeMillis() / 1000L) % count);
        return entry.rewards().get(index);
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

    /** 多奖励标记：画在物品之上（否则会被物品图标和数量盖住）。 */
    private void renderSmallPlus(KineticGraphics graphics, int x, int y) {
        graphics.push();
        graphics.raise(1);
        KineticTheme.stateSurface(graphics, x - 1, y - 1, 8, 8, KineticTheme.Surface.PANEL_ALT, false, false, false);
        graphics.text(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_multi_reward_plus"),
                x, y - 1, KineticTheme.current().translatedText(), true);
        graphics.pop();
    }

    private boolean isSelectableRewardIcon(int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        return isSelectableRewardEntry(entry) && isHover(mouseX, mouseY, detailContentX(), contentTop() + 14, 20, 20);
    }

    private int rewardPickerX() {
        return detailContentX();
    }

    private int rewardPickerY() {
        // Below the Quest Required button (2 px clear) when the selected entry shows one.
        int y = contentTop() + 36;
        return hasQuestList(selectedEntry()) ? Math.max(y, questButtonY() + 16 + 2) : y;
    }

    private int rewardPickerWidth() {
        return Math.max(84, left + panelWidth - 6 - detailContentX());
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
        int x = rewardPickerX();
        int y = rewardPickerY();
        return isHover(mouseX, mouseY, x, y, rewardPickerWidth(), rewardPickerHeight(entry));
    }

    private void renderRewardPicker(KineticGraphics graphics, Shop.Entry entry, int mouseX, int mouseY) {
        int x = rewardPickerX();
        int y = rewardPickerY();
        int width = rewardPickerWidth();
        int height = rewardPickerHeight(entry);
        KineticTheme.panelAlt(graphics, x, y, width, height);
        graphics.scrollingText(AdventureText.translatable(mode == Shop.Mode.SELL
                        ? "gui.adventuresystems.curios.wallet.shop_sell_choice_picker_title"
                        : "gui.adventuresystems.curios.wallet.shop_choice_picker_title"), x + 5, y + 5, Math.max(0, width - 10), KineticTheme.current().text(), true);
        int rewardPickerMax = rewardPickerMaxScroll(entry);
        rewardPickerScroll = Math.max(0D, Math.min(rewardPickerScroll, rewardPickerMax));
        double visualRewardPickerScroll = rewardPickerScrollSmoothing.follow(rewardPickerScroll, rewardPickerMax);
        int rewardPickerStart = Math.max(0, Math.min((int) Math.floor(visualRewardPickerScroll), rewardPickerMax));
        int rewardPickerShift = (int) Math.round((visualRewardPickerScroll - rewardPickerStart) * REWARD_PICKER_ROW_HEIGHT);
        int listY = y + 18;
        int selected = selectedRewardIndex(entry);
        enableShopScissor(graphics, x + 2, listY, x + width - 2, y + height);
        try {
            for (int row = 0; row < REWARD_PICKER_VISIBLE_ROWS + 2; row++) {
                int index = rewardPickerStart + row;
                if (index >= entry.rewards().size()) break;
                int rowY = listY + row * REWARD_PICKER_ROW_HEIGHT - rewardPickerShift;
                if (rowY + REWARD_PICKER_ROW_HEIGHT <= listY || rowY >= y + height) continue;
                boolean hover = isHover(mouseX, mouseY, x + 2, rowY, width - 4, REWARD_PICKER_ROW_HEIGHT);
                boolean active = index == selected;
                KineticTheme.stateSurface(
                        graphics,
                        x + 2,
                        rowY,
                        width - 4,
                        REWARD_PICKER_ROW_HEIGHT - 1,
                        KineticTheme.Surface.PANEL_ALT,
                        active,
                        hover,
                        false
                );
                Shop.Reward reward = entry.rewards().get(index);
                KineticTheme.itemSlot(graphics, x + 4, rowY + 1, 18, 4, hover);
                if (!reward.empty()) {
                    KineticTheme.item(graphics, reward.stack(), x + 4, rowY + 1, 18, 0.75F, true);
                }
                int color = active ? KineticTheme.current().translatedText() : KineticTheme.current().text();
                graphics.scrollingText(rewardName(reward), x + 25, rowY + 6, Math.max(0, width - 34), color, true);
            }
        } finally {
            disableShopScissor(graphics);
        }
    }

    private ItemStack hoveredRewardPickerItem(int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        if (!isRewardPickerOpen() || !isSelectableRewardEntry(entry) || !isInsideRewardPicker(mouseX, mouseY)) return ItemStack.EMPTY;
        int listY = rewardPickerY() + 18;
        double visualScroll = rewardPickerScrollSmoothing.follow(rewardPickerScroll, rewardPickerMaxScroll(entry));
        int index = (int) Math.floor((mouseY - listY) / (double) REWARD_PICKER_ROW_HEIGHT + visualScroll);
        int row = (int) Math.floor((mouseY - listY) / (double) REWARD_PICKER_ROW_HEIGHT);
        if (row >= REWARD_PICKER_VISIBLE_ROWS || index >= entry.rewards().size()) return ItemStack.EMPTY;
        Shop.Reward reward = entry.rewards().get(index);
        return reward.empty() ? ItemStack.EMPTY : reward.stack();
    }

    private boolean handleRewardPickerClick(int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        if (!isRewardPickerOpen() || !isSelectableRewardEntry(entry)) return false;
        if (!isInsideRewardPicker(mouseX, mouseY)) return false;
        int listY = rewardPickerY() + 18;
        if (mouseY < listY) return true;
        int row = (mouseY - listY) / REWARD_PICKER_ROW_HEIGHT;
        double visualScroll = rewardPickerScrollSmoothing.follow(rewardPickerScroll, rewardPickerMaxScroll(entry));
        int index = (int) Math.floor((mouseY - listY) / (double) REWARD_PICKER_ROW_HEIGHT + visualScroll);
        if (row < REWARD_PICKER_VISIBLE_ROWS && index >= 0 && index < entry.rewards().size()) {
            selectedRewardIndices.put(entry.key(), index);
            overlayLayers.close(OverlayLayer.REWARD_PICKER);
            return true;
        }
        return true;
    }

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
        return CHOICE_OVERLAY_COLUMNS * CHOICE_OVERLAY_SLOT + Math.max(0, CHOICE_OVERLAY_COLUMNS - 1) * CHOICE_OVERLAY_GAP + 34;
    }

    private int choiceOverlayHeight() {
        return 32 + CHOICE_OVERLAY_VISIBLE_ROWS * CHOICE_OVERLAY_SLOT + Math.max(0, CHOICE_OVERLAY_VISIBLE_ROWS - 1) * CHOICE_OVERLAY_GAP + 42;
    }

    private int choiceOverlayX() {
        return left + Math.max(10, (panelWidth - choiceOverlayWidth()) / 2);
    }

    private int choiceOverlayY() {
        return top + Math.max(28, (panelHeight - choiceOverlayHeight()) / 2);
    }

    private int choiceGridX() {
        return choiceOverlayX() + 14;
    }

    private int choiceGridY() {
        return choiceOverlayY() + 34;
    }

    private int choiceOverlayMaxScroll(Shop.Entry entry) {
        if (!isSelectableRewardEntry(entry)) return 0;
        int rows = (entry.rewards().size() + CHOICE_OVERLAY_COLUMNS - 1) / CHOICE_OVERLAY_COLUMNS;
        return Math.max(0, rows - CHOICE_OVERLAY_VISIBLE_ROWS);
    }

    private boolean isOutsideChoiceOverlay(int mouseX, int mouseY) {
        return !isChoiceOverlayOpen() || !isHover(mouseX, mouseY, choiceOverlayX(), choiceOverlayY(), choiceOverlayWidth(), choiceOverlayHeight());
    }

    private int choiceOverlayIndexAt(int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        if (!isChoiceOverlayOpen() || !isSelectableRewardEntry(entry)) return -1;
        int localX = mouseX - choiceGridX();
        int localY = mouseY - choiceGridY();
        if (localX < 0 || localY < 0) return -1;
        int pitch = CHOICE_OVERLAY_SLOT + CHOICE_OVERLAY_GAP;
        int column = localX / pitch;
        double visualScroll = choiceOverlayVisualScroll(entry);
        double visualRow = localY / (double) pitch + visualScroll;
        int row = (int) Math.floor(visualRow);
        int visibleRow = (int) Math.floor(localY / (double) pitch);
        if (column >= CHOICE_OVERLAY_COLUMNS || visibleRow >= CHOICE_OVERLAY_VISIBLE_ROWS) return -1;
        int shiftedLocalY = (int) Math.floor((visualRow - row) * pitch);
        if (localX % pitch >= CHOICE_OVERLAY_SLOT || shiftedLocalY >= CHOICE_OVERLAY_SLOT) return -1;
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
        if (isHover(mouseX, mouseY, choiceOverlayCancelX(), choiceOverlayButtonY(), CHOICE_OVERLAY_BUTTON_WIDTH, BUTTON_HEIGHT)) {
            closeChoiceOverlay();
            return;
        }
        if (isHover(mouseX, mouseY, choiceOverlayConfirmX(), choiceOverlayButtonY(), CHOICE_OVERLAY_BUTTON_WIDTH, BUTTON_HEIGHT)) {
            confirmChoiceOverlay();
            return;
        }
        Scrollbar scrollbar = choiceOverlayScrollbar(entry);
        if (choiceOverlayScroller.beginDrag(mouseX, mouseY, MouseButton.of(button), scrollbar.x(), scrollbar.trackTop(),
                CHOICE_OVERLAY_SCROLLBAR_WIDTH, scrollbar.trackHeight(), 20, 3)) {
            return;
        }
        int index = choiceOverlayIndexAt(mouseX, mouseY);
        if (index >= 0) {
            choiceOverlaySelectedIndex = index;
            selectedRewardIndices.put(entry.key(), index);
            return;
        }
        if (isOutsideChoiceOverlay(mouseX, mouseY)) {
            closeChoiceOverlay();
        }
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

    private int choiceOverlayCancelX() {
        return choiceOverlayX() + 12;
    }

    private int choiceOverlayConfirmX() {
        return choiceOverlayX() + choiceOverlayWidth() - CHOICE_OVERLAY_BUTTON_WIDTH - 12;
    }

    private int choiceOverlayButtonY() {
        return choiceOverlayY() + 7;
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
        graphics.fill(x, y, x + width, y + height, 0xFF161616);
        KineticTheme.panel(graphics, x, y, width, height);
        graphics.scrollingTextCentered(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_choice_overlay_title"), x + width / 2, y + 13, Math.max(0, 2 * (choiceOverlayConfirmX() - 4 - (x + width / 2))), KineticTheme.current().text(), true);
        renderChoiceOverlayGrid(graphics, entry, mouseX, mouseY);
        renderChoiceOverlayScrollbar(graphics, entry, mouseX, mouseY);
        KineticTheme.button(graphics, choiceOverlayCancelX(), choiceOverlayButtonY(), CHOICE_OVERLAY_BUTTON_WIDTH,
                BUTTON_HEIGHT, AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_choice_cancel"),
                isHover(mouseX, mouseY, choiceOverlayCancelX(), choiceOverlayButtonY(), CHOICE_OVERLAY_BUTTON_WIDTH, BUTTON_HEIGHT),
                true, false);
        KineticTheme.button(graphics, choiceOverlayConfirmX(), choiceOverlayButtonY(), CHOICE_OVERLAY_BUTTON_WIDTH,
                BUTTON_HEIGHT, AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_choice_confirm"),
                isHover(mouseX, mouseY, choiceOverlayConfirmX(), choiceOverlayButtonY(), CHOICE_OVERLAY_BUTTON_WIDTH, BUTTON_HEIGHT),
                choiceOverlaySelectedIndex >= 0, false);
        graphics.pop();
    }

    private void renderChoiceOverlayGrid(KineticGraphics graphics, Shop.Entry entry, int mouseX, int mouseY) {
        int gridX = choiceGridX();
        int gridY = choiceGridY();
        int pitch = CHOICE_OVERLAY_SLOT + CHOICE_OVERLAY_GAP;
        int selected = choiceOverlaySelectedIndex < 0 ? -1 : Math.max(0, Math.min(choiceOverlaySelectedIndex, entry.rewards().size() - 1));
        int choiceMax = choiceOverlayMaxScroll(entry);
        double visualChoiceScroll = choiceOverlayVisualScroll(entry);
        int choiceStart = Math.max(0, Math.min((int) Math.floor(visualChoiceScroll), choiceMax));
        int choiceShift = (int) Math.round((visualChoiceScroll - choiceStart) * pitch);
        int gridHeight = CHOICE_OVERLAY_VISIBLE_ROWS * pitch - CHOICE_OVERLAY_GAP;
        enableShopScissor(graphics, gridX, gridY, gridX + CHOICE_OVERLAY_COLUMNS * pitch - CHOICE_OVERLAY_GAP, gridY + gridHeight);
        for (int row = 0; row < CHOICE_OVERLAY_VISIBLE_ROWS + 2; row++) {
            for (int column = 0; column < CHOICE_OVERLAY_COLUMNS; column++) {
                int index = (choiceStart + row) * CHOICE_OVERLAY_COLUMNS + column;
                if (index >= entry.rewards().size()) break;
                int slotX = gridX + column * pitch;
                int slotY = gridY + row * pitch - choiceShift;
                if (slotY + CHOICE_OVERLAY_SLOT <= gridY || slotY >= gridY + gridHeight) continue;
                boolean hover = isHover(mouseX, mouseY, slotX, slotY, CHOICE_OVERLAY_SLOT, CHOICE_OVERLAY_SLOT);
                boolean active = index == selected;
                Shop.Reward reward = entry.rewards().get(index);
                KineticTheme.itemSlot(graphics, slotX, slotY, CHOICE_OVERLAY_SLOT, 4, hover);
                if (active) {
                    KineticTheme.stateOutline(graphics, slotX, slotY, CHOICE_OVERLAY_SLOT, CHOICE_OVERLAY_SLOT, true, hover, false);
                }
                if (reward.empty()) {
                    graphics.scrollingTextCentered(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_gacha_marker"), slotX + CHOICE_OVERLAY_SLOT / 2, slotY + 9, CHOICE_OVERLAY_SLOT - 4, KineticTheme.current().text(), true);
                } else {
                    graphics.item(reward.stack(), slotX + 6, slotY + 5);
                    graphics.itemDecorations(reward.stack(), slotX + 6, slotY + 5);
                }
            }
        }
        disableShopScissor(graphics);
    }

    private void renderChoiceOverlayScrollbar(KineticGraphics graphics, Shop.Entry entry, int mouseX, int mouseY) {
        Scrollbar scrollbar = choiceOverlayScrollbar(entry);
        if (!scrollbar.visible()) return;
        choiceOverlayVisualScroll(entry);
        choiceOverlayScroller.render(graphics, mouseX, mouseY, scrollbar.x(), scrollbar.trackTop(),
                CHOICE_OVERLAY_SCROLLBAR_WIDTH, scrollbar.trackHeight(), 20);
    }

    private Scrollbar choiceOverlayScrollbar(Shop.Entry entry) {
        int max = choiceOverlayMaxScroll(entry);
        if (max <= 0) return new Scrollbar(false, 0, 0, 0, 0, 0);
        int trackTop = choiceGridY();
        int trackBottom = choiceGridY() + CHOICE_OVERLAY_VISIBLE_ROWS * CHOICE_OVERLAY_SLOT + Math.max(0, CHOICE_OVERLAY_VISIBLE_ROWS - 1) * CHOICE_OVERLAY_GAP;
        int x = choiceOverlayX() + choiceOverlayWidth() - 14;
        int trackHeight = trackBottom - trackTop;
        int thumbHeight = Math.max(20, trackHeight * CHOICE_OVERLAY_VISIBLE_ROWS / Math.max(CHOICE_OVERLAY_VISIBLE_ROWS + max, 1));
        int travel = Math.max(1, trackHeight - thumbHeight);
        double visualScroll = choiceOverlayVisualScroll(entry);
        int thumbTop = trackTop + (int) Math.round(travel * (visualScroll / max));
        return new Scrollbar(true, x, trackTop, trackBottom, thumbTop, thumbTop + thumbHeight);
    }

    private double choiceOverlayVisualScroll(Shop.Entry entry) {
        int max = choiceOverlayMaxScroll(entry);
        choiceOverlayScroller.updateRange(max, CHOICE_OVERLAY_VISIBLE_ROWS + max, CHOICE_OVERLAY_VISIBLE_ROWS);
        return choiceOverlayScroller.smoothOffset();
    }

    private List<Component> choiceOverlayTooltipAt(int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (!isChoiceOverlayOpen()) return tooltip;
        if (isHover(mouseX, mouseY, choiceOverlayConfirmX(), choiceOverlayButtonY(), CHOICE_OVERLAY_BUTTON_WIDTH, BUTTON_HEIGHT)) {
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.tooltip_name", AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_choice_confirm")));
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_choice_confirm_tip"));
            return tooltip;
        }
        if (isHover(mouseX, mouseY, choiceOverlayCancelX(), choiceOverlayButtonY(), CHOICE_OVERLAY_BUTTON_WIDTH, BUTTON_HEIGHT)) {
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.tooltip_name", AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_choice_cancel")));
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_choice_cancel_tip"));
            return tooltip;
        }
        int index = choiceOverlayIndexAt(mouseX, mouseY);
        Shop.Entry entry = selectedEntry();
        if (isSelectableRewardEntry(entry) && index >= 0) {
            Shop.Reward reward = entry.rewards().get(index);
            if (!reward.empty()) {
                try {
                    tooltip.addAll(KineticItemTooltips.textLines(reward.stack()));
                } catch (Throwable ignored) {
                    tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.tooltip_name", rewardName(reward)));
                }
            } else {
                tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.tooltip_name", rewardName(reward)));
            }
            tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_choice_pick_tip"));
            if (reward.commandReward()) tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_reward_command_tooltip"));
        }
        return tooltip;
    }


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

    @Override
    protected boolean onMouseRelease(MouseInput input) {
        double mouseX = input.x();
        double mouseY = input.y();
        int button = input.rawButton();
        OverlayLayer activeLayer =
                overlayLayers.activeLayer();

        if (activeLayer != null) {
            questPickerScroller.release(input.button());
            choiceOverlayScroller.release(input.button());
            return true;
        }

        if (KineticMouseButtons.isPrimary(button)
                && dragSourceEntry != null) {
            finishEntryDrag(
                    (int) mouseX,
                    (int) mouseY
            );
            return true;
        }

        boolean rewardPreviewReleased = rewardPreviewScroller.release(input.button());
        if (KineticMouseButtons.isPrimary(button)
                && (draggingScrollbar
                || rewardPreviewReleased
                || draggingPageScrollbar)) {
            draggingScrollbar = false;
            draggingPageScrollbar = false;
            return true;
        }

        return false;
    }

    @Override
    protected boolean onMouseDrag(MouseDragInput input) {
        double mouseX = input.x();
        double mouseY = input.y();
        int button = input.rawButton();
        OverlayLayer activeLayer =
                overlayLayers.activeLayer();

        if (activeLayer != null) {
            if (KineticMouseButtons.isPrimary(button) && activeLayer == OverlayLayer.CHOICE_OVERLAY) {
                Scrollbar choiceScrollbar = choiceOverlayScrollbar(selectedEntry());
                choiceOverlayScroller.drag(mouseY, choiceScrollbar.trackTop(), choiceScrollbar.trackHeight(), 20);
            } else if (KineticMouseButtons.isPrimary(button) && activeLayer == OverlayLayer.QUEST_PICKER) {
                Scrollbar questScrollbar = questPickerScrollbar(selectedEntry());
                questPickerScroller.drag(mouseY, questScrollbar.trackTop(), questScrollbar.trackHeight(), 16);
            }

            return true;
        }

        if (KineticMouseButtons.isPrimary(button)
                && dragSourceEntry != null) {
            updateEntryDrag(
                    (int) mouseX,
                    (int) mouseY
            );
            return true;
        }

        if (KineticMouseButtons.isPrimary(button)
                && draggingScrollbar) {
            updateScrollFromMouse(
                    (int) mouseY
            );
            return true;
        }

        if (KineticMouseButtons.isPrimary(button) && rewardPreviewExpanded && selectedEntry() != null) {
            Scrollbar rewardPreviewScrollbar = rewardPreviewScrollbar(selectedEntry(), rewardPreviewY());
            if (rewardPreviewScroller.drag(mouseY, rewardPreviewScrollbar.trackTop(), rewardPreviewScrollbar.trackHeight(), 20)) {
                return true;
            }
        }

        if (KineticMouseButtons.isPrimary(button)
                && draggingPageScrollbar) {
            updatePageScrollFromMouse(
                    (int) mouseX
            );
            return true;
        }

        return false;
    }

    @Override
    protected boolean onMouseScroll(ScrollInput input) {
        double mouseX = input.x();
        double mouseY = input.y();
        double delta = input.deltaY();
        OverlayLayer activeLayer =
                overlayLayers.activeLayer();

        if (activeLayer != null) {
            switch (activeLayer) {
                case CHOICE_OVERLAY -> {
                    choiceOverlayVisualScroll(selectedEntry());
                    choiceOverlayScroller.scroll(delta);
                }

                case QUEST_PICKER -> {
                    if (isInsideQuestPicker(
                            (int) mouseX,
                            (int) mouseY
                    )) {
                        questPickerVisualScroll(selectedEntry());
                        questPickerScroller.scroll(delta);
                    }
                }

                case REWARD_PICKER -> {
                    if (isInsideRewardPicker(
                            (int) mouseX,
                            (int) mouseY
                    )) {
                        Shop.Entry entry = selectedEntry();
                        rewardPickerScroll = rewardPickerScrollSmoothing.wheel(
                                rewardPickerScroll, delta, 1.0D, rewardPickerMaxScroll(entry)
                        );
                    }
                }

            }

            return true;
        }

        if (isInsideRewardPreview((int) mouseX, (int) mouseY)) {
            Shop.Entry entry = selectedEntry();
            int previewY = rewardPreviewY();
            if (entry != null && previewY >= 0) {
                rewardPreviewVisualScroll(entry, previewY);
                rewardPreviewScroller.scroll(delta);
            }
            return true;
        }
        int tabY = pageTabsY();
        if (mouseY >= tabY && mouseY <= pageScrollBarY() + PAGE_SCROLLBAR_HEIGHT + 4 && mouseX >= listLeft() && mouseX <= pageRightArrowX() + PAGE_TAB_ARROW_WIDTH) {
            pageScroll = Math.max(0, Math.min(maxPageScroll(), pageScroll - Math.round((float) (delta * 28.0D * KineticScrollSettings.wheelItemsPerNotch()))));
            refreshPageButtons();
            return true;
        }
        if (isHover((int) mouseX, (int) mouseY, listLeft(), contentTop(), listWidth(), contentHeightVisible())) {
            scroll = Math.max(0, Math.min(maxScroll(), scroll - Math.round((float) (delta * 18.0D * KineticScrollSettings.wheelItemsPerNotch()))));
            return true;
        }
        return false;
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

        // 排序只在当前分页内进行：移到最前 / 最后、上移 / 下移都以同一分页里相邻的商品为准。
        List<Shop.Entry> siblings = pageSiblings(contextEntry);
        int position = indexOfEntry(siblings, contextEntry);
        Shop.Entry first = siblings.isEmpty() ? null : siblings.get(0);
        Shop.Entry previous = position > 0 ? siblings.get(position - 1) : null;
        Shop.Entry next = position >= 0 && position < siblings.size() - 1 ? siblings.get(position + 1) : null;
        Shop.Entry last = siblings.isEmpty() ? null : siblings.get(siblings.size() - 1);
        List<KineticOverlays.MenuItem> items = new ArrayList<>();
        items.add(KineticOverlays.MenuItem.action(
                AdventureText.translatable(isFavorite(contextEntry)
                        ? "gui.adventuresystems.curios.wallet.shop_context_unfavorite"
                        : "gui.adventuresystems.curios.wallet.shop_context_favorite"),
                this::toggleContextFavorite
        ));
        if (editorMode) {
            items.add(KineticOverlays.MenuItem.action(
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_context_edit"),
                    this::editContextEntry
            ));
            items.add(KineticOverlays.MenuItem.action(
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_context_copy"),
                    this::copyContextEntry
            ));
            items.add(contextMenuMoveAction(
                    "gui.adventuresystems.curios.wallet.shop_context_move_front", previous != null && first != null,
                    first
            ));
            items.add(contextMenuMoveAction(
                    "gui.adventuresystems.curios.wallet.shop_context_move_up", previous != null,
                    previous
            ));
            items.add(contextMenuMoveAction(
                    "gui.adventuresystems.curios.wallet.shop_context_move_down", next != null,
                    next
            ));
            items.add(contextMenuMoveAction(
                    "gui.adventuresystems.curios.wallet.shop_context_move_end", next != null && last != null,
                    last
            ));
            items.add(KineticOverlays.MenuItem.danger(
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_context_delete"),
                    this::deleteContextEntry
            ));
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
     * 与 {@code entry} 同在当前分页（全部 / 收藏 / 具体分页）里的商品，按全局顺序排列；不受搜索影响。
     * 服务器的移动是“取出后插入到目标序号”，所以移到某个相邻商品的序号即可排到它的前面（上移）或后面（下移）。
     */
    private List<Shop.Entry> pageSiblings(Shop.Entry entry) {
        List<Shop.Entry> result = new ArrayList<>();
        if (entry == null) return result;
        for (Shop.Entry candidate : Shop.entriesFromTag(shopTag, entry.mode())) {
            if (!entryExcludedFromActivePage(candidate)) result.add(candidate);
        }
        result.sort(java.util.Comparator.comparingInt(Shop.Entry::index));
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

    // ---- 本地内存更新：编辑操作先改本地数据立即显示，服务器保存后广播的刷新再覆盖为权威数据 ----

    private ListTag localEntryList(Shop.Mode entryMode, boolean create) {
        if (shopTag == null || entryMode == null) return null;
        String key = entryMode == Shop.Mode.BUY ? "Buy" : "Sell";
        if (!shopTag.contains(key, Tag.TAG_LIST)) {
            if (!create) return null;
            shopTag.put(key, new ListTag());
        }
        return shopTag.getList(key, Tag.TAG_COMPOUND);
    }

    /** 与服务器 {@code Shop.move} 相同：取出 from 后插入到 to，其它商品依次让位。 */
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

    /** 编辑器保存时调用：修改已有商品或在末尾追加新商品，并切到它所在的分页选中它。 */
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
            resetScrollImmediately();
        }
        String page = draft.pageName == null ? "" : draft.pageName.trim();
        if (!Objects.equals(activePageName, ALL_PAGE) && !Objects.equals(activePageName, page)) activePageName = page;
        refreshAfterLocalEdit(tag.getString("Key"));
    }

    /** 本地新建的商品被服务器数据替换后，重新选中当前分页里最新追加的那件。 */
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

    /** 编辑器分页选择器的候选：当前模式的分页在前，另一模式独有的分页在后；不含默认空分页。 */
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

    /** 当前浏览的具体分页名（全部 / 收藏时为空），新建商品默认放进这个分页。 */
    String activeConcretePage() {
        if (Objects.equals(activePageName, ALL_PAGE) || Objects.equals(activePageName, FAVORITES_PAGE)) return "";
        return activePageName == null ? "" : activePageName;
    }

    private Component sourceButtonText(boolean backpack) {
        boolean enabled = backpack ? useBackpackSource : useRsSource;
        String key;
        if (backpack) {
            key = enabled
                    ? "gui.adventuresystems.curios.wallet.shop_source_backpack_on"
                    : "gui.adventuresystems.curios.wallet.shop_source_backpack_off";
        } else {
            key = enabled
                    ? "gui.adventuresystems.curios.wallet.shop_source_rs_on"
                    : "gui.adventuresystems.curios.wallet.shop_source_rs_off";
        }
        return AdventureText.translatable(key);
    }

    private Component editorModeButtonText() {
        if (!canEdit) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_mode_locked");
        return AdventureText.translatable(editorMode
                ? "gui.adventuresystems.curios.wallet.shop_editor_mode_on"
                : "gui.adventuresystems.curios.wallet.shop_editor_mode_off");
    }

    private void adjustAmount(int delta) {
        setAmount(Math.max(1, Math.min(64, amount + delta)));
    }

    private void setAmount(int value) {
        amount = Math.max(1, Math.min(64, value));
        if (amountBox != null) amountBox.setIntValue(amount);
        if (amountSlider != null) amountSlider.setSliderValue(amount);
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

    private void updateSmoothScrolling() {
        int maxScroll = maxScroll();
        productScrollController.updateRange(maxScroll, contentHeight(), contentHeightVisible());
        int productControllerOffset = productScrollController.offset();
        if (productControllerOffset != syncedProductScrollOffset) {
            scroll = productControllerOffset;
            smoothScroll = productControllerOffset;
        }
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        float scrollDifference = scroll - smoothScroll;
        if (Math.abs(scrollDifference) < 0.1F) smoothScroll = scroll;
        else smoothScroll += scrollDifference * 0.28F;
        productScrollController.setOffset(Math.round(smoothScroll));
        syncedProductScrollOffset = productScrollController.offset();

        int maxPageScroll = maxPageScroll();
        pageScrollController.updateRange(maxPageScroll, categoryContentWidth(), categoryViewportWidth());
        int controllerOffset = pageScrollController.offset();
        if (controllerOffset != syncedPageScrollOffset) {
            pageScroll = controllerOffset;
            smoothPageScroll = controllerOffset;
        }
        pageScroll = Math.max(0, Math.min(pageScroll, maxPageScroll));
        if (!draggingPageScrollbar) {
            float pageDifference = pageScroll - smoothPageScroll;
            if (Math.abs(pageDifference) < 0.05F) smoothPageScroll = pageScroll;
            else smoothPageScroll += pageDifference * 0.22F;
        }
        smoothPageScroll = Math.max(0.0F, Math.min(smoothPageScroll, maxPageScroll));
        pageScrollController.setOffset(Math.round(smoothPageScroll));
        syncedPageScrollOffset = pageScrollController.offset();
        refreshPageButtons();
    }

    private void resetScrollImmediately() {
        scroll = 0;
        smoothScroll = 0.0F;
    }

    private int renderedScroll() {
        return Math.max(0, Math.min(maxScroll(), Math.round(smoothScroll)));
    }

    private void updateScrollFromMouse(int mouseY) {
        Scrollbar scrollbar = scrollbar();
        if (!scrollbar.visible()) return;
        int travel = scrollbar.trackHeight() - scrollbar.thumbHeight();
        int local = mouseY - scrollbar.trackTop() - scrollbarGrabOffset;
        local = Math.max(0, Math.min(travel, local));
        scroll = local * maxScroll() / Math.max(1, travel);
        smoothScroll = scroll;
    }

    private void updatePageScrollFromMouse(int mouseX) {
        int width = pageScrollBarWidth();
        int max = maxPageScroll();
        if (width <= 0 || max <= 0) {
            pageScroll = 0;
            smoothPageScroll = 0.0F;
            return;
        }
        int thumbWidth = pageScrollThumbWidth(width);
        int travel = Math.max(1, width - thumbWidth);
        int local = Math.max(0, Math.min(travel, mouseX - listLeft() - pageScrollbarGrabOffset));
        smoothPageScroll = Math.max(0.0F, Math.min(max, local * max / (float) travel));
        pageScroll = Math.max(0, Math.min(max, Math.round(smoothPageScroll)));
        clampPageScroll();
        refreshPageButtons();
    }

    private void refreshPageButtons() {
        if (favoritesPageButton == null || allPageButton == null || pagePrevButton == null || pageNextButton == null) return;
        int y = pageTabsY();
        int rightArrowX = pageRightArrowX();
        visiblePageButtonPages.clear();

        int favoritesWidth = pageButtonWidthForPage(FAVORITES_PAGE);
        int allWidth = pageButtonWidthForPage(ALL_PAGE);
        favoritesPageButton.moveControlX(listLeft());
        favoritesPageButton.moveControlY(y);
        favoritesPageButton.resizeControlWidth(favoritesWidth);
        favoritesPageButton.setText(pageButtonLabel(FAVORITES_PAGE));
        favoritesPageButton.setSelected(Objects.equals(activePageName, FAVORITES_PAGE));
        allPageButton.moveControlX(listLeft() + favoritesWidth + PAGE_TAB_GAP);
        allPageButton.moveControlY(y);
        allPageButton.resizeControlWidth(allWidth);
        allPageButton.setText(pageButtonLabel(ALL_PAGE));
        allPageButton.setSelected(Objects.equals(activePageName, ALL_PAGE));

        int slot = 0;
        int leftArrowX = fixedPageTabsEndX();
        pagePrevButton.moveControlX(leftArrowX);
        pagePrevButton.moveControlY(y);
        pagePrevButton.resizeControlWidth(PAGE_TAB_ARROW_WIDTH);
        setControlEnabled(pagePrevButton, pageScroll > 0);
        setControlVisible(pagePrevButton, true);

        pageNextButton.moveControlX(rightArrowX);
        pageNextButton.moveControlY(y);
        pageNextButton.resizeControlWidth(PAGE_TAB_ARROW_WIDTH);
        setControlVisible(pageNextButton, true);
        setControlEnabled(pageNextButton, pageScroll < maxPageScroll());

        int viewportLeft = categoryViewportLeft();
        int viewportRight = categoryViewportRight();
        float offset = smoothPageScroll;
        int contentX = 0;
        for (String page : pages) {
            int width = pageButtonWidthForPage(page);
            float buttonLeft = viewportLeft + contentX - offset;
            float buttonRight = buttonLeft + width;
            if (buttonRight > viewportLeft && buttonLeft < viewportRight && slot < pageButtons.size()) {
                bindPageButton(slot++, page, Math.round(buttonLeft), y, viewportLeft, viewportRight);
            }
            contentX += width + PAGE_TAB_GAP;
        }
        for (int i = slot; i < pageButtons.size(); i++) {
            KineticButton button = pageButtons.get(i);
            button.setText(Component.empty());
            setControlVisible(button, false);
            setControlEnabled(button, false);
            button.setSelected(false);
        }
    }

    private void bindPageButton(int slot, String page, int x, int y, int viewportLeft, int viewportRight) {
        if (slot < 0 || slot >= pageButtons.size()) return;
        KineticButton button = pageButtons.get(slot);
        int clippedLeft = Math.max(x, viewportLeft);
        int clippedRight = Math.min(x + pageButtonWidthForPage(page), viewportRight);
        button.moveControlX(clippedLeft);
        button.moveControlY(y);
        int clippedWidth = clippedRight - clippedLeft;
        button.resizeControlWidth(Math.max(8, clippedWidth));
        button.setText(pageButtonLabel(page));
        setControlVisible(button, clippedWidth >= 8);
        setControlEnabled(button, clippedWidth >= 8);
        button.setSelected(Objects.equals(page, activePageName));
        visiblePageButtonPages.add(page);
    }

    private Component pageButtonLabel(String page) {
        Component label = pageLabel(page);
        if (Objects.equals(page, activePageName)) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_page_active_prefix", label);
        return label;
    }

    private int pageButtonWidth(Component label) {
        return PAGE_TAB_WIDTH;
    }

    private int pageButtonWidthForPage(String page) {
        Component normal = pageLabel(page);
        Component active = AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_page_active_prefix", normal);
        return Math.max(pageButtonWidth(normal), pageButtonWidth(active));
    }

    private void selectVisiblePage(int slot) {
        if (slot < 0 || slot >= visiblePageButtonPages.size()) return;
        selectPage(visiblePageButtonPages.get(slot));
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
        resetScrollImmediately();
        closeShopContextMenu();
        clearDragState();
        rebuildRows();
        updateDetailWidgetState();
        clearTextFocus();
    }

    private String pageAt(int mouseX, int mouseY) {
        if (isControlVisible(favoritesPageButton) && favoritesPageButton.contains(mouseX, mouseY)) return FAVORITES_PAGE;
        if (isControlVisible(allPageButton) && allPageButton.contains(mouseX, mouseY)) return ALL_PAGE;
        for (int i = 0; i < visiblePageButtonPages.size() && i < pageButtons.size(); i++) {
            KineticButton button = pageButtons.get(i);
            if (isControlVisible(button) && button.contains(mouseX, mouseY)) return visiblePageButtonPages.get(i);
        }
        return null;
    }

    private Long detailQuestClickAt(int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        if (!hasQuestList(entry)) return null;
        if (isInsideQuestPickerScrollbar(mouseX, mouseY)) return null;
        if (isInsideQuestButton(mouseX, mouseY) && !hasMultipleQuests(entry)) return entry.requiredQuestId();
        if (!isQuestPickerOpen() || !isInsideQuestPicker(mouseX, mouseY)) return null;
        int listY = questPickerY() + 18;
        if (mouseY < listY) return null;
        int row = (mouseY - listY) / QUEST_PICKER_ROW_HEIGHT;
        double visualScroll = questPickerVisualScroll(entry);
        int index = (int) Math.floor((mouseY - listY) / (double) QUEST_PICKER_ROW_HEIGHT + visualScroll);
        if (row < QUEST_PICKER_VISIBLE_ROWS && index < entry.requiredQuestIds().size()) return entry.requiredQuestIds().get(index);
        return null;
    }

    private boolean hasQuestList(Shop.Entry entry) {
        return entry != null && entry.requiredQuestIds() != null && !entry.requiredQuestIds().isEmpty();
    }

    private boolean hasMultipleQuests(Shop.Entry entry) {
        return hasQuestList(entry) && entry.requiredQuestIds().size() > 1;
    }

    private int questButtonX() {
        return detailContentX();
    }

    private int questButtonY() {
        return detailTop() + 45;
    }

    private int questButtonWidth() {
        return Math.min(DETAIL_QUEST_BUTTON_WIDTH, detailContentWidth());
    }

    private boolean isInsideQuestButton(int mouseX, int mouseY) {
        return hasQuestList(selectedEntry()) && isHover(mouseX, mouseY, questButtonX(), questButtonY(), questButtonWidth(), QUEST_PICKER_ROW_HEIGHT);
    }

    private void handleQuestButtonClick() {
        Shop.Entry entry = selectedEntry();
        if (!hasQuestList(entry)) return;
        if (hasMultipleQuests(entry)) {
            if (isQuestPickerOpen()) {
                overlayLayers.close(OverlayLayer.QUEST_PICKER);
            } else {
                overlayLayers.open(OverlayLayer.QUEST_PICKER);
            }
            questPickerVisualScroll(entry);
                return;
        }
        openKtQuest(entry.requiredQuestId());
    }

    private Component questButtonText(Shop.Entry entry) {
        if (!hasQuestList(entry)) return Component.empty();
        boolean done = !entry.locked();
        Component status = done
                ? AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_task_requirement_done")
                : AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_task_requirement_need", missingRequiredQuestCount(entry));
        Component title = hasMultipleQuests(entry) ? AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_task_multiple", entry.requiredQuestIds().size()) : AdventureText.literal(questTitleById(entry, entry.requiredQuestId()));
        return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_task_button", status, title);
    }

    private int missingRequiredQuestCount(Shop.Entry entry) {
        if (entry == null) return 0;
        int need = Math.max(0, entry.requiredQuestNeed());
        int completed = Math.max(0, entry.requiredQuestCompleted());
        return Math.max(1, need - completed);
    }

    private int questPickerX() {
        return detailContentX();
    }

    private int questPickerY() {
        return questButtonY() + QUEST_PICKER_ROW_HEIGHT + 3;
    }

    private int questPickerWidth() {
        return questButtonWidth();
    }

    private int questPickerHeight(Shop.Entry entry) {
        int count = entry == null || entry.requiredQuestIds() == null ? 0 : entry.requiredQuestIds().size();
        int rows = Math.min(QUEST_PICKER_VISIBLE_ROWS, count);
        return 18 + rows * QUEST_PICKER_ROW_HEIGHT + 6;
    }

    private int questPickerMaxScroll(Shop.Entry entry) {
        if (!hasQuestList(entry)) return 0;
        return Math.max(0, entry.requiredQuestIds().size() - QUEST_PICKER_VISIBLE_ROWS);
    }

    private boolean isInsideQuestPicker(int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        if (!isQuestPickerOpen() || !hasQuestList(entry)) return false;
        return isHover(mouseX, mouseY, questPickerX(), questPickerY(), questPickerWidth(), questPickerHeight(entry));
    }

    private void renderQuestPicker(KineticGraphics graphics, Shop.Entry entry, int mouseX, int mouseY) {
        int x = questPickerX();
        int y = questPickerY();
        int width = questPickerWidth();
        int height = questPickerHeight(entry);
        graphics.push();
        graphics.raise(3);
        // The list covers the detail text under it, so it gets an opaque backing under the themed panel.
        graphics.fill(x, y, x + width, y + height, 0xFF161616);
        KineticTheme.panelAlt(graphics, x, y, width, height);
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_task_list_title"), x + 5, y + 5, width - 10, KineticTheme.current().text(), true);
        int questMax = questPickerMaxScroll(entry);
        double visualQuestScroll = questPickerVisualScroll(entry);
        int questStart = Math.max(0, Math.min((int) Math.floor(visualQuestScroll), questMax));
        int questShift = (int) Math.round((visualQuestScroll - questStart) * QUEST_PICKER_ROW_HEIGHT);
        int listY = y + 18;
        boolean hasScrollbar = questPickerScrollbarVisible(entry);
        int rowRight = x + width - (hasScrollbar ? LIST_SCROLLBAR_WIDTH + 7 : 4);
        enableShopScissor(graphics, x + 2, listY, rowRight, y + height);
        try {
            for (int row = 0; row < QUEST_PICKER_VISIBLE_ROWS + 2; row++) {
                int index = questStart + row;
                if (index >= entry.requiredQuestIds().size()) break;
                long questId = entry.requiredQuestIds().get(index);
                boolean completed = entry.isQuestCompleted(questId);
                int rowY = listY + row * QUEST_PICKER_ROW_HEIGHT - questShift;
                if (rowY + QUEST_PICKER_ROW_HEIGHT <= listY || rowY >= y + height) continue;
                boolean hover = isHover(mouseX, mouseY, x + 2, rowY, rowRight - x - 2, QUEST_PICKER_ROW_HEIGHT);
                KineticTheme.stateSurface(
                        graphics,
                        x + 2, rowY, rowRight - x - 2, QUEST_PICKER_ROW_HEIGHT - 1,
                        KineticTheme.Surface.PANEL_ALT, completed, hover, false
                );
                Component rowTitle = AdventureText.translatable(
                        completed
                                ? "gui.adventuresystems.curios.wallet.shop_task_title_done"
                                : hover
                                ? "gui.adventuresystems.curios.wallet.shop_task_title_hover"
                                : "gui.adventuresystems.curios.wallet.shop_task_title_pending",
                        questTitleById(entry, questId)
                );
                int titleX = x + 6;
                int titleY = rowY + 6;
                int titleWidth = Math.max(12, rowRight - 6 - titleX);
                if (titleWidth > 12) drawScrollingQuestTitle(graphics, rowTitle, titleX, titleY, titleWidth);
            }
        } finally {
            disableShopScissor(graphics);
        }
        renderQuestPickerScrollbar(graphics, entry, mouseX, mouseY);
        graphics.pop();
    }

    private void drawScrollingQuestTitle(KineticGraphics graphics, Component text, int x, int y, int width) {
        if (text == null || text.getString().isBlank() || width <= 0) return;
        graphics.scrollingText(text, x, y, width, KineticTheme.current().text(), true);
    }

    private boolean handleQuestPickerClick(int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        if (!isQuestPickerOpen() || !hasQuestList(entry)) return false;
        if (!isInsideQuestPicker(mouseX, mouseY)) return false;
        if (isInsideQuestPickerScrollbar(mouseX, mouseY)) return true;
        int listY = questPickerY() + 18;
        if (mouseY < listY) return true;
        int row = (mouseY - listY) / QUEST_PICKER_ROW_HEIGHT;
        double visualScroll = questPickerVisualScroll(entry);
        int index = (int) Math.floor((mouseY - listY) / (double) QUEST_PICKER_ROW_HEIGHT + visualScroll);
        if (row < QUEST_PICKER_VISIBLE_ROWS && index < entry.requiredQuestIds().size()) {
            openKtQuest(entry.requiredQuestIds().get(index));
            return true;
        }
        return true;
    }

    private boolean questPickerScrollbarVisible(Shop.Entry entry) {
        return questPickerMaxScroll(entry) > 0;
    }

    private Scrollbar questPickerScrollbar(Shop.Entry entry) {
        if (!questPickerScrollbarVisible(entry)) return new Scrollbar(false, 0, 0, 0, 0, 0);
        int barX = questPickerX() + questPickerWidth() - LIST_SCROLLBAR_WIDTH - 2;
        int barTop = questPickerY() + 18;
        int barBottom = questPickerY() + questPickerHeight(entry) - 4;
        int trackHeight = Math.max(20, barBottom - barTop);
        int content = Math.max(1, entry.requiredQuestIds().size() * QUEST_PICKER_ROW_HEIGHT);
        int visible = Math.max(1, QUEST_PICKER_VISIBLE_ROWS * QUEST_PICKER_ROW_HEIGHT);
        int thumbHeight = Math.max(16, trackHeight * visible / Math.max(visible, content));
        int maxScroll = questPickerMaxScroll(entry);
        double visualScroll = questPickerVisualScroll(entry);
        int thumbTop = barTop + (int) Math.round(visualScroll * Math.max(0, trackHeight - thumbHeight) / Math.max(1, maxScroll));
        return new Scrollbar(true, barX, barTop, barBottom, thumbTop, thumbTop + thumbHeight);
    }

    private boolean isInsideQuestPickerScrollbar(int mouseX, int mouseY) {
        Scrollbar scrollbar = questPickerScrollbar(selectedEntry());
        return scrollbar.visible() && mouseX >= scrollbar.x() - 2 && mouseX <= scrollbar.x() + LIST_SCROLLBAR_WIDTH + 2 && mouseY >= scrollbar.trackTop() && mouseY <= scrollbar.trackBottom();
    }

    private void renderQuestPickerScrollbar(KineticGraphics graphics, Shop.Entry entry, int mouseX, int mouseY) {
        Scrollbar scrollbar = questPickerScrollbar(entry);
        if (!scrollbar.visible()) return;
        questPickerVisualScroll(entry);
        questPickerScroller.render(graphics, mouseX, mouseY, scrollbar.x(), scrollbar.trackTop(),
                LIST_SCROLLBAR_WIDTH, scrollbar.trackHeight(), 16);
    }

    private double questPickerVisualScroll(Shop.Entry entry) {
        questPickerScroller.updateRange(questPickerMaxScroll(entry),
                hasQuestList(entry) ? entry.requiredQuestIds().size() : 0, QUEST_PICKER_VISIBLE_ROWS);
        return questPickerScroller.smoothOffset();
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

    private void toggleRewardPreviewSection() {
        Shop.Entry entry = selectedEntry();
        if (entry == null || !entry.gacha()) return;
        rewardPreviewExpanded = !rewardPreviewExpanded;
        rewardPreviewScroller.reset();
        updateDetailWidgetState();
    }

    private void clearTextFocus() {
        clearFocus();
    }

    private List<CurrencyType> sortedCurrenciesByValueDesc() {
        List<CurrencyType> currencies = new ArrayList<>(Data.currencies());
        currencies.sort(Comparator.comparingLong(CurrencyType::value));
        return currencies;
    }

    private int balanceRows() {
        int size = Math.min(Data.currencies().size(), CURRENCY_COLUMNS * 2);
        return Math.max(1, (size + CURRENCY_COLUMNS - 1) / CURRENCY_COLUMNS);
    }

    private int balanceY() {
        return top + 30;
    }

    private int buyButtonX() {
        return backButtonX() + BACK_BUTTON_WIDTH + 2;
    }

    private int sellButtonX() {
        return buyButtonX() + TOP_BUTTON_WIDTH + 4;
    }

    private int editorModeButtonX() {
        return sellButtonX() + TOP_BUTTON_WIDTH + 8;
    }

    // Menu buttons and page tabs have fixed widths in every language; their labels scroll when longer.
    private static final int MENU_BUTTON_WIDTH = 64;
    private static final int PAGE_TAB_WIDTH = 80;

    private int editorMenuButtonWidth() {
        return Math.min(MENU_BUTTON_WIDTH, panelWidth / 6);
    }

    private int sourceMenuButtonWidth() {
        return Math.min(MENU_BUTTON_WIDTH, panelWidth / 6);
    }

    // 开关项直接用“名称:开/关”和绿/红文字表示状态，不用橘黄选中边框（那是“选中”的意思，容易误读）。
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
                ? KineticOverlays.MenuItem.action(newEntry,
                        AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_new_entry"), this::openNewEditor)
                : KineticOverlays.MenuItem.disabled(newEntry,
                        AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_menu_editor_need_mode")));
        openContextMenu(editorModeButton.controlX(), editorModeButton.controlY() + BUTTON_HEIGHT + 2, items);
    }

    private void openSourceMenu() {
        if (backpackSourceButton == null) return;
        List<KineticOverlays.MenuItem> items = new ArrayList<>();
        items.add(KineticOverlays.MenuItem.action(sourceButtonText(true),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_source_backpack_tip"),
                Network::sendToggleShopBackpack));
        items.add(KineticOverlays.MenuItem.action(sourceButtonText(false),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_source_rs_tip"),
                Network::sendToggleShopRs));
        openContextMenu(backpackSourceButton.controlX(), backpackSourceButton.controlY() + BUTTON_HEIGHT + 2, items);
    }

    private int closeButtonX() {
        return left + panelWidth - 8 - CLOSE_BUTTON_WIDTH;
    }

    private int backButtonX() {
        return left + 8;
    }

    private int backpackButtonX() {
        return closeButtonX() - 4 - BACK_BUTTON_WIDTH - 8 - sourceMenuButtonWidth();
    }

    private int shopTitleX() {
        return left + panelWidth / 2;
    }

    private int balanceHeight() {
        return 18 + balanceRows() * CURRENCY_ROW_HEIGHT;
    }

    private int pageTabsY() {
        return balanceY() + balanceHeight() + 4;
    }

    private int pageScrollBarY() {
        return pageTabsY() + PAGE_TAB_HEIGHT + 5;
    }

    private int contentTop() {
        return pageScrollBarY() + PAGE_SCROLLBAR_HEIGHT + 14;
    }


    private int searchBoxX() {
        return detailX() + 4;
    }

    private int searchBoxY() {
        return pageTabsY() + 1;
    }

    private int pageRightArrowX() {
        int min = categoryViewportLeft() + 36 + PAGE_TAB_GAP;
        int right = listLeft() + listWidth() - 2;
        return Math.max(min, right - PAGE_TAB_ARROW_WIDTH);
    }

    private int contentHeightVisible() {
        return Math.max(60, top + panelHeight - 6 - contentTop());
    }

    private int listLeft() {
        return left + 5;
    }

    private int listWidth() {
        int maxGridWidth = GRID_MAX_COLUMNS * GRID_CELL_WIDTH + Math.max(0, GRID_MAX_COLUMNS - 1) * GRID_COLUMN_GAP + 2;
        int available = left + panelWidth - DETAIL_RIGHT_MARGIN - DETAIL_WIDTH - DETAIL_GAP - listLeft();
        int minimum = GRID_CELL_WIDTH + 2;
        return Math.min(maxGridWidth, Math.max(minimum, available));
    }

    private int detailX() {
        return listLeft() + listWidth() + DETAIL_GAP;
    }

    private int pageScrollBarWidth() {
        return pageRightArrowX() + PAGE_TAB_ARROW_WIDTH - listLeft();
    }

    private int fixedPageTabsEndX() {
        return listLeft()
                + pageButtonWidthForPage(FAVORITES_PAGE)
                + PAGE_TAB_GAP
                + pageButtonWidthForPage(ALL_PAGE)
                + PAGE_TAB_GAP;
    }

    private int categoryViewportLeft() {
        return fixedPageTabsEndX() + PAGE_TAB_ARROW_WIDTH + PAGE_TAB_GAP + PAGE_TAB_EDGE_PADDING;
    }

    private int categoryViewportRight() {
        return pageRightArrowX() - PAGE_TAB_GAP - PAGE_TAB_EDGE_PADDING;
    }

    private int categoryViewportWidth() {
        return Math.max(1, categoryViewportRight() - categoryViewportLeft());
    }

    private int categoryContentWidth() {
        int width = 0;
        for (String page : pages) {
            width += pageButtonWidthForPage(page) + PAGE_TAB_GAP;
        }
        return Math.max(0, width - (pages.isEmpty() ? 0 : PAGE_TAB_GAP));
    }

    private int maxPageScroll() {
        return Math.max(0, categoryContentWidth() - categoryViewportWidth());
    }

    private int pageScrollStep() {
        return Math.max(24, categoryViewportWidth() / 3);
    }

    private int pageScrollThumbWidth(int width) {
        int contentWidth = categoryContentWidth();
        int viewportWidth = categoryViewportWidth();
        if (contentWidth <= 0 || contentWidth <= viewportWidth) return width;
        return Math.max(24, Math.round(width * viewportWidth / (float) contentWidth));
    }

    private int pageScrollThumbX(int x, int width, int thumbWidth) {
        int max = maxPageScroll();
        if (max <= 0) return x;
        int travel = Math.max(0, width - thumbWidth);
        return x + Math.round(travel * (smoothPageScroll / max));
    }

    private boolean isInsidePageScrollBar(double mouseX, double mouseY) {
        int x = listLeft();
        int y = pageScrollBarY();
        int width = pageScrollBarWidth();
        return width > 0 && mouseX >= x && mouseX <= x + width && mouseY >= y - 2 && mouseY <= y + PAGE_SCROLLBAR_HEIGHT + 4;
    }

    private int gridColumns() {
        int usableWidth = Math.max(1, listWidth() - 2);
        return Math.max(1, Math.min(GRID_MAX_COLUMNS, (usableWidth + GRID_COLUMN_GAP) / (GRID_CELL_WIDTH + GRID_COLUMN_GAP)));
    }

    private Cell cellForIndex(int index) {
        int columns = gridColumns();
        int row = index / columns;
        int column = index % columns;
        int x = listLeft() + 1 + column * (GRID_CELL_WIDTH + GRID_COLUMN_GAP);
        int y = contentTop() + GRID_TOP_PADDING + row * (GRID_CELL_HEIGHT + GRID_ROW_GAP) - renderedScroll();
        return new Cell(x, y);
    }

    private int visibleIndexStart() {
        if (rows.isEmpty()) return 0;
        int columns = gridColumns();
        int currentScroll = renderedScroll();
        int row = Math.max(0, Math.floorDiv(currentScroll - GRID_CELL_HEIGHT - GRID_TOP_PADDING, GRID_CELL_HEIGHT + GRID_ROW_GAP));
        return Math.min(rows.size(), row * columns);
    }

    private int visibleIndexEnd() {
        if (rows.isEmpty()) return 0;
        int columns = gridColumns();
        int rowCount = (rows.size() + columns - 1) / columns;
        int row = Math.min(rowCount - 1, Math.floorDiv(renderedScroll() + contentHeightVisible(), GRID_CELL_HEIGHT + GRID_ROW_GAP) + 1);
        return Math.min(rows.size(), (row + 1) * columns);
    }

    private int detailAmountInputX() {
        // The label has the same room in every language and scrolls when longer.
        int desired = detailContentX() + TRADE_AMOUNT_LABEL_WIDTH + 5;
        // Leave room for the cost text and its item icon after the amount field.
        int limit = left + panelWidth - 6 - DETAIL_PRICE_SLOT_SIZE - 7 - 40 - 3 - 4 - DETAIL_AMOUNT_INPUT_SIZE;
        return Math.max(detailContentX(), Math.min(limit, desired));
    }

    private int detailTradeCostTextX() {
        return detailAmountInputX() + DETAIL_AMOUNT_INPUT_SIZE + 4;
    }

    private int detailTradeCostIconX(Shop.Entry entry) {
        // A fixed spot at the right edge; the cost text scrolls before it.
        return left + panelWidth - DETAIL_PRICE_SLOT_SIZE - 7;
    }

    private int detailTradeCostIconY() {
        return detailAmountInputY();
    }

    private int detailBottom() {
        return detailTop() + detailVisibleHeight() - 4;
    }

    private int detailBuyPaymentY(Shop.Entry entry) {
        return detailTop() + (hasQuestList(entry) ? 70 : 47);
    }

    private int detailStatusY(Shop.Entry entry) {
        int baseY = detailTop() + (hasQuestList(entry) ? 70 : 47);
        return mode == Shop.Mode.BUY ? baseY + 31 : baseY;
    }

    private int rewardPreviewBottomY() {
        Shop.Entry entry =
                selectedEntry();

        if (entry == null) {
            return detailBottom();
        }

        int maximumBottom =
                detailTradeControlsVisible(entry)
                        ? detailAmountInputY() - 4
                        : detailTradeButtonY() - 4;

        int previewY =
                rewardPreviewY();

        if (!rewardPreviewExpanded
                || previewY < 0) {
            return maximumBottom;
        }

        int rows =
                Math.min(
                        REWARD_PREVIEW_MAX_VISIBLE_ROWS,
                        rewardPreviewTotalRows(entry)
                );

        if (rows <= 0) {
            return Math.min(
                    maximumBottom,
                    rewardPreviewFrameY(previewY) + 2
            );
        }

        int naturalBottom =
                rewardPreviewListY(previewY)
                        + rows
                        * REWARD_PREVIEW_ROW_HEIGHT
                        + REWARD_PREVIEW_FRAME_PADDING
                        + 1;

        return Math.min(
                maximumBottom,
                naturalBottom
        );
    }

    private boolean detailTradeControlsVisible(
            Shop.Entry entry
    ) {
        if (entry == null
                || mode == Shop.Mode.BUY
                && entry.selectable()) {
            return false;
        }

        return entry.totalLimit() <= 0
                || totalRemaining(entry) > 1;
    }

    private int detailAmountInputY() {
        return detailAmountSliderY() - 24;
    }

    private int detailAmountSliderX() {
        return detailContentX();
    }

    private int detailAmountSliderY() {
        // The slider ends 2 px above the quick amount buttons.
        return detailTradeButtonY() - 33;
    }

    private int detailAmountQuickButtonWidth() {
        return Math.max(34, (detailContentWidth() - 12) / 4);
    }

    private int detailAmountQuickButtonX(int slot) {
        return detailContentX() + Math.max(0, Math.min(3, slot)) * (detailAmountQuickButtonWidth() + 4);
    }

    private int detailAmountQuickButtonY() {
        return detailTradeButtonY() - 20;
    }

    private int detailTradeButtonX() {
        return detailContentX();
    }

    private int detailTradeButtonY() {
        // The trade button keeps 2 px above the detail panel's bottom line.
        return detailBottom() - BUTTON_HEIGHT + 5;
    }

    private int detailTradeButtonWidth() {
        return Math.min(DETAIL_ACTION_BUTTON_WIDTH, detailContentWidth());
    }

    private int detailContentWidth() {
        return Math.max(40, left + panelWidth - 6 - detailContentX());
    }

    private ItemStack hoveredItemStackAt(int mouseX, int mouseY) {
        ItemStack balance = hoveredCurrencyIcon(mouseX, mouseY);
        if (!balance.isEmpty()) return balance;
        ItemStack picker = hoveredRewardPickerItem(mouseX, mouseY);
        if (!picker.isEmpty()) return picker;
        Shop.Entry selected = selectedEntry();
        if (selected != null) {
            int y = detailTop();
            if (isHover(mouseX, mouseY, detailContentX(), y + 14, 18, 18)) return detailDisplayStack(selected);
            ItemStack priceCurrency = stack(selected.currencyId());
            if (!priceCurrency.isEmpty()
                    && isHover(mouseX, mouseY, detailPriceIconX(selected) - 1, detailPriceIconY() - 1, DETAIL_PRICE_SLOT_SIZE + 2, DETAIL_PRICE_SLOT_SIZE + 2)) {
                return priceCurrency;
            }
            if (detailTradeControlsVisible(selected)
                    && isHover(mouseX, mouseY, detailTradeCostIconX(selected) - 1, detailTradeCostIconY() - 1, DETAIL_PRICE_SLOT_SIZE + 2, DETAIL_PRICE_SLOT_SIZE + 2)) {
                return tradeCostStack(selected);
            }
            if (selected.gacha()) {
                ItemStack preview = hoveredRewardPreviewItem(mouseX, mouseY, selected);
                if (!preview.isEmpty()) return preview;
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack hoveredRewardPreviewItem(int mouseX, int mouseY, Shop.Entry entry) {
        int previewY = rewardPreviewY();
        if (!rewardPreviewExpanded || entry == null || !entry.gacha() || previewY < 0) return ItemStack.EMPTY;
        int listY = rewardPreviewListY(previewY);
        int listHeight = rewardPreviewListHeight(previewY);
        if (listHeight <= 0 || mouseY < listY || mouseY >= listY + listHeight) return ItemStack.EMPTY;
        List<Shop.Reward> rewards = sortedRewardPreviewRewards(entry);
        if (rewards.isEmpty()) return ItemStack.EMPTY;
        Scrollbar scrollbar = rewardPreviewScrollbar(entry, previewY);
        int scrollbarSpace = scrollbar.visible() ? REWARD_PREVIEW_SCROLLBAR_WIDTH + 4 : 0;
        int contentWidth = Math.max(40, detailContentWidth() - 2 - REWARD_PREVIEW_FRAME_PADDING * 2 - scrollbarSpace);
        int cellWidth = Math.max(40, (contentWidth - REWARD_PREVIEW_CELL_GAP) / REWARD_PREVIEW_COLUMNS);
        int visibleRows = rewardPreviewVisibleRows(previewY);
        int displaySlotCount = rewardPreviewDisplaySlotCount(entry);
        int maxScroll = rewardPreviewMaxScroll(entry, previewY);
        double visualScroll = rewardPreviewVisualScroll(entry, previewY);
        int startRow = Math.max(0, Math.min((int) Math.floor(visualScroll + 1.0E-6D), maxScroll));
        int shift = (int) Math.round((visualScroll - startRow) * REWARD_PREVIEW_ROW_HEIGHT);
        for (int row = 0; row < visibleRows + 2; row++) {
            int rewardRow = startRow + row;
            int rowY = listY + row * REWARD_PREVIEW_ROW_HEIGHT - shift;
            if (rowY + REWARD_PREVIEW_ROW_HEIGHT <= listY || rowY >= listY + listHeight) continue;
            for (int column = 0; column < REWARD_PREVIEW_COLUMNS; column++) {
                int displaySlot = rewardRow * REWARD_PREVIEW_COLUMNS + column;
                if (displaySlot >= displaySlotCount) break;
                int rewardIndex = rewardPreviewRewardIndexAtDisplaySlot(rewards.size(), displaySlot, displaySlotCount);
                if (rewardIndex < 0) continue;
                int cellX = rewardPreviewListX() + column * (cellWidth + REWARD_PREVIEW_CELL_GAP);
                if (isHover(mouseX, mouseY, cellX, rowY, REWARD_PREVIEW_SLOT_SIZE, REWARD_PREVIEW_SLOT_SIZE)) {
                    Shop.Reward reward = rewards.get(rewardIndex);
                    return reward.empty() ? new ItemStack(net.minecraft.world.item.Items.BARRIER) : reward.stack();
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack hoveredCurrencyIcon(int mouseX, int mouseY) {
        int x = left + 14;
        int y = balanceY();
        int width = panelWidth - 28;
        int cellStartX = x + 8 + BALANCE_LABEL_W + 8;
        int usableWidth = x + width - 8 - cellStartX;
        int cellWidth = Math.max(92, usableWidth / CURRENCY_COLUMNS);
        List<CurrencyType> currencies = sortedCurrenciesByValueDesc();
        for (int i = 0; i < Math.min(currencies.size(), CURRENCY_COLUMNS * 2); i++) {
            CurrencyType currency = currencies.get(i);
            int column = i % CURRENCY_COLUMNS;
            int row = i / CURRENCY_COLUMNS;
            int cellX = cellStartX + column * cellWidth;
            int cellY = y + 7 + row * CURRENCY_ROW_HEIGHT - 5;
            if (isHover(mouseX, mouseY, cellX, cellY, 16, 16)) return stack(currency.itemId());
        }
        return ItemStack.EMPTY;
    }


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
        scroll = Math.max(0, Math.min(scroll, maxScroll()));
        smoothScroll = Math.max(0.0F, Math.min(smoothScroll, maxScroll()));
        smoothPageScroll = Math.max(0.0F, Math.min(smoothPageScroll, maxPageScroll()));
        refreshPageButtons();
    }

    private boolean entryExcludedFromActivePage(Shop.Entry entry) {
        if (entry == null) return true;
        if (Objects.equals(activePageName, ALL_PAGE)) return false;
        if (Objects.equals(activePageName, FAVORITES_PAGE)) return !isFavorite(entry);
        return !Objects.equals(pageName(entry), activePageName);
    }

    private void rebuildPages(List<Shop.Entry> entries) {
        pages.clear();
        for (Shop.Entry entry : entries) {
            String page = pageName(entry);
            if (!pages.contains(page)) pages.add(page);
        }
        if (!Objects.equals(activePageName, FAVORITES_PAGE)
                && !Objects.equals(activePageName, ALL_PAGE)
                && !pages.contains(activePageName)) {
            activePageName = ALL_PAGE;
        }
        clampPageScroll();
        ensureActivePageVisible();
    }

    private void ensureActivePageVisible() {
        if (Objects.equals(activePageName, FAVORITES_PAGE) || Objects.equals(activePageName, ALL_PAGE)) return;
        int index = pages.indexOf(activePageName);
        if (index < 0) return;
        int start = categoryPageStart(index);
        int end = start + pageButtonWidthForPage(activePageName);
        int viewport = categoryViewportWidth();
        if (start < pageScroll) {
            pageScroll = start;
        } else if (end > pageScroll + viewport) {
            pageScroll = end - viewport;
        }
        clampPageScroll();
        smoothPageScroll = Math.max(0.0F, Math.min(smoothPageScroll, maxPageScroll()));
    }

    private int categoryPageStart(int index) {
        int start = 0;
        int safeEnd = Math.max(0, Math.min(index, pages.size()));
        for (int i = 0; i < safeEnd; i++) {
            start += pageButtonWidthForPage(pages.get(i)) + PAGE_TAB_GAP;
        }
        return start;
    }

    private void clampPageScroll() {
        int max = maxPageScroll();
        pageScroll = Math.max(0, Math.min(pageScroll, max));
        smoothPageScroll = Math.max(0.0F, Math.min(smoothPageScroll, max));
    }

    private String normalizedSearchQuery() {
        return searchQuery == null ? "" : searchQuery.trim().toLowerCase(Locale.ROOT);
    }

    private boolean failsSearch(Shop.Entry entry, String query) {
        if (query == null || query.isBlank()) return false;
        String text = searchText(entry);
        String[] tokens = query.trim().toLowerCase(Locale.ROOT).split("\\s+");
        for (String token : tokens) {
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
        String currencyNamespace = namespaceOf(entry.currencyId());
        if (itemNamespace.contains(token) || currencyNamespace.contains(token)) return false;
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
        String pageSearch = page + " " + KineticSearch.pinyin(page);
        return !KineticSearch.match(pageSearch, token);
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
        ItemStack currency = stack(entry.currencyId());
        String itemName = ShopGuiSupport.stackName(item);
        String currencyName = ShopGuiSupport.stackName(currency);
        StringBuilder builder = new StringBuilder();
        if (entry.displayName() != null && !entry.displayName().isBlank()) {
            builder.append(entry.displayName()).append(' ');
            builder.append(KineticSearch.pinyin(entry.displayName())).append(' ');
        }
        if (entry.description() != null && !entry.description().isBlank()) {
            builder.append(entry.description()).append(' ');
            builder.append(KineticSearch.pinyin(entry.description())).append(' ');
        }
        ResourceLocation itemId = KineticRegistries.items().id(item.getItem());
        if (itemId != null) {
            builder.append(itemId).append(' ');
            builder.append('@').append(itemId.getNamespace()).append(' ');
        }
        if (entry.currencyId() != null && entry.currencyId().contains(":")) builder.append('@').append(namespaceOf(entry.currencyId())).append(' ');
        builder.append('#').append(pageLabel(entry).getString()).append(' ');
        builder.append(itemName).append(' ');
        builder.append(KineticSearch.pinyin(itemName)).append(' ');
        if (entry.rewards() != null) {
            for (Shop.Reward reward : entry.rewards()) {
                if (reward == null || reward.empty()) continue;
                ResourceLocation rewardId = KineticRegistries.items().id(reward.stack().getItem());
                String rewardName = ShopGuiSupport.stackName(reward.stack());
                if (rewardId != null) {
                    builder.append(rewardId).append(' ');
                    builder.append('@').append(rewardId.getNamespace()).append(' ');
                }
                builder.append(rewardName).append(' ');
                builder.append(KineticSearch.pinyin(rewardName)).append(' ');
            }
        }
        builder.append(entry.currencyId()).append(' ');
        builder.append(currencyName).append(' ');
        builder.append(KineticSearch.pinyin(currencyName)).append(' ');
        builder.append(pageLabel(entry).getString()).append(' ');
        if (entry.requiredQuestTitles() != null) {
            for (String title : entry.requiredQuestTitles()) {
                builder.append(title == null ? "" : title).append(' ');
                builder.append(KineticSearch.pinyin(title)).append(' ');
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

    private RowClick rowClickAt(int mouseX, int mouseY) {
        int listLeft = listLeft();
        int listTop = contentTop();
        int listRight = listLeft + listWidth();
        int listBottom = listTop + contentHeightVisible();
        if (mouseX < listLeft || mouseX > listRight || mouseY < listTop || mouseY > listBottom) return null;
        int start = visibleIndexStart();
        int end = visibleIndexEnd();
        for (int i = start; i < end; i++) {
            Cell cell = cellForIndex(i);
            if (isHover(mouseX, mouseY, cell.x(), cell.y(), GRID_CELL_WIDTH, GRID_CELL_HEIGHT)) return new RowClick(rows.get(i), i);
        }
        return null;
    }

    private Row rowAt(int mouseX, int mouseY) {
        RowClick click = rowClickAt(mouseX, mouseY);
        return click == null ? null : click.row();
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
        int edge = 18;
        if (mouseY < contentTop() + edge) {
            scroll = Math.max(0, scroll - 6);
            smoothScroll = scroll;
        } else if (mouseY > contentTop() + contentHeightVisible() - edge) {
            scroll = Math.min(maxScroll(), scroll + 6);
            smoothScroll = scroll;
        }
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
        if (dragTargetEntry != null) {
            for (int i = visibleIndexStart(); i < visibleIndexEnd(); i++) {
                Shop.Entry entry = rows.get(i).entry();
                if (entry == null || entry.index() != dragTargetEntry.index()) continue;
                Cell target = cellForIndex(i);
                renderSelectionOutline(graphics, target.x() - 1, target.y() - 1);
                break;
            }
        }
        int x = Math.max(listLeft(), Math.min(dragMouseX - GRID_CELL_WIDTH / 2, listLeft() + listWidth() - GRID_CELL_WIDTH));
        int y = Math.max(contentTop(), Math.min(dragMouseY - GRID_CELL_HEIGHT / 2, contentTop() + contentHeightVisible() - GRID_CELL_HEIGHT));
        graphics.push();
        graphics.raise(6);
        renderDraggedCell(graphics, dragSourceEntry, new Cell(x, y));
        graphics.pop();
    }

    private void renderDraggedCell(KineticGraphics graphics, Shop.Entry entry, Cell cell) {
        boolean canTrade = canTrade(entry, 1);
        KineticTheme.stateSurface(
                graphics, cell.x(), cell.y(), GRID_CELL_WIDTH, GRID_CELL_HEIGHT,
                KineticTheme.Surface.PANEL_ALT, canTrade, false, !canTrade
        );
        int itemX = cellItemSlotX(cell);
        int itemY = cellItemSlotY(cell);
        renderItemCheckerSlot(graphics, itemX, itemY, PRODUCT_SLOT_SIZE);
                renderProductItem(graphics, cellDisplayStack(entry), itemX, itemY);

        Component primary = cellPrimaryStatusText(entry);
        Component limit = cellLimitStatusText(entry);
        int minX = itemX + PRODUCT_SLOT_SIZE + 3;
        int available = Math.max(4, cell.x() + GRID_CELL_WIDTH - 3 - minX);
        int textY = cell.y() + 2;
        if (primary != null && limit != null) {
            // Fixed halves, so the limit text starts at the same place in every language; each half scrolls.
            int primaryWidth = Math.max(8, (available - 3) / 2);
            drawCellString(graphics, primary, minX, textY, primaryWidth);
            int limitX = minX + primaryWidth + 3;
            drawCellString(graphics, limit, limitX, textY, Math.max(1, available - primaryWidth - 3));
        } else if (primary != null) {
            drawCellString(graphics, primary, minX, textY, available);
        } else if (limit != null) {
            drawCellString(graphics, limit, minX, textY, available);
        }

        String price = formatCompact(entry.price());
        int numberX = cellNumberX(cell);
        int numberY = cellNumberY(cell);
        int numberW = cellNumberWidth(price);
        KineticTheme.stateSurface(
                graphics, numberX, numberY, numberW, NUMBER_BAR_HEIGHT,
                KineticTheme.Surface.FIELD, false, false, !canTrade
        );
        drawCellString(graphics, cellPriceText(price, canTrade), numberX + 3, numberY + (NUMBER_BAR_HEIGHT - 8) / 2, numberW - 6);
                renderCurrencyItem(graphics, stack(entry.currencyId()), numberX + numberW + 2, numberY - 1);
            }


    private Shop.Entry selectedEntry() {
        if (selectedIndex < 0 || selectedIndex >= rows.size()) return null;
        return rows.get(selectedIndex).entry();
    }

    private boolean canTrade(Shop.Entry entry, int times) {
        if (entry == null) return false;
        if (mode == Shop.Mode.BUY) {
            return previewSuccessTrades(entry, times) > 0;
        }
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

    private static long safeAdd(long a, long b) {
        if (a < 0L || b < 0L) return Long.MAX_VALUE;
        if (Long.MAX_VALUE - a < b) return Long.MAX_VALUE;
        return a + b;
    }

    private static Component currencyName(String id) {
        return ShopGuiSupport.stackNameComponent(stack(id));
    }

    private long amountOf(String id) {
        return Data.readAmount(balances, id);
    }

    private long displayAmount(String id) {
        BalanceAnimation animation = animations.get(id);
        if (animation == null) return amountOf(id);
        return animation.display();
    }

    private static ItemStack stack(String id) {
        ItemStack stack = StackCodec.fromConfigString(id);
        return stack.isEmpty() ? new ItemStack(net.minecraft.world.item.Items.BARRIER) : stack;
    }

    private int contentHeight() {
        int columns = gridColumns();
        int rowCount = rows.isEmpty() ? 0 : (rows.size() + columns - 1) / columns;
        if (rowCount <= 0) return 0;
        return GRID_TOP_PADDING + rowCount * GRID_CELL_HEIGHT + Math.max(0, rowCount - 1) * GRID_ROW_GAP;
    }

    private int maxScroll() {
        return Math.max(0, contentHeight() - contentHeightVisible());
    }

    private Scrollbar scrollbar() {
        int visible = contentHeightVisible();
        int content = contentHeight();
        if (content <= visible) return new Scrollbar(false, 0, 0, 0, 0, 0);
        int barX = detailScrollbarX();
        int barTop = contentTop();
        int trackBottom = contentTop() + visible;
        int trackHeight = Math.max(20, trackBottom - barTop);
        int barHeight = Math.max(20, trackHeight * trackHeight / content);
        int maxScroll = maxScroll();
        int barY = barTop + Math.round(smoothScroll * (trackHeight - barHeight) / Math.max(1.0F, maxScroll));
        return new Scrollbar(true, barX, barTop, trackBottom, barY, barY + barHeight);
    }

    private int detailScrollbarX() {
        return listLeft() + listWidth() + 1;
    }

    private int detailTop() {
        return searchBoxY() + KineticPage.CONTROL_HEIGHT + 3;
    }

    private int detailVisibleHeight() {
        return Math.max(60, top + panelHeight - 6 - detailTop());
    }

    private int detailContentX() {
        return detailX() + DETAIL_CONTENT_LEFT_PAD;
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

    private static boolean looksLikeQuestId(String text) {
        if (text == null) return false;
        String value = text.trim();
        if (value.length() < 8) return false;
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) return false;
        }
        return true;
    }

    private static long safeMultiply(long a, long b) {
        if (a <= 0L || b <= 0L) return 0L;
        if (a > Long.MAX_VALUE / b) return Long.MAX_VALUE;
        return a * b;
    }

    private static boolean isHover(int mouseX, int mouseY, int x, int y, int width, int height) {
        return KineticTheme.hovering(mouseX, mouseY, x, y, width, height);
    }

    private void drawHintText(KineticGraphics graphics, Component text, int x, int y) {
        if (text == null || text.getString().isBlank()) return;
        graphics.scrollingText(text, x, y, Math.max(0, listLeft() + listWidth() - x - 2), KineticTheme.current().text(), true);
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

    private record Row(Shop.Entry entry) {}
    private record RowClick(Row row, int index) {}
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

    private record Cell(int x, int y) {}

    private record Scrollbar(boolean visible, int x, int trackTop, int trackBottom, int thumbTop, int thumbBottom) {
        int trackHeight() { return trackBottom - trackTop; }
        int thumbHeight() { return thumbBottom - thumbTop; }
    }

    private record BalanceAnimation(long from, long to, long delta, long start) {
        long display() {
            long age = System.currentTimeMillis() - start;
            if (age < 1000L) return from;
            long progress = Math.min(350L, age - 1000L);
            return from + (to - from) * progress / 350L;
        }
        boolean deltaVisible() { return System.currentTimeMillis() - start < 1000L; }
    }
    private KineticButton addButton(int x, int y, int width, Component label, Runnable action) {
        return ui().button(x, y, width).text(label).onClick(action).build();
    }

    private KineticButton addCompactButton(int x, int y, int width, Component label, Runnable action) {
        return ui().button(x, y, width).compact().text(label).onClick(action).build();
    }

    private void confirmChoiceOverlay() {
        Shop.Entry entry = selectedEntry();
        if (entry == null) return;
        if (choiceOverlaySelectedIndex >= 0) confirmChoicePurchase(entry);
        else KineticOverlays.toast("currency_wallet_shop_notice",
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_choice_need_select"),
                KineticOverlays.Position.BOTTOM_CENTER, 2500, 0, -30);
    }

    private void enableShopScissor(KineticGraphics graphics, int left, int top, int right, int bottom) {
        graphics.scissor(left, top, right, bottom);
    }

    private void disableShopScissor(KineticGraphics graphics) {
        graphics.endScissor();
    }

    private static boolean isControlVisible(KineticControl control) {
        return control != null && control.controlVisible();
    }

    private static void setControlVisible(KineticControl control, boolean visible) {
        if (control != null) control.setControlVisible(visible);
    }

    private static void setControlEnabled(KineticControl control, boolean enabled) {
        if (control != null) control.setEnabled(enabled);
    }

}
