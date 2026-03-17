package com.danrus.bb4j.model.animation;

import java.util.Map;

public class AnimationController {
    private String uuid;
    private String name;
    private Map<String, AnimatorState> states;
    private Map<String, AnimationTransition> transitions;
    private Map<String, Object> extra;

    public AnimationController() {
        this.uuid = java.util.UUID.randomUUID().toString();
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

    public Map<String, AnimatorState> getStates() {
        return states;
    }

    public void setStates(Map<String, AnimatorState> states) {
        this.states = states;
    }

    public Map<String, AnimationTransition> getTransitions() {
        return transitions;
    }

    public void setTransitions(Map<String, AnimationTransition> transitions) {
        this.transitions = transitions;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public static class AnimatorState {
        private String name;
        private String animation;
        private Boolean blend;

        public AnimatorState() {}

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAnimation() {
            return animation;
        }

        public void setAnimation(String animation) {
            this.animation = animation;
        }

        public Boolean getBlend() {
            return blend;
        }

        public void setBlend(Boolean blend) {
            this.blend = blend;
        }
    }

    public static class AnimationTransition {
        private String from;
        private String to;
        private String condition;
        private Double duration;

        public AnimationTransition() {}

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public Double getDuration() {
            return duration;
        }

        public void setDuration(Double duration) {
            this.duration = duration;
        }
    }
}
