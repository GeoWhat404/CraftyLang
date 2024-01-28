package me.geowhat.craftylang.interpreter;

import me.geowhat.craftylang.client.CraftyLangClient;
import me.geowhat.craftylang.client.CraftyLangSettings;
import me.geowhat.craftylang.client.util.Message;
import me.geowhat.craftylang.interpreter.ast.Expression;
import me.geowhat.craftylang.interpreter.ast.Statement;
import me.geowhat.craftylang.interpreter.error.RuntimeError;

import java.util.List;

public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {

    private Environment environment = new Environment();

    public void interpret(List<Statement> statements) {
        environment.define("VERSION", CraftyLangClient.VERSION);
        try {
            for (Statement statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError err) {
            CraftScript.runtimeError(err);
        }
    }


    // ==================================
    // EXPRESSIONS
    // ==================================

    @Override
    public Object visitAssignExpression(Expression.AssignExpression expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitBinaryExpression(Expression.BinaryExpression expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type()) {
            case GREATER:
                checkNumberOperand(expr.operator, left, right);
                return (double) left > (double) right;

            case GREATER_EQUAL:
                checkNumberOperand(expr.operator, left, right);
                return (double) left >= (double) right;

            case LESS:
                checkNumberOperand(expr.operator, left, right);
                return (double) left < (double) right;

            case LESS_EQUAL:
                checkNumberOperand(expr.operator, left, right);
                return (double) left <= (double) right;

            case BANG_EQUAL:
                checkNumberOperand(expr.operator, left, right);
                return !isEqual(left, right);

            case EQUAL_EQUAL:
                checkNumberOperand(expr.operator, left, right);
                return isEqual(left, right);

            case MINUS:
                checkNumberOperand(expr.operator, left, right);
                return (double) left - (double) right;

            case STAR:
                checkNumberOperand(expr.operator, left, right);
                return (double) left * (double) right;

            case SLASH:
                checkNumberOperand(expr.operator, left, right);
                return (double) left / (double) right;

            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return String.valueOf(left) + right;
                }

                throw new RuntimeError(expr.operator, "Operands must be of type number or string");
        }

        throw new RuntimeError(expr.operator, "Operator type `" + expr.operator.type().toString().toLowerCase() + "` is not suitable for BinaryExpression");
    }

    @Override
    public Object visitGroupingExpression(Expression.GroupingExpression expr) {
        return evaluate(expr.expr);
    }

    @Override
    public Object visitLiteralExpression(Expression.LiteralExpression expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpression(Expression.LogicalExpression expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type() == TokenType.OR) {
            if (isTruthy(left))
                return left;
        } else if (expr.operator.type() == TokenType.AND) {
            if (!isTruthy(left))
                return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitUnaryExpression(Expression.UnaryExpression expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type()) {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
            case BANG:
                return !isTruthy(right);
        }

        Message.sendError("It would appear something went terribly wrong");
        return null;
    }

    @Override
    public Object visitVariableExpression(Expression.VariableExpression expr) {
        return environment.get(expr.name);
    }

    // ==================================
    // STATEMENTS
    // ==================================

    @Override
    public Void visitBlockStatement(Statement.BlockStatement statement) {
        executeBlock(statement.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitExpressionStatement(Statement.ExpressionStatement statement) {
        evaluate(statement.expr);
        return null;
    }

    @Override
    public Void visitIfStatement(Statement.IfStatement statement) {
        if (isTruthy(evaluate(statement.condition))) {
            execute(statement.thenBranch);
        } else if (statement.elseBranch != null) {
            execute(statement.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitSayStatement(Statement.SayStatement statement) {
        Object value = evaluate(statement.expr);
        Message.sendInfo("Program: " + stringify(value));
        return null;
    }

    @Override
    public Void visitLetStatement(Statement.LetStatement statement) {
        Object value = null;
        if (statement.initializer != null) {
            value = evaluate(statement.initializer);
        }


        if (environment.isDefined(statement.name)) {
            throw new RuntimeError(statement.name, "Variable \"" + statement.name.lexeme() + "\" has already been defined in this scope");
        }

        environment.define(statement.name.lexeme(), value);
        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.WhileStatement statement) {
        int counter = 0;

        while (isTruthy(evaluate(statement.condition))) {
            if (counter++ > CraftyLangSettings.MAX_WHILE_LOOP_ITERATIONS && CraftyLangSettings.LIMIT_WHILE_LOOP) {
                Message.sendError("While loop iteration limit reached (" + CraftyLangSettings.MAX_WHILE_LOOP_ITERATIONS + ")");
                break;
            }

            execute(statement.body);
        }
        return null;
    }

    private Object evaluate(Expression expr) {
        return expr.accept(this);
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

    private void executeBlock(List<Statement> statements, Environment environment) {
        Environment current = this.environment;
        try {
            this.environment = environment;

            for (Statement statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = current;
        }
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double)
            return;

        throw new RuntimeError(operator, "Operand must be a number");
    }

    private void checkNumberOperand(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double)
            return;

        throw new RuntimeError(operator, "Operands must be numbers");
    }

    private boolean isTruthy(Object obj) {
        if (obj == null)
            return false;

        if (obj instanceof Boolean)
            return (boolean) obj;

        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;

        if (a == null)
            return false;

        return a.equals(b);
    }

    private String stringify(Object obj) {
        if (obj == null)
            return "null";

        if (obj instanceof Double) {
            String txt = obj.toString();
            if (txt.endsWith(".0"))
                txt = txt.substring(0, txt.length() - 2);
            return txt;
        }

        return obj.toString();
    }
}
