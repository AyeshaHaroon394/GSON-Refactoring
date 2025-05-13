package com.google.gson.typeadapters;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class TypeRegistry<T> {
  private final Map<TypeLabel, Class<?>> labelToSubtype = new LinkedHashMap<>();
  private final Map<Class<?>, TypeLabel> subtypeToLabel = new LinkedHashMap<>();

  public void register(Class<? extends T> type, TypeLabel label) {
    validateRegistration(type, label);
    labelToSubtype.put(label, type);
    subtypeToLabel.put(type, label);
  }

  private void validateRegistration(Class<? extends T> type, TypeLabel label) {
    if (type == null || label == null) {
      throw new NullPointerException("Type and label must not be null");
    }
    if (subtypeToLabel.containsKey(type) || labelToSubtype.containsKey(label)) {
      throw new IllegalArgumentException("Types and labels must be unique");
    }
  }

  public TypeLabel getLabelForType(Class<?> type) {
    return subtypeToLabel.get(type);
  }

  public Class<?> getTypeForLabel(TypeLabel label) {
    return labelToSubtype.get(label);
  }

  public Set<Map.Entry<TypeLabel, Class<?>>> entries() {
    return labelToSubtype.entrySet();
  }
}
