/*
 * CobolMath - COBOL arithmetic operations with proper semantics
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.runtime;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * COBOL arithmetic semantics.
 * Handles scale, precision, rounding, and size error conditions.
 * Essential for financial calculations.
 */
public final class CobolMath {

    private CobolMath() {
        throw new AssertionError("No instances");
    }

    /**
     * COBOL ADD with size error detection
     */
    public static Result add(BigDecimal a, BigDecimal b, int targetScale, int targetPrecision) {
        BigDecimal sum = a.add(b);
        return checkAndScale(sum, targetScale, targetPrecision);
    }

    /**
     * COBOL SUBTRACT with size error detection
     */
    public static Result subtract(BigDecimal a, BigDecimal b, int targetScale, int targetPrecision) {
        BigDecimal diff = a.subtract(b);
        return checkAndScale(diff, targetScale, targetPrecision);
    }

    /**
     * COBOL MULTIPLY with size error detection
     */
    public static Result multiply(BigDecimal a, BigDecimal b, int targetScale, int targetPrecision) {
        BigDecimal product = a.multiply(b);
        return checkAndScale(product, targetScale, targetPrecision);
    }

    /**
     * COBOL DIVIDE with size error detection
     */
    public static Result divide(BigDecimal a, BigDecimal b, int targetScale, int targetPrecision,
                                RoundingMode roundingMode) {
        if (b.compareTo(BigDecimal.ZERO) == 0) {
            return new Result(BigDecimal.ZERO, true);  // Division by zero
        }

        BigDecimal quotient = a.divide(b, targetScale + 2, roundingMode);
        return checkAndScale(quotient, targetScale, targetPrecision);
    }

    /**
     * COMPUTE statement - general expression evaluation
     */
    public static Result compute(BigDecimal value, int targetScale, int targetPrecision) {
        return checkAndScale(value, targetScale, targetPrecision);
    }

    /**
     * Check for size error and scale result
     */
    private static Result checkAndScale(BigDecimal value, int targetScale, int targetPrecision) {
        // Scale to target decimal places
        BigDecimal scaled = value.setScale(targetScale, RoundingMode.HALF_UP);

        // Check if value fits in target precision
        int precision = scaled.precision();
        boolean sizeError = precision > targetPrecision;

        if (sizeError) {
            // COBOL behavior: truncate but signal size error
            // In ON SIZE ERROR clause, this would branch
            return new Result(truncate(scaled, targetScale, targetPrecision), true);
        }

        return new Result(scaled, false);
    }

    /**
     * Truncate value to fit target precision (data loss!)
     * Handles both positive and negative numbers correctly.
     */
    private static BigDecimal truncate(BigDecimal value, int targetScale, int targetPrecision) {
        if (targetPrecision <= 0) {
            return BigDecimal.ZERO;
        }
        boolean negative = value.signum() < 0;
        BigDecimal abs = value.abs();
        String str = abs.toPlainString();
        int decimalPos = str.indexOf('.');

        if (decimalPos == -1) {
            // No decimal point - keep rightmost digits
            if (str.length() <= targetPrecision) {
                return value;
            }
            BigDecimal truncated = new BigDecimal(str.substring(str.length() - targetPrecision));
            return negative ? truncated.negate() : truncated;
        }

        // Keep scale and truncate integer part
        int intDigits = Math.max(0, targetPrecision - targetScale);
        if (decimalPos <= intDigits) {
            return value;
        }

        int startPos = Math.max(0, decimalPos - intDigits);
        if (startPos >= str.length()) {
            return BigDecimal.ZERO;
        }
        String truncated = str.substring(startPos);
        BigDecimal result = new BigDecimal(truncated);
        return negative ? result.negate() : result;
    }

    /**
     * COBOL ROUNDED option - various rounding modes
     */
    public static BigDecimal round(BigDecimal value, int scale, RoundMode mode) {
        return switch (mode) {
            case TRUNCATION -> value.setScale(scale, RoundingMode.DOWN);
            case HALF_UP -> value.setScale(scale, RoundingMode.HALF_UP);
            case HALF_DOWN -> value.setScale(scale, RoundingMode.HALF_DOWN);
            case HALF_EVEN -> value.setScale(scale, RoundingMode.HALF_EVEN);
            case UP -> value.setScale(scale, RoundingMode.UP);
            case DOWN -> value.setScale(scale, RoundingMode.DOWN);
            case CEILING -> value.setScale(scale, RoundingMode.CEILING);
            case FLOOR -> value.setScale(scale, RoundingMode.FLOOR);
        };
    }

    /**
     * Result of arithmetic operation with size error flag
     */
    public record Result(BigDecimal value, boolean sizeError) {
        public boolean hasError() {
            return sizeError;
        }

        public BigDecimal getValue() {
            return value;
        }
    }

    /**
     * Result of DIVIDE operation, including optional remainder.
     */
    public record DivideResult(BigDecimal value, BigDecimal remainder, boolean sizeError) {
        public boolean hasError() {
            return sizeError;
        }

        public BigDecimal getValue() {
            return value;
        }

        public BigDecimal getRemainder() {
            return remainder;
        }
    }

