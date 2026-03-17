package com.danrus.bb4j.model.geometry;

import java.util.Map;
import java.util.UUID;

public class Element {
    public static final String CUBE = "cube";
    public static final String MESH = "mesh";

    private String uuid;
    private String type;
    private String name;
    private Double[] from;
    private Double[] to;
    private Integer[] rotation;
    private Double[] translation;
    private Double[] scale;
    private Boolean shade;
    private Boolean mirrorUv;
    private Boolean boxUv;
    private Map<String, Face> faces;
    private Map<String, Object> extra;

    public Element() {
        this.uuid = UUID.randomUUID().toString();
        this.type = CUBE;
        this.faces = new java.util.HashMap<>();
    }

    public Element(String type) {
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

    public Double[] getFrom() {
        return from;
    }

    public void setFrom(Double[] from) {
        this.from = from;
    }

    public Double[] getTo() {
        return to;
    }

    public void setTo(Double[] to) {
        this.to = to;
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

    public Boolean getShade() {
        return shade;
    }

    public void setShade(Boolean shade) {
        this.shade = shade;
    }

    public Boolean getMirrorUv() {
        return mirrorUv;
    }

    public void setMirrorUv(Boolean mirrorUv) {
        this.mirrorUv = mirrorUv;
    }

    public Boolean getBoxUv() {
        return boxUv;
    }

    public void setBoxUv(Boolean boxUv) {
        this.boxUv = boxUv;
    }

    public Map<String, Face> getFaces() {
        return faces;
    }

    public void setFaces(Map<String, Face> faces) {
        this.faces = faces;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public boolean isCube() {
        return CUBE.equals(type);
    }

    public boolean isMesh() {
        return MESH.equals(type);
    }

    public double getWidth() {
        if (from == null || to == null || from.length < 3 || to.length < 3) {
            return 0;
        }
        return to[0] - from[0];
    }

    public double getHeight() {
        if (from == null || to == null || from.length < 3 || to.length < 3) {
            return 0;
        }
        return to[1] - from[1];
    }

    public double getDepth() {
        if (from == null || to == null || from.length < 3 || to.length < 3) {
            return 0;
        }
        return to[2] - from[2];
    }

    public double getCenterX() {
        if (from == null || to == null || from.length < 3 || to.length < 3) {
            return 0;
        }
        return (from[0] + to[0]) / 2.0;
    }

    public double getCenterY() {
        if (from == null || to == null || from.length < 3 || to.length < 3) {
            return 0;
        }
        return (from[1] + to[1]) / 2.0;
    }

    public double getCenterZ() {
        if (from == null || to == null || from.length < 3 || to.length < 3) {
            return 0;
        }
        return (from[2] + to[2]) / 2.0;
    }
}
