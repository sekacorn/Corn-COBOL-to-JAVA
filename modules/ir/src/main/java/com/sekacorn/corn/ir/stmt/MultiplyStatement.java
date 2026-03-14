/*
 * MultiplyStatement - MULTIPLY statement
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir.stmt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sekacorn.corn.ir.SourceLocation;
import com.sekacorn.corn.ir.expr.Expression;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

// MULTIPLY Statement
public record MultiplyStatement(
        @JsonProperty("operand1") Expression operand1,
        @JsonProperty("operand2") Expression operand2,
        @JsonProperty("giving") List<Expression> giving,
        @JsonProperty("rounded") boolean rounded,
        @JsonProperty("roundMode") RoundMode roundMode,
        @JsonProperty("onSizeError") List<Statement> onSizeError,
        @JsonProperty("location") SourceLocation location
) implements Statement {
    @JsonCreator
    public MultiplyStatement {
        Objects.requireNonNull(operand1);
        Objects.requireNonNull(operand2);
        giving = giving != null ? List.copyOf(giving) : Collections.emptyList();
        onSizeError = onSizeError != null ? List.copyOf(onSizeError) : Collections.emptyList();
    }

    @Override
    public SourceLocation getLocation() { return location; }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitMultiply(this);
    }
}
