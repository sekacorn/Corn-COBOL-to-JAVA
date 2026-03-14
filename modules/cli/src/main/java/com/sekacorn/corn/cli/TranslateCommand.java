/*
 * TranslateCommand - Translate COBOL to Java
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Callable;

/**
 * Translate COBOL programs to Java code.
 * The current implementation supports code generation level 2.
 */
@Command(
        name = "translate",
        description = "Translate COBOL programs to Java",
        mixinStandardHelpOptions = true
)
public class TranslateCommand implements Callable<Integer> {

    @Parameters(
            index = "0..*",
            description = "COBOL source files or directories to translate",
            paramLabel = "<files>"
    )
    private List<File> sources;

    @Option(
            names = {"-I", "--copybook-path"},
            description = "Reserved for future copybook resolution support",
            paramLabel = "<path>"
    )
    private List<Path> copybookPaths;

    @Option(
            names = {"--dialect"},
            description = "COBOL dialect: ${COMPLETION-CANDIDATES}",
            defaultValue = "ANSI_85"
    )
    private AnalyzeCommand.Dialect dialect;

    @Option(
            names = {"-o", "--output"},
            description = "Output directory for generated Java code",
            required = true
    )
    private File outputDir;

    @Option(
            names = {"--codegen-level"},
            description = "Code generation quality level (0-3). Only level 2 is currently implemented:\n" +
                         "  0 = Conservative 1:1 (highest fidelity)\n" +
                         "  1 = Structured + readable\n" +
                         "  2 = Idiomatic Java (recommended)\n" +
                         "  3 = Service-friendly (modern architecture)",
            defaultValue = "2"
    )
    private int codegenLevel;

    @Option(
            names = {"--threads"},
            description = "Number of parallel processing threads",
            defaultValue = "8"
    )
    private int threads;

    @Option(
            names = {"--strict-numerics"},
            description = "Enable strict numeric checking",
            defaultValue = "true"
    )
    private boolean strictNumerics;

    @Option(
            names = {"--file-backend"},
            description = "File backend: ${COMPLETION-CANDIDATES}",
            defaultValue = "LOCAL"
    )
    private FileBackend fileBackend;

    @Option(
            names = {"--package"},
            description = "Base Java package for generated code",
            defaultValue = "com.generated.cobol"
    )
    private String basePackage;

    @Option(
            names = {"--dry-run"},
            description = "Perform translation but don't write files"
    )
    private boolean dryRun;

    @Override
    public Integer call() {
        try {
            System.out.println("=== Corn COBOL to Java Translator ===");
            System.out.println("Sources: " + (sources != null ? sources.size() : 0));
            System.out.println("Output: " + outputDir.getAbsolutePath());
            System.out.println("Code Generation Level: " + codegenLevel + " - " + getLevelDescription());
            System.out.println("Base Package: " + basePackage);
            System.out.println("File Backend: " + fileBackend);
            System.out.println("Parallel Threads: " + threads);

            if (copybookPaths != null && !copybookPaths.isEmpty()) {
                System.err.println("Error: --copybook-path is not implemented yet in this MVP.");
                return 1;
            }

            if (codegenLevel < 0 || codegenLevel > 3) {
                System.err.println("Error: --codegen-level must be in range 0..3");
                return 1;
            }
            if (codegenLevel != 2) {
                System.err.println("Error: --codegen-level " + codegenLevel
                        + " is not implemented yet. Only level 2 is currently supported.");
                return 1;
            }

            if (dryRun) {
                System.out.println("\n[DRY RUN MODE - No files will be written]");
            }

            if (sources == null || sources.isEmpty()) {
                System.err.println("Error: No source files specified.");
                return 1;
            }

            List<Path> cobolFiles = collectCobolFiles();
            if (cobolFiles.isEmpty()) {
                System.err.println("Error: No COBOL files found.");
                return 1;
            }
            System.out.println("Found " + cobolFiles.size() + " COBOL file(s) to translate.");

            SourceMetadata.CobolDialect irDialect = mapDialect(dialect);
            int workerCount = Math.max(1, threads);
            ExecutorService pool = Executors.newFixedThreadPool(workerCount);
            AtomicInteger successCount = new AtomicInteger();
            AtomicInteger errorCount = new AtomicInteger();
            List<Future<Void>> futures = new ArrayList<>();

            for (Path cobolFile : cobolFiles) {
                futures.add(pool.submit(() -> {
                    processOneFile(cobolFile, irDialect, successCount, errorCount);
                    return null;
                }));
            }

            pool.shutdown();
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    System.err.println("Unexpected translation failure: " + e.getCause().getMessage());
                    errorCount.incrementAndGet();
                }
            }

