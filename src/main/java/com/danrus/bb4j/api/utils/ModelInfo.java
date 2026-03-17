package com.danrus.bb4j.api.utils;

import com.danrus.bb4j.model.BbModelDocument;

import java.util.*;

public class ModelInfo {
    
    private final BbModelDocument document;
    private final ElementUtils elementUtils;
    private final AnimationUtils animationUtils;
    private final TextureUtils textureUtils;
    private final OutlinerUtils outlinerUtils;
    
    private ModelInfo(BbModelDocument document) {
        this.document = document;
        this.elementUtils = ElementUtils.forDocument(document);
        this.animationUtils = AnimationUtils.forDocument(document);
        this.textureUtils = TextureUtils.forDocument(document);
        this.outlinerUtils = OutlinerUtils.forDocument(document);
    }
    
    public static ModelInfo fromDocument(BbModelDocument document) {
        return new ModelInfo(document);
    }
    
    public String getModelName() {
        return document.getMeta() != null ? document.getMeta().getName() : null;
    }
    
    public String getModelFormat() {
        return document.getMeta() != null && document.getMeta().getModelFormat() != null 
            ? document.getMeta().getModelFormat().getValue() 
            : null;
    }
    
    public String getFormatVersion() {
        return document.getMeta() != null && document.getMeta().getFormatVersion() != null 
            ? document.getMeta().getFormatVersion().getRaw() 
            : null;
    }
    
    public int getTextureWidth() {
        if (document.getResolution() != null && document.getResolution().getWidth() != null) {
            return document.getResolution().getWidth();
        }
        if (document.getMeta() != null && document.getMeta().getTextureWidth() != null) {
            return document.getMeta().getTextureWidth();
        }
        return 16;
    }
    
    public int getTextureHeight() {
        if (document.getResolution() != null && document.getResolution().getHeight() != null) {
            return document.getResolution().getHeight();
        }
        if (document.getMeta() != null && document.getMeta().getTextureHeight() != null) {
            return document.getMeta().getTextureHeight();
        }
        return 16;
    }
    
    public int getElementCount() {
        return elementUtils.getTotalElementCount();
    }
    
    public int getCubeCount() {
        return elementUtils.getCubes().size();
    }
    
    public int getMeshCount() {
        return elementUtils.getMeshes().size();
    }
    
    public int getTextureCount() {
        return textureUtils.getAllTextures().size();
    }
    
    public int getAnimationCount() {
        return animationUtils.getAllAnimations().size();
    }
    
    public int getGroupCount() {
        return outlinerUtils.getTotalGroupCount();
    }
    
    public double getModelWidth() {
        return elementUtils.getModelWidth();
    }
    
    public double getModelHeight() {
        return elementUtils.getModelHeight();
    }
    
    public double getModelDepth() {
        return elementUtils.getModelDepth();
    }
    
    public double getModelVolume() {
        return elementUtils.getTotalVolume();
    }
    
    public double getAnimationDuration() {
        return animationUtils.getTotalAnimationDuration();
    }
    
    public boolean hasAnimations() {
        return getAnimationCount() > 0;
    }
    
    public boolean hasTextures() {
        return getTextureCount() > 0;
    }
    
    public boolean hasGroups() {
        return getGroupCount() > 0;
    }
    
    public boolean isBedrockModel() {
        return document.getMeta() != null && 
               document.getMeta().getModelFormat() != null &&
               document.getMeta().getModelFormat().isBedrock();
    }
    
    public boolean isJavaModel() {
        return document.getMeta() != null && 
               document.getMeta().getModelFormat() != null &&
               document.getMeta().getModelFormat().isJava();
    }
    
    public Map<String, Object> toSummaryMap() {
        Map<String, Object> summary = new LinkedHashMap<>();
        
        summary.put("name", getModelName());
        summary.put("format", getModelFormat());
        summary.put("formatVersion", getFormatVersion());
        summary.put("textureSize", getTextureWidth() + "x" + getTextureHeight());
        summary.put("elementCount", getElementCount());
        summary.put("cubeCount", getCubeCount());
        summary.put("meshCount", getMeshCount());
        summary.put("textureCount", getTextureCount());
        summary.put("animationCount", getAnimationCount());
        summary.put("groupCount", getGroupCount());
        summary.put("modelSize", String.format("%.2f x %.2f x %.2f", getModelWidth(), getModelHeight(), getModelDepth()));
        summary.put("modelVolume", String.format("%.2f", getModelVolume()));
        
        return summary;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Model: ").append(getModelName()).append("\n");
        sb.append("Format: ").append(getModelFormat()).append(" (").append(getFormatVersion()).append(")\n");
        sb.append("Texture Size: ").append(getTextureWidth()).append("x").append(getTextureHeight()).append("\n");
        sb.append("Elements: ").append(getElementCount()).append(" (Cubes: ").append(getCubeCount()).append(", Meshes: ").append(getMeshCount()).append(")\n");
        sb.append("Textures: ").append(getTextureCount()).append("\n");
        sb.append("Animations: ").append(getAnimationCount()).append(" (Duration: ").append(getAnimationDuration()).append("s)\n");
        sb.append("Groups: ").append(getGroupCount()).append("\n");
        sb.append("Model Size: ").append(String.format("%.2f x %.2f x %.2f", getModelWidth(), getModelHeight(), getModelDepth()));
        return sb.toString();
    }
}
