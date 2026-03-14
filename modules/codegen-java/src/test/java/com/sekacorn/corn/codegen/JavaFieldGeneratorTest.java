package com.sekacorn.corn.codegen;

import com.sekacorn.corn.ir.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JavaFieldGeneratorTest {

    private static DataItem numericItem(String name, String pic) {
        return new DataItem(1, name,
                new Picture(pic, Picture.PictureCategory.NUMERIC, 5, 0, false, false, null),
                null, null, null, null, null, null, null, false, false, false, false);
    }

    private static DataItem alphanumericItem(String name, String pic, int length) {
        return new DataItem(1, name,
                new Picture(pic, Picture.PictureCategory.ALPHANUMERIC, length, 0, false, false, null),
                null, null, null, null, null, null, null, false, false, false, false);
    }

    private static DataItem itemWithValue(String name, Picture pic, String value) {
        return new DataItem(1, name, pic, null, null, value, null, null, null, null,
                false, false, false, false);
    }

    private static DataItem itemWithUsage(String name, DataItem.Usage usage) {
        return new DataItem(1, name, null, usage, null, null, null, null, null, null,
                false, false, false, false);
    }

    private static DataItem groupItem(String name, List<DataItem> children) {
        return new DataItem(1, name, null, null, null, null, null, null, children, null,
                false, false, false, false);
    }

    @Test
    void numericFieldGeneratesBigDecimal() {
        CodeBuffer buf = new CodeBuffer();
        JavaFieldGenerator gen = new JavaFieldGenerator(buf);
        gen.generateFields(List.of(numericItem("WS-NUM", "9(5)")));

        String output = buf.getContent();
        assertTrue(output.contains("private BigDecimal wsNum = BigDecimal.ZERO;"));
        assertTrue(buf.getImports().contains("java.math.BigDecimal"));
    }

    @Test
    void alphanumericFieldGeneratesString() {
        CodeBuffer buf = new CodeBuffer();
        JavaFieldGenerator gen = new JavaFieldGenerator(buf);
        gen.generateFields(List.of(alphanumericItem("WS-NAME", "X(20)", 20)));

        String output = buf.getContent();
        assertTrue(output.contains("private String wsName = \"\";"));
    }

    @Test
    void numericFieldWithValueInitializer() {
        Picture pic = new Picture("9(5)", Picture.PictureCategory.NUMERIC, 5, 0, false, false, null);
        CodeBuffer buf = new CodeBuffer();
        JavaFieldGenerator gen = new JavaFieldGenerator(buf);
        gen.generateFields(List.of(itemWithValue("WS-NUM", pic, "100")));

        String output = buf.getContent();
        assertTrue(output.contains("new BigDecimal(\"100\")"));
    }

    @Test
    void stringFieldWithValueInitializer() {
        Picture pic = new Picture("X(20)", Picture.PictureCategory.ALPHANUMERIC, 20, 0, false, false, null);
        CodeBuffer buf = new CodeBuffer();
        JavaFieldGenerator gen = new JavaFieldGenerator(buf);
        gen.generateFields(List.of(itemWithValue("WS-NAME", pic, "HELLO")));

        String output = buf.getContent();
        assertTrue(output.contains("\"HELLO\""));
    }

    @Test
    void comp1GeneratesFloat() {
        assertEquals("float", JavaFieldGenerator.javaType(itemWithUsage("X", DataItem.Usage.COMP_1)));
    }

    @Test
    void comp2GeneratesDouble() {
        assertEquals("double", JavaFieldGenerator.javaType(itemWithUsage("X", DataItem.Usage.COMP_2)));
    }

    @Test
    void indexGeneratesInt() {
        assertEquals("int", JavaFieldGenerator.javaType(itemWithUsage("X", DataItem.Usage.INDEX)));
    }

    @Test
    void groupItemGeneratesComment() {
        DataItem child = alphanumericItem("WS-FIELD", "X(10)", 10);
        DataItem group = groupItem("WS-GROUP", List.of(child));

        CodeBuffer buf = new CodeBuffer();
        JavaFieldGenerator gen = new JavaFieldGenerator(buf);
        gen.generateFields(List.of(group));

        String output = buf.getContent();
        assertTrue(output.contains("// Group: WS-GROUP"));
        assertTrue(output.contains("private String wsField"));
    }

    @Test
    void occursFieldGeneratesArray() {
        OccursClause occurs = new OccursClause(10, null, null, null);
        DataItem item = new DataItem(1, "WS-TABLE",
                new Picture("X(10)", Picture.PictureCategory.ALPHANUMERIC, 10, 0, false, false, null),
                null, null, null, occurs, null, null, null, false, false, false, false);

        CodeBuffer buf = new CodeBuffer();
        JavaFieldGenerator gen = new JavaFieldGenerator(buf);
        gen.generateFields(List.of(item));

        String output = buf.getContent();
        assertTrue(output.contains("private String[] wsTable = new String[10];"));
    }

    @Test
    void conditionNameGeneratesMethod() {
        ConditionName cond = new ConditionName("IS-ACTIVE",
                List.of(new ConditionName.ValueSpec("Y", null)));
        Picture pic = new Picture("X", Picture.PictureCategory.ALPHANUMERIC, 1, 0, false, false, null);
        DataItem item = new DataItem(1, "WS-STATUS", pic, null, null, null, null, null,
                null, List.of(cond), false, false, false, false);

        CodeBuffer buf = new CodeBuffer();
        JavaFieldGenerator gen = new JavaFieldGenerator(buf);
        gen.generateFields(List.of(item));

        String output = buf.getContent();
        assertTrue(output.contains("public boolean isIsActive()"));
        assertTrue(output.contains(".equals(\"Y\")"));
    }

    @Test
    void fillerItemsAreSkipped() {
        DataItem filler = new DataItem(1, "FILLER",
                new Picture("X(10)", Picture.PictureCategory.ALPHANUMERIC, 10, 0, false, false, null),
                null, null, null, null, null, null, null, true, false, false, false);

        CodeBuffer buf = new CodeBuffer();
        JavaFieldGenerator gen = new JavaFieldGenerator(buf);
        gen.generateFields(List.of(filler));

        assertEquals("", buf.getContent());
    }

    @Test
    void figureConstantZeroInitializesToBigDecimalZero() {
        Picture pic = new Picture("9(5)", Picture.PictureCategory.NUMERIC, 5, 0, false, false, null);
        CodeBuffer buf = new CodeBuffer();
        JavaFieldGenerator gen = new JavaFieldGenerator(buf);
        gen.generateFields(List.of(itemWithValue("WS-A", pic, "ZERO")));

        String output = buf.getContent();
        assertTrue(output.contains("BigDecimal.ZERO"));
    }
}
