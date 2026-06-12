package com.danrus.bb4j.ext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An immutable registry binding {@link RawFieldCodec}s to {@code (scope, key)}
 * pairs, controlling how selected unrecognized ("extra") fields of a
 * {@code .bbmodel} are parsed and serialized.
 *
 * <p>The same instance is passed to both reading (via
 * {@code ReadOptions.extensions(...)}) and writing (via
 * {@code WriteOptions.extensions(...)}), so decode and encode stay symmetric.
 * Build one with {@link #builder()}:
 *
 * <pre>{@code
 * ParseExtensions ext = ParseExtensions.builder()
 *         .field(RawScope.ELEMENT, "my_plugin_data", new MyDataCodec())
 *         .field(RawScope.ANY,     "telemetry",      new TelemetryCodec())
 *         .build();
 *
 * BbModelDocument doc = BbModel.read(path, ReadOptions.builder().extensions(ext));
 * MyData data = (MyData) doc.getElements().get(0).getExtra().get("my_plugin_data");
 * }</pre>
 *
 * <p>Lookup prefers an exact {@code (scope, key)} binding and falls back to a
 * {@link RawScope#ANY} binding for the same key.
 */
public final class ParseExtensions {

    /** A registry with no codecs; the default for read/write options. */
    public static final ParseExtensions EMPTY = new ParseExtensions(new HashMap<>());

    private final Map<Binding, RawFieldCodec<?>> codecs;

    private ParseExtensions(Map<Binding, RawFieldCodec<?>> codecs) {
        this.codecs = codecs;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Whether no codecs are registered (the read/write fast path). */
    public boolean isEmpty() {
        return codecs.isEmpty();
    }

    /**
     * Resolves the codec for a field, preferring an exact {@code scope} match and
     * falling back to a {@link RawScope#ANY} registration for the same key.
     *
     * @return the matching codec, or {@code null} if none is registered
     */
    public RawFieldCodec<?> codecFor(RawScope scope, String key) {
        if (codecs.isEmpty()) {
            return null;
        }
        RawFieldCodec<?> exact = codecs.get(new Binding(scope, key));
        if (exact != null) {
            return exact;
        }
        return codecs.get(new Binding(RawScope.ANY, key));
    }

    /** Fluent builder for {@link ParseExtensions}. */
    public static final class Builder {
        private final Map<Binding, RawFieldCodec<?>> codecs = new HashMap<>();

        private Builder() {}

        /**
         * Binds {@code codec} to the field named {@code key} within {@code scope}.
         * Use {@link RawScope#ANY} to match the key in any scope. Re-registering
         * the same {@code (scope, key)} replaces the previous codec.
         */
        public Builder field(RawScope scope, String key, RawFieldCodec<?> codec) {
            Objects.requireNonNull(scope, "scope");
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(codec, "codec");
            codecs.put(new Binding(scope, key), codec);
            return this;
        }

        public ParseExtensions build() {
            return new ParseExtensions(new HashMap<>(codecs));
        }
    }

    private record Binding(RawScope scope, String key) {}
}
