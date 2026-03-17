package com.danrus.bb4j.model.outliner;

import java.util.ArrayList;
import java.util.List;

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
