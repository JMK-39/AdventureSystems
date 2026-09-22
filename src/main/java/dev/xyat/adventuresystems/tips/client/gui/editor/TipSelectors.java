package dev.xyat.adventuresystems.tips.client.gui.editor;

import dev.xyat.adventuresystems.tips.TipsNetwork;
import dev.xyat.adventuresystems.tips.TipsUtils;
import dev.xyat.adventuresystems.tips.client.TipCache;
import dev.xyat.kineticcore.api.client.advancement.KineticClientAdvancements;
import dev.xyat.kineticcore.api.client.screen.KineticNativeScreen;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.widget.input.KineticTextFields.KineticEditBox;
import dev.xyat.kineticcore.api.client.widget.selection.KineticTabs.SelectionItem;
import dev.xyat.kineticcore.api.client.widget.selection.KineticTabs.ScrollableSelectionList;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import dev.xyat.kineticcore.api.text.KineticI18n;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * 提示系统选择器界面合集
 */
public class TipSelectors {

    public static class RegistrySelectorScreen extends KineticNativeScreen {
        private final String type;
        private final Consumer<String> onSelect;
        private final List<RegistryEntry> allEntries = new ArrayList<>();
        private KineticEditBox searchBox;
        private ScrollableSelectionList listWidget;
        private List<RegistryEntry> displayEntries = List.of();
        private int lastListSize;

        public RegistrySelectorScreen(Screen parent, String type, Consumer<String> onSelect) {
            super(KineticI18n.translatable("gui.adventuresystems.tips.tips.setstitle", type));
            setParentScreen(parent);
            this.type = type;
            this.onSelect = onSelect;
        }

        @Override
        protected void buildUi() {
            if ("structures".equals(type)) {
                TipsNetwork.sendToServer(new TipsNetwork.RequestStructure(true));
            }
            loadData();
            this.searchBox = addTextField(
                    20,
                    10,
                    this.width - 100,
                    Component.empty(),
                    KineticI18n.translatable("gui.adventuresystems.tips.tips.search"),
                    null,
                    null
            );
            this.searchBox.setResponder(this::updateSearch);

            addButton(
                    this.width - 70,
                    10,
                    60,
                    KineticI18n.translatable("gui.adventuresystems.tips.tips.cancel"),
                    null,
                    this::closeToParent
            );

            this.listWidget = addScrollableSelectionList(
                    20,
                    40,
                    this.width - 40,
                    this.height - 50,
                    List.of(),
                    -1,
                    0,
                    index -> {
                        if (index < 0 || index >= displayEntries.size()) return;
                        onSelect.accept(displayEntries.get(index).id);
                        navigateBack();
                    }
            );
            updateSearch("");
        }

        @Override
        protected void nativeTick() {
            if ("structures".equals(type) && TipCache.ALL_STRUCTURES.size() > lastListSize) {
                loadData();
                updateSearch(searchBox == null ? "" : searchBox.getValue());
            }
        }

        private void loadData() {
            this.allEntries.clear();
            if (KineticClientRuntime.currentLevel() == null) return;
            try {
                switch (type) {
                    case "structures" -> {
                        List<ResourceLocation> source = !TipCache.ALL_STRUCTURES.isEmpty()
                                ? new ArrayList<>(TipCache.ALL_STRUCTURES)
                                : TipsUtils.getRegistryKeys(Registries.STRUCTURE);
                        lastListSize = source.size();
                        for (ResourceLocation key : source) {
                            allEntries.add(new RegistryEntry(
                                    key.toString(),
                                    TipsUtils.getPrettyName("structure", key),
                                    TipsUtils.getModName(key.getNamespace())
                            ));
                        }
                    }
                    case "biomes" -> {
                        for (ResourceLocation key : TipsUtils.getRegistryKeys(Registries.BIOME)) {
                            allEntries.add(new RegistryEntry(
                                    key.toString(),
                                    TipsUtils.getPrettyName("biome", key),
                                    TipsUtils.getModName(key.getNamespace())
                            ));
                        }
                    }
                    case "advancements" -> KineticClientAdvancements.all().forEach(advancement -> {
                        if (advancement.getDisplay() == null) return;
                        ResourceLocation id = advancement.getId();
                        allEntries.add(new RegistryEntry(
                                id.toString(),
                                advancement.getDisplay().getTitle().getString(),
                                TipsUtils.getModName(id.getNamespace())
                        ));
                    });
                    case "dimensions" -> {
                        for (var levelKey : KineticClientRuntime.knownLevels()) {
                            ResourceLocation key = levelKey.location();
                            allEntries.add(new RegistryEntry(key.toString(), key.getPath(), "Dimension"));
                        }
                    }
                    default -> {
                    }
                }
                allEntries.sort(Comparator.comparing((RegistryEntry entry) -> entry.source).thenComparing(entry -> entry.id));
            } catch (RuntimeException ignored) {
            }
        }

        private void updateSearch(String queryText) {
            String query = queryText == null ? "" : queryText.toLowerCase(Locale.ROOT).trim();
            displayEntries = allEntries.stream()
                    .filter(entry -> query.isEmpty()
                            || entry.id.toLowerCase(Locale.ROOT).contains(query)
                            || entry.name.toLowerCase(Locale.ROOT).contains(query)
                            || entry.source.toLowerCase(Locale.ROOT).contains(query))
                    .toList();
            if (listWidget != null) {
                listWidget.setItems(displayEntries.stream()
                        .map(entry -> new SelectionItem(
                                KineticI18n.translatable("gui.adventuresystems.tips.tips.selector.name", entry.name),
                                KineticI18n.translatable("gui.adventuresystems.tips.tips.selector.meta", entry.source, entry.id),
                                null,
                                true,
                                false,
                                false
                        ))
                        .toList());
                listWidget.setSelectedIndex(-1);
                listWidget.setScrollOffset(0);
            }
        }

        @Override
        protected void renderNativeBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, GuiTheme.current().text());
        }

        @Override
        protected boolean handleCloseRequest() {
            closeToParent();
            return true;
        }

        private void closeToParent() {
            navigateBack();
        }

        record RegistryEntry(String id, String name, String source) {
        }

    }
}
