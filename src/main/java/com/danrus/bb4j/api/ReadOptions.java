package com.danrus.bb4j.api;

public class ReadOptions {
    private CompressionMode compressionMode = CompressionMode.AUTO;
    private VersionPolicy versionPolicy = VersionPolicy.WARN;
    private boolean preserveExtraFields = true;
    private boolean validateStructure = true;

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

    public ReadOptions preserveExtraFields(boolean preserveExtraFields) {
        this.preserveExtraFields = preserveExtraFields;
        return this;
    }

    public ReadOptions validateStructure(boolean validateStructure) {
        this.validateStructure = validateStructure;
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

    public boolean isValidateStructure() {
        return validateStructure;
    }
}
