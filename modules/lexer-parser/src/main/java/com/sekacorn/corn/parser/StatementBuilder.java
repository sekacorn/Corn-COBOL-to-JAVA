/*
 * StatementBuilder - Builds Statement IR nodes from ANTLR parse tree contexts
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.parser;

import com.sekacorn.corn.ir.SourceLocation;
import com.sekacorn.corn.ir.expr.Expression;
import com.sekacorn.corn.ir.expr.Literal;
import com.sekacorn.corn.ir.stmt.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Converts ANTLR statement parse tree contexts into Statement IR nodes.
 */
public class StatementBuilder {

    private final ExpressionBuilder exprBuilder;
    private final String fileName;

    public StatementBuilder(ExpressionBuilder exprBuilder, String fileName) {
        this.exprBuilder = exprBuilder;
        this.fileName = fileName;
    }

    /**
     * Build a Statement from a statement parse context.
     */
    public Statement build(CobolParser.StatementContext ctx) {
        if (ctx.moveStatement() != null) return buildMove(ctx.moveStatement());
        if (ctx.addStatement() != null) return buildAdd(ctx.addStatement());
        if (ctx.subtractStatement() != null) return buildSubtract(ctx.subtractStatement());
        if (ctx.multiplyStatement() != null) return buildMultiply(ctx.multiplyStatement());
        if (ctx.divideStatement() != null) return buildDivide(ctx.divideStatement());
        if (ctx.computeStatement() != null) return buildCompute(ctx.computeStatement());
        if (ctx.ifStatement() != null) return buildIf(ctx.ifStatement());
        if (ctx.evaluateStatement() != null) return buildEvaluate(ctx.evaluateStatement());
        if (ctx.performStatement() != null) return buildPerform(ctx.performStatement());
        if (ctx.goToStatement() != null) return buildGoTo(ctx.goToStatement());
        if (ctx.stopStatement() != null) return buildStop(ctx.stopStatement());
        if (ctx.exitStatement() != null) return buildExit(ctx.exitStatement());
        if (ctx.displayStatement() != null) return buildDisplay(ctx.displayStatement());
        if (ctx.acceptStatement() != null) return buildAccept(ctx.acceptStatement());
        if (ctx.openStatement() != null) return buildOpen(ctx.openStatement());
        if (ctx.closeStatement() != null) return buildClose(ctx.closeStatement());
        if (ctx.readStatement() != null) return buildRead(ctx.readStatement());
        if (ctx.writeStatement() != null) return buildWrite(ctx.writeStatement());
        if (ctx.rewriteStatement() != null) return buildRewrite(ctx.rewriteStatement());
        if (ctx.deleteStatement() != null) return buildDelete(ctx.deleteStatement());
        if (ctx.startStatement() != null) return buildStart(ctx.startStatement());
        if (ctx.callStatement() != null) return buildCall(ctx.callStatement());
        if (ctx.inspectStatement() != null) return buildInspect(ctx.inspectStatement());
        if (ctx.stringStatement() != null) return buildString(ctx.stringStatement());
        if (ctx.unstringStatement() != null) return buildUnstring(ctx.unstringStatement());
        if (ctx.searchStatement() != null) return buildSearch(ctx.searchStatement());
        if (ctx.setStatement() != null) return buildSet(ctx.setStatement());
        if (ctx.initializeStatement() != null) return buildInitialize(ctx.initializeStatement());
        if (ctx.gobackStatement() != null) return buildGoback(ctx.gobackStatement());
        if (ctx.continueStatement() != null) {
            return new PerformStatement(
                    PerformStatement.PerformType.INLINE, null, null,
                    null, null, null, null, Collections.emptyList(), locationOf(ctx));
        }
        throw new IllegalStateException("Unknown statement: " + ctx.getText());
    }

    /**
     * Build a list of statements from statement contexts.
     */
    public List<Statement> buildStatements(List<CobolParser.StatementContext> stmts) {
        if (stmts == null) return Collections.emptyList();
        List<Statement> result = new ArrayList<>();
        for (var s : stmts) {
            result.add(build(s));
        }
        return result;
    }

    // ─── MOVE ───

