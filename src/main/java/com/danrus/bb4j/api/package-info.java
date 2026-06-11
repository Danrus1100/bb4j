/**
 * The public API surface of bb4j.
 *
 * <p>{@link com.danrus.bb4j.api.BbModel} is the single entry point most callers
 * need: a static facade with overloaded {@code read(...)} / {@code write(...)}
 * methods accepting {@code String}, {@code File}, {@code Path},
 * {@code InputStream}/{@code OutputStream}, and {@code Reader}/{@code Writer},
 * each optionally taking a {@link com.danrus.bb4j.api.ReadOptions} or
 * {@link com.danrus.bb4j.api.WriteOptions}.
 *
 * <p>Behavioural knobs live in small option/enum types:
 * {@link com.danrus.bb4j.api.CompressionMode} (AUTO/JSON/LZUTF8),
 * {@link com.danrus.bb4j.api.VersionPolicy} (STRICT/WARN/IGNORE), and the two
 * fluent option builders. Errors surfaced to callers are
 * {@link com.danrus.bb4j.api.BbException}.
 *
 * <p>Higher-level read-oriented helpers live in
 * {@link com.danrus.bb4j.api.utils}.
 */
package com.danrus.bb4j.api;
