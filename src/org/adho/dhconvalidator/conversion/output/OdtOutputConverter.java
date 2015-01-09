package org.adho.dhconvalidator.conversion.output;

import java.io.IOException;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;

import org.adho.dhconvalidator.conftool.Paper;
import org.adho.dhconvalidator.conftool.User;
import org.adho.dhconvalidator.conversion.TeiNamespace;
import org.adho.dhconvalidator.conversion.oxgarage.ZipResult;
import org.adho.dhconvalidator.properties.PropertyKey;
import org.adho.dhconvalidator.util.DocumentUtil;

public class OdtOutputConverter extends CommonOutputConverter {
	
	@Override
	public void convert(Document document, User user, Paper paper) throws IOException {
		super.convert(document, user, paper);
		
		makeComplexTitleStatement(document);
		makeBibliography(document);
	}

	private void makeBibliography(Document document) {
		Nodes searchResult = 
				document.query(
					"//tei:div[@type='div1' and @rend='DH-BibliographyHeading']", 
					xPathContext);
		
		if (searchResult.size() == 1) {
			Element bibDivContainerElement = (Element) searchResult.get(0);
			
			Elements bibParagrElements = 
				bibDivContainerElement.getChildElements("p", TeiNamespace.TEI.toUri());
			
			Element textElement = 
					DocumentUtil.getFirstMatch(
							document, "/tei:TEI/tei:text", xPathContext);
			
			Element backElement = new Element("back", TeiNamespace.TEI.toUri());
			textElement.appendChild(backElement);
			
			Element divBibliogrElement = new Element("div", TeiNamespace.TEI.toUri());
			divBibliogrElement.addAttribute(new Attribute("type", "bibliogr"));
			backElement.appendChild(divBibliogrElement);
			
			Element listBiblElement = new Element("listBibl", TeiNamespace.TEI.toUri());
			divBibliogrElement.appendChild(listBiblElement);
			
			Element listBiblHeadElement = new Element("head", TeiNamespace.TEI.toUri());
			listBiblHeadElement.appendChild("Bibliography");
			listBiblElement.appendChild(listBiblHeadElement);
			
			for (int i=0; i<bibParagrElements.size(); i++) {
				Element bibElement = bibParagrElements.get(i);
				bibElement.getParent().removeChild(bibElement);
				bibElement.setLocalName("bibl");
				listBiblElement.appendChild(bibElement);
			}
			
			bibDivContainerElement.getParent().removeChild(bibDivContainerElement);
		}
	}

	private void makeComplexTitleStatement(Document document) {
		Nodes searchResult = 
				document.query("//tei:head[@type='subtitle']", xPathContext);
		
		if (searchResult.size() > 0) {
			Element titleElement = 
				DocumentUtil.getFirstMatch(
					document, 
					"/tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title", 
					xPathContext);
			
			Element titleStmtElement = (Element) titleElement.getParent();
			Element complexTitle = new Element("title", TeiNamespace.TEI.toUri());
			complexTitle.addAttribute(new Attribute("type", "full"));
			titleStmtElement.insertChild(complexTitle,0);
			
			titleElement.addAttribute(new Attribute("type", "main"));
			titleElement.getParent().removeChild(titleElement);
			complexTitle.appendChild(titleElement);
			
			for (int i=0; i<searchResult.size(); i++) {
				Element pseudoSubtitleElement = (Element) searchResult.get(i);
				pseudoSubtitleElement.getParent().removeChild(pseudoSubtitleElement);
				Element subtitleElement = new Element("title", TeiNamespace.TEI.toUri());
				subtitleElement.addAttribute(new Attribute("type", "sub"));
				subtitleElement.appendChild(pseudoSubtitleElement.getValue());
				complexTitle.appendChild(subtitleElement);
			}
		}
	}

	@Override
	public void convert(ZipResult zipResult) throws IOException {
		adjustImagePath(zipResult, "Pictures", PropertyKey.tei_image_location.getValue().substring(1));
		super.convert(zipResult);
	}
}
