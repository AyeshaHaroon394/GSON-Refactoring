package com.google.gson.typeadapters;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

final class DateParser {
  private static final String GMT_ID = "GMT";

  Date parse(String date) throws ParseException {
    if (date == null) {
      throw new ParseException("Date string must not be null", 0);
    }

    try {
      ParsePosition pos = new ParsePosition(0);
      DateComponents components = parseComponents(date, pos);
      return createDate(components);
    } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
      throw new ParseException("Failed to parse date ['" + date + "']: " + e.getMessage(), 0);
    }
  }

  private DateComponents parseComponents(String date, ParsePosition pos) {
    DateComponents.Builder builder = new DateComponents.Builder();
    int offset = pos.getIndex();

    offset = parseDate(date, offset, builder);
    if (checkOffset(date, offset, 'T')) {
      offset = parseTime(date, offset + 1, builder);
    }
    offset = parseTimezone(date, offset, builder);

    pos.setIndex(offset);
    return builder.build();
  }

  private int parseDate(String date, int offset, DateComponents.Builder builder) {
    int year = parseInt(date, offset, offset + 4);
    offset += 4;
    if (checkOffset(date, offset, '-')) {
      offset++;
    }

    int month = parseInt(date, offset, offset + 2);
    offset += 2;
    if (checkOffset(date, offset, '-')) {
      offset++;
    }

    int day = parseInt(date, offset, offset + 2);
    offset += 2;

    builder.setDate(year, month, day);
    return offset;
  }

  private int parseTime(String date, int offset, DateComponents.Builder builder) {
    int hour = parseInt(date, offset, offset + 2);
    offset += 2;
    if (checkOffset(date, offset, ':')) {
      offset++;
    }

    int minute = parseInt(date, offset, offset + 2);
    offset += 2;
    if (checkOffset(date, offset, ':')) {
      offset++;
    }

    int second = 0;
    int millisecond = 0;

    if (date.length() > offset) {
      char c = date.charAt(offset);
      if (c != 'Z' && c != '+' && c != '-') {
        second = parseInt(date, offset, offset + 2);
        offset += 2;
        if (checkOffset(date, offset, '.')) {
          millisecond = parseInt(date, offset + 1, offset + 4);
          offset += 4;
        }
      }
    }

    builder.setTime(hour, minute, second, millisecond);
    return offset;
  }

  private int parseTimezone(String date, int offset, DateComponents.Builder builder) {
    if (offset >= date.length()) {
      throw new IllegalArgumentException("No time zone indicator");
    }

    char indicator = date.charAt(offset);
    String timezoneId;

    if (indicator == 'Z') {
      timezoneId = GMT_ID;
      offset += 1;
    } else if (indicator == '+' || indicator == '-') {
      String timezoneOffset = date.substring(offset);
      timezoneId = GMT_ID + timezoneOffset;
      offset += timezoneOffset.length();
    } else {
      throw new IllegalArgumentException("Invalid time zone indicator: " + indicator);
    }

    builder.setTimezone(timezoneId);
    return offset;
  }

  private Date createDate(DateComponents components) {
    TimeZone timezone = TimeZone.getTimeZone(components.timezone);
    if (!timezone.getID().equals(components.timezone)) {
      throw new IllegalArgumentException("Invalid timezone: " + components.timezone);
    }

    Calendar calendar = new GregorianCalendar(timezone);
    calendar.setLenient(false);
    calendar.set(Calendar.YEAR, components.year);
    calendar.set(Calendar.MONTH, components.month - 1);
    calendar.set(Calendar.DAY_OF_MONTH, components.day);
    calendar.set(Calendar.HOUR_OF_DAY, components.hour);
    calendar.set(Calendar.MINUTE, components.minute);
    calendar.set(Calendar.SECOND, components.second);
    calendar.set(Calendar.MILLISECOND, components.millisecond);

    return calendar.getTime();
  }

  private static boolean checkOffset(String value, int offset, char expected) {
    return offset < value.length() && value.charAt(offset) == expected;
  }

  private static int parseInt(String value, int beginIndex, int endIndex) {
    if (beginIndex < 0 || endIndex > value.length() || beginIndex > endIndex) {
      throw new NumberFormatException(value);
    }

    int result = 0;
    for (int i = beginIndex; i < endIndex; i++) {
      int digit = Character.digit(value.charAt(i), 10);
      if (digit < 0) {
        throw new NumberFormatException("Invalid number: " + value);
      }
      result = result * 10 + digit;
    }
    return result;
  }
}
