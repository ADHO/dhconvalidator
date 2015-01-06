package org.adho.dhconvalidator.ui;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

@Theme("dhconvalidator")
@PreserveOnRefresh
public class DHConvalidatorLogin extends UI {
	@Override
	protected void init(VaadinRequest request) {
		final LoginPanel loginPanel = new LoginPanel();
		
		setContent(loginPanel);
	}

}