package com.danrus.bb4j.model.outliner;

/**
 * A leaf outliner node that references an
 * {@link com.danrus.bb4j.model.geometry.Element} by its UUID. Serialized as a
 * bare string in the {@code outliner} array.
 */
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
