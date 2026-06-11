/**
 * Animation data.
 *
 * <p>The hierarchy is {@link com.danrus.bb4j.model.animation.Animation} &rarr;
 * {@link com.danrus.bb4j.model.animation.Animator} (one per animated bone/element,
 * keyed by UUID) &rarr; {@link com.danrus.bb4j.model.animation.Keyframe} &rarr;
 * {@link com.danrus.bb4j.model.animation.DataPoint}.
 *
 * <p>Keyframe values are stored as strings because Blockbench fields are often
 * "number-or-Molang-expression". {@link com.danrus.bb4j.model.animation.Interpolation}
 * captures the interpolation mode (linear, stepped, Catmull-Rom, Bézier), and
 * {@link com.danrus.bb4j.model.animation.AnimationBlendState} describes a weighted
 * animation contribution used when blending multiple animations.
 */
package com.danrus.bb4j.model.animation;
