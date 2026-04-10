/*
 * JavaStatementVisitor - Generates Java statements from COBOL IR statements
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.codegen;

import com.sekacorn.corn.ir.ConditionName;
import com.sekacorn.corn.ir.DataItem;
import com.sekacorn.corn.ir.expr.Expression;
import com.sekacorn.corn.ir.expr.Literal;
import com.sekacorn.corn.ir.expr.VariableRef;
import com.sekacorn.corn.ir.stmt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implements StatementVisitor to generate Java source code from COBOL statements.
 * Each visit method appends generated lines to the shared CodeBuffer and returns
 * null (the code is accumulated in the buffer rather than returned as strings).
 */
public final class JavaStatementVisitor implements StatementVisitor<Void> {

    private final JavaExpressionVisitor exprVisitor;
    private final Map<String, DataItem> dataItemMap;
    private final Map<String, String> fileStatusMap;
    private final CodeBuffer buffer;
    private int tempVarCounter = 0;

    public JavaStatementVisitor(JavaExpressionVisitor exprVisitor,
                                Map<String, DataItem> dataItemMap,
                                Map<String, String> fileStatusMap,
                                CodeBuffer buffer) {
        this.exprVisitor = exprVisitor;
        this.dataItemMap = dataItemMap;
        this.fileStatusMap = fileStatusMap;
        this.buffer = buffer;
    }

    /**
     * Generate Java code for a list of statements.
     */
    public void generateStatements(List<Statement> statements) {
        if (statements == null) return;
        for (Statement stmt : statements) {
            stmt.accept(this);
        }
    }

    // ---- Data Movement ----

    @Override
    public Void visitMove(MoveStatement stmt) {
        String source = stmt.getSource().accept(exprVisitor);
        for (Expression target : stmt.getTargets()) {
            String targetName = target.accept(exprVisitor);
            if (isStringField(target)) {
                buffer.addImport("com.sekacorn.corn.runtime.CobolString");
                int len = getFieldLength(target);
                buffer.line("%s = CobolString.move(%s, %d, CobolString.Justification.LEFT);",
                        targetName, source, len);
            } else {
                buffer.line("%s = %s;", targetName, source);
            }
        }
        return null;
    }

    // ---- Arithmetic ----

    @Override
    public Void visitAdd(AddStatement stmt) {
        buffer.addImport("com.sekacorn.corn.runtime.CobolMath");
        buffer.addImport("com.sekacorn.corn.runtime.ArithmeticContext");
        String operands = stmt.operands().stream()
                .map(e -> e.accept(exprVisitor))
                .collect(Collectors.joining(".add("));
        if (stmt.operands().size() > 1) {
            operands += ")".repeat(stmt.operands().size() - 1);
        }
        String ctx = arithmeticContext(stmt.rounded(), stmt.roundMode());
        boolean hasSizeError = stmt.onSizeError() != null && !stmt.onSizeError().isEmpty();
        String lastResult = null;

        if (!stmt.giving().isEmpty()) {
            String toValues = stmt.to().stream()
                    .map(e -> e.accept(exprVisitor))
                    .collect(Collectors.joining(".add("));
            if (stmt.to().size() > 1) {
                toValues += ")".repeat(stmt.to().size() - 1);
            }
            String allValues = operands + ".add(" + toValues + ")";
            for (Expression giving : stmt.giving()) {
                String target = giving.accept(exprVisitor);
                if (hasSizeError) {
                    lastResult = nextTemp("arithResult");
                    buffer.line("CobolMath.Result %s = CobolMath.compute(%s, %s);",
                            lastResult, allValues, ctx);
                    buffer.line("%s = %s.getValue();", target, lastResult);
                } else {
                    buffer.line("%s = CobolMath.compute(%s, %s).getValue();",
                            target, allValues, ctx);
                }
            }
        } else {
            for (Expression to : stmt.to()) {
                String target = to.accept(exprVisitor);
                if (hasSizeError) {
                    lastResult = nextTemp("arithResult");
                    buffer.line("CobolMath.Result %s = CobolMath.add(%s, %s, %s);",
                            lastResult, target, operands, ctx);
                    buffer.line("%s = %s.getValue();", target, lastResult);
                } else {
                    buffer.line("%s = CobolMath.add(%s, %s, %s).getValue();",
                            target, target, operands, ctx);
                }
            }
        }
        generateOnSizeError(stmt.onSizeError(), lastResult);
        return null;
    }

