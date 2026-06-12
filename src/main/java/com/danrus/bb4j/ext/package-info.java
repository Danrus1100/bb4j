/**
 * Extensibility hooks for controlling how selected <em>raw</em> ("extra") fields
 * of a {@code .bbmodel} are parsed and serialized.
 *
 * <p>By default bb4j keeps every field it does not model explicitly as a raw
 * Gson {@code JsonElement} (in each object's {@code extra} map, or
 * {@code BbModelDocument.rawData} at the top level) so it survives a read/write
 * round-trip untouched. This package lets a consumer give chosen fields a typed
 * representation instead, without forking the library:
 *
 * <ul>
 *   <li>{@link com.danrus.bb4j.ext.RawFieldCodec} — a per-field decode/encode
 *       pair (no Gson-reflection default; the mapping is fully explicit).</li>
 *   <li>{@link com.danrus.bb4j.ext.RawScope} — names the kind of object a field
 *       belongs to, so codecs bind to a place in the document, not just a key.</li>
 *   <li>{@link com.danrus.bb4j.ext.ParseExtensions} — an immutable registry of
 *       {@code (scope, key) -> codec} bindings, passed to both
 *       {@code ReadOptions.extensions(...)} and {@code WriteOptions.extensions(...)}.</li>
 *   <li>{@link com.danrus.bb4j.ext.ExtensionProcessor} — the internal engine that
 *       applies the registry across a document after read and before write.</li>
 * </ul>
 */
package com.danrus.bb4j.ext;
