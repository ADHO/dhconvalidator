package org.adho.dhconvalidator.ui;

import java.io.IOException;

import org.adho.dhconvalidator.conversion.oxgarage.ZipResult;
import org.adho.dhconvalidator.properties.PropertyKey;

import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;

final class ExternalResourceRequestHandler implements
		RequestHandler {
	private static final int PATH_PREFIX_LENGTH = 7;
	private String imagePath;
	private ZipResult exampleZipResult;
	private String examplePictureName;
	
	public ExternalResourceRequestHandler(String imagePath) throws IOException {
		super();
		this.imagePath = imagePath;
		this.exampleZipResult = 
			new ZipResult(
				Thread.currentThread().getContextClassLoader().getResourceAsStream(
					"/org/adho/dhconvalidator/conversion/example/1_Digital_Humanities.dhc"));
		this.examplePictureName = 
			exampleZipResult.getExternalResourcePathsStartsWith(
				PropertyKey.tei_image_location.getValue().substring(1)) // skip leading slash
				.get(0) // there has to be exactly one image
				.substring(PropertyKey.tei_image_location.getValue().length()); // skip path information
		//10000000000000DC000000A53B7BF588.jpg
	}

	@Override
	public boolean handleRequest(VaadinSession session,
	                             VaadinRequest request,
	                             VaadinResponse response)
	        throws IOException {

		if (request.getPathInfo().startsWith("/popup"+imagePath)) {
			ZipResult zipResult =
				(ZipResult) VaadinSession.getCurrent().getAttribute(
						SessionStorageKey.ZIPRESULT.name());
			
			if (request.getPathInfo().endsWith(examplePictureName)) {
				zipResult = exampleZipResult;
			}
			
			if (zipResult != null) {
				byte[] resource = zipResult.getExternalResource(
						request.getPathInfo().substring(PATH_PREFIX_LENGTH));
				if (resource != null) {
					response.getOutputStream().write(resource);
					return true;
				}
				else {
					throw new IOException("external resource not found: " 
							+ request.getPathInfo().substring(PATH_PREFIX_LENGTH));
				}
			}
		}
		return false;
	}
}