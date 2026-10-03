package dev.xyat.adventuresystems.text;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
//? if >=1.21 {
/*import net.minecraft.network.chat.contents.PlainTextContents;
*///?} else {
import net.minecraft.network.chat.contents.LiteralContents;
//?}
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.FormattedCharSequence;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

/**
 * Language-file colors as native component styles, including text on either side of arguments.
 * Authored keys stay translatable: network serialization and language changes remain native.
 */
public final class AdventureText {
    private AdventureText() {}

    /** Keep the authored public key and arguments in vanilla's serialized component. */
    public static MutableComponent translatable(String key, Object... args) {
        return Component.translatable(key, normalizedArguments(args));
    }

    private static Object[] normalizedArguments(Object[] args) {
        Object[] values = args == null ? new Object[0] : args.clone();
        for (int index = 0; index < values.length; index++) {
            if (values[index] instanceof Component existing) values[index] = component(existing);
            else if (values[index] instanceof String text && text.indexOf('\u00a7') >= 0) values[index] = literal(text);
        }
        return values;
    }

    /** Called by the common translation mixin with vanilla's current-language template. */
    public static boolean decomposeTemplate(String key, String template, Object[] args,
                                            Consumer<FormattedText> output) {
        return AdventureTextTemplates.decompose(key, template, args, output);
    }

    /** Normalize legacy literal arguments and addon item names while retaining translation and metadata. */
    public static MutableComponent component(Component value) {
        MutableComponent result;
        //? if >=1.21 {
        /*if (value.getContents() instanceof PlainTextContents contents) {
        *///?} else {
        if (value.getContents() instanceof LiteralContents contents) {
        //?}
            result = literal(contents.text());
        } else if (value.getContents() instanceof TranslatableContents contents) {
            result = MutableComponent.create(new TranslatableContents(contents.getKey(), contents.getFallback(),
                    normalizedArguments(contents.getArgs())));
        } else {
            result = MutableComponent.create(value.getContents());
        }
        result.setStyle(value.getStyle());
        for (Component sibling : value.getSiblings()) result.append(component(sibling));
        return result;
    }

    /** Convert user-provided legacy text without flattening existing translated components. */
    public static MutableComponent literal(String text) {
        MutableComponent result = Component.empty();
        Style style = Style.EMPTY;
        StringBuilder buffer = new StringBuilder();
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character == '§' && index + 1 < text.length()) {
                ChatFormatting format = ChatFormatting.getByCode(Character.toLowerCase(text.charAt(index + 1)));
                if (format != null) {
                    if (!buffer.isEmpty()) {
                        result.append(Component.literal(buffer.toString()).setStyle(style));
                        buffer.setLength(0);
                    }
                    style = format == ChatFormatting.RESET ? Style.EMPTY
                            : format.isColor() ? resetStyle().applyFormat(format) : style.applyFormat(format);
                    index++;
                    continue;
                }
            }
            buffer.append(character);
        }
        if (!buffer.isEmpty()) result.append(Component.literal(buffer.toString()).setStyle(style));
        return result;
    }

    /** Trim by the caller's font metrics, preserving styles and complete Unicode code points. */
    public static MutableComponent ellipsize(Component text, int maxWidth, ToIntFunction<Component> width) {
        if (maxWidth <= 0) return Component.empty();
        if (width.applyAsInt(text) <= maxWidth) return text.copy();
        MutableComponent suffix = Component.literal("...");
        while (width.applyAsInt(suffix) > maxWidth && !suffix.getString().isEmpty()) {
            suffix = Component.literal(suffix.getString().substring(1));
        }
        int available = maxWidth - width.applyAsInt(suffix);
        MutableComponent prefix = Component.empty();
        text.visit((style, value) -> {
            for (int offset = 0; offset < value.length();) {
                int codePoint = value.codePointAt(offset);
                MutableComponent character = Component.literal(new String(Character.toChars(codePoint))).setStyle(style);
                if (width.applyAsInt(prefix.copy().append(character)) > available) return Optional.of(Boolean.TRUE);
                prefix.append(character);
                offset += Character.charCount(codePoint);
            }
            return Optional.empty();
        }, Style.EMPTY);
        return prefix.append(suffix);
    }

    /** Keep font-wrapped tooltip lines as components with their rendered styles. */
    public static MutableComponent fromSequence(FormattedCharSequence sequence) {
        MutableComponent result = Component.empty();
        sequence.accept((index, style, codePoint) -> {
            result.append(Component.literal(new String(Character.toChars(codePoint))).setStyle(style));
            return true;
        });
        return result;
    }

    private static Style resetStyle() {
        return Style.EMPTY.withBold(false).withItalic(false).withUnderlined(false)
                .withStrikethrough(false).withObfuscated(false);
    }

}
