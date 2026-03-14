/*
 * CobolString - COBOL string operation semantics
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.runtime;

/**
 * Implements COBOL string semantics (INSPECT, STRING, UNSTRING, etc.)
 * Critical for accurate legacy code translation.
 */
public final class CobolString {

    private CobolString() {
        throw new AssertionError("No instances");
    }

    /**
     * COBOL MOVE semantics for alphanumeric fields.
     * Handles padding and truncation according to COBOL rules.
     *
     * @param source Source string
     * @param targetLength Target field length
     * @param justify Justification (LEFT or RIGHT)
     * @return Properly formatted string
     */
    public static String move(String source, int targetLength, Justification justify) {
        if (source == null) {
            source = "";
        }

        if (source.length() == targetLength) {
            return source;
        }

        if (source.length() > targetLength) {
            // Truncate according to justification
            if (justify == Justification.RIGHT) {
                return source.substring(source.length() - targetLength);
            } else {
                return source.substring(0, targetLength);
            }
        }

        // Pad with spaces
        int padCount = targetLength - source.length();
        String padding = " ".repeat(padCount);

        if (justify == Justification.RIGHT) {
            return padding + source;
        } else {
            return source + padding;
        }
    }

    /**
     * Context-aware MOVE using MoveContext.
     * Handles BLANK WHEN ZERO, JUSTIFIED, and sign positioning.
     */
    public static String move(String source, MoveContext ctx) {
        if (source == null) {
            source = "";
        }

        // BLANK WHEN ZERO: if source is all zeros, return spaces
        if (ctx.blankWhenZero() && isAllZeros(source)) {
            return " ".repeat(ctx.targetLength());
        }

        // Apply sign positioning if present
        if (ctx.sign() != null) {
            boolean negative = source.startsWith("-");
            String digits = source.replaceAll("[^0-9.]", "");
            String signChar = negative ? "-" : "+";

            // Pad digits first, then place sign at the correct position
            Justification justify = ctx.justified() ? Justification.RIGHT : Justification.LEFT;
            String padded = move(digits, ctx.targetLength() - 1, justify);

            return switch (ctx.sign()) {
                case LEADING -> signChar + padded;
                case TRAILING -> padded + signChar;
                case LEADING_SEPARATE -> signChar + " " + move(digits, ctx.targetLength() - 2, justify);
                case TRAILING_SEPARATE -> move(digits, ctx.targetLength() - 2, justify) + " " + signChar;
            };
        }

        // Delegate to existing move with justification derived from context
        Justification justify = ctx.justified() ? Justification.RIGHT : Justification.LEFT;
        return move(source, ctx.targetLength(), justify);
    }

