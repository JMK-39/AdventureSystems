package dev.xyat.adventuresystems.text;

import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/** Standalone regression checks using Minecraft's real component visitor. */
public final class AdventureTextTest {
    public static void main(String[] args) {
        //? if >=1.21 {
        /*if (net.neoforged.fml.loading.LoadingModList.get() == null) {
            net.neoforged.fml.loading.LoadingModList.of(List.of(), List.of(), List.of(), List.of(), Map.of());
        }
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        *///?}
        language("en_us");
        if (args.length > 0 && args[0].equals("--authored-languages")) {
            verifyAuthoredLanguages();
            return;
        }
        Component text = AdventureText.translatable(
                "tip.adventuresystems.curios.heart_of_steel.health_bonus", "+10.50", "5.0", "5.50", "100");
        if (args.length > 0 && args[0].equals("--base-key-wire")) {
            check(((TranslatableContents) text.getContents()).getKey().equals(
                    "tip.adventuresystems.curios.heart_of_steel.health_bonus"),
                    "Message must serialize the authored base key, not a generated formatting key");
            return;
        }
        verifyAuthoredLanguages();
        check("applied".equals(System.getProperty("adventure.text.testAgent")), "Standalone test must exercise the production formatter through vanilla decomposition");
        List<Run> runs = runs(text);
        check(runs.stream().noneMatch(run -> run.text.contains("§")),
                "Translation must contain styled text, not legacy codes split around arguments: " + runs);
        expectStyle(runs, "+10.50", ChatFormatting.GREEN, true);
        expectStyle(runs, "5.0", ChatFormatting.AQUA, false);
        expectStyle(runs, " HP (Base ", ChatFormatting.GOLD, false);
        //? if >=1.21 {
        /*var registries = net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
        String wire = Component.Serializer.toJson(text, registries);
        Component received = Component.Serializer.fromJson(wire, registries);
        var hoverStack = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD);
        hoverStack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, AdventureText.literal("§aNative"));
        var hoveredText = text.copy().withStyle(style -> style.withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                net.minecraft.network.chat.HoverEvent.Action.SHOW_ITEM,
                new net.minecraft.network.chat.HoverEvent.ItemStackInfo(hoverStack))));
        Component receivedHover = Component.Serializer.fromJson(Component.Serializer.toJson(hoveredText, registries), registries);
        var receivedStack = receivedHover.getStyle().getHoverEvent().getValue(net.minecraft.network.chat.HoverEvent.Action.SHOW_ITEM).getItemStack();
        check(net.minecraft.world.item.ItemStack.isSameItemSameComponents(hoverStack, receivedStack),
                "Registry-aware text serialization changed hover item components");
        *///?} else {
        String wire = Component.Serializer.toJson(text);
        Component received = Component.Serializer.fromJson(wire);
        //?}
        check(wire.contains("\"translate\":\"tip.adventuresystems.curios.heart_of_steel.health_bonus\"")
                && !wire.contains(".formatted"), "Wire translation must retain the authored key: " + wire);
        check(received != null && received.getString().equals(text.getString()), "Message serialization changed text");
        expectStyle(runs(received), "+10.50", ChatFormatting.GREEN, true);
        language("zh_cn");
        check(received.getString().equals("生命加成: +10.50 HP (基础 5.0 + 叠层 5.50) / 叠加上限 100 HP"),
                "Serialized messages must use the receiving client's language: " + received.getString());
        expectStyle(runs(received), " HP (基础 ", ChatFormatting.GOLD, false);
        language("en_us");
        Component nested = AdventureText.translatable("tip.adventuresystems.curios.heart_of_steel.health_growth_amount",
                AdventureText.translatable("tip.adventuresystems.curios.heart_of_steel.max_reached"));
        expectStyle(runs(nested), "[Stack Cap Reached]", ChatFormatting.RED, false);
        Component literalArgument = AdventureText.translatable("tip.adventuresystems.curios.global.bound_to",
                Component.literal("§bPlayer"));
        check(runs(literalArgument).stream().noneMatch(run -> run.text.contains("§")),
                "Literal component arguments must also be converted to native styles");
        expectStyle(runs(literalArgument), "Player", ChatFormatting.AQUA, false);
        Component fallback = AdventureText.translatable("tip.adventuresystems.curios.global.bound_to",
                Component.translatableWithFallback("test.adventuresystems.missing", "§dFallback %s", "Name"));
        check(fallback.getString().equals("[Bound to: Fallback Name]"), "Nested fallback translation was discarded: " + fallback.getString());
        expectStyle(runs(fallback), "Fallback ", ChatFormatting.LIGHT_PURPLE, false);
        Component percent = AdventureText.translatable("tip.adventuresystems.curios.paradise_lost.damage_bonus_pos", "7.50");
        check(percent.getString().equals("Final Damage Modifier: +7.50%"), "Percent escape changed: " + percent.getString());
        expectStyle(runs(percent), "7.50", ChatFormatting.GREEN, true);
        Component legacy = AdventureText.literal("§a§lAB§bCD§rEF");
        check(legacy.getString().equals("ABCDEF"), "Legacy text lost characters");
        expectStyle(runs(legacy), "AB", ChatFormatting.GREEN, true);
        expectStyle(runs(legacy), "CD", ChatFormatting.AQUA, false);
        Component clipped = AdventureText.ellipsize(legacy, 5, AdventureTextTest::characterWidth);
        check(clipped.getString().equals("AB..."), "Clipping changed visible prefix");
        for (Run run : runs(clipped)) {
            if (!run.text.equals("...")) check(run.style.isBold() && run.style.getColor().getValue() == ChatFormatting.GREEN.getColor(),
                    "Clipping discarded the prefix style");
        }
        check(AdventureText.ellipsize(legacy, 2, AdventureTextTest::characterWidth).getString().equals(".."), "Tiny clipping overflows");
        check(AdventureText.ellipsize(legacy, 0, AdventureTextTest::characterWidth).getString().isEmpty(), "Zero width must be empty");
        Component unicode = AdventureText.ellipsize(AdventureText.literal("§a😀ABCDE"), 4, AdventureTextTest::characterWidth);
        check(unicode.getString().equals("😀..."), "Clipping split a Unicode code point");
        Component wrappedLine = AdventureText.fromSequence(legacy.getVisualOrderText());
        check(wrappedLine.getString().equals("ABCDEF"), "Wrapped line conversion lost characters");
        for (Run run : runs(wrappedLine)) {
            if (run.text.equals("A") || run.text.equals("B")) check(run.style.isBold()
                    && run.style.getColor().getValue() == ChatFormatting.GREEN.getColor(), "Wrapped line lost bold green style");
            if (run.text.equals("C") || run.text.equals("D")) check(!run.style.isBold()
                    && run.style.getColor().getValue() == ChatFormatting.AQUA.getColor(), "Wrapped line lost color reset");
        }
        int count = verifyLanguages();
        language("en_us", Map.of("tip.adventuresystems.curios.global.bound_to", "§dCustom %1$s §6again %1$s%%"));
        Component packed = AdventureText.translatable("tip.adventuresystems.curios.global.bound_to", "Player");
        check(packed.getString().equals("Custom Player again Player%"), "Resource pack public-key override was ignored");
        expectStyle(runs(packed), "Custom ", ChatFormatting.LIGHT_PURPLE, false);
        List<Run> packedArguments = runs(packed).stream().filter(run -> run.text.equals("Player")).toList();
        check(packedArguments.size() == 2
                && packedArguments.get(0).style.getColor().getValue() == ChatFormatting.LIGHT_PURPLE.getColor()
                && packedArguments.get(1).style.getColor().getValue() == ChatFormatting.GOLD.getColor()
                && packedArguments.stream().noneMatch(run -> run.style.isBold()),
                "Repeated resource-pack argument must use each occurrence's current template style: " + packedArguments);
        Component callerStyled = AdventureText.translatable("tip.adventuresystems.curios.global.bound_to",
                Component.literal("Explicit").withStyle(ChatFormatting.AQUA));
        List<Run> explicitArguments = runs(callerStyled).stream().filter(run -> run.text.equals("Explicit")).toList();
        check(explicitArguments.size() == 2 && explicitArguments.stream().allMatch(run ->
                run.style.getColor().getValue() == ChatFormatting.AQUA.getColor()), "Current template discarded caller's explicit argument style");
        language("en_us", Map.of("tip.adventuresystems.curios.global.bound_to", "§bNuevo %s"));
        check(packed.getString().equals("Nuevo Player"), "Existing component did not pick up the changed language template");
        expectStyle(runs(packed), "Nuevo ", ChatFormatting.AQUA, false);
        expectStyle(runs(packed), "Player", ChatFormatting.AQUA, false);
        verifyRuntimeTemplates();
        verifyInheritedStyles();
        System.out.println("Text formatting regression checks passed: " + count + " language entries, styles, nesting, clipping, serialization and language switching.");
    }

