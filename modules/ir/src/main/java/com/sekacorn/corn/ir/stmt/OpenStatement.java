/*
 * OpenStatement - OPEN file I/O statement
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

public record OpenStatement(
        @JsonProperty("files") List<FileSpec> files,
        @JsonProperty("location") SourceLocation location
) implements Statement {
    @JsonCreator
    public OpenStatement {
        files = files != null ? List.copyOf(files) : Collections.emptyList();
    }

    @Override
    public SourceLocation getLocation() { return location; }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitOpen(this);
    }

    public record FileSpec(String fileName, OpenMode mode) {
        public enum OpenMode { INPUT, OUTPUT, I_O, EXTEND }
    }
}
