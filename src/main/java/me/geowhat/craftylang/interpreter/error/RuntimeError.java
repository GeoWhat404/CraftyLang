package me.geowhat.craftylang.interpreter.error;

import me.geowhat.craftylang.interpreter.Token;

public class RuntimeError extends RuntimeException {
    public final Token token;

    public RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
