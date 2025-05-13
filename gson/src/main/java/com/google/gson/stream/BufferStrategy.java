package com.google.gson.stream;

import java.io.IOException;

public interface BufferStrategy {
    char[] getBuffer();
    int read() throws IOException;
    void fill() throws IOException;
}
