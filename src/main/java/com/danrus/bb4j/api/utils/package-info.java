/**
 * High-level, read-oriented helpers built on top of a parsed
 * {@link com.danrus.bb4j.model.BbModelDocument}.
 *
 * <p>{@link com.danrus.bb4j.api.utils.ModelInfo#fromDocument} is an aggregator
 * that composes the individual helpers
 * ({@link com.danrus.bb4j.api.utils.ElementUtils},
 * {@link com.danrus.bb4j.api.utils.AnimationUtils},
 * {@link com.danrus.bb4j.api.utils.TextureUtils},
 * {@link com.danrus.bb4j.api.utils.OutlinerUtils}) for convenient queries. Each
 * helper also exposes a {@code forDocument(doc)} factory.
 *
 * <p>{@link com.danrus.bb4j.api.utils.TransformUtils} evaluates element/group
 * transforms at a given animation time (with keyframe interpolation, Molang
 * evaluation, and blending), and {@link com.danrus.bb4j.api.utils.RenderUtils}
 * builds on it.
 */
package com.danrus.bb4j.api.utils;
