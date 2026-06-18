package com.danrus.bb4j.ext;

import com.danrus.bb4j.model.animation.Keyframe;

/**
 * Extension point for pluggable keyframe easing.
 *
 * <p>bb4j interpolates keyframes linearly (and catmullrom/bezier/stepped per the
 * {@code interpolation} field) but has no built-in notion of the Blockbench/GeckoLib
 * {@code easing} curves (easeInOutQuad, easeOutBounce, …). Rather than hard-code
 * those, a consumer registers an {@link EasingFunction} here; the linear
 * interpolation path then asks it to remap the {@code [0,1]} fraction using the
 * keyframe being interpolated <em>toward</em> (easing shapes the incoming segment:
 * the first keyframe carries none, each later one eases its approach). The easing name/args live in the
 * keyframe's {@code extra} map (exposed via {@link ParseExtensions}), so the
 * registered function decides how to read and apply them.
 *
 * <p>No function registered ⇒ interpolation stays linear (unchanged behaviour).
 */
public final class EasingRegistry {

	/** Remaps a linear interpolation fraction using {@code target} (the keyframe eased toward). */
	@FunctionalInterface
	public interface EasingFunction {
		double remap(Keyframe target, double fraction);
	}

	private static volatile EasingFunction function;

	private EasingRegistry() {
	}

	/** Registers (or clears, with {@code null}) the global easing function. */
	public static void set(EasingFunction function) {
		EasingRegistry.function = function;
	}

	/** Applies the registered easing to {@code fraction}, or returns it unchanged if none. */
	public static double remap(Keyframe target, double fraction) {
		EasingFunction f = function;
		return f != null && target != null ? f.remap(target, fraction) : fraction;
	}
}
