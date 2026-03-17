package com.danrus.bb4j.model.animation;

import java.util.Map;
import java.util.UUID;

public class Animation {
    private String uuid;
    private String name;
    private String path;
    private Double loop;
    private Double startTime;
    private Double endTime;
    private Double length;
    private Boolean override;
    private Boolean animTimeUpdate;
    private Boolean special;
    private Map<String, Animator> animators;
    private Map<String, Object> extra;

    public Animation() {
        this.uuid = UUID.randomUUID().toString();
    }

    public Animation(String name) {
        this();
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Double getLoop() {
        return loop;
    }

    public void setLoop(Double loop) {
        this.loop = loop;
    }

    public Double getStartTime() {
        return startTime;
    }

    public void setStartTime(Double startTime) {
        this.startTime = startTime;
    }

    public Double getEndTime() {
        return endTime;
    }

    public void setEndTime(Double endTime) {
        this.endTime = endTime;
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    public Boolean getOverride() {
        return override;
    }

    public void setOverride(Boolean override) {
        this.override = override;
    }

    public Boolean getAnimTimeUpdate() {
        return animTimeUpdate;
    }

    public void setAnimTimeUpdate(Boolean animTimeUpdate) {
        this.animTimeUpdate = animTimeUpdate;
    }

    public Boolean getSpecial() {
        return special;
    }

    public void setSpecial(Boolean special) {
        this.special = special;
    }

    public Map<String, Animator> getAnimators() {
        return animators;
    }

    public void setAnimators(Map<String, Animator> animators) {
        this.animators = animators;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public void addAnimator(Animator animator) {
        this.animators.put(animator.getUuid(), animator);
    }

    public Animator getAnimator(String uuid) {
        return this.animators != null ? this.animators.get(uuid) : null;
    }

    public double getDuration() {
        if (length != null) return length;
        if (endTime != null && startTime != null) return endTime - startTime;
        return 0;
    }
}
