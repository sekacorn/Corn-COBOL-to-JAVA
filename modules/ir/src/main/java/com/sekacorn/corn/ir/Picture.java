/*
 * Picture - COBOL PICTURE clause representation
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents a COBOL PICTURE clause with parsed structure.
 * Immutable value object.
 */
public final class Picture {
    private final String raw;
    private final PictureCategory category;
    private final int length;
    private final int scale;  // Number of decimal places
    private final boolean signed;
    private final boolean hasEditSymbols;
    private final String editPattern;  // For edited pictures (e.g., "$$$,$$9.99")

    @JsonCreator
    public Picture(
            @JsonProperty("raw") String raw,
            @JsonProperty("category") PictureCategory category,
            @JsonProperty("length") int length,
            @JsonProperty("scale") int scale,
            @JsonProperty("signed") boolean signed,
            @JsonProperty("hasEditSymbols") boolean hasEditSymbols,
            @JsonProperty("editPattern") String editPattern) {
        this.raw = Objects.requireNonNull(raw, "raw picture string cannot be null");
        this.category = Objects.requireNonNull(category, "category cannot be null");
        this.length = length;
        this.scale = scale;
        this.signed = signed;
        this.hasEditSymbols = hasEditSymbols;
        this.editPattern = editPattern;
    }

    public String getRaw() {
        return raw;
    }

    public PictureCategory getCategory() {
        return category;
    }

    public int getLength() {
        return length;
    }

    public int getScale() {
        return scale;
    }

    public boolean isSigned() {
        return signed;
    }

    public boolean hasEditSymbols() {
        return hasEditSymbols;
    }

    public String getEditPattern() {
        return editPattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Picture picture)) return false;
        return length == picture.length &&
                scale == picture.scale &&
                signed == picture.signed &&
                hasEditSymbols == picture.hasEditSymbols &&
                Objects.equals(raw, picture.raw) &&
                category == picture.category &&
                Objects.equals(editPattern, picture.editPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raw, category, length, scale, signed, hasEditSymbols, editPattern);
    }

    @Override
    public String toString() {
        return String.format("Picture{%s, category=%s, length=%d, scale=%d}",
                raw, category, length, scale);
    }

    /**
     * Picture category according to COBOL semantics
     */
    public enum PictureCategory {
        ALPHABETIC,       // A
        ALPHANUMERIC,     // X
        ALPHANUMERIC_EDITED,  // X with B,0,/
        NUMERIC,          // 9, S, V, P
        NUMERIC_EDITED,   // 9 with Z,*,$,+,-,CR,DB,,,..
        NATIONAL,         // N (for Unicode)
        BOOLEAN           // 1 (boolean)
    }

    /**
     * Builder for creating Picture instances with validation
     */
    public static class Builder {
        private String raw;
        private PictureCategory category;
        private int length;
        private int scale;
        private boolean signed;
        private boolean hasEditSymbols;
        private String editPattern;

        public Builder raw(String raw) {
            this.raw = raw;
            return this;
        }

        public Builder category(PictureCategory category) {
            this.category = category;
            return this;
        }

        public Builder length(int length) {
            this.length = length;
            return this;
        }

        public Builder scale(int scale) {
            this.scale = scale;
            return this;
        }

        public Builder signed(boolean signed) {
            this.signed = signed;
            return this;
        }

        public Builder hasEditSymbols(boolean hasEditSymbols) {
            this.hasEditSymbols = hasEditSymbols;
            return this;
        }

        public Builder editPattern(String editPattern) {
            this.editPattern = editPattern;
            return this;
        }

        public Picture build() {
            return new Picture(raw, category, length, scale, signed, hasEditSymbols, editPattern);
        }
    }
}
