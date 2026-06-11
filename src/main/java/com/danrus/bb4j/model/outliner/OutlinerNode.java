package com.danrus.bb4j.model.outliner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A node in the outliner scene tree.
 *
 * <p>Concretely a node is one of two things: an {@link OutlinerGroupNode} (a
 * named group/bone with a transform and {@link #getChildren() children}) or an
 * {@link OutlinerElementRefNode} (a leaf that references an
 * {@link com.danrus.bb4j.model.geometry.Element} by UUID). In JSON the former is
 * an object and the latter a bare string. This base class carries the fields
 * shared by group nodes; element references override the leaf behaviour.
 */
public class OutlinerNode {
    public static final String GROUP = "group";
    public static final String EMPTY = "empty";

    private String uuid;
    private String type;
    private String name;
    private Double[] rotation;
    private Double[] origin;
    private Double[] translation;
    private Double[] scale;
    private Boolean export;
    private List<OutlinerNode> children;
    private Object raw;
    private Map<String, Object> extra;

    public OutlinerNode() {
        this.uuid = UUID.randomUUID().toString();
    }

    public OutlinerNode(String type) {
        this();
        this.type = type;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double[] getRotation() {
        return rotation;
    }

    public void setRotation(Double[] rotation) {
        this.rotation = rotation;
    }

    public Double[] getOrigin() {
        return origin;
    }

    public void setOrigin(Double[] origin) {
        this.origin = origin;
    }

    public Double[] getTranslation() {
        return translation;
    }

    public void setTranslation(Double[] translation) {
        this.translation = translation;
    }

    public Double[] getScale() {
        return scale;
    }

    public void setScale(Double[] scale) {
        this.scale = scale;
    }

    public Boolean getExport() {
        return export;
    }

    public void setExport(Boolean export) {
        this.export = export;
    }

    public List<OutlinerNode> getChildren() {
        return children;
    }

    public void setChildren(List<OutlinerNode> children) {
        this.children = children;
    }

    public Object getRaw() {
        return raw;
    }

    public void setRaw(Object raw) {
        this.raw = raw;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public boolean isGroup() {
        return GROUP.equals(type);
    }

    public boolean isElement() {
        return uuid != null && children == null;
    }

    public void addChild(OutlinerNode child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    public List<OutlinerNode> getAllChildren() {
        List<OutlinerNode> result = new ArrayList<>();
        if (children != null) {
            for (OutlinerNode child : children) {
                result.add(child);
                if (child.getChildren() != null) {
                    result.addAll(child.getAllChildren());
                }
            }
        }
        return result;
    }
}
