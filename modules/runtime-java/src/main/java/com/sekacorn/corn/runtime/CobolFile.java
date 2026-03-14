/*
 * CobolFile - COBOL file I/O abstraction
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.runtime;

import java.io.Closeable;
import java.util.Optional;

/**
 * Abstract base for COBOL file operations.
 * Provides pluggable backends for different file organizations.
 * Critical for financial institution file processing.
 *
 * @param <T> Record type
 */
public interface CobolFile<T> extends Closeable {

    /**
     * Open the file with specified mode
     */
    FileStatus open(OpenMode mode);

    /**
     * Close the file
     */
    void close();

    /**
     * Read next record
     */
    Result<T> read();

    /**
     * Write record
     */
    FileStatus write(T record);

    /**
     * Rewrite current record (update)
     */
    FileStatus rewrite(T record);

    /**
     * Delete current record
     */
    FileStatus delete();

    /**
     * Get current file status
     */
    FileStatus getStatus();

    /**
     * File open modes
     */
    enum OpenMode {
        INPUT,
        OUTPUT,
        I_O,
        EXTEND
    }

    /**
     * COBOL file status codes (as per COBOL standard)
     */
    enum FileStatus {
        SUCCESS("00", "Successful completion"),
        END_OF_FILE("10", "End of file"),
        INVALID_KEY("23", "Record not found (invalid key)"),
        DUPLICATE_KEY("22", "Duplicate key"),
        FILE_NOT_FOUND("35", "File not found"),
        PERMISSION_DENIED("37", "Permission denied"),
        DISK_FULL("34", "Disk full or boundary violation"),
        LOGIC_ERROR("48", "Attempted to write to INPUT or read from OUTPUT"),
        IO_ERROR("90", "General I/O error"),
        UNKNOWN("99", "Unknown error");

        private final String code;
        private final String description;

        FileStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public boolean isSuccess() {
            return this == SUCCESS;
        }

        public boolean isError() {
            return !isSuccess() && this != END_OF_FILE;
        }
    }

    /**
     * Result wrapper for read operations
     */
    record Result<T>(T record, FileStatus status) {
        public boolean isSuccess() {
            return status.isSuccess();
        }

        public boolean isAtEnd() {
            return status == FileStatus.END_OF_FILE;
        }

        public Optional<T> getRecord() {
            return Optional.ofNullable(record);
        }
    }
}
