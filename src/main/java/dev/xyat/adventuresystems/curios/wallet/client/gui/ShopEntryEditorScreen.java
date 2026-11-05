package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.adventuresystems.curios.wallet.network.Network;
import dev.xyat.adventuresystems.curios.wallet.shop.Shop;
import dev.xyat.kineticcore.api.client.gui.theme.KineticSizes;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.input.MouseInput;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.ui.NumberType;
import dev.xyat.kineticcore.api.client.gui.widget.KineticButton;
import dev.xyat.kineticcore.api.client.gui.widget.KineticNumberField;
import dev.xyat.kineticcore.api.client.gui.widget.KineticTextField;
import dev.xyat.kineticcore.api.client.gui.widget.list.KineticRowList;
import dev.xyat.kineticcore.api.client.gui.overlay.KineticOverlays;
import dev.xyat.kineticcore.api.client.gui.selector.KineticSelectors;
import dev.xyat.adventuresystems.text.AdventureText;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import dev.xyat.kineticcore.api.client.gui.widget.KineticControl;

/**
 * One shop entry on one page. Left: type and page, the goods, payment and display icon as item slots, the numbers,
 * name and description, then the content mode with its reward items. Right: the command rewards and the required
 * quests, listed in full; clicking a list (or its Edit button) opens its manager.
 */
final class ShopEntryEditorScreen extends KineticPage {
    private static final int CANVAS_W = 640, CANVAS_H = 360;
    private static final int SLOT = KineticSizes.ITEM_SLOT, PITCH = KineticSizes.ITEM_PITCH;
    // Left column: a fixed label column, then the controls.
    private static final int LEFT_X = 4, LEFT_RIGHT = 316, LABEL_W = 44, FIELD_X = LEFT_X + LABEL_W + 2;
    private static final int SECOND_LABEL_X = 158, SECOND_FIELD_X = SECOND_LABEL_X + LABEL_W + 2;
    private static final int ROW_TYPE = 28, ROW_ITEM = 52, ROW_PAY = ROW_ITEM + 26, ROW_ICON = ROW_PAY + 26;
    private static final int ROW_PRICE = ROW_ICON + 28, ROW_LIMIT = ROW_PRICE + 24, ROW_NAME = ROW_LIMIT + 24, ROW_DESC = ROW_NAME + 24;
    private static final int ROW_CONTENT = ROW_DESC + 28, REWARD_Y = ROW_CONTENT + 24;
    private static final int REWARD_COLS = (LEFT_RIGHT - LEFT_X + 2) / PITCH, REWARD_ROWS = (CANVAS_H - 4 - REWARD_Y + 2) / PITCH;
    // Right column: command rewards on top, required quests below.
    private static final int DIVIDER_X = 320, RIGHT_X = 324, RIGHT_W = CANVAS_W - 4 - RIGHT_X, MANAGE_W = 40;
    private static final int COMMAND_HEAD_Y = 28, COMMAND_LIST_Y = COMMAND_HEAD_Y + 22, COMMAND_ROWS = 5;
    private static final int QUEST_HEAD_Y = COMMAND_LIST_Y + COMMAND_ROWS * PITCH + 6, QUEST_LIST_Y = QUEST_HEAD_Y + 22;
    private static final int PAGE_LABEL_X = 110, PAGE_X = 148, PAGE_MENU_ARROW_WIDTH = 20;

    private final ShopScreen parent;
    private final ShopGuiSupport.EditorDraft draft;
    private final Map<Long, ShopGuiSupport.QuestDisplay> questDisplays;
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
    private KineticButton rewardModeButton;
    private KineticButton rewardManageButton;
    private CommandRows commandRows;
    private QuestRows questRows;
    private int shownCommandsHash, shownQuestsHash;
    private KineticUi buildingUi;

