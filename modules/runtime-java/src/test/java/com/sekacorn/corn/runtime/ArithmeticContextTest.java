/*
 * ArithmeticContextTest - Unit tests for ArithmeticContext record
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.runtime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ArithmeticContext — context for COBOL arithmetic operations")
class ArithmeticContextTest {

    @Test
    @DisplayName("ofPicture creates context with defaults")
    void ofPicture() {
        var ctx = ArithmeticContext.ofPicture(2, 9);
        assertThat(ctx.targetScale()).isEqualTo(2);
        assertThat(ctx.targetPrecision()).isEqualTo(9);
        assertThat(ctx.rounded()).isFalse();
        assertThat(ctx.roundMode()).isNull();
        assertThat(ctx.onSizeErrorActive()).isFalse();
    }

    @Test
    @DisplayName("withRounded enables rounding with specified mode")
    void withRounded() {
        var ctx = ArithmeticContext.ofPicture(2, 9)
                .withRounded(CobolMath.RoundMode.HALF_UP);
        assertThat(ctx.rounded()).isTrue();
        assertThat(ctx.roundMode()).isEqualTo(CobolMath.RoundMode.HALF_UP);
        assertThat(ctx.targetScale()).isEqualTo(2);
        assertThat(ctx.targetPrecision()).isEqualTo(9);
    }

    @Test
    @DisplayName("withSizeError activates size error flag")
    void withSizeError() {
        var ctx = ArithmeticContext.ofPicture(2, 7).withSizeError();
        assertThat(ctx.onSizeErrorActive()).isTrue();
        assertThat(ctx.targetScale()).isEqualTo(2);
        assertThat(ctx.targetPrecision()).isEqualTo(7);
    }

    @Test
    @DisplayName("chaining withRounded and withSizeError")
    void chaining() {
        var ctx = ArithmeticContext.ofPicture(2, 9)
                .withRounded(CobolMath.RoundMode.HALF_EVEN)
                .withSizeError();
        assertThat(ctx.rounded()).isTrue();
        assertThat(ctx.roundMode()).isEqualTo(CobolMath.RoundMode.HALF_EVEN);
        assertThat(ctx.onSizeErrorActive()).isTrue();
    }

    @Test
    @DisplayName("record equality")
    void equality() {
        var a = ArithmeticContext.ofPicture(2, 9);
        var b = ArithmeticContext.ofPicture(2, 9);
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("different contexts are not equal")
    void inequality() {
        var a = ArithmeticContext.ofPicture(2, 9);
        var b = ArithmeticContext.ofPicture(2, 9).withRounded(CobolMath.RoundMode.HALF_UP);
        assertThat(a).isNotEqualTo(b);
    }
}
