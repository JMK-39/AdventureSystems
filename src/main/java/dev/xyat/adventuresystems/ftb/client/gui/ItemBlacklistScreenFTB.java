package dev.xyat.adventuresystems.ftb.client.gui;

import dev.xyat.kineticcore.api.client.search.KineticSearch;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.selector.ItemSelectorScreen;
import dev.xyat.kineticcore.api.client.screen.KineticScreen;
import dev.xyat.kineticcore.api.client.widget.KineticWidgets.Scroll;
import dev.xyat.adventuresystems.ftb.client.hud.FTBToastUtil;
import dev.xyat.adventuresystems.ftb.data.BindingStoreFTB;
import dev.xyat.adventuresystems.ftb.data.BlacklistStoreFTB;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ItemBlacklistScreenFTB extends KineticScreen {
    private static final int PANEL_X = 10;
    private static final int PANEL_Y = 10;
    private static final int PANEL_W = 600;
    private static final int PANEL_H = 400;
    private static final int LIST_X = 24;
    private static final int LIST_Y = 88;
    private static final int LIST_W = 572;
    private static final int LIST_H = 300;
    private static final int ROW_H = 28;

    private final Screen parent;
    private final List<String> allEntries = new ArrayList<>();
    private final List<String> filtered = new ArrayList<>();

    private EditBox searchBox;
    private double scroll;
    private boolean draggingScrollbar;
    private final Scroll.State scrollState = new Scroll.State();

    public ItemBlacklistScreenFTB(Screen parent) {
        super(Component.translatable("screen.adventuresystems.ftb_item.blacklist"));
        this.parent = parent;
        useCanvas(
                620f,
                420f,
                6
        );
        this.minScale = 0.5f;
        configureBlacklistDraft();
    }

    private void configureBlacklistDraft() {
        configureStandaloneDraft(BlacklistStoreFTB::getAll, BlacklistStoreFTB::replaceAll);
    }

    @Override
    protected void buildUi() {
        reloadEntries();

        searchBox = new EditBox(this.font, LIST_X + 110, 54, 350, 20, Component.translatable("gui.adventuresystems.ftb.search"));
        searchBox.setResponder(s -> {
            applySearch();
            scroll = 0;
        });
        addRenderableWidget(searchBox);

        addRenderableWidget(Button.builder(Component.translatable("button.adventuresystems.ftb_item.add_blacklist"), b -> Minecraft.getInstance().setScreen(new ItemSelectorScreen(this, selection -> {
            String target = "";
            if (selection.isMod()) target = "@" + selection.value();
            else if (selection.isTag()) target = "#" + selection.value();
            else if (selection.isItem()) target = BindingStoreFTB.itemKey(selection.stack());

            if (!target.isEmpty()) {
                BlacklistStoreFTB.add(target);
                reloadEntries();
                FTBToastUtil.show("adventuresystems_blacklist_added", Component.translatable("msg.adventuresystems.ftb.ftb_item.blacklist_added"));
            }
        }))).bounds(LIST_X, 52, 100, 22).build());

        addRenderableWidget(Button.builder(Component.translatable("button.adventuresystems.ftb.save"), b -> saveAndClose())
                .bounds(LIST_X + 470, 52, 100, 22)
                .build());
    }

    private void reloadEntries() {
        allEntries.clear();
        allEntries.addAll(BlacklistStoreFTB.getAll());
        applySearch();
    }

    private void applySearch() {
        filtered.clear();
        String q = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase();
        for (String entry : allEntries) {
            if (q.isEmpty() || KineticSearch.match(entry.toLowerCase(), q)) {
                filtered.add(entry);
            }
        }
    }

    @Override
    protected void renderCanvasBackground(@NotNull GuiGraphics g, int mx, int my, float pt) {
        GuiTheme.shadow(g, canvasWidth, canvasHeight);
        GuiTheme.panel(g, PANEL_X, PANEL_Y, PANEL_W, PANEL_H);

        g.drawString(font, Component.translatable("screen.adventuresystems.ftb_item.blacklist"), 24, 24, 0xFFFFFFFF, false);
        g.drawString(font, Component.translatable("label.adventuresystems.ftb_item.blacklist_desc"), 150, 25, 0xFFFFFFFF, false);
        g.drawString(font, Component.translatable("label.adventuresystems.ftb_item.blacklist_count", Component.literal(String.valueOf(filtered.size())).withStyle(ChatFormatting.GREEN)), 480, 25, 0xFFFFFFFF, false);

        renderList(g, mx, my);
    }

    @Override
    protected void renderCanvasForeground(@NotNull GuiGraphics g, int mx, int my, float pt) {
        if (searchBox != null && searchBox.getValue().isEmpty() && !searchBox.isFocused()) {
            g.pose().pushPose();
            g.pose().translate(0, 0, 300);
            g.drawString(font, Component.translatable("placeholder.adventuresystems.ftb_item.binding_search"), searchBox.getX() + 6, searchBox.getY() + 6, 0xFF888888, false);
            g.pose().popPose();
        }
    }

    private void renderList(GuiGraphics g, int mx, int my) {
        int visible = LIST_H / ROW_H;
        int maxScroll = Math.max(0, filtered.size() - visible);
        if (scroll > maxScroll) scroll = maxScroll;

        GuiTheme.panelAlt(g, LIST_X, LIST_Y, LIST_W, LIST_H);

        if (filtered.isEmpty()) {
            g.drawString(font, Component.translatable("label.adventuresystems.ftb_item.empty_blacklist"), LIST_X + 10, LIST_Y + 14, 0xFFFFFFFF, false);
        }

        double smoothScroll = scrollState.follow(scroll, maxScroll, draggingScrollbar);
        int start = (int) Math.floor(smoothScroll + 1.0E-6D);
        int scrollShift = (int) Math.round((smoothScroll - start) * ROW_H);
        int end = Math.min(filtered.size(), start + visible + 1);
        enableCanvasScissor(g, LIST_X, LIST_Y, LIST_X + LIST_W, LIST_Y + LIST_H);
        for (int i = start; i < end; i++) {
            int rowY = LIST_Y + (i - start) * ROW_H - scrollShift;
            String entry = filtered.get(i);

            int drawX = LIST_X + 2;
            int drawY = rowY + 2;
            int drawW = LIST_W - 14;
            int drawH = ROW_H - 2;

            boolean hover = GuiTheme.hovering(mx, my, drawX, drawY, drawW, drawH);
            int delW = 52;
            int delX = drawX + drawW - delW - 4;
            boolean deleteHover = GuiTheme.hovering(mx, my, delX, drawY + 2, delW, drawH - 6);

            g.fill(drawX, drawY, drawX + drawW, drawY + drawH, hover ? 0x66555555 : 0x33000000);
            g.renderOutline(drawX, drawY, drawW, drawH, 0xFFFFAA00);

            ItemStack stack = BindingStoreFTB.createDisplayStack(entry, "");
            GuiTheme.itemSlot(g, stack, drawX + 5, drawY + 3, 18, 4, false);
            if (!stack.isEmpty()) {
                g.renderItem(stack, drawX + 6, drawY + 4);
            }

            g.drawString(font, entry, drawX + 28, drawY + 8, 0xFFFFFFFF, false);

            g.fill(delX, drawY + 2, delX + delW, drawY + drawH - 2, deleteHover ? 0xAA993333 : 0x88553333);
            Component del = Component.translatable("button.adventuresystems.ftb_item.delete");
            g.drawString(font, del, delX + delW / 2 - font.width(del) / 2, drawY + 6, 0xFFFFFFFF, false);
        }

        g.disableScissor();
        int thumb = Scroll.calculateThumbHeight(LIST_H, visible, filtered.size(), 24);
        Scroll.renderScrollbar(g, mx, my, LIST_X + LIST_W - 6, LIST_Y, 4, LIST_H, thumb, maxScroll, smoothScroll, draggingScrollbar);
    }

    @Override
    protected boolean canvasMouseClicked(double mx, double my, int btn) {
        if (btn == 0) {
            int visible = LIST_H / ROW_H;
            int maxScroll = Math.max(0, filtered.size() - visible);

            if (mx >= LIST_X + LIST_W - 10 && mx <= LIST_X + LIST_W && my >= LIST_Y && my <= LIST_Y + LIST_H && maxScroll > 0) {
                int thumb = Scroll.calculateThumbHeight(LIST_H, visible, filtered.size(), 24);
                scroll = Scroll.calculateScrollOffset(my, LIST_Y, LIST_H, thumb, maxScroll);
                scrollState.snap(scroll, maxScroll);
                draggingScrollbar = true;
                return true;
            }

            if (mx >= LIST_X && mx <= LIST_X + LIST_W - 12 && my >= LIST_Y && my <= LIST_Y + LIST_H) {
                double smoothScroll = scrollState.follow(scroll, maxScroll, draggingScrollbar);
                int start = (int) Math.floor(smoothScroll + 1.0E-6D);
                int shift = (int) Math.round((smoothScroll - start) * ROW_H);
                int index = start + (int) Math.floor((my - LIST_Y + shift) / ROW_H);
                if (index >= 0 && index < filtered.size()) {
                    String entry = filtered.get(index);
                    if (mx >= LIST_X + LIST_W - 70 && mx <= LIST_X + LIST_W - 18) {
                        BlacklistStoreFTB.remove(entry);
                        reloadEntries();
                        FTBToastUtil.show("adventuresystems_blacklist_removed", Component.translatable("msg.adventuresystems.ftb.ftb_item.blacklist_removed"));
                    }
                    return true;
                }
            }
        }
        return super.canvasMouseClicked(mx, my, btn);
    }

    @Override
    protected boolean canvasMouseReleased(double mx, double my, int btn) {
        draggingScrollbar = false;
        return super.canvasMouseReleased(mx, my, btn);
    }

    @Override
    protected boolean canvasMouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (draggingScrollbar) {
            int trackHeight = LIST_H;
            int visible = trackHeight / ROW_H;
            int max = Math.max(0, filtered.size() - visible);
            int thumb = Scroll.calculateThumbHeight(trackHeight, visible, filtered.size(), 24);
            scroll = Scroll.calculateScrollOffset(my, LIST_Y, trackHeight, thumb, max);
            scrollState.snap(scroll, max);
            return true;
        }
        return super.canvasMouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    protected boolean canvasMouseScrolled(double mx, double my, double d) {
        int visible = LIST_H / ROW_H;
        int max = Math.max(0, filtered.size() - visible);
        scroll = scrollState.wheel(scroll, d, 1.0D / 3.0D, max);
        return true;
    }

    private void saveAndClose() {
        commitDraft();
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
