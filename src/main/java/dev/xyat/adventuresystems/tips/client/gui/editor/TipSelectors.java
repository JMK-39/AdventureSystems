package dev.xyat.adventuresystems.tips.client.gui.editor;

import dev.xyat.adventuresystems.tips.TipsUtils;
import dev.xyat.kineticcore.api.client.screen.KineticNativeScreen;
import dev.xyat.kineticcore.api.client.widget.KineticWidgets;
import dev.xyat.adventuresystems.tips.client.TipCache;
import dev.xyat.adventuresystems.tips.TipsNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

/**
 * 提示系统选择器界面合集
 */
public class TipSelectors {

    public static class RegistrySelectorScreen extends KineticNativeScreen {
        private final Screen parent;
        private final String type;
        private final Consumer<String> onSelect;
        private final List<RegistryEntry> allEntries = new ArrayList<>();
        private EditBox searchBox;
        private SelectionList listWidget;
        private int lastListSize = 0;

        public RegistrySelectorScreen(Screen parent, String type, Consumer<String> onSelect) {
            super(Component.translatable("gui.adventuresystems.tips.tips.setstitle", type));
            this.parent = parent; this.type = type; this.onSelect = onSelect;
        }

        @Override protected void init() {
            if ("structures".equals(type)) TipsNetwork.sendToServer(new TipsNetwork.RequestStructure(true));
            loadData();
            this.searchBox = new EditBox(this.font, 20, 10, this.width - 100, 20, Component.empty());
            this.searchBox.setResponder(this::updateSearch);
            this.addRenderableWidget(searchBox);
            this.addRenderableWidget(Button.builder(Component.translatable("gui.adventuresystems.tips.tips.cancel"), b -> onClose())
                    .bounds(this.width - 70, 10, 60, 20).build());
            this.listWidget = new SelectionList(this.minecraft, this.width, this.height, 40, this.height - 10, 36);
            this.addWidget(this.listWidget);
            updateSearch("");
        }

        @Override public void tick() {
            if ("structures".equals(type) && TipCache.ALL_STRUCTURES.size() > lastListSize) {
                loadData(); updateSearch(searchBox.getValue());
            }
        }

        private void loadData() {
            this.allEntries.clear();
            Minecraft mc = Minecraft.getInstance(); if (mc.level == null) return;
            try {
                switch (type) {
                    case "structures" -> {
                        List<ResourceLocation> src = !TipCache.ALL_STRUCTURES.isEmpty() ? new ArrayList<>(TipCache.ALL_STRUCTURES) : TipsUtils.getRegistryKeys(Registries.STRUCTURE);
                        lastListSize = src.size();
                        for (ResourceLocation k : src) allEntries.add(new RegistryEntry(k.toString(), TipsUtils.getPrettyName("structure", k), TipsUtils.getModName(k.getNamespace())));
                    }
                    case "biomes" -> {
                        for (ResourceLocation k : mc.level.registryAccess().registryOrThrow(Registries.BIOME).keySet())
                            allEntries.add(new RegistryEntry(k.toString(), TipsUtils.getPrettyName("biome", k), TipsUtils.getModName(k.getNamespace())));
                    }
                    case "advancements" -> {
                        if (mc.getConnection() != null) mc.getConnection().getAdvancements().getAdvancements().getAllAdvancements().forEach(adv -> {
                            if (adv.getDisplay() != null) allEntries.add(new RegistryEntry(adv.getId().toString(), adv.getDisplay().getTitle().getString(), TipsUtils.getModName(adv.getId().getNamespace())));
                        });
                    }
                    case "dimensions" -> {
                        for (ResourceLocation k : mc.level.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE).keySet())
                            allEntries.add(new RegistryEntry(k.toString(), k.getPath(), "Dimension"));
                    }
                }
                allEntries.sort(Comparator.comparing((RegistryEntry e) -> e.source).thenComparing(e -> e.id));
            } catch (Exception ignored) {}
        }

        private void updateSearch(String q) {
            String query = q.toLowerCase(Locale.ROOT).trim();
            List<RegistryEntry> filtered = allEntries.stream().filter(e -> query.isEmpty() || e.id.toLowerCase().contains(query) || e.name.toLowerCase().contains(query) || e.source.toLowerCase().contains(query)).toList();
            if (listWidget != null) listWidget.refreshList(filtered);
        }

        @Override public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
            this.renderBackground(g);
            if (listWidget != null) listWidget.render(g, mx, my, pt);
            g.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
            if (searchBox != null && searchBox.getValue().isEmpty()) {
                g.drawString(this.font, Component.translatable("gui.adventuresystems.tips.tips.search"), searchBox.getX() + 4, searchBox.getY() + 6, 0x777777, false);
            }
            super.render(g, mx, my, pt);
        }

        record RegistryEntry(String id, String name, String source) {}

        class SelectionList extends KineticWidgets.SmoothSelectionList<SelectionEntry> {
            public SelectionList(Minecraft mc, int w, int h, int t, int b, int ih) { super(mc, w, h, t, b, ih); }
            public void refreshList(List<RegistryEntry> entries) { this.clearEntries(); entries.forEach(e -> this.addEntry(new SelectionEntry(e))); this.snapScrollAmount(0); }
            @Override public int getRowWidth() { return this.width - 40; }
            @Override protected int getScrollbarPosition() { return this.width - 10; }
        }

        class SelectionEntry extends net.minecraft.client.gui.components.ObjectSelectionList.Entry<SelectionEntry> {
            private final RegistryEntry data;
            public SelectionEntry(RegistryEntry d) { this.data = d; }
            @Override public void render(@NotNull GuiGraphics g, int i, int t, int l, int w, int h, int mx, int my, boolean hv, float pt) {
                if (hv) g.fill(l, t, l + w, t + h, 0x22FFFFFF);
                g.drawString(Minecraft.getInstance().font, data.name, l + 5, t + 5, 0xFFFF55);
                g.drawString(Minecraft.getInstance().font, data.source + " | " + data.id, l + 5, t + 18, 0xAAAAAA);
            }
            @Override public boolean mouseClicked(double mx, double my, int b) { if (b == 0) { onSelect.accept(data.id);
                if (minecraft != null) {
                    minecraft.setScreen(parent);
                }
                return true; } return false; }
            @Override public @NotNull Component getNarration() { return Component.literal(data.name); }
        }
    }
}
