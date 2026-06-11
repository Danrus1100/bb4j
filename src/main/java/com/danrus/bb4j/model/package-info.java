/**
 * The in-memory representation of a whole {@code .bbmodel} project.
 *
 * <p>{@link com.danrus.bb4j.model.BbModelDocument} is the root aggregate. Its
 * top-level collections (textures, elements, groups, outliner, animations, etc.)
 * are typed lists; smaller structures (Group, AnimationController, ReferenceImage,
 * Collection, ExportOptions, Warning) are nested static classes on it.
 *
 * <p>Sub-packages model the major concerns:
 * <ul>
 *   <li>{@link com.danrus.bb4j.model.geometry} — elements (cubes, meshes) and faces.</li>
 *   <li>{@link com.danrus.bb4j.model.animation} — animations, animators, keyframes.</li>
 *   <li>{@link com.danrus.bb4j.model.outliner} — the scene tree.</li>
 *   <li>{@link com.danrus.bb4j.model.texture} — textures and texture groups.</li>
 *   <li>{@link com.danrus.bb4j.model.meta} — header/format metadata.</li>
 *   <li>{@link com.danrus.bb4j.model.project} — editor/project-level state.</li>
 * </ul>
 *
 * <h2>Conventions</h2>
 * <ul>
 *   <li>Coordinate/vector fields are boxed and nullable ({@code Double[]} /
 *       {@code Integer[]}) so that "absent" is distinguishable from "zero" during
 *       round-trips.</li>
 *   <li>Most nested classes carry an {@code extra} {@code Map<String, Object>}
 *       holding unrecognized JSON fields, which the writer re-emits.</li>
 * </ul>
 */
package com.danrus.bb4j.model;