    private static void verifyRuntimeTemplates() {
        String key = "test.adventuresystems.runtime_template";
        language("en_us", Map.of(key, "§a§l%2$s §b%s §r%2$s/%s%% §kO§mS§nU§oI"));
        // Construct vanilla contents directly, as receiving clients and third-party tooltip providers do.
        Component mixed = Component.translatable(key, "First", "Second");
        check(mixed.getString().equals("Second First Second/Second% OSUI"), "Mixed indexes or percent escape changed: " + mixed.getString());
        List<Run> styled = runs(mixed);
        expectStyle(styled, "First", ChatFormatting.AQUA, false);
        check(styled.get(0).text.equals("Second") && styled.get(0).style.isBold()
                && styled.get(0).style.getColor().getValue() == ChatFormatting.GREEN.getColor(), "First indexed argument lost style");
        check(styled.stream().filter(run -> run.text.equals("Second")).skip(1).allMatch(run -> !run.style.isBold()
                && run.style.getColor() == null), "Reset did not clear argument formatting");
        Run modifiers = styled.stream().filter(run -> run.text.equals("I")).findFirst().orElseThrow();
        check(modifiers.style.isObfuscated() && modifiers.style.isStrikethrough()
                && modifiers.style.isUnderlined() && modifiers.style.isItalic(), "Combined format modifiers changed");
        check(Component.translatable(key, "Alpha", "Beta").getString().equals("Beta Alpha Beta/Beta% OSUI"),
                "Template cache retained another component's arguments");
        var argument = Component.literal("§dNested").withStyle(style -> style.withInsertion("kept")
                .withClickEvent(new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND, "/kept"))
                .withHoverEvent(new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.literal("hover"))))
                .append(Component.literal(" tail").withStyle(ChatFormatting.RED));
        language("en_us", Map.of(key, "§b[%s]"));
        Component metadata = AdventureText.translatable(key, argument);
        check(metadata.getString().equals("[Nested tail]"), "Nested component siblings were lost");
        expectStyle(runs(metadata), "Nested", ChatFormatting.LIGHT_PURPLE, false);
        expectStyle(runs(metadata), " tail", ChatFormatting.RED, false);
        Run nested = runs(metadata).stream().filter(run -> run.text.equals("Nested")).findFirst().orElseThrow();
        check(nested.style.getInsertion().equals("kept") && nested.style.getClickEvent().equals(argument.getStyle().getClickEvent())
                && nested.style.getHoverEvent().equals(argument.getStyle().getHoverEvent()), "Argument events or insertion metadata changed");
        check(argument.getString().equals("§dNested tail"), "Formatting mutated the caller's argument");
        for (String malformed : List.of("§a%s %s", "§a%0$s", "§a%999999999999999999999$s", "§a%d", "§aunfinished %")) {
            language("en_us", Map.of(key, malformed));
            check(Component.translatable(key, "only").getString().equals(malformed), "Malformed format did not retain vanilla fallback: " + malformed);
        }
        String externalKey = "test.anothermod.runtime_template";
        language("en_us", Map.of(externalKey, "§a%s"));
        check(Component.translatable(externalKey, "external").getString().equals("§aexternal"), "Formatter affected another mod's translation");
    }

    private static void verifyInheritedStyles() {
        String key = "test.adventuresystems.inherited_template";
        String template = "Before §aColor §rAfter";
        Style parent = Style.EMPTY.withBold(true).withItalic(true).withColor(ChatFormatting.GOLD);
        language("en_us", Map.of(key, template));
        Component translated = AdventureText.translatable(key).setStyle(parent);
        List<Glyph> expected = new ArrayList<>();
        StringDecomposer.iterateFormatted(template, parent,
                (index, style, codePoint) -> { expected.add(glyph(codePoint, style)); return true; });
        List<Glyph> actual = new ArrayList<>();
        translated.getVisualOrderText().accept((index, style, codePoint) -> { actual.add(glyph(codePoint, style)); return true; });
        check(actual.equals(expected), "Translation must inherit parent styles before color codes and restore them after reset");
        actual.clear();
        AdventureText.literal(template).setStyle(parent).getVisualOrderText().accept((index, style, codePoint) -> {
            actual.add(glyph(codePoint, style)); return true;
        });
        check(actual.equals(expected), "Normalized literal must restore inherited parent styles after reset");
    }

    private static int characterWidth(Component text) {
        return text.getString().codePointCount(0, text.getString().length());
    }

    private static void verifyAuthoredLanguages() {
        check(AdventureTextTest.class.getResource("/assets/adventuresystems/text-layouts.json") == null,
                "Obsolete generated layout resource remains on the release classpath");
        java.util.Set<String> previous = null;
        for (String locale : List.of("en_us", "zh_cn")) {
            Map<String, String> authored = new HashMap<>();
            Map<String, String> packaged = new HashMap<>();
            try (InputStream source = Files.newInputStream(Path.of(System.getProperty("adventure.text.sourceLanguages"), locale + ".json"));
                 InputStream resource = AdventureTextTest.class.getResourceAsStream("/assets/adventuresystems/lang/" + locale + ".json")) {
                Language.loadFromJson(source, authored::put);
                Language.loadFromJson(resource, packaged::put);
            } catch (Exception exception) { throw new AssertionError(exception); }
            check(packaged.keySet().equals(authored.keySet()), locale + ": packaged keys differ from authored keys: "
                    + packaged.size() + " packaged versus " + authored.size() + " authored");
            check(packaged.keySet().stream().noneMatch(key -> key.contains(".formatted")), "Generated keys leaked into " + locale);
            check(previous == null || previous.equals(packaged.keySet()), "Packaged language key sets differ");
            previous = packaged.keySet();
        }
    }

    private static int verifyLanguages() {
        int checked = 0;
        for (String locale : List.of("en_us", "zh_cn")) {
            language(locale);
            Map<String, String> original = new HashMap<>();
            try (InputStream input = Files.newInputStream(Path.of(System.getProperty("adventure.text.sourceLanguages"), locale + ".json"))) {
                Language.loadFromJson(input, original::put);
            } catch (Exception exception) { throw new AssertionError(exception); }
            Path override = Path.of(System.getProperty("adventure.text.languageOverrides"), locale + ".json");
            if (Files.exists(override)) {
                try (InputStream input = Files.newInputStream(override)) {
                    Language.loadFromJson(input, original::put);
                } catch (Exception exception) { throw new AssertionError(exception); }
            }
            Object[] values = new Object[32];
            for (int index = 0; index < values.length; index++) values[index] = "ARG" + index;
            for (Map.Entry<String, String> entry : original.entrySet()) {
                if (!entry.getValue().matches("(?s).*[§][0-9a-fk-orA-FK-OR].*")) continue;
                String expected = String.format(Locale.ROOT,
                        entry.getValue().replaceAll("(?i)§[0-9a-fk-or]", ""), values);
                Component actual = AdventureText.translatable(entry.getKey(), values);
                check(actual.getString().equals(expected), locale + ": composition changed for " + entry.getKey()
                        + " expected=" + expected + " actual=" + actual.getString());
                check(runs(actual).stream().noneMatch(run -> run.text.matches("(?s).*§[0-9a-fk-orA-FK-OR].*")),
                        "Unparsed legacy color in " + entry.getKey());
                List<Glyph> expectedGlyphs = new ArrayList<>();
                StringDecomposer.iterateFormatted(String.format(Locale.ROOT, entry.getValue(), values), Style.EMPTY,
                        (index, style, codePoint) -> { expectedGlyphs.add(glyph(codePoint, style)); return true; });
                List<Glyph> actualGlyphs = new ArrayList<>();
                actual.getVisualOrderText().accept((index, style, codePoint) -> {
                    actualGlyphs.add(glyph(codePoint, style)); return true;
                });
                check(actualGlyphs.equals(expectedGlyphs), locale + ": rendered color or formatting changed for " + entry.getKey());
                checked++;
            }
        }
        return checked;
    }

    private static void language(String locale) {
        language(locale, Map.of());
    }

    private static void language(String locale, Map<String, String> overrides) {
        Map<String, String> translations = new HashMap<>();
        try (InputStream input = AdventureTextTest.class.getResourceAsStream(
                "/assets/adventuresystems/lang/" + locale + ".json")) {
            Language.loadFromJson(input, translations::put);
        } catch (Exception exception) {
            throw new AssertionError("Cannot load test language", exception);
        }
        translations.putAll(overrides);
        Language.inject(new Language() {
            @Override public String getOrDefault(String key, String fallback) { return translations.getOrDefault(key, fallback); }
            @Override public boolean has(String key) { return translations.containsKey(key); }
            @Override public boolean isDefaultRightToLeft() { return false; }
            @Override public FormattedCharSequence getVisualOrder(FormattedText text) {
                return sink -> text.visit((style, value) -> StringDecomposer.iterateFormatted(value, style, sink)
                        ? Optional.empty() : FormattedText.STOP_ITERATION, Style.EMPTY).isPresent();
            }
        });
    }

    private static List<Run> runs(Component text) {
        List<Run> result = new ArrayList<>();
        text.visit((style, value) -> {
            if (!value.isEmpty()) result.add(new Run(value, style));
            return Optional.empty();
        }, Style.EMPTY);
        return result;
    }

    private static void expectStyle(List<Run> runs, String value, ChatFormatting color, boolean bold) {
        Run run = runs.stream().filter(candidate -> candidate.text.equals(value)).findFirst()
                .orElseThrow(() -> new AssertionError("Missing text: " + value + " in " + runs));
        check(run.style.getColor() != null && run.style.getColor().getValue() == color.getColor()
                && run.style.isBold() == bold, "Incorrect style for " + value + ": " + run.style);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static Glyph glyph(int codePoint, Style style) {
        return new Glyph(codePoint, style.getColor() == null ? null : style.getColor().getValue(), style.isBold(),
                style.isItalic(), style.isUnderlined(), style.isStrikethrough(), style.isObfuscated());
    }

    private record Glyph(int codePoint, Integer color, boolean bold, boolean italic, boolean underlined,
                         boolean strikethrough, boolean obfuscated) {}

    private record Run(String text, Style style) {}
}
