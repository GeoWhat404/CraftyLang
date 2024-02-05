package me.geowhat.craftylang.interpreter.error;

public class LexerError extends RuntimeException {

    public final int page;
    public final int line;

    public LexerError(int page, int line, String message) {
        super(message);

        this.page = page;
        this.line = line;
    }
}
