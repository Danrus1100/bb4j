package com.danrus.bb4j.assets;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataUrl {
    private static final Pattern DATA_URL_PATTERN = Pattern.compile(
        "data:([^;,]+)?(;base64)?,(.+)"
    );

    private final String mimeType;
    private final boolean base64;
    private final String data;
    private final byte[] decodedData;

    private DataUrl(String mimeType, boolean base64, String data, byte[] decodedData) {
        this.mimeType = mimeType;
        this.base64 = base64;
        this.data = data;
        this.decodedData = decodedData;
    }

    public static DataUrl parse(String dataUrl) {
        if (dataUrl == null || !dataUrl.startsWith("data:")) {
            return null;
        }

        Matcher matcher = DATA_URL_PATTERN.matcher(dataUrl);
        if (!matcher.matches()) {
            return null;
        }

        String mimeType = matcher.group(1);
        boolean base64 = ";base64".equals(matcher.group(2));
        String data = matcher.group(3);

        byte[] decodedData;
        if (base64) {
            try {
                decodedData = Base64.getDecoder().decode(data);
            } catch (IllegalArgumentException e) {
                return null;
            }
        } else {
            try {
                decodedData = java.net.URLDecoder.decode(data).getBytes(java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception e) {
                return null;
            }
        }

        return new DataUrl(mimeType, base64, data, decodedData);
    }

    public static boolean isDataUrl(String str) {
        return str != null && str.startsWith("data:");
    }

    public String getMimeType() {
        return mimeType;
    }

    public boolean isBase64() {
        return base64;
    }

    public String getData() {
        return data;
    }

    public byte[] getDecodedData() {
        return decodedData;
    }

    public String getDecodedString() {
        if (decodedData == null) {
            return null;
        }
        return new String(decodedData, java.nio.charset.StandardCharsets.UTF_8);
    }

    public static DataUrl create(String mimeType, byte[] data) {
        String encoded = Base64.getEncoder().encodeToString(data);
        return new DataUrl(mimeType, true, encoded, data);
    }

    public static DataUrl create(String mimeType, String text) {
        byte[] data = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return create(mimeType, data);
    }

    @Override
    public String toString() {
        String prefix = "data:" + (mimeType != null ? mimeType : "") + (base64 ? ";base64" : "") + ",";
        return prefix + data;
    }
}
