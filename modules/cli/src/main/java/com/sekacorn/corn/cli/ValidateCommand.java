/*
 * ValidateCommand - Validate translation correctness
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.cli;

import com.sekacorn.corn.codegen.GeneratedClass;
import com.sekacorn.corn.codegen.JavaCodeGenerator;
import com.sekacorn.corn.ir.SourceMetadata;
import com.sekacorn.corn.parser.CobolSourceParser;
import com.sekacorn.corn.parser.ParseResult;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 * Validate translated Java code against original COBOL.
 * Current implementation verifies parse, generate, compile, and execute flow.
 */
@Command(
        name = "validate",
        description = "Validate parse/generate/compile/execute flow for translated Java",
        mixinStandardHelpOptions = true
)
public class ValidateCommand implements Callable<Integer> {
    private static final Object STDOUT_CAPTURE_LOCK = new Object();

    @Parameters(
            index = "0..*",
            description = "COBOL source files to validate",
            paramLabel = "<files>"
    )
    private List<File> sources;

    @Option(
            names = {"-I", "--copybook-path"},
            description = "Reserved for future copybook resolution support"
    )
    private List<Path> copybookPaths;

    @Option(
            names = {"-o", "--output"},
            description = "Output directory for validation results",
            defaultValue = "./corn-validation"
    )
    private File outputDir;

    @Option(
            names = {"--docker"},
            description = "Reserved for future reference-compiler validation modes",
            defaultValue = "true"
    )
    private boolean useDocker;

    @Option(
            names = {"--gnucobol-path"},
            description = "Reserved for future reference-compiler validation modes"
    )
    private File gnucobolPath;

    @Option(
            names = {"--test-data"},
            description = "Directory containing expected stdout fixtures named <program>.out or <source-file>.out"
    )
    private File testDataDir;

    @Option(
            names = {"--threads"},
            description = "Number of parallel validation threads",
            defaultValue = "4"
    )
    private int threads;

    @Override
    public Integer call() throws Exception {
        System.out.println("=== Corn Validation Harness ===");
        System.out.println("Validating " + (sources != null ? sources.size() : 0) + " program(s)...");
        System.out.println("Validation mode: generated Java compile and execute");
        if (testDataDir != null) {
            System.out.println("Expected output fixtures: " + testDataDir.getAbsolutePath());
        }

        if (sources == null || sources.isEmpty()) {
            System.err.println("Error: No source files supplied.");
            return 1;
        }
        if (copybookPaths != null && !copybookPaths.isEmpty()) {
            System.err.println("Error: --copybook-path is not implemented yet in this MVP.");
            return 1;
        }
        Files.createDirectories(outputDir.toPath());

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        boolean javaCompilerAvailable = compiler != null;
        if (!javaCompilerAvailable) {
            System.err.println("Error: Java compiler is not available in this runtime.");
            return 1;
        }
        List<Path> cobolFiles = collectCobolFiles();
        int passed = 0;
        int failed = 0;
        List<String> fileResults = new ArrayList<>();
        Path classesDir = outputDir.toPath().resolve("classes");
        Files.createDirectories(classesDir);

        int workerCount = Math.max(1, threads);
        ExecutorService pool = Executors.newFixedThreadPool(workerCount);
        List<Future<ValidationFileResult>> futures = new ArrayList<>();
        for (Path cobolFile : cobolFiles) {
            futures.add(pool.submit(() -> validateOneFile(cobolFile, classesDir)));
        }
        pool.shutdown();

        for (Future<ValidationFileResult> future : futures) {
            ValidationFileResult result;
            try {
                result = future.get();
            } catch (ExecutionException e) {
                failed++;
                fileResults.add("""
                        {"file":"<internal>","status":"failed","javaCompile":"failed","executed":false,"outputMatched":false}
                        """.trim());
                continue;
            }
            if (result.success()) {
                passed++;
            } else {
                failed++;
            }
            fileReportsAdd(fileResults, result);
        }

        Path report = outputDir.toPath().resolve("validation-report.json");
        String json = """
                {
                  "summary": {
                    "total": %d,
                    "passed": %d,
                    "failed": %d,
                    "javaCompilerAvailable": %s,
                    "executionValidation": true,
                    "expectedOutputFixtures": %s
                  },
                  "results": [
                    %s
                  ]
                }
                """.formatted(
                cobolFiles.size(),
                passed,
                failed,
                javaCompilerAvailable,
                testDataDir != null,
                String.join(",\n    ", fileResults)
        );
        Files.writeString(report, json, StandardCharsets.UTF_8);

        System.out.println("\nValidation complete!");
        System.out.println("Results: " + outputDir.getAbsolutePath());
        System.out.println("Report: " + report.toAbsolutePath());

        return failed > 0 ? 1 : 0;
    }

