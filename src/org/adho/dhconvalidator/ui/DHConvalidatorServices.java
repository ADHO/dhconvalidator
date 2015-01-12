package org.adho.dhconvalidator.ui;

import java.io.IOException;

import org.adho.dhconvalidator.Messages;
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
			Page.getCurrent().setTitle(Messages.getString("DHConvalidatorServices.loginTitle")); //$NON-NLS-1$
		}
		else {
			Navigator navigator = new Navigator(this, this);
			navigator.addView(
				"",  //$NON-NLS-1$
				new ServiceSelectionPanel());
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
				throw new IllegalStateException(Messages.getString("DHConvalidatorServices.errorExampleFiles"), e); //$NON-NLS-1$
			}
			Page.getCurrent().setTitle(Messages.getString("DHConvalidatorServices.servicesTitle")); //$NON-NLS-1$
		}
	}

	public BackgroundService getBackgroundService() {
		return backgroundService;
	}
}
