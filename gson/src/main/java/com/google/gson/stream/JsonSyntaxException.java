package com.google.gson.stream;

public class JsonSyntaxException extends JsonException {
    public JsonSyntaxException(String message, int line, int column) {
        super(message, line, column);
    }
}
