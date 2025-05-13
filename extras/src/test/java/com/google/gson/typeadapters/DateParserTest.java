package com.google.gson.typeadapters;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.junit.Test;

public class DateParserTest {
  private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

  @Test
  public void testParseComplete() throws ParseException {
    DateParser parser = new DateParser();
    Date date = parser.parse("2020-01-01T12:30:45.123Z");

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
  public void testParseWithoutMillis() throws ParseException {
    DateParser parser = new DateParser();
    Date date = parser.parse("2020-01-01T12:30:45Z");

    Calendar cal = new GregorianCalendar(UTC);
    cal.setTime(date);
    assertThat(cal.get(Calendar.MILLISECOND)).isEqualTo(0);
  }

  @Test
  public void testParseWithTimeZoneOffset() throws ParseException {
    DateParser parser = new DateParser();
    Date date = parser.parse("2020-01-01T12:30:45+01:00");

    Calendar cal = new GregorianCalendar(UTC);
    cal.setTime(date);
    assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(11); // 12:30 +01:00 = 11:30 UTC
  }

  @Test
  public void testParseInvalidFormat() {
    DateParser parser = new DateParser();
    ParseException e = assertThrows(ParseException.class, () -> parser.parse("invalid-date"));
    assertThat(e).hasMessageThat().contains("Failed to parse date");
  }

  @Test
  public void testParseNullDate() {
    DateParser parser = new DateParser();
    ParseException e = assertThrows(ParseException.class, () -> parser.parse(null));
    assertThat(e).hasMessageThat().contains("Date string must not be null");
  }

  @Test
  public void testParseInvalidTimezone() {
    DateParser parser = new DateParser();
    ParseException e =
        assertThrows(ParseException.class, () -> parser.parse("2020-01-01T12:30:45Invalid"));
    assertThat(e).hasMessageThat().contains("Invalid time zone indicator");
  }
}
