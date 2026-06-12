package com.danrus.bb4j.ext;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.animation.Animation;
import com.danrus.bb4j.model.animation.Animator;
import com.danrus.bb4j.model.animation.DataPoint;
import com.danrus.bb4j.model.animation.Keyframe;
import com.danrus.bb4j.model.geometry.Element;
import com.danrus.bb4j.model.geometry.Face;
import com.danrus.bb4j.model.outliner.OutlinerNode;
import com.danrus.bb4j.model.project.Display;
import com.danrus.bb4j.model.project.History;
import com.danrus.bb4j.model.texture.Texture;
import com.danrus.bb4j.model.texture.TextureGroup;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Applies a {@link ParseExtensions} registry across a whole
 * {@link BbModelDocument}, translating selected unrecognized ("extra") fields
 * between their raw {@link JsonElement} form and a codec-defined typed form.
 *
 * <p>This runs as a single pass <em>outside</em> the field-by-field reader and
 * writer, so those hot paths stay untouched and symmetric:
 * <ul>
 *   <li>{@link #decode} runs after a document is read — for every registered
 *       {@code (scope, key)} whose value is still a raw {@link JsonElement}, the
 *       value is replaced by {@link RawFieldCodec#decode}'s typed result.</li>
 *   <li>{@link #encode} runs just before a document is written — for every
 *       registered {@code (scope, key)} whose value is not a {@link JsonElement},
 *       the value is replaced by {@link RawFieldCodec#encode}'s JSON so the
 *       ordinary writer emits it. It returns a {@link Restore} that puts the
 *       typed values back, so writing does not permanently mutate the caller's
 *       document.</li>
 * </ul>
 *
 * <p>The traversal mirrors the set of objects the reader captures {@code extra}
 * fields on; keep it in sync with {@link RawScope} as the model grows.
 */
public final class ExtensionProcessor {

    private ExtensionProcessor() {}

    /** Replaces registered raw fields with their decoded typed values in place. */
    public static void decode(BbModelDocument doc, ParseExtensions ext, Gson gson) {
        if (doc == null || ext == null || ext.isEmpty()) {
            return;
        }
        walk(doc, (scope, extra) -> applyDecode(scope, extra, ext, gson));
    }

    /**
     * Replaces registered typed fields with their encoded JSON so the writer can
     * emit them, returning a handle that reverts the document afterwards.
     */
    public static Restore encode(BbModelDocument doc, ParseExtensions ext, Gson gson) {
        Restore restore = new Restore();
        if (doc == null || ext == null || ext.isEmpty()) {
            return restore;
        }
        walk(doc, (scope, extra) -> applyEncode(scope, extra, ext, gson, restore));
        return restore;
    }

    private static void applyDecode(RawScope scope, Map<String, Object> extra,
                                    ParseExtensions ext, Gson gson) {
        for (Map.Entry<String, Object> entry : extra.entrySet()) {
            Object value = entry.getValue();
            if (!(value instanceof JsonElement)) {
                continue; // already typed (e.g. decoded twice / built in memory)
            }
            RawFieldCodec<?> codec = ext.codecFor(scope, entry.getKey());
            if (codec != null) {
                CodecContext ctx = new CodecContext(scope, entry.getKey(), gson);
                entry.setValue(codec.decode((JsonElement) value, ctx));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void applyEncode(RawScope scope, Map<String, Object> extra,
                                    ParseExtensions ext, Gson gson, Restore restore) {
        for (Map.Entry<String, Object> entry : extra.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof JsonElement) {
                continue; // already raw JSON; the writer emits it verbatim
            }
            RawFieldCodec<Object> codec = (RawFieldCodec<Object>) ext.codecFor(scope, entry.getKey());
            if (codec != null) {
                CodecContext ctx = new CodecContext(scope, entry.getKey(), gson);
                JsonElement encoded = codec.encode(value, ctx);
                restore.record(extra, entry.getKey(), value);
                entry.setValue(encoded);
            }
        }
    }

    /** Visits every {@code (scope, extra-map)} pair present in the document. */
    private static void walk(BbModelDocument doc, BiConsumer<RawScope, Map<String, Object>> op) {
        visit(RawScope.ROOT, doc.getRawData(), op);

        if (doc.getMeta() != null) {
            visit(RawScope.META, doc.getMeta().getExtra(), op);
        }
        if (doc.getResolution() != null) {
            visit(RawScope.RESOLUTION, doc.getResolution().getExtra(), op);
        }
        if (doc.getTextures() != null) {
            for (Texture t : doc.getTextures()) {
                visit(RawScope.TEXTURE, t.getExtra(), op);
            }
        }
        if (doc.getElements() != null) {
            for (Element e : doc.getElements()) {
                visit(RawScope.ELEMENT, e.getExtra(), op);
                if (e.getFaces() != null) {
                    for (Face f : e.getFaces().values()) {
                        visit(RawScope.FACE, f.getExtra(), op);
                    }
                }
            }
        }
        if (doc.getGroups() != null) {
            for (BbModelDocument.Group g : doc.getGroups()) {
                visit(RawScope.GROUP, g.getExtra(), op);
            }
        }
        if (doc.getOutliner() != null) {
            for (OutlinerNode node : doc.getOutliner()) {
                walkOutliner(node, op);
            }
        }
        if (doc.getAnimations() != null) {
            for (Animation a : doc.getAnimations()) {
                visit(RawScope.ANIMATION, a.getExtra(), op);
                if (a.getAnimators() != null) {
                    for (Animator an : a.getAnimators().values()) {
                        visit(RawScope.ANIMATOR, an.getExtra(), op);
                        if (an.getKeyframes() != null) {
                            for (Keyframe kf : an.getKeyframes()) {
                                visit(RawScope.KEYFRAME, kf.getExtra(), op);
                                if (kf.getDataPoints() != null) {
                                    for (DataPoint dp : kf.getDataPoints()) {
                                        visit(RawScope.DATA_POINT, dp.getExtra(), op);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (doc.getAnimationControllers() != null) {
            for (BbModelDocument.AnimationController c : doc.getAnimationControllers()) {
                visit(RawScope.ANIMATION_CONTROLLER, c.getExtra(), op);
            }
        }
        if (doc.getDisplay() != null && doc.getDisplay().getSlots() != null) {
            for (Display.DisplaySlot slot : doc.getDisplay().getSlots().values()) {
                visit(RawScope.DISPLAY_SLOT, slot.getExtra(), op);
            }
        }
        if (doc.getReferenceImages() != null) {
            for (BbModelDocument.ReferenceImage img : doc.getReferenceImages()) {
                visit(RawScope.REFERENCE_IMAGE, img.getExtra(), op);
            }
        }
        if (doc.getEditorState() != null) {
            visit(RawScope.EDITOR_STATE, doc.getEditorState().getExtra(), op);
        }
        if (doc.getHistory() != null) {
            History history = doc.getHistory();
            visit(RawScope.HISTORY, history.getExtra(), op);
            if (history.getHistory() != null) {
                for (History.HistoryEntry entry : history.getHistory()) {
                    visit(RawScope.HISTORY_ENTRY, entry.getExtra(), op);
                }
            }
        }
        if (doc.getCollections() != null) {
            for (BbModelDocument.Collection c : doc.getCollections()) {
                visit(RawScope.COLLECTION, c.getExtra(), op);
            }
        }
        if (doc.getTextureGroups() != null) {
            for (TextureGroup g : doc.getTextureGroups()) {
                visit(RawScope.TEXTURE_GROUP, g.getExtra(), op);
            }
        }
    }

    private static void walkOutliner(OutlinerNode node, BiConsumer<RawScope, Map<String, Object>> op) {
        if (node == null) {
            return;
        }
        visit(RawScope.OUTLINER_GROUP, node.getExtra(), op);
        if (node.getChildren() != null) {
            for (OutlinerNode child : node.getChildren()) {
                walkOutliner(child, op);
            }
        }
    }

    private static void visit(RawScope scope, Map<String, Object> extra,
                              BiConsumer<RawScope, Map<String, Object>> op) {
        if (extra != null && !extra.isEmpty()) {
            op.accept(scope, extra);
        }
    }

    /**
     * Records typed values displaced by {@link #encode} so they can be restored
     * after writing, leaving the caller's document unchanged.
     */
    public static final class Restore {
        private final List<Runnable> actions = new ArrayList<>();

        private void record(Map<String, Object> extra, String key, Object original) {
            actions.add(() -> extra.put(key, original));
        }

        /** Reverts every field {@link #encode} replaced back to its typed value. */
        public void run() {
            for (Runnable action : actions) {
                action.run();
            }
        }
    }
}
