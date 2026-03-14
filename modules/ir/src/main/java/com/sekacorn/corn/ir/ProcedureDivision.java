/*
 * ProcedureDivision - Executable code structure
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the PROCEDURE DIVISION of a COBOL program.
 * Contains sections and paragraphs with executable statements.
 */
public final class ProcedureDivision {
    private final List<String> usingParameters;
    private final String returning;
    private final List<Section> sections;
    private final List<Paragraph> paragraphs;  // For programs without sections

    @JsonCreator
    public ProcedureDivision(
            @JsonProperty("usingParameters") List<String> usingParameters,
            @JsonProperty("returning") String returning,
            @JsonProperty("sections") List<Section> sections,
            @JsonProperty("paragraphs") List<Paragraph> paragraphs) {
        this.usingParameters = usingParameters != null ?
                List.copyOf(usingParameters) : Collections.emptyList();
        this.returning = returning;
        this.sections = sections != null ? List.copyOf(sections) : Collections.emptyList();
        this.paragraphs = paragraphs != null ? List.copyOf(paragraphs) : Collections.emptyList();
    }

    public List<String> getUsingParameters() {
        return usingParameters;
    }

    public String getReturning() {
        return returning;
    }

    public List<Section> getSections() {
        return sections;
    }

    public List<Paragraph> getParagraphs() {
        return paragraphs;
    }

    @JsonIgnore
    public boolean hasSections() {
        return !sections.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProcedureDivision that)) return false;
        return Objects.equals(usingParameters, that.usingParameters) &&
                Objects.equals(returning, that.returning) &&
                Objects.equals(sections, that.sections) &&
                Objects.equals(paragraphs, that.paragraphs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usingParameters, returning, sections, paragraphs);
    }

    /**
     * Section containing multiple paragraphs
     */
    public static final class Section {
        private final String name;
        private final int priority;  // For SECTION priority (0-99)
        private final List<Paragraph> paragraphs;

        @JsonCreator
        public Section(
                @JsonProperty("name") String name,
                @JsonProperty("priority") int priority,
                @JsonProperty("paragraphs") List<Paragraph> paragraphs) {
            this.name = Objects.requireNonNull(name, "name cannot be null");
            this.priority = priority;
            this.paragraphs = paragraphs != null ? List.copyOf(paragraphs) : Collections.emptyList();
        }

        public String getName() {
            return name;
        }

        public int getPriority() {
            return priority;
        }

        public List<Paragraph> getParagraphs() {
            return paragraphs;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Section section)) return false;
            return priority == section.priority &&
                    Objects.equals(name, section.name) &&
                    Objects.equals(paragraphs, section.paragraphs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, priority, paragraphs);
        }

        @Override
        public String toString() {
            return String.format("Section{name='%s', paragraphs=%d}", name, paragraphs.size());
        }
    }
}
