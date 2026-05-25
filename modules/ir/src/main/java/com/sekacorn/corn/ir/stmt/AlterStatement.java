/*
 * AlterStatement - ALTER statement IR node
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir.stmt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sekacorn.corn.ir.SourceLocation;

import java.util.List;
import java.util.Objects;

/**
 * IR node for the ALTER statement (obsolete COBOL feature).
 * Code generation emits a comment — ALTER modifies GO TO destinations at runtime,
 * which has no direct Java equivalent.
 */
public record AlterStatement(
        @JsonProperty("alterations") List<Alteration> alterations,
        @JsonProperty("location") SourceLocation location
) implements Statement {

    public record Alteration(
            @JsonProperty("from") String from,
            @JsonProperty("to") String to
    ) {}

    @JsonCreator
    public AlterStatement {
        Objects.requireNonNull(alterations);
        alterations = List.copyOf(alterations);
    }

    @Override
    public SourceLocation getLocation() { return location; }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitAlter(this);
    }
}
