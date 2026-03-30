package com.danrus.bb4j.io;

import com.danrus.bb4j.api.CompressionMode;
import com.danrus.bb4j.api.WriteOptions;
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
import java.util.List;
import java.util.Map;

public class BbModelWriter {
    private static final JsonCodec JSON_CODEC = new JsonCodec();
    private static final String CURRENT_FORMAT_VERSION = "5.0";

    public static String write(BbModelDocument document, WriteOptions options) {
        JsonObject root = serializeDocument(document, options);
        
        String json = options.isPrettyPrint() 
            ? JSON_CODEC.toPrettyJson(root) 
            : JSON_CODEC.toJson(root);
        
        CompressionMode actualMode = options.getCompressionMode();
        if (actualMode == CompressionMode.AUTO) {
            actualMode = CompressionMode.JSON;
        }
        
        if (actualMode == CompressionMode.LZUTF8) {
            return LzUtf8Codec.compress(json);
        }
        
        return json;
    }

    public static void write(BbModelDocument document, File file, WriteOptions options) {
        try {
            String content = write(document, options);
            Files.writeString(file.toPath(), content);
        } catch (IOException e) {
            throw new com.danrus.bb4j.api.BbException("IO_ERROR", "Failed to write file: " + file.getPath(), e);
        }
    }

    public static void write(BbModelDocument document, Path path, WriteOptions options) {
        try {
            String content = write(document, options);
            Files.writeString(path, content);
        } catch (IOException e) {
            throw new com.danrus.bb4j.api.BbException("IO_ERROR", "Failed to write file: " + path, e);
        }
    }

    public static void write(BbModelDocument document, OutputStream outputStream, WriteOptions options) {
        try {
            String content = write(document, options);
            outputStream.write(content.getBytes());
        } catch (IOException e) {
            throw new com.danrus.bb4j.api.BbException("IO_ERROR", "Failed to write to output stream", e);
        }
    }

    public static void write(BbModelDocument document, Writer writer, WriteOptions options) {
        try {
            String content = write(document, options);
            writer.write(content);
        } catch (IOException e) {
            throw new com.danrus.bb4j.api.BbException("IO_ERROR", "Failed to write to writer", e);
        }
    }

    private static JsonObject serializeDocument(BbModelDocument document, WriteOptions options) {
        JsonObject root = new JsonObject();
        
        root.add("meta", serializeMeta(document.getMeta()));
        
        if (document.getResolution() != null) {
            root.add("resolution", serializeResolution(document.getResolution()));
        }
        
        if (document.getTextures() != null && !document.getTextures().isEmpty()) {
            root.add("textures", serializeTextures(document.getTextures()));
        }
        
        if (document.getElements() != null && !document.getElements().isEmpty()) {
            root.add("elements", serializeElements(document.getElements()));
        }
        
        if (document.getGroups() != null && !document.getGroups().isEmpty()) {
            root.add("groups", serializeGroups(document.getGroups()));
        }
        
        if (document.getOutliner() != null && !document.getOutliner().isEmpty()) {
            root.add("outliner", serializeOutliner(document.getOutliner()));
        }
        
        if (document.getAnimations() != null && !document.getAnimations().isEmpty()) {
            root.add("animations", serializeAnimations(document.getAnimations()));
        }
        
        if (document.getAnimationControllers() != null && !document.getAnimationControllers().isEmpty()) {
            root.add("animation_controllers", serializeAnimationControllers(document.getAnimationControllers()));
        }
        
        if (document.getDisplay() != null) {
            root.add("display", serializeDisplay(document.getDisplay()));
        }
        
        if (options.isIncludeEditorState() && document.getEditorState() != null) {
            root.add("editor_state", serializeEditorState(document.getEditorState()));
        }
        
        if (options.isIncludeHistory() && document.getHistory() != null) {
            root.add("history", serializeHistory(document.getHistory()));
        }
        
        if (document.getExportOptions() != null) {
            root.add("export_options", serializeExportOptions(document.getExportOptions()));
        }
        
        if (document.getCollections() != null && !document.getCollections().isEmpty()) {
            root.add("collections", serializeCollections(document.getCollections()));
        }
        
        if (document.getTextureGroups() != null && !document.getTextureGroups().isEmpty()) {
            root.add("texture_groups", serializeTextureGroups(document.getTextureGroups()));
        }
        
        return root;
    }

