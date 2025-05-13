package com.google.gson.graph;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** A builder for constructing graph-aware type adapters that can handle cyclic references. */
public final class GraphAdapterBuilder {
  private final Map<Type, InstanceCreator<?>> instanceCreators;
  private final ConstructorConstructor constructorConstructor;

  /** Creates a new graph adapter builder. */
  public GraphAdapterBuilder() {
    this.instanceCreators = new HashMap<>();
    this.constructorConstructor =
        new ConstructorConstructor(Collections.emptyMap(), true, Collections.emptyList());
  }

  public GraphAdapterBuilder addType(Type type) {
    Objects.requireNonNull(type, "Type cannot be null");

    ObjectConstructor<?> objectConstructor = constructorConstructor.get(TypeToken.get(type));
    InstanceCreator<Object> instanceCreator =
        new InstanceCreator<Object>() {
          @Override
          public Object createInstance(Type t) {
            return objectConstructor.construct();
          }
        };

    return addType(type, instanceCreator);
  }

  /**
   * Registers a type with a custom instance creator.
   *
   * @param type The type to register
   * @param instanceCreator The instance creator to use
   * @return This builder for chaining
   * @throws NullPointerException if type or instanceCreator is null
   */
  public GraphAdapterBuilder addType(Type type, InstanceCreator<?> instanceCreator) {
    Objects.requireNonNull(type, "Type cannot be null");
    Objects.requireNonNull(instanceCreator, "Instance creator cannot be null");

    instanceCreators.put(type, instanceCreator);
    return this;
  }

  /**
   * Registers the graph adapter on a GsonBuilder.
   *
   * @param gsonBuilder The GsonBuilder to register on
   * @throws NullPointerException if gsonBuilder is null
   */
  public void registerOn(GsonBuilder gsonBuilder) {
    Objects.requireNonNull(gsonBuilder, "GsonBuilder cannot be null");

    Map<Type, InstanceCreator<?>> creatorsCopy = new HashMap<>(instanceCreators);
    GraphAdapterFactory factory = new GraphAdapterFactory(creatorsCopy);

    gsonBuilder.registerTypeAdapterFactory(factory);
    for (Map.Entry<Type, InstanceCreator<?>> entry : creatorsCopy.entrySet()) {
      gsonBuilder.registerTypeAdapter(entry.getKey(), factory);
    }
  }

  /**
   * Gets the registered instance creators for testing.
   *
   * @return Unmodifiable map of registered instance creators
   */
  @VisibleForTesting
  Map<Type, InstanceCreator<?>> getInstanceCreators() {
    return Collections.unmodifiableMap(instanceCreators);
  }
}