    private Statement buildMove(CobolParser.MoveStatementContext ctx) {
        boolean corr = ctx.CORRESPONDING() != null || ctx.CORR() != null;
        Expression source = exprBuilder.buildExpression(ctx.expression());
        List<Expression> targets = new ArrayList<>();
        for (var id : ctx.identifier()) {
            targets.add(exprBuilder.buildIdentifier(id));
        }
        return new MoveStatement(source, targets, corr, locationOf(ctx));
    }

    // ─── Arithmetic ───

    private Statement buildAdd(CobolParser.AddStatementContext ctx) {
        List<Expression> operands = new ArrayList<>();
        for (var expr : ctx.expression()) {
            operands.add(exprBuilder.buildExpression(expr));
        }
        List<Expression> to = new ArrayList<>();
        for (var id : ctx.identifier()) {
            to.add(exprBuilder.buildIdentifier(id));
        }
        List<Expression> giving = buildGiving(ctx.givingClause());
        boolean rounded = ctx.roundedClause() != null;
        List<Statement> onSizeError = buildSizeError(ctx.onSizeErrorClause());
        return new AddStatement(operands, to, giving, rounded, null, onSizeError, locationOf(ctx));
    }

    private Statement buildSubtract(CobolParser.SubtractStatementContext ctx) {
        List<Expression> operands = new ArrayList<>();
        for (var expr : ctx.expression()) {
            operands.add(exprBuilder.buildExpression(expr));
        }
        List<Expression> from = new ArrayList<>();
        for (var id : ctx.identifier()) {
            from.add(exprBuilder.buildIdentifier(id));
        }
        List<Expression> giving = buildGiving(ctx.givingClause());
        boolean rounded = ctx.roundedClause() != null;
        List<Statement> onSizeError = buildSizeError(ctx.onSizeErrorClause());
        return new SubtractStatement(operands, from, giving, rounded, null, onSizeError, locationOf(ctx));
    }

    private Statement buildMultiply(CobolParser.MultiplyStatementContext ctx) {
        var expressions = ctx.expression();
        var targets = ctx.multiplyTarget();
        Expression op1;
        Expression op2;
        boolean rounded;
        List<Expression> giving;

        if (expressions.size() == 2) {
            // Format 2: MULTIPLY expr BY expr GIVING target+
            op1 = exprBuilder.buildExpression(expressions.get(0));
            op2 = exprBuilder.buildExpression(expressions.get(1));
            giving = buildGiving(ctx.givingClause());
            rounded = ctx.roundedClause() != null;
        } else {
            // Format 1: MULTIPLY expr BY multiplyTarget+
            op1 = exprBuilder.buildExpression(expressions.get(0));
            op2 = exprBuilder.buildIdentifier(targets.get(0).identifier());
            rounded = targets.get(0).roundedClause() != null;
            giving = buildGiving(ctx.givingClause());
            if (targets.size() > 1) {
                if (giving.isEmpty()) giving = new java.util.ArrayList<>();
                for (int i = 1; i < targets.size(); i++) {
                    giving.add(exprBuilder.buildIdentifier(targets.get(i).identifier()));
                }
            }
        }
        List<Statement> onSizeError = buildSizeError(ctx.onSizeErrorClause());
        return new MultiplyStatement(op1, op2, giving, rounded, null, onSizeError, locationOf(ctx));
    }

