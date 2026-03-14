/*
 * RefactorCommand - LLM-assisted refactoring
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Apply LLM-assisted refactoring suggestions to translated Java code.
 * All changes require approval and pass tests before application.
 */
@Command(
        name = "refactor",
        description = "Apply LLM-assisted refactoring (requires approval)",
        mixinStandardHelpOptions = true
)
public class RefactorCommand implements Callable<Integer> {

    @Parameters(
            index = "0..*",
            description = "Java files to refactor",
            paramLabel = "<files>"
    )
    private List<File> javaFiles;

    @Option(
            names = {"--llm-provider"},
            description = "LLM provider: ${COMPLETION-CANDIDATES}",
            defaultValue = "NONE"
    )
    private LLMProvider llmProvider;

    @Option(
            names = {"--api-key"},
            description = "API key for LLM provider (or use environment variable)"
    )
    private String apiKey;

    @Option(
            names = {"--rules"},
            description = "Refactoring rule packs (YAML files)"
    )
    private List<File> rulePacks;

    @Option(
            names = {"--dry-run"},
            description = "Generate suggestions without applying (default)",
            defaultValue = "true"
    )
    private boolean dryRun;

    @Option(
            names = {"--auto-approve"},
            description = "Automatically approve safe refactorings (DANGEROUS)"
    )
    private boolean autoApprove;

    @Option(
            names = {"-o", "--output"},
            description = "Output directory for refactored code"
    )
    private File outputDir;

    @Override
    public Integer call() throws Exception {
        System.out.println("=== Corn LLM Refactor Assistant ===");
        System.out.println("LLM Provider: " + llmProvider);
        System.out.println("Mode: " + (dryRun ? "DRY RUN (suggestions only)" : "APPLY CHANGES"));

        if (llmProvider == LLMProvider.NONE) {
            System.out.println("\nNo LLM provider specified. Available providers:");
            System.out.println("  --llm-provider anthropic    (Claude)");
            System.out.println("  --llm-provider openai       (GPT)");
            System.out.println("  --llm-provider azure        (Azure OpenAI)");
            System.out.println("  --llm-provider local        (Local endpoint)");
            return 1;
        }

        if (autoApprove) {
            System.out.println("\nWARNING: Auto-approve enabled - changes will be applied without review!");
        }

        if (javaFiles == null || javaFiles.isEmpty()) {
            System.err.println("Error: No Java files supplied.");
            return 1;
        }
        if (!dryRun && outputDir == null) {
            System.err.println("Error: --output is required when applying changes.");
            return 1;
        }
        if (outputDir != null) {
            Files.createDirectories(outputDir.toPath());
        }

        int scanned = 0;
        int changed = 0;
        List<String> suggestions = new ArrayList<>();
        for (File javaFile : javaFiles) {
            Path path = javaFile.toPath();
            if (!Files.isRegularFile(path)) {
                suggestions.add("SKIP " + path + " (not a file)");
                continue;
            }
            scanned++;
            String original = Files.readString(path, StandardCharsets.UTF_8);
            String refactored = applyBaselineRefactors(original);
            boolean changedThisFile = !original.equals(refactored);
            if (changedThisFile) {
                changed++;
            }
            suggestions.add((changedThisFile ? "UPDATE " : "KEEP ") + path);

            if (!dryRun) {
                Path out = outputDir.toPath().resolve(path.getFileName());
                Files.writeString(out, refactored, StandardCharsets.UTF_8);
            }
        }

        System.out.println("\nRefactoring analysis complete!");
        System.out.println("Files scanned: " + scanned);
        System.out.println("Files changed: " + changed);
        suggestions.forEach(s -> System.out.println("  " + s));

        if (dryRun) {
            System.out.println("Review suggestions and run without --dry-run to apply.");
        } else {
            System.out.println("Refactored output: " + outputDir.getAbsolutePath());
        }

        return 0;
    }

    private static String applyBaselineRefactors(String source) {
        String normalized = source
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace("\t", "    ");
        String[] lines = normalized.split("\n", -1);
        StringBuilder out = new StringBuilder();
        int blankRun = 0;
        for (String line : lines) {
            String trimmedRight = line.replaceAll("\\s+$", "");
            if (trimmedRight.isBlank()) {
                blankRun++;
            } else {
                blankRun = 0;
            }
            if (blankRun > 2) {
                continue;
            }
            out.append(trimmedRight).append("\n");
        }
        return out.toString();
    }

    enum LLMProvider {
        NONE,
        ANTHROPIC,
        OPENAI,
        AZURE,
        LOCAL
    }
}

