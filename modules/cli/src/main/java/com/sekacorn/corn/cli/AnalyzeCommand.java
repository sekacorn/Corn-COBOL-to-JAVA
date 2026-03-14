/*
 * AnalyzeCommand - Parse and analyze COBOL programs
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.cli;

import com.sekacorn.corn.ir.Program;
import com.sekacorn.corn.ir.SourceMetadata;
import com.sekacorn.corn.parser.CobolSourceParser;
import com.sekacorn.corn.parser.ParseResult;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
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

/**
 * Analyze COBOL programs without generating Java code.
 * Produces detailed reports on structure, complexity, and dependencies.
 */
@Command(
        name = "analyze",
        description = "Parse and analyze COBOL programs",
        mixinStandardHelpOptions = true
)
public class AnalyzeCommand implements Callable<Integer> {

    @Parameters(
            index = "0..*",
            description = "COBOL source files or directories to analyze",
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
    private Dialect dialect;

    @Option(
            names = {"-o", "--output"},
            description = "Output directory for reports",
            defaultValue = "./corn-output"
    )
    private File outputDir;

    @Option(
            names = {"--report"},
            description = "Report file (JSON)",
            paramLabel = "<file>"
    )
    private File reportFile;

    @Option(
            names = {"--threads"},
            description = "Number of parallel processing threads",
            defaultValue = "4"
    )
    private int threads;

    @Option(
            names = {"--fail-on-warnings"},
            description = "Exit with error code if warnings are found"
    )
    private boolean failOnWarnings;

    @Override
    public Integer call() throws Exception {
        System.out.println("=== Corn COBOL Analyzer ===");
        System.out.println("Analyzing " + (sources != null ? sources.size() : 0) + " source(s)...");
        System.out.println("Dialect: " + dialect);
        System.out.println("Threads: " + threads);

        if (copybookPaths != null && !copybookPaths.isEmpty()) {
            System.err.println("Error: --copybook-path is not implemented yet in this MVP.");
            return 1;
        }

        if (sources == null || sources.isEmpty()) {
            System.err.println("Error: No source files or directories supplied.");
            return 1;
        }

        List<Path> cobolFiles = collectCobolFiles();
        if (cobolFiles.isEmpty()) {
            System.err.println("Error: No COBOL files found.");
            return 1;
        }

        SourceMetadata.CobolDialect irDialect = switch (dialect) {
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

        int parsed = 0;
        int failed = 0;
        int totalParagraphs = 0;
        int totalStatements = 0;
        int warnings = 0;
        List<String> fileReports = new ArrayList<>();

        int workerCount = Math.max(1, threads);
        ExecutorService pool = Executors.newFixedThreadPool(workerCount);
        List<Future<AnalyzeFileResult>> futures = new ArrayList<>();
        for (Path cobolFile : cobolFiles) {
            futures.add(pool.submit(() -> analyzeOneFile(cobolFile, irDialect)));
        }
        pool.shutdown();

        for (Future<AnalyzeFileResult> future : futures) {
            AnalyzeFileResult result;
            try {
                result = future.get();
            } catch (ExecutionException e) {
                failed++;
                fileReports.add("""
                        {"file":"<internal>","status":"error","errors":1,"warnings":0}
                        """.trim());
                continue;
            }
            warnings += result.warnings();
            if (result.success()) {
                parsed++;
                totalParagraphs += result.paragraphs();
                totalStatements += result.statements();
            } else {
                failed++;
            }
            fileReports.add(result.reportJson());
        }

        Files.createDirectories(outputDir.toPath());
        File reportTarget = reportFile != null ? reportFile : outputDir.toPath().resolve("analysis-report.json").toFile();
        Path reportParent = reportTarget.toPath().getParent();
        if (reportParent != null) {
            Files.createDirectories(reportParent);
        }
        String json = """
                {
                  "summary": {
                    "totalFiles": %d,
                    "parsedFiles": %d,
                    "failedFiles": %d,
                    "warnings": %d,
                    "paragraphs": %d,
                    "statements": %d
                  },
                  "files": [
                    %s
                  ]
                }
                """.formatted(
                cobolFiles.size(), parsed, failed, warnings, totalParagraphs, totalStatements,
                String.join(",\n    ", fileReports)
        );
        Files.writeString(reportTarget.toPath(), json, StandardCharsets.UTF_8);

        System.out.println("\nAnalysis complete!");
        System.out.println("Output: " + outputDir.getAbsolutePath());
        System.out.println("Report: " + reportTarget.getAbsolutePath());

        if (failOnWarnings && warnings > 0) {
            return 1;
        }
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

    private AnalyzeFileResult analyzeOneFile(Path cobolFile, SourceMetadata.CobolDialect irDialect) throws Exception {
        ParseResult result = CobolSourceParser.parse(cobolFile, irDialect);
        long fileWarnings = result.errors().stream()
                .filter(e -> e.severity() == com.sekacorn.corn.parser.ParseError.Severity.WARNING)
                .count();
        if (result.program() == null || result.hasErrors()) {
            return new AnalyzeFileResult(
                    false,
                    0,
                    0,
                    (int) fileWarnings,
                    """
                    {"file":"%s","status":"error","errors":%d,"warnings":%d}
                    """.formatted(escapeJson(cobolFile.toString()), result.errors().size(), fileWarnings).trim()
            );
        }
        Program p = result.program();
        int paragraphCount = p.getProcedure() != null
                ? p.getProcedure().getParagraphs().size()
                + p.getProcedure().getSections().stream().mapToInt(s -> s.getParagraphs().size()).sum()
                : 0;
        int statementCount = p.getProcedure() != null
                ? p.getProcedure().getParagraphs().stream().mapToInt(pa -> pa.getStatements().size()).sum()
                + p.getProcedure().getSections().stream()
                .flatMap(s -> s.getParagraphs().stream())
                .mapToInt(pa -> pa.getStatements().size()).sum()
                : 0;
        return new AnalyzeFileResult(
                true,
                paragraphCount,
                statementCount,
                (int) fileWarnings,
                """
                {"file":"%s","status":"ok","programId":"%s","paragraphs":%d,"statements":%d,"warnings":%d}
                """.formatted(
                        escapeJson(cobolFile.toString()),
                        escapeJson(p.getProgramId()),
                        paragraphCount,
                        statementCount,
                        fileWarnings
                ).trim()
        );
    }

    private record AnalyzeFileResult(boolean success, int paragraphs, int statements, int warnings, String reportJson) {
    }

    enum Dialect {
        ANSI_68, ANSI_74, ANSI_85, ANSI_2002, ANSI_2014,
        IBM_ENTERPRISE, MICRO_FOCUS, GNU_COBOL, UNISYS, FUJITSU
    }
}
