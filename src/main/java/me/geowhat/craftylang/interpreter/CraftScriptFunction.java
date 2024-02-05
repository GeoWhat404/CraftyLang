package me.geowhat.craftylang.interpreter;

import me.geowhat.craftylang.client.util.Message;
import me.geowhat.craftylang.interpreter.ast.Statement;
import me.geowhat.craftylang.interpreter.error.Return;

import java.util.List;

public class CraftScriptFunction implements CraftScriptCallable {

    private final Statement.FunctionStatement declaration;

    public CraftScriptFunction(Statement.FunctionStatement declaration) {
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        Environment environment = new Environment(interpreter.globals);

        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme(), args.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnVal) {
            return returnVal.value;
        }

        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme() + ">";
    }
}
