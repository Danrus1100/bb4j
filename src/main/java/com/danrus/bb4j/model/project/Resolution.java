package com.danrus.bb4j.model.project;

import java.util.Map;

public class Resolution {
    private Integer width;
    private Integer height;
    private Map<String, Object> extra;

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

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }
}
