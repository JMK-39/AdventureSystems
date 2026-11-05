package dev.xyat.adventuresystems.tips.client.gui.editor;

//? if >=1.21 {
/*import dev.xyat.adventuresystems.data.AdventureItemData;
*///?}

import dev.xyat.adventuresystems.tips.TipsNetwork;
import dev.xyat.adventuresystems.tips.api.HelpTip;
import dev.xyat.adventuresystems.tips.client.TipRenderer;
import dev.xyat.adventuresystems.tips.config.ConfigLoader;
import dev.xyat.adventuresystems.tips.config.TipTextLimits;
import dev.xyat.adventuresystems.tips.config.TipsConfigGui;
import dev.xyat.kineticcore.api.client.gui.input.MouseInput;
import dev.xyat.kineticcore.api.client.gui.input.MouseDragInput;
import dev.xyat.kineticcore.api.client.gui.input.ScrollInput;
import dev.xyat.kineticcore.api.client.gui.scroll.KineticScrollController;
import dev.xyat.kineticcore.api.client.gui.overlay.KineticOverlays;
import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.page.PageLayout;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.selector.KineticSelectors;
import dev.xyat.kineticcore.api.client.gui.text.KineticText;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.widget.KineticButton;
import dev.xyat.kineticcore.api.client.gui.widget.KineticCustomControl;
import dev.xyat.kineticcore.api.client.gui.widget.KineticTextField;
import dev.xyat.kineticcore.api.client.gui.widget.list.KineticSelectionList;
import dev.xyat.kineticcore.api.client.gui.widget.list.SelectionItem;
import dev.xyat.kineticcore.api.config.client.KTConfigApi;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import dev.xyat.adventuresystems.text.AdventureText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

public class TipEditorScreen extends KineticPage {
    private final String languageCode;
    private final List<HelpTip.JsonModel.Entry> allEntries;
    private boolean savePending;
    private List<HelpTip.JsonModel.Entry> displayEntries;
    private HelpTip.JsonModel.Entry selectedEntry;

    private KineticSelectionList leftList;
    private KineticTextField textInput;
    private KineticButton stageBtn;
    private KineticButton timeBtn;
    private String searchText = "";
    private int listScroll;
    private boolean deletingCondition;
    private Runnable conditionDeleteAction;

    private int guiW;
    private int guiH;
    private int x0;
    private int y0;
    private int leftW;
    private int editX;
    private int conditionButtonsY;
    private int dynamicCondY;
    private int renderedConditionOffset;
    private final KineticScrollController conditionScroll = new KineticScrollController();

    private static final int[] COLORS = {
            0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA,
            0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF
    };
    private static final String[] CODES = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    public TipEditorScreen(
            String languageCode,
            List<HelpTip.JsonModel.Entry> entries
    ) {
        super(AdventureText.translatable("gui.adventuresystems.tips.tips.editor.title"));
        useCanvas(640, 360, 4);
        this.languageCode = languageCode == null ? "en_us" : languageCode;
        this.allEntries = entries == null ? new ArrayList<>() : new ArrayList<>(entries);
        this.displayEntries = new ArrayList<>(this.allEntries);
        if (!this.allEntries.isEmpty()) this.selectedEntry = this.allEntries.get(0);
        bindDeleteAction(() -> deletingCondition ? conditionDeleteAction != null : selectedEntry != null && allEntries.contains(selectedEntry), () -> {
            if (deletingCondition) {
                Runnable action = conditionDeleteAction;conditionDeleteAction = null;
                if (action != null) action.run();
            } else removeTip(selectedEntry);
        });
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
        updateSearch(searchText);
        updateUI();
    }

