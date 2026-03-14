/*
 * CodegenIntegrationIT - End-to-end integration tests: COBOL → parse → codegen → compile → run
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.codegen;

import com.sekacorn.corn.ir.SourceMetadata;
import com.sekacorn.corn.parser.CobolSourceParser;
import com.sekacorn.corn.parser.ParseResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests that exercise the full pipeline:
 * parse COBOL source → generate Java → compile in memory → execute.
 * <p>
 * Named *IT so maven-failsafe-plugin picks them up during the {@code verify} phase.
 */
@DisplayName("Codegen end-to-end integration tests")
class CodegenIntegrationIT {

    private static final SourceMetadata.CobolDialect DIALECT = SourceMetadata.CobolDialect.ANSI_85;

    // ── Helpers ────────────────────────────────────────────────────

    private Path cobolResource(String name) {
        try {
            return Path.of(getClass().getResource("/cobol/" + name).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /** Parse → generate → return GeneratedClass. */
    private GeneratedClass generateFromCobol(String fileName) throws Exception {
        ParseResult result = CobolSourceParser.parse(cobolResource(fileName), DIALECT);
        assertFalse(result.hasErrors(),
                () -> "Parse errors in " + fileName + ": " + result.errors());

        JavaCodeGenerator gen = new JavaCodeGenerator();
        GeneratedClass generated = gen.generate(result.program());
        assertNotNull(generated, "Generated class must not be null for " + fileName);
        return generated;
    }

    /** Parse inline COBOL string → generate → return GeneratedClass. */
    private GeneratedClass generateFromString(String cobolSource) {
        ParseResult result = CobolSourceParser.parseString(cobolSource, "TEST.cbl", DIALECT);
        assertFalse(result.hasErrors(),
                () -> "Parse errors: " + result.errors());

        JavaCodeGenerator gen = new JavaCodeGenerator();
        GeneratedClass generated = gen.generate(result.program());
        assertNotNull(generated);
        return generated;
    }

    /** Parse → generate → compile in memory → return loaded Class. */
    private Class<?> compileFromCobol(String fileName) throws Exception {
        GeneratedClass generated = generateFromCobol(fileName);
        return compileGenerated(generated, fileName);
    }

    /** Compile a GeneratedClass in memory. */
    private Class<?> compileGenerated(GeneratedClass generated, String label) {
        String fqcn = generated.getPackageName() + "." + generated.getClassName();
        String source = generated.render();
        try {
            return InMemoryCompiler.compile(fqcn, source);
        } catch (InMemoryCompiler.CompilationException e) {
            fail("Compilation failed for " + label + ":\n" + source + "\n\n" + e.getMessage());
            return null; // unreachable
        }
    }

    /** Invoke the no-arg run() method on a new instance, capturing stdout. */
    private String invokeRun(Class<?> clazz) throws Exception {
        Object instance = clazz.getDeclaredConstructor().newInstance();
        Method run = clazz.getMethod("run");

        PrintStream originalOut = System.out;
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capture));
        try {
            run.invoke(instance);
        } finally {
            System.setOut(originalOut);
        }
        return capture.toString();
    }

    // ── Resource file tests ────────────────────────────────────────

    @Test
    @DisplayName("HELLO.cbl compiles and prints HELLO")
    void helloWorldCompilesAndRuns() throws Exception {
        Class<?> clazz = compileFromCobol("HELLO.cbl");
        String output = invokeRun(clazz);
        assertTrue(output.contains("HELLO"),
                "Expected output to contain HELLO, got: " + output);
    }

    @Test
    @DisplayName("Generated HELLO class has correct structure")
    void generatedCodeHasCorrectStructure() throws Exception {
        Class<?> clazz = compileFromCobol("HELLO.cbl");

        // Has public run() method
        Method run = assertDoesNotThrow(() -> clazz.getMethod("run"));
        assertTrue(Modifier.isPublic(run.getModifiers()), "run() should be public");

        // Has public static main(String[])
        Method main = assertDoesNotThrow(() -> clazz.getMethod("main", String[].class));
        assertTrue(Modifier.isPublic(main.getModifiers()), "main() should be public");
        assertTrue(Modifier.isStatic(main.getModifiers()), "main() should be static");

        // Class is public
        assertTrue(Modifier.isPublic(clazz.getModifiers()), "Generated class should be public");
    }

    @Test
    @DisplayName("ARITHMETIC.cbl compiles and runs without error")
    void arithmeticCompilesAndRuns() throws Exception {
        Class<?> clazz = compileFromCobol("ARITHMETIC.cbl");
        String output = invokeRun(clazz);
        assertTrue(output.contains("RESULT"),
                "Expected arithmetic output to contain RESULT, got: " + output);
    }

    @Test
    @DisplayName("CONTROL.cbl compiles and runs without error")
    void controlFlowCompilesAndRuns() throws Exception {
        Class<?> clazz = compileFromCobol("CONTROL.cbl");
        String output = invokeRun(clazz);
        assertFalse(output.isEmpty(), "Expected some output from CONTROL.cbl");
    }

    @Test
    @DisplayName("DATADEF.cbl generates compilable Java")
    void dataDefinitionsCompile() throws Exception {
        Class<?> clazz = compileFromCobol("DATADEF.cbl");
        assertNotNull(clazz, "DATADEF.cbl should compile successfully");
    }

    @Test
    @DisplayName("FILEIO.cbl generates compilable Java")
    void fileIoCompiles() throws Exception {
        Class<?> clazz = compileFromCobol("FILEIO.cbl");
        assertNotNull(clazz, "FILEIO.cbl should compile successfully");
    }

    @Test
    @DisplayName("FILEIO.cbl propagates file status into working-storage")
    void fileIoUpdatesStatusField() throws Exception {
        GeneratedClass generated = generateFromCobol("FILEIO.cbl");
        String source = generated.render();
        assertTrue(source.contains("wsFileStatus = studentFile.getStatus().getCode();"));
        assertTrue(source.contains("wsFileStatus = __readResult"));
        assertTrue(source.contains(".status().getCode();"));
    }

    // ── Inline COBOL tests (guaranteed correct parsing) ────────────

    @Test
    @DisplayName("Inline: simple DISPLAY compiles and runs")
    void inlineDisplayCompilesAndRuns() throws Exception {
        String cobol = String.join("\n",
                "000100 IDENTIFICATION DIVISION.",
                "000200 PROGRAM-ID. SIMPLEDISPLAY.",
                "000300 PROCEDURE DIVISION.",
                "000400     DISPLAY \"HELLO FROM COBOL\".",
                "000500     STOP RUN.", "");
        GeneratedClass gen = generateFromString(cobol);
        Class<?> clazz = compileGenerated(gen, "inline-display");
        String output = invokeRun(clazz);
        assertTrue(output.contains("HELLO FROM COBOL"),
                "Expected 'HELLO FROM COBOL', got: " + output);
    }

    @Test
    @DisplayName("Inline: numeric fields and arithmetic compile and run")
    void inlineArithmeticCompilesAndRuns() throws Exception {
        String cobol = String.join("\n",
                "000100 IDENTIFICATION DIVISION.",
                "000200 PROGRAM-ID. ARITH.",
                "000300 DATA DIVISION.",
                "000400 WORKING-STORAGE SECTION.",
                "000500 01  WS-A PIC 9(5).",
                "000600 01  WS-B PIC 9(5).",
                "000700 PROCEDURE DIVISION.",
                "000800     MOVE 10 TO WS-A.",
                "000900     MOVE 20 TO WS-B.",
                "001000     ADD WS-A TO WS-B.",
                "001100     DISPLAY WS-B.",
                "001200     STOP RUN.", "");
        GeneratedClass gen = generateFromString(cobol);
        Class<?> clazz = compileGenerated(gen, "inline-arithmetic");
        String output = invokeRun(clazz);
        assertFalse(output.isBlank(), "Expected numeric output");
    }

    @Test
    @DisplayName("Inline: IF-ELSE compiles and runs")
    void inlineIfElseCompilesAndRuns() throws Exception {
        String cobol = String.join("\n",
                "000100 IDENTIFICATION DIVISION.",
                "000200 PROGRAM-ID. IFTEST.",
                "000300 DATA DIVISION.",
                "000400 WORKING-STORAGE SECTION.",
                "000500 01  WS-NUM PIC 9(3).",
                "000600 PROCEDURE DIVISION.",
                "000700     MOVE 5 TO WS-NUM.",
                "000800     IF WS-NUM > 3",
                "000900         DISPLAY \"BIG\"",
                "001000     ELSE",
                "001100         DISPLAY \"SMALL\"",
                "001200     END-IF.",
                "001300     STOP RUN.", "");
        GeneratedClass gen = generateFromString(cobol);
        Class<?> clazz = compileGenerated(gen, "inline-if-else");
        String output = invokeRun(clazz);
        assertTrue(output.contains("BIG"), "Expected 'BIG', got: " + output);
    }

    @Test
    @DisplayName("Inline: PERFORM paragraph call compiles and runs")
    void inlinePerformCompilesAndRuns() throws Exception {
        String cobol = String.join("\n",
                "000100 IDENTIFICATION DIVISION.",
                "000200 PROGRAM-ID. PERFTEST.",
                "000300 PROCEDURE DIVISION.",
                "000400 MAIN-PARA.",
                "000500     PERFORM SUB-PARA.",
                "000600     STOP RUN.",
                "000700 SUB-PARA.",
                "000800     DISPLAY \"PERFORMED\".", "");
        GeneratedClass gen = generateFromString(cobol);
        Class<?> clazz = compileGenerated(gen, "inline-perform");
        String output = invokeRun(clazz);
        assertTrue(output.contains("PERFORMED"),
                "Expected 'PERFORMED', got: " + output);
    }

    @Test
    @DisplayName("Inline: PERFORM UNTIL loop compiles and runs")
    void inlinePerformUntilCompilesAndRuns() throws Exception {
        String cobol = String.join("\n",
                "000100 IDENTIFICATION DIVISION.",
                "000200 PROGRAM-ID. LOOPTEST.",
                "000300 DATA DIVISION.",
                "000400 WORKING-STORAGE SECTION.",
                "000500 01  WS-COUNT PIC 9(3).",
                "000600 PROCEDURE DIVISION.",
                "000700 MAIN-PARA.",
                "000800     MOVE 1 TO WS-COUNT.",
                "000900     PERFORM COUNT-PARA UNTIL WS-COUNT > 3.",
                "001000     STOP RUN.",
                "001100 COUNT-PARA.",
                "001200     DISPLAY WS-COUNT.",
                "001300     ADD 1 TO WS-COUNT.", "");
        GeneratedClass gen = generateFromString(cobol);
        Class<?> clazz = compileGenerated(gen, "inline-loop");
        String output = invokeRun(clazz);
        assertFalse(output.isBlank(), "Expected loop output");
    }
}