    private static JsonObject serializeMeta(Meta meta) {
        if (meta == null) {
            return new JsonObject();
        }
        
        JsonObject json = new JsonObject();
        json.addProperty("format_version", CURRENT_FORMAT_VERSION);
        
        if (meta.getModelFormat() != null) {
            json.addProperty("model_format", meta.getModelFormat().getValue());
        }
        
        if (meta.getProjectId() != null) {
            json.addProperty("project_id", meta.getProjectId());
        }
        
        if (meta.getName() != null) {
            json.addProperty("name", meta.getName());
        }
        
        if (meta.getModelIdentifier() != null) {
            json.addProperty("model_identifier", meta.getModelIdentifier());
        }
        
        if (meta.getBoxUv() != null) {
            json.addProperty("box_uv", meta.getBoxUv());
        }
        
        if (meta.getVisibleBox() != null) {
            json.addProperty("visible_box", meta.getVisibleBox());
        }
        
        if (meta.getShadow() != null) {
            json.addProperty("shadow", meta.getShadow());
        }
        
        if (meta.getBoneRig() != null) {
            json.addProperty("bone_rig", meta.getBoneRig());
        }
        
        if (meta.getTextureWidth() != null) {
            json.addProperty("texture_width", meta.getTextureWidth());
        }
        
        if (meta.getTextureHeight() != null) {
            json.addProperty("texture_height", meta.getTextureHeight());
        }
        
        if (meta.getCreationTime() != null) {
            json.addProperty("creation_time", meta.getCreationTime());
        }
        
        if (meta.getModifyTime() != null) {
            json.addProperty("modify_time", meta.getModifyTime());
        }
        
        if (meta.getBackup() != null) {
            json.addProperty("backup", meta.getBackup());
        }
        
        if (meta.getAddedModels() != null) {
            json.addProperty("added_models", meta.getAddedModels());
        }
        
        return json;
    }

    private static JsonObject serializeResolution(Resolution resolution) {
        JsonObject json = new JsonObject();
        json.addProperty("width", resolution.getWidth());
        json.addProperty("height", resolution.getHeight());
        return json;
    }

    private static JsonArray serializeTextures(List<Texture> textures) {
        JsonArray array = new JsonArray();
        
        for (Texture texture : textures) {
            JsonObject json = new JsonObject();
            
            if (texture.getUuid() != null) {
                json.addProperty("uuid", texture.getUuid());
            }
            
            if (texture.getName() != null) {
                json.addProperty("name", texture.getName());
            }
            
            if (texture.getPath() != null) {
                json.addProperty("path", texture.getPath());
            }
            
            if (texture.getRelativePath() != null) {
                json.addProperty("relative_path", texture.getRelativePath());
            }
            
            if (texture.getSource() != null) {
                json.addProperty("source", texture.getSource());
            }
            
            if (texture.getInternal() != null) {
                json.addProperty("internal", texture.getInternal());
            }
            
            if (texture.getRenderSides() != null) {
                json.addProperty("render_sides", texture.getRenderSides());
            }
            
            if (texture.getId() != null) {
                json.addProperty("id", texture.getId());
            }
            
            if (texture.getWidth() != null) {
                json.addProperty("width", texture.getWidth());
            }
            
            if (texture.getHeight() != null) {
                json.addProperty("height", texture.getHeight());
            }
            
            array.add(json);
        }
        
        return array;
    }

