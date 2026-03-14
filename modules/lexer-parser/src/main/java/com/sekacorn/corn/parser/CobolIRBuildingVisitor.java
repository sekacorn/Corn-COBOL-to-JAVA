/*
 * CobolIRBuildingVisitor - Main ANTLR visitor that builds Program IR
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.parser;

import com.sekacorn.corn.ir.*;
import com.sekacorn.corn.ir.Paragraph;
import com.sekacorn.corn.ir.stmt.Statement;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Walks the ANTLR parse tree and constructs the complete Program IR.
 */
public class CobolIRBuildingVisitor extends CobolParserBaseVisitor<Object> {

    private final String fileName;
    private final SourceMetadata.CobolDialect dialect;
    private final ExpressionBuilder exprBuilder;
    private final StatementBuilder stmtBuilder;

    public CobolIRBuildingVisitor(String fileName, SourceMetadata.CobolDialect dialect) {
        this.fileName = fileName;
        this.dialect = dialect;
        this.exprBuilder = new ExpressionBuilder(fileName);
        this.stmtBuilder = new StatementBuilder(exprBuilder, fileName);
    }

    // ════════════════════════════════════════════════════════
    // Top-level
    // ════════════════════════════════════════════════════════

    @Override
    public Program visitProgram(CobolParser.ProgramContext ctx) {
        IdentificationDivision idDiv = visitIdentificationDivision(ctx.identificationDivision());
        String programId = idDiv.getProgramId();

        EnvironmentDivision envDiv = ctx.environmentDivision() != null
                ? visitEnvironmentDivision(ctx.environmentDivision()) : null;

        DataDivision dataDiv = ctx.dataDivision() != null
                ? visitDataDivision(ctx.dataDivision())
                : new DataDivision(null, null, null, null);

        ProcedureDivision procDiv = ctx.procedureDivision() != null
                ? visitProcedureDivision(ctx.procedureDivision())
                : new ProcedureDivision(null, null, null, null);

        SourceMetadata metadata = new SourceMetadata(
                fileName, Instant.now(), "Corn 1.0.0-SNAPSHOT", dialect, null);

        return new Program(programId, idDiv, envDiv, dataDiv, procDiv, metadata);
    }

    // ════════════════════════════════════════════════════════
    // IDENTIFICATION DIVISION
    // ════════════════════════════════════════════════════════

    @Override
    public IdentificationDivision visitIdentificationDivision(CobolParser.IdentificationDivisionContext ctx) {
        String programId = ctx.programIdParagraph().programName().getText().replace("\"", "").replace("'", "");
        String author = null;
        String dateWritten = null;
        String dateCompiled = null;
        String security = null;
        String remarks = null;

        for (var para : ctx.identificationParagraph()) {
            if (para instanceof CobolParser.AuthorParagraphContext a) {
                author = a.freeText() != null ? a.freeText().getText().trim() : "";
            } else if (para instanceof CobolParser.DateWrittenParagraphContext d) {
                dateWritten = d.freeText() != null ? d.freeText().getText().trim() : "";
            } else if (para instanceof CobolParser.DateCompiledParagraphContext d) {
                dateCompiled = d.freeText() != null ? d.freeText().getText().trim() : "";
            } else if (para instanceof CobolParser.SecurityParagraphContext s) {
                security = s.freeText() != null ? s.freeText().getText().trim() : "";
            } else if (para instanceof CobolParser.RemarksParagraphContext r) {
                remarks = r.freeText() != null ? r.freeText().getText().trim() : "";
            }
        }

        return new IdentificationDivision(programId, author, dateWritten, dateCompiled, security, remarks);
    }

    // ════════════════════════════════════════════════════════
    // ENVIRONMENT DIVISION
    // ════════════════════════════════════════════════════════

    @Override
    public EnvironmentDivision visitEnvironmentDivision(CobolParser.EnvironmentDivisionContext ctx) {
        EnvironmentDivision.ConfigurationSection configSection = null;
        if (ctx.configurationSection() != null) {
            configSection = visitConfigurationSection(ctx.configurationSection());
        }

        EnvironmentDivision.InputOutputSection ioSection = null;
        if (ctx.inputOutputSection() != null) {
            ioSection = visitInputOutputSection(ctx.inputOutputSection());
        }

        return new EnvironmentDivision(configSection, ioSection);
    }

