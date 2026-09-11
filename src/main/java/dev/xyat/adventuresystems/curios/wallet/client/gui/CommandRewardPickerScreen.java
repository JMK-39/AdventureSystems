package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.kineticcore.api.client.overlay.GuiOverlay;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.screen.KineticScreen;
import dev.xyat.kineticcore.api.client.widget.KineticWidgets.GridScrollController;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
final class CommandRewardPickerScreen extends KineticScreen {
    private static final int PANEL_WIDTH = 520;
    private static final int PANEL_HEIGHT = 280;
    private static final int CELL_SIZE = 26;
    private static final int CELL_GAP = 6;
    private static final int CELL_STEP = CELL_SIZE + CELL_GAP;
    private static final int GRID_PADDING = 6;
    private static final int SCROLLBAR_WIDTH = 4;

    private final Screen parent;
    private final ShopGuiSupport.EditorDraft draft;
    private final boolean gacha;
    private int left;
    private int top;
    private final GridScrollController gridScroll = new GridScrollController();

    CommandRewardPickerScreen(Screen parent, ShopGuiSupport.EditorDraft draft, boolean gacha) {
        super(Component.translatable("gui.adventuresystems.curios.wallet.shop_command_pick_title"));
        this.parent = parent;
        this.draft = draft;
        this.gacha = gacha;
        useCanvas(
                560f,
                300f,
                6
        );
    }

    @Override
    protected void buildUi() {
        left = (canvasWidth - PANEL_WIDTH) / 2;
        top = (canvasHeight - PANEL_HEIGHT) / 2;
        gridScroll.update(totalRows(), visibleRows());
        int buttonW = 76;
        addRenderableWidget(Button.builder(Component.translatable("gui.adventuresystems.curios.wallet.shop_cancel"), b -> Minecraft.getInstance().setScreen(parent))
                .bounds(left + PANEL_WIDTH - buttonW - 14, top + 14, buttonW, 20)
                .build());
    }

    @Override
    protected void renderCanvasBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ShopGuiSupport.renderBox(graphics, left, top, PANEL_WIDTH, PANEL_HEIGHT, ShopGuiSupport.PANEL_BG);
        graphics.drawCenteredString(font, title, left + PANEL_WIDTH / 2, top + 8, ShopGuiSupport.GOLD);
        graphics.drawString(font, Component.translatable("gui.adventuresystems.curios.wallet.shop_command_pick_hint"), left + 14, top + 34, ShopGuiSupport.CYAN, true);

