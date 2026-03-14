/*
 * RoundMode - Rounding mode for arithmetic operations
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir.stmt;

/**
 * Rounding mode for arithmetic operations.
 * Maps to COBOL ROUNDED MODE IS clause (COBOL 2002+).
 */
public enum RoundMode {
    TRUNCATION,
    HALF_UP,
    HALF_DOWN,
    HALF_EVEN,
    UP,
    DOWN,
    CEILING,
    FLOOR
}
