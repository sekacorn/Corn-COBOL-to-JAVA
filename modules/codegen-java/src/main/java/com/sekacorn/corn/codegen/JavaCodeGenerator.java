/*
 * JavaCodeGenerator - Public API facade for generating Java source from COBOL IR
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.codegen;

import com.sekacorn.corn.ir.*;
import com.sekacorn.corn.ir.stmt.Statement;

import java.util.*;

/**
 * Generates a complete Java source file from a COBOL Program IR.
 * <p>
 * Each COBOL program maps to one Java class with:
 * <ul>
 *   <li>Private fields for WORKING-STORAGE items</li>
 *   <li>A {@code run()} method that calls the first paragraph</li>
 *   <li>Private methods for each COBOL paragraph</li>
 *   <li>A {@code main()} entry point</li>
 * </ul>
 */
public final class JavaCodeGenerator {

    private static final String DEFAULT_PACKAGE = "com.generated.cobol";

    private String packageName = DEFAULT_PACKAGE;

    public JavaCodeGenerator() {}

    /**
     * Set the target package name for generated Java classes.
     */
    public JavaCodeGenerator withPackage(String packageName) {
        this.packageName = Objects.requireNonNull(packageName);
        return this;
    }

    /**
     * Generate a Java class from a COBOL Program IR.
     */
    public GeneratedClass generate(Program program) {
        Objects.requireNonNull(program, "program cannot be null");

        String className = JavaNameMapper.toClassName(program.getProgramId());
        CodeBuffer buffer = new CodeBuffer(1); // Start at indent level 1 (inside class body)

        // Build the data item lookup map
        Map<String, DataItem> dataItemMap = buildDataItemMap(program.getData());
        Map<String, String> fileStatusMap = buildFileStatusMap(program.getEnvironment());

        // Create visitors
        JavaExpressionVisitor exprVisitor = new JavaExpressionVisitor(dataItemMap, buffer);
        JavaStatementVisitor stmtVisitor = new JavaStatementVisitor(exprVisitor, dataItemMap, fileStatusMap, buffer);
        JavaFieldGenerator fieldGen = new JavaFieldGenerator(buffer);

        // Generate fields from WORKING-STORAGE
        generateFields(program.getData(), fieldGen);

        // Generate file fields
        generateFileFields(program.getData(), buffer);

        buffer.emptyLine();

        // Generate run() method
        generateRunMethod(program.getProcedure(), buffer);

        buffer.emptyLine();

        // Generate paragraph methods
        generateParagraphMethods(program.getProcedure(), stmtVisitor, buffer);

        // Generate main() method
        generateMainMethod(className, buffer);

        return new GeneratedClass(
                packageName,
                className,
                buffer.getImports(),
                buffer.getClassFields(),
                buffer.getContent(),
                program.getProgramId()
        );
    }

    private void generateFields(DataDivision data, JavaFieldGenerator fieldGen) {
        if (!data.getWorkingStorage().isEmpty()) {
            fieldGen.generateFields(data.getWorkingStorage());
        }
        if (!data.getLinkageSection().isEmpty()) {
            fieldGen.generateFields(data.getLinkageSection());
        }
        if (!data.getLocalStorage().isEmpty()) {
            fieldGen.generateFields(data.getLocalStorage());
        }
    }

    private void generateFileFields(DataDivision data, CodeBuffer buffer) {
        JavaFieldGenerator fieldGen = new JavaFieldGenerator(buffer);
        for (DataDivision.FileSection fs : data.getFileSection()) {
            buffer.addImport("com.sekacorn.corn.runtime.CobolFile");
            buffer.addImport("com.sekacorn.corn.runtime.SimpleCobolFile");
            String fieldName = JavaNameMapper.toFieldName(fs.getFileName());
            buffer.line("private CobolFile<String> %s = new SimpleCobolFile<>(\"%s\");",
                    fieldName, fs.getFileName());

            // Generate record fields from file section
            for (DataItem record : fs.getRecords()) {
                if (!record.isFiller()) {
                    String recName = JavaNameMapper.toFieldName(record.getName());
                    buffer.line("private String %s = \"\";", recName);
                    buffer.line("private CobolFile<String> %sFile = %s;", recName, fieldName);
                    if (record.isGroup() && !record.getChildren().isEmpty()) {
                        fieldGen.generateFields(record.getChildren());
                    }
                }
            }
        }
    }