    public EnvironmentDivision.ConfigurationSection visitConfigurationSection(
            CobolParser.ConfigurationSectionContext ctx) {
        String sourceComputer = null;
        String objectComputer = null;
        List<EnvironmentDivision.SpecialName> specialNames = new ArrayList<>();

        for (var para : ctx.configurationParagraph()) {
            if (para instanceof CobolParser.SourceComputerParagraphContext src) {
                sourceComputer = src.freeText() != null ? src.freeText().getText().trim() : "";
            } else if (para instanceof CobolParser.ObjectComputerParagraphContext obj) {
                objectComputer = obj.freeText() != null ? obj.freeText().getText().trim() : "";
            }
        }

        return new EnvironmentDivision.ConfigurationSection(sourceComputer, objectComputer, specialNames);
    }

    public EnvironmentDivision.InputOutputSection visitInputOutputSection(
            CobolParser.InputOutputSectionContext ctx) {
        List<FileControlEntry> fileControl = new ArrayList<>();
        if (ctx.fileControlParagraph() != null) {
            for (var select : ctx.fileControlParagraph().selectClause()) {
                fileControl.add(buildFileControlEntry(select));
            }
        }
        return new EnvironmentDivision.InputOutputSection(fileControl);
    }

    private FileControlEntry buildFileControlEntry(CobolParser.SelectClauseContext ctx) {
        String name = ctx.fileName.getText();
        String assignTo = ctx.assignName() != null ? ctx.assignName().getText().replace("\"", "") : null;

        FileControlEntry.FileOrganization org = FileControlEntry.FileOrganization.SEQUENTIAL;
        FileControlEntry.AccessMode access = FileControlEntry.AccessMode.SEQUENTIAL;
        String recordKey = null;
        String altKey = null;
        String relativeKey = null;
        String fileStatus = null;

        for (var option : ctx.fileControlOption()) {
            if (option instanceof CobolParser.OrganizationOptionContext orgOpt) {
                org = switch (orgOpt.fileOrganization().getText().toUpperCase()) {
                    case "INDEXED" -> FileControlEntry.FileOrganization.INDEXED;
                    case "RELATIVE" -> FileControlEntry.FileOrganization.RELATIVE;
                    default -> FileControlEntry.FileOrganization.SEQUENTIAL;
                };
            } else if (option instanceof CobolParser.AccessModeOptionContext accOpt) {
                access = switch (accOpt.accessMode().getText().toUpperCase()) {
                    case "RANDOM" -> FileControlEntry.AccessMode.RANDOM;
                    case "DYNAMIC" -> FileControlEntry.AccessMode.DYNAMIC;
                    default -> FileControlEntry.AccessMode.SEQUENTIAL;
                };
            } else if (option instanceof CobolParser.RecordKeyOptionContext keyOpt) {
                recordKey = keyOpt.keyName.getText();
            } else if (option instanceof CobolParser.AlternateKeyOptionContext altOpt) {
                altKey = altOpt.altKey.getText();
            } else if (option instanceof CobolParser.RelativeKeyOptionContext relOpt) {
                relativeKey = relOpt.relKey.getText();
            } else if (option instanceof CobolParser.FileStatusOptionContext statOpt) {
                fileStatus = statOpt.statusVar.getText();
            } else if (option instanceof CobolParser.StatusOptionContext statOpt) {
                fileStatus = statOpt.statusVar.getText();
            }
        }

        return new FileControlEntry(name, assignTo, org, access, recordKey, altKey,
                null, relativeKey, fileStatus);
    }

    // ════════════════════════════════════════════════════════
    // DATA DIVISION
    // ════════════════════════════════════════════════════════

    @Override
    public DataDivision visitDataDivision(CobolParser.DataDivisionContext ctx) {
        List<DataDivision.FileSection> fileSections = new ArrayList<>();
        if (ctx.fileSection() != null) {
            for (var fd : ctx.fileSection().fileDescriptionEntry()) {
                String name = fd.fileName.getText();
                List<DataItem> records = buildDataItems(fd.dataItemEntry());
                fileSections.add(new DataDivision.FileSection(name, records));
            }
        }

        List<DataItem> workingStorage = Collections.emptyList();
        if (ctx.workingStorageSection() != null) {
            workingStorage = buildDataItems(ctx.workingStorageSection().dataItemEntry());
        }

        List<DataItem> linkage = Collections.emptyList();
        if (ctx.linkageSection() != null) {
            linkage = buildDataItems(ctx.linkageSection().dataItemEntry());
        }

        List<DataItem> localStorage = Collections.emptyList();
        if (ctx.localStorageSection() != null) {
            localStorage = buildDataItems(ctx.localStorageSection().dataItemEntry());
        }

        return new DataDivision(fileSections, workingStorage, linkage, localStorage);
    }

