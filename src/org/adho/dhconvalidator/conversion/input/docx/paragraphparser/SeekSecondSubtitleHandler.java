package org.adho.dhconvalidator.conversion.input.docx.paragraphparser;

import org.adho.dhconvalidator.conversion.input.docx.DocxInputConverter;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;

class SeekSecondSubtitleHandler implements StateHandler {

	@Override
	public State handleParagraph(Element paragraphElement, Document document, XPathContext xPathContext) {
		Nodes searchResult = paragraphElement.query("w:pPr/w:pStyle[@w:val='DH-Subtitle']", xPathContext);
		

		if (searchResult.size()>0) {
			
			Element styleElement = (Element)searchResult.get(0);
			
			//we put the subtitle as a preliminary title and handle the 
			// title stuff later when merging with the ConfTool data
			styleElement.getAttribute(
					"val", 
					DocxInputConverter.Namespace.MAIN.toUri()).setValue("Title");
			
			return State.SECONDSUBTITLEFOUND; //TODO: should we support more than one subtitle, probably not...
		}
		
		paragraphElement.getParent().removeChild(paragraphElement);
		
		return State.FIRSTSUBTITLEFOUND;

	}

}
