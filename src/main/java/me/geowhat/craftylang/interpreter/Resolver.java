package me.geowhat.craftylang.interpreter;

import me.geowhat.craftylang.interpreter.ast.Expression;
import me.geowhat.craftylang.interpreter.ast.Statement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expression.Visitor<Void>, Statement.Visitor<Void> {

    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Override
    public Void visitAssignExpression(Expression.AssignExpression expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitBinaryExpression(Expression.BinaryExpression expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpression(Expression.CallExpression expr) {
        resolve(expr.callee);

        for (Expression arg : expr.args)
            resolve(arg);
        return null;
    }

    @Override
    public Void visitGroupingExpression(Expression.GroupingExpression expr) {
        resolve(expr.expr);
        return null;
    }

    @Override
    public Void visitLiteralExpression(Expression.LiteralExpression expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpression(Expression.LogicalExpression expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitUnaryExpression(Expression.UnaryExpression expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpression(Expression.VariableExpression expr) {
        if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme()) == Boolean.FALSE) {
            CraftScript.error(expr.name, "Can't read local variable in its own initializer");
        }
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitBlockStatement(Statement.BlockStatement statement) {
        beginScope();
        resolve(statement.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitBreakStatement(Statement.BreakStatement statement) {
        return null;
    }

    @Override
    public Void visitExpressionStatement(Statement.ExpressionStatement statement) {
        resolve(statement.expr);
        return null;
    }

    @Override
    public Void visitFunctionStatement(Statement.FunctionStatement statement) {
        declare(statement.name);
        define(statement.name);

        resolveFunction(statement, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitIfStatement(Statement.IfStatement statement) {
        resolve(statement.condition);
        resolve(statement.thenBranch);

        if (statement.elseBranch != null)
            resolve(statement.elseBranch);
        return null;
    }

    @Override
    public Void visitSayStatement(Statement.SayStatement statement) {
        resolve(statement.expr);
        return null;
    }

    @Override
    public Void visitLetStatement(Statement.LetStatement statement) {
        declare(statement.name);
        if (statement.initializer != null) {
            resolve(statement.initializer);
        }
        define(statement.name);
        return null;
    }

    @Override
    public Void visitRepeatStatement(Statement.RepeatStatement statement) {
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.ReturnStatement statement) {
        if (currentFunction == FunctionType.NONE) {
            CraftScript.error(statement.keyword, "Can't return from top-level code");
        }

        if (statement.value != null) {
            resolve(statement.value);
        }
        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.WhileStatement statement) {
        resolve(statement.condition);
        resolve(statement.body);
        return null;
    }

    public void resolve(List<Statement> statements) {
        for (Statement stmt : statements) {
            resolve(stmt);
        }
    }

    private void resolve(Statement statement) {
        statement.accept(this);
    }

    private void resolve(Expression expression) {
        expression.accept(this);
    }
    private void resolveLocal(Expression expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme())) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }

    private void resolveFunction(Statement.FunctionStatement function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;
        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
        currentFunction = enclosingFunction;
    }

    private void declare(Token name) {
        if (scopes.isEmpty())
            return;

        Map<String, Boolean> scope = scopes.peek();

        if (scope.containsKey(name.lexeme())) {
            CraftScript.error(name, "Variable \"" + name.lexeme() + "\" has already been declared in this scope");
        }

        scope.put(name.lexeme(), false);
    }

    private void define(Token name) {
        if (scopes.isEmpty())
            return;

        scopes.peek().put(name.lexeme(), true);
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    private enum FunctionType {
        NONE,
        FUNCTION,
    }
}
