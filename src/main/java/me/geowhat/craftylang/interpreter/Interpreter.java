package me.geowhat.craftylang.interpreter;

import me.geowhat.craftylang.client.CraftyLangClient;
import me.geowhat.craftylang.client.CraftyLangSettings;
import me.geowhat.craftylang.client.util.Message;
import me.geowhat.craftylang.client.util.Scheduler;
import me.geowhat.craftylang.interpreter.ast.Expression;
import me.geowhat.craftylang.interpreter.ast.Parser;
import me.geowhat.craftylang.interpreter.ast.Statement;
import me.geowhat.craftylang.interpreter.error.BreakException;
import me.geowhat.craftylang.interpreter.error.ReturnException;
import me.geowhat.craftylang.interpreter.error.RuntimeError;
import me.geowhat.craftylang.mixin.MinecraftAccessor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {

    public final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expression, Integer> locals = new HashMap<>();

    private boolean shouldStopExecution = false;

    public Interpreter() {
        registerGlobals();
    }

    public void exitInterpreter(int code) {
        Message.sendDebug("Stopping execution: " + code);

        Scheduler.stopExecution();

        shouldStopExecution = true;

        switch (code) {
            case CraftScript.SUCCESS_CODE -> Message.sendSuccess("Program execution finished with code " + code);
            case CraftScript.PARSE_ERROR_CODE,
                 CraftScript.RUNTIME_ERROR_CODE -> Message.sendError("Program execution finished with code " + code + ".");
            case CraftScript.USER_REQUEST_CODE -> Message.sendSuccess("Program execution finished as per user request (" + code + ")");
        }
    }

    public void registerGlobals() {
        Message.sendDebug("Loading predefined globals");

        globals.define("VERSION", CraftyLangClient.VERSION);

        assert Minecraft.getInstance().player != null;
        globals.define("xc", Minecraft.getInstance().player.getX());
        globals.define("yc", Minecraft.getInstance().player.getY());
        globals.define("zc", Minecraft.getInstance().player.getZ());
        globals.define("time", Minecraft.getInstance().player.level().getGameTime());

        ClientTickEvents.END_CLIENT_TICK.register(event -> {
            if (Minecraft.getInstance().player != null) {
                globals.redefine("xc", Math.round(Minecraft.getInstance().player.getX()));
                globals.redefine("yc", Math.round(Minecraft.getInstance().player.getY()));
                globals.redefine("zc", Math.round(Minecraft.getInstance().player.getZ()));
                globals.redefine("time", Math.round(Minecraft.getInstance().player.level().getGameTime()));
            }
        });


        globals.define("glob", new CraftScriptCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                Message.sendGlobal(stringify(args.get(0)));
                return null;
            }

            @Override
            public String toString() {
                return "<builtin fn>";
            }
        });

        globals.define("exec", new CraftScriptCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                Minecraft.getInstance().player.connection.sendCommand(stringify(args.get(0)));
                return null;
            }

            @Override
            public String toString() {
                return "<builtin fn>";
            }
        });

        globals.define("sleep", new CraftScriptCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                AtomicInteger beginTicks = new AtomicInteger();

                int delay = 0;
                try {
                    delay = Integer.parseInt(stringify(args.get(0)));
                } catch (NumberFormatException e) {
                    Message.sendError("Invalid sleep() delay");
                }

                int finalDelay = delay;
                CraftScript.timer.setCode(() -> {
                    while (beginTicks.get() <= finalDelay) {
                        beginTicks.getAndIncrement();
                    }
                    Message.sendDebug("1");
                    CraftScript.timer.stop(); // Stop the timer after the code is executed
                    if (CraftScript.timer.getOnCompletionListener() != null) {
                        CraftScript.timer.getOnCompletionListener().run(); // Execute the listener if it's set
                    }
                });

                CraftScript.timer.setOnCompletionListener(() -> Message.sendDebug("2"));

                return null;
            }

            @Override
            public String toString() {
                return "<builtin fn>";
            }
        });

        globals.define("close", new CraftScriptCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                Minecraft.getInstance().setScreen(null);
                return null;
            }

            @Override
            public String toString() {
                return "<builtin fn>";
            }
        });

        globals.define("clear", new CraftScriptCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                Minecraft.getInstance().gui.getChat().clearMessages(true);
                return null;
            }

            @Override
            public String toString() {
                return "<builtin fn>";
            }
        });

        globals.define("exit", new CraftScriptCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                Minecraft.getInstance().setScreen(null);
                int code = Integer.parseInt(stringify(args.get(0)));
                CraftScript.kill(code);
                return null;
            }

            @Override
            public String toString() {
                return "<builtin fn>";
            }
        });

        globals.define("str", new CraftScriptCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                return stringify(args.get(0));
            }

            @Override
            public String toString() {
                return "<builtin fn>";
            }
        });

        globals.define("attack", new CraftScriptCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                MinecraftAccessor mca = (MinecraftAccessor) Minecraft.getInstance();
                mca.attack();

                return null;
            }

            @Override
            public String toString() {
                return "<builtin fn>";
            }
        });
    }

    public void interpret(List<Statement> statements) {
        Message.sendDebug("Interpreter is interpreting");
        environment = globals;

        try {
            for (Statement statement : statements) {
                if (shouldStopExecution)
                    return;

                execute(statement);
            }
        } catch (RuntimeError err) {
            CraftScript.runtimeError(err);
        }
    }

    public void resolve(Expression expr, int depth) {
        locals.put(expr, depth);
    }

    // ==================================
    // EXPRESSIONS
    // ==================================

    @Override
    public Object visitAssignExpression(Expression.AssignExpression expr) {
        Object value = evaluate(expr.value);

        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }

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
                return !isEqual(left, right);

            case EQUAL_EQUAL:
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

            case MOD:
                checkNumberOperand(expr.operator, right);
                return (double) left % (double) right;
        }

        throw new RuntimeError(expr.operator, "Operator type `" + expr.operator.type().toString().toLowerCase() + "` is not suitable for BinaryExpression");
    }

    @Override
    public Object visitCallExpression(Expression.CallExpression expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        if (!expr.args.isEmpty()) {
            for (Expression arg : expr.args) {
                arguments.add(evaluate(arg));
            }
        }

        if (!(callee instanceof CraftScriptCallable function)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes");
        }

        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + " parameters but got " + arguments.size());
        }

        return function.call(this, arguments);
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
        return lookUpVariable(expr.name, expr);
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
    public Void visitBreakStatement(Statement.BreakStatement statement) {

        throw new BreakException();
    }

    @Override
    public Void visitExpressionStatement(Statement.ExpressionStatement statement) {
        evaluate(statement.expr);
        return null;
    }

    @Override
    public Void visitFunctionStatement(Statement.FunctionStatement statement) {
        CraftScriptFunction function = new CraftScriptFunction(statement);
        environment.define(statement.name.lexeme(), function);
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
    public Void visitRepeatStatement(Statement.RepeatStatement statement) {

        if (statement.delay == null) {
            return null;
        }

        long delay = Long.parseLong(stringify(evaluate(statement.delay)));

        Scheduler.repeat(() -> execute(statement.body), delay * 50L, CraftyLangSettings.MAX_WHILE_LOOP_ITERATIONS);
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.ReturnStatement statement) {
        Object value = null;
        if (statement.value != null)
            value = evaluate(statement.value);

        throw new ReturnException(value);
    }

    @Override
    public Void visitWhileStatement(Statement.WhileStatement statement) {
        int counter = 0;

        try {
            while (isTruthy(evaluate(statement.condition))) {
                if (counter++ > CraftyLangSettings.MAX_WHILE_LOOP_ITERATIONS) {
                    Message.sendDebug("While loop iteration limit reached (" + CraftyLangSettings.MAX_WHILE_LOOP_ITERATIONS + ")");
                    break;
                }

                execute(statement.body);
            }
        } catch (BreakException ignored) { }
        return null;
    }

    private Object lookUpVariable(Token name, Expression expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme());
        } else {
            return globals.get(name);
        }
    }

    private Object evaluate(Expression expr) {
        return expr.accept(this);
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

    public void executeBlock(List<Statement> statements, Environment environment) {
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
            if (txt.endsWith(".0")) {
                txt = txt.substring(0, txt.length() - 2);
            }
            return txt;
        }

        return obj.toString();
    }
}
