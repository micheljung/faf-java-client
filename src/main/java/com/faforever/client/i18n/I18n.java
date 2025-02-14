package com.faforever.client.i18n;

import com.faforever.client.preferences.DataPrefs;
import com.faforever.client.preferences.LocalizationPrefs;
import com.google.common.base.Strings;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.property.ReadOnlySetWrapper;
import javafx.collections.FXCollections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class I18n implements InitializingBean {
  private static final Pattern MESSAGES_FILE_PATTERN = Pattern.compile("(.*[/\\\\]messages)(?:_([a-z]{2}))(?:_([a-z]{2}))?\\.properties", Pattern.CASE_INSENSITIVE);

  private final ReloadableResourceBundleMessageSource messageSource;
  private final LocalizationPrefs localizationPrefs;
  private final DataPrefs dataPrefs;

  private final ReadOnlySetWrapper<Locale> availableLanguages = new ReadOnlySetWrapper<>(FXCollections.observableSet());

  private Locale userSpecificLocale;

  @Override
  public void afterPropertiesSet() throws IOException {
    Locale locale = localizationPrefs.getLanguage();
    if (locale != null) {
      userSpecificLocale = Locale.of(locale.getLanguage(), locale.getCountry());
      Locale.setDefault(userSpecificLocale);
    } else {
      userSpecificLocale = Locale.getDefault();
    }

    loadAvailableLanguages();
  }

  private void loadAvailableLanguages() throws IOException {
    // These are the default languages shipped with the client
    availableLanguages.addAll(Set.of(
        Locale.US,
        Locale.GERMAN,
        Locale.FRENCH,
        Locale.of("ru"),
        Locale.CHINESE,
        Locale.TRADITIONAL_CHINESE,
        Locale.of("cs"),
        Locale.of("es"),
        Locale.of("uk"),
        Locale.of("tr"),
        Locale.of("nl"),
        Locale.of("cat"),
        Locale.ITALIAN,
        Locale.of("pl")
    ));

    Path languagesDirectory = dataPrefs.getLanguagesDirectory();
    if (Files.notExists(languagesDirectory)) {
      return;
    }

    Set<String> currentBaseNames = messageSource.getBasenameSet();
    Set<String> newBaseNames = new LinkedHashSet<>();
    try (Stream<Path> dir = Files.list(languagesDirectory)) {
      dir
          .map(path -> MESSAGES_FILE_PATTERN.matcher(path.toString()))
          .filter(Matcher::matches)
          .forEach(matcher -> {
            newBaseNames.add(Path.of(matcher.group(1)).toUri().toString());
            availableLanguages.add(Locale.of(matcher.group(2), Strings.nullToEmpty(matcher.group(3))));
          });
    }
    // Make sure that current base names are added last; the files above have precedence
    newBaseNames.addAll(currentBaseNames);
    messageSource.setBasenames(newBaseNames.toArray(new String[0]));
  }

  public String get(String key, Object... args) {
    return get(userSpecificLocale, key, args);
  }

  public String get(Locale locale, String key, Object... args) {
    try {
      return messageSource.getMessage(key, args, locale);
    } catch (Exception e) {
      log.error("Could not load message `{}` with locale `{}` defaulting to US english", key, locale, e);
      return messageSource.getMessage(key, args, Locale.US);
    }
  }

  public String getOrDefault(String defaultMessage, String key, Object... args) {
    return getOrDefault(userSpecificLocale, defaultMessage, key, args);
  }

  public String getOrDefault(Locale locale, String defaultMessage, String key, Object... args) {
    try {
      return messageSource.getMessage(key, args, defaultMessage, locale);
    } catch (Exception e) {
      log.error("Could not load message `{}` with locale `{}` defaulting to US english", key, locale, e);
      return messageSource.getMessage(key, args, defaultMessage, Locale.US);
    }
  }

  public Locale getUserSpecificLocale() {
    return this.userSpecificLocale;
  }

  public String getCountryNameLocalized(String isoCode) {
    if (isoCode == null) {
      return "";
    }
    return Locale.of("", isoCode).getDisplayCountry(this.userSpecificLocale);
  }

  public String getQuantized(String singularKey, String pluralKey, long arg) {
    Object[] args = {arg};
    if (Math.abs(arg) == 1) {
      return messageSource.getMessage(singularKey, args, userSpecificLocale);
    }
    return messageSource.getMessage(pluralKey, args, userSpecificLocale);
  }

  public String number(Number number) {
    return rounded(number, 0);
  }

  public String numberWithSign(int number) {
    return String.format(userSpecificLocale, "%+d", number);
  }

  public String rounded(Number number, int digits) {
    return String.format(userSpecificLocale, "%." + digits + "f", number.doubleValue());
  }

  public ReadOnlySetProperty<Locale> getAvailableLanguages() {
    return availableLanguages.getReadOnlyProperty();
  }
}
