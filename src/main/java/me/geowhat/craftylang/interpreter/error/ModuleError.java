package me.geowhat.craftylang.interpreter.error;

public class ModuleError extends RuntimeException {

    public final String module;

    public ModuleError(String module, String message) {
        super(message);
        this.module = module;
    }
}
