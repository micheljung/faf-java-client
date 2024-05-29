package com.faforever.client.util;

import com.faforever.client.i18n.I18n;
import com.faforever.client.preferences.ChatPrefs;
import com.faforever.client.preferences.DateInfo;
import com.faforever.client.preferences.LocalizationPrefs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.FormatStyle;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith({MockitoExtension.class})
class TimeServiceTest {

  @Mock
  I18n i18n;
  @Mock
  ChatPrefs chatPrefs;
  @Mock
  LocalizationPrefs localizationPrefs;

  @Mock
  DateFormatterUtil dateFormatterUtil;

  @InjectMocks
  TimeService service;

  private final Locale localeUS = Locale.of("en", "US");
  private final Locale localeFR = Locale.of("fr", "FR");
  private final FormatStyle formatStyleMedium = FormatStyle.MEDIUM;

  @BeforeEach
  void setUp() {

  }

  @Test
  void asDateAutoUS() {
    var date = generateDate();
    var dateInfo = DateInfo.AUTO;

    when(localizationPrefs.getDateFormat()).thenReturn(dateInfo);
    when(dateFormatterUtil.getAutoFormatter(formatStyleMedium)).thenReturn(DateFormatterUtil.FORMATTER_AUTO_MEDIUM);
    when(i18n.getUserSpecificLocale()).thenReturn(localeUS);

    var result = service.asDate(date, formatStyleMedium);

    assertEquals("May 9, 2022", result);
  }

  @Test
  void asDateMonthDayYearUS() {
    var date = generateDate();
    var dateInfo = DateInfo.MONTH_DAY_YEAR;

    when(localizationPrefs.getDateFormat()).thenReturn(dateInfo);
    when(dateFormatterUtil.getMDYFormatter(formatStyleMedium)).thenReturn(DateFormatterUtil.FORMATTER_MDY_MEDIUM);
    when(i18n.getUserSpecificLocale()).thenReturn(localeUS);

    var result = service.asDate(date, formatStyleMedium);

    assertEquals("May 9, 2022", result);
  }

  @Test
  void asDateDayMonthYearUS() {
    var date = generateDate();
    var dateInfo = DateInfo.DAY_MONTH_YEAR;

    when(localizationPrefs.getDateFormat()).thenReturn(dateInfo);
    when(dateFormatterUtil.getDMYFormatter(formatStyleMedium)).thenReturn(DateFormatterUtil.FORMATTER_DMY_MEDIUM);
    when(i18n.getUserSpecificLocale()).thenReturn(localeUS);

    var result = service.asDate(date, formatStyleMedium);

    assertEquals("9 May, 2022", result);
  }

  @Test
  void asDateAutoFR() {
    var date = generateDate();
    var dateInfo = DateInfo.AUTO;

    when(localizationPrefs.getDateFormat()).thenReturn(dateInfo);
    when(dateFormatterUtil.getAutoFormatter(formatStyleMedium)).thenReturn(DateFormatterUtil.FORMATTER_AUTO_MEDIUM);
    when(i18n.getUserSpecificLocale()).thenReturn(localeFR);

    var result = service.asDate(date, formatStyleMedium);

    assertEquals("9 mai 2022", result);
  }

  @Test
  void asDateMonthDayYearFR() {
    var date = generateDate();
    var dateInfo = DateInfo.MONTH_DAY_YEAR;

    when(localizationPrefs.getDateFormat()).thenReturn(dateInfo);
    when(dateFormatterUtil.getMDYFormatter(formatStyleMedium)).thenReturn(DateFormatterUtil.FORMATTER_MDY_MEDIUM);
    when(i18n.getUserSpecificLocale()).thenReturn(localeFR);

    var result = service.asDate(date, formatStyleMedium);

    assertEquals("mai 9, 2022", result);
  }

  @Test
  void asDateDayMonthYearFR() {
    var date = generateDate();
    var dateInfo = DateInfo.DAY_MONTH_YEAR;

    when(localizationPrefs.getDateFormat()).thenReturn(dateInfo);
    when(dateFormatterUtil.getDMYFormatter(formatStyleMedium)).thenReturn(DateFormatterUtil.FORMATTER_DMY_MEDIUM);
    when(i18n.getUserSpecificLocale()).thenReturn(localeFR);

    var result = service.asDate(date, formatStyleMedium);

    assertEquals("9 mai, 2022", result);
  }

  @Test
  void asDateAutoFullUS() {
    var date = generateDate();
    var dateInfo = DateInfo.AUTO;

    when(localizationPrefs.getDateFormat()).thenReturn(dateInfo);
    when(dateFormatterUtil.getAutoFormatter(FormatStyle.FULL)).thenReturn(DateFormatterUtil.FORMATTER_AUTO_FULL);
    when(i18n.getUserSpecificLocale()).thenReturn(localeUS);

    var result = service.asDate(date, FormatStyle.FULL);

    assertEquals("Monday, May 9, 2022", result);
  }

  @Test
  void asDateDMYFullUS() {
    var date = generateDate();
    var dateInfo = DateInfo.DAY_MONTH_YEAR;

    when(localizationPrefs.getDateFormat()).thenReturn(dateInfo);
    when(dateFormatterUtil.getDMYFormatter(FormatStyle.FULL)).thenReturn(DateFormatterUtil.FORMATTER_DMY_FULL);
    when(i18n.getUserSpecificLocale()).thenReturn(localeUS);

    var result = service.asDate(date, FormatStyle.FULL);

    assertEquals("Monday, 9 May, 2022", result);
  }

  @Test
  void asDateDMYFullFR() {
    var date = generateDate();
    var dateInfo = DateInfo.DAY_MONTH_YEAR;

    when(localizationPrefs.getDateFormat()).thenReturn(dateInfo);
    when(dateFormatterUtil.getDMYFormatter(FormatStyle.FULL)).thenReturn(DateFormatterUtil.FORMATTER_DMY_FULL);
    when(i18n.getUserSpecificLocale()).thenReturn(localeFR);

    var result = service.asDate(date, FormatStyle.FULL);

    assertEquals("lundi, 9 mai, 2022", result);
  }


  private OffsetDateTime generateDate() {
    return OffsetDateTime.of(2022, 5, 9, 9, 9, 9, 9, ZoneOffset.UTC);
  }
}
