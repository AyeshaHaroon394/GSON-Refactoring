package com.google.gson.stream;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import java.util.Stack;

import static com.google.gson.stream.JsonScope.*;

public class JsonWriter implements Closeable {
  private final Writer out;
  private final Stack<Integer> stack = new Stack<>();
  private String indent = "  ";
  private String separator = ": ";
  private boolean lenient;
  private boolean htmlSafe;
  private String deferredName;
  private boolean serializeNulls = true;

  public JsonWriter(Writer out) {
    this.out = Objects.requireNonNull(out);
    stack.push(EMPTY_DOCUMENT);
  }

  public JsonWriter beginObject() throws IOException {
    writeDeferredName();
    return open(JsonScope.EMPTY_OBJECT, "{");
  }

  public JsonWriter endObject() throws IOException {
    return close(3, "}");
  }

  public JsonWriter beginArray() throws IOException {
    writeDeferredName();
    return open(EMPTY_ARRAY, "[");
  }

  public JsonWriter endArray() throws IOException {
    return close(1, "]");
  }

  public JsonWriter name(String name) throws IOException {
    Objects.requireNonNull(name);
    if (deferredName != null) {
      throw new IllegalStateException();
    }
    if (stack.isEmpty()) {
      throw new IllegalStateException("JsonWriter is closed.");
    }
    deferredName = name;
    return this;
  }

  public JsonWriter value(String value) throws IOException {
    if (value == null) {
      return nullValue();
    }
    writeDeferredName();
    beforeValue();
    string(value);
    return this;
  }

  public JsonWriter value(boolean value) throws IOException {
    writeDeferredName();
    beforeValue();
    out.write(value ? "true" : "false");
    return this;
  }

  public JsonWriter value(double value) throws IOException {
    if (!lenient && (Double.isNaN(value) || Double.isInfinite(value))) {
      throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
    }
    writeDeferredName();
    beforeValue();
    out.write(Double.toString(value));
    return this;
  }

  public JsonWriter value(long value) throws IOException {
    writeDeferredName();
    beforeValue();
    out.write(Long.toString(value));
    return this;
  }

  public JsonWriter nullValue() throws IOException {
    if (deferredName != null) {
      if (serializeNulls) {
        writeDeferredName();
      } else {
        deferredName = null;
        return this;
      }
    }
    beforeValue();
    out.write("null");
    return this;
  }

  private JsonWriter open(int scope, String openBracket) throws IOException {
    beforeValue();
    stack.push(scope);
    out.write(openBracket);
    return this;
  }

  private JsonWriter close(int scope, String closeBracket) throws IOException {
    int currentScope = stack.pop();
    if (currentScope != scope) {
      throw new IllegalStateException("Nesting problem: expected " + scope + " but was " + currentScope);
    }
    out.write(closeBracket);
    return this;
  }

  private void string(String value) throws IOException {
    out.write("\"");
    int length = value.length();
    for (int i = 0; i < length; i++) {
      char c = value.charAt(i);
      switch (c) {
        case '"', '\\' -> {
          out.write('\\');
          out.write(c);
        }
        case '\b' -> out.write("\\b");
        case '\f' -> out.write("\\f");
        case '\n' -> out.write("\\n");
        case '\r' -> out.write("\\r");
        case '\t' -> out.write("\\t");
        default -> {
          if (c < ' ' || (htmlSafe && (c == '<' || c == '>' || c == '&'))) {
            out.write(String.format("\\u%04x", (int) c));
          } else {
            out.write(c);
          }
        }
      }
    }
    out.write("\"");
  }

  private void writeDeferredName() throws IOException {
    if (deferredName != null) {
      beforeName();
      string(deferredName);
      out.write(separator);
      deferredName = null;
    }
  }

  private void beforeName() throws IOException {
    int context = stack.peek();
    if (context == JsonScope.NONEMPTY_OBJECT) { // first in object
      out.write(',');
    } else if (context != JsonScope.EMPTY_OBJECT) { // not in object!
      throw new IllegalStateException("Nesting problem");
    }
    if (indent != null) {
      out.write("\n");
      for (int i = 1; i < stack.size(); i++) {
        out.write(indent);
      }
    }
    stack.set(stack.size() - 1, JsonScope.NONEMPTY_OBJECT);
  }

  private void beforeValue() throws IOException {
    switch (stack.peek()) {
      case JsonScope.NONEMPTY_DOCUMENT -> throw new IllegalStateException(
              "JSON must have only one top-level value.");
      case EMPTY_DOCUMENT -> stack.set(stack.size() - 1, JsonScope.NONEMPTY_DOCUMENT);
      case EMPTY_ARRAY, NONEMPTY_ARRAY -> {
        if (indent != null) {
          out.write("\n");
          for (int i = 1; i < stack.size(); i++) {
            out.write(indent);
          }
        }
        stack.set(stack.size() - 1, NONEMPTY_ARRAY);
      }
      default -> throw new IllegalStateException("Nesting problem");
    }
  }

  @Override
  public void close() throws IOException {
    out.close();
    if (!stack.isEmpty()) {
      throw new IOException("Incomplete document");
    }
  }

  public JsonWriter setIndent(String indent) {
    this.indent = indent;
    return this;
  }

  public JsonWriter setLenient(boolean lenient) {
    this.lenient = lenient;
    return this;
  }

  public JsonWriter setHtmlSafe(boolean htmlSafe) {
    this.htmlSafe = htmlSafe;
    return this;
  }

  public JsonWriter setSerializeNulls(boolean serializeNulls) {
    this.serializeNulls = serializeNulls;
    return this;
  }
}

