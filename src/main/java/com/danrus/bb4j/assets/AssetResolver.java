package com.danrus.bb4j.assets;

import java.io.InputStream;
import java.net.URI;

public interface AssetResolver {
    boolean exists(String path);
    
    InputStream open(String path) throws AssetNotFoundException;
    
    default InputStream open(URI uri) throws AssetNotFoundException {
        return open(uri.toString());
    }
    
    default String resolve(String basePath, String relativePath) {
        if (relativePath == null) {
            return basePath;
        }
        if (basePath == null) {
            return relativePath;
        }
        if (relativePath.startsWith("/") || relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath;
        }
        if (!basePath.endsWith("/")) {
            return basePath + "/" + relativePath;
        }
        return basePath + relativePath;
    }
    
    public static class AssetNotFoundException extends Exception {
        public AssetNotFoundException(String message) {
            super(message);
        }
        
        public AssetNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
