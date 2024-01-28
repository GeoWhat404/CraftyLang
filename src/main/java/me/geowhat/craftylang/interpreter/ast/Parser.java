package me.geowhat.craftylang.interpreter.ast;

import me.geowhat.craftylang.interpreter.CraftScript;
import me.geowhat.craftylang.interpreter.Token;
import me.geowhat.craftylang.interpreter.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Statement declaration() {
        try {
            if (match(TokenType.LET)) {
                return varDeclaration();
            }
            return statement();
        } catch (ParseError err) {
            synchronize();
            return null;
        }
    }

    private Expression expression() {
        return assignment();
    }

    private Statement statement() {
        if (match(TokenType.FOR))
            return forStatement();
        if (match(TokenType.IF))
            return ifStatement();
        if (match(TokenType.SAY))
            return sayStatement();
        if (match(TokenType.WHILE))
            return whileStatement();
        if (match(TokenType.LEFT_BRACE))
            return new Statement.BlockStatement(block());

        return expressionStatement();
    }

    private Statement forStatement() {
        consume(TokenType.LEFT_PAREN, "Expected a \"(\" after \"for\"");

        Statement initializer;
        if (match(TokenType.SEMICOLON))
            initializer = null;
        else if (match(TokenType.LET))
            initializer = varDeclaration();
        else
            initializer = expressionStatement();

        Expression condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = expression();
        }
        consume(TokenType.SEMICOLON, "Expected a \";\" after loop condition");

        Expression increment = null;
        if (!check(TokenType.RIGHT_PAREN)) {
            increment = expression();
        }
        consume(TokenType.RIGHT_PAREN, "Expected a \")\" after for clauses");

        Statement body = statement();

        if (increment != null) {
            body = new Statement.BlockStatement(Arrays.asList(
                            body, new Statement.ExpressionStatement(increment)));
        }

        if (condition == null)
            condition = new Expression.LiteralExpression(true);

        body = new Statement.WhileStatement(condition, body);

        if (initializer != null) {
            body = new Statement.BlockStatement(Arrays.asList(initializer, body));
        }

        return body;
    }

    private Statement varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expected an identifier");

        Expression initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }

        consume(TokenType.SEMICOLON, "Expected a \";\" after variable declaration");
        return new Statement.LetStatement(name, initializer);
    }

    private Statement whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expected a \"(\" after \"while\"");
        Expression condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected a \")\" after while condition");

        Statement body = statement();

        return new Statement.WhileStatement(condition, body);
    }

    private Statement ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expected a \"(\" after \"if\"");
        Expression condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected a \") after if condition");

        Statement thenBranch = statement();
        Statement elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }

        return new Statement.IfStatement(condition, thenBranch, elseBranch);
    }

    private Statement sayStatement() {
        Expression value = expression();
        consume(TokenType.SEMICOLON, "Expected a \";\" after value");
        return new Statement.SayStatement(value);
    }

    private Statement expressionStatement() {
        Expression expr = expression();
        consume(TokenType.SEMICOLON, "Expected a \";\" after expression");
        return new Statement.ExpressionStatement(expr);
    }

    private List<Statement> block() {
        List<Statement> statements = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Expected a \"}\" after a block");
        return statements;
    }

    private Expression assignment() {
        Expression expr = or();

        while (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expression value = assignment();

            if (expr instanceof Expression.VariableExpression) {
                Token name = ((Expression.VariableExpression) expr).name;
                expr = new Expression.AssignExpression(name, value);
            } else {
                error(equals, "Invalid assignment target");
            }
        }

        return expr;
    }

    private Expression or() {
        Expression expr = and();

        while (match(TokenType.OR)) {
            Token operator = previous();
            Expression right = and();
            expr = new Expression.LogicalExpression(expr, operator, right);
        }
        return expr;
    }

    private Expression and() {
        Expression expr = equality();

        while (match(TokenType.AND)) {
            Token operator = previous();
            Expression right = equality();
            expr = new Expression.LogicalExpression(expr, operator, right);
        }

        return expr;
    }


    private Expression equality() {
        Expression expr = comparison();

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expression right = comparison();

            expr = new Expression.BinaryExpression(expr, operator, right);
        }

        return expr;
    }

    private Expression comparison() {
        Expression expr = term();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expression right = term();
            expr = new Expression.BinaryExpression(expr, operator, right);
        }

        return expr;
    }

    private Expression term() {
        Expression expr = factor();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expression right = factor();
            expr = new Expression.BinaryExpression(expr, operator, right);
        }

        return expr;
    }

    private Expression factor() {
        Expression expr = unary();

        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = previous();
            Expression right = unary();
            expr = new Expression.BinaryExpression(expr, operator, right);
        }

        return expr;
    }

    private Expression unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expression right = unary();
            return new Expression.UnaryExpression(operator, right);
        }

        return primary();
    }

    private Expression primary() {
        if (match(TokenType.FALSE)) return new Expression.LiteralExpression(false);
        if (match(TokenType.TRUE)) return new Expression.LiteralExpression(true);
        if (match(TokenType.NULL)) return new Expression.LiteralExpression(null);

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expression.LiteralExpression(previous().literal());
        }

        if (match(TokenType.IDENTIFIER)) {
            return new Expression.VariableExpression(previous());
        }

        if (match(TokenType.LEFT_PAREN)) {
            Expression expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expected a \")\" after expression");
            return new Expression.GroupingExpression(expr);
        }
        throw parseError(peek(), "Expected an expression");
    }

    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();

        throw parseError(peek(), message);
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type() == TokenType.END;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    // ERROR HANDLING
    private static class ParseError extends RuntimeException { }

    private ParseError parseError(Token token, String message) {
        CraftScript.error(token, message);
        return new ParseError();
    }

    private void error(Token token, String message) {
        CraftScript.error(token, message);
        synchronize();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type() == TokenType.SEMICOLON) return;

            switch (peek().type()) {
                case UNIT:
                case FUNCTION:
                case LET:
                case FOR:
                case IF:
                case WHILE:
                case SAY:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}
