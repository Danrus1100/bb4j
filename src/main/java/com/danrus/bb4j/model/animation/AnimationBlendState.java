package com.danrus.bb4j.model.animation;

public class AnimationBlendState {
    private String animationName;
    private double time;
    private double weight;

    public AnimationBlendState(String animationName, double time, double weight) {
        this.animationName = animationName;
        this.time = time;
        this.weight = weight;
    }

    public String getAnimationName() {
        return animationName;
    }

    public void setAnimationName(String animationName) {
        this.animationName = animationName;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
