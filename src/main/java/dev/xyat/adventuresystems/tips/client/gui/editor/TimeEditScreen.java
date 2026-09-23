package dev.xyat.adventuresystems.tips.client.gui.editor;

import dev.xyat.kineticcore.api.client.screen.KineticNativeScreen;
import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.widget.input.KineticNumericFields.NumericEditBox;
import dev.xyat.kineticcore.api.text.KineticI18n;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Consumer;

public class TimeEditScreen extends KineticNativeScreen {
    private final int currentTimeMs;
    private final Consumer<Integer> onSave;
    private NumericEditBox input;

    public TimeEditScreen(Screen parent, int currentTimeMs, Consumer<Integer> onSave) {
        super(KineticI18n.translatable("gui.adventuresystems.tips.tips.time"));
        setParentScreen(parent);
        this.currentTimeMs = currentTimeMs;
        this.onSave = onSave;
    }

    @Override
    protected void buildUi() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.input = addDecimalField(
                centerX - 100,
                centerY - 10,
                200,
                KineticI18n.translatable("gui.adventuresystems.tips.tips.label"),
                false,
                0.25D,
                3600.0D,
                value -> true
        );
        this.input.setDoubleValue(currentTimeMs / 1000.0D);

        addButton(
                centerX - 105,
                centerY + 20,
                100,
                KineticI18n.translatable("gui.adventuresystems.tips.tips.save"),
                null,
                this::save
        );
        addButton(
                centerX + 5,
                centerY + 20,
                100,
                KineticI18n.translatable("gui.adventuresystems.tips.tips.cancel"),
                null,
                this::closeToParent
        );
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
    protected void renderNativeBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int w = 240;
        int h = 100;
        int px = (this.width - w) / 2;
        int py = (this.height - h) / 2;
        GuiTheme.panel(graphics, px, py, w, h);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, py + 15, GuiTheme.current().text());
        graphics.drawCenteredString(
                this.font,
                KineticI18n.translatable("gui.adventuresystems.tips.tips.hint"),
                this.width / 2,
                py + 30,
                GuiTheme.current().text()
        );
    }

    @Override
    protected boolean handleCloseRequest() {
        closeToParent();
        return true;
    }

    private void closeToParent() {
        navigateBack();
    }
}
