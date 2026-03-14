/*
 * SourceLocation - Source code position tracking
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents a location in COBOL source code.
 * Used for error reporting and source mapping.
 */
public final class SourceLocation {
    private final String fileName;
    private final int startLine;
    private final int startColumn;
    private final int endLine;
    private final int endColumn;

    @JsonCreator
    public SourceLocation(
            @JsonProperty("fileName") String fileName,
            @JsonProperty("startLine") int startLine,
            @JsonProperty("startColumn") int startColumn,
            @JsonProperty("endLine") int endLine,
            @JsonProperty("endColumn") int endColumn) {
        this.fileName = fileName;
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }

    public static SourceLocation of(String fileName, int line, int column) {
        return new SourceLocation(fileName, line, column, line, column);
    }

    public static SourceLocation range(String fileName, int startLine, int startCol,
                                       int endLine, int endCol) {
        return new SourceLocation(fileName, startLine, startCol, endLine, endCol);
    }

    public String getFileName() {
        return fileName;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getEndColumn() {
        return endColumn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SourceLocation that)) return false;
        return startLine == that.startLine &&
                startColumn == that.startColumn &&
                endLine == that.endLine &&
                endColumn == that.endColumn &&
                Objects.equals(fileName, that.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, startLine, startColumn, endLine, endColumn);
    }

    @Override
    public String toString() {
        return String.format("%s:%d:%d", fileName, startLine, startColumn);
    }
}
