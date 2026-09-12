package dev.xyat.adventuresystems.ftb.client.gui;

import dev.xyat.kineticcore.api.client.overlay.GuiOverlay;
import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FTBBlacklistScreen extends KineticScreen {
    private static final int SLOT_SIZE = 20;
    private final Screen parent;
    private final List<String> allEntries = new ArrayList<>();

    private int gridX, gridY, gridCols, gridRowsVisible;
    private double scrollOffset = 0D;
    private int maxScroll = 0;
    private boolean isScrolling = false;
    private final Scroll.State scrollState = new Scroll.State();

    public FTBBlacklistScreen(Screen parent) {
        super(Component.translatable("screen.adventuresystems.ftb.blacklist"));
        this.parent = parent;
        useCanvas(
                500f,
                320f,
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
        int paddingX = 14;
        int maxAvailableWidth = this.canvasWidth - paddingX * 2 - 8 - 4;
        this.gridCols = Math.max(1, maxAvailableWidth / SLOT_SIZE);
        int contentW = gridCols * SLOT_SIZE;
        this.gridX = (this.canvasWidth - (contentW + 8 + 4)) / 2;
        this.gridY = 36;
        int bottomY = this.canvasHeight - 10;
        this.gridRowsVisible = Math.max(1, (bottomY - gridY) / SLOT_SIZE);
        reloadEntries();

        int rightEdge = gridX + contentW + 8 + 4;

        addRenderableWidget(Button.builder(Component.translatable("button.adventuresystems.ftb.blacklist.add"), b -> openSelector())
                .bounds(gridX, 10, 100, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("button.adventuresystems.ftb.save"), b -> saveAndClose())
                .bounds(rightEdge - 60, 10, 60, 20).build());
    }

    private void reloadEntries() {
        allEntries.clear();
        allEntries.addAll(BlacklistStoreFTB.getAll());
        maxScroll = Math.max(0, (int) Math.ceil((double) allEntries.size() / gridCols) - gridRowsVisible);
    }

    private void openSelector() {
        Minecraft.getInstance().setScreen(new ItemSelectorScreen(this, selection -> {
            String target = "";
            if (selection.isMod()) target = "@" + selection.value();
            else if (selection.isTag()) target = "#" + selection.value();
            else if (selection.isItem()) {
                ItemStack s = selection.stack();
                target = BindingStoreFTB.itemKey(s);
                if (s.getTag() != null && s.hasTag() && !s.getTag().isEmpty()) {
                    String nbtStr = BindingStoreFTB.stackNbtString(s);
                    if (!nbtStr.isEmpty()) target += "|nbt:" + nbtStr;
                }
            }

            if (!target.isEmpty()) {
                BlacklistStoreFTB.add(target);
                reloadEntries();
                FTBToastUtil.show("adventuresystems_blacklist_added", Component.translatable("msg.adventuresystems.ftb.blacklist.added"));
            }
        }));
    }

    @Override
    protected void renderCanvasBackground(@NotNull GuiGraphics g, int mx, int my, float pt) {
        GuiTheme.shadow(g, canvasWidth, canvasHeight);
        g.fillGradient(0, 0, this.canvasWidth, this.canvasHeight, 0xFF222222, 0xFF111111);

        g.drawString(font, Component.translatable("label.adventuresystems.ftb.blacklist.count", Component.literal(String.valueOf(allEntries.size())).withStyle(ChatFormatting.GREEN)), gridX + 110, 16, 0xFFFFFFFF, false);

        int contentW = gridCols * SLOT_SIZE;
        int contentH = gridRowsVisible * SLOT_SIZE;
        g.fill(gridX - 3, gridY - 3, gridX + contentW + 8 + 7, gridY + contentH + 3, 0xFF000000);
        g.fill(gridX - 2, gridY - 2, gridX + contentW + 8 + 6, gridY + contentH + 2, 0xFF2A2A2A);
    }

    @Override
    protected void renderCanvasForeground(@NotNull GuiGraphics g, int mx, int my, float pt) {
        double smoothScroll = scrollState.follow(scrollOffset, maxScroll, isScrolling);
        int smoothRow = (int) Math.floor(smoothScroll + 1.0E-6D);
        int scrollShift = (int) Math.round((smoothScroll - smoothRow) * SLOT_SIZE);
        int startIdx = smoothRow * gridCols;
        int endIdx = Math.min(startIdx + (gridRowsVisible + 1) * gridCols, allEntries.size());

        for (int i = startIdx; i < endIdx; i++) {
            String entry = allEntries.get(i);
            int col = (i - startIdx) % gridCols;
            int row = (i - startIdx) / gridCols;
            int x = gridX + col * SLOT_SIZE;
            int y = gridY + row * SLOT_SIZE - scrollShift;

            boolean hovered = mx >= x && mx < x + SLOT_SIZE && my >= y && my < y + SLOT_SIZE;
            ItemStack icon = getIconForRule(entry);
            GuiTheme.itemSlot(g, icon, x, y, SLOT_SIZE, 4, hovered);

            RenderSystem.enableDepthTest();
            g.renderItem(icon, x + 2, y + 2);
            g.renderItemDecorations(this.font, icon, x + 2, y + 2);
            RenderSystem.disableDepthTest();

            if (entry.startsWith("@") || entry.startsWith("#")) {
                g.pose().pushPose();
                g.pose().translate(x + 2, y + 10, 200);
                g.pose().scale(0.6f, 0.6f, 1.0f);
                g.drawString(font, entry.startsWith("@") ? "@Mod" : "#Tag", 0, 0, 0xFFFFAA00, true);
                g.pose().popPose();
            } else if (entry.contains("|nbt:")) {
                g.pose().pushPose();
                g.pose().translate(x + 2, y + 10, 200);
                g.pose().scale(0.6f, 0.6f, 1.0f);
                g.drawString(font, "NBT", 0, 0, 0xFF55FFFF, true);
                g.pose().popPose();
            }

            g.fill(x + 1, y + SLOT_SIZE - 2, x + SLOT_SIZE - 1, y + SLOT_SIZE, 0xFFFF3333);

        }

        if (maxScroll > 0) {
            int contentH = gridRowsVisible * SLOT_SIZE;
            int thumbH = Scroll.calculateThumbHeight(contentH, gridRowsVisible, (int) Math.ceil((double) allEntries.size() / gridCols), 20);
            Scroll.renderScrollbar(g, mx, my, gridX + gridCols * SLOT_SIZE + 8, gridY, 4, contentH, thumbH, maxScroll, smoothScroll, isScrolling);
        }
    }

    @Override
    protected void renderTooltips(GuiGraphics g, int smx, int smy, int mx, int my) {
        double smoothScroll = scrollState.follow(scrollOffset, maxScroll, isScrolling);
        int smoothRow = (int) Math.floor(smoothScroll + 1.0E-6D);
        int scrollShift = (int) Math.round((smoothScroll - smoothRow) * SLOT_SIZE);
        int startIdx = smoothRow * gridCols;
        int endIdx = Math.min(startIdx + (gridRowsVisible + 1) * gridCols, allEntries.size());
        if (smx < gridX || smx >= gridX + gridCols * SLOT_SIZE
                || smy < gridY || smy >= gridY + gridRowsVisible * SLOT_SIZE) {
            return;
        }
        for (int i = startIdx; i < endIdx; i++) {
            int x = gridX + (i - startIdx) % gridCols * SLOT_SIZE;
            int y = gridY + (i - startIdx) / gridCols * SLOT_SIZE - scrollShift;
            if (smx >= x && smx < x + SLOT_SIZE && smy >= y && smy < y + SLOT_SIZE) {
                String entry = allEntries.get(i);
                ItemStack icon = getIconForRule(entry);
                List<Component> tips = new ArrayList<>();

                if (entry.startsWith("@")) {
                    tips.add(Component.translatable("label.adventuresystems.ftb.type.mod"));
                } else if (entry.startsWith("#")) {
                    tips.add(Component.translatable("label.adventuresystems.ftb.type.tag"));
                } else {
                    tips.add(icon.getHoverName());
                }

                tips.add(Component.literal(entry).withStyle(net.minecraft.ChatFormatting.GOLD));
                tips.add(Component.translatable("tip.adventuresystems.ftb.blacklist.remove"));

                GuiOverlay.requestTooltip(tips, mx, my);
                return;
            }
        }
    }

    @Override
    protected boolean canvasMouseClicked(double mx, double my, int btn) {
        int contentW = gridCols * SLOT_SIZE;
        int contentH = gridRowsVisible * SLOT_SIZE;

        if (maxScroll > 0 && mx >= gridX + contentW + 4 && mx <= gridX + contentW + 4 + 8 && my >= gridY && my < gridY + contentH) {
            this.isScrolling = true;
            return true;
        }

        if (mx >= gridX && mx < gridX + contentW && my >= gridY && my < gridY + contentH) {
            double smoothScroll = scrollState.follow(scrollOffset, maxScroll, isScrolling);
            int smoothRow = (int) Math.floor(smoothScroll + 1.0E-6D);
            int shift = (int) Math.round((smoothScroll - smoothRow) * SLOT_SIZE);
            int idx = (smoothRow + (int) Math.floor((my - gridY + shift) / SLOT_SIZE)) * gridCols + (int) ((mx - gridX) / SLOT_SIZE);
            if (idx >= 0 && idx < allEntries.size()) {
                if (btn == 1) { // 仅右键触发移除
                    BlacklistStoreFTB.remove(allEntries.get(idx));
                    reloadEntries();
                    FTBToastUtil.show("adventuresystems_blacklist_removed", Component.translatable("msg.adventuresystems.ftb.blacklist.removed"));
                }
            }
            return true;
        }
        return super.canvasMouseClicked(mx, my, btn);
    }

    @Override
    protected boolean canvasMouseReleased(double mx, double my, int btn) {
        if (btn == 0) this.isScrolling = false;
        return super.canvasMouseReleased(mx, my, btn);
    }

    @Override
    protected boolean canvasMouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (this.isScrolling && maxScroll > 0) {
            int contentH = gridRowsVisible * SLOT_SIZE;
            int thumbH = Scroll.calculateThumbHeight(contentH, gridRowsVisible, (int) Math.ceil((double) allEntries.size() / gridCols), 20);
            this.scrollOffset = Scroll.calculateScrollOffset(my, gridY, contentH, thumbH, maxScroll);
            scrollState.snap(this.scrollOffset, maxScroll);
            return true;
        }
        return super.canvasMouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    protected boolean canvasMouseScrolled(double mx, double my, double d) {
        if (maxScroll > 0) {
            scrollOffset = scrollState.wheel(scrollOffset, d, 1.0D / 3.0D, maxScroll);
            return true;
        }
        return false;
    }

    private ItemStack getIconForRule(String rule) {
        if (rule.startsWith("@")) {
            String modid = rule.substring(1);
            for (Item item : ForgeRegistries.ITEMS.getValues()) {
                ResourceLocation rl = ForgeRegistries.ITEMS.getKey(item);
                if (rl != null && rl.getNamespace().equals(modid)) return new ItemStack(item);
            }
            return new ItemStack(Items.BARRIER);
        } else if (rule.startsWith("#")) {
            return BindingStoreFTB.createDisplayStack(rule, "");
        } else {
            String[] parts = rule.split("\\|nbt:", 2);
            return BindingStoreFTB.createDisplayStack(parts[0], parts.length > 1 ? parts[1] : "");
        }
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
