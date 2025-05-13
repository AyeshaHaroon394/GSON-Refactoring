package com.google.gson.stream;

import java.nio.charset.Charset;

public class JsonReaderConfig {
    private boolean lenient;
    private int maxDepth;
    private boolean allowNaN;
    private Charset charset;

    public static class Builder {
        private final JsonReaderConfig config = new JsonReaderConfig();

        public Builder setLenient(boolean lenient) {
            config.lenient = lenient;
            return this;
        }

        public Builder setMaxDepth(int maxDepth) {
            config.maxDepth = maxDepth;
            return this;
        }

        public JsonReaderConfig build() {
            return config;
        }
    }
}