package com.danrus.bb4j.api;

/**
 * What to do when a document declares a format version outside the
 * {@linkplain com.danrus.bb4j.migrate.SupportedVersions supported range}.
 *
 * <p>With {@link #STRICT} and {@link #WARN}, migrations still run on read; the
 * policy only governs the reaction to an <em>unsupported</em> version.
 */
public enum VersionPolicy {
    /** Throw a {@link BbException} for an unsupported version. */
    STRICT,

    /**
     * Proceed, but attach a {@link com.danrus.bb4j.model.BbModelDocument.Warning}
     * describing the unsupported version. This is the default.
     */
    WARN,

    /** Silently proceed and skip migration entirely. */
    IGNORE
}
