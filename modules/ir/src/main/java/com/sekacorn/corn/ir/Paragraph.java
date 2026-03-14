/*
 * Paragraph - Named block of statements
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sekacorn.corn.ir.stmt.Statement;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a paragraph (labeled block of statements) in COBOL.
 * Paragraphs are the basic unit of control flow.
 */
public final class Paragraph {
    private final String name;
    private final List<Statement> statements;
    private final SourceLocation location;

    @JsonCreator
    public Paragraph(
            @JsonProperty("name") String name,
            @JsonProperty("statements") List<Statement> statements,
            @JsonProperty("location") SourceLocation location) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.statements = statements != null ? List.copyOf(statements) : Collections.emptyList();
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Paragraph paragraph)) return false;
        return Objects.equals(name, paragraph.name) &&
                Objects.equals(statements, paragraph.statements) &&
                Objects.equals(location, paragraph.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, statements, location);
    }

    @Override
    public String toString() {
        return String.format("Paragraph{name='%s', statements=%d}", name, statements.size());
    }
}
