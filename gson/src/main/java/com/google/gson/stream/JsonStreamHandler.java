package com.google.gson.stream;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class JsonStreamHandler implements AutoCloseable {
    private final JsonReader reader;
    private final JsonWriter writer;

    public JsonStreamHandler(Reader input, Writer output) {
        this.reader = new JsonReader(input);
        this.writer = new JsonWriter(output);
    }

    @Override
    public void close() throws IOException {
        try {
            reader.close();
        } finally {
            writer.close();
        }
    }
}
