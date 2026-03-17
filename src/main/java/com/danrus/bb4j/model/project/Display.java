package com.danrus.bb4j.model.project;

import java.util.Map;

public class Display {
    public static final String FIRST_PERSON = "first_person";
    public static final String THIRD_PERSON = "third_person";
    public static final String GUI = "gui";
    public static final String HEAD = "head";
    public static final String GROUND = "ground";
    public static final String FIXED = "fixed";

    private Map<String, DisplaySlot> slots;

    public Display() {}

    public Map<String, DisplaySlot> getSlots() {
        return slots;
    }

    public void setSlots(Map<String, DisplaySlot> slots) {
        this.slots = slots;
    }

    public DisplaySlot getSlot(String name) {
        return slots != null ? slots.get(name) : null;
    }

    public void putSlot(String name, DisplaySlot slot) {
        if (slots == null) {
            slots = new java.util.HashMap<>();
        }
        slots.put(name, slot);
    }

    public static class DisplaySlot {
        private Double[] rotation;
        private Double[] translation;
        private Double[] scale;

        public DisplaySlot() {}

        public Double[] getRotation() {
            return rotation;
        }

        public void setRotation(Double[] rotation) {
            this.rotation = rotation;
        }

        public Double[] getTranslation() {
            return translation;
        }

        public void setTranslation(Double[] translation) {
            this.translation = translation;
        }

        public Double[] getScale() {
            return scale;
        }

        public void setScale(Double[] scale) {
            this.scale = scale;
        }
    }
}
