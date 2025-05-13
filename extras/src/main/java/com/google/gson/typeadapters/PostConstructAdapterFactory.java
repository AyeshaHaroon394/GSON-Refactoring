/*
 * Copyright (C) 2016 Gson Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gson.typeadapters;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import javax.annotation.PostConstruct;

public final class PostConstructAdapterFactory implements TypeAdapterFactory {

  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    Method postConstructMethod = findPostConstructMethod(type.getRawType());
    if (postConstructMethod == null) {
      return null;
    }

    TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
    return new PostConstructAdapter<>(delegate, new PostConstructInvoker(postConstructMethod));
  }

  private Method findPostConstructMethod(Class<?> type) {
    for (Class<?> t = type; t != Object.class && t != null; t = t.getSuperclass()) {
      for (Method method : t.getDeclaredMethods()) {
        if (method.isAnnotationPresent(PostConstruct.class)) {
          method.setAccessible(true);
          return method;
        }
      }
    }
    return null;
  }

  static final class PostConstructAdapter<T> extends TypeAdapter<T> {
    private final TypeAdapter<T> delegate;
    private final PostConstructInvoker invoker;

    PostConstructAdapter(TypeAdapter<T> delegate, PostConstructInvoker invoker) {
      this.delegate = Objects.requireNonNull(delegate);
      this.invoker = Objects.requireNonNull(invoker);
    }

    @Override
    public T read(JsonReader in) throws IOException {
      T result = delegate.read(in);
      if (result != null) {
        invoker.invoke(result);
      }
      return result;
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
      delegate.write(out, value);
    }
  }

  static final class PostConstructInvoker {
    private final Method method;

    PostConstructInvoker(Method method) {
      this.method = Objects.requireNonNull(method);
    }

    void invoke(Object target) {
      try {
        method.invoke(target);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException("Failed to access @PostConstruct method", e);
      } catch (InvocationTargetException e) {
        Throwable cause = e.getCause();
        if (cause instanceof RuntimeException) {
          throw (RuntimeException) cause;
        }
        throw new PostConstructInvocationException(cause);
      }
    }
  }

  static final class PostConstructInvocationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    PostConstructInvocationException(Throwable cause) {
      super("Failed to invoke @PostConstruct method", cause);
    }
  }
}
