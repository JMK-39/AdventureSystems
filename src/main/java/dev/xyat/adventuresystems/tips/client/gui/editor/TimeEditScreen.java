package dev.xyat.adventuresystems.tips.client.gui.editor;

import dev.xyat.kineticcore.api.client.theme.GuiTheme;
import dev.xyat.kineticcore.api.client.screen.KineticNativeScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class TimeEditScreen extends KineticNativeScreen {
    private final Screen parent;
    private final int currentTimeMs;
    private final Consumer<Integer> onSave;
    private EditBox input;

    public TimeEditScreen(Screen parent, int currentTimeMs, Consumer<Integer> onSave) {
        super(Component.translatable("gui.adventuresystems.tips.tips.time"));
        this.parent = parent;
        this.currentTimeMs = currentTimeMs;
        this.onSave = onSave;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.input = new EditBox(this.font, centerX - 100, centerY - 10, 200, 20, Component.translatable("gui.adventuresystems.tips.tips.label"));
        this.input.setValue(String.format("%.1f", currentTimeMs / 1000.0f));
        this.addRenderableWidget(input);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.adventuresystems.tips.tips.save"), b -> save())
                .bounds(centerX - 105, centerY + 20, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.adventuresystems.tips.tips.cancel"), b -> onClose())
                .bounds(centerX + 5, centerY + 20, 100, 20).build());
    }

    private void save() {
        try {
            double seconds = Double.parseDouble(input.getValue());
            if (!Double.isFinite(seconds) || seconds < 0.25D || seconds > 3600.0D) {
                input.setTextColor(0xFF0000);
                return;
            }
            int ms = (int) Math.round(seconds * 1000.0D);
            onSave.accept(ms);
            if (this.minecraft != null) this.minecraft.setScreen(parent);
        } catch (NumberFormatException e) {
            input.setTextColor(0xFF0000); // 错误变红
        }
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        // 使用KT的全局半透阴影与标准底板绘制框架
        GuiTheme.shadow(g, this.width, this.height);

        int w = 240, h = 100;
        int px = (this.width - w) / 2;
        int py = (this.height - h) / 2;
        GuiTheme.panel(g, px, py, w, h);

        g.drawCenteredString(this.font, this.title, this.width / 2, py + 15, 0xFFFFFF);
        g.drawCenteredString(this.font, Component.translatable("gui.adventuresystems.tips.tips.hint"), this.width / 2, py + 30, 0xAAAAAA);

        super.render(g, mx, my, pt);
    }
}
