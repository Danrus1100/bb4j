package com.danrus.bb4j.assets;

public class PathPolicy {
    private boolean allowParentReferences;
    private boolean normalizePaths;
    private String basePath;

    private PathPolicy() {}

    public static PathPolicy strict() {
        PathPolicy policy = new PathPolicy();
        policy.allowParentReferences = false;
        policy.normalizePaths = true;
        return policy;
    }

    public static PathPolicy lenient() {
        PathPolicy policy = new PathPolicy();
        policy.allowParentReferences = true;
        policy.normalizePaths = false;
        return policy;
    }

    public PathPolicy allowParentReferences(boolean allow) {
        this.allowParentReferences = allow;
        return this;
    }

    public PathPolicy normalizePaths(boolean normalize) {
        this.normalizePaths = normalize;
        return this;
    }

    public PathPolicy basePath(String basePath) {
        this.basePath = basePath;
        return this;
    }

    public boolean isAllowParentReferences() {
        return allowParentReferences;
    }

    public boolean isNormalizePaths() {
        return normalizePaths;
    }

    public String getBasePath() {
        return basePath;
    }
}
