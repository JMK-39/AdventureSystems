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

    CommandRewardPickerScreen(ShopGuiSupport.EditorDraft draft, boolean gacha) {
        super(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_pick_title"));
        this.draft = draft;
        this.gacha = gacha;
        useCanvas(560, 300, 6);
    }

    @Override
    protected void build(KineticUi ui) {
        if (grid != null) scrollOffset = grid.scrollOffset();
        ui.button(444, 24, 76)
                .text(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_cancel"))
                .onClick(this::navigateBack).build();
        List<ItemGridItem> items = new ArrayList<>(draft.commands.size());
        for (ShopGuiSupport.CommandDraft command : draft.commands) {
            items.add(new ItemGridItem(ShopGuiSupport.stack(command.iconId()), null,
                    true, false, false, ItemGridOutline.NONE));
        }
        grid = ui.itemGrid(34, 66, 492, 200, ItemGridDensity.LARGE, items)
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
        KineticTheme.panel(graphics, 20, 10, 520, 280);
        graphics.centeredText(title(), 280, 18, KineticTheme.current().text(), false);
        graphics.text(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_pick_hint"),
                34, 44, KineticTheme.current().text(), true);
        if (draft.commands.isEmpty()) {
            graphics.centeredText(AdventureText.translatable("gui.adventuresystems.curios.wallet.shop_command_empty"),
                    280, 160, KineticTheme.current().text(), false);
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
