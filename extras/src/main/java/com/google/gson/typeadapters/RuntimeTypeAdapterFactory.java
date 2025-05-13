package com.google.gson.typeadapters;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RuntimeTypeAdapterFactory<T> implements TypeAdapterFactory {
  private final Class<?> baseType;
  private final String typeFieldName;
  private final TypeRegistry<T> typeRegistry;
  private final boolean maintainType;
  private boolean recognizeSubtypes;

  private RuntimeTypeAdapterFactory(Builder<T> builder) {
    this.baseType = builder.baseType;
    this.typeFieldName = builder.typeFieldName;
    this.maintainType = builder.maintainType;
    this.typeRegistry = new TypeRegistry<>();
  }

  public static <T> Builder<T> builder(Class<T> baseType) {
    return new Builder<>(baseType);
  }

  @CanIgnoreReturnValue
  public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> type, String label) {
    typeRegistry.register(type, TypeLabel.of(label));
    return this;
  }

  @CanIgnoreReturnValue
  public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> type) {
    return registerSubtype(type, type.getSimpleName());
  }

  @Override
  public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> type) {
    if (!isApplicableType(type)) {
      return null;
    }
    return createTypeAdapter(gson);
  }

  private <R> boolean isApplicableType(TypeToken<R> type) {
    if (type == null) {
      return false;
    }
    Class<?> rawType = type.getRawType();
    return recognizeSubtypes ? baseType.isAssignableFrom(rawType) : baseType.equals(rawType);
  }

  private <R> TypeAdapter<R> createTypeAdapter(Gson gson) {
    return new RuntimeTypeAdapter<>(
        gson, typeFieldName, maintainType, typeRegistry, createDelegateAdapters(gson));
  }

  private Map<TypeLabel, TypeAdapter<?>> createDelegateAdapters(Gson gson) {
    Map<TypeLabel, TypeAdapter<?>> adapters = new LinkedHashMap<>();
    for (Map.Entry<TypeLabel, Class<?>> entry : typeRegistry.entries()) {
      TypeAdapter<?> delegate = gson.getDelegateAdapter(this, TypeToken.get(entry.getValue()));
      adapters.put(entry.getKey(), delegate);
    }
    return adapters;
  }

  private static final class RuntimeTypeAdapter<R> extends TypeAdapter<R> {
    private final String typeFieldName;
    private final boolean maintainType;
    private final TypeRegistry<?> typeRegistry;
    private final Map<TypeLabel, TypeAdapter<?>> labelToDelegate;
    private final TypeAdapter<JsonElement> jsonElementAdapter;

    RuntimeTypeAdapter(
        Gson gson,
        String typeFieldName,
        boolean maintainType,
        TypeRegistry<?> typeRegistry,
        Map<TypeLabel, TypeAdapter<?>> labelToDelegate) {
      this.typeFieldName = typeFieldName;
      this.maintainType = maintainType;
      this.typeRegistry = typeRegistry;
      this.labelToDelegate = labelToDelegate;
      this.jsonElementAdapter = gson.getAdapter(JsonElement.class);
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
      JsonElement labelJsonElement = jsonObject.get(typeFieldName);
      if (labelJsonElement == null) {
        throw new JsonParseException("Missing type field: " + typeFieldName);
      }

      if (!maintainType) {
        jsonObject.remove(typeFieldName);
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
      if (value == null) {
        out.nullValue();
        return;
      }

      Class<?> srcType = value.getClass();
      TypeLabel label = typeRegistry.getLabelForType(srcType);
      TypeAdapter<R> delegate = getDelegateAdapter(label);

      JsonObject jsonObject = delegate.toJsonTree(value).getAsJsonObject();
      writeWithTypeField(out, jsonObject, label);
    }

    private void writeWithTypeField(JsonWriter out, JsonObject jsonObject, TypeLabel label)
        throws IOException {
      if (maintainType) {
        jsonElementAdapter.write(out, jsonObject);
        return;
      }

      if (jsonObject.has(typeFieldName)) {
        throw new JsonParseException("Type field name conflict: " + typeFieldName);
      }

      JsonObject result = new JsonObject();
      result.add(typeFieldName, new JsonPrimitive(label.getValue()));
      jsonObject.entrySet().forEach(e -> result.add(e.getKey(), e.getValue()));

      jsonElementAdapter.write(out, result);
    }
  }

  public static final class Builder<T> {
    private final Class<T> baseType;
    private String typeFieldName = "type";
    private boolean maintainType;

    private Builder(Class<T> baseType) {
      if (baseType == null) {
        throw new NullPointerException("Base type cannot be null");
      }
      this.baseType = baseType;
    }

    public Builder<T> withTypeFieldName(String typeFieldName) {
      this.typeFieldName = typeFieldName;
      return this;
    }

    public Builder<T> withMaintainType(boolean maintainType) {
      this.maintainType = maintainType;
      return this;
    }

    public RuntimeTypeAdapterFactory<T> build() {
      return new RuntimeTypeAdapterFactory<>(this);
    }
  }
}
