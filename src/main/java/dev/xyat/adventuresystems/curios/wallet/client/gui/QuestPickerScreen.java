package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.adventuresystems.ftb.util.BridgeFTB;
import dev.xyat.kineticcore.api.client.input.KineticMouseButtons;
import dev.xyat.kineticcore.api.client.overlay.KineticOverlays;
import dev.xyat.kineticcore.api.client.screen.KineticScreen;
import dev.xyat.kineticcore.api.client.search.KineticSearch;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.widget.input.KineticTextFields.KineticEditBox;
import dev.xyat.kineticcore.api.client.widget.scroll.KineticScroll.GridScrollController;
import dev.xyat.kineticcore.api.text.KineticI18n;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

final class QuestPickerScreen extends KineticScreen {
    private static final int PANEL_WIDTH = 560;
    private static final int PANEL_HEIGHT = 316;
    private static final int ROW_HEIGHT = 24;
    private static final int VISIBLE_ROWS = 8;
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int MIN_THUMB_HEIGHT = 16;
    private final Screen parent;
    private final Consumer<Long> callback;
    private final List<QuestOption> all = new ArrayList<>();
    private final KineticSearch.Model<QuestOption> questModel;
    private final GridScrollController listScroll = new GridScrollController();
    private KineticEditBox searchBox;
    private int left;
    private int top;

    QuestPickerScreen(Screen parent, Consumer<Long> callback) {
        super(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_pick_task_title"));
        useCanvas(580.0F, 336.0F, 6);
        this.parent = parent;
        setParentScreen(parent);
        this.callback = callback;
        loadQuests();
        questModel = new KineticSearch.Model<>(
                all,
                (option, query) -> option.searchText().contains(query)
        );
        questModel.refresh("");
        listScroll.update(questModel.items().size(), VISIBLE_ROWS);
    }

    @Override
    protected void buildUi() {
        left = (canvasWidth() - PANEL_WIDTH) / 2;
        top = (canvasHeight() - PANEL_HEIGHT) / 2;
        searchBox = addTextField(
                left + 14,
                top + 30,
                PANEL_WIDTH - 132,
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_task_search"),
                KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_task_search"),
                null,
                null
        );
        searchBox.setResponder(text -> applySearch());
        addButton(
                left + PANEL_WIDTH - 96,
                top + 30,
                82,
                KineticI18n.translatable("gui.done"),
                null,
                this::returnToParent
        );
        focusControl(searchBox);
    }

    private void loadQuests() {
        all.clear();
        try {
            for (var ref : BridgeFTB.getAllQuestRefs()) {
                if (ref == null || ref.id() <= 0L) continue;
                all.add(new QuestOption(ref.id(), safeString(ref.code()), safeString(ref.title()), safeString(ref.chapter())));
            }
        } catch (Throwable ignored) {
        }
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private void applySearch() {
        String query = searchBox == null ? "" : searchBox.getValue();
        questModel.refresh(query);
        listScroll.reset();
        listScroll.update(questModel.items().size(), VISIBLE_ROWS);
    }

    @Override
    protected void renderCanvasBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        GuiTheme.panel(graphics, left, top, PANEL_WIDTH, PANEL_HEIGHT);
        graphics.drawCenteredString(font, title, left + PANEL_WIDTH / 2, top + 10, GuiTheme.current().text());
    }

    @Override
    protected void renderCanvasForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderQuestRows(graphics, mouseX, mouseY);
        renderScrollbar(graphics, mouseX, mouseY);

        if (all.isEmpty()) {
            graphics.drawCenteredString(
                    font,
                    KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_task_empty"),
                    left + PANEL_WIDTH / 2,
                    top + 156,
                    GuiTheme.current().text()
            );
        }
    }

    @Override
    protected void renderTooltips(GuiGraphics graphics, int scaledMouseX, int scaledMouseY, int rawMouseX, int rawMouseY) {
        List<Component> tooltip = questTooltipAt(scaledMouseX, scaledMouseY);
        if (!tooltip.isEmpty()) KineticOverlays.requestTooltip(tooltip, rawMouseX, rawMouseY);
    }

