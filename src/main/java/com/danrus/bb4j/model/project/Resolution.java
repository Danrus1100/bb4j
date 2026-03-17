package com.danrus.bb4j.model.project;

public class Resolution {
    private Integer width;
    private Integer height;

    public Resolution() {}

    public Resolution(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }
}
