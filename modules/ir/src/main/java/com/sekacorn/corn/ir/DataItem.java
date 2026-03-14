/*
 * DataItem - Individual data item (field/record) definition
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
import java.util.Optional;

/**
 * Represents a single data item (elementary or group).
 * Can have children (for group items) and various clauses.
 */
public final class DataItem {
    private final int levelNumber;
    private final String name;
    private final Picture picture;
    private final Usage usage;
    private final Sign sign;
    private final String value;
    private final OccursClause occurs;
    private final String redefines;
    private final List<DataItem> children;
    private final List<ConditionName> conditionNames;
    private final boolean isFiller;
    private final boolean justified;
    private final boolean blankWhenZero;
    private final boolean synchronizedField;

    @JsonCreator
    public DataItem(
            @JsonProperty("levelNumber") int levelNumber,
            @JsonProperty("name") String name,
            @JsonProperty("picture") Picture picture,
            @JsonProperty("usage") Usage usage,
            @JsonProperty("sign") Sign sign,
            @JsonProperty("value") String value,
            @JsonProperty("occurs") OccursClause occurs,
            @JsonProperty("redefines") String redefines,
            @JsonProperty("children") List<DataItem> children,
            @JsonProperty("conditionNames") List<ConditionName> conditionNames,
            @JsonProperty("isFiller") boolean isFiller,
            @JsonProperty("justified") boolean justified,
            @JsonProperty("blankWhenZero") boolean blankWhenZero,
            @JsonProperty("synchronizedField") boolean synchronizedField) {
        if (levelNumber < 1 || levelNumber > 88) {
            throw new IllegalArgumentException("Invalid level number: " + levelNumber);
        }
        this.levelNumber = levelNumber;
        this.name = name;
        this.picture = picture;
        this.usage = usage;
        this.sign = sign;
        this.value = value;
        this.occurs = occurs;
        this.redefines = redefines;
        this.children = children != null ? List.copyOf(children) : Collections.emptyList();
        this.conditionNames = conditionNames != null ? List.copyOf(conditionNames) : Collections.emptyList();
        this.isFiller = isFiller;
        this.justified = justified;
        this.blankWhenZero = blankWhenZero;
        this.synchronizedField = synchronizedField;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public String getName() {
        return name;
    }

    public Optional<Picture> getPicture() {
        return Optional.ofNullable(picture);
    }

    public Optional<Usage> getUsage() {
        return Optional.ofNullable(usage);
    }

    public Optional<Sign> getSign() {
        return Optional.ofNullable(sign);
    }

    public Optional<String> getValue() {
        return Optional.ofNullable(value);
    }

    public Optional<OccursClause> getOccurs() {
        return Optional.ofNullable(occurs);
    }

    public Optional<String> getRedefines() {
        return Optional.ofNullable(redefines);
    }

    public List<DataItem> getChildren() {
        return children;
    }

    public List<ConditionName> getConditionNames() {
        return conditionNames;
    }

    @JsonProperty("isFiller")
    public boolean isFiller() {
        return isFiller;
    }

    public boolean isJustified() {
        return justified;
    }

    public boolean isBlankWhenZero() {
        return blankWhenZero;
    }

    @JsonProperty("synchronizedField")
    public boolean isSynchronizedField() {
        return synchronizedField;
    }

    @JsonIgnore
    public boolean isGroup() {
        return !children.isEmpty();
    }

    @JsonIgnore
    public boolean isElementary() {
        return children.isEmpty() && picture != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataItem dataItem)) return false;
        return levelNumber == dataItem.levelNumber &&
                isFiller == dataItem.isFiller &&
                justified == dataItem.justified &&
                blankWhenZero == dataItem.blankWhenZero &&
                synchronizedField == dataItem.synchronizedField &&
                Objects.equals(name, dataItem.name) &&
                Objects.equals(picture, dataItem.picture) &&
                usage == dataItem.usage &&
                sign == dataItem.sign &&
                Objects.equals(value, dataItem.value) &&
                Objects.equals(occurs, dataItem.occurs) &&
                Objects.equals(redefines, dataItem.redefines) &&
                Objects.equals(children, dataItem.children) &&
                Objects.equals(conditionNames, dataItem.conditionNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(levelNumber, name, picture, usage, sign, value,
                occurs, redefines, children, conditionNames, isFiller,
                justified, blankWhenZero, synchronizedField);
    }

    @Override
    public String toString() {
        return String.format("DataItem{level=%d, name='%s', picture=%s, isGroup=%b}",
                levelNumber, name, picture, isGroup());
    }

    public enum Usage {
        DISPLAY,
        COMP,
        COMP_1,
        COMP_2,
        COMP_3,
        COMP_4,
        COMP_5,
        BINARY,
        PACKED_DECIMAL,
        INDEX,
        POINTER,
        FUNCTION_POINTER,
        PROCEDURE_POINTER
    }

    public enum Sign {
        LEADING,
        TRAILING,
        LEADING_SEPARATE,
        TRAILING_SEPARATE
    }
}
