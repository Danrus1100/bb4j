package com.danrus.bb4j.model.animation;

public class Interpolation {
    public static final String LINEAR = "linear";
    public static final String CATMULLROM = "catmullrom";
    public static final String BEZIER = "bezier";
    public static final String STEPPED = "stepped";

    private final String value;

    public Interpolation(String value) {
        this.value = value != null ? value : LINEAR;
    }

    public String getValue() {
        return value;
    }

    public boolean isLinear() {
        return LINEAR.equals(value);
    }

    public boolean isCatmullrom() {
        return CATMULLROM.equals(value);
    }

    public boolean isBezier() {
        return BEZIER.equals(value);
    }

    public boolean isStepped() {
        return STEPPED.equals(value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Interpolation that = (Interpolation) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
