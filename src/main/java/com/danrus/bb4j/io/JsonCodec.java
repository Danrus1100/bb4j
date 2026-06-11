package com.danrus.bb4j.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

public class JsonCodec {
    private final Gson gson;
    private final Gson prettyGson;

    public JsonCodec() {
        this.gson = new GsonBuilder().serializeNulls().create();
        this.prettyGson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
    }

    public <T> T fromJson(String json, Class<T> classOfT) {
        try {
            return gson.fromJson(json, classOfT);
        } catch (JsonSyntaxException e) {
            throw new com.danrus.bb4j.api.BbException("PARSE_ERROR", "Failed to parse JSON: " + e.getMessage(), e);
        }
    }

    public <T> T fromJson(Reader reader, Class<T> classOfT) {
        try {
            return gson.fromJson(reader, classOfT);
        } catch (JsonSyntaxException e) {
            throw new com.danrus.bb4j.api.BbException("PARSE_ERROR", "Failed to parse JSON: " + e.getMessage(), e);
        }
    }

    public String toJson(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * Converts an arbitrary Java value into a Gson {@link JsonElement} tree.
     * Used when re-emitting preserved "extra" fields that were stored as plain
     * Java objects rather than as {@link JsonElement}s.
     *
     * @param obj the value to convert (may be {@code null})
     * @return the corresponding JSON tree
     */
    public JsonElement toTree(Object obj) {
        return gson.toJsonTree(obj);
    }

    public String toPrettyJson(Object obj) {
        return prettyGson.toJson(obj);
    }

    public void toJson(Object obj, Writer writer) {
        gson.toJson(obj, writer);
    }

    public void toPrettyJson(Object obj, Writer writer) {
        prettyGson.toJson(obj, writer);
    }

    public JsonElement parse(String json) {
        try {
            return JsonParser.parseString(json);
        } catch (JsonSyntaxException e) {
            throw new com.danrus.bb4j.api.BbException("PARSE_ERROR", "Failed to parse JSON: " + e.getMessage(), e);
        }
    }

    public JsonElement parse(Reader reader) {
        try {
            return JsonParser.parseReader(reader);
        } catch (JsonSyntaxException e) {
            throw new com.danrus.bb4j.api.BbException("PARSE_ERROR", "Failed to parse JSON: " + e.getMessage(), e);
        }
    }

    public String minify(String json) {
        JsonElement element = parse(json);
        return gson.toJson(element);
    }

    public String prettify(String json) {
        JsonElement element = parse(json);
        return prettyGson.toJson(element);
    }

    public boolean isValidJson(String json) {
        try {
            JsonParser.parseString(json);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }
}
