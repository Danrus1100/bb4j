package com.danrus.bb4j.model.raw;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RawObject {
    private final Map<String, Object> properties;
    
    public RawObject() {
        this.properties = new HashMap<>();
    }
    
    public RawObject(Map<String, Object> properties) {
        this.properties = new HashMap<>(properties);
    }
    
    public Object get(String key) {
        return properties.get(key);
    }
    
    public void set(String key, Object value) {
        properties.put(key, value);
    }
    
    public void remove(String key) {
        properties.remove(key);
    }
    
    public boolean has(String key) {
        return properties.containsKey(key);
    }
    
    public Map<String, Object> toMap() {
        return new HashMap<>(properties);
    }
    
    public String getString(String key) {
        Object value = properties.get(key);
        return value != null ? value.toString() : null;
    }
    
    public Integer getInt(String key) {
        Object value = properties.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    public Double getDouble(String key) {
        Object value = properties.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    public Boolean getBoolean(String key) {
        Object value = properties.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, Object> getObject(String key) {
        Object value = properties.get(key);
        if (value instanceof Map) {
            return new HashMap<>((Map<String, Object>) value);
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public List<Object> getArray(String key) {
        Object value = properties.get(key);
        if (value instanceof List) {
            return (List<Object>) value;
        }
        return null;
    }
}
