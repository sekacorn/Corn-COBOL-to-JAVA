/*
 * FunctionCall - Intrinsic function call
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
 * Represents a call to an intrinsic COBOL function.
 */
public final class FunctionCall implements Expression {
    private final String functionName;
    private final List<Expression> arguments;
    private final SourceLocation location;

    @JsonCreator
    public FunctionCall(
            @JsonProperty("functionName") String functionName,
            @JsonProperty("arguments") List<Expression> arguments,
            @JsonProperty("location") SourceLocation location) {
        this.functionName = Objects.requireNonNull(functionName, "functionName cannot be null");
        this.arguments = arguments != null ? List.copyOf(arguments) : Collections.emptyList();
        this.location = location;
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitFunction(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FunctionCall that)) return false;
        return Objects.equals(functionName, that.functionName) &&
                Objects.equals(arguments, that.arguments) &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionName, arguments, location);
    }

    @Override
    public String toString() {
        return String.format("FUNCTION %s(%s)", functionName, arguments);
    }
}
