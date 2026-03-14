/*
 * SimpleCobolFile - In-memory CobolFile implementation
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.runtime;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal in-memory file backend used by generated code.
 *
 * @param <T> record type
 */
public final class SimpleCobolFile<T> implements CobolFile<T> {
    private final String logicalName;
    private final List<T> records = new ArrayList<>();
    private int cursor = 0;
    private OpenMode openMode = null;
    private FileStatus status = FileStatus.SUCCESS;

    public SimpleCobolFile(String logicalName) {
        this.logicalName = logicalName;
    }

    public String getLogicalName() {
        return logicalName;
    }

    @Override
    public FileStatus open(OpenMode mode) {
        openMode = mode;
        if (mode == OpenMode.OUTPUT) {
            records.clear();
            cursor = 0;
        } else if (mode == OpenMode.EXTEND) {
            cursor = records.size();
        } else {
            cursor = 0;
        }
        status = FileStatus.SUCCESS;
        return status;
    }

    @Override
    public void close() {
        openMode = null;
        status = FileStatus.SUCCESS;
    }

    @Override
    public Result<T> read() {
        if (openMode == OpenMode.OUTPUT || openMode == OpenMode.EXTEND) {
            status = FileStatus.LOGIC_ERROR;
            return new Result<>(null, status);
        }
        if (cursor >= records.size()) {
            status = FileStatus.END_OF_FILE;
            return new Result<>(null, status);
        }
        T record = records.get(cursor++);
        status = FileStatus.SUCCESS;
        return new Result<>(record, status);
    }

    @Override
    public FileStatus write(T record) {
        if (openMode == OpenMode.INPUT) {
            status = FileStatus.LOGIC_ERROR;
            return status;
        }
        if (openMode == OpenMode.EXTEND) {
            records.add(record);
            cursor = records.size();
        } else if (openMode == OpenMode.OUTPUT || openMode == OpenMode.I_O) {
            if (cursor < records.size()) {
                records.set(cursor, record);
            } else {
                records.add(record);
            }
            cursor++;
        } else {
            status = FileStatus.LOGIC_ERROR;
            return status;
        }
        status = FileStatus.SUCCESS;
        return status;
    }

    @Override
    public FileStatus rewrite(T record) {
        if (openMode != OpenMode.I_O || cursor <= 0 || cursor > records.size()) {
            status = FileStatus.LOGIC_ERROR;
            return status;
        }
        records.set(cursor - 1, record);
        status = FileStatus.SUCCESS;
        return status;
    }

    @Override
    public FileStatus delete() {
        if (openMode != OpenMode.I_O || cursor <= 0 || cursor > records.size()) {
            status = FileStatus.LOGIC_ERROR;
            return status;
        }
        records.remove(cursor - 1);
        cursor--;
        status = FileStatus.SUCCESS;
        return status;
    }

    @Override
    public FileStatus getStatus() {
        return status;
    }
}
