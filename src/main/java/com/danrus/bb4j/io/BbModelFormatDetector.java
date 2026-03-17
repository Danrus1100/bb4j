package com.danrus.bb4j.io;

public class BbModelFormatDetector {
    private static final String LZ_PREFIX = "<lz>";
    private static final int LZ_PREFIX_LENGTH = 4;

    public static boolean isCompressed(String content) {
        if (content == null || content.length() < LZ_PREFIX_LENGTH) {
            return false;
        }
        return content.startsWith(LZ_PREFIX);
    }

    public static CompressionType detect(String content) {
        if (isCompressed(content)) {
            return CompressionType.LZUTF8;
        }
        return CompressionType.JSON;
    }

    public static String extractCompressedContent(String content) {
        if (isCompressed(content)) {
            return content.substring(LZ_PREFIX_LENGTH);
        }
        return content;
    }

    public enum CompressionType {
        JSON,
        LZUTF8
    }
}
