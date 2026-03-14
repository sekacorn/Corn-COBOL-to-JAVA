/*
 * ParseError - Diagnostic error from COBOL parsing
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.parser;

import com.sekacorn.corn.ir.SourceLocation;

/**
 * Represents a parsing error or warning with source location.
 */
public record ParseError(
        String message,
        SourceLocation location,
        Severity severity
) {
    public enum Severity { ERROR, WARNING }
}
