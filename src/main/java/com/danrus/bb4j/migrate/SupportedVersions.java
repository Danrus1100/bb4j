package com.danrus.bb4j.migrate;

public class SupportedVersions {
    
    public static final String MIN_SUPPORTED = "3.2";
    public static final String MAX_SUPPORTED = "5.0";
    
    public static final String VERSION_3_2 = "3.2";
    public static final String VERSION_4_0 = "4.0";
    public static final String VERSION_4_5 = "4.5";
    public static final String VERSION_4_10 = "4.10";
    public static final String VERSION_5_0 = "5.0";
    
    public static boolean isSupported(String version) {
        if (version == null) return false;
        
        int minCompare = VersionComparator.compare(version, MIN_SUPPORTED);
        int maxCompare = VersionComparator.compare(version, MAX_SUPPORTED);
        
        return minCompare >= 0 && maxCompare <= 0;
    }
    
    public static boolean needsMigration(String version) {
        if (version == null) return true;
        return VersionComparator.isLessThan(version, MAX_SUPPORTED);
    }
    
    public static String getMaxSupported() {
        return MAX_SUPPORTED;
    }
    
    public static String getMinSupported() {
        return MIN_SUPPORTED;
    }
}
