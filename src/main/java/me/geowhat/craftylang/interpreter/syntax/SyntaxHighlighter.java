package me.geowhat.craftylang.interpreter.syntax;

import java.util.Collection;
import java.util.Collections;

public final class SyntaxHighlighter {

    private final SyntaxColorPalette syntaxColorPalette;
    private final Collection<String> keywords;

    public SyntaxHighlighter(SyntaxColorPalette syntaxColorPalette, Collection<String> keywords) {
        this.syntaxColorPalette = syntaxColorPalette;
        this.keywords = Collections.unmodifiableCollection(keywords);
    }

    public SyntaxColorPalette getSyntaxColors() {
        return syntaxColorPalette;
    }

    public Collection<String> getKeywords() {
        return keywords;
    }

    public int getTextColor(String text) {
        if (!keywords.contains(text))
            return syntaxColorPalette.defaultColor();

        return syntaxColorPalette.keywordColor();
    }
}
