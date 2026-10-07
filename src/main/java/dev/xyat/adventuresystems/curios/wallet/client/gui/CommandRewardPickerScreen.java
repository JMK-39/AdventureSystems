package dev.xyat.adventuresystems.curios.wallet.client.gui;

import net.minecraft.network.chat.Component;

import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.widget.list.ItemGridDensity;
import dev.xyat.kineticcore.api.client.gui.widget.list.ItemGridItem;
import dev.xyat.kineticcore.api.client.gui.widget.list.ItemGridOutline;
import dev.xyat.kineticcore.api.client.gui.widget.list.KineticItemGrid;
import dev.xyat.adventuresystems.text.AdventureText;

import java.util.ArrayList;
import java.util.List;

final class CommandRewardPickerScreen extends KineticPage {
    private final ShopGuiSupport.EditorDraft draft;
    private final boolean gacha;
    private KineticItemGrid grid;
    private int scrollOffset;
    // The grid shows the rows its commands need (2 to 6, then it scrolls) and the panel sits in the middle of the canvas.
    private static final int GRID_COLUMNS = (492 - 2 * 6 + 6) / (26 + 6);
    private int top;
    private int gridHeight;

    CommandRewardPickerScreen(ShopGuiSupport.EditorDraft draft, boolean gacha) {
        super(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_pick_title"));
        this.draft = draft;
        this.gacha = gacha;
        useCanvas(560, 300, 6);
    }

    @Override
    protected void build(KineticUi ui) {
        if (grid != null) scrollOffset = grid.scrollOffset();
        int rows = Math.max(2, Math.min(6, (draft.commands.size() + GRID_COLUMNS - 1) / GRID_COLUMNS));
        gridHeight = 6 + rows * (26 + 6);
        top = Math.max(0, (height() - (56 + gridHeight + 14)) / 2);
        ui.button(444, top + 14, 76)
                .text(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_cancel"))
                .onClick(this::navigateBack).build();
        List<ItemGridItem> items = new ArrayList<>(draft.commands.size());
        for (ShopGuiSupport.CommandDraft command : draft.commands) {
            items.add(new ItemGridItem(ShopGuiSupport.stack(command.iconId()), null,
                    true, false, false, ItemGridOutline.NONE));
        }
        grid = ui.itemGrid(34, top + 56, 492, gridHeight, ItemGridDensity.LARGE, items)
                .scrollOffset(scrollOffset).onClick(this::selectCommand).build();
    }

    private void selectCommand(int index) {
        if (index < 0 || index >= draft.commands.size()) return;
        draft.rewards.add(draft.commands.get(index).toRewardDraft(gacha ? 10 : 1));
        navigateBack();
    }

    @Override
    protected void renderBackground(KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        KineticTheme.shadow(graphics, width(), height());
        KineticTheme.panel(graphics, 20, top, 520, 56 + gridHeight + 14);
        // Centred, and ending 4 px before the Cancel button on the right.
        graphics.scrollingTextCentered(title(), 280, top + 8, 2 * (444 - 4 - 280), KineticTheme.current().text(), false);
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_pick_hint"), 34, top + 38, 520 - 28, KineticTheme.current().text(), true);
        if (draft.commands.isEmpty()) {
            graphics.scrollingTextCentered(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_empty"), 280, top + 56 + (gridHeight - 8) / 2, 520 - 28, KineticTheme.current().text(), false);
        }
    }

    @Override
    protected void renderTooltips(int mouseX, int mouseY) {
        if (grid == null) return;
        int index = grid.itemAt(mouseX, mouseY);
        if (index < 0 || index >= draft.commands.size()) return;
        ShopGuiSupport.CommandDraft command = draft.commands.get(index);
        Component name = command.displayName() == null || command.displayName().isBlank()
                ? AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_empty_name")
                : AdventureText.literal(command.displayName());
        showTooltip(List.of(
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_name", name),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_text_tooltip",
                        command.command() == null ? "" : command.command()),
                AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_pick_tooltip_hint")
        ), 280);
    }
}
