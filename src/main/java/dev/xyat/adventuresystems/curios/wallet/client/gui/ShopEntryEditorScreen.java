package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.adventuresystems.curios.util.ColorText;
import dev.xyat.adventuresystems.curios.wallet.network.Network;
import dev.xyat.adventuresystems.curios.wallet.shop.Shop;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.overlay.GuiOverlay;
import dev.xyat.kineticcore.api.client.selector.ItemSelectorScreen;
import dev.xyat.kineticcore.api.client.screen.KineticScreen;
import dev.xyat.kineticcore.api.client.widget.KineticWidgets.NumericEditBox;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
final class ShopEntryEditorScreen extends KineticScreen {
    private static final int MIN_PANEL_WIDTH = 632;
    private static final int MIN_PANEL_HEIGHT = 348;
    private final ShopScreen parent;
    private final ShopGuiSupport.EditorDraft draft;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;
    private NumericEditBox priceBox;
    private NumericEditBox countBox;
    private NumericEditBox dailyLimitBox;
    private NumericEditBox totalLimitBox;
    private EditBox pageBox;
    private NumericEditBox questNeedBox;
    private EditBox displayNameBox;
    private EditBox descriptionBox;
    private Button commandManageButton;
    private Button rewardModeButton;
    private Button rewardManageButton;

