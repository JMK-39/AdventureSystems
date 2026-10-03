package dev.xyat.adventuresystems.text;

import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Parsed templates contain styles and argument indexes, never resolved language or argument text. */
final class AdventureTextTemplates {
    private static final int CACHE_LIMIT = 512;
    private static Language cachedLanguage;
    private static final Map<String, List<Part>> CACHE = new LinkedHashMap<>(16, 0.75F, true);

    private AdventureTextTemplates() {}

    static boolean decompose(String key, String template, Object[] args, Consumer<FormattedText> output) {
        if (!(key.startsWith("adventuresystems.") || key.contains(".adventuresystems.")
                || key.endsWith(".adventuresystems"))) return false;
        if (template.indexOf('\u00a7') < 0) return false;
        List<Part> parts = parts(template);
        if (parts == null) return false; // Let vanilla retain its invalid-format fallback.
        for (Part part : parts) if (part.argument >= args.length) return false;
        for (Part part : parts) {
            MutableComponent value;
            if (part.argument < 0) {
                value = Component.literal(part.text);
            } else {
                Object argument = args[part.argument];
                value = argument instanceof Component component
                        ? AdventureText.component(component) : AdventureText.literal(String.valueOf(argument));
            }
            output.accept(value.setStyle(value.getStyle().applyTo(part.style)));
        }
        return true;
    }

    private static synchronized List<Part> parts(String template) {
        Language language = Language.getInstance();
        if (language != cachedLanguage) {
            CACHE.clear();
            cachedLanguage = language;
        }
        List<Part> cached = CACHE.get(template);
        if (cached != null) return cached;
        List<Part> parsed = parse(template);
        if (parsed != null) {
            CACHE.put(template, parsed);
            if (CACHE.size() > CACHE_LIMIT) CACHE.remove(CACHE.keySet().iterator().next());
        }
        return parsed;
    }

    private static List<Part> parse(String template) {
        List<Part> parts = new ArrayList<>();
        Style style = Style.EMPTY;
        StringBuilder text = new StringBuilder();
        int sequentialArgument = 0;
        for (int index = 0; index < template.length(); index++) {
            char character = template.charAt(index);
            ChatFormatting format = character == '\u00a7' && index + 1 < template.length()
                    ? ChatFormatting.getByCode(Character.toLowerCase(template.charAt(index + 1))) : null;
            if (format != null) {
                flush(parts, text, style);
                style = format == ChatFormatting.RESET ? Style.EMPTY
                        : format.isColor() ? resetStyle().applyFormat(format) : style.applyFormat(format);
                index++;
            } else if (character == '%') {
                if (index + 1 < template.length() && template.charAt(index + 1) == '%') {
                    text.append('%');
                    index++;
                    continue;
                }
                int end = index + 1;
                while (end < template.length() && template.charAt(end) >= '0' && template.charAt(end) <= '9') end++;
                int argument;
                if (end == index + 1 && end < template.length() && template.charAt(end) == 's') {
                    argument = sequentialArgument++;
                } else if (end > index + 1 && end + 1 < template.length()
                        && template.charAt(end) == '$' && template.charAt(end + 1) == 's') {
                    try { argument = Integer.parseInt(template.substring(index + 1, end)) - 1; }
                    catch (NumberFormatException exception) { return null; }
                    if (argument < 0) return null;
                    end++;
                } else {
                    return null;
                }
                flush(parts, text, style);
                parts.add(new Part(null, argument, style));
                index = end;
            } else {
                text.append(character);
            }
        }
        flush(parts, text, style);
        return List.copyOf(parts);
    }

    private static void flush(List<Part> parts, StringBuilder text, Style style) {
        if (text.isEmpty()) return;
        parts.add(new Part(text.toString(), -1, style));
        text.setLength(0);
    }

    private static Style resetStyle() {
        return Style.EMPTY.withBold(false).withItalic(false).withUnderlined(false)
                .withStrikethrough(false).withObfuscated(false);
    }

    private record Part(String text, int argument, Style style) {}
}
