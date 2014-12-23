package org.adho.dhconvalidator.conversion.input.docx;

import java.io.IOException;
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import org.adho.dhconvalidator.conftool.ConfToolCacheProvider;
import org.adho.dhconvalidator.conftool.Paper;
import org.adho.dhconvalidator.conftool.User;
import org.adho.dhconvalidator.conversion.Type;
import org.adho.dhconvalidator.conversion.ZipFs;
import org.adho.dhconvalidator.conversion.input.InputConverter;
import org.adho.dhconvalidator.conversion.input.docx.paragraphparser.ParagaphParser;
import org.adho.dhconvalidator.util.DocumentUtil;
import org.adho.dhconvalidator.util.Pair;

public class DocxInputConverter implements InputConverter {
	public enum Namespace {
		MAIN("w", "http://schemas.openxmlformats.org/wordprocessingml/2006/main"),
		DOCPROPSVTYPES("vt", "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes"),
		;
		private String name;
		private String uri;

		private Namespace(String name, String uri) {
			this.name = name;
			this.uri = uri;
		}

		public String toUri() {
			return uri;
		}
		
		public String getName() {
			return name;
		}
	}

	private static final String TEMPLATE = "template/DH_template_v3.docx";
	
	private XPathContext xPathContext;
	private Paper paper;

	
	public DocxInputConverter() {
		xPathContext = new XPathContext();
		for (Namespace ns : Namespace.values()) {
			xPathContext.addNamespace(
				ns.getName(),
				ns.toUri());
		}
	}
	
	@Override
	public byte[] convert(byte[] sourceData, User user) throws IOException {
		ZipFs zipFs = new ZipFs(sourceData);

		Document document = zipFs.getDocument("word/document.xml");

		cleanupParagraphStyles(document);
		stripTemplateSections(document);

		zipFs.putDocument("word/document.xml", document);
		
		Document customPropDoc = zipFs.getDocument("docProps/custom.xml");
		Integer paperId = getPaperIdFromMeta(customPropDoc);
		paper = ConfToolCacheProvider.INSTANCE.getConfToolCache().getPaper(user, paperId);

		return zipFs.toZipData();
	}

	private void stripTemplateSections(Document document) {
		ParagaphParser paragaphParser = new ParagaphParser();
		paragaphParser.stripTemplateSections(document, xPathContext);
	}

	private void cleanupParagraphStyles(Document document) {
		Nodes searchResult = 
				document.query("//w:pPr/w:rPr", xPathContext);
		// remove region properties from paragraphs, they are not supported 
		for (int i=0; i<searchResult.size(); i++) {
			Element element = (Element) searchResult.get(i);
			element.getParent().removeChild(element);
		}

		searchResult = document.query("//w:p", xPathContext);
		
		for (int i=0; i<searchResult.size(); i++) {
			Element paragraphElement = (Element) searchResult.get(i);
			Elements regions = paragraphElement.getChildElements("r", Namespace.MAIN.toUri());
			// there is only one region...
			if (regions.size() == 1) {
				Element region = regions.get(0);
				Element regionProps = region.getFirstChildElement("rPr", Namespace.MAIN.toUri());
				if (regionProps != null) {
					// ... and it contains region properties. This looks like a trick to avoid proper head styles
					// so we remove the properties.
					region.removeChild(regionProps);
				}
			}
		}		
		
	}

	private Integer getPaperIdFromMeta(Document customPropDoc) throws IOException {
		Element propertyElement = 
				DocumentUtil.getFirstMatch(
					customPropDoc, 
					"//*[@name='ConfToolPaperID']/vt:lpwstr", 
					xPathContext);
		if (propertyElement == null) {
			throw new IOException("document property ConfToolPaperID not found");
		}
		try {
			return Integer.valueOf(propertyElement.getValue());
		}
		catch (NumberFormatException nfe) {
			throw new IOException("invalid ConfToolPaperID", nfe);

		}
	}

	@Override
	public byte[] getPersonalizedTemplate(Paper paper) throws IOException {
		ZipFs zipFs = 
			new ZipFs(
				Thread.currentThread().getContextClassLoader().getResourceAsStream(TEMPLATE));
		Document document = zipFs.getDocument("word/document.xml");
		
		injectTitleIntoContent(document, paper.getTitle());
		injectAuthorsIntoContent(document, paper.getAuthorsAndAffiliations());
		
		zipFs.putDocument("word/document.xml", document);

		
		Document customPropDoc = zipFs.getDocument("docProps/custom.xml");
		
		injectPaperIdIntoMeta(customPropDoc, paper.getPaperId());
		
		zipFs.putDocument("docProps/custom.xml", customPropDoc);
		
		return zipFs.toZipData();
	}

	private void injectPaperIdIntoMeta(Document customPropDoc, Integer paperId) {
		Element propertyElement = 
			DocumentUtil.getFirstMatch(
				customPropDoc, 
				"//*[@name='ConfToolPaperID']/vt:lpwstr", 
				xPathContext);

		propertyElement.removeChildren();
		propertyElement.appendChild(String.valueOf(paperId));
	}

	private void injectAuthorsIntoContent(Document document,
			List<Pair<String, String>> authorsAndAffiliations) throws IOException {
		Nodes searchResult = 
				document.query("//w:pStyle[@w:val='DH-AuthorAffiliation']", xPathContext);
		
		if (searchResult.size() != 1) {
			throw new IOException(
				"template error, could not find exactly one author/affiliation style element, "
				+ "found " + searchResult.size());
		}
		
		Element authorStyleElement = (Element) searchResult.get(0);
		Element authorParagraphElement = (Element) authorStyleElement.getParent().getParent();
		Element paragraphParent = (Element) authorParagraphElement.getParent();
		int insertPosition = paragraphParent.indexOf(authorParagraphElement)-1;
		
		for (Pair<String,String> authorAffiliation : authorsAndAffiliations){
			Element curAuthorParagraphElement = (Element) authorParagraphElement.copy();
			
			Element authorElement = DocumentUtil.getFirstMatch(
					curAuthorParagraphElement, "w:r/w:t", xPathContext);
			
			authorElement.removeChildren();
			authorElement.appendChild(
				authorAffiliation.getFirst() + ", " + authorAffiliation.getSecond());
			paragraphParent.insertChild(curAuthorParagraphElement, insertPosition);
			insertPosition++;
		}
		
		paragraphParent.removeChild(authorParagraphElement);
		
	}

	private void injectTitleIntoContent(Document document, String title) throws IOException {
		Nodes searchResult = document.query("//w:pStyle[@w:val='DH-Title']", xPathContext);
		
		if (searchResult.size() != 2) {
			throw new IOException(
				"template error, could not find 2 title style elements, found " 
						+ searchResult.size());
		}
		
		Element titleStyleElement = (Element) searchResult.get(1);// we want the second hit
		Element titlParagraphElement = (Element) titleStyleElement.getParent().getParent();
		
		Element titleElement = DocumentUtil.getFirstMatch(
				titlParagraphElement, "w:r/w:t", xPathContext);
		
		titleElement.removeChildren();
		titleElement.appendChild(title);
		
	}

	@Override
	public String getFileExtension() {
		return Type.DOCX.getExtension();
	}

	@Override
	public Paper getPaper() {
		return paper;
	}

}
