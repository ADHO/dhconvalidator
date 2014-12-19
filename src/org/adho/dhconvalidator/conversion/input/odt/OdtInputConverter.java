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

import org.adho.dhconvalidator.conftool.ConfToolCacheProvider;
import org.adho.dhconvalidator.conftool.Paper;
import org.adho.dhconvalidator.conftool.User;
import org.adho.dhconvalidator.conversion.Type;
import org.adho.dhconvalidator.conversion.ZipFs;
import org.adho.dhconvalidator.conversion.input.InputConverter;
import org.adho.dhconvalidator.util.Pair;

public class OdtInputConverter implements InputConverter {
	private enum Namespace {
		STYLE("style", "urn:oasis:names:tc:opendocument:xmlns:style:1.0"),
		TEXT("text", "urn:oasis:names:tc:opendocument:xmlns:text:1.0"),
		DC("dc", "http://purl.org/dc/elements/1.1/"),
		OFFICE("office", "urn:oasis:names:tc:opendocument:xmlns:office:1.0"),
		META("meta", "urn:oasis:names:tc:opendocument:xmlns:meta:1.0"),
		XLINK("xlink", "http://www.w3.org/1999/xlink"),
		DRAW("draw", "urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"), 
		SVG("svg", "urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"),
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
	private static final String TEMPLATE = "template/DH_template_v1.ott";
	private static final String CONFTOOLPAPERID_ATTRIBUTENAME = "ConfToolPaperID";
	
	private XPathContext xPathContext;
	private Paper paper;
	
	public OdtInputConverter() {
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
		Document contentDoc = zipFs.getDocument("content.xml");
		
		cleanupParagraphStyles(contentDoc);
		makeHeaderElement(contentDoc);
		stripTemplateSections(contentDoc);
		makeReferencesChapter(contentDoc);
		embedExternalFormulae(contentDoc, zipFs);
		
		Document metaDoc = zipFs.getDocument("meta.xml");
		Integer paperId = getPaperIdFromMeta(metaDoc);
		paper = ConfToolCacheProvider.INSTANCE.getConfToolCache().getPaper(user, paperId);

		injectTitleIntoMeta(metaDoc, paper.getTitle());
		injectAuthorsIntoMeta(metaDoc, paper.getAuthorsAndAffiliations());

		zipFs.putDocument("content.xml", contentDoc);
		return zipFs.toZipData();
	}

	private void embedExternalFormulae(Document contentDoc, ZipFs zipFs) throws IOException {
		Nodes searchResult = 
				contentDoc.query("//draw:object", xPathContext);
		
		for (int i=0; i<searchResult.size(); i++) {
			Element drawObjectElement = (Element)searchResult.get(i);
			String contentPath = 
				drawObjectElement.getAttributeValue("href", Namespace.XLINK.toUri()).substring(2)
				+ "/content.xml";
			
			Element parent = (Element) drawObjectElement.getParent();
			
			Document externalContentDoc = zipFs.getDocument(contentPath);
			
			if (!externalContentDoc.getRootElement().getLocalName().equals("math")) {
				throw new IOException(
					"We only support Math formulae as embedded content so far, "
					+ "expected math but found " 
					+ externalContentDoc.getRootElement().getLocalName()); 
			}
			
			Element drawImageElement = parent.getFirstChildElement("image", Namespace.DRAW.toUri());
			if (drawImageElement != null) {
				parent.removeChild(drawImageElement);
			}
			
			Element svgDescElement = parent.getFirstChildElement("desc", Namespace.SVG.toUri());
			if (svgDescElement != null) {
				parent.removeChild(svgDescElement);
			}
			parent.replaceChild(
				drawObjectElement, 
				externalContentDoc.getRootElement().copy());
		}
	}

	private void makeReferencesChapter(Document contentDoc) throws IOException {
		Nodes searchResult = 
				contentDoc.query(
					"//text:section[@text:name='References']", 
					xPathContext);
		if (searchResult.size() == 1) {
			Element referencesSectionElement = (Element) searchResult.get(0);
			Element parent = (Element) referencesSectionElement.getParent();
			int position = parent.indexOf(referencesSectionElement);
			if (position == parent.getChildCount()-1) {
				parent.removeChild(referencesSectionElement);
			}
			else {
				Element headElement = new Element("text:h", Namespace.TEXT.toUri());
				headElement.addAttribute(
					new Attribute("text:outline-level", Namespace.TEXT.toUri(), "1"));
				headElement.addAttribute(
					new Attribute("text:style-name", Namespace.TEXT.toUri(), "DH-BibliographyHeading"));
				headElement.appendChild("Bibliography");
				parent.replaceChild(referencesSectionElement, headElement);
			}
		}
		else {
			throw new IOException(
				"found " + searchResult.size() + " References section(s) "
						+ "but expected one and only one");
		}
	}

