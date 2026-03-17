package com.danrus.bb4j.model.animation;

public class DataPoint {
    private String x;
    private String y;
    private String z;
    private String w;

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
        return copy;
    }
}
