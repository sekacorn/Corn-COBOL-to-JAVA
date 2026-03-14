/*
 * StatementVisitor - Visitor pattern for statement processing
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir.stmt;

/**
 * Visitor interface for processing COBOL statements.
 * Enables type-safe traversal and transformation of the IR.
 */
public interface StatementVisitor<R> {
    R visitMove(MoveStatement stmt);
    R visitCompute(ComputeStatement stmt);
    R visitAdd(AddStatement stmt);
    R visitSubtract(SubtractStatement stmt);
    R visitMultiply(MultiplyStatement stmt);
    R visitDivide(DivideStatement stmt);
    R visitIf(IfStatement stmt);
    R visitEvaluate(EvaluateStatement stmt);
    R visitPerform(PerformStatement stmt);
    R visitGoTo(GoToStatement stmt);
    R visitStop(StopStatement stmt);
    R visitExit(ExitStatement stmt);
    R visitDisplay(DisplayStatement stmt);
    R visitAccept(AcceptStatement stmt);
    R visitOpen(OpenStatement stmt);
    R visitClose(CloseStatement stmt);
    R visitRead(ReadStatement stmt);
    R visitWrite(WriteStatement stmt);
    R visitRewrite(RewriteStatement stmt);
    R visitDelete(DeleteStatement stmt);
    R visitStart(StartStatement stmt);
    R visitCall(CallStatement stmt);
    R visitInspect(InspectStatement stmt);
    R visitString(StringStatement stmt);
    R visitUnstring(UnstringStatement stmt);
    R visitSearch(SearchStatement stmt);
    R visitSet(SetStatement stmt);
    R visitInitialize(InitializeStatement stmt);
}
