/*
 * RelativeFile - Relative file organization
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.runtime;

/**
 * Relative file organization.
 * Records are accessed by relative record number.
 *
 * @param <T> Record type
 */
public interface RelativeFile<T> extends CobolFile<T> {
    /**
     * Read by relative record number
     */
    Result<T> read(long recordNumber);

    /**
     * Write at specific relative record number
     */
    FileStatus write(long recordNumber, T record);
}
