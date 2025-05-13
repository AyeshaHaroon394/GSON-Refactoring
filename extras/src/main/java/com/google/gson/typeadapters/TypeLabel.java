package com.google.gson.typeadapters;

import java.util.Objects;

/** Represents a type label used for type identification in JSON serialization. */
public final class TypeLabel {
  private final String value;

  private TypeLabel(String value) {
    this.value = Objects.requireNonNull(value, "Type label value cannot be null");
  }

  public static TypeLabel of(String value) {
    return new TypeLabel(value);
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TypeLabel)) {
      return false;
    }
    TypeLabel that = (TypeLabel) o;
    return value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public String toString() {
    return value;
  }
}
