/*
 * AcceptStatement - ACCEPT statement IR node.
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

// ACCEPT Statement
public record AcceptStatement(
        @JsonProperty("target") Expression target,
        @JsonProperty("from") String from,
        @JsonProperty("location") SourceLocation location
) implements Statement {
    @JsonCreator
    public AcceptStatement {
        Objects.requireNonNull(target);
    }

    @Override
    public SourceLocation getLocation() { return location; }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitAccept(this);
    }
}
