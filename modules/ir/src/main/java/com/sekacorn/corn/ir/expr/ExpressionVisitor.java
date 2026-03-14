/*
 * ExpressionVisitor - Visitor pattern for expression processing
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir.expr;

/**
 * Visitor interface for processing expressions.
 */
public interface ExpressionVisitor<R> {
    R visitLiteral(Literal expr);
    R visitVariable(VariableRef expr);
    R visitSubscript(SubscriptRef expr);
    R visitBinary(BinaryOp expr);
    R visitUnary(UnaryOp expr);
    R visitFunction(FunctionCall expr);
    R visitCondition(ConditionExpr expr);
}
