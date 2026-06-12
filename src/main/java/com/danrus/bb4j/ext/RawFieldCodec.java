package com.danrus.bb4j.ext;

import com.google.gson.JsonElement;

/**
 * A pluggable codec controlling how a single unrecognized ("extra") field of a
 * {@code .bbmodel} is turned into a typed Java value on read and back into JSON
 * on write.
 *
 * <p>By default bb4j keeps every unmodeled field as a raw Gson
 * {@link JsonElement} so it survives a read/write round-trip untouched. Register
 * a codec via {@link ParseExtensions} to instead expose such a field as a typed
 * object: on read the stored {@link JsonElement} is replaced by
 * {@link #decode(JsonElement, CodecContext) decode}'s result, and on write that
 * value is turned back into JSON by {@link #encode(Object, CodecContext) encode}.
 *
 * <p>Implementations should be symmetric — {@code encode(decode(x))} must be
 * JSON-equal to {@code x} — otherwise the library's round-trip guarantee no
 * longer holds for that field. There is intentionally no Gson-reflection default:
 * the (de)serialization is entirely under the implementor's control (the supplied
 * {@link CodecContext#gson()} may be used if convenient).
 *
 * @param <T> the typed representation exposed to callers
 * @see ParseExtensions
 * @see RawScope
 */
public interface RawFieldCodec<T> {

    /**
     * Converts the raw JSON value of the field into its typed representation.
     *
     * @param raw the on-disk JSON value (never {@code null}; may be JSON null)
     * @param ctx the scope/key/gson context of this field
     * @return the typed value to store in the owning object's {@code extra} map
     */
    T decode(JsonElement raw, CodecContext ctx);

    /**
     * Converts a typed value back into the JSON to be written for the field.
     *
     * @param value the typed value previously produced by {@link #decode}
     *              (or set by the caller); never {@code null}
     * @param ctx   the scope/key/gson context of this field
     * @return the JSON element to emit for the field
     */
    JsonElement encode(T value, CodecContext ctx);
}
