package com.danrus.bb4j.api;

/**
 * Options controlling how a {@link com.danrus.bb4j.model.BbModelDocument} is
 * written.
 *
 * <p>Built fluently via {@link #builder()}; each setter returns {@code this}.
 * Defaults: pretty-printed JSON output, {@link CompressionMode#AUTO} (which means
 * uncompressed JSON on write), and editor state / undo history excluded.
 *
 */
public class WriteOptions {
    private CompressionMode compressionMode = CompressionMode.AUTO;
    private boolean prettyPrint = true;
    private boolean includeEditorState = false;
    private boolean includeHistory = false;

    private WriteOptions() {}

    public static WriteOptions builder() {
        return new WriteOptions();
    }

    public WriteOptions compressionMode(CompressionMode compressionMode) {
        this.compressionMode = compressionMode;
        return this;
    }

    public WriteOptions prettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
        return this;
    }

    public WriteOptions includeEditorState(boolean includeEditorState) {
        this.includeEditorState = includeEditorState;
        return this;
    }

    public WriteOptions includeHistory(boolean includeHistory) {
        this.includeHistory = includeHistory;
        return this;
    }

    public CompressionMode getCompressionMode() {
        return compressionMode;
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public boolean isIncludeEditorState() {
        return includeEditorState;
    }

    public boolean isIncludeHistory() {
        return includeHistory;
    }
}