    private Statement buildDivide(CobolParser.DivideStatementContext ctx) {
        var expressions = ctx.expression();
        Expression dividend;
        Expression divisor;
        Expression into;
        List<Expression> giving;

        if (expressions.size() == 2) {
            // Format 2/3: DIVIDE expr (INTO|BY) expr GIVING target+
            Expression left = exprBuilder.buildExpression(expressions.get(0));
            Expression right = exprBuilder.buildExpression(expressions.get(1));
            if (ctx.INTO() != null) {
                dividend = right;
                divisor = left;
                into = right;
            } else {
                dividend = left;
                divisor = right;
                into = right;
            }
            giving = buildGiving(ctx.givingClause());
        } else {
            // Format 1: DIVIDE expr INTO divideTarget+
            Expression left = exprBuilder.buildExpression(expressions.get(0));
            var targets = ctx.divideTarget();
            Expression firstTarget = exprBuilder.buildIdentifier(targets.get(0).identifier());
            dividend = firstTarget;
            divisor = left;
            into = firstTarget;
            giving = new ArrayList<>();
            for (int i = 1; i < targets.size(); i++) {
                giving.add(exprBuilder.buildIdentifier(targets.get(i).identifier()));
            }
        }

        Expression remainder = null;
        if (ctx.remainderClause() != null) {
            remainder = exprBuilder.buildIdentifier(ctx.remainderClause().identifier());
        }
        boolean rounded = ctx.roundedClause() != null;
        List<Statement> onSizeError = buildSizeError(ctx.onSizeErrorClause());
        return new DivideStatement(dividend, divisor, into, giving, remainder,
                rounded, null, onSizeError, locationOf(ctx));
    }

    private Statement buildCompute(CobolParser.ComputeStatementContext ctx) {
        List<Expression> targets = new ArrayList<>();
        for (var id : ctx.identifier()) {
            targets.add(exprBuilder.buildIdentifier(id));
        }
        Expression expression = exprBuilder.buildExpression(ctx.expression());
        boolean rounded = ctx.roundedClause() != null;
        List<Statement> onSizeError = buildSizeError(ctx.onSizeErrorClause());
        return new ComputeStatement(targets, expression, rounded, null, onSizeError, locationOf(ctx));
    }

    private List<Expression> buildGiving(CobolParser.GivingClauseContext ctx) {
        if (ctx == null) return Collections.emptyList();
        List<Expression> giving = new ArrayList<>();
        for (var target : ctx.givingTarget()) {
            giving.add(exprBuilder.buildIdentifier(target.identifier()));
        }
        return giving;
    }

    private List<Statement> buildSizeError(CobolParser.OnSizeErrorClauseContext ctx) {
        if (ctx == null) return Collections.emptyList();
        return buildStatements(ctx.statement());
    }

    // ─── IF ───

    private Statement buildIf(CobolParser.IfStatementContext ctx) {
        Expression condition = exprBuilder.buildCondition(ctx.condition());

        // Split statements between THEN and ELSE
        List<Statement> thenBranch = new ArrayList<>();
        List<Statement> elseBranch = new ArrayList<>();

        boolean inElse = false;
        for (var child : ctx.children) {
            if (child instanceof org.antlr.v4.runtime.tree.TerminalNode tn
                    && tn.getSymbol().getType() == CobolLexer.ELSE) {
                inElse = true;
                continue;
            }
            if (child instanceof CobolParser.StatementContext stmtCtx) {
                if (inElse) {
                    elseBranch.add(build(stmtCtx));
                } else {
                    thenBranch.add(build(stmtCtx));
                }
            }
        }

        return new IfStatement(condition, thenBranch, elseBranch, locationOf(ctx));
    }

    // ─── EVALUATE ───

    private Statement buildEvaluate(CobolParser.EvaluateStatementContext ctx) {
        List<Expression> subjects = new ArrayList<>();
        for (var expr : ctx.expression()) {
            subjects.add(exprBuilder.buildExpression(expr));
        }

        List<EvaluateStatement.WhenClause> whenClauses = new ArrayList<>();
        for (var when : ctx.evaluateWhenClause()) {
            List<Expression> conditions = new ArrayList<>();
            for (var evalCond : when.evaluateCondition()) {
                conditions.add(buildEvaluateCondition(evalCond));
            }
            List<Statement> stmts = buildStatements(when.statement());
            whenClauses.add(new EvaluateStatement.WhenClause(conditions, stmts));
        }

        List<Statement> whenOther = Collections.emptyList();
        if (ctx.evaluateWhenOther() != null) {
            whenOther = buildStatements(ctx.evaluateWhenOther().statement());
        }

        return new EvaluateStatement(subjects, whenClauses, whenOther, locationOf(ctx));
    }

