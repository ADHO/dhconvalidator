/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
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

/**
 * The converter that handles all steps to convert some input data to
 * the final {@link ZipResult} that gets returned the user.
 * 
 * @author marco.petris@web.de
 *
 */
public class Converter {

	private String contentAsXhtml;
	private Document document;
	private String oxGarageBaseURL;
	
	/**
	 * @param oxGarageBaseURL the URL for the OxGarage web service
	 * @throws IOException in case of any failure
	 */
	public Converter(String oxGarageBaseURL) throws IOException {
		this.oxGarageBaseURL = oxGarageBaseURL;
	}

	/**
	 * @param sourceData the source data
	 * @param toTeiConversionPath the path to convert the input to TEI
	 * @param user the user who initiated the conversion
	 * @param inputFilename the name of the inputfile
	 * @param progressListener a listener that can be notified about progress
	 * @return the result package
	 * @throws IOException in case of any failure
	 */
	public ZipResult convert(
			byte[] sourceData, ConversionPath toTeiConversionPath, 
			User user, String inputFilename, ConversionProgressListener progressListener) throws IOException {

		progressListener.setProgress(Messages.getString("Converter.progress1")); //$NON-NLS-1$
		// do input conversion to prepare the data for the OxGarage service
		InputConverterFactory inputConverterFactory = toTeiConversionPath.getInputConverterFactory();
		InputConverter	inputConverter = inputConverterFactory.createInputConverter();
		sourceData = inputConverter.convert(sourceData, user);		
		Paper paper = inputConverter.getPaper();
		
		progressListener.setProgress(Messages.getString("Converter.progress2")); //$NON-NLS-1$
		// do the OxGarage conversion
		OxGarageConversionClient oxGarageConversionClient = new OxGarageConversionClient(oxGarageBaseURL);
	
		ZipResult zipResult = new ZipResult(oxGarageConversionClient.convert(
				sourceData, 
				toTeiConversionPath, 
				toTeiConversionPath.getDefaultProperties()),
				inputFilename.substring(0, inputFilename.lastIndexOf('.')) + ".xml"); //$NON-NLS-1$
		
		document = zipResult.getDocument();
		
		DocumentLog.logConversionStepOutput(Messages.getString("Converter.log1"), document.toXML()); //$NON-NLS-1$
		
		progressListener.setProgress(Messages.getString("Converter.progress3")); //$NON-NLS-1$
		// do the post processing to tweak the resulting TEI
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
		
		// do the conversion to HTML via OxGarage
		contentAsXhtml = oxGarageConversionClient.convertToString(
				bos.toByteArray(), 
				ConversionPath.TEI_TO_XHTML,
				ConversionPath.TEI_TO_XHTML.getDefaultProperties());

		DocumentLog.logConversionStepOutput(Messages.getString("Converter.log3"), contentAsXhtml); //$NON-NLS-1$
		
		// add the HTML to the result
		zipResult.putResource(
			inputFilename.substring(0, inputFilename.lastIndexOf('.')) + ".html", 
			contentAsXhtml.getBytes("UTF-8"));
		
		return zipResult;
	}
	
	/**
	 * Validates the given data against the DHConvalidator schema.
	 * @param bos the data to be validated
	 * @param progressListener a listner that can be notified about progress
	 * @throws IOException in case of any failure
	 */
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

	/**
	 * @return the TEI document of the conversion result
	 */
	public Document getDocument() {
		return document;
	}
	
	/**
	 * @return the TEI->HTML conversionn result for visual feedback
	 */
	public String getContentAsXhtml() {
		return contentAsXhtml;
	}

}
