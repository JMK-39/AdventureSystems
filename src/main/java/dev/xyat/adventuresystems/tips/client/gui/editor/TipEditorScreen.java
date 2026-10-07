package dev.xyat.adventuresystems.tips.client.gui.editor;

//? if >=1.21 {
/*import dev.xyat.adventuresystems.data.AdventureItemData;
*///?}

import dev.xyat.adventuresystems.tips.TipsNetwork;
import dev.xyat.adventuresystems.tips.api.HelpTip;
import dev.xyat.adventuresystems.tips.client.TipRenderer;
import dev.xyat.adventuresystems.tips.config.ConfigLoader;
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
import dev.xyat.kineticcore.api.client.gui.widget.KineticTextField;
import dev.xyat.kineticcore.api.client.gui.widget.list.KineticSelectionList;
import dev.xyat.kineticcore.api.client.gui.widget.list.SelectionItem;
import dev.xyat.kineticcore.api.config.client.KTConfigApi;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import dev.xyat.adventuresystems.text.AdventureText;
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

    private int guiW;
    private int guiH;
    private int x0;
    private int y0;
    private int leftW;
    private int editX;
    private int dynamicCondY;
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
        super(AdventureText.translatable("gui.adventuresystems.tips.tips.editor.title"), PageLayout.NATIVE);
        this.languageCode = languageCode == null ? "en_us" : languageCode;
        this.allEntries = entries == null ? new ArrayList<>() : new ArrayList<>(entries);
        this.displayEntries = new ArrayList<>(this.allEntries);
        if (!this.allEntries.isEmpty()) this.selectedEntry = this.allEntries.get(0);
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
        this.guiW = (int) (width() * 0.95D);
        this.guiH = (int) (height() * 0.95D);
        this.x0 = (width() - guiW) / 2;
        this.y0 = (height() - guiH) / 2;

        this.leftW = (int) (guiW * 0.30D);
        // The label column has the same width in every language; longer labels scroll inside it.
        int maxLabelW = 50 + 8;

        // Keep long translations from consuming the editor controls' space.
        maxLabelW = Math.min(maxLabelW, Math.max(24, (guiW - leftW - 30) / 4));
        this.editX = x0 + leftW + maxLabelW + 15;
        int editW = (x0 + guiW - 15) - editX;

        updateSearch(searchText);
        ui.textField(x0 + 10, y0 + 10, leftW - 15)
                .label(Component.empty())
                .placeholder(AdventureText.translatable("gui.adventuresystems.tips.tips.search"))
                .value(searchText)
                .onChange(value -> {
                    searchText = value;
                    updateSearch(value);
                }).firstShownTextAsDefault().build();

        this.leftList = ui.selectionList(x0 + 10, y0 + 40, leftW - 20, guiH - 80, tipSelectionItems())
                .selected(selectedDisplayIndex()).scrollOffset(listScroll)
                .onSelect(index -> {
                    if (index >= 0 && index < displayEntries.size()) select(displayEntries.get(index));
                }).build();

        int curY = y0 + 10;
        int swatchGap = 2;
        int swatchSize = KineticPage.CONTROL_HEIGHT;
        int swatchColumns = 8;
        int swatchRows = (COLORS.length + swatchColumns - 1) / swatchColumns;
        int paletteWidth = swatchColumns * swatchSize + (swatchColumns - 1) * swatchGap;
        for (int i = 0; i < COLORS.length; i++) {
            final String code = "§" + CODES[i];
            int row = i / swatchColumns;
            int column = i % swatchColumns;
            ui.colorSwatch(editX + column * (swatchSize + swatchGap),
                    curY + row * (swatchSize + swatchGap), COLORS[i])
                    .onClick(() -> insertCode(code)).build();
        }
        ui.button(editX + paletteWidth + 8, curY + (swatchRows - 1) * (swatchSize + swatchGap), 24)
                .compact().text(AdventureText.translatable("gui.adventuresystems.tips.tips.reset_short"))
                .tooltip(AdventureText.translatable("gui.adventuresystems.tips.tips.reset_tooltip"))
                .onClick(() -> insertCode("§r")).build();

        curY += swatchRows * swatchSize + (swatchRows - 1) * swatchGap + 12;
        this.textInput = ui.textField(editX, curY, editW)
                .label(Component.empty())
                .placeholder(AdventureText.translatable("gui.adventuresystems.tips.tips.content_hint_amp"))
                .maxLength(512)
                .value(selectedEntry != null && selectedEntry.text != null ? selectedEntry.text : "")
                .onChange(value -> {
                    if (selectedEntry != null) {
                        selectedEntry.text = value;
                        refreshTipList(false);
                    }
                }).firstShownTextAsDefault().build();
        this.textInput.limitTextLength(512);
        this.textInput.formatText((string, index) -> {
            Style activeStyle = getStyleAtPos(this.textInput.textValue(), index);
            return Component.literal(string).setStyle(activeStyle).getVisualOrderText();
        });
        int vGap = KineticPage.CONTROL_HEIGHT + 8;
        curY += vGap;
        int halfW = (editW - 10) / 2;
        this.stageBtn = ui.button(editX, curY, halfW)
                .text(stageText()).onClick(this::cycleStage).build();
        this.timeBtn = ui.button(editX + halfW + 10, curY, halfW)
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
        int thirdW = (editW - 20) / 3;
        ui.button(editX, curY, thirdW).text(AdventureText.translatable("gui.adventuresystems.tips.tips.add_item"))
                .onClick(() -> openItemSelector(false)).build();
        ui.button(editX + thirdW + 10, curY, thirdW).text(AdventureText.translatable("gui.adventuresystems.tips.tips.add_curios"))
                .onClick(() -> openItemSelector(true)).build();
        ui.button(editX + (thirdW + 10) * 2, curY, thirdW)
                .text(AdventureText.translatable("gui.adventuresystems.tips.tips.set_structure"))
                .onClick(() -> openRegistrySelector("structures")).build();

        curY += KineticPage.CONTROL_HEIGHT + 5;
        ui.button(editX, curY, thirdW).text(AdventureText.translatable("gui.adventuresystems.tips.tips.set_biome"))
                .onClick(() -> openRegistrySelector("biomes")).build();
        ui.button(editX + thirdW + 10, curY, thirdW)
                .text(AdventureText.translatable("gui.adventuresystems.tips.tips.set_advancement"))
                .onClick(() -> openRegistrySelector("advancements")).build();
        ui.button(editX + (thirdW + 10) * 2, curY, thirdW)
                .text(AdventureText.translatable("gui.adventuresystems.tips.tips.set_dimension"))
                .onClick(() -> openRegistrySelector("dimensions")).build();

        this.dynamicCondY = curY + KineticPage.CONTROL_HEIGHT + 15;
        ui.button(x0 + 10, y0 + guiH - 30, 75)
                .text(AdventureText.translatable("gui.adventuresystems.tips.tips.new"))
                .onClick(this::addNew).build();
        ui.button(x0 + guiW - 170, y0 + guiH - 30, 75)
                .text(AdventureText.translatable("gui.adventuresystems.tips.tips.save"))
                .onClick(this::save).build();
        ui.button(x0 + guiW - 85, y0 + guiH - 30, 75)
                .text(AdventureText.translatable("gui.adventuresystems.tips.tips.back"))
                .onClick(this::close).build();
        updateUI();
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
        KineticTheme.verticalSeparator(graphics, x0 + leftW + 5, y0 + 5, guiH - 40);

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
                    dynamicCondY - KineticPage.CONTROL_HEIGHT - 15
            );
        }

        graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.tips.tips.delete_hint"), editX, dynamicCondY, Math.max(0, x0 + guiW - 15 - editX), KineticTheme.current().text(), true);
        if (selectedEntry != null && selectedEntry.conditions != null) {
            updateConditionScroll();
            graphics.clipped(editX, conditionTop(), conditionRight(), conditionBottom(),
                    () -> renderConditions(graphics, mouseX, mouseY, editX, conditionTop() - conditionOffset()));
            conditionScroll.render(graphics, mouseX, mouseY, conditionRight() + 2, conditionTop(), 3,
                    Math.max(1, conditionBottom() - conditionTop()), 8);
        }
    }

    private int conditionTop() { return dynamicCondY + 15; }
    private int conditionBottom() { return y0 + guiH - 34; }
    private int conditionRight() { return x0 + guiW - 20; }
    private int conditionOffset() { return (int) Math.round(conditionScroll.smoothOffset()); }
    private boolean conditionContains(double x, double y) {
        return x >= editX && x < conditionRight() && y >= conditionTop() && y < conditionBottom();
    }
    private void updateConditionScroll() {
        int contentHeight = 0;
        if (selectedEntry != null && selectedEntry.conditions != null) {
            var conditions = selectedEntry.conditions;
            if (hasText(conditions.structure)) contentHeight += 12;
            if (hasText(conditions.biome)) contentHeight += 12;
            if (hasText(conditions.dimension)) contentHeight += 12;
            if (hasText(conditions.advancement)) contentHeight += 12;
            if (conditions.items != null && !conditions.items.isEmpty()) contentHeight += 18;
            if (conditions.curios != null) contentHeight += 18;
        }
        int visible = Math.max(1, conditionBottom() - conditionTop());
        conditionScroll.updateRange(Math.max(0, contentHeight - visible), contentHeight, visible);
    }
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
                allEntries.remove(displayEntries.get(index));
                updateSearch(searchText);
                return true;
            }
        }
        if (selectedEntry == null || selectedEntry.conditions == null) return false;
        HelpTip.JsonModel.Conditions conditions = selectedEntry.conditions;
        int x = this.editX;
        if (!conditionContains(mouseX, mouseY)) return false;
        int currentY = conditionTop() - conditionOffset();
        if (input.isRight()) {
            if (hasText(conditions.structure)) {
                if (isHover(mouseX, mouseY, x, currentY, 150, 10)) {
                    conditions.structure = null;
                    return true;
                }
                currentY += 12;
            }
            if (hasText(conditions.biome)) {
                if (isHover(mouseX, mouseY, x, currentY, 150, 10)) {
                    conditions.biome = null;
                    return true;
                }
                currentY += 12;
            }
            if (hasText(conditions.dimension)) {
                if (isHover(mouseX, mouseY, x, currentY, 150, 10)) {
                    conditions.dimension = null;
                    return true;
                }
                currentY += 12;
            }
            if (hasText(conditions.advancement)) {
                if (isHover(mouseX, mouseY, x, currentY, 150, 10)) {
                    conditions.advancement = null;
                    return true;
                }
                currentY += 12;
            }
        } else {
            if (hasText(conditions.structure)) currentY += 12;
            if (hasText(conditions.biome)) currentY += 12;
            if (hasText(conditions.dimension)) currentY += 12;
            if (hasText(conditions.advancement)) currentY += 12;
        }

        if (conditions.items != null) {
            int itemX = x;
            for (int i = 0; i < conditions.items.size(); i++) {
                if (isHover(mouseX, mouseY, itemX, currentY, 16, 16)) {
                    if (input.isRight()) {
                        conditions.items.remove(i);
                        return true;
                    }
                    if (input.isLeft()) {
                        cycleNbtMode(conditions.items.get(i));
                        return true;
                    }
                }
                itemX += 18;
            }
            if (!conditions.items.isEmpty()) currentY += 18;
        }
        if (conditions.curios != null) {
            int itemX = x + 35;
            for (int i = 0; i < conditions.curios.size(); i++) {
                if (isHover(mouseX, mouseY, itemX, currentY, 16, 16)) {
                    if (input.isRight()) {
                        conditions.curios.remove(i);
                        return true;
                    }
                    if (input.isLeft()) {
                        cycleNbtMode(conditions.curios.get(i));
                        return true;
                    }
                }
                itemX += 18;
            }
        }
        return false;
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

    private void renderConditions(KineticGraphics graphics, int mouseX, int mouseY, int x, int y) {
        HelpTip.JsonModel.Conditions conditions = selectedEntry.conditions;
        int currentY = y;
        if (hasText(conditions.structure)) {
            drawConditionLine(graphics, mouseX, mouseY, x, currentY, AdventureText.translatable("gui.adventuresystems.tips.tips.cond_prefix.structure").append(conditions.structure));
            currentY += 12;
        }
        if (hasText(conditions.biome)) {
            drawConditionLine(graphics, mouseX, mouseY, x, currentY, AdventureText.translatable("gui.adventuresystems.tips.tips.cond_prefix.biome").append(conditions.biome));
            currentY += 12;
        }
        if (hasText(conditions.dimension)) {
            drawConditionLine(graphics, mouseX, mouseY, x, currentY, AdventureText.translatable("gui.adventuresystems.tips.tips.cond_prefix.dimension").append(conditions.dimension));
            currentY += 12;
        }
        if (hasText(conditions.advancement)) {
            drawConditionLine(graphics, mouseX, mouseY, x, currentY, AdventureText.translatable("gui.adventuresystems.tips.tips.cond_prefix.advancement").append(conditions.advancement));
            currentY += 12;
        }
        if (conditions.items != null) {
            int itemX = x;
            for (HelpTip.JsonModel.ItemCheck check : conditions.items) {
                renderItemCheck(graphics, mouseX, mouseY, itemX, currentY, check);
                itemX += 18;
            }
            if (!conditions.items.isEmpty()) currentY += 18;
        }
        if (conditions.curios != null) {
            graphics.scrollingText(AdventureText.translatable("gui.adventuresystems.tips.tips.condition.normal", AdventureText.translatable("gui.adventuresystems.tips.tips.cond_prefix.curios")), x, currentY, 31, KineticTheme.current().text(), true);
            int itemX = x + 35;
            for (HelpTip.JsonModel.ItemCheck check : conditions.curios) {
                renderItemCheck(graphics, mouseX, mouseY, itemX, currentY, check);
                itemX += 18;
            }
        }
    }

    private void drawConditionLine(KineticGraphics graphics, int mouseX, int mouseY, int x, int y, Component text) {
        String key = isHover(mouseX, mouseY, x, y, 150, 10)
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
        boolean hovered = isHover(mouseX, mouseY, x, y, 18, 18);
        KineticTheme.itemSlot(graphics, x, y, 18, 4, hovered);
        KineticTheme.item(graphics, stack, x, y, 18, 0.875F, false);
        graphics.push();
        graphics.translate(x + 2, y + 2);
        graphics.scale(0.875F, 0.875F);
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
        HelpTip.JsonModel.Conditions conditions = selectedEntry.conditions;
        int x = editX;
        if (!conditionContains(mouseX, mouseY)) return ItemStack.EMPTY;
        int currentY = conditionTop() - conditionOffset();
        if (hasText(conditions.structure)) currentY += 12;
        if (hasText(conditions.biome)) currentY += 12;
        if (hasText(conditions.dimension)) currentY += 12;
        if (hasText(conditions.advancement)) currentY += 12;
        if (conditions.items != null) {
            int itemX = x;
            for (HelpTip.JsonModel.ItemCheck check : conditions.items) {
                if (isHover(mouseX, mouseY, itemX, currentY, 18, 18)) return stackFor(check);
                itemX += 18;
            }
            if (!conditions.items.isEmpty()) currentY += 18;
        }
        if (conditions.curios != null) {
            int itemX = x + 35;
            for (HelpTip.JsonModel.ItemCheck check : conditions.curios) {
                if (isHover(mouseX, mouseY, itemX, currentY, 18, 18)) return stackFor(check);
                itemX += 18;
            }
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
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
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
        conditionScroll.reset();
        if (textInput != null) textInput.setTextValue(entry.text != null ? entry.text : "");
        if (leftList != null) {
            leftList.setSelectedIndex(selectedDisplayIndex());
            leftList.ensureSelectedVisible();
        }
        updateUI();
    }

    private Component stageText() {
        return AdventureText.translatable(
                "gui.adventuresystems.tips.tips.stage",
                selectedEntry != null ? selectedEntry.stage : "any"
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

    private List<SelectionItem> tipSelectionItems() {
        return displayEntries.stream()
                .map(entry -> new SelectionItem(
                        tipLabel(entry),
                        null,
                        null,
                        true,
                        false
                ))
                .toList();
    }

    private Component tipLabel(HelpTip.JsonModel.Entry entry) {
        if (entry == null || entry.text == null || entry.text.isEmpty()) {
            return AdventureText.translatable("gui.adventuresystems.tips.tips.unnamed");
        }
        return AdventureText.literal(entry.text);
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
            commitDraft();
        } else {
            KineticOverlays.toast(AdventureText.translatable("msg.adventuresystems.tips.tips.save_failed"));
        }
    }

}
