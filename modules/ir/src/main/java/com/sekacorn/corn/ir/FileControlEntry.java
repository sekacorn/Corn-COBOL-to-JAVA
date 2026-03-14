/*
 * FileControlEntry - File definition from ENVIRONMENT DIVISION
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a file control entry from the FILE-CONTROL paragraph.
 * Maps to a physical file and specifies organization and access mode.
 */
public final class FileControlEntry {
    private final String fileName;
    private final String assignTo;
    private final FileOrganization organization;
    private final AccessMode accessMode;
    private final String recordKey;
    private final String alternateRecordKey;
    private final List<AlternateKey> alternateKeys;
    private final String relativeKey;
    private final String fileStatus;

    @JsonCreator
    public FileControlEntry(
            @JsonProperty("fileName") String fileName,
            @JsonProperty("assignTo") String assignTo,
            @JsonProperty("organization") FileOrganization organization,
            @JsonProperty("accessMode") AccessMode accessMode,
            @JsonProperty("recordKey") String recordKey,
            @JsonProperty("alternateRecordKey") String alternateRecordKey,
            @JsonProperty("alternateKeys") List<AlternateKey> alternateKeys,
            @JsonProperty("relativeKey") String relativeKey,
            @JsonProperty("fileStatus") String fileStatus) {
        this.fileName = Objects.requireNonNull(fileName, "fileName cannot be null");
        this.assignTo = assignTo;
        this.organization = organization != null ? organization : FileOrganization.SEQUENTIAL;
        this.accessMode = accessMode != null ? accessMode : AccessMode.SEQUENTIAL;
        this.recordKey = recordKey;
        this.alternateRecordKey = alternateRecordKey;
        this.alternateKeys = alternateKeys != null ? List.copyOf(alternateKeys) : Collections.emptyList();
        this.relativeKey = relativeKey;
        this.fileStatus = fileStatus;
    }

    public String getFileName() {
        return fileName;
    }

    public Optional<String> getAssignTo() {
        return Optional.ofNullable(assignTo);
    }

    public FileOrganization getOrganization() {
        return organization;
    }

    public AccessMode getAccessMode() {
        return accessMode;
    }

    public Optional<String> getRecordKey() {
        return Optional.ofNullable(recordKey);
    }

    public Optional<String> getAlternateRecordKey() {
        return Optional.ofNullable(alternateRecordKey);
    }

    public List<AlternateKey> getAlternateKeys() {
        return alternateKeys;
    }

    public Optional<String> getRelativeKey() {
        return Optional.ofNullable(relativeKey);
    }

    public Optional<String> getFileStatus() {
        return Optional.ofNullable(fileStatus);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileControlEntry that)) return false;
        return Objects.equals(fileName, that.fileName) &&
                Objects.equals(assignTo, that.assignTo) &&
                organization == that.organization &&
                accessMode == that.accessMode &&
                Objects.equals(recordKey, that.recordKey) &&
                Objects.equals(alternateRecordKey, that.alternateRecordKey) &&
                Objects.equals(alternateKeys, that.alternateKeys) &&
                Objects.equals(relativeKey, that.relativeKey) &&
                Objects.equals(fileStatus, that.fileStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, assignTo, organization, accessMode,
                recordKey, alternateRecordKey, alternateKeys, relativeKey, fileStatus);
    }

    public enum FileOrganization {
        SEQUENTIAL,
        INDEXED,
        RELATIVE
    }

    public enum AccessMode {
        SEQUENTIAL,
        RANDOM,
        DYNAMIC
    }

    /**
     * Represents an alternate record key with optional duplicates support.
     */
    public record AlternateKey(
            @JsonProperty("keyName") String keyName,
            @JsonProperty("allowDuplicates") boolean allowDuplicates
    ) {
        @JsonCreator
        public AlternateKey {
            Objects.requireNonNull(keyName, "keyName cannot be null");
        }
    }
}
