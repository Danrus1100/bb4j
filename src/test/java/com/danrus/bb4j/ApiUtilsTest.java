package com.danrus.bb4j;

import com.danrus.bb4j.api.BbModel;
import com.danrus.bb4j.api.utils.ModelInfo;
import com.danrus.bb4j.api.utils.OutlinerUtils;
import com.danrus.bb4j.api.utils.TransformUtils;
import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.animation.Animation;
import com.danrus.bb4j.model.outliner.OutlinerGroupNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Light coverage of {@code com.danrus.bb4j.api.utils} — ModelInfo, OutlinerUtils,
 * and TransformUtils.
 */
class ApiUtilsTest {

    // -----------------------------------------------------------------------
    // Fixture loading
    // -----------------------------------------------------------------------

    private static BbModelDocument axeDoc;
    private static BbModelDocument geckolibDoc;
    private static BbModelDocument prettyDoc;

    @BeforeAll
    static void loadFixtures() throws IOException {
        axeDoc = loadFixture("java_block_my_axe");
        geckolibDoc = loadFixture("geckolib_animations");
        prettyDoc = loadFixture("pretty_printed");
    }

    private static BbModelDocument loadFixture(String name) throws IOException {
        String path = "/models/" + name + ".bbmodel";
        try (InputStream in = ApiUtilsTest.class.getResourceAsStream(path)) {
            assertNotNull(in, "Missing fixture: " + path);
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return BbModel.read(json);
        }
    }

    // -----------------------------------------------------------------------
    // ModelInfo — java_block_my_axe fixture
    // -----------------------------------------------------------------------

    @Test
    void modelInfo_formatVersion_isNonNull() {
        ModelInfo info = ModelInfo.fromDocument(axeDoc);
        assertNotNull(info.getFormatVersion(), "formatVersion must be non-null");
    }

    @Test
    void modelInfo_formatVersion_matchesDocument() {
        ModelInfo info = ModelInfo.fromDocument(axeDoc);
        // The fixture format_version is "5.0"; after read it is normalized to "5.0"
        String expected = axeDoc.getMeta().getFormatVersion().getRaw();
        assertEquals(expected, info.getFormatVersion());
    }

    @Test
    void modelInfo_elementCount_greaterThanZero() {
        ModelInfo info = ModelInfo.fromDocument(axeDoc);
        assertTrue(info.getElementCount() > 0,
                "java_block_my_axe must have at least one element");
    }

    @Test
    void modelInfo_textureCount_matchesDocumentList() {
        ModelInfo info = ModelInfo.fromDocument(axeDoc);
        assertEquals(axeDoc.getTextures().size(), info.getTextureCount(),
                "texture count from ModelInfo must equal document's texture list size");
    }

    @Test
    void modelInfo_elementCount_equalsCubeCountPlusMeshCount() {
        ModelInfo info = ModelInfo.fromDocument(axeDoc);
        assertEquals(info.getElementCount(), info.getCubeCount() + info.getMeshCount(),
                "total elements must equal cubes + meshes");
    }

    // -----------------------------------------------------------------------
    // OutlinerUtils — pretty_printed fixture (nested outliner)
    // -----------------------------------------------------------------------

    @Test
    void outlinerUtils_getAllGroups_returnsNonEmptyList() {
        OutlinerUtils utils = OutlinerUtils.forDocument(prettyDoc);
        List<OutlinerGroupNode> groups = utils.getAllGroups();
        assertFalse(groups.isEmpty(), "pretty_printed must have at least one outliner group");
    }

    @Test
    void outlinerUtils_getGroupByUuid_findsGroupThatExistsInDocument() {
        OutlinerUtils utils = OutlinerUtils.forDocument(prettyDoc);

        // Get the first group's uuid from the flat list, then look it up by uuid
        List<OutlinerGroupNode> allGroups = utils.getAllGroups();
        assertFalse(allGroups.isEmpty());

        String targetUuid = allGroups.get(0).getUuid();
        OutlinerGroupNode found = utils.getGroupByUuid(targetUuid);

        assertNotNull(found, "getGroupByUuid must find a group whose uuid was obtained from getAllGroups");
        assertEquals(targetUuid, found.getUuid());
    }

    @Test
    void outlinerUtils_getGroupByUuid_returnsNullForUnknownUuid() {
        OutlinerUtils utils = OutlinerUtils.forDocument(prettyDoc);
        assertNull(utils.getGroupByUuid("non-existent-uuid"),
                "unknown uuid must return null");
    }

