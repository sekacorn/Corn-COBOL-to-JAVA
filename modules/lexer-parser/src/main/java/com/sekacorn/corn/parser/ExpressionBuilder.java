/*
 * ExpressionBuilder - Builds Expression IR nodes from ANTLR parse tree contexts
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.parser;

import com.sekacorn.corn.ir.SourceLocation;
import com.sekacorn.corn.ir.expr.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts ANTLR expression parse tree contexts into Expression IR nodes.
 */
public class ExpressionBuilder {

    private final String fileName;

    public ExpressionBuilder(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Build an Expression from a parser expression context.
     */
    public Expression buildExpression(CobolParser.ExpressionContext ctx) {
        return buildArithmetic(ctx.arithmeticExpression());
    }

    /**
     * Build an Expression from an arithmetic expression (handles + and -).
     */
    public Expression buildArithmetic(CobolParser.ArithmeticExpressionContext ctx) {
        List<CobolParser.TermContext> terms = ctx.term();
        Expression result = buildTerm(terms.get(0));

        for (int i = 1; i < terms.size(); i++) {
            Token op = (Token) ctx.getChild(2 * i - 1).getPayload();
            BinaryOp.Operator operator = op.getType() == CobolLexer.PLUS
                    ? BinaryOp.Operator.ADD : BinaryOp.Operator.SUBTRACT;
            Expression right = buildTerm(terms.get(i));
            result = new BinaryOp(result, operator, right, locationOf(ctx));
        }
        return result;
    }

    /**
     * Build from a term (handles * and /).
     */
    private Expression buildTerm(CobolParser.TermContext ctx) {
        List<CobolParser.PowerContext> powers = ctx.power();
        Expression result = buildPower(powers.get(0));

        for (int i = 1; i < powers.size(); i++) {
            Token op = (Token) ctx.getChild(2 * i - 1).getPayload();
            BinaryOp.Operator operator = op.getType() == CobolLexer.STAR
                    ? BinaryOp.Operator.MULTIPLY : BinaryOp.Operator.DIVIDE;
            Expression right = buildPower(powers.get(i));
            result = new BinaryOp(result, operator, right, locationOf(ctx));
        }
        return result;
    }

    /**
     * Build from a power expression (handles **).
     */
    private Expression buildPower(CobolParser.PowerContext ctx) {
        List<CobolParser.UnaryExpressionContext> unaries = ctx.unaryExpression();
        Expression result = buildUnary(unaries.get(0));

        if (unaries.size() > 1) {
            Expression right = buildUnary(unaries.get(1));
            result = new BinaryOp(result, BinaryOp.Operator.POWER, right, locationOf(ctx));
        }
        return result;
    }

    /**
     * Build from a unary expression (handles + and - prefixes).
     */
    private Expression buildUnary(CobolParser.UnaryExpressionContext ctx) {
        Expression primary = buildPrimary(ctx.primaryExpression());

        if (ctx.MINUS() != null) {
            return new UnaryOp(UnaryOp.Operator.NEGATE, primary, locationOf(ctx));
        }
        return primary;
    }

    /**
     * Build from a primary expression (literal, identifier, function call, parenthesized).
     */
    private Expression buildPrimary(CobolParser.PrimaryExpressionContext ctx) {
        if (ctx instanceof CobolParser.LiteralExprContext litCtx) {
            return buildLiteral(litCtx.literal());
        }
        if (ctx instanceof CobolParser.IdentifierExprContext idCtx) {
            return buildIdentifier(idCtx.identifier());
        }
        if (ctx instanceof CobolParser.FunctionExprContext funcCtx) {
            return buildFunctionCall(funcCtx.functionCall());
        }
        if (ctx instanceof CobolParser.ParenExprContext parenCtx) {
            return buildExpression(parenCtx.expression());
        }
        throw new IllegalStateException("Unknown primary expression type: " + ctx.getClass().getName());
    }

    /**
     * Build a Literal expression.
     */
    public Expression buildLiteral(CobolParser.LiteralContext ctx) {
        if (ctx.INTEGERLITERAL() != null) {
            return new Literal(
                    Long.parseLong(ctx.INTEGERLITERAL().getText()),
                    Literal.LiteralType.NUMERIC,
                    locationOf(ctx));
        }
        if (ctx.DECIMALLITERAL() != null) {
            return new Literal(
                    Double.parseDouble(ctx.DECIMALLITERAL().getText()),
                    Literal.LiteralType.NUMERIC,
                    locationOf(ctx));
        }
        if (ctx.STRINGLITERAL() != null) {
            String text = ctx.STRINGLITERAL().getText();
            // Strip surrounding quotes
            String value = text.substring(1, text.length() - 1)
                    .replace("\"\"", "\"")
                    .replace("''", "'");
            return new Literal(value, Literal.LiteralType.STRING, locationOf(ctx));
        }
        if (ctx.figurativeConstant() != null) {
            return buildFigurativeConstant(ctx.figurativeConstant());
        }
        throw new IllegalStateException("Unknown literal type");
    }

    /**
     * Build a figurative constant.
     */
    private Expression buildFigurativeConstant(CobolParser.FigurativeConstantContext ctx) {
        Literal.FigurativeConstant fc;
        if (ctx.ZERO() != null || ctx.ZEROS() != null || ctx.ZEROES() != null) {
            fc = Literal.FigurativeConstant.ZERO;
        } else if (ctx.SPACE() != null || ctx.SPACES() != null) {
            fc = Literal.FigurativeConstant.SPACE;
        } else if (ctx.HIGH_VALUE() != null || ctx.HIGH_VALUES() != null) {
            fc = Literal.FigurativeConstant.HIGH_VALUE;
        } else if (ctx.LOW_VALUE() != null || ctx.LOW_VALUES() != null) {
            fc = Literal.FigurativeConstant.LOW_VALUE;
        } else if (ctx.QUOTE() != null || ctx.QUOTES() != null) {
            fc = Literal.FigurativeConstant.QUOTE;
        } else if (ctx.NULL_KW() != null || ctx.NULLS() != null) {
            fc = Literal.FigurativeConstant.NULL;
        } else {
            fc = Literal.FigurativeConstant.SPACE; // fallback
        }
        return new Literal(fc, Literal.LiteralType.FIGURATIVE, locationOf(ctx));
    }

    /**
     * Build an identifier reference (variable, subscript, or reference-modified).
     */
    public Expression buildIdentifier(CobolParser.IdentifierContext ctx) {
        String name = ctx.IDENTIFIER().getText();

        // Qualified reference (OF/IN)
        String qualifier = null;
        if (ctx.qualifiedTail() != null && !ctx.qualifiedTail().isEmpty()) {
            qualifier = ctx.qualifiedTail().get(ctx.qualifiedTail().size() - 1).IDENTIFIER().getText();
        }

        // Subscript reference
        if (ctx.subscriptPart() != null) {
            List<Expression> subscripts = new ArrayList<>();
            for (var expr : ctx.subscriptPart().expression()) {
                subscripts.add(buildExpression(expr));
            }
            return new SubscriptRef(name, subscripts, locationOf(ctx));
        }

        // Reference modification
        VariableRef.ReferenceModification refMod = null;
        if (ctx.referenceModification() != null) {
            var rm = ctx.referenceModification();
            Expression start = buildExpression(rm.expression(0));
            Expression length = rm.expression().size() > 1 ? buildExpression(rm.expression(1)) : null;
            refMod = new VariableRef.ReferenceModification(start, length);
        }

        return new VariableRef(name, qualifier, refMod, locationOf(ctx));
    }

    /**
     * Build a function call expression.
     */
    private Expression buildFunctionCall(CobolParser.FunctionCallContext ctx) {
        String funcName = ctx.IDENTIFIER().getText();
        List<Expression> args = new ArrayList<>();
        for (var expr : ctx.expression()) {
            args.add(buildExpression(expr));
        }
        return new FunctionCall(funcName, args, locationOf(ctx));
    }

    /**
     * Build a condition (used by IF, PERFORM UNTIL, etc.).
     */
    public Expression buildCondition(CobolParser.ConditionContext ctx) {
        List<CobolParser.CombinableConditionContext> parts = ctx.combinableCondition();
        Expression result = buildCombinableCondition(parts.get(0));

        for (int i = 1; i < parts.size(); i++) {
            // Get the logical operator between conditions
            // The tokens are interleaved: cond0 AND cond1 OR cond2 ...
            Token opToken = (Token) ctx.getChild(2 * i - 1).getPayload();
            BinaryOp.Operator op = opToken.getType() == CobolLexer.AND
                    ? BinaryOp.Operator.AND : BinaryOp.Operator.OR;
            Expression right = buildCombinableCondition(parts.get(i));
            result = new BinaryOp(result, op, right, locationOf(ctx));
        }
        return result;
    }

    private Expression buildCombinableCondition(CobolParser.CombinableConditionContext ctx) {
        Expression cond = buildSimpleCondition(ctx.simpleCondition());
        if (ctx.NOT() != null) {
            return new UnaryOp(UnaryOp.Operator.NOT, cond, locationOf(ctx));
        }
        return cond;
    }

    private Expression buildSimpleCondition(CobolParser.SimpleConditionContext ctx) {
        if (ctx.classCondition() != null) {
            return buildClassCondition(ctx.classCondition());
        }
        if (ctx.relationCondition() != null) {
            return buildRelationCondition(ctx.relationCondition());
        }
        if (ctx.condition() != null) {
            return buildCondition(ctx.condition());
        }
        if (ctx.conditionNameCondition() != null) {
            return buildConditionNameCondition(ctx.conditionNameCondition());
        }
        throw new IllegalStateException("Unknown simple condition type");
    }

    /**
     * Build a condition name reference (88-level condition used as boolean).
     * Returns a ConditionExpr with CONDITION_NAME type wrapping a VariableRef.
     */
    private Expression buildConditionNameCondition(CobolParser.ConditionNameConditionContext ctx) {
        Expression subject = buildIdentifier(ctx.identifier());
        return new ConditionExpr(subject, ConditionExpr.ConditionType.CONDITION_NAME, false, locationOf(ctx));
    }

    private Expression buildClassCondition(CobolParser.ClassConditionContext ctx) {
        Expression subject = buildIdentifier(ctx.identifier());
        boolean negated = ctx.NOT() != null;
        ConditionExpr.ConditionType type;

        if (ctx.NUMERIC() != null) type = ConditionExpr.ConditionType.NUMERIC;
        else if (ctx.ALPHABETIC() != null) type = ConditionExpr.ConditionType.ALPHABETIC;
        else if (ctx.ALPHABETIC_LOWER() != null) type = ConditionExpr.ConditionType.ALPHABETIC_LOWER;
        else if (ctx.ALPHABETIC_UPPER() != null) type = ConditionExpr.ConditionType.ALPHABETIC_UPPER;
        else if (ctx.POSITIVE() != null) type = ConditionExpr.ConditionType.POSITIVE;
        else if (ctx.NEGATIVE() != null) type = ConditionExpr.ConditionType.NEGATIVE;
        else type = ConditionExpr.ConditionType.ZERO;

        return new ConditionExpr(subject, type, negated, locationOf(ctx));
    }

    private Expression buildRelationCondition(CobolParser.RelationConditionContext ctx) {
        Expression left = buildExpression(ctx.expression(0));
        Expression right = buildExpression(ctx.expression(1));
        BinaryOp.Operator op = mapRelationalOperator(ctx.relationalOperator());
        return new BinaryOp(left, op, right, locationOf(ctx));
    }

    private BinaryOp.Operator mapRelationalOperator(CobolParser.RelationalOperatorContext ctx) {
        boolean negated = ctx.NOT() != null;
        boolean hasEqual = ctx.EQUAL() != null || ctx.EQUAL_WORD() != null;
        boolean hasGreater = ctx.GREATER() != null || ctx.GREATER_WORD() != null;
        boolean hasLess = ctx.LESS() != null || ctx.LESS_WORD() != null;
        if (hasEqual && !hasGreater && !hasLess) {
            return negated ? BinaryOp.Operator.NOT_EQUAL : BinaryOp.Operator.EQUAL;
        }
        if (hasGreater && !hasEqual) {
            return negated ? BinaryOp.Operator.LESS_EQUAL : BinaryOp.Operator.GREATER_THAN;
        }
        if (hasLess && !hasEqual) {
            return negated ? BinaryOp.Operator.GREATER_EQUAL : BinaryOp.Operator.LESS_THAN;
        }
        if (ctx.GREATER_EQUAL() != null) {
            return negated ? BinaryOp.Operator.LESS_THAN : BinaryOp.Operator.GREATER_EQUAL;
        }
        if (ctx.LESS_EQUAL() != null) {
            return negated ? BinaryOp.Operator.GREATER_THAN : BinaryOp.Operator.LESS_EQUAL;
        }
        return BinaryOp.Operator.EQUAL;
    }

    /**
     * Create a SourceLocation from a parse tree context.
     */
    SourceLocation locationOf(ParserRuleContext ctx) {
        if (ctx == null || ctx.start == null) return null;
        Token start = ctx.start;
        Token stop = ctx.stop != null ? ctx.stop : start;
        return SourceLocation.range(fileName, start.getLine(), start.getCharPositionInLine(),
                stop.getLine(), stop.getCharPositionInLine());
    }
}
