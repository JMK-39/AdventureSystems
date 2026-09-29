package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.adventuresystems.curios.wallet.network.Network;
import dev.xyat.adventuresystems.curios.wallet.shop.Shop;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.ui.NumberType;
import dev.xyat.kineticcore.api.client.gui.widget.KineticButton;
import dev.xyat.kineticcore.api.client.gui.widget.KineticNumberField;
import dev.xyat.kineticcore.api.client.gui.widget.KineticTextField;
import dev.xyat.kineticcore.api.client.gui.overlay.KineticOverlays;
import dev.xyat.kineticcore.api.client.gui.selector.KineticSelectors;
import dev.xyat.kineticcore.api.text.KineticI18n;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import dev.xyat.kineticcore.api.client.gui.widget.KineticControl;

final class ShopEntryEditorScreen extends KineticPage {
    private static final int MIN_PANEL_WIDTH = 632;
    private static final int MIN_PANEL_HEIGHT = 348;
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
    private KineticNumberField questNeedBox;
    private KineticTextField displayNameBox;
    private KineticTextField descriptionBox;
    private KineticButton commandManageButton;
    private KineticButton rewardModeButton;
    private KineticButton rewardManageButton;
    private KineticUi buildingUi;

    ShopEntryEditorScreen(ShopScreen parent, ShopGuiSupport.EditorDraft draft) {
        super(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_title"));
        this.parent = parent;
        this.draft = draft;
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

        addButton(typeButtonX(), typeButtonY(), buttonColumnWidth(), typeButtonText(), null, () -> {
            syncBasicInputsQuietly();
            draft.mode = draft.mode == Shop.Mode.BUY ? Shop.Mode.SELL : Shop.Mode.BUY;
            draft.gacha = false;
            if (draft.mode == Shop.Mode.BUY) draft.selectable = false;
            rebuildEditorWidgets();
        });
        addButton(selectItemButtonX(), selectItemButtonY(), buttonColumnWidth(), itemButtonText(), null, this::openMainItemSelector);
        addButton(selectCurrencyButtonX(), selectCurrencyButtonY(), buttonColumnWidth(), currencyButtonText(), null, () -> openChild(new CurrencyPickerScreen(draft)));
        addButton(selectPaymentButtonX(), selectPaymentButtonY(), buttonColumnWidth(), paymentButtonText(), null, this::openPaymentItemSelector);

        priceBox = addLongField(
                inputLeftX(),
                priceInputY(),
                leftInputWidth(),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_price_label"),
                false,
                1L,
                null,
                null
        );
        priceBox.limitTextLength(18);
        priceBox.setLongValue(draft.price);

        countBox = addIntegerField(
                rightInputX(),
                priceInputY(),
                rightInputWidth(),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_count_label"),
                false,
                1,
                64,
                null
        );
        countBox.limitTextLength(2);
        countBox.setIntValue(draft.count);

        dailyLimitBox = addIntegerField(
                inputLeftX(),
                dailyInputY(),
                leftInputWidth(),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_timed_label"),
                false,
                0,
                Integer.MAX_VALUE,
                null
        );
        dailyLimitBox.limitTextLength(10);
        dailyLimitBox.setIntValue(draft.dailyLimit);

        totalLimitBox = addIntegerField(
                rightInputX(),
                dailyInputY(),
                rightInputWidth(),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_total_label"),
                false,
                0,
                999999999,
                null
        );
        totalLimitBox.limitTextLength(9);
        totalLimitBox.setIntValue(draft.totalLimit);

        pageBox = addTextField(
                inputLeftX(),
                pageInputY(),
                leftInputWidth(),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_page_label"),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_page_label"),
                null,
                null
        );
        pageBox.limitTextLength(32);
        pageBox.setTextValue(draft.pageName == null ? "" : draft.pageName);

        questNeedBox = addIntegerField(
                rightInputX(),
                pageInputY(),
                rightInputWidth(),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_quest_need_label"),
                false,
                0,
                999,
                null
        );
        questNeedBox.limitTextLength(3);
        questNeedBox.setIntValue(draft.requiredQuestCount);

        displayNameBox = addTextField(
                inputLeftX(),
                displayNameInputY(),
                fullInputWidth(),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_display_name_label"),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_display_name_label"),
                null,
                null
        );
        displayNameBox.limitTextLength(64);
        displayNameBox.setTextValue(draft.displayName == null ? "" : draft.displayName);

