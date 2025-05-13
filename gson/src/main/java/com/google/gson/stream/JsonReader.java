package com.google.gson.stream;

import com.google.gson.stream.JsonParserState;
import com.google.gson.stream.JsonReaderConfig;
import com.google.gson.stream.JsonToken;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public abstract class JsonReader implements Closeable {
  private static final long MIN_INCOMPLETE_INTEGER = Long.MIN_VALUE / 10;

  private static final int PEEKED_NONE = 0;
  private static final int PEEKED_BEGIN_OBJECT = 1;
  private static final int PEEKED_END_OBJECT = 2;
  private static final int PEEKED_BEGIN_ARRAY = 3;
  private static final int PEEKED_END_ARRAY = 4;
  private static final int PEEKED_TRUE = 5;
  private static final int PEEKED_FALSE = 6;
  private static final int PEEKED_NULL = 7;
  private static final int PEEKED_BOOLEAN = 5; // Using same value as PEEKED_TRUE since both indicate boolean
  private static final int PEEKED_SINGLE_QUOTED = 8;
  private static final int PEEKED_DOUBLE_QUOTED = 9;
  private static final int PEEKED_UNQUOTED = 10;

  /** When this is returned, the string value is stored in peekedString. */
  private static final int PEEKED_BUFFERED = 11;

  private static final int PEEKED_SINGLE_QUOTED_NAME = 12;
  private static final int PEEKED_DOUBLE_QUOTED_NAME = 13;
  private static final int PEEKED_UNQUOTED_NAME = 14;

  /** When this is returned, the integer value is stored in peekedLong. */
  private static final int PEEKED_LONG = 15;

  private static final int PEEKED_NUMBER = 16;
  private static final int PEEKED_EOF = 17;

  /* State machine when parsing numbers */
  private static final int NUMBER_CHAR_NONE = 0;
  private static final int NUMBER_CHAR_SIGN = 1;
  private static final int NUMBER_CHAR_DIGIT = 2;
  private static final int NUMBER_CHAR_DECIMAL = 3;
  private static final int NUMBER_CHAR_FRACTION_DIGIT = 4;
  private static final int NUMBER_CHAR_EXP_E = 5;
  private static final int NUMBER_CHAR_EXP_SIGN = 6;
  private static final int NUMBER_CHAR_EXP_DIGIT = 7;

  private final Reader in;
  private char[] buffer = new char[8192];
  private int pos = 0;
  private int limit = 0;
  private int lineNumber = 1;
  private int lineStart = 0;

  private int nextNonWhitespace() throws IOException {
    while (true) {
      int c = nextCharacter();
      if (c == -1) {
        return c;
      }

      if (c == '\n') {
        lineNumber++;
        lineStart = pos;
      } else if (c == ' ' || c == '\r' || c == '\t') {
        continue;
      }

      return c;
    }
  }

  private int nextCharacter() throws IOException {
    if (pos >= limit) {
      int count = in.read();
      if (count == -1) {
        return -1;
      }
      pos = 0;
      limit = count;
    }
    return buffer[pos++];
  }

  private boolean isDigit(int c) {
    return c >= '0' && c <= '9';
  }
  private JsonParserState state = JsonParserState.START;
  private final List<JsonParserState> stack = new ArrayList<>();
  private final JsonReaderConfig config;

  private void push(JsonParserState newState) {
    stack.add(state);
    state = newState;
  }

  private void pop() {
    state = stack.remove(stack.size() - 1);
  }

  private void pushBack(int c) {
    pos--;
    buffer[pos] = (char) c;
  }



  public JsonReader(Reader in, JsonReaderConfig config) {
    this.in = in;
    this.config = config;
  }

  public JsonToken peek() throws IOException {
    int peeked = doPeek();
    switch (peeked) {
      case PEEKED_BEGIN_OBJECT:
        return JsonToken.BEGIN_OBJECT;
      case PEEKED_END_OBJECT:
        return JsonToken.END_OBJECT;
      case PEEKED_BEGIN_ARRAY:
        return JsonToken.BEGIN_ARRAY;
      case PEEKED_END_ARRAY:
        return JsonToken.END_ARRAY;
      case PEEKED_NULL:
        return JsonToken.NULL;
      case PEEKED_SINGLE_QUOTED:
      case PEEKED_DOUBLE_QUOTED:
      case PEEKED_UNQUOTED:
        return JsonToken.STRING;
      case PEEKED_NUMBER:
        return JsonToken.NUMBER;
      case PEEKED_BOOLEAN:
        return JsonToken.BOOLEAN;
      default:
        throw new IllegalStateException("Unknown token: " + peeked);
    }
  }

  private int doPeek() throws IOException {
    int next = nextNonWhitespace();
    switch (next) {
      case '{' -> {
        push(JsonParserState.IN_OBJECT);
        return PEEKED_BEGIN_OBJECT;
      }
      case '}' -> {
        if (state != JsonParserState.IN_OBJECT) {
          throw new IOException(String.format("Unexpected end of object at line %d column %d", lineNumber, pos - lineStart + 1));
        }
        pop();
        return PEEKED_END_OBJECT;
      }
      case '[' -> {
        push(JsonParserState.IN_ARRAY);
        return PEEKED_BEGIN_ARRAY;
      }
      case ']' -> {
        if (state != JsonParserState.IN_ARRAY) {
          throw new IOException(String.format("Unexpected end of array at line %d column %d", lineNumber, pos - lineStart + 1));
        }
        pop();
        return PEEKED_END_ARRAY;
      }
      case '"' -> {
            return PEEKED_DOUBLE_QUOTED;
          }
      case 't', 'f' -> {
        return PEEKED_BOOLEAN;
      }
      case 'n' -> {
        return PEEKED_NULL;
      }
      default -> {
        if (isDigit(next) || next == '-') {
          return PEEKED_NUMBER;
        }
          throw new IOException(String.format("Unexpected character: %c at line %d column %d", (char)next, lineNumber, pos - lineStart + 1));
      }
    }
  }

  public String nextString() throws IOException {
    StringBuilder result = new StringBuilder();
    // Skip the opening quote
    int quote = nextNonWhitespace();
    if (quote != '"') {
      throw new IOException(String.format("Expected '\"' but was %c at line %d column %d", (char)quote, lineNumber, pos - lineStart + 1));
    }

    while (true) {
      int c = nextCharacter();
      switch (c) {
        case '"' -> {
          return result.toString();
        }
        case '\\' -> {
            int escaped = nextCharacter();
            switch (escaped) {
              case 'b' -> result.append('\b');
              case 'f' -> result.append('\f');
              case 'n' -> result.append('\n');
              case 'r' -> result.append('\r');
              case 't' -> result.append('\t');
              case '\"', '\\', '/' -> result.append((char) escaped);
              case 'u' -> {
                result.append((char) Integer.parseInt(""
                    + (char) nextCharacter()
                    + (char) nextCharacter()
                    + (char) nextCharacter()
                    + (char) nextCharacter(), 16));
              }
              default -> throw new IOException(
                  String.format("Invalid escape sequence: \\%c at line %d column %d",
                      (char) escaped, lineNumber, pos - lineStart + 1));
            }
          }
        case -1 -> {
          throw new IOException(String.format("Unterminated string at line %d column %d", lineNumber, pos - lineStart + 1));
        }
        default -> result.append((char)c);
      }
    }
  }

  public double nextDouble() throws IOException {
    String number = nextNumberString();
    try {
      return Double.parseDouble(number);
    } catch (NumberFormatException e) {
      throw new IOException(String.format("Expected double but was %s at line %d column %d", number, lineNumber, pos - lineStart + 1));
    }
  }

  public long nextLong() throws IOException {
    String number = nextNumberString();
    try {
      return Long.parseLong(number);
    } catch (NumberFormatException e) {
      throw new IOException(String.format("Expected long but was %s at line %d column %d", number, lineNumber, pos - lineStart + 1));
    }
  }

  public boolean nextBoolean() throws IOException {
    StringBuilder value = new StringBuilder();
    int c = nextNonWhitespace();
    while (c != -1 && Character.isLetter(c)) {
      value.append((char)c);
      c = nextCharacter();
    }
    pushBack(c);
    String result = value.toString();
    if ("true".equals(result)) {
      return true;
    } else if ("false".equals(result)) {
      return false;
    }
    throw new IOException(String.format("Expected boolean but was %s at line %d column %d", result, lineNumber, pos - lineStart + 1));
  }

  private String nextNumberString() throws IOException {
    StringBuilder number = new StringBuilder();
    int c = nextNonWhitespace();

    // Handle sign
    if (c == '-') {
      number.append((char)c);
      c = nextCharacter();
    }

    // Integer part
    while (isDigit(c)) {
      number.append((char)c);
      c = nextCharacter();
    }

    // Fraction part
    if (c == '.') {
      number.append((char)c);
      c = nextCharacter();
      while (isDigit(c)) {
        number.append((char)c);
        c = nextCharacter();
      }
    }

    // Exponent part
    if (c == 'e' || c == 'E') {
      number.append((char)c);
      c = nextCharacter();
      if (c == '+' || c == '-') {
        number.append((char)c);
        c = nextCharacter();
      }
      while (isDigit(c)) {
        number.append((char)c);
        c = nextCharacter();
      }
    }

    pushBack(c);
    return number.toString();
  }
}