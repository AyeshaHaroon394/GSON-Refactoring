package com.google.gson.graph;

import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

/**
 * Represents an element in an object graph during serialization or deserialization. Each element
 * has a unique identifier and can hold a value of type T.
 *
 * @param <T> The type of value this element holds
 */
public final class GraphElement<T> {
  /** Unique identifier for this element in the graph */
  private final String id;

  /** The actual value this element holds */
  private T value;

  /** Type adapter for serializing/deserializing the value */
  private TypeAdapter<T> typeAdapter;

  /** JSON representation used during deserialization */
  private final JsonElement element;

  /**
   * Constructs a new graph element.
   *
   * @param value The initial value (can be null for deserialization)
   * @param id The unique identifier (must not be null)
   * @param typeAdapter The type adapter for the value (can be null initially)
   * @param element The JSON element (can be null for serialization)
   * @throws NullPointerException if id is null
   */
  public GraphElement(T value, String id, TypeAdapter<T> typeAdapter, JsonElement element) {
    if (id == null) {
      throw new NullPointerException("Element id cannot be null");
    }
    this.id = id;
    this.value = value;
    this.typeAdapter = typeAdapter;
    this.element = element;
  }

  /**
   * Gets the unique identifier of this element.
   *
   * @return The element's identifier
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the current value held by this element.
   *
   * @return The current value (may be null)
   */
  public T getValue() {
    return value;
  }

  /**
   * Sets a new value for this element.
   *
   * @param value The new value (can be null)
   */
  public void setValue(T value) {
    this.value = value;
  }

  /**
   * Sets or updates the type adapter for this element.
   *
   * @param typeAdapter The new type adapter
   * @throws NullPointerException if typeAdapter is null
   */
  public void setTypeAdapter(TypeAdapter<T> typeAdapter) {
    if (typeAdapter == null) {
      throw new NullPointerException("Type adapter cannot be null");
    }
    this.typeAdapter = typeAdapter;
  }

  /**
   * Writes this element's value using the type adapter.
   *
   * @param out The JSON writer to write to
   * @throws IOException If an I/O error occurs
   * @throws IllegalStateException if typeAdapter is null
   */
  public void write(JsonWriter out) throws IOException {
    if (typeAdapter == null) {
      throw new IllegalStateException("Cannot write element " + id + ": type adapter is null");
    }
    typeAdapter.write(out, value);
  }

  /**
   * Reads this element's value from its JSON element using the type adapter.
   *
   * @param graph The graph operations interface for managing object creation
   * @throws IllegalStateException if there's a recursive call, if typeAdapter is null, or if
   *     deserialization results in a null value
   */
  public void read(GraphOperations graph) {
    if (graph.getNextCreate() != null) {
      throw new IllegalStateException("Unexpected recursive call to read() for " + id);
    }
    if (typeAdapter == null) {
      throw new IllegalStateException("Cannot read element " + id + ": type adapter is null");
    }
    if (element == null) {
      throw new IllegalStateException("Cannot read element " + id + ": JSON element is null");
    }

    graph.setNextCreate(this);
    try {
      value = typeAdapter.fromJsonTree(element);
      if (value == null) {
        throw new IllegalStateException("Non-null value deserialized to null: " + element);
      }
    } catch (Exception e) {
      throw new IllegalStateException("Failed to read element " + id, e);
    }
  }

  @Override
  public String toString() {
    return String.format("GraphElement{id='%s', value=%s}", id, value);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof GraphElement)) {
      return false;
    }

    GraphElement<?> other = (GraphElement<?>) obj;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