    @Override
    public Void visitSubtract(SubtractStatement stmt) {
        buffer.addImport("com.sekacorn.corn.runtime.CobolMath");
        buffer.addImport("com.sekacorn.corn.runtime.ArithmeticContext");
        String operands = stmt.operands().stream()
                .map(e -> e.accept(exprVisitor))
                .collect(Collectors.joining(".add("));
        if (stmt.operands().size() > 1) {
            operands += ")".repeat(stmt.operands().size() - 1);
        }
        String ctx = arithmeticContext(stmt.rounded(), stmt.roundMode());
        boolean hasSizeError = stmt.onSizeError() != null && !stmt.onSizeError().isEmpty();
        String lastResult = null;

        if (!stmt.giving().isEmpty()) {
            for (Expression giving : stmt.giving()) {
                String from = stmt.from().isEmpty() ? "BigDecimal.ZERO"
                        : stmt.from().get(0).accept(exprVisitor);
                String target = giving.accept(exprVisitor);
                if (hasSizeError) {
                    lastResult = nextTemp("arithResult");
                    buffer.line("CobolMath.Result %s = CobolMath.subtract(%s, %s, %s);",
                            lastResult, from, operands, ctx);
                    buffer.line("%s = %s.getValue();", target, lastResult);
                } else {
                    buffer.line("%s = CobolMath.subtract(%s, %s, %s).getValue();",
                            target, from, operands, ctx);
                }
            }
        } else {
            for (Expression from : stmt.from()) {
                String target = from.accept(exprVisitor);
                if (hasSizeError) {
                    lastResult = nextTemp("arithResult");
                    buffer.line("CobolMath.Result %s = CobolMath.subtract(%s, %s, %s);",
                            lastResult, target, operands, ctx);
                    buffer.line("%s = %s.getValue();", target, lastResult);
                } else {
                    buffer.line("%s = CobolMath.subtract(%s, %s, %s).getValue();",
                            target, target, operands, ctx);
                }
            }
        }
        generateOnSizeError(stmt.onSizeError(), lastResult);
        return null;
    }

    @Override
    public Void visitMultiply(MultiplyStatement stmt) {
        buffer.addImport("com.sekacorn.corn.runtime.CobolMath");
        buffer.addImport("com.sekacorn.corn.runtime.ArithmeticContext");
        String op1 = stmt.operand1().accept(exprVisitor);
        String op2 = stmt.operand2().accept(exprVisitor);
        String ctx = arithmeticContext(stmt.rounded(), stmt.roundMode());
        boolean hasSizeError = stmt.onSizeError() != null && !stmt.onSizeError().isEmpty();
        String lastResult = null;

        if (!stmt.giving().isEmpty()) {
            for (Expression giving : stmt.giving()) {
                String target = giving.accept(exprVisitor);
                if (hasSizeError) {
                    lastResult = nextTemp("arithResult");
                    buffer.line("CobolMath.Result %s = CobolMath.multiply(%s, %s, %s);",
                            lastResult, op1, op2, ctx);
                    buffer.line("%s = %s.getValue();", target, lastResult);
                } else {
                    buffer.line("%s = CobolMath.multiply(%s, %s, %s).getValue();",
                            target, op1, op2, ctx);
                }
            }
        } else {
            if (hasSizeError) {
                lastResult = nextTemp("arithResult");
                buffer.line("CobolMath.Result %s = CobolMath.multiply(%s, %s, %s);",
                        lastResult, op1, op2, ctx);
                buffer.line("%s = %s.getValue();", op2, lastResult);
            } else {
                buffer.line("%s = CobolMath.multiply(%s, %s, %s).getValue();",
                        op2, op1, op2, ctx);
            }
        }
        generateOnSizeError(stmt.onSizeError(), lastResult);
        return null;
    }

    @Override
    public Void visitDivide(DivideStatement stmt) {
        buffer.addImport("com.sekacorn.corn.runtime.CobolMath");
        buffer.addImport("com.sekacorn.corn.runtime.ArithmeticContext");
        String dividend = stmt.dividend().accept(exprVisitor);
        String divisor = stmt.divisor().accept(exprVisitor);
        String ctx = arithmeticContext(stmt.rounded(), stmt.roundMode());
        boolean hasSizeError = stmt.onSizeError() != null && !stmt.onSizeError().isEmpty();
        String lastResult = null;

        if (stmt.remainder() != null) {
            String divRes = nextTemp("divideResult");
            buffer.line("CobolMath.DivideResult %s = CobolMath.divideWithRemainder(%s, %s, %s);",
                    divRes, dividend, divisor, ctx);
            if (!stmt.giving().isEmpty()) {
                for (Expression giving : stmt.giving()) {
                    String target = giving.accept(exprVisitor);
                    buffer.line("%s = %s.getValue();", target, divRes);
                }
            } else if (stmt.into() != null) {
                String into = stmt.into().accept(exprVisitor);
                buffer.line("%s = %s.getValue();", into, divRes);
            }
            String rem = stmt.remainder().accept(exprVisitor);
            buffer.line("%s = %s.getRemainder();", rem, divRes);
            if (hasSizeError) {
                lastResult = divRes;
            }
        } else {
            if (!stmt.giving().isEmpty()) {
                for (Expression giving : stmt.giving()) {
                    String target = giving.accept(exprVisitor);
                    if (hasSizeError) {
                        lastResult = nextTemp("arithResult");
                        buffer.line("CobolMath.Result %s = CobolMath.divide(%s, %s, %s);",
                                lastResult, dividend, divisor, ctx);
                        buffer.line("%s = %s.getValue();", target, lastResult);
                    } else {
                        buffer.line("%s = CobolMath.divide(%s, %s, %s).getValue();",
                                target, dividend, divisor, ctx);
                    }
                }
            } else if (stmt.into() != null) {
                String into = stmt.into().accept(exprVisitor);
                if (hasSizeError) {
                    lastResult = nextTemp("arithResult");
                    buffer.line("CobolMath.Result %s = CobolMath.divide(%s, %s, %s);",
                            lastResult, dividend, divisor, ctx);
                    buffer.line("%s = %s.getValue();", into, lastResult);
                } else {
                    buffer.line("%s = CobolMath.divide(%s, %s, %s).getValue();",
                            into, dividend, divisor, ctx);
                }
            }
        }
        generateOnSizeError(stmt.onSizeError(), lastResult);
        return null;
    }

