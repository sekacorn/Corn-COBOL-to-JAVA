/*
 * PerformStatement - PERFORM statement (loops and procedure calls)
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
import java.util.Optional;

/**
 * Represents a PERFORM statement with various forms.
 */
public final class PerformStatement implements Statement {
    private final PerformType type;
    private final String targetParagraph;
    private final String throughParagraph;
    private final Expression times;
    private final Expression untilCondition;
    private final VaryingClause varying;
    private final TestPosition testPosition;
    private final List<Statement> inlineStatements;
    private final SourceLocation location;

    @JsonCreator
    public PerformStatement(
            @JsonProperty("type") PerformType type,
            @JsonProperty("targetParagraph") String targetParagraph,
            @JsonProperty("throughParagraph") String throughParagraph,
            @JsonProperty("times") Expression times,
            @JsonProperty("untilCondition") Expression untilCondition,
            @JsonProperty("varying") VaryingClause varying,
            @JsonProperty("testPosition") TestPosition testPosition,
            @JsonProperty("inlineStatements") List<Statement> inlineStatements,
            @JsonProperty("location") SourceLocation location) {
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.targetParagraph = targetParagraph;
        this.throughParagraph = throughParagraph;
        this.times = times;
        this.untilCondition = untilCondition;
        this.varying = varying;
        this.testPosition = testPosition;
        this.inlineStatements = inlineStatements != null ?
                List.copyOf(inlineStatements) : Collections.emptyList();
        this.location = location;
    }

    public PerformType getType() {
        return type;
    }

    public Optional<String> getTargetParagraph() {
        return Optional.ofNullable(targetParagraph);
    }

    public Optional<String> getThroughParagraph() {
        return Optional.ofNullable(throughParagraph);
    }

    public Optional<Expression> getTimes() {
        return Optional.ofNullable(times);
    }

    public Optional<Expression> getUntilCondition() {
        return Optional.ofNullable(untilCondition);
    }

    public Optional<VaryingClause> getVarying() {
        return Optional.ofNullable(varying);
    }

    public Optional<TestPosition> getTestPosition() {
        return Optional.ofNullable(testPosition);
    }

    public List<Statement> getInlineStatements() {
        return inlineStatements;
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitPerform(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PerformStatement that)) return false;
        return type == that.type &&
                Objects.equals(targetParagraph, that.targetParagraph) &&
                Objects.equals(throughParagraph, that.throughParagraph) &&
                Objects.equals(times, that.times) &&
                Objects.equals(untilCondition, that.untilCondition) &&
                Objects.equals(varying, that.varying) &&
                testPosition == that.testPosition &&
                Objects.equals(inlineStatements, that.inlineStatements) &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, targetParagraph, throughParagraph, times,
                untilCondition, varying, testPosition, inlineStatements, location);
    }

    public enum PerformType {
        SIMPLE,           // PERFORM paragraph
        TIMES,            // PERFORM n TIMES
        UNTIL,            // PERFORM UNTIL condition
        VARYING,          // PERFORM VARYING
        INLINE            // PERFORM ... END-PERFORM
    }

    /**
     * Controls when the UNTIL/VARYING condition is evaluated.
     * WITH TEST BEFORE (default): condition checked before each iteration.
     * WITH TEST AFTER: condition checked after each iteration (always executes at least once).
     */
    public enum TestPosition {
        BEFORE,
        AFTER
    }

    public static final class VaryingClause {
        private final String variable;
        private final Expression from;
        private final Expression by;
        private final Expression until;
        private final List<VaryingClause> after;  // For nested AFTER clauses

        @JsonCreator
        public VaryingClause(
                @JsonProperty("variable") String variable,
                @JsonProperty("from") Expression from,
                @JsonProperty("by") Expression by,
                @JsonProperty("until") Expression until,
                @JsonProperty("after") List<VaryingClause> after) {
            this.variable = Objects.requireNonNull(variable, "variable cannot be null");
            this.from = Objects.requireNonNull(from, "from cannot be null");
            this.by = by;
            this.until = Objects.requireNonNull(until, "until cannot be null");
            this.after = after != null ? List.copyOf(after) : Collections.emptyList();
        }

        public String getVariable() {
            return variable;
        }

        public Expression getFrom() {
            return from;
        }

        public Expression getBy() {
            return by;
        }

        public Expression getUntil() {
            return until;
        }

        public List<VaryingClause> getAfter() {
            return after;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VaryingClause that)) return false;
            return Objects.equals(variable, that.variable) &&
                    Objects.equals(from, that.from) &&
                    Objects.equals(by, that.by) &&
                    Objects.equals(until, that.until) &&
                    Objects.equals(after, that.after);
        }

        @Override
        public int hashCode() {
            return Objects.hash(variable, from, by, until, after);
        }
    }
}
