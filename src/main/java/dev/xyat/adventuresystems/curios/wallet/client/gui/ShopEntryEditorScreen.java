package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.adventuresystems.curios.wallet.network.Network;
import dev.xyat.adventuresystems.curios.wallet.shop.Shop;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.input.MouseInput;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.ui.NumberType;
import dev.xyat.kineticcore.api.client.gui.widget.KineticButton;
import dev.xyat.kineticcore.api.client.gui.widget.KineticNumberField;
import dev.xyat.kineticcore.api.client.gui.widget.KineticTextField;
import dev.xyat.kineticcore.api.client.gui.text.KineticText;
import dev.xyat.kineticcore.api.client.gui.overlay.KineticOverlays;
import dev.xyat.kineticcore.api.client.gui.selector.KineticSelectors;
import dev.xyat.adventuresystems.text.AdventureText;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import dev.xyat.kineticcore.api.client.gui.widget.KineticControl;

final class ShopEntryEditorScreen extends KineticPage {
    private static final int MIN_PANEL_WIDTH = 632;
    private static final int MIN_PANEL_HEIGHT = 348;
    private static final int BASIC_SECTION_HEIGHT = 54;
    private static final int PRICE_SECTION_HEIGHT = 122;
    private static final int QUEST_SECTION_HEIGHT = 52;
    private static final int BUTTON_GAP = 8;
    private static final int FIELD_LABEL_WIDTH = 58;
    private static final int COLUMN_GAP = 24;
    private static final int LEFT_COLUMN_GAP = 18;
    private final ShopScreen parent;
    private final ShopGuiSupport.EditorDraft draft;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;
    private KineticNumberField priceBox;
    private KineticNumberField countBox;
    private KineticNumberField dailyLimitBox;
    private KineticNumberField totalLimitBox;
    private KineticTextField pageBox;
    private KineticButton pageButton;
    /** 分页选择器选了“新建分页…”，此时用输入框填写新分页名。 */
    private boolean newPageMode;
    private boolean focusPageBoxOnBuild;
    /** 打开编辑器时商品所在的模式与序号；切换买入/卖出后保存时需要从原模式里移除。 */
    private final Shop.Mode originalMode;
    private final int originalIndex;
    private KineticNumberField questNeedBox;
    private KineticTextField displayNameBox;
    private KineticTextField descriptionBox;
    private KineticButton commandManageButton;
    private KineticButton rewardModeButton;
    private KineticButton rewardManageButton;
    private KineticUi buildingUi;

    ShopEntryEditorScreen(ShopScreen parent, ShopGuiSupport.EditorDraft draft) {
        super(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_title"));
        this.parent = parent;
        this.draft = draft;
        this.originalMode = draft.mode;
        this.originalIndex = draft.index;
        useCanvas(640, 360, 6);
        configureStandaloneDraft(this::captureDraftSnapshot, this::restoreDraftSnapshot);
    }

    private record DraftSnapshot(
            Shop.Mode mode,
            int index,
            String itemId,
            String currencyId,
            long price,
            int count,
            int dailyLimit,
            int totalLimit,
            String pageName,
            String displayName,
            String description,
            String commandText,
            String iconId,
            int requiredQuestCount,
            List<Long> questIds,
            boolean gacha,
            boolean selectable,
            List<ShopGuiSupport.RewardDraft> rewards,
            List<ShopGuiSupport.CommandDraft> commands
    ) {
    }

    private DraftSnapshot captureDraftSnapshot() {
        return new DraftSnapshot(
                draft.mode,
                draft.index,
                draft.itemId,
                draft.currencyId,
                draft.price,
                draft.count,
                draft.dailyLimit,
                draft.totalLimit,
                draft.pageName,
                draft.displayName,
                draft.description,
                draft.commandText,
                draft.iconId,
                draft.requiredQuestCount,
                new ArrayList<>(draft.questIds),
                draft.gacha,
                draft.selectable,
                new ArrayList<>(draft.rewards),
                new ArrayList<>(draft.commands)
        );
    }

    private void restoreDraftSnapshot(DraftSnapshot snapshot) {
        if (snapshot == null) return;
        draft.mode = snapshot.mode();
        draft.index = snapshot.index();
        draft.itemId = snapshot.itemId();
        draft.currencyId = snapshot.currencyId();
        draft.price = snapshot.price();
        draft.count = snapshot.count();
        draft.dailyLimit = snapshot.dailyLimit();
        draft.totalLimit = snapshot.totalLimit();
        draft.pageName = snapshot.pageName();
        draft.displayName = snapshot.displayName();
        draft.description = snapshot.description();
        draft.commandText = snapshot.commandText();
        draft.iconId = snapshot.iconId();
        draft.requiredQuestCount = snapshot.requiredQuestCount();
        draft.questIds.clear();
        draft.questIds.addAll(snapshot.questIds());
        draft.gacha = snapshot.gacha();
        draft.selectable = snapshot.selectable();
        draft.rewards.clear();
        draft.rewards.addAll(snapshot.rewards());
        draft.commands.clear();
        draft.commands.addAll(snapshot.commands());
    }

    @Override
    protected void build(KineticUi ui) {
        syncBasicInputsQuietly();
        buildingUi = ui;
        panelWidth = Math.max(320, Math.min(MIN_PANEL_WIDTH, width() - 8));
        panelHeight = Math.max(300, Math.min(MIN_PANEL_HEIGHT, height() - 12));
        left = Math.max(4, (width() - panelWidth) / 2);
        top = Math.max(4, (height() - panelHeight) / 2);

        addButton(typeButtonX(), typeButtonY(), buttonColumnWidth(), typeButtonText(), this::openTypeMenu);
        addButton(selectItemButtonX(), selectItemButtonY(), buttonColumnWidth(), Component.empty(), this::openMainItemSelector);
        addButton(selectCurrencyButtonX(), selectCurrencyButtonY(), buttonColumnWidth(), Component.empty(), () -> openChild(new CurrencyPickerScreen(draft)));
        addButton(selectPaymentButtonX(), selectPaymentButtonY(), buttonColumnWidth(), Component.empty(), this::openPaymentItemSelector);

        priceBox = addLongField(
                inputLeftX(),
                priceInputY(),
                leftInputWidth(),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_price_label")
        );
        priceBox.limitTextLength(18);
        priceBox.setLongValue(draft.price);

        countBox = addIntegerField(
                rightInputX(),
                priceInputY(),
                rightInputWidth(),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_count_label"),
                1,
                64
        );
        countBox.limitTextLength(2);
        countBox.setIntValue(draft.count);

        dailyLimitBox = addIntegerField(
                inputLeftX(),
                dailyInputY(),
                leftInputWidth(),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_timed_label"),
                0,
                Integer.MAX_VALUE
        );
        dailyLimitBox.limitTextLength(10);
        dailyLimitBox.setIntValue(draft.dailyLimit);

        totalLimitBox = addIntegerField(
                rightInputX(),
                dailyInputY(),
                rightInputWidth(),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_total_label"),
                0,
                999999999
        );
        totalLimitBox.limitTextLength(9);
        totalLimitBox.setIntValue(draft.totalLimit);

        // 分页：下拉选择已有分页 / 默认分页，或选“新建分页…”后在右侧输入框填写新名字。
        pageButton = addButton(inputLeftX(), pageInputY(), newPageMode ? PAGE_MENU_ARROW_WIDTH : leftInputWidth(),
                pageButtonText(), this::openPageMenu);
        pageBox = addTextField(
                inputLeftX() + PAGE_MENU_ARROW_WIDTH + 2,
                pageInputY() + 1,
                Math.max(40, leftInputWidth() - PAGE_MENU_ARROW_WIDTH - 2),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_page_label"),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_page_new_hint")
        );
        pageBox.limitTextLength(32);
        pageBox.setTextValue(newPageMode && draft.pageName != null ? draft.pageName : "");
        setControlVisible(pageBox, newPageMode);
        setControlEnabled(pageBox, newPageMode);

        questNeedBox = addIntegerField(
                rightInputX(),
                pageInputY(),
                rightInputWidth(),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_quest_need_label"),
                0,
                999
        );
        questNeedBox.limitTextLength(3);
        questNeedBox.setIntValue(draft.requiredQuestCount);

        displayNameBox = addTextField(
                inputLeftX(),
                displayNameInputY(),
                fullInputWidth(),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_display_name_label"),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_display_name_label")
        );
        displayNameBox.limitTextLength(64);
        displayNameBox.setTextValue(draft.displayName == null ? "" : draft.displayName);

