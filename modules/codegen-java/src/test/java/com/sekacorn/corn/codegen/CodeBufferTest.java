package com.sekacorn.corn.codegen;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CodeBufferTest {

    @Test
    void lineAppendsWithNewline() {
        CodeBuffer buf = new CodeBuffer();
        buf.line("int x = 0;");
        assertEquals("int x = 0;\n", buf.getContent());
    }

    @Test
    void lineFormatsWithArgs() {
        CodeBuffer buf = new CodeBuffer();
        buf.line("private %s %s = %s;", "int", "count", "0");
        assertEquals("private int count = 0;\n", buf.getContent());
    }

    @Test
    void emptyLineAppendsBlank() {
        CodeBuffer buf = new CodeBuffer();
        buf.line("a");
        buf.emptyLine();
        buf.line("b");
        assertEquals("a\n\nb\n", buf.getContent());
    }

    @Test
    void indentIncreasesLevel() {
        CodeBuffer buf = new CodeBuffer();
        buf.line("class Foo {");
        buf.indent();
        buf.line("int x;");
        buf.dedent();
        buf.line("}");
        assertEquals("class Foo {\n    int x;\n}\n", buf.getContent());
    }

    @Test
    void openBlockAndCloseBlock() {
        CodeBuffer buf = new CodeBuffer();
        buf.openBlock("if (true)");
        buf.line("doSomething();");
        buf.closeBlock();
        assertEquals("if (true) {\n    doSomething();\n}\n", buf.getContent());
    }

    @Test
    void closeBlockWithSuffix() {
        CodeBuffer buf = new CodeBuffer();
        buf.openBlock("if (a)");
        buf.line("x();");
        buf.closeBlockWith("else {");
        buf.indent();
        buf.line("y();");
        buf.closeBlock();
        assertEquals("if (a) {\n    x();\n} else {\n    y();\n}\n", buf.getContent());
    }

    @Test
    void importTracking() {
        CodeBuffer buf = new CodeBuffer();
        buf.addImport("java.math.BigDecimal");
        buf.addImport("java.util.List");
        buf.addImport("java.math.BigDecimal"); // duplicate

        var imports = buf.getImports();
        assertEquals(2, imports.size());
        assertTrue(imports.contains("java.math.BigDecimal"));
        assertTrue(imports.contains("java.util.List"));
    }

    @Test
    void rawLineSkipsIndent() {
        CodeBuffer buf = new CodeBuffer(2);
        buf.rawLine("no indent");
        buf.line("with indent");
        assertTrue(buf.getContent().startsWith("no indent\n"));
        assertTrue(buf.getContent().contains("        with indent\n"));
    }

    @Test
    void dedentDoesNotGoBelowZero() {
        CodeBuffer buf = new CodeBuffer();
        buf.dedent();
        buf.dedent();
        assertEquals(0, buf.getIndentLevel());
        buf.line("test");
        assertEquals("test\n", buf.getContent());
    }

    @Test
    void chainingWorks() {
        CodeBuffer buf = new CodeBuffer();
        buf.line("a").line("b").emptyLine().line("c");
        assertEquals("a\nb\n\nc\n", buf.getContent());
    }
}
