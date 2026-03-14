/*
 * InitializeStatement - INITIALIZE statement IR node.
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

// INITIALIZE Statement
public record InitializeStatement(
        @JsonProperty("targets") List<Expression> targets,
        @JsonProperty("location") SourceLocation location
) implements Statement {
    @JsonCreator
    public InitializeStatement {
        targets = targets != null ? List.copyOf(targets) : Collections.emptyList();
    }

    @Override
    public SourceLocation getLocation() { return location; }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitInitialize(this);
    }
}
