package com.google.gson.graph;

import java.util.Queue;

public interface GraphOperations {
  String nextName();

  void addElement(Object value, GraphElement<?> element);

  GraphElement<?> getElement(Object key);

  Queue<GraphElement<?>> getQueue();

  void setNextCreate(GraphElement<?> element);

  GraphElement<?> getNextCreate();
}
