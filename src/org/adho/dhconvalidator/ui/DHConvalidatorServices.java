package org.adho.dhconvalidator.ui;

import java.io.IOException;

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
import com.vaadin.shared.communication.PushMode;
import com.vaadin.ui.UI;

import de.catma.backgroundservice.BackgroundService;

@Theme("dhconvalidator")
@PreserveOnRefresh
@Push(value=PushMode.MANUAL)
public class DHConvalidatorServices extends UI {
	
	private BackgroundService backgroundService;

	@Override
	protected void init(VaadinRequest request) {
		backgroundService = new UIBackgroundService(true);
		
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
			
			try {
				VaadinSession.getCurrent().addRequestHandler(
				        new ExternalResourceRequestHandler(
				        		PropertyKey.tei_image_location.getValue()));
			} catch (IOException e) {
				throw new IllegalStateException("cannot find example files", e);
			}
			Page.getCurrent().setTitle("DHConvalidator Services");
		}
	}

	public BackgroundService getBackgroundService() {
		return backgroundService;
	}
}
