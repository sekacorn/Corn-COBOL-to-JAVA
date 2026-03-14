/*
 * IrExpressionTest - Unit tests for IR expression hierarchy
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.sekacorn.corn.ir.expr.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("IR Expression — Expression hierarchy and visitor pattern")
class IrExpressionTest {

    private static final SourceLocation LOC = SourceLocation.of("EXPR.cbl", 10, 12);

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new Jdk8Module());

    // ─── Literal ──────────────────────────────────────────

    @Nested
    @DisplayName("Literal")
    class LiteralTests {
        @Test
        @DisplayName("numeric literal via factory")
        void numericFactory() {
            var lit = Literal.numeric(42, LOC);
            assertThat(lit.getValue()).isEqualTo(42);
            assertThat(lit.getLiteralType()).isEqualTo(Literal.LiteralType.NUMERIC);
            assertThat(lit.getLocation()).isEqualTo(LOC);
        }

        @Test
        @DisplayName("string literal via factory")
        void stringFactory() {
            var lit = Literal.string("HELLO", LOC);
            assertThat(lit.getValue()).isEqualTo("HELLO");
            assertThat(lit.getLiteralType()).isEqualTo(Literal.LiteralType.STRING);
        }

        @Test
        @DisplayName("figurative literal via factory")
        void figurativeFactory() {
            var lit = Literal.figurative(Literal.FigurativeConstant.ZERO, LOC);
            assertThat(lit.getValue()).isEqualTo(Literal.FigurativeConstant.ZERO);
            assertThat(lit.getLiteralType()).isEqualTo(Literal.LiteralType.FIGURATIVE);
        }

        @Test
        @DisplayName("all figurative constants defined")
        void figurativeConstants() {
            var constants = Literal.FigurativeConstant.values();
            assertThat(constants).hasSize(15);
            assertThat(constants).contains(
                    Literal.FigurativeConstant.ZERO,
                    Literal.FigurativeConstant.SPACES,
                    Literal.FigurativeConstant.HIGH_VALUE,
                    Literal.FigurativeConstant.LOW_VALUE,
                    Literal.FigurativeConstant.QUOTE,
                    Literal.FigurativeConstant.NULL,
                    Literal.FigurativeConstant.ALL,
                    Literal.FigurativeConstant.TRUE,
                    Literal.FigurativeConstant.FALSE);
        }

        @Test
        @DisplayName("equality")
        void equality() {
            var a = Literal.numeric(10, LOC);
            var b = Literal.numeric(10, LOC);
            var c = Literal.numeric(20, LOC);
            assertThat(a).isEqualTo(b);
            assertThat(a).isNotEqualTo(c);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("toString includes value and type")
        void toStringFormat() {
            var lit = Literal.string("ABC", LOC);
            assertThat(lit.toString()).contains("ABC").contains("STRING");
        }

        @Test
        @DisplayName("literalType is required")
        void nullTypeFails() {
            assertThatThrownBy(() -> new Literal("val", null, LOC))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("JSON round-trip")
        void jsonRoundTrip() throws Exception {
            var lit = Literal.numeric(99, LOC);
            String json = mapper.writeValueAsString(lit);
            assertThat(json).contains("\"type\":\"literal\"");
            Expression deserialized = mapper.readValue(json, Expression.class);
            assertThat(deserialized).isInstanceOf(Literal.class);
            assertThat(((Literal) deserialized).getLiteralType()).isEqualTo(Literal.LiteralType.NUMERIC);
        }

        @Test
        @DisplayName("accepts visitor")
        void visitorAccept() {
            var lit = Literal.numeric(5, LOC);
            String result = lit.accept(new TestExpressionVisitor());
            assertThat(result).isEqualTo("literal");
        }
    }

    // ─── VariableRef ──────────────────────────────────────

    @Nested
    @DisplayName("VariableRef")
    class VariableRefTests {
        @Test
        @DisplayName("simple variable reference")
        void simpleRef() {
            var ref = VariableRef.simple("WS-AMOUNT", LOC);
            assertThat(ref.getName()).isEqualTo("WS-AMOUNT");
            assertThat(ref.getQualifier()).isEmpty();
            assertThat(ref.getRefMod()).isEmpty();
            assertThat(ref.getLocation()).isEqualTo(LOC);
        }

        @Test
        @DisplayName("qualified variable reference")
        void qualifiedRef() {
            var ref = new VariableRef("ACCOUNT-NUM", "CUSTOMER-REC", null, LOC);
            assertThat(ref.getQualifier()).hasValue("CUSTOMER-REC");
        }

        @Test
        @DisplayName("reference modification")
        void refMod() {
            var start = Literal.numeric(1, LOC);
            var length = Literal.numeric(5, LOC);
            var mod = new VariableRef.ReferenceModification(start, length);
            var ref = new VariableRef("WS-NAME", null, mod, LOC);
            assertThat(ref.getRefMod()).isPresent();
            assertThat(ref.getRefMod().get().getStart()).isEqualTo(start);
            assertThat(ref.getRefMod().get().getLength()).hasValue(length);
        }

        @Test
        @DisplayName("reference modification with no length")
        void refModNoLength() {
            var start = Literal.numeric(3, LOC);
            var mod = new VariableRef.ReferenceModification(start, null);
            assertThat(mod.getLength()).isEmpty();
        }

        @Test
        @DisplayName("name is required")
        void nullNameFails() {
            assertThatThrownBy(() -> VariableRef.simple(null, LOC))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("equality")
        void equality() {
            var a = VariableRef.simple("X", LOC);
            var b = VariableRef.simple("X", LOC);
            var c = VariableRef.simple("Y", LOC);
            assertThat(a).isEqualTo(b);
            assertThat(a).isNotEqualTo(c);
        }

        @Test
        @DisplayName("toString with qualification")
        void toStringQualified() {
            var ref = new VariableRef("FIELD", "RECORD", null, LOC);
            assertThat(ref.toString()).contains("FIELD").contains("OF").contains("RECORD");
        }

        @Test
        @DisplayName("JSON round-trip")
        void jsonRoundTrip() throws Exception {
            var ref = VariableRef.simple("WS-VAR", LOC);
            String json = mapper.writeValueAsString(ref);
            assertThat(json).contains("\"type\":\"variable\"");
            Expression deserialized = mapper.readValue(json, Expression.class);
            assertThat(deserialized).isInstanceOf(VariableRef.class);
            assertThat(((VariableRef) deserialized).getName()).isEqualTo("WS-VAR");
        }

        @Test
        @DisplayName("accepts visitor")
        void visitorAccept() {
            assertThat(VariableRef.simple("X", LOC).accept(new TestExpressionVisitor()))
                    .isEqualTo("variable");
        }
    }

    // ─── BinaryOp ─────────────────────────────────────────

    @Nested
    @DisplayName("BinaryOp")
    class BinaryOpTests {
        @Test
        @DisplayName("arithmetic addition")
        void addOp() {
            var left = Literal.numeric(10, LOC);
            var right = Literal.numeric(20, LOC);
            var op = new BinaryOp(left, BinaryOp.Operator.ADD, right, LOC);
            assertThat(op.getLeft()).isEqualTo(left);
            assertThat(op.getRight()).isEqualTo(right);
            assertThat(op.getOperator()).isEqualTo(BinaryOp.Operator.ADD);
        }

        @Test
        @DisplayName("all operator types defined")
        void operators() {
            assertThat(BinaryOp.Operator.values()).hasSize(13);
            assertThat(BinaryOp.Operator.values()).contains(
                    BinaryOp.Operator.ADD, BinaryOp.Operator.SUBTRACT,
                    BinaryOp.Operator.MULTIPLY, BinaryOp.Operator.DIVIDE,
                    BinaryOp.Operator.POWER,
                    BinaryOp.Operator.EQUAL, BinaryOp.Operator.NOT_EQUAL,
                    BinaryOp.Operator.LESS_THAN, BinaryOp.Operator.GREATER_THAN,
                    BinaryOp.Operator.AND, BinaryOp.Operator.OR);
        }

        @Test
        @DisplayName("null operands rejected")
        void nullOperands() {
            var lit = Literal.numeric(1, LOC);
            assertThatThrownBy(() -> new BinaryOp(null, BinaryOp.Operator.ADD, lit, LOC))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new BinaryOp(lit, null, lit, LOC))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new BinaryOp(lit, BinaryOp.Operator.ADD, null, LOC))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("toString shows infix notation")
        void toStringInfix() {
            var op = new BinaryOp(Literal.numeric(1, LOC), BinaryOp.Operator.ADD,
                    Literal.numeric(2, LOC), LOC);
            assertThat(op.toString()).contains("ADD");
        }

        @Test
        @DisplayName("JSON round-trip")
        void jsonRoundTrip() throws Exception {
            var op = new BinaryOp(Literal.numeric(5, LOC), BinaryOp.Operator.MULTIPLY,
                    Literal.numeric(3, LOC), LOC);
            String json = mapper.writeValueAsString(op);
            assertThat(json).contains("\"type\":\"binary\"");
            Expression deserialized = mapper.readValue(json, Expression.class);
            assertThat(deserialized).isInstanceOf(BinaryOp.class);
            assertThat(((BinaryOp) deserialized).getOperator()).isEqualTo(BinaryOp.Operator.MULTIPLY);
        }

        @Test
        @DisplayName("accepts visitor")
        void visitorAccept() {
            var op = new BinaryOp(Literal.numeric(1, LOC), BinaryOp.Operator.ADD,
                    Literal.numeric(2, LOC), LOC);
            assertThat(op.accept(new TestExpressionVisitor())).isEqualTo("binary");
        }
    }

    // ─── UnaryOp ──────────────────────────────────────────

    @Nested
    @DisplayName("UnaryOp")
    class UnaryOpTests {
        @Test
        @DisplayName("negation")
        void negate() {
            var operand = Literal.numeric(42, LOC);
            var op = new UnaryOp(UnaryOp.Operator.NEGATE, operand, LOC);
            assertThat(op.getOperator()).isEqualTo(UnaryOp.Operator.NEGATE);
            assertThat(op.getOperand()).isEqualTo(operand);
        }

        @Test
        @DisplayName("logical NOT")
        void logicalNot() {
            var cond = VariableRef.simple("WS-FLAG", LOC);
            var op = new UnaryOp(UnaryOp.Operator.NOT, cond, LOC);
            assertThat(op.getOperator()).isEqualTo(UnaryOp.Operator.NOT);
        }

        @Test
        @DisplayName("null operand rejected")
        void nullOperand() {
            assertThatThrownBy(() -> new UnaryOp(UnaryOp.Operator.NEGATE, null, LOC))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("JSON round-trip")
        void jsonRoundTrip() throws Exception {
            var op = new UnaryOp(UnaryOp.Operator.NOT, Literal.numeric(0, LOC), LOC);
            String json = mapper.writeValueAsString(op);
            Expression deserialized = mapper.readValue(json, Expression.class);
            assertThat(deserialized).isInstanceOf(UnaryOp.class);
        }

        @Test
        @DisplayName("accepts visitor")
        void visitorAccept() {
            var op = new UnaryOp(UnaryOp.Operator.NEGATE, Literal.numeric(1, LOC), LOC);
            assertThat(op.accept(new TestExpressionVisitor())).isEqualTo("unary");
        }
    }

    // ─── SubscriptRef ─────────────────────────────────────

    @Nested
    @DisplayName("SubscriptRef")
    class SubscriptRefTests {
        @Test
        @DisplayName("single subscript")
        void singleSubscript() {
            var idx = Literal.numeric(1, LOC);
            var ref = new SubscriptRef("ITEM-TABLE", List.of(idx), LOC);
            assertThat(ref.getName()).isEqualTo("ITEM-TABLE");
            assertThat(ref.getSubscripts()).hasSize(1);
        }

        @Test
        @DisplayName("multi-dimensional subscript")
        void multiDimSubscript() {
            var ref = new SubscriptRef("MATRIX",
                    List.of(Literal.numeric(1, LOC), Literal.numeric(2, LOC)), LOC);
            assertThat(ref.getSubscripts()).hasSize(2);
        }

        @Test
        @DisplayName("null subscripts defaults to empty list")
        void nullSubscripts() {
            var ref = new SubscriptRef("TBL", null, LOC);
            assertThat(ref.getSubscripts()).isEmpty();
        }

        @Test
        @DisplayName("name is required")
        void nullName() {
            assertThatThrownBy(() -> new SubscriptRef(null, List.of(), LOC))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("JSON round-trip")
        void jsonRoundTrip() throws Exception {
            var ref = new SubscriptRef("TBL", List.of(Literal.numeric(3, LOC)), LOC);
            String json = mapper.writeValueAsString(ref);
            Expression deserialized = mapper.readValue(json, Expression.class);
            assertThat(deserialized).isInstanceOf(SubscriptRef.class);
            assertThat(((SubscriptRef) deserialized).getName()).isEqualTo("TBL");
        }

        @Test
        @DisplayName("accepts visitor")
        void visitorAccept() {
            var ref = new SubscriptRef("T", List.of(), LOC);
            assertThat(ref.accept(new TestExpressionVisitor())).isEqualTo("subscript");
        }
    }

    // ─── FunctionCall ─────────────────────────────────────

    @Nested
    @DisplayName("FunctionCall")
    class FunctionCallTests {
        @Test
        @DisplayName("intrinsic function with arguments")
        void withArgs() {
            var arg = Literal.numeric(100, LOC);
            var call = new FunctionCall("LENGTH", List.of(arg), LOC);
            assertThat(call.getFunctionName()).isEqualTo("LENGTH");
            assertThat(call.getArguments()).hasSize(1);
        }

        @Test
        @DisplayName("function with no arguments")
        void noArgs() {
            var call = new FunctionCall("CURRENT-DATE", null, LOC);
            assertThat(call.getArguments()).isEmpty();
        }

        @Test
        @DisplayName("functionName is required")
        void nullName() {
            assertThatThrownBy(() -> new FunctionCall(null, List.of(), LOC))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("toString shows FUNCTION prefix")
        void toStringFormat() {
            var call = new FunctionCall("SQRT", List.of(Literal.numeric(9, LOC)), LOC);
            assertThat(call.toString()).contains("FUNCTION").contains("SQRT");
        }

        @Test
        @DisplayName("JSON round-trip")
        void jsonRoundTrip() throws Exception {
            var call = new FunctionCall("ABS", List.of(Literal.numeric(-5, LOC)), LOC);
            String json = mapper.writeValueAsString(call);
            Expression deserialized = mapper.readValue(json, Expression.class);
            assertThat(deserialized).isInstanceOf(FunctionCall.class);
        }

        @Test
        @DisplayName("accepts visitor")
        void visitorAccept() {
            var call = new FunctionCall("LEN", List.of(), LOC);
            assertThat(call.accept(new TestExpressionVisitor())).isEqualTo("function");
        }
    }

    // ─── ConditionExpr ────────────────────────────────────

    @Nested
    @DisplayName("ConditionExpr")
    class ConditionExprTests {
        @Test
        @DisplayName("NUMERIC class condition")
        void numericCondition() {
            var subject = VariableRef.simple("WS-AMOUNT", LOC);
            var cond = new ConditionExpr(subject, ConditionExpr.ConditionType.NUMERIC, false, LOC);
            assertThat(cond.getSubject()).isEqualTo(subject);
            assertThat(cond.getConditionType()).isEqualTo(ConditionExpr.ConditionType.NUMERIC);
            assertThat(cond.isNegated()).isFalse();
        }

        @Test
        @DisplayName("negated condition")
        void negated() {
            var cond = new ConditionExpr(VariableRef.simple("X", LOC),
                    ConditionExpr.ConditionType.ALPHABETIC, true, LOC);
            assertThat(cond.isNegated()).isTrue();
        }

        @Test
        @DisplayName("all condition types defined")
        void conditionTypes() {
            assertThat(ConditionExpr.ConditionType.values()).hasSize(8);
            assertThat(ConditionExpr.ConditionType.values()).contains(
                    ConditionExpr.ConditionType.NUMERIC,
                    ConditionExpr.ConditionType.ALPHABETIC,
                    ConditionExpr.ConditionType.ALPHABETIC_LOWER,
                    ConditionExpr.ConditionType.ALPHABETIC_UPPER,
                    ConditionExpr.ConditionType.POSITIVE,
                    ConditionExpr.ConditionType.NEGATIVE,
                    ConditionExpr.ConditionType.ZERO,
                    ConditionExpr.ConditionType.CONDITION_NAME);
        }

        @Test
        @DisplayName("toString includes NOT when negated")
        void toStringNegated() {
            var cond = new ConditionExpr(VariableRef.simple("V", LOC),
                    ConditionExpr.ConditionType.ZERO, true, LOC);
            assertThat(cond.toString()).contains("NOT");
        }

        @Test
        @DisplayName("JSON round-trip")
        void jsonRoundTrip() throws Exception {
            var cond = new ConditionExpr(Literal.numeric(0, LOC),
                    ConditionExpr.ConditionType.ZERO, false, LOC);
            String json = mapper.writeValueAsString(cond);
            Expression deserialized = mapper.readValue(json, Expression.class);
            assertThat(deserialized).isInstanceOf(ConditionExpr.class);
        }

        @Test
        @DisplayName("accepts visitor")
        void visitorAccept() {
            var cond = new ConditionExpr(Literal.numeric(0, LOC),
                    ConditionExpr.ConditionType.POSITIVE, false, LOC);
            assertThat(cond.accept(new TestExpressionVisitor())).isEqualTo("condition");
        }
    }

    // ─── ExpressionVisitor pattern ────────────────────────

    @Nested
    @DisplayName("ExpressionVisitor")
    class VisitorTests {
        @Test
        @DisplayName("visitor dispatches to correct method for each expression type")
        void allDispatches() {
            var visitor = new TestExpressionVisitor();

            assertThat(Literal.numeric(1, LOC).accept(visitor)).isEqualTo("literal");
            assertThat(VariableRef.simple("X", LOC).accept(visitor)).isEqualTo("variable");
            assertThat(new SubscriptRef("T", List.of(), LOC).accept(visitor)).isEqualTo("subscript");
            assertThat(new BinaryOp(Literal.numeric(1, LOC), BinaryOp.Operator.ADD,
                    Literal.numeric(2, LOC), LOC).accept(visitor)).isEqualTo("binary");
            assertThat(new UnaryOp(UnaryOp.Operator.NEGATE,
                    Literal.numeric(1, LOC), LOC).accept(visitor)).isEqualTo("unary");
            assertThat(new FunctionCall("F", List.of(), LOC).accept(visitor)).isEqualTo("function");
            assertThat(new ConditionExpr(Literal.numeric(0, LOC),
                    ConditionExpr.ConditionType.ZERO, false, LOC).accept(visitor)).isEqualTo("condition");
        }
    }

    // ─── Nested expression trees ──────────────────────────

    @Nested
    @DisplayName("Nested expressions")
    class NestedTests {
        @Test
        @DisplayName("nested binary expression tree")
        void nestedBinary() {
            // (A + B) * C
            var a = Literal.numeric(10, LOC);
            var b = Literal.numeric(20, LOC);
            var c = Literal.numeric(3, LOC);
            var sum = new BinaryOp(a, BinaryOp.Operator.ADD, b, LOC);
            var product = new BinaryOp(sum, BinaryOp.Operator.MULTIPLY, c, LOC);

            assertThat(product.getLeft()).isInstanceOf(BinaryOp.class);
            assertThat(((BinaryOp) product.getLeft()).getOperator()).isEqualTo(BinaryOp.Operator.ADD);
        }

        @Test
        @DisplayName("function call with nested expression argument")
        void functionWithNestedArg() {
            var expr = new BinaryOp(Literal.numeric(3, LOC), BinaryOp.Operator.POWER,
                    Literal.numeric(2, LOC), LOC);
            var call = new FunctionCall("SQRT", List.of(expr), LOC);
            assertThat(call.getArguments().get(0)).isInstanceOf(BinaryOp.class);
        }

        @Test
        @DisplayName("nested expression JSON round-trip")
        void nestedJsonRoundTrip() throws Exception {
            var inner = new BinaryOp(Literal.numeric(1, LOC), BinaryOp.Operator.ADD,
                    Literal.numeric(2, LOC), LOC);
            var outer = new UnaryOp(UnaryOp.Operator.NEGATE, inner, LOC);
            String json = mapper.writeValueAsString(outer);
            Expression deserialized = mapper.readValue(json, Expression.class);
            assertThat(deserialized).isInstanceOf(UnaryOp.class);
            assertThat(((UnaryOp) deserialized).getOperand()).isInstanceOf(BinaryOp.class);
        }
    }

    // ─── Test visitor implementation ──────────────────────

    private static class TestExpressionVisitor implements ExpressionVisitor<String> {
        @Override public String visitLiteral(Literal expr) { return "literal"; }
        @Override public String visitVariable(VariableRef expr) { return "variable"; }
        @Override public String visitSubscript(SubscriptRef expr) { return "subscript"; }
        @Override public String visitBinary(BinaryOp expr) { return "binary"; }
        @Override public String visitUnary(UnaryOp expr) { return "unary"; }
        @Override public String visitFunction(FunctionCall expr) { return "function"; }
        @Override public String visitCondition(ConditionExpr expr) { return "condition"; }
    }
}
