/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conversion.output;

import java.io.IOException;
import java.util.regex.Pattern;

import org.adho.dhconvalidator.Messages;
import org.adho.dhconvalidator.conversion.TeiNamespace;
import org.adho.dhconvalidator.conversion.oxgarage.ZipResult;
import org.adho.dhconvalidator.paper.Paper;
import org.adho.dhconvalidator.properties.PropertyKey;
import org.adho.dhconvalidator.user.User;
import org.adho.dhconvalidator.util.DocumentUtil;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;

/**
 * Converts the TEI that results from docx conversion.
 * 
 * @author marco.petris@web.de
 *
 */
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
		cleanupBoldAndItalicsRendition(document);
		removeFrontSection(document);
		makeQuotations(document);
		renameImageDir(document);
		fixFormulae(document);
		removeSegs(document);
	}
	
	/**
	 * Segments are not allowed so far, so we convert them to hi elements or remove the
	 * completely.
	 * @param document
	 */
	private void removeSegs(Document document) {
		Nodes searchResult = 
				document.query(
					"//tei:seg",  //$NON-NLS-1$
					xPathContext);
		for (int i=0; i<searchResult.size(); i++) {
			Element segElement = (Element)searchResult.get(i);
			Element parentElement = (Element)segElement.getParent();
			if (segElement.getAttribute("rend") != null) {//$NON-NLS-1$
				segElement.setLocalName("hi");	//$NON-NLS-1$
			}
			else {
				int position = parentElement.indexOf(segElement);
				for (int j=0; j<segElement.getChildCount(); j++) {
					parentElement.insertChild(segElement.getChild(j).copy(), position);
					position++;
				}
				parentElement.removeChild(segElement);
			}
		}		
	}

	//TODO: this should probably be fixed in the stylesheets
	/**
	 * Ensures that mathml formulae appear always within a formula element
	 * @param document
	 */
	private void fixFormulae(Document document) {
		Nodes searchResult = 
				document.query(
					"//mml:math",  //$NON-NLS-1$
					xPathContext);
		for (int i=0; i<searchResult.size(); i++) {
			Element mathElement = (Element)searchResult.get(i);
			Element parentElement = (Element)mathElement.getParent();
			if (!parentElement.getLocalName().equals("formula")) { //$NON-NLS-1$
				Element formulaElement = new Element("formula", TeiNamespace.TEI.toUri()); //$NON-NLS-1$
				parentElement.replaceChild(mathElement, formulaElement);
				formulaElement.appendChild(mathElement.copy());
			}
		}
	}

	private void renameImageDir(Document document) {
		Nodes searchResult = 
				document.query(
					"//tei:*[starts-with(@url, 'media/')]",  //$NON-NLS-1$
					xPathContext);
		
		for (int i=0; i<searchResult.size(); i++) {
			Element element = (Element)searchResult.get(i);
			Attribute urlAttr = element.getAttribute("url"); //$NON-NLS-1$
			urlAttr.setValue(
				urlAttr.getValue().replaceFirst(Pattern.quote("media/"), //$NON-NLS-1$
				PropertyKey.tei_image_location.getValue().substring(1) // skip leading slash
						+ "/")); //$NON-NLS-1$
		}
	}

	private void makeQuotations(Document document) {
		Nodes searchResult = 
				document.query(
					"//tei:p[@rend='DH-Quotation']",  //$NON-NLS-1$
					xPathContext);
		
		for (int i=0; i<searchResult.size(); i++) {
			Element element = (Element)searchResult.get(i);
			element.setLocalName("quote"); //$NON-NLS-1$
			element.removeAttribute(element.getAttribute("rend")); //$NON-NLS-1$
		}
	}

	private void removeFrontSection(Document document) {
		Element frontElement = DocumentUtil.tryFirstMatch(
				document, "//tei:front", xPathContext); //$NON-NLS-1$
		if (frontElement != null) {
			frontElement.getParent().removeChild(frontElement);
		}
	}

	private void cleanupBoldAndItalicsRendition(Document document) {
		Nodes searchResult = 
				document.query(
					"//*[@rend='Strong']",  //$NON-NLS-1$
					xPathContext);
		
		for (int i=0; i<searchResult.size(); i++) {
			Element element = (Element)searchResult.get(i);
			element.getAttribute("rend").setValue("bold"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		searchResult = 
				document.query(
					"//*[@rend='Emphasis']",  //$NON-NLS-1$
					xPathContext);
		
		for (int i=0; i<searchResult.size(); i++) {
			Element element = (Element)searchResult.get(i);
			element.getAttribute("rend").setValue("italic"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private void cleanupGraphics(Document document) {
		Nodes searchResult = 
				document.query(
					"//tei:graphic/tei:desc",  //$NON-NLS-1$
					xPathContext);
		
		for (int i=0; i<searchResult.size(); i++) {
			Element descElement = (Element)searchResult.get(i);
			descElement.getParent().removeChild(descElement);
		}
	}

	private void makeBibliography(Document document) {
		Nodes searchResult = 
				document.query(
					"//tei:p[@rend='DH-BibliographyHeading']",  //$NON-NLS-1$
					xPathContext);
		
		if (searchResult.size() == 1) {
			
			Element bibParagraphHeaderElement = (Element) searchResult.get(0);
			
			Element parent = (Element) bibParagraphHeaderElement.getParent();
			int startPosition = parent.indexOf(bibParagraphHeaderElement)+1;
			
			Elements children = parent.getChildElements();
			
			if (children.size() > startPosition) {
				Element textElement = 
						DocumentUtil.getFirstMatch(
								document, "/tei:TEI/tei:text", xPathContext); //$NON-NLS-1$
				
				Element backElement = 
						new Element("back", TeiNamespace.TEI.toUri()); //$NON-NLS-1$
				textElement.appendChild(backElement);
				
				Element divBibliogrElement = 
						new Element("div", TeiNamespace.TEI.toUri()); //$NON-NLS-1$
				divBibliogrElement.addAttribute(new Attribute("type", "bibliogr")); //$NON-NLS-1$ //$NON-NLS-2$
				backElement.appendChild(divBibliogrElement);
				
				Element listBiblElement = 
						new Element("listBibl", TeiNamespace.TEI.toUri()); //$NON-NLS-1$
				divBibliogrElement.appendChild(listBiblElement);
				
				Element listBiblHeadElement = 
						new Element("head", TeiNamespace.TEI.toUri()); //$NON-NLS-1$
				listBiblHeadElement.appendChild(Messages.getString(
						"Converter.bibliography")); //$NON-NLS-1$
				listBiblElement.appendChild(listBiblHeadElement);
				
			
				for (int i=startPosition; i<children.size(); i++) {
					Element bibEntryParagraphElement = children.get(i);
					
					bibEntryParagraphElement.getParent().removeChild(
							bibEntryParagraphElement);
					bibEntryParagraphElement.setLocalName("bibl"); //$NON-NLS-1$
					listBiblElement.appendChild(bibEntryParagraphElement);
				}
			}
			
			bibParagraphHeaderElement.getParent().removeChild(
					bibParagraphHeaderElement);
		}
	}


	/**
	 * We support only our styles.
	 * @param document
	 */
	private void cleanupParagraphRendAttribute(Document document) {
		Nodes searchResult = document.query("//tei:p[@rend='DH-Default']", xPathContext); //$NON-NLS-1$
		
		for (int i=0; i<searchResult.size(); i++) {
			Element paragraphElement = (Element)searchResult.get(i);
			paragraphElement.removeAttribute(paragraphElement.getAttribute("rend")); //$NON-NLS-1$
		}
	}

	/**
	 * We want numbered chapters.
	 * @param document
	 */
	private void makeChapterAttributes(Document document) {
		Element bodyElement = 
			DocumentUtil.getFirstMatch(
					document, 
					"/tei:TEI/tei:text/tei:body",  //$NON-NLS-1$
					xPathContext);
		
		insertChapterAttributes(bodyElement, 1);
		
	}

	private void insertChapterAttributes(Element parentElemnt, int depth) {
	
		Nodes searchResult = parentElemnt.query("tei:div/tei:head", xPathContext); //$NON-NLS-1$
		
		for (int i=0; i<searchResult.size(); i++) {
			Element chapterElement = (Element) searchResult.get(i).getParent();
			chapterElement.addAttribute(new Attribute("type", "div"+depth)); //$NON-NLS-1$ //$NON-NLS-2$
			chapterElement.addAttribute(new Attribute("rend", "DH-Heading"+depth)); //$NON-NLS-1$ //$NON-NLS-2$
			insertChapterAttributes(chapterElement, depth+1);
		}
	}

	private void makeComplexTitleStatement(Document document, Paper paper) {
		
		Element titleElement = 
				DocumentUtil.getFirstMatch(
						document, 
						"/tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title",  //$NON-NLS-1$
						xPathContext);
		
		Nodes subTitleResults = document.query(
				"//tei:p[@rend='DH-Subtitle']",  //$NON-NLS-1$
				xPathContext);
		
		if (subTitleResults.size() > 0) {
			String title = paper.getTitle();
			
			Element titleStmtElement = (Element) titleElement.getParent();
			Element complexTitle = new Element("title", TeiNamespace.TEI.toUri()); //$NON-NLS-1$
			complexTitle.addAttribute(new Attribute("type", "full")); //$NON-NLS-1$ //$NON-NLS-2$
			titleStmtElement.insertChild(complexTitle,0);
			
			titleElement.addAttribute(new Attribute("type", "main")); //$NON-NLS-1$ //$NON-NLS-2$
			titleElement.appendChild(title);
			
			titleElement.getParent().removeChild(titleElement);
			complexTitle.appendChild(titleElement);
			
			for (int i=0; i<subTitleResults.size(); i++) {
				Node subtitleNode = subTitleResults.get(i);
				
				Element subtitleElement = new Element("title", TeiNamespace.TEI.toUri()); //$NON-NLS-1$
				subtitleElement.addAttribute(new Attribute("type", "sub")); //$NON-NLS-1$ //$NON-NLS-2$
				subtitleElement.appendChild(subtitleNode.getValue());
				complexTitle.appendChild(subtitleElement);
				subtitleNode.getParent().removeChild(subtitleNode);
			}
		}
		else {
			titleElement.appendChild(paper.getTitle());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.adho.dhconvalidator.conversion.output.CommonOutputConverter#convert(org.adho.dhconvalidator.conversion.oxgarage.ZipResult)
	 */
	@Override
	public void convert(ZipResult zipResult) throws IOException {
		adjustImagePath(zipResult, "media", PropertyKey.tei_image_location.getValue().substring(1)); //$NON-NLS-1$
		super.convert(zipResult);
	}

}
