package com.google.gson.typeadapters;

import static com.google.common.truth.Truth.assertThat;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.junit.Test;

public class DateFormatterTest {
  private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

  @Test
  public void testFormatWithMilliseconds() {
    DateFormatter formatter = new DateFormatter(UTC);
    Calendar cal = new GregorianCalendar(UTC);
    cal.clear();
    cal.set(2024, Calendar.MARCH, 15, 14, 30, 45);
    cal.set(Calendar.MILLISECOND, 123);

    String formatted = formatter.format(cal.getTime(), true);
    assertThat(formatted).isEqualTo("2024-03-15T14:30:45.123Z");
  }

  @Test
  public void testFormatWithoutMilliseconds() {
    DateFormatter formatter = new DateFormatter(UTC);
    Calendar cal = new GregorianCalendar(UTC);
    cal.clear();
    cal.set(2024, Calendar.MARCH, 15, 14, 30, 45);

    String formatted = formatter.format(cal.getTime(), false);
    assertThat(formatted).isEqualTo("2024-03-15T14:30:45Z");
  }

  @Test
  public void testFormatWithTimezone() {
    TimeZone timezone = TimeZone.getTimeZone("GMT+02:00");
    DateFormatter formatter = new DateFormatter(timezone);
    Calendar cal = new GregorianCalendar(timezone);
    cal.clear();
    cal.set(2024, Calendar.MARCH, 15, 14, 30, 45);

    String formatted = formatter.format(cal.getTime(), false);
    assertThat(formatted).isEqualTo("2024-03-15T14:30:45+02:00");
  }

  @Test
  public void testFormatWithNegativeTimezone() {
    TimeZone timezone = TimeZone.getTimeZone("GMT-05:00");
    DateFormatter formatter = new DateFormatter(timezone);
    Calendar cal = new GregorianCalendar(timezone);
    cal.clear();
    cal.set(2024, Calendar.MARCH, 15, 14, 30, 45);

    String formatted = formatter.format(cal.getTime(), false);
    assertThat(formatted).isEqualTo("2024-03-15T14:30:45-05:00");
  }
}
