/**
 * bb4j — a Java library for reading, writing, migrating, and inspecting
 * Blockbench {@code .bbmodel} project files.
 *
 * <p>The library has no application entry point; its public surface is the
 * {@link com.danrus.bb4j.api} package. The typical flow is
 * <em>bytes &rarr; typed document &rarr; (optional migration) &rarr; typed
 * document &rarr; bytes</em>, with everything orbiting
 * {@link com.danrus.bb4j.model.BbModelDocument}.
 *
 * <h2>Getting started</h2>
 * <pre>{@code
 * BbModelDocument doc = BbModel.read(new File("model.bbmodel"));
 * // ... inspect or mutate doc ...
 * BbModel.write(doc, new File("model.bbmodel"));
 * }</pre>
 *
 * <h2>Package map</h2>
 * <ul>
 *   <li>{@link com.danrus.bb4j.api} — the {@code BbModel} facade and option types
 *       callers use.</li>
 *   <li>{@link com.danrus.bb4j.model} — the in-memory document model.</li>
 *   <li>{@link com.danrus.bb4j.io} — JSON (de)serialization and LZ-UTF8
 *       compression handling.</li>
 *   <li>{@link com.danrus.bb4j.migrate} — version detection and upgrade steps.</li>
 *   <li>{@link com.danrus.bb4j.molang} — Molang expression utilities.</li>
 *   <li>{@link com.danrus.bb4j.assets} — texture/asset resolution.</li>
 * </ul>
 *
 * <h2>Lossless round-tripping</h2>
 * A core design goal is that a {@code read} &rarr; {@code write} round-trip does
 * not silently lose data. Unrecognized JSON fields are captured (top-level into
 * {@link com.danrus.bb4j.model.BbModelDocument#getRawData()}, nested into each
 * model object's {@code extra} map) and re-emitted on write.
 */
package com.danrus.bb4j;
