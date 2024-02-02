package me.geowhat.craftylang.interpreter;

import java.util.HashMap;
import java.util.Map;

public class Keywords {

    public static final Map<String, TokenType> keywords = new HashMap<>();

    public static void addKeywords() {
        keywords.put("unit", TokenType.UNIT);
        keywords.put("else", TokenType.ELSE);
        keywords.put("F", TokenType.FALSE);
        keywords.put("fn", TokenType.FUNCTION);
        keywords.put("for", TokenType.FOR);
        keywords.put("if", TokenType.IF);
        keywords.put("null", TokenType.NULL);
        keywords.put("say", TokenType.SAY);
        keywords.put("ret", TokenType.RETURN);
        keywords.put("sup", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("T", TokenType.TRUE);
        keywords.put("let", TokenType.LET);
        keywords.put("while", TokenType.WHILE);
        keywords.put("rep", TokenType.REPEAT);
    }
}