    @Override
    public Void visitCompute(ComputeStatement stmt) {
        buffer.addImport("com.sekacorn.corn.runtime.CobolMath");
        buffer.addImport("com.sekacorn.corn.runtime.ArithmeticContext");
        String expr = stmt.expression().accept(exprVisitor);
        String ctx = arithmeticContext(stmt.rounded(), stmt.roundMode());
        boolean hasSizeError = stmt.onSizeError() != null && !stmt.onSizeError().isEmpty();
        String lastResult = null;

        for (Expression target : stmt.targets()) {
            String targetName = target.accept(exprVisitor);
            if (hasSizeError) {
                lastResult = nextTemp("arithResult");
                buffer.line("CobolMath.Result %s = CobolMath.compute(%s, %s);",
                        lastResult, expr, ctx);
                buffer.line("%s = %s.getValue();", targetName, lastResult);
            } else {
                buffer.line("%s = CobolMath.compute(%s, %s).getValue();",
                        targetName, expr, ctx);
            }
        }
        generateOnSizeError(stmt.onSizeError(), lastResult);
        return null;
    }

    // ---- Control Flow ----

    @Override
    public Void visitIf(IfStatement stmt) {
        String condition = stmt.getCondition().accept(exprVisitor);
        buffer.openBlock("if (" + condition + ")");
        generateStatements(stmt.getThenBranch());
        if (stmt.hasElse()) {
            buffer.closeBlockWith("else {");
            buffer.indent();
            generateStatements(stmt.getElseBranch());
            buffer.closeBlock();
        } else {
            buffer.closeBlock();
        }
        return null;
    }

    @Override
    public Void visitEvaluate(EvaluateStatement stmt) {
        // Build subject expressions for comparison
        List<String> subjects = stmt.subjects().stream()
                .map(e -> e.accept(exprVisitor))
                .toList();

        boolean first = true;
        for (EvaluateStatement.WhenClause when : stmt.whenClauses()) {
            String conditions;
            if (subjects.isEmpty()) {
                // EVALUATE TRUE — conditions are boolean expressions
                conditions = when.conditions().stream()
                        .map(e -> e.accept(exprVisitor))
                        .collect(Collectors.joining(" && "));
            } else {
                // EVALUATE subject — compare each WHEN value against the subject
                List<String> comparisons = new ArrayList<>();
                for (int i = 0; i < when.conditions().size(); i++) {
                    String subject = subjects.get(i < subjects.size() ? i : 0);
                    Expression cond = when.conditions().get(i);
                    if (isEvaluateAny(cond)) {
                        comparisons.add("true");
                        continue;
                    }
                    String condValue = cond.accept(exprVisitor);
                    // Generate appropriate comparison
                    if (isNumericField(subject) || condValue.contains("BigDecimal")) {
                        comparisons.add(subject + ".compareTo(" + condValue + ") == 0");
                    } else {
                        comparisons.add(subject + ".equals(" + condValue + ")");
                    }
                }
                conditions = String.join(" && ", comparisons);
            }
            if (conditions.isEmpty()) conditions = "true";

            if (first) {
                buffer.openBlock("if (" + conditions + ")");
                first = false;
            } else {
                buffer.closeBlockWith("else if (" + conditions + ") {");
                buffer.indent();
            }
            generateStatements(when.statements());
        }

        if (!stmt.whenOther().isEmpty()) {
            buffer.closeBlockWith("else {");
            buffer.indent();
            generateStatements(stmt.whenOther());
        }

        if (!stmt.whenClauses().isEmpty() || !stmt.whenOther().isEmpty()) {
            buffer.closeBlock();
        }
        return null;
    }

    /**
     * Check if a Java expression refers to a numeric (BigDecimal) value.
     */
    private boolean isNumericField(String javaExpr) {
        for (var entry : dataItemMap.entrySet()) {
            String fieldName = JavaNameMapper.toFieldName(entry.getKey());
            if (fieldName.equals(javaExpr) && entry.getValue().getPicture().isPresent()) {
                return entry.getValue().getPicture().get().getCategory()
                        == com.sekacorn.corn.ir.Picture.PictureCategory.NUMERIC;
            }
        }
        return false;
    }