	private void makeHeaderElement(Document contentDoc) {
		Nodes searchResult = 
			contentDoc.query(
				"//office:text/text:p[starts-with(@text:style-name,'DH-Heading')]",
				xPathContext);
		
		for (int i=0; i<searchResult.size(); i++) {
			Element headElement = (Element) searchResult.get(i);
			String styleName = headElement.getAttributeValue("style-name", Namespace.TEXT.toUri());
			Integer level = 1;
			if (!styleName.equals("DH-Heading")) {
				level = Integer.valueOf(styleName.substring("DH-Heading".length()));
			}
			headElement.setLocalName("h");
			headElement.addAttribute(
				new Attribute("text:outline-level", Namespace.TEXT.toUri(), level.toString()));
		}
		
	}

	private void stripTemplateSections(Document contentDoc) {
		Nodes searchResult = 
				contentDoc.query(
					"//text:section[@text:name='Authors from ConfTool']", 
					xPathContext);
		if (searchResult.size() > 0) {
			removeNodes(searchResult);
		}
		
		searchResult = 
			contentDoc.query(
				"//text:section[@text:name='Guidelines']", 
				xPathContext);
		
		if (searchResult.size() > 0) {
			removeNodes(searchResult);
		}
				
		searchResult = 
			contentDoc.query(
				"//text:section[@text:name='Title from ConfTool']", 
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
				"/office:document-meta/office:meta/meta:user-defined[@meta:name='"
						+CONFTOOLPAPERID_ATTRIBUTENAME+"']", 
				xPathContext);
	
		if (searchResult.size() == 1) {
			Element confToolPaperIdElement = (Element) searchResult.get(0);
			return Integer.valueOf(confToolPaperIdElement.getValue());
		}
		else {
			throw new IOException(
				"document has invalid meta section: ConfToolPaperID not found!");
		}
	}

	private void cleanupParagraphStyles(Document contentDoc) {
		Map<String,String> paragraphStyleMapping = new HashMap<>();
		
		Nodes styleResult = contentDoc.query(
			"/office:document-content/office:automatic-styles/style:style[@style:family='paragraph']",
			xPathContext);
		
		for (int i=0; i<styleResult.size(); i++) {
			Element styleNode = (Element)styleResult.get(i);
			String adhocName = styleNode.getAttributeValue("name", Namespace.STYLE.toUri());
			String definedName = 
				styleNode.getAttributeValue("parent-style-name", Namespace.STYLE.toUri());
			paragraphStyleMapping.put(adhocName, definedName);
		}
		
		Nodes textResult = contentDoc.query(
			"/office:document-content/office:body/office:text/text:*", xPathContext);
		
		for (int i=0; i<textResult.size(); i++) {
			Element textNode = (Element)textResult.get(i);
			String styleName = textNode.getAttributeValue("style-name", Namespace.TEXT.toUri());
			if (styleName != null) {
				String definedName = paragraphStyleMapping.get(styleName);
				if (definedName != null) {
					textNode.getAttribute("style-name", Namespace.TEXT.toUri()).setValue(definedName);
				}
			}
		}
	}

	public byte[] getPersonalizedTemplate(Paper paper) throws IOException {
		ZipFs zipFs = 
			new ZipFs(
				Thread.currentThread().getContextClassLoader().getResourceAsStream(TEMPLATE));
		Document contentDoc = zipFs.getDocument("content.xml");
		
		injectTitleIntoContent(contentDoc, paper.getTitle());
		injectAuthorsIntoContent(contentDoc, paper.getAuthorsAndAffiliations());
		
		zipFs.putDocument("content.xml", contentDoc);
		
		Document metaDoc = zipFs.getDocument("meta.xml");
		injectTitleIntoMeta(metaDoc, paper.getTitle());
		injectAuthorsIntoMeta(metaDoc, paper.getAuthorsAndAffiliations());
		injectPaperIdIntoMeta(metaDoc, paper.getPaperId());
		
		zipFs.putDocument("meta.xml", metaDoc);
		
		return zipFs.toZipData();
	}

	private void injectPaperIdIntoMeta(Document metaDoc, Integer paperId) {
		Nodes searchResult = 
			metaDoc.query(
				"/office:document-meta/office:meta/meta:user-defined[@meta:name='"
						+CONFTOOLPAPERID_ATTRIBUTENAME+"']", 
				xPathContext);
		
		if (searchResult.size() != 0) {
			for (int i=0; i<searchResult.size(); i++) {
				Node n = searchResult.get(i);
				n.getParent().removeChild(n);
			}
		}
		
		Element confToolPaperIdElement = 
				new Element("meta:user-defined", Namespace.META.toUri());
		confToolPaperIdElement.addAttribute(
			new Attribute("meta:name", Namespace.META.toUri(), CONFTOOLPAPERID_ATTRIBUTENAME));
		confToolPaperIdElement.appendChild(String.valueOf(paperId));
		
		Element metaElement = metaDoc.getRootElement()
				.getFirstChildElement("meta", Namespace.OFFICE.toUri());
		metaElement.appendChild(confToolPaperIdElement);
		
	}