    private static JsonArray serializeElements(List<Element> elements) {
        JsonArray array = new JsonArray();
        
        for (Element element : elements) {
            JsonObject json = new JsonObject();
            
            if (element.getUuid() != null) {
                json.addProperty("uuid", element.getUuid());
            }
            
            if (element.getType() != null) {
                json.addProperty("type", element.getType());
            }
            
            if (element.getName() != null) {
                json.addProperty("name", element.getName());
            }
            
            if (element.getFrom() != null) {
                json.add("from", serializeDoubleArray(element.getFrom()));
            }
            
            if (element.getTo() != null) {
                json.add("to", serializeDoubleArray(element.getTo()));
            }

            if (element.getOrigin() != null) {
                json.add("origin", serializeDoubleArray(element.getOrigin()));
            }

            if (element.getInflate() != null) {
                json.addProperty("inflate", element.getInflate());
            }
            
            if (element.getRotation() != null) {
                json.add("rotation", serializeDoubleArray(element.getRotation()));
            }
            
            if (element.getTranslation() != null) {
                json.add("translation", serializeDoubleArray(element.getTranslation()));
            }
            
            if (element.getScale() != null) {
                json.add("scale", serializeDoubleArray(element.getScale()));
            }
            
            if (element.getShade() != null) {
                json.addProperty("shade", element.getShade());
            }
            
            if (element.getMirrorUv() != null) {
                json.addProperty("mirror_uv", element.getMirrorUv());
            }
            
            if (element.getFaces() != null && !element.getFaces().isEmpty()) {
                json.add("faces", serializeFaces(element.getFaces()));
            }
            
            array.add(json);
        }
        
        return array;
    }

    private static JsonObject serializeFaces(Map<String, Face> faces) {
        JsonObject json = new JsonObject();
        
        for (Map.Entry<String, Face> entry : faces.entrySet()) {
            Face face = entry.getValue();
            JsonObject faceJson = new JsonObject();
            
            if (face.getUv() != null) {
                faceJson.add("uv", serializeDoubleArrayPrimitive(face.getUv().getUv()));
            }
            
            if (face.getTexture() != null) {
                faceJson.addProperty("texture", face.getTexture());
            }

            if (face.getRotation() != null) {
                faceJson.addProperty("rotation", face.getRotation());
            }
            
            if (face.getCullface() != null) {
                faceJson.addProperty("cullface", face.getCullface());
            }
            
            if (face.getTintindex() != null) {
                faceJson.addProperty("tintindex", face.getTintindex());
            }
            
            if (face.getMirrorUv() != null) {
                faceJson.addProperty("mirror_uv", face.getMirrorUv());
            }
            
            json.add(entry.getKey(), faceJson);
        }
        
        return json;
    }

    private static JsonArray serializeDoubleArray(Double[] array) {
        JsonArray json = new JsonArray();
        if (array != null) {
            for (Double value : array) {
                if (value != null) {
                    json.add(value);
                }
            }
        }
        return json;
    }

    private static JsonArray serializeDoubleArrayPrimitive(double[] array) {
        JsonArray json = new JsonArray();
        if (array != null) {
            for (double value : array) {
                json.add(value);
            }
        }
        return json;
    }

    private static JsonArray serializeIntArray(Integer[] array) {
        JsonArray json = new JsonArray();
        if (array != null) {
            for (Integer value : array) {
                if (value != null) {
                    json.add(value);
                }
            }
        }
        return json;
    }

    private static JsonArray serializeGroups(List<BbModelDocument.Group> groups) {
        JsonArray array = new JsonArray();
        
        for (BbModelDocument.Group group : groups) {
            JsonObject json = new JsonObject();
            
            if (group.getUuid() != null) {
                json.addProperty("uuid", group.getUuid());
            }
            
            if (group.getName() != null) {
                json.addProperty("name", group.getName());
            }
            
            if (group.getRotation() != null) {
                json.add("rotation", serializeDoubleArray(group.getRotation()));
            }
            
            if (group.getMirror() != null) {
                json.addProperty("mirror", group.getMirror());
            }
            
            if (group.getExport() != null) {
                json.addProperty("export", group.getExport());
            }
            
            array.add(json);
        }
        
        return array;
    }

    private static JsonArray serializeOutliner(List<OutlinerNode> nodes) {
        JsonArray array = new JsonArray();
        
        for (OutlinerNode node : nodes) {
            array.add(serializeOutlinerNode(node));
        }
        
        return array;
    }

