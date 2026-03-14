/*
 * CobolRuntimeTest - Unit tests for CobolRuntime entry point
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.runtime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CobolRuntime — main runtime entry point")
class CobolRuntimeTest {

    @Test
    @DisplayName("VERSION constant is set")
    void versionConstant() {
        assertThat(CobolRuntime.VERSION).isNotBlank();
        assertThat(CobolRuntime.VERSION).isEqualTo("1.0.0-SNAPSHOT");
    }

    @Test
    @DisplayName("NAME constant is set")
    void nameConstant() {
        assertThat(CobolRuntime.NAME).isEqualTo("Corn COBOL Runtime");
    }

    @Test
    @DisplayName("getVersion returns VERSION constant")
    void getVersion() {
        assertThat(CobolRuntime.getVersion()).isEqualTo(CobolRuntime.VERSION);
    }

    @Test
    @DisplayName("initialize does not throw")
    void initializeNoOp() {
        assertThatCode(CobolRuntime::initialize).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("constructor is private and throws AssertionError")
    void privateConstructor() throws Exception {
        var constructor = CobolRuntime.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(AssertionError.class);
    }
}
