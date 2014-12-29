package org.adho.dhconvalidator.conversion.input.docx.paragraphparser;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.XPathContext;

public class InPermHandler implements StateHandler {

	@Override
	public State handleParagraph(Element paragraphElement, Document document,
			XPathContext xPathContext) {
		
		if (paragraphElement.getLocalName().equals("permEnd")) {
			return State.SEEKPERMSTART;
		}
		
		return State.INPERM;
	}

}
