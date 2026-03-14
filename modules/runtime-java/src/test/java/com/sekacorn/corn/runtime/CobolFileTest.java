/*
 * CobolFileTest - Unit tests for COBOL file I/O abstraction
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.runtime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CobolFile — File I/O abstraction")
class CobolFileTest {

    @Nested
    @DisplayName("FileStatus enum")
    class FileStatusTests {
        @Test
        @DisplayName("SUCCESS status code is 00")
        void successCode() {
            assertThat(CobolFile.FileStatus.SUCCESS.getCode()).isEqualTo("00");
            assertThat(CobolFile.FileStatus.SUCCESS.isSuccess()).isTrue();
            assertThat(CobolFile.FileStatus.SUCCESS.isError()).isFalse();
        }

        @Test
        @DisplayName("END_OF_FILE is not an error")
        void endOfFile() {
            assertThat(CobolFile.FileStatus.END_OF_FILE.getCode()).isEqualTo("10");
            assertThat(CobolFile.FileStatus.END_OF_FILE.isSuccess()).isFalse();
            assertThat(CobolFile.FileStatus.END_OF_FILE.isError()).isFalse();
        }

        @Test
        @DisplayName("error statuses are correctly identified")
        void errorStatuses() {
            assertThat(CobolFile.FileStatus.INVALID_KEY.isError()).isTrue();
            assertThat(CobolFile.FileStatus.DUPLICATE_KEY.isError()).isTrue();
            assertThat(CobolFile.FileStatus.FILE_NOT_FOUND.isError()).isTrue();
            assertThat(CobolFile.FileStatus.PERMISSION_DENIED.isError()).isTrue();
            assertThat(CobolFile.FileStatus.DISK_FULL.isError()).isTrue();
            assertThat(CobolFile.FileStatus.LOGIC_ERROR.isError()).isTrue();
            assertThat(CobolFile.FileStatus.IO_ERROR.isError()).isTrue();
            assertThat(CobolFile.FileStatus.UNKNOWN.isError()).isTrue();
        }

        @Test
        @DisplayName("all status codes follow COBOL standard format")
        void standardCodes() {
            assertThat(CobolFile.FileStatus.INVALID_KEY.getCode()).isEqualTo("23");
            assertThat(CobolFile.FileStatus.DUPLICATE_KEY.getCode()).isEqualTo("22");
            assertThat(CobolFile.FileStatus.FILE_NOT_FOUND.getCode()).isEqualTo("35");
            assertThat(CobolFile.FileStatus.LOGIC_ERROR.getCode()).isEqualTo("48");
        }

        @Test
        @DisplayName("all statuses have descriptions")
        void descriptions() {
            for (CobolFile.FileStatus status : CobolFile.FileStatus.values()) {
                assertThat(status.getDescription()).isNotNull().isNotEmpty();
            }
        }
    }

    @Nested
    @DisplayName("OpenMode enum")
    class OpenModeTests {
        @Test
        @DisplayName("all open modes defined")
        void allModes() {
            assertThat(CobolFile.OpenMode.values()).containsExactly(
                    CobolFile.OpenMode.INPUT,
                    CobolFile.OpenMode.OUTPUT,
                    CobolFile.OpenMode.I_O,
                    CobolFile.OpenMode.EXTEND);
        }
    }

    @Nested
    @DisplayName("Result record")
    class ResultTests {
        @Test
        @DisplayName("successful result")
        void successResult() {
            var result = new CobolFile.Result<>("DATA", CobolFile.FileStatus.SUCCESS);
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.isAtEnd()).isFalse();
            assertThat(result.getRecord()).isPresent().contains("DATA");
        }

        @Test
        @DisplayName("end of file result")
        void endOfFileResult() {
            var result = new CobolFile.Result<String>(null, CobolFile.FileStatus.END_OF_FILE);
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.isAtEnd()).isTrue();
            assertThat(result.getRecord()).isEmpty();
        }

        @Test
        @DisplayName("error result")
        void errorResult() {
            var result = new CobolFile.Result<String>(null, CobolFile.FileStatus.FILE_NOT_FOUND);
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.isAtEnd()).isFalse();
            assertThat(result.getRecord()).isEmpty();
        }
    }

    @Nested
    @DisplayName("IndexedFile.KeyComparison enum")
    class KeyComparisonTests {
        @Test
        @DisplayName("all comparison types defined")
        void allComparisons() {
            assertThat(IndexedFile.KeyComparison.values()).containsExactly(
                    IndexedFile.KeyComparison.EQUAL,
                    IndexedFile.KeyComparison.GREATER,
                    IndexedFile.KeyComparison.GREATER_OR_EQUAL,
                    IndexedFile.KeyComparison.NOT_LESS);
        }
    }
}
