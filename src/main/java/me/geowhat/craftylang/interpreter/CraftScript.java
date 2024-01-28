package me.geowhat.craftylang.interpreter;

import me.geowhat.craftylang.client.util.Message;
import me.geowhat.craftylang.interpreter.ast.Parser;
import me.geowhat.craftylang.interpreter.ast.Statement;
import me.geowhat.craftylang.interpreter.error.RuntimeError;

import java.util.List;

public class CraftScript {

    private static boolean hadError;
    private static boolean hadRuntimeError;

    public static void run(String src) {

        hadError = false;
        hadRuntimeError = false;

        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.lex();
        Parser parser = new Parser(tokens);
        List<Statement> statements = parser.parse();

        Interpreter interpreter = new Interpreter();

        if (hadError) {
            Message.sendError("Error while parsing");
            return;
        }

        interpreter.interpret(statements);

        if (hadRuntimeError) {
            Message.sendError("Exited due to a Runtime Error");
            return;
        }

        Message.sendSuccess("Program execution finished");
        Message.sendNewline();
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
