/*
 * Literal - Literal value expression
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
 * Represents a literal value (numeric, string, or figurative constant).
 */
public final class Literal implements Expression {
    private final Object value;
    private final LiteralType literalType;
    private final SourceLocation location;

    @JsonCreator
    public Literal(
            @JsonProperty("value") Object value,
            @JsonProperty("literalType") LiteralType literalType,
            @JsonProperty("location") SourceLocation location) {
        this.value = value;
        this.literalType = Objects.requireNonNull(literalType, "literalType cannot be null");
        this.location = location;
    }

    public static Literal numeric(Number value, SourceLocation location) {
        return new Literal(value, LiteralType.NUMERIC, location);
    }

    public static Literal string(String value, SourceLocation location) {
        return new Literal(value, LiteralType.STRING, location);
    }

    public static Literal figurative(FigurativeConstant constant, SourceLocation location) {
        return new Literal(constant, LiteralType.FIGURATIVE, location);
    }

    public Object getValue() {
        return value;
    }

    public LiteralType getLiteralType() {
        return literalType;
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitLiteral(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Literal literal)) return false;
        return Objects.equals(value, literal.value) &&
                literalType == literal.literalType &&
                Objects.equals(location, literal.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, literalType, location);
    }

    @Override
    public String toString() {
        return String.format("Literal{%s, type=%s}", value, literalType);
    }

    public enum LiteralType {
        NUMERIC,
        STRING,
        FIGURATIVE
    }

    public enum FigurativeConstant {
        ZERO,
        ZEROS,
        SPACE,
        SPACES,
        HIGH_VALUE,
        HIGH_VALUES,
        LOW_VALUE,
        LOW_VALUES,
        QUOTE,
        QUOTES,
        NULL,
        NULLS,
        ALL,
        TRUE,
        FALSE
    }
}
