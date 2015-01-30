/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
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

/**
 * Converts the TEI that results from odt conversion.
 * 
 * @author marco.petris@web.de
 *
 */
public class OdtOutputConverter extends CommonOutputConverter {
	
	/* (non-Javadoc)
	 * @see org.adho.dhconvalidator.conversion.output.CommonOutputConverter#convert(nu.xom.Document, org.adho.dhconvalidator.conftool.User, org.adho.dhconvalidator.conftool.Paper)
	 */
	@Override
	public void convert(Document document, User user, Paper paper) throws IOException {
		super.convert(document, user, paper);
		
		makeComplexTitleStatement(document);
		makeBibliography(document);
	}

	/**
	 * Creates a proper back matter bibliography.
	 * @param document
	 */
	private void makeBibliography(Document document) {
		Nodes searchResult = 
				document.query(
					"//tei:div[@type='div1' and @rend='DH-BibliographyHeading']",  //$NON-NLS-1$
					xPathContext);
		
		if (searchResult.size() == 1) {
			Element bibDivContainerElement = (Element) searchResult.get(0);
			
			Elements bibParagrElements = 
				bibDivContainerElement.getChildElements("p", TeiNamespace.TEI.toUri()); //$NON-NLS-1$
			if (bibParagrElements.size() > 0) {
				Element textElement = 
						DocumentUtil.getFirstMatch(
								document, "/tei:TEI/tei:text", xPathContext); //$NON-NLS-1$
				
				Element backElement = new Element("back", TeiNamespace.TEI.toUri()); //$NON-NLS-1$
				textElement.appendChild(backElement);
				
				Element divBibliogrElement = new Element("div", TeiNamespace.TEI.toUri()); //$NON-NLS-1$
				divBibliogrElement.addAttribute(new Attribute("type", "bibliogr")); //$NON-NLS-1$ //$NON-NLS-2$
				backElement.appendChild(divBibliogrElement);
				
				Element listBiblElement = new Element("listBibl", TeiNamespace.TEI.toUri()); //$NON-NLS-1$
				divBibliogrElement.appendChild(listBiblElement);
				
				Element listBiblHeadElement = new Element("head", TeiNamespace.TEI.toUri()); //$NON-NLS-1$
				listBiblHeadElement.appendChild("Bibliography"); //$NON-NLS-1$
				listBiblElement.appendChild(listBiblHeadElement);
				
				for (int i=0; i<bibParagrElements.size(); i++) {
					Element bibElement = bibParagrElements.get(i);
					bibElement.getParent().removeChild(bibElement);
					bibElement.setLocalName("bibl"); //$NON-NLS-1$
					listBiblElement.appendChild(bibElement);
				}
			}			
			bibDivContainerElement.getParent().removeChild(bibDivContainerElement);
		}
	}

	/**
	 * Creates the title statement with optional subtitle
	 * @param document
	 */
	private void makeComplexTitleStatement(Document document) {
		Nodes searchResult = 
				document.query("//tei:head[@type='subtitle']", xPathContext); //$NON-NLS-1$
		
		if (searchResult.size() > 0) {
			Element titleElement = 
				DocumentUtil.getFirstMatch(
					document, 
					"/tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title",  //$NON-NLS-1$
					xPathContext);
			
			Element titleStmtElement = (Element) titleElement.getParent();
			Element complexTitle = new Element("title", TeiNamespace.TEI.toUri()); //$NON-NLS-1$
			complexTitle.addAttribute(new Attribute("type", "full")); //$NON-NLS-1$ //$NON-NLS-2$
			titleStmtElement.insertChild(complexTitle,0);
			
			titleElement.addAttribute(new Attribute("type", "main")); //$NON-NLS-1$ //$NON-NLS-2$
			titleElement.getParent().removeChild(titleElement);
			complexTitle.appendChild(titleElement);
			
			for (int i=0; i<searchResult.size(); i++) {
				Element pseudoSubtitleElement = (Element) searchResult.get(i);
				pseudoSubtitleElement.getParent().removeChild(pseudoSubtitleElement);
				Element subtitleElement = new Element("title", TeiNamespace.TEI.toUri()); //$NON-NLS-1$
				subtitleElement.addAttribute(new Attribute("type", "sub")); //$NON-NLS-1$ //$NON-NLS-2$
				subtitleElement.appendChild(pseudoSubtitleElement.getValue());
				complexTitle.appendChild(subtitleElement);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.adho.dhconvalidator.conversion.output.CommonOutputConverter#convert(org.adho.dhconvalidator.conversion.oxgarage.ZipResult)
	 */
	@Override
	public void convert(ZipResult zipResult) throws IOException {
		adjustImagePath(zipResult, "Pictures", PropertyKey.tei_image_location.getValue().substring(1)); //$NON-NLS-1$
		super.convert(zipResult);
	}
}
