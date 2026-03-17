package com.danrus.bb4j.model.geometry;

public class Uv {
    private double[] uv;

    public Uv() {
        this.uv = new double[4];
    }

    public Uv(double u1, double v1, double u2, double v2) {
        this.uv = new double[]{u1, v1, u2, v2};
    }

    public Uv(double[] uv) {
        if (uv != null && uv.length == 4) {
            this.uv = uv.clone();
        } else {
            this.uv = new double[4];
        }
    }

    public double[] getUv() {
        return uv;
    }

    public void setUv(double[] uv) {
        if (uv != null && uv.length == 4) {
            this.uv = uv.clone();
        }
    }

    public double getU1() {
        return uv[0];
    }

    public void setU1(double u1) {
        this.uv[0] = u1;
    }

    public double getV1() {
        return uv[1];
    }

    public void setV1(double v1) {
        this.uv[1] = v1;
    }

    public double getU2() {
        return uv[2];
    }

    public void setU2(double u2) {
        this.uv[2] = u2;
    }

    public double getV2() {
        return uv[3];
    }

    public void setV2(double v2) {
        this.uv[3] = v2;
    }

    public double getWidth() {
        return Math.abs(uv[2] - uv[0]);
    }

    public double getHeight() {
        return Math.abs(uv[3] - uv[1]);
    }

    public void rotate90() {
        double[] newUv = new double[4];
        newUv[0] = uv[1];
        newUv[1] = 16 - uv[2];
        newUv[2] = uv[3];
        newUv[3] = 16 - uv[0];
        this.uv = newUv;
    }

    public void flipHorizontal() {
        double temp = uv[0];
        uv[0] = uv[2];
        uv[2] = temp;
    }

    public void flipVertical() {
        double temp = uv[1];
        uv[1] = uv[3];
        uv[3] = temp;
    }

    public Uv copy() {
        return new Uv(uv.clone());
    }
}