    @Override
    public Void visitPerform(PerformStatement stmt) {
        switch (stmt.getType()) {
            case SIMPLE -> {
                stmt.getTargetParagraph().ifPresent(target ->
                        buffer.line("%s();", JavaCodeGenerator.toParagraphMethodName(target)));
            }
            case TIMES -> {
                buffer.addImport("java.math.BigDecimal");
                String times = stmt.getTimes().map(t -> t.accept(exprVisitor))
                        .orElse("new BigDecimal(\"1\")");
                if (stmt.getTargetParagraph().isPresent()) {
                    buffer.openBlock("for (int i = 0; i < (" + times + ").intValue(); i++)");
                    buffer.line("%s();", JavaCodeGenerator.toParagraphMethodName(stmt.getTargetParagraph().get()));
                    buffer.closeBlock();
                } else {
                    buffer.openBlock("for (int i = 0; i < (" + times + ").intValue(); i++)");
                    generateStatements(stmt.getInlineStatements());
                    buffer.closeBlock();
                }
            }
            case UNTIL -> {
                String condition = stmt.getUntilCondition()
                        .map(c -> c.accept(exprVisitor)).orElse("false");
                boolean testAfter = stmt.getTestPosition()
                        .map(tp -> tp == PerformStatement.TestPosition.AFTER).orElse(false);

                if (testAfter) {
                    buffer.openBlock("do");
                } else {
                    buffer.openBlock("while (!(" + condition + "))");
                }

                if (stmt.getTargetParagraph().isPresent()) {
                    buffer.line("%s();", JavaCodeGenerator.toParagraphMethodName(stmt.getTargetParagraph().get()));
                } else {
                    generateStatements(stmt.getInlineStatements());
                }

                if (testAfter) {
                    buffer.closeBlockWith("while (!(" + condition + "));");
                } else {
                    buffer.closeBlock();
                }
            }
            case VARYING -> {
                stmt.getVarying().ifPresent(vc -> generateVaryingLoop(vc, stmt));
            }
            case INLINE -> {
                buffer.line("// PERFORM inline");
                generateStatements(stmt.getInlineStatements());
            }
        }
        return null;
    }

    private void generateVaryingLoop(PerformStatement.VaryingClause vc, PerformStatement stmt) {
        String varName = JavaNameMapper.toFieldName(vc.getVariable());
        String from = vc.getFrom().accept(exprVisitor);
        String by = vc.getBy() != null ? vc.getBy().accept(exprVisitor) : "new BigDecimal(\"1\")";
        String until = vc.getUntil().accept(exprVisitor);

        buffer.line("%s = %s;", varName, from);
        buffer.openBlock("while (!(" + until + "))");

        if (stmt.getTargetParagraph().isPresent()) {
            buffer.line("%s();", JavaCodeGenerator.toParagraphMethodName(stmt.getTargetParagraph().get()));
        } else {
            generateStatements(stmt.getInlineStatements());
        }

        buffer.line("%s = %s.add(%s);", varName, varName, by);
        buffer.closeBlock();
    }

    @Override
    public Void visitGoTo(GoToStatement stmt) {
        buffer.line("%s();", JavaCodeGenerator.toParagraphMethodName(stmt.targetParagraph()));
        buffer.line("return;");
        return null;
    }

    @Override
    public Void visitStop(StopStatement stmt) {
        if (stmt.type() == StopStatement.StopType.LITERAL && stmt.message() != null) {
            String msg = stmt.message().accept(exprVisitor);
            buffer.line("System.out.println(%s);", msg);
        }
        buffer.line("return;");
        return null;
    }

    @Override
    public Void visitExit(ExitStatement stmt) {
        buffer.line("return;");
        return null;
    }

    // ---- I/O ----

    @Override
    public Void visitDisplay(DisplayStatement stmt) {
        if (stmt.items().isEmpty()) {
            buffer.line("System.out.println();");
        } else if (stmt.items().size() == 1) {
            String item = stmt.items().get(0).accept(exprVisitor);
            buffer.line("System.out.println(%s);", item);
        } else {
            String items = stmt.items().stream()
                    .map(e -> "String.valueOf(" + e.accept(exprVisitor) + ")")
                    .collect(Collectors.joining(" + "));
            buffer.line("System.out.println(%s);", items);
        }
        return null;
    }

