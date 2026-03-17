package com.danrus.bb4j.model.outliner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OutlinerNode {
    public static final String GROUP = "group";
    public static final String EMPTY = "empty";

    private String uuid;
    private String type;
    private String name;
    private Integer[] rotation;
    private Double[] translation;
    private Double[] scale;
    private Boolean export;
    private List<OutlinerNode> children;
    private Object raw;

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

    public Integer[] getRotation() {
        return rotation;
    }

    public void setRotation(Integer[] rotation) {
        this.rotation = rotation;
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
