/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conversion.input;

/**
 * A factory that creates {@link InputConverter}s.
 *
 * @author marco.petris@web.de
 */
public interface InputConverterFactory {
  /** @return the created input converter */
  public InputConverter createInputConverter();
}
