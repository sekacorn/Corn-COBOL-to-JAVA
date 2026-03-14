/*
 * ArithmeticContext - Context for COBOL arithmetic operations
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.runtime;

/**
 * Carries context from the COBOL source for arithmetic operations.
 * Encapsulates target field precision, rounding mode, and size error handling
 * so that generated Java code faithfully preserves COBOL arithmetic semantics.
 */
public record ArithmeticContext(
        int targetScale,
        int targetPrecision,
        boolean rounded,
        CobolMath.RoundMode roundMode,
        boolean onSizeErrorActive
) {
    /**
     * Create a basic context from target field's PICTURE.
     * No rounding, no size error handler.
     */
    public static ArithmeticContext ofPicture(int scale, int precision) {
        return new ArithmeticContext(scale, precision, false, null, false);
    }

    /**
     * Derive a new context with ROUNDED enabled using the specified mode.
     */
    public ArithmeticContext withRounded(CobolMath.RoundMode mode) {
        return new ArithmeticContext(targetScale, targetPrecision, true,
                mode != null ? mode : CobolMath.RoundMode.HALF_UP, onSizeErrorActive);
    }

    /**
     * Derive a new context with ON SIZE ERROR handler active.
     */
    public ArithmeticContext withSizeError() {
        return new ArithmeticContext(targetScale, targetPrecision, rounded, roundMode, true);
    }
}
