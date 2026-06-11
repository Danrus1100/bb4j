package com.danrus.bb4j.model.animation;

import java.util.Map;

/**
 * The value(s) held at a {@link Keyframe}, one component per axis
 * ({@code x}/{@code y}/{@code z}, plus {@code w} for some channels).
 *
 * <p>Each component is stored as a {@code String} rather than a number because a
 * Blockbench keyframe value may be either a literal number or a Molang
 * expression; keeping it as text preserves the original verbatim.
 */
public class DataPoint {
    private String x;
    private String y;
    private String z;
    private String w;
    private Map<String, Object> extra;

    public DataPoint() {}

    public DataPoint(String x, String y, String z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public DataPoint(String x, String y, String z, String w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getZ() {
        return z;
    }

    public void setZ(String z) {
        this.z = z;
    }

    public String getW() {
        return w;
    }

    public void setW(String w) {
        this.w = w;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public boolean isNumeric() {
        try {
            if (x != null) Double.parseDouble(x);
            if (y != null) Double.parseDouble(y);
            if (z != null) Double.parseDouble(z);
            if (w != null) Double.parseDouble(w);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public DataPoint copy() {
        DataPoint copy = new DataPoint();
        copy.x = this.x;
        copy.y = this.y;
        copy.z = this.z;
        copy.w = this.w;
        copy.extra = this.extra;
        return copy;
    }
}
