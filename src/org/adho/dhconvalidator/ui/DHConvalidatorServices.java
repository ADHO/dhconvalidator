package org.adho.dhconvalidator.ui;

import org.adho.dhconvalidator.conversion.input.OdtInputConverter;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

@Theme("dhconvalidator")
@PreserveOnRefresh
@Push
public class DHConvalidatorServices extends UI {

	@Override
	protected void init(VaadinRequest request) {
		if ((VaadinSession.getCurrent().getAttribute(SessionStorageKey.USER.name())) == null) {
			setContent(new LoginPanel());
		}
		else {
			Navigator navigator = new Navigator(this, this);
			navigator.addView("", new ServiceSelectionPanel());
			navigator.addView(
				ServicesViewName.odt.name(), 
				new PaperSelectionPanel(new OdtInputConverter()));
			navigator.addView(
				ServicesViewName.converter.name(), 
				new ConverterPanel());
			
			VaadinSession.getCurrent().addRequestHandler(
			        new ExternalResourceRequestHandler("/Pictures")); //TODO: config
		}
	}

}
