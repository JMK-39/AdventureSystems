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

    // A compact dialog: Back, the title and Save on one row, the seconds field below it (the hint is its tooltip).
    private static final int W = 220, H = 48, FIELD_W = 80;

    @Override
    protected void build(KineticUi ui) {
        int px = (width() - W) / 2;
        int py = (height() - H) / 2;
        this.input = ui.numberField(px + (W - FIELD_W) / 2, py + 28, FIELD_W, NumberType.DECIMAL)
                .label(AdventureText.translatable("gui.adventuresystems.tips.tips.label"))
                .tooltip(AdventureText.translatable("gui.adventuresystems.tips.tips.hint"))
                .allowNegative(false).range(0.25D, 3600.0D)
                .validator(value -> true)
                .onChange(value -> inputValue = value).firstShownTextAsDefault().build();
        this.input.setTextValue(inputValue);
        ui.button(px + 4, py + 4, 40)
                .text(AdventureText.translatable("gui.adventuresystems.tips.tips.cancel"))
                .onClick(this::closeToParent).build();
        ui.button(px + W - 44, py + 4, 40)
                .text(AdventureText.translatable("gui.adventuresystems.tips.tips.save"))
                .onClick(this::save).build();
    }

    private void save() {
        Double seconds = input == null ? null : input.getDoubleValue();
        if (seconds == null || !Double.isFinite(seconds)) {
            if (input != null) input.flashValidationError();
            return;
        }
        int ms = (int) Math.round(seconds * 1000.0D);
        onSave.accept(ms);
        navigateBack();
    }

    @Override
    protected void renderBackground(KineticGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int px = (width() - W) / 2;
        int py = (height() - H) / 2;
        KineticTheme.panel(graphics, px, py, W, H);
        graphics.scrollingTextCentered(title(), width() / 2, py + 8, W - 2 * 48, KineticTheme.current().text(), false);
        KineticTheme.separator(graphics, px + 4, py + 24, W - 8);
        graphics.text(AdventureText.translatable("gui.adventuresystems.tips.tips.seconds"), px + (W + FIELD_W) / 2 + 4, py + 32,
                KineticTheme.current().mutedText(), false);
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
