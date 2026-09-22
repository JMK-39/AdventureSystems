package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.kineticcore.api.client.input.KineticMouseButtons;
import dev.xyat.kineticcore.api.client.command.KineticCommandSuggestions;
import dev.xyat.kineticcore.api.client.overlay.KineticOverlays;
import dev.xyat.kineticcore.api.client.screen.KineticScreen;
import dev.xyat.kineticcore.api.client.selector.KineticSelectors;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.widget.button.KineticButtons.StateButton;
import dev.xyat.kineticcore.api.client.widget.input.KineticTextFields.KineticEditBox;
import dev.xyat.kineticcore.api.client.widget.scroll.KineticScroll.GridScrollController;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import dev.xyat.kineticcore.api.text.KineticI18n;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

final class CommandManageScreen extends KineticScreen {
    private static final int PANEL_WIDTH = 640;
    private static final int PANEL_HEIGHT = 360;
    private static final int ICON_BUTTON_SIZE = 20;
    private static final int GRID_CELL = 24;
    private static final int GRID_GAP = 5;
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int MIN_THUMB_HEIGHT = 18;

    private final Screen parent;
    private final ShopGuiSupport.EditorDraft draft;
    private final GridScrollController gridScroll = new GridScrollController();
    private int left;
    private int top;
    private int selectedIndex = -1;
    private KineticEditBox nameBox;
    private KineticEditBox commandBox;
    private StateButton saveButton;
    private String iconId = "";
    private KineticCommandSuggestions.Session commandSuggestions;

    CommandManageScreen(Screen parent, ShopGuiSupport.EditorDraft draft) {
        super(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_command_manage_title"));
        this.parent = parent;
        setParentScreen(parent);
        this.draft = draft;
        useCanvas(640f, 360f, 6);
    }

    @Override
    protected void buildUi() {
        left = (canvasWidth() - PANEL_WIDTH) / 2;
        top = (canvasHeight() - PANEL_HEIGHT) / 2;
        if (iconId.isBlank()) iconId = fallbackIconId();

        addButton(
                iconButtonX(),
                iconButtonY(),
                ICON_BUTTON_SIZE,
                Component.empty(),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_command_icon_tooltip"),
                this::openIconSelector
        );

        nameBox = addTextField(
                nameBoxX(),
                nameBoxY(),
                nameBoxWidth(),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_command_name"),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_command_name"),
                null,
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_command_name_tooltip")
        );
        nameBox.setMaxLength(64);

        saveButton = addButton(
                saveButtonX(),
                nameBoxY() - 1,
                58,
                saveButtonText(),
                null,
                this::saveCommand
        );
        addButton(
                clearButtonX(),
                nameBoxY() - 1,
                54,
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_command_new"),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_command_clear_tooltip"),
                this::clearEditor
        );
        addButton(
                left + PANEL_WIDTH - 90,
                top + 8,
                76,
                KineticI18n.translatable("gui.done"),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_command_done_tooltip"),
                this::returnToParent
        );

