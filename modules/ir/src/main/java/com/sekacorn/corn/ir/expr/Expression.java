/*
 * Expression - Base sealed interface for all expressions
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir.expr;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sekacorn.corn.ir.SourceLocation;

/**
 * Base sealed interface for all expression types in IR.
 * Expressions produce values and can be used in conditions and computations.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Literal.class, name = "literal"),
    @JsonSubTypes.Type(value = VariableRef.class, name = "variable"),
    @JsonSubTypes.Type(value = SubscriptRef.class, name = "subscript"),
    @JsonSubTypes.Type(value = BinaryOp.class, name = "binary"),
    @JsonSubTypes.Type(value = UnaryOp.class, name = "unary"),
    @JsonSubTypes.Type(value = FunctionCall.class, name = "function"),
    @JsonSubTypes.Type(value = ConditionExpr.class, name = "condition")
})
public sealed interface Expression
    permits Literal, VariableRef, SubscriptRef, BinaryOp, UnaryOp,
            FunctionCall, ConditionExpr {

    /**
     * Get the source location of this expression
     */
    SourceLocation getLocation();

    /**
     * Accept a visitor for processing this expression
     */
    <R> R accept(ExpressionVisitor<R> visitor);
}
