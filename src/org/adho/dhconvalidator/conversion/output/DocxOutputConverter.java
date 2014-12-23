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
import org.adho.dhconvalidator.util.DocumentUtil;

public class DocxOutputConverter extends CommonOutputConverter {

	@Override
	public void convert(Document document, User user, Paper paper)
			throws IOException {
		super.convert(document, user, paper);
		
		makeComplexTitleStatement(document, paper);
		makeChapterAttributes(document);
		cleanupParagraphRendAttribute(document);
		cleanupGraphics(document);
		
		makeBibliography(document);
		cleanupBoldRendition(document);
	}
	
	private void cleanupBoldRendition(Document document) {
		Nodes searchResult = 
				document.query(
					"//*[@rend='Strong']", 
					xPathContext);
		
		for (int i=0; i<searchResult.size(); i++) {
			Element element = (Element)searchResult.get(i);
			element.getAttribute("rend").setValue("bold");
		}
	}

	private void cleanupGraphics(Document document) {
		Nodes searchResult = 
				document.query(
					"//tei:graphic/tei:desc", 
					xPathContext);
		
		for (int i=0; i<searchResult.size(); i++) {
			Element descElement = (Element)searchResult.get(i);
			descElement.getParent().removeChild(descElement);
		}
	}

	private void makeBibliography(Document document) {
		Nodes searchResult = 
				document.query(
					"//tei:p[@rend='DH-BibliographyHeading']", 
					xPathContext);
		
		if (searchResult.size() == 1) {
			
			Element bibParagraphHeaderElement = (Element) searchResult.get(0);
			
			Element parent = (Element) bibParagraphHeaderElement.getParent();
			int startPosition = parent.indexOf(bibParagraphHeaderElement)+1;
			
			Elements children = parent.getChildElements();
			
			if (children.size() >= startPosition) {
				Element textElement = 
						DocumentUtil.getFirstMatch(
								document, "/tei:TEI/tei:text", xPathContext);
				
				Element backElement = 
						new Element("back", TeiNamespace.TEI.toUri());
				textElement.appendChild(backElement);
				
				Element divBibliogrElement = 
						new Element("div", TeiNamespace.TEI.toUri());
				divBibliogrElement.addAttribute(new Attribute("type", "bibliogr"));
				backElement.appendChild(divBibliogrElement);
				
				Element listBiblElement = 
						new Element("listBibl", TeiNamespace.TEI.toUri());
				divBibliogrElement.appendChild(listBiblElement);
				
				Element listBiblHeadElement = 
						new Element("head", TeiNamespace.TEI.toUri());
				listBiblHeadElement.appendChild("Bibliography");
				listBiblElement.appendChild(listBiblHeadElement);
				
			
				for (int i=startPosition; i<children.size(); i++) {
					Element bibEntryParagraphElement = children.get(i);
					
					bibEntryParagraphElement.getParent().removeChild(
							bibEntryParagraphElement);
					bibEntryParagraphElement.setLocalName("bibl");
					listBiblElement.appendChild(bibEntryParagraphElement);
				}
			}
			
			bibParagraphHeaderElement.getParent().removeChild(
					bibParagraphHeaderElement);
		}
	}


	private void cleanupParagraphRendAttribute(Document document) {
		Nodes searchResult = document.query("//tei:p[@rend='DH-Default']", xPathContext);
		
		for (int i=0; i<searchResult.size(); i++) {
			Element paragraphElement = (Element)searchResult.get(i);
			paragraphElement.removeAttribute(paragraphElement.getAttribute("rend"));
		}
	}

	private void makeChapterAttributes(Document document) {
		Element bodyElement = 
			DocumentUtil.getFirstMatch(
					document, 
					"/tei:TEI/tei:text/tei:body", 
					xPathContext);
		
		insertChapterAttributes(bodyElement, 1);
		
	}

	private void insertChapterAttributes(Element parentElemnt, int depth) {
	
		Nodes searchResult = parentElemnt.query("tei:div/tei:head", xPathContext);
		
		for (int i=0; i<searchResult.size(); i++) {
			Element chapterElement = (Element) searchResult.get(i).getParent();
			chapterElement.addAttribute(new Attribute("type", "div"+depth));
			chapterElement.addAttribute(new Attribute("rend", "DH-Heading"+depth));
			insertChapterAttributes(chapterElement, depth+1);
		}
	}

	private void makeComplexTitleStatement(Document document, Paper paper) {
		
		Element titleElement = 
				DocumentUtil.getFirstMatch(
						document, 
						"/tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title", 
						xPathContext);
		
		Element frontElement = 
			DocumentUtil.getFirstMatch(document, "/tei:TEI/tei:text/tei:front", xPathContext);
		
		Element titlePageElement =
			frontElement.getFirstChildElement("titlePage", TeiNamespace.TEI.toUri());
		

		if (titlePageElement.getChildElements().size() > 0) {
			Element titlePart =
				DocumentUtil.getFirstMatch(
					titlePageElement,
					"tei:docTitle/tei:titlePart", 
					xPathContext);
			String subtitle = titlePart.getValue();
			
			String title = paper.getTitle();
			
			Element titleStmtElement = (Element) titleElement.getParent();
			Element complexTitle = new Element("title", TeiNamespace.TEI.toUri());
			complexTitle.addAttribute(new Attribute("type", "full"));
			titleStmtElement.appendChild(complexTitle);
			
			titleElement.addAttribute(new Attribute("type", "main"));
			titleElement.appendChild(title);
			
			titleElement.getParent().removeChild(titleElement);
			complexTitle.appendChild(titleElement);

			Element subtitleElement = new Element("title", TeiNamespace.TEI.toUri());
			subtitleElement.addAttribute(new Attribute("type", "sub"));
			subtitleElement.appendChild(subtitle);
			complexTitle.appendChild(subtitleElement);
		}
		else {
			titleElement.appendChild(paper.getTitle());
		}
		
		frontElement.getParent().removeChild(frontElement);
	}
}
