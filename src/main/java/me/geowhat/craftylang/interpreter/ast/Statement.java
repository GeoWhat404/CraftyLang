package me.geowhat.craftylang.interpreter.ast;

import me.geowhat.craftylang.interpreter.Token;

import java.util.List;

public abstract class Statement {
    public interface Visitor<R> {
        R visitBlockStatement(BlockStatement statement);
        R visitExpressionStatement(ExpressionStatement statement);
        R visitIfStatement(IfStatement statement);
        R visitSayStatement(SayStatement statement);
        R visitLetStatement(LetStatement statement);
        R visitWhileStatement(WhileStatement statement);
    }
    public static class BlockStatement extends Statement {

        public final List<Statement> statements;

        public BlockStatement(List<Statement> statements) {
            this.statements = statements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStatement(this);
        }
    }
    public static class ExpressionStatement extends Statement {

        public final Expression expr;

        public ExpressionStatement(Expression expr) {
            this.expr = expr;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStatement(this);
        }
    }
    public static class IfStatement extends Statement {

        public final Expression condition;
        public final Statement thenBranch;
        public final Statement elseBranch;

        public IfStatement(Expression condition, Statement thenBranch, Statement elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStatement(this);
        }
    }
    public static class SayStatement extends Statement {

        public final Expression expr;

        public SayStatement(Expression expr) {
            this.expr = expr;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSayStatement(this);
        }
    }
    public static class LetStatement extends Statement {

        public final Token name;
        public final Expression initializer;

        public LetStatement(Token name, Expression initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLetStatement(this);
        }
    }
    public static class WhileStatement extends Statement {

        public final Expression condition;
        public final Statement body;

        public WhileStatement(Expression condition, Statement body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStatement(this);
        }
    }

    public abstract <R> R accept(Visitor<R> visitor);
}
