package com.google.gson.stream;

import java.io.IOException;

public interface JsonTypeAdapter<T> {
    void write(JsonWriter writer, T value) throws IOException;
    T read(JsonReader reader) throws IOException;
}

public class JsonNumberAdapter implements JsonTypeAdapter<Number> {
    @Override
    public void write(JsonWriter writer, Number value) throws IOException {
        if (value == null) {
            writer.nullValue();
            return;
        }
        writer.value(value);
    }

    @Override
    public Number read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        return reader.nextNumber();
    }
}