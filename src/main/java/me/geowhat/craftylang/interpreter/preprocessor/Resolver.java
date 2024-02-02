package me.geowhat.craftylang.interpreter.preprocessor;

import me.geowhat.craftylang.interpreter.error.ModuleError;

public class Resolver {
    
    private final String src;
    private final StringBuilder builder;
    
    public Resolver(String src) {
        this.src = src;
        this.builder = new StringBuilder();

        for (int c = 0; c < src.length(); c++) {
            char current = src.charAt(c);

            if (current == '!' && c + 1 < src.length() && Character.isAlphabetic(src.charAt(c + 1))) {
                StringBuilder keyword = new StringBuilder();

                int wordIdx = c + 1;
                while (wordIdx < src.length() && Character.isAlphabetic(src.charAt(wordIdx))) {
                    keyword.append(src.charAt(wordIdx));
                    wordIdx++;
                }
                c = wordIdx - 1;

                String word = keyword.toString();
                if (word.trim().equals("use")) {
                    resolveModule(wordIdx);
                }
            }
        }
    }

    private void resolveModule(int wordIdx) {
        ModuleLoader loader;
        StringBuilder nextStr = new StringBuilder();

        while (wordIdx < src.length() && Character.isWhitespace(src.charAt(wordIdx))) {
            wordIdx++;
        }

        while (wordIdx < src.length() && !Character.isWhitespace(src.charAt(wordIdx))) {
            nextStr.append(src.charAt(wordIdx));
            wordIdx++;
        }

        String module = nextStr.toString();
        try {
            loader = new ModuleLoader(module);

            builder.append("$");
            builder.append(loader);
            builder.append("$");

        } catch (ModuleError ignored) {

        }
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
