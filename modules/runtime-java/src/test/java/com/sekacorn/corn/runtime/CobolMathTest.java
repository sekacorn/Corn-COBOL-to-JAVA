/*
 * CobolMathTest - Unit tests for COBOL arithmetic semantics
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.runtime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CobolMath — COBOL arithmetic semantics")
class CobolMathTest {

    // PIC 9(7)V99 => scale=2, precision=9
    private static final int SCALE_2 = 2;
    private static final int PRECISION_9 = 9;

    // PIC 9(5)V99 => scale=2, precision=7
    private static final int PRECISION_7 = 7;

    @Nested
    @DisplayName("ADD operations")
    class AddTests {
        @Test
        @DisplayName("simple addition within precision")
        void simpleAdd() {
            var result = CobolMath.add(
                    new BigDecimal("100.50"),
                    new BigDecimal("200.75"),
                    SCALE_2, PRECISION_9);
            assertThat(result.hasError()).isFalse();
            assertThat(result.value()).isEqualByComparingTo("301.25");
        }

        @Test
        @DisplayName("addition with rounding to target scale")
        void addWithRounding() {
            var result = CobolMath.add(
                    new BigDecimal("1.005"),
                    new BigDecimal("2.005"),
                    SCALE_2, PRECISION_9);
            assertThat(result.hasError()).isFalse();
            // 3.010 rounded to 2 decimal places = 3.01
            assertThat(result.value()).isEqualByComparingTo("3.01");
        }

        @Test
        @DisplayName("addition causing size error (overflow)")
        void addOverflow() {
            // PIC 9(5)V99 -> precision 7, scale 2
            var result = CobolMath.add(
                    new BigDecimal("99999.99"),
                    new BigDecimal("1.00"),
                    SCALE_2, PRECISION_7);
            assertThat(result.hasError()).isTrue();
        }

        @Test
        @DisplayName("adding zero")
        void addZero() {
            var result = CobolMath.add(
                    new BigDecimal("12345.67"),
                    BigDecimal.ZERO,
                    SCALE_2, PRECISION_9);
            assertThat(result.hasError()).isFalse();
            assertThat(result.value()).isEqualByComparingTo("12345.67");
        }

        @Test
        @DisplayName("adding negative numbers")
        void addNegative() {
            var result = CobolMath.add(
                    new BigDecimal("100.00"),
                    new BigDecimal("-50.25"),
                    SCALE_2, PRECISION_9);
            assertThat(result.hasError()).isFalse();
            assertThat(result.value()).isEqualByComparingTo("49.75");
        }
    }

    @Nested
    @DisplayName("SUBTRACT operations")
    class SubtractTests {
        @Test
        @DisplayName("simple subtraction")
        void simpleSubtract() {
            var result = CobolMath.subtract(
                    new BigDecimal("500.00"),
                    new BigDecimal("200.50"),
                    SCALE_2, PRECISION_9);
            assertThat(result.hasError()).isFalse();
            assertThat(result.value()).isEqualByComparingTo("299.50");
        }

        @Test
        @DisplayName("subtraction resulting in negative")
        void subtractNegativeResult() {
            var result = CobolMath.subtract(
                    new BigDecimal("100.00"),
                    new BigDecimal("200.00"),
                    SCALE_2, PRECISION_9);
            assertThat(result.hasError()).isFalse();
            assertThat(result.value()).isEqualByComparingTo("-100.00");
        }
    }

    @Nested
    @DisplayName("MULTIPLY operations")
    class MultiplyTests {
        @Test
        @DisplayName("simple multiplication")
        void simpleMultiply() {
            var result = CobolMath.multiply(
                    new BigDecimal("10.50"),
                    new BigDecimal("3.00"),
                    SCALE_2, PRECISION_9);
            assertThat(result.hasError()).isFalse();
            assertThat(result.value()).isEqualByComparingTo("31.50");
        }

        @Test
        @DisplayName("multiplication causing size error")
        void multiplyOverflow() {
            var result = CobolMath.multiply(
                    new BigDecimal("99999.99"),
                    new BigDecimal("100.00"),
                    SCALE_2, PRECISION_7);
            assertThat(result.hasError()).isTrue();
        }

        @Test
        @DisplayName("multiply by zero")
        void multiplyByZero() {
            var result = CobolMath.multiply(
                    new BigDecimal("12345.67"),
                    BigDecimal.ZERO,
                    SCALE_2, PRECISION_9);
            assertThat(result.hasError()).isFalse();
            assertThat(result.value()).isEqualByComparingTo("0.00");
        }
    }

    @Nested
    @DisplayName("DIVIDE operations")
    class DivideTests {
        @Test
        @DisplayName("simple division")
        void simpleDivide() {
            var result = CobolMath.divide(
                    new BigDecimal("100.00"),
                    new BigDecimal("3.00"),
                    SCALE_2, PRECISION_9,
                    RoundingMode.HALF_UP);
            assertThat(result.hasError()).isFalse();
            assertThat(result.value()).isEqualByComparingTo("33.33");
        }

        @Test
        @DisplayName("division by zero returns size error")
        void divideByZero() {
            var result = CobolMath.divide(
                    new BigDecimal("100.00"),
                    BigDecimal.ZERO,
                    SCALE_2, PRECISION_9,
                    RoundingMode.HALF_UP);
            assertThat(result.hasError()).isTrue();
            assertThat(result.value()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("exact division")
        void exactDivide() {
            var result = CobolMath.divide(
                    new BigDecimal("100.00"),
                    new BigDecimal("4.00"),
                    SCALE_2, PRECISION_9,
                    RoundingMode.HALF_UP);
            assertThat(result.hasError()).isFalse();
            assertThat(result.value()).isEqualByComparingTo("25.00");
        }

        @Test
        @DisplayName("division with different rounding modes")
        void divideRoundingModes() {
            // 10 / 3 = 3.333...
            var halfUp = CobolMath.divide(
                    new BigDecimal("10.00"),
                    new BigDecimal("3.00"),
                    SCALE_2, PRECISION_9,
                    RoundingMode.HALF_UP);
            assertThat(halfUp.value()).isEqualByComparingTo("3.33");

            var down = CobolMath.divide(
                    new BigDecimal("10.00"),
                    new BigDecimal("3.00"),
                    SCALE_2, PRECISION_9,
                    RoundingMode.DOWN);
            assertThat(down.value()).isEqualByComparingTo("3.33");
        }
    }

    @Nested
    @DisplayName("COMPUTE operations")
    class ComputeTests {
        @Test
        @DisplayName("compute truncates value to target scale (COBOL default: no ROUNDED)")
        void computeScales() {
            var result = CobolMath.compute(
                    new BigDecimal("123.456789"),
                    SCALE_2, PRECISION_9);
            assertThat(result.hasError()).isFalse();
            // COBOL truncates without ROUNDED phrase
            assertThat(result.value()).isEqualByComparingTo("123.45");
        }

        @Test
        @DisplayName("compute detects size error")
        void computeOverflow() {
            var result = CobolMath.compute(
                    new BigDecimal("9999999.99"),
                    SCALE_2, PRECISION_7);
            // 9999999.99 has precision 9 which exceeds target precision 7
            assertThat(result.hasError()).isTrue();
        }
    }

    @Nested
    @DisplayName("Rounding modes")
    class RoundingTests {
        @Test
        @DisplayName("TRUNCATION mode")
        void truncationMode() {
            BigDecimal result = CobolMath.round(
                    new BigDecimal("3.456"), 2, CobolMath.RoundMode.TRUNCATION);
            assertThat(result).isEqualByComparingTo("3.45");
        }

        @Test
        @DisplayName("HALF_UP mode (COBOL default)")
        void halfUpMode() {
            BigDecimal result = CobolMath.round(
                    new BigDecimal("3.455"), 2, CobolMath.RoundMode.HALF_UP);
            assertThat(result).isEqualByComparingTo("3.46");
        }

        @Test
        @DisplayName("HALF_EVEN mode (Banker's rounding)")
        void halfEvenMode() {
            BigDecimal even = CobolMath.round(
                    new BigDecimal("2.445"), 2, CobolMath.RoundMode.HALF_EVEN);
            assertThat(even).isEqualByComparingTo("2.44");

            BigDecimal odd = CobolMath.round(
                    new BigDecimal("2.455"), 2, CobolMath.RoundMode.HALF_EVEN);
            assertThat(odd).isEqualByComparingTo("2.46");
        }

        @Test
        @DisplayName("CEILING and FLOOR modes")
        void ceilingFloor() {
            BigDecimal ceil = CobolMath.round(
                    new BigDecimal("-3.451"), 2, CobolMath.RoundMode.CEILING);
            assertThat(ceil).isEqualByComparingTo("-3.45");

            BigDecimal floor = CobolMath.round(
                    new BigDecimal("-3.451"), 2, CobolMath.RoundMode.FLOOR);
            assertThat(floor).isEqualByComparingTo("-3.46");
        }
    }

    @Nested
    @DisplayName("Comparison operations")
    class CompareTests {
        @Test
        @DisplayName("compare equal values")
        void compareEqual() {
            assertThat(CobolMath.compare(
                    new BigDecimal("100.00"),
                    new BigDecimal("100.00"))).isEqualTo(0);
        }

        @Test
        @DisplayName("compare different scales but equal values")
        void compareDifferentScales() {
            assertThat(CobolMath.compare(
                    new BigDecimal("100"),
                    new BigDecimal("100.00"))).isEqualTo(0);
        }

        @Test
        @DisplayName("compare with nulls")
        void compareNulls() {
            assertThat(CobolMath.compare(null, null)).isEqualTo(0);
            assertThat(CobolMath.compare(null, BigDecimal.ONE)).isLessThan(0);
            assertThat(CobolMath.compare(BigDecimal.ONE, null)).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Numeric type checks")
    class NumericCheckTests {
        @ParameterizedTest
        @CsvSource({
                "'123', true",
                "'123.45', true",
                "'-456.78', true",
                "'+99', true",
                "'abc', false",
                "'', false",
                "'12.34.56', false"
        })
        @DisplayName("isNumeric checks")
        void isNumeric(String value, boolean expected) {
            assertThat(CobolMath.isNumeric(value)).isEqualTo(expected);
        }

        @Test
        @DisplayName("isNumeric with null")
        void isNumericNull() {
            assertThat(CobolMath.isNumeric(null)).isFalse();
        }

        @Test
        @DisplayName("isPositive checks")
        void isPositive() {
            assertThat(CobolMath.isPositive(new BigDecimal("1"))).isTrue();
            assertThat(CobolMath.isPositive(BigDecimal.ZERO)).isFalse();
            assertThat(CobolMath.isPositive(new BigDecimal("-1"))).isFalse();
            assertThat(CobolMath.isPositive(null)).isFalse();
        }

        @Test
        @DisplayName("isNegative checks")
        void isNegative() {
            assertThat(CobolMath.isNegative(new BigDecimal("-1"))).isTrue();
            assertThat(CobolMath.isNegative(BigDecimal.ZERO)).isFalse();
            assertThat(CobolMath.isNegative(new BigDecimal("1"))).isFalse();
            assertThat(CobolMath.isNegative(null)).isFalse();
        }

        @Test
        @DisplayName("isZero checks")
        void isZero() {
            assertThat(CobolMath.isZero(BigDecimal.ZERO)).isTrue();
            assertThat(CobolMath.isZero(new BigDecimal("0.00"))).isTrue();
            assertThat(CobolMath.isZero(new BigDecimal("1"))).isFalse();
            assertThat(CobolMath.isZero(null)).isTrue(); // COBOL: uninitialized = zero
        }
    }

    @Nested
    @DisplayName("Financial calculation scenarios")
    class FinancialTests {
        @Test
        @DisplayName("interest calculation: principal * rate")
        void interestCalc() {
            // PIC 9(9)V99
            BigDecimal principal = new BigDecimal("50000.00");
            BigDecimal rate = new BigDecimal("0.05"); // 5%
            var result = CobolMath.multiply(principal, rate, SCALE_2, PRECISION_9);
            assertThat(result.hasError()).isFalse();
            assertThat(result.value()).isEqualByComparingTo("2500.00");
        }

        @Test
        @DisplayName("running balance through multiple operations")
        void runningBalance() {
            BigDecimal balance = new BigDecimal("10000.00");

            // Deposit
            var afterDeposit = CobolMath.add(balance, new BigDecimal("5000.00"), SCALE_2, PRECISION_9);
            assertThat(afterDeposit.hasError()).isFalse();

            // Withdrawal
            var afterWithdraw = CobolMath.subtract(afterDeposit.value(), new BigDecimal("3000.50"), SCALE_2, PRECISION_9);
            assertThat(afterWithdraw.hasError()).isFalse();
            assertThat(afterWithdraw.value()).isEqualByComparingTo("11999.50");
        }
    }

    @Nested
    @DisplayName("Context-aware arithmetic")
    class ContextAwareTests {
        @Test
        @DisplayName("add with ArithmeticContext — no rounding")
        void addWithContext() {
            var ctx = ArithmeticContext.ofPicture(SCALE_2, PRECISION_9);
            var result = CobolMath.add(
                    new BigDecimal("100.50"),
                    new BigDecimal("200.75"), ctx);
            assertThat(result.hasError()).isFalse();
            assertThat(result.value()).isEqualByComparingTo("301.25");
        }

        @Test
        @DisplayName("add with ArithmeticContext — ROUNDED HALF_UP")
        void addWithRounded() {
            var ctx = ArithmeticContext.ofPicture(SCALE_2, PRECISION_9)
                    .withRounded(CobolMath.RoundMode.HALF_UP);
            var result = CobolMath.add(
                    new BigDecimal("1.005"),
                    new BigDecimal("2.005"), ctx);
            assertThat(result.hasError()).isFalse();
            assertThat(result.value()).isEqualByComparingTo("3.01");
        }

        @Test
        @DisplayName("subtract with ArithmeticContext")
        void subtractWithContext() {
            var ctx = ArithmeticContext.ofPicture(SCALE_2, PRECISION_9);
            var result = CobolMath.subtract(
                    new BigDecimal("500.00"),
                    new BigDecimal("200.50"), ctx);
            assertThat(result.hasError()).isFalse();
            assertThat(result.value()).isEqualByComparingTo("299.50");
        }

        @Test
        @DisplayName("multiply with ArithmeticContext")
        void multiplyWithContext() {
            var ctx = ArithmeticContext.ofPicture(SCALE_2, PRECISION_9);
            var result = CobolMath.multiply(
                    new BigDecimal("10.50"),
                    new BigDecimal("3.00"), ctx);
            assertThat(result.hasError()).isFalse();
            assertThat(result.value()).isEqualByComparingTo("31.50");
        }

        @Test
        @DisplayName("divide with ArithmeticContext")
        void divideWithContext() {
            var ctx = ArithmeticContext.ofPicture(SCALE_2, PRECISION_9);
            var result = CobolMath.divide(
                    new BigDecimal("100.00"),
                    new BigDecimal("3.00"), ctx);
            assertThat(result.hasError()).isFalse();
            assertThat(result.value()).isEqualByComparingTo("33.33");
        }

        @Test
        @DisplayName("compute with ArithmeticContext truncates without ROUNDED")
        void computeWithContext() {
            var ctx = ArithmeticContext.ofPicture(SCALE_2, PRECISION_9);
            var result = CobolMath.compute(new BigDecimal("123.456789"), ctx);
            assertThat(result.hasError()).isFalse();
            // Without ROUNDED, COBOL truncates
            assertThat(result.value()).isEqualByComparingTo("123.45");
        }

        @Test
        @DisplayName("compute with ROUNDED HALF_UP rounds instead of truncating")
        void computeWithRounded() {
            var ctx = ArithmeticContext.ofPicture(SCALE_2, PRECISION_9)
                    .withRounded(CobolMath.RoundMode.HALF_UP);
            var result = CobolMath.compute(new BigDecimal("123.456789"), ctx);
            assertThat(result.hasError()).isFalse();
            // With ROUNDED HALF_UP, .456 rounds up to .46
            assertThat(result.value()).isEqualByComparingTo("123.46");
        }

        @Test
        @DisplayName("context with size error active")
        void contextWithSizeError() {
            var ctx = ArithmeticContext.ofPicture(SCALE_2, PRECISION_7)
                    .withSizeError();
            assertThat(ctx.onSizeErrorActive()).isTrue();
        }
    }

    @Test
    @DisplayName("cannot instantiate utility class")
    void cannotInstantiate() {
        assertThatThrownBy(() -> {
            var ctor = CobolMath.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            ctor.newInstance();
        }).hasCauseInstanceOf(AssertionError.class);
    }
}
