/*
 * Picture - COBOL PICTURE clause runtime support
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.runtime;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Handles COBOL PICTURE formatting and numeric conversions.
 * Implements COBOL numeric editing rules for financial applications.
 */
public final class Picture {

    /**
     * Format a numeric value according to COBOL edited picture rules.
     *
     * @param value The numeric value to format
     * @param pattern The COBOL picture pattern (e.g., "$$$,$$9.99", "ZZZ9")
     * @return Formatted string according to COBOL rules
     */
    public static String formatNumeric(BigDecimal value, String pattern) {
        if (value == null) {
            return formatZero(pattern);
        }

        PictureParser parser = new PictureParser(pattern);
        return parser.format(value);
    }

    /**
     * Parse a string into a BigDecimal according to COBOL picture rules.
     *
     * @param input The input string
     * @param pattern The COBOL picture pattern
     * @return BigDecimal value
     */
    public static BigDecimal parseNumeric(String input, String pattern) {
        if (input == null || input.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Remove editing characters
        String cleaned = input.replaceAll("[^0-9.-]", "");
        if (cleaned.isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Format zero value according to pattern (shows suppression).
     * Z's become spaces, 9's become 0's, editing chars preserved.
     */
    private static String formatZero(String pattern) {
        StringBuilder sb = new StringBuilder(pattern.length());
        for (char c : pattern.toUpperCase().toCharArray()) {
            switch (c) {
                case 'Z' -> sb.append(' ');
                case '9' -> sb.append('0');
                case '$' -> sb.append(' ');
                case '-', '+' -> sb.append(' ');
                case '.', 'V' -> sb.append('.');
                case ',' -> sb.append(' ');
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Move with COBOL truncation rules
     *
     * @param source Source value
     * @param targetScale Target decimal places
     * @param targetPrecision Target total digits
     * @param roundingMode Rounding mode (COBOL default is HALF-UP)
     * @return Truncated/rounded value
     */
    public static BigDecimal move(BigDecimal source, int targetScale,
                                   int targetPrecision, RoundingMode roundingMode) {
        if (source == null) {
            return BigDecimal.ZERO;
        }

        // Scale to target decimal places
        BigDecimal scaled = source.setScale(targetScale, roundingMode);

        // Check for overflow (precision too large)
        int sourcePrecision = scaled.precision();
        if (sourcePrecision > targetPrecision && targetPrecision > 0) {
            // COBOL behavior: truncate high-order digits (data loss!)
            // In production, this should trigger a size error condition
            String str = scaled.toPlainString();
            boolean negative = str.startsWith("-");
            if (negative) str = str.substring(1);
            int decimalPos = str.indexOf('.');
            if (decimalPos == -1) {
                // Integer: keep rightmost digits
                int keep = Math.min(targetPrecision, str.length());
                str = str.substring(str.length() - keep);
            } else {
                // Keep decimal places and truncate integer part
                int intDigits = Math.max(0, targetPrecision - targetScale);
                int startPos = Math.max(0, decimalPos - intDigits);
                if (startPos < str.length()) {
                    str = str.substring(startPos);
                }
            }
            scaled = new BigDecimal(str);
            if (negative) scaled = scaled.negate();
        }

        return scaled;
    }

    /**
     * Format a numeric value with BLANK WHEN ZERO support.
     * When blankWhenZero is true and value is zero, returns all spaces.
     */
    public static String formatNumeric(BigDecimal value, String pattern, boolean blankWhenZero) {
        if (blankWhenZero && (value == null || value.compareTo(BigDecimal.ZERO) == 0)) {
            return " ".repeat(pattern.length());
        }
        return formatNumeric(value, pattern);
    }

    /**
     * Internal parser for COBOL picture patterns
     */
    private static class PictureParser {
        private final String pattern;
        private final boolean hasDollar;
        private final boolean hasComma;
        private final boolean hasMinus;
        private final boolean hasPlus;
        private final int integerDigits;
        private final int decimalDigits;
        private final boolean zeroSuppression;

        PictureParser(String pattern) {
            this.pattern = pattern.toUpperCase();
            this.hasDollar = pattern.contains("$");
            this.hasComma = pattern.contains(",");
            this.hasMinus = pattern.contains("-");
            this.hasPlus = pattern.contains("+");
            this.zeroSuppression = pattern.contains("Z");

            // Count digits
            int intCount = 0;
            int decCount = 0;
            boolean afterDecimal = false;

            for (char c : this.pattern.toCharArray()) {
                if (c == 'V' || c == '.') {
                    afterDecimal = true;
                } else if (c == '9' || c == 'Z' || c == '$' || c == '-' || c == '+') {
                    if (afterDecimal) {
                        decCount++;
                    } else {
                        intCount++;
                    }
                }
            }

            this.integerDigits = intCount;
            this.decimalDigits = decCount;
        }

        String format(BigDecimal value) {
            // Scale to match decimal places
            BigDecimal scaled = value.setScale(decimalDigits, RoundingMode.HALF_UP);
            boolean isNegative = scaled.compareTo(BigDecimal.ZERO) < 0;
            BigDecimal abs = scaled.abs();

            // Format the number
            String formatted = abs.toPlainString();

            // Apply COBOL editing rules
            if (zeroSuppression && abs.compareTo(BigDecimal.ZERO) == 0) {
                // Zero suppression: replace leading zeros with spaces
                formatted = " ".repeat(pattern.length());
            }

            // Add currency symbol
            if (hasDollar) {
                formatted = "$" + formatted;
            }

            // Add sign
            if (isNegative && hasMinus) {
                formatted = formatted + "-";
            } else if (hasPlus) {
                formatted = formatted + (isNegative ? "-" : "+");
            }

            // Pad to pattern length
            while (formatted.length() < pattern.length()) {
                formatted = " " + formatted;
            }

            return formatted;
        }
    }

    /**
     * COBOL USAGE types conversion
     */
    public enum Usage {
        DISPLAY,        // Standard ASCII/EBCDIC
        COMP,           // Binary
        COMP_3,         // Packed decimal
        COMP_4,         // Binary
        COMP_5          // Native binary
    }

    /**
     * Convert between USAGE types
     */
    public static byte[] convertUsage(BigDecimal value, Usage fromUsage, Usage toUsage) {
        // Implementation of COBOL USAGE conversions
        // This is a simplified version - production would handle all edge cases
        return value.toPlainString().getBytes();
    }
}
