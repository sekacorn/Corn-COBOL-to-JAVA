/*
 * MoveStatement - MOVE statement
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir.stmt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sekacorn.corn.ir.SourceLocation;
import com.sekacorn.corn.ir.expr.Expression;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a MOVE statement.
 */
public final class MoveStatement implements Statement {
    private final Expression source;
    private final List<Expression> targets;
    private final boolean corresponding;
    private final SourceLocation location;

    @JsonCreator
    public MoveStatement(
            @JsonProperty("source") Expression source,
            @JsonProperty("targets") List<Expression> targets,
            @JsonProperty("corresponding") boolean corresponding,
            @JsonProperty("location") SourceLocation location) {
        this.source = Objects.requireNonNull(source, "source cannot be null");
        this.targets = targets != null ? List.copyOf(targets) : Collections.emptyList();
        this.corresponding = corresponding;
        this.location = location;
    }

    public Expression getSource() {
        return source;
    }

    public List<Expression> getTargets() {
        return targets;
    }

    @JsonIgnore
    public boolean isCorresponding() {
        return corresponding;
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitMove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MoveStatement that)) return false;
        return corresponding == that.corresponding &&
                Objects.equals(source, that.source) &&
                Objects.equals(targets, that.targets) &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, targets, corresponding, location);
    }
}
