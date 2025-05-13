package com.google.gson.stream;

public class JsonException extends RuntimeException {
    private final int line;
    private final int column;

    public JsonException(String message, int line, int column) {
        super(String.format("Error at line %d, column %d: %s", line, column, message));
        this.line = line;
        this.column = column;
    }
}

