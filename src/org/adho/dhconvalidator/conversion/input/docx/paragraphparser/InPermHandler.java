/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conversion.input.docx.paragraphparser;

import org.adho.dhconvalidator.conversion.input.docx.DocxInputConverter.Namespace;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.XPathContext;

/**
 * Handles paragraphs in {@link State#INPERM}.
 * @author marco.petris@web.de
 *
 */
public class InPermHandler implements StateHandler {

	@Override
	public State handleParagraph(Element matchElement, Document document,
			XPathContext xPathContext) {
		
		// are we still in an editable section?
		if (matchElement.getLocalName().equals("permEnd") //$NON-NLS-1$
				|| matchElement.getFirstChildElement("permEnd", Namespace.MAIN.toUri()) != null) { //$NON-NLS-1$
			// editable section ends here, start searching for the next one
			return State.SEEKPERMSTART;
		}
		
		return State.INPERM; // still within an editable section
	}

}
