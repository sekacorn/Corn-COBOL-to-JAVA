/*
 * IfStatement - IF conditional statement
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
 * Represents an IF statement with optional ELSE branch.
 */
public final class IfStatement implements Statement {
    private final Expression condition;
    private final List<Statement> thenBranch;
    private final List<Statement> elseBranch;
    private final SourceLocation location;

    @JsonCreator
    public IfStatement(
            @JsonProperty("condition") Expression condition,
            @JsonProperty("thenBranch") List<Statement> thenBranch,
            @JsonProperty("elseBranch") List<Statement> elseBranch,
            @JsonProperty("location") SourceLocation location) {
        this.condition = Objects.requireNonNull(condition, "condition cannot be null");
        this.thenBranch = thenBranch != null ? List.copyOf(thenBranch) : Collections.emptyList();
        this.elseBranch = elseBranch != null ? List.copyOf(elseBranch) : Collections.emptyList();
        this.location = location;
    }

    public Expression getCondition() {
        return condition;
    }

    public List<Statement> getThenBranch() {
        return thenBranch;
    }

    public List<Statement> getElseBranch() {
        return elseBranch;
    }

    @JsonIgnore
    public boolean hasElse() {
        return !elseBranch.isEmpty();
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitIf(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IfStatement that)) return false;
        return Objects.equals(condition, that.condition) &&
                Objects.equals(thenBranch, that.thenBranch) &&
                Objects.equals(elseBranch, that.elseBranch) &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, thenBranch, elseBranch, location);
    }
}