    /**
     * COBOL DIVIDE with remainder support.
     */
    public static DivideResult divideWithRemainder(BigDecimal a, BigDecimal b,
                                                    int targetScale, int targetPrecision,
                                                    RoundingMode roundingMode) {
        if (b.compareTo(BigDecimal.ZERO) == 0) {
            return new DivideResult(BigDecimal.ZERO, BigDecimal.ZERO, true);
        }
        BigDecimal quotient = a.divide(b, targetScale + 2, roundingMode);
        BigDecimal truncatedQuotient = quotient.setScale(0, RoundingMode.DOWN);
        BigDecimal remainder = a.subtract(truncatedQuotient.multiply(b));
        Result qResult = checkAndScale(quotient, targetScale, targetPrecision);
        return new DivideResult(qResult.value(), remainder, qResult.sizeError());
    }

    /**
     * Context-aware DIVIDE with remainder.
     */
    public static DivideResult divideWithRemainder(BigDecimal a, BigDecimal b,
                                                    ArithmeticContext ctx) {
        if (b.compareTo(BigDecimal.ZERO) == 0) {
            return new DivideResult(BigDecimal.ZERO, BigDecimal.ZERO, true);
        }
        java.math.RoundingMode javaMode = ctx.rounded()
                ? toJavaRoundingMode(ctx.roundMode())
                : java.math.RoundingMode.HALF_UP;
        BigDecimal quotient = a.divide(b, ctx.targetScale() + 2, javaMode);
        BigDecimal truncatedQuotient = quotient.setScale(0, RoundingMode.DOWN);
        BigDecimal remainder = a.subtract(truncatedQuotient.multiply(b));
        Result qResult = applyContext(quotient, ctx);
        return new DivideResult(qResult.value(), remainder, qResult.sizeError());
    }

    /**
     * COBOL rounding modes
     */
    public enum RoundMode {
        TRUNCATION,     // No rounding, just truncate
        HALF_UP,        // COBOL default
        HALF_DOWN,
        HALF_EVEN,      // Banker's rounding
        UP,             // Away from zero
        DOWN,           // Toward zero
        CEILING,        // Toward positive infinity
        FLOOR           // Toward negative infinity
    }

    // ─── Context-aware overloads ─────────────────────────

    /**
     * Context-aware ADD: applies ROUNDED and ON SIZE ERROR from ArithmeticContext.
     */
    public static Result add(BigDecimal a, BigDecimal b, ArithmeticContext ctx) {
        BigDecimal sum = a.add(b);
        return applyContext(sum, ctx);
    }

    /**
     * Context-aware SUBTRACT.
     */
    public static Result subtract(BigDecimal a, BigDecimal b, ArithmeticContext ctx) {
        BigDecimal diff = a.subtract(b);
        return applyContext(diff, ctx);
    }

    /**
     * Context-aware MULTIPLY.
     */
    public static Result multiply(BigDecimal a, BigDecimal b, ArithmeticContext ctx) {
        BigDecimal product = a.multiply(b);
        return applyContext(product, ctx);
    }

    /**
     * Context-aware DIVIDE.
     */
    public static Result divide(BigDecimal a, BigDecimal b, ArithmeticContext ctx) {
        if (b.compareTo(BigDecimal.ZERO) == 0) {
            return new Result(BigDecimal.ZERO, true);
        }
        java.math.RoundingMode javaMode = ctx.rounded()
                ? toJavaRoundingMode(ctx.roundMode())
                : java.math.RoundingMode.HALF_UP;
        BigDecimal quotient = a.divide(b, ctx.targetScale() + 2, javaMode);
        return applyContext(quotient, ctx);
    }

    /**
     * Context-aware COMPUTE.
     */
    public static Result compute(BigDecimal value, ArithmeticContext ctx) {
        return applyContext(value, ctx);
    }

    /**
     * Apply ArithmeticContext: round if ROUNDED, then check size.
     */
    private static Result applyContext(BigDecimal value, ArithmeticContext ctx) {
        if (ctx.rounded()) {
            value = round(value, ctx.targetScale(), ctx.roundMode());
        }
        return checkAndScale(value, ctx.targetScale(), ctx.targetPrecision());
    }

    private static java.math.RoundingMode toJavaRoundingMode(RoundMode mode) {
        return switch (mode) {
            case TRUNCATION, DOWN -> java.math.RoundingMode.DOWN;
            case HALF_UP -> java.math.RoundingMode.HALF_UP;
            case HALF_DOWN -> java.math.RoundingMode.HALF_DOWN;
            case HALF_EVEN -> java.math.RoundingMode.HALF_EVEN;
            case UP -> java.math.RoundingMode.UP;
            case CEILING -> java.math.RoundingMode.CEILING;
            case FLOOR -> java.math.RoundingMode.FLOOR;
        };
    }

    /**
     * Numeric comparison with COBOL semantics
     */
    public static int compare(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareTo(b);
    }

    /**
     * Check if value is numeric (NUMERIC test)
     */
    public static boolean isNumeric(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        try {
            new BigDecimal(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check if value is positive
     */
    public static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Check if value is negative
     */
    public static boolean isNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Check if value is zero
     */
    public static boolean isZero(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) == 0;
    }
}
