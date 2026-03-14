/*
 * StringStatement - COBOL STRING statement IR node.
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir.stmt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sekacorn.corn.ir.SourceLocation;
import com.sekacorn.corn.ir.expr.Expression;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

// STRING Statement
public record StringStatement(
        @JsonProperty("sources") List<Expression> sources,
        @JsonProperty("into") Expression into,
        @JsonProperty("pointer") Expression pointer,
        @JsonProperty("onOverflow") List<Statement> onOverflow,
        @JsonProperty("location") SourceLocation location
) implements Statement {
    @JsonCreator
    public StringStatement {
        sources = sources != null ? List.copyOf(sources) : Collections.emptyList();
        Objects.requireNonNull(into);
        onOverflow = onOverflow != null ? List.copyOf(onOverflow) : Collections.emptyList();
    }

    @Override
    public SourceLocation getLocation() { return location; }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitString(this);
    }
}
