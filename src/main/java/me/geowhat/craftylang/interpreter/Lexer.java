package me.geowhat.craftylang.interpreter;

import me.geowhat.craftylang.client.util.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {

    private final String src;
    private final List<Token> tokens;
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int page = 1;

    private boolean moduleCode = false;

    public Lexer(String src) {
        this.src = src;
        this.tokens = new ArrayList<>();
    }

    public List<Token> lex() {
        while (!isAtEnd()) {
            start = current;
            lexToken();
        }

        tokens.add(new Token(TokenType.END, "", null, page, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= src.length();
    }

    private void lexToken() {
        char ch = advance();

        switch (ch) {
            case '$': moduleCode = !moduleCode;              break;

            case '(': addToken(TokenType.LEFT_PAREN);   break;
            case ')': addToken(TokenType.RIGHT_PAREN);  break;
            case '{': addToken(TokenType.LEFT_BRACE);   break;
            case '}': addToken(TokenType.RIGHT_BRACE);  break;
            case ',': addToken(TokenType.COMMA);        break;
            case '.': addToken(TokenType.DOT);          break;
            case '-': addToken(TokenType.MINUS);        break;
            case '+': addToken(TokenType.PLUS);         break;
            case ';': addToken(TokenType.SEMICOLON);    break;
            case '*': addToken(TokenType.STAR);         break;
            case '/': addToken(TokenType.SLASH);        break;
            case '&': addToken(TokenType.AND);          break;
            case '|': addToken(TokenType.OR);           break;
            case '%': addToken(TokenType.MOD);          break;
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;

            case '#':
                while (peek() != '\n' && !isAtEnd()) advance();
                break;

            case '\'':
                handleString();
                break;

            case ' ':
            case '\r':
            case '\t':
                break;

            case '\n':
                advanceLine();
                break;


            default:
                if (isDigit(ch)) {
                    handleNumber();
                } else if (isAlpha(ch)) {
                    handleIdentifier();
                } else {
                    CraftScript.error(page, line, "Unexpected character: " + ch + (moduleCode ? " in module" : ""));
                }

                break;
        }
    }

    private boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private boolean isAlpha(char ch) {
        return ch >= 'a' && ch <= 'z' ||
               ch >= 'A' && ch <= 'Z' ||
               ch == '_';
    }

    private boolean isAlphaNum(char ch) {
        return isAlpha(ch) || isDigit(ch);
    }

    private void advanceLine() {
        if (moduleCode)
            return;

        if (line + 1 > 14) {
            page++;
            line = 1;
        } else {
            line++;
        }
    }

    private void handleIdentifier() {
        while (isAlphaNum(peek()))
            advance();

        String value = src.substring(start, current);
        TokenType type = Keywords.keywords.get(value);

        if (type == null)
            type = TokenType.IDENTIFIER;

        addToken(type);
    }

    private void handleString() {
        while (peek() != '\'' && !isAtEnd()) {
            if (peek() == '\n') {
                advanceLine();
            }

            advance();
        }

        if (isAtEnd()) {
            CraftScript.error(page, line, "Unterminated String" + (moduleCode ? " in module" : ""));
            return;
        }

        // handle the closing "
        advance();

        String value = src.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private void handleNumber() {
        while (isDigit(peek()) && !isAtEnd()) {
            advance();
        }

        if (peek() == '.' && isDigit(peek(1))) {
            advance();

            while (isDigit(peek()))
                advance();
        }

        double value = Double.parseDouble(src.substring(start, current));
        addToken(TokenType.NUMBER, value);
    }

    private char advance() {
        return src.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = src.substring(start, current);
        tokens.add(new Token(type, text, literal, page, line));
    }

    private boolean match(char expect) {
        if (isAtEnd()) return false;
        if (src.charAt(current) != expect) return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return src.charAt(current);
    }

    private char peek(int offset) {
        if (current + offset > src.length()) return '\0';
        return src.charAt(current + offset);
    }
}
