/*
 * OccursClause - Array/Table definition
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents an OCCURS clause for arrays and tables.
 */
public final class OccursClause {
    private final int minOccurs;
    private final Integer maxOccurs;  // Null for fixed OCCURS
    private final String dependingOn;  // Variable name for OCCURS DEPENDING ON
    private final String indexedBy;    // Index name for INDEXED BY

    @JsonCreator
    public OccursClause(
            @JsonProperty("minOccurs") int minOccurs,
            @JsonProperty("maxOccurs") Integer maxOccurs,
            @JsonProperty("dependingOn") String dependingOn,
            @JsonProperty("indexedBy") String indexedBy) {
        if (minOccurs < 1) {
            throw new IllegalArgumentException("minOccurs must be >= 1");
        }
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
        this.dependingOn = dependingOn;
        this.indexedBy = indexedBy;
    }

    public static OccursClause fixed(int times) {
        return new OccursClause(times, null, null, null);
    }

    public static OccursClause variable(int min, int max, String dependingOn) {
        return new OccursClause(min, max, dependingOn, null);
    }

    public int getMinOccurs() {
        return minOccurs;
    }

    public Optional<Integer> getMaxOccurs() {
        return Optional.ofNullable(maxOccurs);
    }

    public Optional<String> getDependingOn() {
        return Optional.ofNullable(dependingOn);
    }

    public Optional<String> getIndexedBy() {
        return Optional.ofNullable(indexedBy);
    }

    @JsonIgnore
    public boolean isFixed() {
        return maxOccurs == null;
    }

    @JsonIgnore
    public boolean isDynamic() {
        return dependingOn != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OccursClause that)) return false;
        return minOccurs == that.minOccurs &&
                Objects.equals(maxOccurs, that.maxOccurs) &&
                Objects.equals(dependingOn, that.dependingOn) &&
                Objects.equals(indexedBy, that.indexedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minOccurs, maxOccurs, dependingOn, indexedBy);
    }

    @Override
    public String toString() {
        return String.format("OCCURS %d%s%s",
                minOccurs,
                maxOccurs != null ? " TO " + maxOccurs : "",
                dependingOn != null ? " DEPENDING ON " + dependingOn : "");
    }
}
