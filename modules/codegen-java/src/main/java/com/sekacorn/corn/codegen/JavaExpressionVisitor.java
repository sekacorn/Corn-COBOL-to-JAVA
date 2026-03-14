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
                yield left + ".divide(" + right + ", 10, RoundingMode.HALF_UP)";
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
            case ALPHABETIC -> prefix + "String.valueOf(" + subject + ").chars().allMatch(Character::isLetter)";
            case ALPHABETIC_LOWER -> prefix + "String.valueOf(" + subject + ").chars().allMatch(Character::isLowerCase)";
            case ALPHABETIC_UPPER -> prefix + "String.valueOf(" + subject + ").chars().allMatch(Character::isUpperCase)";
            case CONDITION_NAME -> throw new IllegalStateException("Handled above");
        };
    }

    private String mapIntrinsicFunction(String name, List<String> args) {
        String argList = String.join(", ", args);
        return switch (name.toUpperCase()) {
            case "LENGTH" -> args.isEmpty() ? "0" : "String.valueOf(" + args.get(0) + ").length()";
            case "UPPER-CASE" -> args.isEmpty() ? "\"\"" : args.get(0) + ".toUpperCase()";
            case "LOWER-CASE" -> args.isEmpty() ? "\"\"" : args.get(0) + ".toLowerCase()";
            case "TRIM" -> args.isEmpty() ? "\"\"" : args.get(0) + ".trim()";
            case "REVERSE" -> args.isEmpty() ? "\"\"" : "new StringBuilder(" + args.get(0) + ").reverse().toString()";
            case "CURRENT-DATE" -> {
                buffer.addImport("java.time.LocalDateTime");
                buffer.addImport("java.time.format.DateTimeFormatter");
                yield "LocalDateTime.now().format(DateTimeFormatter.ofPattern(\"yyyyMMddHHmmss\"))";
            }
            case "ABS", "ABSOLUTE-VALUE" -> {
                if (args.isEmpty()) {
                    buffer.addImport("java.math.BigDecimal");
                    yield "BigDecimal.ZERO";
                }
                yield args.get(0) + ".abs()";
            }
            case "MAX" -> reduceMaxMin(args, true);
            case "MIN" -> reduceMaxMin(args, false);
            case "MOD" -> {
                // FUNCTION MOD(a, b) -- args is "a, b"
                if (args.size() >= 2) {
                    yield args.get(0) + ".remainder(" + args.get(1) + ")";
                }
                buffer.addImport("java.math.BigDecimal");
                yield "BigDecimal.ZERO";
            }
            default -> "/* FUNCTION " + name + " */ " + argList;
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
