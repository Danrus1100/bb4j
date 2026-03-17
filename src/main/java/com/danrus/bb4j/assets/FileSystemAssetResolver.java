package com.danrus.bb4j.assets;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSystemAssetResolver implements AssetResolver {
    private Path basePath;

    public FileSystemAssetResolver() {}

    public FileSystemAssetResolver(String basePath) {
        this.basePath = basePath != null ? Paths.get(basePath) : null;
    }

    public FileSystemAssetResolver(Path basePath) {
        this.basePath = basePath;
    }

    @Override
    public boolean exists(String path) {
        return resolvePath(path) != null && Files.exists(resolvePath(path));
    }

    @Override
    public InputStream open(String path) throws AssetNotFoundException {
        Path resolvedPath = resolvePath(path);
        if (resolvedPath == null || !Files.exists(resolvedPath)) {
            throw new AssetNotFoundException("Asset not found: " + path);
        }
        try {
            return Files.newInputStream(resolvedPath);
        } catch (IOException e) {
            throw new AssetNotFoundException("Failed to open asset: " + path, e);
        }
    }

    private Path resolvePath(String path) {
        if (path == null) {
            return null;
        }
        
        if (basePath == null) {
            return Paths.get(path);
        }
        
        return basePath.resolve(path).normalize();
    }

    public Path getBasePath() {
        return basePath;
    }

    public void setBasePath(Path basePath) {
        this.basePath = basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath != null ? Paths.get(basePath) : null;
    }
}
