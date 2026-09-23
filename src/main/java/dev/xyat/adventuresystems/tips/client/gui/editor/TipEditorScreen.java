package dev.xyat.adventuresystems.tips.client.gui.editor;

import dev.xyat.adventuresystems.tips.TipsNetwork;
import dev.xyat.adventuresystems.tips.api.HelpTip;
import dev.xyat.adventuresystems.tips.client.TipRenderer;
import dev.xyat.adventuresystems.tips.config.ConfigLoader;
import dev.xyat.adventuresystems.tips.config.TipsConfigGui;
import dev.xyat.kineticcore.api.client.input.KineticMouseButtons;
import dev.xyat.kineticcore.api.client.overlay.KineticOverlays;
import dev.xyat.kineticcore.api.client.screen.KineticNativeScreen;
import dev.xyat.kineticcore.api.client.screen.KineticScreen;
import dev.xyat.kineticcore.api.client.selector.KineticSelectors;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.widget.button.KineticButtons.StateButton;
import dev.xyat.kineticcore.api.client.widget.input.KineticTextFields.KineticEditBox;
import dev.xyat.kineticcore.api.client.widget.selection.KineticTabs.SelectionItem;
import dev.xyat.kineticcore.api.client.widget.selection.KineticTabs.ScrollableSelectionList;
import dev.xyat.kineticcore.api.config.client.KTConfigApi;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import dev.xyat.kineticcore.api.text.KineticI18n;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

public class TipEditorScreen extends KineticNativeScreen {
    private final String languageCode;
    private final List<HelpTip.JsonModel.Entry> allEntries;
    private boolean savePending;
    private List<HelpTip.JsonModel.Entry> displayEntries;
    private HelpTip.JsonModel.Entry selectedEntry;

    private ScrollableSelectionList leftList;
    private KineticEditBox searchBox;
    private KineticEditBox textInput;
    private StateButton stageBtn;
    private StateButton timeBtn;

