package me.geowhat.craftylang.interpreter;

public record Token(TokenType type, String lexeme, Object literal, int page, int line) {

    public String toString() {
        return "TokenType: " + type + " - Lexeme: " + lexeme + " - Literal: " + literal;
    }
}
