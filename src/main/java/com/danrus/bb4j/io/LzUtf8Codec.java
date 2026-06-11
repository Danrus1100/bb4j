package com.danrus.bb4j.io;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

/**
 * Java port of the <a href="https://github.com/rotemdan/lzutf8.js">LZUTF8</a>
 * compression used by Blockbench for compressed {@code .bbmodel} files.
 *
 * <p>Blockbench stores a compressed project as {@code "<lz>"} followed by
 * {@code LZUTF8.compress(json, {outputEncoding: 'StorageBinaryString'})}. This
 * class reproduces that exact pipeline so bb4j can both read Blockbench's
 * compressed files and produce files Blockbench can read:
 *
 * <ol>
 *   <li>UTF-8 encode the JSON text;</li>
 *   <li>LZ77-compress it into the LZUTF8 token stream;</li>
 *   <li>pack the bytes into a {@code StorageBinaryString} (15-bit packing, with
 *       the null character {@code U+0000} stored as {@code U+8002}).</li>
 * </ol>
 *
 * <p>The decompressor is a faithful port of the reference implementation. The
 * compressor is an independent greedy LZ77 matcher that emits a valid token
 * stream (length 4..31, distance 1..32767); its output is not byte-identical to
 * the reference compressor but decompresses to exactly the same bytes.
 */
public class LzUtf8Codec {
    private static final String LZ_PREFIX = "<lz>";

    /** The null character {@code U+0000}. */
    private static final char NULL_CHAR = (char) 0x0000;
    /** {@code U+0000} is stored as this character so the packed string has no nulls. */
    private static final char NULL_REPLACEMENT = (char) 0x8002;

    private static final int MIN_MATCH = 4;
    private static final int MAX_MATCH = 31;
    private static final int MAX_DISTANCE = 32767;
    /** Cap on candidates examined per hash bucket, to bound compression time. */
    private static final int MAX_CHAIN = 64;

    public static String compress(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        byte[] utf8 = input.getBytes(StandardCharsets.UTF_8);
        byte[] compressed = lz77Compress(utf8);
        return LZ_PREFIX + encodeStorageBinaryString(compressed);
    }

    public static String decompress(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        if (!input.startsWith(LZ_PREFIX)) {
            return input;
        }
        String payload = input.substring(LZ_PREFIX.length());
        byte[] compressed = decodeStorageBinaryString(payload);
        byte[] utf8 = lz77Decompress(compressed);
        return new String(utf8, StandardCharsets.UTF_8);
    }

    /** Decode a raw {@code StorageBinaryString} (without the {@code <lz>} prefix). */
    public static String decompressFromStorageBinaryString(String input) {
        return decompress(LZ_PREFIX + input);
    }

    /** Encode to a raw {@code StorageBinaryString} (without the {@code <lz>} prefix). */
    public static String compressToStorageBinaryString(String input) {
        String compressed = compress(input);
        return compressed == null ? null : compressed.substring(LZ_PREFIX.length());
    }

    // ------------------------------------------------------------------
    // LZ77 compression (greedy match finder producing a valid token stream)
    // ------------------------------------------------------------------

    private static byte[] lz77Compress(byte[] input) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(input.length);
        Map<Integer, ArrayDeque<Integer>> buckets = new HashMap<>();
        int n = input.length;
        int pos = 0;