    @Override
    public Void visitAccept(AcceptStatement stmt) {
        String target = stmt.target().accept(exprVisitor);
        if (stmt.from() != null) {
            switch (stmt.from().toUpperCase()) {
                case "DATE" -> {
                    buffer.addImport("java.time.LocalDate");
                    buffer.addImport("java.time.format.DateTimeFormatter");
                    buffer.line("%s = LocalDate.now().format(DateTimeFormatter.ofPattern(\"yyMMdd\"));", target);
                }
                case "DAY" -> {
                    buffer.addImport("java.time.LocalDate");
                    buffer.addImport("java.time.format.DateTimeFormatter");
                    buffer.line("%s = LocalDate.now().format(DateTimeFormatter.ofPattern(\"yyDDD\"));", target);
                }
                case "TIME" -> {
                    buffer.addImport("java.time.LocalTime");
                    buffer.addImport("java.time.format.DateTimeFormatter");
                    buffer.line("%s = LocalTime.now().format(DateTimeFormatter.ofPattern(\"HHmmssSSS\")).substring(0, 8);", target);
                }
                default -> {
                    addScannerField();
                    buffer.line("%s = __scanner.nextLine();", target);
                }
            }
        } else {
            addScannerField();
            buffer.line("%s = __scanner.nextLine();", target);
        }
        return null;
    }

    // ---- File I/O ----

    @Override
    public Void visitOpen(OpenStatement stmt) {
        buffer.addImport("com.sekacorn.corn.runtime.CobolFile");
        for (OpenStatement.FileSpec spec : stmt.files()) {
            String fileName = JavaNameMapper.toFieldName(spec.fileName());
            String mode = switch (spec.mode()) {
                case INPUT -> "CobolFile.OpenMode.INPUT";
                case OUTPUT -> "CobolFile.OpenMode.OUTPUT";
                case I_O -> "CobolFile.OpenMode.I_O";
                case EXTEND -> "CobolFile.OpenMode.EXTEND";
            };
            buffer.line("%s.open(%s);", fileName, mode);
            updateFileStatus(spec.fileName(), fileName + ".getStatus().getCode()");
        }
        return null;
    }

    @Override
    public Void visitClose(CloseStatement stmt) {
        buffer.addImport("com.sekacorn.corn.runtime.CobolFile");
        for (String fileName : stmt.fileNames()) {
            String javaFileName = JavaNameMapper.toFieldName(fileName);
            buffer.line("%s.close();", javaFileName);
            updateFileStatus(fileName, javaFileName + ".getStatus().getCode()");
        }
        return null;
    }

    @Override
    public Void visitRead(ReadStatement stmt) {
        buffer.addImport("com.sekacorn.corn.runtime.CobolFile");
        String file = JavaNameMapper.toFieldName(stmt.fileName());
        String resultVar = nextTemp("readResult");
        buffer.line("CobolFile.Result<String> %s = %s.read();", resultVar, file);
        updateFileStatus(stmt.fileName(), resultVar + ".status().getCode()");

        if (stmt.into() != null) {
            String into = stmt.into().accept(exprVisitor);
            buffer.openBlock("if (" + resultVar + ".getRecord().isPresent())");
            buffer.line("%s = %s.getRecord().get();", into, resultVar);
            buffer.closeBlock();
        }

        if (!stmt.atEnd().isEmpty()) {
            buffer.openBlock("if (" + resultVar + ".isAtEnd())");
            generateStatements(stmt.atEnd());
            if (!stmt.notAtEnd().isEmpty()) {
                buffer.closeBlockWith("else {");
                buffer.indent();
                generateStatements(stmt.notAtEnd());
                buffer.closeBlock();
            } else {
                buffer.closeBlock();
            }
        }
        return null;
    }

    @Override
    public Void visitWrite(WriteStatement stmt) {
        buffer.addImport("com.sekacorn.corn.runtime.CobolFile");
        String record = JavaNameMapper.toFieldName(stmt.recordName());
        String recordFile = record + "File";
        if (stmt.from() != null) {
            String from = stmt.from().accept(exprVisitor);
            buffer.line("%s.write(String.valueOf(%s));", recordFile, from);
        } else {
            buffer.line("%s.write(String.valueOf(%s));", recordFile, record);
        }
        updateFileStatus(stmt.recordName(), recordFile + ".getStatus().getCode()");
        return null;
    }

    @Override
    public Void visitRewrite(RewriteStatement stmt) {
        buffer.addImport("com.sekacorn.corn.runtime.CobolFile");
        String record = JavaNameMapper.toFieldName(stmt.recordName());
        String recordFile = record + "File";
        if (stmt.from() != null) {
            String from = stmt.from().accept(exprVisitor);
            buffer.line("%s.rewrite(String.valueOf(%s));", recordFile, from);
        } else {
            buffer.line("%s.rewrite(String.valueOf(%s));", recordFile, record);
        }
        updateFileStatus(stmt.recordName(), recordFile + ".getStatus().getCode()");
        return null;
    }

    @Override
    public Void visitDelete(DeleteStatement stmt) {
        buffer.addImport("com.sekacorn.corn.runtime.CobolFile");
        String file = JavaNameMapper.toFieldName(stmt.fileName());
        buffer.line("%s.delete();", file);
        updateFileStatus(stmt.fileName(), file + ".getStatus().getCode()");
        return null;
    }