    private void generateRunMethod(ProcedureDivision procedure, CodeBuffer buffer) {
        buffer.openBlock("public void run()");

        List<Paragraph> allParagraphs = getAllParagraphs(procedure);
        if (!allParagraphs.isEmpty()) {
            // Call the first paragraph to start execution
            String firstPara = toParagraphMethodName(allParagraphs.get(0).getName());
            buffer.line("%s();", firstPara);
        }

        buffer.closeBlock();
    }

    private void generateParagraphMethods(ProcedureDivision procedure,
                                          JavaStatementVisitor stmtVisitor,
                                          CodeBuffer buffer) {
        List<Paragraph> allParagraphs = getAllParagraphs(procedure);

        for (Paragraph para : allParagraphs) {
            String methodName = toParagraphMethodName(para.getName());
            buffer.emptyLine();
            buffer.openBlock("private void " + methodName + "()");
            stmtVisitor.generateStatements(para.getStatements());
            buffer.closeBlock();
        }
    }

    /**
     * Ensure a method name does not collide with reserved method names in
     * the generated class (e.g. "main", "run", "toString", "hashCode").
     */
    private static String safeMethodName(String name) {
        return switch (name) {
            case "main", "run", "toString", "hashCode", "equals",
                 "getClass", "notify", "notifyAll", "wait", "clone", "finalize"
                    -> name + "Para";
            default -> name;
        };
    }

    static String toParagraphMethodName(String cobolParagraphName) {
        return safeMethodName(JavaNameMapper.toMethodName(cobolParagraphName));
    }

    private void generateMainMethod(String className, CodeBuffer buffer) {
        buffer.emptyLine();
        buffer.openBlock("public static void main(String[] args)");
        buffer.line("new %s().run();", className);
        buffer.closeBlock();
    }

    /**
     * Collect all paragraphs from sections and top-level paragraphs.
     * Merges consecutive paragraphs with the same name (e.g. multiple _MAIN
     * paragraphs created from bare sentences).
     */
    private List<Paragraph> getAllParagraphs(ProcedureDivision procedure) {
        List<Paragraph> raw = new ArrayList<>();

        if (procedure.hasSections()) {
            for (ProcedureDivision.Section section : procedure.getSections()) {
                raw.addAll(section.getParagraphs());
            }
        }

        raw.addAll(procedure.getParagraphs());

        // Merge paragraphs that share the same name (preserves insertion order)
        Map<String, List<Statement>> merged = new LinkedHashMap<>();
        for (Paragraph p : raw) {
            merged.computeIfAbsent(p.getName(), k -> new ArrayList<>())
                  .addAll(p.getStatements());
        }

        List<Paragraph> result = new ArrayList<>();
        for (var entry : merged.entrySet()) {
            result.add(new Paragraph(entry.getKey(), entry.getValue(), null));
        }
        return result;
    }

    /**
     * Build a lookup map from COBOL names to DataItems for field type resolution.
     */
    private Map<String, DataItem> buildDataItemMap(DataDivision data) {
        Map<String, DataItem> map = new LinkedHashMap<>();
        addItemsToMap(data.getWorkingStorage(), map);
        addItemsToMap(data.getLinkageSection(), map);
        addItemsToMap(data.getLocalStorage(), map);
        for (DataDivision.FileSection fs : data.getFileSection()) {
            addItemsToMap(fs.getRecords(), map);
        }
        return Collections.unmodifiableMap(map);
    }

    private void addItemsToMap(List<DataItem> items, Map<String, DataItem> map) {
        if (items == null) return;
        for (DataItem item : items) {
            if (!item.isFiller() && item.getName() != null) {
                map.put(item.getName(), item);
            }
            if (item.getChildren() != null && !item.getChildren().isEmpty()) {
                addItemsToMap(item.getChildren(), map);
            }
        }
    }

    private Map<String, String> buildFileStatusMap(EnvironmentDivision environment) {
        if (environment == null || environment.getInputOutput() == null) {
            return Map.of();
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (FileControlEntry entry : environment.getInputOutput().getFileControl()) {
            entry.getFileStatus().ifPresent(status -> map.put(entry.getFileName(), status));
        }
        return Collections.unmodifiableMap(map);
    }
}
