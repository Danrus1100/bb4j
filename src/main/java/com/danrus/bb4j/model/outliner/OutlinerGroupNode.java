package com.danrus.bb4j.model.outliner;

import java.util.ArrayList;
import java.util.List;

/**
 * A group/bone node in the outliner: a named, transformable container with
 * {@link #getChildren() child} nodes. Adds Bedrock-style {@code mirror},
 * {@code stretch}, and {@code box_size} fields on top of the common
 * {@link OutlinerNode} state.
 */
public class OutlinerGroupNode extends OutlinerNode {
    private Boolean mirror;
    private Integer[] stretch;
    private Integer boxSize;

    public OutlinerGroupNode() {
        super(OutlinerNode.GROUP);
    }

    public OutlinerGroupNode(String name) {
        super(OutlinerNode.GROUP);
        setName(name);
    }

    public Boolean getMirror() {
        return mirror;
    }

    public void setMirror(Boolean mirror) {
        this.mirror = mirror;
    }

    public Integer[] getStretch() {
        return stretch;
    }

    public void setStretch(Integer[] stretch) {
        this.stretch = stretch;
    }

    public Integer getBoxSize() {
        return boxSize;
    }

    public void setBoxSize(Integer boxSize) {
        this.boxSize = boxSize;
    }

    public void addElement(String elementUuid) {
        OutlinerElementRefNode elementRef = new OutlinerElementRefNode(elementUuid);
        addChild(elementRef);
    }
}
