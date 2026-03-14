/*
 * IndexedFile - Indexed file organization (VSAM-like)
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.runtime;

/**
 * Indexed file organization (VSAM-like).
 * Records are accessed by key value.
 *
 * @param <T> Record type
 */
public interface IndexedFile<T> extends CobolFile<T> {
    /**
     * Read by key
     */
    Result<T> read(Object key);

    /**
     * Start positioning at key
     */
    FileStatus start(Object key, KeyComparison comparison);

    /**
     * Key comparison operators
     */
    enum KeyComparison {
        EQUAL,
        GREATER,
        GREATER_OR_EQUAL,
        NOT_LESS
    }
}
