package dev.xyat.adventuresystems.ftb.mixin.client;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.gui.quests.ValidItemsScreen;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.xyat.adventuresystems.ftb.api.FTBTaskSubmitHelper;
import dev.xyat.adventuresystems.ftb.client.gui.FTBSubmitCountScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = ValidItemsScreen.class, remap = false)
public abstract class ValidItemsScreenSubmitLimitMixin extends BaseScreen {
    @Unique
    private ItemTask adventuresystems_ftb$submitTask;

    @ModifyVariable(method = "<init>", at = @At("TAIL"), argsOnly = true, ordinal = 0)
    public ItemTask adventuresystems_ftb$captureSubmitTask(ItemTask task) {
        this.adventuresystems_ftb$submitTask = task;
        return task;
    }

    @Inject(method = "addWidgets", at = @At("TAIL"))
    public void adventuresystems_ftb$replaceSubmitButton(CallbackInfo ignored) {
        if (!FTBTaskSubmitHelper.isCustomSubmitAllowed(this.adventuresystems_ftb$submitTask)) {
            return;
        }

        List<Widget> widgets = this.getWidgets();
        if (widgets.size() < 3) {
            return;
        }

        int submitButtonIndex = 2;
        if (!(widgets.get(submitButtonIndex) instanceof Button originalButton)) {
            return;
        }

        SimpleTextButton replacementButton = new SimpleTextButton(
                this,
                Component.translatable("button.adventuresystems.ftb.submit.confirm"),
                Color4I.empty()
        ) {
            private void adventuresystems_ftb$syncBounds() {
                this.posX = originalButton.posX;
                this.posY = originalButton.posY;
                this.width = originalButton.width;
                this.height = originalButton.height;
            }

            @Override
            public int getX() {
                this.adventuresystems_ftb$syncBounds();
                return super.getX();
            }

            @Override
            public int getY() {
                this.adventuresystems_ftb$syncBounds();
                return super.getY();
            }

            @Override
            public void onClicked(MouseButton button) {
                if (!FTBTaskSubmitHelper.isCustomSubmitAllowed(adventuresystems_ftb$submitTask)) {
                    originalButton.onClicked(button);
                    return;
                }

                this.playClickSound();
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.setScreen(new FTBSubmitCountScreen(minecraft.screen, adventuresystems_ftb$submitTask));
            }

            @Override
            public void addMouseOverText(TooltipList list) {
                list.add(Component.translatable("tip.adventuresystems.ftb.ftb.submit.button"));
            }

            @Override
            public boolean renderTitleInCenter() {
                return true;
            }
        };

        widgets.set(submitButtonIndex, replacementButton);
    }
}
