package com.sekacorn.corn.codegen;

import com.sekacorn.corn.ir.DataItem;
import com.sekacorn.corn.ir.Picture;
import com.sekacorn.corn.ir.expr.*;
import com.sekacorn.corn.ir.stmt.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JavaStatementVisitorTest {

    private JavaStatementVisitor stmtVisitor;
    private CodeBuffer buffer;

    @BeforeEach
    void setUp() {
        buffer = new CodeBuffer();
        Map<String, DataItem> dataItemMap = Map.of(
                "WS-NAME", new DataItem(1, "WS-NAME",
                        new Picture("X(20)", Picture.PictureCategory.ALPHANUMERIC, 20, 0, false, false, null),
                        null, null, null, null, null, null, null, false, false, false, false),
                "WS-NUM", new DataItem(1, "WS-NUM",
                        new Picture("9(5)", Picture.PictureCategory.NUMERIC, 5, 0, false, false, null),
                        null, null, null, null, null, null, null, false, false, false, false),
                "WS-A", new DataItem(1, "WS-A",
                        new Picture("9(5)", Picture.PictureCategory.NUMERIC, 5, 0, false, false, null),
                        null, null, null, null, null, null, null, false, false, false, false),
                "WS-B", new DataItem(1, "WS-B",
                        new Picture("9(5)", Picture.PictureCategory.NUMERIC, 5, 0, false, false, null),
                        null, null, null, null, null, null, null, false, false, false, false)
        );
        JavaExpressionVisitor exprVisitor = new JavaExpressionVisitor(dataItemMap, buffer);
        stmtVisitor = new JavaStatementVisitor(exprVisitor, dataItemMap, Map.of("MY-FILE", "WS-NAME"), buffer);
    }

    @Test
    void displaySingleItem() {
        DisplayStatement stmt = new DisplayStatement(
                List.of(Literal.string("HELLO", null)), null, null);
        stmt.accept(stmtVisitor);
        assertTrue(buffer.getContent().contains("System.out.println(\"HELLO\")"));
    }

    @Test
    void displayMultipleItems() {
        DisplayStatement stmt = new DisplayStatement(
                List.of(Literal.string("A", null), Literal.string("B", null)), null, null);
        stmt.accept(stmtVisitor);
        String output = buffer.getContent();
        assertTrue(output.contains("String.valueOf("));
        assertTrue(output.contains(" + "));
    }

    @Test
    void displayEmpty() {
        DisplayStatement stmt = new DisplayStatement(
                Collections.emptyList(), null, null);
        stmt.accept(stmtVisitor);
        assertTrue(buffer.getContent().contains("System.out.println()"));
    }

    @Test
    void moveToNumericField() {
        MoveStatement stmt = new MoveStatement(
                Literal.numeric(42, null),
                List.of(VariableRef.simple("WS-NUM", null)),
                false, null);
        stmt.accept(stmtVisitor);
        assertTrue(buffer.getContent().contains("wsNum = new BigDecimal(\"42\")"));
    }

    @Test
    void moveToStringFieldUsesCobolString() {
        MoveStatement stmt = new MoveStatement(
                Literal.string("HELLO", null),
                List.of(VariableRef.simple("WS-NAME", null)),
                false, null);
        stmt.accept(stmtVisitor);
        String output = buffer.getContent();
        assertTrue(output.contains("CobolString.move"));
        assertTrue(output.contains("20")); // field length
    }

    @Test
    void addStatement() {
        AddStatement stmt = new AddStatement(
                List.of(Literal.numeric(10, null)),
                List.of(VariableRef.simple("WS-A", null)),
                Collections.emptyList(),
                false, null, null, null);
        stmt.accept(stmtVisitor);
        String output = buffer.getContent();
        assertTrue(output.contains("CobolMath.add"));
    }

    @Test
    void subtractStatement() {
        SubtractStatement stmt = new SubtractStatement(
                List.of(Literal.numeric(5, null)),
                List.of(VariableRef.simple("WS-A", null)),
                Collections.emptyList(),
                false, null, null, null);
        stmt.accept(stmtVisitor);
        assertTrue(buffer.getContent().contains("CobolMath.subtract"));
    }

    @Test
    void multiplyStatement() {
        MultiplyStatement stmt = new MultiplyStatement(
                VariableRef.simple("WS-A", null),
                VariableRef.simple("WS-B", null),
                Collections.emptyList(),
                false, null, null, null);
        stmt.accept(stmtVisitor);
        assertTrue(buffer.getContent().contains("CobolMath.multiply"));
    }

    @Test
    void divideStatement() {
        DivideStatement stmt = new DivideStatement(
                VariableRef.simple("WS-A", null),
                VariableRef.simple("WS-B", null),
                VariableRef.simple("WS-A", null),
                Collections.emptyList(),
                null, false, null, null, null);
        stmt.accept(stmtVisitor);
        assertTrue(buffer.getContent().contains("CobolMath.divide"));
    }

    @Test
    void computeStatement() {
        ComputeStatement stmt = new ComputeStatement(
                List.of(VariableRef.simple("WS-A", null)),
                Literal.numeric(100, null),
                false, null, null, null);
        stmt.accept(stmtVisitor);
        String output = buffer.getContent();
        assertTrue(output.contains("CobolMath.compute"));
    }

    @Test
    void ifStatement() {
        IfStatement stmt = new IfStatement(
                new BinaryOp(VariableRef.simple("WS-A", null),
                        BinaryOp.Operator.GREATER_THAN,
                        Literal.numeric(5, null), null),
                List.of(new DisplayStatement(List.of(Literal.string("BIG", null)), null, null)),
                Collections.emptyList(), null);
        stmt.accept(stmtVisitor);
        String output = buffer.getContent();
        assertTrue(output.contains("if ("));
        assertTrue(output.contains("compareTo"));
        assertTrue(output.contains("System.out.println(\"BIG\")"));
    }

    @Test
    void ifElseStatement() {
        IfStatement stmt = new IfStatement(
                new BinaryOp(VariableRef.simple("WS-A", null),
                        BinaryOp.Operator.EQUAL,
                        Literal.numeric(0, null), null),
                List.of(new DisplayStatement(List.of(Literal.string("ZERO", null)), null, null)),
                List.of(new DisplayStatement(List.of(Literal.string("NOT ZERO", null)), null, null)),
                null);
        stmt.accept(stmtVisitor);
        String output = buffer.getContent();
        assertTrue(output.contains("if ("));
        assertTrue(output.contains("} else {"));
    }

    @Test
    void performSimple() {
        PerformStatement stmt = new PerformStatement(
                PerformStatement.PerformType.SIMPLE,
                "MAIN-PARA", null, null, null, null, null, null, null);
        stmt.accept(stmtVisitor);
        assertTrue(buffer.getContent().contains("mainPara();"));
    }

    @Test
    void performTimes() {
        PerformStatement stmt = new PerformStatement(
                PerformStatement.PerformType.TIMES,
                "SUB-PARA", null, Literal.numeric(3, null),
                null, null, null, null, null);
        stmt.accept(stmtVisitor);
        String output = buffer.getContent();
        assertTrue(output.contains("for (int i = 0;"));
        assertTrue(output.contains("subPara();"));
    }

    @Test
    void performUntil() {
        PerformStatement stmt = new PerformStatement(
                PerformStatement.PerformType.UNTIL,
                "SUB-PARA", null, null,
                new BinaryOp(VariableRef.simple("WS-A", null),
                        BinaryOp.Operator.GREATER_THAN,
                        Literal.numeric(10, null), null),
                null, null, null, null);
        stmt.accept(stmtVisitor);
        String output = buffer.getContent();
        assertTrue(output.contains("while (!("));
        assertTrue(output.contains("subPara();"));
    }

    @Test
    void goToStatement() {
        GoToStatement stmt = new GoToStatement("EXIT-PARA", null, null, null);
        stmt.accept(stmtVisitor);
        String output = buffer.getContent();
        assertTrue(output.contains("exitPara();"));
        assertTrue(output.contains("return;"));
    }

    @Test
    void stopRunStatement() {
        StopStatement stmt = new StopStatement(StopStatement.StopType.RUN, null, null);
        stmt.accept(stmtVisitor);
        assertTrue(buffer.getContent().contains("return;"));
    }

    @Test
    void exitStatement() {
        ExitStatement stmt = new ExitStatement(ExitStatement.ExitType.PROGRAM, null);
        stmt.accept(stmtVisitor);
        assertTrue(buffer.getContent().contains("return;"));
    }

    @Test
    void openStatement() {
        OpenStatement stmt = new OpenStatement(
                List.of(new OpenStatement.FileSpec("MY-FILE", OpenStatement.FileSpec.OpenMode.INPUT)),
                null);
        stmt.accept(stmtVisitor);
        String output = buffer.getContent();
        assertTrue(output.contains("myFile.open(CobolFile.OpenMode.INPUT)"));
        assertTrue(output.contains("wsName = myFile.getStatus().getCode()"));
    }

    @Test
    void closeStatement() {
        CloseStatement stmt = new CloseStatement(List.of("MY-FILE"), null);
        stmt.accept(stmtVisitor);
        assertTrue(buffer.getContent().contains("myFile.close()"));
        assertTrue(buffer.getContent().contains("wsName = myFile.getStatus().getCode()"));
    }

    @Test
    void readStatementUpdatesFileStatus() {
        ReadStatement stmt = new ReadStatement("MY-FILE", null, null, List.of(), List.of(), List.of(), null);
        stmt.accept(stmtVisitor);
        assertTrue(buffer.getContent().contains("wsName = __readResult1.status().getCode()"));
    }

    @Test
    void setStatement() {
        SetStatement stmt = new SetStatement(
                List.of(VariableRef.simple("WS-A", null)),
                Literal.numeric(1, null), null);
        stmt.accept(stmtVisitor);
        assertTrue(buffer.getContent().contains("wsA = new BigDecimal(\"1\")"));
    }

    @Test
    void initializeStatement() {
        InitializeStatement stmt = new InitializeStatement(
                List.of(VariableRef.simple("WS-A", null)), null);
        stmt.accept(stmtVisitor);
        String output = buffer.getContent();
        assertTrue(output.contains("wsA = BigDecimal.ZERO"));
    }

    @Test
    void acceptFromDate() {
        AcceptStatement stmt = new AcceptStatement(
                VariableRef.simple("WS-NAME", null), "DATE", null);
        stmt.accept(stmtVisitor);
        String output = buffer.getContent();
        assertTrue(output.contains("LocalDate.now()"));
    }

    @Test
    void evaluateStatement() {
        EvaluateStatement stmt = new EvaluateStatement(
                List.of(VariableRef.simple("WS-A", null)),
                List.of(
                        new EvaluateStatement.WhenClause(
                                List.of(Literal.numeric(1, null)),
                                List.of(new DisplayStatement(List.of(Literal.string("ONE", null)), null, null))
                        ),
                        new EvaluateStatement.WhenClause(
                                List.of(Literal.numeric(2, null)),
                                List.of(new DisplayStatement(List.of(Literal.string("TWO", null)), null, null))
                        )
                ),
                List.of(new DisplayStatement(List.of(Literal.string("OTHER", null)), null, null)),
                null);
        stmt.accept(stmtVisitor);
        String output = buffer.getContent();
        assertTrue(output.contains("if ("));
        assertTrue(output.contains("else if ("));
        assertTrue(output.contains("else {"));
    }

    @Test
    void inspectTallyingStatementGeneratesRuntimeCall() {
        InspectStatement stmt = new InspectStatement(
                VariableRef.simple("WS-NAME", null),
                InspectStatement.InspectOp.TALLYING,
                new InspectStatement.TallyingClause(
                        VariableRef.simple("WS-NUM", null),
                        List.of(new InspectStatement.TallyFor(
                                InspectStatement.TallyMode.LEADING,
                                Literal.string("A", null),
                                new InspectStatement.Boundary(Literal.string("B", null), true),
                                null))),
                List.of(),
                null,
                null);
        stmt.accept(stmtVisitor);
        String output = buffer.getContent();
        assertTrue(output.contains("CobolString.inspectTallying"));
        assertTrue(output.contains("wsNum = BigDecimal.valueOf("));
    }

    @Test
    void inspectReplacingStatementGeneratesRuntimeCall() {
        InspectStatement stmt = new InspectStatement(
                VariableRef.simple("WS-NAME", null),
                InspectStatement.InspectOp.REPLACING,
                null,
                List.of(new InspectStatement.ReplacingClause(
                        InspectStatement.ReplaceMode.FIRST,
                        Literal.string("A", null),
                        Literal.string("Z", null),
                        null,
                        new InspectStatement.Boundary(Literal.string("B", null), false))),
                null,
                null);
        stmt.accept(stmtVisitor);
        String output = buffer.getContent();
        assertTrue(output.contains("CobolString.inspectReplacing"));
        assertTrue(output.contains("wsName = CobolString.inspectReplacing("));
    }

    @Test
    void inspectConvertingStatementGeneratesRuntimeCall() {
        InspectStatement stmt = new InspectStatement(
                VariableRef.simple("WS-NAME", null),
                InspectStatement.InspectOp.CONVERTING,
                null,
                List.of(),
                new InspectStatement.ConvertingClause(
                        Literal.string("ABC", null),
                        Literal.string("XYZ", null),
                        null,
                        new InspectStatement.Boundary(Literal.string("Q", null), false)),
                null);
        stmt.accept(stmtVisitor);
        String output = buffer.getContent();
        assertTrue(output.contains("CobolString.inspectConverting"));
        assertTrue(output.contains("wsName = CobolString.inspectConverting("));
    }
}
