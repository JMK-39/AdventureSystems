package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.adventuresystems.curios.wallet.shop.Shop;
import dev.xyat.kineticcore.api.client.input.KineticMouseButtons;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.overlay.KineticOverlays;
import dev.xyat.kineticcore.api.client.selector.KineticSelectors;
import dev.xyat.kineticcore.api.client.screen.KineticScreen;
import dev.xyat.kineticcore.api.client.widget.scroll.KineticScroll.GridScrollController;
import dev.xyat.kineticcore.api.client.widget.input.KineticNumericFields.NumericEditBox;
import dev.xyat.kineticcore.api.client.widget.input.KineticTextFields.KineticEditBox;
import dev.xyat.kineticcore.api.client.widget.button.KineticButtons.StateButton;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import dev.xyat.kineticcore.api.text.KineticI18n;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import dev.xyat.kineticcore.api.client.widget.KineticControl;

final class RewardPoolScreen extends KineticScreen {
    private static final int PANEL_WIDTH = 640;
    private static final int PANEL_HEIGHT = 392;
    private static final int ROW_HEIGHT = 26;
        private static final int SCROLLBAR_WIDTH = 4;
    private static final int MIN_THUMB_HEIGHT = 16;
    private final Screen parent;
    private final ShopGuiSupport.EditorDraft draft;
    private final List<NumericEditBox> countBoxes = new ArrayList<>();
    private final List<NumericEditBox> weightBoxes = new ArrayList<>();
    private final List<StateButton> deleteButtons = new ArrayList<>();
    private KineticEditBox rewardNameBox;
    private KineticEditBox rewardCommandBox;
    private int selectedIndex = -1;
    private int left;
    private int top;
    private final GridScrollController listScroll = new GridScrollController();

    RewardPoolScreen(Screen parent, ShopGuiSupport.EditorDraft draft) {
        super(KineticI18n.translatable(titleKey(draft)));
        this.parent = parent;
        setParentScreen(parent);
        this.draft = draft;
        useCanvas(
                680f,
                430f,
                6
        );
    }

    @Override
    protected void buildUi() {
        left = (canvasWidth() - PANEL_WIDTH) / 2;
        top = (canvasHeight() - PANEL_HEIGHT) / 2;
        addButton(left + 14, top + 52, 104, KineticI18n.translatable(addItemButtonKey()), null, this::openItemSelector);
        if (!sellMode()) {
            addButton(left + 124, top + 52, 112, KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_command_add_reward"), null, this::openCommandRewardPicker);
        }
        if (gachaMode()) {
            addButton(left + 242, top + 52, 96, KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_reward_add_empty"), null, this::addEmptyReward);
            addButton(left + 344, top + 52, 66, KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_reward_clear"), null, () -> {
                draft.rewards.clear();
                rebuildRewardWidgets();
            });
        } else {
            addButton(left + (sellMode() ? 124 : 242), top + 52, 78, KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_reward_clear"), null, () -> {
                draft.rewards.clear();
                rebuildRewardWidgets();
            });
        }
        addButton(left + PANEL_WIDTH - 96, top + 52, 82, KineticI18n.translatable("gui.done"), null, this::finish);

        rewardNameBox = addTextField(
                listX() + 92,
                rewardNameInputY(),
                216,
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_reward_display_name_label"),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_reward_display_name_label"),
                null,
                null
        );
        rewardNameBox.setMaxLength(64);
        rewardNameBox.setResponder(this::updateSelectedRewardName);

