package org.adho.dhconvalidator.ui;

import java.io.IOException;

import org.adho.dhconvalidator.conversion.oxgarage.ZipResult;

import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;

final class ExternalResourceRequestHandler implements
		RequestHandler {
	private static final int PATH_PREFIX_LENGTH = 7;
	private String imagePath;
	
	public ExternalResourceRequestHandler(String imagePath) {
		super();
		this.imagePath = imagePath;
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
			if (zipResult != null) {
				byte[] resource = zipResult.getExternalResource(
						request.getPathInfo().substring(PATH_PREFIX_LENGTH));
				response.getOutputStream().write(resource);
				return true;
			}
		}
		return false;
	}
}