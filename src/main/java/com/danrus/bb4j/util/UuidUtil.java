package com.danrus.bb4j.util;

import java.util.UUID;
import java.util.regex.Pattern;

public class UuidUtil {
    
    private static final Pattern UUID_PATTERN = Pattern.compile(
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );
    
    public static String generate() {
        return UUID.randomUUID().toString();
    }
    
    public static UUID parseUuid(String uuid) {
        if (uuid == null) {
            return null;
        }
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    public static boolean isValid(String uuid) {
        if (uuid == null) {
            return false;
        }
        return UUID_PATTERN.matcher(uuid).matches();
    }
    
    public static String shorten(String uuid) {
        if (uuid == null) {
            return null;
        }
        UUID parsed = parseUuid(uuid);
        if (parsed == null) {
            return uuid;
        }
        
        long mostSigBits = parsed.getMostSignificantBits();
        long leastSigBits = parsed.getLeastSignificantBits();
        
        return String.format("%08x%08x", mostSigBits, leastSigBits);
    }
    
    public static String expand(String shortUuid) {
        if (shortUuid == null) {
            return null;
        }
        
        String cleaned = shortUuid.replace("-", "").toLowerCase();
        if (cleaned.length() != 32) {
            return null;
        }
        
        try {
            long mostSigBits = Long.parseUnsignedLong(cleaned.substring(0, 16), 16);
            long leastSigBits = Long.parseUnsignedLong(cleaned.substring(16), 16);
            return new UUID(mostSigBits, leastSigBits).toString();
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public static String emptyToNull(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return null;
        }
        return uuid;
    }
    
    public static String nullToEmpty(String uuid) {
        return uuid == null ? "" : uuid;
    }
}
