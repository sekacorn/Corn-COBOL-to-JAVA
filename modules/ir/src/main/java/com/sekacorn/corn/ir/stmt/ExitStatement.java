/*
 * ExitStatement - EXIT control flow statement
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir.stmt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sekacorn.corn.ir.SourceLocation;

import java.util.Objects;

// EXIT Statement
public record ExitStatement(
        @JsonProperty("type") ExitType type,
        @JsonProperty("location") SourceLocation location
) implements Statement {
    @JsonCreator
    public ExitStatement {
        Objects.requireNonNull(type);
    }

    @Override
    public SourceLocation getLocation() { return location; }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitExit(this);
    }

    public enum ExitType { PROGRAM, PARAGRAPH, SECTION, PERFORM }
}