    @Override
    protected void build(KineticUi ui) {
        if (leftList != null) listScroll = leftList.scrollOffset();
        leftList = null;
        this.guiW = width();
        this.guiH = height();
        this.x0 = (width() - guiW) / 2;
        this.y0 = (height() - guiH) / 2;

        this.leftW = 234;
        // The label column has the same width in every language; longer labels scroll inside it.
        int maxLabelW = 36 + 4;

        // Keep long translations from consuming the editor controls' space.
        maxLabelW = Math.min(maxLabelW, Math.max(24, (guiW - leftW - 30) / 4));
        this.editX = x0 + leftW + maxLabelW + 8;
        int editW = (x0 + guiW - 4) - editX;

        updateSearch(searchText);
        int toolbarY = y0 + 4;
        ui.button(x0 + 4, toolbarY, 40)
                .text(AdventureText.translatable("gui.adventuresystems.tips.tips.back"))
                .onClick(this::close).build();
        ui.textField(x0 + 48, toolbarY, leftW - 52)
                .label(Component.empty())
                .placeholder(AdventureText.translatable("gui.adventuresystems.tips.tips.search"))
                .value(searchText)
                .onChange(value -> {
                    searchText = value;
                    updateSearch(value);
                }).firstShownTextAsDefault().build();

        // Tips are text, so they are listed as plain rows; buttons are only for actions.
        int listTop = toolbarY + KineticPage.CONTROL_HEIGHT + 4;
        this.leftList = ui.selectionList(x0 + 4, listTop, leftW - 10, y0 + guiH - 4 - listTop, tipSelectionItems()).textRows()
                .selected(selectedDisplayIndex()).scrollOffset(listScroll)
                .onSelect(index -> {
                    if (index >= 0 && index < displayEntries.size()) select(displayEntries.get(index));
                }).build();

        ui.button(x0 + guiW - 88, toolbarY, 40)
                .text(AdventureText.translatable("gui.adventuresystems.tips.tips.new"))
                .onClick(this::addNew).build();
        ui.button(x0 + guiW - 44, toolbarY, 40)
                .text(AdventureText.translatable("gui.adventuresystems.tips.tips.save"))
                .onClick(this::save).build();

        int curY = toolbarY + KineticPage.CONTROL_HEIGHT + 4;
        int swatchGap = 2;
        int swatchColumns = COLORS.length;
        int swatchSize = Math.min(KineticPage.CONTROL_HEIGHT, Math.max(4, (editW - 44 - (swatchColumns - 1) * swatchGap) / swatchColumns));
        int paletteWidth = swatchColumns * swatchSize + (swatchColumns - 1) * swatchGap;
        for (int i = 0; i < COLORS.length; i++) {
            final String code = "§" + CODES[i];
            ui.add(new TipColorSwatch(editX + i * (swatchSize + swatchGap),
                    curY + (KineticPage.CONTROL_HEIGHT - swatchSize) / 2, swatchSize, COLORS[i], () -> insertCode(code)));
        }
        ui.button(editX + paletteWidth + 4, curY, 40)
                .compact().text(AdventureText.translatable("gui.adventuresystems.tips.tips.reset_short"))
                .tooltip(AdventureText.translatable("gui.adventuresystems.tips.tips.reset_tooltip"))
                .onClick(() -> insertCode("§r")).build();

        curY += KineticPage.CONTROL_HEIGHT + 4;
        this.textInput = ui.textField(editX, curY, editW)
                .label(Component.empty())
                .placeholder(AdventureText.translatable("gui.adventuresystems.tips.tips.content_hint_amp"))
                .tooltip(AdventureText.translatable("gui.adventuresystems.tips.tips.text_limits"))
                .validator(value -> TipTextLimits.isValid(TipTextLimits.fromEditor(value)))
                .maxLength(65534)
                .value(TipTextLimits.toEditor(selectedEntry==null?null:selectedEntry.text))
                .onChange(value -> {
                    if (selectedEntry != null) {
                        selectedEntry.text = TipTextLimits.fromEditor(value);
                        refreshTipList(false);
                    }
                }).firstShownTextAsDefault().build();
        this.textInput.limitTextLength(65534);
        this.textInput.formatText((string, index) -> {
            Style activeStyle = getStyleAtPos(this.textInput.textValue(), index);
            return Component.literal(string).setStyle(activeStyle).getVisualOrderText();
        });
        int vGap = KineticPage.CONTROL_HEIGHT + 4;
        curY += vGap;
        // Stage and duration only as wide as their values need; the six condition buttons share one row.
        int halfW = Math.min(96, (editW - 4) / 2);
        this.stageBtn = ui.button(editX, curY, halfW)
                .text(stageText()).onClick(this::cycleStage).build();
        this.timeBtn = ui.button(editX + halfW + 4, curY, Math.min(72, halfW))
                .text(durationText())
                .onClick(
                () -> {
                    if (selectedEntry == null) return;
                    openChild(new TimeEditScreen(
                            selectedEntry.time,
                            value -> {
                                if (selectedEntry != null) selectedEntry.time = value;
                                updateUI();
                            }
                    ));
                }).build();

        curY += vGap;
        this.conditionButtonsY = curY;
        int sixthW = Math.min(56, (editW - 5 * 2) / 6);
        String[] keys = {"add_item", "add_curios", "set_structure", "set_biome", "set_advancement", "set_dimension"};
        Runnable[] actions = {() -> openItemSelector(false), () -> openItemSelector(true), () -> openRegistrySelector("structures"),
                () -> openRegistrySelector("biomes"), () -> openRegistrySelector("advancements"), () -> openRegistrySelector("dimensions")};
        for (int i = 0; i < keys.length; i++) {
            Runnable action = actions[i];
            ui.button(editX + i * (sixthW + 2), curY, sixthW)
                    .text(AdventureText.translatable("gui.adventuresystems.tips.tips." + keys[i])).onClick(action).build();
        }

        this.dynamicCondY = curY + KineticPage.CONTROL_HEIGHT + 6;
        updateUI();
    }

