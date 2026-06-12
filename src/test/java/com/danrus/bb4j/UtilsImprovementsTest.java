package com.danrus.bb4j;

import com.danrus.bb4j.api.utils.ElementUtils;
import com.danrus.bb4j.api.utils.ModelValidator;
import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.geometry.CubeElement;
import com.danrus.bb4j.model.geometry.Face;
import com.danrus.bb4j.model.geometry.MeshElement;
import com.danrus.bb4j.model.outliner.OutlinerElementRefNode;
import com.danrus.bb4j.model.outliner.OutlinerGroupNode;
import com.danrus.bb4j.model.outliner.OutlinerNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the {@code api/utils} fixes: correct model bounds (including for
 * negative-coordinate and mesh models), reuse of the outliner traversal, and the
 * new {@link ModelValidator}.
 */
class UtilsImprovementsTest {

    private static CubeElement cube(String uuid, double[] from, double[] to) {
        CubeElement e = new CubeElement();
        e.setUuid(uuid);
        e.setFrom(new Double[]{from[0], from[1], from[2]});
        e.setTo(new Double[]{to[0], to[1], to[2]});
        return e;
    }

    @Test
    void modelBoundsCorrectForNegativeCoordinates() {
        BbModelDocument doc = new BbModelDocument();
        doc.setElements(List.of(cube("a", new double[]{-10, -10, -10}, new double[]{-2, -4, -6})));

        double[] b = ElementUtils.forDocument(doc).getModelBounds();
        assertEquals(-10, b[0]);
        assertEquals(-10, b[1]);
        assertEquals(-10, b[2]);
        assertEquals(-2, b[3]);
        assertEquals(-4, b[4]);
        assertEquals(-6, b[5]);
        assertEquals(8, ElementUtils.forDocument(doc).getModelWidth());
    }

    @Test
    void modelBoundsAreZeroWhenNoPositionedGeometry() {
        BbModelDocument doc = new BbModelDocument();
        double[] b = ElementUtils.forDocument(doc).getModelBounds();
        assertEquals(0, ElementUtils.forDocument(doc).getModelWidth());
        for (double v : b) {
            assertEquals(0, v, "empty model bounds must be all zeros, not garbage");
        }
    }

    @Test
    void meshContributesToBoundsAndVolume() {
        MeshElement mesh = new MeshElement();
        mesh.setUuid("m");
        Map<String, Double[]> vertices = new HashMap<>();
        vertices.put("v1", new Double[]{0.0, 0.0, 0.0});
        vertices.put("v2", new Double[]{4.0, 0.0, 0.0});
        vertices.put("v3", new Double[]{0.0, 3.0, 0.0});
        vertices.put("v4", new Double[]{0.0, 0.0, 2.0});
        mesh.setVertices(vertices);

        BbModelDocument doc = new BbModelDocument();
        doc.setElements(List.of(mesh));

        ElementUtils utils = ElementUtils.forDocument(doc);
        assertEquals(4, utils.getModelWidth(), "mesh width must come from its vertices");
        assertEquals(3, utils.getModelHeight());
        assertEquals(2, utils.getModelDepth());
        assertEquals(24, utils.getTotalVolume(), "mesh model must not report zero volume");
    }

    @Test
    void getElementsInGroupResolvesOutlinerRefs() {
        CubeElement e = cube("e1", new double[]{0, 0, 0}, new double[]{1, 1, 1});

        OutlinerGroupNode group = new OutlinerGroupNode();
        group.setUuid("g1");
        group.addChild(new OutlinerElementRefNode("e1"));

        BbModelDocument doc = new BbModelDocument();
        doc.setElements(List.of(e));
        List<OutlinerNode> outliner = new ArrayList<>();
        outliner.add(group);
        doc.setOutliner(outliner);

        var inGroup = ElementUtils.forDocument(doc).getElementsInGroup("g1");
        assertEquals(1, inGroup.size());
        assertEquals("e1", inGroup.get(0).getUuid());
    }

    @Test
    void validatorReportsDuplicateUuidsAndDanglingRefs() {
        BbModelDocument doc = new BbModelDocument();
        doc.setElements(List.of(
                cube("dup", new double[]{0, 0, 0}, new double[]{1, 1, 1}),
                cube("dup", new double[]{1, 1, 1}, new double[]{2, 2, 2})));

        OutlinerGroupNode group = new OutlinerGroupNode();
        group.setUuid("g1");
        group.addChild(new OutlinerElementRefNode("ghost")); // no such element
        List<OutlinerNode> outliner = new ArrayList<>();
        outliner.add(group);
        doc.setOutliner(outliner);

        ModelValidator validator = ModelValidator.forDocument(doc);
        List<ModelValidator.Issue> issues = validator.validate();

        assertTrue(hasCode(issues, ModelValidator.DUPLICATE_ELEMENT_UUID));
        assertTrue(hasCode(issues, ModelValidator.DANGLING_OUTLINER_REF));
        assertFalse(validator.isValid(), "a model with errors must not be valid");
    }

    @Test
    void validatorFlagsMissingFaceTextureAndNonFiniteCoords() {
        CubeElement e = cube("e1", new double[]{Double.NaN, 0, 0}, new double[]{1, 1, 1});
        Face face = new Face("north");
        face.setTexture("99"); // no textures exist in the document
        e.getFaces().put("north", face);

        BbModelDocument doc = new BbModelDocument();
        doc.setElements(List.of(e));

        List<ModelValidator.Issue> issues = ModelValidator.forDocument(doc).validate();
        assertTrue(hasCode(issues, ModelValidator.MISSING_FACE_TEXTURE));
        assertTrue(hasCode(issues, ModelValidator.NON_FINITE_COORDINATE));
    }

    @Test
    void validatorPassesForCleanModel() {
        CubeElement e = cube("e1", new double[]{0, 0, 0}, new double[]{1, 1, 1});
        BbModelDocument doc = new BbModelDocument();
        doc.setElements(List.of(e));
        assertTrue(ModelValidator.forDocument(doc).isValid());
    }

    private static boolean hasCode(List<ModelValidator.Issue> issues, String code) {
        return issues.stream().anyMatch(i -> i.code().equals(code));
    }
}
