/*
 * DeleteStatement - DELETE file I/O statement
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir.stmt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sekacorn.corn.ir.SourceLocation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record DeleteStatement(
        @JsonProperty("fileName") String fileName,
        @JsonProperty("invalidKey") List<Statement> invalidKey,
        @JsonProperty("location") SourceLocation location
) implements Statement {
    @JsonCreator
    public DeleteStatement {
        Objects.requireNonNull(fileName);
        invalidKey = invalidKey != null ? List.copyOf(invalidKey) : Collections.emptyList();
    }

    @Override
    public SourceLocation getLocation() { return location; }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitDelete(this);
    }
}
