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
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
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

  @InjectMocks
  TimeService service;

  @BeforeEach
  void setUp() {

  }

  @Test
  void asDateAuto() {
    var date = generateDate();
    var dateInfo = DateInfo.AUTO;
    var locale = Locale.of("en", "US");

    when(localizationPrefs.getDateFormat()).thenReturn(dateInfo);
    when(i18n.getUserSpecificLocale()).thenReturn(locale);

    var result = service.asDate(date, FormatStyle.MEDIUM);

    assertEquals(result, "Sep 9, 2022");
  }

  @Test
  void asDateMonthDayYear() {
    var date = generateDate();
    var dateInfo = DateInfo.MONTH_DAY_YEAR;
    var locale = Locale.of("en", "US");

    when(localizationPrefs.getDateFormat()).thenReturn(dateInfo);
    when(i18n.getUserSpecificLocale()).thenReturn(locale);

    var result = service.asDate(date, FormatStyle.MEDIUM);

    assertEquals(result, "Sep 9, 2022");
  }

  @Test
  void asDateDayMonthYear() {
    var date = generateDate();
    var dateInfo = DateInfo.DAY_MONTH_YEAR;
    var locale = Locale.of("en", "US");

    when(localizationPrefs.getDateFormat()).thenReturn(dateInfo);
    when(i18n.getUserSpecificLocale()).thenReturn(locale);

    var result = service.asDate(date, FormatStyle.MEDIUM);

    assertEquals(result, "9 Sep, 2022");
  }


  private OffsetDateTime generateDate() {
    return OffsetDateTime.of(2022, 9, 9, 9, 9, 9, 9, ZoneOffset.UTC);
  }
}
