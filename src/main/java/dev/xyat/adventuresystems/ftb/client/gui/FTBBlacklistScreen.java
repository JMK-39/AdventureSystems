package dev.xyat.adventuresystems.ftb.client.gui;

import dev.xyat.adventuresystems.ftb.client.hud.FTBToastUtil;
import dev.xyat.adventuresystems.ftb.data.BindingStoreFTB;
import dev.xyat.adventuresystems.ftb.data.BlacklistStoreFTB;
import dev.xyat.kineticcore.api.client.input.KineticMouseButtons;
import dev.xyat.kineticcore.api.client.overlay.KineticOverlays;
import dev.xyat.kineticcore.api.client.screen.KineticScreen;
import dev.xyat.kineticcore.api.client.selector.KineticSelectors;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.widget.selection.KineticTabs.ItemGridDensity;
import dev.xyat.kineticcore.api.client.widget.selection.KineticTabs.ItemGridItem;
import dev.xyat.kineticcore.api.client.widget.selection.KineticTabs.ItemGridOutline;
import dev.xyat.kineticcore.api.client.widget.selection.KineticTabs.ScrollableItemGrid;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.text.KineticI18n;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FTBBlacklistScreen extends KineticScreen {
    private final Screen parent;
    private final List<String> allEntries = new ArrayList<>();
    private ScrollableItemGrid itemGrid;

    public FTBBlacklistScreen(Screen parent) {
        super(KineticI18n.translatable("screen.adventuresystems.ftb.blacklist"));
        this.parent = parent;
        setParentScreen(parent);
        useCanvas(500f, 320f, 6);
        configureStandaloneDraft(BlacklistStoreFTB::getAll, BlacklistStoreFTB::replaceAll);
    }

    @Override
    protected void buildUi() {
        reloadEntries();
        addButton(14, 10, 100, KineticI18n.translatable("button.adventuresystems.ftb.blacklist.add"), null, this::openSelector);
        addButton(canvasWidth() - 74, 10, 60, KineticI18n.translatable("button.adventuresystems.ftb.save"), null, this::save);
        itemGrid = addScrollableItemGrid(
                14,
                36,
                canvasWidth() - 28,
                canvasHeight() - 50,
                ItemGridDensity.STANDARD,
                gridItems(),
                0,
                ignored -> { }
        );
    }

    private void reloadEntries() {
        allEntries.clear();
        allEntries.addAll(BlacklistStoreFTB.getAll());
        if (itemGrid != null) itemGrid.setItems(gridItems());
    }

    private List<ItemGridItem> gridItems() {
        List<ItemGridItem> items = new ArrayList<>(allEntries.size());
        for (String entry : allEntries) {
            items.add(new ItemGridItem(
                    getIconForRule(entry),
                    Component.literal(entry),
                    true,
                    false,
                    false,
                    ItemGridOutline.WARNING
            ));
        }
        return items;
    }

    private void openSelector() {
        KineticSelectors.openItemSelector(this, selection -> {
            String target = "";
            if (selection.isMod()) target = "@" + selection.value();
            else if (selection.isTag()) target = "#" + selection.value();
            else if (selection.isItem()) {
                ItemStack stack = selection.stack();
                target = BindingStoreFTB.itemKey(stack);
                if (stack.getTag() != null && stack.hasTag() && !stack.getTag().isEmpty()) {
                    String nbt = BindingStoreFTB.stackNbtString(stack);
                    if (!nbt.isEmpty()) target += "|nbt:" + nbt;
                }
            }
            if (target.isEmpty()) return;
            BlacklistStoreFTB.add(target);
            reloadEntries();
            FTBToastUtil.show("adventuresystems_blacklist_added", KineticI18n.translatable("msg.adventuresystems.ftb.blacklist.added"));
        });
    }

    @Override
    protected void renderCanvasBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        GuiTheme.shadow(graphics, canvasWidth(), canvasHeight());
        GuiTheme.canvasBackground(graphics, canvasWidth(), canvasHeight());
        graphics.drawString(
                font,
                KineticI18n.translatable(
                        "label.adventuresystems.ftb.blacklist.count",
                        Component.literal(String.valueOf(allEntries.size()))
                ),
                122,
                16,
                GuiTheme.current().text(),
                false
        );
    }

    @Override
    protected void renderTooltips(GuiGraphics graphics, int screenMouseX, int screenMouseY, int mouseX, int mouseY) {
        if (itemGrid == null) return;
        int index = itemGrid.itemAt(screenMouseX, screenMouseY);
        if (index < 0 || index >= allEntries.size()) return;
        String entry = allEntries.get(index);
        ItemStack icon = getIconForRule(entry);
        List<Component> tips = new ArrayList<>();
        if (entry.startsWith("@")) tips.add(KineticI18n.translatable("label.adventuresystems.ftb.type.mod"));
        else if (entry.startsWith("#")) tips.add(KineticI18n.translatable("label.adventuresystems.ftb.type.tag"));
        else tips.add(icon.getHoverName());
        tips.add(KineticI18n.translatable("tip.adventuresystems.ftb.blacklist.rule_value", entry));
        tips.add(KineticI18n.translatable("tip.adventuresystems.ftb.blacklist.remove"));
        KineticOverlays.requestTooltip(tips, mouseX, mouseY);
    }

    @Override
    protected boolean canvasMouseClicked(double mouseX, double mouseY, int button) {
        if (KineticMouseButtons.isSecondary(button) && itemGrid != null) {
            int index = itemGrid.itemAt(mouseX, mouseY);
            if (index >= 0 && index < allEntries.size()) {
                BlacklistStoreFTB.remove(allEntries.get(index));
                reloadEntries();
                FTBToastUtil.show("adventuresystems_blacklist_removed", KineticI18n.translatable("msg.adventuresystems.ftb.blacklist.removed"));
                return true;
            }
        }
        return super.canvasMouseClicked(mouseX, mouseY, button);
    }

    private ItemStack getIconForRule(String rule) {
        if (rule.startsWith("@")) {
            String modId = rule.substring(1);
            for (Item item : KineticRegistries.items().values()) {
                ResourceLocation id = KineticRegistries.items().id(item);
                if (id != null && id.getNamespace().equals(modId)) return new ItemStack(item);
            }
            return new ItemStack(Items.BARRIER);
        }
        if (rule.startsWith("#")) return BindingStoreFTB.createDisplayStack(rule, "");
        String[] parts = rule.split("\\|nbt:", 2);
        return BindingStoreFTB.createDisplayStack(parts[0], parts.length > 1 ? parts[1] : "");
    }

    private void save() {
        commitDraft();
    }

    @Override
    protected boolean handleCloseRequest() {
        navigateBack();
        return true;
    }

    public Screen getParent() {
        return parent;
    }
}
