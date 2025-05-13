package com.google.gson.stream;

public class JsonParseException extends JsonException {
    public JsonParseException(String message, int line, int column) {
        super(message, line, column);
    }
}
