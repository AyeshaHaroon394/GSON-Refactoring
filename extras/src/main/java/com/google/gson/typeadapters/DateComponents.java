package com.google.gson.typeadapters;

final class DateComponents {
  private static final int DEFAULT_VALUE = 0;

  final int year;
  final int month;
  final int day;
  final int hour;
  final int minute;
  final int second;
  final int millisecond;
  final String timezone;

  private DateComponents(Builder builder) {
    this.year = builder.year;
    this.month = builder.month;
    this.day = builder.day;
    this.hour = builder.hour;
    this.minute = builder.minute;
    this.second = builder.second;
    this.millisecond = builder.millisecond;
    this.timezone = builder.timezone;
  }

  static class Builder {
    private int year = DEFAULT_VALUE;
    private int month = DEFAULT_VALUE;
    private int day = DEFAULT_VALUE;
    private int hour = DEFAULT_VALUE;
    private int minute = DEFAULT_VALUE;
    private int second = DEFAULT_VALUE;
    private int millisecond = DEFAULT_VALUE;
    private String timezone;

    void setDate(int year, int month, int day) {
      this.year = year;
      this.month = month;
      this.day = day;
    }

    void setTime(int hour, int minute, int second, int millisecond) {
      this.hour = hour;
      this.minute = minute;
      this.second = second;
      this.millisecond = millisecond;
    }

    void setTimezone(String timezone) {
      this.timezone = timezone;
    }

    DateComponents build() {
      if (timezone == null) {
        throw new IllegalStateException("Timezone must be set");
      }
      return new DateComponents(this);
    }
  }
}
