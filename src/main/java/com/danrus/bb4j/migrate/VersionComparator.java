package com.danrus.bb4j.migrate;

import java.util.Objects;

public class VersionComparator {
    
    public static int compare(String v1, String v2) {
        if (v1 == null && v2 == null) return 0;
        if (v1 == null) return -1;
        if (v2 == null) return 1;
        
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        
        int maxLen = Math.max(parts1.length, parts2.length);
        
        for (int i = 0; i < maxLen; i++) {
            String p1 = i < parts1.length ? parts1[i] : "0";
            String p2 = i < parts2.length ? parts2[i] : "0";
            
            p1 = removeBetaSuffix(p1);
            p2 = removeBetaSuffix(p2);
            
            int n1 = parseIntOrZero(p1);
            int n2 = parseIntOrZero(p2);
            
            if (n1 != n2) {
                return Integer.compare(n1, n2);
            }
        }
        
        boolean hasBeta1 = containsBeta(v1);
        boolean hasBeta2 = containsBeta(v2);
        
        if (hasBeta1 && !hasBeta2) return 1;
        if (!hasBeta1 && hasBeta2) return -1;
        
        if (hasBeta1 && hasBeta2) {
            String beta1 = extractBeta(v1);
            String beta2 = extractBeta(v2);
            return compare(beta1, beta2);
        }
        
        return 0;
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
    
    private static String removeBetaSuffix(String version) {
        int idx = version.toLowerCase().indexOf("-beta");
        if (idx > 0) {
            return version.substring(0, idx);
        }
        return version;
    }
    
    private static boolean containsBeta(String version) {
        return version.toLowerCase().contains("-beta");
    }
    
    private static String extractBeta(String version) {
        int idx = version.toLowerCase().indexOf("-beta.");
        if (idx >= 0) {
            return version.substring(idx + 6);
        }
        return "0";
    }
    
    private static int parseIntOrZero(String s) {
        try {
            return Integer.parseInt(s.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
