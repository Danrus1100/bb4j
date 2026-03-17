package com.danrus.bb4j.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtil {
    
    private static final String FORWARD_SLASH = "/";
    private static final String BACK_SLASH = "\\";
    
    public static String normalize(String path) {
        if (path == null) {
            return null;
        }
        return path.replace(BACK_SLASH, FORWARD_SLASH);
    }
    
    public static String join(String... parts) {
        if (parts == null || parts.length == 0) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        boolean first = true;
        
        for (String part : parts) {
            if (part == null || part.isEmpty()) {
                continue;
            }
            
            if (!first) {
                if (!result.toString().endsWith(FORWARD_SLASH)) {
                    result.append(FORWARD_SLASH);
                }
            }
            
            String normalizedPart = normalize(part);
            if (normalizedPart.startsWith(FORWARD_SLASH)) {
                normalizedPart = normalizedPart.substring(1);
            }
            result.append(normalizedPart);
            first = false;
        }
        
        return result.toString();
    }
    
    public static String getFileName(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        String normalized = normalize(path);
        int lastSlash = normalized.lastIndexOf(FORWARD_SLASH);
        if (lastSlash < 0) {
            return normalized;
        }
        if (lastSlash == normalized.length() - 1) {
            return "";
        }
        return normalized.substring(lastSlash + 1);
    }
    
    public static String getExtension(String path) {
        String fileName = getFileName(path);
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(lastDot + 1);
    }
    
    public static String getBaseName(String path) {
        String fileName = getFileName(path);
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0) {
            return fileName;
        }
        return fileName.substring(0, lastDot);
    }
    
    public static String getParent(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        String normalized = normalize(path);
        int lastSlash = normalized.lastIndexOf(FORWARD_SLASH);
        if (lastSlash < 0) {
            return "";
        }
        return normalized.substring(0, lastSlash);
    }
    
    public static String resolve(String base, String relative) {
        if (base == null) {
            return normalize(relative);
        }
        if (relative == null) {
            return normalize(base);
        }
        
        String normalizedBase = normalize(base);
        String normalizedRelative = normalize(relative);
        
        if (normalizedRelative.startsWith(FORWARD_SLASH)) {
            return normalizedRelative;
        }
        
        return normalizedBase + FORWARD_SLASH + normalizedRelative;
    }
    
    public static boolean isAbsolute(String path) {
        if (path == null) {
            return false;
        }
        if (path.startsWith(FORWARD_SLASH)) {
            return true;
        }
        if (path.length() >= 2 && path.charAt(1) == ':') {
            return true;
        }
        return false;
    }
    
    public static String[] split(String path) {
        if (path == null) {
            return new String[0];
        }
        String normalized = normalize(path);
        return normalized.split(FORWARD_SLASH);
    }
}
