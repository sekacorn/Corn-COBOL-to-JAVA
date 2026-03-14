/*
 * UnaryOp - Unary operation expression
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sekacorn.corn.ir.SourceLocation;

import java.util.Objects;

/**
 * Represents a unary operation (negation or NOT).
 */
public final class UnaryOp implements Expression {
    private final Operator operator;
    private final Expression operand;
    private final SourceLocation location;

    @JsonCreator
    public UnaryOp(
            @JsonProperty("operator") Operator operator,
            @JsonProperty("operand") Expression operand,
            @JsonProperty("location") SourceLocation location) {
        this.operator = Objects.requireNonNull(operator, "operator cannot be null");
        this.operand = Objects.requireNonNull(operand, "operand cannot be null");
        this.location = location;
    }

    public Operator getOperator() {
        return operator;
    }

    public Expression getOperand() {
        return operand;
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitUnary(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UnaryOp unaryOp)) return false;
        return operator == unaryOp.operator &&
                Objects.equals(operand, unaryOp.operand) &&
                Objects.equals(location, unaryOp.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, operand, location);
    }

    @Override
    public String toString() {
        return String.format("(%s %s)", operator, operand);
    }

    public enum Operator {
        NEGATE,  // Unary minus
        NOT      // Logical NOT
    }
}