        descriptionBox = addTextField(
                commandAreaX(),
                descriptionInputY(),
                commandAreaWidth(),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_description_label"),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_description_label"),
                null,
                null
        );
        descriptionBox.limitTextLength(256);
        descriptionBox.setTextValue(draft.description == null ? "" : draft.description);

        commandManageButton = addButton(
                commandManageButtonX(),
                commandManageButtonY(),
                commandManageButtonWidth(),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_command_manage_button"),
                null,
                () -> {
                    syncBasicInputsQuietly();
                    openChild(new CommandManageScreen(draft));
                }
        );

        addButton(
                questManageButtonX(),
                questManageButtonY(),
                160,
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_quest_manage"),
                null,
                () -> {
                    syncBasicInputsQuietly();
                    openChild(new QuestManageScreen(draft));
                }
        );

        rewardModeButton = addButton(contentModeButtonX(), contentModeButtonY(), 120, contentModeButtonText(), null, this::cycleContentMode);
        rewardManageButton = addButton(contentManageButtonX(), contentModeButtonY(), contentManageButtonWidth(), contentManageButtonText(), null, this::openContentManager);
        refreshContentButtons();

        addButton(saveButtonX(), actionButtonY(), 76, KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_save"), null, this::saveDraft);
        addButton(cancelButtonX(), actionButtonY(), 76, KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_cancel"), null, this::cancelAndReturn);
    }

    private void rebuildEditorWidgets() {
        rebuild();
    }

    private KineticButton addButton(int x, int y, int width, Component label, Component tooltip, Runnable action) {
        return buildingUi.button(x, y, width).text(label).tooltip(tooltip).onClick(action).build();
    }

    private KineticNumberField addLongField(int x, int y, int width, Component label,
                                            boolean allowNegative, long minimum, Object maximum, Component tooltip) {
        return buildingUi.numberField(x, y, width, NumberType.LONG)
                .label(label).allowNegative(allowNegative).range(minimum, maximum instanceof Number n ? n : null)
                .tooltip(tooltip).firstShownTextAsDefault().build();
    }

    private KineticNumberField addIntegerField(int x, int y, int width, Component label,
                                               boolean allowNegative, int minimum, int maximum, Component tooltip) {
        return buildingUi.numberField(x, y, width, NumberType.INT)
                .label(label).allowNegative(allowNegative).range(minimum, maximum).tooltip(tooltip).firstShownTextAsDefault().build();
    }

    private KineticTextField addTextField(int x, int y, int width, Component label,
                                         Component placeholder, Object validator, Component tooltip) {
        return buildingUi.textField(x, y, width).label(label).placeholder(placeholder).tooltip(tooltip).firstShownTextAsDefault().build();
    }

    private int basicSectionY() { return top + 28; }
    private int basicSectionHeight() { return 54; }
    private int priceSectionY() { return basicSectionY() + basicSectionHeight() + 6; }
    private int priceSectionHeight() { return 122; }
    private int questSectionY() { return priceSectionY() + priceSectionHeight() + 6; }
    private int questSectionHeight() { return 52; }
    private int contentSectionY() { return questSectionY() + questSectionHeight() + 6; }
    private int contentSectionHeight() { return Math.max(46, top + panelHeight - contentSectionY() - 10); }
    private int innerLeft() { return left + 14; }
    private int innerRight() { return left + panelWidth - 14; }
    private int innerWidth() { return Math.max(1, innerRight() - innerLeft()); }
    private int buttonGap() { return 8; }
    private int buttonColumnWidth() { return Math.max(82, (innerWidth() - buttonGap() * 3) / 4); }
    private int typeButtonX() { return innerLeft(); }
    private int typeButtonY() { return basicSectionY() + 22; }
    private int selectItemButtonX() { return typeButtonX() + buttonColumnWidth() + buttonGap(); }
    private int selectItemButtonY() { return typeButtonY(); }
    private int selectCurrencyButtonX() { return typeButtonX() + (buttonColumnWidth() + buttonGap()) * 2; }
    private int selectCurrencyButtonY() { return typeButtonY(); }
    private int selectPaymentButtonX() { return typeButtonX() + (buttonColumnWidth() + buttonGap()) * 3; }
    private int selectPaymentButtonY() { return typeButtonY(); }
    private int fieldLabelWidth() { return 58; }
    private int columnGap() { return 24; }
    private int leftAreaWidth() { return Math.max(270, (innerWidth() - columnGap()) / 2); }
    private int commandAreaX() { return innerLeft() + leftAreaWidth() + columnGap(); }
    private int commandAreaWidth() { return Math.max(230, innerRight() - commandAreaX()); }
    private int leftColumnGap() { return 18; }
    private int leftColumnWidth() { return Math.max(132, (leftAreaWidth() - leftColumnGap()) / 2); }
    private int labelLeftX() { return innerLeft(); }
    private int inputLeftX() { return labelLeftX() + fieldLabelWidth() + 6; }
    private int rightLabelX() { return innerLeft() + leftColumnWidth() + leftColumnGap(); }
    private int rightInputX() { return rightLabelX() + fieldLabelWidth() + 6; }
    private int leftInputWidth() { return Math.max(72, leftColumnWidth() - fieldLabelWidth() - 6); }
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
    private int saveButtonX() { return left + panelWidth - 170; }
    private int cancelButtonX() { return left + panelWidth - 88; }

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

    private void openPaymentItemSelector() {
        KineticSelectors.openItemSelector(selection -> {
            if (!selection.isItem()) return;
            ItemStack stack = selection.stack();
            String spec = ShopGuiSupport.selectedStackSpec(stack);
            if (!spec.isBlank()) draft.currencyId = spec;
        });
    }

    private void syncBasicInputsQuietly() {
        if (pageBox != null) draft.pageName = pageBox.textValue().trim();
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
        return draft.mode == Shop.Mode.BUY ? KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_type_buy") : KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_type_sell");
    }

    private Component itemButtonText() {
        return KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_button_item", shortStackName(draft.itemId, KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_none").getString()));
    }

    private Component currencyButtonText() {
        return KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_button_currency", shortStackName(currentCurrencyId(), KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_none").getString()));
    }

    private Component paymentButtonText() {
        return KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_button_payment", shortStackName(currentBarterItemId(), KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_none").getString()));
    }

    private String currentCurrencyId() {
        return ShopGuiSupport.isConfiguredCurrency(draft.currencyId) ? draft.currencyId : "";
    }

    private String currentBarterItemId() {
        return draft.currencyId == null || draft.currencyId.isBlank() || ShopGuiSupport.isConfiguredCurrency(draft.currencyId) ? "" : draft.currencyId;
    }

    private String shortStackName(String id, String fallback) {
        if (id == null || id.isBlank()) return fallback;
        String name = ShopGuiSupport.stackName(id);
        return name.isBlank() ? fallback : name;
    }



    private Component contentModeButtonText() {
        if (draft.mode == Shop.Mode.SELL) {
            return KineticI18n.translatable(draft.selectable ? "gui.adventuresystems.curios.wallet.shop_editor_sell_multi_on" : "gui.adventuresystems.curios.wallet.shop_editor_sell_multi_off");
        }
        if (draft.selectable) return KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_choice_table_active");
        if (draft.gacha) return KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_gacha_pool_active");
        return KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_reward_single_active");
    }

    private Component contentManageButtonText() {
        if (draft.mode == Shop.Mode.SELL) return KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_sell_choice_manage");
        if (draft.selectable) return KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_choice_table_manage");
        return KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_gacha_pool_manage");
    }

    private boolean shouldShowContentManageButton() {
        if (draft.mode == Shop.Mode.SELL) return draft.selectable;
        return draft.gacha || draft.selectable;
    }

    private Component contentSectionTitle() {
        if (draft.mode == Shop.Mode.SELL) {
            return draft.selectable ? KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_sell_content_section") : KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_single_content_section");
        }
        if (draft.gacha) return KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_gacha_content_section");
        if (draft.selectable) return KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_choice_content_section");
        return KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_single_content_section");
    }

    private void cycleContentMode() {
        syncBasicInputsQuietly();
        if (draft.mode == Shop.Mode.SELL) {
            draft.gacha = false;
            draft.selectable = !draft.selectable;
            refreshContentButtons();
            return;
        }
        if (!draft.selectable && !draft.gacha) {
            draft.selectable = true;
            normalizeChoiceRewards();
        } else if (draft.selectable) {
            draft.selectable = false;
            draft.gacha = true;
        } else {
            draft.gacha = false;
        }
        refreshContentButtons();
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
                    KineticI18n.translatable(
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
        draft.pageName = pageBox.textValue().trim();
        draft.displayName = displayNameBox.textValue().trim();
        draft.description = descriptionBox.textValue().trim();
        draft.syncDirectCommandFromList();
        draft.requiredQuestCount = requiredQuestCount;

        if (draft.itemId == null || draft.itemId.isBlank()) {
            KineticOverlays.toast("shop_editor_error", KineticI18n.translatable("msg.adventuresystems.curios.wallet.shop_editor_missing_item"), KineticOverlays.Position.BOTTOM_CENTER, 2500, 0, -30);
            return;
        }
        if (draft.currencyId == null || draft.currencyId.isBlank()) {
            KineticOverlays.toast("shop_editor_error", KineticI18n.translatable("msg.adventuresystems.curios.wallet.shop_editor_missing_currency"), KineticOverlays.Position.BOTTOM_CENTER, 2500, 0, -30);
            return;
        }
        if (draft.price <= 0L || draft.count < 1 || draft.count > 64 || draft.dailyLimit < 0 || draft.totalLimit < 0 || draft.requiredQuestCount < 0) {
            KineticOverlays.toast("shop_editor_error", KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_invalid_input"), KineticOverlays.Position.BOTTOM_CENTER, 2500, 0, -30);
            return;
        }
        if (draft.mode == Shop.Mode.SELL && draft.selectable && !draft.rewards.isEmpty()) normalizeChoiceRewards();
        if (draft.mode == Shop.Mode.BUY && draft.selectable) normalizeChoiceRewards();
        if ((draft.gacha || draft.selectable) && draft.rewards.isEmpty()) {
            String key;
            if (draft.mode == Shop.Mode.SELL) key = "msg.adventuresystems.curios.wallet.shop_editor_missing_sell_choices";
            else if (draft.selectable) key = "msg.adventuresystems.curios.wallet.shop_editor_missing_choice_rewards";
            else key = "msg.adventuresystems.curios.wallet.shop_editor_missing_rewards";
            KineticOverlays.toast("shop_editor_error", KineticI18n.translatable(key), KineticOverlays.Position.BOTTOM_CENTER, 2500, 0, -30);
            return;
        }
        if ((draft.mode == Shop.Mode.SELL || draft.selectable) && hasEmptyReward()) {
            KineticOverlays.toast("shop_editor_error", KineticI18n.translatable(draft.mode == Shop.Mode.SELL ? "msg.adventuresystems.curios.wallet.shop_sell_choice_no_empty" : "msg.adventuresystems.curios.wallet.shop_choice_no_empty"), KineticOverlays.Position.BOTTOM_CENTER, 2500, 0, -30);
            return;
        }
        String rewardsText = (draft.gacha || draft.selectable) ? draft.buildRewardsText() : "";
        if (draft.index < 0) {
            draft.index = parent.entryCount(draft.mode);
        }
        Network.sendSaveShopEntry(draft.mode, draft.index, draft.itemId, draft.currencyId, draft.price, draft.count, draft.dailyLimit, draft.totalLimit, draft.buildQuestText(), draft.gacha, rewardsText);
        commitDraft();
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
        graphics.centeredText(title(), left + panelWidth / 2, top + 12, KineticTheme.current().text(), false);

        renderSection(graphics, left + 12, basicSectionY(), basicSectionHeight(), KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_basic_info"));
        renderButtonItemIcon(graphics, draft.itemId, selectItemButtonX(), selectItemButtonY());
        renderButtonItemIcon(graphics, currentCurrencyId(), selectCurrencyButtonX(), selectCurrencyButtonY());
        renderButtonItemIcon(graphics, currentBarterItemId(), selectPaymentButtonX(), selectPaymentButtonY());

        renderSection(graphics, left + 12, priceSectionY(), priceSectionHeight(), KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_price_limit"));
        graphics.text(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_price_label"), labelLeftX(), priceInputY() + 5, KineticTheme.current().mutedText(), true);
        graphics.text(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_count_label"), rightLabelX(), priceInputY() + 5, KineticTheme.current().mutedText(), true);
        graphics.text(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_timed_label"), labelLeftX(), dailyInputY() + 5, KineticTheme.current().mutedText(), true);
        graphics.text(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_total_label"), rightLabelX(), dailyInputY() + 5, KineticTheme.current().mutedText(), true);
        graphics.text(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_page_label"), labelLeftX(), pageInputY() + 5, KineticTheme.current().mutedText(), true);
        graphics.text(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_quest_need_label"), rightLabelX(), pageInputY() + 5, KineticTheme.current().mutedText(), true);
        graphics.text(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_display_name_label"), labelLeftX(), displayNameInputY() + 5, KineticTheme.current().mutedText(), true);
        graphics.text(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_command_manage_label"), commandManageButtonX(), commandManageButtonY() - 14, KineticTheme.current().mutedText(), true);
        graphics.text(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_command_count", draft.commands.size()), commandManageButtonX(), commandManageButtonY() + 26, KineticTheme.current().text(), true);
        graphics.text(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_description_label"), commandAreaX(), descriptionInputY() - 12, KineticTheme.current().mutedText(), true);

        renderSection(graphics, left + 12, questSectionY(), questSectionHeight(), KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_requirements"));
        graphics.text(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_quest_count", draft.questIds.size()), innerLeft(), questSectionY() + 28, KineticTheme.current().text(), true);
        graphics.text(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_quest_rule", draft.requiredQuestCount <= 0 ? draft.questIds.size() : Math.min(draft.requiredQuestCount, draft.questIds.size())), rightLabelX(), questSectionY() + 28, KineticTheme.current().mutedText(), true);

        renderSection(graphics, left + 12, contentSectionY(), contentSectionHeight(), contentSectionTitle());
        graphics.text(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_content_mode_label"), labelLeftX(), contentModeButtonY() + 5, KineticTheme.current().mutedText(), true);
        graphics.text(contentCountText(), contentPreviewX(), contentPreviewY() + 5, KineticTheme.current().text(), true);
    }

    private Component contentCountText() {
        if (draft.mode == Shop.Mode.SELL) {
            if (!draft.selectable) return KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_single_reward_hint");
            return KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_sell_choice_count", draft.rewards.size());
        }
        if (draft.gacha) return KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_gacha_reward_count", draft.rewards.size());
        if (draft.selectable) return KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_choice_reward_count", draft.rewards.size());
        return KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_single_reward_hint");
    }


    private void renderButtonItemIcon(KineticGraphics graphics, String id, int buttonX, int buttonY) {
        if (id == null || id.isBlank()) return;
        graphics.item(ShopGuiSupport.stack(id), buttonX + buttonColumnWidth() - 20, buttonY + 2);
    }


    private void renderSection(KineticGraphics graphics, int x, int y, int height, Component title) {
        KineticTheme.panelAlt(graphics, x, y, panelWidth - 24, height);
        graphics.text(title, x + 10, y + 5, KineticTheme.current().text(), true);
    }

    @Override
    protected void renderTooltips(int mouseX, int mouseY) {
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
        else if (KineticTheme.hovering(mouseX, mouseY, pageBox.controlX(), pageBox.controlY(), pageBox.controlWidth(), 18)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_page_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_page");
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
        } else if (KineticTheme.hovering(mouseX, mouseY, saveButtonX(), actionButtonY(), 92, 20)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_save", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_save");
        return tooltip;
    }

    private void addTooltip(List<Component> tooltip, String titleKey, String bodyKey) {
        tooltip.add(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_editor_tooltip_title", KineticI18n.translatable(titleKey)));
        tooltip.add(KineticI18n.translatable(bodyKey));
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

