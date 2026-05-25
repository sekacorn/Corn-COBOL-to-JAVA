/*
 * ReleaseStatement - RELEASE / RETURN statement IR node
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir.stmt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sekacorn.corn.ir.SourceLocation;
import com.sekacorn.corn.ir.expr.Expression;

import java.util.List;
import java.util.Objects;

/**
 * IR node for RELEASE and RETURN statements used in SORT INPUT/OUTPUT procedures.
 */
public record ReleaseStatement(
        @JsonProperty("recordName") String recordName,
        @JsonProperty("isReturn") boolean isReturn,
        @JsonProperty("from") Expression from,
        @JsonProperty("into") Expression into,
        @JsonProperty("atEnd") List<Statement> atEnd,
        @JsonProperty("notAtEnd") List<Statement> notAtEnd,
        @JsonProperty("location") SourceLocation location
) implements Statement {

    @JsonCreator
    public ReleaseStatement {
        Objects.requireNonNull(recordName);
        atEnd = atEnd != null ? List.copyOf(atEnd) : List.of();
        notAtEnd = notAtEnd != null ? List.copyOf(notAtEnd) : List.of();
    }

    @Override
    public SourceLocation getLocation() { return location; }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitRelease(this);
    }
}
