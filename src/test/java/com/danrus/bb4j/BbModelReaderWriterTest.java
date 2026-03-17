package com.danrus.bb4j;

import com.danrus.bb4j.api.BbModel;
import com.danrus.bb4j.api.CompressionMode;
import com.danrus.bb4j.api.ReadOptions;
import com.danrus.bb4j.api.VersionPolicy;
import com.danrus.bb4j.api.WriteOptions;
import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.animation.Animation;
import com.danrus.bb4j.model.geometry.CubeElement;
import com.danrus.bb4j.model.geometry.Element;
import com.danrus.bb4j.model.meta.FormatVersion;
import com.danrus.bb4j.model.meta.ModelFormatId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BbModelReaderWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void testReadFromString() {
        String json = getTestModelJson();
        ReadOptions options = ReadOptions.builder()
                .versionPolicy(VersionPolicy.IGNORE);
        BbModelDocument doc = BbModel.read(json, options);

        assertNotNull(doc);
        assertEquals("test_model", doc.getMeta().getName());
        assertEquals(64, doc.getResolution().getWidth());
        assertEquals(64, doc.getResolution().getHeight());
    }

    @Test
    void testReadFromFile() throws Exception {
        File testFile = tempDir.resolve("test.json").toFile();
        Files.writeString(testFile.toPath(), getTestModelJson());

        BbModelDocument doc = BbModel.read(testFile);

        assertNotNull(doc);
        assertEquals("test_model", doc.getMeta().getName());
    }

    @Test
    void testReadFromPath() throws Exception {
        Path testPath = tempDir.resolve("test.json");
        Files.writeString(testPath, getTestModelJson());

        ReadOptions options = ReadOptions.builder()
                .versionPolicy(VersionPolicy.IGNORE);
        BbModelDocument doc = BbModel.read(testPath, options);

        assertNotNull(doc);
        assertEquals("test_model", doc.getMeta().getName());
    }

    @Test
    void testReadFromInputStream() throws Exception {
        Path testPath = tempDir.resolve("test.json");
        Files.writeString(testPath, getTestModelJson());
        
        InputStream is = Files.newInputStream(testPath);
        ReadOptions options = ReadOptions.builder()
                .versionPolicy(VersionPolicy.IGNORE);
        BbModelDocument doc = BbModel.read(is, options);
        is.close();

        assertNotNull(doc);
        assertEquals("test_model", doc.getMeta().getName());
    }

    @Test
    void testWriteToString() {
        BbModelDocument doc = createTestDocument();
        String json = BbModel.write(doc);

        assertNotNull(json);
        assertTrue(json.contains("test_model"));
        assertTrue(json.contains("format_version"));
    }

    @Test
    void testWriteToFile() throws Exception {
        BbModelDocument doc = createTestDocument();
        File outputFile = tempDir.resolve("output.json").toFile();

        BbModel.write(doc, outputFile);

        assertTrue(outputFile.exists());
        String content = Files.readString(outputFile.toPath());
        assertTrue(content.contains("test_model"));
    }

    @Test
    void testWriteToPath() throws Exception {
        BbModelDocument doc = createTestDocument();
        Path outputPath = tempDir.resolve("output.json");

        BbModel.write(doc, outputPath);

        assertTrue(Files.exists(outputPath));
        String content = Files.readString(outputPath);
        assertTrue(content.contains("test_model"));
    }

    @Test
    void testRoundTrip() {
        BbModelDocument original = createTestDocument();
        String json = BbModel.write(original);
        BbModelDocument parsed = BbModel.read(json);

        assertEquals(original.getMeta().getName(), parsed.getMeta().getName());
        assertEquals(original.getResolution().getWidth(), parsed.getResolution().getWidth());
    }

    @Test
    void testReadOptionsWithMigration() {
        String oldFormat = """
            {
              "name": "old_model",
              "elements": [{"name": "cube", "from": [0,0,0], "to": [16,16,16]}],
              "outliner": [{"name": "root", "type": "group", "children": []}]
            }
            """;

        ReadOptions options = ReadOptions.builder()
                .versionPolicy(VersionPolicy.WARN);
        BbModelDocument doc = BbModel.read(oldFormat, options);

        assertNotNull(doc);
    }

    @Test
    void testWriteOptionsCompression() {
        BbModelDocument doc = createTestDocument();

        WriteOptions options = WriteOptions.builder()
                .compressionMode(CompressionMode.JSON);
        String uncompressed = BbModel.write(doc, options);

        assertFalse(uncompressed.startsWith("H4sI"));
    }

    @Test
    void testElements() {
        String json = getTestModelJson();
        ReadOptions options = ReadOptions.builder()
                .versionPolicy(VersionPolicy.IGNORE);
        BbModelDocument doc = BbModel.read(json, options);

        List<Element> elements = doc.getElements();
        assertNotNull(elements);
        assertFalse(elements.isEmpty());

        Element element = elements.get(0);
        assertTrue(element instanceof CubeElement, "Element is not CubeElement");
        CubeElement cube = (CubeElement) element;
        assertArrayEquals(new Double[]{0.0, 0.0, 0.0}, cube.getFrom());
        assertArrayEquals(new Double[]{16.0, 16.0, 16.0}, cube.getTo());
    }

    @Test
    void testAnimations() {
        String json = getTestModelJson();
        BbModelDocument doc = BbModel.read(json);

        List<Animation> animations = doc.getAnimations();
        assertFalse(animations.isEmpty());

        Animation anim = animations.get(0);
        assertEquals("test_animation", anim.getName());
        assertEquals(1.0, anim.getLoop(), 0.001);
        assertEquals(20.0, anim.getLength(), 0.001);
    }

    @Test
    void testMeta() {
        String json = getTestModelJson();
        ReadOptions options = ReadOptions.builder()
                .versionPolicy(VersionPolicy.IGNORE);
        BbModelDocument doc = BbModel.read(json, options);

        FormatVersion version = doc.getMeta().getFormatVersion();
        assertNotNull(version);
        assertEquals("4.11", version.toString());

        ModelFormatId format = doc.getMeta().getModelFormat();
        assertNotNull(format);
        assertEquals("java_block", format.getValue());
    }

    @Test
    void testInvalidJson() {
        assertThrows(Exception.class, () -> BbModel.read("invalid json"));
    }

    @Test
    void testEmptyJson() {
        BbModelDocument doc = BbModel.read("{}");
        assertNotNull(doc);
        assertNotNull(doc.getMeta());
    }

    private String getTestModelJson() {
        try {
            Path testFile = Path.of("src/test/resources/test_model.json");
            return Files.readString(testFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private BbModelDocument createTestDocument() {
        BbModelDocument doc = new BbModelDocument();
        doc.getMeta().setName("test_model");
        doc.getResolution().setWidth(64);
        doc.getResolution().setHeight(64);
        return doc;
    }
}
