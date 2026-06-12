package com.danrus.bb4j.io;

import com.danrus.bb4j.api.BbException;
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
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Serializes a {@link BbModelDocument} back into {@code .bbmodel} bytes.
 *
 * <p>The writer is the mirror image of {@link BbModelReader}: every field the
 * reader understands is emitted here, and every preserved {@code extra} /
 * {@link BbModelDocument#getRawData() rawData} entry is written back via
 * {@code writeExtra(...)}, so a read/write round-trip does not lose data. The
 * {@code meta.format_version} is always written as the current format version,
 * reflecting that reading upgrades older documents.
 *
 * <p>Output is pretty-printed JSON by default and may be LZ-UTF8 compressed
 * depending on {@link WriteOptions}. Editor state and undo history are excluded
 * unless explicitly enabled. Callers should use the
 * {@link com.danrus.bb4j.api.BbModel} facade rather than this class directly.
 */
public class BbModelWriter {
    private static final JsonCodec JSON_CODEC = new JsonCodec();
    private static final String CURRENT_FORMAT_VERSION = "5.0";

    public static String write(BbModelDocument document, WriteOptions options) {
        // Encode any registered typed "extra" fields back to JsonElement so the
        // ordinary serializer emits them; restore the typed values afterwards so
        // writing does not mutate the caller's document. No-op without extensions.
        com.danrus.bb4j.ext.ExtensionProcessor.Restore restore =
                com.danrus.bb4j.ext.ExtensionProcessor.encode(
                        document, options.getExtensions(), JSON_CODEC.gson());
        JsonObject root;
        try {
            root = serializeDocument(document, options);
        } finally {
            restore.run();
        }

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
            throw BbException.ioError("Failed to write file: " + file.getPath(), e);
        }
    }

    public static void write(BbModelDocument document, Path path, WriteOptions options) {
        try {
            String content = write(document, options);
            Files.writeString(path, content);
        } catch (IOException e) {
            throw BbException.ioError("Failed to write file: " + path, e);
        }
    }

    public static void write(BbModelDocument document, OutputStream outputStream, WriteOptions options) {
        try {
            String content = write(document, options);
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw BbException.ioError("Failed to write to output stream", e);
        }
    }

    public static void write(BbModelDocument document, Writer writer, WriteOptions options) {
        try {
            String content = write(document, options);
            writer.write(content);
        } catch (IOException e) {
            throw BbException.ioError("Failed to write to writer", e);
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

        if (document.getReferenceImages() != null && !document.getReferenceImages().isEmpty()) {
            root.add("reference_images", serializeReferenceImages(document.getReferenceImages()));
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

        // Re-emit any unrecognized top-level fields captured on read, so that a
        // read -> write round-trip does not silently drop unknown data.
        writeExtra(root, document.getRawData());

        return root;
    }

    /**
     * Writes back the "extra" map of fields that were captured on read but are
     * not modeled explicitly. Existing keys are never overwritten. Values stored
     * as {@link JsonElement} are emitted verbatim; any other value is converted
     * to a JSON tree first.
     */
    /**
     * Writes a face's {@code texture} reference using Blockbench's on-disk
     * convention: in a {@code .bbmodel} file the texture is the <em>integer
     * index</em> of the texture in the project's texture list, or JSON
     * {@code null} for an untextured face. The value is always emitted (matching
     * Blockbench, which writes {@code texture} for every face). A non-numeric
     * value (e.g. a UUID set programmatically) is written as a string.
     */
    private static void serializeTextureRef(JsonObject json, String texture) {
        if (texture == null) {
            json.add("texture", JsonNull.INSTANCE);
        } else if (texture.matches("\\d+")) {
            json.addProperty("texture", Integer.valueOf(texture));
        } else {
            json.addProperty("texture", texture);
        }
    }

    private static void writeExtra(JsonObject json, Map<String, Object> extra) {
        if (extra == null || extra.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : extra.entrySet()) {
            if (json.has(entry.getKey())) {
                continue;
            }
            Object value = entry.getValue();
            if (value instanceof JsonElement) {
                json.add(entry.getKey(), (JsonElement) value);
            } else {
                json.add(entry.getKey(), JSON_CODEC.toTree(value));
            }
        }
    }

    private static JsonObject serializeMeta(Meta meta) {
        if (meta == null) {
            return new JsonObject();
        }
        
        JsonObject json = new JsonObject();
        if (meta.getFormatVersion() != null) {
            json.addProperty("format_version", meta.getFormatVersion().getRaw());
        } else {
            json.addProperty("format_version", CURRENT_FORMAT_VERSION);
        }
        
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

        if (meta.getMimic() != null) {
            json.addProperty("mimic", meta.getMimic());
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

        writeExtra(json, meta.getExtra());

        return json;
    }

    private static JsonObject serializeResolution(Resolution resolution) {
        JsonObject json = new JsonObject();
        json.addProperty("width", resolution.getWidth());
        json.addProperty("height", resolution.getHeight());
        writeExtra(json, resolution.getExtra());
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

            if (texture.getUvWidth() != null) {
                json.addProperty("uv_width", texture.getUvWidth());
            }

            if (texture.getUvHeight() != null) {
                json.addProperty("uv_height", texture.getUvHeight());
            }

            if (texture.getParticleData() != null) {
                json.addProperty("particle_data", texture.getParticleData());
            }

            if (texture.getFolder() != null) {
                json.addProperty("folder", texture.getFolder());
            }

            if (texture.getPinned() != null) {
                json.addProperty("pinned", texture.getPinned());
            }

            writeExtra(json, texture.getExtra());

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

            if (element.getBoxUv() != null) {
                json.addProperty("box_uv", element.getBoxUv());
            }

            if (element instanceof MeshElement mesh) {
                if (mesh.getVertices() != null && !mesh.getVertices().isEmpty()) {
                    json.add("vertices", serializeMeshVertices(mesh.getVertices()));
                }
                if (mesh.getMeshFaces() != null && !mesh.getMeshFaces().isEmpty()) {
                    json.add("faces", serializeMeshFaces(mesh.getMeshFaces()));
                }
            } else if (element.getFaces() != null && !element.getFaces().isEmpty()) {
                json.add("faces", serializeFaces(element.getFaces()));
            }

            writeExtra(json, element.getExtra());

            array.add(json);
        }

        return array;
    }

    private static JsonObject serializeMeshVertices(Map<String, Double[]> vertices) {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, Double[]> entry : vertices.entrySet()) {
            json.add(entry.getKey(), serializeDoubleArray(entry.getValue()));
        }
        return json;
    }

    private static JsonObject serializeMeshFaces(List<MeshElement.MeshFaceData> faces) {
        JsonObject json = new JsonObject();
        for (MeshElement.MeshFaceData face : faces) {
            JsonObject faceJson = new JsonObject();

            if (face.getVertices() != null) {
                JsonArray vertices = new JsonArray();
                for (String vertexKey : face.getVertices()) {
                    vertices.add(vertexKey);
                }
                faceJson.add("vertices", vertices);
            }

            if (face.getUvByVertex() != null) {
                JsonObject uv = new JsonObject();
                for (Map.Entry<String, Double[]> entry : face.getUvByVertex().entrySet()) {
                    uv.add(entry.getKey(), serializeDoubleArray(entry.getValue()));
                }
                faceJson.add("uv", uv);
            }

            serializeTextureRef(faceJson, face.getTexture());

            json.add(face.getId(), faceJson);
        }
        return json;
    }

    private static JsonObject serializeFaces(Map<String, Face> faces) {
        JsonObject json = new JsonObject();
        
        for (Map.Entry<String, Face> entry : faces.entrySet()) {
            Face face = entry.getValue();
            JsonObject faceJson = new JsonObject();
            
            if (face.getUv() != null) {
                faceJson.add("uv", serializeDoubleArrayPrimitive(face.getUv().getUv()));
            }

            serializeTextureRef(faceJson, face.getTexture());

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

            writeExtra(faceJson, face.getExtra());

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
                } else {
                    json.add(JsonNull.INSTANCE);
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
                } else {
                    json.add(JsonNull.INSTANCE);
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

            if (group.getOrigin() != null) {
                json.add("origin", serializeDoubleArray(group.getOrigin()));
            }

            if (group.getMirror() != null) {
                json.addProperty("mirror", group.getMirror());
            }

            if (group.getStretch() != null) {
                json.add("stretch", serializeIntArray(group.getStretch()));
            }

            if (group.getBoxSize() != null) {
                json.addProperty("box_size", group.getBoxSize());
            }

            if (group.getExport() != null) {
                json.addProperty("export", group.getExport());
            }

            writeExtra(json, group.getExtra());

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

        if (node.getOrigin() != null) {
            json.add("origin", serializeDoubleArray(node.getOrigin()));
        }

        if (node.getTranslation() != null) {
            json.add("translation", serializeDoubleArray(node.getTranslation()));
        }

        if (node.getScale() != null) {
            json.add("scale", serializeDoubleArray(node.getScale()));
        }

        if (node instanceof OutlinerGroupNode group) {
            if (group.getMirror() != null) {
                json.addProperty("mirror", group.getMirror());
            }
            if (group.getStretch() != null) {
                json.add("stretch", serializeIntArray(group.getStretch()));
            }
            if (group.getBoxSize() != null) {
                json.addProperty("box_size", group.getBoxSize());
            }
        }

        if (node.getExport() != null) {
            json.addProperty("export", node.getExport());
        }

        writeExtra(json, node.getExtra());

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

            if (animation.getAnimTimeUpdate() != null) {
                json.addProperty("anim_time_update", animation.getAnimTimeUpdate());
            }

            if (animation.getSpecial() != null) {
                json.addProperty("special", animation.getSpecial());
            }

            if (animation.getPath() != null) {
                json.addProperty("path", animation.getPath());
            }

            if (animation.getAnimators() != null && !animation.getAnimators().isEmpty()) {
                json.add("animators", serializeAnimators(animation.getAnimators()));
            }

            writeExtra(json, animation.getExtra());

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

            writeExtra(animatorJson, animator.getExtra());

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

            writeExtra(json, keyframe.getExtra());

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

            writeExtra(json, dp.getExtra());

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

            writeExtra(json, controller.getExtra());

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

                writeExtra(slotJson, slot.getExtra());

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

        if (state.getTool() != null) {
            json.addProperty("tool", state.getTool());
        }

        if (state.getDisplayUv() != null) {
            json.addProperty("display_uv", state.getDisplayUv());
        }

        if (state.getExplodedView() != null) {
            json.addProperty("exploded_view", state.getExplodedView());
        }

        if (state.getAddedModels() != null) {
            json.addProperty("added_models", state.getAddedModels());
        }

        writeExtra(json, state.getExtra());

        return json;
    }

    private static JsonObject serializeHistory(History history) {
        JsonObject json = new JsonObject();

        if (history.getHistory() != null) {
            JsonArray entries = new JsonArray();
            for (History.HistoryEntry entry : history.getHistory()) {
                JsonObject entryJson = new JsonObject();
                if (entry.getAction() != null) {
                    entryJson.addProperty("action", entry.getAction());
                }
                if (entry.getTime() != null) {
                    entryJson.addProperty("time", entry.getTime());
                }
                writeExtra(entryJson, entry.getExtra());
                entries.add(entryJson);
            }
            json.add("history", entries);
        }

        if (history.getHistoryIndex() != null) {
            json.addProperty("history_index", history.getHistoryIndex());
        }

        writeExtra(json, history.getExtra());

        return json;
    }

    private static JsonObject serializeExportOptions(BbModelDocument.ExportOptions options) {
        JsonObject json = new JsonObject();

        if (options.getOptions() != null) {
            for (Map.Entry<String, Map<String, Object>> codecEntry : options.getOptions().entrySet()) {
                JsonObject codecJson = new JsonObject();
                for (Map.Entry<String, Object> optionEntry : codecEntry.getValue().entrySet()) {
                    Object value = optionEntry.getValue();
                    if (value instanceof JsonElement) {
                        codecJson.add(optionEntry.getKey(), (JsonElement) value);
                    } else {
                        codecJson.add(optionEntry.getKey(), JSON_CODEC.toTree(value));
                    }
                }
                json.add(codecEntry.getKey(), codecJson);
            }
        }

        return json;
    }

    private static JsonArray serializeCollections(List<BbModelDocument.Collection> collections) {
        JsonArray array = new JsonArray();

        for (BbModelDocument.Collection collection : collections) {
            JsonObject json = new JsonObject();

            if (collection.getUuid() != null) {
                json.addProperty("uuid", collection.getUuid());
            }

            if (collection.getName() != null) {
                json.addProperty("name", collection.getName());
            }

            if (collection.getOrder() != null) {
                json.addProperty("order", collection.getOrder());
            }

            if (collection.getColor() != null) {
                json.addProperty("color", collection.getColor());
            }

            if (collection.getLocked() != null) {
                json.addProperty("locked", collection.getLocked());
            }

            if (collection.getHidden() != null) {
                json.addProperty("hidden", collection.getHidden());
            }

            writeExtra(json, collection.getExtra());

            array.add(json);
        }

        return array;
    }

    private static JsonArray serializeReferenceImages(List<BbModelDocument.ReferenceImage> images) {
        JsonArray array = new JsonArray();

        for (BbModelDocument.ReferenceImage image : images) {
            JsonObject json = new JsonObject();

            if (image.getUuid() != null) {
                json.addProperty("uuid", image.getUuid());
            }

            if (image.getName() != null) {
                json.addProperty("name", image.getName());
            }

            if (image.getPosition() != null) {
                json.add("position", serializeDoubleArray(image.getPosition()));
            }

            if (image.getSize() != null) {
                json.add("size", serializeDoubleArray(image.getSize()));
            }

            if (image.getSource() != null) {
                json.addProperty("source", image.getSource());
            }

            if (image.getIsBlueprint() != null) {
                json.addProperty("is_blueprint", image.getIsBlueprint());
            }

            if (image.getLayer() != null) {
                json.addProperty("layer", image.getLayer());
            }

            writeExtra(json, image.getExtra());

            array.add(json);
        }

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

            if (group.getTextures() != null) {
                JsonArray textures = new JsonArray();
                for (String textureUuid : group.getTextures()) {
                    textures.add(textureUuid);
                }
                json.add("textures", textures);
            }

            writeExtra(json, group.getExtra());

            array.add(json);
        }

        return array;
    }
}
