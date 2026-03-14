/*
 * VariableRef - Variable reference expression
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sekacorn.corn.ir.SourceLocation;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a reference to a variable (data item).
 * May include qualification and reference modification.
 */
public final class VariableRef implements Expression {
    private final String name;
    private final String qualifier;  // For OF/IN qualification
    private final ReferenceModification refMod;  // For substring (start:length)
    private final SourceLocation location;

    @JsonCreator
    public VariableRef(
            @JsonProperty("name") String name,
            @JsonProperty("qualifier") String qualifier,
            @JsonProperty("refMod") ReferenceModification refMod,
            @JsonProperty("location") SourceLocation location) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.qualifier = qualifier;
        this.refMod = refMod;
        this.location = location;
    }

    public static VariableRef simple(String name, SourceLocation location) {
        return new VariableRef(name, null, null, location);
    }

    public String getName() {
        return name;
    }

    public Optional<String> getQualifier() {
        return Optional.ofNullable(qualifier);
    }

    public Optional<ReferenceModification> getRefMod() {
        return Optional.ofNullable(refMod);
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitVariable(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VariableRef that)) return false;
        return Objects.equals(name, that.name) &&
                Objects.equals(qualifier, that.qualifier) &&
                Objects.equals(refMod, that.refMod) &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, qualifier, refMod, location);
    }

    @Override
    public String toString() {
        return String.format("VariableRef{%s%s%s}",
                name,
                qualifier != null ? " OF " + qualifier : "",
                refMod != null ? refMod : "");
    }

    /**
     * Reference modification for substring operations
     */
    public static final class ReferenceModification {
        private final Expression start;
        private final Expression length;

        @JsonCreator
        public ReferenceModification(
                @JsonProperty("start") Expression start,
                @JsonProperty("length") Expression length) {
            this.start = Objects.requireNonNull(start, "start cannot be null");
            this.length = length;
        }

        public Expression getStart() {
            return start;
        }

        public Optional<Expression> getLength() {
            return Optional.ofNullable(length);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ReferenceModification that)) return false;
            return Objects.equals(start, that.start) &&
                    Objects.equals(length, that.length);
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, length);
        }

        @Override
        public String toString() {
            return String.format("(%s:%s)", start, length != null ? length : "");
        }
    }
}
