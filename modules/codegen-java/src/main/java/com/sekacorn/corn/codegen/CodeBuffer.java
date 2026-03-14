/*
 * CodeBuffer - Manages indentation, code assembly, and import tracking
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.codegen;

import java.util.Set;
import java.util.TreeSet;

public final class CodeBuffer {
    private final StringBuilder content = new StringBuilder();
    private int indentLevel;
    private static final String INDENT_UNIT = "    ";
    private final Set<String> imports = new TreeSet<>();
    private final Set<String> classFields = new TreeSet<>();

    public CodeBuffer() {
        this(0);
    }

    public CodeBuffer(int initialIndent) {
        this.indentLevel = initialIndent;
    }

    public CodeBuffer line(String text) {
        content.append(currentIndent()).append(text).append("\n");
        return this;
    }

    public CodeBuffer line(String fmt, Object... args) {
        content.append(currentIndent()).append(String.format(fmt, args)).append("\n");
        return this;
    }

    public CodeBuffer emptyLine() {
        content.append("\n");
        return this;
    }

    public CodeBuffer rawLine(String text) {
        content.append(text).append("\n");
        return this;
    }

    public CodeBuffer openBlock(String header) {
        content.append(currentIndent()).append(header).append(" {\n");
        indentLevel++;
        return this;
    }

    public CodeBuffer closeBlock() {
        indentLevel--;
        content.append(currentIndent()).append("}\n");
        return this;
    }

    public CodeBuffer closeBlockWith(String suffix) {
        indentLevel--;
        content.append(currentIndent()).append("} ").append(suffix).append("\n");
        return this;
    }

    public CodeBuffer addImport(String fqcn) {
        imports.add(fqcn);
        return this;
    }

    /**
     * Register a class-level field declaration (e.g. shared Scanner).
     * Duplicates are ignored. Fields are rendered before the main body.
     */
    public CodeBuffer addClassField(String fieldDeclaration) {
        classFields.add(fieldDeclaration);
        return this;
    }

    public Set<String> getClassFields() {
        return Set.copyOf(classFields);
    }

    public Set<String> getImports() {
        return Set.copyOf(imports);
    }

    public String getContent() {
        return content.toString();
    }

    public int getIndentLevel() {
        return indentLevel;
    }

    public CodeBuffer indent() {
        indentLevel++;
        return this;
    }

    public CodeBuffer dedent() {
        if (indentLevel > 0) indentLevel--;
        return this;
    }

    private String currentIndent() {
        return INDENT_UNIT.repeat(indentLevel);
    }
}
