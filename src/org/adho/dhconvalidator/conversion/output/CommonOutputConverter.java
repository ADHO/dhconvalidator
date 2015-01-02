package org.adho.dhconvalidator.conversion.output;

import java.io.IOException;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
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
		makePublicationStmt(document);
		makeEncodingDesc(document);
		makeProfileDesc(document, paper);
		removeRevisions(document);
	}

	private void makeProfileDesc(Document document, Paper paper) {
		Element headerElement = DocumentUtil.getFirstMatch(
				document, 
				"/tei:TEI/tei:teiHeader", 
				xPathContext);
		
		Element oldProfileDescElement = 
				headerElement.getFirstChildElement("profileDesc", TeiNamespace.TEI.toUri());

		Element profileDescElement = new Element("profileDesc", TeiNamespace.TEI.toUri());
		
		Element textClassElement = new Element("textClass", TeiNamespace.TEI.toUri());
		profileDescElement.appendChild(textClassElement);
		
		Element keywordsCategoryElement = new Element("keywords", TeiNamespace.TEI.toUri());
		keywordsCategoryElement.addAttribute(new Attribute("scheme", "ConfTool"));
		keywordsCategoryElement.addAttribute(new Attribute("n", "category"));
		Element paperTermElement = new Element("term", TeiNamespace.TEI.toUri());
		paperTermElement.appendChild("Paper");
		keywordsCategoryElement.appendChild(paperTermElement);
		textClassElement.appendChild(keywordsCategoryElement);
		
		Element keywordsSubcategoryElement = new Element("keywords", TeiNamespace.TEI.toUri());
		keywordsSubcategoryElement.addAttribute(new Attribute("scheme", "ConfTool"));
		keywordsSubcategoryElement.addAttribute(new Attribute("n", "subcategory"));
		Element confToolTypeTermElement = new Element("term", TeiNamespace.TEI.toUri());
		confToolTypeTermElement.appendChild(paper.getContributionType());
		keywordsSubcategoryElement.appendChild(confToolTypeTermElement);
		textClassElement.appendChild(keywordsSubcategoryElement);

		if (paper.getKeywords().size() > 0) {
			Element confToolKeywordsElement = new Element("keywords", TeiNamespace.TEI.toUri());
			confToolKeywordsElement.addAttribute(new Attribute("scheme", "ConfTool"));
			confToolKeywordsElement.addAttribute(new Attribute("n", "keywords"));
			textClassElement.appendChild(confToolKeywordsElement);
	
			for (String keyword : paper.getKeywords()) {
				Element confToolKeywordsTermElement = new Element("term", TeiNamespace.TEI.toUri());
				confToolKeywordsTermElement.appendChild(keyword);
				confToolKeywordsElement.appendChild(confToolKeywordsTermElement);
			}
		}	
		
		if (paper.getTopics().size() > 0) {
			Element confToolTopicsElement = new Element("keywords", TeiNamespace.TEI.toUri());
			confToolTopicsElement.addAttribute(new Attribute("scheme", "ConfTool"));
			confToolTopicsElement.addAttribute(new Attribute("n", "topics"));
			textClassElement.appendChild(confToolTopicsElement);
	
			for (String topic : paper.getTopics()) {
				Element confToolTopicsTermElement = new Element("term", TeiNamespace.TEI.toUri());
				confToolTopicsTermElement.appendChild(topic);
				confToolTopicsElement.appendChild(confToolTopicsTermElement);
			}
		}	
		
		if (oldProfileDescElement != null) {
			headerElement.replaceChild(
				oldProfileDescElement, profileDescElement);
		}
		else {
			headerElement.appendChild(profileDescElement);
		}
	}

	private void makeEncodingDesc(Document document) throws IOException {
		String version = PropertyKey.version.getValue();
		
		Element headerElement = DocumentUtil.getFirstMatch(
				document, 
				"/tei:TEI/tei:teiHeader", 
				xPathContext);
		
		Element oldEncodingDesc = 
				headerElement.getFirstChildElement("encodingDesc", TeiNamespace.TEI.toUri());
		
		try {
			Document encodingDescDoc = 
				new Builder().build(
					PropertyKey.encodingDesc.getValue().replace("{VERSION}", version),
					TeiNamespace.TEI.toUri());
			if (oldEncodingDesc != null) {
				headerElement.replaceChild(
					oldEncodingDesc, encodingDescDoc.getRootElement().copy());
			}
			else {
				headerElement.appendChild( encodingDescDoc.getRootElement().copy());
			}
		}
		catch (ParsingException pe) {
			throw new IOException(pe);
		}
		
	}

	private void removeRevisions(Document document) {
		Nodes searchResult = document.query(
				"/tei:TEI/tei:teiHeader/tei:revisionDesc", 
				xPathContext);
		
		if (searchResult.size() > 0) {
			searchResult.get(0).getParent().removeChild(searchResult.get(0));
		}
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
				Element persNameElement = new Element("persName", TeiNamespace.TEI.toUri());
				authorElement.appendChild(persNameElement);
				
				Element surnameElement = new Element("surname", TeiNamespace.TEI.toUri());
				surnameElement.appendChild(surname);
				persNameElement.appendChild(surnameElement);
				Element forenameElement = new Element("forename", TeiNamespace.TEI.toUri());
				forenameElement.appendChild(forename);
				persNameElement.appendChild(forenameElement);
				
				Element affiliationElement = new Element("affiliation", TeiNamespace.TEI.toUri());
				affiliationElement.appendChild(authorAffiliation.getSecond());
				authorElement.appendChild(affiliationElement);
				
				titleStmtElement.appendChild(authorElement);
			}
		}
		
	}

}
