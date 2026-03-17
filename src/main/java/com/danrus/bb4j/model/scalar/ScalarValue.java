package com.danrus.bb4j.model.scalar;

import java.util.Objects;

public class ScalarValue {
    private final Object value;
    private final ScalarType type;
    
    public enum ScalarType {
        STRING,
        NUMBER,
        BOOLEAN,
        NULL
    }
    
    protected ScalarValue(Object value, ScalarType type) {
        this.value = value;
        this.type = type;
    }
    
    public static ScalarValue of(String value) {
        return new ScalarValue(value, ScalarType.STRING);
    }
    
    public static ScalarValue of(Number value) {
        return new ScalarValue(value, ScalarType.NUMBER);
    }
    
    public static ScalarValue of(Boolean value) {
        return new ScalarValue(value, ScalarType.BOOLEAN);
    }
    
    public static ScalarValue ofNull() {
        return new ScalarValue(null, ScalarType.NULL);
    }
    
    public static ScalarValue parse(Object value) {
        if (value == null) return ofNull();
        if (value instanceof Number) return of((Number) value);
        if (value instanceof Boolean) return of((Boolean) value);
        if (value instanceof String) return parseString((String) value);
        return of(value.toString());
    }
    
    private static ScalarValue parseString(String value) {
        try {
            if (value.contains(".")) {
                return of(Double.parseDouble(value));
            }
            return of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                return of(Boolean.parseBoolean(value));
            }
            return of(value);
        }
    }
    
    public Object getValue() {
        return value;
    }
    
    public ScalarType getType() {
        return type;
    }
    
    public String asString() {
        return value != null ? value.toString() : null;
    }
    
    public Number asNumber() {
        return (Number) value;
    }
    
    public Integer asInt() {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }
    
    public Double asDouble() {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }
    
    public Boolean asBoolean() {
        return (Boolean) value;
    }
    
    public boolean isString() {
        return type == ScalarType.STRING;
    }
    
    public boolean isNumber() {
        return type == ScalarType.NUMBER;
    }
    
    public boolean isBoolean() {
        return type == ScalarType.BOOLEAN;
    }
    
    public boolean isNull() {
        return type == ScalarType.NULL;
    }
    
    @Override
    public String toString() {
        return value != null ? value.toString() : "null";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScalarValue that = (ScalarValue) o;
        return Objects.equals(value, that.value) && type == that.type;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }
}
