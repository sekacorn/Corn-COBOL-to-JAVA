/*
 * DataDivision - Complete data model with all sections
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

/**
 * Represents the DATA DIVISION of a COBOL program.
 * Contains file, working-storage, linkage, and local-storage sections.
 */
public final class DataDivision {
    private final List<FileSection> fileSection;
    private final List<DataItem> workingStorage;
    private final List<DataItem> linkageSection;
    private final List<DataItem> localStorage;

    @JsonCreator
    public DataDivision(
            @JsonProperty("fileSection") List<FileSection> fileSection,
            @JsonProperty("workingStorage") List<DataItem> workingStorage,
            @JsonProperty("linkageSection") List<DataItem> linkageSection,
            @JsonProperty("localStorage") List<DataItem> localStorage) {
        this.fileSection = fileSection != null ? List.copyOf(fileSection) : Collections.emptyList();
        this.workingStorage = workingStorage != null ? List.copyOf(workingStorage) : Collections.emptyList();
        this.linkageSection = linkageSection != null ? List.copyOf(linkageSection) : Collections.emptyList();
        this.localStorage = localStorage != null ? List.copyOf(localStorage) : Collections.emptyList();
    }

    public List<FileSection> getFileSection() {
        return fileSection;
    }

    public List<DataItem> getWorkingStorage() {
        return workingStorage;
    }

    public List<DataItem> getLinkageSection() {
        return linkageSection;
    }

    public List<DataItem> getLocalStorage() {
        return localStorage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataDivision that)) return false;
        return Objects.equals(fileSection, that.fileSection) &&
                Objects.equals(workingStorage, that.workingStorage) &&
                Objects.equals(linkageSection, that.linkageSection) &&
                Objects.equals(localStorage, that.localStorage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileSection, workingStorage, linkageSection, localStorage);
    }

    /**
     * File section entry containing record descriptions
     */
    public static final class FileSection {
        private final String fileName;
        private final List<DataItem> records;

        @JsonCreator
        public FileSection(
                @JsonProperty("fileName") String fileName,
                @JsonProperty("records") List<DataItem> records) {
            this.fileName = Objects.requireNonNull(fileName, "fileName cannot be null");
            this.records = records != null ? List.copyOf(records) : Collections.emptyList();
        }

        public String getFileName() {
            return fileName;
        }

        public List<DataItem> getRecords() {
            return records;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FileSection that)) return false;
            return Objects.equals(fileName, that.fileName) &&
                    Objects.equals(records, that.records);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fileName, records);
        }
    }
}