    private List<Path> collectCobolFiles() throws Exception {
        List<Path> files = new ArrayList<>();
        for (File source : sources) {
            Path path = source.toPath();
            if (Files.isRegularFile(path)) {
                files.add(path);
            } else if (Files.isDirectory(path)) {
                try (var stream = Files.walk(path)) {
                    stream.filter(Files::isRegularFile)
                            .filter(p -> {
                                String name = p.getFileName().toString().toUpperCase();
                                return name.endsWith(".CBL") || name.endsWith(".COB") || name.endsWith(".COBOL");
                            })
                            .forEach(files::add);
                }
            }
        }
        return files;
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private ValidationFileResult validateOneFile(Path cobolFile, Path classesRoot) throws Exception {
        ParseResult parse = CobolSourceParser.parse(cobolFile, SourceMetadata.CobolDialect.ANSI_85);
        if (parse.program() == null || parse.hasErrors()) {
            return new ValidationFileResult(false, """
                    {"file":"%s","status":"parse_error","errorCount":%d}
                    """.formatted(escapeJson(cobolFile.toString()), parse.errors().size()).trim());
        }

        GeneratedClass generated = new JavaCodeGenerator().withPackage("com.generated.validation").generate(parse.program());
        Path javaFile = outputDir.toPath().resolve(generated.getRelativePath());
        Files.createDirectories(javaFile.getParent());
        Files.writeString(javaFile, generated.render(), StandardCharsets.UTF_8);

        String safeName = stripExtension(cobolFile.getFileName().toString()).replaceAll("[^A-Za-z0-9._-]", "_");
        Path classDir = classesRoot.resolve(safeName);
        Files.createDirectories(classDir);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int compileExit = compiler.run(null, null, null, "-d", classDir.toString(), javaFile.toString());
        if (compileExit != 0) {
            return new ValidationFileResult(false, """
                    {"file":"%s","status":"failed","javaCompile":"failed","executed":false,"outputMatched":false}
                    """.formatted(escapeJson(cobolFile.toString())).trim());
        }

        ExecutionResult execution = executeGeneratedClass(classDir, generated);
        String expectedOutput = loadExpectedOutput(cobolFile, parse.program().getProgramId());
        boolean expectedPresent = expectedOutput != null;
        boolean outputMatched = !expectedPresent || normalizeOutput(expectedOutput).equals(normalizeOutput(execution.output()));
        boolean success = execution.success() && outputMatched;
        return new ValidationFileResult(success, """
                {"file":"%s","status":"%s","javaCompile":"ok","executed":%s,"outputMatched":%s,"expectedOutput":%s}
                """.formatted(
                escapeJson(cobolFile.toString()),
                success ? "ok" : "failed",
                execution.success(),
                outputMatched,
                expectedPresent
        ).trim());
    }

    private static void fileReportsAdd(List<String> fileReports, ValidationFileResult result) {
        fileReports.add(result.reportJson());
    }

    private ExecutionResult executeGeneratedClass(Path classesDir, GeneratedClass generated) {
        String fqcn = generated.getPackageName() + "." + generated.getClassName();
        try (URLClassLoader loader = new URLClassLoader(new URL[]{classesDir.toUri().toURL()})) {
            Class<?> clazz = Class.forName(fqcn, true, loader);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            Method run = clazz.getMethod("run");

            java.io.ByteArrayOutputStream capture = new java.io.ByteArrayOutputStream();
            synchronized (STDOUT_CAPTURE_LOCK) {
                PrintStream originalOut = System.out;
                try (PrintStream tempOut = new PrintStream(capture, true, StandardCharsets.UTF_8)) {
                    System.setOut(tempOut);
                    run.invoke(instance);
                } finally {
                    System.setOut(originalOut);
                }
            }
            return new ExecutionResult(true, capture.toString(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return new ExecutionResult(false, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private String loadExpectedOutput(Path cobolFile, String programId) throws Exception {
        if (testDataDir == null) {
            return null;
        }
        Path base = testDataDir.toPath();
        List<Path> candidates = List.of(
                base.resolve(programId.toUpperCase() + ".out"),
                base.resolve(programId + ".out"),
                base.resolve(stripExtension(cobolFile.getFileName().toString()) + ".out")
        );
        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return Files.readString(candidate, detectCharset(candidate));
            }
        }
        return null;
    }

    private static Charset detectCharset(Path path) {
        return StandardCharsets.UTF_8;
    }

    private static String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(0, dot) : fileName;
    }

    private static String normalizeOutput(String text) {
        return text.replace("\r\n", "\n").replace('\r', '\n').trim();
    }

    private record ExecutionResult(boolean success, String output) {
    }

    private record ValidationFileResult(boolean success, String reportJson) {
    }
}