    private static boolean isAllZeros(String s) {
        if (s == null || s.isEmpty()) {
            return true;
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != '0' && c != '.' && c != ' ' && c != '+' && c != '-') {
                return false;
            }
        }
        return true;
    }

    private static String applySignPosition(String value, MoveContext.SignPosition sign) {
        boolean negative = value.startsWith("-");
        String digits = value.replaceAll("[^0-9.]", "");
        String signChar = negative ? "-" : "+";

        return switch (sign) {
            case LEADING -> signChar + digits;
            case TRAILING -> digits + signChar;
            case LEADING_SEPARATE -> signChar + " " + digits;
            case TRAILING_SEPARATE -> digits + " " + signChar;
        };
    }

    /**
     * INSPECT TALLYING - count occurrences
     *
     * @param subject String to inspect
     * @param target Character or string to count
     * @return Number of occurrences
     */
    public static int inspectTallying(String subject, String target) {
        return inspectTallying(subject, target, false, false, null, null);
    }

    public static int inspectTallying(String subject, String target,
                                      boolean leading, boolean characters,
                                      String beforeDelim, String afterDelim) {
        if (subject == null) {
            return 0;
        }
        String range = inspectRange(subject, beforeDelim, afterDelim);
        if (characters) {
            return range.length();
        }
        if (subject == null || target == null || target.isEmpty()) {
            return 0;
        }
        if (leading) {
            return countLeadingOccurrences(range, target);
        }

        int count = 0;
        int index = 0;

        while ((index = range.indexOf(target, index)) != -1) {
            count++;
            index += target.length();
        }

        return count;
    }

    /**
     * INSPECT REPLACING - replace all occurrences
     *
     * @param subject String to inspect
     * @param target String to find
     * @param replacement String to replace with
     * @return Modified string
     */
    public static String inspectReplacing(String subject, String target, String replacement) {
        return inspectReplacing(subject, target, replacement, false, false, false, null, null);
    }

    public static String inspectReplacing(String subject, String target, String replacement,
                                          boolean leading, boolean first, boolean characters,
                                          String beforeDelim, String afterDelim) {
        if (subject == null) {
            return "";
        }
        if (!characters && (target == null || target.isEmpty())) {
            return subject;
        }
        String effectiveReplacement = replacement != null ? replacement : "";
        Range range = inspectRangeBounds(subject, beforeDelim, afterDelim);
        String prefix = subject.substring(0, range.start());
        String segment = subject.substring(range.start(), range.end());
        String suffix = subject.substring(range.end());

        String replaced;
        if (characters) {
            replaced = effectiveReplacement.isEmpty()
                    ? segment
                    : effectiveReplacement.substring(0, 1).repeat(segment.length());
        } else if (leading) {
            int count = countLeadingOccurrences(segment, target);
            replaced = effectiveReplacement.repeat(count) + segment.substring(count * target.length());
        } else if (first) {
            int index = segment.indexOf(target);
            replaced = index < 0
                    ? segment
                    : segment.substring(0, index) + effectiveReplacement + segment.substring(index + target.length());
        } else {
            replaced = segment.replace(target, effectiveReplacement);
        }

        return prefix + replaced + suffix;
    }

    /**
     * INSPECT CONVERTING - character-by-character replacement
     *
     * @param subject String to convert
     * @param fromChars Characters to find
     * @param toChars Replacement characters
     * @return Converted string
     */
    public static String inspectConverting(String subject, String fromChars, String toChars) {
        return inspectConverting(subject, fromChars, toChars, null, null);
    }

    public static String inspectConverting(String subject, String fromChars, String toChars,
                                           String beforeDelim, String afterDelim) {
        if (subject == null) {
            return "";
        }
        if (fromChars == null || toChars == null) {
            return subject;
        }

        Range range = inspectRangeBounds(subject, beforeDelim, afterDelim);
        char[] result = subject.toCharArray();
        for (int i = range.start(); i < range.end(); i++) {
            int pos = fromChars.indexOf(result[i]);
            if (pos >= 0 && pos < toChars.length()) {
                result[i] = toChars.charAt(pos);
            }
        }

        return new String(result);
    }

    /**
     * STRING statement - concatenate strings with pointer
     *
     * @param target Target buffer
     * @param pointer Current pointer position (1-based, COBOL style)
     * @param sources Source strings to concatenate
     * @return New pointer position, or -1 if overflow
     */
    public static int stringInto(char[] target, int pointer, String... sources) {
        if (target == null || pointer < 1) {
            return -1;
        }

        int currentPos = pointer - 1;  // Convert to 0-based

        for (String source : sources) {
            if (source == null) continue;

            for (int i = 0; i < source.length(); i++) {
                if (currentPos >= target.length) {
                    return -1;  // Overflow
                }
                target[currentPos++] = source.charAt(i);
            }
        }

        return currentPos + 1;  // Convert back to 1-based
    }

    /**
     * UNSTRING statement - split string by delimiters
     *
     * @param source Source string
     * @param delimiter Delimiter character/string
     * @param maxFields Maximum number of fields to extract
     * @return Array of extracted fields
     */
    public static String[] unstringBy(String source, String delimiter, int maxFields) {
        if (source == null || source.isEmpty()) {
            return new String[0];
        }

        if (delimiter == null || delimiter.isEmpty()) {
            return new String[] { source };
        }

        String[] parts = source.split(java.util.regex.Pattern.quote(delimiter), maxFields);

        // COBOL fills with spaces if not enough fields
        if (parts.length < maxFields) {
            String[] result = new String[maxFields];
            System.arraycopy(parts, 0, result, 0, parts.length);
            for (int i = parts.length; i < maxFields; i++) {
                result[i] = "";
            }
            return result;
        }

        return parts;
    }

    /**
     * Reference modification - substring with COBOL semantics
     *
     * @param source Source string
     * @param startPos Start position (1-based, COBOL style)
     * @param length Length to extract (optional, -1 for rest of string)
     * @return Substring
     */
    public static String referenceModification(String source, int startPos, int length) {
        if (source == null || startPos < 1) {
            return "";
        }

        int start = startPos - 1;  // Convert to 0-based
        if (start >= source.length()) {
            return "";
        }

        if (length < 0) {
            return source.substring(start);
        }

        int end = Math.min(start + length, source.length());
        return source.substring(start, end);
    }

    private static String inspectRange(String subject, String beforeDelim, String afterDelim) {
        Range range = inspectRangeBounds(subject, beforeDelim, afterDelim);
        return subject.substring(range.start(), range.end());
    }

    private static Range inspectRangeBounds(String subject, String beforeDelim, String afterDelim) {
        int start = 0;
        int end = subject.length();

        if (afterDelim != null && !afterDelim.isEmpty()) {
            int index = subject.indexOf(afterDelim);
            if (index >= 0) {
                start = index + afterDelim.length();
            }
        }
        if (beforeDelim != null && !beforeDelim.isEmpty()) {
            int index = subject.indexOf(beforeDelim, start);
            if (index >= 0) {
                end = index;
            }
        }
        if (start > end) {
            start = end;
        }
        return new Range(start, end);
    }

    private static int countLeadingOccurrences(String subject, String target) {
        int count = 0;
        int index = 0;
        while (subject.startsWith(target, index)) {
            count++;
            index += target.length();
        }
        return count;
    }

    /**
     * Justification options for COBOL fields
     */
    public enum Justification {
        LEFT,
        RIGHT
    }

    /**
     * Figurative constants
     */
    public static final class FigurativeConstants {
        public static final String SPACE = " ";
        public static final String SPACES = " ";
        public static final String ZERO = "0";
        public static final String ZEROS = "0";
        public static final String ZEROES = "0";
        public static final String HIGH_VALUE = "\u00FF";
        public static final String HIGH_VALUES = "\u00FF";
        public static final String LOW_VALUE = "\u0000";
        public static final String LOW_VALUES = "\u0000";
        public static final String QUOTE = "\"";
        public static final String QUOTES = "\"";

        private FigurativeConstants() {}

        public static String all(char c, int length) {
            return String.valueOf(c).repeat(length);
        }
    }

    private record Range(int start, int end) {}
}
