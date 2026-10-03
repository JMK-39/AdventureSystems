package dev.xyat.adventuresystems.text.mixin;

import dev.xyat.adventuresystems.text.AdventureText;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/** Resolve legacy styles from the authored key's current template during vanilla visitation. */
@Mixin(TranslatableContents.class)
public abstract class TranslatableContentsMixin {
    @Shadow @Final private String key;
    @Shadow @Final private Object[] args;

    @Inject(method = "decomposeTemplate", at = @At("HEAD"), cancellable = true)
    private void adventuresystems$overrideTranslation(String template, Consumer<FormattedText> output, CallbackInfo callback) {
        if (AdventureText.decomposeTemplate(key, template, args, output)) callback.cancel();
    }
}
