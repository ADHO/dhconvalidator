package org.adho.dhconvalidator.conversion;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

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
		logger.log(Level.WARNING, "warning during doc validation", arg0);
	}

}
