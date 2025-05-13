package com.google.gson.graph;

import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.IdentityHashMap;

public class GraphTypeAdapter<T> extends TypeAdapter<T> {
  private final TypeAdapter<T> delegateAdapter;
  private final TypeAdapter<JsonElement> elementAdapter;
  private final GraphAdapterFactory factory;

  public GraphTypeAdapter(
      TypeAdapter<T> delegateAdapter,
      TypeAdapter<JsonElement> elementAdapter,
      GraphAdapterFactory factory) {
    this.delegateAdapter = delegateAdapter;
    this.elementAdapter = elementAdapter;
    this.factory = factory;
  }

  @Override
  public void write(JsonWriter out, T value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }

    Graph graph = factory.getGraph();
    boolean writeEntireGraph = false;

    if (graph == null) {
      writeEntireGraph = true;
      graph = new Graph(new IdentityHashMap<>());
      factory.setGraph(graph);
    }

    try {
      writeValue(out, value, graph, writeEntireGraph);
    } finally {
      if (writeEntireGraph) {
        factory.clearGraph();
      }
    }
  }

  @Override
  public T read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }

    Graph graph = factory.getGraph();
    boolean readEntireGraph = false;
    String currentName = null;

    if (graph == null) {
      readEntireGraph = true;
      graph = new Graph(new HashMap<>());
      factory.setGraph(graph);
      currentName = readGraphObject(in, graph);
    } else {
      currentName = in.nextString();
    }

    try {
      return readElement(currentName, graph);
    } finally {
      if (readEntireGraph) {
        factory.clearGraph();
      }
    }
  }

  private void writeValue(JsonWriter out, T value, Graph graph, boolean writeEntireGraph)
      throws IOException {
    @SuppressWarnings("unchecked")
    GraphElement<T> element = (GraphElement<T>) graph.getElement(value);

    if (element == null) {
      element = new GraphElement<>(value, graph.nextName(), delegateAdapter, null);
      graph.addElement(value, element);
    }

    if (writeEntireGraph) {
      out.beginObject();
      while (!graph.getQueue().isEmpty()) {
        GraphElement<?> current = graph.getQueue().poll();
        out.name(current.getId());
        current.write(out);
      }
      out.endObject();
    } else {
      out.value(element.getId());
    }
  }

  private String readGraphObject(JsonReader in, Graph graph) throws IOException {
    String firstRead = null;
    in.beginObject();

    while (in.hasNext()) {
      String name = in.nextName();
      if (firstRead == null) {
        firstRead = name;
      }
      JsonElement element = elementAdapter.read(in);
      graph.addElement(name, new GraphElement<>(null, name, delegateAdapter, element));
    }

    in.endObject();
    return firstRead;
  }

  @SuppressWarnings("unchecked")
  private T readElement(String name, Graph graph) {
    GraphElement<T> element = (GraphElement<T>) graph.getElement(name);
    if (element.getValue() == null) {
      element.setTypeAdapter(delegateAdapter);
      element.read(graph);
    }
    return element.getValue();
  }
}
