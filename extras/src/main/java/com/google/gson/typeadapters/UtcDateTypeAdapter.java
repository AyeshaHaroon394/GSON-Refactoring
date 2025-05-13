package com.google.gson.typeadapters;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

public final class UtcDateTypeAdapter extends TypeAdapter<Date> {
  private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");
  private final DateFormatter formatter;
  private final DateParser parser;

  public UtcDateTypeAdapter() {
    this.formatter = new DateFormatter(UTC_TIME_ZONE);
    this.parser = new DateParser();
  }

  @Override
  public void write(JsonWriter out, Date date) throws IOException {
    if (date == null) {
      out.nullValue();
      return;
    }
    out.value(formatter.format(date, true));
  }

  @Override
  public Date read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }
    try {
      return parser.parse(in.nextString());
    } catch (ParseException e) {
      throw new JsonParseException(e);
    }
  }
}
