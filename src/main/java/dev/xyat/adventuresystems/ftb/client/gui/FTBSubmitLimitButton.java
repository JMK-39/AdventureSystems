//? if >=1.21 {
/*package dev.xyat.adventuresystems.ftb.client.gui;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.xyat.adventuresystems.ftb.api.FTBTaskSubmitHelper;
import dev.xyat.adventuresystems.text.AdventureText;
import dev.xyat.kineticcore.api.client.gui.KineticGui;

/^* Kept outside the mixin package: a widget must never load or reference the mixin class at runtime. *^/
public final class FTBSubmitLimitButton extends SimpleTextButton {
    private final Button original;
    private final ItemTask task;

    public FTBSubmitLimitButton(Panel parent, Button original, ItemTask task) {
        super(parent, AdventureText.translatable("button.adventuresystems.ftb.submit.confirm"), Color4I.empty());
        this.original = original;
        this.task = task;
    }

    private void syncBounds() {
        posX = original.posX;
        posY = original.posY;
        width = original.width;
        height = original.height;
    }

    @Override public int getX() { syncBounds(); return super.getX(); }
    @Override public int getY() { syncBounds(); return super.getY(); }

    @Override public void onClicked(MouseButton button) {
        if (!FTBTaskSubmitHelper.isCustomSubmitAllowed(task)) {
            original.onClicked(button);
            return;
        }
        playClickSound();
        KineticGui.openChild(new FTBSubmitCountScreen(task));
    }

    @Override public void addMouseOverText(TooltipList list) {
        list.add(AdventureText.translatable("tip.adventuresystems.ftb.submit.button"));
    }

    @Override public boolean renderTitleInCenter() { return true; }
}
*///?}
