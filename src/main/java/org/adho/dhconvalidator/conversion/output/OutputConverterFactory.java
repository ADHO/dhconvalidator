/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conversion.output;

/**
 * A factory for Ouputconverters.
 *
 * @author marco.petris@web.de
 */
public interface OutputConverterFactory {
  /** @return the created OutputConverter. */
  public OutputConverter createOutputConverter();
}
