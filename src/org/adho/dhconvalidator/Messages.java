/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Provides the strings for the UI and error messages.
 *
 * @author marco.petris@web.de
 */
public class Messages {

  /** A provider for a session specific locale. */
  public static interface LocaleProvider {
    public Locale getLocale();
  }

  private static final String BUNDLE_NAME = "org.adho.dhconvalidator.messages"; // $NON-NLS-1$

  private static LocaleProvider localeProvider;

  private Messages() {}

  public static String getString(String key) {
    try {
      Locale locale = null;

      if (localeProvider != null) {
        locale = localeProvider.getLocale();
      }

      if (locale == null) {
        locale = Locale.ENGLISH;
      }

      return ResourceBundle.getBundle(BUNDLE_NAME, locale).getString(key);
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }

  public static String getString(String key, Object... params) {
    try {
      Locale locale = null;

      if (localeProvider != null) {
        locale = localeProvider.getLocale();
      }

      if (locale == null) {
        locale = Locale.ENGLISH;
      }

      return MessageFormat.format(
          ResourceBundle.getBundle(BUNDLE_NAME, locale).getString(key), params);
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }

  public static void setLocaleProvider(LocaleProvider localeProvider) {
    Messages.localeProvider = localeProvider;
  }
}
