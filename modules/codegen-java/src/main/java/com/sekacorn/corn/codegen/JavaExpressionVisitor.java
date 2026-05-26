/*
 * JavaExpressionVisitor - Generates Java expressions from COBOL IR expressions
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.codegen;

import com.sekacorn.corn.ir.ConditionName;
import com.sekacorn.corn.ir.DataItem;
import com.sekacorn.corn.ir.expr.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class JavaExpressionVisitor implements ExpressionVisitor<String> {

    private final Map<String, DataItem> dataItemMap;
    private final CodeBuffer buffer;

    public JavaExpressionVisitor(Map<String, DataItem> dataItemMap, CodeBuffer buffer) {
        this.dataItemMap = dataItemMap;
        this.buffer = buffer;
    }

    @Override
    public String visitLiteral(Literal expr) {
        return switch (expr.getLiteralType()) {
            case NUMERIC -> {
                buffer.addImport("java.math.BigDecimal");
                yield "new BigDecimal(\"" + expr.getValue() + "\")";
            }
            case STRING -> "\"" + escapeJava(expr.getValue().toString()) + "\"";
            case FIGURATIVE -> {
                Object val = expr.getValue();
                if (val instanceof Literal.FigurativeConstant fc) {
                    yield visitFigurative(fc);
                }
                yield "\"" + val + "\"";
            }
        };
    }

    private String visitFigurative(Literal.FigurativeConstant fc) {
        return switch (fc) {
            case ZERO, ZEROS -> {
                buffer.addImport("java.math.BigDecimal");
                yield "BigDecimal.ZERO";
            }
            case SPACE, SPACES -> {
                buffer.addImport("com.sekacorn.corn.runtime.CobolString");
                yield "\" \"";
            }
            case HIGH_VALUE, HIGH_VALUES -> {
                buffer.addImport("com.sekacorn.corn.runtime.CobolString");
                yield "CobolString.FigurativeConstants.HIGH_VALUE";
            }
            case LOW_VALUE, LOW_VALUES -> {
                buffer.addImport("com.sekacorn.corn.runtime.CobolString");
                yield "CobolString.FigurativeConstants.LOW_VALUE";
            }
            case QUOTE, QUOTES -> {
                buffer.addImport("com.sekacorn.corn.runtime.CobolString");
                yield "CobolString.FigurativeConstants.QUOTE";
            }
            case NULL, NULLS -> "null";
            case ALL -> "\"\""; // ALL needs context for repetition
            case TRUE -> "true";
            case FALSE -> "false";
        };
    }

    @Override
    public String visitVariable(VariableRef expr) {
        String javaName = JavaNameMapper.toFieldName(expr.getName());

        if (expr.getRefMod().isPresent()) {
            VariableRef.ReferenceModification rm = expr.getRefMod().get();
            String start = rm.getStart().accept(this);
            String length = rm.getLength().map(l -> l.accept(this)).orElse("" + Integer.MAX_VALUE);
            buffer.addImport("com.sekacorn.corn.runtime.CobolString");
            return "CobolString.referenceModification(" + javaName + ", " + start + ", " + length + ")";
        }

        return javaName;
    }

    @Override
    public String visitSubscript(SubscriptRef expr) {
        String name = JavaNameMapper.toFieldName(expr.getName());
        String subscripts = expr.getSubscripts().stream()
                .map(s -> "(" + s.accept(this) + ").intValue() - 1")
                .collect(Collectors.joining("]["));
        return name + "[" + subscripts + "]";
    }

    @Override
    public String visitBinary(BinaryOp expr) {
        String left = expr.getLeft().accept(this);
        String right = expr.getRight().accept(this);
        boolean numericComparison = isNumericExpression(expr.getLeft()) || isNumericExpression(expr.getRight());

        return switch (expr.getOperator()) {
            case ADD -> left + ".add(" + right + ")";
            case SUBTRACT -> left + ".subtract(" + right + ")";
            case MULTIPLY -> left + ".multiply(" + right + ")";
            case DIVIDE -> {
                buffer.addImport("java.math.RoundingMode");
                yield left + ".divide(" + right + ", 10, RoundingMode.DOWN)";
            }
            case POWER -> left + ".pow(" + right + ".intValue())";
            case EQUAL -> numericComparison
                    ? left + ".compareTo(" + right + ") == 0"
                    : "java.util.Objects.equals(" + left + ", " + right + ")";
            case NOT_EQUAL -> numericComparison
                    ? left + ".compareTo(" + right + ") != 0"
                    : "!java.util.Objects.equals(" + left + ", " + right + ")";
            case LESS_THAN -> numericComparison
                    ? left + ".compareTo(" + right + ") < 0"
                    : "String.valueOf(" + left + ").compareTo(String.valueOf(" + right + ")) < 0";
            case LESS_EQUAL -> numericComparison
                    ? left + ".compareTo(" + right + ") <= 0"
                    : "String.valueOf(" + left + ").compareTo(String.valueOf(" + right + ")) <= 0";
            case GREATER_THAN -> numericComparison
                    ? left + ".compareTo(" + right + ") > 0"
                    : "String.valueOf(" + left + ").compareTo(String.valueOf(" + right + ")) > 0";
            case GREATER_EQUAL -> numericComparison
                    ? left + ".compareTo(" + right + ") >= 0"
                    : "String.valueOf(" + left + ").compareTo(String.valueOf(" + right + ")) >= 0";
            case AND -> "(" + left + " && " + right + ")";
            case OR -> "(" + left + " || " + right + ")";
        };
    }

    @Override
    public String visitUnary(UnaryOp expr) {
        String operand = expr.getOperand().accept(this);
        return switch (expr.getOperator()) {
            case NEGATE -> operand + ".negate()";
            case NOT -> "!(" + operand + ")";
        };
    }

    @Override
    public String visitFunction(FunctionCall expr) {
        List<String> args = expr.getArguments().stream()
                .map(a -> a.accept(this))
                .collect(Collectors.toList());
        return mapIntrinsicFunction(expr.getFunctionName(), args);
    }

    @Override
    public String visitCondition(ConditionExpr expr) {
        String subject = expr.getSubject().accept(this);
        String prefix = expr.isNegated() ? "!" : "";

        if (expr.getConditionType() == ConditionExpr.ConditionType.CONDITION_NAME) {
            // 88-level condition name reference — call the generated isXxx() method
            if (expr.getSubject() instanceof VariableRef vr) {
                String methodName = "is" + JavaNameMapper.toClassName(vr.getName());
                return prefix + methodName + "()";
            }
            return prefix + subject;
        }

        buffer.addImport("com.sekacorn.corn.runtime.CobolMath");
        return switch (expr.getConditionType()) {
            case NUMERIC -> prefix + "CobolMath.isNumeric(String.valueOf(" + subject + "))";
            case POSITIVE -> prefix + "CobolMath.isPositive(" + subject + ")";
            case NEGATIVE -> prefix + "CobolMath.isNegative(" + subject + ")";
            case ZERO -> prefix + "CobolMath.isZero(" + subject + ")";
            case ALPHABETIC -> prefix + "String.valueOf(" + subject + ").chars().allMatch(c -> (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == ' ')";
            case ALPHABETIC_LOWER -> prefix + "String.valueOf(" + subject + ").chars().allMatch(c -> (c >= 'a' && c <= 'z') || c == ' ')";
            case ALPHABETIC_UPPER -> prefix + "String.valueOf(" + subject + ").chars().allMatch(c -> (c >= 'A' && c <= 'Z') || c == ' ')";
            case CONDITION_NAME -> throw new IllegalStateException("Handled above");
        };
    }

    private String mapIntrinsicFunction(String name, List<String> args) {
        String argList = String.join(", ", args);
        buffer.addImport("java.math.BigDecimal");
        return switch (name.toUpperCase()) {
            // ─── String functions ───
            case "LENGTH" -> args.isEmpty() ? "0" : "String.valueOf(" + args.get(0) + ").length()";
            case "UPPER-CASE" -> args.isEmpty() ? "\"\"" : "String.valueOf(" + args.get(0) + ").toUpperCase()";
            case "LOWER-CASE" -> args.isEmpty() ? "\"\"" : "String.valueOf(" + args.get(0) + ").toLowerCase()";
            case "TRIM" -> args.isEmpty() ? "\"\"" : "String.valueOf(" + args.get(0) + ").trim()";
            case "REVERSE" -> args.isEmpty() ? "\"\"" : "new StringBuilder(String.valueOf(" + args.get(0) + ")).reverse().toString()";
            case "CHAR" -> args.size() >= 1 ? "String.valueOf((char)(" + args.get(0) + ").intValue())" : "\" \"";
            case "ORD" -> args.size() >= 1 ? "new BigDecimal((int) String.valueOf(" + args.get(0) + ").charAt(0))" : "BigDecimal.ZERO";
            case "NUMVAL" -> args.size() >= 1 ? "new BigDecimal(String.valueOf(" + args.get(0) + ").trim())" : "BigDecimal.ZERO";
            case "NUMVAL-C" -> args.size() >= 1 ? "new BigDecimal(String.valueOf(" + args.get(0) + ").trim().replace(\",\", \"\").replace(\"$\", \"\").replace(\"CR\", \"\").replace(\"DB\", \"\"))" : "BigDecimal.ZERO";

            // ─── Date/time functions ───
            case "CURRENT-DATE" -> {
                buffer.addImport("java.time.LocalDateTime");
                buffer.addImport("java.time.format.DateTimeFormatter");
                yield "LocalDateTime.now().format(DateTimeFormatter.ofPattern(\"yyyyMMddHHmmssSSSSS\")) + \"    \"";
            }
            case "WHEN-COMPILED" -> {
                buffer.addImport("java.time.LocalDateTime");
                buffer.addImport("java.time.format.DateTimeFormatter");
                yield "LocalDateTime.now().format(DateTimeFormatter.ofPattern(\"yyyyMMddHHmmssSSSSS\")) + \"    \"";
            }
            case "INTEGER-OF-DATE" -> args.size() >= 1 ? "CobolMath.integerOfDate(" + args.get(0) + ")" : "BigDecimal.ZERO";
            case "INTEGER-OF-DAY" -> args.size() >= 1 ? "CobolMath.integerOfDay(" + args.get(0) + ")" : "BigDecimal.ZERO";
            case "DATE-OF-INTEGER" -> args.size() >= 1 ? "CobolMath.dateOfInteger(" + args.get(0) + ")" : "BigDecimal.ZERO";
            case "DAY-OF-INTEGER" -> args.size() >= 1 ? "CobolMath.dayOfInteger(" + args.get(0) + ")" : "BigDecimal.ZERO";

            // ─── Arithmetic functions ───
            case "ABS", "ABSOLUTE-VALUE" -> args.isEmpty() ? "BigDecimal.ZERO" : args.get(0) + ".abs()";
            case "MAX" -> reduceMaxMin(args, true);
            case "MIN" -> reduceMaxMin(args, false);
            case "ORD-MAX" -> reduceOrdMaxMin(args, true);
            case "ORD-MIN" -> reduceOrdMaxMin(args, false);
            case "MOD" -> args.size() >= 2 ? "CobolMath.mod(" + args.get(0) + ", " + args.get(1) + ")" : "BigDecimal.ZERO";
            case "REM" -> args.size() >= 2 ? args.get(0) + ".remainder(" + args.get(1) + ")" : "BigDecimal.ZERO";
            case "SUM" -> args.isEmpty() ? "BigDecimal.ZERO" : reduceSum(args);
            case "MEAN" -> args.isEmpty() ? "BigDecimal.ZERO" : reduceMean(args);
            case "MEDIAN" -> {
                buffer.addImport("com.sekacorn.corn.runtime.CobolMath");
                yield "CobolMath.median(" + argList + ")";
            }
            case "MIDRANGE" -> {
                yield "(" + reduceMaxMin(args, true) + ".add(" + reduceMaxMin(args, false) + ")).divide(new BigDecimal(\"2\"), 10, java.math.RoundingMode.DOWN)";
            }
            case "RANGE" -> {
                yield reduceMaxMin(args, true) + ".subtract(" + reduceMaxMin(args, false) + ")";
            }
            case "VARIANCE" -> {
                buffer.addImport("com.sekacorn.corn.runtime.CobolMath");
                yield "CobolMath.variance(" + argList + ")";
            }
            case "STANDARD-DEVIATION" -> {
                buffer.addImport("com.sekacorn.corn.runtime.CobolMath");
                yield "CobolMath.standardDeviation(" + argList + ")";
            }
            case "INTEGER" -> args.size() >= 1 ? args.get(0) + ".setScale(0, java.math.RoundingMode.FLOOR)" : "BigDecimal.ZERO";
            case "INTEGER-PART" -> args.size() >= 1 ? args.get(0) + ".setScale(0, java.math.RoundingMode.DOWN)" : "BigDecimal.ZERO";
            case "FACTORIAL" -> args.size() >= 1 ? "CobolMath.factorial(" + args.get(0) + ")" : "BigDecimal.ONE";
            case "ANNUITY" -> args.size() >= 2 ? "CobolMath.annuity(" + args.get(0) + ", " + args.get(1) + ")" : "BigDecimal.ZERO";
            case "PRESENT-VALUE" -> {
                buffer.addImport("com.sekacorn.corn.runtime.CobolMath");
                yield "CobolMath.presentValue(" + argList + ")";
            }
            case "RANDOM" -> args.isEmpty()
                    ? "new BigDecimal(Math.random())"
                    : "new BigDecimal(new java.util.Random(" + args.get(0) + ".longValue()).nextDouble())";

            // ─── Trigonometric / math functions ───
            case "SQRT" -> args.size() >= 1 ? "new BigDecimal(Math.sqrt(" + args.get(0) + ".doubleValue()))" : "BigDecimal.ZERO";
            case "LOG" -> args.size() >= 1 ? "new BigDecimal(Math.log(" + args.get(0) + ".doubleValue()))" : "BigDecimal.ZERO";
            case "LOG10" -> args.size() >= 1 ? "new BigDecimal(Math.log10(" + args.get(0) + ".doubleValue()))" : "BigDecimal.ZERO";
            case "SIN" -> args.size() >= 1 ? "new BigDecimal(Math.sin(" + args.get(0) + ".doubleValue()))" : "BigDecimal.ZERO";
            case "COS" -> args.size() >= 1 ? "new BigDecimal(Math.cos(" + args.get(0) + ".doubleValue()))" : "BigDecimal.ZERO";
            case "TAN" -> args.size() >= 1 ? "new BigDecimal(Math.tan(" + args.get(0) + ".doubleValue()))" : "BigDecimal.ZERO";
            case "ASIN" -> args.size() >= 1 ? "new BigDecimal(Math.asin(" + args.get(0) + ".doubleValue()))" : "BigDecimal.ZERO";
            case "ACOS" -> args.size() >= 1 ? "new BigDecimal(Math.acos(" + args.get(0) + ".doubleValue()))" : "BigDecimal.ZERO";
            case "ATAN" -> args.size() >= 1 ? "new BigDecimal(Math.atan(" + args.get(0) + ".doubleValue()))" : "BigDecimal.ZERO";

            default -> {
                // Unknown function: emit zero so generated code compiles
                yield "BigDecimal.ZERO /* TODO: FUNCTION " + name + "(" + argList + ") */";
            }
        };
    }

    private String reduceMaxMin(List<String> args, boolean max) {
        if (args.isEmpty()) {
            buffer.addImport("java.math.BigDecimal");
            return "BigDecimal.ZERO";
        }
        String current = args.get(0);
        for (int i = 1; i < args.size(); i++) {
            String next = args.get(i);
            current = max
                    ? "(" + current + ".compareTo(" + next + ") >= 0 ? " + current + " : " + next + ")"
                    : "(" + current + ".compareTo(" + next + ") <= 0 ? " + current + " : " + next + ")";
        }
        return current;
    }

    private String reduceOrdMaxMin(List<String> args, boolean max) {
        if (args.isEmpty()) {
            buffer.addImport("java.math.BigDecimal");
            return "BigDecimal.ZERO";
        }
        // ORD-MAX/ORD-MIN returns the ordinal position (1-based) of the max/min argument
        String expr = "new BigDecimal(1)";
        for (int i = 1; i < args.size(); i++) {
            String comparison = max
                    ? args.get(i) + ".compareTo(" + args.get(i - 1) + ") > 0"
                    : args.get(i) + ".compareTo(" + args.get(i - 1) + ") < 0";
            expr = "(" + comparison + " ? new BigDecimal(" + (i + 1) + ") : " + expr + ")";
        }
        return expr;
    }

    private String reduceSum(List<String> args) {
        String current = args.get(0);
        for (int i = 1; i < args.size(); i++) {
            current = current + ".add(" + args.get(i) + ")";
        }
        return current;
    }

    private String reduceMean(List<String> args) {
        String sum = reduceSum(args);
        return "(" + sum + ").divide(new BigDecimal(\"" + args.size() + "\"), 10, java.math.RoundingMode.DOWN)";
    }

    // Helper to check if a COBOL name refers to a numeric field
    public boolean isNumericField(String cobolName) {
        DataItem item = dataItemMap.get(cobolName);
        if (item == null) return false;
        String type = JavaFieldGenerator.javaType(item);
        return "BigDecimal".equals(type) || "int".equals(type) || "long".equals(type)
                || "float".equals(type) || "double".equals(type);
    }

    private boolean isNumericExpression(Expression expr) {
        if (expr instanceof Literal lit) {
            return lit.getLiteralType() == Literal.LiteralType.NUMERIC;
        }
        if (expr instanceof VariableRef ref) {
            return isNumericField(ref.getName());
        }
        if (expr instanceof SubscriptRef ref) {
            return isNumericField(ref.getName());
        }
        if (expr instanceof UnaryOp unary) {
            return isNumericExpression(unary.getOperand());
        }
        if (expr instanceof BinaryOp binary) {
            return switch (binary.getOperator()) {
                case ADD, SUBTRACT, MULTIPLY, DIVIDE, POWER -> true;
                default -> false;
            };
        }
        if (expr instanceof FunctionCall call) {
            String n = call.getFunctionName().toUpperCase();
            return n.equals("ABS") || n.equals("ABSOLUTE-VALUE") || n.equals("MAX")
                    || n.equals("MIN") || n.equals("MOD");
        }
        return false;
    }

    private static String escapeJava(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