    private Expression buildEvaluateCondition(CobolParser.EvaluateConditionContext ctx) {
        if (ctx.ANY() != null) {
            return Literal.string("__ANY__", locationOf(ctx));
        }
        if (ctx.TRUE_KW() != null) {
            return Literal.figurative(Literal.FigurativeConstant.TRUE, locationOf(ctx));
        }
        if (ctx.FALSE_KW() != null) {
            return Literal.figurative(Literal.FigurativeConstant.FALSE, locationOf(ctx));
        }
        if (ctx.expression() != null && !ctx.expression().isEmpty()) {
            return exprBuilder.buildExpression(ctx.expression(0));
        }
        return Literal.string("__ANY__", locationOf(ctx));
    }

    // ─── PERFORM ───

    private Statement buildPerform(CobolParser.PerformStatementContext ctx) {
        if (ctx instanceof CobolParser.PerformProcedureContext proc) {
            return buildPerformProcedure(proc);
        }
        if (ctx instanceof CobolParser.PerformInlineContext inline) {
            return buildPerformInline(inline);
        }
        throw new IllegalStateException("Unknown perform type");
    }

    private Statement buildPerformProcedure(CobolParser.PerformProcedureContext ctx) {
        var ref = ctx.procedureRef();
        String target = ref.IDENTIFIER(0).getText();
        String through = ref.IDENTIFIER().size() > 1 ? ref.IDENTIFIER(1).getText() : null;

        if (ctx.performOption() == null) {
            return new PerformStatement(
                    PerformStatement.PerformType.SIMPLE, target, through,
                    null, null, null, null, Collections.emptyList(), locationOf(ctx));
        }
        return buildPerformOption(ctx.performOption(), target, through, Collections.emptyList(), ctx);
    }

    private Statement buildPerformInline(CobolParser.PerformInlineContext ctx) {
        List<Statement> body = buildStatements(ctx.statement());
        if (ctx.performOption() == null) {
            return new PerformStatement(
                    PerformStatement.PerformType.INLINE, null, null,
                    null, null, null, null, body, locationOf(ctx));
        }
        return buildPerformOption(ctx.performOption(), null, null, body, ctx);
    }

    private Statement buildPerformOption(CobolParser.PerformOptionContext option,
                                          String target, String through,
                                          List<Statement> inlineStatements,
                                          org.antlr.v4.runtime.ParserRuleContext parentCtx) {
        if (option instanceof CobolParser.PerformTimesContext times) {
            Expression count = exprBuilder.buildExpression(times.expression());
            return new PerformStatement(
                    PerformStatement.PerformType.TIMES, target, through,
                    count, null, null, null, inlineStatements, locationOf(parentCtx));
        }
        if (option instanceof CobolParser.PerformUntilContext until) {
            Expression condition = exprBuilder.buildCondition(until.condition());
            PerformStatement.TestPosition testPos = null;
            if (until.BEFORE() != null) testPos = PerformStatement.TestPosition.BEFORE;
            if (until.AFTER() != null) testPos = PerformStatement.TestPosition.AFTER;
            return new PerformStatement(
                    PerformStatement.PerformType.UNTIL, target, through,
                    null, condition, null, testPos, inlineStatements, locationOf(parentCtx));
        }
        if (option instanceof CobolParser.PerformVaryingContext varying) {
            String varName = varying.IDENTIFIER().getText();
            Expression from = exprBuilder.buildExpression(varying.expression(0));
            Expression by = exprBuilder.buildExpression(varying.expression(1));
            Expression untilCond = exprBuilder.buildCondition(varying.condition());
            PerformStatement.TestPosition testPos = null;
            if (varying.BEFORE() != null) testPos = PerformStatement.TestPosition.BEFORE;
            if (varying.AFTER() != null) testPos = PerformStatement.TestPosition.AFTER;

            var varyingClause = new PerformStatement.VaryingClause(
                    varName, from, by, untilCond, Collections.emptyList());

            return new PerformStatement(
                    PerformStatement.PerformType.VARYING, target, through,
                    null, null, varyingClause, testPos, inlineStatements, locationOf(parentCtx));
        }
        throw new IllegalStateException("Unknown perform option type");
    }

    // ─── GO TO ───

