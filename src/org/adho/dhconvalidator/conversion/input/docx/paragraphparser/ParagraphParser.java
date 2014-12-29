package org.adho.dhconvalidator.conversion.input.docx.paragraphparser;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;

public class ParagraphParser {

	public void stripTemplateSections(Document document, XPathContext xPathContext) {
		Nodes searchResult = 
				document.query("/w:document/w:body/w:p | /w:document/w:body/w:permEnd", xPathContext);
		State currentState = State.SEEKPERMSTART;
		
		for (int i=0; i<searchResult.size(); i++) {
			Element paragraphElement = (Element)searchResult.get(i);
			currentState = currentState.getStateHandler().handleParagraph(
					paragraphElement, document, xPathContext);
		}
	}
}
