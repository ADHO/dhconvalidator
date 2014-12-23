package org.adho.dhconvalidator.conversion.output;

import java.io.IOException;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.XPathContext;

import org.adho.dhconvalidator.conftool.Paper;
import org.adho.dhconvalidator.conftool.User;
import org.adho.dhconvalidator.conversion.TeiNamespace;
import org.adho.dhconvalidator.properties.PropertyKey;
import org.adho.dhconvalidator.util.DocumentUtil;
import org.adho.dhconvalidator.util.Pair;

public class CommonOutputConverter implements OutputConverter {
	
	protected XPathContext xPathContext;

	public CommonOutputConverter() {
		xPathContext = new XPathContext();
		xPathContext.addNamespace(TeiNamespace.TEI.getName(), TeiNamespace.TEI.toUri());
	}

	@Override
	public void convert(Document document, User user, Paper paper) throws IOException {
		
		makeAuthorStatement(document, paper);
//TODO: make editionStmt, sourceDesc, encodingDesc, remove revisionDesc
		makePublicationStmt(document);
	}

	private void makePublicationStmt(Document document) throws IOException {
		
		Element publicationStmtElement = DocumentUtil.getFirstMatch(
				document, 
				"/tei:TEI/tei:teiHeader/tei:fileDesc/tei:publicationStmt", 
				xPathContext);
		
		publicationStmtElement.removeChildren();
		try {
			Document publicationStmtDoc = 
				new Builder().build(
					PropertyKey.publicationStmt.getValue(), TeiNamespace.TEI.toUri());
			publicationStmtElement.getParent().replaceChild(
					publicationStmtElement, publicationStmtDoc.getRootElement().copy());
		}
		catch (ParsingException pe) {
			throw new IOException(pe);
		}
	}

	private void makeAuthorStatement(Document document, Paper paper) throws IOException {
		Element titleStmtElement = DocumentUtil.getFirstMatch(
				document, 
				"/tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt", 
				xPathContext);
		
		Element oldAuthor = 
				titleStmtElement.getFirstChildElement(
						"author", TeiNamespace.TEI.toUri());
		if (oldAuthor != null) {
			oldAuthor.getParent().removeChild(oldAuthor);
		}
		
		for (Pair<String,String> authorAffiliation : paper.getAuthorsAndAffiliations()) {
			String author = authorAffiliation.getFirst();
			if (author.contains(",")) {
				int splitPos = author.indexOf(",");
				String surname = author.substring(0, splitPos).trim();
				String forename = author.substring(splitPos+1, author.length());
				
				Element authorElement = new Element("author", TeiNamespace.TEI.toUri());
				
				Element surnameElement = new Element("surname", TeiNamespace.TEI.toUri());
				surnameElement.appendChild(surname);
				authorElement.appendChild(surnameElement);
				Element forenameElement = new Element("forename", TeiNamespace.TEI.toUri());
				forenameElement.appendChild(forename);
				authorElement.appendChild(forenameElement);
				
				Element affiliationElement = new Element("affiliation", TeiNamespace.TEI.toUri());
				affiliationElement.appendChild(authorAffiliation.getSecond());
				authorElement.appendChild(affiliationElement);
				
				titleStmtElement.appendChild(authorElement);
			}
		}
		
	}

}
