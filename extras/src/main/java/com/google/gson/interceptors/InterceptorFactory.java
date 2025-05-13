package com.google.gson.interceptors;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * TypeAdapterFactory that implements @Intercept functionality. Creates adapters that apply
 * post-deserialization processing.
 */
public final class InterceptorFactory implements TypeAdapterFactory {

  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    Intercept intercept = type.getRawType().getAnnotation(Intercept.class);
    if (intercept == null) {
      return null;
    }

    TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
    return createInterceptorAdapter(delegate, intercept);
  }

  private <T> TypeAdapter<T> createInterceptorAdapter(
      TypeAdapter<T> delegate, Intercept intercept) {
    try {
      @SuppressWarnings("unchecked")
      JsonPostDeserializer<T> postDeserializer =
          (JsonPostDeserializer<T>)
              intercept.postDeserialize().getDeclaredConstructor().newInstance();
      return new InterceptorAdapter<>(delegate, postDeserializer);
    } catch (InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException e) {
      throw new RuntimeException("Failed to instantiate post-deserializer", e);
    }
  }

  private static final class InterceptorAdapter<T> extends TypeAdapter<T> {
    private final TypeAdapter<T> delegate;
    private final JsonPostDeserializer<T> postDeserializer;

    InterceptorAdapter(TypeAdapter<T> delegate, JsonPostDeserializer<T> postDeserializer) {
      this.delegate = delegate;
      this.postDeserializer = postDeserializer;
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
      delegate.write(out, value);
    }

    @Override
    public T read(JsonReader in) throws IOException {
      T result = delegate.read(in);
      postDeserializer.postDeserialize(result);
      return result;
    }
  }
}
