package org.adho.dhconvalidator.conversion.input.docx.paragraphparser;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.XPathContext;

interface StateHandler {
	public State handleParagraph(Element paragraphElement, Document document, XPathContext xPathContext);
}
