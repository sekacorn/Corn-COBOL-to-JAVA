/*
 * EvaluateStatement - EVALUATE control flow statement
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

// EVALUATE Statement (COBOL's switch/case)
public record EvaluateStatement(
        @JsonProperty("subjects") List<Expression> subjects,
        @JsonProperty("whenClauses") List<WhenClause> whenClauses,
        @JsonProperty("whenOther") List<Statement> whenOther,
        @JsonProperty("location") SourceLocation location
) implements Statement {
    @JsonCreator
    public EvaluateStatement {
        subjects = subjects != null ? List.copyOf(subjects) : Collections.emptyList();
        whenClauses = whenClauses != null ? List.copyOf(whenClauses) : Collections.emptyList();
        whenOther = whenOther != null ? List.copyOf(whenOther) : Collections.emptyList();
    }

    @Override
    public SourceLocation getLocation() { return location; }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitEvaluate(this);
    }

    public record WhenClause(List<Expression> conditions, List<Statement> statements) {
        @JsonCreator
        public WhenClause {
            conditions = conditions != null ? List.copyOf(conditions) : Collections.emptyList();
            statements = statements != null ? List.copyOf(statements) : Collections.emptyList();
        }
    }
}
