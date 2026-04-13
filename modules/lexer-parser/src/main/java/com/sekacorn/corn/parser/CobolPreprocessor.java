/*
 * CobolPreprocessor - Fixed-format COBOL source preprocessor
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Preprocesses fixed-format COBOL source (columns 1-6 sequence, 7 indicator,
 * 8-72 code area, 73-80 identification area).
 * <p>
 * Strips sequence numbers and identification area, handles continuation lines,
 * skips comment and debug lines, and maintains a line-number map for
 * SourceLocation tracking.
 */
public class CobolPreprocessor {

    private final Map<Integer, Integer> lineMap = new HashMap<>();

    /**
     * Preprocess fixed-format COBOL source text.
     *
     * @param source raw COBOL source text
     * @return preprocessed text suitable for ANTLR lexer
     */
    public String process(String source) {
        if (source == null || source.isEmpty()) {
            return "";
        }
        lineMap.clear();

        String[] rawLines = source.split("\\r?\\n", -1);
        List<String> outputLines = new ArrayList<>();
        StringBuilder continuationBuffer = null;
        int continuationStartLine = -1;

        for (int i = 0; i < rawLines.length; i++) {
            String raw = rawLines[i];
            int originalLine = i + 1;

            // Extract indicator (column 7, 0-indexed position 6)
            char indicator = raw.length() > 6 ? raw.charAt(6) : ' ';

            // Skip comment lines
            if (indicator == '*' || indicator == '/') {
                continue;
            }

            // Skip debug lines
            if (indicator == 'D' || indicator == 'd') {
                continue;
            }

            // Skip conditional compilation lines (NIST CCVS85 X-card markers)
            // These are implementation-specific lines that should be treated
            // as comments when not activated by the test harness.
            if (Character.isLetter(indicator) && indicator != ' '
                    && indicator != '-') {
                continue;
            }

            // Extract code area (columns 8-72)
            String codeArea = extractCodeArea(raw);

            // Handle continuation lines (indicator '-')
            if (indicator == '-') {
                if (continuationBuffer != null) {
                    String trimmed = codeArea.stripLeading();
                    // COBOL string literal continuation: there are two cases.
                    //
                    // Case 1 (closed-open splice): The previous line ends
                    // with a closing quote (e.g. VALUE "FOO") and the
                    // continuation starts with an opening quote ("BAR").
                    // We remove the trailing quote from the previous buffer
                    // and the leading quote from the continuation.
                    //
                    // Case 2 (mid-literal): The previous line ends mid-string
                    // (the string runs into column 72 without a closing quote)
                    // and the continuation starts with a quote. We just remove
                    // the opening quote on the continuation to stitch them.
                    if (!trimmed.isEmpty() && isQuoteChar(trimmed.charAt(0))) {
                        String bufStr = continuationBuffer.toString();
                        // Find last non-space char in buffer
                        int lastNonSpace = bufStr.length() - 1;
                        while (lastNonSpace >= 0 && bufStr.charAt(lastNonSpace) == ' ') {
                            lastNonSpace--;
                        }
                        if (lastNonSpace >= 0 && isQuoteChar(bufStr.charAt(lastNonSpace))) {
                            // Case 1: previous line ended with a quote — remove it
                            continuationBuffer.setLength(lastNonSpace);
                        }
                        // In both cases, skip the opening quote on continuation
                        trimmed = trimmed.substring(1);
                    }
                    continuationBuffer.append(trimmed);
                }
                continue;
            }

            // Flush previous continuation buffer
            if (continuationBuffer != null) {
                int outputLine = outputLines.size() + 1;
                lineMap.put(outputLine, continuationStartLine);
                outputLines.add(continuationBuffer.toString());
                continuationBuffer = null;
            }

            // Start a new line (or continuation buffer)
            if (codeArea.isBlank()) {
                continue;
            }

            // Check if the NEXT line is a continuation
            boolean nextIsContinuation = false;
            if (i + 1 < rawLines.length) {
                String nextRaw = rawLines[i + 1];
                char nextIndicator = nextRaw.length() > 6 ? nextRaw.charAt(6) : ' ';
                if (nextIndicator == '-') {
                    nextIsContinuation = true;
                }
            }

            if (nextIsContinuation) {
                continuationBuffer = new StringBuilder(codeArea);
                continuationStartLine = originalLine;
            } else {
                int outputLine = outputLines.size() + 1;
                lineMap.put(outputLine, originalLine);
                outputLines.add(codeArea);
            }
        }

        // Flush any remaining continuation buffer
        if (continuationBuffer != null) {
            int outputLine = outputLines.size() + 1;
            lineMap.put(outputLine, continuationStartLine);
            outputLines.add(continuationBuffer.toString());
        }

        return String.join("\n", outputLines);
    }

    private boolean isQuoteChar(char c) {
        return c == '"' || c == '\'';
    }

    /**
     * Extract the code area (columns 8-72) from a raw line.
     */
    private String extractCodeArea(String raw) {
        if (raw.length() <= 7) {
            return "";
        }
        int end = Math.min(raw.length(), 72);
        return raw.substring(7, end);
    }

    /**
     * Map a preprocessed line number back to the original source line.
     */
    public int getOriginalLine(int preprocessedLine) {
        return lineMap.getOrDefault(preprocessedLine, preprocessedLine);
    }

    /**
     * Get the full line-number map (preprocessed → original).
     */
    public Map<Integer, Integer> getLineMap() {
        return Map.copyOf(lineMap);
    }
}
