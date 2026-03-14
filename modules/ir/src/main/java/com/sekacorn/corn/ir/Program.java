/*
 * Program - Root IR node representing a complete COBOL program
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents a complete COBOL program in intermediate representation.
 * Immutable after construction for thread-safety.
 */
public final class Program {
    private final String programId;
    private final IdentificationDivision identification;
    private final EnvironmentDivision environment;
    private final DataDivision data;
    private final ProcedureDivision procedure;
    private final SourceMetadata metadata;

    @JsonCreator
    public Program(
            @JsonProperty("programId") String programId,
            @JsonProperty("identification") IdentificationDivision identification,
            @JsonProperty("environment") EnvironmentDivision environment,
            @JsonProperty("data") DataDivision data,
            @JsonProperty("procedure") ProcedureDivision procedure,
            @JsonProperty("metadata") SourceMetadata metadata) {
        this.programId = Objects.requireNonNull(programId, "programId cannot be null");
        this.identification = identification;
        this.environment = environment;
        this.data = Objects.requireNonNull(data, "data division cannot be null");
        this.procedure = Objects.requireNonNull(procedure, "procedure division cannot be null");
        this.metadata = Objects.requireNonNull(metadata, "metadata cannot be null");
    }

    public String getProgramId() {
        return programId;
    }

    public IdentificationDivision getIdentification() {
        return identification;
    }

    public EnvironmentDivision getEnvironment() {
        return environment;
    }

    public DataDivision getData() {
        return data;
    }

    public ProcedureDivision getProcedure() {
        return procedure;
    }

    public SourceMetadata getMetadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Program program)) return false;
        return Objects.equals(programId, program.programId) &&
                Objects.equals(identification, program.identification) &&
                Objects.equals(environment, program.environment) &&
                Objects.equals(data, program.data) &&
                Objects.equals(procedure, program.procedure) &&
                Objects.equals(metadata, program.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(programId, identification, environment, data, procedure, metadata);
    }

    @Override
    public String toString() {
        return "Program{" +
                "programId='" + programId + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}
