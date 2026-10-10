package dev.xyat.adventuresystems.tips.client.gui.editor;

import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.page.PageLayout;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.ui.NumberType;
import dev.xyat.kineticcore.api.client.gui.widget.KineticNumberField;
import dev.xyat.adventuresystems.text.AdventureText;

import java.util.function.Consumer;

public class TimeEditScreen extends KineticPage {
    private final Consumer<Integer> onSave;
    private KineticNumberField input;
    private String inputValue;

    public TimeEditScreen(int currentTimeMs, Consumer<Integer> onSave) {
        super(AdventureText.translatable("gui.adventuresystems.tips.tips.time"), PageLayout.NATIVE);
        this.onSave = onSave;
        this.inputValue = NumberType.DECIMAL.format(currentTimeMs / 1000.0D);
    }

    @Override
    protected void build(KineticUi ui) {
        int centerX = width() / 2;
        int centerY = height() / 2;

        this.input = ui.numberField(centerX - 100, centerY - 10, 200, NumberType.DECIMAL)
                .label(AdventureText.translatable("gui.adventuresystems.tips.tips.label"))
                .allowNegative(false).range(0.25D, 3600.0D)
                .validator(value -> true)
                .onChange(value -> inputValue = value).firstShownTextAsDefault().build();
        this.input.setTextValue(inputValue);

        ui.button(centerX - 105, centerY + 20, 100)
                .text(AdventureText.translatable("gui.adventuresystems.tips.tips.save"))
                .onClick(this::save).build();
        ui.button(centerX - 110, centerY - 42, 100)
                .text(AdventureText.translatable("gui.adventuresystems.tips.tips.cancel"))
                .onClick(this::closeToParent).build();
    }

    private void save() {
        Double seconds = input == null ? null : input.getDoubleValue();
        if (seconds == null || !Double.isFinite(seconds)) {
            if (input != null) input.flashValidationError();
            return;
        }
        int ms = (int) Math.round(seconds * 1000.0D);
        onSave.accept(ms);
    }

    @Override
    protected void renderBackground(KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int w = 240;
        int h = 100;
        int px = (width() - w) / 2;
        int py = (height() - h) / 2;
        KineticTheme.panel(graphics, px, py, w, h);

        int titleLeft = px + 10 + 100 + 2;
        int titleRight = px + w - 12;
        graphics.scrollingTextCentered(title(), (titleLeft + titleRight) / 2, py + 15,
                titleRight - titleLeft, KineticTheme.current().text(), true);
        graphics.scrollingTextCentered(AdventureText.translatable("gui.adventuresystems.tips.tips.hint"), width() / 2, py + 30, w - 24, KineticTheme.current().text(), true);
    }

    @Override
    protected boolean onCloseRequested() {
        closeToParent();
        return true;
    }

    private void closeToParent() {
        navigateBack();
    }
}
