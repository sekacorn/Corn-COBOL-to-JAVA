/*
 * SortStatement - SORT / MERGE statement IR node
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir.stmt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sekacorn.corn.ir.SourceLocation;

import java.util.List;
import java.util.Objects;

/**
 * IR node for SORT and MERGE statements.
 * Code generation emits a stub comment; real sort execution is a runtime concern.
 */
public record SortStatement(
        @JsonProperty("sortFile") String sortFile,
        @JsonProperty("isMerge") boolean isMerge,
        @JsonProperty("keyFields") List<String> keyFields,
        @JsonProperty("inputFiles") List<String> inputFiles,
        @JsonProperty("outputFiles") List<String> outputFiles,
        @JsonProperty("inputProcedure") String inputProcedure,
        @JsonProperty("outputProcedure") String outputProcedure,
        @JsonProperty("location") SourceLocation location
) implements Statement {

    @JsonCreator
    public SortStatement {
        Objects.requireNonNull(sortFile);
        keyFields = keyFields != null ? List.copyOf(keyFields) : List.of();
        inputFiles = inputFiles != null ? List.copyOf(inputFiles) : List.of();
        outputFiles = outputFiles != null ? List.copyOf(outputFiles) : List.of();
    }

    @Override
    public SourceLocation getLocation() { return location; }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitSort(this);
    }
}
