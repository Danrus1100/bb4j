package com.danrus.bb4j.model.texture;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TextureGroup {
    private String uuid;
    private String name;
    private List<String> textures;
    private Integer order;
    private String folder;
    private Map<String, Object> extra;

    public TextureGroup() {
        this.uuid = UUID.randomUUID().toString();
    }

    public TextureGroup(String name) {
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

    public List<String> getTextures() {
        return textures;
    }

    public void setTextures(List<String> textures) {
        this.textures = textures;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }
}
