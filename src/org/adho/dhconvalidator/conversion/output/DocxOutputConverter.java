package org.adho.dhconvalidator.conversion.output;

import java.io.IOException;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;

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
