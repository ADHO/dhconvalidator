/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conversion;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.adho.dhconvalidator.Messages;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * An error handler for the validation against the DHConvalidator schema.
 *
 * @author marco.petris@web.de
 */
public class ValidateConversionErrorHandler implements ErrorHandler {
  private Logger logger = Logger.getLogger(ValidateConversionErrorHandler.class.getName());

  @Override
  public void error(SAXParseException arg0) throws SAXException {
    throw new SAXException(arg0);
  }

  @Override
  public void fatalError(SAXParseException arg0) throws SAXException {
    throw new SAXException(arg0);
  }

  @Override
  public void warning(SAXParseException arg0) throws SAXException {
    logger.log(
        Level.WARNING,
        Messages.getString("ValidateConversionErrorHandler.validationWarning"),
        arg0);
  }
}
