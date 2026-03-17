package com.danrus.bb4j.model.outliner;

public class OutlinerElementRefNode extends OutlinerNode {
    private String elementUuid;

    public OutlinerElementRefNode() {
        super();
    }

    public OutlinerElementRefNode(String elementUuid) {
        super();
        this.elementUuid = elementUuid;
        setUuid(elementUuid);
    }

    public String getElementUuid() {
        return elementUuid;
    }

    public void setElementUuid(String elementUuid) {
        this.elementUuid = elementUuid;
    }

    @Override
    public boolean isElement() {
        return true;
    }
}
