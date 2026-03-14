/*
 * SourceMetadata - Metadata for entire program
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Metadata about the source program and compilation.
 */
public final class SourceMetadata {
    private final String sourceFile;
    private final Instant compiledAt;
    private final String compilerVersion;
    private final CobolDialect dialect;
    private final Map<String, String> properties;

    @JsonCreator
    public SourceMetadata(
            @JsonProperty("sourceFile") String sourceFile,
            @JsonProperty("compiledAt") Instant compiledAt,
            @JsonProperty("compilerVersion") String compilerVersion,
            @JsonProperty("dialect") CobolDialect dialect,
            @JsonProperty("properties") Map<String, String> properties) {
        this.sourceFile = sourceFile;
        this.compiledAt = compiledAt;
        this.compilerVersion = compilerVersion;
        this.dialect = dialect != null ? dialect : CobolDialect.ANSI_85;
        this.properties = properties != null ?
                Map.copyOf(properties) : Collections.emptyMap();
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public Instant getCompiledAt() {
        return compiledAt;
    }

    public String getCompilerVersion() {
        return compilerVersion;
    }

    public CobolDialect getDialect() {
        return dialect;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SourceMetadata that)) return false;
        return Objects.equals(sourceFile, that.sourceFile) &&
                Objects.equals(compiledAt, that.compiledAt) &&
                Objects.equals(compilerVersion, that.compilerVersion) &&
                dialect == that.dialect &&
                Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceFile, compiledAt, compilerVersion, dialect, properties);
    }

    public enum CobolDialect {
        ANSI_68,
        ANSI_74,
        ANSI_85,
        ANSI_2002,
        ANSI_2014,
        IBM_ENTERPRISE,
        MICRO_FOCUS,
        GNU_COBOL,
        UNISYS,
        FUJITSU
    }
}
