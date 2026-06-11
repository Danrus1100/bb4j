package com.danrus.bb4j;

import com.danrus.bb4j.api.BbModel;
import com.danrus.bb4j.api.ReadOptions;
import com.danrus.bb4j.api.WriteOptions;
import com.danrus.bb4j.model.BbModelDocument;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that reading and writing {@code .bbmodel} files is stable and lossless.
 *
 * <p>The core property checked is <em>idempotent round-tripping</em>: after the
 * first {@code read -> write} normalizes a file (running migrations and canonical
 * formatting), a subsequent {@code read -> write} must reproduce byte-for-byte the
 * same output. Any field the reader understands but the writer drops would surface
 * here as a difference between the two writes.
 */
class RoundTripTest {

    /** Fixtures bundled under {@code src/test/resources/models}. */
    private static final String[] FIXTURES = {
            "java_block_my_axe",
            "geckolib_animations",
            "reference_images",
            "bedrock_v45_boxuv",
            "pretty_printed"
    };

    private static String loadFixture(String name) throws IOException {
        String path = "/models/" + name + ".bbmodel";
        try (InputStream in = RoundTripTest.class.getResourceAsStream(path)) {
            assertNotNull(in, "Missing fixture: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void roundTripIsStableForAllFixtures() throws IOException {
        for (String fixture : FIXTURES) {
            String original = loadFixture(fixture);

            BbModelDocument doc1 = BbModel.read(original);
            String write1 = BbModel.write(doc1);

            BbModelDocument doc2 = BbModel.read(write1);
            String write2 = BbModel.write(doc2);

            assertEquals(write1, write2,
                    "Round-trip not stable for fixture '" + fixture + "' — "
                            + "the writer is dropping or reordering data the reader preserves.");
        }
    }

    @Test
    void unknownTopLevelFieldsArePreserved() {
        String json = "{"
                + "\"meta\":{\"format_version\":\"5.0\",\"model_format\":\"free\"},"
                + "\"resolution\":{\"width\":16,\"height\":16},"
                + "\"some_future_field\":{\"nested\":[1,2,3]},"
                + "\"another_unknown\":42"
                + "}";

        BbModelDocument doc = BbModel.read(json);
        assertNotNull(doc.getRawData(), "Unknown top-level fields should be captured into rawData");
        assertTrue(doc.getRawData().containsKey("some_future_field"));
        assertTrue(doc.getRawData().containsKey("another_unknown"));

        String written = BbModel.write(doc);
        assertTrue(written.contains("some_future_field"),
                "Unknown top-level fields must be re-emitted on write");
        assertTrue(written.contains("another_unknown"));
    }

    @Test
    void unknownNestedFieldsArePreserved() {
        String json = "{"
                + "\"meta\":{\"format_version\":\"5.0\",\"model_format\":\"free\"},"
                + "\"textures\":[{\"name\":\"t\",\"uuid\":\"u\",\"future_texture_prop\":\"keepme\"}]"
                + "}";

        BbModelDocument doc = BbModel.read(json);
        String written = BbModel.write(doc);
        assertTrue(written.contains("future_texture_prop"),
                "Unknown nested fields must survive a round-trip via the 'extra' mechanism");
        assertTrue(written.contains("keepme"));
    }

    @Test
    void meshGeometrySurvivesRoundTrip() {
        String json = "{"
                + "\"meta\":{\"format_version\":\"5.0\",\"model_format\":\"free\"},"
                + "\"elements\":[{"
                + "  \"type\":\"mesh\",\"name\":\"m\",\"uuid\":\"e1\","
                + "  \"vertices\":{\"v0\":[0,0,0],\"v1\":[1,0,0],\"v2\":[1,1,0]},"
                + "  \"faces\":{\"f0\":{\"vertices\":[\"v0\",\"v1\",\"v2\"],"
                + "    \"uv\":{\"v0\":[0,0],\"v1\":[1,0],\"v2\":[1,1]},\"texture\":0}}"
                + "}]"
                + "}";

        BbModelDocument doc = BbModel.read(json);
        String write1 = BbModel.write(doc);

        assertTrue(write1.contains("vertices"), "Mesh vertices must be serialized");
        assertTrue(write1.contains("\"v0\""), "Mesh vertex keys must be serialized");

        // And it must be stable across a second round-trip.
        String write2 = BbModel.write(BbModel.read(write1));
        assertEquals(write1, write2, "Mesh round-trip must be stable");
    }

    @Test
    void animationLoopKeepsBlockbenchStringEnum() throws IOException {
        // Blockbench stores loop as the string enum "once"/"loop"/"hold", not a number.
        String original = loadFixture("geckolib_animations");
        String written = BbModel.write(BbModel.read(original));
        assertTrue(written.contains("\"loop\": \"once\""),
                "loop must be re-emitted as the Blockbench string enum, got: "
                        + written.replaceAll("(?s).*(\"loop\"[^,}]*).*", "$1"));
    }

    @Test
    void faceTextureIsWrittenAsIntegerIndexOrNull() {
        // Blockbench writes a face's texture as an integer index, or null — never a string.
        String json = "{"
                + "\"meta\":{\"format_version\":\"5.0\",\"model_format\":\"java_block\"},"
                + "\"elements\":[{\"type\":\"cube\",\"uuid\":\"e\",\"from\":[0,0,0],\"to\":[1,1,1],"
                + "  \"faces\":{"
                + "    \"north\":{\"uv\":[0,0,1,1],\"texture\":0},"
                + "    \"south\":{\"uv\":[0,0,1,1],\"texture\":null}"
                + "  }}]"
                + "}";
        String written = BbModel.write(BbModel.read(json));
        assertTrue(written.contains("\"texture\": 0"), "numeric texture must stay an integer index");
        assertTrue(written.contains("\"texture\": null"), "explicit null texture must be preserved");
        assertTrue(!written.contains("\"texture\": \"0\""), "texture index must not become a string");
    }

    @Test
    void compactWriteRoundTrips() throws IOException {
        String original = loadFixture("java_block_my_axe");
        WriteOptions compact = WriteOptions.builder().prettyPrint(false);

        BbModelDocument doc1 = BbModel.read(original);
        String write1 = BbModel.write(doc1, compact);

        BbModelDocument doc2 = BbModel.read(write1, ReadOptions.builder());
        String write2 = BbModel.write(doc2, compact);

        assertEquals(write1, write2, "Compact round-trip must be stable");
    }
}
