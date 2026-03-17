package com.danrus.bb4j.assets;

import com.danrus.bb4j.model.texture.Texture;

import java.io.InputStream;

public class TextureContentLoader {
    private AssetResolver resolver;
    private PathPolicy pathPolicy;

    public TextureContentLoader() {
        this.pathPolicy = PathPolicy.strict();
    }

    public TextureContentLoader(AssetResolver resolver) {
        this.resolver = resolver;
        this.pathPolicy = PathPolicy.strict();
    }

    public TextureContentLoader(AssetResolver resolver, PathPolicy pathPolicy) {
        this.resolver = resolver;
        this.pathPolicy = pathPolicy;
    }

    public InputStream loadTexture(Texture texture) throws AssetResolver.AssetNotFoundException {
        if (texture == null) {
            return null;
        }

        if (texture.getSource() != null && DataUrl.isDataUrl(texture.getSource())) {
            return DataUrlDecoder.decodeToStream(texture.getSource());
        }

        String path = resolveTexturePath(texture);
        if (path != null && resolver != null) {
            return resolver.open(path);
        }

        return null;
    }

    public String resolveTexturePath(Texture texture) {
        if (texture == null) {
            return null;
        }

        if (texture.getPath() != null) {
            return texture.getPath();
        }

        if (texture.getRelativePath() != null) {
            return resolveRelativePath(texture.getRelativePath());
        }

        return null;
    }

    private String resolveRelativePath(String relativePath) {
        if (pathPolicy == null) {
            return relativePath;
        }

        if (!pathPolicy.isAllowParentReferences() && relativePath.contains("..")) {
            return null;
        }

        if (pathPolicy.isNormalizePaths()) {
            return com.danrus.bb4j.util.PathUtil.normalize(relativePath);
        }

        return relativePath;
    }

    public boolean textureExists(Texture texture) {
        if (texture == null) {
            return false;
        }

        if (texture.getSource() != null && DataUrl.isDataUrl(texture.getSource())) {
            return true;
        }

        String path = resolveTexturePath(texture);
        return path != null && resolver != null && resolver.exists(path);
    }

    public AssetResolver getResolver() {
        return resolver;
    }

    public void setResolver(AssetResolver resolver) {
        this.resolver = resolver;
    }

    public PathPolicy getPathPolicy() {
        return pathPolicy;
    }

    public void setPathPolicy(PathPolicy pathPolicy) {
        this.pathPolicy = pathPolicy;
    }
}
