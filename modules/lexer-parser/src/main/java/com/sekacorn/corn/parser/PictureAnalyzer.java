/*
 * PictureAnalyzer - Parses COBOL PIC strings into Picture IR objects
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.parser;

import com.sekacorn.corn.ir.Picture;

/**
 * Analyzes raw COBOL PICTURE strings to determine category, length,
 * scale (decimal places), sign, and edit symbol presence.
 */
public final class PictureAnalyzer {

    private PictureAnalyzer() {}

    /**
     * Analyze a raw PIC string and return a Picture object.
     *
     * @param raw the raw PICTURE clause string (e.g. "S9(5)V99", "X(20)", "ZZ,ZZ9.99")
     * @return a Picture IR object
     */
    public static Picture analyze(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("PIC string cannot be null or blank");
        }

        String trimmed = raw.trim();
        String expanded = expand(trimmed);

        boolean signed = expanded.contains("S") || expanded.contains("s");
        String withoutSign = expanded.replace("S", "").replace("s", "");

        int scale = computeScale(withoutSign);
        int length = computeLength(withoutSign);
        Picture.PictureCategory category = determineCategory(withoutSign);
        boolean hasEditSymbols = hasEditSymbols(withoutSign);
        String editPattern = hasEditSymbols ? withoutSign : null;

        return new Picture(trimmed, category, length, scale, signed, hasEditSymbols, editPattern);
    }

    /**
     * Expand parenthesized repetition counts: 9(5) → 99999, X(10) → XXXXXXXXXX
     */
    static String expand(String pic) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < pic.length()) {
            char c = pic.charAt(i);
            if (i + 1 < pic.length() && pic.charAt(i + 1) == '(') {
                int closeIdx = pic.indexOf(')', i + 2);
                if (closeIdx > 0) {
                    int count = Integer.parseInt(pic.substring(i + 2, closeIdx).trim());
                    result.append(String.valueOf(c).repeat(count));
                    i = closeIdx + 1;
                    continue;
                }
            }
            result.append(c);
            i++;
        }
        return result.toString();
    }

    /**
     * Compute decimal scale (digits after implied decimal point V).
     */
    private static int computeScale(String expanded) {
        int vPos = expanded.indexOf('V');
        if (vPos < 0) vPos = expanded.indexOf('v');
        if (vPos < 0) return 0;

        int scale = 0;
        for (int i = vPos + 1; i < expanded.length(); i++) {
            char c = Character.toUpperCase(expanded.charAt(i));
            if (c == '9' || c == 'Z' || c == '*') {
                scale++;
            }
        }
        return scale;
    }

    /**
     * Compute display length (character positions, excluding S and V).
     */
    private static int computeLength(String expanded) {
        int len = 0;
        for (char c : expanded.toCharArray()) {
            char uc = Character.toUpperCase(c);
            if (uc != 'S' && uc != 'V' && uc != 'P') {
                len++;
            }
        }
        return len;
    }

    /**
     * Determine the PICTURE category.
     */
    private static Picture.PictureCategory determineCategory(String expanded) {
        String upper = expanded.toUpperCase();
        boolean has9 = upper.contains("9");
        boolean hasX = upper.contains("X");
        boolean hasA = upper.contains("A");
        boolean hasV = upper.contains("V");
        boolean hasP = upper.contains("P");
        boolean hasN = upper.contains("N");
        boolean has1 = upper.contains("1");
        boolean hasEdit = hasEditSymbols(upper);

        if (hasN) return Picture.PictureCategory.NATIONAL;
        if (has1 && !has9 && !hasX && !hasA) return Picture.PictureCategory.BOOLEAN;

        if (hasEdit && (has9 || hasV || hasP)) {
            return Picture.PictureCategory.NUMERIC_EDITED;
        }
        if (hasX && hasEdit) {
            return Picture.PictureCategory.ALPHANUMERIC_EDITED;
        }
        if (has9 || hasV || hasP) {
            return Picture.PictureCategory.NUMERIC;
        }
        if (hasX) {
            return Picture.PictureCategory.ALPHANUMERIC;
        }
        if (hasA) {
            return Picture.PictureCategory.ALPHABETIC;
        }

        return Picture.PictureCategory.ALPHANUMERIC;
    }

    /**
     * Check if the expanded PIC contains edit symbols.
     */
    private static boolean hasEditSymbols(String expanded) {
        for (char c : expanded.toUpperCase().toCharArray()) {
            if (c == 'Z' || c == '*' || c == '$' || c == ',' || c == '.'
                    || c == '+' || c == '-' || c == 'B' || c == '0' || c == '/') {
                return true;
            }
        }
        // Check for CR/DB
        String upper = expanded.toUpperCase();
        return upper.contains("CR") || upper.contains("DB");
    }
}
