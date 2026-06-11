package com.danrus.bb4j.model.animation;

import java.util.Map;
import java.util.UUID;

/**
 * A single named animation.
 *
 * <p>It carries timing ({@code length}, {@code start_time}/{@code end_time}),
 * playback flags ({@code loop}, {@code override}, {@code anim_time_update}), and a
 * map of {@link Animator}s keyed by the animated target's UUID. The {@code loop}
 * field is stored as a {@code Double} because Blockbench expresses it variously as
 * a boolean, the string {@code "loop"}, or a number; the reader normalizes those.
 */
public class Animation {
    /** Play the animation once and stop ({@code "once"}). */
    public static final String LOOP_ONCE = "once";
    /** Loop the animation continuously ({@code "loop"}). */
    public static final String LOOP_LOOP = "loop";
    /** Play once and hold the final frame ({@code "hold"}). */
    public static final String LOOP_HOLD = "hold";

    private String uuid;
    private String name;
    private String path;
    private String loop;
    private Double startTime;
    private Double endTime;
    private Double length;
    private Boolean override;
    private String animTimeUpdate;
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

    /**
     * The loop mode: {@link #LOOP_ONCE}, {@link #LOOP_LOOP}, or {@link #LOOP_HOLD}.
     * Stored as a string to match Blockbench's {@code .bbmodel} format (older
     * boolean/numeric representations are normalized to this enum on read).
     */
    public String getLoop() {
        return loop;
    }

    public void setLoop(String loop) {
        this.loop = loop;
    }

    /** @return {@code true} if this animation loops continuously. */
    public boolean isLooping() {
        return LOOP_LOOP.equals(loop);
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

    public String getAnimTimeUpdate() {
        return animTimeUpdate;
    }

    public void setAnimTimeUpdate(String animTimeUpdate) {
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
