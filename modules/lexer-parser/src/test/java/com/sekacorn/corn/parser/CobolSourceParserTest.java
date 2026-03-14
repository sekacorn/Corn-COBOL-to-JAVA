package com.sekacorn.corn.parser;

import com.sekacorn.corn.ir.*;
import com.sekacorn.corn.ir.stmt.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CobolSourceParserTest {

    private static final SourceMetadata.CobolDialect DIALECT = SourceMetadata.CobolDialect.ANSI_85;

    private ParseResult parse(String source) {
        return CobolSourceParser.parseString(source, "TEST.cbl", DIALECT);
    }

    @Test
    void parsesHelloWorld() {
        String source = String.join("\n",
            "000100 IDENTIFICATION DIVISION.",
            "000200 PROGRAM-ID. HELLO.",
            "000300 PROCEDURE DIVISION.",
            "000400     DISPLAY \"HELLO, WORLD!\".",
            "000500     STOP RUN."
        );
        ParseResult result = parse(source);
        assertFalse(result.hasErrors(), () -> "Errors: " + result.errors());
        assertNotNull(result.program());
        assertEquals("HELLO", result.program().getProgramId());
    }

    @Test
    void parsesWorkingStorageData() {
        String source = String.join("\n",
            "000100 IDENTIFICATION DIVISION.",
            "000200 PROGRAM-ID. DATAPROG.",
            "000300 DATA DIVISION.",
            "000400 WORKING-STORAGE SECTION.",
            "000500 01  WS-NAME    PIC X(20).",
            "000600 01  WS-NUM     PIC 9(5).",
            "000700 PROCEDURE DIVISION.",
            "000800     STOP RUN."
        );
        ParseResult result = parse(source);
        assertFalse(result.hasErrors(), () -> "Errors: " + result.errors());
        assertNotNull(result.program().getData());
        var ws = result.program().getData().getWorkingStorage();
        assertEquals(2, ws.size());
        assertEquals("WS-NAME", ws.get(0).getName());
        assertEquals("WS-NUM", ws.get(1).getName());
    }

    @Test
    void parsesArithmeticStatements() {
        String source = String.join("\n",
            "000100 IDENTIFICATION DIVISION.",
            "000200 PROGRAM-ID. ARITH.",
            "000300 DATA DIVISION.",
            "000400 WORKING-STORAGE SECTION.",
            "000500 01  WS-A    PIC 9(5).",
            "000600 01  WS-B    PIC 9(5).",
            "000700 01  WS-R    PIC 9(5).",
            "000800 PROCEDURE DIVISION.",
            "000900     ADD WS-A TO WS-B.",
            "001000     STOP RUN."
        );
        ParseResult result = parse(source);
        assertFalse(result.hasErrors(), () -> "Errors: " + result.errors());
        var paras = result.program().getProcedure().getParagraphs();
        assertFalse(paras.isEmpty(), "Expected at least one paragraph");
        // Find all statements across paragraphs (bare sentences may be split)
        var allStmts = paras.stream()
                .flatMap(p -> p.getStatements().stream())
                .toList();
        assertTrue(allStmts.size() >= 2, "Expected >= 2 statements but got " + allStmts.size());
        assertTrue(allStmts.stream().anyMatch(s -> s instanceof AddStatement), "Expected an ADD statement");
        assertTrue(allStmts.stream().anyMatch(s -> s instanceof StopStatement), "Expected a STOP statement");
    }

    @Test
    void parsesIfElse() {
        String source = String.join("\n",
            "000100 IDENTIFICATION DIVISION.",
            "000200 PROGRAM-ID. IFTEST.",
            "000300 DATA DIVISION.",
            "000400 WORKING-STORAGE SECTION.",
            "000500 01  WS-A    PIC 9(3).",
            "000600 PROCEDURE DIVISION.",
            "000700     IF WS-A > 5",
            "000800         DISPLAY \"BIG\"",
            "000900     ELSE",
            "001000         DISPLAY \"SMALL\"",
            "001100     END-IF.",
            "001200     STOP RUN."
        );
        ParseResult result = parse(source);
        assertFalse(result.hasErrors(), () -> "Errors: " + result.errors());
        var stmts = result.program().getProcedure().getParagraphs().get(0).getStatements();
        assertInstanceOf(IfStatement.class, stmts.get(0));
    }

    @Test
    void parsesMultipleParagraphs() {
        String source = String.join("\n",
            "000100 IDENTIFICATION DIVISION.",
            "000200 PROGRAM-ID. PARAS.",
            "000300 PROCEDURE DIVISION.",
            "000400 MAIN-PARA.",
            "000500     DISPLAY \"MAIN\".",
            "000600     PERFORM SUB-PARA.",
            "000700     STOP RUN.",
            "000800 SUB-PARA.",
            "000900     DISPLAY \"SUB\"."
        );
        ParseResult result = parse(source);
        assertFalse(result.hasErrors(), () -> "Errors: " + result.errors());
        var paras = result.program().getProcedure().getParagraphs();
        assertTrue(paras.size() >= 2);
    }

    @Test
    void parsesFileControlEntries() {
        String source = String.join("\n",
            "000100 IDENTIFICATION DIVISION.",
            "000200 PROGRAM-ID. FILETEST.",
            "000300 ENVIRONMENT DIVISION.",
            "000400 INPUT-OUTPUT SECTION.",
            "000500 FILE-CONTROL.",
            "000600     SELECT MY-FILE ASSIGN TO \"data.dat\"",
            "000700         ORGANIZATION IS SEQUENTIAL.",
            "000800 DATA DIVISION.",
            "000900 FILE SECTION.",
            "001000 FD  MY-FILE.",
            "001100 01  MY-RECORD    PIC X(80).",
            "001200 PROCEDURE DIVISION.",
            "001300     STOP RUN."
        );
        ParseResult result = parse(source);
        assertFalse(result.hasErrors(), () -> "Errors: " + result.errors());
        assertNotNull(result.program().getEnvironment());
    }

    @Test
    void parsesInspectClauses() {
        String source = String.join("\n",
            "000100 IDENTIFICATION DIVISION.",
            "000200 PROGRAM-ID. INSPECTTEST.",
            "000300 DATA DIVISION.",
            "000400 WORKING-STORAGE SECTION.",
            "000500 01  WS-TEXT PIC X(10) VALUE \"AABBAA\".",
            "000600 01  WS-COUNT PIC 9(2) VALUE ZERO.",
            "000700 PROCEDURE DIVISION.",
            "000800     INSPECT WS-TEXT TALLYING WS-COUNT FOR LEADING \"A\" BEFORE INITIAL \"BB\".",
            "000900     STOP RUN."
        );
        ParseResult result = parse(source);
        assertFalse(result.hasErrors(), () -> "Errors: " + result.errors());
        var stmts = result.program().getProcedure().getParagraphs().get(0).getStatements();
        assertInstanceOf(InspectStatement.class, stmts.get(0));
        InspectStatement inspect = (InspectStatement) stmts.get(0);
        assertNotNull(inspect.tallyingClause());
        assertEquals("WS-COUNT", ((com.sekacorn.corn.ir.expr.VariableRef) inspect.tallyingClause().counter()).getName());
        assertEquals(InspectStatement.TallyMode.LEADING, inspect.tallyingClause().forClauses().get(0).mode());
        assertNotNull(inspect.tallyingClause().forClauses().get(0).beforeBoundary());
    }

    @Test
    void parsesHelloResourceFile() throws IOException {
        Path path = Path.of("src/test/resources/cobol/HELLO.cbl");
        if (!Files.exists(path)) return; // skip if not available
        ParseResult result = CobolSourceParser.parse(path, DIALECT);
        assertFalse(result.hasErrors(), () -> "Errors: " + result.errors());
        assertEquals("HELLO", result.program().getProgramId());
    }

    @Test
    void parsesArithmeticResourceFile() throws IOException {
        Path path = Path.of("src/test/resources/cobol/ARITHMETIC.cbl");
        if (!Files.exists(path)) return;
        ParseResult result = CobolSourceParser.parse(path, DIALECT);
        assertFalse(result.hasErrors(), () -> "Errors: " + result.errors());
        assertEquals("ARITHMETIC", result.program().getProgramId());
    }

    @Test
    void parseResultHasErrorsForInvalidSource() {
        String source = "THIS IS NOT VALID COBOL";
        ParseResult result = parse(source);
        assertTrue(result.hasErrors() || result.program() == null);
    }
}
