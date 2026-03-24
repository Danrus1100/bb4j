package com.danrus.bb4j.model;

import com.danrus.bb4j.model.animation.Animation;
import com.danrus.bb4j.model.geometry.Element;
import com.danrus.bb4j.model.meta.Meta;
import com.danrus.bb4j.model.outliner.OutlinerNode;
import com.danrus.bb4j.model.project.Display;
import com.danrus.bb4j.model.project.EditorState;
import com.danrus.bb4j.model.project.History;
import com.danrus.bb4j.model.project.Resolution;
import com.danrus.bb4j.model.texture.Texture;
import com.danrus.bb4j.model.texture.TextureGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BbModelDocument {
    private Meta meta;
    private Resolution resolution;
    private List<Texture> textures;
    private List<Element> elements;
    private List<Group> groups;
    private List<OutlinerNode> outliner;
    private List<Animation> animations;
    private List<AnimationController> animationControllers;
    private Display display;
    private List<ReferenceImage> referenceImages;
    private EditorState editorState;
    private History history;
    private ExportOptions exportOptions;
    private List<Collection> collections;
    private List<TextureGroup> textureGroups;
    private Map<String, Object> rawData;
    private List<Warning> warnings;

    public BbModelDocument() {
        this.meta = new Meta();
        this.resolution = new Resolution(16, 16);
        this.textures = new ArrayList<>();
        this.elements = new ArrayList<>();
        this.groups = new ArrayList<>();
        this.outliner = new ArrayList<>();
        this.animations = new ArrayList<>();
        this.animationControllers = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public Resolution getResolution() {
        return resolution;
    }

    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }

    public List<Texture> getTextures() {
        return textures;
    }

    public void setTextures(List<Texture> textures) {
        this.textures = textures;
    }

    public void addTexture(Texture texture) {
        this.textures.add(texture);
    }

    public List<Element> getElements() {
        return elements;
    }

    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

    public void addElement(Element element) {
        this.elements.add(element);
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public void addGroup(Group group) {
        this.groups.add(group);
    }

    public List<OutlinerNode> getOutliner() {
        return outliner;
    }

    public void setOutliner(List<OutlinerNode> outliner) {
        this.outliner = outliner;
    }

    public void addOutlinerNode(OutlinerNode node) {
        this.outliner.add(node);
    }

    public List<Animation> getAnimations() {
        return animations;
    }

    public void setAnimations(List<Animation> animations) {
        this.animations = animations;
    }

    public void addAnimation(Animation animation) {
        this.animations.add(animation);
    }

    public List<AnimationController> getAnimationControllers() {
        return animationControllers;
    }

    public void setAnimationControllers(List<AnimationController> animationControllers) {
        this.animationControllers = animationControllers;
    }

    public Display getDisplay() {
        return display;
    }

    public void setDisplay(Display display) {
        this.display = display;
    }

    public List<ReferenceImage> getReferenceImages() {
        return referenceImages;
    }

    public void setReferenceImages(List<ReferenceImage> referenceImages) {
        this.referenceImages = referenceImages;
    }

    public EditorState getEditorState() {
        return editorState;
    }

    public void setEditorState(EditorState editorState) {
        this.editorState = editorState;
    }

    public History getHistory() {
        return history;
    }

    public void setHistory(History history) {
        this.history = history;
    }

    public ExportOptions getExportOptions() {
        return exportOptions;
    }

    public void setExportOptions(ExportOptions exportOptions) {
        this.exportOptions = exportOptions;
    }

    public List<Collection> getCollections() {
        return collections;
    }

    public void setCollections(List<Collection> collections) {
        this.collections = collections;
    }

    public List<TextureGroup> getTextureGroups() {
        return textureGroups;
    }

    public void setTextureGroups(List<TextureGroup> textureGroups) {
        this.textureGroups = textureGroups;
    }

    public Map<String, Object> getRawData() {
        return rawData;
    }

    public void setRawData(Map<String, Object> rawData) {
        this.rawData = rawData;
    }

    public List<Warning> getWarnings() {
        return warnings;
    }

    public void addWarning(Warning warning) {
        this.warnings.add(warning);
    }

    public Texture findTextureByUuid(String uuid) {
        return textures.stream()
                .filter(t -> t.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    public Element findElementByUuid(String uuid) {
        return elements.stream()
                .filter(e -> e.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    public Animation findAnimationByUuid(String uuid) {
        return animations.stream()
                .filter(a -> a.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    public static class Group {
        private Double[] origin;
        public Double[] getOrigin() { return origin; }
        public void setOrigin(Double[] origin) { this.origin = origin; }
        private String uuid;
        private String name;
        
        
        
        private Integer[] rotation;
        private Boolean mirror;
        private Integer[] stretch;
        private Integer boxSize;
        private Boolean export;
        private Map<String, Object> extra;

        public Group() {
            this.uuid = java.util.UUID.randomUUID().toString();
        }

        public Group(String name) {
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

        public Integer[] getRotation() {
            return rotation;
        }

        
        
        

        public void setRotation(Integer[] rotation) {
            this.rotation = rotation;
        }

        public Boolean getMirror() {
            return mirror;
        }

        public void setMirror(Boolean mirror) {
            this.mirror = mirror;
        }

        public Integer[] getStretch() {
            return stretch;
        }

        public void setStretch(Integer[] stretch) {
            this.stretch = stretch;
        }

        public Integer getBoxSize() {
            return boxSize;
        }

        public void setBoxSize(Integer boxSize) {
            this.boxSize = boxSize;
        }

        public Boolean getExport() {
            return export;
        }

        public void setExport(Boolean export) {
            this.export = export;
        }

        public Map<String, Object> getExtra() {
            return extra;
        }

        public void setExtra(Map<String, Object> extra) {
            this.extra = extra;
        }
    }

    public static class AnimationController {
        private String uuid;
        private String name;
        
        
        
        private Map<String, Object> states;
        private Map<String, Object> transitions;
        private Map<String, Object> extra;

        public AnimationController() {
            this.uuid = java.util.UUID.randomUUID().toString();
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

        public Map<String, Object> getStates() {
            return states;
        }

        public void setStates(Map<String, Object> states) {
            this.states = states;
        }

        public Map<String, Object> getTransitions() {
            return transitions;
        }

        public void setTransitions(Map<String, Object> transitions) {
            this.transitions = transitions;
        }

        public Map<String, Object> getExtra() {
            return extra;
        }

        public void setExtra(Map<String, Object> extra) {
            this.extra = extra;
        }
    }

    public static class ReferenceImage {
        private String uuid;
        private String name;
        
        
        
        private Double[] position;
        private Double[] size;
        private String source;
        private Boolean isBlueprint;
        private String layer;
        private Map<String, Object> extra;

        public ReferenceImage() {
            this.uuid = java.util.UUID.randomUUID().toString();
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

        public Double[] getPosition() {
            return position;
        }

        public void setPosition(Double[] position) {
            this.position = position;
        }

        public Double[] getSize() {
            return size;
        }

        public void setSize(Double[] size) {
            this.size = size;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public Boolean getIsBlueprint() {
            return isBlueprint;
        }

        public void setIsBlueprint(Boolean isBlueprint) {
            this.isBlueprint = isBlueprint;
        }

        public String getLayer() {
            return layer;
        }

        public void setLayer(String layer) {
            this.layer = layer;
        }

        public Map<String, Object> getExtra() {
            return extra;
        }

        public void setExtra(Map<String, Object> extra) {
            this.extra = extra;
        }
    }

    public static class Collection {
        private String uuid;
        private String name;
        
        
        
        private Integer order;
        private String color;
        private Boolean locked;
        private Boolean hidden;
        private Map<String, Object> extra;

        public Collection() {
            this.uuid = java.util.UUID.randomUUID().toString();
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

        public Integer getOrder() {
            return order;
        }

        public void setOrder(Integer order) {
            this.order = order;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public Boolean getLocked() {
            return locked;
        }

        public void setLocked(Boolean locked) {
            this.locked = locked;
        }

        public Boolean getHidden() {
            return hidden;
        }

        public void setHidden(Boolean hidden) {
            this.hidden = hidden;
        }

        public Map<String, Object> getExtra() {
            return extra;
        }

        public void setExtra(Map<String, Object> extra) {
            this.extra = extra;
        }
    }

    public static class ExportOptions {
        private Map<String, Map<String, Object>> options;

        public ExportOptions() {
            this.options = new HashMap<>();
        }

        public Map<String, Map<String, Object>> getOptions() {
            return options;
        }

        public void setOptions(Map<String, Map<String, Object>> options) {
            this.options = options;
        }

        public Map<String, Object> getOptionsFor(String codecId) {
            return options.get(codecId);
        }

        public void putOptionsFor(String codecId, Map<String, Object> codecOptions) {
            options.put(codecId, codecOptions);
        }
    }

    public static class Warning {
        private final String message;
        private final WarningType type;
        private final String path;

        public Warning(String message, WarningType type) {
            this(message, type, null);
        }

        public Warning(String message, WarningType type, String path) {
            this.message = message;
            this.type = type;
            this.path = path;
        }

        public String getMessage() {
            return message;
        }

        public WarningType getType() {
            return type;
        }

        public String getPath() {
            return path;
        }

        public enum WarningType {
            UNSUPPORTED_VERSION,
            DEPRECATED_FIELD,
            MISSING_REQUIRED_FIELD,
            UNKNOWN_FIELD,
            PARSE_WARNING
        }
    }
}
