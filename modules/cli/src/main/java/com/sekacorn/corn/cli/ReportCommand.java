/*
 * ReportCommand - Generate comprehensive reports
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.concurrent.Callable;

/**
 * Generate HTML/JSON reports on translation status and quality.
 */
@Command(
        name = "report",
        description = "Generate HTML/JSON reports",
        mixinStandardHelpOptions = true
)
public class ReportCommand implements Callable<Integer> {

    @Option(
            names = {"--workspace"},
            description = "Workspace directory",
            defaultValue = "."
    )
    private File workspaceDir;

    @Option(
            names = {"-o", "--output"},
            description = "Output file for report",
            required = true
    )
    private File outputFile;

    @Option(
            names = {"--format"},
            description = "Report format: ${COMPLETION-CANDIDATES}",
            defaultValue = "HTML"
    )
    private ReportFormat format;

    @Option(
            names = {"--include-metrics"},
            description = "Include detailed metrics",
            defaultValue = "true"
    )
    private boolean includeMetrics;

    @Override
    public Integer call() throws Exception {
        System.out.println("=== Generating Corn Report ===");
        System.out.println("Workspace: " + workspaceDir.getAbsolutePath());
        System.out.println("Format: " + format);
        System.out.println("Output: " + outputFile.getAbsolutePath());

        Path workspace = workspaceDir.toPath();
        if (!Files.exists(workspace)) {
            System.err.println("Error: Workspace does not exist: " + workspace);
            return 1;
        }
        Path outputParent = outputFile.toPath().getParent();
        if (outputParent != null) {
            Files.createDirectories(outputParent);
        }

        long cobolFiles;
        long javaFiles;
        long totalFiles;
        try (Stream<Path> stream = Files.walk(workspace)) {
            var files = stream.filter(Files::isRegularFile).toList();
            totalFiles = files.size();
            cobolFiles = files.stream().filter(p -> {
                String n = p.getFileName().toString().toUpperCase();
                return n.endsWith(".CBL") || n.endsWith(".COB") || n.endsWith(".COBOL");
            }).count();
            javaFiles = files.stream().filter(p -> p.getFileName().toString().endsWith(".java")).count();
        }

        String baseJson = """
                {
                  "workspace": "%s",
                  "metrics": {
                    "totalFiles": %d,
                    "cobolFiles": %d,
                    "javaFiles": %d,
                    "includeMetrics": %s
                  }
                }
                """.formatted(
                escapeJson(workspace.toAbsolutePath().toString()),
                totalFiles,
                cobolFiles,
                javaFiles,
                includeMetrics
        );

        String rendered = switch (format) {
            case JSON -> baseJson;
            case MARKDOWN -> """
                    # Corn Report

                    - Workspace: `%s`
                    - Total files: %d
                    - COBOL files: %d
                    - Java files: %d
                    - Include metrics: %s
                    """.formatted(workspace.toAbsolutePath(), totalFiles, cobolFiles, javaFiles, includeMetrics);
            case HTML -> """
                    <!doctype html>
                    <html lang="en">
                    <head><meta charset="utf-8"><title>Corn Report</title></head>
                    <body>
                      <h1>Corn Report</h1>
                      <ul>
                        <li><strong>Workspace:</strong> %s</li>
                        <li><strong>Total files:</strong> %d</li>
                        <li><strong>COBOL files:</strong> %d</li>
                        <li><strong>Java files:</strong> %d</li>
                        <li><strong>Include metrics:</strong> %s</li>
                      </ul>
                    </body>
                    </html>
                    """.formatted(escapeHtml(workspace.toAbsolutePath().toString()), totalFiles, cobolFiles, javaFiles, includeMetrics);
            case PDF -> """
                    Corn Report (PDF placeholder)
                    Workspace: %s
                    Total files: %d
                    COBOL files: %d
                    Java files: %d
                    Include metrics: %s
                    """.formatted(workspace.toAbsolutePath(), totalFiles, cobolFiles, javaFiles, includeMetrics);
        };

        Files.writeString(outputFile.toPath(), rendered, StandardCharsets.UTF_8);

        System.out.println("\nReport generated successfully!");

        return 0;
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    enum ReportFormat {
        HTML,
        JSON,
        PDF,
        MARKDOWN
    }
}
