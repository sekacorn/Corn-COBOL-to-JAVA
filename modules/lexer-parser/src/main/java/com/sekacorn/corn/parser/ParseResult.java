/*
 * ParseResult - Result of parsing a COBOL source file
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.parser;

import com.sekacorn.corn.ir.Program;

import java.util.List;

/**
 * Wraps the parsed Program IR along with any errors encountered.
 */
public record ParseResult(
        Program program,
        List<ParseError> errors
) {
    public boolean hasErrors() {
        return errors.stream().anyMatch(e -> e.severity() == ParseError.Severity.ERROR);
    }
}
