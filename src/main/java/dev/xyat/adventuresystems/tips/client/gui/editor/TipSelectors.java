package dev.xyat.adventuresystems.tips.client.gui.editor;

import dev.xyat.adventuresystems.tips.TipsNetwork;
import dev.xyat.adventuresystems.tips.TipsUtils;
import dev.xyat.adventuresystems.tips.client.TipCache;
import dev.xyat.kineticcore.api.client.advancement.KineticClientAdvancements;
import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.page.PageLayout;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.widget.list.KineticSelectionList;
import dev.xyat.kineticcore.api.client.gui.widget.list.SelectionItem;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import dev.xyat.kineticcore.api.text.KineticI18n;
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

    public static class RegistrySelectorScreen extends KineticPage {
        private final String type;
        private final Consumer<String> onSelect;
        private final List<RegistryEntry> allEntries = new ArrayList<>();
        private KineticSelectionList listWidget;
        private String searchText = "";
        private int listScroll;
        private List<RegistryEntry> displayEntries = List.of();
        private int lastListSize;

        public RegistrySelectorScreen(String type, Consumer<String> onSelect) {
            super(KineticI18n.translatable("gui.adventuresystems.tips.tips.setstitle", type), PageLayout.NATIVE);
            this.type = type;
            this.onSelect = onSelect;
        }

        @Override
        protected void build(KineticUi ui) {
            if (listWidget != null) listScroll = listWidget.scrollOffset();
            listWidget = null;
            if ("structures".equals(type)) {
                TipsNetwork.sendToServer(new TipsNetwork.RequestStructure(true));
            }
            loadData();
            updateSearch(searchText);
            ui.textField(20, 10, width() - 100)
                    .label(Component.empty())
                    .placeholder(KineticI18n.translatable("gui.adventuresystems.tips.tips.search"))
                    .value(searchText)
                    .onChange(value -> {
                        searchText = value;
                        updateSearch(value);
                    }).firstShownTextAsDefault().build();

            ui.button(width() - 70, 10, 60)
                    .text(KineticI18n.translatable("gui.adventuresystems.tips.tips.cancel"))
                    .onClick(this::closeToParent).build();

            this.listWidget = ui.selectionList(20, 40, width() - 40, height() - 50,
                    selectionItems())
                    .selected(-1).scrollOffset(listScroll)
                    .onSelect(index -> {
                        if (index < 0 || index >= displayEntries.size()) return;
                        onSelect.accept(displayEntries.get(index).id);
                        navigateBack();
                    }).build();
        }

        @Override
        protected void onTick() {
            if ("structures".equals(type) && TipCache.ALL_STRUCTURES.size() > lastListSize) {
                loadData();
                updateSearch(searchText);
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
                listWidget.setItems(selectionItems());
                listWidget.setSelectedIndex(-1);
                listWidget.setScrollOffset(0);
            }
        }

        private List<SelectionItem> selectionItems() {
            return displayEntries.stream()
                    .map(entry -> new SelectionItem(
                            KineticI18n.translatable("gui.adventuresystems.tips.tips.selector.name", entry.name),
                            KineticI18n.translatable("gui.adventuresystems.tips.tips.selector.meta", entry.source, entry.id),
                            null, true, false))
                    .toList();
        }

        @Override
        protected void renderBackground(KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.centeredText(title(), width() / 2, 15, KineticTheme.current().text(), true);
        }

        @Override
        protected boolean onCloseRequested() {
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
