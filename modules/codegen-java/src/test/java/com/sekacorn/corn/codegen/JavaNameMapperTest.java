package com.sekacorn.corn.codegen;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JavaNameMapperTest {

    @Test
    void toClassNameConvertsPascalCase() {
        assertEquals("HelloWorld", JavaNameMapper.toClassName("HELLO-WORLD"));
        assertEquals("Hello", JavaNameMapper.toClassName("HELLO"));
        assertEquals("MyProgram", JavaNameMapper.toClassName("MY-PROGRAM"));
    }

    @Test
    void toClassNameHandlesSingleWord() {
        assertEquals("Dataprog", JavaNameMapper.toClassName("DATAPROG"));
    }

    @Test
    void toClassNameHandlesNullAndBlank() {
        assertEquals("UnnamedProgram", JavaNameMapper.toClassName(null));
        assertEquals("UnnamedProgram", JavaNameMapper.toClassName(""));
        assertEquals("UnnamedProgram", JavaNameMapper.toClassName("   "));
    }

    @Test
    void toFieldNameConvertsCamelCase() {
        assertEquals("wsCounter", JavaNameMapper.toFieldName("WS-COUNTER"));
        assertEquals("wsName", JavaNameMapper.toFieldName("WS-NAME"));
        assertEquals("myField", JavaNameMapper.toFieldName("MY-FIELD"));
    }

    @Test
    void toFieldNameHandlesSingleWord() {
        assertEquals("counter", JavaNameMapper.toFieldName("COUNTER"));
    }

    @Test
    void toFieldNameHandlesNullAndBlank() {
        assertEquals("unnamed", JavaNameMapper.toFieldName(null));
        assertEquals("unnamed", JavaNameMapper.toFieldName(""));
    }

    @Test
    void toFieldNameHandlesLeadingDigit() {
        assertEquals("_88Level", JavaNameMapper.toFieldName("88-LEVEL"));
    }

    @Test
    void toMethodNameDelegatesToFieldName() {
        assertEquals("mainPara", JavaNameMapper.toMethodName("MAIN-PARA"));
        assertEquals("subPara", JavaNameMapper.toMethodName("SUB-PARA"));
    }

    @Test
    void toConstantNameConvertsUnderscoreCase() {
        assertEquals("IS_ACTIVE", JavaNameMapper.toConstantName("IS-ACTIVE"));
        assertEquals("MAX_VALUE", JavaNameMapper.toConstantName("MAX-VALUE"));
    }

    @Test
    void toConstantNameHandlesNullAndBlank() {
        assertEquals("UNNAMED", JavaNameMapper.toConstantName(null));
        assertEquals("UNNAMED", JavaNameMapper.toConstantName(""));
    }

    @Test
    void escapesJavaReservedWords() {
        assertEquals("returnField", JavaNameMapper.toFieldName("RETURN"));
        assertEquals("classField", JavaNameMapper.toFieldName("CLASS"));
        assertEquals("InterfaceField", JavaNameMapper.toClassName("INTERFACE"));
    }

    @Test
    void handlesUnderscoreSeparator() {
        assertEquals("wsName", JavaNameMapper.toFieldName("WS_NAME"));
        assertEquals("WsName", JavaNameMapper.toClassName("WS_NAME"));
    }
}
