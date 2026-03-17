package com.danrus.bb4j.model.meta;

import java.util.Set;

public class ModelFormatId {
    public static final String FREE = "free";
    public static final String JAVA_BLOCK = "java_block";
    public static final String JAVA_ITEM = "java_item";
    public static final String BEDROCK_ENTITY = "bedrock_entity";
    public static final String BEDROCK_OLD = "bedrock_old";
    public static final String BEDROCK_RIG = "bedrock_rig";
    public static final String MOB_SKIN = "skin";
    public static final String POCKET = "pocket";
    public static final String CUSTOM = "custom";

    private static final Set<String> ALL_FORMATS = Set.of(
            FREE, JAVA_BLOCK, JAVA_ITEM, BEDROCK_ENTITY, 
            BEDROCK_OLD, BEDROCK_RIG, MOB_SKIN, POCKET, CUSTOM
    );

    private final String value;

    public ModelFormatId(String value) {
        this.value = value != null ? value : FREE;
    }

    public String getValue() {
        return value;
    }

    public boolean isValid() {
        return ALL_FORMATS.contains(value);
    }

    public boolean isJava() {
        return JAVA_BLOCK.equals(value) || JAVA_ITEM.equals(value);
    }

    public boolean isBedrock() {
        return BEDROCK_ENTITY.equals(value) || 
               BEDROCK_OLD.equals(value) || 
               BEDROCK_RIG.equals(value);
    }

    public boolean isEntity() {
        return BEDROCK_ENTITY.equals(value);
    }

    public boolean isBlock() {
        return JAVA_BLOCK.equals(value);
    }

    public boolean isItem() {
        return JAVA_ITEM.equals(value);
    }

    public boolean isSkin() {
        return MOB_SKIN.equals(value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelFormatId that = (ModelFormatId) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
