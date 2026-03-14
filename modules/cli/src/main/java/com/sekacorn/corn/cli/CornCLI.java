/*
 * CornCLI - Main CLI entry point
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

/**
 * Main CLI entry point for Corn COBOL-to-Java Compiler.
 * Evaluation-stage command-line interface for the current repository.
 */
@Command(
        name = "corn-cobol-to-java",
        description = "COBOL-to-Java translation toolchain (evaluation edition)",
        version = "Corn COBOL-to-Java Compiler 1.0.0-SNAPSHOT\n" +
                 "Copyright (c) 2025-2026 Cornmeister LLC\n" +
                 "License: Corn Evaluation License (non-production)",
        mixinStandardHelpOptions = true,
        subcommands = {
                HelpCommand.class,
                AnalyzeCommand.class,
                TranslateCommand.class,
                ValidateCommand.class,
                RefactorCommand.class,
                GuiCommand.class,
                InitCommand.class,
                ReportCommand.class
        }
)
public class CornCLI implements Runnable {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new CornCLI())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setExecutionExceptionHandler(new ExceptionHandler())
                .execute(args);

        System.exit(exitCode);
    }

    @Override
    public void run() {
        // Show help if no command specified
        CommandLine.usage(this, System.out);
    }

    /**
     * Custom exception handler for better error messages
     */
    static class ExceptionHandler implements CommandLine.IExecutionExceptionHandler {
        @Override
        public int handleExecutionException(Exception ex,
                                            CommandLine commandLine,
                                            CommandLine.ParseResult parseResult) {
            commandLine.getErr().println(commandLine.getColorScheme().errorText("Error: " + ex.getMessage()));

            if (commandLine.getCommandSpec().findOption("--verbose") != null) {
                ex.printStackTrace(commandLine.getErr());
            } else {
                commandLine.getErr().println("Run with --verbose for stack trace.");
            }

            return commandLine.getExitCodeExceptionMapper() != null
                    ? commandLine.getExitCodeExceptionMapper().getExitCode(ex)
                    : commandLine.getCommandSpec().exitCodeOnExecutionException();
        }
    }
}
