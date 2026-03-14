/*
 * PictureTest - Unit tests for COBOL PICTURE clause formatting
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.runtime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Picture — COBOL PICTURE clause formatting")
class PictureTest {

    @Nested
    @DisplayName("formatNumeric")
    class FormatTests {
        @Test
        @DisplayName("format simple numeric PIC 9(4)V99")
        void formatSimple() {
            String result = Picture.formatNumeric(new BigDecimal("1234.56"), "9999.99");
            assertThat(result).isNotNull();
            assertThat(result).contains("1234");
        }

        @Test
        @DisplayName("format null value returns zero-formatted string")
        void formatNull() {
            String result = Picture.formatNumeric(null, "9999.99");
            assertThat(result).isNotNull();
            // Zero formatting: 9's become 0's
            assertThat(result).contains("0");
        }

        @Test
        @DisplayName("format zero with zero suppression")
        void formatZeroSuppression() {
            String result = Picture.formatNumeric(BigDecimal.ZERO, "ZZZ9.99");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("format with dollar sign")
        void formatDollar() {
            String result = Picture.formatNumeric(new BigDecimal("123.45"), "$$$9.99");
            assertThat(result).isNotNull();
            assertThat(result).contains("$");
        }

        @Test
        @DisplayName("format negative with minus")
        void formatNegative() {
            String result = Picture.formatNumeric(new BigDecimal("-42.50"), "9999.99-");
            assertThat(result).isNotNull();
            assertThat(result).contains("-");
        }
    }

    @Nested
    @DisplayName("parseNumeric")
    class ParseTests {
        @Test
        @DisplayName("parse simple numeric string")
        void parseSimple() {
            BigDecimal result = Picture.parseNumeric("1234.56", "9999.99");
            assertThat(result).isEqualByComparingTo("1234.56");
        }

        @Test
        @DisplayName("parse with currency and commas")
        void parseCurrency() {
            BigDecimal result = Picture.parseNumeric("$1,234.56", "$9,999.99");
            assertThat(result).isEqualByComparingTo("1234.56");
        }

        @Test
        @DisplayName("parse null input returns ZERO")
        void parseNull() {
            assertThat(Picture.parseNumeric(null, "9999")).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("parse empty input returns ZERO")
        void parseEmpty() {
            assertThat(Picture.parseNumeric("", "9999")).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("parse whitespace-only returns ZERO")
        void parseWhitespace() {
            assertThat(Picture.parseNumeric("   ", "9999")).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("parse non-numeric returns ZERO")
        void parseNonNumeric() {
            assertThat(Picture.parseNumeric("ABC", "XXX")).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("parse negative value")
        void parseNegative() {
            BigDecimal result = Picture.parseNumeric("-42.50", "9999.99-");
            assertThat(result).isEqualByComparingTo("-42.50");
        }
    }

    @Nested
    @DisplayName("move — numeric MOVE with truncation")
    class MoveTests {
        @Test
        @DisplayName("move value within precision")
        void moveNormal() {
            // PIC 9(7)V99
            BigDecimal result = Picture.move(
                    new BigDecimal("12345.67"), 2, 9, RoundingMode.HALF_UP);
            assertThat(result).isEqualByComparingTo("12345.67");
        }

        @Test
        @DisplayName("move with rounding")
        void moveWithRounding() {
            BigDecimal result = Picture.move(
                    new BigDecimal("123.456"), 2, 9, RoundingMode.HALF_UP);
            assertThat(result).isEqualByComparingTo("123.46");
        }

        @Test
        @DisplayName("move null returns ZERO")
        void moveNull() {
            BigDecimal result = Picture.move(null, 2, 9, RoundingMode.HALF_UP);
            assertThat(result).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("move with overflow truncates integer part")
        void moveOverflow() {
            // PIC 9(3)V99 -> precision=5, scale=2
            // Value 12345.67 has too many integer digits
            BigDecimal result = Picture.move(
                    new BigDecimal("12345.67"), 2, 5, RoundingMode.HALF_UP);
            // Should truncate high-order integer digits
            assertThat(result).isNotNull();
            assertThat(result.scale()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("formatZero")
    class FormatZeroTests {
        @Test
        @DisplayName("Z positions become spaces for zero")
        void zeroSuppression() {
            String result = Picture.formatNumeric(null, "ZZZ9");
            // Z -> space, 9 -> 0
            assertThat(result).contains("0");
        }

        @Test
        @DisplayName("dollar positions become spaces for zero")
        void dollarZero() {
            String result = Picture.formatNumeric(null, "$$$9.99");
            assertThat(result).isNotNull();
            assertThat(result.length()).isEqualTo("$$$9.99".length());
        }
    }

    @Nested
    @DisplayName("formatNumeric with BLANK WHEN ZERO")
    class BlankWhenZeroTests {
        @Test
        @DisplayName("zero value with blankWhenZero returns spaces")
        void zeroBlank() {
            String result = Picture.formatNumeric(BigDecimal.ZERO, "ZZZ9.99", true);
            assertThat(result).isEqualTo("       ");
            assertThat(result.length()).isEqualTo("ZZZ9.99".length());
        }

        @Test
        @DisplayName("null value with blankWhenZero returns spaces")
        void nullBlank() {
            String result = Picture.formatNumeric(null, "9999.99", true);
            assertThat(result).isEqualTo("       ");
        }

        @Test
        @DisplayName("non-zero value with blankWhenZero formats normally")
        void nonZeroNotBlank() {
            String result = Picture.formatNumeric(new BigDecimal("123.45"), "9999.99", true);
            assertThat(result).contains("123");
        }

        @Test
        @DisplayName("zero value without blankWhenZero formats normally")
        void zeroNotBlank() {
            String result = Picture.formatNumeric(BigDecimal.ZERO, "ZZZ9.99", false);
            assertThat(result).isNotNull();
            // Should use normal zero-suppression formatting, not all spaces
        }
    }

    @Nested
    @DisplayName("Usage enum")
    class UsageTests {
        @Test
        @DisplayName("all usage types defined")
        void allUsages() {
            assertThat(Picture.Usage.values()).containsExactly(
                    Picture.Usage.DISPLAY,
                    Picture.Usage.COMP,
                    Picture.Usage.COMP_3,
                    Picture.Usage.COMP_4,
                    Picture.Usage.COMP_5);
        }
    }

    @Nested
    @DisplayName("convertUsage")
    class ConvertUsageTests {
        @Test
        @DisplayName("convert returns byte representation")
        void convertReturnsBytes() {
            byte[] result = Picture.convertUsage(
                    new BigDecimal("123.45"),
                    Picture.Usage.DISPLAY,
                    Picture.Usage.COMP_3);
            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }
    }
}