    private int guiW;
    private int guiH;
    private int x0;
    private int y0;
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
        super(KineticI18n.translatable("gui.adventuresystems.tips.tips.editor.title"));
        setParentScreen(lastScreen);
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
    protected void buildUi() {
        this.guiW = (int) (this.width * 0.95D);
        this.guiH = (int) (this.height * 0.95D);
        this.x0 = (this.width - guiW) / 2;
        this.y0 = (this.height - guiH) / 2;

        this.leftW = (int) (guiW * 0.30D);
        int maxLabelW = Math.max(
                font.width(KineticI18n.translatable("gui.adventuresystems.tips.tips.label.content")),
                Math.max(
                        font.width(KineticI18n.translatable("gui.adventuresystems.tips.tips.label.setting")),
                        font.width(KineticI18n.translatable("gui.adventuresystems.tips.tips.label.condition"))
                )
        ) + 8;

        this.editX = x0 + leftW + maxLabelW + 15;
        int editW = (x0 + guiW - 15) - editX;

        this.searchBox = addTextField(
                x0 + 10,
                y0 + 10,
                leftW - 15,
                Component.empty(),
                KineticI18n.translatable("gui.adventuresystems.tips.tips.search"),
                null,
                null
        );
        this.searchBox.setResponder(this::updateSearch);

        this.leftList = addScrollableSelectionList(
                x0 + 10,
                y0 + 40,
                leftW - 20,
                guiH - 80,
                tipSelectionItems(),
                selectedDisplayIndex(),
                0,
                index -> {
                    if (index >= 0 && index < displayEntries.size()) select(displayEntries.get(index));
                }
        );

        int curY = y0 + 10;
        int swatchGap = 2;
        int swatchSize = KineticScreen.COMPACT_CONTROL_HEIGHT;
        int swatchColumns = 8;
        int swatchRows = (COLORS.length + swatchColumns - 1) / swatchColumns;
        int paletteWidth = swatchColumns * swatchSize + (swatchColumns - 1) * swatchGap;
        for (int i = 0; i < COLORS.length; i++) {
            final String code = "§" + CODES[i];
            int row = i / swatchColumns;
            int column = i % swatchColumns;
            addColorSwatchButton(
                    editX + column * (swatchSize + swatchGap),
                    curY + row * (swatchSize + swatchGap),
                    COLORS[i],
                    null,
                    () -> insertCode(code)
            );
        }
        addCompactButton(
                editX + paletteWidth + 8,
                curY + (swatchRows - 1) * (swatchSize + swatchGap),
                24,
                KineticI18n.translatable("gui.adventuresystems.tips.tips.reset_short"),
                KineticI18n.translatable("gui.adventuresystems.tips.tips.reset_tooltip"),
                () -> insertCode("§r")
        );

        curY += swatchRows * swatchSize + (swatchRows - 1) * swatchGap + 12;
        this.textInput = addTextField(
                editX,
                curY,
                editW,
                Component.empty(),
                KineticI18n.translatable("gui.adventuresystems.tips.tips.content_hint_amp"),
                null,
                null
        );
        this.textInput.setMaxLength(512);
        this.textInput.setFormatter((string, index) -> {
            Style activeStyle = getStyleAtPos(this.textInput.getValue(), index);
            return Component.literal(string).setStyle(activeStyle).getVisualOrderText();
        });
        if (selectedEntry != null) this.textInput.setValue(selectedEntry.text != null ? selectedEntry.text : "");
        this.textInput.setResponder(value -> {
            if (selectedEntry != null) {
                selectedEntry.text = value;
                refreshTipList(false);
            }
        });

        int vGap = KineticScreen.STANDARD_CONTROL_HEIGHT + 8;
        curY += vGap;
        int halfW = (editW - 10) / 2;
        this.stageBtn = addButton(
                editX,
                curY,
                halfW,
                stageText(),
                null,
                this::cycleStage
        );
        this.timeBtn = addButton(
                editX + halfW + 10,
                curY,
                halfW,
                durationText(),
                null,
                () -> {
                    if (selectedEntry == null) return;
                    KineticClientRuntime.openScreen(new TimeEditScreen(
                            this,
                            selectedEntry.time,
                            value -> {
                                if (selectedEntry != null) selectedEntry.time = value;
                                updateUI();
                            }
                    ));
                }
        );

        curY += vGap;
        int thirdW = (editW - 20) / 3;
        addButton(editX, curY, thirdW, KineticI18n.translatable("gui.adventuresystems.tips.tips.add_item"), null, () -> openItemSelector(false));
        addButton(editX + thirdW + 10, curY, thirdW, KineticI18n.translatable("gui.adventuresystems.tips.tips.add_curios"), null, () -> openItemSelector(true));
        addButton(editX + (thirdW + 10) * 2, curY, thirdW, KineticI18n.translatable("gui.adventuresystems.tips.tips.set_structure"), null, () -> openRegistrySelector("structures"));

        curY += KineticScreen.STANDARD_CONTROL_HEIGHT + 5;
        addButton(editX, curY, thirdW, KineticI18n.translatable("gui.adventuresystems.tips.tips.set_biome"), null, () -> openRegistrySelector("biomes"));
        addButton(editX + thirdW + 10, curY, thirdW, KineticI18n.translatable("gui.adventuresystems.tips.tips.set_advancement"), null, () -> openRegistrySelector("advancements"));
        addButton(editX + (thirdW + 10) * 2, curY, thirdW, KineticI18n.translatable("gui.adventuresystems.tips.tips.set_dimension"), null, () -> openRegistrySelector("dimensions"));

        this.dynamicCondY = curY + KineticScreen.STANDARD_CONTROL_HEIGHT + 15;
        addButton(x0 + 10, y0 + guiH - 30, 75, KineticI18n.translatable("gui.adventuresystems.tips.tips.new"), null, this::addNew);
        addButton(x0 + guiW - 170, y0 + guiH - 30, 75, KineticI18n.translatable("gui.adventuresystems.tips.tips.save"), null, this::save);
        addButton(x0 + guiW - 85, y0 + guiH - 30, 75, KineticI18n.translatable("gui.adventuresystems.tips.tips.back"), null, this::onClose);
        updateSearch(searchBox.getValue());
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
    protected void renderNativeBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        GuiTheme.panel(graphics, x0, y0, guiW, guiH);
        GuiTheme.verticalSeparator(graphics, x0 + leftW + 5, y0 + 5, guiH - 40);

        if (textInput != null && stageBtn != null) {
            int labelX = editX - 10;
            int fontHeight = font.lineHeight;
            drawRightAligned(
                    graphics,
                    KineticI18n.translatable("gui.adventuresystems.tips.tips.label.content"),
                    labelX,
                    textInput.getY() + (textInput.getHeight() - fontHeight) / 2
            );
            drawRightAligned(
                    graphics,
                    KineticI18n.translatable("gui.adventuresystems.tips.tips.label.setting"),
                    labelX,
                    stageBtn.getY() + (stageBtn.getHeight() - fontHeight) / 2
            );
            drawRightAligned(
                    graphics,
                    KineticI18n.translatable("gui.adventuresystems.tips.tips.label.condition"),
                    labelX,
                    dynamicCondY - KineticScreen.STANDARD_CONTROL_HEIGHT - 15
            );
        }

        graphics.drawString(
                this.font,
                KineticI18n.translatable("gui.adventuresystems.tips.tips.delete_hint"),
                editX,
                dynamicCondY,
                GuiTheme.current().text()
        );
        if (selectedEntry != null && selectedEntry.conditions != null) {
            renderConditions(graphics, mouseX, mouseY, editX, dynamicCondY + 15);
        }
    }

