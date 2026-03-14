/*
 * JavaNameMapper - Static utility class for COBOL-to-Java name conversion
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.codegen;

import java.util.Set;

public final class JavaNameMapper {
    private JavaNameMapper() {}

    private static final Set<String> JAVA_RESERVED = Set.of(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
        "class", "const", "continue", "default", "do", "double", "else", "enum",
        "extends", "final", "finally", "float", "for", "goto", "if", "implements",
        "import", "instanceof", "int", "interface", "long", "native", "new",
        "package", "private", "protected", "public", "return", "short", "static",
        "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
        "transient", "try", "void", "volatile", "while", "var", "yield", "record",
        "sealed", "permits"
    );

    // HELLO-WORLD → HelloWorld
    public static String toClassName(String cobolName) {
        if (cobolName == null || cobolName.isBlank()) return "UnnamedProgram";
        String[] parts = splitName(cobolName);
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1).toLowerCase());
            }
        }
        String result = sb.toString();
        return escapeIfReserved(result);
    }

    // WS-COUNTER → wsCounter
    public static String toFieldName(String cobolName) {
        if (cobolName == null || cobolName.isBlank()) return "unnamed";
        String[] parts = splitName(cobolName);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) continue;
            if (sb.isEmpty()) {
                sb.append(part.toLowerCase());
            } else {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1).toLowerCase());
            }
        }
        String result = sb.toString();
        // Handle names starting with digit
        if (!result.isEmpty() && Character.isDigit(result.charAt(0))) {
            result = "_" + result;
        }
        return escapeIfReserved(result);
    }

    // MAIN-PARA → mainPara (same as field but for methods)
    public static String toMethodName(String cobolName) {
        return toFieldName(cobolName);
    }

    // IS-ACTIVE → IS_ACTIVE
    public static String toConstantName(String cobolName) {
        if (cobolName == null || cobolName.isBlank()) return "UNNAMED";
        return cobolName.toUpperCase().replace('-', '_');
    }

    private static String[] splitName(String name) {
        return name.split("[-_]");
    }

    private static String escapeIfReserved(String name) {
        if (JAVA_RESERVED.contains(name.toLowerCase())) {
            return name + "Field";
        }
        return name;
    }
}