    private List<DataItem> buildDataItems(List<CobolParser.DataItemEntryContext> entries) {
        List<DataItemBuilder.FlatDataItem> flat = new ArrayList<>();

        for (var entry : entries) {
            if (entry instanceof CobolParser.RegularDataItemContext reg) {
                flat.add(buildFlatDataItem(reg));
            } else if (entry instanceof CobolParser.ConditionNameEntryContext cond) {
                flat.add(buildConditionItem(cond));
            }
        }

        return DataItemBuilder.build(flat);
    }

    private DataItemBuilder.FlatDataItem buildFlatDataItem(CobolParser.RegularDataItemContext ctx) {
        int level = Integer.parseInt(ctx.levelNumber().getText());
        String name = ctx.dataName().getText();
        boolean isFiller = name.equalsIgnoreCase("FILLER");

        Picture picture = null;
        DataItem.Usage usage = null;
        DataItem.Sign sign = null;
        String value = null;
        String redefines = null;
        OccursClause occurs = null;
        boolean justified = false;
        boolean blankWhenZero = false;
        boolean synchronizedField = false;

        for (var clause : ctx.dataItemClause()) {
            if (clause.picClause() != null) {
                String picString = clause.picClause().PIC_STRING().getText().trim();
                picture = PictureAnalyzer.analyze(picString);
            } else if (clause.valueClause() != null) {
                String valueSign = clause.valueClause().MINUS() != null ? "-" : "";
                value = valueSign + extractLiteralValue(clause.valueClause().literal());
            } else if (clause.usageClause() != null) {
                usage = mapUsage(clause.usageClause().usageType());
            } else if (clause.signClause() != null) {
                sign = mapSign(clause.signClause());
            } else if (clause.redefinesClause() != null) {
                redefines = clause.redefinesClause().IDENTIFIER().getText();
            } else if (clause.occursClause() != null) {
                occurs = buildOccursClause(clause.occursClause());
            } else if (clause.justifiedClause() != null) {
                justified = true;
            } else if (clause.blankWhenZeroClause() != null) {
                blankWhenZero = true;
            } else if (clause.synchronizedClause() != null) {
                synchronizedField = true;
            }
        }

        return new DataItemBuilder.FlatDataItem(
                level, name, isFiller, picture, usage, sign, value, redefines,
                occurs, justified, blankWhenZero, synchronizedField, null);
    }

    private DataItemBuilder.FlatDataItem buildConditionItem(CobolParser.ConditionNameEntryContext ctx) {
        int level = Integer.parseInt(ctx.INTEGERLITERAL().getText());
        String name = ctx.dataName().getText();
        List<ConditionName.ValueSpec> values = new ArrayList<>();
        for (var vs : ctx.valueSpec()) {
            String val = extractLiteralValue(vs.literal(0));
            String thruVal = vs.literal().size() > 1 ? extractLiteralValue(vs.literal(1)) : null;
            values.add(new ConditionName.ValueSpec(val, thruVal));
        }
        return new DataItemBuilder.FlatDataItem(
                level, name, false, null, null, null, null, null,
                null, false, false, false, values);
    }

    private String extractLiteralValue(CobolParser.LiteralContext ctx) {
        if (ctx.STRINGLITERAL() != null) {
            String text = ctx.STRINGLITERAL().getText();
            return text.substring(1, text.length() - 1);
        }
        return ctx.getText();
    }

    private DataItem.Usage mapUsage(CobolParser.UsageTypeContext ctx) {
        String text = ctx.getText().toUpperCase().replace("COMPUTATIONAL", "COMP");
        return switch (text) {
            case "DISPLAY" -> DataItem.Usage.DISPLAY;
            case "COMP", "COMPUTATIONAL" -> DataItem.Usage.COMP;
            case "COMP-1" -> DataItem.Usage.COMP_1;
            case "COMP-2" -> DataItem.Usage.COMP_2;
            case "COMP-3" -> DataItem.Usage.COMP_3;
            case "COMP-4" -> DataItem.Usage.COMP_4;
            case "COMP-5" -> DataItem.Usage.COMP_5;
            case "BINARY" -> DataItem.Usage.BINARY;
            case "PACKED-DECIMAL" -> DataItem.Usage.PACKED_DECIMAL;
            case "INDEX" -> DataItem.Usage.INDEX;
            case "POINTER" -> DataItem.Usage.POINTER;
            case "FUNCTION-POINTER" -> DataItem.Usage.FUNCTION_POINTER;
            case "PROCEDURE-POINTER" -> DataItem.Usage.PROCEDURE_POINTER;
            default -> DataItem.Usage.DISPLAY;
        };
    }

