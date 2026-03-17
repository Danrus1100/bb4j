package com.danrus.bb4j.model.animation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Animator {
    private String uuid;
    private String name;
    private String type;
    private List<Keyframe> keyframes;
    private Map<String, Object> extra;

    public Animator() {
        this.uuid = UUID.randomUUID().toString();
    }

    public Animator(String uuid) {
        this.uuid = uuid;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Keyframe> getKeyframes() {
        return keyframes;
    }

    public void setKeyframes(List<Keyframe> keyframes) {
        this.keyframes = keyframes;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public void addKeyframe(Keyframe keyframe) {
        this.keyframes.add(keyframe);
    }

    public Keyframe findKeyframeAtTime(double time) {
        if (keyframes == null) return null;
        return keyframes.stream()
                .filter(k -> k.getTime() != null && Math.abs(k.getTime() - time) < 0.001)
                .findFirst()
                .orElse(null);
    }
}