    private Statement buildGoTo(CobolParser.GoToStatementContext ctx) {
        List<String> targets = new ArrayList<>();
        for (var id : ctx.IDENTIFIER()) {
            targets.add(id.getText());
        }
        Expression dependingOn = null;
        if (ctx.identifier() != null) {
            dependingOn = exprBuilder.buildIdentifier(ctx.identifier());
        }
        String firstTarget = targets.isEmpty() ? null : targets.get(0);
        return new GoToStatement(firstTarget, dependingOn, targets, locationOf(ctx));
    }

    // ─── STOP / EXIT / GOBACK ───

    private Statement buildStop(CobolParser.StopStatementContext ctx) {
        Expression message = ctx.expression() != null
                ? exprBuilder.buildExpression(ctx.expression()) : null;
        return new StopStatement(StopStatement.StopType.RUN, message, locationOf(ctx));
    }

    private Statement buildExit(CobolParser.ExitStatementContext ctx) {
        ExitStatement.ExitType type = ExitStatement.ExitType.PROGRAM;
        if (ctx.PARAGRAPH() != null) type = ExitStatement.ExitType.PARAGRAPH;
        else if (ctx.SECTION() != null) type = ExitStatement.ExitType.SECTION;
        else if (ctx.PERFORM() != null) type = ExitStatement.ExitType.PERFORM;
        return new ExitStatement(type, locationOf(ctx));
    }

    private Statement buildGoback(CobolParser.GobackStatementContext ctx) {
        return new StopStatement(StopStatement.StopType.RUN, null, locationOf(ctx));
    }

    // ─── I/O ───

    private Statement buildOpen(CobolParser.OpenStatementContext ctx) {
        List<OpenStatement.FileSpec> files = new ArrayList<>();
        for (var clause : ctx.openFileClause()) {
            OpenStatement.FileSpec.OpenMode mode;
            if (clause.INPUT() != null) mode = OpenStatement.FileSpec.OpenMode.INPUT;
            else if (clause.OUTPUT() != null) mode = OpenStatement.FileSpec.OpenMode.OUTPUT;
            else if (clause.I_O() != null) mode = OpenStatement.FileSpec.OpenMode.I_O;
            else mode = OpenStatement.FileSpec.OpenMode.EXTEND;

            for (var id : clause.IDENTIFIER()) {
                files.add(new OpenStatement.FileSpec(id.getText(), mode));
            }
        }
        return new OpenStatement(files, locationOf(ctx));
    }

    private Statement buildClose(CobolParser.CloseStatementContext ctx) {
        List<String> fileNames = new ArrayList<>();
        for (var id : ctx.IDENTIFIER()) {
            fileNames.add(id.getText());
        }
        return new CloseStatement(fileNames, locationOf(ctx));
    }

    private Statement buildRead(CobolParser.ReadStatementContext ctx) {
        String fileName = ctx.IDENTIFIER().getText();
        List<CobolParser.IdentifierContext> identifiers = ctx.identifier();
        Expression into = !identifiers.isEmpty()
                ? exprBuilder.buildIdentifier(identifiers.get(0)) : null;
        Expression key = identifiers.size() > 1
                ? exprBuilder.buildIdentifier(identifiers.get(1)) : null;
        List<Statement> atEnd = ctx.atEndClause() != null
                ? buildStatements(ctx.atEndClause().statement()) : Collections.emptyList();
        List<Statement> notAtEnd = ctx.notAtEndClause() != null
                ? buildStatements(ctx.notAtEndClause().statement()) : Collections.emptyList();
        List<Statement> invalidKey = ctx.invalidKeyClause() != null
                ? buildStatements(ctx.invalidKeyClause().statement()) : Collections.emptyList();
        return new ReadStatement(fileName, into, key, atEnd, notAtEnd, invalidKey, locationOf(ctx));
    }

    private Statement buildWrite(CobolParser.WriteStatementContext ctx) {
        String recordName = ctx.IDENTIFIER().getText();
        Expression from = ctx.expression() != null
                ? exprBuilder.buildExpression(ctx.expression()) : null;
        List<Statement> invalidKey = ctx.invalidKeyClause() != null
                ? buildStatements(ctx.invalidKeyClause().statement()) : Collections.emptyList();
        return new WriteStatement(recordName, from, invalidKey, locationOf(ctx));
    }

