/*
 * BinaryOp - Binary operation expression
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
 * Represents a binary operation (arithmetic, relational, or logical).
 */
public final class BinaryOp implements Expression {
    private final Expression left;
    private final Operator operator;
    private final Expression right;
    private final SourceLocation location;

    @JsonCreator
    public BinaryOp(
            @JsonProperty("left") Expression left,
            @JsonProperty("operator") Operator operator,
            @JsonProperty("right") Expression right,
            @JsonProperty("location") SourceLocation location) {
        this.left = Objects.requireNonNull(left, "left cannot be null");
        this.operator = Objects.requireNonNull(operator, "operator cannot be null");
        this.right = Objects.requireNonNull(right, "right cannot be null");
        this.location = location;
    }

    public Expression getLeft() {
        return left;
    }

    public Operator getOperator() {
        return operator;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitBinary(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BinaryOp binaryOp)) return false;
        return Objects.equals(left, binaryOp.left) &&
                operator == binaryOp.operator &&
                Objects.equals(right, binaryOp.right) &&
                Objects.equals(location, binaryOp.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, operator, right, location);
    }

    @Override
    public String toString() {
        return String.format("(%s %s %s)", left, operator, right);
    }

    public enum Operator {
        // Arithmetic
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE,
        POWER,

        // Relational
        EQUAL,
        NOT_EQUAL,
        LESS_THAN,
        LESS_EQUAL,
        GREATER_THAN,
        GREATER_EQUAL,

        // Logical
        AND,
        OR
    }
}
