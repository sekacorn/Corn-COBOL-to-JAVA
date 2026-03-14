/*
 * StopStatement - STOP control flow statement
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir.stmt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sekacorn.corn.ir.SourceLocation;
import com.sekacorn.corn.ir.expr.Expression;

import java.util.Objects;

// STOP Statement
public record StopStatement(
        @JsonProperty("type") StopType type,
        @JsonProperty("message") Expression message,
        @JsonProperty("location") SourceLocation location
) implements Statement {
    @JsonCreator
    public StopStatement {
        Objects.requireNonNull(type);
    }

    @Override
    public SourceLocation getLocation() { return location; }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitStop(this);
    }

    public enum StopType { RUN, LITERAL }
}
