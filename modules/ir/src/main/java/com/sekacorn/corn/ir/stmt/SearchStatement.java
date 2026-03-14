/*
 * SearchStatement - SEARCH statement IR node.
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

// SEARCH Statement
public record SearchStatement(
        @JsonProperty("tableName") String tableName,
        @JsonProperty("searchAll") boolean searchAll,
        @JsonProperty("varying") Expression varying,
        @JsonProperty("atEnd") List<Statement> atEnd,
        @JsonProperty("whenClauses") List<WhenClause> whenClauses,
        @JsonProperty("location") SourceLocation location
) implements Statement {
    @JsonCreator
    public SearchStatement {
        Objects.requireNonNull(tableName);
        atEnd = atEnd != null ? List.copyOf(atEnd) : Collections.emptyList();
        whenClauses = whenClauses != null ? List.copyOf(whenClauses) : Collections.emptyList();
    }

    @Override
    public SourceLocation getLocation() { return location; }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitSearch(this);
    }

    public record WhenClause(Expression condition, List<Statement> statements) {
        @JsonCreator
        public WhenClause {
            Objects.requireNonNull(condition);
            statements = statements != null ? List.copyOf(statements) : Collections.emptyList();
        }
    }
}