        commandBox = addTextField(
                commandBoxX(),
                commandBoxY(),
                commandBoxWidth(),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_command_text"),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_command_text"),
                null,
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_command_text_tooltip", "")
        );
        commandBox.setMaxLength(2048);
        commandBox.setResponder(this::onCommandEdited);

        initCommandSuggestions();
        loadSelectedIntoEditor();
        updateButtons();
    }

    private String fallbackIconId() {
        return draft.itemId == null || draft.itemId.isBlank() ? "minecraft:barrier" : draft.itemId;
    }

    private int iconLabelX() { return left + 18; }
    private int iconButtonX() { return left + 76; }
    private int iconButtonY() { return top + 40; }
    private int iconPreviewX() { return iconButtonX() + 2; }
    private int iconPreviewY() { return iconButtonY() + 2; }
    private int nameLabelX() { return left + 120; }
    private int nameBoxX() { return left + 188; }
    private int nameBoxY() { return top + 41; }
    private int nameBoxWidth() { return 306; }
    private int saveButtonX() { return left + 506; }
    private int clearButtonX() { return left + 568; }
    private int listX() { return left + 16; }
    private int listY() { return top + 78; }
    private int listW() { return PANEL_WIDTH - 32; }
    private int listH() { return commandBoxY() - listY() - 14; }
    private int gridColumns() { return Math.max(1, (listW() - 14) / (GRID_CELL + GRID_GAP)); }
    private int visibleGridRows() { return Math.max(1, (listH() - 8) / (GRID_CELL + GRID_GAP)); }
    private int totalGridRows() { return (draft.commands.size() + gridColumns() - 1) / gridColumns(); }
    private boolean scrollbarVisible() {
        gridScroll.update(totalGridRows(), visibleGridRows());
        return gridScroll.canScroll();
    }
    private int scrollbarX() { return listX() + listW() - SCROLLBAR_WIDTH - 3; }
    private int commandLabelX() { return left + 18; }
    private int commandBoxX() { return left + 84; }
    private int commandBoxY() { return top + PANEL_HEIGHT - 28; }
    private int commandBoxWidth() { return PANEL_WIDTH - 104; }

    private Component saveButtonText() {
        return KineticI18n.translatable(selectedIndex >= 0
                ? "gui.adventuresystems.curios.wallet.shop_command_update"
                : "gui.adventuresystems.curios.wallet.shop_command_add");
    }

    private Component saveButtonTooltip() {
        return KineticI18n.translatable(selectedIndex >= 0
                ? "gui.adventuresystems.curios.wallet.shop_command_update_tooltip"
                : "gui.adventuresystems.curios.wallet.shop_command_add_tooltip");
    }

    private void updateButtons() {
        if (saveButton != null) saveButton.setText(saveButtonText());
    }

    private void openIconSelector() {
        KineticSelectors.openItemSelector(this, selection -> {
            if (!selection.isItem()) return;
            ItemStack stack = selection.stack();
            String spec = ShopGuiSupport.selectedStackSpec(stack);
            if (!spec.isBlank()) iconId = spec;
        });
    }

    private void saveCommand() {
        String command = commandBox == null ? "" : commandBox.getValue().trim();
        String name = nameBox == null ? "" : nameBox.getValue().trim();
        if (name.isBlank()) {
            KineticOverlays.toast("wallet_command_name_empty", KineticI18n.translatable("msg.adventuresystems.curios.wallet.shop_command_name_empty"), KineticOverlays.Position.BOTTOM_CENTER, 2200, 0, -30);
            return;
        }
        if (command.isBlank()) {
            KineticOverlays.toast("wallet_command_empty", KineticI18n.translatable("msg.adventuresystems.curios.wallet.shop_command_empty"), KineticOverlays.Position.BOTTOM_CENTER, 2200, 0, -30);
            return;
        }
        if (!command.startsWith("/")) {
            KineticOverlays.toast("wallet_command_slash_required", KineticI18n.translatable("msg.adventuresystems.curios.wallet.shop_command_slash_required"), KineticOverlays.Position.BOTTOM_CENTER, 2400, 0, -30);
            if (commandBox != null) {
                focusControl(commandBox);
            }
            return;
        }
        for (int i = 0; i < draft.commands.size(); i++) {
            if (i == selectedIndex) continue;
            ShopGuiSupport.CommandDraft existing = draft.commands.get(i);
            if (existing.displayName() != null && existing.displayName().trim().equalsIgnoreCase(name)) {
                KineticOverlays.toast("wallet_command_name_duplicate", KineticI18n.translatable("msg.adventuresystems.curios.wallet.shop_command_name_duplicate"), KineticOverlays.Position.BOTTOM_CENTER, 2200, 0, -30);
                return;
            }
            if (commandCompareKey(existing.command()).equals(commandCompareKey(command))) {
                KineticOverlays.toast("wallet_command_duplicate", KineticI18n.translatable("msg.adventuresystems.curios.wallet.shop_command_duplicate"), KineticOverlays.Position.BOTTOM_CENTER, 2200, 0, -30);
                return;
            }
        }
        String icon = iconId == null || iconId.isBlank() ? fallbackIconId() : iconId;
        ShopGuiSupport.CommandDraft value = new ShopGuiSupport.CommandDraft(icon, name, command);
        if (selectedIndex >= 0 && selectedIndex < draft.commands.size()) {
            draft.commands.set(selectedIndex, value);
        } else {
            draft.commands.add(value);
        }
        clearEditor();
        gridScroll.update(totalGridRows(), visibleGridRows());
    }

    private void clearEditor() {
        selectedIndex = -1;
        iconId = fallbackIconId();
        if (nameBox != null) nameBox.setValue("");
        if (commandBox != null) commandBox.setValue("");
        updateButtons();
        updateCommandSuggestions();
    }

    private void loadSelectedIntoEditor() {
        if (selectedIndex < 0 || selectedIndex >= draft.commands.size()) {
            updateButtons();
            return;
        }
        ShopGuiSupport.CommandDraft command = draft.commands.get(selectedIndex);
        iconId = command.iconId() == null || command.iconId().isBlank() ? fallbackIconId() : command.iconId();
        if (nameBox != null) nameBox.setValue(command.displayName());
        if (commandBox != null) commandBox.setValue(commandInputText(command.command()));
        if (commandBox != null) {
            focusControl(commandBox);
        }
        updateButtons();
        updateCommandSuggestions();
    }

    private String commandInputText(String value) {
        if (value == null || value.isBlank()) return "";
        String trimmed = value.trim();
        return trimmed.startsWith("/") ? trimmed : "/" + trimmed;
    }

    private String commandCompareKey(String value) {
        if (value == null) return "";
        String trimmed = value.trim();
        while (trimmed.startsWith("/")) trimmed = trimmed.substring(1).trim();
        return trimmed.toLowerCase(java.util.Locale.ROOT);
    }

    private void initCommandSuggestions() {
        if (commandBox == null) return;
        commandSuggestions = KineticCommandSuggestions.create(
                commandBox,
                canvasWidth(),
                canvasHeight(),
                KineticCommandSuggestions.Options.fieldAligned(false, true, 8, Integer.MIN_VALUE)
        );
        commandSuggestions.setAllowSuggestions(true);
        commandSuggestions.update();
    }

    private void onCommandEdited(String text) {
        updateCommandSuggestions();
    }

    private void updateCommandSuggestions() {
        if (commandSuggestions == null) return;
        commandSuggestions.setAllowSuggestions(true);
        commandSuggestions.update();
    }

    @Override
    protected void renderCanvasBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        GuiTheme.panel(graphics, left, top, PANEL_WIDTH, PANEL_HEIGHT);
        graphics.drawCenteredString(font, title, left + PANEL_WIDTH / 2, top + 10, GuiTheme.current().text());

        graphics.drawString(font, KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_command_icon_preview"), iconLabelX(), iconButtonY() + 6, GuiTheme.current().mutedText(), true);
        GuiTheme.itemSlot(graphics, iconButtonX(), iconButtonY(), ICON_BUTTON_SIZE, 4, GuiTheme.hovering(mouseX, mouseY, iconButtonX(), iconButtonY(), ICON_BUTTON_SIZE, ICON_BUTTON_SIZE));
        graphics.renderItem(ShopGuiSupport.stack(iconId), iconPreviewX(), iconPreviewY());
        graphics.drawString(font, KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_command_name"), nameLabelX(), nameBoxY() + 5, GuiTheme.current().mutedText(), true);

        graphics.drawString(font, KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_command_list"), listX(), listY() - 14, GuiTheme.current().text(), true);
        GuiTheme.panelAlt(graphics, listX(), listY(), listW(), listH());
        renderCommandGrid(graphics, mouseX, mouseY);
        if (draft.commands.isEmpty()) {
            graphics.drawCenteredString(font, KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_command_empty"), left + PANEL_WIDTH / 2, listY() + listH() / 2 - 4, GuiTheme.current().mutedText());
        }
        renderScrollbar(graphics, mouseX, mouseY);

        graphics.drawString(font, KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_command_text"), commandLabelX(), commandBoxY() + 5, GuiTheme.current().mutedText(), true);
    }

    private void renderCommandGrid(GuiGraphics graphics, int mouseX, int mouseY) {
        int columns = gridColumns();
        gridScroll.update(totalGridRows(), visibleGridRows());
        int firstRow = gridScroll.smoothIndexOffset();
        int shift = gridScroll.visualShift(GRID_CELL + GRID_GAP);
        int start = firstRow * columns;
        int end = Math.min(draft.commands.size(), start + columns * (visibleGridRows() + 1));
        int usableRight = scrollbarVisible() ? scrollbarX() - 4 : listX() + listW() - 4;
        enableCanvasScissor(graphics, listX(), listY(), listX() + listW(), listY() + listH());
        try {
            for (int i = start; i < end; i++) {
                int local = i - start;
                int col = local % columns;
                int row = local / columns;
                int x = listX() + 4 + col * (GRID_CELL + GRID_GAP);
                int y = listY() + 4 + row * (GRID_CELL + GRID_GAP) - shift;
                if (x + GRID_CELL > usableRight) continue;
                boolean hover = GuiTheme.hovering(mouseX, mouseY, x, y, GRID_CELL, GRID_CELL);
                boolean selected = i == selectedIndex;
                ItemStack icon = ShopGuiSupport.stack(draft.commands.get(i).iconId());
                GuiTheme.itemSlot(graphics, x, y, GRID_CELL, GRID_CELL, 4, selected, hover, false);
                graphics.renderItem(icon, x + 4, y + 4);
            }
        } finally {
            disableCanvasScissor(graphics);
        }
    }

    private void renderScrollbar(GuiGraphics graphics, int mouseX, int mouseY) {
        gridScroll.update(totalGridRows(), visibleGridRows());
        if (!gridScroll.canScroll()) return;
        gridScroll.render(
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
    protected void renderCanvasForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (commandSuggestions == null || commandBox == null || !isControlFocused(commandBox)) return;
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 500);
        commandSuggestions.render(graphics, mouseX, mouseY);
        graphics.pose().popPose();
    }

    @Override
    protected void renderTooltips(GuiGraphics graphics, int scaledMouseX, int scaledMouseY, int rawMouseX, int rawMouseY) {
        if (saveButton != null && GuiTheme.hovering(scaledMouseX, scaledMouseY, saveButton.getX(), saveButton.getY(), saveButton.getWidth(), saveButton.getHeight())) {
            KineticOverlays.requestTooltip(List.of(saveButtonTooltip()), rawMouseX, rawMouseY);
            return;
        }
        int index = commandIndexAt(scaledMouseX, scaledMouseY);
        if (index < 0 || index >= draft.commands.size()) return;
        ShopGuiSupport.CommandDraft command = draft.commands.get(index);
        List<FormattedCharSequence> lines = new ArrayList<>();
        String name = command.displayName() == null || command.displayName().isBlank()
                ? KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_command_empty_name").getString()
                : command.displayName();
        lines.addAll(font.split(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_command_tooltip_name", name), 280));
        lines.addAll(font.split(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_command_tooltip_command", commandInputText(command.command())), 280));
        KineticOverlays.requestFormattedTooltip(lines, rawMouseX, rawMouseY);
    }

    @Override
    protected boolean canvasKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (commandSuggestions != null) {
            commandSuggestions.setAllowSuggestions(true);
            commandSuggestions.update();
            if (commandSuggestions.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }

    @Override
    protected boolean canvasMouseClicked(double mouseX, double mouseY, int button) {
        if (commandSuggestions != null && commandSuggestions.mouseClicked(mouseX, mouseY, button)) return true;
        gridScroll.update(totalGridRows(), visibleGridRows());
        if (KineticMouseButtons.isPrimary(button) && gridScroll.beginDrag(mouseX, mouseY, scrollbarX(), listY(), SCROLLBAR_WIDTH, listH(), MIN_THUMB_HEIGHT, 2)) {
            return true;
        }
        if (super.canvasMouseClicked(mouseX, mouseY, button)) return true;
        if (!GuiTheme.hovering(mouseX, mouseY, listX(), listY(), listW(), listH())) return false;
        int index = commandIndexAt((int) mouseX, (int) mouseY);
        if (index < 0 || index >= draft.commands.size()) return false;
        if (KineticMouseButtons.isPrimary(button)) {
            selectedIndex = index;
            loadSelectedIntoEditor();
            return true;
        }
        if (KineticMouseButtons.isSecondary(button)) {
            draft.commands.remove(index);
            if (selectedIndex == index) clearEditor();
            else if (selectedIndex > index) selectedIndex--;
            gridScroll.update(totalGridRows(), visibleGridRows());
            updateButtons();
            return true;
        }
        return false;
    }

    private int commandIndexAt(int mouseX, int mouseY) {
        int localX = mouseX - listX() - 4;
        int localY = mouseY - listY() - 4 + gridScroll.visualShift(GRID_CELL + GRID_GAP);
        if (localX < 0 || localY < 0) return -1;
        int col = localX / (GRID_CELL + GRID_GAP);
        int row = localY / (GRID_CELL + GRID_GAP);
        if (col >= gridColumns() || row >= visibleGridRows()) return -1;
        if (localX % (GRID_CELL + GRID_GAP) >= GRID_CELL || localY % (GRID_CELL + GRID_GAP) >= GRID_CELL) return -1;
        return (gridScroll.smoothIndexOffset() + row) * gridColumns() + col;
    }

    @Override
    protected boolean canvasMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (gridScroll.drag(mouseY, listY(), listH(), MIN_THUMB_HEIGHT)) return true;
        return super.canvasMouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected boolean canvasMouseReleased(double mouseX, double mouseY, int button) {
        if (gridScroll.release(button)) return true;
        return super.canvasMouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected boolean canvasMouseScrolled(double mouseX, double mouseY, double delta) {
        if (commandSuggestions != null && isControlFocused(commandBox) && commandSuggestions.mouseScrolled(Mth.clamp(delta, -1.0, 1.0))) {
            return true;
        }
        if (!GuiTheme.hovering(mouseX, mouseY, listX(), listY(), listW(), listH())) {
            return super.canvasMouseScrolled(mouseX, mouseY, delta);
        }
        gridScroll.update(totalGridRows(), visibleGridRows());
        return gridScroll.scroll(delta);
    }

    @Override
    protected boolean handleCloseRequest() {
        returnToParent();
        return true;
    }

    private void returnToParent() {
        navigateBack();
    }
}
