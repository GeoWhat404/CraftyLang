package me.geowhat.craftylang.interpreter;

import me.geowhat.craftylang.client.util.Message;
import me.geowhat.craftylang.interpreter.error.RuntimeError;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    public final Environment enclosing;

    private final Map<String, Object> values = new HashMap<>();

    public Environment() {
        enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public boolean isDefined(Token name) {
        return values.containsKey(name.lexeme());
    }

    public boolean isDefined(String name) {
        return values.containsKey(name);
    }

    public Object get(Token name) {
        if (isDefined(name.lexeme())) {
            return values.get(name.lexeme());
        }

        if (enclosing != null)
            return enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable: \"" + name.lexeme() + "\"");
    }

    public void define(String name, Object value) {
        Message.sendDebug("Defined " + name + " with a value of " + value);
        values.put(name, value);
    }

    public void redefine(String name, Object value) {
        if (isDefined(name)) {

            if (values.get(name) != value)
                values.replace(name, value);
        } else {
            define(name, value);
        }
    }

    public void assign(Token token, Object value) {
        Message.sendDebug("Assigned " + token.lexeme() + " to new value of " + value);

        if (isDefined(token)) {
            values.put(token.lexeme(), value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(token, value);
            return;
        }

        throw new RuntimeError(token, "Undefined variable: \"" + token.lexeme() + "\"");
    }

    public Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    public void assignAt(int distance, Token name, Object value) {
        ancestor(distance).assign(name, value);
    }

    public Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            if (environment == null)
                continue;
            environment = environment.enclosing;
        }

        return environment;
    }
}
