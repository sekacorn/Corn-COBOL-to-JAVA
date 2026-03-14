/*
 * IrModelTest - Unit tests for COBOL IR model classes
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("IR Model — Intermediate Representation classes")
class IrModelTest {

    @Nested
    @DisplayName("SourceLocation")
    class SourceLocationTests {
        @Test
        @DisplayName("create single point location")
        void singlePoint() {
            var loc = SourceLocation.of("TEST.cbl", 10, 7);
            assertThat(loc.getFileName()).isEqualTo("TEST.cbl");
            assertThat(loc.getStartLine()).isEqualTo(10);
            assertThat(loc.getStartColumn()).isEqualTo(7);
            assertThat(loc.getEndLine()).isEqualTo(10);
            assertThat(loc.getEndColumn()).isEqualTo(7);
        }

        @Test
        @DisplayName("create range location")
        void rangeLocation() {
            var loc = SourceLocation.range("TEST.cbl", 10, 7, 15, 72);
            assertThat(loc.getStartLine()).isEqualTo(10);
            assertThat(loc.getEndLine()).isEqualTo(15);
            assertThat(loc.getEndColumn()).isEqualTo(72);
        }

        @Test
        @DisplayName("toString uses file:line:col format")
        void toStringFormat() {
            var loc = SourceLocation.of("PROGRAM.cbl", 42, 12);
            assertThat(loc.toString()).isEqualTo("PROGRAM.cbl:42:12");
        }

        @Test
        @DisplayName("equality and hashCode")
        void equality() {
            var loc1 = SourceLocation.of("A.cbl", 1, 1);
            var loc2 = SourceLocation.of("A.cbl", 1, 1);
            var loc3 = SourceLocation.of("B.cbl", 1, 1);

            assertThat(loc1).isEqualTo(loc2);
            assertThat(loc1).isNotEqualTo(loc3);
            assertThat(loc1.hashCode()).isEqualTo(loc2.hashCode());
        }
    }

    @Nested
    @DisplayName("SourceMetadata")
    class SourceMetadataTests {
        @Test
        @DisplayName("default dialect is ANSI_85")
        void defaultDialect() {
            var meta = new SourceMetadata("test.cbl", Instant.now(), "1.0.0", null, null);
            assertThat(meta.getDialect()).isEqualTo(SourceMetadata.CobolDialect.ANSI_85);
        }

        @Test
        @DisplayName("properties default to empty map")
        void defaultProperties() {
            var meta = new SourceMetadata("test.cbl", Instant.now(), "1.0.0",
                    SourceMetadata.CobolDialect.IBM_ENTERPRISE, null);
            assertThat(meta.getProperties()).isEmpty();
        }

        @Test
        @DisplayName("properties are immutable")
        void immutableProperties() {
            var meta = new SourceMetadata("test.cbl", Instant.now(), "1.0.0",
                    SourceMetadata.CobolDialect.ANSI_85,
                    Map.of("key", "value"));
            assertThatThrownBy(() -> meta.getProperties().put("new", "val"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("all dialects defined")
        void allDialects() {
            assertThat(SourceMetadata.CobolDialect.values()).hasSize(10);
        }
    }

    @Nested
    @DisplayName("Picture (IR)")
    class PictureTests {
        @Test
        @DisplayName("builder creates picture correctly")
        void builderCreation() {
            var pic = new Picture.Builder()
                    .raw("9(9)V99")
                    .category(Picture.PictureCategory.NUMERIC)
                    .length(11)
                    .scale(2)
                    .signed(false)
                    .hasEditSymbols(false)
                    .editPattern(null)
                    .build();

            assertThat(pic.getRaw()).isEqualTo("9(9)V99");
            assertThat(pic.getCategory()).isEqualTo(Picture.PictureCategory.NUMERIC);
            assertThat(pic.getLength()).isEqualTo(11);
            assertThat(pic.getScale()).isEqualTo(2);
            assertThat(pic.isSigned()).isFalse();
            assertThat(pic.hasEditSymbols()).isFalse();
        }

        @Test
        @DisplayName("null raw throws NullPointerException")
        void nullRaw() {
            assertThatThrownBy(() -> new Picture.Builder()
                    .raw(null)
                    .category(Picture.PictureCategory.NUMERIC)
                    .build())
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null category throws NullPointerException")
        void nullCategory() {
            assertThatThrownBy(() -> new Picture.Builder()
                    .raw("X(10)")
                    .category(null)
                    .build())
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("all picture categories defined")
        void allCategories() {
            assertThat(Picture.PictureCategory.values()).containsExactly(
                    Picture.PictureCategory.ALPHABETIC,
                    Picture.PictureCategory.ALPHANUMERIC,
                    Picture.PictureCategory.ALPHANUMERIC_EDITED,
                    Picture.PictureCategory.NUMERIC,
                    Picture.PictureCategory.NUMERIC_EDITED,
                    Picture.PictureCategory.NATIONAL,
                    Picture.PictureCategory.BOOLEAN);
        }

        @Test
        @DisplayName("equality and hashCode")
        void equality() {
            var pic1 = new Picture("9(5)", Picture.PictureCategory.NUMERIC,
                    5, 0, false, false, null);
            var pic2 = new Picture("9(5)", Picture.PictureCategory.NUMERIC,
                    5, 0, false, false, null);
            assertThat(pic1).isEqualTo(pic2);
            assertThat(pic1.hashCode()).isEqualTo(pic2.hashCode());
        }
    }

    @Nested
    @DisplayName("DataItem")
    class DataItemTests {
        @Test
        @DisplayName("elementary item has no children and has picture")
        void elementaryItem() {
            var pic = new Picture("9(5)", Picture.PictureCategory.NUMERIC,
                    5, 0, false, false, null);
            var item = new DataItem(5, "WS-COUNT", pic,
                    DataItem.Usage.DISPLAY, null, null, null, null,
                    null, null, false, false, false, false);

            assertThat(item.isElementary()).isTrue();
            assertThat(item.isGroup()).isFalse();
            assertThat(item.getLevelNumber()).isEqualTo(5);
            assertThat(item.getName()).isEqualTo("WS-COUNT");
            assertThat(item.getPicture()).isPresent();
            assertThat(item.isFiller()).isFalse();
        }

        @Test
        @DisplayName("group item has children")
        void groupItem() {
            var child = new DataItem(5, "CHILD", null,
                    null, null, null, null, null,
                    null, null, false, false, false, false);
            var group = new DataItem(1, "WS-GROUP", null,
                    null, null, null, null, null,
                    List.of(child), null, false, false, false, false);

            assertThat(group.isGroup()).isTrue();
            assertThat(group.isElementary()).isFalse();
            assertThat(group.getChildren()).hasSize(1);
        }

        @Test
        @DisplayName("level number validation (1-88)")
        void levelNumberValidation() {
            assertThatThrownBy(() -> new DataItem(0, "BAD", null,
                    null, null, null, null, null, null, null, false, false, false, false))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> new DataItem(89, "BAD", null,
                    null, null, null, null, null, null, null, false, false, false, false))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("level 88 for condition names")
        void level88() {
            var item = new DataItem(88, "IS-ACTIVE", null,
                    null, null, "Y", null, null, null, null, false, false, false, false);
            assertThat(item.getLevelNumber()).isEqualTo(88);
            assertThat(item.getValue()).isPresent().contains("Y");
        }

        @Test
        @DisplayName("FILLER item")
        void fillerItem() {
            var item = new DataItem(5, "FILLER", null,
                    null, null, null, null, null, null, null, true, false, false, false);
            assertThat(item.isFiller()).isTrue();
        }

        @Test
        @DisplayName("children are immutable")
        void immutableChildren() {
            var child = new DataItem(5, "CHILD", null,
                    null, null, null, null, null, null, null, false, false, false, false);
            var group = new DataItem(1, "GROUP", null,
                    null, null, null, null, null,
                    List.of(child), null, false, false, false, false);
            assertThatThrownBy(() -> group.getChildren().add(child))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("all Usage types defined")
        void allUsageTypes() {
            assertThat(DataItem.Usage.values()).contains(
                    DataItem.Usage.DISPLAY,
                    DataItem.Usage.COMP,
                    DataItem.Usage.COMP_3,
                    DataItem.Usage.PACKED_DECIMAL,
                    DataItem.Usage.BINARY,
                    DataItem.Usage.INDEX,
                    DataItem.Usage.POINTER);
        }

        @Test
        @DisplayName("all Sign types defined")
        void allSignTypes() {
            assertThat(DataItem.Sign.values()).containsExactly(
                    DataItem.Sign.LEADING,
                    DataItem.Sign.TRAILING,
                    DataItem.Sign.LEADING_SEPARATE,
                    DataItem.Sign.TRAILING_SEPARATE);
        }

        @Test
        @DisplayName("optional fields return empty when null")
        void optionalFields() {
            var item = new DataItem(5, "FIELD", null,
                    null, null, null, null, null, null, null, false, false, false, false);
            assertThat(item.getPicture()).isEmpty();
            assertThat(item.getUsage()).isEmpty();
            assertThat(item.getSign()).isEmpty();
            assertThat(item.getValue()).isEmpty();
            assertThat(item.getOccurs()).isEmpty();
            assertThat(item.getRedefines()).isEmpty();
        }
    }

    @Nested
    @DisplayName("OccursClause")
    class OccursClauseTests {
        @Test
        @DisplayName("fixed occurs")
        void fixedOccurs() {
            var occurs = OccursClause.fixed(10);
            assertThat(occurs.getMinOccurs()).isEqualTo(10);
            assertThat(occurs.getMaxOccurs()).isEmpty();
            assertThat(occurs.isFixed()).isTrue();
            assertThat(occurs.isDynamic()).isFalse();
        }

        @Test
        @DisplayName("variable occurs (DEPENDING ON)")
        void variableOccurs() {
            var occurs = OccursClause.variable(1, 100, "WS-COUNT");
            assertThat(occurs.getMinOccurs()).isEqualTo(1);
            assertThat(occurs.getMaxOccurs()).isPresent().contains(100);
            assertThat(occurs.getDependingOn()).isPresent().contains("WS-COUNT");
            assertThat(occurs.isFixed()).isFalse();
            assertThat(occurs.isDynamic()).isTrue();
        }

        @Test
        @DisplayName("minOccurs must be >= 1")
        void minOccursValidation() {
            assertThatThrownBy(() -> OccursClause.fixed(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("toString for fixed")
        void toStringFixed() {
            assertThat(OccursClause.fixed(5).toString()).isEqualTo("OCCURS 5");
        }

        @Test
        @DisplayName("toString for variable")
        void toStringVariable() {
            assertThat(OccursClause.variable(1, 50, "CNT").toString())
                    .isEqualTo("OCCURS 1 TO 50 DEPENDING ON CNT");
        }
    }

    @Nested
    @DisplayName("ConditionName")
    class ConditionNameTests {
        @Test
        @DisplayName("single value condition")
        void singleValue() {
            var spec = ConditionName.ValueSpec.single("Y");
            var cond = new ConditionName("IS-ACTIVE", List.of(spec));

            assertThat(cond.getName()).isEqualTo("IS-ACTIVE");
            assertThat(cond.getValues()).hasSize(1);
            assertThat(cond.getValues().get(0).isRange()).isFalse();
            assertThat(cond.getValues().get(0).getValue()).isEqualTo("Y");
        }

        @Test
        @DisplayName("range value condition")
        void rangeValue() {
            var spec = ConditionName.ValueSpec.range("A", "Z");
            assertThat(spec.isRange()).isTrue();
            assertThat(spec.getValue()).isEqualTo("A");
            assertThat(spec.getThroughValue()).isEqualTo("Z");
        }

        @Test
        @DisplayName("null name throws NullPointerException")
        void nullName() {
            assertThatThrownBy(() -> new ConditionName(null, List.of()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null values default to empty list")
        void nullValues() {
            var cond = new ConditionName("FLAG", null);
            assertThat(cond.getValues()).isEmpty();
        }
    }

    @Nested
    @DisplayName("DataDivision")
    class DataDivisionTests {
        @Test
        @DisplayName("empty data division")
        void emptyDivision() {
            var div = new DataDivision(null, null, null, null);
            assertThat(div.getFileSection()).isEmpty();
            assertThat(div.getWorkingStorage()).isEmpty();
            assertThat(div.getLinkageSection()).isEmpty();
            assertThat(div.getLocalStorage()).isEmpty();
        }

        @Test
        @DisplayName("all lists are immutable")
        void immutableLists() {
            var item = new DataItem(1, "WS-ITEM", null,
                    null, null, null, null, null, null, null, false, false, false, false);
            var div = new DataDivision(null, List.of(item), null, null);

            assertThatThrownBy(() -> div.getWorkingStorage().add(item))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("file section with records")
        void fileSection() {
            var record = new DataItem(1, "ACCOUNT-RECORD", null,
                    null, null, null, null, null, null, null, false, false, false, false);
            var fs = new DataDivision.FileSection("ACCOUNT-FILE", List.of(record));

            assertThat(fs.getFileName()).isEqualTo("ACCOUNT-FILE");
            assertThat(fs.getRecords()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("ProcedureDivision")
    class ProcedureDivisionTests {
        @Test
        @DisplayName("empty procedure division")
        void emptyDivision() {
            var proc = new ProcedureDivision(null, null, null, null);
            assertThat(proc.getUsingParameters()).isEmpty();
            assertThat(proc.getReturning()).isNull();
            assertThat(proc.getSections()).isEmpty();
            assertThat(proc.getParagraphs()).isEmpty();
            assertThat(proc.hasSections()).isFalse();
        }

        @Test
        @DisplayName("procedure with sections")
        void withSections() {
            var para = new Paragraph("MAIN-PARA", Collections.emptyList(), null);
            var section = new ProcedureDivision.Section("MAIN-SECTION", 0, List.of(para));
            var proc = new ProcedureDivision(null, null, List.of(section), null);

            assertThat(proc.hasSections()).isTrue();
            assertThat(proc.getSections()).hasSize(1);
            assertThat(proc.getSections().get(0).getName()).isEqualTo("MAIN-SECTION");
        }

        @Test
        @DisplayName("procedure with USING parameters")
        void withUsing() {
            var proc = new ProcedureDivision(
                    List.of("PARAM-1", "PARAM-2"), "RETURN-VAL",
                    null, null);
            assertThat(proc.getUsingParameters()).containsExactly("PARAM-1", "PARAM-2");
            assertThat(proc.getReturning()).isEqualTo("RETURN-VAL");
        }
    }

    @Nested
    @DisplayName("Program (root)")
    class ProgramTests {
        @Test
        @DisplayName("create minimal program")
        void minimalProgram() {
            var data = new DataDivision(null, null, null, null);
            var proc = new ProcedureDivision(null, null, null, null);
            var meta = new SourceMetadata("test.cbl", Instant.now(), "1.0.0",
                    SourceMetadata.CobolDialect.ANSI_85, null);

            var program = new Program("TEST-PROG", null, null, data, proc, meta);

            assertThat(program.getProgramId()).isEqualTo("TEST-PROG");
            assertThat(program.getIdentification()).isNull();
            assertThat(program.getEnvironment()).isNull();
            assertThat(program.getData()).isNotNull();
            assertThat(program.getProcedure()).isNotNull();
            assertThat(program.getMetadata()).isNotNull();
        }

        @Test
        @DisplayName("null programId throws")
        void nullProgramId() {
            var data = new DataDivision(null, null, null, null);
            var proc = new ProcedureDivision(null, null, null, null);
            var meta = new SourceMetadata("test.cbl", Instant.now(), "1.0.0", null, null);

            assertThatThrownBy(() -> new Program(null, null, null, data, proc, meta))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null data division throws")
        void nullDataDivision() {
            var proc = new ProcedureDivision(null, null, null, null);
            var meta = new SourceMetadata("test.cbl", Instant.now(), "1.0.0", null, null);

            assertThatThrownBy(() -> new Program("TEST", null, null, null, proc, meta))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null procedure division throws")
        void nullProcedureDivision() {
            var data = new DataDivision(null, null, null, null);
            var meta = new SourceMetadata("test.cbl", Instant.now(), "1.0.0", null, null);

            assertThatThrownBy(() -> new Program("TEST", null, null, data, null, meta))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null metadata throws")
        void nullMetadata() {
            var data = new DataDivision(null, null, null, null);
            var proc = new ProcedureDivision(null, null, null, null);

            assertThatThrownBy(() -> new Program("TEST", null, null, data, proc, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("equality check")
        void equality() {
            var data = new DataDivision(null, null, null, null);
            var proc = new ProcedureDivision(null, null, null, null);
            var meta = new SourceMetadata("test.cbl", Instant.now(), "1.0.0", null, null);

            var p1 = new Program("TEST", null, null, data, proc, meta);
            var p2 = new Program("TEST", null, null, data, proc, meta);
            assertThat(p1).isEqualTo(p2);
        }

        @Test
        @DisplayName("toString includes programId")
        void toStringFormat() {
            var data = new DataDivision(null, null, null, null);
            var proc = new ProcedureDivision(null, null, null, null);
            var meta = new SourceMetadata("test.cbl", Instant.now(), "1.0.0", null, null);

            var program = new Program("BANK-ACCOUNT", null, null, data, proc, meta);
            assertThat(program.toString()).contains("BANK-ACCOUNT");
        }
    }

    @Nested
    @DisplayName("JSON Serialization")
    class JsonSerializationTests {
        private final ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module());

        @Test
        @DisplayName("SourceLocation round-trips through JSON")
        void sourceLocationJson() throws Exception {
            var loc = SourceLocation.of("TEST.cbl", 10, 7);
            String json = mapper.writeValueAsString(loc);
            var deserialized = mapper.readValue(json, SourceLocation.class);
            assertThat(deserialized).isEqualTo(loc);
        }

        @Test
        @DisplayName("Picture round-trips through JSON")
        void pictureJson() throws Exception {
            var pic = new Picture("9(5)V99", Picture.PictureCategory.NUMERIC,
                    7, 2, false, false, null);
            String json = mapper.writeValueAsString(pic);
            var deserialized = mapper.readValue(json, Picture.class);
            assertThat(deserialized).isEqualTo(pic);
        }

        @Test
        @DisplayName("DataItem round-trips through JSON")
        void dataItemJson() throws Exception {
            var pic = new Picture("X(10)", Picture.PictureCategory.ALPHANUMERIC,
                    10, 0, false, false, null);
            var item = new DataItem(5, "WS-NAME", pic,
                    DataItem.Usage.DISPLAY, null, null, null, null,
                    null, null, false, false, false, false);

            String json = mapper.writeValueAsString(item);
            var deserialized = mapper.readValue(json, DataItem.class);
            assertThat(deserialized).isEqualTo(item);
        }

        @Test
        @DisplayName("OccursClause round-trips through JSON")
        void occursJson() throws Exception {
            var occurs = OccursClause.variable(1, 100, "WS-COUNT");
            String json = mapper.writeValueAsString(occurs);
            var deserialized = mapper.readValue(json, OccursClause.class);
            assertThat(deserialized).isEqualTo(occurs);
        }

        @Test
        @DisplayName("ConditionName round-trips through JSON")
        void conditionNameJson() throws Exception {
            var cond = new ConditionName("IS-ACTIVE",
                    List.of(ConditionName.ValueSpec.single("Y")));
            String json = mapper.writeValueAsString(cond);
            var deserialized = mapper.readValue(json, ConditionName.class);
            assertThat(deserialized).isEqualTo(cond);
        }

        @Test
        @DisplayName("SourceMetadata round-trips through JSON")
        void metadataJson() throws Exception {
            var meta = new SourceMetadata("test.cbl", Instant.parse("2025-01-10T00:00:00Z"),
                    "1.0.0", SourceMetadata.CobolDialect.IBM_ENTERPRISE,
                    Map.of("key", "value"));
            String json = mapper.writeValueAsString(meta);
            var deserialized = mapper.readValue(json, SourceMetadata.class);
            assertThat(deserialized).isEqualTo(meta);
        }
    }
}