    ShopEntryEditorScreen(ShopScreen parent, ShopGuiSupport.EditorDraft draft) {
        super(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_title"));
        this.parent = parent;
        this.draft = draft;
        useCanvas(
                640f,
                360f,
                6
        );
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
    protected void buildUi() {
        panelWidth = Math.max(320, Math.min(MIN_PANEL_WIDTH, canvasWidth - 8));
        panelHeight = Math.max(300, Math.min(MIN_PANEL_HEIGHT, canvasHeight - 12));
        left = Math.max(4, (canvasWidth - panelWidth) / 2);
        top = Math.max(4, (canvasHeight - panelHeight) / 2);
        addRenderableWidget(Button.builder(typeButtonText(), b -> {
            syncBasicInputsQuietly();
            draft.mode = draft.mode == Shop.Mode.BUY ? Shop.Mode.SELL : Shop.Mode.BUY;
            draft.gacha = false;
            if (draft.mode == Shop.Mode.BUY) {
                draft.selectable = false;
            }
            rebuildEditorWidgets();
        }).bounds(typeButtonX(), typeButtonY(), buttonColumnWidth(), 20).build());

        addRenderableWidget(Button.builder(itemButtonText(), b -> openMainItemSelector()).bounds(selectItemButtonX(), selectItemButtonY(), buttonColumnWidth(), 20).build());

        addRenderableWidget(Button.builder(currencyButtonText(), b -> Minecraft.getInstance().setScreen(new CurrencyPickerScreen(this, draft))).bounds(selectCurrencyButtonX(), selectCurrencyButtonY(), buttonColumnWidth(), 20).build());
        addRenderableWidget(Button.builder(paymentButtonText(), b -> openPaymentItemSelector()).bounds(selectPaymentButtonX(), selectPaymentButtonY(), buttonColumnWidth(), 20).build());

        priceBox = NumericEditBox.longInteger(
                font,
                inputLeftX(),
                priceInputY(),
                leftInputWidth(),
                18,
                ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_price_label"),
                false,
                1L,
                null
        );
        priceBox.setMaxLength(18);
        priceBox.setLongValue(draft.price);
        addRenderableWidget(priceBox);

        countBox = NumericEditBox.integer(
                font,
                rightInputX(),
                priceInputY(),
                rightInputWidth(),
                18,
                ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_count_label"),
                false,
                1,
                64
        );
        countBox.setMaxLength(2);
        countBox.setIntValue(draft.count);
        addRenderableWidget(countBox);

        dailyLimitBox = NumericEditBox.integer(
                font,
                inputLeftX(),
                dailyInputY(),
                leftInputWidth(),
                18,
                ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_timed_label"),
                false,
                0,
                Integer.MAX_VALUE
        );
        dailyLimitBox.setMaxLength(10);
        dailyLimitBox.setIntValue(draft.dailyLimit);
        addRenderableWidget(dailyLimitBox);

        totalLimitBox = NumericEditBox.integer(
                font,
                rightInputX(),
                dailyInputY(),
                rightInputWidth(),
                18,
                ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_total_label"),
                false,
                0,
                999999999
        );
        totalLimitBox.setMaxLength(9);
        totalLimitBox.setIntValue(draft.totalLimit);
        addRenderableWidget(totalLimitBox);

        pageBox = new EditBox(font, inputLeftX(), pageInputY(), leftInputWidth(), 18, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_page_label"));
        pageBox.setMaxLength(32);
        pageBox.setValue(draft.pageName == null ? "" : draft.pageName);
        addRenderableWidget(pageBox);

        questNeedBox = NumericEditBox.integer(
                font,
                rightInputX(),
                pageInputY(),
                rightInputWidth(),
                18,
                ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_quest_need_label"),
                false,
                0,
                999
        );
        questNeedBox.setMaxLength(3);
        questNeedBox.setIntValue(draft.requiredQuestCount);
        addRenderableWidget(questNeedBox);

        displayNameBox = new EditBox(font, inputLeftX(), displayNameInputY(), fullInputWidth(), 18, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_display_name_label"));
        displayNameBox.setMaxLength(64);
        displayNameBox.setValue(draft.displayName == null ? "" : draft.displayName);
        addRenderableWidget(displayNameBox);

        descriptionBox = new EditBox(font, commandAreaX(), descriptionInputY(), commandAreaWidth(), 18, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_description_label"));
        descriptionBox.setMaxLength(256);
        descriptionBox.setValue(draft.description == null ? "" : draft.description);
        addRenderableWidget(descriptionBox);

        commandManageButton = addRenderableWidget(Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_command_manage_button"), b -> {
            syncBasicInputsQuietly();
            Minecraft.getInstance().setScreen(new CommandManageScreen(this, draft));
        }).bounds(commandManageButtonX(), commandManageButtonY(), commandManageButtonWidth(), 20).build());

        addRenderableWidget(Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_quest_manage"), b -> {
            syncBasicInputsQuietly();
            Minecraft.getInstance().setScreen(new QuestManageScreen(this, draft));
        }).bounds(questManageButtonX(), questManageButtonY(), 160, 20).build());

        rewardModeButton = addRenderableWidget(Button.builder(contentModeButtonText(), b -> cycleContentMode()).bounds(contentModeButtonX(), contentModeButtonY(), 120, 20).build());
        rewardManageButton = addRenderableWidget(Button.builder(contentManageButtonText(), b -> openContentManager()).bounds(contentManageButtonX(), contentModeButtonY(), contentManageButtonWidth(), 20).build());
        refreshContentButtons();

        addRenderableWidget(Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_save"), b -> saveDraft()).bounds(saveButtonX(), actionButtonY(), 76, 20).build());
        addRenderableWidget(Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_cancel"), b -> Minecraft.getInstance().setScreen(parent)).bounds(cancelButtonX(), actionButtonY(), 76, 20).build());

    }

    private void rebuildEditorWidgets() {
        clearWidgets();
        buildUi();
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
        Minecraft.getInstance().setScreen(new ItemSelectorScreen(this, selection -> {
            if (!selection.isItem()) return;
            ItemStack stack = selection.stack();
            String spec = ShopGuiSupport.selectedStackSpec(stack);
            if (!spec.isBlank()) {
                draft.itemId = spec;
                draft.count = Math.max(1, Math.min(64, stack.getCount()));
                if (countBox != null) countBox.setValue(String.valueOf(draft.count));
                Minecraft.getInstance().setScreen(this);
            }
        }));
    }

    private void openPaymentItemSelector() {
        Minecraft.getInstance().setScreen(new ItemSelectorScreen(this, selection -> {
            if (!selection.isItem()) return;
            ItemStack stack = selection.stack();
            String spec = ShopGuiSupport.selectedStackSpec(stack);
            if (!spec.isBlank()) {
                draft.currencyId = spec;
                Minecraft.getInstance().setScreen(this);
            }
        }));
    }

    private void syncBasicInputsQuietly() {
        if (pageBox != null) draft.pageName = pageBox.getValue().trim();
        if (displayNameBox != null) draft.displayName = displayNameBox.getValue().trim();
        if (descriptionBox != null) draft.description = descriptionBox.getValue().trim();

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
        if (rewardModeButton != null) rewardModeButton.setMessage(contentModeButtonText());
        if (rewardManageButton != null) {
            rewardManageButton.visible = shouldShowContentManageButton();
            rewardManageButton.active = shouldShowContentManageButton();
            rewardManageButton.setMessage(contentManageButtonText());
        }
    }

    private Component typeButtonText() {
        return draft.mode == Shop.Mode.BUY ? ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_type_buy") : ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_type_sell");
    }

    private Component itemButtonText() {
        return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_button_item", shortStackName(draft.itemId, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_none").getString()));
    }

    private Component currencyButtonText() {
        return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_button_currency", shortStackName(currentCurrencyId(), ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_none").getString()));
    }

    private Component paymentButtonText() {
        return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_button_payment", shortStackName(currentBarterItemId(), ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_none").getString()));
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
            return ColorText.translatable(draft.selectable ? "gui.adventuresystems.curios.wallet.shop_editor_sell_multi_on" : "gui.adventuresystems.curios.wallet.shop_editor_sell_multi_off");
        }
        if (draft.selectable) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_choice_table_active");
        if (draft.gacha) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_gacha_pool_active");
        return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_reward_single_active");
    }

    private Component contentManageButtonText() {
        if (draft.mode == Shop.Mode.SELL) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_sell_choice_manage");
        if (draft.selectable) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_choice_table_manage");
        return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_gacha_pool_manage");
    }

    private boolean shouldShowContentManageButton() {
        if (draft.mode == Shop.Mode.SELL) return draft.selectable;
        return draft.gacha || draft.selectable;
    }

    private Component contentSectionTitle() {
        if (draft.mode == Shop.Mode.SELL) {
            return draft.selectable ? ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_sell_content_section") : ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_single_content_section");
        }
        if (draft.gacha) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_gacha_content_section");
        if (draft.selectable) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_choice_content_section");
        return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_single_content_section");
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
            Minecraft.getInstance().setScreen(new RewardPoolScreen(this, draft));
            return;
        }
        if (draft.selectable) {
            normalizeChoiceRewards();
            Minecraft.getInstance().setScreen(new RewardPoolScreen(this, draft));
            return;
        }
        if (draft.gacha) Minecraft.getInstance().setScreen(new RewardPoolScreen(this, draft));
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
            GuiOverlay.toast(
                    "shop_editor_error",
                    ColorText.translatable(
                            "gui.adventuresystems.curios.wallet.shop_editor_invalid_input"
                    ),
                    GuiOverlay.Position.BOTTOM_CENTER,
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
        draft.pageName = pageBox.getValue().trim();
        draft.displayName = displayNameBox.getValue().trim();
        draft.description = descriptionBox.getValue().trim();
        draft.syncDirectCommandFromList();
        draft.requiredQuestCount = requiredQuestCount;

        if (draft.itemId == null || draft.itemId.isBlank()) {
            GuiOverlay.toast("shop_editor_error", ColorText.translatable("msg.adventuresystems.curios.wallet.shop_editor_missing_item"), GuiOverlay.Position.BOTTOM_CENTER, 2500, 0, -30);
            return;
        }
        if (draft.currencyId == null || draft.currencyId.isBlank()) {
            GuiOverlay.toast("shop_editor_error", ColorText.translatable("msg.adventuresystems.curios.wallet.shop_editor_missing_currency"), GuiOverlay.Position.BOTTOM_CENTER, 2500, 0, -30);
            return;
        }
        if (draft.price <= 0L || draft.count < 1 || draft.count > 64 || draft.dailyLimit < 0 || draft.totalLimit < 0 || draft.requiredQuestCount < 0) {
            GuiOverlay.toast("shop_editor_error", ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_invalid_input"), GuiOverlay.Position.BOTTOM_CENTER, 2500, 0, -30);
            return;
        }
        if (draft.mode == Shop.Mode.SELL && draft.selectable && !draft.rewards.isEmpty()) normalizeChoiceRewards();
        if (draft.mode == Shop.Mode.BUY && draft.selectable) normalizeChoiceRewards();
        if ((draft.gacha || draft.selectable) && draft.rewards.isEmpty()) {
            String key;
            if (draft.mode == Shop.Mode.SELL) key = "msg.adventuresystems.curios.wallet.shop_editor_missing_sell_choices";
            else if (draft.selectable) key = "msg.adventuresystems.curios.wallet.shop_editor_missing_choice_rewards";
            else key = "msg.adventuresystems.curios.wallet.shop_editor_missing_rewards";
            GuiOverlay.toast("shop_editor_error", ColorText.translatable(key), GuiOverlay.Position.BOTTOM_CENTER, 2500, 0, -30);
            return;
        }
        if ((draft.mode == Shop.Mode.SELL || draft.selectable) && hasEmptyReward()) {
            GuiOverlay.toast("shop_editor_error", ColorText.translatable(draft.mode == Shop.Mode.SELL ? "msg.adventuresystems.curios.wallet.shop_sell_choice_no_empty" : "msg.adventuresystems.curios.wallet.shop_choice_no_empty"), GuiOverlay.Position.BOTTOM_CENTER, 2500, 0, -30);
            return;
        }
        String rewardsText = (draft.gacha || draft.selectable) ? draft.buildRewardsText() : "";
        Network.sendSaveShopEntry(draft.mode, draft.index, draft.itemId, draft.currencyId, draft.price, draft.count, draft.dailyLimit, draft.totalLimit, draft.buildQuestText(), draft.gacha, rewardsText);
        commitDraft();
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    protected void renderCanvasBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ShopGuiSupport.renderBox(graphics, left, top, panelWidth, panelHeight, ShopGuiSupport.PANEL_BG);
        graphics.drawCenteredString(font, title, left + panelWidth / 2, top + 12, ShopGuiSupport.GOLD);

        renderSection(graphics, left + 12, basicSectionY(), basicSectionHeight(), ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_basic_info"));
        renderButtonItemIcon(graphics, draft.itemId, selectItemButtonX(), selectItemButtonY());
        renderButtonItemIcon(graphics, currentCurrencyId(), selectCurrencyButtonX(), selectCurrencyButtonY());
        renderButtonItemIcon(graphics, currentBarterItemId(), selectPaymentButtonX(), selectPaymentButtonY());

        renderSection(graphics, left + 12, priceSectionY(), priceSectionHeight(), ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_price_limit"));
        graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_price_label"), labelLeftX(), priceInputY() + 5, ShopGuiSupport.CYAN, true);
        graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_count_label"), rightLabelX(), priceInputY() + 5, ShopGuiSupport.CYAN, true);
        graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_timed_label"), labelLeftX(), dailyInputY() + 5, ShopGuiSupport.CYAN, true);
        graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_total_label"), rightLabelX(), dailyInputY() + 5, ShopGuiSupport.CYAN, true);
        graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_page_label"), labelLeftX(), pageInputY() + 5, ShopGuiSupport.CYAN, true);
        graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_quest_need_label"), rightLabelX(), pageInputY() + 5, ShopGuiSupport.CYAN, true);
        graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_display_name_label"), labelLeftX(), displayNameInputY() + 5, ShopGuiSupport.CYAN, true);
        graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_command_manage_label"), commandManageButtonX(), commandManageButtonY() - 14, ShopGuiSupport.CYAN, true);
        graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_command_count", draft.commands.size()), commandManageButtonX(), commandManageButtonY() + 26, ShopGuiSupport.TEXT_WHITE, true);
        graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_description_label"), commandAreaX(), descriptionInputY() - 12, ShopGuiSupport.CYAN, true);

        renderSection(graphics, left + 12, questSectionY(), questSectionHeight(), ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_requirements"));
        graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_quest_count", draft.questIds.size()), innerLeft(), questSectionY() + 28, ShopGuiSupport.TEXT_WHITE, true);
        graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_quest_rule", draft.requiredQuestCount <= 0 ? draft.questIds.size() : Math.min(draft.requiredQuestCount, draft.questIds.size())), rightLabelX(), questSectionY() + 28, ShopGuiSupport.TEXT_GRAY, true);

        renderSection(graphics, left + 12, contentSectionY(), contentSectionHeight(), contentSectionTitle());
        graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_content_mode_label"), labelLeftX(), contentModeButtonY() + 5, ShopGuiSupport.CYAN, true);
        graphics.drawString(font, contentCountText(), contentPreviewX(), contentPreviewY() + 5, ShopGuiSupport.TEXT_WHITE, true);
    }

    private Component contentCountText() {
        if (draft.mode == Shop.Mode.SELL) {
            if (!draft.selectable) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_single_reward_hint");
            return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_sell_choice_count", draft.rewards.size());
        }
        if (draft.gacha) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_gacha_reward_count", draft.rewards.size());
        if (draft.selectable) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_choice_reward_count", draft.rewards.size());
        return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_editor_single_reward_hint");
    }


    private void renderButtonItemIcon(GuiGraphics graphics, String id, int buttonX, int buttonY) {
        if (id == null || id.isBlank()) return;
        graphics.renderItem(ShopGuiSupport.stack(id), buttonX + buttonColumnWidth() - 20, buttonY + 2);
    }


    private void renderSection(GuiGraphics graphics, int x, int y, int height, Component title) {
        ShopGuiSupport.renderBox(graphics, x, y, panelWidth - 24, height, 0xEE111111);
        graphics.drawString(font, title, x + 10, y + 5, ShopGuiSupport.GOLD, true);
    }

    @Override
    protected void renderTooltips(GuiGraphics graphics, int scaledMouseX, int scaledMouseY, int rawMouseX, int rawMouseY) {
        ItemStack stack = hoveredEditorItem(scaledMouseX, scaledMouseY);
        if (!stack.isEmpty()) {
            GuiOverlay.requestItemTooltip(stack, rawMouseX, rawMouseY);
            return;
        }
        List<Component> tooltip = editorTooltipAt(scaledMouseX, scaledMouseY);
        if (!tooltip.isEmpty()) GuiOverlay.requestTooltip(tooltip, rawMouseX, rawMouseY);
    }

    private ItemStack hoveredEditorItem(int mouseX, int mouseY) {
        if (draft.itemId != null && !draft.itemId.isBlank() && GuiTheme.hovering(mouseX, mouseY, selectItemButtonX() + buttonColumnWidth() - 20, selectItemButtonY() + 2, 16, 16)) return ShopGuiSupport.stack(draft.itemId);
        if (!currentCurrencyId().isBlank() && GuiTheme.hovering(mouseX, mouseY, selectCurrencyButtonX() + buttonColumnWidth() - 20, selectCurrencyButtonY() + 2, 16, 16)) return ShopGuiSupport.stack(currentCurrencyId());
        if (!currentBarterItemId().isBlank() && GuiTheme.hovering(mouseX, mouseY, selectPaymentButtonX() + buttonColumnWidth() - 20, selectPaymentButtonY() + 2, 16, 16)) return ShopGuiSupport.stack(currentBarterItemId());
        return ItemStack.EMPTY;
    }

    private List<Component> editorTooltipAt(int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (GuiTheme.hovering(mouseX, mouseY, typeButtonX(), typeButtonY(), buttonColumnWidth(), 20)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_type_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_type");
        else if (GuiTheme.hovering(mouseX, mouseY, selectItemButtonX(), selectItemButtonY(), buttonColumnWidth(), 20)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_item_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_item");
        else if (GuiTheme.hovering(mouseX, mouseY, selectCurrencyButtonX(), selectCurrencyButtonY(), buttonColumnWidth(), 20)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_currency_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_currency");
        else if (GuiTheme.hovering(mouseX, mouseY, selectPaymentButtonX(), selectPaymentButtonY(), buttonColumnWidth(), 20)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_payment_item", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_payment_item");
        else if (GuiTheme.hovering(mouseX, mouseY, priceBox.getX(), priceBox.getY(), priceBox.getWidth(), 18)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_price_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_price");
        else if (GuiTheme.hovering(mouseX, mouseY, countBox.getX(), countBox.getY(), countBox.getWidth(), 18)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_count_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_count");
        else if (GuiTheme.hovering(mouseX, mouseY, dailyLimitBox.getX(), dailyLimitBox.getY(), dailyLimitBox.getWidth(), 18)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_timed_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_timed");
        else if (GuiTheme.hovering(mouseX, mouseY, totalLimitBox.getX(), totalLimitBox.getY(), totalLimitBox.getWidth(), 18)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_total_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_total");
        else if (GuiTheme.hovering(mouseX, mouseY, pageBox.getX(), pageBox.getY(), pageBox.getWidth(), 18)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_page_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_page");
        else if (GuiTheme.hovering(mouseX, mouseY, questNeedBox.getX(), questNeedBox.getY(), questNeedBox.getWidth(), 18)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_quest_need_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_quest_need");
        else if (GuiTheme.hovering(mouseX, mouseY, displayNameBox.getX(), displayNameBox.getY(), displayNameBox.getWidth(), 18)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_display_name_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_display_name");
        else if (GuiTheme.hovering(mouseX, mouseY, descriptionBox.getX(), descriptionBox.getY(), descriptionBox.getWidth(), 18)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_description_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_description");
        else if (commandManageButton != null && GuiTheme.hovering(mouseX, mouseY, commandManageButton.getX(), commandManageButton.getY(), commandManageButton.getWidth(), 20)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_command_manage_button", "gui.adventuresystems.curios.wallet.shop_command_manage_tooltip");
        else if (GuiTheme.hovering(mouseX, mouseY, questManageButtonX(), questManageButtonY(), 220, 20)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_quest_manage", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_quest");
        else if (GuiTheme.hovering(mouseX, mouseY, contentModeButtonX(), contentModeButtonY(), 180, 20)) {
            String body = draft.mode == Shop.Mode.SELL ? "gui.adventuresystems.curios.wallet.shop_editor_tooltip_sell_multi_switch" : (!draft.selectable && !draft.gacha ? "gui.adventuresystems.curios.wallet.shop_editor_tooltip_single_reward_mode" : (draft.selectable ? "gui.adventuresystems.curios.wallet.shop_editor_tooltip_choice_table" : "gui.adventuresystems.curios.wallet.shop_editor_tooltip_gacha_pool"));
            addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_content_mode_label", body);
        } else if (shouldShowContentManageButton() && GuiTheme.hovering(mouseX, mouseY, contentManageButtonX(), contentModeButtonY(), contentManageButtonWidth(), 20)) {
            if (draft.mode == Shop.Mode.SELL) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_sell_choice_manage", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_sell_choices");
            else if (draft.selectable) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_choice_table_manage", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_choice_table");
            else addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_gacha_pool_manage", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_gacha_pool");
        } else if (GuiTheme.hovering(mouseX, mouseY, saveButtonX(), actionButtonY(), 92, 20)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_save", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_save");
        return tooltip;
    }

    private void addTooltip(List<Component> tooltip, String titleKey, String bodyKey) {
        tooltip.add(ColorText.translatable(titleKey).withStyle(ChatFormatting.GOLD));
        tooltip.add(ColorText.translatable(bodyKey));
    }
}

