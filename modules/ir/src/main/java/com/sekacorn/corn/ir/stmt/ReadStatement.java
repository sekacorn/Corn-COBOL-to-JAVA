/*
 * ReadStatement - READ file I/O statement
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

public record ReadStatement(
        @JsonProperty("fileName") String fileName,
        @JsonProperty("into") Expression into,
        @JsonProperty("key") Expression key,
        @JsonProperty("atEnd") List<Statement> atEnd,
        @JsonProperty("notAtEnd") List<Statement> notAtEnd,
        @JsonProperty("invalidKey") List<Statement> invalidKey,
        @JsonProperty("location") SourceLocation location
) implements Statement {
    @JsonCreator
    public ReadStatement {
        Objects.requireNonNull(fileName);
        atEnd = atEnd != null ? List.copyOf(atEnd) : Collections.emptyList();
        notAtEnd = notAtEnd != null ? List.copyOf(notAtEnd) : Collections.emptyList();
        invalidKey = invalidKey != null ? List.copyOf(invalidKey) : Collections.emptyList();
    }

    @Override
    public SourceLocation getLocation() { return location; }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitRead(this);
    }
}
