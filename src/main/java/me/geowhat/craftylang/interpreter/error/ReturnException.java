package me.geowhat.craftylang.interpreter.error;

public class ReturnException extends RuntimeException {

    public final Object value;

    public ReturnException(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
