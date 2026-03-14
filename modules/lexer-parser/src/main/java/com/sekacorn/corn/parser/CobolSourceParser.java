/*
 * CobolSourceParser - Public API for parsing COBOL source files
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.parser;

import com.sekacorn.corn.ir.Program;
import com.sekacorn.corn.ir.SourceMetadata;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Public facade for parsing COBOL source files into Corn IR.
 * <p>
 * Usage:
 * <pre>{@code
 * ParseResult result = CobolSourceParser.parse(path, CobolDialect.ANSI_85);
 * if (!result.hasErrors()) {
 *     Program program = result.program();
 * }
 * }</pre>
 */
public final class CobolSourceParser {

    private CobolSourceParser() {}

    /**
     * Parse a COBOL source file and return the Program IR.
     *
     * @param source  path to the COBOL source file
     * @param dialect the COBOL dialect to use
     * @return ParseResult containing the Program and any errors
     * @throws IOException if the file cannot be read
     */
    public static ParseResult parse(Path source, SourceMetadata.CobolDialect dialect) throws IOException {
        String rawSource = Files.readString(source, StandardCharsets.UTF_8);
        String fileName = source.getFileName().toString();
        return parseString(rawSource, fileName, dialect);
    }

    /**
     * Parse COBOL source from a string (for testing).
     *
     * @param source   the COBOL source text
     * @param fileName name to use in error messages and SourceLocation
     * @param dialect  the COBOL dialect to use
     * @return ParseResult containing the Program and any errors
     */
    public static ParseResult parseString(String source, String fileName,
                                            SourceMetadata.CobolDialect dialect) {
        List<ParseError> errors = new ArrayList<>();

        String preprocessed;
        if (looksLikeFixedFormat(source)) {
            CobolPreprocessor preprocessor = new CobolPreprocessor();
            preprocessed = preprocessor.process(source);
        } else {
            preprocessed = source;
        }

        // Lex
        CobolLexer lexer = new CobolLexer(CharStreams.fromString(preprocessed));
        ParseErrorListener errorListener = new ParseErrorListener(fileName);
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        // Parse
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CobolParser parser = new CobolParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        CobolParser.CompilationUnitContext tree = parser.compilationUnit();
        errors.addAll(errorListener.getErrors());

        // Build IR
        CobolIRBuildingVisitor visitor = new CobolIRBuildingVisitor(fileName, dialect);
        Program program = null;
        try {
            program = visitor.visitProgram(tree.program());
        } catch (Exception e) {
            String msg = e.getMessage() != null
                    ? e.getMessage()
                    : e.getClass().getSimpleName();
            errors.add(new ParseError(
                    "IR build error: " + msg,
                    null,
                    ParseError.Severity.ERROR));
        }

        return new ParseResult(program, List.copyOf(errors));
    }

    private static boolean looksLikeFixedFormat(String source) {
        int inspected = 0;
        int fixedLike = 0;

        int start = 0;
        int length = source.length();
        while (start < length && inspected < 30) {
            int end = source.indexOf('\n', start);
            if (end < 0) {
                end = length;
            }
            String line = source.substring(start, end);
            if (!line.isBlank()) {
                inspected++;
                if (line.length() >= 7) {
                    String seq = line.substring(0, 6);
                    char indicator = line.charAt(6);
                    boolean seqLooksFixed = seq.chars().allMatch(ch -> Character.isDigit(ch) || ch == ' ');
                    boolean indicatorLooksFixed = indicator == ' ' || indicator == '*' || indicator == '/' || indicator == '-'
                            || indicator == 'D' || indicator == 'd';
                    if (seqLooksFixed && indicatorLooksFixed) {
                        fixedLike++;
                    }
                }
            }
            start = end + 1;
        }

        if (inspected == 0) {
            return true;
        }
        return fixedLike * 2 >= inspected;
    }
}
