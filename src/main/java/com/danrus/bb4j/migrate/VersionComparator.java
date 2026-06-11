package com.danrus.bb4j.migrate;

import com.danrus.bb4j.model.meta.FormatVersion;

/**
 * Compares Blockbench {@code major.minor[-beta.x]} version strings.
 *
 * <p>Mirrors Blockbench's {@code VersionUtil.compare}: parsing separates the
 * {@code -beta.x} suffix from the main version, and a beta build sorts
 * <em>before</em> its corresponding release (e.g. {@code 5.0-beta.1 < 5.0}).
 * Delegates to {@link FormatVersion} so both share one correct implementation.
 */
public class VersionComparator {

    /**
     * @return {@code 0} if equal, a negative number if {@code v1 < v2}, a
     *         positive number if {@code v1 > v2}. {@code null} sorts first.
     */
    public static int compare(String v1, String v2) {
        if (v1 == null && v2 == null) return 0;
        if (v1 == null) return -1;
        if (v2 == null) return 1;
        return new FormatVersion(v1).compareTo(new FormatVersion(v2));
    }

    public static boolean isGreaterThan(String v1, String v2) {
        return compare(v1, v2) > 0;
    }

    public static boolean isLessThan(String v1, String v2) {
        return compare(v1, v2) < 0;
    }

    public static boolean isEqual(String v1, String v2) {
        return compare(v1, v2) == 0;
    }

    public static boolean isGreaterOrEqual(String v1, String v2) {
        return compare(v1, v2) >= 0;
    }

    public static boolean isLessOrEqual(String v1, String v2) {
        return compare(v1, v2) <= 0;
    }
}
