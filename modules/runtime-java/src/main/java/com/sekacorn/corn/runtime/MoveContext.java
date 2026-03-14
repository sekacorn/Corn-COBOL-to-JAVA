/*
 * MoveContext - Context for COBOL MOVE operations
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.runtime;

/**
 * Carries context from the COBOL source for MOVE operations.
 * Encapsulates target field attributes (JUSTIFIED, BLANK WHEN ZERO, sign position)
 * so that generated Java code faithfully preserves COBOL data movement semantics.
 */
public record MoveContext(
        int targetLength,
        boolean justified,
        boolean blankWhenZero,
        SignPosition sign
) {
    /**
     * Create a context for alphanumeric MOVE.
     */
    public static MoveContext alphanumeric(int targetLength, boolean justified) {
        return new MoveContext(targetLength, justified, false, null);
    }

    /**
     * Create a context for numeric display MOVE.
     */
    public static MoveContext numeric(int targetLength, boolean blankWhenZero) {
        return new MoveContext(targetLength, false, blankWhenZero, null);
    }

    /**
     * Derive a new context with sign position set.
     */
    public MoveContext withSign(SignPosition signPos) {
        return new MoveContext(targetLength, justified, blankWhenZero, signPos);
    }

    /**
     * COBOL sign position options for numeric display fields.
     */
    public enum SignPosition {
        LEADING,
        TRAILING,
        LEADING_SEPARATE,
        TRAILING_SEPARATE
    }
}
