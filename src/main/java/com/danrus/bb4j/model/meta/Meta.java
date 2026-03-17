package com.danrus.bb4j.model.meta;

import java.time.Instant;
import java.util.Map;

public class Meta {
    private FormatVersion formatVersion;
    private ModelFormatId modelFormat;
    private String projectId;
    private String name;
    private String modelIdentifier;
    private Boolean boxUv;
    private Boolean visibleBox;
    private Boolean shadow;
    private Boolean boneRig;
    private Boolean mimic;
    private Integer textureWidth;
    private Integer textureHeight;
    private Integer unhandled;
    private Long creationTime;
    private Long modifyTime;
    private Boolean backup;
    private Integer addedModels;
    private Map<String, Object> extra;

    public Meta() {}

    public FormatVersion getFormatVersion() {
        return formatVersion;
    }

    public void setFormatVersion(FormatVersion formatVersion) {
        this.formatVersion = formatVersion;
    }

    public void setFormatVersion(String formatVersion) {
        this.formatVersion = new FormatVersion(formatVersion);
    }

    public ModelFormatId getModelFormat() {
        return modelFormat;
    }

    public void setModelFormat(ModelFormatId modelFormat) {
        this.modelFormat = modelFormat;
    }

    public void setModelFormat(String modelFormat) {
        this.modelFormat = new ModelFormatId(modelFormat);
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModelIdentifier() {
        return modelIdentifier;
    }

    public void setModelIdentifier(String modelIdentifier) {
        this.modelIdentifier = modelIdentifier;
    }

    public Boolean getBoxUv() {
        return boxUv;
    }

    public void setBoxUv(Boolean boxUv) {
        this.boxUv = boxUv;
    }

    public Boolean getVisibleBox() {
        return visibleBox;
    }

    public void setVisibleBox(Boolean visibleBox) {
        this.visibleBox = visibleBox;
    }

    public Boolean getShadow() {
        return shadow;
    }

    public void setShadow(Boolean shadow) {
        this.shadow = shadow;
    }

    public Boolean getBoneRig() {
        return boneRig;
    }

    public void setBoneRig(Boolean boneRig) {
        this.boneRig = boneRig;
    }

    public Boolean getMimic() {
        return mimic;
    }

    public void setMimic(Boolean mimic) {
        this.mimic = mimic;
    }

    public Integer getTextureWidth() {
        return textureWidth;
    }

    public void setTextureWidth(Integer textureWidth) {
        this.textureWidth = textureWidth;
    }

    public Integer getTextureHeight() {
        return textureHeight;
    }

    public void setTextureHeight(Integer textureHeight) {
        this.textureHeight = textureHeight;
    }

    public Integer getUnhandled() {
        return unhandled;
    }

    public void setUnhandled(Integer unhandled) {
        this.unhandled = unhandled;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    public Long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public Boolean getBackup() {
        return backup;
    }

    public void setBackup(Boolean backup) {
        this.backup = backup;
    }

    public Integer getAddedModels() {
        return addedModels;
    }

    public void setAddedModels(Integer addedModels) {
        this.addedModels = addedModels;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public Instant getCreationTimeAsInstant() {
        return creationTime != null ? Instant.ofEpochSecond(creationTime) : null;
    }

    public Instant getModifyTimeAsInstant() {
        return modifyTime != null ? Instant.ofEpochSecond(modifyTime) : null;
    }
}
