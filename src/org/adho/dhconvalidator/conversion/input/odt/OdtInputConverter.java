/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conversion.input.odt;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import org.adho.dhconvalidator.Messages;
import org.adho.dhconvalidator.conftool.ConfToolClient;
import org.adho.dhconvalidator.conversion.Type;
import org.adho.dhconvalidator.conversion.ZipFs;
import org.adho.dhconvalidator.conversion.input.InputConverter;
import org.adho.dhconvalidator.paper.Paper;
import org.adho.dhconvalidator.properties.PropertyKey;
import org.adho.dhconvalidator.user.User;
import org.adho.dhconvalidator.util.DocumentUtil;

/**
 * An InputConverter for the OASIS odt format.
 * 
 * @author marco.petris@web.de
 *
 */
public class OdtInputConverter implements InputConverter {
	/**
	 * Namespaces used during conversion.
	 */
	private enum Namespace {
		STYLE("style", "urn:oasis:names:tc:opendocument:xmlns:style:1.0"), //$NON-NLS-1$ //$NON-NLS-2$
		TEXT("text", "urn:oasis:names:tc:opendocument:xmlns:text:1.0"), //$NON-NLS-1$ //$NON-NLS-2$
		DC("dc", "http://purl.org/dc/elements/1.1/"), //$NON-NLS-1$ //$NON-NLS-2$
		OFFICE("office", "urn:oasis:names:tc:opendocument:xmlns:office:1.0"), //$NON-NLS-1$ //$NON-NLS-2$
		META("meta", "urn:oasis:names:tc:opendocument:xmlns:meta:1.0"), //$NON-NLS-1$ //$NON-NLS-2$
		XLINK("xlink", "http://www.w3.org/1999/xlink"), //$NON-NLS-1$ //$NON-NLS-2$
		DRAW("draw", "urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"),  //$NON-NLS-1$ //$NON-NLS-2$
		SVG("svg", "urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"), //$NON-NLS-1$ //$NON-NLS-2$
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
	private static final String TEMPLATE = "template/DH_template_v3.ott"; //$NON-NLS-1$
	private static final String CONFTOOLPAPERID_ATTRIBUTENAME = "ConfToolPaperID"; //$NON-NLS-1$
	
	private XPathContext xPathContext;
	private Paper paper; //holds the paper loaded during conversion
	
	public OdtInputConverter() {
		// create xpathcontext with all the necessar namespaces
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
		// unzip data
		ZipFs zipFs = new ZipFs(sourceData);
		Document contentDoc = zipFs.getDocument("content.xml"); //$NON-NLS-1$
		
		cleanupParagraphStyles(contentDoc);
		makeHeaderElement(contentDoc);
		stripTemplateSections(contentDoc);
		makeReferencesChapter(contentDoc);
		embedExternalFormulae(contentDoc, zipFs);
		
		Document metaDoc = zipFs.getDocument("meta.xml"); //$NON-NLS-1$
		Integer paperId = getPaperIdFromMeta(metaDoc);
		paper = PropertyKey.getPaperProviderInstance().getPaper(user, paperId);

		injectTitleIntoMeta(metaDoc, paper.getTitle());
		injectAuthorsIntoMeta(metaDoc, paper.getAuthorsAndAffiliations());
		
		zipFs.putDocument("content.xml", contentDoc); //$NON-NLS-1$
		return zipFs.toZipData();
	}

	/**
	 * Formulae that are kept externally are getting embedded into the content.xml.
	 * @param contentDoc
	 * @param zipFs
	 * @throws IOException
	 */
	private void embedExternalFormulae(Document contentDoc, ZipFs zipFs) throws IOException {
		Nodes searchResult = 
				contentDoc.query("//draw:object", xPathContext); //$NON-NLS-1$
		
		for (int i=0; i<searchResult.size(); i++) {
			Element drawObjectElement = (Element)searchResult.get(i);
			String contentPath = 
				drawObjectElement.getAttributeValue("href", Namespace.XLINK.toUri()).substring(2) //$NON-NLS-1$
				+ "/content.xml"; //$NON-NLS-1$
			
			Element parent = (Element) drawObjectElement.getParent();
			
			Document externalContentDoc = zipFs.getDocument(contentPath);
			
			if (!externalContentDoc.getRootElement().getLocalName().equals("math")) { //$NON-NLS-1$
				throw new IOException(
					Messages.getString(
						"OdtInputConverter.matherror", //$NON-NLS-1$
						externalContentDoc.getRootElement().getLocalName())); 
			}
			
			Element drawImageElement = parent.getFirstChildElement("image", Namespace.DRAW.toUri()); //$NON-NLS-1$
			if (drawImageElement != null) {
				parent.removeChild(drawImageElement);
			}
			
			Element svgDescElement = parent.getFirstChildElement("desc", Namespace.SVG.toUri()); //$NON-NLS-1$
			if (svgDescElement != null) {
				parent.removeChild(svgDescElement);
			}
			parent.replaceChild(
				drawObjectElement, 
				externalContentDoc.getRootElement().copy());
		}
	}

