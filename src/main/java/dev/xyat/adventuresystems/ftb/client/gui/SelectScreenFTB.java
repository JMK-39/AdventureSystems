package dev.xyat.adventuresystems.ftb.client.gui;

import dev.xyat.kineticcore.api.client.overlay.GuiOverlay;
import dev.xyat.kineticcore.api.client.search.KineticSearch;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.screen.KineticScreen;
import dev.xyat.kineticcore.api.client.widget.KineticWidgets.Scroll;
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
import dev.xyat.adventuresystems.ftb.util.BridgeFTB;
import dev.xyat.adventuresystems.ftb.data.FavoritesStoreFTB;
import dev.xyat.adventuresystems.ftb.data.RefFTB;

public class SelectScreenFTB extends KineticScreen {
    private static final int LIST_X = 24;
    private static final int LIST_Y = 88;
    private static final int LIST_W = 792;
    private static final int LIST_H = 330;
    private static final int ROW_H = 36;
    private static final int FAV_W = 40;
    private static final int FAVORITE_ON_COLOR = 0xFF55FF55;
    private static final int FAVORITE_OFF_COLOR = 0xFFCFCFCF;

    private final Screen parent;
    private final ItemStack stack;
    private final List<RefFTB> allRefs;
    private final List<RefFTB> filtered = new ArrayList<>();

    private EditBox searchBox;
    private double scroll;
    private boolean draggingScrollbar;
    private final Scroll.State scrollState = new Scroll.State();

    public SelectScreenFTB(Screen parent, ItemStack stack, List<RefFTB> refs) {
        super(Component.translatable("screen.adventuresystems.ftb.select"));
        this.parent = parent;
        this.stack = stack;
        this.allRefs = new ArrayList<>(refs);
        this.filtered.addAll(refs);
        useCanvas(
                840f,
                470f,
                6
        );
        this.minScale = 0.5f;
    }

