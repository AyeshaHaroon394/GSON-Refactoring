package com.google.gson.graph;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

public class Graph implements GraphOperations {
  private final Map<Object, GraphElement<?>> map;
  private final Queue<GraphElement<?>> queue;
  private GraphElement<Object> nextCreate;

  public Graph(Map<Object, GraphElement<?>> map) {
    this.map = map;
    this.queue = new ArrayDeque<>();
  }

  @Override
  public String nextName() {
    return "0x" + Integer.toHexString(map.size() + 1);
  }

  @Override
  public void addElement(Object key, GraphElement<?> element) {
    map.put(key, element);
    queue.add(element);
  }

  @Override
  public GraphElement<?> getElement(Object key) {
    return map.get(key);
  }

  @Override
  public Queue<GraphElement<?>> getQueue() {
    return queue;
  }

  @Override
  public void setNextCreate(GraphElement<?> element) {
    @SuppressWarnings("unchecked")
    GraphElement<Object> cast = (GraphElement<Object>) element;
    this.nextCreate = cast;
  }

  @Override
  public GraphElement<?> getNextCreate() {
    return nextCreate;
  }
}
