/*
 * GuiCommand - Open workspace in file explorer
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.awt.Desktop;
import java.io.File;
import java.util.concurrent.Callable;

/**
 * Open the workspace in the system file explorer.
 */
@Command(
        name = "gui",
        description = "Open the workspace in the system file explorer",
        mixinStandardHelpOptions = true
)
public class GuiCommand implements Callable<Integer> {

    @Option(
            names = {"--workspace"},
            description = "Workspace directory to open"
    )
    private String workspace;

    @Override
    public Integer call() throws Exception {
        System.out.println("=== Opening Workspace Explorer ===");

        File workspaceDir = workspace != null ? new File(workspace) : new File(".");
        System.out.println("Workspace: " + workspaceDir.getAbsolutePath());
        if (!workspaceDir.exists()) {
            System.err.println("Error: Workspace does not exist.");
            return 1;
        }

        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(workspaceDir);
                System.out.println("\nOpened workspace in system file explorer.");
                return 0;
            } catch (Exception e) {
                System.err.println("Could not open desktop explorer: " + e.getMessage());
            }
        }

        System.out.println("\nDesktop integration unavailable in this environment.");
        System.out.println("Fallback: use CLI commands from this workspace.");
        return 1;
    }
}
