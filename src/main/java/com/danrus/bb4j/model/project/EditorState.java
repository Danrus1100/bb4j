package com.danrus.bb4j.model.project;

import java.util.List;
import java.util.Map;

public class EditorState {
    private String savePath;
    private String exportPath;
    private Boolean saved;
    private Integer addedModels;
    private String mode;
    private String tool;
    private String displayUv;
    private Boolean explodedView;
    private UvViewport uvViewport;
    private Map<String, Object> previews;
    private List<String> selectedElements;
    private List<String> selectedGroups;
    private List<List<Double>> meshSelection;
    private String selectedTexture;
    private Map<String, Object> extra;

    public EditorState() {}

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public String getExportPath() {
        return exportPath;
    }

    public void setExportPath(String exportPath) {
        this.exportPath = exportPath;
    }

    public Boolean getSaved() {
        return saved;
    }

    public void setSaved(Boolean saved) {
        this.saved = saved;
    }

    public Integer getAddedModels() {
        return addedModels;
    }

    public void setAddedModels(Integer addedModels) {
        this.addedModels = addedModels;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public String getDisplayUv() {
        return displayUv;
    }

    public void setDisplayUv(String displayUv) {
        this.displayUv = displayUv;
    }

    public Boolean getExplodedView() {
        return explodedView;
    }

    public void setExplodedView(Boolean explodedView) {
        this.explodedView = explodedView;
    }

    public UvViewport getUvViewport() {
        return uvViewport;
    }

    public void setUvViewport(UvViewport uvViewport) {
        this.uvViewport = uvViewport;
    }

    public Map<String, Object> getPreviews() {
        return previews;
    }

    public void setPreviews(Map<String, Object> previews) {
        this.previews = previews;
    }

    public List<String> getSelectedElements() {
        return selectedElements;
    }

    public void setSelectedElements(List<String> selectedElements) {
        this.selectedElements = selectedElements;
    }

    public List<String> getSelectedGroups() {
        return selectedGroups;
    }

    public void setSelectedGroups(List<String> selectedGroups) {
        this.selectedGroups = selectedGroups;
    }

    public List<List<Double>> getMeshSelection() {
        return meshSelection;
    }

    public void setMeshSelection(List<List<Double>> meshSelection) {
        this.meshSelection = meshSelection;
    }

    public String getSelectedTexture() {
        return selectedTexture;
    }

    public void setSelectedTexture(String selectedTexture) {
        this.selectedTexture = selectedTexture;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public static class UvViewport {
        private Double zoom;
        private Double[] offset;

        public UvViewport() {}

        public Double getZoom() {
            return zoom;
        }

        public void setZoom(Double zoom) {
            this.zoom = zoom;
        }

        public Double[] getOffset() {
            return offset;
        }

        public void setOffset(Double[] offset) {
            this.offset = offset;
        }
    }
}
