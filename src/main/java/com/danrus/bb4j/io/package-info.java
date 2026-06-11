/**
 * Serialization between {@code .bbmodel} bytes and the typed document model.
 *
 * <p>{@link com.danrus.bb4j.io.BbModelReader} and
 * {@link com.danrus.bb4j.io.BbModelWriter} perform <em>manual, field-by-field</em>
 * Gson {@link com.google.gson.JsonObject} traversal — there is no reflective
 * {@code @SerializedName} mapping. The two are intentionally symmetric: every
 * field the reader understands, the writer emits, and any unrecognized field is
 * preserved through the {@code extra}/{@code rawData} mechanism so round-trips
 * stay lossless. <strong>When you add a field to a model class, wire it up in
 * both the reader and the writer.</strong>
 *
 * <p>{@link com.danrus.bb4j.io.JsonCodec} wraps the configured Gson instances.
 * {@code .bbmodel} files may be LZ-UTF8 compressed;
 * {@link com.danrus.bb4j.io.BbModelFormatDetector} and
 * {@link com.danrus.bb4j.io.LzUtf8Codec} handle detection and (de)compression,
 * controlled by {@link com.danrus.bb4j.api.CompressionMode}.
 */
package com.danrus.bb4j.io;
