package org.adho.dhconvalidator.conversion.input;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import org.adho.dhconvalidator.conftool.Paper;
import org.adho.dhconvalidator.conversion.Type;
import org.adho.dhconvalidator.conversion.ZipFs;

public class OdtInputConverter implements InputConverter {
	private static final String STYLE_NAMESPACE = "urn:oasis:names:tc:opendocument:xmlns:style:1.0";
	private static final String TEXT_NAMESPACE = "urn:oasis:names:tc:opendocument:xmlns:text:1.0"; 
	private static final String TEMPLATE = "template/DH_template_v1.ott";
	
	private XPathContext xPathContext;
	
	public OdtInputConverter() {
		xPathContext = new XPathContext();
		xPathContext.addNamespace("office", "urn:oasis:names:tc:opendocument:xmlns:office:1.0");
		xPathContext.addNamespace("style", STYLE_NAMESPACE);
		xPathContext.addNamespace("text", TEXT_NAMESPACE);
	}

	@Override
	public byte[] convert(byte[] sourceData) throws IOException {
		ZipFs zipFs = new ZipFs(sourceData);
		Document contentDoc = zipFs.getDocument("content.xml");
		
		stripAutomaticParagraphStyles(contentDoc);

		zipFs.putDocument("content.xml", contentDoc);
		return zipFs.toZipData();
	}

	private void stripAutomaticParagraphStyles(Document contentDoc) {
		Map<String,String> paragraphStyleMapping = new HashMap<>();
		
		Nodes styleResult = contentDoc.query(
			"/office:document-content/office:automatic-styles/style:style[@style:family='paragraph']",
			xPathContext);
		
		for (int i=0; i<styleResult.size(); i++) {
			Element styleNode = (Element)styleResult.get(i);
			System.out.println(styleNode);
			String adhocName = styleNode.getAttributeValue("name", STYLE_NAMESPACE);
			String definedName = styleNode.getAttributeValue("parent-style-name", STYLE_NAMESPACE);
			paragraphStyleMapping.put(adhocName, definedName);
		}
		
		Nodes textResult = contentDoc.query(
			"/office:document-content/office:body/office:text/text:*", xPathContext);
		
		for (int i=0; i<textResult.size(); i++) {
			Element textNode = (Element)textResult.get(i);
			String styleName = textNode.getAttributeValue("style-name", TEXT_NAMESPACE);
			if (styleName != null) {
				String definedName = paragraphStyleMapping.get(styleName);
				if (definedName != null) {
					textNode.getAttribute("style-name", TEXT_NAMESPACE).setValue(definedName);
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
		return zipFs.toZipData();
	}

	private void injectAuthorsIntoContent(Document contentDoc,
			List<String> authorsAndAffiliations) throws IOException {
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
		for (String authorAffiliation : authorsAndAffiliations){
			Element authorParagraphElement = new Element("p", TEXT_NAMESPACE);
			authorSectionElement.appendChild(authorParagraphElement);
			authorParagraphElement.appendChild(authorAffiliation);
			authorParagraphElement.addAttribute(
				new Attribute("text:style-name", TEXT_NAMESPACE, "P6"));
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
		Element titleParagraphElement = new Element("p", TEXT_NAMESPACE);
		titleSectionElement.appendChild(titleParagraphElement);
		titleParagraphElement.appendChild(title);
		titleParagraphElement.addAttribute(
				new Attribute("text:style-name", TEXT_NAMESPACE, "P1"));

	}
	
	@Override
	public String getFileExtension() {
		return Type.ODT.getExtension();
	}
}
