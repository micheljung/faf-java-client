package com.faforever.client.util;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Service
@NoArgsConstructor
public class DateFormatterUtil {

  public static final DateTimeFormatter FORMATTER_AUTO_SHORT = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
  public static final DateTimeFormatter FORMATTER_AUTO_MEDIUM = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
  public static final DateTimeFormatter FORMATTER_AUTO_LONG = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);
  public static final DateTimeFormatter FORMATTER_AUTO_FULL = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL);

  public static final DateTimeFormatter FORMATTER_DMY_SHORT = DateTimeFormatter.ofPattern("d/M/yy");
  public static final DateTimeFormatter FORMATTER_DMY_MEDIUM = DateTimeFormatter.ofPattern("d MMM, yyyy");
  public static final DateTimeFormatter FORMATTER_DMY_LONG = DateTimeFormatter.ofPattern("d MMMM, yyyy");
  public static final DateTimeFormatter FORMATTER_DMY_FULL = DateTimeFormatter.ofPattern("EEEE, d MMMM, yyyy");

  public static final DateTimeFormatter FORMATTER_MDY_SHORT = DateTimeFormatter.ofPattern("M/d/yy");
  public static final DateTimeFormatter FORMATTER_MDY_MEDIUM = DateTimeFormatter.ofPattern("MMM d, yyyy");
  public static final DateTimeFormatter FORMATTER_MDY_LONG = DateTimeFormatter.ofPattern("MMMM d, yyyy");
  public static final DateTimeFormatter FORMATTER_MDY_FULL = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");


  public DateTimeFormatter getAutoFormatter(FormatStyle style) {
    return switch (style) {
      case SHORT -> FORMATTER_AUTO_SHORT;
      case MEDIUM -> FORMATTER_AUTO_MEDIUM;
      case LONG -> FORMATTER_AUTO_LONG;
      case FULL -> FORMATTER_AUTO_FULL;
    };
  }

  public DateTimeFormatter getDMYFormatter(FormatStyle style) {
    return switch (style) {
      case SHORT -> FORMATTER_DMY_SHORT;
      case MEDIUM -> FORMATTER_DMY_MEDIUM;
      case LONG -> FORMATTER_DMY_LONG;
      case FULL -> FORMATTER_DMY_FULL;
    };
  }

  public DateTimeFormatter getMDYFormatter(FormatStyle style) {
    return switch (style) {
      case SHORT -> FORMATTER_MDY_SHORT;
      case MEDIUM -> FORMATTER_MDY_MEDIUM;
      case LONG -> FORMATTER_MDY_LONG;
      case FULL -> FORMATTER_MDY_FULL;
    };
  }

}