    private List<Component> questTooltipAt(int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (scrollbarVisible() && GuiTheme.hovering(mouseX, mouseY, scrollbarX(), listY(), SCROLLBAR_WIDTH, listH())) {
            tooltip.add(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_scrollbar_tip"));
            return tooltip;
        }
        if (!GuiTheme.hovering(mouseX, mouseY, listX(), listY(), listW() - scrollbarReserve(), listH())) return tooltip;
        int shift = listScroll.visualShift(ROW_HEIGHT);
        int row = (mouseY - listY() + shift) / ROW_HEIGHT;
        int index = listScroll.smoothIndexOffset() + row;
        if (index < 0 || index >= questModel.items().size()) return tooltip;
        QuestOption option = questModel.items().get(index);
        tooltip.add(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_name", option.displayTitle()));
        tooltip.add(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_quest_id_scaled", option.id()));
        if (!option.displayMeta().isBlank()) tooltip.add(Component.literal(option.displayMeta()));
        tooltip.add(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_task_pick_tip"));
        return tooltip;
    }

    private void renderQuestRows(GuiGraphics graphics, int mouseX, int mouseY) {
        GuiTheme.surface(graphics, listX(), listY(), listW(), listH(), GuiTheme.Surface.PANEL_ALT);
        listScroll.update(questModel.items().size(), VISIBLE_ROWS);
        int start = listScroll.smoothIndexOffset();
        int shift = listScroll.visualShift(ROW_HEIGHT);
        enableCanvasScissor(graphics, listX(), listY(), listX() + listW(), listY() + listH());
        try {
            for (int i = start; i < Math.min(questModel.items().size(), start + VISIBLE_ROWS + 1); i++) {
                int y = listY() + (i - start) * ROW_HEIGHT - shift;
                QuestOption option = questModel.items().get(i);
                boolean hover = GuiTheme.hovering(mouseX, mouseY, listX(), y, listW() - scrollbarReserve(), ROW_HEIGHT);
                GuiTheme.stateSurface(
                        graphics,
                        listX() + 1,
                        y + 1,
                        listW() - scrollbarReserve() - 2,
                        ROW_HEIGHT - 2,
                        GuiTheme.Surface.PANEL_ALT,
                        false,
                        hover,
                        false
                );
                graphics.drawString(
                        font,
                        Component.literal(GuiTheme.trim(font, option.displayTitle(), 300)),
                        listX() + 6,
                        y + 3,
                        GuiTheme.current().text(),
                        true
                );
                ShopGuiSupport.drawScaledString(
                        graphics,
                        font,
                        KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_quest_id_scaled", option.id()).getString(),
                        listX() + 6,
                        y + 14,
                        GuiTheme.current().mutedText()
                );
                graphics.drawString(
                        font,
                        Component.literal(GuiTheme.trim(font, option.displayMeta(), 170)),
                        listX() + 348,
                        y + 7,
                        GuiTheme.current().mutedText(),
                        true
                );
            }
        } finally {
            disableCanvasScissor(graphics);
        }
    }

    private void renderScrollbar(GuiGraphics graphics, int mouseX, int mouseY) {
        listScroll.update(questModel.items().size(), VISIBLE_ROWS);
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
    protected boolean canvasMouseClicked(double mouseX, double mouseY, int button) {
        if (super.canvasMouseClicked(mouseX, mouseY, button)) return true;
        if (!KineticMouseButtons.isPrimary(button)) return false;

        listScroll.update(questModel.items().size(), VISIBLE_ROWS);
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

        if (!GuiTheme.hovering(mouseX, mouseY, listX(), listY(), listW() - scrollbarReserve(), listH())) return false;
        int row = (int) ((mouseY - listY() + listScroll.visualShift(ROW_HEIGHT)) / ROW_HEIGHT);
        int index = listScroll.smoothIndexOffset() + row;
        if (index >= 0 && index < questModel.items().size()) {
            callback.accept(questModel.items().get(index).id());
            returnToParent();
            return true;
        }
        return false;
    }

    @Override
    protected boolean canvasMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (listScroll.drag(mouseY, listY(), listH(), MIN_THUMB_HEIGHT)) return true;
        return super.canvasMouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected boolean canvasMouseReleased(double mouseX, double mouseY, int button) {
        if (listScroll.release(button)) return true;
        return super.canvasMouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected boolean canvasMouseScrolled(double mouseX, double mouseY, double delta) {
        listScroll.update(questModel.items().size(), VISIBLE_ROWS);
        return listScroll.scroll(delta) || super.canvasMouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    protected boolean handleCloseRequest() {
        returnToParent();
        return true;
    }

    private void returnToParent() {
        navigateBack();
    }

    private int listX() { return left + 14; }
    private int listY() { return top + 62; }
    private int listW() { return PANEL_WIDTH - 28; }
    private int listH() { return ROW_HEIGHT * VISIBLE_ROWS; }
    private int scrollbarX() { return listX() + listW() - SCROLLBAR_WIDTH - 3; }
    private int scrollbarReserve() { return listScroll.canScroll() ? 16 : 0; }
    private boolean scrollbarVisible() { return listScroll.canScroll(); }

    public Screen getParent() {
        return parent;
    }

    private record QuestOption(long id, String code, String title, String chapter) {
        String displayTitle() {
            String value = title == null || title.isBlank() ? code : title;
            return value == null || value.isBlank() ? String.valueOf(id) : value;
        }

        String displayMeta() {
            String chapterText = chapter == null ? "" : chapter;
            String codeText = code == null ? "" : code;
            if (!chapterText.isBlank() && !codeText.isBlank()) return chapterText + "  " + codeText;
            if (!chapterText.isBlank()) return chapterText;
            if (!codeText.isBlank()) return codeText;
            return String.valueOf(id);
        }

        String searchText() {
            return (id + " " + code + " " + title + " " + chapter).toLowerCase(Locale.ROOT);
        }
    }
}