    private Statement buildRewrite(CobolParser.RewriteStatementContext ctx) {
        String recordName = ctx.IDENTIFIER().getText();
        Expression from = ctx.expression() != null
                ? exprBuilder.buildExpression(ctx.expression()) : null;
        List<Statement> invalidKey = ctx.invalidKeyClause() != null
                ? buildStatements(ctx.invalidKeyClause().statement()) : Collections.emptyList();
        return new RewriteStatement(recordName, from, invalidKey, locationOf(ctx));
    }

    private Statement buildDelete(CobolParser.DeleteStatementContext ctx) {
        String fileName = ctx.IDENTIFIER().getText();
        List<Statement> invalidKey = ctx.invalidKeyClause() != null
                ? buildStatements(ctx.invalidKeyClause().statement()) : Collections.emptyList();
        return new DeleteStatement(fileName, invalidKey, locationOf(ctx));
    }

    private Statement buildStart(CobolParser.StartStatementContext ctx) {
        String fileName = ctx.IDENTIFIER().getText();
        Expression key = ctx.identifier() != null
                ? exprBuilder.buildIdentifier(ctx.identifier()) : null;
        List<Statement> invalidKey = ctx.invalidKeyClause() != null
                ? buildStatements(ctx.invalidKeyClause().statement()) : Collections.emptyList();
        return new StartStatement(fileName, key, invalidKey, locationOf(ctx));
    }

    // ─── DISPLAY / ACCEPT ───

    private Statement buildDisplay(CobolParser.DisplayStatementContext ctx) {
        List<Expression> items = new ArrayList<>();
        for (var expr : ctx.expression()) {
            items.add(exprBuilder.buildExpression(expr));
        }
        String upon = null;
        if (ctx.UPON() != null && ctx.IDENTIFIER() != null) {
            upon = ctx.IDENTIFIER().getText();
        }
        return new DisplayStatement(items, upon, locationOf(ctx));
    }

    private Statement buildAccept(CobolParser.AcceptStatementContext ctx) {
        Expression target = exprBuilder.buildIdentifier(ctx.identifier());
        String from = null;
        if (ctx.DATE() != null) from = "DATE";
        else if (ctx.DAY() != null) from = "DAY";
        else if (ctx.DAY_OF_WEEK() != null) from = "DAY-OF-WEEK";
        else if (ctx.TIME() != null) from = "TIME";
        else if (ctx.IDENTIFIER() != null) from = ctx.IDENTIFIER().getText();
        return new AcceptStatement(target, from, locationOf(ctx));
    }

    // ─── CALL ───

    private Statement buildCall(CobolParser.CallStatementContext ctx) {
        Expression programName = exprBuilder.buildExpression(ctx.expression());
        List<CallArgument> arguments = new ArrayList<>();
        for (var arg : ctx.callUsingArg()) {
            CallArgument.PassingMode mode = CallArgument.PassingMode.BY_REFERENCE;
            if (arg.VALUE() != null) mode = CallArgument.PassingMode.BY_VALUE;
            else if (arg.CONTENT() != null) mode = CallArgument.PassingMode.BY_CONTENT;
            arguments.add(new CallArgument(exprBuilder.buildExpression(arg.expression()), mode));
        }
        Expression returning = null;
        if (ctx.identifier() != null) {
            returning = exprBuilder.buildIdentifier(ctx.identifier());
        }
        List<Statement> onException = Collections.emptyList();
        if (ctx.onExceptionClause() != null) {
            onException = buildStatements(ctx.onExceptionClause().statement());
        }
        return new CallStatement(programName, arguments, returning, onException, locationOf(ctx));
    }

    // ─── INSPECT ───

