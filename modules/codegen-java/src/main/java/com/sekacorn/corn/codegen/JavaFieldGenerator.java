/*
 * JavaFieldGenerator - Generates Java field declarations from COBOL DataItems
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.codegen;

import com.sekacorn.corn.ir.ConditionName;
import com.sekacorn.corn.ir.DataItem;
import com.sekacorn.corn.ir.OccursClause;
import com.sekacorn.corn.ir.Picture;

import java.util.List;

public final class JavaFieldGenerator {

    private final CodeBuffer buffer;

    public JavaFieldGenerator(CodeBuffer buffer) {
        this.buffer = buffer;
    }

    public void generateFields(List<DataItem> items) {
        for (DataItem item : items) {
            if (item.isFiller()) continue;
            generateField(item);
        }
    }

    private void generateField(DataItem item) {
        String javaName = JavaNameMapper.toFieldName(item.getName());

        if (item.isGroup() && !item.getChildren().isEmpty()) {
            // Group item: generate comment and child fields
            buffer.line("// Group: %s", item.getName());
            for (DataItem child : item.getChildren()) {
                if (!child.isFiller()) generateField(child);
            }
            buffer.emptyLine();
        } else {
            // Elementary item: generate field
            String type = javaType(item);
            String init = javaInitialValue(item, type);
            if (item.getOccurs().isPresent()) {
                OccursClause occurs = item.getOccurs().get();
                int size = occurs.getMaxOccurs().orElse(occurs.getMinOccurs());
                buffer.line("private %s[] %s = new %s[%d];", type, javaName, type, size);
            } else {
                buffer.line("private %s %s = %s;", type, javaName, init);
            }
        }

        // Generate 88-level condition methods
        if (!item.getConditionNames().isEmpty()) {
            for (ConditionName cond : item.getConditionNames()) {
                generateConditionMethod(item, cond);
            }
        }
    }

    public static String javaType(DataItem item) {
        if (item.isGroup()) return "Object"; // Group items handled specially

        if (item.getUsage().isPresent()) {
            DataItem.Usage usage = item.getUsage().get();
            switch (usage) {
                case COMP_1: return "float";
                case COMP_2: return "double";
                case INDEX: return "int";
                case POINTER, FUNCTION_POINTER, PROCEDURE_POINTER: return "long";
                default: break; // fall through to picture
            }
        }

        return item.getPicture().map(JavaFieldGenerator::javaTypeFromPicture)
                .orElse("String");
    }

    private static String javaTypeFromPicture(Picture pic) {
        return switch (pic.getCategory()) {
            case NUMERIC, NUMERIC_EDITED -> "BigDecimal";
            case ALPHABETIC, ALPHANUMERIC, ALPHANUMERIC_EDITED, NATIONAL -> "String";
            case BOOLEAN -> "boolean";
        };
    }

    private String javaInitialValue(DataItem item, String type) {
        String value = item.getValue().orElse(null);

        if (value != null) {
            if ("BigDecimal".equals(type)) {
                // Handle figurative constants
                String upper = value.toUpperCase();
                if (upper.equals("ZERO") || upper.equals("ZEROS") || upper.equals("ZEROES")) {
                    buffer.addImport("java.math.BigDecimal");
                    return "BigDecimal.ZERO";
                }
                buffer.addImport("java.math.BigDecimal");
                return "new BigDecimal(\"" + value.replace("+", "") + "\")";
            } else if ("String".equals(type)) {
                String upper = value.toUpperCase();
                if (upper.equals("SPACE") || upper.equals("SPACES")) {
                    int len = item.getPicture().map(Picture::getLength).orElse(1);
                    return "\" \".repeat(" + len + ")";
                }
                return "\"" + escapeJava(value) + "\"";
            } else if ("int".equals(type) || "long".equals(type)) {
                return value;
            } else if ("float".equals(type) || "double".equals(type)) {
                return value;
            } else if ("boolean".equals(type)) {
                return "false";
            }
        }

        // Default values
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

    private void generateConditionMethod(DataItem parent, ConditionName cond) {
        String methodName = "is" + JavaNameMapper.toClassName(cond.getName());
        String parentField = JavaNameMapper.toFieldName(parent.getName());
        String parentType = javaType(parent);

        StringBuilder condition = new StringBuilder();
        for (int i = 0; i < cond.getValues().size(); i++) {
            if (i > 0) condition.append(" || ");
            ConditionName.ValueSpec vs = cond.getValues().get(i);
            if ("BigDecimal".equals(parentType)) {
                buffer.addImport("java.math.BigDecimal");
                condition.append(parentField).append(".compareTo(new BigDecimal(\"")
                        .append(vs.getValue()).append("\")) == 0");
            } else {
                condition.append(parentField).append(".equals(\"")
                        .append(escapeJava(vs.getValue())).append("\")");
            }
        }

        buffer.emptyLine();
        buffer.line("public boolean %s() {", methodName);
        buffer.indent();
        buffer.line("return %s;", condition.toString());
        buffer.dedent();
        buffer.line("}");
    }

    private static String escapeJava(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
