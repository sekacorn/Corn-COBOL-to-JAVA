/*
 * SequentialFile - Sequential file organization
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.runtime;

/**
 * Sequential file organization.
 * Records are accessed in the order they were written.
 *
 * @param <T> Record type
 */
public interface SequentialFile<T> extends CobolFile<T> {
    // Sequential-specific operations
}
