/*
 * Statement - Base sealed interface for all COBOL statements
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir.stmt;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sekacorn.corn.ir.SourceLocation;

/**
 * Base sealed interface for all COBOL statement types in IR.
 * Uses sealed hierarchy for exhaustive pattern matching and type safety.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = MoveStatement.class, name = "move"),
    @JsonSubTypes.Type(value = ComputeStatement.class, name = "compute"),
    @JsonSubTypes.Type(value = AddStatement.class, name = "add"),
    @JsonSubTypes.Type(value = SubtractStatement.class, name = "subtract"),
    @JsonSubTypes.Type(value = MultiplyStatement.class, name = "multiply"),
    @JsonSubTypes.Type(value = DivideStatement.class, name = "divide"),
    @JsonSubTypes.Type(value = IfStatement.class, name = "if"),
    @JsonSubTypes.Type(value = EvaluateStatement.class, name = "evaluate"),
    @JsonSubTypes.Type(value = PerformStatement.class, name = "perform"),
    @JsonSubTypes.Type(value = GoToStatement.class, name = "goto"),
    @JsonSubTypes.Type(value = StopStatement.class, name = "stop"),
    @JsonSubTypes.Type(value = ExitStatement.class, name = "exit"),
    @JsonSubTypes.Type(value = DisplayStatement.class, name = "display"),
    @JsonSubTypes.Type(value = AcceptStatement.class, name = "accept"),
    @JsonSubTypes.Type(value = OpenStatement.class, name = "open"),
    @JsonSubTypes.Type(value = CloseStatement.class, name = "close"),
    @JsonSubTypes.Type(value = ReadStatement.class, name = "read"),
    @JsonSubTypes.Type(value = WriteStatement.class, name = "write"),
    @JsonSubTypes.Type(value = RewriteStatement.class, name = "rewrite"),
    @JsonSubTypes.Type(value = DeleteStatement.class, name = "delete"),
    @JsonSubTypes.Type(value = StartStatement.class, name = "start"),
    @JsonSubTypes.Type(value = CallStatement.class, name = "call"),
    @JsonSubTypes.Type(value = InspectStatement.class, name = "inspect"),
    @JsonSubTypes.Type(value = StringStatement.class, name = "string"),
    @JsonSubTypes.Type(value = UnstringStatement.class, name = "unstring"),
    @JsonSubTypes.Type(value = SearchStatement.class, name = "search"),
    @JsonSubTypes.Type(value = SetStatement.class, name = "set"),
    @JsonSubTypes.Type(value = InitializeStatement.class, name = "initialize")
})
public sealed interface Statement
    permits MoveStatement, ComputeStatement, AddStatement, SubtractStatement,
            MultiplyStatement, DivideStatement, IfStatement, EvaluateStatement,
            PerformStatement, GoToStatement, StopStatement, ExitStatement,
            DisplayStatement, AcceptStatement, OpenStatement, CloseStatement,
            ReadStatement, WriteStatement, RewriteStatement, DeleteStatement,
            StartStatement, CallStatement, InspectStatement, StringStatement,
            UnstringStatement, SearchStatement, SetStatement, InitializeStatement {

    /**
     * Get the source location of this statement
     */
    SourceLocation getLocation();

    /**
     * Accept a visitor for processing this statement
     */
    <R> R accept(StatementVisitor<R> visitor);
}
