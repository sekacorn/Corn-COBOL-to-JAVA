/*
 * InspectStatement - INSPECT statement IR node.
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

// INSPECT Statement
public record InspectStatement(
        @JsonProperty("target") Expression target,
        @JsonProperty("operation") InspectOp operation,
        @JsonProperty("tallyingClause") TallyingClause tallyingClause,
        @JsonProperty("replacingClauses") List<ReplacingClause> replacingClauses,
        @JsonProperty("convertingClause") ConvertingClause convertingClause,
        @JsonProperty("location") SourceLocation location
) implements Statement {
    @JsonCreator
    public InspectStatement {
        Objects.requireNonNull(target);
        Objects.requireNonNull(operation);
        replacingClauses = replacingClauses != null ? List.copyOf(replacingClauses) : Collections.emptyList();
    }

    @Override
    public SourceLocation getLocation() { return location; }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitInspect(this);
    }

    public enum InspectOp { TALLYING, REPLACING, CONVERTING }

    public enum TallyMode { ALL, LEADING, CHARACTERS }

    public enum ReplaceMode { ALL, LEADING, FIRST, CHARACTERS }

    public record Boundary(
            @JsonProperty("delimiter") Expression delimiter,
            @JsonProperty("before") boolean before
    ) {
        @JsonCreator
        public Boundary {
            Objects.requireNonNull(delimiter);
        }
    }

    public record TallyFor(
            @JsonProperty("mode") TallyMode mode,
            @JsonProperty("value") Expression value,
            @JsonProperty("beforeBoundary") Boundary beforeBoundary,
            @JsonProperty("afterBoundary") Boundary afterBoundary
    ) {
        @JsonCreator
        public TallyFor {
            Objects.requireNonNull(mode);
        }
    }

    public record TallyingClause(
            @JsonProperty("counter") Expression counter,
            @JsonProperty("forClauses") List<TallyFor> forClauses
    ) {
        @JsonCreator
        public TallyingClause {
            Objects.requireNonNull(counter);
            forClauses = forClauses != null ? List.copyOf(forClauses) : Collections.emptyList();
        }
    }

    public record ReplacingClause(
            @JsonProperty("mode") ReplaceMode mode,
            @JsonProperty("target") Expression target,
            @JsonProperty("replacement") Expression replacement,
            @JsonProperty("beforeBoundary") Boundary beforeBoundary,
            @JsonProperty("afterBoundary") Boundary afterBoundary
    ) {
        @JsonCreator
        public ReplacingClause {
            Objects.requireNonNull(mode);
            Objects.requireNonNull(replacement);
        }
    }

    public record ConvertingClause(
            @JsonProperty("from") Expression from,
            @JsonProperty("to") Expression to,
            @JsonProperty("beforeBoundary") Boundary beforeBoundary,
            @JsonProperty("afterBoundary") Boundary afterBoundary
    ) {
        @JsonCreator
        public ConvertingClause {
            Objects.requireNonNull(from);
            Objects.requireNonNull(to);
        }
    }
}
