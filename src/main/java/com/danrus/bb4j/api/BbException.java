package com.danrus.bb4j.api;

public class BbException extends RuntimeException {
    private final String errorCode;

    public BbException(String message) {
        super(message);
        this.errorCode = null;
    }

    public BbException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    public BbException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BbException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public static BbException invalidFormat(String message) {
        return new BbException("INVALID_FORMAT", message);
    }

    public static BbException unsupportedVersion(String version) {
        return new BbException("UNSUPPORTED_VERSION", "Unsupported format version: " + version);
    }

    public static BbException parseError(String message) {
        return new BbException("PARSE_ERROR", message);
    }

    public static BbException ioError(String message, Throwable cause) {
        return new BbException("IO_ERROR", message, cause);
    }
}
