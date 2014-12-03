package org.adho.dhconvalidator.conversion.input;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.adho.dhconvalidator.conversion.ZipFs;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;

public class OdtInputConverter implements InputConverter {
	private static final String STYLE_NAMESPACE = "urn:oasis:names:tc:opendocument:xmlns:style:1.0";
	private static final String TEXT_NAMESPACE = "urn:oasis:names:tc:opendocument:xmlns:text:1.0"; 
	
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

}