    @Override
    public Void visitStart(StartStatement stmt) {
        buffer.addImport("com.sekacorn.corn.runtime.IndexedFile");
        String file = JavaNameMapper.toFieldName(stmt.fileName());
        if (stmt.key() != null) {
            String key = stmt.key().accept(exprVisitor);
            buffer.openBlock("if (" + file + " instanceof IndexedFile<?> idx)");
            buffer.line("idx.start(%s, IndexedFile.KeyComparison.EQUAL);", key);
            buffer.closeBlock();
            updateFileStatus(stmt.fileName(), file + ".getStatus().getCode()");
        } else {
            buffer.line("// START without key is not applicable for generic CobolFile");
        }
        return null;
    }

    // ---- CALL ----

    @Override
    public Void visitCall(CallStatement stmt) {
        String programName = stmt.programName().accept(exprVisitor);
        String args = stmt.arguments().stream()
                .map(arg -> arg.expression().accept(exprVisitor))
                .collect(Collectors.joining(", "));

        boolean hasOnException = !stmt.onException().isEmpty();

        if (hasOnException) {
            buffer.openBlock("try");
        }

        buffer.line("// CALL %s", programName);
        if (stmt.returning() != null) {
            String ret = stmt.returning().accept(exprVisitor);
            buffer.line("%s = %s.run(%s);", ret, programName, args);
        } else {
            buffer.line("%s.run(%s);", programName, args);
        }

        if (hasOnException) {
            buffer.closeBlockWith("catch (Exception __callEx) {");
            buffer.indent();
            generateStatements(stmt.onException());
            buffer.closeBlock();
        }
        return null;
    }

    // ---- String Operations ----

    @Override
    public Void visitInspect(InspectStatement stmt) {
        buffer.addImport("com.sekacorn.corn.runtime.CobolString");
        String target = stmt.target().accept(exprVisitor);

        switch (stmt.operation()) {
            case TALLYING -> generateInspectTallying(stmt, target);
            case REPLACING -> generateInspectReplacing(stmt, target);
            case CONVERTING -> generateInspectConverting(stmt, target);
        }
        return null;
    }

    @Override
    public Void visitString(StringStatement stmt) {
        buffer.addImport("com.sekacorn.corn.runtime.CobolString");
        buffer.addImport("java.math.BigDecimal");
        String into = stmt.into().accept(exprVisitor);
        String sources = stmt.sources().stream()
                .map(s -> "String.valueOf(" + s.accept(exprVisitor) + ")")
                .collect(Collectors.joining(", "));
        String bufferVar = nextTemp("stringBuf");
        String ptrVar = nextTemp("stringPtr");
        buffer.line("char[] %s = %s.toCharArray();", bufferVar, into);
        buffer.line("int %s = CobolString.stringInto(%s, 1, %s);", ptrVar, bufferVar, sources);
        buffer.line("%s = new String(%s);", into, bufferVar);
        if (stmt.pointer() != null) {
            String pointer = stmt.pointer().accept(exprVisitor);
            if (isNumericExpression(stmt.pointer())) {
                buffer.line("%s = new BigDecimal(String.valueOf(%s));", pointer, ptrVar);
            } else {
                buffer.line("%s = String.valueOf(%s);", pointer, ptrVar);
            }
        }
        if (!stmt.onOverflow().isEmpty()) {
            buffer.openBlock("if (" + ptrVar + " < 0)");
            generateStatements(stmt.onOverflow());
            buffer.closeBlock();
        }
        return null;
    }

    @Override
    public Void visitUnstring(UnstringStatement stmt) {
        buffer.addImport("com.sekacorn.corn.runtime.CobolString");
        String source = stmt.source().accept(exprVisitor);
        String delimiter = stmt.delimiters().isEmpty()
                ? "\" \""
                : "String.valueOf(" + stmt.delimiters().get(0).accept(exprVisitor) + ")";
        String resultVar = nextTemp("unstringResult");

        buffer.line("String[] %s = CobolString.unstringBy(String.valueOf(%s), %s, %d);",
                resultVar, source, delimiter, stmt.into().size());

        for (int i = 0; i < stmt.into().size(); i++) {
            String target = stmt.into().get(i).accept(exprVisitor);
            buffer.line("%s = %d < %s.length ? %s[%d] : \"\";", target, i, resultVar, resultVar, i);
        }
        return null;
    }

    // ---- Table Operations ----

    @Override
    public Void visitSearch(SearchStatement stmt) {
        String table = JavaNameMapper.toFieldName(stmt.tableName());

        if (stmt.searchAll()) {
            buffer.line("// SEARCH ALL %s (binary search)", table);
        }

        buffer.openBlock("for (int searchIdx = 0; searchIdx < " + table + ".length; searchIdx++)");

        for (SearchStatement.WhenClause when : stmt.whenClauses()) {
            String condition = when.condition().accept(exprVisitor);
            buffer.openBlock("if (" + condition + ")");
            generateStatements(when.statements());
            buffer.line("break;");
            buffer.closeBlock();
        }

        buffer.closeBlock();

        if (!stmt.atEnd().isEmpty()) {
            buffer.line("// AT END");
            generateStatements(stmt.atEnd());
        }
        return null;
    }

