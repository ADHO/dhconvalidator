package org.adho.dhconvalidator.ui;

import java.io.IOException;

import org.adho.dhconvalidator.conversion.ZipResult;

import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;

final class ExternalResourceRequestHandler implements
		RequestHandler {
	
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
		if (request.getPathInfo().startsWith(imagePath)) { //TODO: more external resoures?
			ZipResult zipResult =
				(ZipResult) VaadinSession.getCurrent().getAttribute(SessionStorageKey.ZIPRESULT.name());
			if (zipResult != null) {
				byte[] resource = zipResult.getExternalResource(request.getPathInfo().substring(1));
				response.getOutputStream().write(resource);
				return true;
			}
		}
		return false;
	}
}