        rewardCommandBox = addTextField(
                listX() + 92,
                rewardCommandInputY(),
                listW() - 104,
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_reward_command_label"),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_reward_command_label"),
                null,
                null
        );
        rewardCommandBox.setMaxLength(512);
        rewardCommandBox.setVisible(false);
        rewardCommandBox.setEnabled(false);

        rebuildDraftInputs();
        updateRewardDetailBoxes();
    }

    private void rebuildRewardWidgets() {
        rebuildUi();
    }

    private void rebuildDraftInputs() {
        countBoxes.clear();
        weightBoxes.clear();
        deleteButtons.clear();
        for (int i = 0; i < draft.rewards.size(); i++) {
            final int index = i;
            ShopGuiSupport.RewardDraft reward = draft.rewards.get(i);
            NumericEditBox countBox = addIntegerField(
                    0,
                    0,
                    42,
                    Component.empty(),
                    false,
                    1,
                    64,
                    null
            );
            countBox.setMaxLength(2);
            countBox.setIntValue(Math.max(1, reward.count()));
            countBox.setResponder(text -> updateDraftCountFromInput(index, text));
            countBox.setVisible(false);
            countBox.setEnabled(false);
            countBoxes.add(countBox);

            NumericEditBox weightBox = addIntegerField(
                    0,
                    0,
                    58,
                    Component.empty(),
                    false,
                    1,
                    999999999,
                    null
            );
            weightBox.setMaxLength(9);
            weightBox.setIntValue(Math.max(1, reward.weight()));
            weightBox.setResponder(text -> updateDraftWeightFromInput(index, text));
            weightBox.setVisible(false);
            weightBox.setEnabled(false);
            weightBoxes.add(weightBox);

            StateButton deleteButton = addCompactButton(
                    0,
                    0,
                    38,
                    KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_reward_delete_short"),
                    KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_reward_tooltip_delete"),
                    () -> deleteReward(index)
            );
            setControlVisible(deleteButton, false);
            setControlEnabled(deleteButton, false);
            deleteButtons.add(deleteButton);
        }
        recalc();
        updateDraftInputPositions();
    }

    private void updateDraftInputPositions() {
        listScroll.update(draft.rewards.size(), visibleRows());
        for (int i = 0; i < draft.rewards.size(); i++) {
            int first = listScroll.smoothIndexOffset();
            int shift = listScroll.visualShift(ROW_HEIGHT);
            int y = listY() + (i - first) * ROW_HEIGHT - shift + 4;
            boolean visible = i >= first && i <= first + visibleRows();
            ShopGuiSupport.RewardDraft r = draft.rewards.get(i);
            NumericEditBox countBox = countBoxes.get(i);
            countBox.setX(listX() + 264);
            countBox.setY(y);
            countBox.setVisible(visible && !r.empty());
            countBox.setEnabled(visible && !r.empty());
            NumericEditBox weightBox = weightBoxes.get(i);
            weightBox.setX(listX() + 338);
            weightBox.setY(y);
            weightBox.setVisible(visible && gachaMode());
            weightBox.setEnabled(visible && gachaMode());
            StateButton deleteButton = deleteButtons.get(i);
            deleteButton.setX(deleteX());
            deleteButton.setY(y);
            setControlVisible(deleteButton, visible);
            setControlEnabled(deleteButton, visible);
        }
    }

    private void updateDraftCountFromInput(int index, String text) {
        if (index < 0 || index >= draft.rewards.size()) return;
        ShopGuiSupport.RewardDraft r = draft.rewards.get(index);
        if (r.empty()) return;
        Integer value = countBoxes.get(index).getIntValue();
        if (value == null) return;
        draft.rewards.set(index, new ShopGuiSupport.RewardDraft(ShopGuiSupport.stackSpecWithCount(r.itemId(), value), value, r.weight(), false, 0.0D, r.displayName(), r.command()));
        recalc();
    }

    private void updateDraftWeightFromInput(int index, String text) {
        if (!gachaMode()) return;
        if (index < 0 || index >= draft.rewards.size()) return;
        ShopGuiSupport.RewardDraft r = draft.rewards.get(index);
        Integer value = weightBoxes.get(index).getIntValue();
        if (value == null) return;
        draft.rewards.set(index, new ShopGuiSupport.RewardDraft(r.itemId(), r.count(), value, r.empty(), 0.0D, r.displayName(), r.command()));
        recalc();
    }

    private void syncDraftsFromInputs() {
        for (int i = 0; i < draft.rewards.size(); i++) {
            ShopGuiSupport.RewardDraft r = draft.rewards.get(i);
            int count = r.count();
            int weight = gachaMode() ? r.weight() : 1;
            if (!r.empty()) {
                Integer parsedCount = countBoxes.get(i).getIntValue();
                if (parsedCount != null) count = parsedCount;
            }
            if (gachaMode()) {
                Integer parsedWeight = weightBoxes.get(i).getIntValue();
                if (parsedWeight != null) weight = parsedWeight;
            }
            draft.rewards.set(i, new ShopGuiSupport.RewardDraft(r.empty() ? r.itemId() : ShopGuiSupport.stackSpecWithCount(r.itemId(), count), count, weight, r.empty(), 0.0D, r.displayName(), r.command()));
        }
        recalc();
    }

    private void openItemSelector() {
        KineticSelectors.openItemSelector(this, selection -> {
            if (!selection.isItem()) return;
            ItemStack stack = selection.stack();
            String spec = ShopGuiSupport.selectedStackSpec(stack);
            if (!spec.isBlank()) {
                draft.rewards.add(new ShopGuiSupport.RewardDraft(
                        spec,
                        Math.max(1, Math.min(64, stack.getCount())),
                        gachaMode() ? 10 : 1,
                        false,
                        0.0D,
                        "",
                        ""
                ));
                selectedIndex = draft.rewards.size() - 1;
                rebuildRewardWidgets();
            }
        });
    }

    private void openCommandRewardPicker() {
        syncSelectedRewardFromDetailBoxes();
        syncDraftsFromInputs();
        KineticClientRuntime.openScreen(new CommandRewardPickerScreen(this, draft, gachaMode()));
    }
    private void addEmptyReward() {
        if (!gachaMode()) {
            KineticOverlays.toast("currency_wallet_choice_no_empty", KineticI18n.translatable(choiceMode() ? "msg.adventuresystems.curios.wallet.shop_choice_no_empty" : "msg.adventuresystems.curios.wallet.shop_sell_choice_no_empty"), KineticOverlays.Position.BOTTOM_CENTER, 2200, 0, -30);
            return;
        }
        for (ShopGuiSupport.RewardDraft r : draft.rewards) {
            if (r.empty()) {
                KineticOverlays.toast("currency_wallet_reward_empty_exists", KineticI18n.translatable("msg.adventuresystems.curios.wallet.shop_empty_reward_exists"), KineticOverlays.Position.BOTTOM_CENTER, 2200, 0, -30);
                return;
            }
        }
        draft.rewards.add(new ShopGuiSupport.RewardDraft("", 0, 10, true, 0.0D, "", ""));
        selectedIndex = draft.rewards.size() - 1;
        rebuildRewardWidgets();
    }

    private void recalc() {
        if (!gachaMode()) {
            for (int i = 0; i < draft.rewards.size(); i++) {
                ShopGuiSupport.RewardDraft r = draft.rewards.get(i);
                draft.rewards.set(i, new ShopGuiSupport.RewardDraft(r.empty() ? r.itemId() : ShopGuiSupport.stackSpecWithCount(r.itemId(), r.count()), r.count(), 1, r.empty(), 0.0D, r.displayName(), r.command()));
            }
            return;
        }
        int total = 0;
        for (ShopGuiSupport.RewardDraft r : draft.rewards) total += Math.max(0, r.weight());
        if (total <= 0) return;
        for (int i = 0; i < draft.rewards.size(); i++) {
            ShopGuiSupport.RewardDraft r = draft.rewards.get(i);
            draft.rewards.set(i, new ShopGuiSupport.RewardDraft(r.empty() ? r.itemId() : ShopGuiSupport.stackSpecWithCount(r.itemId(), r.count()), r.count(), r.weight(), r.empty(), r.weight() * 100.0D / total, r.displayName(), r.command()));
        }
    }

    private void finish() {
        syncSelectedRewardFromDetailBoxes();
        syncDraftsFromInputs();
        navigateBack();
    }

    @Override
    public void renderCanvasBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        GuiTheme.panel(graphics, left, top, PANEL_WIDTH, PANEL_HEIGHT);
        graphics.drawCenteredString(font, title, left + PANEL_WIDTH / 2, top + 8, GuiTheme.current().text());
        graphics.drawString(font, KineticI18n.translatable(primaryHintKey()), left + 14, top + 27, GuiTheme.current().text(), true);
        graphics.drawString(font, KineticI18n.translatable(secondaryHintKey()), left + 14, top + 39, GuiTheme.current().mutedText(), true);
        updateDraftInputPositions();

        graphics.drawString(font, KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_reward_column_name"), listX() + 6, listY() - 13, GuiTheme.current().mutedText(), true);
        graphics.drawString(font, KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_reward_column_count"), listX() + 264, listY() - 13, GuiTheme.current().mutedText(), true);
        if (gachaMode()) {
            graphics.drawString(font, KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_reward_column_weight"), listX() + 338, listY() - 13, GuiTheme.current().mutedText(), true);
            graphics.drawString(font, KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_reward_column_chance"), listX() + 416, listY() - 13, GuiTheme.current().mutedText(), true);
        } else {
            graphics.drawString(font, KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_choice_column_note"), listX() + 338, listY() - 13, GuiTheme.current().mutedText(), true);
        }
        GuiTheme.panelAlt(graphics, listX(), listY(), listW(), listH());

        if (draft.rewards.isEmpty()) {
            graphics.drawCenteredString(font, KineticI18n.translatable(emptyListKey()), left + PANEL_WIDTH / 2, listY() + listH() / 2 - 4, GuiTheme.current().text());
            return;
        }
        listScroll.update(draft.rewards.size(), visibleRows());
        int start = listScroll.smoothIndexOffset();
                enableCanvasScissor(graphics, listX(), listY(), listX() + listW(), listY() + listH());
        try {
for (int i = start; i < Math.min(draft.rewards.size(), start + visibleRows() + 1); i++) {
            renderRewardRow(graphics, mouseX, mouseY, i);
        }
        } finally {
            disableCanvasScissor(graphics);
        }
        renderScrollbar(graphics, mouseX, mouseY);
    }

    private void renderRewardRow(GuiGraphics graphics, int mouseX, int mouseY, int index) {
        int y = listY() + (index - listScroll.smoothIndexOffset()) * ROW_HEIGHT - listScroll.visualShift(ROW_HEIGHT);
        int contentW = listW() - scrollbarReserve();
        ShopGuiSupport.RewardDraft r = draft.rewards.get(index);
        boolean hover = GuiTheme.hovering(mouseX, mouseY, listX(), y, contentW, ROW_HEIGHT);
        GuiTheme.stateSurface(
                graphics,
                listX() + 1,
                y + 1,
                contentW - 2,
                ROW_HEIGHT - 2,
                GuiTheme.Surface.PANEL_ALT,
                index == selectedIndex,
                hover,
                false
        );
        if (!r.empty()) {
            ItemStack stack = ShopGuiSupport.stack(r.itemId());
            GuiTheme.itemSlot(graphics, listX() + 5, y + 4, 18, 4, hover);
            graphics.renderItem(stack, listX() + 6, y + 5);
        }
        String name = rewardDisplayName(r);
        int nameX = r.empty() ? listX() + 8 : listX() + 28;
        int nameWidth = r.empty() ? 228 : 205;
        graphics.drawString(font, GuiTheme.trim(font, name, nameWidth), nameX, y + 8, GuiTheme.current().text(), true);
        if (r.empty()) graphics.drawString(font, "-", listX() + 276, y + 8, GuiTheme.current().mutedText(), true);
        if (gachaMode()) {
            graphics.drawString(font, Component.literal(ShopGuiSupport.percent(r.chance())), listX() + 416, y + 8, GuiTheme.current().text(), true);
        } else {
            graphics.drawString(font, KineticI18n.translatable(sellMode() ? "gui.adventuresystems.curios.wallet.shop_sell_choice_note_ready" : "gui.adventuresystems.curios.wallet.shop_choice_note_ready"), listX() + 338, y + 8, GuiTheme.current().text(), true);
        }
        if (!r.command().isBlank()) {
            graphics.drawString(font, KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_reward_command_marker"), deleteX() - 44, y + 8, GuiTheme.current().text(), true);
        }
    }

    private void renderScrollbar(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        listScroll.update(
                draft.rewards.size(),
                visibleRows()
        );

        if (!listScroll.canScroll()) return;

        listScroll.render(
                graphics,
                mouseX,
                mouseY,
                scrollbarX(),
                listY(),
                SCROLLBAR_WIDTH,
                listH(),
                MIN_THUMB_HEIGHT
        );
    }

    @Override
    protected void renderTooltips(GuiGraphics graphics, int scaledMouseX, int scaledMouseY, int rawMouseX, int rawMouseY) {
        ItemStack stack = hoveredRewardItem(scaledMouseX, scaledMouseY);
        if (!stack.isEmpty()) {
            KineticOverlays.requestItemTooltip(stack, rawMouseX, rawMouseY);
            return;
        }
        List<Component> tooltip = rewardTooltipAt(scaledMouseX, scaledMouseY);
        if (!tooltip.isEmpty()) KineticOverlays.requestTooltip(tooltip, rawMouseX, rawMouseY);
    }

    private ItemStack hoveredRewardItem(int mouseX, int mouseY) {
        listScroll.update(draft.rewards.size(), visibleRows());
        int start = listScroll.smoothIndexOffset();
        int shift = listScroll.visualShift(ROW_HEIGHT);
        for (int i = start; i < Math.min(draft.rewards.size(), start + visibleRows() + 1); i++) {
            int y = listY() + (i - start) * ROW_HEIGHT - shift;
            ShopGuiSupport.RewardDraft reward = draft.rewards.get(i);
            if (!reward.empty() && GuiTheme.hovering(mouseX, mouseY, listX() + 6, y + 5, 16, 16)) return ShopGuiSupport.stack(reward.itemId());
        }
        return ItemStack.EMPTY;
    }

    private List<Component> rewardTooltipAt(int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (GuiTheme.hovering(mouseX, mouseY, left + 14, top + 52, 112, 20)) addRewardTooltip(tooltip, addItemButtonKey(), addItemTooltipKey());
        else if (!sellMode() && GuiTheme.hovering(mouseX, mouseY, left + 124, top + 52, 112, 20)) addRewardTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_command_add_reward", "gui.adventuresystems.curios.wallet.shop_command_add_reward_tip");
        else if (gachaMode() && GuiTheme.hovering(mouseX, mouseY, left + 242, top + 52, 96, 20)) addRewardTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_reward_add_empty", "gui.adventuresystems.curios.wallet.shop_reward_tooltip_add_empty");
        else if (GuiTheme.hovering(mouseX, mouseY, gachaMode() ? left + 344 : left + (sellMode() ? 124 : 242), top + 52, gachaMode() ? 66 : 78, 20)) addRewardTooltip(tooltip, "gui.adventuresystems.curios.wallet.shop_reward_clear", clearTooltipKey());
        else if (GuiTheme.hovering(mouseX, mouseY, left + PANEL_WIDTH - 96, top + 52, 82, 20)) addRewardTooltip(tooltip, "gui.done", "gui.adventuresystems.curios.wallet.shop_reward_tooltip_done");
        else if (scrollbarVisible() && GuiTheme.hovering(mouseX, mouseY, scrollbarX(), listY(), SCROLLBAR_WIDTH, listH())) tooltip.add(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_scrollbar_tip"));
        if (tooltip.isEmpty() && GuiTheme.hovering(mouseX, mouseY, listX(), listY(), listW() - scrollbarReserve(), listH())) {
            int shift = listScroll.visualShift(ROW_HEIGHT);
            int row = (mouseY - listY() + shift) / ROW_HEIGHT;
            int index = listScroll.smoothIndexOffset() + row;
            if (index >= 0 && index < draft.rewards.size()) {
                ShopGuiSupport.RewardDraft reward = draft.rewards.get(index);
                tooltip.add(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_name", rewardDisplayName(reward)));
                if (!reward.command().isBlank()) tooltip.add(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_reward_command_tooltip"));
                if (gachaMode()) {
                    tooltip.add(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_reward_tooltip_weight", reward.weight()));
                    tooltip.add(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_reward_tooltip_chance", ShopGuiSupport.percent(reward.chance())));
                } else {
                    tooltip.add(KineticI18n.translatable(sellMode() ? "gui.adventuresystems.curios.wallet.shop_sell_choice_note_ready" : "gui.adventuresystems.curios.wallet.shop_choice_note_ready"));
                }
                if (!reward.empty()) tooltip.add(KineticI18n.translatable(sellMode() ? "gui.adventuresystems.curios.wallet.shop_sell_choice_tooltip_count" : "gui.adventuresystems.curios.wallet.shop_reward_tooltip_count", Math.max(1, reward.count())));
            }
        }
        return tooltip;
    }

    private void addRewardTooltip(List<Component> tooltip, String titleKey, String bodyKey) {
        tooltip.add(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_name", KineticI18n.translatable(titleKey)));
        tooltip.add(KineticI18n.translatable(bodyKey));
    }

    private void deleteReward(int index) {
        if (index < 0 || index >= draft.rewards.size()) return;
        syncSelectedRewardFromDetailBoxes();
        syncDraftsFromInputs();
        draft.rewards.remove(index);
        if (selectedIndex == index) selectedIndex = -1;
        else if (selectedIndex > index) selectedIndex--;
        listScroll.update(draft.rewards.size(), visibleRows());
        rebuildRewardWidgets();
    }

    @Override
    protected boolean canvasMouseClicked(double mouseX, double mouseY, int button) {
        updateDraftInputPositions();
        listScroll.update(
                draft.rewards.size(),
                visibleRows()
        );

        if (KineticMouseButtons.isPrimary(button)
                && listScroll.beginDrag(
                        mouseX,
                        mouseY,
                        scrollbarX(),
                        listY(),
                        SCROLLBAR_WIDTH,
                        listH(),
                        MIN_THUMB_HEIGHT,
                        0
                )) {
            return true;
        }
        if (super.canvasMouseClicked(mouseX, mouseY, button)) return true;
        if (!KineticMouseButtons.isPrimary(button)) return false;
        if (!GuiTheme.hovering(mouseX, mouseY, listX(), listY(), listW() - scrollbarReserve(), listH())) return false;
        int shift = listScroll.visualShift(ROW_HEIGHT);
        int row = (int) ((mouseY - listY() + shift) / ROW_HEIGHT);
        int index = listScroll.smoothIndexOffset() + row;
        if (index < 0 || index >= draft.rewards.size()) return false;
        syncSelectedRewardFromDetailBoxes();
        selectedIndex = index;
        updateRewardDetailBoxes();
        return true;
    }

    @Override
    protected boolean canvasMouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY
    ) {
        if (listScroll.drag(
                mouseY,
                listY(),
                listH(),
                MIN_THUMB_HEIGHT
        )) {
            updateDraftInputPositions();
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
    protected boolean canvasMouseReleased(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (listScroll.release(button)) return true;
        return super.canvasMouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected boolean canvasMouseScrolled(
            double mouseX,
            double mouseY,
            double delta
    ) {
        if (!GuiTheme.hovering(
                mouseX,
                mouseY,
                listX(),
                listY(),
                listW(),
                listH()
        )) {
            return super.canvasMouseScrolled(
                    mouseX,
                    mouseY,
                    delta
            );
        }

        listScroll.update(
                draft.rewards.size(),
                visibleRows()
        );

        if (listScroll.scroll(delta)) {
            updateDraftInputPositions();
            return true;
        }

        return false;
    }

    @Override
    protected boolean handleCloseRequest() {
        finish();
        return true;
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

    private String rewardDisplayName(ShopGuiSupport.RewardDraft reward) {
        if (reward == null) return "";
        if (reward.displayName() != null && !reward.displayName().isBlank()) return reward.displayName();
        if (reward.empty()) return KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_gacha_empty").getString();
        return ShopGuiSupport.stackName(reward.itemId());
    }

    private void updateSelectedRewardName(String value) {
        if (selectedIndex < 0 || selectedIndex >= draft.rewards.size()) return;
        ShopGuiSupport.RewardDraft r = draft.rewards.get(selectedIndex);
        draft.rewards.set(selectedIndex, new ShopGuiSupport.RewardDraft(r.itemId(), r.count(), r.weight(), r.empty(), r.chance(), value == null ? "" : value.trim(), r.command()));
    }

    private void syncSelectedRewardFromDetailBoxes() {
        if (selectedIndex < 0 || selectedIndex >= draft.rewards.size()) return;
        ShopGuiSupport.RewardDraft r = draft.rewards.get(selectedIndex);
        String name = rewardNameBox == null ? r.displayName() : rewardNameBox.getValue().trim();
        draft.rewards.set(selectedIndex, new ShopGuiSupport.RewardDraft(r.itemId(), r.count(), r.weight(), r.empty(), r.chance(), name, r.command()));
    }

    private void updateRewardDetailBoxes() {
        boolean active = false;
        if (rewardNameBox != null) {
            rewardNameBox.setVisible(active);
            rewardNameBox.setEnabled(active);
        }
        if (rewardCommandBox != null) {
            rewardCommandBox.setVisible(false);
            rewardCommandBox.setEnabled(false);
        }
        if (!active) {
            if (rewardNameBox != null) rewardNameBox.setValue("");
            if (rewardCommandBox != null) rewardCommandBox.setValue("");
            return;
        }
        ShopGuiSupport.RewardDraft r = draft.rewards.get(selectedIndex);
        if (rewardNameBox != null && !Objects.equals(rewardNameBox.getValue(), r.displayName())) rewardNameBox.setValue(r.displayName());
        if (rewardCommandBox != null && !Objects.equals(rewardCommandBox.getValue(), r.command())) rewardCommandBox.setValue(r.command());
    }

    private boolean gachaMode() { return draft.mode == Shop.Mode.BUY && draft.gacha && !draft.selectable; }
    private boolean choiceMode() { return draft.mode == Shop.Mode.BUY && draft.selectable; }

    private boolean sellMode() { return draft.mode == Shop.Mode.SELL; }
    private int listX() { return left + 14; }
    private int listY() { return top + 88; }
    private int listW() { return PANEL_WIDTH - 28; }
    private int listH() { return Math.max(ROW_HEIGHT * 7, top + PANEL_HEIGHT - 34 - listY()); }
    private int visibleRows() { return Math.max(1, listH() / ROW_HEIGHT); }
    private int rewardDetailY() { return listY() + listH() + 14; }
    private int rewardNameInputY() { return rewardDetailY() + 24; }
    private int rewardCommandInputY() { return rewardDetailY() + 48; }
    private int deleteX() { return listX() + listW() - scrollbarReserve() - 46; }
    private int scrollbarX() { return listX() + listW() - SCROLLBAR_WIDTH - 3; }
    private int scrollbarReserve() { return listScroll.canScroll() ? 16 : 0; }
    private boolean scrollbarVisible() { return listScroll.canScroll(); }
    private static boolean isControlVisible(KineticControl control) {
        return control != null && control.isVisible();
    }

    private static boolean isControlEnabled(KineticControl control) {
        return control != null && control.isEnabled();
    }

    private static void setControlVisible(KineticControl control, boolean visible) {
        if (control != null) control.setVisible(visible);
    }

    private static void setControlEnabled(KineticControl control, boolean enabled) {
        if (control != null) control.setEnabled(enabled);
    }

}

