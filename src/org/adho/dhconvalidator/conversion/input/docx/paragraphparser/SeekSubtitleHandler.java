package org.adho.dhconvalidator.conversion.input.docx.paragraphparser;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;

class SeekSubtitleHandler implements StateHandler {

	@Override
	public State handleParagraph(Element paragraphElement, Document document, XPathContext xPathContext) {
		Nodes searchResult = paragraphElement.query("w:pPr/w:pStyle[@w:val='DH-Subtitle']", xPathContext);
		
		paragraphElement.getParent().removeChild(paragraphElement);

		if (searchResult.size()>0) {
			return State.FIRSTSUBTITLEFOUND;
		}
		
		return State.SEEKSUBTITLE;
	}

}
