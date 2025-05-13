package com.google.gson.stream;

import java.io.IOException;
import java.io.Reader;

public abstract class DefaultBufferStrategy implements BufferStrategy {
    private static final int BUFFER_SIZE = 1024;
    private final char[] buffer = new char[BUFFER_SIZE];
    private final Reader reader;

    protected DefaultBufferStrategy(Reader reader) {
        this.reader = reader;
    }

    @Override
    public char[] getBuffer() {
        return buffer;
    }

    @Override
    public void fill() throws IOException {
        // Implementation
    }
}