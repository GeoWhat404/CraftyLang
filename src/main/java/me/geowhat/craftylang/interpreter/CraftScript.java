package me.geowhat.craftylang.interpreter;

import me.geowhat.craftylang.client.CraftyLangClient;
import me.geowhat.craftylang.client.util.Message;
import me.geowhat.craftylang.client.util.Scheduler;
import me.geowhat.craftylang.client.util.Timer;
import me.geowhat.craftylang.crs.CRSMath;
import me.geowhat.craftylang.interpreter.ast.Parser;
import me.geowhat.craftylang.interpreter.ast.Statement;
import me.geowhat.craftylang.interpreter.error.ModuleError;
import me.geowhat.craftylang.interpreter.error.RuntimeError;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class CraftScript {

    public static final int SUCCESS_CODE = 0;
    public static final int INTERPRET_ERROR_CODE = 1;
    public static final int PARSE_ERROR_CODE = 2;
    public static final int RUNTIME_ERROR_CODE = 3;
    public static final int USER_REQUEST_CODE = 4;

    public static boolean running = false;

    private static boolean hadError;
    private static boolean hadRuntimeError;
    public static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private static Interpreter interpreter;

    public static Timer timer;

    public static void init() {
        timer = new Timer();
        timer.start();

        Modules.reload();
    }

    public static void run(String src) {
        Message.sendDebug("Running new script");
        Scheduler.startExecution();

        hadError = false;
        hadRuntimeError = false;

        interpreter = new Interpreter();

        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();

        if (hadError) {
            Message.sendError("Error while lexing");
            interpreter.exitInterpreter(INTERPRET_ERROR_CODE);
            return;
        }

        Parser parser = new Parser(tokens);
        List<Statement> statements = parser.parse();

        if (hadError) {
            Message.sendError("Error while parsing");
            interpreter.exitInterpreter(PARSE_ERROR_CODE);
            return;
        }

        running = true;

        new Resolver(interpreter).resolve(statements);

        if (hadError) {
            Message.sendError("Error while resolving");
            interpreter.exitInterpreter(PARSE_ERROR_CODE);
            return;
        }

        interpreter.interpret(statements);

        if (hadRuntimeError) {
            Message.sendError("Exited due to a Runtime Error");
            interpreter.exitInterpreter(RUNTIME_ERROR_CODE);
            return;
        }

        if (running)
            interpreter.exitInterpreter(SUCCESS_CODE);
    }

    public static void kill(int code) {
        if (interpreter != null) {
            interpreter.exitInterpreter(code);
            running = false;
        }
    }

    public static void error(Token token, String message) {
        if (token.type() == TokenType.END)
            report(token.page(), token.line(), "at end", message);
        else
            report(token.page(), token.line(), "at \"" + token.lexeme() + "\"", message);
    }

    public static void error(int page, int line, String message) {
        report(page, line, "", message);
    }

    public static void moduleError(ModuleError error) {
        Message.sendError("CraftScript ModuleError: " + error.getMessage());
        Message.sendError("           unknown module: " + error.module);
        kill(1);
    }

    public static void runtimeError(RuntimeError error) {
        if (!hadRuntimeError)
            Message.sendError("CraftScript RuntimeError: " + error.getMessage());
        Message.sendError("           at page: " + error.token.page() + ", line: " + error.token.line());
        Message.sendError("              |  \"" + error.token.lexeme() + "\"");
        hadRuntimeError = true;
    }

    private static void report(int page, int line, String position, String message) {
        if (!hadError)
            Message.sendError("CraftScript Error: " + message);

        Message.sendError("           at page: " + page + ", line: " + line);
        if (!position.isBlank() && !position.isEmpty())
            Message.sendError("              | " + position);

        hadError = true;
    }
}