        graphics.renderOutline(listX(), listY(), listW(), listH(), ShopGuiSupport.CYAN_DARK);
        if (draft.commands.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("gui.adventuresystems.curios.wallet.shop_command_empty"), left + PANEL_WIDTH / 2, listY() + listH() / 2 - 4, ShopGuiSupport.TEXT_GRAY);
            return;
        }

        renderGrid(graphics, mouseX, mouseY);
        renderScrollbar(graphics, mouseX, mouseY);
    }

    private void renderGrid(GuiGraphics graphics, int mouseX, int mouseY) {
        int cols = columns();
        int rows = visibleRows();
        gridScroll.update(totalRows(), rows);
        int firstRow = gridScroll.smoothIndexOffset();
        int shift = gridScroll.visualShift(CELL_STEP);
        int start = firstRow * cols;
        int end = Math.min(draft.commands.size(), start + cols * (rows + 1));
                enableCanvasScissor(graphics, listX(), listY(), listX() + listW(), listY() + listH());
        try {
for (int i = start; i < end; i++) {
            int local = i - start;
            int x = cellX(local % cols);
            int y = cellY(local / cols) - shift;
            boolean hover = GuiTheme.hovering(mouseX, mouseY, x, y, CELL_SIZE, CELL_SIZE);
            ItemStack icon = ShopGuiSupport.stack(draft.commands.get(i).iconId());
            GuiTheme.itemSlot(graphics, icon, x, y, CELL_SIZE, 4, hover);
            graphics.renderItem(icon, x + 5, y + 5);
        }
        } finally {
            graphics.disableScissor();
        }
    }

    private void renderScrollbar(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        gridScroll.update(
                totalRows(),
                visibleRows()
        );

        gridScroll.render(
                graphics,
                mouseX,
                mouseY,
                scrollbarX(),
                listY(),
                SCROLLBAR_WIDTH,
                listH(),
                18,
                ShopGuiSupport.SCROLLBAR_TRACK,
                ShopGuiSupport.SCROLLBAR_THUMB,
                ShopGuiSupport.SCROLLBAR_HOVER
        );
    }

    @Override
    protected void renderTooltips(GuiGraphics graphics, int scaledMouseX, int scaledMouseY, int rawMouseX, int rawMouseY) {
        int index = indexAt(scaledMouseX, scaledMouseY);
        if (index < 0 || index >= draft.commands.size()) return;
        ShopGuiSupport.CommandDraft command = draft.commands.get(index);
        String name = command.displayName() == null || command.displayName().isBlank()
                ? Component.translatable("gui.adventuresystems.curios.wallet.shop_command_empty_name").getString()
                : command.displayName();
        List<FormattedCharSequence> lines = new ArrayList<>();
        lines.addAll(font.split(Component.literal(name).withStyle(ChatFormatting.GOLD), 280));
        lines.addAll(font.split(Component.literal(command.command() == null ? "" : command.command()).withStyle(ChatFormatting.GRAY), 280));
        lines.addAll(font.split(Component.translatable("gui.adventuresystems.curios.wallet.shop_command_pick_hint").withStyle(ChatFormatting.YELLOW), 280));
        GuiOverlay.requestFormattedTooltip(lines, rawMouseX, rawMouseY);
    }

    @Override
    protected boolean canvasMouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (super.canvasMouseClicked(mouseX, mouseY, button)) return true;

        gridScroll.update(
                totalRows(),
                visibleRows()
        );

        if (button == 0
                && gridScroll.beginDrag(
                        mouseX,
                        mouseY,
                        scrollbarX(),
                        listY(),
                        SCROLLBAR_WIDTH,
                        listH(),
                        18,
                        2
                )) {
            return true;
        }

        if (button != 0) return false;

        int index = indexAt((int) mouseX, (int) mouseY);
        if (index < 0 || index >= draft.commands.size()) return false;

        draft.rewards.add(
                draft.commands.get(index)
                        .toRewardDraft(gacha ? 10 : 1)
        );

        Minecraft.getInstance().setScreen(parent);
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
        if (gridScroll.drag(
                mouseY,
                listY(),
                listH(),
                18
        )) {
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
        if (gridScroll.release(button)) return true;
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

        gridScroll.update(
                totalRows(),
                visibleRows()
        );

        return gridScroll.scroll(delta);
    }

    private int indexAt(int mouseX, int mouseY) {
        if (!GuiTheme.hovering(mouseX, mouseY, listX(), listY(), listW(), listH())) return -1;
        int relativeX = mouseX - gridX();
        int relativeY = mouseY - gridY() + gridScroll.visualShift(CELL_STEP);
        if (relativeX < 0 || relativeY < 0) return -1;
        int col = relativeX / CELL_STEP;
        int row = relativeY / CELL_STEP;
        if (col >= columns() || row >= visibleRows()) return -1;
        int cellLocalX = relativeX - col * CELL_STEP;
        int cellLocalY = relativeY - row * CELL_STEP;
        if (cellLocalX >= CELL_SIZE || cellLocalY >= CELL_SIZE) return -1;
        return (gridScroll.smoothIndexOffset() + row) * columns() + col;
    }


    private int listX() { return left + 14; }
    private int listY() { return top + 56; }
    private int listW() { return PANEL_WIDTH - 28; }
    private int listH() { return PANEL_HEIGHT - 70; }
    private int gridX() { return listX() + GRID_PADDING; }
    private int gridY() { return listY() + GRID_PADDING; }
    private int gridW() { return listW() - GRID_PADDING * 2 - SCROLLBAR_WIDTH - 6; }
    private int columns() { return Math.max(1, gridW() / CELL_STEP); }
    private int visibleRows() { return Math.max(1, (listH() - GRID_PADDING * 2) / CELL_STEP); }
    private int totalRows() { return (draft.commands.size() + columns() - 1) / columns(); }
    private int scrollbarX() { return listX() + listW() - SCROLLBAR_WIDTH - 4; }
    private int cellX(int col) { return gridX() + col * CELL_STEP; }
    private int cellY(int row) { return gridY() + row * CELL_STEP; }
}

