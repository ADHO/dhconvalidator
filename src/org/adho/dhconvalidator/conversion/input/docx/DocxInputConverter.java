/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conversion.input.docx;

import java.io.IOException;
import java.util.List;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import org.adho.dhconvalidator.Messages;
import org.adho.dhconvalidator.conversion.Type;
import org.adho.dhconvalidator.conversion.ZipFs;
import org.adho.dhconvalidator.conversion.input.InputConverter;
import org.adho.dhconvalidator.conversion.input.docx.paragraphparser.ParagraphParser;
import org.adho.dhconvalidator.paper.Paper;
import org.adho.dhconvalidator.properties.PropertyKey;
import org.adho.dhconvalidator.user.User;
import org.adho.dhconvalidator.util.DocumentLog;
import org.adho.dhconvalidator.util.DocumentUtil;

/**
 * A converter for Microsoft docx format.
 * 
 * @author marco.petris@web.de
 *
 */
public class DocxInputConverter implements InputConverter {
	/**
	 * Namespaces used during conversion. public only as an implementation side effect.
	 */
	public enum Namespace {
		MAIN("w", "http://schemas.openxmlformats.org/wordprocessingml/2006/main"), //$NON-NLS-1$ //$NON-NLS-2$
		DOCPROPSVTYPES("vt", "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes"), //$NON-NLS-1$ //$NON-NLS-2$
		RELS("rels", "http://schemas.openxmlformats.org/package/2006/relationships"),  //$NON-NLS-1$ //$NON-NLS-2$
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

	private static final String TEMPLATE = "template/DH_template_v4.docx"; //$NON-NLS-1$
	
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
	
	/* (non-Javadoc)
	 * @see org.adho.dhconvalidator.conversion.input.InputConverter#convert(byte[], org.adho.dhconvalidator.conftool.User)
	 */
	@Override
	public byte[] convert(byte[] sourceData, User user) throws IOException {
		ZipFs zipFs = new ZipFs(sourceData);

		Document document = zipFs.getDocument("word/document.xml"); //$NON-NLS-1$

		cleanupParagraphStyles(document);
		stripTemplateSections(document);
		ensureNumberedHeading(document);
		
		zipFs.putDocument("word/document.xml", document); //$NON-NLS-1$

		DocumentLog.logConversionStepOutput(
				Messages.getString("DocxInputConverter.log1"), document.toXML()); //$NON-NLS-1$

		Document customPropDoc = zipFs.getDocument("docProps/custom.xml"); //$NON-NLS-1$
		Integer paperId = getPaperIdFromMeta(customPropDoc);
		paper = PropertyKey.getPaperProviderInstance().getPaper(user, paperId);

		return zipFs.toZipData();
	}

