/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conversion.input.docx.paragraphparser;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.XPathContext;

/**
 * A handler for Paragraphs while in a specific State.
 * 
 * @author marco.petris@web.de
 *
 */
interface StateHandler {
	/**
	 * @param matchElement the element that matched the original query (p or permEnd)
	 * @param document the docx document
	 * @param xPathContext query context
	 * @return the next current State
	 */
	public State handleParagraph(Element matchElement, Document document, XPathContext xPathContext);
}
