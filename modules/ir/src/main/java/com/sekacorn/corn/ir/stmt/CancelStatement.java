/*
 * CancelStatement - IR for COBOL CANCEL statement
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir.stmt;

import com.sekacorn.corn.ir.SourceLocation;
import java.util.List;

public record CancelStatement(
        List<String> programNames,
        SourceLocation location
) implements Statement {

    @Override
    public SourceLocation getLocation() {
        return location;
    }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitCancel(this);
    }
}
