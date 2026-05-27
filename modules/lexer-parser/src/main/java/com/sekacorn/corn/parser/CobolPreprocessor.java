/*
 * CobolPreprocessor - Fixed-format COBOL source preprocessor
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.parser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Preprocesses fixed-format COBOL source (columns 1-6 sequence, 7 indicator,
 * 8-72 code area, 73-80 identification area).
 * <p>
 * Strips sequence numbers and identification area, handles continuation lines,
 * skips comment and debug lines, processes COPY directives (with REPLACING),
 * processes REPLACE/REPLACE OFF directives, and maintains a line-number map
 * for SourceLocation tracking.
 */
public class CobolPreprocessor {

    private final Map<Integer, Integer> lineMap = new HashMap<>();
    private static final int MAX_COPY_DEPTH = 10;

    /** Search directories for copybook resolution. */
    private final List<Path> copyDirs = new ArrayList<>();

    /**
     * Preprocess fixed-format COBOL source text (no copybook support).
     */
    public String process(String source) {
        return process(source, (Path) null);
    }

    /**
     * Preprocess fixed-format COBOL source text with copybook resolution.
     */
    public String process(String source, Path sourceFile) {
        if (source == null || source.isEmpty()) {
            return "";
        }
        lineMap.clear();
        copyDirs.clear();

        // Build copybook search directories
        if (sourceFile != null) {
            Path parent = sourceFile.toAbsolutePath().getParent();
            if (parent != null) {
                copyDirs.add(parent);
                Path copyDir = parent.resolve("COPY");
                if (Files.isDirectory(copyDir)) {
                    copyDirs.add(copyDir);
                }
                Path parentCopyDir = parent.getParent() != null
                        ? parent.getParent().resolve("COPY") : null;
                if (parentCopyDir != null && Files.isDirectory(parentCopyDir)) {
                    copyDirs.add(parentCopyDir);
                }
            }
        }

        // Phase 1: Process COPY directives on raw fixed-format lines
        String[] rawLines = source.split("\\r?\\n", -1);
        List<String> expandedLines = processCopyDirectives(rawLines, 0);

        // Phase 2: Fixed-format processing (indicators, code area, continuations)
        List<String> outputLines = processFixedFormat(expandedLines);

        // Phase 3: Process REPLACE directives on the code-area text
        outputLines = processReplaceDirectives(outputLines);

        return String.join("\n", outputLines);
    }

    // ─── Phase 1: COPY directive processing ─────────────────────────

    // Matches COPY followed by whitespace and an identifier (copybook name)
    private static final Pattern COPY_DIRECTIVE = Pattern.compile(
            "(?i)(?<![A-Za-z0-9-])COPY\\s+[A-Za-z][A-Za-z0-9-]*");

    private List<String> processCopyDirectives(String[] lines, int depth) {
        if (depth > MAX_COPY_DEPTH) {
            return List.of(lines);
        }

        List<String> result = new ArrayList<>();
        int i = 0;

        while (i < lines.length) {
            String raw = lines[i];
            char indicator = raw.length() > 6 ? raw.charAt(6) : ' ';

            // Skip comment lines
            if (indicator == '*' || indicator == '/') {
                result.add(raw);
                i++;
                continue;
            }

            // Pre-processed lines pass through as-is
            if (raw.startsWith(PREPROCESSED_MARKER)) {
                result.add(raw);
                i++;
                continue;
            }

            String codeArea = extractCodeArea(raw);

            // Find COPY directive, avoiding matches inside string literals
            int copyPos = findCopyDirective(codeArea);
            if (copyPos >= 0) {
                // Content before COPY stays as a regular line
                String beforeCopy = codeArea.substring(0, copyPos).trim();
                String copyPart = codeArea.substring(copyPos).trim();

                if (!beforeCopy.isEmpty()) {
                    String prefix = raw.substring(0, Math.min(7, raw.length()));
                    result.add(buildFixedFormatLine(prefix, beforeCopy));
                }

                // Collect the full COPY statement (may span multiple lines)
                StringBuilder copyStmt = new StringBuilder(copyPart);
                int startLine = i;

                while (!copyStmt.toString().trim().endsWith(".") && i + 1 < lines.length) {
                    i++;
                    String nextRaw = lines[i];
                    char nextInd = nextRaw.length() > 6 ? nextRaw.charAt(6) : ' ';
                    if (nextInd == '*' || nextInd == '/') continue;
                    String nextCode = extractCodeArea(nextRaw);
                    if (nextInd == '-') {
                        // Continuation — no space, content splices directly
                        copyStmt.append(nextCode.stripLeading());
                    } else {
                        copyStmt.append(' ').append(nextCode.trim());
                    }
                }

                List<String> expanded = expandCopy(copyStmt.toString(), depth);
                if (expanded != null) {
                    result.addAll(expanded);
                } else {
                    for (int j = startLine; j <= i; j++) {
                        result.add(lines[j]);
                    }
                }
            } else {
                result.add(raw);
            }
            i++;
        }
        return result;
    }

