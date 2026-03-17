package com.danrus.bb4j.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Map;

public class JsonUtil {
    
    public static JsonObject getAsJsonObject(JsonObject obj, String key) {
        if (obj == null || !obj.has(key)) {
            return null;
        }
        JsonElement element = obj.get(key);
        return element.isJsonObject() ? element.getAsJsonObject() : null;
    }
    
    public static JsonArray getAsJsonArray(JsonObject obj, String key) {
        if (obj == null || !obj.has(key)) {
            return null;
        }
        JsonElement element = obj.get(key);
        return element.isJsonArray() ? element.getAsJsonArray() : null;
    }
    
    public static String getAsString(JsonObject obj, String key) {
        if (obj == null || !obj.has(key)) {
            return null;
        }
        JsonElement element = obj.get(key);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element.getAsString();
    }
    
    public static String getAsString(JsonObject obj, String key, String defaultValue) {
        String value = getAsString(obj, key);
        return value != null ? value : defaultValue;
    }
    
    public static Integer getAsInt(JsonObject obj, String key) {
        if (obj == null || !obj.has(key)) {
            return null;
        }
        JsonElement element = obj.get(key);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element.getAsInt();
    }
    
    public static int getAsInt(JsonObject obj, String key, int defaultValue) {
        Integer value = getAsInt(obj, key);
        return value != null ? value : defaultValue;
    }
    
    public static Long getAsLong(JsonObject obj, String key) {
        if (obj == null || !obj.has(key)) {
            return null;
        }
        JsonElement element = obj.get(key);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element.getAsLong();
    }
    
    public static Double getAsDouble(JsonObject obj, String key) {
        if (obj == null || !obj.has(key)) {
            return null;
        }
        JsonElement element = obj.get(key);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element.getAsDouble();
    }
    
    public static Boolean getAsBoolean(JsonObject obj, String key) {
        if (obj == null || !obj.has(key)) {
            return null;
        }
        JsonElement element = obj.get(key);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element.getAsBoolean();
    }
    
    public static boolean getAsBoolean(JsonObject obj, String key, boolean defaultValue) {
        Boolean value = getAsBoolean(obj, key);
        return value != null ? value : defaultValue;
    }
    
    public static boolean hasKey(JsonObject obj, String key) {
        return obj != null && obj.has(key);
    }
    
    public static boolean isNull(JsonElement element) {
        return element == null || element.isJsonNull();
    }
    
    public static JsonPrimitive createPrimitive(Object value) {
        if (value == null) {
            return new JsonPrimitive((String) null);
        }
        if (value instanceof String) {
            return new JsonPrimitive((String) value);
        }
        if (value instanceof Number) {
            return new JsonPrimitive((Number) value);
        }
        if (value instanceof Boolean) {
            return new JsonPrimitive((Boolean) value);
        }
        if (value instanceof Character) {
            return new JsonPrimitive((Character) value);
        }
        return new JsonPrimitive(value.toString());
    }
    
    public static void putIfNotNull(JsonObject obj, String key, Object value) {
        if (value != null) {
            obj.add(key, createPrimitive(value));
        }
    }
    
    public static void putIfNotNull(JsonObject obj, String key, String value) {
        if (value != null) {
            obj.addProperty(key, value);
        }
    }
    
    public static void putIfNotNull(JsonObject obj, String key, Number value) {
        if (value != null) {
            obj.addProperty(key, value);
        }
    }
    
    public static void putIfNotNull(JsonObject obj, String key, Boolean value) {
        if (value != null) {
            obj.addProperty(key, value);
        }
    }
    
    public static String toStringValue(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        }
        return element.toString();
    }
}
