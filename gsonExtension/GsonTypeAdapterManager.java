package com.google.gson.gsonExtension;

import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.bind.JsonAdapterAnnotationTypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class GsonTypeAdapterManager {
    private final ConcurrentMap<TypeToken<?>, TypeAdapter<?>> typeTokenCache;
    private final ThreadLocal<Map<TypeToken<?>, TypeAdapter<?>>> threadLocalAdapterResults;
    private final List<TypeAdapterFactory> factories;
    private final ConstructorConstructor constructorConstructor;
    private final JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory;

    public GsonTypeAdapterManager(List<TypeAdapterFactory> factories,
                                  ConstructorConstructor constructorConstructor,
                                  JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory) {
        this.typeTokenCache = new ConcurrentHashMap<>();
        this.threadLocalAdapterResults = new ThreadLocal<>();
        this.factories = factories;
        this.constructorConstructor = constructorConstructor;
        this.jsonAdapterFactory = jsonAdapterFactory;
    }

    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> getAdapter(TypeToken<T> type) {
        TypeAdapter<?> cached = typeTokenCache.get(type);
        if (cached != null) {
            return (TypeAdapter<T>) cached;
        }

        Map<TypeToken<?>, TypeAdapter<?>> threadResults = threadLocalAdapterResults.get();
        boolean createThreadLocalMap = threadResults == null;
        if (createThreadLocalMap) {
            threadResults = new HashMap<>();
            threadLocalAdapterResults.set(threadResults);
        }

        TypeAdapter<T> adapter = getAdapterFromFactories(type);
        threadResults.put(type, adapter);

        if (createThreadLocalMap) {
            threadLocalAdapterResults.remove();
        }

        typeTokenCache.put(type, adapter);
        return adapter;
    }

    private <T> TypeAdapter<T> getAdapterFromFactories(TypeToken<T> type) {
        for (TypeAdapterFactory factory : factories) {
            TypeAdapter<T> adapter = factory.create(new Gson(), type);
            if (adapter != null) {
                return adapter;
            }
        }
        throw new IllegalArgumentException("No type adapter found for " + type);
    }
}