/*
 * SubscriptRef - Array/table subscript reference
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sekacorn.corn.ir.SourceLocation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a subscripted reference to an array element.
 */
public final class SubscriptRef implements Expression {
    private final String name;
    private final List<Expression> subscripts;
    private final SourceLocation location;

    @JsonCreator
    public SubscriptRef(
            @JsonProperty("name") String name,
            @JsonProperty("subscripts") List<Expression> subscripts,
            @JsonProperty("location") SourceLocation location) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.subscripts = subscripts != null ? List.copyOf(subscripts) : Collections.emptyList();
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public List<Expression> getSubscripts() {
        return subscripts;
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitSubscript(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscriptRef that)) return false;
        return Objects.equals(name, that.name) &&
                Objects.equals(subscripts, that.subscripts) &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, subscripts, location);
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", name, subscripts);
    }
}
