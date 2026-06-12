package com.danrus.bb4j.api.utils;

import com.danrus.bb4j.model.outliner.OutlinerNode;

import java.util.List;
import java.util.function.Consumer;

/**
 * Shared pre-order traversal of the outliner scene tree, so the various
 * {@code *Utils} collectors do not each re-implement the same recursion.
 */
final class OutlinerWalk {

    private OutlinerWalk() {}

    /**
     * Visits {@code roots} and all their descendants in pre-order (parent before
     * children). {@code null} node lists are treated as empty.
     */
    static void preOrder(List<OutlinerNode> roots, Consumer<OutlinerNode> visit) {
        if (roots == null) {
            return;
        }
        for (OutlinerNode node : roots) {
            if (node == null) {
                continue;
            }
            visit.accept(node);
            preOrder(node.getChildren(), visit);
        }
    }
}
