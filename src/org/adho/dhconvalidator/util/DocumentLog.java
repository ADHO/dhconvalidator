/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.util;

import org.adho.dhconvalidator.properties.PropertyKey;

/**
 * Log conversion results.
 *
 * @author marco.petris@web.de
 */
public class DocumentLog {

  public static void logConversionStepOutput(String step, String output) {
    if (PropertyKey.logConversionStepOutput.isTrue()) {
      System.out.println(step);
      System.out.println(output);
    }
  }
}
