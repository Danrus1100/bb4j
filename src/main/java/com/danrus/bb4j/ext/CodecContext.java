package com.danrus.bb4j.ext;

import com.google.gson.Gson;

/**
 * Side information passed to a {@link RawFieldCodec} during decode/encode.
 *
 * <p>Carries the {@link #scope()} and {@link #key()} the codec was invoked for
 * (handy for codecs registered against {@link RawScope#ANY}) and a shared
 * {@link #gson()} instance the codec may use to (de)serialize nested structures.
 * Instances are created internally by the library; codec authors only read them.
 */
public final class CodecContext {
    private final RawScope scope;
    private final String key;
    private final Gson gson;

    public CodecContext(RawScope scope, String key, Gson gson) {
        this.scope = scope;
        this.key = key;
        this.gson = gson;
    }

    /** The scope of the object whose field is being (de)serialized. */
    public RawScope scope() {
        return scope;
    }

    /** The JSON key of the field being (de)serialized. */
    public String key() {
        return key;
    }

    /** A Gson instance the codec may use for nested (de)serialization. */
    public Gson gson() {
        return gson;
    }
}
