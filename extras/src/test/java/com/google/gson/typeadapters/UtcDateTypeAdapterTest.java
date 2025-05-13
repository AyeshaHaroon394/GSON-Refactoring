package com.google.gson.typeadapters;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;

public class UtcDateTypeAdapterTest {
  private Gson gson;
  private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

  @Before
  public void setUp() {
    gson = new GsonBuilder().registerTypeAdapter(Date.class, new UtcDateTypeAdapter()).create();
  }

  @Test
  public void testSerializeDate() {
    Calendar cal = new GregorianCalendar(UTC);
    cal.clear();
    cal.set(2020, Calendar.JANUARY, 1, 12, 30, 45);
    cal.set(Calendar.MILLISECOND, 123);

    String json = gson.toJson(cal.getTime());
    assertThat(json).isEqualTo("\"2020-01-01T12:30:45.123Z\"");
  }

  @Test
  public void testDeserializeDate() {
    Date date = gson.fromJson("\"2020-01-01T12:30:45.123Z\"", Date.class);

    Calendar cal = new GregorianCalendar(UTC);
    cal.setTime(date);

    assertThat(cal.get(Calendar.YEAR)).isEqualTo(2020);
    assertThat(cal.get(Calendar.MONTH)).isEqualTo(Calendar.JANUARY);
    assertThat(cal.get(Calendar.DAY_OF_MONTH)).isEqualTo(1);
    assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(12);
    assertThat(cal.get(Calendar.MINUTE)).isEqualTo(30);
    assertThat(cal.get(Calendar.SECOND)).isEqualTo(45);
    assertThat(cal.get(Calendar.MILLISECOND)).isEqualTo(123);
  }

  @Test
  public void testDeserializeWithoutMillis() {
    Date date = gson.fromJson("\"2020-01-01T12:30:45Z\"", Date.class);

    Calendar cal = new GregorianCalendar(UTC);
    cal.setTime(date);

    assertThat(cal.get(Calendar.MILLISECOND)).isEqualTo(0);
  }

  @Test
  public void testDeserializeWithTimeZoneOffset() {
    Date date = gson.fromJson("\"2020-01-01T12:30:45+01:00\"", Date.class);

    Calendar cal = new GregorianCalendar(UTC);
    cal.setTime(date);

    assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(11); // 12:30 +01:00 = 11:30 UTC
  }

  @Test
  public void testNullValue() {
    assertThat(gson.fromJson("null", Date.class)).isNull();
    assertThat(gson.toJson(null, Date.class)).isEqualTo("null");
  }

  @Test
  public void testInvalidFormat() {
    JsonParseException e =
        assertThrows(JsonParseException.class, () -> gson.fromJson("\"invalid-date\"", Date.class));
    assertThat(e).hasMessageThat().contains("Failed to parse date");
  }
}
