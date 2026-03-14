package com.sekacorn.corn.parser;

import com.sekacorn.corn.ir.ConditionName;
import com.sekacorn.corn.ir.DataItem;
import com.sekacorn.corn.ir.Picture;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataItemBuilderTest {

    private static DataItemBuilder.FlatDataItem flat(int level, String name) {
        return new DataItemBuilder.FlatDataItem(level, name, false, null, null, null, null, null, null, false, false, false, null);
    }

    private static DataItemBuilder.FlatDataItem flat88(String name, String value) {
        return new DataItemBuilder.FlatDataItem(88, name, false, null, null, null, null, null, null, false, false, false,
                List.of(new ConditionName.ValueSpec(value, null)));
    }

    @Test
    void singleLevel01Item() {
        var items = DataItemBuilder.build(List.of(flat(1, "WS-REC")));
        assertEquals(1, items.size());
        assertEquals("WS-REC", items.get(0).getName());
        assertEquals(1, items.get(0).getLevelNumber());
    }

    @Test
    void level01WithChildren() {
        var items = DataItemBuilder.build(List.of(
            flat(1, "WS-REC"),
            flat(5, "WS-FIELD1"),
            flat(5, "WS-FIELD2")
        ));
        assertEquals(1, items.size());
        DataItem parent = items.get(0);
        assertNotNull(parent.getChildren());
        assertEquals(2, parent.getChildren().size());
        assertEquals("WS-FIELD1", parent.getChildren().get(0).getName());
        assertEquals("WS-FIELD2", parent.getChildren().get(1).getName());
    }

    @Test
    void nestedLevels() {
        var items = DataItemBuilder.build(List.of(
            flat(1, "WS-REC"),
            flat(5, "WS-GROUP"),
            flat(10, "WS-CHILD1"),
            flat(10, "WS-CHILD2")
        ));
        assertEquals(1, items.size());
        DataItem group = items.get(0).getChildren().get(0);
        assertEquals("WS-GROUP", group.getName());
        assertNotNull(group.getChildren());
        assertEquals(2, group.getChildren().size());
    }

    @Test
    void level88ConditionNames() {
        var items = DataItemBuilder.build(List.of(
            flat(1, "WS-STATUS"),
            flat88("ACTIVE", "A"),
            flat88("INACTIVE", "I")
        ));
        assertEquals(1, items.size());
        DataItem status = items.get(0);
        assertNotNull(status.getConditionNames());
        assertEquals(2, status.getConditionNames().size());
        assertEquals("ACTIVE", status.getConditionNames().get(0).getName());
    }

    @Test
    void level77IndependentItem() {
        var items = DataItemBuilder.build(List.of(
            flat(77, "WS-COUNTER"),
            flat(77, "WS-FLAG")
        ));
        assertEquals(2, items.size());
        assertEquals("WS-COUNTER", items.get(0).getName());
        assertEquals("WS-FLAG", items.get(1).getName());
    }

    @Test
    void multipleLevel01Records() {
        var items = DataItemBuilder.build(List.of(
            flat(1, "REC-A"),
            flat(5, "FIELD-A"),
            flat(1, "REC-B"),
            flat(5, "FIELD-B")
        ));
        assertEquals(2, items.size());
        assertEquals("REC-A", items.get(0).getName());
        assertEquals("REC-B", items.get(1).getName());
    }

    @Test
    void emptyListReturnsEmpty() {
        assertTrue(DataItemBuilder.build(List.of()).isEmpty());
        assertTrue(DataItemBuilder.build(null).isEmpty());
    }
}
