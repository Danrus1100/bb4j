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

public class BbModel {

    private BbModel() {}

    public static BbModelDocument read(String json) {
        return read(json, ReadOptions.builder());
    }

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

    public static String write(BbModelDocument document) {
        return write(document, WriteOptions.builder());
    }

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
