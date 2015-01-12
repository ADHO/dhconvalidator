package org.adho.dhconvalidator.conversion.oxgarage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.Serializer;

import org.adho.dhconvalidator.conversion.ByteArrayStreamRepresentation;
import org.adho.dhconvalidator.conversion.ConversionPath;
import org.adho.dhconvalidator.conversion.Type;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.ext.html.FormData;
import org.restlet.ext.html.FormDataSet;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StreamRepresentation;
import org.restlet.resource.ClientResource;

public class OxGarageConversionClient {
	
	private static final String CONVERSION_OPERATION = "Conversions/"; //$NON-NLS-1$
	
	private String baseURL;
	
	public OxGarageConversionClient(String baseURL) {
		this.baseURL = baseURL;
	}
	
	public String convertToString(
			byte[] sourceStream, 
			ConversionPath conversionPath, 
			Properties properties) throws IOException {
		
		ZipResult zipResult = new ZipResult(convert(sourceStream, conversionPath, properties));
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		Serializer serializer = new Serializer(buffer);
		serializer.setIndent(2);
		serializer.write(zipResult.getDocument());
		return buffer.toString("UTF-8"); //$NON-NLS-1$
	}
	
	public InputStream convert(
			final byte[] sourceStream, 
			ConversionPath conversionPath, 
			Properties properties) throws IOException {
		StreamRepresentation sr = new ByteArrayStreamRepresentation(sourceStream);
		return convert(sr, conversionPath, properties);
	}
	
	
	private InputStream convert(
			Representation sourceRep, 
			ConversionPath conversionPath, 
			Properties properties) throws IOException {
		
		String uri = baseURL + CONVERSION_OPERATION + conversionPath.getPath();
		if (!properties.isEmpty()) {
			Document propertyDoc = new Document(new Element("conversions")); //$NON-NLS-1$
			Element conversion = new Element("conversion"); //$NON-NLS-1$
			conversion.addAttribute(new Attribute("index", "0")); //$NON-NLS-1$ //$NON-NLS-2$
			propertyDoc.getRootElement().appendChild(conversion);
			
			Enumeration<?> propertyNames = properties.propertyNames(); 
			
			while (propertyNames.hasMoreElements()) {
				String key = propertyNames.nextElement().toString();
				Element property = new Element("property"); //$NON-NLS-1$
				property.addAttribute(new Attribute("id", key)); //$NON-NLS-1$
				property.appendChild(properties.getProperty(key));
				conversion.appendChild(property);
			}

			uri += "?properties=" + propertyDoc.getRootElement().toXML(); //$NON-NLS-1$
		}
		
		ClientResource client = new ClientResource(Context.getCurrent(), Method.POST, uri);

		FormDataSet form = new FormDataSet();
		form.setMultipart(true);
		form.getEntries().add(new FormData("upload", sourceRep)); //$NON-NLS-1$
		
		Representation result = client.post(form);
		return result.getStream();
	}
	
	Document getInputDataTypes() throws IOException {
		String uri = baseURL + CONVERSION_OPERATION;
		ClientResource client = new ClientResource(Context.getCurrent(), Method.GET, uri);

		Representation result = client.get();
		Builder builder = new Builder();
		try {
			Document inputDataTypes = builder.build(result.getStream());
			
			return inputDataTypes;
		}
		catch (ParsingException e) {
			throw new IOException(e);
		}
	}
	
	Document getConversions(Type type) throws IOException {
		String uri = baseURL + CONVERSION_OPERATION + type.getIdentifier();
		ClientResource client = new ClientResource(Context.getCurrent(), Method.GET, uri);
		Representation result = client.get();
		Builder builder = new Builder();
		try {
			Document conversions = builder.build(result.getStream());
			
			return conversions;
		}
		catch (ParsingException e) {
			throw new IOException(e);
		}
	}
	
	
	@SuppressWarnings("unused")
	private String convertToString(
			File file, ConversionPath conversionPath, Properties properties) throws IOException {
		
		ZipResult zipResult = new ZipResult(convert(file, conversionPath, properties));
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		Serializer serializer = new Serializer(buffer);
		serializer.setIndent(2);
		serializer.write(zipResult.getDocument());
		return buffer.toString("UTF-8"); //$NON-NLS-1$
	}

	private InputStream convert(
			File file, ConversionPath conversionPath, Properties properties) throws IOException {
		FileRepresentation fr = new FileRepresentation(file, MediaType.APPLICATION_ALL);
		return convert(fr, conversionPath, properties);
	}
}
