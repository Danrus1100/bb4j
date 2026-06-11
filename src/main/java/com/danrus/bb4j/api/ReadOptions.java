package com.danrus.bb4j.api;

/**
 * Options controlling how a {@code .bbmodel} is read.
 *
 * <p>Built fluently via {@link #builder()}; each setter returns {@code this}.
 * Defaults: {@link CompressionMode#AUTO} compression,
 * {@link VersionPolicy#WARN} version policy, and extra-field preservation on.
 *
 * <pre>{@code
 * ReadOptions opts = ReadOptions.builder()
 *         .versionPolicy(VersionPolicy.STRICT)
 *         .preserveExtraFields(true);
 * }</pre>
 */
public class ReadOptions {
    private CompressionMode compressionMode = CompressionMode.AUTO;
    private VersionPolicy versionPolicy = VersionPolicy.WARN;
    private boolean preserveExtraFields = true;

    private ReadOptions() {}

    public static ReadOptions builder() {
        return new ReadOptions();
    }

    public ReadOptions compressionMode(CompressionMode compressionMode) {
        this.compressionMode = compressionMode;
        return this;
    }

    public ReadOptions versionPolicy(VersionPolicy versionPolicy) {
        this.versionPolicy = versionPolicy;
        return this;
    }

    /**
     * Whether unrecognized top-level fields are captured into
     * {@link com.danrus.bb4j.model.BbModelDocument#getRawData()} (and nested
     * unknown fields into each object's {@code extra} map) so they survive a
     * round-trip. Enabled by default; disable to discard unknown data.
     */
    public ReadOptions preserveExtraFields(boolean preserveExtraFields) {
        this.preserveExtraFields = preserveExtraFields;
        return this;
    }

    public CompressionMode getCompressionMode() {
        return compressionMode;
    }

    public VersionPolicy getVersionPolicy() {
        return versionPolicy;
    }

    public boolean isPreserveExtraFields() {
        return preserveExtraFields;
    }
}
