/*
 * ConditionExpr - Condition expression (class test, sign test, etc.)
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
 * Represents a COBOL condition (NUMERIC, ALPHABETIC, etc.).
 */
public final class ConditionExpr implements Expression {
    private final Expression subject;
    private final ConditionType conditionType;
    private final boolean negated;
    private final SourceLocation location;

    @JsonCreator
    public ConditionExpr(
            @JsonProperty("subject") Expression subject,
            @JsonProperty("conditionType") ConditionType conditionType,
            @JsonProperty("negated") boolean negated,
            @JsonProperty("location") SourceLocation location) {
        this.subject = Objects.requireNonNull(subject, "subject cannot be null");
        this.conditionType = Objects.requireNonNull(conditionType, "conditionType cannot be null");
        this.negated = negated;
        this.location = location;
    }

    public Expression getSubject() {
        return subject;
    }

    public ConditionType getConditionType() {
        return conditionType;
    }

    public boolean isNegated() {
        return negated;
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitCondition(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConditionExpr that)) return false;
        return negated == that.negated &&
                Objects.equals(subject, that.subject) &&
                conditionType == that.conditionType &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, conditionType, negated, location);
    }

    @Override
    public String toString() {
        return String.format("%s IS %s%s",
                subject,
                negated ? "NOT " : "",
                conditionType);
    }

    public enum ConditionType {
        NUMERIC,
        ALPHABETIC,
        ALPHABETIC_LOWER,
        ALPHABETIC_UPPER,
        POSITIVE,
        NEGATIVE,
        ZERO,
        CONDITION_NAME
    }
}
