package me.geowhat.craftylang.interpreter.syntax;

import com.mojang.datafixers.util.Pair;
import me.geowhat.craftylang.utils.ComponentUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SyntaxHighlighter {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    private static final Pattern STRING_PATTERN = Pattern.compile("'[^']*'");

    private final SyntaxColorPalette syntaxColorPalette;
    private final Collection<String> keywords;
    private final Pattern keywordsPattern;

    public SyntaxHighlighter(SyntaxColorPalette syntaxColorPalette, Collection<String> keywords) {
        this.syntaxColorPalette = syntaxColorPalette;
        this.keywords = Collections.unmodifiableCollection(keywords);
        this.keywordsPattern = combineKeywordsIntoRegex();
    }

    private Pattern combineKeywordsIntoRegex() {
        return Pattern.compile(String.join("|", keywords));
    }

    public Component formatPageText(String pageText) {
        MutableComponent component = Component.literal(pageText)
                .withColor(syntaxColorPalette.defaultColor());

        component = replaceMatches(component, NUMBER_PATTERN, syntaxColorPalette.numberColor());
        component = replaceMatches(component, keywordsPattern, syntaxColorPalette.keywordColor());
        component = replaceMatches(component, STRING_PATTERN, syntaxColorPalette.stringColor());

        return component;
    }

    private static MutableComponent replaceMatches(Component component, Pattern pattern, int color) {
        MutableComponent newComponent = component.copy();
        Matcher matcher = pattern.matcher(component.getString());

        while (matcher.find()) {
            int start = matcher.start();
            Pair<Component, Component> splitComponent = ComponentUtils.splitAt(newComponent, start);
            Component beforeComponent = splitComponent.getFirst();

            Pair<Component, Component> splitComponent2 = ComponentUtils.splitAt(splitComponent.getSecond(), matcher.end() - start);
            MutableComponent resultComponent = Component.literal(splitComponent2.getFirst().getString()).withColor(color);
            Component afterComponent = splitComponent2.getSecond();

            newComponent = beforeComponent.copy().append(resultComponent).append(afterComponent);
        }

        return newComponent;
    }
}
