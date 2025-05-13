package com.google.gson.gsonExtension;

import com.google.gson.JsonNull;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;

public final class GsonSerializer {
    private final GsonConfiguration config;
    private final GsonTypeAdapterManager adapterManager;

    public GsonSerializer(GsonConfiguration config, GsonTypeAdapterManager adapterManager) {
        this.config = config;
        this.adapterManager = adapterManager;
    }

    public String toJson(Object src) throws IOException {
        if (src == null) {
            return toJson(JsonNull.INSTANCE);
        }
        return toJson(src, src.getClass());
    }

    public String toJson(Object src, Type typeOfSrc) throws IOException {
        StringWriter writer = new StringWriter();
        toJson(src, typeOfSrc, writer);
        return writer.toString();
    }

    public void toJson(Object src, Type typeOfSrc, Writer writer) throws IOException {
        JsonWriter jsonWriter = newJsonWriter(writer);
        toJson(src, typeOfSrc, jsonWriter);
        checkStrictness(jsonWriter);
    }

    @SuppressWarnings("unchecked")
    public void toJson(Object src, Type typeOfSrc, JsonWriter writer) throws IOException {
        TypeAdapter<?> adapter = adapterManager.getAdapter(TypeToken.get(typeOfSrc));
        ((TypeAdapter<Object>) adapter).write(writer, src);
    }

    private JsonWriter newJsonWriter(Writer writer) {
        JsonWriter jsonWriter = new JsonWriter(writer);
        jsonWriter.setFormattingStyle(config.formattingStyle);
        jsonWriter.setStrictness(config.strictness);
        jsonWriter.setSerializeNulls(config.serializeNulls);
        jsonWriter.setHtmlSafe(config.htmlSafe);
        return jsonWriter;
    }

    private void checkStrictness(JsonWriter writer) {
        if (config.strictness != null) {
            writer.getStrictness();
        }
    }
}