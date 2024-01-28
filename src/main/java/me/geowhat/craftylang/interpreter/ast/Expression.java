package me.geowhat.craftylang.interpreter.ast;

import me.geowhat.craftylang.interpreter.Token;

import java.util.List;

public abstract class Expression {

    public interface Visitor<R> {
        R visitAssignExpression(AssignExpression expr);
        R visitBinaryExpression(BinaryExpression expr);
        R visitCallExpression(CallExpression expr);
        R visitGroupingExpression(GroupingExpression expr);
        R visitLiteralExpression(LiteralExpression expr);
        R visitLogicalExpression(LogicalExpression expr);
        R visitUnaryExpression(UnaryExpression expr);
        R visitVariableExpression(VariableExpression expr);
    }

    public static class AssignExpression extends Expression {
        public final Token name;
        public final Expression value;

        public AssignExpression(Token name, Expression value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpression(this);
        }
    }

    public static class BinaryExpression extends Expression {

        public final Expression left;
        public final Token operator;
        public final Expression right;

        public BinaryExpression(Expression left, Token operator, Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpression(this);
        }
    }

    public static class CallExpression extends Expression {

        public final Expression callee;
        public final Token paren;
        public final List<Expression> args;

        public CallExpression(Expression callee, Token paren, List<Expression> args) {
            this.callee = callee;
            this.paren = paren;
            this.args = args;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpression(this);
        }
    }

    public static class GroupingExpression extends Expression {

        public final Expression expr;

        public GroupingExpression(Expression expr) {
            this.expr = expr;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpression(this);
        }
    }

    public static class LogicalExpression extends Expression {

        public final Expression left;
        public final Token operator;
        public final Expression right;

        public LogicalExpression(Expression left, Token operator, Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpression(this);
        }
    }

    public static class LiteralExpression extends Expression {

        public final Object value;

        public LiteralExpression(Object value) {
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpression(this);
        }
    }

    public static class UnaryExpression extends Expression {

        public final Token operator;
        public final Expression right;

        public UnaryExpression(Token operator, Expression right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpression(this);
        }
    }

    public static class VariableExpression extends Expression {

        public final Token name;

        public VariableExpression(Token name) {
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpression(this);
        }
    }

    public abstract <R> R accept(Visitor<R> visitor);
}
