package com.danrus.bb4j.model.texture;

import java.util.Map;
import java.util.UUID;

public class Texture {
    private String uuid;
    private String name;
    private String path;
    private String relativePath;
    private String source;
    private Boolean internal;
    private Boolean renderSides;
    private Integer id;
    private Integer width;
    private Integer height;
    private Integer uvWidth;
    private Integer uvHeight;
    private String particleData;
    private String folder;
    private Boolean pinned;
    private Map<String, Object> extra;

    public Texture() {
        this.uuid = UUID.randomUUID().toString();
    }

    public Texture(String name) {
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

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Boolean getInternal() {
        return internal;
    }

    public void setInternal(Boolean internal) {
        this.internal = internal;
    }

    public Boolean getRenderSides() {
        return renderSides;
    }

    public void setRenderSides(Boolean renderSides) {
        this.renderSides = renderSides;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getUvWidth() {
        return uvWidth;
    }

    public void setUvWidth(Integer uvWidth) {
        this.uvWidth = uvWidth;
    }

    public Integer getUvHeight() {
        return uvHeight;
    }

    public void setUvHeight(Integer uvHeight) {
        this.uvHeight = uvHeight;
    }

    public String getParticleData() {
        return particleData;
    }

    public void setParticleData(String particleData) {
        this.particleData = particleData;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public Boolean getPinned() {
        return pinned;
    }

    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public boolean isEmbedded() {
        return source != null && source.startsWith("data:");
    }

    public boolean hasFileReference() {
        return path != null || relativePath != null;
    }
}