    private DataItem.Sign mapSign(CobolParser.SignClauseContext ctx) {
        boolean leading = ctx.LEADING() != null;
        boolean separate = ctx.SEPARATE() != null;
        if (leading && separate) return DataItem.Sign.LEADING_SEPARATE;
        if (leading) return DataItem.Sign.LEADING;
        if (separate) return DataItem.Sign.TRAILING_SEPARATE;
        return DataItem.Sign.TRAILING;
    }

    private OccursClause buildOccursClause(CobolParser.OccursClauseContext ctx) {
        List<org.antlr.v4.runtime.tree.TerminalNode> integers = ctx.INTEGERLITERAL();
        int min = Integer.parseInt(integers.get(0).getText());
        Integer max = integers.size() > 1 ? Integer.parseInt(integers.get(1).getText()) : null;
        String dependingOn = null;
        if (ctx.IDENTIFIER() != null && !ctx.IDENTIFIER().isEmpty()) {
            // First IDENTIFIER after DEPENDING ON
            dependingOn = ctx.IDENTIFIER(0).getText();
        }
        String indexedBy = null;
        // Look for indexed by identifier (appears after INDEXED BY keyword)
        var allIds = ctx.IDENTIFIER();
        if (ctx.INDEXED_BY() != null && allIds.size() > (dependingOn != null ? 1 : 0)) {
            indexedBy = allIds.get(allIds.size() - 1).getText();
        }
        return new OccursClause(min, max, dependingOn, indexedBy);
    }

    // ════════════════════════════════════════════════════════
    // PROCEDURE DIVISION
    // ════════════════════════════════════════════════════════

    @Override
    public ProcedureDivision visitProcedureDivision(CobolParser.ProcedureDivisionContext ctx) {
        List<String> usingParams = new ArrayList<>();
        if (ctx.procedureUsingClause() != null) {
            for (var param : ctx.procedureUsingClause().procedureUsingParam()) {
                usingParams.add(param.IDENTIFIER().getText());
            }
        }
        String returning = null;
        if (ctx.procedureReturningClause() != null) {
            returning = ctx.procedureReturningClause().IDENTIFIER().getText();
        }

        List<ProcedureDivision.Section> sections = new ArrayList<>();
        List<Paragraph> paragraphs = new ArrayList<>();

        var body = ctx.procedureBody();
        if (body != null) {
            for (var section : body.section()) {
                sections.add(buildSection(section));
            }
            for (var para : body.paragraph()) {
                paragraphs.add(buildParagraph(para));
            }
            // Handle bare sentences (statements not in a paragraph)
            for (var sentence : body.sentence()) {
                List<Statement> stmts = new ArrayList<>();
                for (var stmtCtx : sentence.statement()) {
                    stmts.add(stmtBuilder.build(stmtCtx));
                }
                if (!stmts.isEmpty()) {
                    paragraphs.add(new Paragraph("_MAIN", stmts, null));
                }
            }
        }

        return new ProcedureDivision(usingParams, returning, sections, paragraphs);
    }

    private ProcedureDivision.Section buildSection(CobolParser.SectionContext ctx) {
        String name = ctx.sectionName().getText();
        int priority = 0;
        if (ctx.INTEGERLITERAL() != null) {
            priority = Integer.parseInt(ctx.INTEGERLITERAL().getText());
        }
        List<Paragraph> paragraphs = new ArrayList<>();
        for (var para : ctx.paragraph()) {
            paragraphs.add(buildParagraph(para));
        }
        return new ProcedureDivision.Section(name, priority, paragraphs);
    }

    private Paragraph buildParagraph(CobolParser.ParagraphContext ctx) {
        String name = ctx.paragraphName().getText();
        List<Statement> statements = new ArrayList<>();
        for (var sentence : ctx.sentence()) {
            for (var stmtCtx : sentence.statement()) {
                statements.add(stmtBuilder.build(stmtCtx));
            }
        }
        return new Paragraph(name, statements, exprBuilder.locationOf(ctx));
    }
}
