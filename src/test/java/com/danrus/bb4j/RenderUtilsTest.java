package com.danrus.bb4j;

import com.danrus.bb4j.api.BbModel;
import com.danrus.bb4j.api.utils.RenderUtils;
import com.danrus.bb4j.model.BbModelDocument;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Guards that the indexed/cached {@link RenderUtils} still produces the same
 * geometry: results must be deterministic across repeated calls on one (cached)
 * instance and identical to a freshly created instance.
 */
class RenderUtilsTest {

    private static BbModelDocument axeDoc;

    @BeforeAll
    static void load() throws IOException {
        try (InputStream in = RenderUtilsTest.class.getResourceAsStream("/models/java_block_my_axe.bbmodel")) {
            assertNotNull(in);
            axeDoc = BbModel.read(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void producesMeshes() {
        List<RenderUtils.RenderableMesh> meshes = RenderUtils.forDocument(axeDoc).getAllMeshes();
        assertFalse(meshes.isEmpty(), "axe fixture must produce renderable meshes");
    }

    @Test
    void repeatedCallsOnCachedInstanceAreStable() {
        RenderUtils utils = RenderUtils.forDocument(axeDoc);
        List<RenderUtils.RenderableMesh> first = utils.getAllMeshes();
        List<RenderUtils.RenderableMesh> second = utils.getAllMeshes();
        assertSameGeometry(first, second);
    }

    @Test
    void cachedInstanceMatchesFreshInstance() {
        List<RenderUtils.RenderableMesh> fresh = RenderUtils.forDocument(axeDoc).getAllMeshes();
        RenderUtils reused = RenderUtils.forDocument(axeDoc);
        reused.getAllMeshes(); // warm the caches
        assertSameGeometry(fresh, reused.getAllMeshes());
    }

    private static void assertSameGeometry(List<RenderUtils.RenderableMesh> a, List<RenderUtils.RenderableMesh> b) {
        assertEquals(a.size(), b.size(), "mesh count must match");
        for (int i = 0; i < a.size(); i++) {
            RenderUtils.RenderableMesh ma = a.get(i);
            RenderUtils.RenderableMesh mb = b.get(i);
            assertEquals(ma.getElementUuid(), mb.getElementUuid());
            assertEquals(ma.getFaces().size(), mb.getFaces().size(), "face count must match");
            for (int f = 0; f < ma.getFaces().size(); f++) {
                assertEquals(
                    Arrays.deepToString(ma.getFaces().get(f).getVertices()),
                    Arrays.deepToString(mb.getFaces().get(f).getVertices()),
                    "face vertices must match");
                assertEquals(
                    Arrays.deepToString(ma.getFaces().get(f).getVertexUvs()),
                    Arrays.deepToString(mb.getFaces().get(f).getVertexUvs()),
                    "face UVs must match");
            }
        }
    }
}