	/**
	 * We always want numbered headings.
	 * 
	 * @param document
	 */
	private void ensureNumberedHeading(Document document) {
		Nodes searchResult = document.query("//w:pStyle[@w:val='DH-Heading']", xPathContext); //$NON-NLS-1$
		for (int i=0; i<searchResult.size(); i++) {
			Element styleElement = (Element) searchResult.get(i);
			styleElement.getAttribute("val", Namespace.MAIN.toUri()).setValue("DH-Heading1"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Strips the template sections.
	 * 
	 * @param document 
	 */
	private void stripTemplateSections(Document document) {
		// strip the template sections
		ParagraphParser paragraphParser = new ParagraphParser();
		paragraphParser.stripTemplateSections(document, xPathContext);
		
		Element subtitleStyleElement =
			DocumentUtil.tryFirstMatch(
				document, "/w:document/w:body/w:p/w:pPr/w:pStyle[@w:val='DH-Subtitle']",  //$NON-NLS-1$
				xPathContext);

		if (subtitleStyleElement != null) {
			
			//we put the subtitle as a preliminary title and handle the 
			// title stuff later when merging with the ConfTool data
			subtitleStyleElement.getAttribute(
					"val",  //$NON-NLS-1$
					DocxInputConverter.Namespace.MAIN.toUri()).setValue("Title"); //$NON-NLS-1$
		}
	}

	/**
	 * Removes paragraph styles that are not supported.
	 * 
	 * @param document
	 */
	private void cleanupParagraphStyles(Document document) {
		Nodes searchResult = 
				document.query("//w:pPr/w:rPr", xPathContext); //$NON-NLS-1$
		// remove region properties from paragraphs, they are not supported 
		for (int i=0; i<searchResult.size(); i++) {
			Element element = (Element) searchResult.get(i);
			element.getParent().removeChild(element);
		}

		searchResult = document.query("//w:p", xPathContext); //$NON-NLS-1$
		
		for (int i=0; i<searchResult.size(); i++) {
			Element paragraphElement = (Element) searchResult.get(i);
			Elements regions = paragraphElement.getChildElements("r", Namespace.MAIN.toUri()); //$NON-NLS-1$
			// there is only one region...
			if (regions.size() == 1) {
				Element region = regions.get(0);
				Element regionProps = region.getFirstChildElement("rPr", Namespace.MAIN.toUri()); //$NON-NLS-1$
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
				DocumentUtil.tryFirstMatch(
					customPropDoc, 
					"//*[@name='ConfToolPaperID']/vt:lpwstr",  //$NON-NLS-1$
					xPathContext);
		if (propertyElement == null) {
			throw new IOException("DocxInputConverter.confToolPaperIDNotFound"); //$NON-NLS-1$
		}
		try {
			return Integer.valueOf(propertyElement.getValue());
		}
		catch (NumberFormatException nfe) {
			throw new IOException(Messages.getString("DocxInputConverter.invalidConvToolPaperID"), nfe); //$NON-NLS-1$

		}
	}

	/* (non-Javadoc)
	 * @see org.adho.dhconvalidator.conversion.input.InputConverter#getPersonalizedTemplate(org.adho.dhconvalidator.conftool.Paper)
	 */
	@Override
	public byte[] getPersonalizedTemplate(Paper paper) throws IOException {
		ZipFs zipFs = 
			new ZipFs(
				Thread.currentThread().getContextClassLoader().getResourceAsStream(TEMPLATE));
		Document document = zipFs.getDocument("word/document.xml"); //$NON-NLS-1$
		
		injectTitleIntoContent(document, paper.getTitle());
		injectAuthorsIntoContent(document, paper.getAuthorsAndAffiliations());
		
		zipFs.putDocument("word/document.xml", document); //$NON-NLS-1$

		
		Document documentRelations = zipFs.getDocument("word/_rels/document.xml.rels"); //$NON-NLS-1$
		
		updateLinkToConverter(documentRelations, PropertyKey.base_url.getValue());
		
		zipFs.putDocument("word/_rels/document.xml.rels", documentRelations);
		
		
		Document customPropDoc = zipFs.getDocument("docProps/custom.xml"); //$NON-NLS-1$
		
		injectPaperIdIntoMeta(customPropDoc, paper.getPaperId());
		
		zipFs.putDocument("docProps/custom.xml", customPropDoc); //$NON-NLS-1$
		
		return zipFs.toZipData();
	}

	/**
	 * Updates the link to the Conversion service with the current base URL.
	 * @param documentRelations
	 * @param baseURL
	 */
	private void updateLinkToConverter(Document documentRelations,
			String baseURL) {
		Element converterRelElement = 
				DocumentUtil.getFirstMatch(
					documentRelations, 
					"/rels:Relationships/rels:Relationship[starts-with(@Target,'http://localhost:8080/dhconvalidator')]",  //$NON-NLS-1$
					xPathContext);
		Attribute targetAttr = converterRelElement.getAttribute("Target");  //$NON-NLS-1$
		targetAttr.setValue(targetAttr.getValue().replace("http://localhost:8080/dhconvalidator/", baseURL));  //$NON-NLS-1$
	}

	/**
	 * Injects the paperID into the meta data.
	 * @param customPropDoc
	 * @param paperId
	 */
	private void injectPaperIdIntoMeta(Document customPropDoc, Integer paperId) {
		Element propertyElement = 
			DocumentUtil.getFirstMatch(
				customPropDoc, 
				"//*[@name='ConfToolPaperID']/vt:lpwstr",  //$NON-NLS-1$
				xPathContext);

		propertyElement.removeChildren();
		propertyElement.appendChild(String.valueOf(paperId));
	}

	/**
	 * Injects authors into the readonly authors section. 
	 * @param document
	 * @param authorsAndAffiliations
	 * @throws IOException
	 */
	private void injectAuthorsIntoContent(Document document,
			List<User> authorsAndAffiliations) throws IOException {
		Nodes searchResult = 
				document.query("//w:pStyle[@w:val='DH-AuthorAffiliation']", xPathContext); //$NON-NLS-1$
		
		if (searchResult.size() != 1) {
			throw new IOException(
				Messages.getString(
						"DocxInputConverter.templateerror1", //$NON-NLS-1$
						searchResult.size())); 
		}
		
		Element authorStyleElement = (Element) searchResult.get(0);
		Element authorParagraphElement = (Element) authorStyleElement.getParent().getParent();
		Element paragraphParent = (Element) authorParagraphElement.getParent();
		int insertPosition = paragraphParent.indexOf(authorParagraphElement)-1;
		
		for (User authorAffiliation : authorsAndAffiliations){
			Element curAuthorParagraphElement = (Element) authorParagraphElement.copy();
			
			Element authorElement = DocumentUtil.getFirstMatch(
					curAuthorParagraphElement, "w:r/w:t", xPathContext); //$NON-NLS-1$
			
			authorElement.removeChildren();
			authorElement.appendChild(
				authorAffiliation.getFirstName() + " " + authorAffiliation.getLastName()  //$NON-NLS-1
				+ " (" + authorAffiliation.getEmail() + "), " //$NON-NLS-1 //$NON-NLS-2
				+ authorAffiliation.getOrganizations()); //$NON-NLS-1$
			paragraphParent.insertChild(curAuthorParagraphElement, insertPosition);
			insertPosition++;
		}
		
		paragraphParent.removeChild(authorParagraphElement);
		
	}

	/**
	 * Injects the title into the readonly title section.
	 * @param document
	 * @param title
	 * @throws IOException
	 */
	private void injectTitleIntoContent(Document document, String title) throws IOException {
		Nodes searchResult = document.query("//w:pStyle[@w:val='DH-Title']", xPathContext); //$NON-NLS-1$
		
		if (searchResult.size() != 2) {
			throw new IOException(
				Messages.getString(
					"DocxInputConverter.templateerror2",  //$NON-NLS-1$
					searchResult.size()));
		}
		
		Element titleStyleElement = (Element) searchResult.get(1);// we want the second hit
		Element titlParagraphElement = (Element) titleStyleElement.getParent().getParent();
		
		Element titleElement = DocumentUtil.getFirstMatch(
				titlParagraphElement, "w:r/w:t", xPathContext); //$NON-NLS-1$
		
		titleElement.removeChildren();
		titleElement.appendChild(title);
		
	}

	/* (non-Javadoc)
	 * @see org.adho.dhconvalidator.conversion.input.InputConverter#getFileExtension()
	 */
	@Override
	public String getFileExtension() {
		return Type.DOCX.getExtension();
	}

	/* (non-Javadoc)
	 * @see org.adho.dhconvalidator.conversion.input.InputConverter#getPaper()
	 */
	@Override
	public Paper getPaper() {
		return paper;
	}
	
	@Override
	public String getTextEditorDescription() {
		return Messages.getString("DocxInputConverter.editors");
	}

}
