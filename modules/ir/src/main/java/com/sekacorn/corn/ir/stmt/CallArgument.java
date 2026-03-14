/*
 * CallArgument - CALL argument with passing mode.
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir.stmt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sekacorn.corn.ir.expr.Expression;

import java.util.Objects;

/**
 * Represents a single argument to a CALL statement with its passing mode.
 */
public record CallArgument(
        @JsonProperty("expression") Expression expression,
        @JsonProperty("passingMode") PassingMode passingMode
) {
    @JsonCreator
    public CallArgument {
        Objects.requireNonNull(expression);
        if (passingMode == null) passingMode = PassingMode.BY_REFERENCE;
    }

    /**
     * COBOL parameter passing modes for CALL USING.
     */
    public enum PassingMode {
        BY_REFERENCE,
        BY_VALUE,
        BY_CONTENT
    }
}
