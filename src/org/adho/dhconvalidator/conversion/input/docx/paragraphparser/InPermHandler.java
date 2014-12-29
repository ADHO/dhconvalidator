package org.adho.dhconvalidator.conversion.input.docx.paragraphparser;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.XPathContext;

public class InPermHandler implements StateHandler {

	@Override
	public State handleParagraph(Element matchElement, Document document,
			XPathContext xPathContext) {
		
		if (matchElement.getLocalName().equals("permEnd")) {
			return State.SEEKPERMSTART;
		}
		
		return State.INPERM;
	}

}