	private void injectAuthorsIntoMeta(Document metaDoc,
			List<Pair<String,String>> authorsAndAffiliations) {
		Nodes searchResult = 
				metaDoc.query(
					"/office:document-meta/office:meta/meta:initial-creator", 
					xPathContext);
		Element initialCreatorElement = null;
		if (searchResult.size() > 0) {
			initialCreatorElement = (Element) searchResult.get(0);
			
		}
		else {
			initialCreatorElement = 
				new Element("meta:initial-creator", Namespace.META.toUri());
			Element metaElement = metaDoc.getRootElement()
					.getFirstChildElement("meta", Namespace.OFFICE.toUri());
			metaElement.appendChild(initialCreatorElement);
		}
		
		initialCreatorElement.removeChildren();
		StringBuilder builder = new StringBuilder();
		String conc  = "";
		for (Pair<String,String> authorAffiliation : authorsAndAffiliations) {
			builder.append(conc);
			builder.append(authorAffiliation.getFirst());
			builder.append(", ");
			builder.append(authorAffiliation.getSecond());
			conc = "; ";
		}
		initialCreatorElement.appendChild(builder.toString());
		
		Nodes creatorSearchResult = 
				metaDoc.query(
					"/office:document-meta/office:meta/dc:creator", 
					xPathContext);
		if (creatorSearchResult.size() > 0) {
			creatorSearchResult.get(0).getParent().removeChild(creatorSearchResult.get(0));
		}
	}

	private void injectTitleIntoMeta(Document metaDoc, String title) {
		Nodes searchResult = 
				metaDoc.query(
					"/office:document-meta/office:meta/dc:title", 
					xPathContext);
		Element titleElement = null;
		if (searchResult.size() > 0) {
			titleElement = (Element) searchResult.get(0);
			
		}
		else {
			titleElement = new Element("dc:title", Namespace.DC.toUri());
			Element metaElement = metaDoc.getRootElement()
				.getFirstChildElement("meta", Namespace.OFFICE.toUri());
			metaElement.appendChild(titleElement);
		}
		titleElement.removeChildren();
		titleElement.appendChild(title);
	}

	private void injectAuthorsIntoContent(Document contentDoc,
			List<Pair<String,String>> authorsAndAffiliations) throws IOException {
		Nodes searchResult = 
				contentDoc.query(
					"//text:section[@text:name='Authors from ConfTool']", 
					xPathContext);
		
		if (searchResult.size()!=1) {
			throw new IOException(
				"document does not contain exactly one section element "
				+ "for the ConfTool author/affiliation, found: "
				+ searchResult.size());
		}
		
		if (!(searchResult.get(0) instanceof Element)) {
			throw new IllegalStateException(
				"section for ConfTool author/affiliation doesn't seem to be a proper Element");
		}
		
		Element authorSectionElement = (Element) searchResult.get(0);
		
		authorSectionElement.removeChildren();
		for (Pair<String,String> authorAffiliation : authorsAndAffiliations){
			Element authorParagraphElement = new Element("p", Namespace.TEXT.toUri());
			authorSectionElement.appendChild(authorParagraphElement);
			authorParagraphElement.appendChild(
					authorAffiliation.getFirst()
					+", "
					+authorAffiliation.getSecond());
			authorParagraphElement.addAttribute(
				new Attribute("text:style-name", Namespace.TEXT.toUri(), "P6"));
		}
	}

	private void injectTitleIntoContent(Document contentDoc, String title) throws IOException {
		Nodes searchResult = 
			contentDoc.query(
				"//text:section[@text:name='Title from ConfTool']", 
				xPathContext);
		
		if (searchResult.size()!=1) {
			throw new IOException(
				"document does not contain exactly one section element "
				+ "for the ConfTool title, found: "
				+ searchResult.size());
		}
		
		if (!(searchResult.get(0) instanceof Element)) {
			throw new IllegalStateException(
				"section for ConfTool title doesn't seem to be a proper Element");
		}
		
		Element titleSectionElement = (Element) searchResult.get(0);
		
		titleSectionElement.removeChildren();
		Element titleParagraphElement = new Element("p", Namespace.TEXT.toUri());
		titleSectionElement.appendChild(titleParagraphElement);
		titleParagraphElement.appendChild(title);
		titleParagraphElement.addAttribute(
				new Attribute("text:style-name", Namespace.TEXT.toUri(), "P1"));

	}
	
	@Override
	public String getFileExtension() {
		return Type.ODT.getExtension();
	}
	
	@Override
	public Paper getPaper() {
		return paper;
	}
}
