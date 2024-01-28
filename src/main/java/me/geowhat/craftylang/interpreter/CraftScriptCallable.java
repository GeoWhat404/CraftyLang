package me.geowhat.craftylang.interpreter;

import java.util.List;

public interface CraftScriptCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> args);
}
