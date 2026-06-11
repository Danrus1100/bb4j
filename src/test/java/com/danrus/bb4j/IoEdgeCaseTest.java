package com.danrus.bb4j;

import com.danrus.bb4j.api.BbException;
import com.danrus.bb4j.api.BbModel;
import com.danrus.bb4j.api.WriteOptions;
import com.danrus.bb4j.model.BbModelDocument;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for IO edge-cases: corrupt input, empty input, and write-option flags.
 */
class IoEdgeCaseTest {

    // -----------------------------------------------------------------------
    // Corrupted / malformed JSON input
    // -----------------------------------------------------------------------

    @Test
    void corruptedJson_throws_BbExceptionWithParseErrorCode() {
        String corruptJson = "{ this is not json";

        BbException ex = assertThrows(BbException.class,
                () -> BbModel.read(corruptJson));

        assertEquals("PARSE_ERROR", ex.getErrorCode(),
                "Corrupt JSON must surface as PARSE_ERROR");
    }

    /**
     * Empty (or blank) input: Gson parses it as JSON null, which is a valid-but-
     * non-object document. The reader guards against this and surfaces a clean
     * {@code PARSE_ERROR} rather than letting {@code getAsJsonObject()} leak a raw
     * {@link IllegalStateException} to callers.
     */
    @Test
    void emptyString_throwsParseError() {
        BbException ex = assertThrows(BbException.class, () -> BbModel.read(""),
                "Empty input must surface as a BbException");
        assertEquals("PARSE_ERROR", ex.getErrorCode(),
                "Empty/non-object input must surface as PARSE_ERROR");
    }

    // -----------------------------------------------------------------------
    // WriteOptions: includeEditorState / includeHistory flags
    // -----------------------------------------------------------------------

    /**
     * Build a document from inline JSON that includes {@code editor_state} and
     * {@code history} objects, then verify:
     * - with both flags ON  → both keys appear in the output
     * - with both flags OFF → neither key appears in the output
     */
    @Test
    void writeOptions_editorStateAndHistory_emittedOnlyWhenFlagEnabled() {
        // Minimal document that has both editor_state and history
        String json = "{"
                + "\"meta\":{\"format_version\":\"5.0\",\"model_format\":\"free\"},"
                + "\"resolution\":{\"width\":16,\"height\":16},"
                + "\"editor_state\":{\"mode\":\"edit\",\"tool\":\"move\"},"
                + "\"history\":{\"history_index\":0,\"history\":[]}"
                + "}";

        BbModelDocument doc = BbModel.read(json);

        // Sanity: verify the reader populated the fields
        assertNotNull(doc.getEditorState(),
                "editor_state should be parsed into the document");
        assertNotNull(doc.getHistory(),
                "history should be parsed into the document");

        // --- flags ON ---
        String withFlags = BbModel.write(doc,
                WriteOptions.builder()
                        .includeEditorState(true)
                        .includeHistory(true));

        assertTrue(withFlags.contains("editor_state"),
                "editor_state must appear when includeEditorState=true");
        assertTrue(withFlags.contains("history"),
                "history must appear when includeHistory=true");

        // --- flags OFF (defaults) ---
        String withoutFlags = BbModel.write(doc, WriteOptions.builder());

        assertFalse(withoutFlags.contains("editor_state"),
                "editor_state must be absent when includeEditorState=false (default)");
        // "history" may appear as part of other keys in the JSON, so we check
        // for the exact key pattern used by the writer
        assertFalse(withoutFlags.contains("\"history\""),
                "history key must be absent when includeHistory=false (default)");
    }

    @Test
    void writeOptions_editorState_absentByDefault_forDocumentWithoutIt() {
        // A document without editor_state or history
        String json = "{"
                + "\"meta\":{\"format_version\":\"5.0\",\"model_format\":\"free\"},"
                + "\"resolution\":{\"width\":16,\"height\":16}"
                + "}";

        BbModelDocument doc = BbModel.read(json);
        assertNull(doc.getEditorState());
        assertNull(doc.getHistory());

        // Even with flags ON, output must not contain the keys (nothing to emit)
        String written = BbModel.write(doc,
                WriteOptions.builder()
                        .includeEditorState(true)
                        .includeHistory(true));

        assertFalse(written.contains("editor_state"),
                "editor_state must not appear when document has none");
        assertFalse(written.contains("\"history\""),
                "history must not appear when document has none");
    }
}
