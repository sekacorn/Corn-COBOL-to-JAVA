package com.sekacorn.corn.parser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CobolPreprocessorTest {

    @Test
    void stripsSequenceNumbersAndExtracts8to72() {
        // Line:  "000100 IDENTIFICATION DIVISION."
        // cols:   123456 7 8.......................72
        String input = "000100 IDENTIFICATION DIVISION.";
        CobolPreprocessor pp = new CobolPreprocessor();
        String result = pp.process(input);
        assertEquals("IDENTIFICATION DIVISION.", result);
    }

    @Test
    void skipsCommentLines() {
        String input = "000100*THIS IS A COMMENT\n000200 IDENTIFICATION DIVISION.";
        CobolPreprocessor pp = new CobolPreprocessor();
        String result = pp.process(input);
        assertEquals("IDENTIFICATION DIVISION.", result);
    }

    @Test
    void skipsDebugLines() {
        String input = "000100D    DEBUG-LINE\n000200 IDENTIFICATION DIVISION.";
        CobolPreprocessor pp = new CobolPreprocessor();
        String result = pp.process(input);
        assertEquals("IDENTIFICATION DIVISION.", result);
    }

    @Test
    void handlesContinuationLines() {
        // Line 1 has text, line 2 has continuation indicator '-'
        String line1 = "000100     DISPLAY \"HELLO";
        String line2 = "000200-    WORLD\".";
        String input = line1 + "\n" + line2;
        CobolPreprocessor pp = new CobolPreprocessor();
        String result = pp.process(input);
        // Continuation strips leading spaces from line 2 code area and appends
        assertTrue(result.contains("DISPLAY"));
        assertTrue(result.contains("WORLD"));
    }

    @Test
    void lineMapTracksOriginalLineNumbers() {
        String input = "000100*COMMENT\n000200 IDENTIFICATION DIVISION.\n000300 PROGRAM-ID. HELLO.";
        CobolPreprocessor pp = new CobolPreprocessor();
        pp.process(input);
        // Comment skipped, so output line 1 = original line 2
        assertEquals(2, pp.getOriginalLine(1));
        assertEquals(3, pp.getOriginalLine(2));
    }

    @Test
    void emptyInputReturnsEmpty() {
        CobolPreprocessor pp = new CobolPreprocessor();
        assertEquals("", pp.process(""));
        assertEquals("", pp.process(null));
    }

    @Test
    void truncatesAfterColumn72() {
        // Build a line > 80 chars with sequence numbers and id area
        String seq = "000100";
        String indicator = " ";
        String code = "A".repeat(65); // columns 8-72 = 65 chars
        String idArea = "COMMENT!"; // columns 73-80, should be stripped
        String input = seq + indicator + code + idArea;
        CobolPreprocessor pp = new CobolPreprocessor();
        String result = pp.process(input);
        assertEquals(code, result);
    }

    @Test
    void handlesMultipleLinesWithMixedContent() {
        String input = String.join("\n",
            "000100 IDENTIFICATION DIVISION.",
            "000200*THIS IS A COMMENT",
            "000300 PROGRAM-ID. HELLO.",
            "000400D    DEBUG LINE",
            "000500 PROCEDURE DIVISION."
        );
        CobolPreprocessor pp = new CobolPreprocessor();
        String result = pp.process(input);
        String[] lines = result.split("\n");
        assertEquals(3, lines.length);
        assertEquals("IDENTIFICATION DIVISION.", lines[0]);
        assertEquals("PROGRAM-ID. HELLO.", lines[1]);
        assertEquals("PROCEDURE DIVISION.", lines[2]);
    }
}
