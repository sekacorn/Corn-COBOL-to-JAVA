/*
 * ComputeStatement - COMPUTE statement
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

// COMPUTE Statement
public record ComputeStatement(
        @JsonProperty("targets") List<Expression> targets,
        @JsonProperty("expression") Expression expression,
        @JsonProperty("rounded") boolean rounded,
        @JsonProperty("roundMode") RoundMode roundMode,
        @JsonProperty("onSizeError") List<Statement> onSizeError,
        @JsonProperty("location") SourceLocation location
) implements Statement {
    @JsonCreator
    public ComputeStatement {
        targets = targets != null ? List.copyOf(targets) : Collections.emptyList();
        Objects.requireNonNull(expression);
        onSizeError = onSizeError != null ? List.copyOf(onSizeError) : Collections.emptyList();
    }

    @Override
    public SourceLocation getLocation() { return location; }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitCompute(this);
    }
}