    private static JsonElement serializeOutlinerNode(OutlinerNode node) {
        if (node instanceof OutlinerElementRefNode) {
            return new com.google.gson.JsonPrimitive(((OutlinerElementRefNode) node).getElementUuid());
        }
        
        JsonObject json = new JsonObject();
        
        if (node.getUuid() != null) {
            json.addProperty("uuid", node.getUuid());
        }
        
        if (node.getName() != null) {
            json.addProperty("name", node.getName());
        }
        
        if (node.getType() != null) {
            json.addProperty("type", node.getType());
        }
        
        if (node.getRotation() != null) {
            json.add("rotation", serializeDoubleArray(node.getRotation()));
        }
        
        if (node.getTranslation() != null) {
            json.add("translation", serializeDoubleArray(node.getTranslation()));
        }
        
        if (node.getScale() != null) {
            json.add("scale", serializeDoubleArray(node.getScale()));
        }
        
        if (node.getExport() != null) {
            json.addProperty("export", node.getExport());
        }
        
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            json.add("children", serializeOutliner(node.getChildren()));
        }
        
        return json;
    }

    private static JsonArray serializeAnimations(List<Animation> animations) {
        JsonArray array = new JsonArray();
        
        for (Animation animation : animations) {
            JsonObject json = new JsonObject();
            
            if (animation.getUuid() != null) {
                json.addProperty("uuid", animation.getUuid());
            }
            
            if (animation.getName() != null) {
                json.addProperty("name", animation.getName());
            }
            
            if (animation.getLoop() != null) {
                json.addProperty("loop", animation.getLoop());
            }
            
            if (animation.getStartTime() != null) {
                json.addProperty("start_time", animation.getStartTime());
            }
            
            if (animation.getEndTime() != null) {
                json.addProperty("end_time", animation.getEndTime());
            }
            
            if (animation.getLength() != null) {
                json.addProperty("length", animation.getLength());
            }
            
            if (animation.getOverride() != null) {
                json.addProperty("override", animation.getOverride());
            }
            
            if (animation.getAnimators() != null && !animation.getAnimators().isEmpty()) {
                json.add("animators", serializeAnimators(animation.getAnimators()));
            }
            
            array.add(json);
        }
        
        return array;
    }

    private static JsonObject serializeAnimators(Map<String, Animator> animators) {
        JsonObject json = new JsonObject();
        
        for (Map.Entry<String, Animator> entry : animators.entrySet()) {
            Animator animator = entry.getValue();
            JsonObject animatorJson = new JsonObject();
            
            if (animator.getName() != null) {
                animatorJson.addProperty("name", animator.getName());
            }
            
            if (animator.getType() != null) {
                animatorJson.addProperty("type", animator.getType());
            }
            
            if (animator.getKeyframes() != null) {
                animatorJson.add("keyframes", serializeKeyframes(animator.getKeyframes()));
            }
            
            json.add(entry.getKey(), animatorJson);
        }
        
        return json;
    }

    private static JsonArray serializeKeyframes(List<Keyframe> keyframes) {
        JsonArray array = new JsonArray();
        
        for (Keyframe keyframe : keyframes) {
            JsonObject json = new JsonObject();
            
            if (keyframe.getTime() != null) {
                json.addProperty("time", keyframe.getTime());
            }
            
            if (keyframe.getChannel() != null) {
                json.addProperty("channel", keyframe.getChannel());
            }
            
            if (keyframe.getInterpolation() != null) {
                json.addProperty("interpolation", keyframe.getInterpolation().getValue());
            }
            
            if (keyframe.getDataPoints() != null) {
                json.add("data_points", serializeDataPoints(keyframe.getDataPoints()));
            }
            
            if (keyframe.getBezierLeftValue() != null) {
                json.add("bezier_left_value", serializeDoubleArray(keyframe.getBezierLeftValue()));
            }
            
            if (keyframe.getBezierRightValue() != null) {
                json.add("bezier_right_value", serializeDoubleArray(keyframe.getBezierRightValue()));
            }
            
            if (keyframe.getBezierLeftTime() != null) {
                json.add("bezier_left_time", serializeDoubleArray(keyframe.getBezierLeftTime()));
            }
            
            if (keyframe.getBezierRightTime() != null) {
                json.add("bezier_right_time", serializeDoubleArray(keyframe.getBezierRightTime()));
            }
            
            array.add(json);
        }
        
        return array;
    }

    private static JsonArray serializeDataPoints(List<DataPoint> dataPoints) {
        JsonArray array = new JsonArray();
        
        for (DataPoint dp : dataPoints) {
            JsonObject json = new JsonObject();
            
            if (dp.getX() != null) {
                json.addProperty("x", dp.getX());
            }
            
            if (dp.getY() != null) {
                json.addProperty("y", dp.getY());
            }
            
            if (dp.getZ() != null) {
                json.addProperty("z", dp.getZ());
            }
            
            if (dp.getW() != null) {
                json.addProperty("w", dp.getW());
            }
            
            array.add(json);
        }
        
        return array;
    }

    private static JsonArray serializeAnimationControllers(List<BbModelDocument.AnimationController> controllers) {
        JsonArray array = new JsonArray();
        
        for (BbModelDocument.AnimationController controller : controllers) {
            JsonObject json = new JsonObject();
            
            if (controller.getUuid() != null) {
                json.addProperty("uuid", controller.getUuid());
            }
            
            if (controller.getName() != null) {
                json.addProperty("name", controller.getName());
            }
            
            array.add(json);
        }
        
        return array;
    }

    private static JsonObject serializeDisplay(Display display) {
        JsonObject json = new JsonObject();
        
        if (display.getSlots() != null) {
            for (Map.Entry<String, Display.DisplaySlot> entry : display.getSlots().entrySet()) {
                Display.DisplaySlot slot = entry.getValue();
                JsonObject slotJson = new JsonObject();
                
                if (slot.getRotation() != null) {
                    slotJson.add("rotation", serializeDoubleArray(slot.getRotation()));
                }
                
                if (slot.getTranslation() != null) {
                    slotJson.add("translation", serializeDoubleArray(slot.getTranslation()));
                }
                
                if (slot.getScale() != null) {
                    slotJson.add("scale", serializeDoubleArray(slot.getScale()));
                }
                
                json.add(entry.getKey(), slotJson);
            }
        }
        
        return json;
    }

    private static JsonObject serializeEditorState(EditorState state) {
        JsonObject json = new JsonObject();
        
        if (state.getSavePath() != null) {
            json.addProperty("save_path", state.getSavePath());
        }
        
        if (state.getExportPath() != null) {
            json.addProperty("export_path", state.getExportPath());
        }
        
        if (state.getSaved() != null) {
            json.addProperty("saved", state.getSaved());
        }
        
        if (state.getMode() != null) {
            json.addProperty("mode", state.getMode());
        }
        
        return json;
    }

    private static JsonObject serializeHistory(History history) {
        JsonObject json = new JsonObject();
        return json;
    }

    private static JsonObject serializeExportOptions(BbModelDocument.ExportOptions options) {
        JsonObject json = new JsonObject();
        return json;
    }

    private static JsonArray serializeCollections(List<BbModelDocument.Collection> collections) {
        JsonArray array = new JsonArray();
        return array;
    }

    private static JsonArray serializeTextureGroups(List<TextureGroup> groups) {
        JsonArray array = new JsonArray();
        
        for (TextureGroup group : groups) {
            JsonObject json = new JsonObject();
            
            if (group.getUuid() != null) {
                json.addProperty("uuid", group.getUuid());
            }
            
            if (group.getName() != null) {
                json.addProperty("name", group.getName());
            }
            
            if (group.getOrder() != null) {
                json.addProperty("order", group.getOrder());
            }
            
            if (group.getFolder() != null) {
                json.addProperty("folder", group.getFolder());
            }
            
            array.add(json);
        }
        
        return array;
    }
}
