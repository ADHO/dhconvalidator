package org.adho.dhconvalidator.conversion.input.docx.paragraphparser;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;

class SecondSubtitleFoundHandler implements StateHandler {

	@Override
	public State handleParagraph(Element paragraphElement, Document document,
			XPathContext xPathContext) {
		Nodes searchResult = paragraphElement.query("w:pPr/w:pStyle[@w:val='DH-AuthorAffiliation']", xPathContext);

		if (searchResult.size() > 0) {
			paragraphElement.getParent().removeChild(paragraphElement);
			return State.SECONDSUBTITLEFOUND;
		}
		return State.DONE;
	}

}