    private static final class TipColorSwatch extends KineticCustomControl {
        private final int color;
        private final Runnable action;

        private TipColorSwatch(int x, int y, int size, int color, Runnable action) {
            super(x, y, size, size);
            this.color = color;
            this.action = action;
        }

        @Override
        protected void render(KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
            KineticTheme.colorSwatch(graphics, controlX(), controlY(), controlWidth(), controlHeight(), color,
                    controlHovered() || controlFocused());
        }

        @Override
        protected boolean onMouseClick(MouseInput input) {
            if (!input.isLeft()) return false;
            playClickSound();
            action.run();
            return true;
        }
    }

    private Style getStyleAtPos(String text, int index) {
        if (index <= 0 || text.isEmpty()) return Style.EMPTY;
        String sub = text.substring(0, Math.min(index, text.length()));
        Matcher matcher = TipRenderer.COLOR_PATTERN.matcher(sub);
        Style style = Style.EMPTY;
        while (matcher.find()) {
            String code = matcher.group().toLowerCase(Locale.ROOT);
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
    protected void renderBackground(KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        KineticTheme.panel(graphics, x0, y0, guiW, guiH);
        KineticTheme.verticalSeparator(graphics, x0 + leftW - 2, y0 + 4, guiH - 8);
        // Centred on the screen, clear of the search field on the left and New / Save on the right.
        graphics.scrollingTextCentered(title(), width() / 2, y0 + 8, 2 * (width() / 2 - (x0 + leftW + 4)), KineticTheme.current().text(), true);

        if (textInput != null && stageBtn != null) {
            int labelX = editX - 10;
            int fontHeight = KineticText.lineHeight();
            drawRightAligned(
                    graphics,
                    AdventureText.translatable("gui.adventuresystems.tips.tips.label.content"),
                    labelX,
                    textInput.controlY() + (textInput.controlHeight() - fontHeight) / 2
            );
            drawRightAligned(
                    graphics,
                    AdventureText.translatable("gui.adventuresystems.tips.tips.label.setting"),
                    labelX,
                    stageBtn.controlY() + (stageBtn.controlHeight() - fontHeight) / 2
            );
            drawRightAligned(
                    graphics,
                    AdventureText.translatable("gui.adventuresystems.tips.tips.label.condition"),
                    labelX,
                    conditionButtonsY + (KineticPage.CONTROL_HEIGHT - fontHeight) / 2
            );
        }

        KineticTheme.surface(graphics, editX - 2, dynamicCondY - 2,
                x0 + guiW - 6 - (editX - 2), y0 + guiH - 4 - (dynamicCondY - 2), KineticTheme.Surface.PANEL_ALT);
        KineticTheme.separator(graphics, editX, dynamicCondY - 3, Math.max(0, conditionRight() - editX));
        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.tips.tips.delete_hint"), editX + 2, dynamicCondY + 2, Math.max(0, conditionRight() - editX - 2), KineticTheme.current().text(), true);
        KineticTheme.separator(graphics, editX, dynamicCondY + 13, Math.max(0, conditionRight() - editX));
        if (selectedEntry != null && selectedEntry.conditions != null) {
            updateConditionScroll();
            renderedConditionOffset = (int) Math.round(conditionScroll.smoothOffset());
            graphics.clipped(editX, conditionTop(), conditionRight(), conditionBottom(),
                    () -> renderConditions(graphics, mouseX, mouseY, conditionLayout()));
            conditionScroll.render(graphics, mouseX, mouseY, conditionRight() + 2, conditionTop(), 3,
                    Math.max(1, conditionBottom() - conditionTop()), 8);
        }
    }

    private int conditionTop() { return dynamicCondY + 17; }
    private int conditionBottom() { return y0 + guiH - 6; }
    private int conditionRight() { return x0 + guiW - 12; }
    private boolean conditionContains(double x, double y) {
        return x >= editX && x < conditionRight() && y >= conditionTop() && y < conditionBottom();
    }
    private void updateConditionScroll() {
        int contentHeight = conditionLayout().height();
        int visible = Math.max(1, conditionBottom() - conditionTop());
        conditionScroll.updateRange(Math.max(0, contentHeight - visible), contentHeight, visible);
    }

    private record ConditionLine(int field, Component text, int y) {}
    private record ConditionCell(List<HelpTip.JsonModel.ItemCheck> source, int index, int x, int y) {
        HelpTip.JsonModel.ItemCheck check() { return source.get(index); }
    }
    private record ConditionLayout(List<ConditionLine> lines, List<ConditionCell> cells, int curiosY, int height) {}

    /** One layout drives the scroll range, drawing, tooltips and both mouse actions. */
    private ConditionLayout conditionLayout() {
        List<ConditionLine> lines = new ArrayList<>();
        List<ConditionCell> cells = new ArrayList<>();
        if (selectedEntry == null || selectedEntry.conditions == null) return new ConditionLayout(lines, cells, -1, 0);
        var conditions = selectedEntry.conditions;
        String[] values = {conditions.structure, conditions.biome, conditions.dimension, conditions.advancement};
        String[] keys = {"structure", "biome", "dimension", "advancement"};
        int y = 0;
        for (int i = 0; i < values.length; i++) {
            if (!hasText(values[i])) continue;
            lines.add(new ConditionLine(i, AdventureText.translatable("gui.adventuresystems.tips.tips.cond_prefix." + keys[i]).append(values[i]), y));
            y += 12;
        }
        y += addConditionCells(cells, conditions.items, editX, y);
        int curiosY = conditions.curios == null || conditions.curios.isEmpty() ? -1 : y;
        y += addConditionCells(cells, conditions.curios, editX + 35, y);
        return new ConditionLayout(lines, cells, curiosY, y);
    }

    private int addConditionCells(List<ConditionCell> cells, List<HelpTip.JsonModel.ItemCheck> source, int x, int y) {
        if (source == null || source.isEmpty()) return 0;
        int columns = Math.max(1, (conditionRight() - x + 2) / 20);
        for (int i = 0; i < source.size(); i++) {
            cells.add(new ConditionCell(source, i, x + i % columns * 20, y + i / columns * 20));
        }
        return (source.size() + columns - 1) / columns * 20;
    }

    private int conditionScreenY(int y) { return conditionTop() + y - renderedConditionOffset; }
    @Override
    protected boolean onMouseScroll(ScrollInput input) {
        if (!conditionContains(input.x(), input.y())) return false;
        updateConditionScroll();
        return conditionScroll.scroll(input.deltaY(), 12);
    }
    @Override
    protected boolean onMouseDrag(MouseDragInput input) {
        return conditionScroll.drag(input.y(), conditionTop(), Math.max(1, conditionBottom() - conditionTop()), 8);
    }
    @Override
    protected boolean onMouseRelease(MouseInput input) { return conditionScroll.release(input.button()); }

    private void drawRightAligned(KineticGraphics graphics, Component text, int x, int y) {
        graphics.scrollingTextRight(text, x, y, Math.max(0, x - (x0 + leftW + 10)), KineticTheme.current().text(), true);
    }

    @Override
    protected boolean onMouseClick(MouseInput input) {
        int mouseX = (int) input.x();
        int mouseY = (int) input.y();
        updateConditionScroll();
        if (conditionScroll.beginDrag(input.x(), input.y(), input.button(), conditionRight() + 2,
                conditionTop(), 3, Math.max(1, conditionBottom() - conditionTop()), 8, 2)) return true;
        if (input.isRight() && leftList != null) {
            int index = leftList.itemAt(input.x(), input.y());
            if (index >= 0 && index < displayEntries.size()) {
                removeTip(displayEntries.get(index));
                return true;
            }
        }
        if (selectedEntry == null || selectedEntry.conditions == null) return false;
        HelpTip.JsonModel.Conditions conditions = selectedEntry.conditions;
        if (!conditionContains(mouseX, mouseY)) return false;
        ConditionLayout layout = conditionLayout();
        if (input.isRight() || input.isLeft()) {
            for (ConditionLine line : layout.lines()) {
                if (isHover(mouseX, mouseY, editX, conditionScreenY(line.y()), conditionRight() - editX, 10)) {
                    deletingCondition = true;
                    conditionDeleteAction = () -> {
                        switch (line.field()) {
                            case 0 -> conditions.structure = null;
                            case 1 -> conditions.biome = null;
                            case 2 -> conditions.dimension = null;
                            case 3 -> conditions.advancement = null;
                        }
                    };
                    if (input.isRight()) {conditionDeleteAction.run();conditionDeleteAction = null;}
                    return true;
                }
            }
        }
        for (ConditionCell cell : layout.cells()) {
            if (isHover(mouseX, mouseY, cell.x(), conditionScreenY(cell.y()), 18, 18)) {
                deletingCondition = true;
                conditionDeleteAction = () -> cell.source().remove(cell.check());
                if (input.isRight()) {
                    conditionDeleteAction.run();conditionDeleteAction = null;
                    return true;
                }
                if (input.isLeft()) {
                    cycleNbtMode(cell.check());
                    return true;
                }
            }
        }
        return false;
    }

    private void removeTip(HelpTip.JsonModel.Entry entry) {
        if (entry == null) return;
        allEntries.remove(entry);
        if (entry == selectedEntry) {deletingCondition = false;conditionDeleteAction = null;}
        updateSearch(searchText);
    }

    private void insertCode(String code) {
        if (textInput == null) return;
        focus(textInput);
        int position = textInput.cursorIndex();
        String current = textInput.textValue();
        String next = current.substring(0, position) + code + current.substring(position);
        textInput.setTextValue(next);
        textInput.setCursorIndex(position + code.length());
    }

    private void openItemSelector(boolean curio) {
        KineticSelectors.openItemSelector(selection -> {
            if (selectedEntry == null || selection == null || !selection.isItem()) return;
            ItemStack stack = selection.stack();
            if (stack.isEmpty()) return;
            ResourceLocation itemId = KineticRegistries.items().id(stack.getItem());
            if (itemId == null) return;
            if (selectedEntry.conditions == null) selectedEntry.conditions = new HelpTip.JsonModel.Conditions();
            HelpTip.JsonModel.ItemCheck check = new HelpTip.JsonModel.ItemCheck();
            check.id = itemId.toString();
            //? if >=1.21 {
            /*check.componentMode = "NONE";
            String itemText = AdventureItemData.toItemText(stack);
            int componentStart = itemText.indexOf('[');
            check.components = componentStart < 0 ? null : itemText.substring(componentStart);
            *///?} else {
            check.nbtMode = "NONE";
            //?}
            if (curio) {
                if (selectedEntry.conditions.curios == null) selectedEntry.conditions.curios = new ArrayList<>();
                selectedEntry.conditions.curios.add(check);
            } else {
                if (selectedEntry.conditions.items == null) selectedEntry.conditions.items = new ArrayList<>();
                selectedEntry.conditions.items.add(check);
            }
        });
    }

    private void openRegistrySelector(String type) {
        openChild(new TipSelectors.RegistrySelectorScreen(type, id -> {
            if (selectedEntry == null) return;
            if (selectedEntry.conditions == null) selectedEntry.conditions = new HelpTip.JsonModel.Conditions();
            switch (type) {
                case "structures" -> selectedEntry.conditions.structure = id;
                case "biomes" -> selectedEntry.conditions.biome = id;
                case "advancements" -> selectedEntry.conditions.advancement = id;
                case "dimensions" -> selectedEntry.conditions.dimension = id;
                default -> {
                }
            }
        }));
    }

    private void renderConditions(KineticGraphics graphics, int mouseX, int mouseY, ConditionLayout layout) {
        for (ConditionLine line : layout.lines()) {
            drawConditionLine(graphics, mouseX, mouseY, editX, conditionScreenY(line.y()), line.text());
        }
        if (layout.curiosY() >= 0) {
            graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.tips.tips.condition.normal", AdventureText.translatable("gui.adventuresystems.tips.tips.cond_prefix.curios")), editX, conditionScreenY(layout.curiosY()), 31, KineticTheme.current().text(), true);
        }
        for (ConditionCell cell : layout.cells()) {
            int y = conditionScreenY(cell.y());
            if (y + 18 > conditionTop() && y < conditionBottom()) {
                renderItemCheck(graphics, mouseX, mouseY, cell.x(), y, cell.check());
            }
        }
    }

    private void drawConditionLine(KineticGraphics graphics, int mouseX, int mouseY, int x, int y, Component text) {
        String key = conditionContains(mouseX, mouseY) && isHover(mouseX, mouseY, x, y, conditionRight() - x, 10)
                ? "gui.adventuresystems.tips.tips.condition.hover"
                : "gui.adventuresystems.tips.tips.condition.normal";
        graphics.scrollingText(AdventureText.translatable(key, text), x, y, Math.max(0, conditionRight() - x), KineticTheme.current().text(), true);
    }

    private void renderItemCheck(KineticGraphics graphics, int mouseX, int mouseY, int x, int y, HelpTip.JsonModel.ItemCheck check) {
        ResourceLocation id = KineticResourceIds.tryParse(check.id);
        if (id == null) return;
        var item = KineticRegistries.items().get(id);
        if (item == null) return;
        //? if >=1.21 {
        /*ItemStack stack = stackFor(check);
        if (stack.isEmpty()) return;
        *///?} else {
        ItemStack stack = new ItemStack(item);
        //?}
        boolean hovered = conditionContains(mouseX, mouseY) && isHover(mouseX, mouseY, x, y, 18, 18);
        KineticTheme.itemSlot(graphics, x, y, 18, 4, hovered);
        KineticTheme.item(graphics, stack, x, y, 18, 0.75F, false);
        graphics.push();
        graphics.translate(x + 3, y + 3);
        graphics.scale(0.75F, 0.75F);
        //? if >=1.21 {
        /*if ("WEAK".equals(check.componentMode)) graphics.itemDecorations(stack, 0, 0, "W");
        else if ("STRONG".equals(check.componentMode)) graphics.itemDecorations(stack, 0, 0, "S");
        *///?} else {
        if ("WEAK".equals(check.nbtMode)) graphics.itemDecorations(stack, 0, 0, "W");
        else if ("STRONG".equals(check.nbtMode)) graphics.itemDecorations(stack, 0, 0, "S");
        //?}
        graphics.pop();
    }

    @Override
    protected void renderTooltips(int mouseX, int mouseY) {
        ItemStack hovered = hoveredConditionStack(mouseX, mouseY);
        if (!hovered.isEmpty()) showItemTooltip(hovered);
    }

    private ItemStack hoveredConditionStack(int mouseX, int mouseY) {
        if (selectedEntry == null || selectedEntry.conditions == null) return ItemStack.EMPTY;
        if (!conditionContains(mouseX, mouseY)) return ItemStack.EMPTY;
        for (ConditionCell cell : conditionLayout().cells()) {
            if (isHover(mouseX, mouseY, cell.x(), conditionScreenY(cell.y()), 18, 18)) return stackFor(cell.check());
        }
        return ItemStack.EMPTY;
    }

    private ItemStack stackFor(HelpTip.JsonModel.ItemCheck check) {
        if (check == null || check.id == null) return ItemStack.EMPTY;
        //? if >=1.21 {
        /*try {
            return AdventureItemData.parseItemText(check.id + (check.components == null ? "" : check.components));
        } catch (RuntimeException exception) {
            return ItemStack.EMPTY;
        }
        *///?} else {
        ResourceLocation id = KineticResourceIds.tryParse(check.id);
        if (id == null) return ItemStack.EMPTY;
        var item = KineticRegistries.items().get(id);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
        //?}
    }

    private void cycleNbtMode(HelpTip.JsonModel.ItemCheck check) {
        //? if >=1.21 {
        /*if ("NONE".equals(check.componentMode) || check.componentMode == null) check.componentMode = "WEAK";
        else if ("WEAK".equals(check.componentMode)) check.componentMode = "STRONG";
        else check.componentMode = "NONE";
        *///?} else {
        if ("NONE".equals(check.nbtMode) || check.nbtMode == null) check.nbtMode = "WEAK";
        else if ("WEAK".equals(check.nbtMode)) check.nbtMode = "STRONG";
        else check.nbtMode = "NONE";
        //?}
    }

    private boolean hasText(String value) {
        return value != null && !value.isEmpty();
    }

    private boolean isHover(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private void addNew() {
        HelpTip.JsonModel.Entry entry = new HelpTip.JsonModel.Entry();
        entry.text = "";
        entry.stage = "any";
        entry.time = 5000;
        allEntries.add(entry);
        updateSearch(searchText);
        select(entry);
    }

    private void select(HelpTip.JsonModel.Entry entry) {
        this.selectedEntry = entry;
        deletingCondition = false;conditionDeleteAction = null;
        conditionScroll.reset();
        renderedConditionOffset = 0;
        if (textInput != null) textInput.setTextValue(TipTextLimits.toEditor(entry.text));
        if (leftList != null) {
            leftList.setSelectedIndex(selectedDisplayIndex());
            leftList.ensureSelectedVisible();
        }
        updateUI();
    }

    private Component stageText() {
        return AdventureText.translatable(
                "gui.adventuresystems.tips.tips.stage",
                AdventureText.translatable("gui.adventuresystems.tips.tips.stage."
                        + (selectedEntry != null ? selectedEntry.stage : "any"))
        );
    }

    private Component durationText() {
        return AdventureText.translatable(
                "gui.adventuresystems.tips.tips.duration",
                selectedEntry != null ? String.format(Locale.ROOT, "%.1f", selectedEntry.time / 1000.0D) : "3.0"
        );
    }

    private void updateUI() {
        if (stageBtn != null) stageBtn.setText(stageText());
        if (timeBtn != null) timeBtn.setText(durationText());
    }

    private void cycleStage() {
        if (selectedEntry == null) return;
        selectedEntry.stage = "any".equals(selectedEntry.stage)
                ? "loading"
                : ("loading".equals(selectedEntry.stage) ? "game" : "any");
        updateUI();
    }

    private void updateSearch(String queryText) {
        String query = queryText == null ? "" : queryText.toLowerCase(Locale.ROOT);
        displayEntries = allEntries.stream()
                .filter(entry -> entry.text != null && entry.text.toLowerCase(Locale.ROOT).contains(query))
                .toList();
        refreshTipList(true);
    }

    private void refreshTipList(boolean resetScroll) {
        if (leftList == null) return;
        leftList.setItems(tipSelectionItems());
        leftList.setSelectedIndex(selectedDisplayIndex());
        if (resetScroll) leftList.setScrollOffset(0);
    }

    private int selectedDisplayIndex() {
        return selectedEntry == null ? -1 : displayEntries.indexOf(selectedEntry);
    }

    // A tip over the limits blocks saving the whole list, so it is red and its tooltip says why.
    private List<SelectionItem> tipSelectionItems() {
        return displayEntries.stream()
                .map(entry -> {
                    Component problem = limitProblem(entry);
                    Component tooltip = AdventureText.literal(TipTextLimits.normalizeLines(entry.text == null ? "" : entry.text));
                    if (problem != null) tooltip = tooltip.copy().append("\n").append(problem.copy().withStyle(ChatFormatting.RED));
                    return new SelectionItem(tipLabel(entry), null, tooltip, true, problem != null);
                })
                .toList();
    }

    /** What keeps this tip from being saved, or null. */
    private static Component limitProblem(HelpTip.JsonModel.Entry entry) {
        TipTextLimits.Problem problem = TipTextLimits.problem(entry == null ? null : entry.text);
        if (problem == null) return null;
        String key = "gui.adventuresystems.tips.tips.limit.";
        return switch (problem.kind()) {
            case EMPTY -> AdventureText.translatable(key + "empty");
            case TOO_LONG -> AdventureText.translatable("gui.adventuresystems.tips.tips.text_limits");
            case TOO_MANY_LINES -> AdventureText.translatable(key + "lines", problem.count(), TipTextLimits.MAX_LINES);
            case LINE_TOO_LONG -> AdventureText.translatable(key + "line_length", problem.line(), problem.count(),
                    TipTextLimits.CHARACTERS_PER_LINE);
            case CONTROL_CHARACTER -> AdventureText.translatable(key + "control", problem.line());
        };
    }

    private Component tipLabel(HelpTip.JsonModel.Entry entry) {
        if (entry == null || entry.text == null || entry.text.isEmpty()) {
            return AdventureText.translatable("gui.adventuresystems.tips.tips.unnamed");
        }
        String firstLine=TipTextLimits.normalizeLines(entry.text).lines().filter(line->!line.isBlank()).findFirst().orElse("");
        return AdventureText.literal(firstLine);
    }

    private void save() {
        if (savePending) return;
        // Name the tip that blocks saving and select it; it may be one the player never opened.
        HelpTip.JsonModel.Entry blocking = allEntries.stream().filter(entry -> limitProblem(entry) != null).findFirst().orElse(null);
        if (blocking != null) {
            if (!displayEntries.contains(blocking)) {
                searchText = "";
                rebuild();
            }
            select(blocking);
            KineticOverlays.toast(AdventureText.translatable("gui.adventuresystems.tips.tips.limit.save_blocked", limitProblem(blocking)));
            return;
        }
        savePending = true;
        TipsNetwork.saveEditor(languageCode, allEntries);
    }

    public void handleSaveResult(boolean success) {
        savePending = false;
        if (success) {
            KTConfigApi.notifySaved(TipsConfigGui.EDITOR_PAGE_ID);
            commitDraft();
        } else {
            KineticOverlays.toast(AdventureText.translatable("msg.adventuresystems.tips.tips.save_failed"));
        }
    }

}
