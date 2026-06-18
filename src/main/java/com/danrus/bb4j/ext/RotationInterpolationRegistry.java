package com.danrus.bb4j.ext;

/**
 * Extension point for pluggable rotation-channel interpolation.
 *
 * <p>By default bb4j interpolates {@code linear} rotation keyframes along the
 * <em>shortest</em> angular path (wrapping the per-axis delta into {@code [-180, 180]}).
 * Blockbench / GeckoLib, however, interpolate plain {@code linear} rotation keyframes
 * <em>component-wise and directly</em> (no wrapping) — that's precisely why Blockbench
 * offers {@code quaternion_interpolation} as the separate shortest-path opt-in. A
 * consumer that needs Blockbench-faithful behaviour registers an {@link Interpolator}
 * here (e.g. plain linear); the rotation interpolation path then uses it for each axis.
 *
 * <p>No interpolator registered ⇒ bb4j keeps its built-in shortest-path behaviour
 * (unchanged for existing consumers).
 */
public final class RotationInterpolationRegistry {

	/** Interpolates a single rotation axis from {@code from} to {@code to} by {@code fraction} (degrees). */
	@FunctionalInterface
	public interface Interpolator {
		double interpolate(double from, double to, double fraction);
	}

	private static volatile Interpolator interpolator;

	private RotationInterpolationRegistry() {
	}

	/** Registers (or clears, with {@code null}) the global rotation interpolator. */
	public static void set(Interpolator interpolator) {
		RotationInterpolationRegistry.interpolator = interpolator;
	}

	/** The registered interpolator, or {@code null} if none (caller falls back to its default). */
	public static Interpolator get() {
		return interpolator;
	}
}
