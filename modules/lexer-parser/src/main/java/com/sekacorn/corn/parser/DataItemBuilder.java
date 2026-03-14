/*
 * DataItemBuilder - Builds hierarchical DataItem trees from flat level-number sequences
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.parser;

import com.sekacorn.corn.ir.ConditionName;
import com.sekacorn.corn.ir.DataItem;
import com.sekacorn.corn.ir.OccursClause;
import com.sekacorn.corn.ir.Picture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Converts a flat sequence of data item entries (as parsed from COBOL source)
 * into hierarchical DataItem trees based on level numbers.
 * <p>
 * Level 01/77 starts a new top-level item. Levels 02-49 nest under the
 * nearest higher level. Level 88 attaches as a ConditionName to the preceding item.
 */
public final class DataItemBuilder {

    /**
     * Intermediate representation of a parsed data item before tree construction.
     */
    public record FlatDataItem(
            int levelNumber,
            String name,
            boolean isFiller,
            Picture picture,
            DataItem.Usage usage,
            DataItem.Sign sign,
            String value,
            String redefines,
            OccursClause occurs,
            boolean justified,
            boolean blankWhenZero,
            boolean synchronizedField,
            List<ConditionName.ValueSpec> conditionValues  // non-null only for level 88
    ) {}

    private DataItemBuilder() {}

    /**
     * Build a hierarchical list of DataItems from flat entries.
     */
    public static List<DataItem> build(List<FlatDataItem> flatItems) {
        if (flatItems == null || flatItems.isEmpty()) {
            return Collections.emptyList();
        }

        List<DataItem> topLevel = new ArrayList<>();
        Stack<BuildNode> stack = new Stack<>();

        for (FlatDataItem flat : flatItems) {
            // Level 88 is a condition name — attach to the most recent item
            if (flat.levelNumber() == 88) {
                if (!stack.isEmpty()) {
                    stack.peek().conditionNames.add(
                            new ConditionName(flat.name(), flat.conditionValues()));
                }
                continue;
            }

            // Create a new build node
            BuildNode node = new BuildNode(flat);

            if (flat.levelNumber() == 1 || flat.levelNumber() == 77) {
                // Flush the stack to top-level
                flushStack(stack, topLevel);
                stack.push(node);
            } else {
                // Pop until we find a parent with a lower level number
                while (!stack.isEmpty() && stack.peek().flat.levelNumber() >= flat.levelNumber()) {
                    BuildNode child = stack.pop();
                    if (!stack.isEmpty()) {
                        stack.peek().children.add(child.toDataItem());
                    } else {
                        topLevel.add(child.toDataItem());
                    }
                }
                stack.push(node);
            }
        }

        // Flush remaining
        flushStack(stack, topLevel);

        return List.copyOf(topLevel);
    }

    private static void flushStack(Stack<BuildNode> stack, List<DataItem> topLevel) {
        // Pop items from the stack, attaching each to its parent.
        // Items popped first are deeper (higher level numbers) and are children
        // of items popped later (lower level numbers).
        List<DataItem> pending = new ArrayList<>();
        while (!stack.isEmpty()) {
            BuildNode node = stack.pop();
            // pending contains children (in reverse order since we pop from stack)
            if (!pending.isEmpty()) {
                List<DataItem> children = new ArrayList<>(pending);
                Collections.reverse(children);
                node.children.addAll(children);
                pending.clear();
            }
            pending.add(node.toDataItem());
        }
        Collections.reverse(pending);
        topLevel.addAll(pending);
    }

    private static class BuildNode {
        final FlatDataItem flat;
        final List<DataItem> children = new ArrayList<>();
        final List<ConditionName> conditionNames = new ArrayList<>();

        BuildNode(FlatDataItem flat) {
            this.flat = flat;
        }

        DataItem toDataItem() {
            return new DataItem(
                    flat.levelNumber(),
                    flat.name(),
                    flat.picture(),
                    flat.usage(),
                    flat.sign(),
                    flat.value(),
                    flat.occurs(),
                    flat.redefines(),
                    children.isEmpty() ? null : List.copyOf(children),
                    conditionNames.isEmpty() ? null : List.copyOf(conditionNames),
                    flat.isFiller(),
                    flat.justified(),
                    flat.blankWhenZero(),
                    flat.synchronizedField()
            );
        }
    }
}
