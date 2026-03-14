/*
 * ConditionName - 88-level condition name
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
 * Represents an 88-level condition name (boolean predicate).
 * Maps to specific values or ranges of the parent data item.
 */
public final class ConditionName {
    private final String name;
    private final List<ValueSpec> values;

    @JsonCreator
    public ConditionName(
            @JsonProperty("name") String name,
            @JsonProperty("values") List<ValueSpec> values) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.values = values != null ? List.copyOf(values) : Collections.emptyList();
    }

    public String getName() {
        return name;
    }

    public List<ValueSpec> getValues() {
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConditionName that)) return false;
        return Objects.equals(name, that.name) &&
                Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, values);
    }

    @Override
    public String toString() {
        return String.format("88 %s VALUE %s", name, values);
    }

    /**
     * Value specification for condition names (single value or range)
     */
    public static final class ValueSpec {
        private final String value;
        private final String throughValue;  // For THRU ranges

        @JsonCreator
        public ValueSpec(
                @JsonProperty("value") String value,
                @JsonProperty("throughValue") String throughValue) {
            this.value = Objects.requireNonNull(value, "value cannot be null");
            this.throughValue = throughValue;
        }

        public static ValueSpec single(String value) {
            return new ValueSpec(value, null);
        }

        public static ValueSpec range(String from, String through) {
            return new ValueSpec(from, through);
        }

        public String getValue() {
            return value;
        }

        public String getThroughValue() {
            return throughValue;
        }

        @JsonIgnore
        public boolean isRange() {
            return throughValue != null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ValueSpec valueSpec)) return false;
            return Objects.equals(value, valueSpec.value) &&
                    Objects.equals(throughValue, valueSpec.throughValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, throughValue);
        }

        @Override
        public String toString() {
            return throughValue != null ?
                    value + " THRU " + throughValue :
                    value;
        }
    }
}
