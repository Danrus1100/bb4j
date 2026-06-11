package com.danrus.bb4j.api;

import com.danrus.bb4j.io.BbModelReader;
import com.danrus.bb4j.io.BbModelWriter;
import com.danrus.bb4j.model.BbModelDocument;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;

/**
 * Static facade for reading and writing Blockbench {@code .bbmodel} files — the
 * single entry point most callers need.
 *
 * <p>Both {@code read} and {@code write} are overloaded for {@code String},
 * {@code File}, {@code Path}, {@code InputStream}/{@code OutputStream}, and
 * {@code Reader}/{@code Writer}. Each accepts an optional
 * {@link ReadOptions}/{@link WriteOptions}; the no-options overloads use the
 * defaults. Reading auto-detects LZ-UTF8 compression and runs version migrations
 * unless {@link VersionPolicy#IGNORE} is selected.
 *
 * <pre>{@code
 * BbModelDocument doc = BbModel.read(Path.of("model.bbmodel"));
 * BbModel.write(doc, Path.of("out.bbmodel"));
 * }</pre>
 *
 * <p>All failures are reported as {@link BbException}. This class is not
 * instantiable.
 *
 * @see BbModelDocument
 * @see ReadOptions
 * @see WriteOptions
 */
public class BbModel {

    private BbModel() {}

    /** Reads a document from a JSON (or LZ-UTF8) string using default options. */
    public static BbModelDocument read(String json) {
        return read(json, ReadOptions.builder());
    }

    /** Reads a document from a JSON (or LZ-UTF8) string using the given options. */
    public static BbModelDocument read(String json, ReadOptions options) {
        return BbModelReader.read(json, options);
    }

    public static BbModelDocument read(File file) {
        return read(file, ReadOptions.builder());
    }

    public static BbModelDocument read(File file, ReadOptions options) {
        return BbModelReader.read(file, options);
    }

    public static BbModelDocument read(Path path) {
        return read(path, ReadOptions.builder());
    }

    public static BbModelDocument read(Path path, ReadOptions options) {
        return BbModelReader.read(path, options);
    }

    public static BbModelDocument read(InputStream inputStream) {
        return read(inputStream, ReadOptions.builder());
    }

    public static BbModelDocument read(InputStream inputStream, ReadOptions options) {
        return BbModelReader.read(inputStream, options);
    }

    public static BbModelDocument read(Reader reader) {
        return read(reader, ReadOptions.builder());
    }

    public static BbModelDocument read(Reader reader, ReadOptions options) {
        return BbModelReader.read(reader, options);
    }

    /** Serializes a document to a JSON string using default write options. */
    public static String write(BbModelDocument document) {
        return write(document, WriteOptions.builder());
    }

    /** Serializes a document to a string (JSON or LZ-UTF8) using the given options. */
    public static String write(BbModelDocument document, WriteOptions options) {
        return BbModelWriter.write(document, options);
    }

    public static void write(BbModelDocument document, File file) {
        write(document, file, WriteOptions.builder());
    }

    public static void write(BbModelDocument document, File file, WriteOptions options) {
        BbModelWriter.write(document, file, options);
    }

    public static void write(BbModelDocument document, Path path) {
        write(document, path, WriteOptions.builder());
    }

    public static void write(BbModelDocument document, Path path, WriteOptions options) {
        BbModelWriter.write(document, path, options);
    }

    public static void write(BbModelDocument document, OutputStream outputStream) {
        write(document, outputStream, WriteOptions.builder());
    }

    public static void write(BbModelDocument document, OutputStream outputStream, WriteOptions options) {
        BbModelWriter.write(document, outputStream, options);
    }

    public static void write(BbModelDocument document, Writer writer) {
        write(document, writer, WriteOptions.builder());
    }

    public static void write(BbModelDocument document, Writer writer, WriteOptions options) {
        BbModelWriter.write(document, writer, options);
    }
}
