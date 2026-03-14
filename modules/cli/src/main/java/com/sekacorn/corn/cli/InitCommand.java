/*
 * InitCommand - Initialize project structure
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Initialize a new Corn project with configuration and directory structure.
 */
@Command(
        name = "init",
        description = "Initialize a new Corn project",
        mixinStandardHelpOptions = true
)
public class InitCommand implements Callable<Integer> {

    @Option(
            names = {"--source"},
            description = "COBOL source directory",
            required = true
    )
    private File sourceDir;

    @Option(
            names = {"--copybook-path"},
            description = "Copybook directory"
    )
    private File copybookDir;

    @Option(
            names = {"--output"},
            description = "Java output directory",
            defaultValue = "./output/java"
    )
    private File outputDir;

    @Option(
            names = {"--workspace"},
            description = "Workspace directory",
            defaultValue = "."
    )
    private File workspaceDir;

    @Override
    public Integer call() throws Exception {
        System.out.println("=== Initializing Corn Project ===");
        System.out.println("Workspace: " + workspaceDir.getAbsolutePath());

        // Create directory structure
        createDirectory(workspaceDir.toPath());
        createDirectory(outputDir.toPath());

        // Create corn.json configuration
        Path configFile = workspaceDir.toPath().resolve("corn.json");
        String config = generateConfig();

        try {
            Files.writeString(configFile, config);
            System.out.println("\nCreated configuration: " + configFile);
        } catch (IOException e) {
            System.err.println("Failed to create config: " + e.getMessage());
            return 1;
        }

        System.out.println("\nProject initialized successfully!");
        System.out.println("\nNext steps:");
        System.out.println("  1. Review corn.json configuration");
        System.out.println("  2. Run: corn-cobol-to-java analyze --source " + sourceDir);
        System.out.println("  3. Run: corn-cobol-to-java translate --source " + sourceDir);

        return 0;
    }

    private void createDirectory(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            System.out.println("Created directory: " + path);
        }
    }

    private String generateConfig() {
        return """
                {
                  "version": "1.0",
                  "project": {
                    "name": "Corn COBOL Project",
                    "source": "%s",
                    "copybookPath": %s,
                    "output": "%s"
                  },
                  "compiler": {
                    "dialect": "ANSI_85",
                    "codegenLevel": 2,
                    "strictNumerics": true,
                    "threads": 8
                  },
                  "runtime": {
                    "fileBackend": "local"
                  }
                }
                """.formatted(
                sourceDir.getAbsolutePath().replace("\\", "\\\\"),
                copybookDir != null ? "\"" + copybookDir.getAbsolutePath().replace("\\", "\\\\") + "\"" : "null",
                outputDir.getAbsolutePath().replace("\\", "\\\\")
        );
    }
}
