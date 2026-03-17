package com.danrus.bb4j.io;

import com.danrus.bb4j.api.BbException;
import com.danrus.bb4j.api.CompressionMode;
import com.danrus.bb4j.api.ReadOptions;
import com.danrus.bb4j.api.VersionPolicy;
import com.danrus.bb4j.migrate.Migrator;
import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.animation.*;
import com.danrus.bb4j.model.geometry.*;
import com.danrus.bb4j.model.meta.*;
import com.danrus.bb4j.model.outliner.*;
import com.danrus.bb4j.model.project.*;
import com.danrus.bb4j.model.texture.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BbModelReader {
    private static final JsonCodec JSON_CODEC = new JsonCodec();
    private static final String CURRENT_FORMAT_VERSION = "5.0";

    public static BbModelDocument read(String json, ReadOptions options) {
        String decompressed = decompressIfNeeded(json, options.getCompressionMode());
        JsonObject root = JSON_CODEC.parse(decompressed).getAsJsonObject();
        
        BbModelDocument document = parseDocument(root, options);
        
        if (options.getVersionPolicy() != VersionPolicy.IGNORE) {
            Migrator.migrateIfNeeded(document, options.getVersionPolicy());
        }
        
        return document;
    }

    public static BbModelDocument read(File file, ReadOptions options) {
        try {
            String content = Files.readString(file.toPath());
            return read(content, options);
        } catch (IOException e) {
            throw BbException.ioError("Failed to read file: " + file.getPath(), e);
        }
    }

    public static BbModelDocument read(Path path, ReadOptions options) {
        try {
            String content = Files.readString(path);
            return read(content, options);
        } catch (IOException e) {
            throw BbException.ioError("Failed to read file: " + path, e);
        }
    }

    public static BbModelDocument read(InputStream inputStream, ReadOptions options) {
        try {
            String content = new String(inputStream.readAllBytes());
            return read(content, options);
        } catch (IOException e) {
            throw BbException.ioError("Failed to read input stream", e);
        }
    }

    public static BbModelDocument read(Reader reader, ReadOptions options) {
        try {
            BufferedReader bufferedReader = new BufferedReader(reader);
            String content = bufferedReader.lines().collect(java.util.stream.Collectors.joining("\n"));
            return read(content, options);
        } catch (Exception e) {
            throw BbException.ioError("Failed to read reader", e);
        }
    }

    private static String decompressIfNeeded(String content, CompressionMode mode) {
        CompressionMode actualMode = mode;
        if (mode == CompressionMode.AUTO) {
            actualMode = BbModelFormatDetector.isCompressed(content) 
                ? CompressionMode.LZUTF8 
                : CompressionMode.JSON;
        }
        
        if (actualMode == CompressionMode.LZUTF8) {
            return LzUtf8Codec.decompress(content);
        }
        return content;
    }

    private static BbModelDocument parseDocument(JsonObject root, ReadOptions options) {
        BbModelDocument document = new BbModelDocument();
        
        if (root.has("meta")) {
            document.setMeta(parseMeta(root.getAsJsonObject("meta")));
        }
        
        if (root.has("resolution")) {
            document.setResolution(parseResolution(root.getAsJsonObject("resolution")));
        }
        
        if (root.has("textures")) {
            document.setTextures(parseTextures(root.getAsJsonArray("textures")));
        }
        
        if (root.has("elements")) {
            document.setElements(parseElements(root.getAsJsonArray("elements")));
        }
        
        if (root.has("groups")) {
            document.setGroups(parseGroups(root.getAsJsonArray("groups")));
        }
        
        if (root.has("outliner")) {
            document.setOutliner(parseOutliner(root.getAsJsonArray("outliner")));
        }
        
        if (root.has("animations")) {
            document.setAnimations(parseAnimations(root.getAsJsonArray("animations")));
        }
        
        if (root.has("animation_controllers")) {
            document.setAnimationControllers(parseAnimationControllers(root.getAsJsonArray("animation_controllers")));
        }
        
        if (root.has("display")) {
            document.setDisplay(parseDisplay(root.getAsJsonObject("display")));
        }
        
        if (root.has("reference_images")) {
            document.setReferenceImages(parseReferenceImages(root.getAsJsonArray("reference_images")));
        }
        
        if (root.has("editor_state")) {
            document.setEditorState(parseEditorState(root.getAsJsonObject("editor_state")));
        }
        
        if (root.has("history")) {
            document.setHistory(parseHistory(root.getAsJsonObject("history")));
        }
        
        if (root.has("export_options")) {
            document.setExportOptions(parseExportOptions(root.getAsJsonObject("export_options")));
        }
        
        if (root.has("collections")) {
            document.setCollections(parseCollections(root.getAsJsonArray("collections")));
        }
        
        if (root.has("texture_groups")) {
            document.setTextureGroups(parseTextureGroups(root.getAsJsonArray("texture_groups")));
        }
        
        if (options.isPreserveExtraFields()) {
            document.setRawData(extractExtraFields(root));
        }
        
        return document;
    }

    private static Meta parseMeta(JsonObject json) {
        Meta meta = new Meta();
        
        if (json.has("format_version")) {
            meta.setFormatVersion(json.get("format_version").getAsString());
        } else if (json.has("format")) {
            meta.setFormatVersion(json.get("format").getAsString());
        }
        
        if (json.has("model_format")) {
            meta.setModelFormat(json.get("model_format").getAsString());
        }
        
        if (json.has("project_id")) {
            meta.setProjectId(json.get("project_id").getAsString());
        }
        
        if (json.has("name")) {
            meta.setName(json.get("name").getAsString());
        }
        
        if (json.has("model_identifier")) {
            meta.setModelIdentifier(json.get("model_identifier").getAsString());
        }
        
        if (json.has("box_uv")) {
            meta.setBoxUv(json.get("box_uv").getAsBoolean());
        }
        
        if (json.has("visible_box")) {
            meta.setVisibleBox(json.get("visible_box").getAsBoolean());
        }
        
        if (json.has("shadow")) {
            meta.setShadow(json.get("shadow").getAsBoolean());
        }
        
        if (json.has("bone_rig")) {
            meta.setBoneRig(json.get("bone_rig").getAsBoolean());
        }
        
        if (json.has("mimic")) {
            meta.setMimic(json.get("mimic").getAsBoolean());
        }
        
        if (json.has("texture_width")) {
            meta.setTextureWidth(json.get("texture_width").getAsInt());
        }
        
        if (json.has("texture_height")) {
            meta.setTextureHeight(json.get("texture_height").getAsInt());
        }
        
        if (json.has("creation_time")) {
            meta.setCreationTime(json.get("creation_time").getAsLong());
        }
        
        if (json.has("modify_time")) {
            meta.setModifyTime(json.get("modify_time").getAsLong());
        }
        
        if (json.has("backup")) {
            meta.setBackup(json.get("backup").getAsBoolean());
        }
        
        if (json.has("added_models")) {
            meta.setAddedModels(json.get("added_models").getAsInt());
        }
        
        return meta;
    }

    private static Resolution parseResolution(JsonObject json) {
        Resolution resolution = new Resolution();
        
        if (json.has("width")) {
            resolution.setWidth(json.get("width").getAsInt());
        }
        
        if (json.has("height")) {
            resolution.setHeight(json.get("height").getAsInt());
        }
        
        return resolution;
    }

    private static List<Texture> parseTextures(JsonArray array) {
        List<Texture> textures = new ArrayList<>();
        
        for (JsonElement element : array) {
            JsonObject json = element.getAsJsonObject();
            Texture texture = new Texture();
            
            if (json.has("uuid")) {
                texture.setUuid(json.get("uuid").getAsString());
            }
            
            if (json.has("name")) {
                texture.setName(json.get("name").getAsString());
            }
            
            if (json.has("path")) {
                texture.setPath(json.get("path").getAsString());
            }
            
            if (json.has("relative_path")) {
                texture.setRelativePath(json.get("relative_path").getAsString());
            }
            
            if (json.has("source")) {
                texture.setSource(json.get("source").getAsString());
            }
            
            if (json.has("internal")) {
                texture.setInternal(json.get("internal").getAsBoolean());
            }
            
            if (json.has("render_sides")) {
                texture.setRenderSides(json.get("render_sides").getAsBoolean());
            }
            
            if (json.has("id")) {
                texture.setId(json.get("id").getAsInt());
            }
            
            if (json.has("width")) {
                texture.setWidth(json.get("width").getAsInt());
            }
            
            if (json.has("height")) {
                texture.setHeight(json.get("height").getAsInt());
            }
            
            if (json.has("uv_width")) {
                texture.setUvWidth(json.get("uv_width").getAsInt());
            }
            
            if (json.has("uv_height")) {
                texture.setUvHeight(json.get("uv_height").getAsInt());
            }
            
            if (json.has("particle_data")) {
                texture.setParticleData(json.get("particle_data").getAsString());
            }
            
            if (json.has("folder")) {
                texture.setFolder(json.get("folder").getAsString());
            }
            
            if (json.has("pinned")) {
                texture.setPinned(json.get("pinned").getAsBoolean());
            }
            
            textures.add(texture);
        }
        
        return textures;
    }

    private static List<Element> parseElements(JsonArray array) {
        List<Element> elements = new ArrayList<>();
        
        for (JsonElement element : array) {
            JsonObject json = element.getAsJsonObject();
            Element elem = parseElement(json);
            elements.add(elem);
        }
        
        return elements;
    }

    private static Element parseElement(JsonObject json) {
        String type = Element.CUBE;
        if (json.has("type")) {
            type = json.get("type").getAsString();
        }
        
        Element elem;
        if (Element.MESH.equals(type)) {
            elem = new MeshElement();
        } else {
            elem = new CubeElement();
        }
        
        elem.setType(type);
        
        if (json.has("uuid")) {
            elem.setUuid(json.get("uuid").getAsString());
        }
        
        if (json.has("name")) {
            elem.setName(json.get("name").getAsString());
        }
        
        if (json.has("from") && json.get("from").isJsonArray()) {
            elem.setFrom(parseDoubleArray(json.getAsJsonArray("from")));
        }
        
        if (json.has("to") && json.get("to").isJsonArray()) {
            elem.setTo(parseDoubleArray(json.getAsJsonArray("to")));
        }
        
        if (json.has("rotation") && json.get("rotation").isJsonArray()) {
            elem.setRotation(parseIntArray(json.getAsJsonArray("rotation")));
        }
        
        if (json.has("translation") && json.get("translation").isJsonArray()) {
            elem.setTranslation(parseDoubleArray(json.getAsJsonArray("translation")));
        }
        
        if (json.has("scale") && json.get("scale").isJsonArray()) {
            elem.setScale(parseDoubleArray(json.getAsJsonArray("scale")));
        }
        
        if (json.has("shade")) {
            elem.setShade(json.get("shade").getAsBoolean());
        }
        
        if (json.has("mirror_uv")) {
            elem.setMirrorUv(json.get("mirror_uv").getAsBoolean());
        }
        
        if (json.has("box_uv")) {
            elem.setBoxUv(json.get("box_uv").getAsBoolean());
        }
        
        if (json.has("faces") && json.get("faces").isJsonObject()) {
            elem.setFaces(parseFaces(json.getAsJsonObject("faces")));
        }
        
        return elem;
    }

    private static Map<String, Face> parseFaces(JsonObject json) {
        Map<String, Face> faces = new HashMap<>();
        
        for (String key : json.keySet()) {
            JsonObject faceJson = json.getAsJsonObject(key);
            Face face = new Face(key);
            
            if (faceJson.has("uv") && faceJson.get("uv").isJsonArray()) {
                Double[] arr = parseDoubleArray(faceJson.getAsJsonArray("uv"));
                if (arr != null && arr.length >= 4) {
                    face.setUv(new Uv(arr[0], arr[1], arr[2], arr[3]));
                }
            }
            
            if (faceJson.has("texture")) {
                JsonElement texture = faceJson.get("texture");
                if (texture.isJsonPrimitive()) {
                    face.setTexture(texture.getAsString());
                } else if (texture.isJsonNull()) {
                    face.setTexture(null);
                }
            }
            
            if (faceJson.has("cullface")) {
                face.setCullface(faceJson.get("cullface").getAsBoolean());
            }
            
            if (faceJson.has("tintindex")) {
                face.setTintindex(faceJson.get("tintindex").getAsInt());
            }
            
            if (faceJson.has("mirror_uv")) {
                face.setMirrorUv(faceJson.get("mirror_uv").getAsBoolean());
            }
            
            faces.put(key, face);
        }
        
        return faces;
    }

    private static Double[] parseDoubleArray(JsonArray array) {
        Double[] result = new Double[array.size()];
        for (int i = 0; i < array.size(); i++) {
            JsonElement element = array.get(i);
            if (element.isJsonPrimitive()) {
                result[i] = element.getAsDouble();
            }
        }
        return result;
    }

    private static Integer[] parseIntArray(JsonArray array) {
        Integer[] result = new Integer[array.size()];
        for (int i = 0; i < array.size(); i++) {
            JsonElement element = array.get(i);
            if (element.isJsonPrimitive()) {
                result[i] = element.getAsInt();
            }
        }
        return result;
    }

    private static List<BbModelDocument.Group> parseGroups(JsonArray array) {
        List<BbModelDocument.Group> groups = new ArrayList<>();
        
        for (JsonElement element : array) {
            JsonObject json = element.getAsJsonObject();
            BbModelDocument.Group group = new BbModelDocument.Group();
            
            if (json.has("uuid")) {
                group.setUuid(json.get("uuid").getAsString());
            }
            
            if (json.has("name")) {
                group.setName(json.get("name").getAsString());
            }
            
            if (json.has("rotation") && json.get("rotation").isJsonArray()) {
                group.setRotation(parseIntArray(json.getAsJsonArray("rotation")));
            }
            
            if (json.has("mirror")) {
                group.setMirror(json.get("mirror").getAsBoolean());
            }
            
            if (json.has("stretch") && json.get("stretch").isJsonArray()) {
                group.setStretch(parseIntArray(json.getAsJsonArray("stretch")));
            }
            
            if (json.has("box_size")) {
                group.setBoxSize(json.get("box_size").getAsInt());
            }
            
            if (json.has("export")) {
                group.setExport(json.get("export").getAsBoolean());
            }
            
            groups.add(group);
        }
        
        return groups;
    }

    private static List<OutlinerNode> parseOutliner(JsonArray array) {
        List<OutlinerNode> nodes = new ArrayList<>();
        
        for (JsonElement element : array) {
            OutlinerNode node = parseOutlinerNode(element);
            if (node != null) {
                nodes.add(node);
            }
        }
        
        return nodes;
    }

    private static OutlinerNode parseOutlinerNode(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return new OutlinerElementRefNode(element.getAsString());
        }
        
        if (element.isJsonObject()) {
            JsonObject json = element.getAsJsonObject();
            OutlinerGroupNode group = new OutlinerGroupNode();
            
            if (json.has("uuid")) {
                group.setUuid(json.get("uuid").getAsString());
            }
            
            if (json.has("name")) {
                group.setName(json.get("name").getAsString());
            }
            
            if (json.has("type")) {
                group.setType(json.get("type").getAsString());
            }
            
            if (json.has("rotation") && json.get("rotation").isJsonArray()) {
                group.setRotation(parseIntArray(json.getAsJsonArray("rotation")));
            }
            
            if (json.has("translation") && json.get("translation").isJsonArray()) {
                group.setTranslation(parseDoubleArray(json.getAsJsonArray("translation")));
            }
            
            if (json.has("scale") && json.get("scale").isJsonArray()) {
                group.setScale(parseDoubleArray(json.getAsJsonArray("scale")));
            }
            
            if (json.has("mirror")) {
                group.setMirror(json.get("mirror").getAsBoolean());
            }
            
            if (json.has("stretch") && json.get("stretch").isJsonArray()) {
                group.setStretch(parseIntArray(json.getAsJsonArray("stretch")));
            }
            
            if (json.has("export")) {
                group.setExport(json.get("export").getAsBoolean());
            }
            
            if (json.has("children") && json.get("children").isJsonArray()) {
                JsonArray children = json.getAsJsonArray("children");
                for (JsonElement child : children) {
                    OutlinerNode childNode = parseOutlinerNode(child);
                    if (childNode != null) {
                        group.addChild(childNode);
                    }
                }
            }
            
            return group;
        }
        
        return null;
    }

    private static List<Animation> parseAnimations(JsonArray array) {
        List<Animation> animations = new ArrayList<>();
        
        for (JsonElement element : array) {
            JsonObject json = element.getAsJsonObject();
            Animation animation = new Animation();
            
            if (json.has("uuid")) {
                animation.setUuid(json.get("uuid").getAsString());
            }
            
            if (json.has("name")) {
                animation.setName(json.get("name").getAsString());
            }
            
            if (json.has("loop")) {
                JsonElement loopEl = json.get("loop");
                if (loopEl.isJsonPrimitive() && loopEl.getAsJsonPrimitive().isBoolean()) {
                    animation.setLoop(loopEl.getAsBoolean() ? 1.0 : 0.0);
                } else {
                    animation.setLoop(loopEl.getAsDouble());
                }
            }
            
            if (json.has("start_time")) {
                animation.setStartTime(json.get("start_time").getAsDouble());
            }
            
            if (json.has("end_time")) {
                animation.setEndTime(json.get("end_time").getAsDouble());
            }
            
            if (json.has("length")) {
                animation.setLength(json.get("length").getAsDouble());
            }
            
            if (json.has("override")) {
                animation.setOverride(json.get("override").getAsBoolean());
            }
            
            if (json.has("anim_time_update")) {
                animation.setAnimTimeUpdate(json.get("anim_time_update").getAsBoolean());
            }
            
            if (json.has("special")) {
                animation.setSpecial(json.get("special").getAsBoolean());
            }
            
            if (json.has("animators") && json.get("animators").isJsonObject()) {
                animation.setAnimators(parseAnimators(json.getAsJsonObject("animators")));
            }
            
            animations.add(animation);
        }
        
        return animations;
    }

    private static Map<String, Animator> parseAnimators(JsonObject json) {
        Map<String, Animator> animators = new HashMap<>();
        
        for (String key : json.keySet()) {
            JsonObject animatorJson = json.getAsJsonObject(key);
            Animator animator = new Animator(key);
            
            if (animatorJson.has("name")) {
                animator.setName(animatorJson.get("name").getAsString());
            }
            
            if (animatorJson.has("type")) {
                animator.setType(animatorJson.get("type").getAsString());
            }
            
            if (animatorJson.has("keyframes") && animatorJson.get("keyframes").isJsonArray()) {
                animator.setKeyframes(parseKeyframes(animatorJson.getAsJsonArray("keyframes")));
            }
            
            animators.put(key, animator);
        }
        
        return animators;
    }

    private static List<Keyframe> parseKeyframes(JsonArray array) {
        List<Keyframe> keyframes = new ArrayList<>();
        
        for (JsonElement element : array) {
            JsonObject json = element.getAsJsonObject();
            Keyframe keyframe = new Keyframe();
            
            if (json.has("time")) {
                keyframe.setTime(json.get("time").getAsDouble());
            }
            
            if (json.has("channel")) {
                keyframe.setChannel(json.get("channel").getAsString());
            }
            
            if (json.has("interpolation")) {
                keyframe.setInterpolation(json.get("interpolation").getAsString());
            }
            
            if (json.has("data_points") && json.get("data_points").isJsonArray()) {
                keyframe.setDataPoints(parseDataPoints(json.getAsJsonArray("data_points")));
            }
            
            if (json.has("bezier_left_value") && json.get("bezier_left_value").isJsonArray()) {
                keyframe.setBezierLeftValue(parseDoubleArray(json.getAsJsonArray("bezier_left_value")));
            }
            
            if (json.has("bezier_right_value") && json.get("bezier_right_value").isJsonArray()) {
                keyframe.setBezierRightValue(parseDoubleArray(json.getAsJsonArray("bezier_right_value")));
            }
            
            keyframes.add(keyframe);
        }
        
        return keyframes;
    }

    private static List<DataPoint> parseDataPoints(JsonArray array) {
        List<DataPoint> dataPoints = new ArrayList<>();
        
        for (JsonElement element : array) {
            JsonObject json = element.getAsJsonObject();
            DataPoint dataPoint = new DataPoint();
            
            if (json.has("x")) {
                JsonElement x = json.get("x");
                dataPoint.setX(x.isJsonNull() ? null : x.getAsString());
            }
            
            if (json.has("y")) {
                JsonElement y = json.get("y");
                dataPoint.setY(y.isJsonNull() ? null : y.getAsString());
            }
            
            if (json.has("z")) {
                JsonElement z = json.get("z");
                dataPoint.setZ(z.isJsonNull() ? null : z.getAsString());
            }
            
            if (json.has("w")) {
                JsonElement w = json.get("w");
                dataPoint.setW(w.isJsonNull() ? null : w.getAsString());
            }
            
            dataPoints.add(dataPoint);
        }
        
        return dataPoints;
    }

    private static List<BbModelDocument.AnimationController> parseAnimationControllers(JsonArray array) {
        List<BbModelDocument.AnimationController> controllers = new ArrayList<>();
        
        for (JsonElement element : array) {
            JsonObject json = element.getAsJsonObject();
            BbModelDocument.AnimationController controller = new BbModelDocument.AnimationController();
            
            if (json.has("uuid")) {
                controller.setUuid(json.get("uuid").getAsString());
            }
            
            if (json.has("name")) {
                controller.setName(json.get("name").getAsString());
            }
            
            controllers.add(controller);
        }
        
        return controllers;
    }

    private static Display parseDisplay(JsonObject json) {
        Display display = new Display();
        
        if (json.isJsonObject()) {
            Map<String, Display.DisplaySlot> slots = new HashMap<>();
            
            for (String key : json.keySet()) {
                JsonObject slotJson = json.getAsJsonObject(key);
                Display.DisplaySlot slot = new Display.DisplaySlot();
                
                if (slotJson.has("rotation") && slotJson.get("rotation").isJsonArray()) {
                    slot.setRotation(parseDoubleArray(slotJson.getAsJsonArray("rotation")));
                }
                
                if (slotJson.has("translation") && slotJson.get("translation").isJsonArray()) {
                    slot.setTranslation(parseDoubleArray(slotJson.getAsJsonArray("translation")));
                }
                
                if (slotJson.has("scale") && slotJson.get("scale").isJsonArray()) {
                    slot.setScale(parseDoubleArray(slotJson.getAsJsonArray("scale")));
                }
                
                slots.put(key, slot);
            }
            
            display.setSlots(slots);
        }
        
        return display;
    }

    private static List<BbModelDocument.ReferenceImage> parseReferenceImages(JsonArray array) {
        List<BbModelDocument.ReferenceImage> images = new ArrayList<>();
        
        for (JsonElement element : array) {
            JsonObject json = element.getAsJsonObject();
            BbModelDocument.ReferenceImage image = new BbModelDocument.ReferenceImage();
            
            if (json.has("uuid")) {
                image.setUuid(json.get("uuid").getAsString());
            }
            
            if (json.has("name")) {
                image.setName(json.get("name").getAsString());
            }
            
            if (json.has("position") && json.get("position").isJsonArray()) {
                image.setPosition(parseDoubleArray(json.getAsJsonArray("position")));
            }
            
            if (json.has("size") && json.get("size").isJsonArray()) {
                image.setSize(parseDoubleArray(json.getAsJsonArray("size")));
            }
            
            if (json.has("source")) {
                image.setSource(json.get("source").getAsString());
            }
            
            if (json.has("is_blueprint")) {
                image.setIsBlueprint(json.get("is_blueprint").getAsBoolean());
            }
            
            if (json.has("layer")) {
                image.setLayer(json.get("layer").getAsString());
            }
            
            images.add(image);
        }
        
        return images;
    }

    private static EditorState parseEditorState(JsonObject json) {
        EditorState state = new EditorState();
        
        if (json.has("save_path")) {
            state.setSavePath(json.get("save_path").getAsString());
        }
        
        if (json.has("export_path")) {
            state.setExportPath(json.get("export_path").getAsString());
        }
        
        if (json.has("saved")) {
            state.setSaved(json.get("saved").getAsBoolean());
        }
        
        if (json.has("added_models")) {
            state.setAddedModels(json.get("added_models").getAsInt());
        }
        
        if (json.has("mode")) {
            state.setMode(json.get("mode").getAsString());
        }
        
        if (json.has("tool")) {
            state.setTool(json.get("tool").getAsString());
        }
        
        if (json.has("display_uv")) {
            state.setDisplayUv(json.get("display_uv").getAsString());
        }
        
        if (json.has("exploded_view")) {
            state.setExplodedView(json.get("exploded_view").getAsBoolean());
        }
        
        return state;
    }

    private static History parseHistory(JsonObject json) {
        History history = new History();
        
        if (json.has("history") && json.get("history").isJsonArray()) {
            List<History.HistoryEntry> entries = new ArrayList<>();
            JsonArray historyArray = json.getAsJsonArray("history");
            
            for (JsonElement element : historyArray) {
                JsonObject entryJson = element.getAsJsonObject();
                History.HistoryEntry entry = new History.HistoryEntry();
                
                if (entryJson.has("action")) {
                    entry.setAction(entryJson.get("action").getAsString());
                }
                
                if (entryJson.has("time")) {
                    entry.setTime(entryJson.get("time").getAsLong());
                }
                
                entries.add(entry);
            }
            
            history.setHistory(entries);
        }
        
        if (json.has("history_index")) {
            history.setHistoryIndex(json.get("history_index").getAsInt());
        }
        
        return history;
    }

    private static BbModelDocument.ExportOptions parseExportOptions(JsonObject json) {
        BbModelDocument.ExportOptions options = new BbModelDocument.ExportOptions();
        
        for (String key : json.keySet()) {
            Map<String, Object> codecOptions = new HashMap<>();
            JsonObject codecJson = json.getAsJsonObject(key);
            
            for (String optionKey : codecJson.keySet()) {
                codecOptions.put(optionKey, codecJson.get(optionKey));
            }
            
            options.putOptionsFor(key, codecOptions);
        }
        
        return options;
    }

    private static List<BbModelDocument.Collection> parseCollections(JsonArray array) {
        List<BbModelDocument.Collection> collections = new ArrayList<>();
        
        for (JsonElement element : array) {
            JsonObject json = element.getAsJsonObject();
            BbModelDocument.Collection collection = new BbModelDocument.Collection();
            
            if (json.has("uuid")) {
                collection.setUuid(json.get("uuid").getAsString());
            }
            
            if (json.has("name")) {
                collection.setName(json.get("name").getAsString());
            }
            
            if (json.has("order")) {
                collection.setOrder(json.get("order").getAsInt());
            }
            
            if (json.has("color")) {
                collection.setColor(json.get("color").getAsString());
            }
            
            if (json.has("locked")) {
                collection.setLocked(json.get("locked").getAsBoolean());
            }
            
            if (json.has("hidden")) {
                collection.setHidden(json.get("hidden").getAsBoolean());
            }
            
            collections.add(collection);
        }
        
        return collections;
    }

    private static List<TextureGroup> parseTextureGroups(JsonArray array) {
        List<TextureGroup> groups = new ArrayList<>();
        
        for (JsonElement element : array) {
            JsonObject json = element.getAsJsonObject();
            TextureGroup group = new TextureGroup();
            
            if (json.has("uuid")) {
                group.setUuid(json.get("uuid").getAsString());
            }
            
            if (json.has("name")) {
                group.setName(json.get("name").getAsString());
            }
            
            if (json.has("order")) {
                group.setOrder(json.get("order").getAsInt());
            }
            
            if (json.has("folder")) {
                group.setFolder(json.get("folder").getAsString());
            }
            
            groups.add(group);
        }
        
        return groups;
    }

    private static Map<String, Object> extractExtraFields(JsonObject root) {
        Map<String, Object> extra = new HashMap<>();
        
        String[] knownFields = {
            "meta", "resolution", "textures", "elements", "groups", "outliner",
            "animations", "animation_controllers", "display", "reference_images",
            "editor_state", "history", "export_options", "collections", "texture_groups"
        };
        
        for (String key : root.keySet()) {
            boolean isKnown = false;
            for (String known : knownFields) {
                if (known.equals(key)) {
                    isKnown = true;
                    break;
                }
            }
            
            if (!isKnown) {
                extra.put(key, root.get(key));
            }
        }
        
        return extra.isEmpty() ? null : extra;
    }
}
