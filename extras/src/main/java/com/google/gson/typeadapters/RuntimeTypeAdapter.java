package com.google.gson.typeadapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Map;

class RuntimeTypeAdapter<R> extends TypeAdapter<R> {
  private final String typeFieldName;
  private final boolean maintainType;
  private final TypeRegistry<?> typeRegistry;
  private final Map<TypeLabel, TypeAdapter<?>> labelToDelegate;

  RuntimeTypeAdapter(
      String typeFieldName,
      boolean maintainType,
      TypeRegistry<?> typeRegistry,
      Map<TypeLabel, TypeAdapter<?>> labelToDelegate) {
    this.typeFieldName = typeFieldName;
    this.maintainType = maintainType;
    this.typeRegistry = typeRegistry;
    this.labelToDelegate = labelToDelegate;
  }

  @Override
  public R read(JsonReader in) throws IOException {
    JsonElement jsonElement = Streams.parse(in);
    JsonElement labelJsonElement = extractTypeLabel(jsonElement.getAsJsonObject());

    TypeLabel typeLabel = TypeLabel.of(labelJsonElement.getAsString());
    TypeAdapter<R> delegate = getDelegateAdapter(typeLabel);

    return delegate.fromJsonTree(jsonElement);
  }

  private JsonElement extractTypeLabel(JsonObject jsonObject) {
    return getJsonElement(jsonObject, maintainType, typeFieldName);
  }

  static JsonElement getJsonElement(
      JsonObject jsonObject, boolean maintainType, String typeFieldName) {
    JsonElement labelJsonElement;
    if (maintainType) {
      labelJsonElement = jsonObject.get(typeFieldName);
    } else {
      labelJsonElement = jsonObject.remove(typeFieldName);
    }

    if (labelJsonElement == null) {
      throw new JsonParseException("Missing type field: " + typeFieldName);
    }
    return labelJsonElement;
  }

  @SuppressWarnings("unchecked")
  private TypeAdapter<R> getDelegateAdapter(TypeLabel label) {
    TypeAdapter<R> delegate = (TypeAdapter<R>) labelToDelegate.get(label);
    if (delegate == null) {
      throw new JsonParseException("Unknown type: " + label.getValue());
    }
    return delegate;
  }

  @Override
  public void write(JsonWriter out, R value) throws IOException {
    Class<?> srcType = value.getClass();
    TypeLabel label = typeRegistry.getLabelForType(srcType);
    TypeAdapter<R> delegate = getDelegateAdapter(label);

    JsonObject jsonObject = delegate.toJsonTree(value).getAsJsonObject();
    writeWithTypeField(out, jsonObject, label);
  }

  private void writeWithTypeField(JsonWriter out, JsonObject jsonObject, TypeLabel label)
      throws IOException {
    if (maintainType) {
      Streams.write(jsonObject, out);
      return;
    }

    if (jsonObject.has(typeFieldName)) {
      throw new JsonParseException("Type field name conflict: " + typeFieldName);
    }

    JsonObject result = new JsonObject();
    result.add(typeFieldName, new JsonPrimitive(label.getValue()));
    jsonObject.entrySet().forEach(e -> result.add(e.getKey(), e.getValue()));

    Streams.write(result, out);
  }
}
