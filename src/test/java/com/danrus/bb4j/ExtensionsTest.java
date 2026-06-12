package com.danrus.bb4j;

import com.danrus.bb4j.api.BbModel;
import com.danrus.bb4j.api.ReadOptions;
import com.danrus.bb4j.api.WriteOptions;
import com.danrus.bb4j.ext.CodecContext;
import com.danrus.bb4j.ext.ParseExtensions;
import com.danrus.bb4j.ext.RawFieldCodec;
import com.danrus.bb4j.ext.RawScope;
import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.geometry.Element;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the pluggable {@link com.danrus.bb4j.ext} layer: a registered
 * {@link RawFieldCodec} turns a chosen unrecognized field into a typed object on
 * read, encodes it back on write, and the encode/decode pair remains lossless and
 * symmetric (the writer's output equals what a plain pass-through reader produces).
 */
class ExtensionsTest {

    /** A toy typed representation for an otherwise-unmodeled plugin field. */
    record PluginData(int power) {}

    /** Maps {@code {"power": N}} <-> {@link PluginData}. */
    static final class PluginDataCodec implements RawFieldCodec<PluginData> {
        @Override
        public PluginData decode(JsonElement raw, CodecContext ctx) {
            return new PluginData(raw.getAsJsonObject().get("power").getAsInt());
        }

        @Override
        public JsonElement encode(PluginData value, CodecContext ctx) {
            JsonObject obj = new JsonObject();
            obj.add("power", new JsonPrimitive(value.power()));
            return obj;
        }
    }

    private static final String MODEL_JSON = "{"
            + "\"meta\":{\"format_version\":\"4.5\",\"model_format\":\"free\"},"
            + "\"elements\":[{\"type\":\"cube\",\"uuid\":\"e1\",\"name\":\"c\","
            + "\"my_plugin_data\":{\"power\":5}}],"
            + "\"my_root_ext\":{\"power\":7}"
            + "}";

    private static ParseExtensions extensions() {
        return ParseExtensions.builder()
                .field(RawScope.ELEMENT, "my_plugin_data", new PluginDataCodec())
                .field(RawScope.ROOT, "my_root_ext", new PluginDataCodec())
                .build();
    }

    @Test
    void registeredFieldsDecodeToTypedObjects() {
        ParseExtensions ext = extensions();
        BbModelDocument doc = BbModel.read(MODEL_JSON, ReadOptions.builder().extensions(ext));

        Object elementValue = doc.getElements().get(0).getExtra().get("my_plugin_data");
        assertInstanceOf(PluginData.class, elementValue, "element field should be decoded");
        assertEquals(5, ((PluginData) elementValue).power());

        Object rootValue = doc.getRawData().get("my_root_ext");
        assertInstanceOf(PluginData.class, rootValue, "root field should be decoded");
        assertEquals(7, ((PluginData) rootValue).power());
    }

    @Test
    void withoutExtensionsFieldsStayRaw() {
        BbModelDocument doc = BbModel.read(MODEL_JSON);
        Object elementValue = doc.getElements().get(0).getExtra().get("my_plugin_data");
        assertInstanceOf(JsonElement.class, elementValue,
                "without a registered codec the field must remain a raw JsonElement");
    }

    @Test
    void encodeIsLosslessAndDoesNotMutateDocument() {
        ParseExtensions ext = extensions();
        BbModelDocument doc = BbModel.read(MODEL_JSON, ReadOptions.builder().extensions(ext));

        String written = BbModel.write(doc, WriteOptions.builder().extensions(ext));

        // Writing must not leave the document mutated to raw JSON.
        assertInstanceOf(PluginData.class, doc.getElements().get(0).getExtra().get("my_plugin_data"),
                "write should restore the typed value on the document");

        // A plain pass-through read sees exactly the JSON the codec emitted.
        BbModelDocument plain = BbModel.read(written);
        JsonElement elementRaw = (JsonElement) plain.getElements().get(0).getExtra().get("my_plugin_data");
        assertEquals("{\"power\":5}", elementRaw.toString());
        JsonElement rootRaw = (JsonElement) plain.getRawData().get("my_root_ext");
        assertEquals("{\"power\":7}", rootRaw.toString());
    }

    @Test
    void roundTripIsStableWithExtensions() {
        ParseExtensions ext = extensions();

        BbModelDocument doc1 = BbModel.read(MODEL_JSON, ReadOptions.builder().extensions(ext));
        String write1 = BbModel.write(doc1, WriteOptions.builder().extensions(ext));

        BbModelDocument doc2 = BbModel.read(write1, ReadOptions.builder().extensions(ext));
        String write2 = BbModel.write(doc2, WriteOptions.builder().extensions(ext));

        assertEquals(write1, write2, "extension round-trip must be byte-stable");
        assertTrue(write1.contains("my_plugin_data"));
    }
}