    ShopEntryEditorScreen(ShopScreen parent, ShopGuiSupport.EditorDraft draft) {
        super(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_title"));
        this.parent = parent;
        this.draft = draft;
        this.originalMode = draft.mode;
        this.originalIndex = draft.index;
        this.questDisplays = ShopGuiSupport.loadQuestDisplays();
        useCanvas(CANVAS_W, CANVAS_H, 4);
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
        addButton(4, 4, 40, AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_cancel"), this::cancelAndReturn);
        addButton(CANVAS_W - 44, 4, 40, AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_save"), this::saveDraft);

        addButton(typeButtonX(), typeButtonY(), 52, typeButtonText(), this::openTypeMenu);
        // 分页：下拉选择已有分页 / 默认分页，或选“新建分页…”后在右侧输入框填写新名字。
        pageButton = addButton(PAGE_X, ROW_TYPE, newPageMode ? PAGE_MENU_ARROW_WIDTH : LEFT_RIGHT - PAGE_X,
                pageButtonText(), this::openPageMenu);
        pageBox = addTextField(PAGE_X + PAGE_MENU_ARROW_WIDTH + 2, ROW_TYPE + 1, LEFT_RIGHT - PAGE_X - PAGE_MENU_ARROW_WIDTH - 2,
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_page_label"),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_page_new_hint"));
        pageBox.limitTextLength(32);
        pageBox.setTextValue(newPageMode && draft.pageName != null ? draft.pageName : "");
        setControlVisible(pageBox, newPageMode);
        setControlEnabled(pageBox, newPageMode);

        priceBox = addLongField(FIELD_X, ROW_PRICE + 1, 96, label("price"));
        priceBox.limitTextLength(18);
        priceBox.setLongValue(draft.price);
        countBox = addIntegerField(SECOND_FIELD_X, ROW_PRICE + 1, 48, label("count"), 1, 64);
        countBox.limitTextLength(2);
        countBox.setIntValue(draft.count);
        dailyLimitBox = addIntegerField(FIELD_X, ROW_LIMIT + 1, 96, label("timed"), 0, Integer.MAX_VALUE);
        dailyLimitBox.limitTextLength(10);
        dailyLimitBox.setIntValue(draft.dailyLimit);
        totalLimitBox = addIntegerField(SECOND_FIELD_X, ROW_LIMIT + 1, 96, label("total"), 0, 999999999);
        totalLimitBox.limitTextLength(9);
        totalLimitBox.setIntValue(draft.totalLimit);

        displayNameBox = addTextField(FIELD_X, ROW_NAME + 1, LEFT_RIGHT - FIELD_X, label("name"),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_name_default"));
        displayNameBox.limitTextLength(64);
        displayNameBox.setTextValue(draft.displayName == null ? "" : draft.displayName);
        descriptionBox = addTextField(FIELD_X, ROW_DESC + 1, LEFT_RIGHT - FIELD_X, label("desc"), label("desc"));
        descriptionBox.limitTextLength(256);
        descriptionBox.setTextValue(draft.description == null ? "" : draft.description);

        rewardModeButton = addButton(contentModeButtonX(), contentModeButtonY(), 76, contentModeButtonText(), this::openContentModeMenu);
        rewardManageButton = addButton(contentModeButtonX() + 80, contentModeButtonY(), MANAGE_W, manageText(), this::openContentManager);
        refreshContentButtons();

        addButton(CANVAS_W - 4 - MANAGE_W, COMMAND_HEAD_Y, MANAGE_W, manageText(), this::openCommandManager);
        addButton(CANVAS_W - 4 - MANAGE_W, QUEST_HEAD_Y, MANAGE_W, manageText(), this::openQuestManager);
        questNeedBox = addIntegerField(CANVAS_W - 4 - MANAGE_W - 4 - 40, QUEST_HEAD_Y + 1, 40, label("need"), 0, 999);
        questNeedBox.limitTextLength(3);
        questNeedBox.setIntValue(draft.requiredQuestCount);

        commandRows = ui.add(new CommandRows());
        questRows = ui.add(new QuestRows());
        shownCommandsHash = shownQuestsHash = 0;
        refreshLists();
        if (focusPageBoxOnBuild && newPageMode) {
            focusPageBoxOnBuild = false;
            focus(pageBox);
        }
    }

    private static Component label(String name) {
        return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_f_" + name);
    }

    private static Component manageText() {
        return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_manage");
    }

    private void openCommandManager() {
        syncBasicInputsQuietly();
        openChild(new CommandManageScreen(draft));
    }

    private void openQuestManager() {
        syncBasicInputsQuietly();
        openChild(new QuestManageScreen(draft));
    }

    /** The lists follow the draft, which the manager pages change. */
    private void refreshLists() {
        int commandsHash = draft.commands.hashCode(), questsHash = draft.questIds.hashCode() * 31 + draft.questIds.size();
        if (commandRows != null && commandsHash != shownCommandsHash) {
            shownCommandsHash = commandsHash;
            commandRows.setItems(draft.commands);
        }
        if (questRows != null && questsHash != shownQuestsHash) {
            shownQuestsHash = questsHash;
            List<ShopGuiSupport.QuestDisplay> quests = new ArrayList<>();
            for (long id : draft.questIds) {
                ShopGuiSupport.QuestDisplay known = questDisplays.get(id);
                quests.add(known != null ? known : new ShopGuiSupport.QuestDisplay(id, "", "", ""));
            }
            questRows.setItems(quests);
        }
    }

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
        items.add(KineticOverlays.MenuItem.choice(pageDisplayName(""),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_page_default_tip"),
                !newPageMode && current.isEmpty(), () -> choosePage("")));
        List<String> known = new ArrayList<>(parent.knownPageNames(draft.mode));
        // 草稿里的分页可能还不在商店里（例如刚新建、尚未保存），也列出来方便切回。
        if (!current.isEmpty() && !known.contains(current)) known.add(0, current);
        for (String page : known) {
            items.add(KineticOverlays.MenuItem.choice(AdventureText.literal(page), null,
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
        items.add(KineticOverlays.MenuItem.choice(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_type_buy"),
                null, draft.mode == Shop.Mode.BUY, () -> setDraftMode(Shop.Mode.BUY)));
        items.add(KineticOverlays.MenuItem.choice(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_type_sell"),
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
            items.add(KineticOverlays.MenuItem.choice(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_sell_multi_off"),
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_tooltip_sell_multi_switch"),
                    !draft.selectable, () -> setContentMode(false, false)));
            items.add(KineticOverlays.MenuItem.choice(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_sell_multi_on"),
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_tooltip_sell_choices"),
                    draft.selectable, () -> setContentMode(false, true)));
        } else {
            items.add(KineticOverlays.MenuItem.choice(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_reward_single_active"),
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_tooltip_single_reward_mode"),
                    !draft.gacha && !draft.selectable, () -> setContentMode(false, false)));
            items.add(KineticOverlays.MenuItem.choice(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_choice_table_active"),
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_tooltip_choice_table"),
                    draft.selectable, () -> setContentMode(false, true)));
            items.add(KineticOverlays.MenuItem.choice(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_gacha_pool_active"),
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

    private void openIconSelector() {
        KineticSelectors.openItemSelector(selection -> {
            if (!selection.isItem()) return;
            String spec = ShopGuiSupport.selectedStackSpec(selection.stack());
            if (!spec.isBlank()) draft.iconId = ShopGuiSupport.stackSpecWithCount(spec, 1);
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
            rewardManageButton.setText(manageText());
        }
    }

    private Component typeButtonText() {
        return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_menu_value", draft.mode == Shop.Mode.BUY ? AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_type_buy") : AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_type_sell"));
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

    private boolean shouldShowContentManageButton() {
        if (draft.mode == Shop.Mode.SELL) return draft.selectable;
        return draft.gacha || draft.selectable;
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

    private int typeButtonX() { return FIELD_X; }
    private int typeButtonY() { return ROW_TYPE; }
    private int contentModeButtonX() { return FIELD_X; }
    private int contentModeButtonY() { return ROW_CONTENT; }
    private int needLabelRight() { return CANVAS_W - 4 - MANAGE_W - 4 - 40 - 4; }

    private void rebuildEditorWidgets() {
        rebuild();
    }

    private void openPaymentMenu() {
        syncBasicInputsQuietly();
        List<KineticOverlays.MenuItem> items = new ArrayList<>();
        items.add(KineticOverlays.MenuItem.choice(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_pay_currency"),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_tooltip_currency"),
                !currentCurrencyId().isEmpty(), () -> openChild(new CurrencyPickerScreen(draft))));
        items.add(KineticOverlays.MenuItem.choice(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_pay_item"),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_tooltip_payment_item"),
                !currentBarterItemId().isEmpty(), this::openPaymentItemSelector));
        openContextMenu(FIELD_X, ROW_PAY + SLOT + 2, items);
    }

    @Override
    protected boolean onMouseClick(MouseInput input) {
        if (input.inside(FIELD_X, ROW_ICON, SLOT, SLOT) && input.isRight()) {
            draft.iconId = "";
            return true;
        }
        if (!input.isLeft()) return false;
        if (input.inside(FIELD_X, ROW_ITEM, SLOT, SLOT)) {
            syncBasicInputsQuietly();
            openMainItemSelector();
        } else if (input.inside(FIELD_X, ROW_PAY, SLOT, SLOT)) {
            openPaymentMenu();
        } else if (input.inside(FIELD_X, ROW_ICON, SLOT, SLOT)) {
            syncBasicInputsQuietly();
            openIconSelector();
        } else if (rewardIndexAt(input.x(), input.y()) >= 0) {
            openContentManager();
        } else {
            return false;
        }
        return true;
    }

    @Override
    protected void renderBackground(KineticGraphics g, int mouseX, int mouseY, float partialTick) {
        refreshLists();
        KineticTheme.panel(g, 0, 0, CANVAS_W, CANVAS_H);
        KineticTheme.separator(g, 4, 24, CANVAS_W - 8);
        KineticTheme.verticalSeparator(g, DIVIDER_X, 28, CANVAS_H - 32);
        KineticTheme.separator(g, LEFT_X, ROW_CONTENT - 4, LEFT_RIGHT - LEFT_X);
        KineticTheme.separator(g, RIGHT_X, QUEST_HEAD_Y - 4, RIGHT_W);
        g.scrollingTextCentered(title(), CANVAS_W / 2, 8, CANVAS_W - 2 * 48, KineticTheme.current().text(), false);

        int text = KineticTheme.current().text();
        fieldLabel(g, "type", LEFT_X, ROW_TYPE);
        g.scrollingText(label("page"), PAGE_LABEL_X, ROW_TYPE + 6, PAGE_X - PAGE_LABEL_X - 4, text, false);
        fieldLabel(g, "item", LEFT_X, ROW_ITEM + 1);
        fieldLabel(g, "pay", LEFT_X, ROW_PAY + 1);
        fieldLabel(g, "icon", LEFT_X, ROW_ICON + 1);
        fieldLabel(g, "price", LEFT_X, ROW_PRICE);
        fieldLabel(g, "count", SECOND_LABEL_X, ROW_PRICE);
        fieldLabel(g, "timed", LEFT_X, ROW_LIMIT);
        fieldLabel(g, "total", SECOND_LABEL_X, ROW_LIMIT);
        fieldLabel(g, "name", LEFT_X, ROW_NAME);
        fieldLabel(g, "desc", LEFT_X, ROW_DESC);
        fieldLabel(g, "content", LEFT_X, ROW_CONTENT);

        Component commands = AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_commands_head",
                Component.literal(String.valueOf(draft.commands.size())).withStyle(ChatFormatting.GREEN));
        g.scrollingText(commands, RIGHT_X, COMMAND_HEAD_Y + 6, RIGHT_W - MANAGE_W - 8, text, false);
        Component quests = AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_quests_head",
                Component.literal(String.valueOf(draft.questIds.size())).withStyle(ChatFormatting.GREEN));
        int needW = Math.min(64, g.textWidth(label("need")));
        g.scrollingText(quests, RIGHT_X, QUEST_HEAD_Y + 6, needLabelRight() - needW - 8 - RIGHT_X, text, false);
        g.scrollingTextRight(label("need"), needLabelRight(), QUEST_HEAD_Y + 6, needW, text, false);
    }

    private void fieldLabel(KineticGraphics g, String name, int x, int rowY) {
        g.scrollingText(label(name), x, rowY + 6, LABEL_W, KineticTheme.current().text(), false);
    }

    @Override
    protected void renderForeground(KineticGraphics g, int mouseX, int mouseY, float partialTick) {
        String payId = draft.currencyId == null ? "" : draft.currencyId;
        String iconId = hasCustomIcon() ? draft.iconId : "";
        g.batchItems(() -> {
            itemRow(g, mouseX, mouseY, ROW_ITEM, draft.itemId, ChatFormatting.GOLD, null);
            Component payKind = payId.isBlank() ? null : AdventureText.translatable(ShopGuiSupport.isConfiguredCurrency(payId)
                    ? "gui.adventuresystems.curios.wallet.shop_editor_pay_currency" : "gui.adventuresystems.curios.wallet.shop_editor_pay_item");
            itemRow(g, mouseX, mouseY, ROW_PAY, payId, ChatFormatting.AQUA, payKind);
            itemRow(g, mouseX, mouseY, ROW_ICON, iconId, ChatFormatting.GREEN, null);
            if (hasCustomIcon()) KineticTheme.indicatorOutline(g, FIELD_X, ROW_ICON, SLOT, SLOT, KineticTheme.Indicator.SUCCESS);
            renderRewards(g, mouseX, mouseY);
        });
    }

    /** An item slot with the item's name beside it in colour; an empty slot shows what it is for, muted. */
    private void itemRow(KineticGraphics g, int mouseX, int mouseY, int y, String id, ChatFormatting colour, Component suffix) {
        KineticTheme.itemSlot(g, FIELD_X, y, SLOT, KineticTheme.hovering(mouseX, mouseY, FIELD_X, y, SLOT, SLOT));
        int textX = FIELD_X + SLOT + 6;
        Component name;
        if (id == null || id.isBlank()) {
            String empty = y == ROW_ICON ? "shop_editor_icon_default_short" : y == ROW_PAY ? "shop_editor_pay_none" : "shop_editor_item_none";
            name = AdventureText.translatable("gui.adventuresystems.curios.wallet." + empty).copy().withStyle(ChatFormatting.GRAY);
        } else {
            ItemStack stack = ShopGuiSupport.stack(id);
            KineticTheme.item(g, stack, FIELD_X, y, SLOT, 1.0F, false);
            name = ShopGuiSupport.stackNameComponent(stack).copy().withStyle(colour);
            if (suffix != null) name = name.copy().append(Component.literal("  ").append(suffix).withStyle(ChatFormatting.GRAY));
        }
        g.scrollingText(name, textX, y + 7, LEFT_RIGHT - textX, KineticTheme.current().text(), false);
    }

    private boolean showsRewardItems() {
        return draft.gacha || draft.selectable;
    }

    private void renderRewards(KineticGraphics g, int mouseX, int mouseY) {
        if (!showsRewardItems()) {
            String key = draft.mode == Shop.Mode.SELL ? "shop_editor_single_sell_hint" : "shop_editor_single_buy_hint";
            g.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet." + key), LEFT_X, REWARD_Y + 6, LEFT_RIGHT - LEFT_X, KineticTheme.current().mutedText(), false);
            return;
        }
        if (draft.rewards.isEmpty()) {
            g.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_rewards_none"), LEFT_X, REWARD_Y + 6, LEFT_RIGHT - LEFT_X, KineticTheme.current().mutedText(), false);
            return;
        }
        int capacity = REWARD_COLS * REWARD_ROWS;
        int shown = Math.min(draft.rewards.size(), capacity);
        for (int i = 0; i < shown; i++) {
            int x = rewardX(i), y = rewardY(i);
            KineticTheme.itemSlot(g, x, y, SLOT, KineticTheme.hovering(mouseX, mouseY, x, y, SLOT, SLOT));
            boolean last = i == capacity - 1 && draft.rewards.size() > capacity;
            ShopGuiSupport.RewardDraft reward = draft.rewards.get(i);
            if (last) {
                g.centeredText(Component.literal("+" + (draft.rewards.size() - capacity + 1)), x + SLOT / 2, y + 7, KineticTheme.current().text(), false);
            } else if (reward.empty()) {
                g.centeredText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_empty_slot"), x + SLOT / 2, y + 7, KineticTheme.current().mutedText(), false);
            } else {
                KineticTheme.item(g, ShopGuiSupport.stack(reward.itemId()), x, y, SLOT, 1.0F, true);
            }
        }
    }

    private static int rewardX(int index) { return LEFT_X + (index % REWARD_COLS) * PITCH; }
    private static int rewardY(int index) { return REWARD_Y + (index / REWARD_COLS) * PITCH; }

    private int rewardIndexAt(double x, double y) {
        if (!showsRewardItems() || x < LEFT_X || y < REWARD_Y) return -1;
        int col = (int) (x - LEFT_X) / PITCH, row = (int) (y - REWARD_Y) / PITCH;
        if (col >= REWARD_COLS || row >= REWARD_ROWS || (x - LEFT_X) % PITCH >= SLOT || (y - REWARD_Y) % PITCH >= SLOT) return -1;
        int index = row * REWARD_COLS + col;
        return index < Math.min(draft.rewards.size(), REWARD_COLS * REWARD_ROWS) ? index : -1;
    }

    @Override
    protected void renderTooltips(int mouseX, int mouseY) {
        if (KineticTheme.hovering(mouseX, mouseY, FIELD_X, ROW_ICON, SLOT, SLOT)) {
            List<Component> tooltip = List.of(
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_icon_label"),
                    hasCustomIcon()
                            ? AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_icon_custom", ShopGuiSupport.stack(draft.iconId).getHoverName())
                            : AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_icon_default"),
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_icon_tip"));
            showStackTooltip(hasCustomIcon() ? draft.iconId : draft.itemId, tooltip);
            return;
        }
        int reward = rewardIndexAt(mouseX, mouseY);
        if (reward >= 0) {
            ShopGuiSupport.RewardDraft draftReward = draft.rewards.get(reward);
            List<Component> lines = new ArrayList<>();
            if (draft.gacha) lines.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_reward_weight", draftReward.weight()));
            lines.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_rewards_click"));
            if (draftReward.empty()) showTooltip(lines);
            else showStackTooltip(draftReward.itemId(), lines);
            return;
        }
        List<Component> tooltip = new ArrayList<>();
        String stackId = "";
        if (KineticTheme.hovering(mouseX, mouseY, FIELD_X, ROW_ITEM, SLOT, SLOT)) { addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_item_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_item"); stackId = draft.itemId; }
        else if (KineticTheme.hovering(mouseX, mouseY, FIELD_X, ROW_PAY, SLOT, SLOT)) { addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_f_pay", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_pay"); stackId = draft.currencyId; }
        else if (KineticTheme.hovering(mouseX, mouseY, typeButtonX(), typeButtonY(), 52, 20)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_type_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_type");
        else if (hoveringControl(pageButton, mouseX, mouseY)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_page_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_page");
        else if (hoveringControl(priceBox, mouseX, mouseY)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_price_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_price");
        else if (hoveringControl(countBox, mouseX, mouseY)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_count_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_count");
        else if (hoveringControl(dailyLimitBox, mouseX, mouseY)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_timed_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_timed");
        else if (hoveringControl(totalLimitBox, mouseX, mouseY)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_total_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_total");
        else if (hoveringControl(questNeedBox, mouseX, mouseY)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_quest_need_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_quest_need");
        else if (hoveringControl(displayNameBox, mouseX, mouseY)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_display_name_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_display_name");
        else if (hoveringControl(descriptionBox, mouseX, mouseY)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_description_label", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_description");
        else if (hoveringControl(rewardModeButton, mouseX, mouseY)) {
            String body = draft.mode == Shop.Mode.SELL ? "gui.adventuresystems.curios.wallet.shop_editor_tooltip_sell_multi_switch" : (!draft.selectable && !draft.gacha ? "gui.adventuresystems.curios.wallet.shop_editor_tooltip_single_reward_mode" : (draft.selectable ? "gui.adventuresystems.curios.wallet.shop_editor_tooltip_choice_table" : "gui.adventuresystems.curios.wallet.shop_editor_tooltip_gacha_pool"));
            addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_content_mode_label", body);
        } else if (shouldShowContentManageButton() && hoveringControl(rewardManageButton, mouseX, mouseY)) {
            if (draft.mode == Shop.Mode.SELL) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_sell_choice_manage", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_sell_choices");
            else if (draft.selectable) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_choice_table_manage", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_choice_table");
            else addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_gacha_pool_manage", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_gacha_pool");
        } else if (KineticTheme.hovering(mouseX, mouseY, CANVAS_W - 4 - MANAGE_W, COMMAND_HEAD_Y, MANAGE_W, 20)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_command_manage_button", "gui.adventuresystems.curios.wallet.shop_command_manage_tooltip");
        else if (KineticTheme.hovering(mouseX, mouseY, CANVAS_W - 4 - MANAGE_W, QUEST_HEAD_Y, MANAGE_W, 20)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_quest_manage", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_quest");
        else if (KineticTheme.hovering(mouseX, mouseY, CANVAS_W - 44, 4, 40, 20)) addTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_editor_save", "gui.adventuresystems.curios.wallet.shop_editor_tooltip_save");
        if (tooltip.isEmpty()) return;
        showStackTooltip(stackId, tooltip);
    }

    private void showStackTooltip(String id, List<Component> lines) {
        ItemStack stack = id == null || id.isBlank() ? ItemStack.EMPTY : ShopGuiSupport.stack(id);
        if (stack.isEmpty()) showTooltip(lines, 260);
        else showItemTooltip(stack, lines);
    }

    private static boolean hoveringControl(KineticControl control, int mouseX, int mouseY) {
        return control != null && control.controlVisible() && control.contains(mouseX, mouseY);
    }

    private void addTooltip(List<Component> tooltip, String titleKey, String bodyKey) {
        tooltip.add(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_tooltip_title", AdventureText.translatable(titleKey)));
        tooltip.add(AdventureText.translatable(bodyKey));
    }

    private static void setControlVisible(KineticControl control, boolean visible) {
        if (control != null) control.setControlVisible(visible);
    }

    private static void setControlEnabled(KineticControl control, boolean enabled) {
        if (control != null) control.setEnabled(enabled);
    }

    /** The command rewards: icon, name, and the command muted. A click opens the command manager. */
    private final class CommandRows extends KineticRowList<ShopGuiSupport.CommandDraft> {
        CommandRows() {
            super(RIGHT_X, COMMAND_LIST_Y, RIGHT_W, COMMAND_ROWS * PITCH, PITCH);
        }

        @Override
        protected void renderRow(KineticGraphics g, ShopGuiSupport.CommandDraft command, int index, int x, int y, int width, int height, boolean hovered, boolean selected) {
            KineticTheme.itemSlot(g, x + 1, y + 1, SLOT, false);
            if (!command.iconId().isBlank()) KineticTheme.item(g, ShopGuiSupport.stack(command.iconId()), x + 1, y + 1, SLOT, 1.0F, false);
            String name = command.displayName().isBlank() ? command.command() : command.displayName();
            Component line = Component.literal(name).withStyle(ChatFormatting.WHITE);
            if (!command.displayName().isBlank()) line = line.copy().append(Component.literal("  " + command.command()).withStyle(ChatFormatting.GRAY));
            g.scrollingText(line, x + SLOT + 6, y + (height - 8) / 2, width - SLOT - 10, 0xFFFFFFFF, false);
        }

        @Override
        protected boolean onRowClick(ShopGuiSupport.CommandDraft command, int index, MouseInput input) {
            if (!input.isLeft()) return false;
            playClickSound();
            openCommandManager();
            return true;
        }

        @Override
        protected Component rowTooltip(ShopGuiSupport.CommandDraft command, int index) {
            return Component.literal(command.command()).withStyle(ChatFormatting.GRAY).append("\n")
                    .append(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_manage_tooltip"));
        }

        @Override
        protected Component emptyText() {
            return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_commands_none");
        }
    }

    /** The required quests: title, chapter muted. A click opens the quest page. */
    private final class QuestRows extends KineticRowList<ShopGuiSupport.QuestDisplay> {
        QuestRows() {
            super(RIGHT_X, QUEST_LIST_Y, RIGHT_W, CANVAS_H - 4 - QUEST_LIST_Y, 20);
        }

        @Override
        protected void renderRow(KineticGraphics g, ShopGuiSupport.QuestDisplay quest, int index, int x, int y, int width, int height, boolean hovered, boolean selected) {
            Component line = Component.literal(quest.displayTitle()).withStyle(ChatFormatting.WHITE)
                    .append(Component.literal("  " + quest.displayMeta()).withStyle(ChatFormatting.GRAY));
            g.scrollingText(line, x + 4, y + (height - 8) / 2, width - 8, 0xFFFFFFFF, false);
        }

        @Override
        protected boolean onRowClick(ShopGuiSupport.QuestDisplay quest, int index, MouseInput input) {
            if (!input.isLeft()) return false;
            playClickSound();
            openQuestManager();
            return true;
        }

        @Override
        protected Component rowTooltip(ShopGuiSupport.QuestDisplay quest, int index) {
            return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_tooltip_quest");
        }

        @Override
        protected Component emptyText() {
            return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_editor_quests_none");
        }
    }
}