	/**
	 * Removes empty References section or makes non empty References section a proper 
	 * chapter.
	 * @param contentDoc
	 * @throws IOException
	 */
	private void makeReferencesChapter(Document contentDoc) throws IOException {
		Nodes searchResult = 
				contentDoc.query(
					"//text:section[@text:name='References']",  //$NON-NLS-1$
					xPathContext);
		if (searchResult.size() == 1) {
			Element referencesSectionElement = (Element) searchResult.get(0);
			Element parent = (Element) referencesSectionElement.getParent();
			int position = parent.indexOf(referencesSectionElement);
			// remove empty references section
			if (position == parent.getChildCount()-1) {
				parent.removeChild(referencesSectionElement);
			}
			else { // or make it a proper chapter 
				Element headElement = new Element("text:h", Namespace.TEXT.toUri()); //$NON-NLS-1$
				headElement.addAttribute(
					new Attribute("text:outline-level", Namespace.TEXT.toUri(), "1")); //$NON-NLS-1$ //$NON-NLS-2$
				headElement.addAttribute(
					new Attribute(
							"text:style-name", 
							Namespace.TEXT.toUri(), 
							"DH-BibliographyHeading")); //$NON-NLS-1$ //$NON-NLS-2$
				headElement.appendChild("Bibliography"); //$NON-NLS-1$
				parent.replaceChild(referencesSectionElement, headElement);
			}
		}
		else {
			throw new IOException(
				Messages.getString(
					"OdtInputConverter.sectionerror", searchResult.size())); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private void makeHeaderElement(Document contentDoc) {
		Nodes searchResult = 
			contentDoc.query(
				"//office:text/text:p[starts-with(@text:style-name,'DH-Heading')]", //$NON-NLS-1$
				xPathContext);
		
		for (int i=0; i<searchResult.size(); i++) {
			Element headElement = (Element) searchResult.get(i);
			String styleName = 
				headElement.getAttributeValue(
					"style-name", Namespace.TEXT.toUri()); //$NON-NLS-1$
			Integer level = 1;
			if (!styleName.equals("DH-Heading")) { //$NON-NLS-1$
				level = Integer.valueOf(styleName.substring("DH-Heading".length())); //$NON-NLS-1$
			}
			headElement.setLocalName("h"); //$NON-NLS-1$
			headElement.addAttribute(
				new Attribute("text:outline-level", Namespace.TEXT.toUri(), level.toString())); //$NON-NLS-1$
		}
		
	}

	/**
	 * Removes all template sections 
	 * @param contentDoc
	 */
	private void stripTemplateSections(Document contentDoc) {
		Nodes searchResult = 
				contentDoc.query(
					"//text:section[@text:name='Authors from ConfTool']",  //$NON-NLS-1$
					xPathContext);
		if (searchResult.size() > 0) {
			removeNodes(searchResult);
		}
		
		searchResult = 
			contentDoc.query(
				"//text:section[@text:name='Guidelines']",  //$NON-NLS-1$
				xPathContext);
		
		if (searchResult.size() > 0) {
			removeNodes(searchResult);
		}
				
		searchResult = 
			contentDoc.query(
				"//text:section[@text:name='Title from ConfTool']",  //$NON-NLS-1$
				xPathContext);
		
		if (searchResult.size() > 0) {
			removeNodes(searchResult);
		}
		
		searchResult = 
				contentDoc.query(
					"//text:section[@text:name='TitleInfoPre']",  //$NON-NLS-1$
					xPathContext);
		
		if (searchResult.size() > 0) {
			removeNodes(searchResult);
		}
		
		
		searchResult = 
				contentDoc.query(
					"//text:section[@text:name='TitleInfoPost']",  //$NON-NLS-1$
					xPathContext);
		
		if (searchResult.size() > 0) {
			removeNodes(searchResult);
		}
		
		searchResult = 
				contentDoc.query(
						"//text:section[@text:name='AuthorsInfoPre']",  //$NON-NLS-1$
						xPathContext);
		
		if (searchResult.size() > 0) {
			removeNodes(searchResult);
		}
		
		searchResult = 
				contentDoc.query(
						"//text:section[@text:name='AuthorsInfoPost']",  //$NON-NLS-1$
						xPathContext);
		
		if (searchResult.size() > 0) {
			removeNodes(searchResult);
		}
		
		searchResult = 
				contentDoc.query(
						"//text:section[@text:name='EndOfDocInfo']",  //$NON-NLS-1$
						xPathContext);
		
		if (searchResult.size() > 0) {
			removeNodes(searchResult);
		}
	}

	private void removeNodes(Nodes nodes) {
		for (int i=0; i<nodes.size(); i++) {
			Node n = nodes.get(i);
			n.getParent().removeChild(n);
		}
	}

	private Integer getPaperIdFromMeta(Document metaDoc) throws IOException {
		Nodes searchResult = 
			metaDoc.query(
				"/office:document-meta/office:meta/meta:user-defined[@meta:name='" //$NON-NLS-1$
						+CONFTOOLPAPERID_ATTRIBUTENAME+"']",  //$NON-NLS-1$
				xPathContext);
	
		if (searchResult.size() == 1) {
			Element confToolPaperIdElement = (Element) searchResult.get(0);
			return Integer.valueOf(confToolPaperIdElement.getValue());
		}
		else {
			throw new IOException(
				Messages.getString("OdtInputConverter.invalidmeta")); //$NON-NLS-1$
		}
	}

	/**
	 * We remove all adhoc paragraph styles as they are not supported
	 * and might be used to create fake chapter titles.
	 * @param contentDoc
	 */
	private void cleanupParagraphStyles(Document contentDoc) {
		Map<String,String> paragraphStyleMapping = new HashMap<>();
		
		Nodes styleResult = contentDoc.query(
			"/office:document-content/office:automatic-styles/style:style[@style:family='paragraph']", //$NON-NLS-1$
			xPathContext);
		
		for (int i=0; i<styleResult.size(); i++) {
			Element styleNode = (Element)styleResult.get(i);
			String adhocName = styleNode.getAttributeValue("name", Namespace.STYLE.toUri()); //$NON-NLS-1$
			String definedName = 
				styleNode.getAttributeValue("parent-style-name", Namespace.STYLE.toUri()); //$NON-NLS-1$
			paragraphStyleMapping.put(adhocName, definedName);
		}
		
		Nodes textResult = contentDoc.query(
			"/office:document-content/office:body/office:text/text:*", xPathContext); //$NON-NLS-1$
		
		for (int i=0; i<textResult.size(); i++) {
			Element textNode = (Element)textResult.get(i);
			String styleName = textNode.getAttributeValue("style-name", Namespace.TEXT.toUri()); //$NON-NLS-1$
			if (styleName != null) {
				String definedName = paragraphStyleMapping.get(styleName);
				if (definedName != null) {
					textNode.getAttribute("style-name", Namespace.TEXT.toUri()).setValue(definedName); //$NON-NLS-1$
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.adho.dhconvalidator.conversion.input.InputConverter#getPersonalizedTemplate(org.adho.dhconvalidator.conftool.Paper)
	 */
	public byte[] getPersonalizedTemplate(Paper paper) throws IOException {
		ZipFs zipFs = 
			new ZipFs(
				Thread.currentThread().getContextClassLoader().getResourceAsStream(TEMPLATE));
		Document contentDoc = zipFs.getDocument("content.xml"); //$NON-NLS-1$
		
		injectTitleIntoContent(contentDoc, paper.getTitle());
		injectAuthorsIntoContent(contentDoc, paper.getAuthorsAndAffiliations());
		updateLinkToConverter(contentDoc, PropertyKey.base_url.getValue());

		zipFs.putDocument("content.xml", contentDoc); //$NON-NLS-1$
		
		Document metaDoc = zipFs.getDocument("meta.xml"); //$NON-NLS-1$
		injectTitleIntoMeta(metaDoc, paper.getTitle());
		injectAuthorsIntoMeta(metaDoc, paper.getAuthorsAndAffiliations());
		injectPaperIdIntoMeta(metaDoc, paper.getPaperId());
		
		zipFs.putDocument("meta.xml", metaDoc); //$NON-NLS-1$
		
		return zipFs.toZipData();
	}

	private void updateLinkToConverter(Document contentDoc, String baseURL) {
		Element converterLinkElement = 
			DocumentUtil.getFirstMatch(
					contentDoc, 
					"//text:a[starts-with(@xlink:href, 'http://localhost:8080/dhconvalidator')]", 
					xPathContext);
		
		Attribute targetAttr = converterLinkElement.getAttribute("href", Namespace.XLINK.toUri());  //$NON-NLS-1$
		targetAttr.setValue(targetAttr.getValue().replace("http://localhost:8080/dhconvalidator/", baseURL));  //$NON-NLS-1$
	}

	/**
	 * Injects the ConfTool paperId into the meta data of the template.
	 * @param metaDoc
	 * @param paperId
	 */
	private void injectPaperIdIntoMeta(Document metaDoc, Integer paperId) {
		Nodes searchResult = 
			metaDoc.query(
				"/office:document-meta/office:meta/meta:user-defined[@meta:name='" //$NON-NLS-1$
						+CONFTOOLPAPERID_ATTRIBUTENAME+"']",  //$NON-NLS-1$
				xPathContext);
		
		if (searchResult.size() != 0) {
			for (int i=0; i<searchResult.size(); i++) {
				Node n = searchResult.get(i);
				n.getParent().removeChild(n);
			}
		}
		
		Element confToolPaperIdElement = 
				new Element("meta:user-defined", Namespace.META.toUri()); //$NON-NLS-1$
		confToolPaperIdElement.addAttribute(
			new Attribute("meta:name", Namespace.META.toUri(), CONFTOOLPAPERID_ATTRIBUTENAME)); //$NON-NLS-1$
		confToolPaperIdElement.appendChild(String.valueOf(paperId));
		
		Element metaElement = metaDoc.getRootElement()
				.getFirstChildElement("meta", Namespace.OFFICE.toUri()); //$NON-NLS-1$
		metaElement.appendChild(confToolPaperIdElement);
		
	}

	/**
	 * Injects the authors of the paper into the meta data.
	 * @param metaDoc
	 * @param authorsAndAffiliations
	 */
	private void injectAuthorsIntoMeta(Document metaDoc,
			List<User> authorsAndAffiliations) {
		Nodes searchResult = 
				metaDoc.query(
					"/office:document-meta/office:meta/meta:initial-creator",  //$NON-NLS-1$
					xPathContext);
		Element initialCreatorElement = null;
		if (searchResult.size() > 0) {
			initialCreatorElement = (Element) searchResult.get(0);
			
		}
		else {
			initialCreatorElement = 
				new Element("meta:initial-creator", Namespace.META.toUri()); //$NON-NLS-1$
			Element metaElement = metaDoc.getRootElement()
					.getFirstChildElement("meta", Namespace.OFFICE.toUri()); //$NON-NLS-1$
			metaElement.appendChild(initialCreatorElement);
		}
		
		initialCreatorElement.removeChildren();
		StringBuilder builder = new StringBuilder();
		String conc  = ""; //$NON-NLS-1$
		for (User authorAffiliation : authorsAndAffiliations) {
			builder.append(conc);
			builder.append(authorAffiliation.getFirstName() + " " + authorAffiliation.getLastName());
			builder.append(" ("); //$NON-NLS-1$
			builder.append(authorAffiliation.getEmail());
			builder.append("), ");
			builder.append(authorAffiliation.getOrganizations());
			conc = "; "; //$NON-NLS-1$
		}
		initialCreatorElement.appendChild(builder.toString());
		
		Nodes creatorSearchResult = 
				metaDoc.query(
					"/office:document-meta/office:meta/dc:creator",  //$NON-NLS-1$
					xPathContext);
		if (creatorSearchResult.size() > 0) {
			creatorSearchResult.get(0).getParent().removeChild(creatorSearchResult.get(0));
		}
	}

	/**
	 * Injects the title of the paper into the metadata
	 * @param metaDoc
	 * @param title
	 */
	private void injectTitleIntoMeta(Document metaDoc, String title) {
		Nodes searchResult = 
				metaDoc.query(
					"/office:document-meta/office:meta/dc:title",  //$NON-NLS-1$
					xPathContext);
		Element titleElement = null;
		if (searchResult.size() > 0) {
			titleElement = (Element) searchResult.get(0);
			
		}
		else {
			titleElement = new Element("dc:title", Namespace.DC.toUri()); //$NON-NLS-1$
			Element metaElement = metaDoc.getRootElement()
				.getFirstChildElement("meta", Namespace.OFFICE.toUri()); //$NON-NLS-1$
			metaElement.appendChild(titleElement);
		}
		titleElement.removeChildren();
		titleElement.appendChild(title);
	}

	/**
	 * Injects authors into the readonly authors section.
	 * @param contentDoc
	 * @param authorsAndAffiliations
	 * @throws IOException
	 */
	private void injectAuthorsIntoContent(Document contentDoc,
			List<User> authorsAndAffiliations) throws IOException {
		Nodes searchResult = 
				contentDoc.query(
					"//text:section[@text:name='Authors from ConfTool']",  //$NON-NLS-1$
					xPathContext);
		
		if (searchResult.size()!=1) {
			throw new IOException(
				Messages.getString(
					"OdtInputConverter.sectionerror2", //$NON-NLS-1$
					searchResult.size()));
		}
		
		if (!(searchResult.get(0) instanceof Element)) {
			throw new IllegalStateException(
					Messages.getString(
							"OdtInputConverter.sectionerror3")); //$NON-NLS-1$
		}
		
		Element authorSectionElement = (Element) searchResult.get(0);
		
		authorSectionElement.removeChildren();
		for (User authorAffiliation : authorsAndAffiliations){
			Element authorParagraphElement = new Element("p", Namespace.TEXT.toUri()); //$NON-NLS-1$
			authorSectionElement.appendChild(authorParagraphElement);
			authorParagraphElement.appendChild(
					authorAffiliation.getFirstName() + " " + authorAffiliation.getLastName() //$NON-NLS-1$
					+" ("+authorAffiliation.getEmail()+")"
					+", " //$NON-NLS-1$
					+authorAffiliation.getOrganizations());
			authorParagraphElement.addAttribute(
				new Attribute("text:style-name", Namespace.TEXT.toUri(), "P6")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Injects title into the read only title section.
	 * @param contentDoc
	 * @param title
	 * @throws IOException
	 */
	private void injectTitleIntoContent(Document contentDoc, String title) throws IOException {
		Nodes searchResult = 
			contentDoc.query(
				"//text:section[@text:name='Title from ConfTool']",  //$NON-NLS-1$
				xPathContext);
		
		if (searchResult.size()!=1) {
			throw new IOException(
				Messages.getString(
					"OdtInputConverter.titleerror", //$NON-NLS-1$
					searchResult.size()));
		}
		
		if (!(searchResult.get(0) instanceof Element)) {
			throw new IllegalStateException(
				Messages.getString("OdtInputConverter.titleerror2")); //$NON-NLS-1$
		}
		
		Element titleSectionElement = (Element) searchResult.get(0);
		
		titleSectionElement.removeChildren();
		Element titleParagraphElement = new Element("p", Namespace.TEXT.toUri()); //$NON-NLS-1$
		titleSectionElement.appendChild(titleParagraphElement);
		titleParagraphElement.appendChild(title);
		titleParagraphElement.addAttribute(
				new Attribute("text:style-name", Namespace.TEXT.toUri(), "P1")); //$NON-NLS-1$ //$NON-NLS-2$

	}
	
	/* (non-Javadoc)
	 * @see org.adho.dhconvalidator.conversion.input.InputConverter#getFileExtension()
	 */
	@Override
	public String getFileExtension() {
		return Type.ODT.getExtension();
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
		return Messages.getString("OdtInputConverter.editors");
	}
}
