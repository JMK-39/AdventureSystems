package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.adventuresystems.curios.wallet.shop.Shop;
import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.selector.KineticSelectors;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.ui.NumberType;
import dev.xyat.kineticcore.api.client.gui.widget.KineticNumberField;
import dev.xyat.kineticcore.api.client.gui.widget.list.ItemActionItem;
import dev.xyat.kineticcore.api.client.gui.widget.list.KineticItemActionList;
import dev.xyat.kineticcore.api.client.gui.overlay.KineticOverlays;
import dev.xyat.adventuresystems.text.AdventureText;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

final class RewardPoolScreen extends KineticPage {
    private final ShopGuiSupport.EditorDraft draft;
    private int selectedIndex = -1;
    private KineticItemActionList list;
    private KineticNumberField countBox;
    private KineticNumberField weightBox;
    private int scrollOffset;

    RewardPoolScreen(ShopGuiSupport.EditorDraft draft) {
        super(AdventureText.translatable(titleKey(draft)));
        this.draft = draft;
        useCanvas(640, 360, 6);
    }

    @Override
    protected void build(KineticUi ui) {
        if (list != null) scrollOffset = list.scrollOffset();
        ui.button(14, 54, 104).text(AdventureText.translatable(addItemButtonKey()))
                .tooltip(AdventureText.translatable(addItemTooltipKey()))
                .onClick(this::openItemSelector).build();
        if (!sellMode()) {
            ui.button(124, 54, 112)
                    .text(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_add_reward"))
                    .onClick(this::openCommandRewardPicker).build();
        }
        if (gachaMode()) {
            ui.button(242, 54, 96).text(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_reward_add_empty"))
                    .onClick(this::addEmptyReward).build();
        }
        int clearX = gachaMode() ? 344 : sellMode() ? 124 : 242;
        ui.button(clearX, 54, 78).text(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_reward_clear"))
                .tooltip(AdventureText.translatable(clearTooltipKey()))
                .onClick(() -> {
                    draft.rewards.clear();
                    selectedIndex = -1;
                    refreshList();
                }).build();
        ui.button(544, 54, 82).text(AdventureText.translatable("gui.done"))
                .onClick(this::finish).build();
        list = ui.itemActionList(14, 82, 612, 208, listItems())
                .selected(selectedIndex).scrollOffset(scrollOffset).actionWidth(66)
                .onSelect(this::selectReward).onAction(this::deleteReward).build();
        int selectedCount = selectedIndex >= 0 && selectedIndex < draft.rewards.size()
                ? Math.max(1, draft.rewards.get(selectedIndex).count()) : 1;
        int selectedWeight = selectedIndex >= 0 && selectedIndex < draft.rewards.size()
                ? Math.max(1, draft.rewards.get(selectedIndex).weight()) : 1;
        countBox = ui.numberField(96, 314, 70, NumberType.INT)
                .label(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_reward_column_count"))
                .allowNegative(false).range(1, 64).value(selectedCount)
                .onChange(value -> updateSelectedCount()).firstShownTextAsDefault().build();
        weightBox = ui.numberField(256, 314, 80, NumberType.INT)
                .label(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_reward_column_weight"))
                .allowNegative(false).range(1, 999999999).value(selectedWeight)
                .onChange(value -> updateSelectedWeight()).firstShownTextAsDefault().build();
        boolean editable = selectedIndex >= 0 && selectedIndex < draft.rewards.size();
        countBox.setEnabled(editable && !draft.rewards.get(selectedIndex).empty());
        weightBox.setEnabled(editable && gachaMode());
        // Weights only apply to gacha pools; elsewhere the field has no label and nothing to edit.
        weightBox.setControlVisible(gachaMode());
    }

    private List<ItemActionItem> listItems() {
        recalc();
        List<ItemActionItem> items = new ArrayList<>(draft.rewards.size());
        for (ShopGuiSupport.RewardDraft reward : draft.rewards) {
            Component name = rewardDisplayName(reward);
            Component secondary = gachaMode()
                    ? AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_reward_tooltip_chance",
                            ShopGuiSupport.percent(reward.chance()))
                    : AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_reward_tooltip_count",
                            Math.max(1, reward.count()));
            ItemStack icon = reward.empty() ? ItemStack.EMPTY : ShopGuiSupport.stack(reward.itemId());
            items.add(new ItemActionItem(icon, name, secondary, name, true, false,
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_reward_delete_short"),
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_reward_tooltip_delete"), true));
        }
        return items;
    }

    private void refreshList() {
        if (list != null) list.setItems(listItems());
        boolean editable = selectedIndex >= 0 && selectedIndex < draft.rewards.size();
        if (countBox != null) countBox.setEnabled(editable && !draft.rewards.get(selectedIndex).empty());
        if (weightBox != null) weightBox.setEnabled(editable && gachaMode());
    }

    private void selectReward(int index) {
        if (index < 0 || index >= draft.rewards.size()) return;
        selectedIndex = index;
        ShopGuiSupport.RewardDraft reward = draft.rewards.get(index);
        countBox.setIntValue(Math.max(1, reward.count()));
        weightBox.setIntValue(Math.max(1, reward.weight()));
        refreshList();
    }

    private void updateSelectedCount() {
        if (countBox == null || selectedIndex < 0 || selectedIndex >= draft.rewards.size()) return;
        Integer count = countBox.getIntValue();
        if (count == null) return;
        ShopGuiSupport.RewardDraft reward = draft.rewards.get(selectedIndex);
        if (reward.empty()) return;
        draft.rewards.set(selectedIndex, new ShopGuiSupport.RewardDraft(
                ShopGuiSupport.stackSpecWithCount(reward.itemId(), count), count, reward.weight(), false,
                0.0D, reward.displayName(), reward.command()));
        refreshList();
    }

    private void updateSelectedWeight() {
        if (!gachaMode() || weightBox == null || selectedIndex < 0 || selectedIndex >= draft.rewards.size()) return;
        Integer weight = weightBox.getIntValue();
        if (weight == null) return;
        ShopGuiSupport.RewardDraft reward = draft.rewards.get(selectedIndex);
        draft.rewards.set(selectedIndex, new ShopGuiSupport.RewardDraft(
                reward.itemId(), reward.count(), weight, reward.empty(), 0.0D,
                reward.displayName(), reward.command()));
        refreshList();
    }

    private void openItemSelector() {
        KineticSelectors.openItemSelector(selection -> {
            if (!selection.isItem()) return;
            ItemStack stack = selection.stack();
            String spec = ShopGuiSupport.selectedStackSpec(stack);
            if (spec.isBlank()) return;
            draft.rewards.add(new ShopGuiSupport.RewardDraft(spec,
                    Math.max(1, Math.min(64, stack.getCount())), gachaMode() ? 10 : 1,
                    false, 0.0D, "", ""));
            selectedIndex = draft.rewards.size() - 1;
            refreshList();
        });
    }

    private void openCommandRewardPicker() {
        openChild(new CommandRewardPickerScreen(draft, gachaMode()));
    }

    private void addEmptyReward() {
        if (!gachaMode()) {
            toast("currency_wallet_choice_no_empty", choiceMode()
                    ? "msg.adventuresystems.curios.wallet.shop_choice_no_empty"
                    : "msg.adventuresystems.curios.wallet.shop_sell_choice_no_empty");
            return;
        }
        for (ShopGuiSupport.RewardDraft reward : draft.rewards) {
            if (reward.empty()) {
                toast("currency_wallet_reward_empty_exists", "msg.adventuresystems.curios.wallet.shop_empty_reward_exists");
                return;
            }
        }
        draft.rewards.add(new ShopGuiSupport.RewardDraft("", 0, 10, true, 0.0D, "", ""));
        selectedIndex = draft.rewards.size() - 1;
        refreshList();
    }

    private static void toast(String id, String key) {
        KineticOverlays.toast(id, AdventureText.translatable(key), KineticOverlays.Position.BOTTOM_CENTER, 2200, 0, -30);
    }

    private void deleteReward(int index) {
        if (index < 0 || index >= draft.rewards.size()) return;
        draft.rewards.remove(index);
        if (selectedIndex == index) selectedIndex = -1;
        else if (selectedIndex > index) selectedIndex--;
        refreshList();
    }

    private void recalc() {
        int total = 0;
        if (gachaMode()) {
            for (ShopGuiSupport.RewardDraft reward : draft.rewards) total += Math.max(0, reward.weight());
        }
        for (int i = 0; i < draft.rewards.size(); i++) {
            ShopGuiSupport.RewardDraft reward = draft.rewards.get(i);
            int count = reward.empty() ? reward.count() : Math.max(1, Math.min(64, reward.count()));
            int weight = gachaMode() ? reward.weight() : 1;
            double chance = gachaMode() && total > 0 ? weight * 100.0D / total : 0.0D;
            draft.rewards.set(i, new ShopGuiSupport.RewardDraft(
                    reward.empty() ? reward.itemId() : ShopGuiSupport.stackSpecWithCount(reward.itemId(), count),
                    count, weight, reward.empty(), chance, reward.displayName(), reward.command()));
        }
    }

    private void finish() {
        recalc();
        navigateBack();
    }

    @Override
    protected boolean onCloseRequested() {
        finish();
        return true;
    }

    @Override
    protected void renderBackground(KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        KineticTheme.shadow(graphics, width(), height());
        KineticTheme.panel(graphics, 0, 0, 640, 360);
        graphics.scrollingTextCentered(title(), 320, 10, 608, KineticTheme.current().text(), false);
        graphics.scrollingText(AdventureText.translatable(primaryHintKey()), 14, 26, 612, KineticTheme.current().text(), true);
        graphics.scrollingText(AdventureText.translatable(secondaryHintKey()), 14, 38, 612, KineticTheme.current().text(), true);
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_reward_column_count"), 14, 318, 96 - 14 - 4, KineticTheme.current().text(), true);
        if (gachaMode()) graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_reward_column_weight"), 182, 318, 256 - 182 - 4, KineticTheme.current().text(), true);
        if (draft.rewards.isEmpty()) graphics.scrollingTextCentered(AdventureText.translatable(emptyListKey()), 320, 180, 608, KineticTheme.current().text(), false);
    }

    private static String titleKey(ShopGuiSupport.EditorDraft draft) {
        if (draft.mode == Shop.Mode.SELL) return "gui.adventuresystems.curios.wallet.shop_sell_choice_pool_title";
        if (draft.selectable) return "gui.adventuresystems.curios.wallet.shop_choice_table_title";
        return "gui.adventuresystems.curios.wallet.shop_gacha_pool_title";
    }

    private String primaryHintKey() {
        if (sellMode()) return "gui.adventuresystems.curios.wallet.shop_sell_choice_pool_hint_1";
        if (choiceMode()) return "gui.adventuresystems.curios.wallet.shop_choice_table_hint_1";
        return "gui.adventuresystems.curios.wallet.shop_gacha_pool_hint_1";
    }

    private String secondaryHintKey() {
        if (sellMode()) return "gui.adventuresystems.curios.wallet.shop_sell_choice_pool_hint_2";
        if (choiceMode()) return "gui.adventuresystems.curios.wallet.shop_choice_table_hint_2";
        return "gui.adventuresystems.curios.wallet.shop_gacha_pool_hint_2";
    }

    private String addItemButtonKey() {
        if (sellMode()) return "gui.adventuresystems.curios.wallet.shop_sell_choice_add_item";
        if (choiceMode()) return "gui.adventuresystems.curios.wallet.shop_choice_table_add_item";
        return "gui.adventuresystems.curios.wallet.shop_reward_add_item";
    }

    private String addItemTooltipKey() {
        if (sellMode()) return "gui.adventuresystems.curios.wallet.shop_sell_choice_tooltip_add_item";
        if (choiceMode()) return "gui.adventuresystems.curios.wallet.shop_choice_table_tooltip_add_item";
        return "gui.adventuresystems.curios.wallet.shop_reward_tooltip_add_item";
    }

    private String clearTooltipKey() {
        if (sellMode()) return "gui.adventuresystems.curios.wallet.shop_sell_choice_tooltip_clear";
        if (choiceMode()) return "gui.adventuresystems.curios.wallet.shop_choice_table_tooltip_clear";
        return "gui.adventuresystems.curios.wallet.shop_reward_tooltip_clear";
    }

    private String emptyListKey() {
        if (sellMode()) return "gui.adventuresystems.curios.wallet.shop_sell_choice_pool_empty";
        if (choiceMode()) return "gui.adventuresystems.curios.wallet.shop_choice_table_empty";
        return "gui.adventuresystems.curios.wallet.shop_reward_pool_empty";
    }

    private Component rewardDisplayName(ShopGuiSupport.RewardDraft reward) {
        if (reward.displayName() != null && !reward.displayName().isBlank()) return AdventureText.literal(reward.displayName());
        if (reward.empty()) return AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_gacha_empty");
        return ShopGuiSupport.stackNameComponent(reward.itemId());
    }

    private boolean gachaMode() { return draft.mode == Shop.Mode.BUY && draft.gacha && !draft.selectable; }
    private boolean choiceMode() { return draft.mode == Shop.Mode.BUY && draft.selectable; }
    private boolean sellMode() { return draft.mode == Shop.Mode.SELL; }
}
