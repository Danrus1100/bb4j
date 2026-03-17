package com.danrus.bb4j.model.animation;

import java.util.List;
import java.util.Map;

public class Keyframe {
    public static final String CHANNEL_POSITION = "position";
    public static final String CHANNEL_ROTATION = "rotation";
    public static final String CHANNEL_SCALE = "scale";
    public static final String CHANNEL_VISIBILITY = "visibility";

    private Double time;
    private String channel;
    private Interpolation interpolation;
    private List<DataPoint> dataPoints;
    private Double[] bezierLeftValue;
    private Double[] bezierRightValue;
    private Map<String, Object> extra;

    public Keyframe() {}

    public Keyframe(Double time, String channel) {
        this.time = time;
        this.channel = channel;
    }

    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Interpolation getInterpolation() {
        return interpolation;
    }

    public void setInterpolation(Interpolation interpolation) {
        this.interpolation = interpolation;
    }

    public void setInterpolation(String interpolation) {
        this.interpolation = new Interpolation(interpolation);
    }

    public List<DataPoint> getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(List<DataPoint> dataPoints) {
        this.dataPoints = dataPoints;
    }

    public Double[] getBezierLeftValue() {
        return bezierLeftValue;
    }

    public void setBezierLeftValue(Double[] bezierLeftValue) {
        this.bezierLeftValue = bezierLeftValue;
    }

    public Double[] getBezierRightValue() {
        return bezierRightValue;
    }

    public void setBezierRightValue(Double[] bezierRightValue) {
        this.bezierRightValue = bezierRightValue;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public Keyframe copy() {
        Keyframe copy = new Keyframe(this.time, this.channel);
        copy.interpolation = this.interpolation;
        if (this.dataPoints != null) {
            copy.dataPoints = this.dataPoints.stream()
                    .map(DataPoint::copy)
                    .toList();
        }
        if (this.bezierLeftValue != null) {
            copy.bezierLeftValue = this.bezierLeftValue.clone();
        }
        if (this.bezierRightValue != null) {
            copy.bezierRightValue = this.bezierRightValue.clone();
        }
        return copy;
    }
}
