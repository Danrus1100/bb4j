package com.danrus.bb4j.model.raw;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExtraFields {
    private final Map<String, Object> fields;
    
    public ExtraFields() {
        this.fields = new HashMap<>();
    }
    
    public ExtraFields(Map<String, Object> fields) {
        this.fields = new HashMap<>(fields);
    }
    
    public Object get(String key) {
        return fields.get(key);
    }
    
    public void set(String key, Object value) {
        fields.put(key, value);
    }
    
    public void remove(String key) {
        fields.remove(key);
    }
    
    public boolean has(String key) {
        return fields.containsKey(key);
    }
    
    public Set<String> keySet() {
        return fields.keySet();
    }
    
    public Map<String, Object> toMap() {
        return new HashMap<>(fields);
    }
    
    public int size() {
        return fields.size();
    }
    
    public boolean isEmpty() {
        return fields.isEmpty();
    }
    
    public void merge(ExtraFields other) {
        if (other != null && other.fields != null) {
            this.fields.putAll(other.fields);
        }
    }
    
    public static ExtraFields fromJsonObject(Object json) {
        if (json instanceof Map) {
            return new ExtraFields((Map<String, Object>) json);
        }
        return new ExtraFields();
    }
}
