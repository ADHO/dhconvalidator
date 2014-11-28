package org.adho.dhconvalidator.ui;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
@Theme("dhconvalidator")
public class DHConvalidatorUI extends UI {

	@Override
	protected void init(VaadinRequest request) {
		ConverterPanel uploadPanel = new ConverterPanel();
		
		setContent(uploadPanel);
		
		VaadinSession.getCurrent().addRequestHandler(
		        new ExternalResourceRequestHandler("/Pictures")); //TODO: config
	}

}