/*
 * ParseErrorListener - Collects ANTLR parse errors
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.parser;

import com.sekacorn.corn.ir.SourceLocation;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;
import java.util.List;

/**
 * ANTLR error listener that collects parse errors as {@link ParseError} instances.
 */
public class ParseErrorListener extends BaseErrorListener {

    private final String fileName;
    private final List<ParseError> errors = new ArrayList<>();

    public ParseErrorListener(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine, String msg,
                            RecognitionException e) {
        var location = SourceLocation.of(fileName, line, charPositionInLine);
        errors.add(new ParseError(msg, location, ParseError.Severity.ERROR));
    }

    public List<ParseError> getErrors() {
        return List.copyOf(errors);
    }
}
