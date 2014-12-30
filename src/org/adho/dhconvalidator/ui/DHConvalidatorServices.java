package org.adho.dhconvalidator.ui;

import org.adho.dhconvalidator.conversion.input.docx.DocxInputConverter;
import org.adho.dhconvalidator.conversion.input.odt.OdtInputConverter;
import org.adho.dhconvalidator.properties.PropertyKey;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.Page;
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
			Page.getCurrent().setTitle("DHConvalidator Login");
		}
		else {
			Navigator navigator = new Navigator(this, this);
			navigator.addView("", new ServiceSelectionPanel());
			navigator.addView(
				ServicesViewName.odt.name(), 
				new PaperSelectionPanel(new OdtInputConverter()));
			navigator.addView(
				ServicesViewName.docx.name(), 
				new PaperSelectionPanel(new DocxInputConverter()));
			navigator.addView(
				ServicesViewName.converter.name(), 
				new ConverterPanel());
			
			VaadinSession.getCurrent().addRequestHandler(
			        new ExternalResourceRequestHandler(
			        		PropertyKey.tei_pictures_location.getValue(),
			        		PropertyKey.tei_media_location.getValue()));
			Page.getCurrent().setTitle("DHConvalidator Services");
		}
	}

}
