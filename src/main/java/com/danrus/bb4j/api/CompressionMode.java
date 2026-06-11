package com.danrus.bb4j.api;

/**
 * How a {@code .bbmodel} payload is (de)compressed.
 *
 * <p>Blockbench can store projects either as plain JSON or as LZ-UTF8 compressed
 * text. This enum selects how bb4j treats the bytes.
 */
public enum CompressionMode {
    AUTO,
    JSON,
    LZUTF8
}
