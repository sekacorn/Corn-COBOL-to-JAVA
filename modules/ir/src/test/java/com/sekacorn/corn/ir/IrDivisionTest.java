/*
 * IrDivisionTest - Unit tests for IR division classes
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.sekacorn.corn.ir.EnvironmentDivision.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("IR Division — IdentificationDivision, EnvironmentDivision, FileControlEntry, Paragraph")
class IrDivisionTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new Jdk8Module());

    // ─── IdentificationDivision ───────────────────────────

    @Nested
    @DisplayName("IdentificationDivision")
    class IdentificationTests {
        @Test
        @DisplayName("minimal with just programId")
        void minimalProgram() {
            var id = new IdentificationDivision("PAYROLL", null, null, null, null, null);
            assertThat(id.getProgramId()).isEqualTo("PAYROLL");
            assertThat(id.getAuthor()).isEmpty();
            assertThat(id.getDateWritten()).isEmpty();
            assertThat(id.getDateCompiled()).isEmpty();
            assertThat(id.getSecurity()).isEmpty();
            assertThat(id.getRemarks()).isEmpty();
        }

        @Test
        @DisplayName("full identification division")
        void fullProgram() {
            var id = new IdentificationDivision("PAYROLL", "Sekacorn",
                    "2025-01-10", "2025-01-15", "CONFIDENTIAL", "Main payroll processing");
            assertThat(id.getAuthor()).hasValue("Sekacorn");
            assertThat(id.getDateWritten()).hasValue("2025-01-10");
            assertThat(id.getDateCompiled()).hasValue("2025-01-15");
            assertThat(id.getSecurity()).hasValue("CONFIDENTIAL");
            assertThat(id.getRemarks()).hasValue("Main payroll processing");
        }

        @Test
        @DisplayName("programId is required")
        void nullProgramId() {
            assertThatThrownBy(() -> new IdentificationDivision(null, null, null, null, null, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("equality")
        void equality() {
            var a = new IdentificationDivision("PGM", "Author", null, null, null, null);
            var b = new IdentificationDivision("PGM", "Author", null, null, null, null);
            var c = new IdentificationDivision("OTHER", "Author", null, null, null, null);
            assertThat(a).isEqualTo(b);
            assertThat(a).isNotEqualTo(c);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("JSON round-trip")
        void jsonRoundTrip() throws Exception {
            var id = new IdentificationDivision("ACCT-PROC", "Sekacorn",
                    "2025-01-10", null, null, "Account processing");
            String json = mapper.writeValueAsString(id);
            var deserialized = mapper.readValue(json, IdentificationDivision.class);
            assertThat(deserialized.getProgramId()).isEqualTo("ACCT-PROC");
            assertThat(deserialized.getAuthor()).hasValue("Sekacorn");
            assertThat(deserialized.getRemarks()).hasValue("Account processing");
        }
    }

    // ─── EnvironmentDivision ──────────────────────────────

    @Nested
    @DisplayName("EnvironmentDivision")
    class EnvironmentTests {
        @Test
        @DisplayName("with configuration section")
        void withConfiguration() {
            var special = new SpecialName("CONSOLE", "SYSIN");
            var config = new ConfigurationSection("IBM-3090", "IBM-3090", List.of(special));
            var env = new EnvironmentDivision(config, null);

            assertThat(env.getConfiguration()).isNotNull();
            assertThat(env.getConfiguration().getSourceComputer()).isEqualTo("IBM-3090");
            assertThat(env.getConfiguration().getObjectComputer()).isEqualTo("IBM-3090");
            assertThat(env.getConfiguration().getSpecialNames()).hasSize(1);
            assertThat(env.getInputOutput()).isNull();
        }

        @Test
        @DisplayName("with input-output section")
        void withInputOutput() {
            var fileEntry = new FileControlEntry("CUSTOMER-FILE", "CUSTFILE",
                    FileControlEntry.FileOrganization.INDEXED,
                    FileControlEntry.AccessMode.DYNAMIC,
                    "CUST-KEY", null, null, null, "WS-FILE-STATUS");
            var io = new InputOutputSection(List.of(fileEntry));
            var env = new EnvironmentDivision(null, io);

            assertThat(env.getInputOutput()).isNotNull();
            assertThat(env.getInputOutput().getFileControl()).hasSize(1);
        }

        @Test
        @DisplayName("null special names defaults to empty list")
        void nullSpecialNames() {
            var config = new ConfigurationSection("PC", "PC", null);
            assertThat(config.getSpecialNames()).isEmpty();
        }

        @Test
        @DisplayName("null file control defaults to empty list")
        void nullFileControl() {
            var io = new InputOutputSection(null);
            assertThat(io.getFileControl()).isEmpty();
        }

        @Test
        @DisplayName("SpecialName equality")
        void specialNameEquality() {
            var a = new SpecialName("CONSOLE", "SYSIN");
            var b = new SpecialName("CONSOLE", "SYSIN");
            var c = new SpecialName("PRINTER", "SYSOUT");
            assertThat(a).isEqualTo(b);
            assertThat(a).isNotEqualTo(c);
        }

        @Test
        @DisplayName("EnvironmentDivision equality")
        void equality() {
            var config = new ConfigurationSection("PC", "PC", List.of());
            var a = new EnvironmentDivision(config, null);
            var b = new EnvironmentDivision(config, null);
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("JSON round-trip")
        void jsonRoundTrip() throws Exception {
            var special = new SpecialName("CONSOLE", "SYSIN");
            var config = new ConfigurationSection("MAINFRAME", "MAINFRAME", List.of(special));
            var io = new InputOutputSection(List.of());
            var env = new EnvironmentDivision(config, io);

            String json = mapper.writeValueAsString(env);
            var deserialized = mapper.readValue(json, EnvironmentDivision.class);
            assertThat(deserialized).isEqualTo(env);
        }
    }

    // ─── FileControlEntry ─────────────────────────────────

    @Nested
    @DisplayName("FileControlEntry")
    class FileControlTests {
        @Test
        @DisplayName("sequential file with defaults")
        void sequentialDefaults() {
            var entry = new FileControlEntry("INPUT-FILE", "INFILE",
                    null, null, null, null, null, null, null);
            assertThat(entry.getFileName()).isEqualTo("INPUT-FILE");
            assertThat(entry.getAssignTo()).hasValue("INFILE");
            assertThat(entry.getOrganization()).isEqualTo(FileControlEntry.FileOrganization.SEQUENTIAL);
            assertThat(entry.getAccessMode()).isEqualTo(FileControlEntry.AccessMode.SEQUENTIAL);
            assertThat(entry.getRecordKey()).isEmpty();
            assertThat(entry.getAlternateRecordKey()).isEmpty();
            assertThat(entry.getFileStatus()).isEmpty();
        }

        @Test
        @DisplayName("indexed file with keys")
        void indexedFile() {
            var entry = new FileControlEntry("MASTER-FILE", "MASTFILE",
                    FileControlEntry.FileOrganization.INDEXED,
                    FileControlEntry.AccessMode.DYNAMIC,
                    "ACCT-NUMBER", "ALT-KEY", null, null, "WS-STATUS");
            assertThat(entry.getOrganization()).isEqualTo(FileControlEntry.FileOrganization.INDEXED);
            assertThat(entry.getAccessMode()).isEqualTo(FileControlEntry.AccessMode.DYNAMIC);
            assertThat(entry.getRecordKey()).hasValue("ACCT-NUMBER");
            assertThat(entry.getAlternateRecordKey()).hasValue("ALT-KEY");
            assertThat(entry.getFileStatus()).hasValue("WS-STATUS");
        }

        @Test
        @DisplayName("relative file")
        void relativeFile() {
            var entry = new FileControlEntry("REL-FILE", null,
                    FileControlEntry.FileOrganization.RELATIVE,
                    FileControlEntry.AccessMode.RANDOM,
                    null, null, null, null, null);
            assertThat(entry.getOrganization()).isEqualTo(FileControlEntry.FileOrganization.RELATIVE);
            assertThat(entry.getAssignTo()).isEmpty();
        }

        @Test
        @DisplayName("fileName is required")
        void nullFileName() {
            assertThatThrownBy(() -> new FileControlEntry(null, null, null, null, null, null, null, null, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("all organization types defined")
        void organizationTypes() {
            assertThat(FileControlEntry.FileOrganization.values()).hasSize(3);
        }

        @Test
        @DisplayName("all access modes defined")
        void accessModes() {
            assertThat(FileControlEntry.AccessMode.values()).hasSize(3);
        }

        @Test
        @DisplayName("equality")
        void equality() {
            var a = new FileControlEntry("F", "A", null, null, null, null, null, null, null);
            var b = new FileControlEntry("F", "A", null, null, null, null, null, null, null);
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("JSON round-trip")
        void jsonRoundTrip() throws Exception {
            var entry = new FileControlEntry("VSAM-FILE", "DD-VSAM",
                    FileControlEntry.FileOrganization.INDEXED,
                    FileControlEntry.AccessMode.DYNAMIC,
                    "PRIMARY-KEY", "ALT-KEY", null, null, "FILE-STATUS");
            String json = mapper.writeValueAsString(entry);
            var deserialized = mapper.readValue(json, FileControlEntry.class);
            assertThat(deserialized).isEqualTo(entry);
        }
    }

    // ─── Paragraph ────────────────────────────────────────

    @Nested
    @DisplayName("Paragraph")
    class ParagraphTests {
        @Test
        @DisplayName("named paragraph with no statements")
        void emptyParagraph() {
            var para = new Paragraph("MAIN-LOGIC", List.of(), null);
            assertThat(para.getName()).isEqualTo("MAIN-LOGIC");
            assertThat(para.getStatements()).isEmpty();
            assertThat(para.getLocation()).isNull();
        }

        @Test
        @DisplayName("paragraph with statements and location")
        void withStatements() {
            var loc = SourceLocation.of("PGM.cbl", 100, 12);
            var para = new Paragraph("PROCESS-RECORD", List.of(), loc);
            assertThat(para.getLocation()).isEqualTo(loc);
        }

        @Test
        @DisplayName("name is required")
        void nullName() {
            assertThatThrownBy(() -> new Paragraph(null, List.of(), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null statements defaults to empty list")
        void nullStatements() {
            var para = new Paragraph("P", null, null);
            assertThat(para.getStatements()).isEmpty();
        }

        @Test
        @DisplayName("toString includes name and statement count")
        void toStringFormat() {
            var para = new Paragraph("READ-LOOP", List.of(), null);
            assertThat(para.toString()).contains("READ-LOOP").contains("0");
        }

        @Test
        @DisplayName("equality")
        void equality() {
            var a = new Paragraph("A", List.of(), null);
            var b = new Paragraph("A", List.of(), null);
            var c = new Paragraph("B", List.of(), null);
            assertThat(a).isEqualTo(b);
            assertThat(a).isNotEqualTo(c);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("JSON round-trip")
        void jsonRoundTrip() throws Exception {
            var loc = SourceLocation.of("TEST.cbl", 50, 12);
            var para = new Paragraph("CALC-TOTALS", List.of(), loc);
            String json = mapper.writeValueAsString(para);
            var deserialized = mapper.readValue(json, Paragraph.class);
            assertThat(deserialized).isEqualTo(para);
        }
    }
}