            System.out.println("\nTranslation complete!");
            System.out.printf("  Successful: %d, Errors: %d%n", successCount.get(), errorCount.get());

            if (dryRun) {
                System.out.println("(Dry run - no files written)");
            } else if (successCount.get() > 0) {
                System.out.println("Generated Java code: " + outputDir.getAbsolutePath());
            }

            return errorCount.get() > 0 ? 1 : 0;
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace(System.err);
            return 1;
        }
    }

    private void processOneFile(Path cobolFile,
                                SourceMetadata.CobolDialect irDialect,
                                AtomicInteger successCount,
                                AtomicInteger errorCount) {
        System.out.println("\nProcessing: " + cobolFile.getFileName());
        try {
            ParseResult parseResult = CobolSourceParser.parse(cobolFile, irDialect);
            if (parseResult.hasErrors()) {
                System.err.println("  Parse errors in " + cobolFile.getFileName() + ":");
                parseResult.errors().forEach(e -> System.err.println("    " + e));
                errorCount.incrementAndGet();
                return;
            }
            if (parseResult.program() == null) {
                System.err.println("  Failed to parse: " + cobolFile.getFileName());
                errorCount.incrementAndGet();
                return;
            }

            JavaCodeGenerator codeGen = new JavaCodeGenerator().withPackage(basePackage);
            GeneratedClass generated = codeGen.generate(parseResult.program());
            String javaSource = generated.render();

            System.out.println("  Generated: " + generated.getClassName() + ".java");
            if (!dryRun) {
                Path outFile = outputDir.toPath().resolve(generated.getRelativePath());
                Files.createDirectories(outFile.getParent());
                Files.writeString(outFile, javaSource);
                System.out.println("  Written to: " + outFile);
            }
            successCount.incrementAndGet();
        } catch (Exception e) {
            System.err.println("  Error processing " + cobolFile.getFileName() + ": " + e.getMessage());
            errorCount.incrementAndGet();
        }
    }

    private String getLevelDescription() {
        return switch (codegenLevel) {
            case 0 -> "Conservative 1:1";
            case 1 -> "Structured + Readable";
            case 2 -> "Idiomatic Java";
            case 3 -> "Service-Friendly";
            default -> "Unknown";
        };
    }

    private List<Path> collectCobolFiles() throws IOException {
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
                              return name.endsWith(".CBL") || name.endsWith(".COB")
                                     || name.endsWith(".COBOL");
                          })
                          .forEach(files::add);
                }
            }
        }
        return files;
    }

    private static SourceMetadata.CobolDialect mapDialect(AnalyzeCommand.Dialect dialect) {
        return switch (dialect) {
            case ANSI_68 -> SourceMetadata.CobolDialect.ANSI_68;
            case ANSI_74 -> SourceMetadata.CobolDialect.ANSI_74;
            case ANSI_85 -> SourceMetadata.CobolDialect.ANSI_85;
            case ANSI_2002 -> SourceMetadata.CobolDialect.ANSI_2002;
            case ANSI_2014 -> SourceMetadata.CobolDialect.ANSI_2014;
            case IBM_ENTERPRISE -> SourceMetadata.CobolDialect.IBM_ENTERPRISE;
            case MICRO_FOCUS -> SourceMetadata.CobolDialect.MICRO_FOCUS;
            case GNU_COBOL -> SourceMetadata.CobolDialect.GNU_COBOL;
            case UNISYS -> SourceMetadata.CobolDialect.UNISYS;
            case FUJITSU -> SourceMetadata.CobolDialect.FUJITSU;
        };
    }

    enum FileBackend {
        LOCAL,      // Local file system
        JDBC,       // Database tables
        KV          // Key-value store
    }
}