    /**
     * Find COPY directive position in code area, skipping occurrences inside strings.
     */
    private int findCopyDirective(String codeArea) {
        Matcher m = COPY_DIRECTIVE.matcher(codeArea);
        while (m.find()) {
            if (!isInsideString(codeArea, m.start())) {
                return m.start();
            }
        }
        return -1;
    }

    /**
     * Check if a position is inside a string literal by counting quotes before it.
     */
    private boolean isInsideString(String text, int pos) {
        boolean inSingle = false;
        boolean inDouble = false;
        for (int i = 0; i < pos && i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"' && !inSingle) {
                inDouble = !inDouble;
            } else if (c == '\'' && !inDouble) {
                inSingle = !inSingle;
            }
        }
        return inSingle || inDouble;
    }

    private String buildFixedFormatLine(String prefix, String code) {
        StringBuilder sb = new StringBuilder(80);
        sb.append(prefix);
        while (sb.length() < 7) sb.append(' ');
        sb.append(code);
        return sb.toString();
    }

    // Pattern: COPY libname [OF|IN libname] [REPLACING ...] .
    private static final Pattern COPY_PATTERN = Pattern.compile(
            "(?i)COPY\\s+([A-Za-z][A-Za-z0-9-]*)(?:\\s+(?:OF|IN)\\s+[A-Za-z][A-Za-z0-9-]*)?\\s*(REPLACING\\s+.+)?\\.",
            Pattern.DOTALL);

    private List<String> expandCopy(String copyStmt, int depth) {
        Matcher m = COPY_PATTERN.matcher(copyStmt.trim());
        if (!m.matches()) {
            return null;
        }

        String libName = m.group(1);
        String replacingClause = m.group(2);

        Path copyFile = resolveCopybook(libName);
        if (copyFile == null) {
            return null;
        }

        String content;
        try {
            content = Files.readString(copyFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }

        String[] copyLines = content.split("\\r?\\n", -1);

        if (replacingClause != null) {
            copyLines = applyReplacing(copyLines, replacingClause);
        }

        return processCopyDirectives(copyLines, depth + 1);
    }

    private Path resolveCopybook(String name) {
        String[] extensions = {".CPY", ".cpy", ".CBL", ".cbl", ""};
        for (Path dir : copyDirs) {
            for (String ext : extensions) {
                Path candidate = dir.resolve(name + ext);
                if (Files.isRegularFile(candidate)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * Apply REPLACING to copybook lines. Processes through fixed-format first
     * to join continuations, then applies replacements on the token stream,
     * and wraps results back into fixed-format lines.
     */
    private String[] applyReplacing(String[] lines, String replacingClause) {
        List<ReplacePair> pairs = parseReplacingPairs(replacingClause);
        if (pairs.isEmpty()) {
            return lines;
        }

        // Process through fixed-format to join continuations and extract code areas
        List<String> codeLines = processFixedFormatSimple(lines);

        // Join all code lines into a single text for replacement
        String combined = String.join(" ", codeLines);

        // Apply replacements
        for (ReplacePair pair : pairs) {
            combined = replaceWholeToken(combined, pair.oldText, pair.newText);
        }

        // Wrap result back into fixed-format lines that fit in cols 8-72 (65 chars)
        List<String> resultLines = new ArrayList<>();
        wrapAsFixedFormat(combined, resultLines);
        return resultLines.toArray(new String[0]);
    }

    /**
     * Simplified fixed-format processing that just extracts code areas and joins
     * continuations, without updating lineMap. Used for REPLACING processing.
     */
    private List<String> processFixedFormatSimple(String[] lines) {
        List<String> outputLines = new ArrayList<>();
        StringBuilder continuationBuffer = null;

        for (int i = 0; i < lines.length; i++) {
            String raw = lines[i];
            char indicator = raw.length() > 6 ? raw.charAt(6) : ' ';

            if (indicator == '*' || indicator == '/') continue;
            if (indicator == 'D' || indicator == 'd') continue;

            String codeArea = extractCodeArea(raw);

            if (indicator == '-') {
                if (continuationBuffer != null) {
                    String trimmed = codeArea.stripLeading();
                    if (!trimmed.isEmpty() && isQuoteChar(trimmed.charAt(0))) {
                        // String continuation: preserve buffer content, strip only
                        // the leading quote (continuation marker) from this line
                        trimmed = trimmed.substring(1);
                    }
                    // Non-string continuation: append directly (no space)
                    // COBOL continuation splices exactly where col 72 cut off
                    continuationBuffer.append(trimmed);
                }
                continue;
            }

            if (continuationBuffer != null) {
                outputLines.add(continuationBuffer.toString());
                continuationBuffer = null;
            }

            if (codeArea.isBlank()) continue;

            boolean nextIsCont = false;
            if (i + 1 < lines.length) {
                char nextInd = lines[i + 1].length() > 6 ? lines[i + 1].charAt(6) : ' ';
                if (nextInd == '-') nextIsCont = true;
            }

            if (nextIsCont) {
                continuationBuffer = new StringBuilder(codeArea);
            } else {
                outputLines.add(codeArea);
            }
        }
        if (continuationBuffer != null) {
            outputLines.add(continuationBuffer.toString());
        }
        return outputLines;
    }

    /**
     * Wrap a text string into fixed-format COBOL lines (65-char code area).
     * Splits at word boundaries when possible.
     */
    /** Prefix marker for pre-processed lines that should bypass Phase 2. */
    private static final String PREPROCESSED_MARKER = " PREPROCESSED ";

    /**
     * Emit replaced text as pre-processed lines (one per statement).
     * These lines carry a marker prefix so Phase 2 passes them through as-is,
     * avoiding column 72 truncation.
     */
    private void wrapAsFixedFormat(String text, List<String> out) {
        // Split by statement-ending periods
        int pos = 0;
        while (pos < text.length()) {
            int periodPos = findStatementEnd(text, pos);
            String stmt;
            if (periodPos >= 0) {
                stmt = text.substring(pos, periodPos + 1).trim();
                pos = periodPos + 1;
                while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) pos++;
            } else {
                stmt = text.substring(pos).trim();
                pos = text.length();
            }
            if (!stmt.isEmpty()) {
                out.add(PREPROCESSED_MARKER + stmt);
            }
        }
    }

    /**
     * Find the end of a statement (period not inside quotes).
     */
    private int findStatementEnd(String text, int from) {
        boolean inQ = false;
        for (int i = from; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"' || c == '\'') inQ = !inQ;
            if (c == '.' && !inQ) {
                // Decimal point in numeric literal: period preceded by digit and
                // followed by digit — not a statement terminator
                if (i + 1 < text.length() && Character.isDigit(text.charAt(i + 1))
                        && i > from && Character.isDigit(text.charAt(i - 1))) {
                    continue;
                }
                return i;
            }
        }
        return -1;
    }

    /**
     * Whole-token case-insensitive replacement with word boundaries.
     */
    private String replaceWholeToken(String text, String oldText, String newText) {
        if (oldText.isEmpty()) return text;

        // COBOL separators: commas and semicolons are treated as whitespace
        String[] tokens = oldText.trim().split("[\\s,;]+");
        // Filter out empty tokens from leading/trailing separators
        List<String> tokenList = new ArrayList<>();
        for (String t : tokens) {
            if (!t.isEmpty()) tokenList.add(t);
        }
        if (tokenList.isEmpty()) return text;

        StringBuilder patternStr = new StringBuilder();

        if (isWordChar(tokenList.get(0).charAt(0))) {
            patternStr.append("\\b");
        }

        for (int i = 0; i < tokenList.size(); i++) {
            if (i > 0) patternStr.append("[\\s,;]+");
            patternStr.append(Pattern.quote(tokenList.get(i)));
        }

        String lastTok = tokenList.get(tokenList.size() - 1);
        if (isWordChar(lastTok.charAt(lastTok.length() - 1))) {
            patternStr.append("\\b");
        }

        Pattern p = Pattern.compile(patternStr.toString(), Pattern.CASE_INSENSITIVE);
        return p.matcher(text).replaceAll(Matcher.quoteReplacement(newText));
    }

    private boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '-' || c == '_';
    }

    // ─── Phase 2: Fixed-format processing ─────────────────────────

    private static final Pattern ID_COMMENT_PARA = Pattern.compile(
            "(?i)^\\s*(AUTHOR|DATE-WRITTEN|DATE-COMPILED|INSTALLATION|SECURITY|REMARKS)\\s*\\.");
    private static final Pattern ID_COMMENT_END = Pattern.compile(
            "(?i)^\\s{0,3}(PROGRAM-ID\\s*\\.|((ENVIRONMENT|DATA|PROCEDURE|IDENTIFICATION|ID)\\s+DIVISION))");

    private List<String> processFixedFormat(List<String> rawLines) {
        List<String> outputLines = new ArrayList<>();
        StringBuilder continuationBuffer = null;
        int continuationStartLine = -1;
        boolean inIdCommentParagraph = false;

        for (int i = 0; i < rawLines.size(); i++) {
            String raw = rawLines.get(i);
            int originalLine = i + 1;

            // Pre-processed lines from COPY REPLACING bypass fixed-format processing
            if (raw.startsWith(PREPROCESSED_MARKER)) {
                if (continuationBuffer != null) {
                    int outputLine = outputLines.size() + 1;
                    lineMap.put(outputLine, continuationStartLine);
                    outputLines.add(continuationBuffer.toString());
                    continuationBuffer = null;
                }
                String code = raw.substring(PREPROCESSED_MARKER.length());
                if (!code.isBlank()) {
                    int outputLine = outputLines.size() + 1;
                    lineMap.put(outputLine, originalLine);
                    outputLines.add(code);
                }
                continue;
            }

            char indicator = raw.length() > 6 ? raw.charAt(6) : ' ';

            if (indicator == '*' || indicator == '/') continue;
            if (indicator == 'D' || indicator == 'd') continue;

            if (Character.isLetter(indicator) && indicator != ' '
                    && indicator != '-'
                    && Character.toUpperCase(indicator) != 'A') {
                continue;
            }

            String codeArea = extractCodeArea(raw);

            // Track ID DIVISION comment paragraphs to sanitize stray quotes
            if (indicator != '-') {
                if (ID_COMMENT_END.matcher(codeArea).find()) {
                    inIdCommentParagraph = false;
                } else if (ID_COMMENT_PARA.matcher(codeArea).find()) {
                    inIdCommentParagraph = true;
                }
            }
            if (inIdCommentParagraph) {
                codeArea = codeArea.replace('"', ' ').replace('\'', ' ');
            }

            if (indicator == '-') {
                if (continuationBuffer != null) {
                    String trimmed = codeArea.stripLeading();
                    if (!trimmed.isEmpty() && isQuoteChar(trimmed.charAt(0))) {
                        // String continuation: per COBOL standard, all content in the
                        // buffer up to column 72 is preserved as-is (including trailing
                        // spaces and embedded quote pairs ""). Only strip the leading
                        // quote from the continuation line — it's the continuation marker.
                        trimmed = trimmed.substring(1);
                    } else if (continuationBuffer.length() > 0
                            && continuationBuffer.charAt(continuationBuffer.length() - 1) != ' ') {
                        continuationBuffer.append(' ');
                    }
                    continuationBuffer.append(trimmed);
                }
                continue;
            }

            if (continuationBuffer != null) {
                int outputLine = outputLines.size() + 1;
                lineMap.put(outputLine, continuationStartLine);
                outputLines.add(continuationBuffer.toString());
                continuationBuffer = null;
            }

            if (codeArea.isBlank()) continue;

            boolean nextIsContinuation = false;
            if (i + 1 < rawLines.size()) {
                String nextRaw = rawLines.get(i + 1);
                char nextIndicator = nextRaw.length() > 6 ? nextRaw.charAt(6) : ' ';
                if (nextIndicator == '-') nextIsContinuation = true;
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

        if (continuationBuffer != null) {
            int outputLine = outputLines.size() + 1;
            lineMap.put(outputLine, continuationStartLine);
            outputLines.add(continuationBuffer.toString());
        }

        return outputLines;
    }

    // ─── Phase 3: REPLACE directive processing ──────────────────────

    private List<String> processReplaceDirectives(List<String> lines) {
        boolean hasReplace = false;
        for (String line : lines) {
            String t = line.trim().toUpperCase();
            if (t.startsWith("REPLACE ")) {
                hasReplace = true;
                break;
            }
        }
        if (!hasReplace) return lines;

        List<String> result = new ArrayList<>();
        List<ReplacePair> activePairs = new ArrayList<>();
        List<String> pendingLines = new ArrayList<>();
        int i = 0;

        while (i < lines.size()) {
            String line = lines.get(i);
            String trimmed = line.trim();
            String upper = trimmed.toUpperCase();

            if (upper.matches("REPLACE\\s+OFF\\s*\\..*")) {
                if (!activePairs.isEmpty() && !pendingLines.isEmpty()) {
                    result.addAll(applyReplacePairs(pendingLines, activePairs));
                    pendingLines.clear();
                }
                activePairs.clear();
                i++;
                continue;
            }

            if (upper.startsWith("REPLACE ") && !upper.matches("REPLACE\\s+OFF.*")) {
                if (!activePairs.isEmpty() && !pendingLines.isEmpty()) {
                    result.addAll(applyReplacePairs(pendingLines, activePairs));
                    pendingLines.clear();
                }

                StringBuilder replaceStmt = new StringBuilder(trimmed);
                while (!endsWithPeriodOutsidePseudoText(replaceStmt.toString().trim()) && i + 1 < lines.size()) {
                    i++;
                    replaceStmt.append(' ').append(lines.get(i).trim());
                }

                String stmt = replaceStmt.toString();
                String body = stmt.substring(7).trim();
                if (body.endsWith(".")) body = body.substring(0, body.length() - 1);

                activePairs = parseReplacingPairs("REPLACING " + body);
                i++;
                continue;
            }

            if (!activePairs.isEmpty()) {
                pendingLines.add(line);
            } else {
                result.add(line);
            }
            i++;
        }

        if (!activePairs.isEmpty() && !pendingLines.isEmpty()) {
            result.addAll(applyReplacePairs(pendingLines, activePairs));
        }

        return result;
    }

    /**
     * Apply replacement pairs to a block of lines by joining them, applying
     * replacements on the combined text (allowing multi-line matches), and
     * splitting back into lines.
     */
    private List<String> applyReplacePairs(List<String> lines, List<ReplacePair> pairs) {
        String combined = String.join("\n", lines);
        for (ReplacePair pair : pairs) {
            combined = replaceWholeToken(combined, pair.oldText, pair.newText);
        }
        return List.of(combined.split("\n", -1));
    }

    /**
     * Check if text ends with a period that is outside all == pseudo-text delimiters.
     * Periods inside ==...== are part of pseudo-text content, not statement terminators.
     */
    private boolean endsWithPeriodOutsidePseudoText(String text) {
        if (!text.endsWith(".")) return false;
        // Count == delimiters before the final period; if even, the period is outside
        boolean insidePseudo = false;
        for (int i = 0; i < text.length() - 1; i++) {
            if (i + 1 < text.length() && text.charAt(i) == '=' && text.charAt(i + 1) == '=') {
                insidePseudo = !insidePseudo;
                i++; // skip second '='
            }
        }
        return !insidePseudo;
    }

    // ─── Pseudo-text parsing ────────────────────────────────────────

    private record ReplacePair(String oldText, String newText) {}

    /**
     * Parse replacement pairs from a REPLACING clause.
     * Supports: ==pseudo-text== BY ==pseudo-text==, ==pseudo-text== BY word, word BY word
     */
    private List<ReplacePair> parseReplacingPairs(String clause) {
        List<ReplacePair> pairs = new ArrayList<>();
        if (clause == null) return pairs;

        String body = clause.trim();
        if (body.toUpperCase().startsWith("REPLACING")) {
            body = body.substring(9).trim();
        }

        int pos = 0;
        while (pos < body.length()) {
            pos = skipWhitespace(body, pos);
            if (pos >= body.length()) break;

            ParsedOperand oldOp = parseOperand(body, pos);
            if (oldOp == null) break;
            pos = oldOp.endPos;

            pos = skipWhitespace(body, pos);
            if (pos >= body.length()) break;
            String rest = body.substring(pos).toUpperCase();
            if (rest.startsWith("BY")) {
                pos += 2;
            } else {
                break;
            }

            pos = skipWhitespace(body, pos);
            if (pos >= body.length()) {
                pairs.add(new ReplacePair(oldOp.text, ""));
                break;
            }
            ParsedOperand newOp = parseOperand(body, pos);
            if (newOp == null) {
                pairs.add(new ReplacePair(oldOp.text, ""));
                break;
            }
            pos = newOp.endPos;

            pairs.add(new ReplacePair(oldOp.text, newOp.text));
        }
        return pairs;
    }

    private record ParsedOperand(String text, int endPos) {}

    private ParsedOperand parseOperand(String body, int pos) {
        if (pos >= body.length()) return null;

        // Pseudo-text delimited by ==
        if (pos + 1 < body.length() && body.charAt(pos) == '=' && body.charAt(pos + 1) == '=') {
            int start = pos + 2;
            int end = body.indexOf("==", start);
            if (end < 0) return null;
            String text = body.substring(start, end).trim();
            return new ParsedOperand(text, end + 2);
        }

        // Non-delimited operand: handle string literals, then bare words
        // with optional IN/OF qualifiers or (subscript).
        int start = pos;

        // Handle string literals (quoted values like "TRUE " or 'ABC')
        if (body.charAt(pos) == '"' || body.charAt(pos) == '\'') {
            char quote = body.charAt(pos);
            pos++; // skip opening quote
            while (pos < body.length()) {
                if (body.charAt(pos) == quote) {
                    pos++; // skip the quote
                    // Check for doubled quote (embedded quote)
                    if (pos < body.length() && body.charAt(pos) == quote) {
                        pos++; // skip the second quote of the pair
                    } else {
                        break; // closing quote found
                    }
                } else {
                    pos++;
                }
            }
            String text = body.substring(start, pos).trim();
            if (text.isEmpty()) return null;
            return new ParsedOperand(text, pos);
        }

        // Read the base word
        while (pos < body.length() && !Character.isWhitespace(body.charAt(pos))
                && body.charAt(pos) != '(' && body.charAt(pos) != ')') {
            pos++;
        }

        // Check for qualification (IN/OF) and subscripts (...)
        boolean keepGoing = true;
        while (keepGoing && pos < body.length()) {
            int next = skipWhitespace(body, pos);
            if (next >= body.length()) break;

            String ahead = body.substring(next).toUpperCase();
            if (ahead.startsWith("IN ") || ahead.startsWith("IN\t")
                    || ahead.startsWith("OF ") || ahead.startsWith("OF\t")) {
                // IN/OF qualifier — skip the keyword and read the next word
                pos = next + 2; // skip IN/OF
                pos = skipWhitespace(body, pos);
                while (pos < body.length() && !Character.isWhitespace(body.charAt(pos))
                        && body.charAt(pos) != '(' && body.charAt(pos) != ')') {
                    pos++;
                }
            } else if (body.charAt(next) == '(') {
                // Subscript — read until matching close paren
                pos = next + 1;
                int depth = 1;
                while (pos < body.length() && depth > 0) {
                    if (body.charAt(pos) == '(') depth++;
                    else if (body.charAt(pos) == ')') depth--;
                    pos++;
                }
            } else {
                keepGoing = false;
            }
        }

        String text = body.substring(start, pos).trim();
        if (text.isEmpty()) return null;
        return new ParsedOperand(text, pos);
    }

    private int skipWhitespace(String s, int pos) {
        while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
        return pos;
    }

    // ─── Utility methods ────────────────────────────────────────────

    private boolean isQuoteChar(char c) {
        return c == '"' || c == '\'';
    }

    private String extractCodeArea(String raw) {
        if (raw.length() <= 7) return "";
        int end = Math.min(raw.length(), 72);
        return raw.substring(7, end);
    }

    public int getOriginalLine(int preprocessedLine) {
        return lineMap.getOrDefault(preprocessedLine, preprocessedLine);
    }

    public Map<Integer, Integer> getLineMap() {
        return Map.copyOf(lineMap);
    }
}
