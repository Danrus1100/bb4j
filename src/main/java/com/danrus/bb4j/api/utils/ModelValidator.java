package com.danrus.bb4j.api.utils;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.geometry.Element;
import com.danrus.bb4j.model.geometry.Face;
import com.danrus.bb4j.model.outliner.OutlinerElementRefNode;
import com.danrus.bb4j.model.texture.Texture;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Read-only consistency checks over a {@link BbModelDocument}. bb4j happily reads
 * structurally-valid but semantically-broken files (a model may reference a
 * texture or element that is not there); this surfaces those problems so callers
 * can decide how to react.
 *
 * <pre>{@code
 * List<ModelValidator.Issue> issues = ModelValidator.forDocument(doc).validate();
 * if (!issues.isEmpty()) issues.forEach(System.out::println);
 * }</pre>
 */
public final class ModelValidator {

    /** How serious an {@link Issue} is. */
    public enum Severity { ERROR, WARNING }

    /** A single validation finding. {@code uuid} points at the offending object, if any. */
    public record Issue(Severity severity, String code, String message, String uuid) {
        public Issue(Severity severity, String code, String message) {
            this(severity, code, message, null);
        }

        @Override
        public String toString() {
            return severity + " [" + code + "] " + message + (uuid != null ? " (" + uuid + ")" : "");
        }
    }

    public static final String DUPLICATE_ELEMENT_UUID = "DUPLICATE_ELEMENT_UUID";
    public static final String DUPLICATE_TEXTURE_UUID = "DUPLICATE_TEXTURE_UUID";
    public static final String DANGLING_OUTLINER_REF = "DANGLING_OUTLINER_REF";
    public static final String MISSING_FACE_TEXTURE = "MISSING_FACE_TEXTURE";
    public static final String NON_FINITE_COORDINATE = "NON_FINITE_COORDINATE";

    private final BbModelDocument document;

    private ModelValidator(BbModelDocument document) {
        this.document = document;
    }

    public static ModelValidator forDocument(BbModelDocument document) {
        return new ModelValidator(document);
    }

    /** Runs every check and returns all findings (empty if the model is clean). */
    public List<Issue> validate() {
        List<Issue> issues = new ArrayList<>();
        checkDuplicateElementUuids(issues);
        checkDuplicateTextureUuids(issues);
        checkDanglingOutlinerRefs(issues);
        checkFaceTextures(issues);
        checkCoordinates(issues);
        return issues;
    }

    /** Whether {@link #validate()} found no {@link Severity#ERROR} issues. */
    public boolean isValid() {
        return validate().stream().noneMatch(i -> i.severity() == Severity.ERROR);
    }

    private void checkDuplicateElementUuids(List<Issue> issues) {
        if (document.getElements() == null) {
            return;
        }
        Set<String> seen = new HashSet<>();
        Set<String> reported = new HashSet<>();
        for (Element e : document.getElements()) {
            String uuid = e.getUuid();
            if (uuid != null && !seen.add(uuid) && reported.add(uuid)) {
                issues.add(new Issue(Severity.ERROR, DUPLICATE_ELEMENT_UUID,
                        "Multiple elements share uuid " + uuid, uuid));
            }
        }
    }

    private void checkDuplicateTextureUuids(List<Issue> issues) {
        if (document.getTextures() == null) {
            return;
        }
        Set<String> seen = new HashSet<>();
        Set<String> reported = new HashSet<>();
        for (Texture t : document.getTextures()) {
            String uuid = t.getUuid();
            if (uuid != null && !seen.add(uuid) && reported.add(uuid)) {
                issues.add(new Issue(Severity.ERROR, DUPLICATE_TEXTURE_UUID,
                        "Multiple textures share uuid " + uuid, uuid));
            }
        }
    }

    private void checkDanglingOutlinerRefs(List<Issue> issues) {
        Set<String> elementUuids = new HashSet<>();
        if (document.getElements() != null) {
            for (Element e : document.getElements()) {
                if (e.getUuid() != null) {
                    elementUuids.add(e.getUuid());
                }
            }
        }
        for (OutlinerElementRefNode ref : OutlinerUtils.forDocument(document).getAllElementRefs()) {
            String uuid = ref.getElementUuid();
            if (uuid != null && !elementUuids.contains(uuid)) {
                issues.add(new Issue(Severity.ERROR, DANGLING_OUTLINER_REF,
                        "Outliner references element " + uuid + " which does not exist", uuid));
            }
        }
    }

    private void checkFaceTextures(List<Issue> issues) {
        if (document.getElements() == null) {
            return;
        }
        TextureUtils textures = TextureUtils.forDocument(document);
        for (Element e : document.getElements()) {
            if (e.getFaces() == null) {
                continue;
            }
            for (Map.Entry<String, Face> entry : e.getFaces().entrySet()) {
                String ref = entry.getValue().getTexture();
                if (ref != null && textures.getTextureByReference(ref) == null) {
                    issues.add(new Issue(Severity.WARNING, MISSING_FACE_TEXTURE,
                            "Face '" + entry.getKey() + "' references missing texture '" + ref + "'",
                            e.getUuid()));
                }
            }
        }
    }

    private void checkCoordinates(List<Issue> issues) {
        if (document.getElements() == null) {
            return;
        }
        for (Element e : document.getElements()) {
            checkVector(issues, e.getUuid(), "from", e.getFrom());
            checkVector(issues, e.getUuid(), "to", e.getTo());
            checkVector(issues, e.getUuid(), "origin", e.getOrigin());
        }
    }

    private void checkVector(List<Issue> issues, String uuid, String field, Double[] v) {
        if (v == null) {
            return;
        }
        for (Double d : v) {
            if (d != null && !Double.isFinite(d)) {
                issues.add(new Issue(Severity.ERROR, NON_FINITE_COORDINATE,
                        "Element has non-finite value in " + field, uuid));
                return;
            }
        }
    }
}
