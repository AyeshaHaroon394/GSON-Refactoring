package com.google.gson.interceptors;

/**
 * Interface for post-deserialization processing. Implementations can inspect or modify objects
 * after Gson deserialization.
 *
 * @param <T> Type of object to process
 */
public interface JsonPostDeserializer<T> {
  /**
   * Processes an object after deserialization.
   *
   * @param object The deserialized object to process
   */
  void postDeserialize(T object);
}
