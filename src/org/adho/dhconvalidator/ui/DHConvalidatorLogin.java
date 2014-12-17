package org.adho.dhconvalidator.ui;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
@Theme("dhconvalidator")
@PreserveOnRefresh
@Push
public class DHConvalidatorLogin extends UI {
	@Override
	protected void init(VaadinRequest request) {
		final LoginPanel loginPanel = new LoginPanel();
		
		setContent(loginPanel);
		
		VaadinSession.getCurrent().addRequestHandler(
		        new ExternalResourceRequestHandler("/Pictures")); //TODO: config
	}

}