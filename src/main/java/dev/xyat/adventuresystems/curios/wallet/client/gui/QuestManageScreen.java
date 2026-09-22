package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.kineticcore.api.client.input.KineticMouseButtons;
import dev.xyat.kineticcore.api.client.overlay.KineticOverlays;
import dev.xyat.kineticcore.api.client.screen.KineticScreen;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.widget.button.KineticButtons.StateButton;
import dev.xyat.kineticcore.api.client.widget.scroll.KineticScroll.GridScrollController;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import dev.xyat.kineticcore.api.text.KineticI18n;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import dev.xyat.kineticcore.api.client.widget.KineticControl;

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
    private final GridScrollController listScroll = new GridScrollController();
    private final List<StateButton> removeButtons = new ArrayList<>();
    private int left;
    private int top;

    QuestManageScreen(ShopEntryEditorScreen parent, ShopGuiSupport.EditorDraft draft) {
        super(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_title"));
        this.parent = parent;
        setParentScreen(parent);
        this.draft = draft;
        useCanvas(480f, 320f, 6);
    }

    @Override
    protected void buildUi() {
        left = (canvasWidth() - PANEL_WIDTH) / 2;
        top = (canvasHeight() - PANEL_HEIGHT) / 2;

        addButton(
                left + 18,
                top + 264,
                116,
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_add"),
                null,
                () -> KineticClientRuntime.openScreen(new QuestPickerScreen(this, id -> {
                    if (id > 0 && !draft.questIds.contains(id)) draft.questIds.add(id);
                    listScroll.update(draft.questIds.size(), VISIBLE_ROWS);
                }))
        );
        addButton(
                left + PANEL_WIDTH - 98,
                top + 264,
                80,
                KineticI18n.translatable("gui.done"),
                null,
                this::returnToParent
        );

        removeButtons.clear();
        for (int i = 0; i < draft.questIds.size(); i++) {
            final int index = i;
            StateButton button = addCompactButton(
                    0,
                    0,
                    46,
                    KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_remove"),
                    KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_remove_tip"),
                    () -> removeQuest(index)
            );
            setControlVisible(button, false);
            setControlEnabled(button, false);
            removeButtons.add(button);
        }
        updateRemoveButtons();
    }

    @Override
    protected void renderCanvasBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        GuiTheme.panel(graphics, left, top, PANEL_WIDTH, PANEL_HEIGHT);
        graphics.drawCenteredString(font, title, left + PANEL_WIDTH / 2, top + 8, GuiTheme.current().text());
        graphics.drawString(
                font,
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_hint_top"),
                left + 18,
                top + 31,
                GuiTheme.current().mutedText(),
                true
        );

        int listX = listX();
        int listY = listY();
        int listW = listW();
        int listH = listH();
        GuiTheme.panelAlt(graphics, listX, listY, listW, listH);

        if (draft.questIds.isEmpty()) {
            graphics.drawCenteredString(
                    font,
                    KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_quest_manage_empty"),
                    left + PANEL_WIDTH / 2,
                    listY + 84,
                    GuiTheme.current().mutedText()
            );
            updateRemoveButtons();
            return;
        }

        listScroll.update(draft.questIds.size(), VISIBLE_ROWS);
        updateRemoveButtons();
        int start = listScroll.smoothIndexOffset();
        int end = Math.min(draft.questIds.size(), start + VISIBLE_ROWS + 1);
        enableCanvasScissor(graphics, listX, listY, listX + listW, listY + listH);
        try {
            for (int i = start; i < end; i++) {
                renderQuestRow(graphics, mouseX, mouseY, i, listX, listY, listW);
            }
        } finally {
            disableCanvasScissor(graphics);
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
        GuiTheme.stateSurface(
                graphics,
                listX + 1,
                y + 1,
                contentW - 2,
                ROW_HEIGHT - 2,
                GuiTheme.Surface.PANEL_ALT,
                false,
                hover,
                false
        );
        graphics.drawString(
                font,
                Component.literal(GuiTheme.trim(font, display == null ? String.valueOf(questId) : display.displayTitle(), 245)),
                listX + 8,
                y + 4,
                GuiTheme.current().text(),
                true
        );
        String meta = display == null
                ? KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_quest_id_scaled", questId).getString()
                : display.displayMeta();
        ShopGuiSupport.drawScaledString(
                graphics,
                font,
                GuiTheme.trim(font, meta, 170),
                listX + 8,
                y + 15,
                GuiTheme.current().mutedText()
        );
    }

    private void updateRemoveButtons() {
        listScroll.update(draft.questIds.size(), VISIBLE_ROWS);
        int first = listScroll.smoothIndexOffset();
        int shift = listScroll.visualShift(ROW_HEIGHT);
        for (int i = 0; i < removeButtons.size(); i++) {
            StateButton button = removeButtons.get(i);
            int y = listY() + (i - first) * ROW_HEIGHT - shift + 3;
            boolean visible = i >= first
                    && i <= first + VISIBLE_ROWS
                    && y >= listY()
                    && y + 16 <= listY() + listH();
            button.setX(removeX());
            button.setY(y);
            setControlVisible(button, visible);
            setControlEnabled(button, visible);
        }
    }

    private void removeQuest(int index) {
        if (index < 0 || index >= draft.questIds.size()) return;
        draft.questIds.remove(index);
        listScroll.update(draft.questIds.size(), VISIBLE_ROWS);
        rebuildUi();
    }

    private void renderScrollbar(GuiGraphics graphics, int mouseX, int mouseY) {
        listScroll.update(draft.questIds.size(), VISIBLE_ROWS);
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
        List<Component> tooltip = questTooltipAt(scaledMouseX, scaledMouseY);
        if (!tooltip.isEmpty()) KineticOverlays.requestTooltip(tooltip, rawMouseX, rawMouseY);
    }

    private List<Component> questTooltipAt(int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (GuiTheme.hovering(mouseX, mouseY, scrollbarX(), listY(), SCROLLBAR_WIDTH, listH()) && scrollbarVisible()) {
            tooltip.add(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_scrollbar_tip"));
            return tooltip;
        }
        if (!GuiTheme.hovering(mouseX, mouseY, listX(), listY(), listW() - scrollbarReserve(), listH())) return tooltip;
        int shift = listScroll.visualShift(ROW_HEIGHT);
        int row = (mouseY - listY() + shift) / ROW_HEIGHT;
        int index = listScroll.smoothIndexOffset() + row;
        if (index < 0 || index >= draft.questIds.size()) return tooltip;
        long questId = draft.questIds.get(index);
        ShopGuiSupport.QuestDisplay display = questDisplays.get(questId);
        tooltip.add(KineticI18n.translatable(
                "gui.adventuresystems.curios.wallet.shop_quest_title_value",
                display == null ? String.valueOf(questId) : display.displayTitle()
        ));
        tooltip.add(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_quest_id_scaled", questId));
        if (display != null && !display.displayMeta().isBlank()) tooltip.add(Component.literal(display.displayMeta()));
        return tooltip;
    }

    @Override
    protected boolean canvasMouseClicked(double mouseX, double mouseY, int button) {
        listScroll.update(draft.questIds.size(), VISIBLE_ROWS);
        if (KineticMouseButtons.isPrimary(button) && listScroll.beginDrag(
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
        return super.canvasMouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected boolean canvasMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (listScroll.drag(mouseY, listY(), listH(), MIN_THUMB_HEIGHT)) {
            updateRemoveButtons();
            return true;
        }
        return super.canvasMouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected boolean canvasMouseReleased(double mouseX, double mouseY, int button) {
        if (listScroll.release(button)) return true;
        return super.canvasMouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected boolean canvasMouseScrolled(double mouseX, double mouseY, double delta) {
        listScroll.update(draft.questIds.size(), VISIBLE_ROWS);
        if (listScroll.scroll(delta)) {
            updateRemoveButtons();
            return true;
        }
        return super.canvasMouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    protected boolean handleCloseRequest() {
        returnToParent();
        return true;
    }

    private void returnToParent() {
        navigateBack();
    }

    private int listX() { return left + 12; }
    private int listY() { return top + 58; }
    private int listW() { return PANEL_WIDTH - 24; }
    private int listH() { return ROW_HEIGHT * VISIBLE_ROWS; }
    private int removeX() { return listX() + listW() - scrollbarReserve() - 54; }
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