        descriptionBox = addTextField(
                commandAreaX(),
                descriptionInputY(),
                commandAreaWidth(),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_description_label"),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_description_label")
        );
        descriptionBox.limitTextLength(256);
        descriptionBox.setTextValue(draft.description == null ? "" : draft.description);

        commandManageButton = addButton(
                commandManageButtonX(),
                commandManageButtonY(),
                commandManageButtonWidth(),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_manage_button"),
                () -> {
                    syncBasicInputsQuietly();
                    openChild(new CommandManageScreen(draft));
                }
        );

        addButton(
                questManageButtonX(),
                questManageButtonY(),
                160,
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_quest_manage"),
                () -> {
                    syncBasicInputsQuietly();
                    openChild(new QuestManageScreen(draft));
                }
        );

        rewardModeButton = addButton(contentModeButtonX(), contentModeButtonY(), 120, contentModeButtonText(), this::openContentModeMenu);
        rewardManageButton = addButton(contentManageButtonX(), contentModeButtonY(), contentManageButtonWidth(), contentManageButtonText(), this::openContentManager);
        refreshContentButtons();

        addButton(saveButtonX(), actionButtonY(), actionButtonWidth(), AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_save"), this::saveDraft);
        addButton(cancelButtonX(), actionButtonY(), actionButtonWidth(), AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_cancel"), this::cancelAndReturn);
        if (focusPageBoxOnBuild && newPageMode) {
            focusPageBoxOnBuild = false;
            focus(pageBox);
        }
    }

    private static final int PAGE_MENU_ARROW_WIDTH = 20;

    private Component pageButtonText() {
        if (newPageMode) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_menu_arrow");
        return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_menu_value", pageDisplayName(draft.pageName));
    }

    private static Component pageDisplayName(String page) {
        if (page == null || page.isBlank()) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_page_default");
        return AdventureText.literal(page);
    }

    private void openPageMenu() {
        syncBasicInputsQuietly();
        String current = draft.pageName == null ? "" : draft.pageName.trim();
        List<KineticOverlays.MenuItem> items = new ArrayList<>();
        items.add(KineticOverlays.MenuItem.toggle(pageDisplayName(""),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_page_default_tip"),
                !newPageMode && current.isEmpty(), () -> choosePage("")));
        List<String> known = new ArrayList<>(parent.knownPageNames(draft.mode));
        // 草稿里的分页可能还不在商店里（例如刚新建、尚未保存），也列出来方便切回。
        if (!current.isEmpty() && !known.contains(current)) known.add(0, current);
        for (String page : known) {
            items.add(KineticOverlays.MenuItem.toggle(AdventureText.literal(page), null,
                    !newPageMode && page.equals(current), () -> choosePage(page)));
        }
        items.add(KineticOverlays.MenuItem.separator());
        items.add(KineticOverlays.MenuItem.action(
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_page_new"),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_page_new_tip"),
                () -> {
                    syncBasicInputsQuietly();
                    newPageMode = true;
                    draft.pageName = "";
                    focusPageBoxOnBuild = true;
                    rebuildEditorWidgets();
                }));
        openContextMenu(pageButton.controlX(), pageButton.controlY() + 22, items);
    }

    private void choosePage(String page) {
        syncBasicInputsQuietly();
        newPageMode = false;
        draft.pageName = page == null ? "" : page.trim();
        rebuildEditorWidgets();
    }

    private void openTypeMenu() {
        List<KineticOverlays.MenuItem> items = new ArrayList<>();
        items.add(KineticOverlays.MenuItem.toggle(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_type_buy"),
                null, draft.mode == Shop.Mode.BUY, () -> setDraftMode(Shop.Mode.BUY)));
        items.add(KineticOverlays.MenuItem.toggle(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_type_sell"),
                null, draft.mode == Shop.Mode.SELL, () -> setDraftMode(Shop.Mode.SELL)));
        openContextMenu(typeButtonX(), typeButtonY() + 22, items);
    }

    private void setDraftMode(Shop.Mode mode) {
        if (mode == null || draft.mode == mode) return;
        syncBasicInputsQuietly();
        draft.mode = mode;
        draft.gacha = false;
        if (draft.mode == Shop.Mode.BUY) draft.selectable = false;
        rebuildEditorWidgets();
    }

    /** 内容模式改为下拉选择：买入有“单件 / 自选 / 抽奖”，卖出有“单件 / 多选”。 */
    private void openContentModeMenu() {
        List<KineticOverlays.MenuItem> items = new ArrayList<>();
        if (draft.mode == Shop.Mode.SELL) {
            items.add(KineticOverlays.MenuItem.toggle(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_sell_multi_off"),
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_tooltip_sell_multi_switch"),
                    !draft.selectable, () -> setContentMode(false, false)));
            items.add(KineticOverlays.MenuItem.toggle(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_sell_multi_on"),
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_tooltip_sell_choices"),
                    draft.selectable, () -> setContentMode(false, true)));
        } else {
            items.add(KineticOverlays.MenuItem.toggle(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_reward_single_active"),
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_tooltip_single_reward_mode"),
                    !draft.gacha && !draft.selectable, () -> setContentMode(false, false)));
            items.add(KineticOverlays.MenuItem.toggle(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_choice_table_active"),
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_tooltip_choice_table"),
                    draft.selectable, () -> setContentMode(false, true)));
            items.add(KineticOverlays.MenuItem.toggle(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_gacha_pool_active"),
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_tooltip_gacha_pool"),
                    draft.gacha && !draft.selectable, () -> setContentMode(true, false)));
        }
        openContextMenu(contentModeButtonX(), contentModeButtonY() + 22, items);
    }

    private void setContentMode(boolean gacha, boolean selectable) {
        syncBasicInputsQuietly();
        draft.gacha = draft.mode == Shop.Mode.BUY && gacha && !selectable;
        draft.selectable = selectable;
        if (selectable) normalizeChoiceRewards();
        refreshContentButtons();
    }

    private int actionButtonWidth() {
        int save = KineticText.width(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_save"));
        int cancel = KineticText.width(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_cancel"));
        return Math.min(Math.max(save, cancel) + 16, Math.max(40, (panelWidth - 48) / 6));
    }

    private void rebuildEditorWidgets() {
        rebuild();
    }

    private KineticButton addButton(int x, int y, int width, Component label, Runnable action) {
        return buildingUi.button(x, y, width).text(label).onClick(action).build();
    }

    private KineticNumberField addLongField(int x, int y, int width, Component label) {
        return buildingUi.numberField(x, y, width, NumberType.LONG)
                .label(label).allowNegative(false).range(1L, null)
                .firstShownTextAsDefault().build();
    }

    private KineticNumberField addIntegerField(int x, int y, int width, Component label, int minimum, int maximum) {
        return buildingUi.numberField(x, y, width, NumberType.INT)
                .label(label).allowNegative(false).range(minimum, maximum).firstShownTextAsDefault().build();
    }

    private KineticTextField addTextField(int x, int y, int width, Component label,
                                         Component placeholder) {
        return buildingUi.textField(x, y, width).label(label).placeholder(placeholder).firstShownTextAsDefault().build();
    }

    private int basicSectionY() { return top + 28; }
    private int priceSectionY() { return basicSectionY() + BASIC_SECTION_HEIGHT + 6; }
    private int questSectionY() { return priceSectionY() + PRICE_SECTION_HEIGHT + 6; }
    private int contentSectionY() { return questSectionY() + QUEST_SECTION_HEIGHT + 6; }
    private int contentSectionHeight() { return Math.max(46, top + panelHeight - contentSectionY() - 10); }
    private int innerLeft() { return left + 14; }
    private int innerRight() { return left + panelWidth - 14; }
    private int innerWidth() { return Math.max(1, innerRight() - innerLeft()); }
    private int buttonColumnWidth() { return Math.max(82, (innerWidth() - BUTTON_GAP * 4 - ICON_SLOT_SIZE) / 4); }
    // 自定义列表图标格：在基础信息一行的最右侧。
    private static final int ICON_SLOT_SIZE = 20;
    private int iconSlotX() { return innerRight() - ICON_SLOT_SIZE; }
    private int iconSlotY() { return typeButtonY(); }
    private int typeButtonX() { return innerLeft(); }
    private int typeButtonY() { return basicSectionY() + 22; }
    private int selectItemButtonX() { return typeButtonX() + buttonColumnWidth() + BUTTON_GAP; }
    private int selectItemButtonY() { return typeButtonY(); }
    private int selectCurrencyButtonX() { return typeButtonX() + (buttonColumnWidth() + BUTTON_GAP) * 2; }
    private int selectCurrencyButtonY() { return typeButtonY(); }
    private int selectPaymentButtonX() { return typeButtonX() + (buttonColumnWidth() + BUTTON_GAP) * 3; }
    private int selectPaymentButtonY() { return typeButtonY(); }
    private int leftAreaWidth() { return Math.max(270, (innerWidth() - COLUMN_GAP) / 2); }
    private int commandAreaX() { return innerLeft() + leftAreaWidth() + COLUMN_GAP; }
    private int commandAreaWidth() { return Math.max(230, innerRight() - commandAreaX()); }
    private int leftColumnWidth() { return Math.max(132, (leftAreaWidth() - LEFT_COLUMN_GAP) / 2); }
    private int labelLeftX() { return innerLeft(); }
    private int inputLeftX() { return labelLeftX() + FIELD_LABEL_WIDTH + 6; }
    private int rightLabelX() { return innerLeft() + leftColumnWidth() + LEFT_COLUMN_GAP; }
    private int rightInputX() { return rightLabelX() + FIELD_LABEL_WIDTH + 6; }
    private int leftInputWidth() { return Math.max(72, leftColumnWidth() - FIELD_LABEL_WIDTH - 6); }
    private int rightInputWidth() { return Math.max(64, innerLeft() + leftAreaWidth() - rightInputX()); }
    private int fullInputWidth() { return Math.max(180, innerLeft() + leftAreaWidth() - inputLeftX()); }
    private int priceInputY() { return priceSectionY() + 26; }
    private int dailyInputY() { return priceSectionY() + 50; }
    private int pageInputY() { return priceSectionY() + 74; }
    private int displayNameInputY() { return priceSectionY() + 98; }
    private int descriptionInputY() { return priceSectionY() + 88; }
    private int commandManageButtonX() { return commandAreaX(); }
    private int commandManageButtonY() { return priceSectionY() + 26; }
    private int commandManageButtonWidth() { return Math.min(150, Math.max(110, commandAreaWidth())); }
    private int questManageButtonX() { return left + panelWidth / 2 - 80; }
    private int questManageButtonY() { return questSectionY() + 18; }
    private int contentModeButtonX() { return inputLeftX(); }
    private int contentManageButtonX() { return contentModeButtonX() + 132; }
    private int contentManageButtonWidth() { return Math.min(180, Math.max(120, innerRight() - contentManageButtonX())); }
    private int contentModeButtonY() { return contentSectionY() + 22; }
    private int contentPreviewX() { return contentManageButtonX() + contentManageButtonWidth() + 12; }
    private int contentPreviewY() { return contentModeButtonY(); }
    private int actionButtonY() { return top + 8; }
    private int cancelButtonX() { return left + panelWidth - 12 - actionButtonWidth(); }
    private int saveButtonX() { return cancelButtonX() - 4 - actionButtonWidth(); }

    private void openMainItemSelector() {
        KineticSelectors.openItemSelector(selection -> {
            if (!selection.isItem()) return;
            ItemStack stack = selection.stack();
            String spec = ShopGuiSupport.selectedStackSpec(stack);
            if (!spec.isBlank()) {
                draft.itemId = spec;
                draft.count = Math.max(1, Math.min(64, stack.getCount()));
                if (countBox != null) countBox.setIntValue(draft.count);
            }
        });
    }

    private boolean hasCustomIcon() {
        return draft.iconId != null && !draft.iconId.isBlank() && !ShopGuiSupport.stack(draft.iconId).isEmpty();
    }

    /** 未自定义时显示商品本身；自定义后显示所选物品并用绿色边框标记“已修改”。 */
    private void renderIconSlot(KineticGraphics graphics, int mouseX, int mouseY) {
        int x = iconSlotX();
        int y = iconSlotY();
        boolean hovered = KineticTheme.hovering(mouseX, mouseY, x, y, ICON_SLOT_SIZE, ICON_SLOT_SIZE);
        Component label = AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_icon_label");
        graphics.scrollingTextRight(label, x + ICON_SLOT_SIZE, basicSectionY() + 5, buttonColumnWidth(), KineticTheme.current().text(), true);
        KineticTheme.itemSlot(graphics, x, y, ICON_SLOT_SIZE, ICON_SLOT_SIZE, 4, false, hovered, false);
        if (hasCustomIcon() && !hovered) {
            KineticTheme.indicatorOutline(graphics, x, y, ICON_SLOT_SIZE, ICON_SLOT_SIZE, KineticTheme.Indicator.SUCCESS);
        }
        String id = hasCustomIcon() ? draft.iconId : draft.itemId;
        if (id != null && !id.isBlank()) graphics.item(ShopGuiSupport.stack(id), x + 2, y + 2);
    }

    private void openIconSelector() {
        KineticSelectors.openItemSelector(selection -> {
            if (!selection.isItem()) return;
            String spec = ShopGuiSupport.selectedStackSpec(selection.stack());
            if (!spec.isBlank()) draft.iconId = ShopGuiSupport.stackSpecWithCount(spec, 1);
        });
    }

    @Override
    protected boolean onMouseClick(MouseInput input) {
        if (!input.inside(iconSlotX(), iconSlotY(), ICON_SLOT_SIZE, ICON_SLOT_SIZE)) return false;
        if (input.isRight()) {
            draft.iconId = "";
            return true;
        }
        if (input.isLeft()) {
            syncBasicInputsQuietly();
            openIconSelector();
            return true;
        }
        return false;
    }

    private void openPaymentItemSelector() {
        KineticSelectors.openItemSelector(selection -> {
            if (!selection.isItem()) return;
            ItemStack stack = selection.stack();
            String spec = ShopGuiSupport.selectedStackSpec(stack);
            if (!spec.isBlank()) draft.currencyId = spec;
        });
    }

    private void syncBasicInputsQuietly() {
        if (newPageMode && pageBox != null) draft.pageName = pageBox.textValue().trim();
        if (displayNameBox != null) draft.displayName = displayNameBox.textValue().trim();
        if (descriptionBox != null) draft.description = descriptionBox.textValue().trim();

        if (priceBox != null) {
            Long value = priceBox.getLongValue();
            if (value != null) draft.price = value;
        }

        if (countBox != null) {
            Integer value = countBox.getIntValue();
            if (value != null) draft.count = value;
        }

        if (dailyLimitBox != null) {
            Integer value = dailyLimitBox.getIntValue();
            if (value != null) draft.dailyLimit = value;
        }

        if (totalLimitBox != null) {
            Integer value = totalLimitBox.getIntValue();
            if (value != null) draft.totalLimit = value;
        }

        if (questNeedBox != null) {
            Integer value = questNeedBox.getIntValue();
            if (value != null) draft.requiredQuestCount = value;
        }
    }

    void refreshContentButtons() {
        if (rewardModeButton != null) rewardModeButton.setText(contentModeButtonText());
        if (rewardManageButton != null) {
            setControlVisible(rewardManageButton, shouldShowContentManageButton());
            setControlEnabled(rewardManageButton, shouldShowContentManageButton());
            rewardManageButton.setText(contentManageButtonText());
        }
    }

    private Component typeButtonText() {
        return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_menu_value", draft.mode == Shop.Mode.BUY ? AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_type_buy") : AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_type_sell"));
    }

    private Component itemButtonText() {
        return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_button_item", shortStackName(draft.itemId, AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_none")));
    }

    private Component currencyButtonText() {
        return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_button_currency", shortStackName(currentCurrencyId(), AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_none")));
    }

    private Component paymentButtonText() {
        return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_button_payment", shortStackName(currentBarterItemId(), AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_none")));
    }

    private String currentCurrencyId() {
        return ShopGuiSupport.isConfiguredCurrency(draft.currencyId) ? draft.currencyId : "";
    }

    private String currentBarterItemId() {
        return draft.currencyId == null || draft.currencyId.isBlank() || ShopGuiSupport.isConfiguredCurrency(draft.currencyId) ? "" : draft.currencyId;
    }

    private Component shortStackName(String id, Component fallback) {
        if (id == null || id.isBlank()) return fallback;
        Component name = ShopGuiSupport.stackNameComponent(id);
        return name.getString().isBlank() ? fallback : name;
    }



    private Component contentModeButtonText() {
        return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_menu_value", contentModeName());
    }

    private Component contentModeName() {
        if (draft.mode == Shop.Mode.SELL) {
            return AdventureText.translatable(draft.selectable ? "gui.adventuresystems.curios.wallet.shop_editor_sell_multi_on" : "gui.adventuresystems.curios.wallet.shop_editor_sell_multi_off");
        }
        if (draft.selectable) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_choice_table_active");
        if (draft.gacha) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_gacha_pool_active");
        return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_reward_single_active");
    }

    private Component contentManageButtonText() {
        if (draft.mode == Shop.Mode.SELL) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_sell_choice_manage");
        if (draft.selectable) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_choice_table_manage");
        return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_gacha_pool_manage");
    }

    private boolean shouldShowContentManageButton() {
        if (draft.mode == Shop.Mode.SELL) return draft.selectable;
        return draft.gacha || draft.selectable;
    }

    private Component contentSectionTitle() {
        if (draft.mode == Shop.Mode.SELL) {
            return draft.selectable ? AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_sell_content_section") : AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_single_content_section");
        }
        if (draft.gacha) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_gacha_content_section");
        if (draft.selectable) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_choice_content_section");
        return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_single_content_section");
    }

    private void openContentManager() {
        syncBasicInputsQuietly();
        if (draft.mode == Shop.Mode.SELL) {
            if (!draft.selectable) return;
            normalizeChoiceRewards();
            openChild(new RewardPoolScreen(draft));
            return;
        }
        if (draft.selectable) {
            normalizeChoiceRewards();
            openChild(new RewardPoolScreen(draft));
            return;
        }
        if (draft.gacha) openChild(new RewardPoolScreen(draft));
    }

    private void normalizeChoiceRewards() {
        if (draft.rewards.isEmpty()) return;
        List<ShopGuiSupport.RewardDraft> normalized = new ArrayList<>();
        for (ShopGuiSupport.RewardDraft reward : draft.rewards) {
            if (reward.empty()) continue;
            int count = Math.max(1, Math.min(64, reward.count()));
            normalized.add(new ShopGuiSupport.RewardDraft(ShopGuiSupport.stackSpecWithCount(reward.itemId(), count), count, 1, false, 0.0D, reward.displayName(), reward.command()));
        }
        draft.rewards.clear();
        draft.rewards.addAll(normalized);
    }

    private boolean hasEmptyReward() {
        for (ShopGuiSupport.RewardDraft reward : draft.rewards) {
            if (reward.empty()) return true;
        }
        return false;
    }

    private void saveDraft() {
        Long price = priceBox.getLongValue();
        Integer count = countBox.getIntValue();
        Integer dailyLimit = dailyLimitBox.getIntValue();
        Integer totalLimit = totalLimitBox.getIntValue();
        Integer requiredQuestCount = questNeedBox.getIntValue();

        if (price == null
                || count == null
                || dailyLimit == null
                || totalLimit == null
                || requiredQuestCount == null) {
            KineticOverlays.toast(
                    "shop_editor_error",
                    AdventureText.translatable(
                            "gui.adventuresystems.curios.wallet.shop_editor_invalid_input"
                    ),
                    KineticOverlays.Position.BOTTOM_CENTER,
                    2500,
                    0,
                    -30
            );
            return;
        }

        draft.price = price;
        draft.count = count;
        draft.dailyLimit = dailyLimit;
        draft.totalLimit = totalLimit;
        if (newPageMode) draft.pageName = pageBox.textValue().trim();
        draft.displayName = displayNameBox.textValue().trim();
        draft.description = descriptionBox.textValue().trim();
        draft.syncDirectCommandFromList();
        draft.requiredQuestCount = requiredQuestCount;

        if (draft.itemId == null || draft.itemId.isBlank()) {
            KineticOverlays.toast("shop_editor_error", AdventureText.translatable("msg.adventuresystems.curios.wallet.shop_editor_missing_item"), KineticOverlays.Position.BOTTOM_CENTER, 2500, 0, -30);
            return;
        }
        if (draft.currencyId == null || draft.currencyId.isBlank()) {
            KineticOverlays.toast("shop_editor_error", AdventureText.translatable("msg.adventuresystems.curios.wallet.shop_editor_missing_currency"), KineticOverlays.Position.BOTTOM_CENTER, 2500, 0, -30);
            return;
        }
        if (draft.price <= 0L || draft.count < 1 || draft.count > 64 || draft.dailyLimit < 0 || draft.totalLimit < 0 || draft.requiredQuestCount < 0) {
            KineticOverlays.toast("shop_editor_error", AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_invalid_input"), KineticOverlays.Position.BOTTOM_CENTER, 2500, 0, -30);
            return;
        }
        if (draft.mode == Shop.Mode.SELL && draft.selectable && !draft.rewards.isEmpty()) normalizeChoiceRewards();
        if (draft.mode == Shop.Mode.BUY && draft.selectable) normalizeChoiceRewards();
        if ((draft.gacha || draft.selectable) && draft.rewards.isEmpty()) {
            String key;
            if (draft.mode == Shop.Mode.SELL) key = "msg.adventuresystems.curios.wallet.shop_editor_missing_sell_choices";
            else if (draft.selectable) key = "msg.adventuresystems.curios.wallet.shop_editor_missing_choice_rewards";
            else key = "msg.adventuresystems.curios.wallet.shop_editor_missing_rewards";
            KineticOverlays.toast("shop_editor_error", AdventureText.translatable(key), KineticOverlays.Position.BOTTOM_CENTER, 2500, 0, -30);
            return;
        }
        if ((draft.mode == Shop.Mode.SELL || draft.selectable) && hasEmptyReward()) {
            KineticOverlays.toast("shop_editor_error", AdventureText.translatable(draft.mode == Shop.Mode.SELL ? "msg.adventuresystems.curios.wallet.shop_sell_choice_no_empty" : "msg.adventuresystems.curios.wallet.shop_choice_no_empty"), KineticOverlays.Position.BOTTOM_CENTER, 2500, 0, -30);
            return;
        }
        String rewardsText = (draft.gacha || draft.selectable) ? draft.buildRewardsText() : "";
        // 改了买入 / 卖出：在新模式里追加，再从原模式移除；否则原序号会覆盖另一模式里的商品。
        boolean movedMode = originalIndex >= 0 && originalMode != null && draft.mode != originalMode;
        if (movedMode) draft.index = -1;
        // 序号 < 0 时服务器在末尾追加新商品。
        Network.sendSaveShopEntry(draft.mode, draft.index, draft.itemId, draft.currencyId, draft.price, draft.count, draft.dailyLimit, draft.totalLimit, draft.buildQuestText(), draft.gacha, rewardsText);
        if (movedMode) {
            Network.sendRemoveShopEntry(originalMode, originalIndex);
            parent.applyLocalRemove(originalMode, originalIndex);
        }
        // 先更新本地内存立即显示，服务器保存后广播的刷新会再覆盖为权威数据。
        parent.applyLocalSave(draft);
        commitDraft();
        navigateBack();
    }

    private void cancelAndReturn() {
        discardDraft();
        navigateBack();
    }

    @Override
    protected boolean onCloseRequested() {
        cancelAndReturn();
        return true;
    }

    @Override
    protected void renderBackground(KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        KineticTheme.panel(graphics, left, top, panelWidth, panelHeight);
        graphics.scrollingTextCentered(title(), left + panelWidth / 2, top + 12, Math.max(0, (saveButtonX() - 4 - (left + panelWidth / 2)) * 2), KineticTheme.current().text(), false);

        renderSection(graphics, left + 12, basicSectionY(), BASIC_SECTION_HEIGHT, AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_basic_info"));
        renderItemButtonContent(graphics, draft.itemId, itemButtonText(), selectItemButtonX(), selectItemButtonY());
        renderItemButtonContent(graphics, currentCurrencyId(), currencyButtonText(), selectCurrencyButtonX(), selectCurrencyButtonY());
        renderItemButtonContent(graphics, currentBarterItemId(), paymentButtonText(), selectPaymentButtonX(), selectPaymentButtonY());
        renderIconSlot(graphics, mouseX, mouseY);

        renderSection(graphics, left + 12, priceSectionY(), PRICE_SECTION_HEIGHT, AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_price_limit"));
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_price_label"), labelLeftX(), priceInputY() + 5, FIELD_LABEL_WIDTH + 2, KineticTheme.current().text(), true);
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_count_label"), rightLabelX(), priceInputY() + 5, FIELD_LABEL_WIDTH + 2, KineticTheme.current().text(), true);
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_timed_label"), labelLeftX(), dailyInputY() + 5, FIELD_LABEL_WIDTH + 2, KineticTheme.current().text(), true);
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_total_label"), rightLabelX(), dailyInputY() + 5, FIELD_LABEL_WIDTH + 2, KineticTheme.current().text(), true);
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_page_label"), labelLeftX(), pageInputY() + 5, FIELD_LABEL_WIDTH + 2, KineticTheme.current().text(), true);
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_quest_need_label"), rightLabelX(), pageInputY() + 5, FIELD_LABEL_WIDTH + 2, KineticTheme.current().text(), true);
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_display_name_label"), labelLeftX(), displayNameInputY() + 5, FIELD_LABEL_WIDTH + 2, KineticTheme.current().text(), true);
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_manage_label"), commandManageButtonX(), commandManageButtonY() - 14, commandAreaWidth(), KineticTheme.current().text(), true);
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_count", draft.commands.size()), commandManageButtonX(), commandManageButtonY() + 26, commandAreaWidth(), KineticTheme.current().text(), true);
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_description_label"), commandAreaX(), descriptionInputY() - 12, commandAreaWidth(), KineticTheme.current().text(), true);

        renderSection(graphics, left + 12, questSectionY(), QUEST_SECTION_HEIGHT, AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_requirements"));
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_quest_count", draft.questIds.size()), innerLeft(), questSectionY() + 28, Math.max(0, rightLabelX() - innerLeft() - 4), KineticTheme.current().text(), true);
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_quest_rule", draft.requiredQuestCount <= 0 ? draft.questIds.size() : Math.min(draft.requiredQuestCount, draft.questIds.size())), rightLabelX(), questSectionY() + 28, Math.max(0, questManageButtonX() - rightLabelX() - 4), KineticTheme.current().text(), true);

        renderSection(graphics, left + 12, contentSectionY(), contentSectionHeight(), contentSectionTitle());
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_content_mode_label"), labelLeftX(), contentModeButtonY() + 5, FIELD_LABEL_WIDTH + 2, KineticTheme.current().text(), true);
        graphics.scrollingText(contentCountText(), contentPreviewX(), contentPreviewY() + 5, Math.max(0, innerRight() - contentPreviewX()), KineticTheme.current().text(), true);
    }

    private Component contentCountText() {
        if (draft.mode == Shop.Mode.SELL) {
            if (!draft.selectable) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_single_reward_hint");
            return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_sell_choice_count", draft.rewards.size());
        }
        if (draft.gacha) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_gacha_reward_count", draft.rewards.size());
        if (draft.selectable) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_choice_reward_count", draft.rewards.size());
        return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_single_reward_hint");
    }


    private void renderItemButtonContent(KineticGraphics graphics, String id, Component label, int buttonX, int buttonY) {
        boolean hasIcon = id != null && !id.isBlank();
        // Keep the original label center, leaving a four-pixel gap before the item icon.
        graphics.scrollingTextCentered(label, buttonX + buttonColumnWidth() / 2, buttonY + 6,
                Math.max(0, buttonColumnWidth() - (hasIcon ? 48 : 8)), KineticTheme.current().text(), true);
        if (hasIcon) graphics.item(ShopGuiSupport.stack(id), buttonX + buttonColumnWidth() - 20, buttonY + 2);
    }


    private void renderSection(KineticGraphics graphics, int x, int y, int height, Component title) {
        KineticTheme.panelAlt(graphics, x, y, panelWidth - 24, height);
        graphics.scrollingText(title, x + 10, y + 5, Math.max(0, (y == basicSectionY() ? innerRight() - buttonColumnWidth() - 4 : innerRight()) - (x + 10)), KineticTheme.current().text(), true);
    }

    @Override
    protected void renderTooltips(int mouseX, int mouseY) {
        if (KineticTheme.hovering(mouseX, mouseY, iconSlotX(), iconSlotY(), ICON_SLOT_SIZE, ICON_SLOT_SIZE)) {
            showTooltip(List.of(
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_icon_label"),
                    hasCustomIcon()
                            ? AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_icon_custom", ShopGuiSupport.stack(draft.iconId).getHoverName())
                            : AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_icon_default"),
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_icon_tip")), 260);
            return;
        }
        ItemStack stack = hoveredEditorItem(mouseX, mouseY);
        if (!stack.isEmpty()) {
            showItemTooltip(stack);
            return;
        }
        List<Component> tooltip = editorTooltipAt(mouseX, mouseY);
        if (!tooltip.isEmpty()) showTooltip(tooltip);
    }

    private ItemStack hoveredEditorItem(int mouseX, int mouseY) {
        if (draft.itemId != null && !draft.itemId.isBlank() && KineticTheme.hovering(mouseX, mouseY, selectItemButtonX() + buttonColumnWidth() - 20, selectItemButtonY() + 2, 16, 16)) return ShopGuiSupport.stack(draft.itemId);
        if (!currentCurrencyId().isBlank() && KineticTheme.hovering(mouseX, mouseY, selectCurrencyButtonX() + buttonColumnWidth() - 20, selectCurrencyButtonY() + 2, 16, 16)) return ShopGuiSupport.stack(currentCurrencyId());
        if (!currentBarterItemId().isBlank() && KineticTheme.hovering(mouseX, mouseY, selectPaymentButtonX() + buttonColumnWidth() - 20, selectPaymentButtonY() + 2, 16, 16)) return ShopGuiSupport.stack(currentBarterItemId());
        return ItemStack.EMPTY;
    }

    private List<Component> editorTooltipAt(int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (KineticTheme.hovering(mouseX, mouseY, typeButtonX(), typeButtonY(), buttonColumnWidth(), 20)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_type_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_type");
        else if (KineticTheme.hovering(mouseX, mouseY, selectItemButtonX(), selectItemButtonY(), buttonColumnWidth(), 20)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_item_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_item");
        else if (KineticTheme.hovering(mouseX, mouseY, selectCurrencyButtonX(), selectCurrencyButtonY(), buttonColumnWidth(), 20)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_currency_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_currency");
        else if (KineticTheme.hovering(mouseX, mouseY, selectPaymentButtonX(), selectPaymentButtonY(), buttonColumnWidth(), 20)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_payment_item", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_payment_item");
        else if (KineticTheme.hovering(mouseX, mouseY, priceBox.controlX(), priceBox.controlY(), priceBox.controlWidth(), 18)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_price_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_price");
        else if (KineticTheme.hovering(mouseX, mouseY, countBox.controlX(), countBox.controlY(), countBox.controlWidth(), 18)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_count_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_count");
        else if (KineticTheme.hovering(mouseX, mouseY, dailyLimitBox.controlX(), dailyLimitBox.controlY(), dailyLimitBox.controlWidth(), 18)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_timed_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_timed");
        else if (KineticTheme.hovering(mouseX, mouseY, totalLimitBox.controlX(), totalLimitBox.controlY(), totalLimitBox.controlWidth(), 18)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_total_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_total");
        else if (KineticTheme.hovering(mouseX, mouseY, inputLeftX(), pageInputY(), leftInputWidth(), 20)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_page_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_page");
        else if (KineticTheme.hovering(mouseX, mouseY, questNeedBox.controlX(), questNeedBox.controlY(), questNeedBox.controlWidth(), 18)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_quest_need_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_quest_need");
        else if (KineticTheme.hovering(mouseX, mouseY, displayNameBox.controlX(), displayNameBox.controlY(), displayNameBox.controlWidth(), 18)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_display_name_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_display_name");
        else if (KineticTheme.hovering(mouseX, mouseY, descriptionBox.controlX(), descriptionBox.controlY(), descriptionBox.controlWidth(), 18)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_description_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_description");
        else if (commandManageButton != null && KineticTheme.hovering(mouseX, mouseY, commandManageButton.controlX(), commandManageButton.controlY(), commandManageButton.controlWidth(), 20)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_command_manage_button", "gui.adventuresystems.curios.wallet.shop_command_manage_tooltip");
        else if (KineticTheme.hovering(mouseX, mouseY, questManageButtonX(), questManageButtonY(), 220, 20)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_quest_manage", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_quest");
        else if (KineticTheme.hovering(mouseX, mouseY, contentModeButtonX(), contentModeButtonY(), 180, 20)) {
            String body = draft.mode == Shop.Mode.SELL ? "gui.adventuresystems.curios.wallet.shop_editor_tooltip_sell_multi_switch" : (!draft.selectable && !draft.gacha ? "gui.adventuresystems.curios.wallet.shop_editor_tooltip_single_reward_mode" : (draft.selectable ? "gui.adventuresystems.curios.wallet.shop_editor_tooltip_choice_table" : "gui.adventuresystems.curios.wallet.shop_editor_tooltip_gacha_pool"));
            addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_content_mode_label", body);
        } else if (shouldShowContentManageButton() && KineticTheme.hovering(mouseX, mouseY, contentManageButtonX(), contentModeButtonY(), contentManageButtonWidth(), 20)) {
            if (draft.mode == Shop.Mode.SELL) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_sell_choice_manage", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_sell_choices");
            else if (draft.selectable) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_choice_table_manage", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_choice_table");
            else addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_gacha_pool_manage", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_gacha_pool");
        } else if (KineticTheme.hovering(mouseX, mouseY, saveButtonX(), actionButtonY(), actionButtonWidth(), 20)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_save", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_save");
        return tooltip;
    }

    private void addTooltip(List<Component> tooltip, String titleKey, String bodyKey) {
        tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_tooltip_title", AdventureText.translatable(titleKey)));
        tooltip.add(AdventureText.translatable(bodyKey));
    }
    private static boolean isControlVisible(KineticControl control) {
        return control != null && control.controlVisible();
    }

    private static boolean isControlEnabled(KineticControl control) {
        return control != null && control.isEnabled();
    }

    private static void setControlVisible(KineticControl control, boolean visible) {
        if (control != null) control.setControlVisible(visible);
    }

    private static void setControlEnabled(KineticControl control, boolean enabled) {
        if (control != null) control.setEnabled(enabled);
    }

}

