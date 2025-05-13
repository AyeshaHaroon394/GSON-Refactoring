package com.google.gson.typeadapters;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

final class DateFormatter {
  private static final int STANDARD_DATE_LENGTH = "yyyy-MM-ddThh:mm:ss".length();
  private static final int MILLIS_LENGTH = ".sss".length();
  private static final int TIMEZONE_LENGTH = "+hh:mm".length();

  private final TimeZone timeZone;

  DateFormatter(TimeZone timeZone) {
    this.timeZone = timeZone;
  }

  String format(Date date, boolean includeMillis) {
    Calendar calendar = new GregorianCalendar(timeZone, Locale.US);
    calendar.setTime(date);

    StringBuilder formatted = new StringBuilder(calculateCapacity(includeMillis));
    formatDate(formatted, calendar);
    formatted.append('T');
    formatTime(formatted, calendar, includeMillis);
    formatTimeZone(formatted, calendar);

    return formatted.toString();
  }

  private int calculateCapacity(boolean includeMillis) {
    int capacity = STANDARD_DATE_LENGTH;
    if (includeMillis) {
      capacity += MILLIS_LENGTH;
    }
    capacity += timeZone.getRawOffset() == 0 ? 1 : TIMEZONE_LENGTH;
    return capacity;
  }

  private void formatDate(StringBuilder buffer, Calendar calendar) {
    padInt(buffer, calendar.get(Calendar.YEAR), 4);
    buffer.append('-');
    padInt(buffer, calendar.get(Calendar.MONTH) + 1, 2);
    buffer.append('-');
    padInt(buffer, calendar.get(Calendar.DAY_OF_MONTH), 2);
  }

  private void formatTime(StringBuilder buffer, Calendar calendar, boolean includeMillis) {
    padInt(buffer, calendar.get(Calendar.HOUR_OF_DAY), 2);
    buffer.append(':');
    padInt(buffer, calendar.get(Calendar.MINUTE), 2);
    buffer.append(':');
    padInt(buffer, calendar.get(Calendar.SECOND), 2);

    if (includeMillis) {
      buffer.append('.');
      padInt(buffer, calendar.get(Calendar.MILLISECOND), 3);
    }
  }

  private void formatTimeZone(StringBuilder buffer, Calendar calendar) {
    int offset = timeZone.getOffset(calendar.getTimeInMillis());
    if (offset == 0) {
      buffer.append('Z');
      return;
    }

    int hours = Math.abs((offset / (60 * 1000)) / 60);
    int minutes = Math.abs((offset / (60 * 1000)) % 60);
    buffer.append(offset < 0 ? '-' : '+');
    padInt(buffer, hours, 2);
    buffer.append(':');
    padInt(buffer, minutes, 2);
  }

  private static void padInt(StringBuilder buffer, int value, int length) {
    String strValue = Integer.toString(value);
    for (int i = length - strValue.length(); i > 0; i--) {
      buffer.append('0');
    }
    buffer.append(strValue);
  }
}