    private void drawRightAligned(GuiGraphics graphics, Component text, int x, int y) {
        graphics.drawString(this.font, text, x - this.font.width(text), y, GuiTheme.current().text());
    }

    @Override
    protected boolean nativeMouseClicked(double mouseX, double mouseY, int button) {
        if (KineticMouseButtons.isSecondary(button) && leftList != null) {
            int index = leftList.itemAt(mouseX, mouseY);
            if (index >= 0 && index < displayEntries.size()) {
                allEntries.remove(displayEntries.get(index));
                updateSearch(searchBox == null ? "" : searchBox.getValue());
                return true;
            }
        }
        if (selectedEntry == null || selectedEntry.conditions == null) return false;
        HelpTip.JsonModel.Conditions conditions = selectedEntry.conditions;
        int x = this.editX;
        int currentY = this.dynamicCondY + 15;
        if (KineticMouseButtons.isSecondary(button)) {
            if (hasText(conditions.structure)) {
                if (isHover((int) mouseX, (int) mouseY, x, currentY, 150, 10)) {
                    conditions.structure = null;
                    return true;
                }
                currentY += 12;
            }
            if (hasText(conditions.biome)) {
                if (isHover((int) mouseX, (int) mouseY, x, currentY, 150, 10)) {
                    conditions.biome = null;
                    return true;
                }
                currentY += 12;
            }
            if (hasText(conditions.dimension)) {
                if (isHover((int) mouseX, (int) mouseY, x, currentY, 150, 10)) {
                    conditions.dimension = null;
                    return true;
                }
                currentY += 12;
            }
            if (hasText(conditions.advancement)) {
                if (isHover((int) mouseX, (int) mouseY, x, currentY, 150, 10)) {
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
                if (isHover((int) mouseX, (int) mouseY, itemX, currentY, 16, 16)) {
                    if (KineticMouseButtons.isSecondary(button)) {
                        conditions.items.remove(i);
                        return true;
                    }
                    if (KineticMouseButtons.isPrimary(button)) {
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
                if (isHover((int) mouseX, (int) mouseY, itemX, currentY, 16, 16)) {
                    if (KineticMouseButtons.isSecondary(button)) {
                        conditions.curios.remove(i);
                        return true;
                    }
                    if (KineticMouseButtons.isPrimary(button)) {
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
        focusControl(textInput);
        int position = textInput.getCursorPosition();
        String current = textInput.getValue();
        String next = current.substring(0, position) + code + current.substring(position);
        textInput.setValue(next);
        textInput.setCursorPosition(position + code.length());
    }

    private void openItemSelector(boolean curio) {
        KineticSelectors.openItemSelector(this, selection -> {
            if (selectedEntry == null || selection == null || !selection.isItem()) return;
            ItemStack stack = selection.stack();
            if (stack.isEmpty()) return;
            ResourceLocation itemId = KineticRegistries.items().id(stack.getItem());
            if (itemId == null) return;
            if (selectedEntry.conditions == null) selectedEntry.conditions = new HelpTip.JsonModel.Conditions();
            HelpTip.JsonModel.ItemCheck check = new HelpTip.JsonModel.ItemCheck();
            check.id = itemId.toString();
            check.nbtMode = "NONE";
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
        KineticClientRuntime.openScreen(new TipSelectors.RegistrySelectorScreen(this, type, id -> {
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

    private void renderConditions(GuiGraphics graphics, int mouseX, int mouseY, int x, int y) {
        HelpTip.JsonModel.Conditions conditions = selectedEntry.conditions;
        int currentY = y;
        if (hasText(conditions.structure)) {
            drawConditionLine(graphics, mouseX, mouseY, x, currentY, KineticI18n.translatable("gui.adventuresystems.tips.tips.cond_prefix.structure").append(conditions.structure));
            currentY += 12;
        }
        if (hasText(conditions.biome)) {
            drawConditionLine(graphics, mouseX, mouseY, x, currentY, KineticI18n.translatable("gui.adventuresystems.tips.tips.cond_prefix.biome").append(conditions.biome));
            currentY += 12;
        }
        if (hasText(conditions.dimension)) {
            drawConditionLine(graphics, mouseX, mouseY, x, currentY, KineticI18n.translatable("gui.adventuresystems.tips.tips.cond_prefix.dimension").append(conditions.dimension));
            currentY += 12;
        }
        if (hasText(conditions.advancement)) {
            drawConditionLine(graphics, mouseX, mouseY, x, currentY, KineticI18n.translatable("gui.adventuresystems.tips.tips.cond_prefix.advancement").append(conditions.advancement));
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
            graphics.drawString(
                    this.font,
                    KineticI18n.translatable("gui.adventuresystems.tips.tips.condition.normal", KineticI18n.translatable("gui.adventuresystems.tips.tips.cond_prefix.curios")),
                    x,
                    currentY,
                    GuiTheme.current().text()
            );
            int itemX = x + 35;
            for (HelpTip.JsonModel.ItemCheck check : conditions.curios) {
                renderItemCheck(graphics, mouseX, mouseY, itemX, currentY, check);
                itemX += 18;
            }
        }
    }

    private void drawConditionLine(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, Component text) {
        String key = isHover(mouseX, mouseY, x, y, 150, 10)
                ? "gui.adventuresystems.tips.tips.condition.hover"
                : "gui.adventuresystems.tips.tips.condition.normal";
        graphics.drawString(this.font, KineticI18n.translatable(key, text), x, y, GuiTheme.current().text());
    }

    private void renderItemCheck(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, HelpTip.JsonModel.ItemCheck check) {
        ResourceLocation id = KineticResourceIds.tryParse(check.id);
        if (id == null) return;
        var item = KineticRegistries.items().get(id);
        if (item == null) return;
        ItemStack stack = new ItemStack(item);
        boolean hovered = isHover(mouseX, mouseY, x, y, 18, 18);
        GuiTheme.itemSlot(graphics, x, y, 18, 4, hovered);
        graphics.renderItem(stack, x + 1, y + 1);
        if ("WEAK".equals(check.nbtMode)) graphics.renderItemDecorations(this.font, stack, x + 1, y + 1, "W");
        else if ("STRONG".equals(check.nbtMode)) graphics.renderItemDecorations(this.font, stack, x + 1, y + 1, "S");
    }

    @Override
    protected void renderNativeOverlayRequests(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ItemStack hovered = hoveredConditionStack(mouseX, mouseY);
        if (!hovered.isEmpty()) showItemTooltip(hovered);
    }

    private ItemStack hoveredConditionStack(int mouseX, int mouseY) {
        if (selectedEntry == null || selectedEntry.conditions == null) return ItemStack.EMPTY;
        HelpTip.JsonModel.Conditions conditions = selectedEntry.conditions;
        int x = editX;
        int currentY = dynamicCondY + 15;
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
        ResourceLocation id = KineticResourceIds.tryParse(check.id);
        if (id == null) return ItemStack.EMPTY;
        var item = KineticRegistries.items().get(id);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    private void cycleNbtMode(HelpTip.JsonModel.ItemCheck check) {
        if ("NONE".equals(check.nbtMode) || check.nbtMode == null) check.nbtMode = "WEAK";
        else if ("WEAK".equals(check.nbtMode)) check.nbtMode = "STRONG";
        else check.nbtMode = "NONE";
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
        updateSearch(searchBox == null ? "" : searchBox.getValue());
        select(entry);
    }

    private void select(HelpTip.JsonModel.Entry entry) {
        this.selectedEntry = entry;
        if (textInput != null) textInput.setValue(entry.text != null ? entry.text : "");
        if (leftList != null) {
            leftList.setSelectedIndex(selectedDisplayIndex());
            leftList.ensureSelectedVisible();
        }
        updateUI();
    }

    private Component stageText() {
        return KineticI18n.translatable(
                "gui.adventuresystems.tips.tips.stage",
                selectedEntry != null ? selectedEntry.stage : "any"
        );
    }

    private Component durationText() {
        return KineticI18n.translatable(
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
                        false,
                        false
                ))
                .toList();
    }

    private Component tipLabel(HelpTip.JsonModel.Entry entry) {
        if (entry == null || entry.text == null || entry.text.isEmpty()) {
            return KineticI18n.translatable("gui.adventuresystems.tips.tips.unnamed");
        }
        return Component.literal(TipRenderer.COLOR_PATTERN.matcher(entry.text).replaceAll(""));
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
            KineticOverlays.toast(KineticI18n.translatable("msg.adventuresystems.tips.tips.save_failed"));
        }
    }

}
