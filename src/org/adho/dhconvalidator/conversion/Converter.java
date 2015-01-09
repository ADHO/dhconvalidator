package org.adho.dhconvalidator.conversion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.SchemaFactory;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.Serializer;

import org.adho.dhconvalidator.conftool.Paper;
import org.adho.dhconvalidator.conftool.User;
import org.adho.dhconvalidator.conversion.input.InputConverter;
import org.adho.dhconvalidator.conversion.input.InputConverterFactory;
import org.adho.dhconvalidator.conversion.output.OutputConverter;
import org.adho.dhconvalidator.conversion.output.OutputConverterFactory;
import org.adho.dhconvalidator.conversion.oxgarage.OxGarageConversionClient;
import org.adho.dhconvalidator.conversion.oxgarage.ZipResult;
import org.adho.dhconvalidator.properties.PropertyKey;
import org.adho.dhconvalidator.util.DocumentLog;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

//TODO: logging
//TODO: validation: schema
//TODO: validation: warning no titles
//TODO: centerpanel about link/box
public class Converter {

	private String contentAsXhtml;
	private Document document;
	private String baseURL;
	
	public Converter(String baseURL) throws IOException {
		this.baseURL = baseURL;
	}

	public ZipResult convert(
			byte[] sourceData, ConversionPath toTeiConversionPath, 
			User user, String inputFilename, ConversionProgressListener progressListener) throws IOException {

		progressListener.setProgress("Getting the latest ConfTool data and preparing input...");
		InputConverterFactory inputConverterFactory = toTeiConversionPath.getInputConverterFactory();
		InputConverter	inputConverter = inputConverterFactory.createInputConverter();
		sourceData = inputConverter.convert(sourceData, user);		
		Paper paper = inputConverter.getPaper();
		
		progressListener.setProgress("Doing conversion via OxGarage...");
		OxGarageConversionClient oxGarageConversionClient = new OxGarageConversionClient(baseURL);
	
		ZipResult zipResult = new ZipResult(oxGarageConversionClient.convert(
				sourceData, 
				toTeiConversionPath, 
				toTeiConversionPath.getDefaultProperties()),
				inputFilename.substring(0, inputFilename.lastIndexOf('.')) + ".xml");
		
		document = zipResult.getDocument();
		
		DocumentLog.logConversionStepOutput("pre output conversion:", document.toXML());
		
		progressListener.setProgress("Finalizing output format...");
		OutputConverterFactory outputConverterFactory = 
				toTeiConversionPath.getOutputConverterFactory(); 
		OutputConverter outputConverter = outputConverterFactory.createOutputConverter();
		outputConverter.convert(document, user, paper);
		outputConverter.convert(zipResult);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		Serializer serializer = new Serializer(bos);
		serializer.setIndent(2);
		serializer.write(document);
		
		validateDocument(bos, progressListener);
		
		DocumentLog.logConversionStepOutput("post output conversion:", bos.toString("UTF-8"));

		progressListener.setProgress("Converting output to HTML for visual feedback...");
		contentAsXhtml = oxGarageConversionClient.convertToString(
				bos.toByteArray(), 
				ConversionPath.TEI_TO_XHTML,
				ConversionPath.TEI_TO_XHTML.getDefaultProperties());

		DocumentLog.logConversionStepOutput("post xhtml conversion", contentAsXhtml);
	
		return zipResult;
	}
	
	private void validateDocument(ByteArrayOutputStream bos, ConversionProgressListener progressListener) throws IOException {
		if (PropertyKey.performSchemaValidation.isTrue()) {
			try {
				progressListener.setProgress("Validating output...");
				SAXParserFactory factory = SAXParserFactory.newInstance();
				factory.setValidating(false);
				factory.setNamespaceAware(true);
	
				URL xsdResource = 
					Thread.currentThread().getContextClassLoader().getResource(
							"/schema/dhconvalidator.xsd");
				
				SchemaFactory schemaFactory = 
						SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
				factory.setSchema(schemaFactory.newSchema(xsdResource));

				SAXParser parser = factory.newSAXParser();
				XMLReader reader = parser.getXMLReader();
				reader.setErrorHandler(new ValidateConversionErrorHandler());
	
				Builder builder = new Builder(reader);
				builder.build(new ByteArrayInputStream(bos.toByteArray()));
			}
			catch (ParsingException | ParserConfigurationException | SAXException e) {
				throw new IOException(e);
			}
		}
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
