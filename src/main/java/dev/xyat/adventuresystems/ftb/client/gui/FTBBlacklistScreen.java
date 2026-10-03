package dev.xyat.adventuresystems.ftb.client.gui;

import dev.xyat.adventuresystems.ftb.client.hud.FTBToastUtil;
import dev.xyat.adventuresystems.ftb.data.BindingStoreFTB;
import dev.xyat.adventuresystems.ftb.data.BlacklistStoreFTB;
import dev.xyat.kineticcore.api.client.gui.input.MouseInput;
import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.selector.KineticSelectors;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.widget.list.ItemGridDensity;
import dev.xyat.kineticcore.api.client.gui.widget.list.ItemGridItem;
import dev.xyat.kineticcore.api.client.gui.widget.list.ItemGridOutline;
import dev.xyat.kineticcore.api.client.gui.widget.list.KineticItemGrid;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.adventuresystems.text.AdventureText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class FTBBlacklistScreen extends KineticPage {
    private final List<String> allEntries = new ArrayList<>();
    private KineticItemGrid itemGrid;
    private int gridScroll;

    public FTBBlacklistScreen() {
        super(AdventureText.translatable("screen.adventuresystems.ftb.blacklist"));
        useCanvas(500, 320, 6);
        configureStandaloneDraft(BlacklistStoreFTB::getAll, BlacklistStoreFTB::replaceAll);
    }

    @Override
    protected void build(KineticUi ui) {
        if (itemGrid != null) gridScroll = itemGrid.scrollOffset();
        itemGrid = null;
        reloadEntries();
        ui.button(14, 10, 100).text(AdventureText.translatable("button.adventuresystems.ftb.blacklist.add"))
                .onClick(this::openSelector).build();
        ui.button(width() - 74, 10, 60).text(AdventureText.translatable("button.adventuresystems.ftb.save"))
                .onClick(this::save).build();
        itemGrid = ui.itemGrid(14, 36, width() - 28, height() - 50, ItemGridDensity.STANDARD, gridItems())
                .scrollOffset(gridScroll).onClick(ignored -> { }).build();
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
                    null,
                    true,
                    false,
                    false,
                    ItemGridOutline.WARNING
            ));
        }
        return items;
    }

    private void openSelector() {
        KineticSelectors.openItemSelector(selection -> {
            String target = "";
            if (selection.isMod()) target = "@" + selection.value();
            else if (selection.isTag()) target = "#" + selection.value();
            else if (selection.isItem()) {
                ItemStack stack = selection.stack();
                target = BindingStoreFTB.itemKey(stack);
                //? if >=1.21 {
                /*if (!stack.getComponentsPatch().isEmpty()) {
                *///?} else {
                if (stack.getTag() != null && stack.hasTag() && !stack.getTag().isEmpty()) {
                //?}
                    String nbt = BindingStoreFTB.stackNbtString(stack);
                    if (!nbt.isEmpty()) target += "|nbt:" + nbt;
                }
            }
            if (target.isEmpty()) return;
            BlacklistStoreFTB.add(target);
            reloadEntries();
            FTBToastUtil.show("adventuresystems_blacklist_added", AdventureText.translatable("msg.adventuresystems.ftb.blacklist.added"));
        });
    }

    @Override
    protected void renderBackground(KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        KineticTheme.shadow(graphics, width(), height());
        KineticTheme.canvasBackground(graphics, width(), height());
        graphics.text(
                AdventureText.translatable(
                        "label.adventuresystems.ftb.blacklist.count",
                        Component.literal(String.valueOf(allEntries.size()))
                ),
                122,
                16,
                KineticTheme.current().text(),
                false
        );
    }

    @Override
    protected void renderTooltips(int mouseX, int mouseY) {
        if (itemGrid == null) return;
        int index = itemGrid.itemAt(mouseX, mouseY);
        if (index < 0 || index >= allEntries.size()) return;
        String entry = allEntries.get(index);
        ItemStack icon = getIconForRule(entry);
        List<Component> tips = new ArrayList<>();
        if (entry.startsWith("@")) tips.add(AdventureText.translatable("label.adventuresystems.ftb.type.mod"));
        else if (entry.startsWith("#")) tips.add(AdventureText.translatable("label.adventuresystems.ftb.type.tag"));
        else tips.add(icon.getHoverName());
        tips.add(AdventureText.translatable("tip.adventuresystems.ftb.blacklist.rule_value", entry));
        tips.add(AdventureText.translatable("tip.adventuresystems.ftb.blacklist.remove"));
        showTooltip(tips);
    }

    @Override
    protected boolean onMouseClick(MouseInput input) {
        if (input.isRight() && itemGrid != null) {
            int index = itemGrid.itemAt(input.x(), input.y());
            if (index >= 0 && index < allEntries.size()) {
                BlacklistStoreFTB.remove(allEntries.get(index));
                reloadEntries();
                FTBToastUtil.show("adventuresystems_blacklist_removed", AdventureText.translatable("msg.adventuresystems.ftb.blacklist.removed"));
                return true;
            }
        }
        return false;
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
    protected boolean onCloseRequested() {
        navigateBack();
        return true;
    }
}
