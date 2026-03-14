/*
 * DivideStatement - DIVIDE statement
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

// DIVIDE Statement
public record DivideStatement(
        @JsonProperty("dividend") Expression dividend,
        @JsonProperty("divisor") Expression divisor,
        @JsonProperty("into") Expression into,
        @JsonProperty("giving") List<Expression> giving,
        @JsonProperty("remainder") Expression remainder,
        @JsonProperty("rounded") boolean rounded,
        @JsonProperty("roundMode") RoundMode roundMode,
        @JsonProperty("onSizeError") List<Statement> onSizeError,
        @JsonProperty("location") SourceLocation location
) implements Statement {
    @JsonCreator
    public DivideStatement {
        Objects.requireNonNull(dividend);
        Objects.requireNonNull(divisor);
        giving = giving != null ? List.copyOf(giving) : Collections.emptyList();
        onSizeError = onSizeError != null ? List.copyOf(onSizeError) : Collections.emptyList();
    }

    @Override
    public SourceLocation getLocation() { return location; }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitDivide(this);
    }
}
