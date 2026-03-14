/*
 * IrStatementTest - Unit tests for IR statement hierarchy
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir.stmt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.sekacorn.corn.ir.SourceLocation;
import com.sekacorn.corn.ir.expr.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("IR Statement — Statement hierarchy and visitor pattern")
class IrStatementTest {

    private static final SourceLocation LOC = SourceLocation.of("STMT.cbl", 20, 12);
    private static final Expression VAR_X = VariableRef.simple("X", LOC);
    private static final Expression VAR_Y = VariableRef.simple("Y", LOC);
    private static final Expression LIT_1 = Literal.numeric(1, LOC);
    private static final Expression LIT_10 = Literal.numeric(10, LOC);

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new Jdk8Module());

    // ─── MoveStatement ────────────────────────────────────

    @Nested
    @DisplayName("MoveStatement")
    class MoveTests {
        @Test
        @DisplayName("move to single target")
        void singleTarget() {
            var move = new MoveStatement(LIT_1, List.of(VAR_X), false, LOC);
            assertThat(move.getSource()).isEqualTo(LIT_1);
            assertThat(move.getTargets()).hasSize(1);
            assertThat(move.getLocation()).isEqualTo(LOC);
            assertThat(move.isCorresponding()).isFalse();
        }

        @Test
        @DisplayName("move to multiple targets")
        void multipleTargets() {
            var move = new MoveStatement(LIT_1, List.of(VAR_X, VAR_Y), false, LOC);
            assertThat(move.getTargets()).hasSize(2);
        }

        @Test
        @DisplayName("MOVE CORRESPONDING")
        void moveCorresponding() {
            var move = new MoveStatement(VAR_X, List.of(VAR_Y), true, LOC);
            assertThat(move.isCorresponding()).isTrue();
        }

        @Test
        @DisplayName("source is required")
        void nullSource() {
            assertThatThrownBy(() -> new MoveStatement(null, List.of(VAR_X), false, LOC))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null targets defaults to empty")
        void nullTargets() {
            var move = new MoveStatement(LIT_1, null, false, LOC);
            assertThat(move.getTargets()).isEmpty();
        }

        @Test
        @DisplayName("equality")
        void equality() {
            var a = new MoveStatement(LIT_1, List.of(VAR_X), false, LOC);
            var b = new MoveStatement(LIT_1, List.of(VAR_X), false, LOC);
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("JSON round-trip")
        void jsonRoundTrip() throws Exception {
            var move = new MoveStatement(LIT_1, List.of(VAR_X), false, LOC);
            String json = mapper.writeValueAsString(move);
            assertThat(json).contains("\"type\":\"move\"");
            Statement deserialized = mapper.readValue(json, Statement.class);
            assertThat(deserialized).isInstanceOf(MoveStatement.class);
        }

        @Test
        @DisplayName("accepts visitor")
        void visitorAccept() {
            var move = new MoveStatement(LIT_1, List.of(VAR_X), false, LOC);
            assertThat(move.accept(new TestStatementVisitor())).isEqualTo("move");
        }
    }

    // ─── Arithmetic Statements ────────────────────────────

    @Nested
    @DisplayName("Arithmetic Statements")
    class ArithmeticTests {
        @Test
        @DisplayName("ADD with operands and TO")
        void addStatement() {
            var add = new AddStatement(List.of(LIT_1), List.of(VAR_X), List.of(),
                    false, null, List.of(), LOC);
            assertThat(add.operands()).hasSize(1);
            assertThat(add.to()).hasSize(1);
            assertThat(add.giving()).isEmpty();
            assertThat(add.rounded()).isFalse();
            assertThat(add.roundMode()).isNull();
            assertThat(add.accept(new TestStatementVisitor())).isEqualTo("add");
        }

        @Test
        @DisplayName("ADD with ROUNDED")
        void addRounded() {
            var add = new AddStatement(List.of(LIT_1), List.of(VAR_X), List.of(),
                    true, RoundMode.HALF_UP, List.of(), LOC);
            assertThat(add.rounded()).isTrue();
            assertThat(add.roundMode()).isEqualTo(RoundMode.HALF_UP);
        }

        @Test
        @DisplayName("ADD null lists default to empty")
        void addNullDefaults() {
            var add = new AddStatement(null, null, null, false, null, null, LOC);
            assertThat(add.operands()).isEmpty();
            assertThat(add.to()).isEmpty();
            assertThat(add.giving()).isEmpty();
            assertThat(add.onSizeError()).isEmpty();
        }

        @Test
        @DisplayName("SUBTRACT with GIVING")
        void subtractStatement() {
            var sub = new SubtractStatement(List.of(LIT_1), List.of(VAR_X),
                    List.of(VAR_Y), false, null, List.of(), LOC);
            assertThat(sub.from()).hasSize(1);
            assertThat(sub.giving()).hasSize(1);
            assertThat(sub.accept(new TestStatementVisitor())).isEqualTo("subtract");
        }

        @Test
        @DisplayName("MULTIPLY requires both operands")
        void multiplyStatement() {
            var mul = new MultiplyStatement(LIT_1, LIT_10, List.of(VAR_X),
                    false, null, List.of(), LOC);
            assertThat(mul.operand1()).isEqualTo(LIT_1);
            assertThat(mul.operand2()).isEqualTo(LIT_10);
            assertThat(mul.accept(new TestStatementVisitor())).isEqualTo("multiply");
        }

        @Test
        @DisplayName("MULTIPLY null operand rejected")
        void multiplyNullRejected() {
            assertThatThrownBy(() -> new MultiplyStatement(null, LIT_10, List.of(),
                    false, null, List.of(), LOC))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("DIVIDE with remainder")
        void divideWithRemainder() {
            var div = new DivideStatement(LIT_10, LIT_1, null, List.of(VAR_X), VAR_Y,
                    false, null, List.of(), LOC);
            assertThat(div.dividend()).isEqualTo(LIT_10);
            assertThat(div.divisor()).isEqualTo(LIT_1);
            assertThat(div.remainder()).isEqualTo(VAR_Y);
            assertThat(div.accept(new TestStatementVisitor())).isEqualTo("divide");
        }

        @Test
        @DisplayName("COMPUTE expression")
        void computeStatement() {
            var expr = new BinaryOp(VAR_X, BinaryOp.Operator.ADD, LIT_1, LOC);
            var compute = new ComputeStatement(List.of(VAR_Y), expr,
                    false, null, List.of(), LOC);
            assertThat(compute.targets()).hasSize(1);
            assertThat(compute.expression()).isEqualTo(expr);
            assertThat(compute.accept(new TestStatementVisitor())).isEqualTo("compute");
        }

        @Test
        @DisplayName("COMPUTE null expression rejected")
        void computeNullExpr() {
            assertThatThrownBy(() -> new ComputeStatement(List.of(VAR_X), null,
                    false, null, List.of(), LOC))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("all RoundMode values defined")
        void roundModes() {
            assertThat(RoundMode.values()).hasSize(8);
        }

        @Test
        @DisplayName("ADD JSON round-trip")
        void addJsonRoundTrip() throws Exception {
            var add = new AddStatement(List.of(LIT_1), List.of(VAR_X), List.of(),
                    false, null, List.of(), LOC);
            String json = mapper.writeValueAsString(add);
            assertThat(json).contains("\"type\":\"add\"");
            Statement deserialized = mapper.readValue(json, Statement.class);
            assertThat(deserialized).isInstanceOf(AddStatement.class);
        }
    }

    // ─── Control Flow Statements ──────────────────────────

    @Nested
    @DisplayName("Control Flow Statements")
    class ControlFlowTests {
        @Test
        @DisplayName("IF with then and else branches")
        void ifStatement() {
            var cond = new BinaryOp(VAR_X, BinaryOp.Operator.GREATER_THAN, LIT_10, LOC);
            var thenStmt = new MoveStatement(LIT_1, List.of(VAR_Y), false, LOC);
            var elseStmt = new MoveStatement(LIT_10, List.of(VAR_Y), false, LOC);
            var ifStmt = new IfStatement(cond, List.of(thenStmt), List.of(elseStmt), LOC);

            assertThat(ifStmt.getCondition()).isEqualTo(cond);
            assertThat(ifStmt.getThenBranch()).hasSize(1);
            assertThat(ifStmt.getElseBranch()).hasSize(1);
            assertThat(ifStmt.hasElse()).isTrue();
            assertThat(ifStmt.accept(new TestStatementVisitor())).isEqualTo("if");
        }

        @Test
        @DisplayName("IF without else")
        void ifNoElse() {
            var cond = new BinaryOp(VAR_X, BinaryOp.Operator.EQUAL, LIT_1, LOC);
            var ifStmt = new IfStatement(cond, List.of(), List.of(), LOC);
            assertThat(ifStmt.hasElse()).isFalse();
        }

        @Test
        @DisplayName("IF condition is required")
        void ifNullCondition() {
            assertThatThrownBy(() -> new IfStatement(null, List.of(), List.of(), LOC))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("IF JSON round-trip preserves hasElse behavior")
        void ifJsonRoundTrip() throws Exception {
            var cond = new BinaryOp(VAR_X, BinaryOp.Operator.EQUAL, LIT_1, LOC);
            var thenStmt = new MoveStatement(LIT_1, List.of(VAR_Y), false, LOC);
            var ifStmt = new IfStatement(cond, List.of(thenStmt), List.of(), LOC);
            String json = mapper.writeValueAsString(ifStmt);
            assertThat(json).doesNotContain("\"hasElse\"");
            Statement deserialized = mapper.readValue(json, Statement.class);
            assertThat(deserialized).isInstanceOf(IfStatement.class);
            assertThat(((IfStatement) deserialized).hasElse()).isFalse();
        }

        @Test
        @DisplayName("EVALUATE with WHEN clauses")
        void evaluateStatement() {
            var whenClause = new EvaluateStatement.WhenClause(
                    List.of(LIT_1), List.of(new MoveStatement(LIT_1, List.of(VAR_X), false, LOC)));
            var eval = new EvaluateStatement(List.of(VAR_X), List.of(whenClause),
                    List.of(new MoveStatement(LIT_10, List.of(VAR_X), false, LOC)), LOC);
            assertThat(eval.subjects()).hasSize(1);
            assertThat(eval.whenClauses()).hasSize(1);
            assertThat(eval.whenOther()).hasSize(1);
            assertThat(eval.accept(new TestStatementVisitor())).isEqualTo("evaluate");
        }

        @Test
        @DisplayName("GO TO simple")
        void goToSimple() {
            var goTo = new GoToStatement("PROCESS-RECORD", null, List.of(), LOC);
            assertThat(goTo.targetParagraph()).isEqualTo("PROCESS-RECORD");
            assertThat(goTo.accept(new TestStatementVisitor())).isEqualTo("goto");
        }

        @Test
        @DisplayName("GO TO DEPENDING ON")
        void goToDependingOn() {
            var goTo = new GoToStatement(null, VAR_X,
                    List.of("PARA-1", "PARA-2", "PARA-3"), LOC);
            assertThat(goTo.targets()).hasSize(3);
            assertThat(goTo.dependingOn()).isEqualTo(VAR_X);
        }

        @Test
        @DisplayName("STOP RUN")
        void stopRun() {
            var stop = new StopStatement(StopStatement.StopType.RUN, null, LOC);
            assertThat(stop.type()).isEqualTo(StopStatement.StopType.RUN);
            assertThat(stop.accept(new TestStatementVisitor())).isEqualTo("stop");
        }

        @Test
        @DisplayName("EXIT types")
        void exitTypes() {
            for (var type : ExitStatement.ExitType.values()) {
                var exit = new ExitStatement(type, LOC);
                assertThat(exit.type()).isEqualTo(type);
            }
            assertThat(new ExitStatement(ExitStatement.ExitType.PROGRAM, LOC)
                    .accept(new TestStatementVisitor())).isEqualTo("exit");
        }
    }

    // ─── PERFORM Statement ────────────────────────────────

    @Nested
    @DisplayName("PerformStatement")
    class PerformTests {
        @Test
        @DisplayName("PERFORM simple paragraph call")
        void simplePerform() {
            var perf = new PerformStatement(PerformStatement.PerformType.SIMPLE,
                    "PROCESS-RECORD", null, null, null, null, null, List.of(), LOC);
            assertThat(perf.getType()).isEqualTo(PerformStatement.PerformType.SIMPLE);
            assertThat(perf.getTargetParagraph()).hasValue("PROCESS-RECORD");
            assertThat(perf.getThroughParagraph()).isEmpty();
            assertThat(perf.getTestPosition()).isEmpty();
        }

        @Test
        @DisplayName("PERFORM THRU")
        void performThru() {
            var perf = new PerformStatement(PerformStatement.PerformType.SIMPLE,
                    "PARA-A", "PARA-Z", null, null, null, null, List.of(), LOC);
            assertThat(perf.getThroughParagraph()).hasValue("PARA-Z");
        }

        @Test
        @DisplayName("PERFORM n TIMES")
        void performTimes() {
            var perf = new PerformStatement(PerformStatement.PerformType.TIMES,
                    "LOOP-PARA", null, LIT_10, null, null, null, List.of(), LOC);
            assertThat(perf.getTimes()).hasValue(LIT_10);
        }

        @Test
        @DisplayName("PERFORM UNTIL with TEST BEFORE")
        void performUntilTestBefore() {
            var condition = new BinaryOp(VAR_X, BinaryOp.Operator.GREATER_THAN, LIT_10, LOC);
            var perf = new PerformStatement(PerformStatement.PerformType.UNTIL,
                    "READ-LOOP", null, null, condition, null,
                    PerformStatement.TestPosition.BEFORE, List.of(), LOC);
            assertThat(perf.getUntilCondition()).hasValue(condition);
            assertThat(perf.getTestPosition()).hasValue(PerformStatement.TestPosition.BEFORE);
        }

        @Test
        @DisplayName("PERFORM UNTIL with TEST AFTER")
        void performUntilTestAfter() {
            var condition = new BinaryOp(VAR_X, BinaryOp.Operator.GREATER_THAN, LIT_10, LOC);
            var perf = new PerformStatement(PerformStatement.PerformType.UNTIL,
                    "READ-LOOP", null, null, condition, null,
                    PerformStatement.TestPosition.AFTER, List.of(), LOC);
            assertThat(perf.getTestPosition()).hasValue(PerformStatement.TestPosition.AFTER);
        }

        @Test
        @DisplayName("PERFORM VARYING")
        void performVarying() {
            var from = Literal.numeric(1, LOC);
            var by = Literal.numeric(1, LOC);
            var until = new BinaryOp(VAR_X, BinaryOp.Operator.GREATER_THAN, LIT_10, LOC);
            var varying = new PerformStatement.VaryingClause("WS-IDX", from, by, until, List.of());

            assertThat(varying.getVariable()).isEqualTo("WS-IDX");
            assertThat(varying.getFrom()).isEqualTo(from);
            assertThat(varying.getBy()).isEqualTo(by);
            assertThat(varying.getUntil()).isEqualTo(until);
            assertThat(varying.getAfter()).isEmpty();

            var perf = new PerformStatement(PerformStatement.PerformType.VARYING,
                    "LOOP", null, null, null, varying, null, List.of(), LOC);
            assertThat(perf.getVarying()).hasValue(varying);
        }

        @Test
        @DisplayName("PERFORM inline")
        void performInline() {
            var body = new MoveStatement(LIT_1, List.of(VAR_X), false, LOC);
            var perf = new PerformStatement(PerformStatement.PerformType.INLINE,
                    null, null, null, null, null, null, List.of(body), LOC);
            assertThat(perf.getInlineStatements()).hasSize(1);
            assertThat(perf.getTargetParagraph()).isEmpty();
        }

        @Test
        @DisplayName("all perform types defined")
        void performTypes() {
            assertThat(PerformStatement.PerformType.values()).hasSize(5);
        }

        @Test
        @DisplayName("test position enum")
        void testPositionEnum() {
            assertThat(PerformStatement.TestPosition.values()).hasSize(2);
        }

        @Test
        @DisplayName("type is required")
        void nullType() {
            assertThatThrownBy(() -> new PerformStatement(null,
                    null, null, null, null, null, null, List.of(), LOC))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("accepts visitor")
        void visitorAccept() {
            var perf = new PerformStatement(PerformStatement.PerformType.SIMPLE,
                    "P", null, null, null, null, null, List.of(), LOC);
            assertThat(perf.accept(new TestStatementVisitor())).isEqualTo("perform");
        }
    }

    // ─── I/O Statements ───────────────────────────────────

    @Nested
    @DisplayName("I/O Statements")
    class IOTests {
        @Test
        @DisplayName("OPEN statement")
        void openStatement() {
            var spec = new OpenStatement.FileSpec("CUSTOMER-FILE", OpenStatement.FileSpec.OpenMode.INPUT);
            var open = new OpenStatement(List.of(spec), LOC);
            assertThat(open.files()).hasSize(1);
            assertThat(open.files().get(0).fileName()).isEqualTo("CUSTOMER-FILE");
            assertThat(open.files().get(0).mode()).isEqualTo(OpenStatement.FileSpec.OpenMode.INPUT);
            assertThat(open.accept(new TestStatementVisitor())).isEqualTo("open");
        }

        @Test
        @DisplayName("CLOSE statement")
        void closeStatement() {
            var close = new CloseStatement(List.of("FILE-A", "FILE-B"), LOC);
            assertThat(close.fileNames()).hasSize(2);
            assertThat(close.accept(new TestStatementVisitor())).isEqualTo("close");
        }

        @Test
        @DisplayName("READ with AT END")
        void readStatement() {
            var atEnd = new MoveStatement(LIT_1, List.of(VAR_X), false, LOC);
            var read = new ReadStatement("INPUT-FILE", null, null,
                    List.of(atEnd), List.of(), List.of(), LOC);
            assertThat(read.fileName()).isEqualTo("INPUT-FILE");
            assertThat(read.atEnd()).hasSize(1);
            assertThat(read.accept(new TestStatementVisitor())).isEqualTo("read");
        }

        @Test
        @DisplayName("READ fileName is required")
        void readNullFileName() {
            assertThatThrownBy(() -> new ReadStatement(null, null, null,
                    List.of(), List.of(), List.of(), LOC))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("WRITE statement")
        void writeStatement() {
            var write = new WriteStatement("OUTPUT-REC", LIT_1, List.of(), LOC);
            assertThat(write.recordName()).isEqualTo("OUTPUT-REC");
            assertThat(write.from()).isEqualTo(LIT_1);
            assertThat(write.accept(new TestStatementVisitor())).isEqualTo("write");
        }

        @Test
        @DisplayName("REWRITE statement")
        void rewriteStatement() {
            var rewrite = new RewriteStatement("MASTER-REC", VAR_X, List.of(), LOC);
            assertThat(rewrite.recordName()).isEqualTo("MASTER-REC");
            assertThat(rewrite.accept(new TestStatementVisitor())).isEqualTo("rewrite");
        }

        @Test
        @DisplayName("DELETE statement")
        void deleteStatement() {
            var delete = new DeleteStatement("INDEXED-FILE", List.of(), LOC);
            assertThat(delete.fileName()).isEqualTo("INDEXED-FILE");
            assertThat(delete.accept(new TestStatementVisitor())).isEqualTo("delete");
        }

        @Test
        @DisplayName("START statement")
        void startStatement() {
            var start = new StartStatement("VSAM-FILE", VAR_X, List.of(), LOC);
            assertThat(start.fileName()).isEqualTo("VSAM-FILE");
            assertThat(start.key()).isEqualTo(VAR_X);
            assertThat(start.accept(new TestStatementVisitor())).isEqualTo("start");
        }

        @Test
        @DisplayName("all OpenMode types defined")
        void openModes() {
            assertThat(OpenStatement.FileSpec.OpenMode.values()).hasSize(4);
        }
    }

    // ─── Misc Statements ──────────────────────────────────

    @Nested
    @DisplayName("Misc Statements")
    class MiscTests {
        @Test
        @DisplayName("DISPLAY statement")
        void displayStatement() {
            var display = new DisplayStatement(List.of(Literal.string("HELLO", LOC)), "CONSOLE", LOC);
            assertThat(display.items()).hasSize(1);
            assertThat(display.upon()).isEqualTo("CONSOLE");
            assertThat(display.accept(new TestStatementVisitor())).isEqualTo("display");
        }

        @Test
        @DisplayName("ACCEPT statement")
        void acceptStatement() {
            var accept = new AcceptStatement(VAR_X, "DATE", LOC);
            assertThat(accept.target()).isEqualTo(VAR_X);
            assertThat(accept.from()).isEqualTo("DATE");
            assertThat(accept.accept(new TestStatementVisitor())).isEqualTo("accept");
        }

        @Test
        @DisplayName("CALL statement with USING arguments")
        void callStatement() {
            var call = new CallStatement(Literal.string("SUBPROGRAM", LOC),
                    List.of(new CallArgument(VAR_X, null),
                            new CallArgument(VAR_Y, null)),
                    null, List.of(), LOC);
            assertThat(call.programName()).isInstanceOf(Literal.class);
            assertThat(call.arguments()).hasSize(2);
            assertThat(call.arguments().get(0).passingMode())
                    .isEqualTo(CallArgument.PassingMode.BY_REFERENCE);
            assertThat(call.accept(new TestStatementVisitor())).isEqualTo("call");
        }

        @Test
        @DisplayName("CALL with BY VALUE argument")
        void callByValue() {
            var call = new CallStatement(Literal.string("PGM", LOC),
                    List.of(new CallArgument(VAR_X, CallArgument.PassingMode.BY_VALUE)),
                    null, List.of(), LOC);
            assertThat(call.arguments().get(0).passingMode())
                    .isEqualTo(CallArgument.PassingMode.BY_VALUE);
        }

        @Test
        @DisplayName("CallArgument passing modes")
        void passingModes() {
            assertThat(CallArgument.PassingMode.values()).hasSize(3);
        }

        @Test
        @DisplayName("INSPECT statement")
        void inspectStatement() {
            var inspect = new InspectStatement(
                    VAR_X,
                    InspectStatement.InspectOp.TALLYING,
                    new InspectStatement.TallyingClause(VAR_Y, List.of(
                            new InspectStatement.TallyFor(InspectStatement.TallyMode.ALL, VAR_X, null, null))),
                    List.of(),
                    null,
                    LOC);
            assertThat(inspect.target()).isEqualTo(VAR_X);
            assertThat(inspect.operation()).isEqualTo(InspectStatement.InspectOp.TALLYING);
            assertThat(inspect.tallyingClause()).isNotNull();
            assertThat(inspect.accept(new TestStatementVisitor())).isEqualTo("inspect");
        }

        @Test
        @DisplayName("STRING statement")
        void stringStatement() {
            var str = new StringStatement(
                    List.of(Literal.string("A", LOC), Literal.string("B", LOC)),
                    VAR_X, null, List.of(), LOC);
            assertThat(str.sources()).hasSize(2);
            assertThat(str.into()).isEqualTo(VAR_X);
            assertThat(str.accept(new TestStatementVisitor())).isEqualTo("string");
        }

        @Test
        @DisplayName("UNSTRING statement")
        void unstringStatement() {
            var unstr = new UnstringStatement(VAR_X,
                    List.of(Literal.string(",", LOC)),
                    List.of(VAR_Y), null, List.of(), LOC);
            assertThat(unstr.source()).isEqualTo(VAR_X);
            assertThat(unstr.delimiters()).hasSize(1);
            assertThat(unstr.into()).hasSize(1);
            assertThat(unstr.accept(new TestStatementVisitor())).isEqualTo("unstring");
        }

        @Test
        @DisplayName("SEARCH statement")
        void searchStatement() {
            var whenClause = new SearchStatement.WhenClause(
                    new BinaryOp(VAR_X, BinaryOp.Operator.EQUAL, LIT_1, LOC),
                    List.of(new MoveStatement(LIT_1, List.of(VAR_Y), false, LOC)));
            var search = new SearchStatement("RATE-TABLE", false, null,
                    List.of(), List.of(whenClause), LOC);
            assertThat(search.tableName()).isEqualTo("RATE-TABLE");
            assertThat(search.searchAll()).isFalse();
            assertThat(search.whenClauses()).hasSize(1);
            assertThat(search.accept(new TestStatementVisitor())).isEqualTo("search");
        }

        @Test
        @DisplayName("SEARCH ALL (binary search)")
        void searchAll() {
            var whenClause = new SearchStatement.WhenClause(
                    new BinaryOp(VAR_X, BinaryOp.Operator.EQUAL, LIT_1, LOC),
                    List.of(new MoveStatement(LIT_1, List.of(VAR_Y), false, LOC)));
            var search = new SearchStatement("RATE-TABLE", true, null,
                    List.of(), List.of(whenClause), LOC);
            assertThat(search.searchAll()).isTrue();
        }

        @Test
        @DisplayName("SET statement")
        void setStatement() {
            var set = new SetStatement(List.of(VAR_X), LIT_1, LOC);
            assertThat(set.targets()).hasSize(1);
            assertThat(set.value()).isEqualTo(LIT_1);
            assertThat(set.accept(new TestStatementVisitor())).isEqualTo("set");
        }

        @Test
        @DisplayName("INITIALIZE statement")
        void initializeStatement() {
            var init = new InitializeStatement(List.of(VAR_X, VAR_Y), LOC);
            assertThat(init.targets()).hasSize(2);
            assertThat(init.accept(new TestStatementVisitor())).isEqualTo("initialize");
        }
    }

    // ─── JSON Serialization ───────────────────────────────

    @Nested
    @DisplayName("JSON Serialization")
    class JsonTests {
        @Test
        @DisplayName("PERFORM JSON round-trip")
        void performJson() throws Exception {
            var perf = new PerformStatement(PerformStatement.PerformType.TIMES,
                    "LOOP", null, LIT_10, null, null, null, List.of(), LOC);
            String json = mapper.writeValueAsString(perf);
            assertThat(json).contains("\"type\":\"perform\"");
            Statement deserialized = mapper.readValue(json, Statement.class);
            assertThat(deserialized).isInstanceOf(PerformStatement.class);
        }

        @Test
        @DisplayName("EVALUATE JSON round-trip")
        void evaluateJson() throws Exception {
            var eval = new EvaluateStatement(List.of(VAR_X), List.of(), List.of(), LOC);
            String json = mapper.writeValueAsString(eval);
            Statement deserialized = mapper.readValue(json, Statement.class);
            assertThat(deserialized).isInstanceOf(EvaluateStatement.class);
        }

        @Test
        @DisplayName("DISPLAY JSON round-trip")
        void displayJson() throws Exception {
            var display = new DisplayStatement(List.of(Literal.string("HI", LOC)), null, LOC);
            String json = mapper.writeValueAsString(display);
            Statement deserialized = mapper.readValue(json, Statement.class);
            assertThat(deserialized).isInstanceOf(DisplayStatement.class);
        }

        @Test
        @DisplayName("CALL JSON round-trip")
        void callJson() throws Exception {
            var call = new CallStatement(Literal.string("PGM", LOC), List.of(), null, List.of(), LOC);
            String json = mapper.writeValueAsString(call);
            Statement deserialized = mapper.readValue(json, Statement.class);
            assertThat(deserialized).isInstanceOf(CallStatement.class);
        }

        @Test
        @DisplayName("nested statement tree JSON round-trip")
        void nestedJson() throws Exception {
            var innerMove = new MoveStatement(LIT_1, List.of(VAR_X), false, LOC);
            var cond = new BinaryOp(VAR_X, BinaryOp.Operator.EQUAL, LIT_1, LOC);
            var ifStmt = new IfStatement(cond, List.of(innerMove), List.of(), LOC);
            String json = mapper.writeValueAsString(ifStmt);
            Statement deserialized = mapper.readValue(json, Statement.class);
            assertThat(deserialized).isInstanceOf(IfStatement.class);
            var deserializedIf = (IfStatement) deserialized;
            assertThat(deserializedIf.getThenBranch()).hasSize(1);
            assertThat(deserializedIf.getThenBranch().get(0)).isInstanceOf(MoveStatement.class);
        }
    }

    // ─── StatementVisitor pattern ─────────────────────────

    @Nested
    @DisplayName("StatementVisitor")
    class VisitorTests {
        @Test
        @DisplayName("visitor dispatches to correct method for each statement type")
        void allDispatches() {
            var visitor = new TestStatementVisitor();

            assertThat(new MoveStatement(LIT_1, List.of(), false, LOC).accept(visitor)).isEqualTo("move");
            assertThat(new ComputeStatement(List.of(), new BinaryOp(LIT_1, BinaryOp.Operator.ADD, LIT_1, LOC), false, null, List.of(), LOC).accept(visitor)).isEqualTo("compute");
            assertThat(new AddStatement(List.of(), List.of(), List.of(), false, null, List.of(), LOC).accept(visitor)).isEqualTo("add");
            assertThat(new SubtractStatement(List.of(), List.of(), List.of(), false, null, List.of(), LOC).accept(visitor)).isEqualTo("subtract");
            assertThat(new MultiplyStatement(LIT_1, LIT_1, List.of(), false, null, List.of(), LOC).accept(visitor)).isEqualTo("multiply");
            assertThat(new DivideStatement(LIT_1, LIT_1, null, List.of(), null, false, null, List.of(), LOC).accept(visitor)).isEqualTo("divide");
            assertThat(new StopStatement(StopStatement.StopType.RUN, null, LOC).accept(visitor)).isEqualTo("stop");
            assertThat(new ExitStatement(ExitStatement.ExitType.PROGRAM, LOC).accept(visitor)).isEqualTo("exit");
            assertThat(new DisplayStatement(List.of(), null, LOC).accept(visitor)).isEqualTo("display");
            assertThat(new AcceptStatement(VAR_X, null, LOC).accept(visitor)).isEqualTo("accept");
            assertThat(new InspectStatement(
                    VAR_X,
                    InspectStatement.InspectOp.REPLACING,
                    null,
                    List.of(new InspectStatement.ReplacingClause(
                            InspectStatement.ReplaceMode.ALL,
                            VAR_Y,
                            LIT_1,
                            null,
                            null)),
                    null,
                    LOC).accept(visitor)).isEqualTo("inspect");
            assertThat(new SetStatement(List.of(), null, LOC).accept(visitor)).isEqualTo("set");
            assertThat(new InitializeStatement(List.of(), LOC).accept(visitor)).isEqualTo("initialize");
        }
    }

    // ─── Test visitor implementation ──────────────────────

    private static class TestStatementVisitor implements StatementVisitor<String> {
        @Override public String visitMove(MoveStatement stmt) { return "move"; }
        @Override public String visitCompute(ComputeStatement stmt) { return "compute"; }
        @Override public String visitAdd(AddStatement stmt) { return "add"; }
        @Override public String visitSubtract(SubtractStatement stmt) { return "subtract"; }
        @Override public String visitMultiply(MultiplyStatement stmt) { return "multiply"; }
        @Override public String visitDivide(DivideStatement stmt) { return "divide"; }
        @Override public String visitIf(IfStatement stmt) { return "if"; }
        @Override public String visitEvaluate(EvaluateStatement stmt) { return "evaluate"; }
        @Override public String visitPerform(PerformStatement stmt) { return "perform"; }
        @Override public String visitGoTo(GoToStatement stmt) { return "goto"; }
        @Override public String visitStop(StopStatement stmt) { return "stop"; }
        @Override public String visitExit(ExitStatement stmt) { return "exit"; }
        @Override public String visitDisplay(DisplayStatement stmt) { return "display"; }
        @Override public String visitAccept(AcceptStatement stmt) { return "accept"; }
        @Override public String visitOpen(OpenStatement stmt) { return "open"; }
        @Override public String visitClose(CloseStatement stmt) { return "close"; }
        @Override public String visitRead(ReadStatement stmt) { return "read"; }
        @Override public String visitWrite(WriteStatement stmt) { return "write"; }
        @Override public String visitRewrite(RewriteStatement stmt) { return "rewrite"; }
        @Override public String visitDelete(DeleteStatement stmt) { return "delete"; }
        @Override public String visitStart(StartStatement stmt) { return "start"; }
        @Override public String visitCall(CallStatement stmt) { return "call"; }
        @Override public String visitInspect(InspectStatement stmt) { return "inspect"; }
        @Override public String visitString(StringStatement stmt) { return "string"; }
        @Override public String visitUnstring(UnstringStatement stmt) { return "unstring"; }
        @Override public String visitSearch(SearchStatement stmt) { return "search"; }
        @Override public String visitSet(SetStatement stmt) { return "set"; }
        @Override public String visitInitialize(InitializeStatement stmt) { return "initialize"; }
    }
}
