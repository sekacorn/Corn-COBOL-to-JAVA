/*
 * IdentificationDivision - Program metadata and identification
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents the IDENTIFICATION DIVISION of a COBOL program.
 */
public final class IdentificationDivision {
    private final String programId;
    private final String author;
    private final String dateWritten;
    private final String dateCompiled;
    private final String security;
    private final String remarks;

    @JsonCreator
    public IdentificationDivision(
            @JsonProperty("programId") String programId,
            @JsonProperty("author") String author,
            @JsonProperty("dateWritten") String dateWritten,
            @JsonProperty("dateCompiled") String dateCompiled,
            @JsonProperty("security") String security,
            @JsonProperty("remarks") String remarks) {
        this.programId = Objects.requireNonNull(programId, "programId cannot be null");
        this.author = author;
        this.dateWritten = dateWritten;
        this.dateCompiled = dateCompiled;
        this.security = security;
        this.remarks = remarks;
    }

    public String getProgramId() {
        return programId;
    }

    public Optional<String> getAuthor() {
        return Optional.ofNullable(author);
    }

    public Optional<String> getDateWritten() {
        return Optional.ofNullable(dateWritten);
    }

    public Optional<String> getDateCompiled() {
        return Optional.ofNullable(dateCompiled);
    }

    public Optional<String> getSecurity() {
        return Optional.ofNullable(security);
    }

    public Optional<String> getRemarks() {
        return Optional.ofNullable(remarks);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdentificationDivision that)) return false;
        return Objects.equals(programId, that.programId) &&
                Objects.equals(author, that.author) &&
                Objects.equals(dateWritten, that.dateWritten) &&
                Objects.equals(dateCompiled, that.dateCompiled) &&
                Objects.equals(security, that.security) &&
                Objects.equals(remarks, that.remarks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(programId, author, dateWritten, dateCompiled, security, remarks);
    }
}
