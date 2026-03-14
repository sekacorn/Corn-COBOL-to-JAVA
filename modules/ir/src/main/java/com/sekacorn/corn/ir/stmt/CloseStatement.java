/*
 * CloseStatement - CLOSE file I/O statement
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir.stmt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sekacorn.corn.ir.SourceLocation;

import java.util.Collections;
import java.util.List;

public record CloseStatement(
        @JsonProperty("fileNames") List<String> fileNames,
        @JsonProperty("location") SourceLocation location
) implements Statement {
    @JsonCreator
    public CloseStatement {
        fileNames = fileNames != null ? List.copyOf(fileNames) : Collections.emptyList();
    }

    @Override
    public SourceLocation getLocation() { return location; }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitClose(this);
    }
}