    @Test
    void outlinerUtils_maxNestingDepth_greaterThanZero() {
        OutlinerUtils utils = OutlinerUtils.forDocument(prettyDoc);
        assertTrue(utils.getMaxNestingDepth() > 0,
                "a document with groups must have nesting depth > 0");
    }

    @Test
    void outlinerUtils_geckolib_groupHierarchyIsConsistent() {
        // geckolib_animations has a deeply nested outliner (body > torso/head/arm etc.)
        OutlinerUtils utils = OutlinerUtils.forDocument(geckolibDoc);

        Map<String, List<String>> hierarchy = utils.getGroupHierarchy();
        assertFalse(hierarchy.isEmpty(),
                "group hierarchy must not be empty for geckolib model");

        // Every key in the hierarchy map must correspond to a group obtainable by uuid
        for (String uuid : hierarchy.keySet()) {
            OutlinerGroupNode group = utils.getGroupByUuid(uuid);
            assertNotNull(group, "hierarchy key " + uuid + " must map to a real group");
        }
    }

    // -----------------------------------------------------------------------
    // TransformUtils — geckolib_animations fixture (has keyframed animations)
    // -----------------------------------------------------------------------

    @Test
    void transformUtils_getTransformAtTime_returnsNonNull_forAnimatedBone() {
        TransformUtils utils = TransformUtils.forDocument(geckolibDoc);

        // geckolib_animations has animation "block_idle" with keyframes for
        // right_arm (uuid ed4f86aa-4c3d-8f03-4045-c275b02ca405) and
        // right_item (uuid d26bc08b-0780-6631-872a-b935b48c6b5e).
        Animation animation = geckolibDoc.getAnimations().get(0);
        assertNotNull(animation);

        String rightArmUuid = "ed4f86aa-4c3d-8f03-4045-c275b02ca405";

        TransformUtils.Transform transform =
                utils.getTransformAtTime(rightArmUuid, animation, 0.0);

        assertNotNull(transform,
                "getTransformAtTime must return a non-null Transform for an animated bone");
    }

    @Test
    void transformUtils_getTransformAtTime_returnsFiniteValues() {
        TransformUtils utils = TransformUtils.forDocument(geckolibDoc);

        Animation animation = geckolibDoc.getAnimations().get(0);
        String rightItemUuid = "d26bc08b-0780-6631-872a-b935b48c6b5e";

        TransformUtils.Transform t =
                utils.getTransformAtTime(rightItemUuid, animation, 0.5);

        assertNotNull(t);
        // Values should be finite (no NaN/Inf from a bad interpolation)
        assertTrue(Double.isFinite(t.getRotX()), "rotX must be finite");
        assertTrue(Double.isFinite(t.getRotY()), "rotY must be finite");
        assertTrue(Double.isFinite(t.getRotZ()), "rotZ must be finite");
        assertTrue(Double.isFinite(t.getX()), "x must be finite");
        assertTrue(Double.isFinite(t.getY()), "y must be finite");
        assertTrue(Double.isFinite(t.getZ()), "z must be finite");
    }

    @Test
    void transformUtils_getTransformAtTime_returnsEmptyTransformForUnanimatedBone() {
        TransformUtils utils = TransformUtils.forDocument(geckolibDoc);

        Animation animation = geckolibDoc.getAnimations().get(0);
        // "torso" animator (be423c94-b7b8-575f-2a8a-12db04ffea78) has no keyframes
        String torsoUuid = "be423c94-b7b8-575f-2a8a-12db04ffea78";

        TransformUtils.Transform t =
                utils.getTransformAtTime(torsoUuid, animation, 0.5);

        assertNotNull(t);
        // No keyframes => transform has no animation values set; defaults apply
        assertFalse(t.hasAnyValue(),
                "A bone with no keyframes should produce a Transform with no explicit values");
    }

    @Test
    void transformUtils_getAllTransformsAtTime_keysMatchAnimatorUuids() {
        TransformUtils utils = TransformUtils.forDocument(geckolibDoc);

        Animation animation = geckolibDoc.getAnimations().get(0);
        Map<String, TransformUtils.Transform> all =
                utils.getAllTransformsAtTime(animation, 0.0);

        assertNotNull(all);
        // All uuids returned must be keys in the animation's animators map
        for (String uuid : all.keySet()) {
            assertTrue(animation.getAnimators().containsKey(uuid),
                    "getAllTransformsAtTime must only return uuids that exist as animators");
        }
    }
}