    private Statement buildInspect(CobolParser.InspectStatementContext ctx) {
        Expression target = exprBuilder.buildIdentifier(ctx.identifier());
        if (ctx.inspectOp() instanceof CobolParser.InspectTallyingContext tallyingCtx) {
            Expression counter = exprBuilder.buildIdentifier(tallyingCtx.identifier());
            List<InspectStatement.TallyFor> forClauses = new ArrayList<>();
            for (var clause : tallyingCtx.inspectTallyingClause()) {
                InspectStatement.TallyMode mode = clause.CHARACTERS() != null
                        ? InspectStatement.TallyMode.CHARACTERS
                        : clause.LEADING() != null
                        ? InspectStatement.TallyMode.LEADING
                        : InspectStatement.TallyMode.ALL;
                Expression value = clause.CHARACTERS() != null || clause.expression().isEmpty()
                        ? null
                        : exprBuilder.buildExpression(clause.expression(0));
                forClauses.add(new InspectStatement.TallyFor(
                        mode,
                        value,
                        buildBoundary(clause, true),
                        buildBoundary(clause, false)
                ));
            }
            return new InspectStatement(
                    target,
                    InspectStatement.InspectOp.TALLYING,
                    new InspectStatement.TallyingClause(counter, forClauses),
                    Collections.emptyList(),
                    null,
                    locationOf(ctx)
            );
        }
        if (ctx.inspectOp() instanceof CobolParser.InspectReplacingContext replacingCtx) {
            List<InspectStatement.ReplacingClause> clauses = new ArrayList<>();
            for (var clause : replacingCtx.inspectReplacingClause()) {
                InspectStatement.ReplaceMode mode = clause.CHARACTERS() != null
                        ? InspectStatement.ReplaceMode.CHARACTERS
                        : clause.LEADING() != null
                        ? InspectStatement.ReplaceMode.LEADING
                        : clause.FIRST() != null
                        ? InspectStatement.ReplaceMode.FIRST
                        : InspectStatement.ReplaceMode.ALL;
                Expression matchTarget = clause.CHARACTERS() != null
                        ? null
                        : exprBuilder.buildExpression(clause.expression(0));
                Expression replacement = exprBuilder.buildExpression(
                        clause.CHARACTERS() != null ? clause.expression(0) : clause.expression(1));
                clauses.add(new InspectStatement.ReplacingClause(
                        mode,
                        matchTarget,
                        replacement,
                        buildBoundary(clause, true),
                        buildBoundary(clause, false)
                ));
            }
            return new InspectStatement(
                    target,
                    InspectStatement.InspectOp.REPLACING,
                    null,
                    clauses,
                    null,
                    locationOf(ctx)
            );
        }
        CobolParser.InspectConvertingContext convertingCtx = (CobolParser.InspectConvertingContext) ctx.inspectOp();
        return new InspectStatement(
                target,
                InspectStatement.InspectOp.CONVERTING,
                null,
                Collections.emptyList(),
                new InspectStatement.ConvertingClause(
                        exprBuilder.buildExpression(convertingCtx.expression(0)),
                        exprBuilder.buildExpression(convertingCtx.expression(1)),
                        buildBoundary(convertingCtx, true),
                        buildBoundary(convertingCtx, false)
                ),
                locationOf(ctx)
        );
    }

