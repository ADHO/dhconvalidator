package org.adho.dhconvalidator.conversion.output;

import java.io.IOException;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.adho.dhconvalidator.conftool.Paper;
import org.adho.dhconvalidator.conftool.User;
import org.adho.dhconvalidator.conversion.TeiNamespace;
import org.adho.dhconvalidator.util.DocumentUtil;

public class OdtOutputConverter extends CommonOutputConverter {
	
	@Override
	public void convert(Document document, User user, Paper paper) throws IOException {
		super.convert(document, user, paper);
		
		makeComplexTitleStatement(document);
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
			titleElement.removeNamespaceDeclaration("tei");
			
			Element titleStmtElement = (Element) titleElement.getParent();
			Element complexTitle = new Element("title", TeiNamespace.TEI.toUri());
			complexTitle.addAttribute(new Attribute("type", "full"));
			titleStmtElement.appendChild(complexTitle);
			
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

}
