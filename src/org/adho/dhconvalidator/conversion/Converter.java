package org.adho.dhconvalidator.conversion;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import nu.xom.Document;
import nu.xom.Serializer;

import org.adho.dhconvalidator.conftool.Paper;
import org.adho.dhconvalidator.conftool.User;
import org.adho.dhconvalidator.conversion.input.InputConverter;
import org.adho.dhconvalidator.conversion.input.InputConverterFactory;
import org.adho.dhconvalidator.conversion.output.OutputConverter;
import org.adho.dhconvalidator.conversion.output.OutputConverterFactory;
import org.adho.dhconvalidator.conversion.oxgarage.OxGarageConversionClient;
import org.adho.dhconvalidator.conversion.oxgarage.ZipResult;

public class Converter {

	private String contentAsXhtml;
	private Document document;
	private String baseURL;
	
	public Converter(String baseURL) throws IOException {
		this.baseURL = baseURL;
	}

	public ZipResult convert(
			byte[] sourceData, ConversionPath toTeiConversionPath, 
			User user) throws IOException {

		InputConverterFactory inputConverterFactory = toTeiConversionPath.getInputConverterFactory();
		InputConverter	inputConverter = inputConverterFactory.createInputConverter();
		sourceData = inputConverter.convert(sourceData, user);		
		Paper paper = inputConverter.getPaper();
		
		OxGarageConversionClient oxGarageConversionClient = new OxGarageConversionClient(baseURL);
	
		ZipResult zipResult = new ZipResult(oxGarageConversionClient.convert(
				sourceData, 
				toTeiConversionPath, 
				toTeiConversionPath.getDefaultProperties()));
		
		document = zipResult.getDocument();
		
		OutputConverterFactory outputConverterFactory = 
				toTeiConversionPath.getOutputConverterFactory(); 
		OutputConverter outputConverter = outputConverterFactory.createOutputConverter();
		outputConverter.convert(document, user, paper);

		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		Serializer serializer = new Serializer(bos);
		serializer.setIndent(2);
		serializer.write(document);
		System.out.println(bos.toString("UTF-8"));

		try (FileOutputStream fos = new FileOutputStream("c://test/converted.xml")) {
			fos.write(bos.toByteArray());
		}

		contentAsXhtml = oxGarageConversionClient.convertToString(
				bos.toByteArray(), 
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
