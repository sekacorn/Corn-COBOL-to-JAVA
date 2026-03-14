package com.sekacorn.corn.codegen;

import com.sekacorn.corn.ir.DataItem;
import com.sekacorn.corn.ir.Picture;
import com.sekacorn.corn.ir.expr.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JavaExpressionVisitorTest {

    private JavaExpressionVisitor visitor;
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
                        null, null, null, null, null, null, null, false, false, false, false)
        );
        visitor = new JavaExpressionVisitor(dataItemMap, buffer);
    }

    @Test
    void numericLiteralGeneratesBigDecimal() {
        Literal lit = Literal.numeric(42, null);
        String result = lit.accept(visitor);
        assertEquals("new BigDecimal(\"42\")", result);
        assertTrue(buffer.getImports().contains("java.math.BigDecimal"));
    }

    @Test
    void stringLiteralGeneratesQuotedString() {
        Literal lit = Literal.string("HELLO", null);
        String result = lit.accept(visitor);
        assertEquals("\"HELLO\"", result);
    }

    @Test
    void figurativeZeroGeneratesBigDecimalZero() {
        Literal lit = Literal.figurative(Literal.FigurativeConstant.ZERO, null);
        String result = lit.accept(visitor);
        assertEquals("BigDecimal.ZERO", result);
    }

    @Test
    void figurativeSpaceGeneratesString() {
        Literal lit = Literal.figurative(Literal.FigurativeConstant.SPACE, null);
        String result = lit.accept(visitor);
        assertEquals("\" \"", result);
    }

    @Test
    void variableRefGeneratesFieldName() {
        VariableRef ref = VariableRef.simple("WS-NAME", null);
        String result = ref.accept(visitor);
        assertEquals("wsName", result);
    }

    @Test
    void binaryAddGeneratesAddMethod() {
        BinaryOp op = new BinaryOp(
                Literal.numeric(1, null),
                BinaryOp.Operator.ADD,
                Literal.numeric(2, null),
                null);
        String result = op.accept(visitor);
        assertEquals("new BigDecimal(\"1\").add(new BigDecimal(\"2\"))", result);
    }

    @Test
    void binaryEqualGeneratesCompareTo() {
        BinaryOp op = new BinaryOp(
                VariableRef.simple("WS-NUM", null),
                BinaryOp.Operator.EQUAL,
                Literal.numeric(5, null),
                null);
        String result = op.accept(visitor);
        assertEquals("wsNum.compareTo(new BigDecimal(\"5\")) == 0", result);
    }

    @Test
    void binaryAndGeneratesLogicalAnd() {
        BinaryOp op = new BinaryOp(
                Literal.numeric(1, null),
                BinaryOp.Operator.AND,
                Literal.numeric(2, null),
                null);
        String result = op.accept(visitor);
        assertTrue(result.contains("&&"));
    }

    @Test
    void unaryNegateGeneratesNegateMethod() {
        UnaryOp op = new UnaryOp(
                UnaryOp.Operator.NEGATE,
                Literal.numeric(5, null),
                null);
        String result = op.accept(visitor);
        assertEquals("new BigDecimal(\"5\").negate()", result);
    }

    @Test
    void unaryNotGeneratesLogicalNot() {
        UnaryOp op = new UnaryOp(
                UnaryOp.Operator.NOT,
                Literal.numeric(1, null),
                null);
        String result = op.accept(visitor);
        assertTrue(result.startsWith("!("));
    }

    @Test
    void functionCallLength() {
        FunctionCall func = new FunctionCall("LENGTH",
                List.of(VariableRef.simple("WS-NAME", null)), null);
        String result = func.accept(visitor);
        assertTrue(result.contains("length()"));
    }

    @Test
    void conditionExprNumeric() {
        ConditionExpr cond = new ConditionExpr(
                VariableRef.simple("WS-NUM", null),
                ConditionExpr.ConditionType.NUMERIC,
                false, null);
        String result = cond.accept(visitor);
        assertTrue(result.contains("CobolMath.isNumeric"));
    }

    @Test
    void conditionExprNegated() {
        ConditionExpr cond = new ConditionExpr(
                VariableRef.simple("WS-NUM", null),
                ConditionExpr.ConditionType.ZERO,
                true, null);
        String result = cond.accept(visitor);
        assertTrue(result.startsWith("!"));
        assertTrue(result.contains("CobolMath.isZero"));
    }

    @Test
    void isNumericFieldReturnsTrueForNumeric() {
        assertTrue(visitor.isNumericField("WS-NUM"));
    }

    @Test
    void isNumericFieldReturnsFalseForString() {
        assertFalse(visitor.isNumericField("WS-NAME"));
    }

    @Test
    void isNumericFieldReturnsFalseForUnknown() {
        assertFalse(visitor.isNumericField("NONEXISTENT"));
    }
}