    // ---- Set and Initialize ----

    @Override
    public Void visitSet(SetStatement stmt) {
        // Handle SET condition-name TO TRUE/FALSE
        if (stmt.value() instanceof Literal lit
                && lit.getLiteralType() == Literal.LiteralType.FIGURATIVE
                && lit.getValue() instanceof Literal.FigurativeConstant fc
                && (fc == Literal.FigurativeConstant.TRUE || fc == Literal.FigurativeConstant.FALSE)) {
            boolean toTrue = fc == Literal.FigurativeConstant.TRUE;
            for (Expression target : stmt.targets()) {
                if (target instanceof VariableRef vr) {
                    generateSetCondition(vr.getName(), toTrue);
                }
            }
            return null;
        }

        String value = stmt.value().accept(exprVisitor);
        for (Expression target : stmt.targets()) {
            String targetName = target.accept(exprVisitor);
            buffer.line("%s = %s;", targetName, value);
        }
        return null;
    }

    /**
     * Generate code for SET condition-name TO TRUE.
     * Looks up the 88-level condition name's parent and first value,
     * then assigns that value to the parent field.
     */
    private void generateSetCondition(String conditionName, boolean toTrue) {
        for (DataItem item : dataItemMap.values()) {
            for (ConditionName cond : item.getConditionNames()) {
                if (cond.getName().equalsIgnoreCase(conditionName)) {
                    String parentField = JavaNameMapper.toFieldName(item.getName());
                    String parentType = JavaFieldGenerator.javaType(item);
                    if (toTrue && !cond.getValues().isEmpty()) {
                        String val = cond.getValues().get(0).getValue();
                        if ("BigDecimal".equals(parentType)) {
                            buffer.addImport("java.math.BigDecimal");
                            buffer.line("%s = new BigDecimal(\"%s\");", parentField, val);
                        } else {
                            buffer.line("%s = \"%s\";", parentField, val);
                        }
                    } else if (!toTrue) {
                        if ("BigDecimal".equals(parentType)) {
                            buffer.addImport("java.math.BigDecimal");
                            buffer.line("%s = BigDecimal.ZERO;", parentField);
                        } else {
                            buffer.line("%s = \"\";", parentField);
                        }
                    }
                    return;
                }
            }
        }
        // Fallback: if condition name not found, treat as regular set
        buffer.line("// SET %s TO %s - condition name not found", conditionName, toTrue ? "TRUE" : "FALSE");
    }

    @Override
    public Void visitInitialize(InitializeStatement stmt) {
        for (Expression target : stmt.targets()) {
            String targetName = target.accept(exprVisitor);
            buffer.line("%s = %s;", targetName, defaultValueForField(target));
        }
        return null;
    }

    // ---- Helpers ----

    private String arithmeticContext(boolean rounded, RoundMode roundMode) {
        if (!rounded) {
            return "ArithmeticContext.ofPicture(0, 18)";
        }
        String mode = roundMode != null ? roundMode.name() : "HALF_UP";
        return "ArithmeticContext.ofPicture(0, 18).withRounded(CobolMath.RoundMode." + mode + ")";
    }

    private void generateOnSizeError(List<Statement> onSizeError, String resultVar) {
        if (onSizeError != null && !onSizeError.isEmpty() && resultVar != null) {
            buffer.openBlock("if (" + resultVar + ".hasError())");
            generateStatements(onSizeError);
            buffer.closeBlock();
        }
    }

    private void generateOnSizeError(List<Statement> onSizeError) {
        generateOnSizeError(onSizeError, null);
    }

    private boolean isStringField(Expression expr) {
        if (expr instanceof com.sekacorn.corn.ir.expr.VariableRef vr) {
            DataItem item = dataItemMap.get(vr.getName());
            if (item != null) {
                return "String".equals(JavaFieldGenerator.javaType(item));
            }
        }
        return false;
    }

    private int getFieldLength(Expression expr) {
        if (expr instanceof com.sekacorn.corn.ir.expr.VariableRef vr) {
            DataItem item = dataItemMap.get(vr.getName());
            if (item != null && item.getPicture().isPresent()) {
                return item.getPicture().get().getLength();
            }
        }
        return 80; // default length
    }

    private String defaultValueForField(Expression expr) {
        if (expr instanceof com.sekacorn.corn.ir.expr.VariableRef vr) {
            DataItem item = dataItemMap.get(vr.getName());
            if (item != null) {
                String type = JavaFieldGenerator.javaType(item);
                return switch (type) {
                    case "BigDecimal" -> {
                        buffer.addImport("java.math.BigDecimal");
                        yield "BigDecimal.ZERO";
                    }
                    case "String" -> "\"\"";
                    case "int" -> "0";
                    case "long" -> "0L";
                    case "float" -> "0.0f";
                    case "double" -> "0.0";
                    case "boolean" -> "false";
                    default -> "null";
                };
            }
        }
        return "null";
    }

