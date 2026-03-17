package com.danrus.bb4j.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class LzUtf8Codec {
    private static final byte[] LZ_HEADER = "<lz>".getBytes(StandardCharsets.UTF_8);

    public static String compress(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        try {
            byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
            Deflater deflater = new Deflater();
            deflater.setInput(inputBytes);
            deflater.finish();
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            deflater.end();
            
            byte[] compressed = outputStream.toByteArray();
            byte[] result = new byte[compressed.length + 1];
            result[0] = 1;
            System.arraycopy(compressed, 0, result, 1, compressed.length);
            
            String base64 = Base64.getEncoder().encodeToString(result);
            return "<lz>" + base64;
        } catch (Exception e) {
            throw new RuntimeException("Failed to compress data", e);
        }
    }

    public static String decompress(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        if (!input.startsWith("<lz>")) {
            return input;
        }
        
        try {
            String base64 = input.substring(4);
            byte[] decoded = Base64.getDecoder().decode(base64);
            
            if (decoded.length == 0 || decoded[0] != 1) {
                return input;
            }
            
            byte[] compressed = new byte[decoded.length - 1];
            System.arraycopy(decoded, 1, compressed, 0, compressed.length);
            
            Inflater inflater = new Inflater();
            inflater.setInput(compressed);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            inflater.end();
            
            return outputStream.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decompress data", e);
        }
    }

    public static String decompressFromStorageBinaryString(String input) {
        return decompress("<lz>" + input);
    }

    public static String compressToStorageBinaryString(String input) {
        String compressed = compress(input);
        return compressed.substring(4);
    }
}
