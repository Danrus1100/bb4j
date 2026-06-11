package com.danrus.bb4j;

import com.danrus.bb4j.api.BbModel;
import com.danrus.bb4j.api.ReadOptions;
import com.danrus.bb4j.api.VersionPolicy;
import com.danrus.bb4j.model.BbModelDocument;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Semantic round-trip tests: verifies that every value present in the original
 * JSON survives a read→write cycle, and that version policy is honoured
 * correctly.
 *
 * <p>Unlike {@link RoundTripTest}, which checks byte-for-byte idempotence of a
 * second write, these tests compare the first write back against the <em>original
 * source</em> and are therefore sensitive to genuine data-loss even on the first
 * pass.
 */
class SemanticRoundTripTest {

    private static final String[] FIXTURES = {
            "java_block_my_axe",
            "geckolib_animations",
            "reference_images",
            "bedrock_v45_boxuv",
            "pretty_printed"
    };

    private static String loadFixture(String name) throws IOException {
        String path = "/models/" + name + ".bbmodel";
        try (InputStream in = SemanticRoundTripTest.class.getResourceAsStream(path)) {
            assertNotNull(in, "Missing fixture: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    // -------------------------------------------------------------------------
    // Test 1 — every value in the original survives read→write
    // -------------------------------------------------------------------------

    @Test
    void originalValuesSurviveRoundTrip() throws IOException {
        for (String fixture : FIXTURES) {
            String original = loadFixture(fixture);

            // Guard: skip LZ-compressed fixtures — we need raw JSON for comparison.
            if (!original.trim().startsWith("{")) {
                continue;
            }

            JsonElement originalTree = JsonParser.parseString(original);

            // Read with migrations DISABLED so we are testing reader→writer
            // symmetry, not migration transforms.
            BbModelDocument doc = BbModel.read(original,
                    ReadOptions.builder().versionPolicy(VersionPolicy.IGNORE));

            String written = BbModel.write(doc);
            JsonElement actualTree = JsonParser.parseString(written);

            // Use the fixture name as the root path prefix so all failure
            // messages identify which file the problem came from.
            assertContains(originalTree, actualTree, fixture);
        }
    }

    /**
     * Asserts that every leaf value reachable from {@code expected} is also
     * present in {@code actual} at the same path. Extra keys or elements in
     * {@code actual} are tolerated (the writer may add defaults), but nothing
     * the original declared may be silently lost.
     *
     * <p>Intentional skips are documented inline.
     *
     * @param expected the original JSON sub-tree
     * @param actual   the written JSON sub-tree
     * @param path     dot-path from the fixture root, used in failure messages
     */
    private static void assertContains(JsonElement expected, JsonElement actual, String path) {

        // ---- OBJECT ----
        if (expected.isJsonObject()) {
            assertTrue(actual.isJsonObject(),
                    "Expected JSON object at '" + path + "' but got: " + actual);

            JsonObject expObj = expected.getAsJsonObject();
            JsonObject actObj = actual.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : expObj.entrySet()) {
                String key = entry.getKey();
                JsonElement val = entry.getValue();
                String childPath = path + "." + key;

                // --- Intentional skips ---

                // (a) editor_state / history: excluded from write by default
                //     (WriteOptions.isIncludeEditorState/isIncludeHistory default to false).
                //     These keys live at the document root.
                if (isRootLevel(path) && (key.equals("editor_state") || key.equals("history"))) {
                    continue;
                }

                // (b) meta.format_version: with IGNORE policy the reader keeps
                //     the raw version string; the writer re-emits what the model
                //     holds, so this should survive.  We do NOT skip it here —
                //     the test intentionally verifies it is preserved under IGNORE.

                // (c) Empty containers: the writer omits empty arrays/objects to
                //     match Blockbench's own output; tolerate their absence.
                if ((val.isJsonArray()  && val.getAsJsonArray().isEmpty()) ||
                    (val.isJsonObject() && val.getAsJsonObject().isEmpty())) {
                    continue;
                }

                // (d) Explicit JSON null at a known-field position: the model
                //     stores null as Java null which the writer may skip.
                if (val.isJsonNull()) {
                    continue;
                }

                assertTrue(actObj.has(key),
                        "Lost key '" + childPath + "' for fixture '"
                                + rootFixture(path) + "'");

                assertContains(val, actObj.get(key), childPath);
            }

            return;
        }

        // ---- ARRAY ----
        if (expected.isJsonArray()) {
            assertTrue(actual.isJsonArray(),
                    "Expected JSON array at '" + path + "' but got: " + actual);

            JsonArray expArr = expected.getAsJsonArray();
            JsonArray actArr = actual.getAsJsonArray();

            assertEquals(expArr.size(), actArr.size(),
                    "Array size mismatch at '" + path + "' for fixture '"
                            + rootFixture(path) + "'"
                            + " — expected " + expArr.size() + ", got " + actArr.size());

            for (int i = 0; i < expArr.size(); i++) {
                assertContains(expArr.get(i), actArr.get(i), path + "[" + i + "]");
            }

            return;
        }

        // ---- NULL ----
        if (expected.isJsonNull()) {
            assertTrue(actual.isJsonNull(),
                    "Expected null at '" + path + "' for fixture '"
                            + rootFixture(path) + "' but got: " + actual);
            return;
        }

        // ---- PRIMITIVE ----
        assertEquals(expected, actual,
                "Value mismatch at '" + path + "' for fixture '"
                        + rootFixture(path) + "'"
                        + " — expected <" + expected + "> but was <" + actual + ">");
    }

    /**
     * Returns true when {@code path} is at the document root level, i.e. it
     * equals the fixture name with no dot-separator child.
     *
     * <p>Example: for fixture "java_block_my_axe" the root path is exactly
     * {@code "java_block_my_axe"}, and a child key "textures" produces
     * {@code "java_block_my_axe.textures"}.
     */
    private static boolean isRootLevel(String path) {
        // The fixture name itself has no '.' so if there's exactly one segment
        // and no dot the caller is at root.
        return !path.contains(".");
    }

    /**
     * Extracts the fixture name from a path like {@code "java_block_my_axe.meta.format_version"}.
     */
    private static String rootFixture(String path) {
        int dot = path.indexOf('.');
        return dot < 0 ? path : path.substring(0, dot);
    }

    // -------------------------------------------------------------------------
    // Test 2 — VersionPolicy.IGNORE keeps the original format_version
    // -------------------------------------------------------------------------

    @Test
    void ignorePolicyKeepsOriginalFormatVersion() throws IOException {
        String original = loadFixture("bedrock_v45_boxuv");

        BbModelDocument doc = BbModel.read(original,
                ReadOptions.builder().versionPolicy(VersionPolicy.IGNORE));
        String written = BbModel.write(doc);

        JsonObject root = JsonParser.parseString(written).getAsJsonObject();
        assertNotNull(root.getAsJsonObject("meta"), "'meta' missing from written output");
        String fv = root.getAsJsonObject("meta").get("format_version").getAsString();

        assertEquals("4.5", fv,
                "IGNORE policy must preserve the original format_version '4.5'"
                        + " but got '" + fv + "'");
        assertTrue(!written.contains("\"5.0\""),
                "IGNORE policy must not introduce '5.0' into the output");
    }

    // -------------------------------------------------------------------------
    // Test 3 — default (WARN) policy bumps format_version to latest
    // -------------------------------------------------------------------------

    @Test
    void warnPolicyBumpsFormatVersionToLatest() throws IOException {
        String original = loadFixture("bedrock_v45_boxuv");

        // Default read uses WARN policy which triggers migration.
        BbModelDocument doc = BbModel.read(original);
        String written = BbModel.write(doc);

        JsonObject root = JsonParser.parseString(written).getAsJsonObject();
        assertNotNull(root.getAsJsonObject("meta"), "'meta' missing from written output");
        String fv = root.getAsJsonObject("meta").get("format_version").getAsString();

        assertEquals("5.0", fv,
                "Default (WARN) policy must bump format_version to '5.0'"
                        + " but got '" + fv + "'");
    }
}
