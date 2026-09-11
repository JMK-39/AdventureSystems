package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.kineticcore.api.client.overlay.GuiOverlay;
import dev.xyat.adventuresystems.curios.util.ColorText;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.widget.KineticWidgets.GridScrollController;
import dev.xyat.kineticcore.api.client.search.KineticSearch;
import dev.xyat.adventuresystems.ftb.util.BridgeFTB;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import dev.xyat.kineticcore.api.client.screen.KineticScreen;
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
    private EditBox searchBox;
    private int left;
    private int top;

    QuestPickerScreen(Screen parent, Consumer<Long> callback) {
        super(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_pick_task_title"));
        useCanvas(
                580f,
                336f,
                6
        );
        this.parent = parent;
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
        left = (canvasWidth - PANEL_WIDTH) / 2;
        top = (canvasHeight - PANEL_HEIGHT) / 2;
        searchBox = new EditBox(font, left + 14, top + 30, PANEL_WIDTH - 132, 20, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_task_search"));
        searchBox.setResponder(text -> applySearch());
        addRenderableWidget(searchBox);
        addRenderableWidget(Button.builder(ColorText.translatable("gui.done"), button -> Minecraft.getInstance().setScreen(parent)).bounds(left + PANEL_WIDTH - 96, top + 30, 82, 20).build());
        setInitialFocus(searchBox);
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
    protected void renderCanvasBackground(
            @NotNull GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        ShopGuiSupport.renderBox(
                graphics,
                left,
                top,
                PANEL_WIDTH,
                PANEL_HEIGHT,
                ShopGuiSupport.PANEL_BG
        );

        graphics.drawCenteredString(
                font,
                title,
                left + PANEL_WIDTH / 2,
                top + 10,
                ShopGuiSupport.GOLD
        );
    }

    @Override
    protected void renderCanvasForeground(
            @NotNull GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderQuestRows(
                graphics,
                mouseX,
                mouseY
        );

        renderScrollbar(
                graphics,
                mouseX,
                mouseY
        );

        if (searchBox != null && searchBox.getValue().isEmpty() && !searchBox.isFocused()) {
            graphics.drawString(
                    font,
                    ColorText.translatable("gui.adventuresystems.curios.wallet.shop_task_search"),
                    searchBox.getX() + 4,
                    searchBox.getY() + 6,
                    0xFFAAAAAA,
                    false
            );
        }

        if (all.isEmpty()) {
            graphics.drawCenteredString(
                    font,
                    ColorText.translatable(
                            "gui.adventuresystems.curios.wallet.shop_task_empty"
                    ),
                    left + PANEL_WIDTH / 2,
                    top + 156,
                    ShopGuiSupport.RED
            );
        }
    }

    @Override
    protected void renderTooltips(
            GuiGraphics graphics,
            int scaledMouseX,
            int scaledMouseY,
            int rawMouseX,
            int rawMouseY
    ) {
        List<Component> tooltip =
                questTooltipAt(
                        scaledMouseX,
                        scaledMouseY
                );

        if (!tooltip.isEmpty()) {
            GuiOverlay.requestTooltip(tooltip, rawMouseX, rawMouseY);
        }
    }

    private List<Component> questTooltipAt(int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (scrollbarVisible() && GuiTheme.hovering(mouseX, mouseY, scrollbarX(), listY(), SCROLLBAR_WIDTH, listH())) {
            tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_scrollbar_tip").withStyle(ChatFormatting.GOLD));
            return tooltip;
        }
        if (!GuiTheme.hovering(mouseX, mouseY, listX(), listY(), listW() - scrollbarReserve(), listH())) return tooltip;
        int shift = listScroll.visualShift(ROW_HEIGHT);
        int row = (mouseY - listY() + shift) / ROW_HEIGHT;
        int index = listScroll.smoothIndexOffset() + row;
        if (index < 0 || index >= questModel.items().size()) return tooltip;
        QuestOption option = questModel.items().get(index);
        tooltip.add(Component.literal(option.displayTitle()).withStyle(ChatFormatting.GOLD));
        tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_quest_id_scaled", option.id()).withStyle(ChatFormatting.AQUA));
        if (!option.displayMeta().isBlank()) tooltip.add(Component.literal(option.displayMeta()));
        tooltip.add(ColorText.translatable("gui.adventuresystems.curios.wallet.shop_task_pick_tip"));
        return tooltip;
    }

    private void renderQuestRows(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.fill(listX(), listY(), listX() + listW(), listY() + listH(), 0xAA000000);
        listScroll.update(questModel.items().size(), VISIBLE_ROWS);
        int start = listScroll.smoothIndexOffset();
        int shift = listScroll.visualShift(ROW_HEIGHT);
                enableCanvasScissor(graphics, listX(), listY(), listX() + listW(), listY() + listH());
        try {
for (int i = start; i < Math.min(questModel.items().size(), start + VISIBLE_ROWS + 1); i++) {
            int y = listY() + (i - start) * ROW_HEIGHT - shift;
            QuestOption option = questModel.items().get(i);
            boolean hover = GuiTheme.hovering(mouseX, mouseY, listX(), y, listW() - scrollbarReserve(), ROW_HEIGHT);
            graphics.fill(listX() + 1, y + 1, listX() + listW() - scrollbarReserve() - 1, y + ROW_HEIGHT - 1, hover ? ShopGuiSupport.ROW_HOVER : ShopGuiSupport.ROW_BG);
            graphics.drawString(font, Component.literal(GuiTheme.trim(font, option.displayTitle(), 300)), listX() + 6, y + 3, ShopGuiSupport.TEXT_WHITE, true);
            ShopGuiSupport.drawScaledString(graphics, font, ColorText.translatable("gui.adventuresystems.curios.wallet.shop_quest_id_scaled", option.id()).getString(), listX() + 6, y + 14, ShopGuiSupport.CYAN);
            graphics.drawString(font, Component.literal(GuiTheme.trim(font, option.displayMeta(), 170)), listX() + 348, y + 7, ShopGuiSupport.TEXT_GRAY, true);
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
        listScroll.update(
                questModel.items().size(),
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
    protected boolean canvasMouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (super.canvasMouseClicked(mouseX, mouseY, button)) return true;
        if (button != 0) return false;

        listScroll.update(
                questModel.items().size(),
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

        int row = (int) ((mouseY - listY() + listScroll.visualShift(ROW_HEIGHT)) / ROW_HEIGHT);
        int index = listScroll.smoothIndexOffset() + row;

        if (index >= 0 && index < questModel.items().size()) {
            callback.accept(questModel.items().get(index).id());
            Minecraft.getInstance().setScreen(parent);
            return true;
        }

        return false;
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
        return super.canvasMouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected boolean canvasMouseScrolled(
            double mouseX,
            double mouseY,
            double delta
    ) {
        listScroll.update(
                questModel.items().size(),
                VISIBLE_ROWS
        );

        return listScroll.scroll(delta)
                || super.canvasMouseScrolled(
                        mouseX,
                        mouseY,
                        delta
                );
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }


    private int listX() { return left + 14; }
    private int listY() { return top + 62; }
    private int listW() { return PANEL_WIDTH - 28; }
    private int listH() { return ROW_HEIGHT * VISIBLE_ROWS; }
    private int scrollbarX() { return listX() + listW() - SCROLLBAR_WIDTH - 3; }
    private int scrollbarReserve() { return listScroll.canScroll() ? 16 : 0; }
    private boolean scrollbarVisible() { return listScroll.canScroll(); }

    private record QuestOption(
            long id,
            String code,
            String title,
            String chapter
    ) {
        String displayTitle() {
            String value =
                    title == null || title.isBlank()
                            ? code
                            : title;

            return value == null || value.isBlank()
                    ? String.valueOf(id)
                    : value;
        }

        String displayMeta() {
            String chapterText =
                    chapter == null
                            ? ""
                            : chapter;

            String codeText =
                    code == null
                            ? ""
                            : code;

            if (!chapterText.isBlank()
                    && !codeText.isBlank()) {
                return chapterText + "  " + codeText;
            }

            if (!chapterText.isBlank()) {
                return chapterText;
            }

            if (!codeText.isBlank()) {
                return codeText;
            }

            return String.valueOf(id);
        }

        String searchText() {
            return (
                    id
                            + " "
                            + code
                            + " "
                            + title
                            + " "
                            + chapter
            ).toLowerCase(Locale.ROOT);
        }
    }
}

