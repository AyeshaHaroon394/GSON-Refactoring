package com.google.gson.graph;

import com.google.gson.Gson;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

/** A factory for creating type adapters that handle cyclic references in object graphs. */
public final class GraphAdapterFactory implements TypeAdapterFactory, InstanceCreator<Object> {
  private final Map<Type, InstanceCreator<?>> instanceCreators;
  private final ThreadLocal<Graph> graphThreadLocal;

  /**
   * Creates a new graph adapter factory.
   *
   * @param instanceCreators Map of types to their instance creators
   */
  public GraphAdapterFactory(Map<Type, InstanceCreator<?>> instanceCreators) {
    this.instanceCreators =
        Objects.requireNonNull(instanceCreators, "Instance creators map cannot be null");
    this.graphThreadLocal = new ThreadLocal<>();
  }

  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    if (!instanceCreators.containsKey(type.getType())) {
      return null;
    }

    TypeAdapter<T> delegateAdapter = gson.getDelegateAdapter(this, type);
    TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);

    return new GraphTypeAdapter<>(delegateAdapter, elementAdapter, this);
  }

  @Override
  public Object createInstance(Type type) {
    Graph graph = graphThreadLocal.get();
    if (graph == null || graph.getNextCreate() == null) {
      throw new IllegalStateException("Unexpected call to createInstance() for " + type);
    }

    InstanceCreator<?> creator = instanceCreators.get(type);
    if (creator == null) {
      throw new IllegalStateException("No instance creator registered for type: " + type);
    }

    Object result = creator.createInstance(type);
    @SuppressWarnings("unchecked")
    GraphElement<Object> nextCreate = (GraphElement<Object>) graph.getNextCreate();
    nextCreate.setValue(result);
    graph.setNextCreate(null);

    return result;
  }

  /**
   * Sets the current graph context.
   *
   * @param graph The graph to set
   */
  public void setGraph(Graph graph) {
    graphThreadLocal.set(graph);
  }

  /** Clears the current graph context. */
  public void clearGraph() {
    graphThreadLocal.remove();
  }

  /**
   * Gets the current graph context.
   *
   * @return The current graph
   */
  public Graph getGraph() {
    return graphThreadLocal.get();
  }
}
