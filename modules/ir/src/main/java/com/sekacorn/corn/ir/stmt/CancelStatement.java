/*
 * CancelStatement - IR for COBOL CANCEL statement
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir.stmt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sekacorn.corn.ir.SourceLocation;
import java.util.List;
import java.util.Objects;

public record CancelStatement(
        @JsonProperty("programNames") List<String> programNames,
        @JsonProperty("location") SourceLocation location
) implements Statement {

    @JsonCreator
    public CancelStatement {
        programNames = programNames != null ? List.copyOf(programNames) : List.of();
    }

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitCancel(this);
    }
}
