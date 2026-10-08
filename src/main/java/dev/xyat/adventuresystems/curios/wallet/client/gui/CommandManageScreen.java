package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.kineticcore.api.client.gui.command.KineticCommandAssist;
import dev.xyat.kineticcore.api.client.gui.input.KeyInput;
import dev.xyat.kineticcore.api.client.gui.input.MouseInput;
import dev.xyat.kineticcore.api.client.gui.input.ScrollInput;
import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.selector.KineticSelectors;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.widget.KineticButton;
import dev.xyat.kineticcore.api.client.gui.widget.KineticTextField;
import dev.xyat.kineticcore.api.client.gui.widget.list.ItemGridDensity;
import dev.xyat.kineticcore.api.client.gui.widget.list.ItemGridItem;
import dev.xyat.kineticcore.api.client.gui.widget.list.ItemGridOutline;
import dev.xyat.kineticcore.api.client.gui.widget.list.KineticItemGrid;
import dev.xyat.kineticcore.api.client.gui.overlay.KineticOverlays;
import dev.xyat.adventuresystems.text.AdventureText;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class CommandManageScreen extends KineticPage {
    // 图标格：只显示物品格，点击格子打开物品选择器更换图标。
    private static final int ICON_X = 76;
    private static final int ICON_Y = 40;
    private static final int ICON_SIZE = 20;
    // “添加/更新”“新建”和右上角“完成”三个按钮同宽，右缘对齐。
    private static final int ACTION_W = 56;
    private static final int NEW_X = 622 - ACTION_W;
    private static final int SAVE_X = NEW_X - 4 - ACTION_W;
    private final ShopGuiSupport.EditorDraft draft;
    private int selectedIndex = -1;
    private String iconId = "";
    private String nameText = "";
    private String commandText = "";
    private int gridScroll;
    private KineticTextField nameBox;
    private KineticTextField commandBox;
    private KineticButton saveButton;
    private KineticItemGrid grid;
    private KineticCommandAssist assist;

    CommandManageScreen(ShopGuiSupport.EditorDraft draft) {
        super(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_manage_title"));
        this.draft = draft;
        useCanvas(640, 360, 6);
    }

    @Override
    protected void build(KineticUi ui) {
        if (nameBox != null) nameText = nameBox.textValue();
        if (commandBox != null) commandText = commandBox.textValue();
        if (grid != null) gridScroll = grid.scrollOffset();
        if (iconId.isBlank()) iconId = fallbackIconId();
        nameBox = ui.textField(188, 41, 306)
                .label(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_name"))
                .placeholder(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_name"))
                .tooltip(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_name_tooltip"))
                .maxLength(64).value(nameText).onChange(value -> nameText = value).firstShownTextAsDefault().build();
        saveButton = ui.button(SAVE_X, 40, ACTION_W).text(saveButtonText()).onClick(this::saveCommand).build();
        ui.button(NEW_X, 40, ACTION_W).text(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_new"))
                .tooltip(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_clear_tooltip"))
                .onClick(this::clearEditor).build();
        ui.button(NEW_X, 8, ACTION_W).text(AdventureText.translatable("gui.done"))
                .tooltip(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_done_tooltip"))
                .onClick(this::navigateBack).build();
        commandBox = ui.textField(84, 332, 536)
                .label(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_text"))
                .placeholder(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_text"))
                .maxLength(2048).value(commandText).firstShownTextAsDefault().build();
        assist = KineticCommandAssist.attach(commandBox, width(), height(), false, 8,
                value -> commandText = value);
        grid = ui.itemGrid(16, 78, 608, 238, ItemGridDensity.STANDARD, gridItems())
                .scrollOffset(gridScroll).onClick(this::selectCommand).build();
    }

    private String fallbackIconId() {
        return draft.itemId == null || draft.itemId.isBlank() ? "minecraft:barrier" : draft.itemId;
    }

    private Component saveButtonText() {
        return AdventureText.translatable(selectedIndex >= 0
                ? "gui.adventuresystems.curios.wallet.shop_command_update"
                : "gui.adventuresystems.curios.wallet.shop_command_add");
    }

    private void openIconSelector() {
        KineticSelectors.openItemSelector(selection -> {
            if (!selection.isItem()) return;
            String spec = ShopGuiSupport.selectedStackSpec(selection.stack());
            if (!spec.isBlank()) iconId = spec;
        });
    }

    private void saveCommand() {
        String command = commandBox == null ? "" : commandBox.textValue().trim();
        String name = nameBox == null ? "" : nameBox.textValue().trim();
        if (name.isBlank()) {
            toast("wallet_command_name_empty", "msg.adventuresystems.curios.wallet.shop_command_name_empty");
            return;
        }
        if (command.isBlank()) {
            toast("wallet_command_empty", "msg.adventuresystems.curios.wallet.shop_command_empty");
            return;
        }
        if (!command.startsWith("/")) {
            toast("wallet_command_slash_required", "msg.adventuresystems.curios.wallet.shop_command_slash_required");
            focus(commandBox);
            return;
        }
        for (int i = 0; i < draft.commands.size(); i++) {
            if (i == selectedIndex) continue;
            ShopGuiSupport.CommandDraft existing = draft.commands.get(i);
            if (existing.displayName() != null && existing.displayName().trim().equalsIgnoreCase(name)) {
                toast("wallet_command_name_duplicate", "msg.adventuresystems.curios.wallet.shop_command_name_duplicate");
                return;
            }
            if (commandCompareKey(existing.command()).equals(commandCompareKey(command))) {
                toast("wallet_command_duplicate", "msg.adventuresystems.curios.wallet.shop_command_duplicate");
                return;
            }
        }
        String icon = iconId == null || iconId.isBlank() ? fallbackIconId() : iconId;
        ShopGuiSupport.CommandDraft value = new ShopGuiSupport.CommandDraft(icon, name, command);
        if (selectedIndex >= 0 && selectedIndex < draft.commands.size()) draft.commands.set(selectedIndex, value);
        else draft.commands.add(value);
        clearEditor();
        refreshGrid();
    }

    private static void toast(String id, String key) {
        KineticOverlays.toast(id, AdventureText.translatable(key), KineticOverlays.Position.BOTTOM_CENTER, 2200, 0, -30);
    }

    private void clearEditor() {
        selectedIndex = -1;
        iconId = fallbackIconId();
        nameText = "";
        commandText = "";
        if (nameBox != null) nameBox.setTextValue("");
        if (commandBox != null) commandBox.setTextValue("");
        if (saveButton != null) saveButton.setText(saveButtonText());
        refreshGrid();
    }

    private void selectCommand(int index) {
        if (index < 0 || index >= draft.commands.size()) return;
        selectedIndex = index;
        ShopGuiSupport.CommandDraft command = draft.commands.get(index);
        iconId = command.iconId() == null || command.iconId().isBlank() ? fallbackIconId() : command.iconId();
        nameText = command.displayName() == null ? "" : command.displayName();
        commandText = commandInputText(command.command());
        nameBox.setTextValue(nameText);
        commandBox.setTextValue(commandText);
        focus(commandBox);
        saveButton.setText(saveButtonText());
        refreshGrid();
    }

    private static String commandInputText(String value) {
        if (value == null || value.isBlank()) return "";
        String trimmed = value.trim();
        return trimmed.startsWith("/") ? trimmed : "/" + trimmed;
    }

    private static String commandCompareKey(String value) {
        if (value == null) return "";
        String trimmed = value.trim();
        while (trimmed.startsWith("/")) trimmed = trimmed.substring(1).trim();
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private List<ItemGridItem> gridItems() {
        List<ItemGridItem> items = new ArrayList<>(draft.commands.size());
        for (int i = 0; i < draft.commands.size(); i++) {
            ShopGuiSupport.CommandDraft command = draft.commands.get(i);
            items.add(new ItemGridItem(ShopGuiSupport.stack(command.iconId()), null,
                    true, i == selectedIndex, false, ItemGridOutline.NONE));
        }
        return items;
    }

    private void refreshGrid() {
        if (grid != null) grid.setItems(gridItems());
    }

    @Override
    protected void renderBackground(KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        KineticTheme.shadow(graphics, width(), height());
        KineticTheme.panel(graphics, 0, 0, 640, 360);
        graphics.scrollingTextCentered(title(), 320, 10, Math.max(0, (NEW_X - 4 - 320) * 2), KineticTheme.current().text(), false);
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_icon_preview"), 18, 46, ICON_X - 18 - 4, KineticTheme.current().text(), true);
        boolean iconHovered = KineticTheme.hovering(mouseX, mouseY, ICON_X, ICON_Y, ICON_SIZE, ICON_SIZE);
        KineticTheme.itemSlot(graphics, ICON_X, ICON_Y, ICON_SIZE, ICON_SIZE, 4, false, iconHovered, false);
        KineticTheme.item(graphics, ShopGuiSupport.stack(iconId), ICON_X, ICON_Y, ICON_SIZE, 0.875F, false);
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_name"), 120, 46, 188 - 120 - 4, KineticTheme.current().text(), true);
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_list"), 16, 64, 608, KineticTheme.current().text(), true);
        if (draft.commands.isEmpty()) {
            graphics.scrollingTextCentered(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_empty"), 320, 196, 608, KineticTheme.current().text(), false);
        }
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_text"), 18, 337, 84 - 18 - 4, KineticTheme.current().text(), true);
    }

    @Override
    protected void renderForeground(KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (assist != null) assist.render(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltips(int mouseX, int mouseY) {
        if (KineticTheme.hovering(mouseX, mouseY, ICON_X, ICON_Y, ICON_SIZE, ICON_SIZE)) {
            showTooltip(List.of(ShopGuiSupport.stack(iconId).getHoverName(),
                    AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_icon_tooltip")));
            return;
        }
        if (grid == null) return;
        int index = grid.itemAt(mouseX, mouseY);
        if (index < 0 || index >= draft.commands.size()) return;
        ShopGuiSupport.CommandDraft command = draft.commands.get(index);
        Component name = command.displayName() == null || command.displayName().isBlank()
                ? AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_empty_name")
                : AdventureText.literal(command.displayName());
        showTooltip(List.of(
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_tooltip_name", name),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_tooltip_command",
                        commandInputText(command.command()))), 280);
    }

    @Override
    protected boolean onKeyPress(KeyInput input) {
        return assist != null && assist.keyPress(input);
    }

    @Override
    protected boolean onMouseClickCapture(MouseInput input) {
        return assist != null && assist.mouseClick(input);
    }

    @Override
    protected boolean onMouseClick(MouseInput input) {
        if (input.isLeft() && input.inside(ICON_X, ICON_Y, ICON_SIZE, ICON_SIZE)) {
            openIconSelector();
            return true;
        }
        if (!input.isRight() || grid == null) return false;
        int index = grid.itemAt(input.x(), input.y());
        if (index < 0 || index >= draft.commands.size()) return false;
        draft.commands.remove(index);
        if (selectedIndex == index) clearEditor();
        else if (selectedIndex > index) selectedIndex--;
        refreshGrid();
        return true;
    }

    @Override
    protected boolean onMouseScroll(ScrollInput input) {
        return assist != null && assist.mouseScroll(input);
    }
}
