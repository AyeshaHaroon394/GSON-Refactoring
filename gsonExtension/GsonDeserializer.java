package com.google.gson.gsonExtension;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;

public final class GsonDeserializer {
    private final GsonConfiguration config;
    private final GsonTypeAdapterManager adapterManager;

    public GsonDeserializer(GsonConfiguration config, GsonTypeAdapterManager adapterManager) {
        this.config = config;
        this.adapterManager = adapterManager;
    }

    public <T> T fromJson(String json, Class<T> classOfT) throws IOException {
        return fromJson(json, (Type) classOfT);
    }

    public <T> T fromJson(String json, Type typeOfT) throws IOException {
        if (json == null) {
            return null;
        }
        StringReader reader = new StringReader(json);
        return fromJson(reader, typeOfT);
    }

    public <T> T fromJson(Reader json, Type typeOfT) throws IOException {
        JsonReader jsonReader = newJsonReader(json);
        T object = fromJson(jsonReader, typeOfT);
        assertFullConsumption(jsonReader);
        return object;
    }

    @SuppressWarnings("unchecked")
    public <T> T fromJson(JsonReader reader, Type typeOfT) throws IOException {
        TypeToken<T> typeToken = (TypeToken<T>) TypeToken.get(typeOfT);
        TypeAdapter<T> adapter = adapterManager.getAdapter(typeToken);
        return adapter.read(reader);
    }

    private JsonReader newJsonReader(Reader reader) {
        JsonReader jsonReader = new JsonReader(reader);
        jsonReader.setStrictness(config.strictness);
        return jsonReader;
    }

    private void assertFullConsumption(JsonReader reader) {
        try {
            if (reader.peek() != JsonToken.END_DOCUMENT) {
                throw new JsonIOException("JSON document was not fully consumed.");
            }
        } catch (IOException e) {
            throw new JsonSyntaxException(e);
        }
    }
}