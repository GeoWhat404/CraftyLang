package me.geowhat.craftylang.interpreter.ast;

public class PrettyPrint implements Expression.Visitor<String> {

    public String print(Expression expression) {
        return expression.accept(this);
    }

    @Override
    public String visitAssignExpression(Expression.AssignExpression expr) {
        return null;
    }

    @Override
    public String visitBinaryExpression(Expression.BinaryExpression expr) {
        return parenthesize(expr.operator.lexeme(), expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpression(Expression.GroupingExpression expr) {
        return parenthesize("group", expr.expr);
    }

    @Override
    public String visitLiteralExpression(Expression.LiteralExpression expr) {
        if (expr.value == null)
            return "null";

        return expr.value.toString();
    }

    @Override
    public String visitLogicalExpression(Expression.LogicalExpression expr) {
        return null;
    }

    @Override
    public String visitUnaryExpression(Expression.UnaryExpression expr) {
        return parenthesize(expr.operator.lexeme(), expr.right);
    }

    @Override
    public String visitVariableExpression(Expression.VariableExpression expr) {
        return null;
    }

    private String parenthesize(String name, Expression... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expression expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
}
