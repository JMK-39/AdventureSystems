package dev.xyat.adventuresystems.tips.client.gui.editor;

import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.screen.KineticNativeScreen;
import dev.xyat.kineticcore.api.client.widget.KineticWidgets;
import dev.xyat.adventuresystems.tips.api.HelpTip;
import dev.xyat.adventuresystems.tips.client.TipRenderer;
import dev.xyat.adventuresystems.tips.TipsNetwork;
import dev.xyat.adventuresystems.tips.config.ConfigLoader;
import dev.xyat.adventuresystems.tips.config.TipsConfigGui;
import dev.xyat.kineticcore.api.client.overlay.GuiOverlay;
import dev.xyat.kineticcore.config.client.KTConfigApi;
import dev.xyat.kineticcore.api.client.search.ItemSearchIndex;
import dev.xyat.kineticcore.api.client.selector.ItemSelectorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class TipEditorScreen extends KineticNativeScreen {
    private final Screen lastScreen;
    private final String languageCode;
    private final List<HelpTip.JsonModel.Entry> allEntries;
    private boolean savePending;
    private List<HelpTip.JsonModel.Entry> displayEntries;
    private HelpTip.JsonModel.Entry selectedEntry;

    private TipListWidget leftList;
    private EditBox searchBox, textInput;
    private Button stageBtn, timeBtn;

    private int guiW, guiH, x0, y0;
    private int leftW;
    private int editX;
    private int dynamicCondY;

    private static final int[] COLORS = {
            0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA,
            0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF
    };
    private static final String[] CODES = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    public TipEditorScreen(
            Screen lastScreen,
            String languageCode,
            List<HelpTip.JsonModel.Entry> entries
    ) {
        super(Component.translatable("gui.adventuresystems.tips.tips.editor.title"));
        this.lastScreen = lastScreen;
        this.languageCode = languageCode == null ? "en_us" : languageCode;
        this.allEntries = entries == null ? new ArrayList<>() : new ArrayList<>(entries);
        this.displayEntries = new ArrayList<>(this.allEntries);
        if (!allEntries.isEmpty()) this.selectedEntry = allEntries.get(0);
        configureStandaloneDraft(this::captureTipSnapshot, this::restoreTipSnapshot);
    }

    private record TipEditorSnapshot(String json) {
    }

    private TipEditorSnapshot captureTipSnapshot() {
        return new TipEditorSnapshot(ConfigLoader.GSON.toJson(allEntries));
    }

    private void restoreTipSnapshot(TipEditorSnapshot snapshot) {
        int selectedIndex = selectedEntry == null ? -1 : allEntries.indexOf(selectedEntry);
        HelpTip.JsonModel.Entry[] restored = ConfigLoader.GSON.fromJson(
                snapshot == null ? "[]" : snapshot.json(),
                HelpTip.JsonModel.Entry[].class
        );
        allEntries.clear();
        if (restored != null) java.util.Collections.addAll(allEntries, restored);
        if (allEntries.isEmpty()) selectedEntry = null;
        else selectedEntry = allEntries.get(Math.max(0, Math.min(selectedIndex, allEntries.size() - 1)));
        String query = searchBox == null ? "" : searchBox.getValue();
        updateSearch(query);
        updateUI();
    }

    @Override
    protected void init() {
        Minecraft mc = Minecraft.getInstance();
        long pW = mc.getWindow().getScreenWidth();
        long pH = mc.getWindow().getScreenHeight();

        double uiScale;
        if (pW < 900 || pH < 600) uiScale = 0.55;
        else if (pW < 1600 || pH < 900) uiScale = 0.8;
        else uiScale = 1.0;

        this.guiW = (int)(this.width * 0.95);
        this.guiH = (int)(this.height * 0.95);
        this.x0 = (this.width - guiW) / 2;
        this.y0 = (this.height - guiH) / 2;

        this.leftW = (int)(guiW * 0.30);
        int maxLabelW = Math.max(font.width(Component.translatable("gui.adventuresystems.tips.tips.label.content")),
                Math.max(font.width(Component.translatable("gui.adventuresystems.tips.tips.label.setting")),
                        font.width(Component.translatable("gui.adventuresystems.tips.tips.label.condition")))) + 8;

        this.editX = x0 + leftW + maxLabelW + 15;
        int editW = (x0 + guiW - 15) - editX;

        this.searchBox = new EditBox(this.font, x0 + 10, y0 + 10, leftW - 15, 20, Component.empty());
        this.searchBox.setResponder(this::updateSearch);
        this.addRenderableWidget(searchBox);

        int itemHeight = Math.max(14, (int)(18 * uiScale));
        this.leftList = new TipListWidget(this.minecraft, leftW, guiH - 80, y0 + 40, y0 + guiH - 40, itemHeight);
        this.leftList.setLeftPos(x0 + 5);
        this.addWidget(this.leftList);

        int curY = y0 + 10;

        int colorBtnGap = (int)(2 * uiScale);
        int colorBtnSize = (editW - (17 * colorBtnGap)) / 17;
        colorBtnSize = Math.min(16, Math.max(8, colorBtnSize));

        for (int i = 0; i < COLORS.length; i++) {
            final String c = "§" + CODES[i];
            this.addRenderableWidget(new ColorSmallButton(editX + (i * (colorBtnSize + colorBtnGap)), curY, colorBtnSize, COLORS[i], b -> insertCode(c)));
        }
        this.addRenderableWidget(Button.builder(Component.literal("R"), b -> insertCode("§r"))
                .bounds(editX + (COLORS.length * (colorBtnSize + colorBtnGap)), curY - 1, colorBtnSize + 6, colorBtnSize + 2)
                .tooltip(Tooltip.create(Component.translatable("gui.adventuresystems.tips.tips.reset_tooltip")))
                .build());

        curY += (colorBtnSize + 12);
        this.textInput = new EditBox(this.font, editX, curY, editW, 20, Component.empty());
        this.textInput.setMaxLength(512);
        this.textInput.setFormatter((string, index) -> {
            Style activeStyle = getStyleAtPos(this.textInput.getValue(), index);
            return Component.literal(string).setStyle(activeStyle).getVisualOrderText();
        });

        if (selectedEntry != null) this.textInput.setValue(selectedEntry.text != null ? selectedEntry.text : "");
        this.textInput.setResponder(s -> { if (selectedEntry != null) { selectedEntry.text = s; leftList.refresh(); }});
        this.addRenderableWidget(textInput);

        int btnH = (int)(20 * uiScale);
        int vGap = (int)(28 * uiScale);
        curY += vGap;
        int halfW = (editW - 10) / 2;
        this.stageBtn = Button.builder(Component.translatable("gui.adventuresystems.tips.tips.stage", selectedEntry != null ? selectedEntry.stage : "any"), b -> cycleStage()).bounds(editX, curY, halfW, btnH).build();
        this.timeBtn = Button.builder(Component.translatable("gui.adventuresystems.tips.tips.duration", selectedEntry != null ? String.format("%.1f", selectedEntry.time / 1000.0) : "3.0"), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(new TimeEditScreen(this, selectedEntry.time, t -> { if (selectedEntry != null) selectedEntry.time = t; updateUI(); }));
        }).bounds(editX + halfW + 10, curY, halfW, btnH).build();
        this.addRenderableWidget(stageBtn);
        this.addRenderableWidget(timeBtn);

        curY += vGap;
        int thirdW = (editW - 20) / 3;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.adventuresystems.tips.tips.add_item"), b -> openItemSelector(false)).bounds(editX, curY, thirdW, btnH).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.adventuresystems.tips.tips.add_curios"), b -> openItemSelector(true)).bounds(editX + thirdW + 10, curY, thirdW, btnH).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.adventuresystems.tips.tips.set_structure"), b -> openRegistrySelector("structures")).bounds(editX + (thirdW + 10) * 2, curY, thirdW, btnH).build());

        curY += (btnH + 5);
        this.addRenderableWidget(Button.builder(Component.translatable("gui.adventuresystems.tips.tips.set_biome"), b -> openRegistrySelector("biomes")).bounds(editX, curY, thirdW, btnH).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.adventuresystems.tips.tips.set_advancement"), b -> openRegistrySelector("advancements")).bounds(editX + thirdW + 10, curY, thirdW, btnH).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.adventuresystems.tips.tips.set_dimension"), b -> openRegistrySelector("dimensions")).bounds(editX + (thirdW + 10) * 2, curY, thirdW, btnH).build());

        this.dynamicCondY = curY + btnH + 15;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.adventuresystems.tips.tips.new"), b -> addNew()).bounds(x0 + 10, y0 + guiH - 30, 75, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.adventuresystems.tips.tips.save"), b -> save()).bounds(x0 + guiW - 85, y0 + guiH - 30, 75, 20).build());
        updateSearch("");
        updateUI();
    }

    private Style getStyleAtPos(String text, int index) {
        if (index <= 0 || text.isEmpty()) return Style.EMPTY;
        String sub = text.substring(0, Math.min(index, text.length()));
        Matcher m = TipRenderer.COLOR_PATTERN.matcher(sub);
        Style style = Style.EMPTY;
        while (m.find()) {
            String code = m.group().toLowerCase();
            if (code.matches("§[0-9a-f]")) {
                int colorValue = COLORS["0123456789abcdef".indexOf(code.charAt(1))];
                style = Style.EMPTY.withColor(colorValue);
            } else if (code.equals("§l")) style = style.withBold(true);
            else if (code.equals("§o")) style = style.withItalic(true);
            else if (code.equals("§n")) style = style.withUnderlined(true);
            else if (code.equals("§m")) style = style.withStrikethrough(true);
            else if (code.equals("§r")) style = Style.EMPTY;
        }
        return style;
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);
        GuiTheme.panel(g, x0, y0, guiW, guiH, 0xCC000000, 0x88FFFFFF);

        if (leftList != null) leftList.render(g, mx, my, pt);
        g.fill(x0 + leftW + 5, y0 + 5, x0 + leftW + 6, y0 + guiH - 35, 0x33FFFFFF);

        int labelX = editX - 10;
        int fontH = 9;
        drawRightAligned(g, Component.translatable("gui.adventuresystems.tips.tips.label.content"), labelX, textInput.getY() + (textInput.getHeight() - fontH) / 2);
        drawRightAligned(g, Component.translatable("gui.adventuresystems.tips.tips.label.setting"), labelX, stageBtn.getY() + (stageBtn.getHeight() - fontH) / 2);
        drawRightAligned(g, Component.translatable("gui.adventuresystems.tips.tips.label.condition"), labelX, dynamicCondY - 20 - (20 - fontH) / 2 - 15);

        super.render(g, mx, my, pt);

        if (searchBox != null && searchBox.getValue().isEmpty()) {
            g.drawString(this.font, Component.translatable("gui.adventuresystems.tips.tips.search"), searchBox.getX() + 4, searchBox.getY() + (searchBox.getHeight() - 9) / 2, 0x777777, false);
        }

        g.drawString(this.font, Component.translatable("gui.adventuresystems.tips.tips.delete_hint"), editX, dynamicCondY, 0xFFFF55);
        if (selectedEntry != null && selectedEntry.conditions != null) {
            renderConditions(g, mx, my, editX, dynamicCondY + 15);
        }
    }

    private void drawRightAligned(GuiGraphics g, Component text, int x, int y) {
        g.drawString(this.font, text, x - this.font.width(text), y, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (selectedEntry != null && selectedEntry.conditions != null) {
            HelpTip.JsonModel.Conditions c = selectedEntry.conditions;
            int x = this.editX; int cy = this.dynamicCondY + 15;
            if (btn == 1) {
                if (hasText(c.structure)) { if (isHover((int)mx, (int)my, x, cy, 150, 10)) { c.structure = null; return true; } cy += 12; }
                if (hasText(c.biome)) { if (isHover((int)mx, (int)my, x, cy, 150, 10)) { c.biome = null; return true; } cy += 12; }
                if (hasText(c.dimension)) { if (isHover((int)mx, (int)my, x, cy, 150, 10)) { c.dimension = null; return true; } cy += 12; }
                if (hasText(c.advancement)) { if (isHover((int)mx, (int)my, x, cy, 150, 10)) { c.advancement = null; return true; } cy += 12; }
            }
            if (c.items != null) {
                int ix = x;
                for (int i = 0; i < c.items.size(); i++) {
                    if (isHover((int)mx, (int)my, ix, cy, 16, 16)) {
                        if (btn == 1) { c.items.remove(i); return true; }
                        else if (btn == 0) { cycleNbtMode(c.items.get(i)); return true; }
                    }
                    ix += 18;
                }
                if (!c.items.isEmpty()) cy += 18;
            }
            if (c.curios != null) {
                int ix = x + 35;
                for (int i = 0; i < c.curios.size(); i++) {
                    if (isHover((int)mx, (int)my, ix, cy, 16, 16)) {
                        if (btn == 1) { c.curios.remove(i); return true; }
                        else if (btn == 0) { cycleNbtMode(c.curios.get(i)); return true; }
                    }
                    ix += 18;
                }
            }
        }

        boolean handled = super.mouseClicked(mx, my, btn);

        if (!handled && btn == 0) {
            this.setFocused(null);
            if (textInput != null) textInput.setFocused(false);
            if (searchBox != null) searchBox.setFocused(false);
        }

        return handled;
    }

    private void insertCode(String c) { if (textInput == null) return; textInput.setFocused(true); int pos = textInput.getCursorPosition(); String next = textInput.getValue().substring(0, pos) + c + textInput.getValue().substring(pos); textInput.setValue(next); textInput.setCursorPosition(pos + c.length()); }

    private void openItemSelector(boolean curio) {
        ItemSearchIndex.prepareCache(() -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new ItemSelectorScreen(this, selection -> {
                    if (selectedEntry == null) return;
                    if (selection == null || !selection.isItem()) return;
                    ItemStack stack = selection.stack();
                    if (stack == null || stack.isEmpty()) return;
                    ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    if (itemId == null) return;
                    if (selectedEntry.conditions == null) selectedEntry.conditions = new HelpTip.JsonModel.Conditions();
                    HelpTip.JsonModel.ItemCheck ic = new HelpTip.JsonModel.ItemCheck();
                    ic.id = itemId.toString();
                    ic.nbtMode = "NONE";
                    if (curio) {
                        if (selectedEntry.conditions.curios == null) selectedEntry.conditions.curios = new ArrayList<>();
                        selectedEntry.conditions.curios.add(ic);
                    } else {
                        if (selectedEntry.conditions.items == null) selectedEntry.conditions.items = new ArrayList<>();
                        selectedEntry.conditions.items.add(ic);
                    }
                }));
            }
        });
    }

    private void openRegistrySelector(String type) {
        if (this.minecraft != null) this.minecraft.setScreen(new TipSelectors.RegistrySelectorScreen(this, type, id -> {
            if (selectedEntry == null) return;
            if (selectedEntry.conditions == null) selectedEntry.conditions = new HelpTip.JsonModel.Conditions();
            switch(type) {
                case "structures" -> selectedEntry.conditions.structure = id;
                case "biomes" -> selectedEntry.conditions.biome = id;
                case "advancements" -> selectedEntry.conditions.advancement = id;
                case "dimensions" -> selectedEntry.conditions.dimension = id;
            }
        }));
    }

    private void renderConditions(GuiGraphics g, int mx, int my, int x, int y) {
        HelpTip.JsonModel.Conditions c = selectedEntry.conditions;
        int cy = y;
        if (hasText(c.structure)) { g.drawString(this.font, Component.translatable("gui.adventuresystems.tips.tips.cond_prefix.structure").append(c.structure), x, cy, isHover(mx,my,x,cy,150,10)?0xFF5555:0xAAAAAA); cy+=12; }
        if (hasText(c.biome)) { g.drawString(this.font, Component.translatable("gui.adventuresystems.tips.tips.cond_prefix.biome").append(c.biome), x, cy, isHover(mx,my,x,cy,150,10)?0xFF5555:0xAAAAAA); cy+=12; }
        if (hasText(c.dimension)) { g.drawString(this.font, Component.translatable("gui.adventuresystems.tips.tips.cond_prefix.dimension").append(c.dimension), x, cy, isHover(mx,my,x,cy,150,10)?0xFF5555:0xAAAAAA); cy+=12; }
        if (hasText(c.advancement)) { g.drawString(this.font, Component.translatable("gui.adventuresystems.tips.tips.cond_prefix.advancement").append(c.advancement), x, cy, isHover(mx,my,x,cy,150,10)?0xFF5555:0xAAAAAA); cy+=12; }
        if (c.items != null) {
            int ix = x; for (HelpTip.JsonModel.ItemCheck ic : c.items) { renderItemCheck(g, mx, my, ix, cy, ic); ix += 18; }
            if (!c.items.isEmpty()) cy += 18;
        }
        if (c.curios != null) {
            g.drawString(this.font, Component.translatable("gui.adventuresystems.tips.tips.cond_prefix.curios"), x, cy, 0xAAAAAA);
            int ix = x + 35; for (HelpTip.JsonModel.ItemCheck ic : c.curios) { renderItemCheck(g, mx, my, ix, cy, ic); ix += 18; }
        }
    }

    private void renderItemCheck(GuiGraphics g, int mx, int my, int x, int y, HelpTip.JsonModel.ItemCheck ic) {
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(ic.id));
        if (item != null) {
            ItemStack stack = new ItemStack(item);
            boolean hovered = isHover(mx, my, x, y, 18, 18);
            GuiTheme.itemSlot(g, stack, x, y, 18, 4, hovered);
            g.renderItem(stack, x + 1, y + 1);
            if ("WEAK".equals(ic.nbtMode)) g.renderItemDecorations(this.font, stack, x + 1, y + 1, "W");
            else if ("STRONG".equals(ic.nbtMode)) g.renderItemDecorations(this.font, stack, x + 1, y + 1, "S");
        }
    }

    @Override
    protected void renderNativeOverlayRequests(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ItemStack hovered = hoveredConditionStack(mouseX, mouseY);
        if (!hovered.isEmpty()) showItemTooltip(hovered);
    }

    private ItemStack hoveredConditionStack(int mouseX, int mouseY) {
        if (selectedEntry == null || selectedEntry.conditions == null) return ItemStack.EMPTY;
        HelpTip.JsonModel.Conditions c = selectedEntry.conditions;
        int x = editX;
        int cy = dynamicCondY + 15;
        if (hasText(c.structure)) cy += 12;
        if (hasText(c.biome)) cy += 12;
        if (hasText(c.dimension)) cy += 12;
        if (hasText(c.advancement)) cy += 12;
        if (c.items != null) {
            int ix = x;
            for (HelpTip.JsonModel.ItemCheck check : c.items) {
                if (isHover(mouseX, mouseY, ix, cy, 18, 18)) return stackFor(check);
                ix += 18;
            }
            if (!c.items.isEmpty()) cy += 18;
        }
        if (c.curios != null) {
            int ix = x + 35;
            for (HelpTip.JsonModel.ItemCheck check : c.curios) {
                if (isHover(mouseX, mouseY, ix, cy, 18, 18)) return stackFor(check);
                ix += 18;
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack stackFor(HelpTip.JsonModel.ItemCheck check) {
        if (check == null || check.id == null) return ItemStack.EMPTY;
        ResourceLocation id = ResourceLocation.tryParse(check.id);
        if (id == null) return ItemStack.EMPTY;
        var item = ForgeRegistries.ITEMS.getValue(id);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    private void cycleNbtMode(HelpTip.JsonModel.ItemCheck ic) { if ("NONE".equals(ic.nbtMode) || ic.nbtMode == null) ic.nbtMode = "WEAK"; else if ("WEAK".equals(ic.nbtMode)) ic.nbtMode = "STRONG"; else ic.nbtMode = "NONE"; }

    private boolean hasText(String s) { return s != null && !s.isEmpty(); }
    private boolean isHover(int mx, int my, int x, int y, int w, int h) { return mx >= x && mx <= x + w && my >= y && my <= y + h; }

    private void addNew() {
        HelpTip.JsonModel.Entry e = new HelpTip.JsonModel.Entry();
        e.text = ""; e.stage="any"; e.time=5000;
        allEntries.add(e);
        updateSearch(searchBox.getValue()); select(e);
    }

    private void select(HelpTip.JsonModel.Entry e) {
        this.selectedEntry = e;
        if (textInput != null) textInput.setValue(e.text != null ? e.text : "");
        updateUI();
    }

    private void updateUI() {
        if (selectedEntry==null) return;
        if (stageBtn!=null) stageBtn.setMessage(Component.translatable("gui.adventuresystems.tips.tips.stage", selectedEntry.stage));
        if (timeBtn!=null) timeBtn.setMessage(Component.translatable("gui.adventuresystems.tips.tips.duration", String.format("%.1f", selectedEntry.time/1000.0)));
    }

    private void cycleStage() {
        if (selectedEntry==null) return;
        selectedEntry.stage="any".equals(selectedEntry.stage)?"loading":("loading".equals(selectedEntry.stage)?"game":"any");
        updateUI();
    }

    private void updateSearch(String q) {
        displayEntries = allEntries.stream().filter(e -> e.text != null && e.text.toLowerCase().contains(q.toLowerCase())).collect(java.util.stream.Collectors.toList());
        if (leftList != null) { leftList.refresh(); leftList.snapScrollAmount(0); }
    }

    private void save() {
        if (savePending) return;
        savePending = true;
        TipsNetwork.saveEditor(languageCode, allEntries);
    }

    public void handleSaveResult(boolean success) {
        savePending = false;
        if (success) {
            KTConfigApi.notifySaved(TipsConfigGui.EDITOR_PAGE_ID);
        } else {
            GuiOverlay.toast(Component.translatable("msg.adventuresystems.tips.tips.save_failed"));
        }
        if (success && this.minecraft != null) {
            commitDraft();
            this.minecraft.setScreen(lastScreen);
        }
    }

    private static class ColorSmallButton extends Button {
        private final int color;
        public ColorSmallButton(int x, int y, int s, int color, OnPress p) { super(x, y, s, s, Component.empty(), p, DEFAULT_NARRATION); this.color = color; }
        @Override public void renderWidget(GuiGraphics g, int mx, int my, float pt) {
            g.fill(getX(), getY(), getX()+width, getY()+height, 0xFF000000);
            g.fill(getX()+1, getY()+1, getX()+width-1, getY()+height-1, color|0xFF000000);
            if(isHoveredOrFocused()) g.renderOutline(getX(), getY(), width, height, 0xFFFFFFFF);
        }
    }

    class TipListWidget extends KineticWidgets.SmoothSelectionList<TipListWidget.Entry> {
        public TipListWidget(Minecraft mc, int w, int h, int t, int b, int ih) { super(mc, w, h, t, b, ih); this.setRenderHeader(false, 0); this.setRenderBackground(false); this.setRenderTopAndBottom(false); refresh(); }
        public void refresh() { this.clearEntries(); displayEntries.forEach(e -> this.addEntry(new Entry(e))); }
        @Override protected int getScrollbarPosition() { return this.x0 + this.width - 6; }
        @Override public int getRowWidth() { return this.width - 10; }

        class Entry extends net.minecraft.client.gui.components.ObjectSelectionList.Entry<Entry> {
            private final HelpTip.JsonModel.Entry data;
            public Entry(HelpTip.JsonModel.Entry e) { this.data = e; }

            @Override
            public void render(GuiGraphics g, int i, int t, int l, int w, int h, int mx, int my, boolean hv, float pt) {
                boolean isSelected = (data == selectedEntry);

                int bgColor = isSelected ? 0x66000000 : (hv ? 0x44000000 : 0x22000000);
                int outlineColor = isSelected ? 0xFFFFAA00 : (hv ? 0xFFAAAAAA : 0xFF555555);
                g.fill(l, t, l + w, t + h - 2, bgColor);
                g.renderOutline(l, t, w, h - 2, outlineColor);

                String label = (data.text == null || data.text.isEmpty()) ? Component.translatable("gui.adventuresystems.tips.tips.unnamed").getString() : TipRenderer.COLOR_PATTERN.matcher(data.text).replaceAll("");

                Font font = Minecraft.getInstance().font;
                int maxTextW = w - 8;
                String displayStr = label;
                if (font.width(label) > maxTextW) {
                    displayStr = font.plainSubstrByWidth(label, maxTextW - font.width("...")) + "...";
                }

                int textColor = isSelected ? 0xFFFFFF : (hv ? 0xDDDDDD : 0xAAAAAA);
                g.drawString(font, displayStr, l + 4, t + (h - 2 - font.lineHeight) / 2 + 1, textColor, true);
            }

            @Override
            public boolean mouseClicked(double x, double y, int b) {
                if(b == 0) select(data);
                else if(b == 1) { allEntries.remove(data); updateSearch(searchBox.getValue()); }
                return true;
            }
            @Override public @NotNull Component getNarration() { return Component.empty(); }
        }
    }
}
