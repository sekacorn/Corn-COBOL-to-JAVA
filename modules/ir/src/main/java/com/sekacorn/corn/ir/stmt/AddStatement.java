/*
 * AddStatement - ADD statement
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

// ADD Statement
public record AddStatement(
        @JsonProperty("operands") List<Expression> operands,
        @JsonProperty("to") List<Expression> to,
        @JsonProperty("giving") List<Expression> giving,
        @JsonProperty("rounded") boolean rounded,
        @JsonProperty("roundMode") RoundMode roundMode,
        @JsonProperty("onSizeError") List<Statement> onSizeError,
        @JsonProperty("location") SourceLocation location
) implements Statement {
    @JsonCreator
    public AddStatement {
        operands = operands != null ? List.copyOf(operands) : Collections.emptyList();
        to = to != null ? List.copyOf(to) : Collections.emptyList();
        giving = giving != null ? List.copyOf(giving) : Collections.emptyList();
        onSizeError = onSizeError != null ? List.copyOf(onSizeError) : Collections.emptyList();
    }

    @Override
    public SourceLocation getLocation() { return location; }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitAdd(this);
    }
}
