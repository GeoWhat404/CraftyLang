package me.geowhat.craftylang.interpreter.syntax;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public final class SyntaxHighlighter {

    private final SyntaxColors syntaxColors;
    private final Collection<String> keywords;

    public SyntaxHighlighter(SyntaxColors syntaxColors, Collection<String> keywords) {
        this.syntaxColors = syntaxColors;
        this.keywords = Collections.unmodifiableCollection(keywords);
    }

    public SyntaxColors getSyntaxColors() {
        return syntaxColors;
    }

    public Collection<String> getKeywords() {
        return keywords;
    }

    public int getTextColor(String text) {
        if (!keywords.contains(text))
            return syntaxColors.defaultColor();

        return syntaxColors.keywordColor();
    }
}