    private String nextTemp(String prefix) {
        tempVarCounter++;
        return "__" + prefix + tempVarCounter;
    }

    private boolean isNumericExpression(Expression expr) {
        if (expr instanceof Literal lit && lit.getLiteralType() == Literal.LiteralType.NUMERIC) {
            return true;
        }
        if (expr instanceof VariableRef vr) {
            DataItem item = dataItemMap.get(vr.getName());
            if (item != null) {
                String type = JavaFieldGenerator.javaType(item);
                return "BigDecimal".equals(type) || "int".equals(type) || "long".equals(type)
                        || "float".equals(type) || "double".equals(type);
            }
        }
        return false;
    }

    private boolean isEvaluateAny(Expression expr) {
        if (expr instanceof Literal lit && lit.getLiteralType() == Literal.LiteralType.STRING) {
            return "__ANY__".equals(lit.getValue());
        }
        return false;
    }

    private void addScannerField() {
        buffer.addImport("java.util.Scanner");
        buffer.addClassField("private static final Scanner __scanner = new Scanner(System.in);");
    }

    private void generateInspectTallying(InspectStatement stmt, String target) {
        InspectStatement.TallyingClause tallying = stmt.tallyingClause();
        if (tallying == null) {
            buffer.line("// INSPECT %s TALLYING", target);
            return;
        }
        String counter = tallying.counter().accept(exprVisitor);
        String sum = tallying.forClauses().stream()
                .map(clause -> String.format(
                        "CobolString.inspectTallying(String.valueOf(%s), %s, %s, %s, %s, %s)",
                        target,
                        clause.value() != null
                                ? "String.valueOf(" + clause.value().accept(exprVisitor) + ")"
                                : "\"\"",
                        clause.mode() == InspectStatement.TallyMode.LEADING,
                        clause.mode() == InspectStatement.TallyMode.CHARACTERS,
                        boundaryValue(clause.beforeBoundary()),
                        boundaryValue(clause.afterBoundary())))
                .collect(Collectors.joining(" + "));
        if (sum.isEmpty()) {
            sum = "0";
        }
        if (isNumericExpression(tallying.counter())) {
            buffer.addImport("java.math.BigDecimal");
            buffer.line("%s = BigDecimal.valueOf(%s);", counter, sum);
        } else {
            buffer.line("%s = String.valueOf(%s);", counter, sum);
        }
    }

    private void generateInspectReplacing(InspectStatement stmt, String target) {
        if (stmt.replacingClauses().isEmpty()) {
            buffer.line("// INSPECT %s REPLACING", target);
            return;
        }
        String current = "String.valueOf(" + target + ")";
        for (InspectStatement.ReplacingClause clause : stmt.replacingClauses()) {
            current = String.format(
                    "CobolString.inspectReplacing(%s, %s, %s, %s, %s, %s, %s, %s)",
                    current,
                    clause.target() != null
                            ? "String.valueOf(" + clause.target().accept(exprVisitor) + ")"
                            : "\"\"",
                    "String.valueOf(" + clause.replacement().accept(exprVisitor) + ")",
                    clause.mode() == InspectStatement.ReplaceMode.LEADING,
                    clause.mode() == InspectStatement.ReplaceMode.FIRST,
                    clause.mode() == InspectStatement.ReplaceMode.CHARACTERS,
                    boundaryValue(clause.beforeBoundary()),
                    boundaryValue(clause.afterBoundary()));
        }
        buffer.line("%s = %s;", target, current);
    }

    private void generateInspectConverting(InspectStatement stmt, String target) {
        InspectStatement.ConvertingClause clause = stmt.convertingClause();
        if (clause == null) {
            buffer.line("// INSPECT %s CONVERTING", target);
            return;
        }
        buffer.line("%s = CobolString.inspectConverting(String.valueOf(%s), String.valueOf(%s), String.valueOf(%s), %s, %s);",
                target,
                target,
                clause.from().accept(exprVisitor),
                clause.to().accept(exprVisitor),
                boundaryValue(clause.beforeBoundary()),
                boundaryValue(clause.afterBoundary()));
    }

    private String boundaryValue(InspectStatement.Boundary boundary) {
        if (boundary == null) {
            return "null";
        }
        return "String.valueOf(" + boundary.delimiter().accept(exprVisitor) + ")";
    }

    private void updateFileStatus(String cobolFileName, String statusExpression) {
        String statusVar = fileStatusMap.get(cobolFileName);
        if (statusVar != null) {
            buffer.line("%s = %s;", JavaNameMapper.toFieldName(statusVar), statusExpression);
        }
    }
}
