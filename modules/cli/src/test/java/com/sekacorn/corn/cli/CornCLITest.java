/*
 * CornCLITest - Unit tests for CLI commands
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.cli;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CornCLI — Command-line interface")
class CornCLITest {

    @Nested
    @DisplayName("Main command")
    class MainCommandTests {
        @Test
        @DisplayName("--version prints version string")
        void versionFlag() {
            var cli = new CommandLine(new CornCLI());
            StringWriter sw = new StringWriter();
            cli.setOut(new PrintWriter(sw));
            int exitCode = cli.execute("--version");
            assertThat(exitCode).isEqualTo(0);
            assertThat(sw.toString()).contains("1.0.0-SNAPSHOT");
        }

        @Test
        @DisplayName("--help shows available commands")
        void helpFlag() {
            var cli = new CommandLine(new CornCLI());
            StringWriter sw = new StringWriter();
            cli.setOut(new PrintWriter(sw));
            int exitCode = cli.execute("--help");
            assertThat(exitCode).isEqualTo(0);
            String output = sw.toString();
            assertThat(output).contains("translate");
            assertThat(output).contains("analyze");
            assertThat(output).contains("validate");
        }

        @Test
        @DisplayName("no args shows usage")
        void noArgs() {
            var cli = new CommandLine(new CornCLI());
            StringWriter sw = new StringWriter();
            cli.setOut(new PrintWriter(sw));
            cli.setErr(new PrintWriter(new StringWriter()));
            int exitCode = cli.execute();
            // Should show usage / run the default runnable
            assertThat(exitCode).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Init command")
    class InitCommandTests {
        @Test
        @DisplayName("init creates project structure")
        void initCreatesStructure(@TempDir Path tempDir) {
            Path sourceDir = tempDir.resolve("cobol");
            Path outputDir = tempDir.resolve("java");
            sourceDir.toFile().mkdirs();

            var cli = new CommandLine(new CornCLI());
            StringWriter sw = new StringWriter();
            cli.setOut(new PrintWriter(sw));
            cli.setErr(new PrintWriter(new StringWriter()));

            int exitCode = cli.execute("init",
                    "--source", sourceDir.toString(),
                    "--output", outputDir.toString(),
                    "--workspace", tempDir.toString());

            assertThat(exitCode).isEqualTo(0);
            // Verify corn.json was created
            assertThat(tempDir.resolve("corn.json").toFile()).exists();
        }
    }

    @Nested
    @DisplayName("Translate command")
    class TranslateCommandTests {
        @Test
        @DisplayName("translate requires output directory")
        void translateRequiresOutput(@TempDir Path tempDir) {
            Path source = tempDir.resolve("test.cbl");
            try {
                Files.writeString(source, String.join("\n",
                        "000100 IDENTIFICATION DIVISION.",
                        "000200 PROGRAM-ID. TESTPROG.",
                        "000300 PROCEDURE DIVISION.",
                        "000400     STOP RUN.", ""));
            } catch (Exception e) {
                fail("Could not create test file", e);
            }

            var cli = new CommandLine(new CornCLI());
            StringWriter errSw = new StringWriter();
            cli.setErr(new PrintWriter(errSw));
            cli.setOut(new PrintWriter(new StringWriter()));

            // Missing required --output flag
            int exitCode = cli.execute("translate", source.toString());
            assertThat(exitCode).isNotEqualTo(0);
        }

        @Test
        @DisplayName("translate --dry-run mode")
        void translateDryRun(@TempDir Path tempDir) {
            Path source = tempDir.resolve("test.cbl");
            Path output = tempDir.resolve("output");
            try {
                Files.writeString(source, String.join("\n",
                        "000100 IDENTIFICATION DIVISION.",
                        "000200 PROGRAM-ID. TESTPROG.",
                        "000300 PROCEDURE DIVISION.",
                        "000400     STOP RUN.", ""));
            } catch (Exception e) {
                fail("Could not create test file", e);
            }

            var cli = new CommandLine(new CornCLI());
            StringWriter sw = new StringWriter();
            cli.setOut(new PrintWriter(sw));
            cli.setErr(new PrintWriter(new StringWriter()));

            int exitCode = cli.execute("translate",
                    source.toString(),
                    "--output", output.toString(),
                    "--dry-run");
            assertThat(exitCode).isEqualTo(0);
        }

        @Test
        @DisplayName("translate supports level 2 and rejects unimplemented levels")
        void codegenLevels(@TempDir Path tempDir) {
            Path source = tempDir.resolve("test.cbl");
            Path output = tempDir.resolve("output");
            try {
                Files.writeString(source, String.join("\n",
                        "000100 IDENTIFICATION DIVISION.",
                        "000200 PROGRAM-ID. TESTPROG.",
                        "000300 PROCEDURE DIVISION.",
                        "000400     STOP RUN.", ""));
            } catch (Exception e) {
                fail("Could not create test file", e);
            }

            var cli = new CommandLine(new CornCLI());
            cli.setOut(new PrintWriter(new StringWriter()));
            cli.setErr(new PrintWriter(new StringWriter()));

            int supportedExitCode = cli.execute("translate",
                    source.toString(),
                    "--output", output.toString(),
                    "--codegen-level", "2",
                    "--dry-run");
            assertThat(supportedExitCode).isEqualTo(0);

            for (int level : new int[]{0, 1, 3}) {
                int exitCode = cli.execute("translate",
                        source.toString(),
                        "--output", output.toString(),
                        "--codegen-level", String.valueOf(level),
                        "--dry-run");
                assertThat(exitCode)
                        .as("Level %d should fail until implemented", level)
                        .isEqualTo(1);
            }
        }

        @Test
        @DisplayName("translate fails fast when copybook paths are requested")
        void translateRejectsCopybookPath(@TempDir Path tempDir) {
            Path source = tempDir.resolve("test.cbl");
            Path output = tempDir.resolve("output");
            try {
                Files.writeString(source, String.join("\n",
                        "000100 IDENTIFICATION DIVISION.",
                        "000200 PROGRAM-ID. TESTPROG.",
                        "000300 PROCEDURE DIVISION.",
                        "000400     STOP RUN.", ""));
            } catch (Exception e) {
                fail("Could not create test file", e);
            }

            var cli = new CommandLine(new CornCLI());
            cli.setOut(new PrintWriter(new StringWriter()));
            cli.setErr(new PrintWriter(new StringWriter()));

            int exitCode = cli.execute("translate",
                    source.toString(),
                    "--output", output.toString(),
                    "--copybook-path", tempDir.toString());
            assertThat(exitCode).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Analyze command")
    class AnalyzeCommandTests {
        @Test
        @DisplayName("analyze with dialect flag")
        void analyzeWithDialect(@TempDir Path tempDir) {
            Path source = tempDir.resolve("test.cbl");
            try {
                Files.writeString(source, String.join("\n",
                        "000100 IDENTIFICATION DIVISION.",
                        "000200 PROGRAM-ID. TESTPROG.",
                        "000300 PROCEDURE DIVISION.",
                        "000400     STOP RUN.", ""));
            } catch (Exception e) {
                fail("Could not create test file", e);
            }

            var cli = new CommandLine(new CornCLI());
            StringWriter sw = new StringWriter();
            cli.setOut(new PrintWriter(sw));
            cli.setErr(new PrintWriter(new StringWriter()));

            int exitCode = cli.execute("analyze",
                    source.toString(),
                    "--dialect", "IBM_ENTERPRISE",
                    "--output", tempDir.resolve("out").toString());
            assertThat(exitCode).isEqualTo(0);
        }

        @Test
        @DisplayName("analyze supports explicit thread count")
        void analyzeWithThreads(@TempDir Path tempDir) {
            Path sourceA = tempDir.resolve("test-a.cbl");
            Path sourceB = tempDir.resolve("test-b.cbl");
            try {
                Files.writeString(sourceA, String.join("\n",
                        "000100 IDENTIFICATION DIVISION.",
                        "000200 PROGRAM-ID. TESTA.",
                        "000300 PROCEDURE DIVISION.",
                        "000400     STOP RUN.", ""));
                Files.writeString(sourceB, String.join("\n",
                        "000100 IDENTIFICATION DIVISION.",
                        "000200 PROGRAM-ID. TESTB.",
                        "000300 PROCEDURE DIVISION.",
                        "000400     STOP RUN.", ""));
            } catch (Exception e) {
                fail("Could not create test files", e);
            }

            var cli = new CommandLine(new CornCLI());
            cli.setOut(new PrintWriter(new StringWriter()));
            cli.setErr(new PrintWriter(new StringWriter()));

            int exitCode = cli.execute("analyze",
                    tempDir.toString(),
                    "--threads", "2",
                    "--output", tempDir.resolve("out").toString());
            assertThat(exitCode).isEqualTo(0);
        }

        @Test
        @DisplayName("analyze fails fast when copybook paths are requested")
        void analyzeRejectsCopybookPath(@TempDir Path tempDir) {
            Path source = tempDir.resolve("test.cbl");
            try {
                Files.writeString(source, String.join("\n",
                        "000100 IDENTIFICATION DIVISION.",
                        "000200 PROGRAM-ID. TESTPROG.",
                        "000300 PROCEDURE DIVISION.",
                        "000400     STOP RUN.", ""));
            } catch (Exception e) {
                fail("Could not create test file", e);
            }

            var cli = new CommandLine(new CornCLI());
            cli.setOut(new PrintWriter(new StringWriter()));
            cli.setErr(new PrintWriter(new StringWriter()));

            int exitCode = cli.execute("analyze",
                    source.toString(),
                    "--copybook-path", tempDir.toString(),
                    "--output", tempDir.resolve("out").toString());
            assertThat(exitCode).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Report command")
    class ReportCommandTests {
        @Test
        @DisplayName("report accepts all formats")
        void reportFormats(@TempDir Path tempDir) {
            var cli = new CommandLine(new CornCLI());
            cli.setOut(new PrintWriter(new StringWriter()));
            cli.setErr(new PrintWriter(new StringWriter()));

            for (String format : new String[]{"HTML", "JSON", "PDF", "MARKDOWN"}) {
                int exitCode = cli.execute("report",
                        "--workspace", tempDir.toString(),
                        "--output", tempDir.resolve("report." + format.toLowerCase()).toString(),
                        "--format", format);
                assertThat(exitCode)
                        .as("Format %s should succeed", format)
                        .isEqualTo(0);
            }
        }
    }

    @Nested
    @DisplayName("Validate command")
    class ValidateCommandTests {
        @Test
        @DisplayName("validate executes generated Java and matches expected output fixture")
        void validateMatchesExpectedOutput(@TempDir Path tempDir) {
            Path source = tempDir.resolve("test.cbl");
            Path testData = tempDir.resolve("test-data");
            try {
                Files.writeString(source, String.join("\n",
                        "000100 IDENTIFICATION DIVISION.",
                        "000200 PROGRAM-ID. HELLOPROG.",
                        "000300 PROCEDURE DIVISION.",
                        "000400     DISPLAY \"HELLO MVP\".",
                        "000500     STOP RUN.", ""));
                Files.createDirectories(testData);
                Files.writeString(testData.resolve("HELLOPROG.out"), "HELLO MVP");
            } catch (Exception e) {
                fail("Could not create test file", e);
            }

            var cli = new CommandLine(new CornCLI());
            StringWriter sw = new StringWriter();
            cli.setOut(new PrintWriter(new StringWriter()));
            cli.setErr(new PrintWriter(sw));

            int exitCode = cli.execute("validate",
                    source.toString(),
                    "--output", tempDir.resolve("validation").toString(),
                    "--test-data", testData.toString());
            assertThat(exitCode).isEqualTo(0);
            assertThat(tempDir.resolve("validation").resolve("validation-report.json")).exists();
        }

        @Test
        @DisplayName("validate fails when expected output does not match")
        void validateFailsOnOutputMismatch(@TempDir Path tempDir) {
            Path source = tempDir.resolve("test.cbl");
            Path testData = tempDir.resolve("test-data");
            try {
                Files.writeString(source, String.join("\n",
                        "000100 IDENTIFICATION DIVISION.",
                        "000200 PROGRAM-ID. HELLOPROG.",
                        "000300 PROCEDURE DIVISION.",
                        "000400     DISPLAY \"HELLO MVP\".",
                        "000500     STOP RUN.", ""));
                Files.createDirectories(testData);
                Files.writeString(testData.resolve("HELLOPROG.out"), "WRONG OUTPUT");
            } catch (Exception e) {
                fail("Could not create test file", e);
            }

            var cli = new CommandLine(new CornCLI());
            cli.setOut(new PrintWriter(new StringWriter()));
            cli.setErr(new PrintWriter(new StringWriter()));

            int exitCode = cli.execute("validate",
                    source.toString(),
                    "--output", tempDir.resolve("validation").toString(),
                    "--test-data", testData.toString());
            assertThat(exitCode).isEqualTo(1);
        }

        @Test
        @DisplayName("validate supports explicit thread count")
        void validateWithThreads(@TempDir Path tempDir) {
            Path sourceA = tempDir.resolve("test-a.cbl");
            Path sourceB = tempDir.resolve("test-b.cbl");
            Path testData = tempDir.resolve("test-data");
            try {
                Files.writeString(sourceA, String.join("\n",
                        "000100 IDENTIFICATION DIVISION.",
                        "000200 PROGRAM-ID. THREADA.",
                        "000300 PROCEDURE DIVISION.",
                        "000400     DISPLAY \"A\".",
                        "000500     STOP RUN.", ""));
                Files.writeString(sourceB, String.join("\n",
                        "000100 IDENTIFICATION DIVISION.",
                        "000200 PROGRAM-ID. THREADB.",
                        "000300 PROCEDURE DIVISION.",
                        "000400     DISPLAY \"B\".",
                        "000500     STOP RUN.", ""));
                Files.createDirectories(testData);
                Files.writeString(testData.resolve("THREADA.out"), "A");
                Files.writeString(testData.resolve("THREADB.out"), "B");
            } catch (Exception e) {
                fail("Could not create test files", e);
            }

            var cli = new CommandLine(new CornCLI());
            cli.setOut(new PrintWriter(new StringWriter()));
            cli.setErr(new PrintWriter(new StringWriter()));

            int exitCode = cli.execute("validate",
                    tempDir.toString(),
                    "--threads", "2",
                    "--output", tempDir.resolve("validation").toString(),
                    "--test-data", testData.toString());
            assertThat(exitCode).isEqualTo(0);
        }

        @Test
        @DisplayName("validate fails fast when copybook paths are requested")
        void validateRejectsCopybookPath(@TempDir Path tempDir) {
            Path source = tempDir.resolve("test.cbl");
            try {
                Files.writeString(source, String.join("\n",
                        "000100 IDENTIFICATION DIVISION.",
                        "000200 PROGRAM-ID. TESTPROG.",
                        "000300 PROCEDURE DIVISION.",
                        "000400     STOP RUN.", ""));
            } catch (Exception e) {
                fail("Could not create test file", e);
            }

            var cli = new CommandLine(new CornCLI());
            cli.setOut(new PrintWriter(new StringWriter()));
            cli.setErr(new PrintWriter(new StringWriter()));

            int exitCode = cli.execute("validate",
                    source.toString(),
                    "--copybook-path", tempDir.toString(),
                    "--output", tempDir.resolve("validation").toString());
            assertThat(exitCode).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Refactor command")
    class RefactorCommandTests {
        @Test
        @DisplayName("refactor with LLM provider NONE returns 1 (no provider)")
        void refactorNone(@TempDir Path tempDir) {
            Path source = tempDir.resolve("Test.java");
            try {
                Files.writeString(source, "public class Test {}");
            } catch (Exception e) {
                fail("Could not create test file", e);
            }

            var cli = new CommandLine(new CornCLI());
            cli.setOut(new PrintWriter(new StringWriter()));
            cli.setErr(new PrintWriter(new StringWriter()));

            // NONE provider exits with 1, prompting user to select a real provider
            int exitCode = cli.execute("refactor",
                    source.toString(),
                    "--llm-provider", "NONE",
                    "--output", tempDir.resolve("refactored").toString());
            assertThat(exitCode).isEqualTo(1);
        }
    }
}
