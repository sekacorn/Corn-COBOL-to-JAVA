/*
 * CallStatement - CALL statement IR node.
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

// CALL Statement
public record CallStatement(
        @JsonProperty("programName") Expression programName,
        @JsonProperty("arguments") List<CallArgument> arguments,
        @JsonProperty("returning") Expression returning,
        @JsonProperty("onException") List<Statement> onException,
        @JsonProperty("location") SourceLocation location
) implements Statement {
    @JsonCreator
    public CallStatement {
        Objects.requireNonNull(programName);
        arguments = arguments != null ? List.copyOf(arguments) : Collections.emptyList();
        onException = onException != null ? List.copyOf(onException) : Collections.emptyList();
    }

    @Override
    public SourceLocation getLocation() { return location; }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitCall(this);
    }
}
