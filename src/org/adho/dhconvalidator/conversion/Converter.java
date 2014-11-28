package org.adho.dhconvalidator.conversion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import nu.xom.Document;
import nu.xom.Serializer;

public class Converter {

	private String contentAsXhtml;
	private Document document;
	private String baseURL;
	
	public Converter(String baseURL) throws IOException {
		this.baseURL = baseURL;
	}

	public ZipResult convert(InputStream sourceStream, ConversionPath toTeiConversionPath) throws IOException {
		OxGarageConversionClient oxGarageConversionClient = new OxGarageConversionClient(baseURL);
	
		ZipResult zipResult = new ZipResult(oxGarageConversionClient.convert(
				sourceStream, 
				toTeiConversionPath, 
				toTeiConversionPath.getDefaultProperties()));
		
		document = zipResult.getDocument();
		
		
		
		
		
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Serializer serializer = new Serializer(bos);
		serializer.setIndent(2);
		serializer.write(document);
		System.out.println(bos.toString());
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		
		
		contentAsXhtml = oxGarageConversionClient.convertToString(
				bis, 
				ConversionPath.TEI_TO_XHTML,
				ConversionPath.TEI_TO_XHTML.getDefaultProperties());
		System.out.println(contentAsXhtml);
	
		return zipResult;
	}
	
	public Document getDocument() {
		return document;
	}
	
	public String getContentAsXhtml() {
		return contentAsXhtml;
	}

//	public static void main(String[] args) {
//		try {
//			new Converter("http://www.tei-c.org/ege-webservice/", new File("testdata/odttest1.odt"));
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
}
