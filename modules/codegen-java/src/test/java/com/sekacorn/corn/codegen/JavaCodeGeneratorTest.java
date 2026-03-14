package com.sekacorn.corn.codegen;

import com.sekacorn.corn.ir.*;
import com.sekacorn.corn.ir.expr.*;
import com.sekacorn.corn.ir.stmt.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JavaCodeGeneratorTest {

    private static final SourceMetadata METADATA = new SourceMetadata(
            "TEST.cbl", Instant.now(), "1.0.0", SourceMetadata.CobolDialect.ANSI_85, null);

    private static Program buildHelloWorldProgram() {
        // DISPLAY "HELLO, WORLD!"  +  STOP RUN
        List<Statement> stmts = List.of(
                new DisplayStatement(List.of(Literal.string("HELLO, WORLD!", null)), null, null),
                new StopStatement(StopStatement.StopType.RUN, null, null)
        );
        Paragraph mainPara = new Paragraph("MAIN-PARA", stmts, null);
        ProcedureDivision proc = new ProcedureDivision(null, null, null, List.of(mainPara));
        DataDivision data = new DataDivision(null, Collections.emptyList(), null, null);
        return new Program("HELLO", null, null, data, proc, METADATA);
    }

    private static Program buildProgramWithData() {
        DataItem wsName = new DataItem(1, "WS-NAME",
                new Picture("X(20)", Picture.PictureCategory.ALPHANUMERIC, 20, 0, false, false, null),
                null, null, "WORLD", null, null, null, null, false, false, false, false);
        DataItem wsNum = new DataItem(1, "WS-NUM",
                new Picture("9(5)", Picture.PictureCategory.NUMERIC, 5, 0, false, false, null),
                null, null, null, null, null, null, null, false, false, false, false);

        List<Statement> stmts = List.of(
                new DisplayStatement(
                        List.of(Literal.string("HELLO, ", null), VariableRef.simple("WS-NAME", null)),
                        null, null),
                new StopStatement(StopStatement.StopType.RUN, null, null)
        );
        Paragraph mainPara = new Paragraph("MAIN-PARA", stmts, null);
        ProcedureDivision proc = new ProcedureDivision(null, null, null, List.of(mainPara));
        DataDivision data = new DataDivision(null, List.of(wsName, wsNum), null, null);
        return new Program("DATAPROG", null, null, data, proc, METADATA);
    }

    private static Program buildArithmeticProgram() {
        DataItem wsA = new DataItem(1, "WS-A",
                new Picture("9(5)", Picture.PictureCategory.NUMERIC, 5, 0, false, false, null),
                null, null, "10", null, null, null, null, false, false, false, false);
        DataItem wsB = new DataItem(1, "WS-B",
                new Picture("9(5)", Picture.PictureCategory.NUMERIC, 5, 0, false, false, null),
                null, null, "20", null, null, null, null, false, false, false, false);
        DataItem wsR = new DataItem(1, "WS-R",
                new Picture("9(5)", Picture.PictureCategory.NUMERIC, 5, 0, false, false, null),
                null, null, null, null, null, null, null, false, false, false, false);

        List<Statement> stmts = List.of(
                new AddStatement(
                        List.of(VariableRef.simple("WS-A", null)),
                        List.of(VariableRef.simple("WS-B", null)),
                        Collections.emptyList(), false, null, null, null),
                new DisplayStatement(List.of(VariableRef.simple("WS-B", null)), null, null),
                new StopStatement(StopStatement.StopType.RUN, null, null)
        );
        Paragraph mainPara = new Paragraph("MAIN-PARA", stmts, null);
        ProcedureDivision proc = new ProcedureDivision(null, null, null, List.of(mainPara));
        DataDivision data = new DataDivision(null, List.of(wsA, wsB, wsR), null, null);
        return new Program("ARITHMETIC", null, null, data, proc, METADATA);
    }

    private static Program buildMultiParagraphProgram() {
        List<Statement> mainStmts = List.of(
                new DisplayStatement(List.of(Literal.string("MAIN", null)), null, null),
                new PerformStatement(PerformStatement.PerformType.SIMPLE,
                        "SUB-PARA", null, null, null, null, null, null, null),
                new StopStatement(StopStatement.StopType.RUN, null, null)
        );
        List<Statement> subStmts = List.of(
                new DisplayStatement(List.of(Literal.string("SUB", null)), null, null)
        );
        Paragraph mainPara = new Paragraph("MAIN-PARA", mainStmts, null);
        Paragraph subPara = new Paragraph("SUB-PARA", subStmts, null);

        ProcedureDivision proc = new ProcedureDivision(null, null, null, List.of(mainPara, subPara));
        DataDivision data = new DataDivision(null, Collections.emptyList(), null, null);
        return new Program("PARAS", null, null, data, proc, METADATA);
    }

    private static Program buildFileSectionProgram() {
        DataItem stuId = new DataItem(5, "STU-ID",
                new Picture("9(5)", Picture.PictureCategory.NUMERIC, 5, 0, false, false, null),
                null, null, null, null, null, null, null, false, false, false, false);
        DataItem stuName = new DataItem(5, "STU-NAME",
                new Picture("X(20)", Picture.PictureCategory.ALPHANUMERIC, 20, 0, false, false, null),
                null, null, null, null, null, null, null, false, false, false, false);
        DataItem stuGrade = new DataItem(5, "STU-GRADE",
                new Picture("9(3)", Picture.PictureCategory.NUMERIC, 3, 0, false, false, null),
                null, null, null, null, null, null, null, false, false, false, false);
        DataItem studentRecord = new DataItem(1, "STUDENT-RECORD",
                null, null, null, null, null, null,
                List.of(stuId, stuName, stuGrade), null, false, false, false, false);
        DataDivision.FileSection fileSection = new DataDivision.FileSection("STUDENT-FILE", List.of(studentRecord));
        Paragraph mainPara = new Paragraph("MAIN-PARA",
                List.of(new DisplayStatement(List.of(
                        VariableRef.simple("STU-ID", null),
                        VariableRef.simple("STU-NAME", null),
                        VariableRef.simple("STU-GRADE", null)), null, null)),
                null);
        ProcedureDivision proc = new ProcedureDivision(null, null, null, List.of(mainPara));
        DataDivision data = new DataDivision(List.of(fileSection), Collections.emptyList(), null, null);
        return new Program("FILEPROG", null, null, data, proc, METADATA);
    }

    @Test
    void generatesHelloWorld() {
        JavaCodeGenerator gen = new JavaCodeGenerator();
        GeneratedClass result = gen.generate(buildHelloWorldProgram());

        assertNotNull(result);
        assertEquals("Hello", result.getClassName());
        assertEquals("com.generated.cobol", result.getPackageName());

        String source = result.render();
        assertTrue(source.contains("package com.generated.cobol;"));
        assertTrue(source.contains("public class Hello"));
        assertTrue(source.contains("System.out.println(\"HELLO, WORLD!\")"));
        assertTrue(source.contains("public void run()"));
        assertTrue(source.contains("mainPara();"));
        assertTrue(source.contains("private void mainPara()"));
        assertTrue(source.contains("public static void main(String[] args)"));
        assertTrue(source.contains("new Hello().run()"));
    }

    @Test
    void generatesFieldsFromWorkingStorage() {
        JavaCodeGenerator gen = new JavaCodeGenerator();
        GeneratedClass result = gen.generate(buildProgramWithData());

        String source = result.render();
        assertTrue(source.contains("private String wsName"));
        assertTrue(source.contains("\"WORLD\""));
        assertTrue(source.contains("private BigDecimal wsNum"));
    }

    @Test
    void generatesArithmeticCode() {
        JavaCodeGenerator gen = new JavaCodeGenerator();
        GeneratedClass result = gen.generate(buildArithmeticProgram());

        String source = result.render();
        assertTrue(source.contains("CobolMath.add"));
        assertTrue(source.contains("import com.sekacorn.corn.runtime.CobolMath;"));
    }

    @Test
    void generatesMultipleParagraphMethods() {
        JavaCodeGenerator gen = new JavaCodeGenerator();
        GeneratedClass result = gen.generate(buildMultiParagraphProgram());

        String source = result.render();
        assertTrue(source.contains("private void mainPara()"));
        assertTrue(source.contains("private void subPara()"));
        assertTrue(source.contains("subPara();"), "run or mainPara should call subPara");
    }

    @Test
    void customPackage() {
        JavaCodeGenerator gen = new JavaCodeGenerator().withPackage("com.example.output");
        GeneratedClass result = gen.generate(buildHelloWorldProgram());

        assertEquals("com.example.output", result.getPackageName());
        assertTrue(result.render().contains("package com.example.output;"));
    }

    @Test
    void generatedClassRelativePath() {
        JavaCodeGenerator gen = new JavaCodeGenerator();
        GeneratedClass result = gen.generate(buildHelloWorldProgram());

        assertEquals("com/generated/cobol/Hello.java", result.getRelativePath());
        assertEquals("Hello.java", result.getFileName());
    }

    @Test
    void headerCommentContainsProgramId() {
        JavaCodeGenerator gen = new JavaCodeGenerator();
        GeneratedClass result = gen.generate(buildHelloWorldProgram());

        String source = result.render();
        assertTrue(source.contains("Generated from COBOL program: HELLO"));
    }

    @Test
    void programWithIfElse() {
        DataItem wsA = new DataItem(1, "WS-A",
                new Picture("9(3)", Picture.PictureCategory.NUMERIC, 3, 0, false, false, null),
                null, null, null, null, null, null, null, false, false, false, false);

        List<Statement> stmts = List.of(
                new IfStatement(
                        new BinaryOp(VariableRef.simple("WS-A", null),
                                BinaryOp.Operator.GREATER_THAN,
                                Literal.numeric(5, null), null),
                        List.of(new DisplayStatement(List.of(Literal.string("BIG", null)), null, null)),
                        List.of(new DisplayStatement(List.of(Literal.string("SMALL", null)), null, null)),
                        null),
                new StopStatement(StopStatement.StopType.RUN, null, null)
        );

        Paragraph mainPara = new Paragraph("MAIN-PARA", stmts, null);
        ProcedureDivision proc = new ProcedureDivision(null, null, null, List.of(mainPara));
        DataDivision data = new DataDivision(null, List.of(wsA), null, null);
        Program program = new Program("IFTEST", null, null, data, proc, METADATA);

        JavaCodeGenerator gen = new JavaCodeGenerator();
        GeneratedClass result = gen.generate(program);

        String source = result.render();
        assertTrue(source.contains("if ("));
        assertTrue(source.contains("} else {"));
        assertTrue(source.contains("System.out.println(\"BIG\")"));
        assertTrue(source.contains("System.out.println(\"SMALL\")"));
    }

    @Test
    void nullProgramThrows() {
        JavaCodeGenerator gen = new JavaCodeGenerator();
        assertThrows(NullPointerException.class, () -> gen.generate(null));
    }

    @Test
    void generatesChildFieldsForFileSectionGroupRecords() {
        JavaCodeGenerator gen = new JavaCodeGenerator();
        GeneratedClass result = gen.generate(buildFileSectionProgram());

        String source = result.render();
        assertTrue(source.contains("private String studentRecord = \"\";"));
        assertTrue(source.contains("private BigDecimal stuId"));
        assertTrue(source.contains("private String stuName"));
        assertTrue(source.contains("private BigDecimal stuGrade"));
    }
}
