package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.adventuresystems.curios.util.ColorText;
import dev.xyat.adventuresystems.curios.wallet.shop.Shop;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.overlay.GuiOverlay;
import dev.xyat.kineticcore.api.client.selector.ItemSelectorScreen;
import dev.xyat.kineticcore.api.client.screen.KineticScreen;
import dev.xyat.kineticcore.api.client.widget.KineticWidgets.GridScrollController;
import dev.xyat.kineticcore.api.client.widget.KineticWidgets.NumericEditBox;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
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
    private EditBox rewardNameBox;
    private EditBox rewardCommandBox;
    private int selectedIndex = -1;
    private int left;
    private int top;
    private final GridScrollController listScroll = new GridScrollController();

    RewardPoolScreen(Screen parent, ShopGuiSupport.EditorDraft draft) {
        super(ColorText.translatable(titleKey(draft)));
        this.parent = parent;
        this.draft = draft;
        useCanvas(
                680f,
                430f,
                6
        );
    }

    @Override
    protected void buildUi() {
        left = (canvasWidth - PANEL_WIDTH) / 2;
        top = (canvasHeight - PANEL_HEIGHT) / 2;
        addRenderableWidget(Button.builder(ColorText.translatable(addItemButtonKey()), button -> openItemSelector()).bounds(left + 14, top + 52, 104, 20).build());
        if (!sellMode()) {
            addRenderableWidget(Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_command_add_reward"), button -> openCommandRewardPicker()).bounds(left + 124, top + 52, 112, 20).build());
        }
        if (gachaMode()) {
            addRenderableWidget(Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_reward_add_empty"), button -> addEmptyReward()).bounds(left + 242, top + 52, 96, 20).build());
            addRenderableWidget(Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_reward_clear"), button -> { draft.rewards.clear(); rebuildRewardWidgets(); }).bounds(left + 344, top + 52, 66, 20).build());
        } else {
            addRenderableWidget(Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_reward_clear"), button -> { draft.rewards.clear(); rebuildRewardWidgets(); }).bounds(left + (sellMode() ? 124 : 242), top + 52, 78, 20).build());
        }
        addRenderableWidget(Button.builder(ColorText.translatable("gui.done"), button -> finish()).bounds(left + PANEL_WIDTH - 96, top + 52, 82, 20).build());

        rewardNameBox = new EditBox(font, listX() + 92, rewardNameInputY(), 216, 18, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_reward_display_name_label"));
        rewardNameBox.setMaxLength(64);
        rewardNameBox.setResponder(this::updateSelectedRewardName);
        addRenderableWidget(rewardNameBox);

        rewardCommandBox = new EditBox(font, listX() + 92, rewardCommandInputY(), listW() - 104, 18, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_reward_command_label"));
        rewardCommandBox.setMaxLength(512);
        rewardCommandBox.visible = false;
        rewardCommandBox.active = false;
        addRenderableWidget(rewardCommandBox);

        rebuildDraftInputs();
        updateRewardDetailBoxes();
    }

    private void rebuildRewardWidgets() {
        clearWidgets();
        buildUi();
    }

    private void rebuildDraftInputs() {
        countBoxes.clear();
        weightBoxes.clear();
        for (int i = 0; i < draft.rewards.size(); i++) {
            final int index = i;
            ShopGuiSupport.RewardDraft r = draft.rewards.get(i);
            NumericEditBox countBox = NumericEditBox.integer(
                    font,
                    0,
                    0,
                    42,
                    18,
                    Component.empty(),
                    false,
                    1,
                    64
            );
            countBox.setMaxLength(2);
            countBox.setIntValue(Math.max(1, r.count()));
            countBox.setResponder(text -> updateDraftCountFromInput(index, text));
            countBox.visible = false;
            countBox.active = false;
            countBoxes.add(countBox);
            addRenderableWidget(countBox);

            NumericEditBox weightBox = NumericEditBox.integer(
                    font,
                    0,
                    0,
                    58,
                    18,
                    Component.empty(),
                    false,
                    1,
                    999999999
            );
            weightBox.setMaxLength(9);
            weightBox.setIntValue(Math.max(1, r.weight()));
            weightBox.setResponder(text -> updateDraftWeightFromInput(index, text));
            weightBox.visible = false;
            weightBox.active = false;
            weightBoxes.add(weightBox);
            addRenderableWidget(weightBox);
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
            EditBox countBox = countBoxes.get(i);
            countBox.setX(listX() + 264);
            countBox.setY(y);
            countBox.visible = visible && !r.empty();
            countBox.active = visible && !r.empty();
            EditBox weightBox = weightBoxes.get(i);
            weightBox.setX(listX() + 338);
            weightBox.setY(y);
            weightBox.visible = visible && gachaMode();
            weightBox.active = visible && gachaMode();
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
        Minecraft.getInstance().setScreen(new ItemSelectorScreen(this, selection -> {
            if (!selection.isItem()) return;
            ItemStack stack = selection.stack();
            String spec = ShopGuiSupport.selectedStackSpec(stack);
            if (!spec.isBlank()) {
                draft.rewards.add(new ShopGuiSupport.RewardDraft(spec, Math.max(1, Math.min(64, stack.getCount())), gachaMode() ? 10 : 1, false, 0.0D, "", ""));
                selectedIndex = draft.rewards.size() - 1;
                rebuildRewardWidgets();
            }
        }));
    }


    private void openCommandRewardPicker() {
        syncSelectedRewardFromDetailBoxes();
        syncDraftsFromInputs();
        Minecraft.getInstance().setScreen(new CommandRewardPickerScreen(this, draft, gachaMode()));
    }
    private void addEmptyReward() {
        if (!gachaMode()) {
            GuiOverlay.toast("currency_wallet_choice_no_empty", ColorText.translatable(choiceMode() ? "msg.adventuresystems.curios.wallet.shop_choice_no_empty" : "msg.adventuresystems.curios.wallet.shop_sell_choice_no_empty"), GuiOverlay.Position.BOTTOM_CENTER, 2200, 0, -30);
            return;
        }
        for (ShopGuiSupport.RewardDraft r : draft.rewards) {
            if (r.empty()) {
                GuiOverlay.toast("currency_wallet_reward_empty_exists", ColorText.translatable("msg.adventuresystems.curios.wallet.shop_empty_reward_exists"), GuiOverlay.Position.BOTTOM_CENTER, 2200, 0, -30);
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
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void renderCanvasBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ShopGuiSupport.renderBox(graphics, left, top, PANEL_WIDTH, PANEL_HEIGHT, 0xFF101010);
        graphics.drawCenteredString(font, title, left + PANEL_WIDTH / 2, top + 8, ShopGuiSupport.GOLD);
        graphics.drawString(font, ColorText.translatable(primaryHintKey()), left + 14, top + 27, ShopGuiSupport.CYAN, true);
        graphics.drawString(font, ColorText.translatable(secondaryHintKey()), left + 14, top + 39, ShopGuiSupport.TEXT_GRAY, true);
        updateDraftInputPositions();

        graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_reward_column_name"), listX() + 6, listY() - 13, ShopGuiSupport.TEXT_GRAY, true);
        graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_reward_column_count"), listX() + 264, listY() - 13, ShopGuiSupport.TEXT_GRAY, true);
        if (gachaMode()) {
            graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_reward_column_weight"), listX() + 338, listY() - 13, ShopGuiSupport.TEXT_GRAY, true);
            graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_reward_column_chance"), listX() + 416, listY() - 13, ShopGuiSupport.TEXT_GRAY, true);
        } else {
            graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_choice_column_note"), listX() + 338, listY() - 13, ShopGuiSupport.TEXT_GRAY, true);
        }
        graphics.renderOutline(listX(), listY(), listW(), listH(), ShopGuiSupport.CYAN_DARK);

        if (draft.rewards.isEmpty()) {
            graphics.drawCenteredString(font, ColorText.translatable(emptyListKey()), left + PANEL_WIDTH / 2, listY() + listH() / 2 - 4, ShopGuiSupport.RED);
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
            graphics.disableScissor();
        }
        renderScrollbar(graphics, mouseX, mouseY);
    }

    private void renderRewardRow(GuiGraphics graphics, int mouseX, int mouseY, int index) {
        int y = listY() + (index - listScroll.smoothIndexOffset()) * ROW_HEIGHT - listScroll.visualShift(ROW_HEIGHT);
        int contentW = listW() - scrollbarReserve();
        ShopGuiSupport.RewardDraft r = draft.rewards.get(index);
        boolean hover = GuiTheme.hovering(mouseX, mouseY, listX(), y, contentW, ROW_HEIGHT);
        graphics.fill(listX() + 1, y + 1, listX() + contentW - 1, y + ROW_HEIGHT - 1, hover ? ShopGuiSupport.ROW_HOVER : ShopGuiSupport.ROW_BG);
        if (index == selectedIndex) graphics.renderOutline(listX(), y, contentW, ROW_HEIGHT, ShopGuiSupport.CYAN);
        if (!r.empty()) {
            ItemStack stack = ShopGuiSupport.stack(r.itemId());
            GuiTheme.itemSlot(graphics, stack, listX() + 5, y + 4, 18, 4, hover);
            graphics.renderItem(stack, listX() + 6, y + 5);
        }
        String name = rewardDisplayName(r);
        int nameX = r.empty() ? listX() + 8 : listX() + 28;
        int nameColor = r.empty() ? ShopGuiSupport.CYAN : r.command().isBlank() ? ShopGuiSupport.TEXT_WHITE : ShopGuiSupport.GOLD;
        int nameWidth = r.empty() ? 228 : 205;
        graphics.drawString(font, GuiTheme.trim(font, name, nameWidth), nameX, y + 8, nameColor, true);
        if (r.empty()) graphics.drawString(font, "-", listX() + 276, y + 8, ShopGuiSupport.TEXT_GRAY, true);
        if (gachaMode()) {
            graphics.drawString(font, Component.literal(ShopGuiSupport.percent(r.chance())), listX() + 416, y + 8, ShopGuiSupport.CYAN, true);
        } else {
            graphics.drawString(font, ColorText.translatable(sellMode() ? "gui.adventuresystems.curios.wallet.shop_sell_choice_note_ready" : "gui.adventuresystems.curios.wallet.shop_choice_note_ready"), listX() + 338, y + 8, ShopGuiSupport.CYAN, true);
        }
        if (!r.command().isBlank()) {
            graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_reward_command_marker"), deleteX() - 44, y + 8, ShopGuiSupport.GOLD, true);
        }
        boolean deleteHover = GuiTheme.hovering(mouseX, mouseY, deleteX(), y + 4, 38, 18);
        graphics.fill(deleteX(), y + 4, deleteX() + 38, y + 22, deleteHover ? 0xAA993333 : 0x88553333);
        graphics.drawCenteredString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_reward_delete_short"), deleteX() + 19, y + 9, ShopGuiSupport.TEXT_WHITE);
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
                MIN_THUMB_HEIGHT,
                ShopGuiSupport.SCROLLBAR_TRACK,
                ShopGuiSupport.SCROLLBAR_THUMB,
                ShopGuiSupport.SCROLLBAR_HOVER
        );
    }

    @Override
    protected void renderTooltips(GuiGraphics graphics, int scaledMouseX, int scaledMouseY, int rawMouseX, int rawMouseY) {
        ItemStack stack = hoveredRewardItem(scaledMouseX, scaledMouseY);
        if (!stack.isEmpty()) {
            GuiOverlay.requestItemTooltip(stack, rawMouseX, rawMouseY);
            return;
        }
        List<Component> tooltip = rewardTooltipAt(scaledMouseX, scaledMouseY);
        if (!tooltip.isEmpty()) GuiOverlay.requestTooltip(tooltip, rawMouseX, rawMouseY);
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
        else if (scrollbarVisible() && GuiTheme.hovering(mouseX, mouseY, scrollbarX(), listY(), SCROLLBAR_WIDTH, listH())) tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_scrollbar_tip").withStyle(ChatFormatting.GOLD));
        if (tooltip.isEmpty() && GuiTheme.hovering(mouseX, mouseY, listX(), listY(), listW() - scrollbarReserve(), listH())) {
            int shift = listScroll.visualShift(ROW_HEIGHT);
            int row = (mouseY - listY() + shift) / ROW_HEIGHT;
            int index = listScroll.smoothIndexOffset() + row;
            if (index >= 0 && index < draft.rewards.size()) {
                ShopGuiSupport.RewardDraft reward = draft.rewards.get(index);
                tooltip.add(Component.literal(rewardDisplayName(reward)).withStyle(ChatFormatting.GOLD));
                if (!reward.command().isBlank()) tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_reward_command_tooltip").withStyle(ChatFormatting.YELLOW));
                if (gachaMode()) {
                    tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_reward_tooltip_weight", reward.weight()).withStyle(ChatFormatting.YELLOW));
                    tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_reward_tooltip_chance", ShopGuiSupport.percent(reward.chance())).withStyle(ChatFormatting.AQUA));
                } else {
                    tooltip.add(ColorText.translatable(sellMode() ? "gui.adventuresystems.curios.wallet.shop_sell_choice_note_ready" : "gui.adventuresystems.curios.wallet.shop_choice_note_ready").withStyle(ChatFormatting.AQUA));
                }
                if (!reward.empty()) tooltip.add(ColorText.translatable(sellMode() ? "gui.adventuresystems.curios.wallet.shop_sell_choice_tooltip_count" : "gui.adventuresystems.curios.wallet.shop_reward_tooltip_count", Math.max(1, reward.count())));
                if (GuiTheme.hovering(mouseX, mouseY, deleteX(), listY() + row * ROW_HEIGHT - shift + 4, 38, 18)) tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_reward_tooltip_delete").withStyle(ChatFormatting.RED));
            }
        }
        return tooltip;
    }

    private void addRewardTooltip(List<Component> tooltip, String titleKey, String bodyKey) {
        tooltip.add(ColorText.translatable(titleKey).withStyle(ChatFormatting.GOLD));
        tooltip.add(ColorText.translatable(bodyKey));
    }

    @Override
    protected boolean canvasMouseClicked(double mouseX, double mouseY, int button) {
        updateDraftInputPositions();
        listScroll.update(
                draft.rewards.size(),
                visibleRows()
        );

        if (button == 0
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
        if (button != 0) return false;
        if (!GuiTheme.hovering(mouseX, mouseY, listX(), listY(), listW() - scrollbarReserve(), listH())) return false;
        int shift = listScroll.visualShift(ROW_HEIGHT);
        int row = (int) ((mouseY - listY() + shift) / ROW_HEIGHT);
        int index = listScroll.smoothIndexOffset() + row;
        if (index < 0 || index >= draft.rewards.size()) return false;
        if (GuiTheme.hovering(mouseX, mouseY, deleteX(), listY() + row * ROW_HEIGHT - shift + 4, 38, 18)) {
            syncSelectedRewardFromDetailBoxes();
            syncDraftsFromInputs();
            draft.rewards.remove(index);
            if (selectedIndex == index) selectedIndex = -1;
            else if (selectedIndex > index) selectedIndex--;
            listScroll.update(draft.rewards.size(), visibleRows());
            rebuildRewardWidgets();
            return true;
        }
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
    public void onClose() {
        finish();
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
        if (reward.empty()) return ColorText.translatable("gui.adventuresystems.curios.wallet.shop_gacha_empty").getString();
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
            rewardNameBox.visible = active;
            rewardNameBox.active = active;
        }
        if (rewardCommandBox != null) {
            rewardCommandBox.visible = false;
            rewardCommandBox.active = false;
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
}