    @Override
    protected void buildUi() {
        searchBox = new EditBox(this.font, LIST_X, 54, 520, 20, Component.translatable("gui.adventuresystems.ftb.search"));
        searchBox.setResponder(s -> {
            applySearch();
            scroll = 0;
        });
        addRenderableWidget(searchBox);

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> this.onClose())
                .bounds(728, 432, 88, 22)
                .build());
    }

    private void applySearch() {
        filtered.clear();
        String q = searchBox.getValue().trim().toLowerCase();
        for (RefFTB ref : allRefs) {
            if (KineticSearch.match(ref.searchText(), q)) {
                filtered.add(ref);
            }
        }
    }

    @Override
    protected void renderCanvasBackground(@NotNull GuiGraphics g, int mx, int my, float pt) {
        GuiTheme.shadow(g, canvasWidth, canvasHeight);
        GuiTheme.panel(g, 10, 10, 820, 450);

        g.drawString(font, Component.translatable("screen.adventuresystems.ftb.select"), 24, 24, 0xFFFFFFFF, false);
        g.drawString(font, Component.translatable("label.adventuresystems.ftb.select.subtitle"), 170, 25, 0xFFFFFFFF, false);
        GuiTheme.itemSlot(g, stack, 565, 53);
        g.renderItem(stack, 566, 54);
        g.drawString(font, Component.translatable("label.adventuresystems.ftb.item.name", stack.getHoverName().copy().withStyle(ChatFormatting.AQUA)), 588, 59, 0xFFFFFFFF, false);

        renderList(g, mx, my);
    }

    @Override
    protected void renderCanvasForeground(@NotNull GuiGraphics g, int mx, int my, float pt) {
        if (searchBox != null && searchBox.getValue().isEmpty() && !searchBox.isFocused()) {
            g.pose().pushPose();
            g.pose().translate(0, 0, 300);
            g.drawString(font, Component.translatable("placeholder.adventuresystems.ftb.select.search"), searchBox.getX() + 6, searchBox.getY() + 6, 0xFF888888, false);
            g.pose().popPose();
        }
    }

    private void renderList(GuiGraphics g, int mx, int my) {
        int visible = LIST_H / ROW_H;
        int maxScroll = Math.max(0, filtered.size() - visible);
        if (scroll > maxScroll) scroll = maxScroll;

        GuiTheme.panelAlt(g, LIST_X, LIST_Y, LIST_W, LIST_H);

        if (filtered.isEmpty()) {
            g.drawString(font, Component.translatable("label.adventuresystems.ftb.empty.search"), LIST_X + 10, LIST_Y + 12, 0xFFFFFFFF, false);
        }

        long favId = FavoritesStoreFTB.getFavorite(stack);
        long actualFavId = favId;
        if (allRefs.stream().noneMatch(r -> r.id() == favId) && !allRefs.isEmpty()) {
            actualFavId = allRefs.get(0).id();
        }

        double smoothScroll = scrollState.follow(scroll, maxScroll, draggingScrollbar);
        int start = (int) Math.floor(smoothScroll + 1.0E-6D);
        int scrollShift = (int) Math.round((smoothScroll - start) * ROW_H);
        int end = Math.min(filtered.size(), start + visible + 1);
        enableCanvasScissor(g, LIST_X, LIST_Y, LIST_X + LIST_W, LIST_Y + LIST_H);
        for (int i = start; i < end; i++) {
            int rowY = LIST_Y + (i - start) * ROW_H - scrollShift;
            RefFTB ref = filtered.get(i);

            int drawX = LIST_X + 2;
            int drawY = rowY + 2;
            int drawW = LIST_W - 16 - FAV_W;
            int drawH = ROW_H - 2;
            int favX = drawX + drawW + 2;

            boolean hoverRow = GuiTheme.hovering(mx, my, drawX, drawY, drawW, drawH);
            boolean hoverFav = GuiTheme.hovering(mx, my, favX, drawY, FAV_W, drawH);
            boolean isFav = ref.id() == actualFavId;

            g.fill(drawX, drawY, drawX + drawW, drawY + drawH, hoverRow ? 0x66555555 : 0x33000000);
            g.renderOutline(drawX, drawY, drawW, drawH, 0xFFFFAA00);

            g.drawString(font, Component.translatable("label.adventuresystems.ftb.quest.title", Component.literal(GuiTheme.trim(font, ref.title(), LIST_W - 80)).withStyle(ChatFormatting.GOLD)), drawX + 8, drawY + 6, 0xFFFFFFFF, false);
            g.drawString(font, Component.translatable("label.adventuresystems.ftb.quest.id", Component.literal(ref.code()).withStyle(ChatFormatting.AQUA)), drawX + 8, drawY + 20, 0xFFFFFFFF, false);
            g.drawString(font, Component.translatable("label.adventuresystems.ftb.quest.meta", Component.literal(GuiTheme.trim(font, ref.chapter() + "  ·  " + ref.source(), LIST_W - 230)).withStyle(ChatFormatting.GRAY)), drawX + 190, drawY + 20, 0xFFFFFFFF, false);

            g.fill(favX, drawY, favX + FAV_W, drawY + drawH, hoverFav ? 0x6688AA22 : (isFav ? 0x44668811 : 0x33000000));
            g.renderOutline(favX, drawY, FAV_W, drawH, 0xFFFFAA00);

            Component star = Component.literal(isFav ? "★" : "☆");
            g.drawString(font, star, favX + FAV_W / 2 - font.width(star) / 2, drawY + 13, isFav ? FAVORITE_ON_COLOR : FAVORITE_OFF_COLOR, false);
        }

        g.disableScissor();
        int thumb = Scroll.calculateThumbHeight(LIST_H, visible, filtered.size(), 24);
        Scroll.renderScrollbar(g, mx, my, LIST_X + LIST_W - 6, LIST_Y, 4, LIST_H, thumb, maxScroll, smoothScroll, draggingScrollbar);
    }

    @Override
    protected void renderTooltips(GuiGraphics g, int smx, int smy, int mx, int my) {
        if (smx >= LIST_X && smx <= LIST_X + 520 && smy >= 54 && smy <= 74) {
            GuiOverlay.requestTooltip(List.of(
                    Component.translatable("tip.adventuresystems.ftb.search.title"),
                    Component.translatable("tip.adventuresystems.ftb.search.desc"),
                    Component.translatable("tip.adventuresystems.ftb.search.match")
            ), mx, my);
            return;
        }
        if (smx >= LIST_X && smx <= LIST_X + LIST_W - 10 && smy >= LIST_Y && smy <= LIST_Y + LIST_H) {
            if (smx >= LIST_X + LIST_W - 10 - FAV_W) {
                GuiOverlay.requestTooltip(List.of(
                        Component.translatable("tip.adventuresystems.ftb.favorite.title"),
                        Component.translatable("tip.adventuresystems.ftb.favorite.desc")
                ), mx, my);
            } else {
                GuiOverlay.requestTooltip(List.of(
                        Component.translatable("tip.adventuresystems.ftb.list.title"),
                        Component.translatable("tip.adventuresystems.ftb.list.open"),
                        Component.translatable("tip.adventuresystems.ftb.list.multi")
                ), mx, my);
            }
        }
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
                    if (mx >= LIST_X + LIST_W - 12 - FAV_W) {
                        FavoritesStoreFTB.setFavorite(this.stack, filtered.get(index).id());
                    } else {
                        Minecraft.getInstance().setScreen(parent);
                        BridgeFTB.openQuest(filtered.get(index).id());
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
            int visible = LIST_H / ROW_H;
            int maxScroll = Math.max(0, filtered.size() - visible);
            int thumb = Scroll.calculateThumbHeight(LIST_H, visible, filtered.size(), 24);
            scroll = Scroll.calculateScrollOffset(my, LIST_Y, LIST_H, thumb, maxScroll);
            scrollState.snap(scroll, maxScroll);
            return true;
        }
        return super.canvasMouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    protected boolean canvasMouseScrolled(double mx, double my, double d) {
        int visible = LIST_H / ROW_H;
        int maxScroll = Math.max(0, filtered.size() - visible);
        scroll = scrollState.wheel(scroll, d, 1.0D / 3.0D, maxScroll);
        return true;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
