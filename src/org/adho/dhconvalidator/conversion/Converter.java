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

import org.adho.dhconvalidator.Messages;
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

		progressListener.setProgress(Messages.getString("Converter.progress1")); //$NON-NLS-1$
		InputConverterFactory inputConverterFactory = toTeiConversionPath.getInputConverterFactory();
		InputConverter	inputConverter = inputConverterFactory.createInputConverter();
		sourceData = inputConverter.convert(sourceData, user);		
		Paper paper = inputConverter.getPaper();
		
		progressListener.setProgress(Messages.getString("Converter.progress2")); //$NON-NLS-1$
		OxGarageConversionClient oxGarageConversionClient = new OxGarageConversionClient(baseURL);
	
		ZipResult zipResult = new ZipResult(oxGarageConversionClient.convert(
				sourceData, 
				toTeiConversionPath, 
				toTeiConversionPath.getDefaultProperties()),
				inputFilename.substring(0, inputFilename.lastIndexOf('.')) + ".xml"); //$NON-NLS-1$
		
		document = zipResult.getDocument();
		
		DocumentLog.logConversionStepOutput(Messages.getString("Converter.log1"), document.toXML()); //$NON-NLS-1$
		
		progressListener.setProgress(Messages.getString("Converter.progress3")); //$NON-NLS-1$
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
		
		DocumentLog.logConversionStepOutput(Messages.getString("Converter.log2"), bos.toString("UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$

		progressListener.setProgress(Messages.getString("Converter.progress4")); //$NON-NLS-1$
		contentAsXhtml = oxGarageConversionClient.convertToString(
				bos.toByteArray(), 
				ConversionPath.TEI_TO_XHTML,
				ConversionPath.TEI_TO_XHTML.getDefaultProperties());

		DocumentLog.logConversionStepOutput(Messages.getString("Converter.log3"), contentAsXhtml); //$NON-NLS-1$
		
		zipResult.putResource(
			inputFilename.substring(0, inputFilename.lastIndexOf('.')) + ".html", 
			contentAsXhtml.getBytes("UTF-8"));
		
		return zipResult;
	}
	
	private void validateDocument(ByteArrayOutputStream bos, ConversionProgressListener progressListener) throws IOException {
		if (PropertyKey.performSchemaValidation.isTrue()) {
			try {
				progressListener.setProgress(Messages.getString("Converter.progress5")); //$NON-NLS-1$
				SAXParserFactory factory = SAXParserFactory.newInstance();
				factory.setValidating(false);
				factory.setNamespaceAware(true);
	
				URL xsdResource = 
					Thread.currentThread().getContextClassLoader().getResource(
							"/schema/dhconvalidator.xsd"); //$NON-NLS-1$
				
				SchemaFactory schemaFactory = 
						SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema"); //$NON-NLS-1$
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
