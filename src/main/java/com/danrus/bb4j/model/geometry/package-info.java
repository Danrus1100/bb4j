/**
 * Geometry: the shapes that make up a model.
 *
 * <p>{@link com.danrus.bb4j.model.geometry.Element} is the common base, with two
 * concrete subtypes selected by the JSON {@code type} field:
 * {@link com.danrus.bb4j.model.geometry.CubeElement} (box geometry with six named
 * {@link com.danrus.bb4j.model.geometry.Face}s and {@link com.danrus.bb4j.model.geometry.Uv}
 * coordinates) and {@link com.danrus.bb4j.model.geometry.MeshElement} (free-form
 * vertices and polygonal faces).
 */
package com.danrus.bb4j.model.geometry;
