package dev.xyat.adventuresystems.curios.wallet.client.gui;

import dev.xyat.adventuresystems.curios.wallet.data.CurrencyType;
import dev.xyat.adventuresystems.curios.wallet.data.Data;
import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.widget.list.ItemActionItem;
import dev.xyat.kineticcore.api.client.gui.widget.list.KineticItemActionList;
import dev.xyat.kineticcore.api.text.KineticI18n;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class CurrencyPickerScreen extends KineticPage {
    private final ShopGuiSupport.EditorDraft draft;
    private final List<CurrencyType> currencies;
    private KineticItemActionList list;
    private int scrollOffset;

    CurrencyPickerScreen(ShopGuiSupport.EditorDraft draft) {
        super(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_currency_picker_title"));
        this.draft = draft;
        this.currencies = new ArrayList<>(Data.currencies());
        this.currencies.sort(Comparator.comparingLong(CurrencyType::value));
        useCanvas(360, 240, 6);
    }

    @Override
    protected void build(KineticUi ui) {
        if (list != null) scrollOffset = list.scrollOffset();
        ui.button(140, 204, 80)
                .text(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_cancel"))
                .onClick(this::navigateBack).build();
        List<ItemActionItem> items = new ArrayList<>(currencies.size());
        for (CurrencyType currency : currencies) {
            Component name = ShopGuiSupport.stackNameComponent(currency.itemId());
            Component tooltip = KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_tooltip_currency_value",
                    ShopGuiSupport.formatExact(currency.value()));
            items.add(new ItemActionItem(ShopGuiSupport.stack(currency.itemId()), name, null, tooltip,
                    true, false,
                    KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_currency_picker_select_tip"),
                    tooltip, true));
        }
        list = ui.itemActionList(30, 45, 300, 150, items)
                .actionWidth(68).scrollOffset(scrollOffset)
                .onSelect(this::selectCurrency).onAction(this::selectCurrency).build();
    }

    private void selectCurrency(int index) {
        if (index < 0 || index >= currencies.size()) return;
        draft.currencyId = currencies.get(index).itemId();
        navigateBack();
    }

    @Override
    protected void renderBackground(KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        KineticTheme.shadow(graphics, width(), height());
        KineticTheme.panel(graphics, 20, 20, 320, 200);
        graphics.centeredText(title(), 180, 28, KineticTheme.current().text(), false);
        if (currencies.isEmpty()) {
            graphics.centeredText(KineticI18n.translatable("gui.adventuresystems.curios.wallet.shop_currency_picker_empty"),
                    180, 104, KineticTheme.current().mutedText(), false);
        }
    }
}
