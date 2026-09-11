package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.kineticcore.api.client.overlay.GuiOverlay;
import dev.xyat.adventuresystems.curios.util.ColorText;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.screen.KineticScreen;
import dev.xyat.kineticcore.api.client.widget.KineticWidgets.GridScrollController;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
final class QuestManageScreen extends KineticScreen {
    private static final int PANEL_WIDTH = 440;
    private static final int PANEL_HEIGHT = 296;
    private static final int ROW_HEIGHT = 24;
    private static final int VISIBLE_ROWS = 8;
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int MIN_THUMB_HEIGHT = 16;
    private final ShopEntryEditorScreen parent;
    private final ShopGuiSupport.EditorDraft draft;
    private final Map<Long, ShopGuiSupport.QuestDisplay> questDisplays = ShopGuiSupport.loadQuestDisplays();
    private int left;
    private int top;
    private final GridScrollController listScroll = new GridScrollController();

    QuestManageScreen(ShopEntryEditorScreen parent, ShopGuiSupport.EditorDraft draft) {
        super(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_title"));
        this.parent = parent;
        this.draft = draft;
        useCanvas(
                480f,
                320f,
                6
        );
    }

    @Override
    protected void buildUi() {
        left = (canvasWidth - PANEL_WIDTH) / 2;
        top = (canvasHeight - PANEL_HEIGHT) / 2;
        addRenderableWidget(Button.builder(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_add"), b -> Minecraft.getInstance().setScreen(new QuestPickerScreen(this, id -> {
            if (id > 0 && !draft.questIds.contains(id)) draft.questIds.add(id);
            listScroll.update(draft.questIds.size(), VISIBLE_ROWS);
        }))).bounds(left + 18, top + 264, 116, 20).build());
        addRenderableWidget(Button.builder(ColorText.translatable("gui.done"), b -> Minecraft.getInstance().setScreen(parent)).bounds(left + PANEL_WIDTH - 98, top + 264, 80, 20).build());
    }

    @Override
    protected void renderCanvasBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ShopGuiSupport.renderBox(graphics, left, top, PANEL_WIDTH, PANEL_HEIGHT, ShopGuiSupport.PANEL_BG);
        graphics.drawCenteredString(font, title, left + PANEL_WIDTH / 2, top + 8, ShopGuiSupport.GOLD);
        graphics.drawString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_hint_top"), left + 18, top + 31, ShopGuiSupport.CYAN, true);

        int listX = listX();
        int listY = listY();
        int listW = listW();
        int listH = listH();
        graphics.fill(listX, listY, listX + listW, listY + listH, 0xAA000000);

        if (draft.questIds.isEmpty()) {
            graphics.drawCenteredString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_empty"), left + PANEL_WIDTH / 2, listY + 84, ShopGuiSupport.RED);
            return;
        }

        listScroll.update(draft.questIds.size(), VISIBLE_ROWS);
        int start = listScroll.smoothIndexOffset();
        int end = Math.min(draft.questIds.size(), start + VISIBLE_ROWS + 1);
                enableCanvasScissor(graphics, listX, listY, listX + listW, listY + listH);
        try {
for (int i = start; i < end; i++) {
            renderQuestRow(graphics, mouseX, mouseY, i, listX, listY, listW);
        }
        } finally {
            graphics.disableScissor();
        }
        renderScrollbar(graphics, mouseX, mouseY);
    }

    private void renderQuestRow(GuiGraphics graphics, int mouseX, int mouseY, int index, int listX, int listY, int listW) {
        int row = index - listScroll.smoothIndexOffset();
        int y = listY + row * ROW_HEIGHT - listScroll.visualShift(ROW_HEIGHT);
        int contentW = listW - scrollbarReserve();
        long questId = draft.questIds.get(index);
        ShopGuiSupport.QuestDisplay display = questDisplays.get(questId);
        boolean hover = GuiTheme.hovering(mouseX, mouseY, listX, y, contentW, ROW_HEIGHT);
        graphics.fill(listX + 1, y + 1, listX + contentW - 1, y + ROW_HEIGHT - 1, hover ? ShopGuiSupport.ROW_HOVER : ShopGuiSupport.ROW_BG);
        graphics.drawString(font, GuiTheme.trim(font, display == null ? String.valueOf(questId) : display.displayTitle(), 245), listX + 8, y + 4, ShopGuiSupport.TEXT_WHITE, true);
        String meta = display == null ? ColorText.translatable("gui.adventuresystems.curios.wallet.shop_quest_id_scaled", questId).getString() : display.displayMeta();
        ShopGuiSupport.drawScaledString(graphics, font, GuiTheme.trim(font, meta, 170), listX + 8, y + 15, ShopGuiSupport.TEXT_GRAY);
        boolean delHover = GuiTheme.hovering(mouseX, mouseY, removeX(), y + 3, 46, 18);
        graphics.fill(removeX(), y + 3, removeX() + 46, y + 21, delHover ? 0xAA993333 : 0x88553333);
        graphics.drawCenteredString(font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_remove"), removeX() + 23, y + 8, ShopGuiSupport.TEXT_WHITE);
    }

    private void renderScrollbar(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        listScroll.update(
                draft.questIds.size(),
                VISIBLE_ROWS
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
        List<Component> tooltip = questTooltipAt(scaledMouseX, scaledMouseY);
        if (!tooltip.isEmpty()) GuiOverlay.requestTooltip(tooltip, rawMouseX, rawMouseY);
    }

    private List<Component> questTooltipAt(int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (GuiTheme.hovering(mouseX, mouseY, scrollbarX(), listY(), SCROLLBAR_WIDTH, listH()) && scrollbarVisible()) {
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_scrollbar_tip").withStyle(ChatFormatting.GOLD));
            return tooltip;
        }
        if (!GuiTheme.hovering(mouseX, mouseY, listX(), listY(), listW() - scrollbarReserve(), listH())) return tooltip;
        int shift = listScroll.visualShift(ROW_HEIGHT);
        int row = (mouseY - listY() + shift) / ROW_HEIGHT;
        int index = listScroll.smoothIndexOffset() + row;
        if (index < 0 || index >= draft.questIds.size()) return tooltip;
        long questId = draft.questIds.get(index);
        ShopGuiSupport.QuestDisplay display = questDisplays.get(questId);
        tooltip.add(Component.literal(display == null ? String.valueOf(questId) : display.displayTitle()).withStyle(ChatFormatting.GOLD));
        tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_quest_id_scaled", questId).withStyle(ChatFormatting.AQUA));
        if (display != null && !display.displayMeta().isBlank()) tooltip.add(Component.literal(display.displayMeta()));
        if (GuiTheme.hovering(mouseX, mouseY, removeX(), listY() + row * ROW_HEIGHT - listScroll.visualShift(ROW_HEIGHT) + 3, 46, 18)) tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_remove_tip").withStyle(ChatFormatting.RED));
        return tooltip;
    }

    @Override
    protected boolean canvasMouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (super.canvasMouseClicked(mouseX, mouseY, button)) return true;
        if (button != 0) return false;

        listScroll.update(
                draft.questIds.size(),
                VISIBLE_ROWS
        );

        if (listScroll.beginDrag(
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

        if (!GuiTheme.hovering(
                mouseX,
                mouseY,
                listX(),
                listY(),
                listW() - scrollbarReserve(),
                listH()
        )) {
            return false;
        }

        int shift = listScroll.visualShift(ROW_HEIGHT);
        int row = (int) ((mouseY - listY() + shift) / ROW_HEIGHT);
        int index = listScroll.smoothIndexOffset() + row;

        if (index >= 0
                && index < draft.questIds.size()
                && GuiTheme.hovering(
                        mouseX,
                        mouseY,
                        removeX(),
                        listY() + row * ROW_HEIGHT - shift + 3,
                        46,
                        18
                )) {
            draft.questIds.remove(index);
            listScroll.update(
                    draft.questIds.size(),
                    VISIBLE_ROWS
            );
        }

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

        return super.canvasMouseReleased(
                mouseX,
                mouseY,
                button
        );
    }

    @Override
    protected boolean canvasMouseScrolled(
            double mouseX,
            double mouseY,
            double delta
    ) {
        listScroll.update(
                draft.questIds.size(),
                VISIBLE_ROWS
        );

        return listScroll.scroll(delta)
                || super.canvasMouseScrolled(
                        mouseX,
                        mouseY,
                        delta
                );
    }

    private int listX() { return left + 12; }
    private int listY() { return top + 58; }
    private int listW() { return PANEL_WIDTH - 24; }
    private int listH() { return ROW_HEIGHT * VISIBLE_ROWS; }
    private int removeX() { return listX() + listW() - scrollbarReserve() - 54; }
    private int scrollbarX() { return listX() + listW() - SCROLLBAR_WIDTH - 3; }
    private int scrollbarReserve() { return listScroll.canScroll() ? 16 : 0; }
    private boolean scrollbarVisible() { return listScroll.canScroll(); }
}

