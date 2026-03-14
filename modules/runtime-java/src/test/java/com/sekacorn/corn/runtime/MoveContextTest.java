/*
 * MoveContextTest - Unit tests for MoveContext record
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.runtime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MoveContext — context for COBOL MOVE operations")
class MoveContextTest {

    @Test
    @DisplayName("alphanumeric creates left-justified context by default")
    void alphanumericDefault() {
        var ctx = MoveContext.alphanumeric(10, false);
        assertThat(ctx.targetLength()).isEqualTo(10);
        assertThat(ctx.justified()).isFalse();
        assertThat(ctx.blankWhenZero()).isFalse();
        assertThat(ctx.sign()).isNull();
    }

    @Test
    @DisplayName("alphanumeric with JUSTIFIED RIGHT")
    void alphanumericJustified() {
        var ctx = MoveContext.alphanumeric(10, true);
        assertThat(ctx.justified()).isTrue();
    }

    @Test
    @DisplayName("numeric creates context with BLANK WHEN ZERO option")
    void numericBlankWhenZero() {
        var ctx = MoveContext.numeric(5, true);
        assertThat(ctx.targetLength()).isEqualTo(5);
        assertThat(ctx.blankWhenZero()).isTrue();
        assertThat(ctx.justified()).isFalse();
    }

    @Test
    @DisplayName("numeric without BLANK WHEN ZERO")
    void numericNoBlank() {
        var ctx = MoveContext.numeric(5, false);
        assertThat(ctx.blankWhenZero()).isFalse();
    }

    @Test
    @DisplayName("withSign sets sign position")
    void withSign() {
        var ctx = MoveContext.numeric(7, false)
                .withSign(MoveContext.SignPosition.LEADING);
        assertThat(ctx.sign()).isEqualTo(MoveContext.SignPosition.LEADING);
    }

    @Test
    @DisplayName("all sign positions defined")
    void signPositions() {
        assertThat(MoveContext.SignPosition.values()).hasSize(4);
        assertThat(MoveContext.SignPosition.values()).containsExactly(
                MoveContext.SignPosition.LEADING,
                MoveContext.SignPosition.TRAILING,
                MoveContext.SignPosition.LEADING_SEPARATE,
                MoveContext.SignPosition.TRAILING_SEPARATE);
    }

    @Test
    @DisplayName("record equality")
    void equality() {
        var a = MoveContext.alphanumeric(10, false);
        var b = MoveContext.alphanumeric(10, false);
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("different contexts are not equal")
    void inequality() {
        var a = MoveContext.alphanumeric(10, false);
        var b = MoveContext.alphanumeric(10, true);
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("withSign preserves other fields")
    void withSignPreservesFields() {
        var ctx = MoveContext.numeric(8, true)
                .withSign(MoveContext.SignPosition.TRAILING_SEPARATE);
        assertThat(ctx.targetLength()).isEqualTo(8);
        assertThat(ctx.blankWhenZero()).isTrue();
        assertThat(ctx.justified()).isFalse();
        assertThat(ctx.sign()).isEqualTo(MoveContext.SignPosition.TRAILING_SEPARATE);
    }
}
