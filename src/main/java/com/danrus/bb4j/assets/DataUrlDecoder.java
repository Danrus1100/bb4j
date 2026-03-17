package com.danrus.bb4j.assets;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

public class DataUrlDecoder {
    
    public static DataUrl decode(String dataUrl) {
        return DataUrl.parse(dataUrl);
    }
    
    public static InputStream decodeToStream(String dataUrl) {
        DataUrl dataUrlObj = decode(dataUrl);
        if (dataUrlObj == null || dataUrlObj.getDecodedData() == null) {
            return null;
        }
        return new ByteArrayInputStream(dataUrlObj.getDecodedData());
    }
    
    public static byte[] decodeToBytes(String dataUrl) {
        DataUrl dataUrlObj = decode(dataUrl);
        return dataUrlObj != null ? dataUrlObj.getDecodedData() : null;
    }
    
    public static String decodeToString(String dataUrl) {
        DataUrl dataUrlObj = decode(dataUrl);
        return dataUrlObj != null ? dataUrlObj.getDecodedString() : null;
    }
    
    public static String encode(byte[] data) {
        if (data == null) {
            return null;
        }
        return "data:application/octet-stream;base64," + Base64.getEncoder().encodeToString(data);
    }
    
    public static String encode(byte[] data, String mimeType) {
        if (data == null) {
            return null;
        }
        return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(data);
    }
    
    public static String encode(String text) {
        if (text == null) {
            return null;
        }
        byte[] data = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return encode(data);
    }
    
    public static String encode(String text, String mimeType) {
        if (text == null) {
            return null;
        }
        byte[] data = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return encode(data, mimeType);
    }
}
