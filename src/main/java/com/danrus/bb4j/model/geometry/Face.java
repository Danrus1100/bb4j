package com.danrus.bb4j.model.geometry;

import java.util.Map;

/**
 * One face of a {@link CubeElement}, identified by direction
 * (north/south/east/west/up/down).
 *
 * <p>It records the {@link Uv} rectangle into the texture, the referenced
 * {@code texture}, an optional UV {@code rotation}, the {@code cullface}
 * direction string (e.g. {@code "north"}, {@code "down"}), and the
 * {@code tintindex} and {@code mirror_uv} flags.
 */
public class Face {
    public static final String NORTH = "north";
    public static final String SOUTH = "south";
    public static final String EAST = "east";
    public static final String WEST = "west";
    public static final String UP = "up";
    public static final String DOWN = "down";

    private String name;
    private Uv uv;
    private String texture;
    private Integer rotation;
    private String cullface;
    private Integer tintindex;
    private Boolean mirrorUv;
    private Map<String, Object> extra;

    public Face() {
        this.uv = new Uv();
    }

    public Face(String name) {
        this.name = name;
        this.uv = new Uv();
    }

    public Face(String name, Uv uv) {
        this.name = name;
        this.uv = uv;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Uv getUv() {
        return uv;
    }

    public void setUv(Uv uv) {
        this.uv = uv;
    }

    public String getTexture() {
        return texture;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }

    public Integer getRotation() {
        return rotation;
    }

    public void setRotation(Integer rotation) {
        this.rotation = rotation;
    }

    public String getCullface() {
        return cullface;
    }

    public void setCullface(String cullface) {
        this.cullface = cullface;
    }

    public Integer getTintindex() {
        return tintindex;
    }

    public void setTintindex(Integer tintindex) {
        this.tintindex = tintindex;
    }

    public Boolean getMirrorUv() {
        return mirrorUv;
    }

    public void setMirrorUv(Boolean mirrorUv) {
        this.mirrorUv = mirrorUv;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public Face copy() {
        Face copy = new Face(name);
        copy.uv = this.uv.copy();
        copy.texture = this.texture;
        copy.rotation = this.rotation;
        copy.cullface = this.cullface;
        copy.tintindex = this.tintindex;
        copy.mirrorUv = this.mirrorUv;
        return copy;
    }

    public static String getOpposite(String face) {
        return switch (face) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
            case UP -> DOWN;
            case DOWN -> UP;
            default -> face;
        };
    }
}
