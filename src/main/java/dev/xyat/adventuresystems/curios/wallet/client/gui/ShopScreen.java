package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.adventuresystems.curios.util.ColorText;
import dev.xyat.adventuresystems.curios.wallet.client.ShopClientPreferences;
import dev.xyat.adventuresystems.curios.wallet.data.CurrencyType;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.adventuresystems.curios.wallet.data.StackCodec;
import dev.xyat.adventuresystems.curios.wallet.network.Network;
import dev.xyat.adventuresystems.curios.wallet.shop.Shop;
import dev.xyat.kineticcore.api.client.search.KineticSearch;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.overlay.GuiOverlay;
import dev.xyat.kineticcore.api.client.screen.KineticScreen;
import dev.xyat.kineticcore.api.client.widget.KineticWidgets.LayerState;
import dev.xyat.kineticcore.api.client.widget.KineticWidgets.Scroll;
import dev.xyat.adventuresystems.ftb.util.BridgeFTB;
import java.text.NumberFormat;
import java.util.*;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class ShopScreen extends KineticScreen {
    private enum OverlayLayer {
        REWARD_PICKER,
        QUEST_PICKER,
        CONTEXT_MENU,
        CHOICE_OVERLAY
    }

    private static final int BUTTON_HEIGHT = 20;
    private static final int TOP_BUTTON_WIDTH = 48;
    private static final int EDIT_MODE_BUTTON_WIDTH = 82;
    private static final int NEW_BUTTON_WIDTH = 62;
    private static final int BACKPACK_BUTTON_WIDTH = 82;
    private static final int RS_BUTTON_WIDTH = 72;
    private static final int BACK_BUTTON_WIDTH = 52;
    private static final int CLOSE_BUTTON_WIDTH = 52;
    private static final int DETAIL_WIDTH = 160;
    private static final int GRID_CELL_WIDTH = 90;
    private static final int GRID_CELL_HEIGHT = 26;
    private static final int GRID_COLUMN_GAP = 1;
    private static final int GRID_ROW_GAP = 3;
    private static final int GRID_TOP_PADDING = 1;
    private static final int PRODUCT_SLOT_SIZE = 20;
    private static final int DETAIL_AMOUNT_SLIDER_WIDTH = 118;
    private static final int CURRENCY_COLUMNS = 4;
    private static final int CURRENCY_ROW_HEIGHT = 20;
    private static final int GRID_MAX_COLUMNS = 5;
    private static final int GRID_VISIBLE_ROWS = 8;
    private static final int MAX_PRODUCT_BUTTON_WIDGETS = GRID_MAX_COLUMNS * (GRID_VISIBLE_ROWS + 2);
    private static final int PAGE_TAB_HEIGHT = 20;
    private static final int PAGE_TAB_GAP = 4;
    private static final int PAGE_TAB_ARROW_WIDTH = 18;
    private static final int PAGE_TAB_EDGE_PADDING = 2;
    private static final int MAX_PAGE_BUTTON_WIDGETS = 16;
    private static final int SEARCH_BOX_WIDTH = 150;
    private static final int PAGE_SCROLLBAR_HEIGHT = 4;
    private static final int LIST_SCROLLBAR_WIDTH = 4;
    private static final int DETAIL_GAP = 6;
    private static final int DETAIL_CONTENT_LEFT_PAD = 8;
    private static final int DETAIL_RIGHT_MARGIN = 2;
    private static final int GOLD = 0xFFFFAA00;
    private static final int GOLD_DARK = 0xFF8A5A00;
    private static final int PANEL_BG = 0xF0101010;
    private static final int PANEL_INNER = 0xF01A1A1A;
    private static final int BOX_BG = 0xEE141414;
    private static final int ROW_BG = 0xEE171717;
    private static final int ROW_HOVER = 0xEE252525;
    private static final int SELECT_BG = 0xEE27321A;
    private static final int GREEN = 0xFF55FF55;
    private static final int GREEN_DARK = 0xFF2F8B2F;
    private static final int RED = 0xFFFF5555;
    private static final int DEEP_RED = 0xFF7A1717;
    private static final int LIMIT_TIME = 0xFFFFAA33;
    private static final int LIMIT_COUNT = 0xFFB7FF55;
    private static final int CYAN = 0xFFBBBBBB;
    private static final int CYAN_DARK = 0xFF444444;
    private static final int TEXT_WHITE = 0xFFFFFFFF;
    private static final int TEXT_GRAY = 0xFFAAAAAA;
    private static final int SCROLLBAR_BORDER = 0xFF666666;
    private static final int SCROLLBAR_TRACK = 0xFF171717;
    private static final int SCROLLBAR_THUMB = 0xFFFF9800;
    private static final int SCROLLBAR_HOVER = 0xFFFFD700;
    private static final int NUMBER_BAR_BG = 0xE0FFFFFF;
    private static final int NUMBER_BAR_BORDER = 0xFF606060;
    private static final int NUMBER_BAR_TEXT = 0xFF101010;
    private static final int PLACEHOLDER_GRAY = 0xFFAAAAAA;
    private static final float CURRENCY_ITEM_SCALE = 0.80F;
    private static final float PRODUCT_ITEM_SCALE = 1.20F;
    private static final int NUMBER_BAR_HEIGHT = 11;
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
    private static final int REWARD_PREVIEW_CHANCE_COLOR = 0xFFFFD85A;
    private static final int CONTEXT_MENU_WIDTH = 128;
    private static final int CONTEXT_MENU_BUTTON_HEIGHT = 18;
    private static final int CONTEXT_MENU_GAP = 1;
    private static final String FAVORITES_PAGE = "\u0001favorites";
    private static final String ALL_PAGE = "\u0001all";
    private static final ShopMemory SHOP_MEMORY = new ShopMemory();

    @Nullable
    private final Screen parent;

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
    private boolean draggingRewardPreviewScrollbar;
    private boolean draggingAmountSlider;
    private boolean draggingPageScrollbar;
    private int scrollbarGrabOffset;
    private int pageScrollbarGrabOffset;
    private int rewardPreviewScrollbarGrabOffset;
    private double rewardPickerScroll;
    private final Scroll.State rewardPickerScrollSmoothing = new Scroll.State();
    private double rewardPreviewScroll;
    private final Scroll.State rewardPreviewScrollSmoothing = new Scroll.State();
    private boolean rewardPreviewExpanded;
    private double questPickerScroll;
    private final Scroll.State questPickerScrollSmoothing = new Scroll.State();
    private boolean draggingQuestPickerScrollbar;
    private int questPickerScrollbarGrabOffset;
    private double choiceOverlayScroll;
    private final Scroll.State choiceOverlayScrollSmoothing = new Scroll.State();
    private boolean draggingChoiceOverlayScrollbar;
    private int choiceOverlayScrollbarGrabOffset;
    private int choiceOverlaySelectedIndex = -1;
    private Button choiceOverlayCancelButton;
    private Button choiceOverlayConfirmButton;
    private Button tradeButton;
    private Button questButton;
    private Button amountMinusButton;
    private Button amountPlusButton;
    private Button amountTenButton;
    private Button amountMaxButton;
    private Button editorModeButton;
    private Button backpackSourceButton;
    private Button rsSourceButton;
    private EditBox amountBox;
    private EditBox searchBox;
    private Button favoritesPageButton;
    private Button allPageButton;
    private Button pagePrevButton;
    private Button pageNextButton;
    private Button rewardPreviewToggleButton;
    private String searchQuery = "";
    private String activePageName = ALL_PAGE;
    private int pageScroll;
    private long shopReceivedAt;
    private final List<String> pages = new ArrayList<>();
    private final List<String> visiblePageButtonPages = new ArrayList<>();
    private final List<PageTabButton> pageButtons = new ArrayList<>();
    private final List<ProductButton> productButtons = new ArrayList<>();
    private final List<Button> contextButtons = new ArrayList<>();
    private final List<Row> rows = new ArrayList<>();
    private final Map<String, BalanceAnimation> animations = new HashMap<>();
    private final Map<String, Integer> selectedRewardIndices = new HashMap<>();
    private final Map<Long, String> clientQuestTitleCache = new HashMap<>();
    private final Map<String, String> searchTextCache = new HashMap<>();
    private long clientQuestTitleCacheAt;
    private Shop.Entry contextEntry;
    private int contextMenuX;
    private int contextMenuY;
    private Shop.Entry dragSourceEntry;
    private Shop.Entry dragTargetEntry;
    private int dragMouseX;
    private int dragMouseY;
    private boolean useBackpackSource;
    private boolean useRsSource;

    private boolean isRewardPickerOpen() {
        return overlayLayers.isOpen(OverlayLayer.REWARD_PICKER);
    }

    private boolean isQuestPickerOpen() {
        return overlayLayers.isOpen(OverlayLayer.QUEST_PICKER);
    }

    private boolean isContextMenuOpen() {
        return overlayLayers.isOpen(OverlayLayer.CONTEXT_MENU);
    }

    private boolean isChoiceOverlayOpen() {
        return overlayLayers.isOpen(OverlayLayer.CHOICE_OVERLAY);
    }

    private void closeAllOverlays() {
        overlayLayers.closeAll();
        draggingQuestPickerScrollbar = false;
        draggingChoiceOverlayScrollbar = false;
        updateChoiceOverlayButtons();
        updateContextMenuButtons();
    }

    public ShopScreen(CompoundTag balances, CompoundTag shopTag, boolean editorMode) {
        this(null, balances, shopTag, editorMode);
    }

    public ShopScreen(@Nullable Screen parent, CompoundTag balances, CompoundTag shopTag, boolean editorMode) {
        super(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_title"));
        this.parent = parent;
        useCanvas(
                640f,
                360f,
                6
        );
        this.balances = balances == null ? new CompoundTag() : balances.copy();
        this.shopTag = shopTag == null ? new CompoundTag() : shopTag.copy();
        this.editorMode = editorMode;
        this.canEdit = this.shopTag.getBoolean("CanEdit");
        this.useBackpackSource = this.shopTag.getBoolean("UseBackpack");
        this.useRsSource = this.shopTag.getBoolean("UseRs");
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
    public void removed() {
        rememberSessionPosition();
        super.removed();
    }

    public void updateShop(CompoundTag balances, CompoundTag shopTag, boolean editorMode) {
        CompoundTag nextShopTag = shopTag == null ? new CompoundTag() : shopTag.copy();
        boolean nextCanEdit = nextShopTag.getBoolean("CanEdit");
        boolean changed = this.editorMode != editorMode || this.canEdit != nextCanEdit;
        Shop.Entry selected = selectedEntry();
        String selectedKey = selected == null ? "" : selected.key();
        updateBalances(balances);
        this.shopTag = nextShopTag;
        this.editorMode = editorMode;
        this.canEdit = nextCanEdit;
        this.useBackpackSource = this.shopTag.getBoolean("UseBackpack");
        this.useRsSource = this.shopTag.getBoolean("UseRs");
        this.shopReceivedAt = System.currentTimeMillis();
        this.searchTextCache.clear();
        closeContextMenu();
        clearDragState();
        if (this.minecraft != null && this.width > 0 && this.height > 0) {
            rebuildRows();
            selectRowByKey(selectedKey);
            updateDetailWidgetState();
        }
        if (changed && this.minecraft != null) this.init(this.minecraft, this.width, this.height);
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
    protected void buildUi() {
        this.left = 4;
        this.top = 4;
        this.panelWidth = Math.max(620, this.canvasWidth - 8);
        this.panelHeight = Math.max(348, this.canvasHeight - 8);
        this.favoritesPageButton = null;
        this.allPageButton = null;
        this.pagePrevButton = null;
        this.pageNextButton = null;
        this.rewardPreviewToggleButton = null;
        this.editorModeButton = null;
        this.pageButtons.clear();
        this.visiblePageButtonPages.clear();
        this.productButtons.clear();
        this.contextButtons.clear();

        addRenderableWidget(Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_buy_tab"), button -> switchMode(Shop.Mode.BUY))
                .bounds(buyButtonX(), top + 6, TOP_BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        addRenderableWidget(Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_sell_tab"), button -> switchMode(Shop.Mode.SELL))
                .bounds(sellButtonX(), top + 6, TOP_BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        editorModeButton = addRenderableWidget(Button.builder(editorModeButtonText(), button -> Network.sendToggleShopEditorMode())
                .bounds(editorModeButtonX(), top + 6, EDIT_MODE_BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        editorModeButton.active = canEdit;
        if (editorMode) {
            addRenderableWidget(Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_new_entry"), button -> openNewEditor())
                    .bounds(newEntryButtonX(), top + 6, NEW_BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());
        }
        backpackSourceButton = addRenderableWidget(Button.builder(sourceButtonText(true), button -> Network.sendToggleShopBackpack())
                .bounds(backpackButtonX(), top + 6, BACKPACK_BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        rsSourceButton = addRenderableWidget(Button.builder(sourceButtonText(false), button -> Network.sendToggleShopRs())
                .bounds(rsButtonX(), top + 6, RS_BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        addRenderableWidget(Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.back"), button -> returnToPreviousScreen())
                .bounds(backButtonX(), top + 6, BACK_BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        addRenderableWidget(Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.close"), button -> onClose())
                .bounds(closeButtonX(), top + 6, CLOSE_BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        favoritesPageButton = addRenderableWidget(Button.builder(pageButtonLabel(FAVORITES_PAGE), button -> selectPage(FAVORITES_PAGE))
                .bounds(listLeft(), pageTabsY(), pageButtonWidthForPage(FAVORITES_PAGE), PAGE_TAB_HEIGHT)
                .build());
        allPageButton = addRenderableWidget(Button.builder(pageButtonLabel(ALL_PAGE), button -> selectPage(ALL_PAGE))
                .bounds(listLeft() + pageButtonWidthForPage(FAVORITES_PAGE) + PAGE_TAB_GAP, pageTabsY(), pageButtonWidthForPage(ALL_PAGE), PAGE_TAB_HEIGHT)
                .build());

        pagePrevButton = Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_page_previous"), button -> {
            pageScroll = Math.max(0, pageScroll - pageScrollStep());
            clampPageScroll();
            refreshPageButtons();
        }).bounds(fixedPageTabsEndX(), pageTabsY(), PAGE_TAB_ARROW_WIDTH, PAGE_TAB_HEIGHT).build();
        addRenderableWidget(pagePrevButton);

        for (int i = 0; i < MAX_PAGE_BUTTON_WIDGETS; i++) {
            PageTabButton pageButton = new PageTabButton(i);
            pageButtons.add(pageButton);
            addRenderableWidget(pageButton);
        }

        pageNextButton = Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_page_next"), button -> {
            pageScroll = Math.min(maxPageScroll(), pageScroll + pageScrollStep());
            clampPageScroll();
            refreshPageButtons();
        }).bounds(pageRightArrowX(), pageTabsY(), PAGE_TAB_ARROW_WIDTH, PAGE_TAB_HEIGHT).build();
        addRenderableWidget(pageNextButton);

        searchBox = new PlaceholderEditBox(font, searchBoxX(), searchBoxY(), searchBoxWidth(), 18, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_search"), ColorText.translatable("gui.adventuresystems.curios.wallet.shop_search_hint"));
        searchBox.setMaxLength(64);
        searchBox.setValue(searchQuery);
        searchBox.setResponder(value -> {
            searchQuery = value == null ? "" : value;
            resetScrollImmediately();
            selectedIndex = -1;
            closeContextMenu();
            clearDragState();
            rebuildRows();
            updateDetailWidgetState();
        });
        addRenderableWidget(searchBox);

        amountBox = new CenteredAmountEditBox(font, detailAmountInputX(), detailAmountInputY(), DETAIL_AMOUNT_INPUT_SIZE, DETAIL_AMOUNT_INPUT_SIZE, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_trade_amount_input"));
        amountBox.setMaxLength(2);
        amountBox.setValue(String.valueOf(amount));
        amountBox.setResponder(this::onAmountInput);
        addRenderableWidget(amountBox);

        rewardPreviewToggleButton = addRenderableWidget(Button.builder(sectionToggleButtonText(rewardPreviewExpanded), button -> toggleRewardPreviewSection())
                .bounds(detailContentX(), 0, DETAIL_SECTION_BUTTON_SIZE, DETAIL_SECTION_BUTTON_SIZE)
                .build());

        for (int i = 0; i < MAX_PRODUCT_BUTTON_WIDGETS; i++) {
            ProductButton button = new ProductButton();
            productButtons.add(button);
            addRenderableWidget(button);
        }

        questButton = addRenderableWidget(Button.builder(Component.empty(), button -> handleQuestButtonClick())
                .bounds(questButtonX(), questButtonY(), questButtonWidth(), QUEST_PICKER_ROW_HEIGHT)
                .build());
        amountMinusButton = addRenderableWidget(Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_amount_minus"), button -> adjustAmount(-1))
                .bounds(detailAmountQuickButtonX(0), detailAmountQuickButtonY(), detailAmountQuickButtonWidth(), 18)
                .build());
        amountPlusButton = addRenderableWidget(Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_amount_plus"), button -> adjustAmount(1))
                .bounds(detailAmountQuickButtonX(1), detailAmountQuickButtonY(), detailAmountQuickButtonWidth(), 18)
                .build());
        amountTenButton = addRenderableWidget(Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_amount_ten"), button -> setAmount(Math.min(64, Math.max(1, amount) * 10)))
                .bounds(detailAmountQuickButtonX(2), detailAmountQuickButtonY(), detailAmountQuickButtonWidth(), 18)
                .build());
        amountMaxButton = addRenderableWidget(Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_amount_max"), button -> setAmount(maxUsefulTradeAmount()))
                .bounds(detailAmountQuickButtonX(3), detailAmountQuickButtonY(), detailAmountQuickButtonWidth(), 18)
                .build());
        tradeButton = addRenderableWidget(Button.builder(Component.empty(), button -> tradeSelectedEntry())
                .bounds(detailTradeButtonX(), detailTradeButtonY(), detailTradeButtonWidth(), BUTTON_HEIGHT)
                .build());
        choiceOverlayCancelButton = Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_choice_cancel"), button -> closeChoiceOverlay())
                .bounds(0, 0, choiceOverlayButtonWidth(), BUTTON_HEIGHT)
                .build();
        choiceOverlayConfirmButton = Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_choice_confirm"), button -> {
            Shop.Entry entry = selectedEntry();
            if (entry == null) return;
            if (choiceOverlaySelectedIndex >= 0) confirmChoicePurchase(entry);
            else GuiOverlay.toast("currency_wallet_shop_notice", ColorText.translatable("gui.adventuresystems.curios.wallet.shop_choice_need_select"), GuiOverlay.Position.BOTTOM_CENTER, 2500, 0, -30);
        }).bounds(0, 0, choiceOverlayButtonWidth(), BUTTON_HEIGHT).build();
        addRenderableWidget(choiceOverlayCancelButton);
        addRenderableWidget(choiceOverlayConfirmButton);
        updateChoiceOverlayButtons();

        createContextMenuButtons();
        rebuildRows();
        updateDetailWidgetState();
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
            return;
        }
        super.onClose();
    }

    private void returnToPreviousScreen() {
        if (parent != null && minecraft != null) {
            minecraft.setScreen(parent);
            return;
        }
        Network.sendOpen();
    }

    private Component sectionToggleButtonText(boolean expanded) {
        return Component.literal(expanded ? "▼" : "▶");
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
        this.questPickerScroll = 0;
        this.choiceOverlayScroll = 0;
        this.choiceOverlaySelectedIndex = -1;
        this.draggingChoiceOverlayScrollbar = false;
        closeAllOverlays();
        closeContextMenu();
        clearDragState();
        rebuildRows();
        updateDetailWidgetState();
    }

    private void updateDetailWidgetState() {
        Shop.Entry entry = selectedEntry();
        if (editorModeButton != null) {
            editorModeButton.setMessage(editorModeButtonText());
            editorModeButton.active = canEdit;
        }
        if (backpackSourceButton != null) backpackSourceButton.setMessage(sourceButtonText(true));
        if (rsSourceButton != null) rsSourceButton.setMessage(sourceButtonText(false));
        boolean choiceBuy = entry != null && mode == Shop.Mode.BUY && entry.selectable();
        boolean amountActive = detailTradeControlsVisible(entry);
        if (entry != null && !amountActive && !choiceBuy && amount != 1) {
            amount = 1;
            if (amountBox != null) amountBox.setValue("1");
        }
        if (amountBox != null) {
            amountBox.setX(detailAmountInputX());
            amountBox.setY(detailAmountInputY());
            amountBox.visible = amountActive;
            amountBox.active = amountActive;
        }
        updateAmountButton(amountMinusButton, 0, amountActive);
        updateAmountButton(amountPlusButton, 1, amountActive);
        updateAmountButton(amountTenButton, 2, amountActive);
        updateAmountButton(amountMaxButton, 3, amountActive);
        if (tradeButton != null) {
            tradeButton.setX(detailTradeButtonX());
            tradeButton.setY(detailTradeButtonY());
            tradeButton.setWidth(detailTradeButtonWidth());
            tradeButton.visible = entry != null;
            tradeButton.active = entry != null && !entry.locked() && (choiceBuy || canTrade(entry, amount));
            tradeButton.setMessage(ColorText.translatable(choiceBuy
                    ? "gui.adventuresystems.curios.wallet.shop_choice_open_button"
                    : mode == Shop.Mode.BUY
                    ? "gui.adventuresystems.curios.wallet.shop_buy"
                    : "gui.adventuresystems.curios.wallet.shop_sell"));
        }
        if (rewardPreviewToggleButton != null) {
            boolean visible = entry != null && entry.gacha() && rewardPreviewY() >= 0;
            rewardPreviewToggleButton.setX(detailContentX());
            rewardPreviewToggleButton.setY(visible ? rewardPreviewY() : 0);
            rewardPreviewToggleButton.setWidth(DETAIL_SECTION_BUTTON_SIZE);
            rewardPreviewToggleButton.setMessage(sectionToggleButtonText(rewardPreviewExpanded));
            rewardPreviewToggleButton.visible = visible;
            rewardPreviewToggleButton.active = visible;
        }
        if (questButton != null) {
            questButton.setX(questButtonX());
            questButton.setY(questButtonY());
            questButton.setWidth(questButtonWidth());
            questButton.visible = hasQuestList(entry);
            questButton.active = questButton.visible;
            questButton.setMessage(questButtonText(entry));
        }
        refreshProductButtons();
        updateContextMenuButtons();
    }

    private void updateAmountButton(Button button, int slot, boolean visible) {
        if (button == null) return;
        button.setX(detailAmountQuickButtonX(slot));
        button.setY(detailAmountQuickButtonY());
        button.setWidth(detailAmountQuickButtonWidth());
        button.visible = visible;
        button.active = visible;
    }

    @Override
    protected void renderCanvasBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updateSmoothScrolling();
        updateDetailWidgetState();
        renderPanel(graphics);
        graphics.drawCenteredString(font, title, shopTitleX(), top + 8, GOLD);
        renderBalanceSection(graphics);
        renderPageScrollBar(graphics, mouseX, mouseY);
        renderListBorder(graphics);
        renderEmptyListHint(graphics);
        Component hint = ColorText.translatable(shopHintKey());
        drawHintText(graphics, hint.getString(), listLeft() + 2, contentTop() - 11);
        renderDetailBackground(graphics);
    }

    @Override
    protected void renderCanvasForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderScrollbar(graphics, mouseX, mouseY);
        renderSelectedButtonHighlights(graphics);
        renderDetailForeground(graphics, mouseX, mouseY);
        renderDragPreview(graphics);
        renderContextMenuOverlay(graphics, mouseX, mouseY, partialTick);
        if (isChoiceOverlayOpen()) renderChoiceOverlay(graphics, mouseX, mouseY);
    }

    private void renderContextMenuOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!isContextMenuOpen() || contextEntry == null) return;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 600.0F);
        renderBox(graphics, contextMenuX - 4, contextMenuY - 4, CONTEXT_MENU_WIDTH + 8, contextMenuHeight() + 8, GOLD, 0xFF0A0A0A);
        renderBox(graphics, contextMenuX - 2, contextMenuY - 2, CONTEXT_MENU_WIDTH + 4, contextMenuHeight() + 4, GOLD_DARK, 0xFF111111);
        for (Button button : contextButtons) {
            if (button.visible) button.render(graphics, mouseX, mouseY, partialTick);
        }
        graphics.pose().popPose();
    }

    @Override
    protected void renderTooltips(GuiGraphics graphics, int scaledMouseX, int scaledMouseY, int rawMouseX, int rawMouseY) {
        if (isChoiceOverlayOpen()) {
            List<Component> overlayTooltip = choiceOverlayTooltipAt(scaledMouseX, scaledMouseY);
            if (!overlayTooltip.isEmpty()) {
                graphics.pose().pushPose();
                graphics.pose().translate(0, 0, 900);
                GuiOverlay.requestTooltip(overlayTooltip, rawMouseX, rawMouseY);
                graphics.pose().popPose();
            }
            return;
        }
        if (isContextMenuOpen()) return;
        ItemStack stack = hoveredItemStackAt(scaledMouseX, scaledMouseY);
        if (!stack.isEmpty()) {
            GuiOverlay.requestItemTooltip(stack, rawMouseX, rawMouseY);
            return;
        }
        List<Component> tooltip = tooltipAt(scaledMouseX, scaledMouseY);
        if (!tooltip.isEmpty()) GuiOverlay.requestTooltip(tooltip, rawMouseX, rawMouseY);
    }

    private void renderPanel(GuiGraphics graphics) {
        graphics.fill(left, top, left + panelWidth, top + panelHeight, GOLD_DARK);
        graphics.fill(left + 1, top + 1, left + panelWidth - 1, top + panelHeight - 1, GOLD);
        graphics.fill(left + 2, top + 2, left + panelWidth - 2, top + panelHeight - 2, PANEL_BG);
        graphics.fill(left + 6, top + 18, left + panelWidth - 6, top + panelHeight - 6, PANEL_INNER);
    }

    private static void renderBox(GuiGraphics graphics, int x, int y, int width, int height, int border, int fill) {
        graphics.fill(x, y, x + width, y + height, border);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, fill);
    }

    private void renderBalanceSection(GuiGraphics graphics) {
        int x = left + 14;
        int y = balanceY();
        int width = panelWidth - 28;
        int height = balanceHeight();
        renderBox(graphics, x, y, width, height, GOLD_DARK, BOX_BG);
        Component label = ColorText.translatable("gui.adventuresystems.curios.wallet.balance_title");
        int labelX = x + 8;
        int labelY = y + 7;
        graphics.drawString(font, label, labelX, labelY, CYAN, true);
        List<CurrencyType> currencies = sortedCurrenciesByValueDesc();
        int cellStartX = labelX + font.width(label) + 8;
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

    private void renderCurrencyCell(GuiGraphics graphics, CurrencyType currency, int x, int y, int width) {
        ItemStack stack = stack(currency.itemId());
        String text = formatCompact(displayAmount(currency.itemId()));
        graphics.renderItem(stack, x, y - 5);
        graphics.drawString(font, text, x + 20, y, GOLD, true);
        BalanceAnimation animation = animations.get(currency.itemId());
        if (animation != null && animation.deltaVisible()) {
            String delta = formatDelta(animation.delta());
            int deltaX = x + 23 + font.width(text);
            if (deltaX + font.width(delta) <= x + width) {
                graphics.drawString(font, delta, deltaX, y, animation.delta() > 0L ? GREEN : RED, true);
            }
        }
    }

    private void renderPageScrollBar(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = listLeft();
        int y = pageScrollBarY();
        int width = pageScrollBarWidth();
        if (width <= 0) return;
        boolean hover = isHover(mouseX, mouseY, x, y - 2, width, PAGE_SCROLLBAR_HEIGHT + 4);
        graphics.fill(x, y, x + width, y + PAGE_SCROLLBAR_HEIGHT, SCROLLBAR_BORDER);
        graphics.fill(x + 1, y + 1, x + width - 1, y + PAGE_SCROLLBAR_HEIGHT - 1, SCROLLBAR_TRACK);
        int thumbWidth = pageScrollThumbWidth(width);
        int thumbX = pageScrollThumbX(x, width, thumbWidth);
        graphics.fill(thumbX, y, thumbX + thumbWidth, y + PAGE_SCROLLBAR_HEIGHT, hover || draggingPageScrollbar ? SCROLLBAR_HOVER : SCROLLBAR_THUMB);
    }

    private void renderListBorder(GuiGraphics graphics) {
        int x = listLeft();
        int y = contentTop();
        int width = listWidth();
        int height = contentHeightVisible();
        renderBox(graphics, x - 1, y - 1, width + 2, height + 2, GOLD_DARK, 0xAA000000);
    }

    private void renderEmptyListHint(GuiGraphics graphics) {
        if (!rows.isEmpty()) return;
        Component text = ColorText.translatable(Objects.equals(activePageName, FAVORITES_PAGE)
                ? "gui.adventuresystems.curios.wallet.shop_favorites_empty"
                : "gui.adventuresystems.curios.wallet.shop_page_empty");
        graphics.drawCenteredString(font, text, listLeft() + listWidth() / 2, contentTop() + contentHeightVisible() / 2 - 4, TEXT_GRAY);
    }

    private void renderCell(GuiGraphics graphics, Row row, int index, Cell cell, int mouseX, int mouseY) {
        Shop.Entry entry = row.entry();
        if (dragSourceEntry != null && entry.index() == dragSourceEntry.index()) {
            renderBox(graphics, cell.x(), cell.y(), GRID_CELL_WIDTH, GRID_CELL_HEIGHT, CYAN_DARK, 0xAA101010);
            renderSelectionOutline(graphics, cell.x(), cell.y(), GRID_CELL_WIDTH, GRID_CELL_HEIGHT);
            return;
        }
        boolean selected = index == selectedIndex;
        boolean hover = isHover(mouseX, mouseY, cell.x(), cell.y(), GRID_CELL_WIDTH, GRID_CELL_HEIGHT);
        boolean canTrade = canTrade(entry, 1);
        int border = selected ? GOLD : canTrade ? GREEN_DARK : DEEP_RED;
        int fill = selected ? SELECT_BG : hover ? ROW_HOVER : ROW_BG;
        renderBox(graphics, cell.x(), cell.y(), GRID_CELL_WIDTH, GRID_CELL_HEIGHT, border, fill);
        if (selected) renderSelectionOutline(graphics, cell.x(), cell.y(), GRID_CELL_WIDTH, GRID_CELL_HEIGHT);
        int itemSlotX = cellItemSlotX(cell);
        int itemSlotY = cellItemSlotY(cell);
        ItemStack displayStack = cellDisplayStack(entry);
        renderItemCheckerSlot(graphics, itemSlotX, itemSlotY, PRODUCT_SLOT_SIZE);
        renderProductItem(graphics, displayStack, itemSlotX + 1, itemSlotY + 1);
        if (hasMultipleRewards(entry)) renderSmallPlus(graphics, itemSlotX + PRODUCT_SLOT_SIZE - 6, itemSlotY + PRODUCT_SLOT_SIZE - 6);

        renderCellStatus(graphics, entry, cell, canTrade, itemSlotX + PRODUCT_SLOT_SIZE + 3);

        ItemStack currency = stack(entry.currencyId());
        String price = formatCompact(entry.price());
        int numberX = cellNumberX(cell);
        int numberY = cellNumberY(cell);
        int numberW = cellNumberWidth(price);
        renderNumberBar(graphics, numberX, numberY, numberW);
        drawCellString(graphics, price, numberX + 3, numberY + 1, numberW - 5, canTrade ? NUMBER_BAR_TEXT : RED);
        renderCurrencyItem(graphics, currency, numberX + numberW + 1, numberY - 1);
    }

    private void renderCellStatus(GuiGraphics graphics, Shop.Entry entry, Cell cell, boolean canTrade, int minX) {
        Component primary = cellPrimaryStatusText(entry);
        Component limit = cellLimitStatusText(entry);
        int available = Math.max(4, cell.x() + GRID_CELL_WIDTH - 3 - minX);
        int y = cell.y() + 2;
        if (primary != null && limit != null) {
            String primaryText = primary.getString();
            int primaryWidth = Math.min(font.width(primaryText), Math.max(8, available / 2));
            drawCellString(graphics, primaryText, minX, y, primaryWidth, cellPrimaryStatusColor(entry, canTrade));
            int limitX = minX + primaryWidth + 3;
            drawCellString(graphics, limit.getString(), limitX, y, Math.max(1, available - primaryWidth - 3), cellLimitStatusColor(entry));
            return;
        }
        if (primary != null) {
            drawCellString(graphics, primary.getString(), minX, y, available, cellPrimaryStatusColor(entry, canTrade));
            return;
        }
        if (limit != null) {
            drawCellString(graphics, limit.getString(), minX, y, available, cellLimitStatusColor(entry));
            return;
        }
        if (entry.gacha()) {
            drawCellString(graphics, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_gacha_marker").getString(), minX, y, available, CYAN);
        }
    }

    private int cellItemSlotX(Cell cell) {
        return cell.x() + 3;
    }

    private int cellItemSlotY(Cell cell) {
        return cell.y() + 3;
    }

    private int cellNumberX(Cell cell) {
        return cell.x() + PRODUCT_SLOT_SIZE + 5;
    }

    private int cellNumberY(Cell cell) {
        return cell.y() + GRID_CELL_HEIGHT - NUMBER_BAR_HEIGHT - 3;
    }

    private int cellNumberWidth(String price) {
        int startOffset = PRODUCT_SLOT_SIZE + 5;
        int maximum = Math.max(24, GRID_CELL_WIDTH - startOffset - 17);
        return Math.min(maximum, Math.max(24, font.width(price) + 6));
    }

    private Component cellPrimaryStatusText(Shop.Entry entry) {
        if (entry.requiredQuestIds() != null && !entry.requiredQuestIds().isEmpty()) {
            if (!entry.locked()) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_cell_quest_done");
            return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_cell_quest_need", missingRequiredQuestCount(entry));
        }
        if (entry.timedLimitSeconds() > 0) {
            long remaining = timedRemaining(entry);
            if (remaining <= 0L) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_cell_timed_ready");
            return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_cell_timed_wait", cellDurationText(remaining));
        }
        return null;
    }

    private int cellPrimaryStatusColor(Shop.Entry entry, boolean canTrade) {
        if (entry.requiredQuestIds() != null && !entry.requiredQuestIds().isEmpty()) return entry.locked() ? RED : GREEN;
        if (entry.timedLimitSeconds() > 0) return timedRemaining(entry) > 0L ? RED : GREEN;
        return canTrade ? GREEN : RED;
    }

    private Component cellLimitStatusText(Shop.Entry entry) {
        if (entry.totalLimit() <= 0) return null;
        int remaining = totalRemaining(entry);
        if (remaining <= 0) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_cell_limit_sold_out");
        return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_cell_limit_left", remaining, entry.totalLimit());
    }

    private int cellLimitStatusColor(Shop.Entry entry) {
        return totalRemaining(entry) > 0 ? GREEN : RED;
    }

    private void renderItemCheckerSlot(GuiGraphics graphics, int x, int y) {
        renderItemCheckerSlot(graphics, x, y, 20);
    }

    private void renderItemCheckerSlot(GuiGraphics graphics, int x, int y, int size) {
        GuiTheme.itemSlot(graphics, x, y, size, 4, false);
    }

    private void renderProductItem(GuiGraphics graphics, ItemStack stack, int x, int y) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(PRODUCT_ITEM_SCALE, PRODUCT_ITEM_SCALE, 1.0F);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();
        int decorationOffset = Math.round(16.0F * PRODUCT_ITEM_SCALE) - 16;
        graphics.renderItemDecorations(font, stack, x + decorationOffset, y + decorationOffset);
    }

    private void renderNumberBar(GuiGraphics graphics, int x, int y, int width) {
        graphics.fill(x, y, x + width, y + NUMBER_BAR_HEIGHT, NUMBER_BAR_BORDER);
        graphics.fill(x + 1, y + 1, x + width - 1, y + NUMBER_BAR_HEIGHT - 1, NUMBER_BAR_BG);
    }

    private void renderCurrencyItem(GuiGraphics graphics, ItemStack stack, int x, int y) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(CURRENCY_ITEM_SCALE, CURRENCY_ITEM_SCALE, 1.0F);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();
    }

    private void drawCellString(GuiGraphics graphics, String text, int x, int y, int available, int color) {
        if (text == null || text.isBlank()) return;
        String line = clipped(text, Math.max(1, available));
        graphics.drawString(font, line, x, y, color, false);
    }

    private void renderDetailBackground(GuiGraphics graphics) {
        int x = detailX();
        int y = detailTop();
        int right = left + panelWidth - 2;
        graphics.fill(x, y, right, y + detailVisibleHeight(), BOX_BG);
    }

    private void renderDetailForeground(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = detailX();
        int y = detailTop();
        int contentX = detailContentX();
        int textX = contentX + 24;
        int clipRight = left + panelWidth - 6;
        int scissorLeft = Math.max(x, contentX - 4);
        enableCanvasScissor(graphics, scissorLeft, y + 2, clipRight, y + detailVisibleHeight() - 2);
        try {
            Shop.Entry entry = selectedEntry();
            if (entry == null) {
                graphics.drawCenteredString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_select_entry_hint"), x + (left + panelWidth - x) / 2, y + 18, TEXT_GRAY);
                return;
            }
            ItemStack item = detailDisplayStack(entry);
            renderItemCheckerSlot(graphics, contentX - 2, y + 4);
            graphics.renderItem(item, contentX, y + 6);
            graphics.renderItemDecorations(font, item, contentX, y + 6);
            if (hasMultipleRewards(entry)) renderSmallPlus(graphics, contentX + 12, y + 18);
            graphics.drawString(font, clipped(entryDisplayName(entry, item).getString(), Math.max(40, clipRight - textX - 4)), textX, y + 4, entry.locked() ? DEEP_RED : TEXT_WHITE, true);
            graphics.drawString(font, entryCountText(entry), textX, y + 18, TEXT_GRAY, true);
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
            if (rewardPreviewToggleButton != null && rewardPreviewToggleButton.visible) {
                rewardPreviewToggleButton.render(graphics, mouseX, mouseY, 0.0F);
            }
            if (detailTradeControlsVisible(entry)) {
                graphics.drawString(font, tradeAmountLabel(), contentX, detailAmountInputY() + 5, TEXT_WHITE, true);
                renderTradeCostPreview(graphics, entry);
                renderDetailAmountSlider(graphics, detailAmountSliderX(), detailAmountSliderY());
            }
            if (isRewardPickerOpen() && isSelectableRewardEntry(entry)) renderRewardPicker(graphics, entry, mouseX, mouseY);
            if (isQuestPickerOpen() && hasQuestList(entry)) renderQuestPicker(graphics, entry, mouseX, mouseY);
        } finally {
            graphics.disableScissor();
        }
    }


    private void renderTradeCostPreview(GuiGraphics graphics, Shop.Entry entry) {
        long cost = tradeCostAmount(entry);
        Component text = ColorText.translatable("gui.adventuresystems.curios.wallet.shop_trade_cost_preview", formatCompact(cost));
        int textX = detailTradeCostTextX();
        int textY = detailAmountInputY() + 5;
        graphics.drawString(font, text, textX, textY, TEXT_WHITE, false);
        ItemStack stack = tradeCostStack(entry);
        if (!stack.isEmpty()) {
            int slotX = detailTradeCostIconX(entry);
            int slotY = detailTradeCostIconY();
            renderItemCheckerSlot(graphics, slotX, slotY, DETAIL_PRICE_SLOT_SIZE);
            graphics.renderItem(stack, slotX, slotY);
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

    private void renderStatusLines(GuiGraphics graphics, Shop.Entry entry, int x, int y) {
        int lineY = y;
        if (isSelectableRewardEntry(entry)) {
            graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_sell_choice_hint"), x, lineY, CYAN, true);
            lineY += 11;
        }
        graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_sell_inventory_count", materialCompact(sellInventoryCount(entry))), x, lineY, sellInventoryCount(entry) > 0L ? LIMIT_COUNT : DEEP_RED, true);
        lineY += 11;
        graphics.drawString(font, backpackMaterialText(entry), x, lineY, sellBackpackCount(entry) > 0L ? LIMIT_COUNT : DEEP_RED, true);
        lineY += 11;
        graphics.drawString(font, rsMaterialText(entry), x, lineY, sellRsCount(entry) > 0L ? LIMIT_COUNT : DEEP_RED, true);
        lineY += 11;
        if (sellProgress(entry) > 0L) {
            graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_sell_progress_detail", materialCompact(sellProgress(entry)), materialCompact(sellTradeStack(entry).getCount())), x, lineY, LIMIT_TIME, true);
        }
    }

    private void renderBuyLimitStatusLines(GuiGraphics graphics, Shop.Entry entry, int x, int y) {
        int lineY = y;
        if (entry.timedLimitSeconds() > 0) {
            long remaining = timedRemaining(entry);
            graphics.drawString(font, timedLimitText(entry, remaining), x, lineY, remaining > 0L ? DEEP_RED : LIMIT_TIME, true);
            lineY += 14;
        }
        if (entry.totalLimit() > 0) {
            int remaining = totalRemaining(entry);
            graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_total_limit_status", entry.totalBought(), entry.totalLimit(), remaining), x, lineY, remaining > 0 ? LIMIT_COUNT : DEEP_RED, true);
        }
    }



    private void renderBuyPaymentSourceLines(GuiGraphics graphics, Shop.Entry entry, int x, int y) {
        int invColor = buyInventoryCount(entry) > 0L ? LIMIT_COUNT : TEXT_GRAY;
        int walletColor = buyWalletCount(entry) > 0L ? LIMIT_COUNT : TEXT_GRAY;
        int backpackColor = entry.backpackCount() > 0L ? LIMIT_COUNT : TEXT_GRAY;
        int rsColor = entry.rsCount() > 0L ? LIMIT_COUNT : TEXT_GRAY;
        graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_buy_source_inventory", materialCompact(buyInventoryCount(entry))), x, y, invColor, true);
        graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_buy_source_wallet", materialCompact(buyWalletCount(entry))), x + 74, y, walletColor, true);
        graphics.drawString(font, buyBackpackMaterialText(entry), x, y + 11, backpackColor, true);
        graphics.drawString(font, buyRsMaterialText(entry), x + 74, y + 11, rsColor, true);
        graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_buy_source_total", materialCompact(buyPaymentTotal(entry))), x, y + 22, buyPaymentTotal(entry) >= entry.price() ? CYAN : DEEP_RED, true);
    }

    private MutableComponent buyBackpackMaterialText(Shop.Entry entry) {
        if (!useBackpackSource) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_buy_source_backpack_disabled");
        if (!entry.backpackLoaded() || !entry.hasBackpack()) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_buy_source_backpack_missing");
        return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_buy_source_backpack", materialCompact(entry.backpackCount()));
    }

    private MutableComponent buyRsMaterialText(Shop.Entry entry) {
        if (!useRsSource) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_buy_source_rs_disabled");
        if ("BOUND".equals(entry.rsState())) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_buy_source_rs", materialCompact(entry.rsCount()));
        return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_buy_source_rs_unbound");
    }

    private void renderDetailAmountSlider(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + DETAIL_AMOUNT_SLIDER_WIDTH, y + 6, CYAN_DARK);
        int fill = amount * DETAIL_AMOUNT_SLIDER_WIDTH / 64;
        graphics.fill(x + 1, y + 1, x + fill, y + 5, CYAN);
        int knob = x + fill - 3;
        graphics.fill(knob, y - 3, knob + 6, y + 9, GOLD);
    }

    private void renderSelectionOutline(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x - 1, y - 1, x + width + 1, y, CYAN);
        graphics.fill(x - 1, y + height, x + width + 1, y + height + 1, CYAN);
        graphics.fill(x - 1, y, x, y + height, CYAN);
        graphics.fill(x + width, y, x + width + 1, y + height, CYAN);
    }

    private void renderSelectedButtonHighlights(GuiGraphics graphics) {
        int modeX = mode == Shop.Mode.BUY ? buyButtonX() : sellButtonX();
        renderSelectionOutline(graphics, modeX, top + 6, TOP_BUTTON_WIDTH, BUTTON_HEIGHT);
        if (Objects.equals(activePageName, FAVORITES_PAGE) && favoritesPageButton != null) {
            renderSelectionOutline(graphics, favoritesPageButton.getX(), favoritesPageButton.getY(), favoritesPageButton.getWidth(), PAGE_TAB_HEIGHT);
        } else if (Objects.equals(activePageName, ALL_PAGE) && allPageButton != null) {
            renderSelectionOutline(graphics, allPageButton.getX(), allPageButton.getY(), allPageButton.getWidth(), PAGE_TAB_HEIGHT);
        }
        for (int i = 0; i < visiblePageButtonPages.size() && i < pageButtons.size(); i++) {
            if (!Objects.equals(visiblePageButtonPages.get(i), activePageName)) continue;
            PageTabButton button = pageButtons.get(i);
            if (!button.visible) continue;
            enableCanvasScissor(graphics, categoryViewportLeft() - 1, pageTabsY() - 1, categoryViewportRight() + 1, pageTabsY() + PAGE_TAB_HEIGHT + 1);
            try {
                renderSelectionOutline(graphics, button.getX(), button.getY(), button.getWidth(), PAGE_TAB_HEIGHT);
            } finally {
                graphics.disableScissor();
            }
        }
    }


    private void renderScrollbar(GuiGraphics graphics, int mouseX, int mouseY) {
        Scrollbar scrollbar = scrollbar();
        if (!scrollbar.visible()) return;
        boolean hover = isHover(mouseX, mouseY, scrollbar.x() - 2, scrollbar.thumbTop(), LIST_SCROLLBAR_WIDTH + 4, scrollbar.thumbBottom() - scrollbar.thumbTop());
        graphics.fill(scrollbar.x(), scrollbar.trackTop(), scrollbar.x() + LIST_SCROLLBAR_WIDTH, scrollbar.trackBottom(), SCROLLBAR_BORDER);
        graphics.fill(scrollbar.x() + 1, scrollbar.trackTop() + 1, scrollbar.x() + LIST_SCROLLBAR_WIDTH - 1, scrollbar.trackBottom() - 1, SCROLLBAR_TRACK);
        graphics.fill(scrollbar.x(), scrollbar.thumbTop(), scrollbar.x() + LIST_SCROLLBAR_WIDTH, scrollbar.thumbBottom(), hover || draggingScrollbar ? SCROLLBAR_HOVER : SCROLLBAR_THUMB);
    }

    private List<Component> tooltipAt(int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (isHover(mouseX, mouseY, buyButtonX(), top + 6, TOP_BUTTON_WIDTH, BUTTON_HEIGHT)) {
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_buy_tab").withStyle(ChatFormatting.GOLD));
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_buy_tab"));
            return tooltip;
        }
        if (isHover(mouseX, mouseY, sellButtonX(), top + 6, TOP_BUTTON_WIDTH, BUTTON_HEIGHT)) {
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_sell_tab").withStyle(ChatFormatting.GOLD));
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_sell_tab"));
            return tooltip;
        }
        if (editorModeButton != null && isHover(mouseX, mouseY, editorModeButton.getX(), editorModeButton.getY(), editorModeButton.getWidth(), BUTTON_HEIGHT)) {
            tooltip.add(editorModeButtonText().copy().withStyle(ChatFormatting.GOLD));
            tooltip.add(ColorText.translatable(canEdit
                    ? editorMode
                    ? "gui.adventuresystems.curios.wallet.shop_editor_mode_on_tip"
                    : "gui.adventuresystems.curios.wallet.shop_editor_mode_off_tip"
                    : "gui.adventuresystems.curios.wallet.shop_editor_mode_no_permission_tip"));
            return tooltip;
        }
        if (editorMode && isHover(mouseX, mouseY, newEntryButtonX(), top + 6, NEW_BUTTON_WIDTH, BUTTON_HEIGHT)) {
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_new_entry").withStyle(ChatFormatting.GOLD));
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_new_entry"));
            return tooltip;
        }
        if (backpackSourceButton != null && isHover(mouseX, mouseY, backpackSourceButton.getX(), backpackSourceButton.getY(), backpackSourceButton.getWidth(), BUTTON_HEIGHT)) {
            tooltip.add(sourceButtonText(true).copy().withStyle(ChatFormatting.GOLD));
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_source_backpack_tip"));
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_source_priority_tip"));
            return tooltip;
        }
        if (rsSourceButton != null && isHover(mouseX, mouseY, rsSourceButton.getX(), rsSourceButton.getY(), rsSourceButton.getWidth(), BUTTON_HEIGHT)) {
            tooltip.add(sourceButtonText(false).copy().withStyle(ChatFormatting.GOLD));
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_source_rs_tip"));
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_source_priority_tip"));
            return tooltip;
        }
        String hoveredPage = pageAt(mouseX, mouseY);
        if (hoveredPage != null) {
            tooltip.add(pageLabel(hoveredPage).copy().withStyle(ChatFormatting.GOLD));
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_page_click_tip"));
            return tooltip;
        }
        Long questClick = detailQuestClickAt(mouseX, mouseY);
        if (questClick != null && questClick != 0L) {
            Shop.Entry entry = selectedEntry();
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_quest_click_tip").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal(questTitleById(entry, questClick)).withStyle(ChatFormatting.AQUA));
            return tooltip;
        }
        if (selectedEntry() != null && isInsideQuestPicker(mouseX, mouseY)) {
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_quest_list_click_tip").withStyle(ChatFormatting.GOLD));
            return tooltip;
        }
        currencyTooltip(mouseX, mouseY, tooltip);
        if (!tooltip.isEmpty()) return tooltip;
        if (detailTradeControlsVisible(selectedEntry()) && isHover(mouseX, mouseY, detailAmountInputX(), detailAmountInputY(), DETAIL_AMOUNT_INPUT_SIZE, DETAIL_AMOUNT_INPUT_SIZE)) {
            tooltip.add(tradeAmountLabel().withStyle(ChatFormatting.GOLD));
            tooltip.add(ColorText.translatable(mode == Shop.Mode.BUY ? "gui.adventuresystems.curios.wallet.shop_tooltip_amount_input" : "gui.adventuresystems.curios.wallet.shop_tooltip_sell_amount_input"));
            return tooltip;
        }
        if (detailTradeControlsVisible(selectedEntry()) && isHover(mouseX, mouseY, detailAmountSliderX(), detailAmountSliderY() - 4, DETAIL_AMOUNT_SLIDER_WIDTH, 16)) {
            tooltip.add(tradeAmountLabel().withStyle(ChatFormatting.GOLD));
            tooltip.add(ColorText.translatable(mode == Shop.Mode.BUY ? "gui.adventuresystems.curios.wallet.shop_tooltip_amount_slider" : "gui.adventuresystems.curios.wallet.shop_tooltip_sell_amount_slider"));
            return tooltip;
        }
        if (selectedEntry() != null && isHover(mouseX, mouseY, detailTradeButtonX(), detailTradeButtonY(), detailTradeButtonWidth(), BUTTON_HEIGHT)) {
            Shop.Entry entry = selectedEntry();
            if (entry != null) {
                if (entry.locked()) {
                    tooltip.add(ColorText.translatable(mode == Shop.Mode.BUY && entry.selectable() ? "gui.adventuresystems.curios.wallet.shop_choice_open_button" : mode == Shop.Mode.BUY ? "gui.adventuresystems.curios.wallet.shop_buy" : "gui.adventuresystems.curios.wallet.shop_sell").withStyle(ChatFormatting.RED));
                    tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_need_complete_task", requiredQuestTitlesText(entry)).withStyle(ChatFormatting.RED));
                } else {
                    tooltip.add(ColorText.translatable(mode == Shop.Mode.BUY && entry.selectable() ? "gui.adventuresystems.curios.wallet.shop_choice_open_button" : mode == Shop.Mode.BUY ? "gui.adventuresystems.curios.wallet.shop_buy" : "gui.adventuresystems.curios.wallet.shop_sell").withStyle(ChatFormatting.GOLD));
                    tooltip.add(ColorText.translatable(mode == Shop.Mode.BUY && entry.selectable() ? "gui.adventuresystems.curios.wallet.shop_choice_open_tip" : mode == Shop.Mode.BUY ? "gui.adventuresystems.curios.wallet.shop_tooltip_buy_button" : "gui.adventuresystems.curios.wallet.shop_tooltip_sell_button"));
                    long previewTotal = mode == Shop.Mode.BUY ? safeMultiply(entry.price(), amount) : safeMultiply(entry.price(), previewSuccessTrades(entry, amount));
                    tooltip.add(totalTradeText(previewTotal, entry.currencyId()));
                    if (mode == Shop.Mode.SELL) tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_sell_inventory_notice"));
                }
                return tooltip;
            }
        }
        Row row = rowAt(mouseX, mouseY);
        if (row == null) return tooltip;
        Shop.Entry entry = row.entry();
        if (entry == null) return tooltip;

        tooltip.add(entryDisplayName(entry, cellDisplayStack(entry)).copy().withStyle(ChatFormatting.GOLD));
        appendEntryDescriptionTooltip(tooltip, entry);
        tooltip.add(ColorText.translatable(mode == Shop.Mode.BUY ? "gui.adventuresystems.curios.wallet.shop_tooltip_click_buy_view" : "gui.adventuresystems.curios.wallet.shop_tooltip_click_sell_view"));
        tooltip.add(ColorText.translatable(editorMode
                ? "gui.adventuresystems.curios.wallet.shop_tooltip_editor_actions"
                : "gui.adventuresystems.curios.wallet.shop_tooltip_right_favorite"));
        return tooltip;
    }

    private void appendEntryDescriptionTooltip(List<Component> tooltip, Shop.Entry entry) {
        if (entry == null || entry.description() == null || entry.description().isBlank()) return;
        for (String paragraph : entry.description().split("\\R", -1)) {
            String remaining = paragraph.strip();
            if (remaining.isEmpty()) {
                tooltip.add(Component.empty());
                continue;
            }
            while (!remaining.isEmpty()) {
                String line = font.plainSubstrByWidth(remaining, 220);
                if (line.isEmpty()) break;
                tooltip.add(Component.literal(line));
                remaining = remaining.substring(line.length()).stripLeading();
            }
        }
    }

    private String shopHintKey() {
        if (mode == Shop.Mode.SELL) return editorMode ? "gui.adventuresystems.curios.wallet.shop_editor_sell_hint_modal" : "gui.adventuresystems.curios.wallet.shop_sell_hint";
        return editorMode ? "gui.adventuresystems.curios.wallet.shop_editor_hint_modal" : "gui.adventuresystems.curios.wallet.shop_hint";
    }

    private MutableComponent tradeAmountLabel() {
        return ColorText.translatable(mode == Shop.Mode.BUY ? "gui.adventuresystems.curios.wallet.shop_trade_amount_label" : "gui.adventuresystems.curios.wallet.shop_sell_times_label");
    }

    private Component entryDisplayName(Shop.Entry entry, ItemStack fallbackStack) {
        if (entry != null && entry.displayName() != null && !entry.displayName().isBlank()) return Component.literal(entry.displayName());
        if (entry != null && entry.command() != null && !entry.command().isBlank()) return ShopGuiSupport.stackNameComponent(entry.stack());
        if (entry != null && entry.gacha() && !entry.selectable()) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_gacha_title");
        return ShopGuiSupport.stackNameComponent(fallbackStack == null || fallbackStack.isEmpty() ? entry == null ? ItemStack.EMPTY : entry.stack() : fallbackStack);
    }

    private MutableComponent entryCountText(Shop.Entry entry) {
        int count = mode == Shop.Mode.SELL ? sellTradeStack(entry).getCount() : entry.stack().getCount();
        return ColorText.translatable(mode == Shop.Mode.BUY ? "gui.adventuresystems.curios.wallet.shop_single_count" : "gui.adventuresystems.curios.wallet.shop_sell_item_count", count);
    }

    private void renderEntryPriceLine(GuiGraphics graphics, Shop.Entry entry, int x, int y) {
        Component text = entryPriceIconText(entry);
        graphics.drawString(font, text, x, y, TEXT_WHITE, false);
        ItemStack currency = stack(entry.currencyId());
        if (currency.isEmpty()) return;
        int slotX = detailPriceIconX(entry);
        int slotY = detailPriceIconY();
        renderItemCheckerSlot(graphics, slotX, slotY, DETAIL_PRICE_SLOT_SIZE);
        graphics.renderItem(currency, slotX, slotY);
    }

    private Component entryPriceIconText(Shop.Entry entry) {
        return ColorText.translatable(
                mode == Shop.Mode.BUY
                        ? "gui.adventuresystems.curios.wallet.shop_single_price_icon"
                        : "gui.adventuresystems.curios.wallet.shop_sell_income_single_icon",
                formatExact(entry.price())
        );
    }

    private int detailPriceIconX(Shop.Entry entry) {
        return detailContentX() + font.width(entryPriceIconText(entry)) + 3;
    }

    private int detailPriceIconY() {
        return detailTop() + 28;
    }

    private MutableComponent totalTradeText(long total, String currencyId) {
        return ColorText.translatable(mode == Shop.Mode.BUY ? "gui.adventuresystems.curios.wallet.shop_price" : "gui.adventuresystems.curios.wallet.shop_sell_total_income", Component.literal(formatExact(total)).withStyle(ChatFormatting.YELLOW), currencyName(currencyId));
    }

    private void currencyTooltip(int mouseX, int mouseY, List<Component> tooltip) {
        int x = left + 14;
        int y = balanceY();
        int width = panelWidth - 28;
        Component label = ColorText.translatable("gui.adventuresystems.curios.wallet.balance_title");
        int cellStartX = x + 8 + font.width(label) + 8;
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
                tooltip.add(ShopGuiSupport.stackNameComponent(currency.itemId()).copy().withStyle(ChatFormatting.GOLD));
                tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_currency_balance", Component.literal(formatExact(amountOf(currency.itemId()))).withStyle(ChatFormatting.YELLOW)));
                tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_currency_value", Component.literal(formatExact(currency.value())).withStyle(ChatFormatting.AQUA)));
                return;
            }
        }
    }

    @Override
    protected boolean canvasMouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
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

        if (button == 0
                && editorMode
                && Screen.hasControlDown()
                && ctrlDragRow != null) {
            beginEntryDrag(
                    ctrlDragRow,
                    (int) mouseX,
                    (int) mouseY
            );

            clearTextFocus();
            return true;
        }

        if (super.canvasMouseClicked(
                mouseX,
                mouseY,
                button
        )) {
            return true;
        }

        if (button == 0
                && isInsideRewardPreviewScrollbar(
                        (int) mouseX,
                        (int) mouseY
                )) {
            draggingRewardPreviewScrollbar = true;

            Shop.Entry entry =
                    selectedEntry();

            int previewY =
                    rewardPreviewY();

            Scrollbar rewardPreviewScrollbar =
                    rewardPreviewScrollbar(
                            entry,
                            previewY
                    );

            if (mouseY >= rewardPreviewScrollbar.thumbTop()
                    && mouseY <= rewardPreviewScrollbar.thumbBottom()) {
                rewardPreviewScrollbarGrabOffset =
                        (int) mouseY
                                - rewardPreviewScrollbar.thumbTop();
            } else {
                rewardPreviewScrollbarGrabOffset =
                        rewardPreviewScrollbar.thumbHeight() / 2;

                updateRewardPreviewScrollFromMouse(
                        (int) mouseY
                );
            }

            clearTextFocus();
            return true;
        }

        if (button == 0
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

        if (button == 0
                && isInsideDetailAmountSlider(
                        mouseX,
                        mouseY
                )) {
            draggingAmountSlider = true;
            updateAmountFromMouse(
                    (int) mouseX
            );
            clearTextFocus();
            return true;
        }

        Long questClick =
                detailQuestClickAt(
                        (int) mouseX,
                        (int) mouseY
                );

        if (button == 0
                && questClick != null
                && questClick != 0L) {
            openKtQuest(questClick);
            clearTextFocus();
            return true;
        }

        if (button == 0
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

        if (button == 0
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

        if (button == 1) {
            openContextMenu(
                    rowClick,
                    (int) mouseX,
                    (int) mouseY
            );

            clearTextFocus();
            return true;
        }

        if (button == 0) {
            selectRow(
                    rowClick.index()
            );

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
            case CONTEXT_MENU ->
                    handleContextMenuClick(
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
        if (button == 0
                && isInsideQuestPickerScrollbar(
                        mouseX,
                        mouseY
                )) {
            draggingQuestPickerScrollbar = true;

            Scrollbar questScrollbar =
                    questPickerScrollbar(
                            selectedEntry()
                    );

            if (mouseY >= questScrollbar.thumbTop()
                    && mouseY <= questScrollbar.thumbBottom()) {
                questPickerScrollbarGrabOffset =
                        mouseY
                                - questScrollbar.thumbTop();
            } else {
                questPickerScrollbarGrabOffset =
                        questScrollbar.thumbHeight() / 2;

                updateQuestPickerScrollFromMouse(
                        mouseY
                );
            }

            return;
        }

        if (button == 0
                && handleQuestPickerClick(
                        mouseX,
                        mouseY
                )) {
            return;
        }

        if (button == 0) {
            overlayLayers.close(
                    OverlayLayer.QUEST_PICKER
            );

            draggingQuestPickerScrollbar = false;
        }
    }

    private void handleRewardOverlayClick(
            int mouseX,
            int mouseY,
            int button
    ) {
        if (button == 0
                && handleRewardPickerClick(
                        mouseX,
                        mouseY
                )) {
            return;
        }

        if (button == 0) {
            overlayLayers.close(
                    OverlayLayer.REWARD_PICKER
            );
        }
    }

    private void tradeSelectedEntry() {
        Shop.Entry entry = selectedEntry();
        if (entry == null) return;
        if (entry.locked()) {
            GuiOverlay.toast("currency_wallet_shop_notice", tradeFailText(entry), GuiOverlay.Position.BOTTOM_CENTER, 2500, 0, -30);
            return;
        }
        if (mode == Shop.Mode.BUY && entry.selectable()) {
            openChoiceOverlay(entry);
            return;
        }
        if (!canTrade(entry, amount)) {
            GuiOverlay.toast("currency_wallet_shop_notice", tradeFailText(entry), GuiOverlay.Position.BOTTOM_CENTER, 2500, 0, -30);
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
            GuiOverlay.toast("currency_wallet_shop_notice", ColorText.translatable("msg.adventuresystems.curios.wallet.shop_open_task_fail"), GuiOverlay.Position.BOTTOM_CENTER, 2500, 0, -30);
        }
    }

    private MutableComponent tradeFailText(Shop.Entry entry) {
        if (entry != null && entry.locked()) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_need_complete_task_first", requiredQuestTitlesText(entry));
        if (mode == Shop.Mode.SELL) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_sell_fail_preview", entry == null ? 0L : sellTotalMaterials(entry));
        if (entry != null && timedRemaining(entry) > 0L) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_timed_wait", formatDuration(timedRemaining(entry)));
        if (entry != null && totalRemaining(entry) <= 0) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_limit_reached");
        return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_buy_fail_no_money");
    }

    private void renderRewardPreview(GuiGraphics graphics, Shop.Entry entry, int x, int y, int mouseX, int mouseY) {
        if (y < 0 || rewardPreviewBottomY() - y < 12) return;
        graphics.drawString(font, ColorText.translatable(entry.selectable() ? (mode == Shop.Mode.SELL ? "gui.adventuresystems.curios.wallet.shop_sell_choice_title" : "gui.adventuresystems.curios.wallet.shop_choice_title") : "gui.adventuresystems.curios.wallet.shop_reward_probability_title"), x + DETAIL_SECTION_BUTTON_SIZE + 4, y + 4, CYAN, true);
        if (!rewardPreviewExpanded) return;
        List<Shop.Reward> rewards = sortedRewardPreviewRewards(entry);
        if (rewards.isEmpty()) return;

        int listX = rewardPreviewListX();
        int listY = rewardPreviewListY(y);
        int listHeight = rewardPreviewListHeight(y);
        if (listHeight < REWARD_PREVIEW_ROW_HEIGHT) return;

        renderRewardPreviewFrame(graphics, y);
        int rewardPreviewMax = rewardPreviewMaxScroll(entry, y);
        rewardPreviewScroll = Math.max(0D, Math.min(rewardPreviewMax, rewardPreviewScroll));
        double visualRewardPreviewScroll = rewardPreviewScrollSmoothing.follow(rewardPreviewScroll, rewardPreviewMax);
        int rewardPreviewStart = Math.max(0, Math.min((int) Math.floor(visualRewardPreviewScroll), rewardPreviewMax));
        int rewardPreviewShift = (int) Math.round((visualRewardPreviewScroll - rewardPreviewStart) * REWARD_PREVIEW_ROW_HEIGHT);
        Scrollbar scrollbar = rewardPreviewScrollbar(entry, y);
        int scrollbarSpace = scrollbar.visible() ? REWARD_PREVIEW_SCROLLBAR_WIDTH + 4 : 0;
        int contentWidth = Math.max(40, detailContentWidth() - 2 - REWARD_PREVIEW_FRAME_PADDING * 2 - scrollbarSpace);
        int cellWidth = Math.max(40, (contentWidth - REWARD_PREVIEW_CELL_GAP) / REWARD_PREVIEW_COLUMNS);
        int visibleRows = rewardPreviewVisibleRows(y);

        int displaySlotCount = rewardPreviewDisplaySlotCount(entry);
        enableCanvasScissor(graphics, listX, listY, listX + contentWidth, listY + listHeight);
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
                renderRewardPreviewCell(graphics, reward, cellX, rowY);
            }
        }
        graphics.disableScissor();

        renderRewardPreviewScrollbar(graphics, scrollbar, mouseX, mouseY);
    }

    private void renderRewardPreviewFrame(GuiGraphics graphics, int previewY) {
        int x = detailContentX();
        int y = rewardPreviewFrameY(previewY);
        int right = x + detailContentWidth();
        int bottom = rewardPreviewBottomY();
        if (bottom <= y + 1) return;
        graphics.fill(x, y, right, y + 1, CYAN_DARK);
        graphics.fill(x, bottom - 1, right, bottom, CYAN_DARK);
        graphics.fill(x, y + 1, x + 1, bottom - 1, CYAN_DARK);
        graphics.fill(right - 1, y + 1, right, bottom - 1, CYAN_DARK);
    }

    private void renderRewardPreviewCell(GuiGraphics graphics, Shop.Reward reward, int x, int y) {
        renderItemCheckerSlot(graphics, x, y, REWARD_PREVIEW_SLOT_SIZE);
        ItemStack stack = reward.empty() ? new ItemStack(net.minecraft.world.item.Items.BARRIER) : reward.stack();
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(font, stack, x, y);
        String chanceText = percent(reward.chance());
        graphics.drawString(font, chanceText, x + REWARD_PREVIEW_SLOT_SIZE + 6, y + 4, REWARD_PREVIEW_CHANCE_COLOR, true);
    }

    private void renderRewardPreviewScrollbar(GuiGraphics graphics, Scrollbar scrollbar, int mouseX, int mouseY) {
        if (!scrollbar.visible()) return;
        boolean hover = isInsideRewardPreviewScrollbar(mouseX, mouseY);
        graphics.fill(scrollbar.x(), scrollbar.trackTop(), scrollbar.x() + REWARD_PREVIEW_SCROLLBAR_WIDTH, scrollbar.trackBottom(), SCROLLBAR_BORDER);
        graphics.fill(scrollbar.x() + 1, scrollbar.trackTop() + 1, scrollbar.x() + REWARD_PREVIEW_SCROLLBAR_WIDTH - 1, scrollbar.trackBottom() - 1, SCROLLBAR_TRACK);
        graphics.fill(scrollbar.x(), scrollbar.thumbTop(), scrollbar.x() + REWARD_PREVIEW_SCROLLBAR_WIDTH, scrollbar.thumbBottom(), hover || draggingRewardPreviewScrollbar ? SCROLLBAR_HOVER : SCROLLBAR_THUMB);
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
        double visualScroll = rewardPreviewScrollSmoothing.follow(rewardPreviewScroll, maxScroll);
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

    private void updateRewardPreviewScrollFromMouse(int mouseY) {
        Shop.Entry entry = selectedEntry();
        int previewY = rewardPreviewY();
        if (!rewardPreviewExpanded || entry == null || !entry.gacha() || previewY < 0) return;
        Scrollbar scrollbar = rewardPreviewScrollbar(entry, previewY);
        if (!scrollbar.visible()) return;
        int travel = scrollbar.trackHeight() - scrollbar.thumbHeight();
        int local = mouseY - scrollbar.trackTop() - rewardPreviewScrollbarGrabOffset;
        local = Math.max(0, Math.min(travel, local));
        rewardPreviewScroll = local * rewardPreviewMaxScroll(entry, previewY) / (double) Math.max(1, travel);
        rewardPreviewScrollSmoothing.snap(rewardPreviewScroll, rewardPreviewMaxScroll(entry, previewY));
    }

    private ItemStack cellDisplayStack(Shop.Entry entry) {
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
        return cellDisplayStack(entry);
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
        int index = (int) ((System.currentTimeMillis() / 650L) % count);
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

    private void renderSmallPlus(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 7, y + 7, 0xCC001A00);
        ShopGuiSupport.drawScaledString(graphics, font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_multi_reward_plus").getString(), x, y - 1, GREEN);
    }

    private boolean isSelectableRewardIcon(int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        return isSelectableRewardEntry(entry) && isHover(mouseX, mouseY, detailContentX(), contentTop() + 14, 20, 20);
    }

    private int rewardPickerX() {
        return detailContentX();
    }

    private int rewardPickerY() {
        return contentTop() + 36;
    }

    private int rewardPickerWidth() {
        return Math.max(84, left + panelWidth - 6 - detailContentX());
    }

    private int rewardPickerVisibleRows() {
        return 5;
    }

    private int rewardPickerRowHeight() {
        return 20;
    }

    private int rewardPickerHeight(Shop.Entry entry) {
        int rows = Math.min(rewardPickerVisibleRows(), entry == null || entry.rewards() == null ? 0 : entry.rewards().size());
        return 18 + rows * rewardPickerRowHeight() + 6;
    }

    private int rewardPickerMaxScroll(Shop.Entry entry) {
        if (!isSelectableRewardEntry(entry)) return 0;
        return Math.max(0, entry.rewards().size() - rewardPickerVisibleRows());
    }

    private boolean isInsideRewardPicker(int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        if (!isRewardPickerOpen() || !isSelectableRewardEntry(entry)) return false;
        int x = rewardPickerX();
        int y = rewardPickerY();
        return isHover(mouseX, mouseY, x, y, rewardPickerWidth(), rewardPickerHeight(entry));
    }

    private void renderRewardPicker(GuiGraphics graphics, Shop.Entry entry, int mouseX, int mouseY) {
        int x = rewardPickerX();
        int y = rewardPickerY();
        int width = rewardPickerWidth();
        int height = rewardPickerHeight(entry);
        renderBox(graphics, x, y, width, height, CYAN_DARK, 0xF0181818);
        graphics.drawString(font, ColorText.translatable(mode == Shop.Mode.SELL ? "gui.adventuresystems.curios.wallet.shop_sell_choice_picker_title" : "gui.adventuresystems.curios.wallet.shop_choice_picker_title"), x + 5, y + 5, CYAN, true);
        int rewardPickerMax = rewardPickerMaxScroll(entry);
        rewardPickerScroll = Math.max(0D, Math.min(rewardPickerScroll, rewardPickerMax));
        double visualRewardPickerScroll = rewardPickerScrollSmoothing.follow(rewardPickerScroll, rewardPickerMax);
        int rewardPickerStart = Math.max(0, Math.min((int) Math.floor(visualRewardPickerScroll), rewardPickerMax));
        int rewardPickerShift = (int) Math.round((visualRewardPickerScroll - rewardPickerStart) * rewardPickerRowHeight());
        int listY = y + 18;
        int selected = selectedRewardIndex(entry);
        enableCanvasScissor(graphics, x + 2, listY, x + width - 2, y + height);
        try {
            for (int row = 0; row < rewardPickerVisibleRows() + 2; row++) {
                int index = rewardPickerStart + row;
                if (index >= entry.rewards().size()) break;
                int rowY = listY + row * rewardPickerRowHeight() - rewardPickerShift;
                if (rowY + rewardPickerRowHeight() <= listY || rowY >= y + height) continue;
                boolean hover = isHover(mouseX, mouseY, x + 2, rowY, width - 4, rewardPickerRowHeight());
                boolean active = index == selected;
                graphics.fill(x + 2, rowY, x + width - 2, rowY + rewardPickerRowHeight() - 1, active ? SELECT_BG : hover ? ROW_HOVER : ROW_BG);
                Shop.Reward reward = entry.rewards().get(index);
                GuiTheme.itemSlot(graphics, reward.empty() ? ItemStack.EMPTY : reward.stack(), x + 4, rowY + 1, 18, 4, hover);
                if (!reward.empty()) {
                    graphics.renderItem(reward.stack(), x + 5, rowY + 2);
                    graphics.renderItemDecorations(font, reward.stack(), x + 5, rowY + 2);
                }
                int color = active ? GREEN : TEXT_WHITE;
                graphics.drawString(font, clipped(rewardName(reward), width - 34), x + 25, rowY + 6, color, true);
            }
        } finally {
            graphics.disableScissor();
        }
    }

    private ItemStack hoveredRewardPickerItem(int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        if (!isRewardPickerOpen() || !isSelectableRewardEntry(entry) || !isInsideRewardPicker(mouseX, mouseY)) return ItemStack.EMPTY;
        int listY = rewardPickerY() + 18;
        double visualScroll = rewardPickerScrollSmoothing.follow(rewardPickerScroll, rewardPickerMaxScroll(entry));
        int index = (int) Math.floor((mouseY - listY) / (double) rewardPickerRowHeight() + visualScroll);
        int row = (int) Math.floor((mouseY - listY) / (double) rewardPickerRowHeight());
        if (row >= rewardPickerVisibleRows() || index >= entry.rewards().size()) return ItemStack.EMPTY;
        Shop.Reward reward = entry.rewards().get(index);
        return reward.empty() ? ItemStack.EMPTY : reward.stack();
    }

    private boolean handleRewardPickerClick(int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        if (!isRewardPickerOpen() || !isSelectableRewardEntry(entry)) return false;
        if (!isInsideRewardPicker(mouseX, mouseY)) return false;
        int listY = rewardPickerY() + 18;
        if (mouseY < listY) return true;
        int row = (mouseY - listY) / rewardPickerRowHeight();
        double visualScroll = rewardPickerScrollSmoothing.follow(rewardPickerScroll, rewardPickerMaxScroll(entry));
        int index = (int) Math.floor((mouseY - listY) / (double) rewardPickerRowHeight() + visualScroll);
        if (row < rewardPickerVisibleRows() && index >= 0 && index < entry.rewards().size()) {
            selectedRewardIndices.put(entry.key(), index);
            overlayLayers.close(OverlayLayer.REWARD_PICKER);
            return true;
        }
        return true;
    }

    private void updateChoiceOverlayButtons() {
        boolean visible = isChoiceOverlayOpen() && isSelectableRewardEntry(selectedEntry());
        if (choiceOverlayCancelButton != null) {
            choiceOverlayCancelButton.setX(choiceOverlayCancelX());
            choiceOverlayCancelButton.setY(choiceOverlayButtonY());
            choiceOverlayCancelButton.setWidth(choiceOverlayButtonWidth());
            choiceOverlayCancelButton.visible = visible;
            choiceOverlayCancelButton.active = visible;
        }
        if (choiceOverlayConfirmButton != null) {
            choiceOverlayConfirmButton.setX(choiceOverlayConfirmX());
            choiceOverlayConfirmButton.setY(choiceOverlayButtonY());
            choiceOverlayConfirmButton.setWidth(choiceOverlayButtonWidth());
            choiceOverlayConfirmButton.visible = visible;
            choiceOverlayConfirmButton.active = visible;
            choiceOverlayConfirmButton.setMessage(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_choice_confirm"));
        }
    }

    private void openChoiceOverlay(Shop.Entry entry) {
        if (!isSelectableRewardEntry(entry) || mode != Shop.Mode.BUY || entry.locked()) return;
        closeContextMenu();
        overlayLayers.open(OverlayLayer.CHOICE_OVERLAY);
        choiceOverlayScroll = Math.max(0D, Math.min(choiceOverlayScroll, choiceOverlayMaxScroll(entry)));
        choiceOverlaySelectedIndex = -1;
        updateChoiceOverlayButtons();
    }

    private void closeChoiceOverlay() {
        overlayLayers.close(OverlayLayer.CHOICE_OVERLAY);
        draggingChoiceOverlayScrollbar = false;
        choiceOverlayScroll = 0;
        updateChoiceOverlayButtons();
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

    private boolean isInsideChoiceOverlay(int mouseX, int mouseY) {
        return isChoiceOverlayOpen() && isHover(mouseX, mouseY, choiceOverlayX(), choiceOverlayY(), choiceOverlayWidth(), choiceOverlayHeight());
    }

    private int choiceOverlayIndexAt(int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        if (!isChoiceOverlayOpen() || !isSelectableRewardEntry(entry)) return -1;
        int localX = mouseX - choiceGridX();
        int localY = mouseY - choiceGridY();
        if (localX < 0 || localY < 0) return -1;
        int pitch = CHOICE_OVERLAY_SLOT + CHOICE_OVERLAY_GAP;
        int column = localX / pitch;
        double visualScroll = choiceOverlayScrollSmoothing.follow(choiceOverlayScroll, choiceOverlayMaxScroll(entry));
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
        if (button != 0) return;
        Shop.Entry entry = selectedEntry();
        if (!isSelectableRewardEntry(entry)) {
            closeChoiceOverlay();
            return;
        }
        if (isHover(mouseX, mouseY, choiceOverlayCancelX(), choiceOverlayButtonY(), choiceOverlayButtonWidth(), BUTTON_HEIGHT)) {
            closeChoiceOverlay();
            return;
        }
        if (isHover(mouseX, mouseY, choiceOverlayConfirmX(), choiceOverlayButtonY(), choiceOverlayButtonWidth(), BUTTON_HEIGHT)) {
            if (choiceOverlaySelectedIndex >= 0) confirmChoicePurchase(entry);
            else GuiOverlay.toast("currency_wallet_shop_notice", ColorText.translatable("gui.adventuresystems.curios.wallet.shop_choice_need_select"), GuiOverlay.Position.BOTTOM_CENTER, 2500, 0, -30);
            return;
        }
        Scrollbar scrollbar = choiceOverlayScrollbar(entry);
        if (scrollbar.visible() && mouseX >= scrollbar.x() - 3 && mouseX <= scrollbar.x() + CHOICE_OVERLAY_SCROLLBAR_WIDTH + 3 && mouseY >= scrollbar.trackTop() && mouseY <= scrollbar.trackBottom()) {
            draggingChoiceOverlayScrollbar = true;
            if (mouseY >= scrollbar.thumbTop() && mouseY <= scrollbar.thumbBottom()) choiceOverlayScrollbarGrabOffset = mouseY - scrollbar.thumbTop();
            else {
                choiceOverlayScrollbarGrabOffset = scrollbar.thumbHeight() / 2;
                updateChoiceOverlayScrollFromMouse(mouseY);
            }
            return;
        }
        int index = choiceOverlayIndexAt(mouseX, mouseY);
        if (index >= 0) {
            choiceOverlaySelectedIndex = index;
            selectedRewardIndices.put(entry.key(), index);
            return;
        }
        if (!isInsideChoiceOverlay(mouseX, mouseY)) {
            closeChoiceOverlay();
            return;
        }
    }

    private void confirmChoicePurchase(Shop.Entry entry) {
        if (!isSelectableRewardEntry(entry)) return;
        int index = Math.max(0, Math.min(choiceOverlaySelectedIndex, entry.rewards().size() - 1));
        selectedRewardIndices.put(entry.key(), index);
        if (!canTrade(entry, amount)) {
            GuiOverlay.toast("currency_wallet_shop_notice", tradeFailText(entry), GuiOverlay.Position.BOTTOM_CENTER, 2500, 0, -30);
            return;
        }
        Network.sendShopBuy(entry.index(), Math.max(1, Math.min(64, amount)) + (index + 1) * 1000);
        closeChoiceOverlay();
    }

    private int choiceOverlayCancelX() {
        return choiceOverlayX() + 12;
    }

    private int choiceOverlayConfirmX() {
        return choiceOverlayX() + choiceOverlayWidth() - choiceOverlayButtonWidth() - 12;
    }

    private int choiceOverlayButtonY() {
        return choiceOverlayY() + 7;
    }

    private int choiceOverlayButtonWidth() {
        return 86;
    }

    private void renderChoiceOverlay(GuiGraphics graphics, int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        if (!isSelectableRewardEntry(entry)) return;
        int x = choiceOverlayX();
        int y = choiceOverlayY();
        int width = choiceOverlayWidth();
        int height = choiceOverlayHeight();
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 420);
        graphics.fill(0, 0, canvasWidth, canvasHeight, 0xDD000000);
        renderBox(graphics, x, y, width, height, CYAN_DARK, 0xFF101414);
        updateChoiceOverlayButtons();
        graphics.drawCenteredString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_choice_overlay_title"), x + width / 2, y + 13, GOLD);
        renderChoiceOverlayGrid(graphics, entry, mouseX, mouseY);
        renderChoiceOverlayScrollbar(graphics, entry, mouseX, mouseY);
        if (choiceOverlayCancelButton != null) choiceOverlayCancelButton.render(graphics, mouseX, mouseY, 0.0F);
        if (choiceOverlayConfirmButton != null) choiceOverlayConfirmButton.render(graphics, mouseX, mouseY, 0.0F);
        graphics.pose().popPose();
    }

    private void renderChoiceOverlayGrid(GuiGraphics graphics, Shop.Entry entry, int mouseX, int mouseY) {
        int gridX = choiceGridX();
        int gridY = choiceGridY();
        int pitch = CHOICE_OVERLAY_SLOT + CHOICE_OVERLAY_GAP;
        int selected = choiceOverlaySelectedIndex < 0 ? -1 : Math.max(0, Math.min(choiceOverlaySelectedIndex, entry.rewards().size() - 1));
        int choiceMax = choiceOverlayMaxScroll(entry);
        double visualChoiceScroll = choiceOverlayScrollSmoothing.follow(choiceOverlayScroll, choiceMax);
        int choiceStart = Math.max(0, Math.min((int) Math.floor(visualChoiceScroll), choiceMax));
        int choiceShift = (int) Math.round((visualChoiceScroll - choiceStart) * pitch);
        int gridHeight = CHOICE_OVERLAY_VISIBLE_ROWS * pitch - CHOICE_OVERLAY_GAP;
        enableCanvasScissor(graphics, gridX, gridY, gridX + CHOICE_OVERLAY_COLUMNS * pitch - CHOICE_OVERLAY_GAP, gridY + gridHeight);
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
                GuiTheme.itemSlot(graphics, reward.empty() ? ItemStack.EMPTY : reward.stack(), slotX, slotY, CHOICE_OVERLAY_SLOT, 4, hover);
                if (active) {
                    graphics.renderOutline(slotX, slotY, CHOICE_OVERLAY_SLOT, CHOICE_OVERLAY_SLOT, GREEN);
                }
                if (reward.empty()) {
                    graphics.drawCenteredString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_gacha_marker"), slotX + CHOICE_OVERLAY_SLOT / 2, slotY + 9, CYAN);
                } else {
                    graphics.renderItem(reward.stack(), slotX + 6, slotY + 5);
                    graphics.renderItemDecorations(font, reward.stack(), slotX + 6, slotY + 5);
                }
            }
        }
        graphics.disableScissor();
    }

    private void renderChoiceOverlayScrollbar(GuiGraphics graphics, Shop.Entry entry, int mouseX, int mouseY) {
        Scrollbar scrollbar = choiceOverlayScrollbar(entry);
        if (!scrollbar.visible()) return;
        boolean hover = isHover(mouseX, mouseY, scrollbar.x() - 2, scrollbar.thumbTop(), CHOICE_OVERLAY_SCROLLBAR_WIDTH + 4, scrollbar.thumbBottom() - scrollbar.thumbTop());
        graphics.fill(scrollbar.x(), scrollbar.trackTop(), scrollbar.x() + CHOICE_OVERLAY_SCROLLBAR_WIDTH, scrollbar.trackBottom(), SCROLLBAR_BORDER);
        graphics.fill(scrollbar.x() + 1, scrollbar.trackTop() + 1, scrollbar.x() + CHOICE_OVERLAY_SCROLLBAR_WIDTH - 1, scrollbar.trackBottom() - 1, SCROLLBAR_TRACK);
        graphics.fill(scrollbar.x(), scrollbar.thumbTop(), scrollbar.x() + CHOICE_OVERLAY_SCROLLBAR_WIDTH, scrollbar.thumbBottom(), hover || draggingChoiceOverlayScrollbar ? SCROLLBAR_HOVER : SCROLLBAR_THUMB);
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
        double visualScroll = choiceOverlayScrollSmoothing.follow(choiceOverlayScroll, max);
        int thumbTop = trackTop + (int) Math.round(travel * (visualScroll / max));
        return new Scrollbar(true, x, trackTop, trackBottom, thumbTop, thumbTop + thumbHeight);
    }

    private void updateChoiceOverlayScrollFromMouse(int mouseY) {
        Shop.Entry entry = selectedEntry();
        Scrollbar scrollbar = choiceOverlayScrollbar(entry);
        int max = choiceOverlayMaxScroll(entry);
        if (!scrollbar.visible() || max <= 0) return;
        int travel = Math.max(1, scrollbar.trackHeight() - scrollbar.thumbHeight());
        int local = Math.max(0, Math.min(travel, mouseY - scrollbar.trackTop() - choiceOverlayScrollbarGrabOffset));
        choiceOverlayScroll = Math.max(0D, Math.min(max, local * max / (double) travel));
        choiceOverlayScrollSmoothing.snap(choiceOverlayScroll, max);
    }

    private List<Component> choiceOverlayTooltipAt(int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (!isChoiceOverlayOpen()) return tooltip;
        if (isHover(mouseX, mouseY, choiceOverlayConfirmX(), choiceOverlayButtonY(), choiceOverlayButtonWidth(), BUTTON_HEIGHT)) {
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_choice_confirm").withStyle(ChatFormatting.GOLD));
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_choice_confirm_tip"));
            return tooltip;
        }
        if (isHover(mouseX, mouseY, choiceOverlayCancelX(), choiceOverlayButtonY(), choiceOverlayButtonWidth(), BUTTON_HEIGHT)) {
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_choice_cancel").withStyle(ChatFormatting.GOLD));
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_choice_cancel_tip"));
            return tooltip;
        }
        int index = choiceOverlayIndexAt(mouseX, mouseY);
        Shop.Entry entry = selectedEntry();
        if (isSelectableRewardEntry(entry) && index >= 0) {
            Shop.Reward reward = entry.rewards().get(index);
            if (!reward.empty()) {
                try {
                    tooltip.addAll(Screen.getTooltipFromItem(Minecraft.getInstance(), reward.stack()));
                } catch (Throwable ignored) {
                    tooltip.add(Component.literal(rewardName(reward)).withStyle(ChatFormatting.GOLD));
                }
            } else {
                tooltip.add(Component.literal(rewardName(reward)).withStyle(ChatFormatting.GOLD));
            }
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_choice_pick_tip").withStyle(ChatFormatting.YELLOW));
            if (reward.commandReward()) tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_reward_command_tooltip").withStyle(ChatFormatting.AQUA));
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
        if (remaining > 0L) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_timed_limit_wait", formatDuration(remaining), formatDuration(entry.timedLimitSeconds()));
        return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_timed_limit_ready", formatDuration(entry.timedLimitSeconds()));
    }

    private static String rewardName(Shop.Reward reward) {
        if (reward == null) return "";
        if (reward.displayName() != null && !reward.displayName().isBlank()) return reward.displayName();
        if (reward.empty()) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_gacha_empty").getString();
        return ShopGuiSupport.stackNameWithCount(reward.stack());
    }

    private static String percent(double value) {
        return String.format(Locale.ROOT, "%.2f%%", value);
    }

    @Override
    protected boolean canvasMouseReleased(
            double mouseX,
            double mouseY,
            int button
    ) {
        OverlayLayer activeLayer =
                overlayLayers.activeLayer();

        if (activeLayer != null) {
            if (button == 0) {
                draggingQuestPickerScrollbar = false;
                draggingChoiceOverlayScrollbar = false;
            }

            return true;
        }

        if (button == 0
                && dragSourceEntry != null) {
            finishEntryDrag(
                    (int) mouseX,
                    (int) mouseY
            );
            return true;
        }

        if (button == 0
                && (draggingScrollbar
                || draggingRewardPreviewScrollbar
                || draggingAmountSlider
                || draggingPageScrollbar)) {
            draggingScrollbar = false;
            draggingRewardPreviewScrollbar = false;
            draggingAmountSlider = false;
            draggingPageScrollbar = false;
            return true;
        }

        return super.canvasMouseReleased(
                mouseX,
                mouseY,
                button
        );
    }

    @Override
    protected boolean canvasMouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY
    ) {
        OverlayLayer activeLayer =
                overlayLayers.activeLayer();

        if (activeLayer != null) {
            if (button == 0
                    && activeLayer == OverlayLayer.CHOICE_OVERLAY
                    && draggingChoiceOverlayScrollbar) {
                updateChoiceOverlayScrollFromMouse(
                        (int) mouseY
                );
            } else if (button == 0
                    && activeLayer == OverlayLayer.QUEST_PICKER
                    && draggingQuestPickerScrollbar) {
                updateQuestPickerScrollFromMouse(
                        (int) mouseY
                );
            }

            return true;
        }

        if (button == 0
                && dragSourceEntry != null) {
            updateEntryDrag(
                    (int) mouseX,
                    (int) mouseY
            );
            return true;
        }

        if (button == 0
                && draggingScrollbar) {
            updateScrollFromMouse(
                    (int) mouseY
            );
            return true;
        }

        if (button == 0
                && draggingRewardPreviewScrollbar) {
            updateRewardPreviewScrollFromMouse(
                    (int) mouseY
            );
            return true;
        }

        if (button == 0
                && draggingAmountSlider) {
            updateAmountFromMouse(
                    (int) mouseX
            );
            return true;
        }

        if (button == 0
                && draggingPageScrollbar) {
            updatePageScrollFromMouse(
                    (int) mouseX
            );
            return true;
        }

        return super.canvasMouseDragged(
                mouseX,
                mouseY,
                button,
                dragX,
                dragY
        );
    }

    @Override
    protected boolean canvasMouseScrolled(
            double mouseX,
            double mouseY,
            double delta
    ) {
        OverlayLayer activeLayer =
                overlayLayers.activeLayer();

        if (activeLayer != null) {
            switch (activeLayer) {
                case CHOICE_OVERLAY -> {
                    Shop.Entry entry = selectedEntry();
                    choiceOverlayScroll = choiceOverlayScrollSmoothing.wheel(
                            choiceOverlayScroll, delta, 1.0D / 3.0D, choiceOverlayMaxScroll(entry)
                    );
                }

                case QUEST_PICKER -> {
                    if (isInsideQuestPicker(
                            (int) mouseX,
                            (int) mouseY
                    )) {
                        Shop.Entry entry = selectedEntry();
                        questPickerScroll = questPickerScrollSmoothing.wheel(
                                questPickerScroll, delta, 1.0D / 3.0D, questPickerMaxScroll(entry)
                        );
                    }
                }

                case REWARD_PICKER -> {
                    if (isInsideRewardPicker(
                            (int) mouseX,
                            (int) mouseY
                    )) {
                        Shop.Entry entry = selectedEntry();
                        rewardPickerScroll = rewardPickerScrollSmoothing.wheel(
                                rewardPickerScroll, delta, 1.0D / 3.0D, rewardPickerMaxScroll(entry)
                        );
                    }
                }

                case CONTEXT_MENU -> {
                }
            }

            return true;
        }

        if (isInsideRewardPreview((int) mouseX, (int) mouseY)) {
            Shop.Entry entry = selectedEntry();
            int previewY = rewardPreviewY();
            if (entry != null && previewY >= 0) {
                rewardPreviewScroll = rewardPreviewScrollSmoothing.wheel(
                        rewardPreviewScroll, delta, 1.0D / 3.0D, rewardPreviewMaxScroll(entry, previewY)
                );
            }
            return true;
        }
        int tabY = pageTabsY();
        if (mouseY >= tabY && mouseY <= pageScrollBarY() + PAGE_SCROLLBAR_HEIGHT + 4 && mouseX >= listLeft() && mouseX <= pageRightArrowX() + PAGE_TAB_ARROW_WIDTH) {
            pageScroll = Math.max(0, Math.min(maxPageScroll(), pageScroll - Math.round((float) delta * 28.0F)));
            refreshPageButtons();
            return true;
        }
        if (isHover((int) mouseX, (int) mouseY, listLeft(), contentTop(), listWidth(), contentHeightVisible())) {
            scroll = Math.max(0, Math.min(maxScroll(), scroll - Math.round((float) delta * 18.0F)));
            refreshProductButtons();
            return true;
        }
        return super.canvasMouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void openNewEditor() {
        Minecraft.getInstance().setScreen(new ShopEntryEditorScreen(this, new ShopGuiSupport.EditorDraft(mode)));
    }

    private void openEditEditor(Shop.Entry entry) {
        Minecraft.getInstance().setScreen(new ShopEntryEditorScreen(this, new ShopGuiSupport.EditorDraft(entry)));
    }

    private void openCopyEditor(Shop.Entry entry) {
        if (entry == null) return;
        ShopGuiSupport.EditorDraft draft = new ShopGuiSupport.EditorDraft(entry);
        draft.index = -1;
        Minecraft.getInstance().setScreen(new ShopEntryEditorScreen(this, draft));
    }

    private void createContextMenuButtons() {
        addContextButton("gui.adventuresystems.curios.wallet.shop_context_favorite", this::toggleContextFavorite);
        addContextButton("gui.adventuresystems.curios.wallet.shop_context_edit", this::editContextEntry);
        addContextButton("gui.adventuresystems.curios.wallet.shop_context_copy", this::copyContextEntry);
        addContextButton("gui.adventuresystems.curios.wallet.shop_context_move_front", () -> moveContextEntry(0));
        addContextButton("gui.adventuresystems.curios.wallet.shop_context_move_up", () -> moveContextEntry(contextEntry == null ? 0 : contextEntry.index() - 1));
        addContextButton("gui.adventuresystems.curios.wallet.shop_context_move_down", () -> moveContextEntry(contextEntry == null ? 0 : contextEntry.index() + 1));
        addContextButton("gui.adventuresystems.curios.wallet.shop_context_move_end", () -> moveContextEntry(Integer.MAX_VALUE));
        addContextButton("gui.adventuresystems.curios.wallet.shop_context_delete", this::deleteContextEntry);
        updateContextMenuButtons();
    }

    private void addContextButton(String translationKey, Runnable action) {
        Button button = Button.builder(ColorText.translatable(translationKey), value -> action.run())
                .bounds(0, 0, CONTEXT_MENU_WIDTH, CONTEXT_MENU_BUTTON_HEIGHT)
                .build();
        button.visible = false;
        button.active = false;
        contextButtons.add(button);
    }

    private void openContextMenu(RowClick click, int mouseX, int mouseY) {
        if (click == null || click.row().entry() == null) return;
        contextEntry = click.row().entry();
        selectRow(click.index());
        int height = contextMenuHeight();
        contextMenuX = Math.max(listLeft(), Math.min(mouseX, left + panelWidth - CONTEXT_MENU_WIDTH - 6));
        contextMenuY = Math.max(contentTop(), Math.min(mouseY, top + panelHeight - height - 6));
        draggingQuestPickerScrollbar = false;
        draggingChoiceOverlayScrollbar = false;
        overlayLayers.open(OverlayLayer.CONTEXT_MENU);
        updateChoiceOverlayButtons();
        updateContextMenuButtons();
    }

    private void closeContextMenu() {
        overlayLayers.close(OverlayLayer.CONTEXT_MENU);
        contextEntry = null;
        updateContextMenuButtons();
    }

    private void updateContextMenuButtons() {
        int currentIndex = contextEntry == null ? -1 : contextEntry.index();
        int lastIndex = Math.max(0, modeEntryCount() - 1);
        int visibleSlot = 0;
        for (int i = 0; i < contextButtons.size(); i++) {
            Button button = contextButtons.get(i);
            boolean visible = isContextMenuOpen() && contextEntry != null && (i == 0 || editorMode);
            button.visible = visible;
            button.active = visible;
            if (!visible) continue;
            button.setX(contextMenuX);
            button.setY(contextMenuY + visibleSlot * (CONTEXT_MENU_BUTTON_HEIGHT + CONTEXT_MENU_GAP));
            button.setWidth(CONTEXT_MENU_WIDTH);
            if (i == 0) {
                button.setMessage(ColorText.translatable(isFavorite(contextEntry)
                        ? "gui.adventuresystems.curios.wallet.shop_context_unfavorite"
                        : "gui.adventuresystems.curios.wallet.shop_context_favorite"));
            }
            if ((i == 3 || i == 4) && currentIndex <= 0) button.active = false;
            if ((i == 5 || i == 6) && currentIndex >= lastIndex) button.active = false;
            visibleSlot++;
        }
        for (ProductButton button : productButtons) {
            button.active = button.visible && !overlayLayers.isAnyOpen() && dragSourceEntry == null;
        }
    }

    private int contextMenuHeight() {
        int count = editorMode ? contextButtons.size() : Math.min(1, contextButtons.size());
        return count * CONTEXT_MENU_BUTTON_HEIGHT + Math.max(0, count - 1) * CONTEXT_MENU_GAP;
    }

    private int modeEntryCount() {
        String key = mode == Shop.Mode.BUY ? "Buy" : "Sell";
        return shopTag == null ? 0 : shopTag.getList(key, Tag.TAG_COMPOUND).size();
    }

    private void handleContextMenuClick(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (Button contextButton : contextButtons) {
                if (contextButton.visible && contextButton.mouseClicked(mouseX, mouseY, button)) return;
            }
        }
        closeContextMenu();
    }

    private void toggleContextFavorite() {
        Shop.Entry entry = contextEntry;
        if (entry == null) {
            closeContextMenu();
            return;
        }
        boolean favorite = ShopClientPreferences.toggleFavorite(entry.mode(), entry.key());
        closeContextMenu();
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
        closeContextMenu();
        if (entry != null) openEditEditor(entry);
    }

    private void copyContextEntry() {
        Shop.Entry entry = contextEntry;
        closeContextMenu();
        if (entry != null) openCopyEditor(entry);
    }

    private void moveContextEntry(int targetIndex) {
        Shop.Entry entry = contextEntry;
        closeContextMenu();
        if (entry != null) Network.sendMoveShopEntry(entry.mode(), entry.index(), targetIndex);
    }

    private void deleteContextEntry() {
        Shop.Entry entry = contextEntry;
        closeContextMenu();
        if (entry != null) {
            Network.sendRemoveShopEntry(entry.mode(), entry.index());
            selectedIndex = -1;
            updateDetailWidgetState();
        }
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
        return ColorText.translatable(key);
    }

    private Component editorModeButtonText() {
        if (!canEdit) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_mode_locked");
        return ColorText.translatable(editorMode
                ? "gui.adventuresystems.curios.wallet.shop_editor_mode_on"
                : "gui.adventuresystems.curios.wallet.shop_editor_mode_off");
    }

    private void adjustAmount(int delta) {
        setAmount(Math.max(1, Math.min(64, amount + delta)));
    }

    private void setAmount(int value) {
        amount = Math.max(1, Math.min(64, value));
        if (amountBox != null) amountBox.setValue(String.valueOf(amount));
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
            GuiOverlay.toast("currency_wallet_shop_notice", ColorText.translatable("gui.adventuresystems.curios.wallet.shop_invalid_trade_amount"), GuiOverlay.Position.BOTTOM_CENTER, 2500, 0, -30);
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

    private void updateAmountFromMouse(int mouseX) {
        int x = detailAmountSliderX();
        int local = Math.max(0, Math.min(DETAIL_AMOUNT_SLIDER_WIDTH, mouseX - x));
        amount = Math.max(1, Math.min(64, Math.round(local * 64.0f / DETAIL_AMOUNT_SLIDER_WIDTH)));
        if (amountBox != null) amountBox.setValue(String.valueOf(amount));
    }

    private void updateSmoothScrolling() {
        int maxScroll = maxScroll();
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        float scrollDifference = scroll - smoothScroll;
        if (Math.abs(scrollDifference) < 0.1F) smoothScroll = scroll;
        else smoothScroll += scrollDifference * 0.28F;

        int maxPageScroll = maxPageScroll();
        pageScroll = Math.max(0, Math.min(pageScroll, maxPageScroll));
        if (!draggingPageScrollbar) {
            float pageDifference = pageScroll - smoothPageScroll;
            if (Math.abs(pageDifference) < 0.05F) smoothPageScroll = pageScroll;
            else smoothPageScroll += pageDifference * 0.22F;
        }
        smoothPageScroll = Math.max(0.0F, Math.min(smoothPageScroll, maxPageScroll));
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
        favoritesPageButton.setX(listLeft());
        favoritesPageButton.setY(y);
        favoritesPageButton.setWidth(favoritesWidth);
        favoritesPageButton.setMessage(pageButtonLabel(FAVORITES_PAGE));
        allPageButton.setX(listLeft() + favoritesWidth + PAGE_TAB_GAP);
        allPageButton.setY(y);
        allPageButton.setWidth(allWidth);
        allPageButton.setMessage(pageButtonLabel(ALL_PAGE));

        int slot = 0;
        int leftArrowX = fixedPageTabsEndX();
        pagePrevButton.setX(leftArrowX);
        pagePrevButton.setY(y);
        pagePrevButton.setWidth(PAGE_TAB_ARROW_WIDTH);
        pagePrevButton.active = pageScroll > 0;
        pagePrevButton.visible = true;

        pageNextButton.setX(rightArrowX);
        pageNextButton.setY(y);
        pageNextButton.setWidth(PAGE_TAB_ARROW_WIDTH);
        pageNextButton.visible = true;
        pageNextButton.active = pageScroll < maxPageScroll();

        int viewportLeft = categoryViewportLeft();
        int viewportRight = categoryViewportRight();
        float offset = smoothPageScroll;
        int contentX = 0;
        for (String page : pages) {
            int width = pageButtonWidthForPage(page);
            float buttonLeft = viewportLeft + contentX - offset;
            float buttonRight = buttonLeft + width;
            if (buttonRight > viewportLeft && buttonLeft < viewportRight && slot < pageButtons.size()) {
                bindPageButton(slot++, page, Math.round(buttonLeft), y);
            }
            contentX += width + PAGE_TAB_GAP;
        }
        for (int i = slot; i < pageButtons.size(); i++) {
            PageTabButton button = pageButtons.get(i);
            button.unbind();
        }
    }

    private void bindPageButton(int slot, String page, int x, int y) {
        if (slot < 0 || slot >= pageButtons.size()) return;
        PageTabButton button = pageButtons.get(slot);
        Component label = pageButtonLabel(page);
        int width = pageButtonWidthForPage(page);
        button.bind(x, y, width, label);
        visiblePageButtonPages.add(page);
    }

    private Component pageButtonLabel(String page) {
        Component label = pageLabel(page);
        if (Objects.equals(page, activePageName)) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_page_active_prefix", label);
        return label;
    }

    private int pageButtonWidth(Component label) {
        return Math.max(36, Math.min(126, font.width(label) + 18));
    }

    private int pageButtonWidthForPage(String page) {
        Component normal = pageLabel(page);
        Component active = ColorText.translatable("gui.adventuresystems.curios.wallet.shop_page_active_prefix", normal);
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
        if (searchBox != null) searchBox.setValue("");
        selectedIndex = -1;
        overlayLayers.closeAll();
        rewardPickerScroll = 0;
        questPickerScroll = 0;
        choiceOverlayScroll = 0;
        draggingQuestPickerScrollbar = false;
        draggingChoiceOverlayScrollbar = false;
        resetScrollImmediately();
        closeContextMenu();
        clearDragState();
        rebuildRows();
        updateDetailWidgetState();
        clearTextFocus();
    }

    private String pageAt(int mouseX, int mouseY) {
        if (favoritesPageButton != null && favoritesPageButton.visible && favoritesPageButton.isMouseOver(mouseX, mouseY)) return FAVORITES_PAGE;
        if (allPageButton != null && allPageButton.visible && allPageButton.isMouseOver(mouseX, mouseY)) return ALL_PAGE;
        for (int i = 0; i < visiblePageButtonPages.size() && i < pageButtons.size(); i++) {
            PageTabButton button = pageButtons.get(i);
            if (button.visible && button.isMouseOver(mouseX, mouseY)) return visiblePageButtonPages.get(i);
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
        double visualScroll = questPickerScrollSmoothing.follow(questPickerScroll, questPickerMaxScroll(entry));
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
            questPickerScroll = Math.max(0D, Math.min(questPickerScroll, questPickerMaxScroll(entry)));
            updateChoiceOverlayButtons();
            updateContextMenuButtons();
            return;
        }
        openKtQuest(entry.requiredQuestId());
    }

    private Component questButtonText(Shop.Entry entry) {
        if (!hasQuestList(entry)) return Component.empty();
        boolean done = !entry.locked();
        Component status = done
                ? ColorText.translatable("gui.adventuresystems.curios.wallet.shop_task_requirement_done")
                : ColorText.translatable("gui.adventuresystems.curios.wallet.shop_task_requirement_need", missingRequiredQuestCount(entry));
        String title = hasMultipleQuests(entry) ? ColorText.translatable("gui.adventuresystems.curios.wallet.shop_task_multiple", entry.requiredQuestIds().size()).getString() : questTitleById(entry, entry.requiredQuestId());
        return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_task_button", status, clipped(title, Math.max(40, questButtonWidth() - font.width(status) - 24)));
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

    private void renderQuestPicker(GuiGraphics graphics, Shop.Entry entry, int mouseX, int mouseY) {
        int x = questPickerX();
        int y = questPickerY();
        int width = questPickerWidth();
        int height = questPickerHeight(entry);
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 240);
        graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF000000);
        renderBox(graphics, x, y, width, height, CYAN_DARK, 0xFF050505);
        graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_task_list_title"), x + 5, y + 5, CYAN, true);
        int questMax = questPickerMaxScroll(entry);
        questPickerScroll = Math.max(0D, Math.min(questPickerScroll, questMax));
        double visualQuestScroll = questPickerScrollSmoothing.follow(questPickerScroll, questMax);
        int questStart = Math.max(0, Math.min((int) Math.floor(visualQuestScroll), questMax));
        int questShift = (int) Math.round((visualQuestScroll - questStart) * QUEST_PICKER_ROW_HEIGHT);
        int listY = y + 18;
        boolean hasScrollbar = questPickerScrollbarVisible(entry);
        int rowRight = x + width - (hasScrollbar ? LIST_SCROLLBAR_WIDTH + 7 : 4);
        enableCanvasScissor(graphics, x + 2, listY, rowRight, y + height);
        try {
            for (int row = 0; row < QUEST_PICKER_VISIBLE_ROWS + 2; row++) {
                int index = questStart + row;
                if (index >= entry.requiredQuestIds().size()) break;
                long questId = entry.requiredQuestIds().get(index);
                boolean completed = entry.isQuestCompleted(questId);
                int rowY = listY + row * QUEST_PICKER_ROW_HEIGHT - questShift;
                if (rowY + QUEST_PICKER_ROW_HEIGHT <= listY || rowY >= y + height) continue;
                boolean hover = isHover(mouseX, mouseY, x + 2, rowY, rowRight - x - 2, QUEST_PICKER_ROW_HEIGHT);
                int rowBorder = hover ? CYAN : completed ? CYAN_DARK : 0xFF3A4A52;
                int rowFill = completed ? hover ? 0xFF1D3320 : 0xFF142414 : hover ? 0xFF263238 : 0xFF171717;
                int rowText = completed ? GREEN : hover ? TEXT_WHITE : LIMIT_TIME;
                renderBox(graphics, x + 2, rowY, rowRight - x - 2, QUEST_PICKER_ROW_HEIGHT - 1, rowBorder, rowFill);
                int titleX = x + 6;
                int titleY = rowY + 6;
                int titleWidth = Math.max(12, rowRight - 6 - titleX);
                if (titleWidth > 12) drawScrollingQuestTitle(graphics, questTitleById(entry, questId), titleX, titleY, titleWidth, rowText);
            }
        } finally {
            graphics.disableScissor();
        }
        renderQuestPickerScrollbar(graphics, entry, mouseX, mouseY);
        graphics.pose().popPose();
    }

    private void drawScrollingQuestTitle(GuiGraphics graphics, String text, int x, int y, int width, int color) {
        if (text == null || text.isBlank() || width <= 0) return;
        int textWidth = font.width(text);
        if (textWidth <= width) {
            graphics.drawString(font, text, x, y, color, true);
            return;
        }
        enableCanvasScissor(graphics, x, y - 1, x + width, y + 10);
        try {
            int overflow = textWidth - width;
            long time = System.currentTimeMillis();
            double phase = (time % 5000L) / 5000.0D;
            int offset = (int) Math.round((Math.sin(phase * Math.PI * 2.0D - Math.PI / 2.0D) + 1.0D) * 0.5D * overflow);
            graphics.drawString(font, text, x - offset, y, color, true);
        } finally {
            graphics.disableScissor();
        }
    }

    private boolean handleQuestPickerClick(int mouseX, int mouseY) {
        Shop.Entry entry = selectedEntry();
        if (!isQuestPickerOpen() || !hasQuestList(entry)) return false;
        if (!isInsideQuestPicker(mouseX, mouseY)) return false;
        if (isInsideQuestPickerScrollbar(mouseX, mouseY)) return true;
        int listY = questPickerY() + 18;
        if (mouseY < listY) return true;
        int row = (mouseY - listY) / QUEST_PICKER_ROW_HEIGHT;
        double visualScroll = questPickerScrollSmoothing.follow(questPickerScroll, questPickerMaxScroll(entry));
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
        double visualScroll = questPickerScrollSmoothing.follow(questPickerScroll, maxScroll);
        int thumbTop = barTop + (int) Math.round(visualScroll * Math.max(0, trackHeight - thumbHeight) / Math.max(1, maxScroll));
        return new Scrollbar(true, barX, barTop, barBottom, thumbTop, thumbTop + thumbHeight);
    }

    private boolean isInsideQuestPickerScrollbar(int mouseX, int mouseY) {
        Scrollbar scrollbar = questPickerScrollbar(selectedEntry());
        return scrollbar.visible() && mouseX >= scrollbar.x() - 2 && mouseX <= scrollbar.x() + LIST_SCROLLBAR_WIDTH + 2 && mouseY >= scrollbar.trackTop() && mouseY <= scrollbar.trackBottom();
    }

    private void renderQuestPickerScrollbar(GuiGraphics graphics, Shop.Entry entry, int mouseX, int mouseY) {
        Scrollbar scrollbar = questPickerScrollbar(entry);
        if (!scrollbar.visible()) return;
        boolean hover = isHover(mouseX, mouseY, scrollbar.x() - 2, scrollbar.thumbTop(), LIST_SCROLLBAR_WIDTH + 4, scrollbar.thumbBottom() - scrollbar.thumbTop());
        graphics.fill(scrollbar.x(), scrollbar.trackTop(), scrollbar.x() + LIST_SCROLLBAR_WIDTH, scrollbar.trackBottom(), SCROLLBAR_BORDER);
        graphics.fill(scrollbar.x() + 1, scrollbar.trackTop() + 1, scrollbar.x() + LIST_SCROLLBAR_WIDTH - 1, scrollbar.trackBottom() - 1, SCROLLBAR_TRACK);
        graphics.fill(scrollbar.x(), scrollbar.thumbTop(), scrollbar.x() + LIST_SCROLLBAR_WIDTH, scrollbar.thumbBottom(), hover || draggingQuestPickerScrollbar ? SCROLLBAR_HOVER : SCROLLBAR_THUMB);
    }

    private void updateQuestPickerScrollFromMouse(int mouseY) {
        Shop.Entry entry = selectedEntry();
        Scrollbar scrollbar = questPickerScrollbar(entry);
        if (!scrollbar.visible()) return;
        int travel = scrollbar.trackHeight() - scrollbar.thumbHeight();
        int local = mouseY - scrollbar.trackTop() - questPickerScrollbarGrabOffset;
        local = Math.max(0, Math.min(travel, local));
        questPickerScroll = local * questPickerMaxScroll(entry) / (double) Math.max(1, travel);
        questPickerScrollSmoothing.snap(questPickerScroll, questPickerMaxScroll(entry));
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

    private boolean isInsideDetailAmountSlider(double mouseX, double mouseY) {
        int x = detailAmountSliderX();
        int y = detailAmountSliderY();
        return detailTradeControlsVisible(selectedEntry()) && mouseX >= x && mouseX <= x + DETAIL_AMOUNT_SLIDER_WIDTH && mouseY >= y - 4 && mouseY <= y + 12;
    }

    private void toggleRewardPreviewSection() {
        Shop.Entry entry = selectedEntry();
        if (entry == null || !entry.gacha()) return;
        rewardPreviewExpanded = !rewardPreviewExpanded;
        rewardPreviewScroll = 0;
        draggingRewardPreviewScrollbar = false;
        updateDetailWidgetState();
    }

    private void clearTextFocus() {
        setFocused(null);
        if (amountBox != null) amountBox.setFocused(false);
        if (searchBox != null) searchBox.setFocused(false);
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
        return left + 8;
    }

    private int sellButtonX() {
        return buyButtonX() + TOP_BUTTON_WIDTH + 4;
    }

    private int editorModeButtonX() {
        return editorMode
                ? newEntryButtonX() + NEW_BUTTON_WIDTH + 4
                : sellButtonX() + TOP_BUTTON_WIDTH + 4;
    }

    private int newEntryButtonX() {
        return sellButtonX() + TOP_BUTTON_WIDTH + 4;
    }

    private int closeButtonX() {
        return left + panelWidth - 8 - CLOSE_BUTTON_WIDTH;
    }

    private int backButtonX() {
        return closeButtonX() - 4 - BACK_BUTTON_WIDTH;
    }

    private int rsButtonX() {
        return backButtonX() - 6 - RS_BUTTON_WIDTH;
    }

    private int backpackButtonX() {
        return rsButtonX() - 4 - BACKPACK_BUTTON_WIDTH;
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

    private int searchBoxWidth() {
        return SEARCH_BOX_WIDTH;
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
        int desired = detailContentX() + font.width(tradeAmountLabel()) + 5;
        return Math.min(left + panelWidth - DETAIL_AMOUNT_INPUT_SIZE - 6, Math.max(detailContentX(), desired));
    }

    private int detailTradeCostTextX() {
        return detailAmountInputX() + DETAIL_AMOUNT_INPUT_SIZE + 4;
    }

    private int detailTradeCostIconX(Shop.Entry entry) {
        Component text = ColorText.translatable("gui.adventuresystems.curios.wallet.shop_trade_cost_preview", formatCompact(tradeCostAmount(entry)));
        return Math.min(left + panelWidth - DETAIL_PRICE_SLOT_SIZE - 7, detailTradeCostTextX() + font.width(text) + 3);
    }

    private int detailTradeCostIconY() {
        return detailAmountInputY() + 1;
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

        if (entry.totalLimit() > 0
                && totalRemaining(entry) <= 1) {
            return false;
        }

        return true;
    }

    private int detailAmountInputY() {
        return detailAmountSliderY() - 20;
    }

    private int detailAmountSliderX() {
        return detailContentX();
    }

    private int detailAmountSliderY() {
        return detailAmountQuickButtonY() - 9;
    }

    private int detailAmountQuickButtonWidth() {
        return Math.max(34, (detailContentWidth() - 12) / 4);
    }

    private int detailAmountQuickButtonX(int slot) {
        return detailContentX() + Math.max(0, Math.min(3, slot)) * (detailAmountQuickButtonWidth() + 4);
    }

    private int detailAmountQuickButtonY() {
        return detailTradeButtonY() - 22;
    }

    private int detailTradeButtonX() {
        return detailContentX();
    }

    private int detailTradeButtonY() {
        return detailBottom() - BUTTON_HEIGHT + 6;
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
        double visualScroll = rewardPreviewScrollSmoothing.follow(rewardPreviewScroll, maxScroll);
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
        Component label = ColorText.translatable("gui.adventuresystems.curios.wallet.balance_title");
        int cellStartX = x + 8 + font.width(label) + 8;
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
            if (!entryBelongsToActivePage(entry)) continue;
            if (!query.isEmpty() && !matchesSearch(entry, query)) continue;
            rows.add(new Row(entry));
        }
        if (selectedIndex >= rows.size()) selectedIndex = -1;
        scroll = Math.max(0, Math.min(scroll, maxScroll()));
        smoothScroll = Math.max(0.0F, Math.min(smoothScroll, maxScroll()));
        smoothPageScroll = Math.max(0.0F, Math.min(smoothPageScroll, maxPageScroll()));
        refreshPageButtons();
        refreshProductButtons();
    }

    private boolean entryBelongsToActivePage(Shop.Entry entry) {
        if (entry == null) return false;
        if (Objects.equals(activePageName, ALL_PAGE)) return true;
        if (Objects.equals(activePageName, FAVORITES_PAGE)) return isFavorite(entry);
        return Objects.equals(pageName(entry), activePageName);
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

    private boolean matchesSearch(Shop.Entry entry, String query) {
        if (query == null || query.isBlank()) return true;
        String text = searchText(entry);
        String[] tokens = query.trim().toLowerCase(Locale.ROOT).split("\\s+");
        for (String token : tokens) {
            if (token.isBlank()) continue;
            if (token.startsWith("@")) {
                if (!matchesModToken(entry, token.substring(1))) return false;
                continue;
            }
            if (token.startsWith("#")) {
                if (!matchesPageToken(entry, token.substring(1))) return false;
                continue;
            }
            if (!KineticSearch.match(text, token)) return false;
        }
        return true;
    }

    private boolean matchesModToken(Shop.Entry entry, String token) {
        if (entry == null || token == null || token.isBlank()) return true;
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(entry.stack().getItem());
        String itemNamespace = itemId == null ? "" : itemId.getNamespace().toLowerCase(Locale.ROOT);
        String currencyNamespace = namespaceOf(entry.currencyId());
        if (itemNamespace.contains(token) || currencyNamespace.contains(token)) return true;
        if (entry.rewards() != null) {
            for (Shop.Reward reward : entry.rewards()) {
                if (reward == null || reward.empty()) continue;
                ResourceLocation rewardId = ForgeRegistries.ITEMS.getKey(reward.stack().getItem());
                if (rewardId != null && rewardId.getNamespace().toLowerCase(Locale.ROOT).contains(token)) return true;
            }
        }
        return false;
    }

    private boolean matchesPageToken(Shop.Entry entry, String token) {
        if (entry == null || token == null || token.isBlank()) return true;
        String page = pageLabel(entry).getString().toLowerCase(Locale.ROOT);
        String pageSearch = page + " " + KineticSearch.pinyin(page);
        return KineticSearch.match(pageSearch, token);
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
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item.getItem());
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
                ResourceLocation rewardId = ForgeRegistries.ITEMS.getKey(reward.stack().getItem());
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
        if (Objects.equals(page, FAVORITES_PAGE)) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_page_favorites");
        if (Objects.equals(page, ALL_PAGE)) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_page_all");
        if (page == null || page.isBlank()) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_page_default");
        return Component.literal(page);
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
        rewardPreviewScroll = 0;
        draggingRewardPreviewScrollbar = false;
        questPickerScroll = 0;
        draggingQuestPickerScrollbar = false;
        choiceOverlayScroll = 0;
        choiceOverlaySelectedIndex = -1;
        draggingChoiceOverlayScrollbar = false;
        amount = 1;
        if (amountBox != null) amountBox.setValue("1");
        updateDetailWidgetState();
    }

    private void selectRowByKey(String key) {
        if (key == null || key.isBlank()) return;
        for (int i = 0; i < rows.size(); i++) {
            Shop.Entry entry = rows.get(i).entry();
            if (entry != null && Objects.equals(key, entry.key())) {
                selectedIndex = i;
                rewardPreviewExpanded = false;
                        rewardPreviewScroll = 0;
                                updateDetailWidgetState();
                return;
            }
        }
    }

    private void refreshProductButtons() {
        int start = visibleIndexStart();
        int end = visibleIndexEnd();
        int slot = 0;
        for (int i = start; i < end && slot < productButtons.size(); i++, slot++) {
            ProductButton button = productButtons.get(slot);
            Cell cell = cellForIndex(i);
            button.bind(i, cell);
            button.visible = cell.y() + GRID_CELL_HEIGHT > contentTop()
                    && cell.y() < contentTop() + contentHeightVisible();
            button.active = button.visible && !overlayLayers.isAnyOpen() && dragSourceEntry == null;
        }
        for (int i = slot; i < productButtons.size(); i++) {
            productButtons.get(i).unbind();
        }
    }

    private void beginEntryDrag(RowClick click, int mouseX, int mouseY) {
        if (click == null || click.row().entry() == null) return;
        closeContextMenu();
        selectRow(click.index());
        dragSourceEntry = click.row().entry();
        dragTargetEntry = dragSourceEntry;
        dragMouseX = mouseX;
        dragMouseY = mouseY;
        updateContextMenuButtons();
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
        refreshProductButtons();
    }

    private void finishEntryDrag(int mouseX, int mouseY) {
        updateEntryDrag(mouseX, mouseY);
        Shop.Entry source = dragSourceEntry;
        Shop.Entry target = dragTargetEntry;
        clearDragState();
        if (source == null || target == null || source.index() == target.index()) return;
        Network.sendMoveShopEntry(source.mode(), source.index(), target.index());
    }

    private void clearDragState() {
        dragSourceEntry = null;
        dragTargetEntry = null;
        dragMouseX = 0;
        dragMouseY = 0;
        refreshProductButtons();
    }

    private void renderDragPreview(GuiGraphics graphics) {
        if (dragSourceEntry == null) return;
        if (dragTargetEntry != null) {
            for (int i = visibleIndexStart(); i < visibleIndexEnd(); i++) {
                Shop.Entry entry = rows.get(i).entry();
                if (entry == null || entry.index() != dragTargetEntry.index()) continue;
                Cell target = cellForIndex(i);
                renderSelectionOutline(graphics, target.x() - 1, target.y() - 1, GRID_CELL_WIDTH + 2, GRID_CELL_HEIGHT + 2);
                break;
            }
        }
        int x = Math.max(listLeft(), Math.min(dragMouseX - GRID_CELL_WIDTH / 2, listLeft() + listWidth() - GRID_CELL_WIDTH));
        int y = Math.max(contentTop(), Math.min(dragMouseY - GRID_CELL_HEIGHT / 2, contentTop() + contentHeightVisible() - GRID_CELL_HEIGHT));
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 500.0F);
        renderDraggedCell(graphics, dragSourceEntry, new Cell(x, y));
        graphics.pose().popPose();
    }

    private void renderDraggedCell(GuiGraphics graphics, Shop.Entry entry, Cell cell) {
        boolean canTrade = canTrade(entry, 1);
        int border = alphaColor(canTrade ? GREEN_DARK : DEEP_RED, 190);
        renderBox(graphics, cell.x(), cell.y(), GRID_CELL_WIDTH, GRID_CELL_HEIGHT, border, alphaColor(ROW_BG, 184));
        int itemX = cellItemSlotX(cell);
        int itemY = cellItemSlotY(cell);
        renderItemCheckerSlot(graphics, itemX, itemY, PRODUCT_SLOT_SIZE);
        graphics.setColor(1.0F, 1.0F, 1.0F, 0.72F);
        renderProductItem(graphics, cellDisplayStack(entry), itemX + 1, itemY + 1);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        Component primary = cellPrimaryStatusText(entry);
        Component limit = cellLimitStatusText(entry);
        int minX = itemX + PRODUCT_SLOT_SIZE + 3;
        int available = Math.max(4, cell.x() + GRID_CELL_WIDTH - 3 - minX);
        int textY = cell.y() + 2;
        if (primary != null && limit != null) {
            String primaryText = primary.getString();
            int primaryWidth = Math.min(font.width(primaryText), Math.max(8, available / 2));
            drawCellString(graphics, primaryText, minX, textY, primaryWidth, alphaColor(cellPrimaryStatusColor(entry, canTrade), 190));
            int limitX = minX + primaryWidth + 3;
            drawCellString(graphics, limit.getString(), limitX, textY, Math.max(1, available - primaryWidth - 3), alphaColor(cellLimitStatusColor(entry), 190));
        } else if (primary != null) {
            drawCellString(graphics, primary.getString(), minX, textY, available, alphaColor(cellPrimaryStatusColor(entry, canTrade), 190));
        } else if (limit != null) {
            drawCellString(graphics, limit.getString(), minX, textY, available, alphaColor(cellLimitStatusColor(entry), 190));
        }

        String price = formatCompact(entry.price());
        int numberX = cellNumberX(cell);
        int numberY = cellNumberY(cell);
        int numberW = cellNumberWidth(price);
        graphics.fill(numberX, numberY, numberX + numberW, numberY + NUMBER_BAR_HEIGHT, alphaColor(NUMBER_BAR_BORDER, 190));
        graphics.fill(numberX + 1, numberY + 1, numberX + numberW - 1, numberY + NUMBER_BAR_HEIGHT - 1, alphaColor(NUMBER_BAR_BG, 190));
        drawCellString(graphics, price, numberX + 3, numberY + 1, numberW - 5, alphaColor(canTrade ? NUMBER_BAR_TEXT : RED, 210));
        graphics.setColor(1.0F, 1.0F, 1.0F, 0.72F);
        renderCurrencyItem(graphics, stack(entry.currencyId()), numberX + numberW + 1, numberY - 1);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static int alphaColor(int color, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0x00FFFFFF);
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

    private boolean sellBackpackLoaded(Shop.Entry entry) {
        Shop.Reward reward = selectedSellReward(entry);
        return reward == null ? entry != null && entry.backpackLoaded() : reward.backpackLoaded();
    }

    private boolean sellHasBackpack(Shop.Entry entry) {
        Shop.Reward reward = selectedSellReward(entry);
        return reward == null ? entry != null && entry.hasBackpack() : reward.hasBackpack();
    }

    private boolean sellRsLoaded(Shop.Entry entry) {
        Shop.Reward reward = selectedSellReward(entry);
        return reward == null ? entry != null && entry.rsLoaded() : reward.rsLoaded();
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
        return searchBoxY() + 19;
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
        return questId != 0L ? Long.toUnsignedString(questId) : ColorText.translatable("gui.adventuresystems.curios.wallet.shop_task_fallback_name", index + 1).getString();
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
        return questId != 0L ? Long.toUnsignedString(questId) : ColorText.translatable("gui.adventuresystems.curios.wallet.shop_task_fallback_name", 1).getString();
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
        return GuiTheme.hovering(mouseX, mouseY, x, y, width, height);
    }

    private String clipped(String text, int maxWidth) {
        return GuiTheme.trim(font, text, maxWidth);
    }

    private void drawHintText(GuiGraphics graphics, String text, int x, int y) {
        if (text == null || text.isBlank()) return;
        graphics.drawString(font, text, x, y, CYAN, true);
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
        if (!useBackpackSource) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_sell_backpack_disabled");
        if (entry == null || !sellBackpackLoaded(entry) || !sellHasBackpack(entry)) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_sell_backpack_missing");
        return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_sell_backpack_count", materialCompact(sellBackpackCount(entry)));
    }

    private MutableComponent rsMaterialText(Shop.Entry entry) {
        if (!useRsSource) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_sell_rs_disabled");
        if (entry == null || !sellRsLoaded(entry)) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_sell_rs_unbound");
        if ("BOUND".equals(sellRsState(entry))) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_sell_rs_count", materialCompact(sellRsCount(entry)));
        return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_sell_rs_unbound");
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
        if (value < 60L) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_cell_duration_seconds", value);
        if (value < 3600L) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_cell_duration_minutes_plus", Math.max(1L, value / 60L));
        if (value < 86400L) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_cell_duration_hours_plus", Math.max(1L, value / 3600L));
        return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_cell_duration_days_plus", Math.max(1L, value / 86400L));
    }

    private static String formatExact(long value) {
        return NumberFormat.getIntegerInstance(Locale.ROOT).format(Math.max(0L, value));
    }

    private static String cleanPlaceholderText(String text) {
        if (text == null || text.isBlank()) return "";
        return text.replaceAll("§.", "");
    }

    private static final class CenteredAmountEditBox extends EditBox {
        private final Font boxFont;

        private CenteredAmountEditBox(Font font, int x, int y, int width, int height, Component title) {
            super(font, x, y, width, height, title);
            this.boxFont = font;
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int border = isFocused() ? CYAN : NUMBER_BAR_BORDER;
            int fill = active ? NUMBER_BAR_BG : 0xD0B8B8B8;
            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + height, border);
            graphics.fill(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + height - 1, fill);
            String text = getValue();
            int textX = getX() + Math.max(2, (getWidth() - boxFont.width(text)) / 2);
            int textY = getY() + (height - 8) / 2;
            graphics.drawString(boxFont, text, textX, textY, active ? NUMBER_BAR_TEXT : TEXT_GRAY, false);
            if (isFocused() && (System.currentTimeMillis() / 500L & 1L) == 0L) {
                int cursorX = Math.min(getX() + getWidth() - 2, textX + boxFont.width(text) + 1);
                graphics.fill(cursorX, textY - 1, cursorX + 1, textY + 9, NUMBER_BAR_TEXT);
            }
        }
    }

    private static final class PlaceholderEditBox extends EditBox {
        private final Font placeholderFont;
        private final Component placeholder;

        private PlaceholderEditBox(Font font, int x, int y, int width, int height, Component title, Component placeholder) {
            super(font, x, y, width, height, title);
            this.placeholderFont = font;
            this.placeholder = placeholder;
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
            if (isFocused() || !getValue().isEmpty()) return;
            String text = cleanPlaceholderText(placeholder.getString());
            if (text.isBlank()) return;
            graphics.drawString(placeholderFont, text, getX() + 4, getY() + (height - 8) / 2, PLACEHOLDER_GRAY, false);
        }
    }

    private final class PageTabButton extends AbstractButton {
        private final int slot;

        private PageTabButton(int slot) {
            super(0, 0, 40, PAGE_TAB_HEIGHT, Component.empty());
            this.slot = slot;
            visible = false;
            active = false;
        }

        private void bind(int x, int y, int width, Component label) {
            setX(x);
            setY(y);
            setWidth(width);
            setMessage(label);
            visible = true;
            active = true;
        }

        private void unbind() {
            setMessage(Component.empty());
            visible = false;
            active = false;
        }

        @Override
        public void onPress() {
            selectVisiblePage(slot);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (!super.isMouseOver(mouseX, mouseY)) return false;
            return mouseX >= categoryViewportLeft() && mouseX < categoryViewportRight()
                    && mouseY >= pageTabsY() && mouseY < pageTabsY() + PAGE_TAB_HEIGHT;
        }

        @Override
        public void updateWidgetNarration(@NotNull NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (!visible) return;
            enableCanvasScissor(graphics, categoryViewportLeft() - 1, pageTabsY(), categoryViewportRight() + 1, pageTabsY() + PAGE_TAB_HEIGHT);
            try {
                boolean hovered = isMouseOver(mouseX, mouseY) || isFocused();
                int border = active ? hovered ? CYAN_DARK : 0xFF202020 : 0xFF181818;
                int fill = active ? hovered ? 0xFF6A6A74 : 0xFF55555F : 0xFF38383E;
                graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), border);
                graphics.fill(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, fill);
                graphics.drawCenteredString(font, getMessage(), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, active ? TEXT_WHITE : TEXT_GRAY);
            } finally {
                graphics.disableScissor();
            }
        }
    }

    private final class ProductButton extends AbstractButton {
        private int rowIndex = -1;

        private ProductButton() {
            super(0, 0, GRID_CELL_WIDTH, GRID_CELL_HEIGHT, Component.empty());
            visible = false;
            active = false;
        }

        private void bind(int index, Cell cell) {
            rowIndex = index;
            setX(cell.x());
            setY(cell.y());
            setWidth(GRID_CELL_WIDTH);
            Shop.Entry entry = index >= 0 && index < rows.size() ? rows.get(index).entry() : null;
            setMessage(entry == null
                    ? Component.empty()
                    : entryDisplayName(entry, cellDisplayStack(entry)));
        }

        private void unbind() {
            rowIndex = -1;
            visible = false;
            active = false;
            setMessage(Component.empty());
        }

        @Override
        public void onPress() {
            if (rowIndex >= 0 && rowIndex < rows.size()) selectRow(rowIndex);
        }

        @Override
        public void updateWidgetNarration(@NotNull NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (!visible || rowIndex < 0 || rowIndex >= rows.size()) return;
            enableCanvasScissor(graphics, listLeft(), contentTop(), listLeft() + listWidth(), contentTop() + contentHeightVisible());
            try {
                Cell cell = cellForIndex(rowIndex);
                renderCell(graphics, rows.get(rowIndex), rowIndex, cell, mouseX, mouseY);
                if (isFocused()) renderSelectionOutline(graphics, cell.x(), cell.y(), GRID_CELL_WIDTH, GRID_CELL_HEIGHT);
            } finally {
                graphics.disableScissor();
            }
        }
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
}