    private InspectStatement.Boundary buildBoundary(org.antlr.v4.runtime.ParserRuleContext ctx, boolean before) {
        int boundaryToken = before ? CobolLexer.BEFORE : CobolLexer.AFTER;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof TerminalNode terminal && terminal.getSymbol().getType() == boundaryToken) {
                for (int j = i + 1; j < ctx.getChildCount(); j++) {
                    ParseTree candidate = ctx.getChild(j);
                    if (candidate instanceof CobolParser.ExpressionContext exprCtx) {
                        return new InspectStatement.Boundary(exprBuilder.buildExpression(exprCtx), before);
                    }
                }
            }
        }
        return null;
    }

    // ─── STRING / UNSTRING ───

    private Statement buildString(CobolParser.StringStatementContext ctx) {
        List<Expression> sources = new ArrayList<>();
        for (var sending : ctx.stringSendingClause()) {
            sources.add(exprBuilder.buildExpression(sending.expression(0)));
        }
        Expression into = exprBuilder.buildIdentifier(ctx.identifier(0));
        Expression pointer = ctx.identifier().size() > 1
                ? exprBuilder.buildIdentifier(ctx.identifier(1)) : null;
        List<Statement> onOverflow = Collections.emptyList();
        for (var child : ctx.children) {
            if (child instanceof CobolParser.StatementContext stmtCtx) {
                if (onOverflow.isEmpty()) {
                    onOverflow = new ArrayList<>();
                }
                ((List<Statement>) onOverflow).add(build(stmtCtx));
            }
        }
        return new StringStatement(sources, into, pointer, onOverflow, locationOf(ctx));
    }

    private Statement buildUnstring(CobolParser.UnstringStatementContext ctx) {
        Expression source = exprBuilder.buildIdentifier(ctx.identifier(0));
        List<Expression> delimiters = new ArrayList<>();
        // Delimiters come from expressions in the DELIMITED BY clause
        for (var expr : ctx.expression()) {
            delimiters.add(exprBuilder.buildExpression(expr));
        }
        List<Expression> into = new ArrayList<>();
        for (var intoClause : ctx.unstringIntoClause()) {
            into.add(exprBuilder.buildIdentifier(intoClause.identifier(0)));
        }
        return new UnstringStatement(source, delimiters, into, null,
                Collections.emptyList(), locationOf(ctx));
    }

    // ─── SEARCH ───

    private Statement buildSearch(CobolParser.SearchStatementContext ctx) {
        String tableName = ctx.IDENTIFIER().getText();
        boolean searchAll = ctx.ALL() != null;
        Expression varying = ctx.identifier() != null
                ? exprBuilder.buildIdentifier(ctx.identifier()) : null;
        List<Statement> atEnd = Collections.emptyList();
        if (ctx.statement() != null && !ctx.statement().isEmpty()) {
            atEnd = buildStatements(ctx.statement());
        }
        List<SearchStatement.WhenClause> whenClauses = new ArrayList<>();
        for (var when : ctx.searchWhenClause()) {
            Expression condition = exprBuilder.buildCondition(when.condition());
            List<Statement> stmts = buildStatements(when.statement());
            whenClauses.add(new SearchStatement.WhenClause(condition, stmts));
        }
        return new SearchStatement(tableName, searchAll, varying, atEnd,
                whenClauses, locationOf(ctx));
    }

    // ─── SET / INITIALIZE ───

    private Statement buildSet(CobolParser.SetStatementContext ctx) {
        if (ctx instanceof CobolParser.SetToTrueStatementContext trueCtx) {
            List<Expression> targets = new ArrayList<>();
            for (var id : trueCtx.identifier()) {
                targets.add(exprBuilder.buildIdentifier(id));
            }
            Literal.FigurativeConstant fc = trueCtx.TRUE_KW() != null
                    ? Literal.FigurativeConstant.TRUE
                    : Literal.FigurativeConstant.FALSE;
            Expression value = Literal.figurative(fc, locationOf(ctx));
            return new SetStatement(targets, value, locationOf(ctx));
        }
        if (ctx instanceof CobolParser.SetToValueStatementContext valCtx) {
            List<Expression> targets = new ArrayList<>();
            for (var id : valCtx.identifier()) {
                targets.add(exprBuilder.buildIdentifier(id));
            }
            Expression value = exprBuilder.buildExpression(valCtx.expression());
            return new SetStatement(targets, value, locationOf(ctx));
        }
        if (ctx instanceof CobolParser.SetUpDownStatementContext udCtx) {
            List<Expression> targets = new ArrayList<>();
            for (var id : udCtx.identifier()) {
                targets.add(exprBuilder.buildIdentifier(id));
            }
            Expression value = exprBuilder.buildExpression(udCtx.expression());
            return new SetStatement(targets, value, locationOf(ctx));
        }
        throw new IllegalStateException("Unknown SET statement type: " + ctx.getClass().getName());
    }

    private Statement buildInitialize(CobolParser.InitializeStatementContext ctx) {
        List<Expression> targets = new ArrayList<>();
        for (var id : ctx.identifier()) {
            targets.add(exprBuilder.buildIdentifier(id));
        }
        return new InitializeStatement(targets, locationOf(ctx));
    }

    // ─── Utility ───

    private SourceLocation locationOf(org.antlr.v4.runtime.ParserRuleContext ctx) {
        return exprBuilder.locationOf(ctx);
    }
}
