/*
 * CobolRuntime - Main runtime entry point
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.runtime;

/**
 * Main entry point for the COBOL runtime library.
 * Provides version info and initialization hooks.
 */
public final class CobolRuntime {
    public static final String VERSION = "1.0.0-SNAPSHOT";
    public static final String NAME = "Corn COBOL Runtime";

    private CobolRuntime() {
        throw new AssertionError("No instances");
    }

    /**
     * Initialize the runtime with default settings
     */
    public static void initialize() {
        // Hook for future initialization if needed
    }

    /**
     * Get runtime version
     */
    public static String getVersion() {
        return VERSION;
    }
}