        while (pos < n) {
            int bestLen = 0;
            int bestDist = 0;

            if (pos + MIN_MATCH <= n) {
                int hash = hash4(input, pos);
                ArrayDeque<Integer> chain = buckets.get(hash);
                if (chain != null) {
                    int examined = 0;
                    for (int candidate : chain) { // most-recent first
                        if (++examined > MAX_CHAIN) break;
                        int dist = pos - candidate;
                        if (dist > MAX_DISTANCE) break; // older entries are farther still
                        int len = matchLength(input, candidate, pos, n);
                        if (len > bestLen) {
                            bestLen = len;
                            bestDist = dist;
                            if (bestLen >= MAX_MATCH) break;
                        }
                    }
                }
            }

            if (bestLen >= MIN_MATCH) {
                outputPointer(out, bestLen, bestDist);
                int end = pos + bestLen;
                for (; pos < end; pos++) {
                    if (pos + MIN_MATCH <= n) addToBucket(buckets, hash4(input, pos), pos);
                }
            } else {
                out.write(input[pos] & 0xFF);
                if (pos + MIN_MATCH <= n) addToBucket(buckets, hash4(input, pos), pos);
                pos++;
            }
        }
        return out.toByteArray();
    }

    private static int matchLength(byte[] input, int candidate, int pos, int n) {
        int len = 0;
        while (len < MAX_MATCH && pos + len < n && input[candidate + len] == input[pos + len]) {
            len++;
        }
        return len;
    }

    private static void addToBucket(Map<Integer, ArrayDeque<Integer>> buckets, int hash, int pos) {
        ArrayDeque<Integer> chain = buckets.computeIfAbsent(hash, k -> new ArrayDeque<>());
        chain.addFirst(pos);
        if (chain.size() > MAX_CHAIN) {
            chain.removeLast();
        }
    }

    private static int hash4(byte[] input, int p) {
        return ((input[p] & 0xFF) * 7880599
                + (input[p + 1] & 0xFF) * 39601
                + (input[p + 2] & 0xFF) * 199
                + (input[p + 3] & 0xFF));
    }

    private static void outputPointer(ByteArrayOutputStream out, int length, int distance) {
        if (distance < 128) {
            out.write(192 | length);
            out.write(distance);
        } else {
            out.write(224 | length);
            out.write(distance >>> 8);
            out.write(distance & 255);
        }
    }

    // ------------------------------------------------------------------
    // LZ77 decompression (faithful port of LZUTF8 Decompressor.decompressBlock)
    // ------------------------------------------------------------------

    private static byte[] lz77Decompress(byte[] input) {
        byte[] output = new byte[Math.max(input.length * 4, 1024)];
        int outputPosition = 0;
        int inputLength = input.length;

        for (int readPosition = 0; readPosition < inputLength; readPosition++) {
            int inputValue = input[readPosition] & 0xFF;

            if (inputValue >>> 6 != 3) {
                if (outputPosition == output.length) output = grow(output);
                output[outputPosition++] = (byte) inputValue;
                continue;
            }

            int sequenceLengthIdentifier = inputValue >>> 5;

            // Not enough trailing bytes to form a full pointer token: stop (the
            // reference defers these to the next block; for a complete stream this
            // never drops real data because JSON never ends with a lone marker byte).
            if (readPosition == inputLength - 1
                    || (readPosition == inputLength - 2 && sequenceLengthIdentifier == 7)) {
                break;
            }

            if ((input[readPosition + 1] & 0xFF) >>> 7 == 1) {
                // Next byte is a UTF-8 continuation byte: this is a raw byte, not a pointer.
                if (outputPosition == output.length) output = grow(output);
                output[outputPosition++] = (byte) inputValue;
            } else {
                int matchLength = inputValue & 31;
                int matchDistance;
                if (sequenceLengthIdentifier == 6) {
                    matchDistance = input[readPosition + 1] & 0xFF;
                    readPosition += 1;
                } else {
                    matchDistance = ((input[readPosition + 1] & 0xFF) << 8) | (input[readPosition + 2] & 0xFF);
                    readPosition += 2;
                }
                int matchPosition = outputPosition - matchDistance;
                for (int offset = 0; offset < matchLength; offset++) {
                    if (outputPosition == output.length) output = grow(output);
                    output[outputPosition++] = output[matchPosition + offset];
                }
            }
        }

        byte[] result = new byte[outputPosition];
        System.arraycopy(output, 0, result, 0, outputPosition);
        return result;
    }

    private static byte[] grow(byte[] array) {
        byte[] bigger = new byte[array.length * 2];
        System.arraycopy(array, 0, bigger, 0, array.length);
        return bigger;
    }

    // ------------------------------------------------------------------
    // BinaryString / StorageBinaryString encoding (15-bit packing)
    // ------------------------------------------------------------------

    private static String encodeStorageBinaryString(byte[] input) {
        return encodeBinaryString(input).replace(NULL_CHAR, NULL_REPLACEMENT);
    }

    private static byte[] decodeStorageBinaryString(String input) {
        return decodeBinaryString(input.replace(NULL_REPLACEMENT, NULL_CHAR));
    }

    private static String encodeBinaryString(byte[] input) {
        if (input.length == 0) {
            return "";
        }
        int inputLength = input.length;
        StringBuilder out = new StringBuilder();
        int remainder = 0;
        int state = 1;
        for (int i = 0; i < inputLength; i += 2) {
            int value;
            if (i == inputLength - 1) {
                value = (input[i] & 0xFF) << 8;
            } else {
                value = ((input[i] & 0xFF) << 8) | (input[i + 1] & 0xFF);
            }
            out.append((char) ((remainder << (16 - state)) | (value >>> state)));
            remainder = value & ((1 << state) - 1);
            if (state == 15) {
                out.append((char) remainder);
                remainder = 0;
                state = 1;
            } else {
                state += 1;
            }
            if (i >= inputLength - 2) {
                out.append((char) (remainder << (16 - state)));
            }
        }
        out.append((char) (32768 | (inputLength % 2)));
        return out.toString();
    }

    private static byte[] decodeBinaryString(String input) {
        if (input.isEmpty()) {
            return new byte[0];
        }
        byte[] output = new byte[input.length() * 3];
        int outputPosition = 0;
        int remainder = 0;
        int state = 0;
        for (int i = 0; i < input.length(); i++) {
            int value = input.charAt(i);
            if (value >= 32768) {
                if (value == (32768 | 1)) {
                    outputPosition--;
                }
                state = 0;
                continue;
            }
            if (state == 0) {
                remainder = value;
            } else {
                int combined = (remainder << state) | (value >>> (15 - state));
                output[outputPosition++] = (byte) (combined >>> 8);
                output[outputPosition++] = (byte) (combined & 255);
                remainder = value & ((1 << (15 - state)) - 1);
            }
            if (state == 15) {
                state = 0;
            } else {
                state += 1;
            }
        }
        byte[] result = new byte[outputPosition];
        System.arraycopy(output, 0, result, 0, outputPosition);
        return result;
    }
}
