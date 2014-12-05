package org.adho.dhconvalidator.ui;

import java.io.IOException;

import org.adho.dhconvalidator.conftool.ConfToolClient;
import org.adho.dhconvalidator.conftool.ConfToolClient.AuthenticationException;
import org.adho.dhconvalidator.conftool.User;
import org.adho.dhconvalidator.properties.PropertyKey;
import org.adho.dhconvalidator.ui.LoginPanel.LoginListener;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
@Theme("dhconvalidator")
public class DHConvalidatorUI extends UI {

	private class ConfToolAuthenticationHandler implements LoginListener {
		
		private ComponentContainer targetPanel;

		private ConfToolAuthenticationHandler(ComponentContainer targetPanel) {
			this.targetPanel = targetPanel;
		}

		@Override
		public void authenticate(String username, char[] pass) {
			ConfToolClient confToolClient = 
				new ConfToolClient(
					PropertyKey.conftool_url.getValue(), 
					PropertyKey.conftool_shared_pass.getValue().toCharArray());
			try {
				User user = confToolClient.authenticate(username, pass);

				
				VaadinSession.getCurrent().setAttribute(
					SessionStorageKey.USER.name(), user);
				setContent(targetPanel);
				Notification.show(
					"Info",
					"Login was successful!",
					Type.TRAY_NOTIFICATION);
					
			} catch (IOException e) {
				Notification.show(
					"Problem",
					"There was a problem during the authentication: " + e.getLocalizedMessage(), 
					Type.ERROR_MESSAGE);
			} catch (AuthenticationException a) {
				Notification.show(
					"Info", 
					"Authentication failed! User name or password may be wrong.", 
					Type.TRAY_NOTIFICATION);
			}
			
		}
	}

	@Override
	protected void init(VaadinRequest request) {
//		final ConverterPanel uploadPanel = new ConverterPanel();

		final LoginPanel loginPanel = 
				new LoginPanel(new ConfToolAuthenticationHandler(new ServiceSelectionPanel()));
		
		setContent(loginPanel);
		
		VaadinSession.getCurrent().addRequestHandler(
		        new ExternalResourceRequestHandler("/Pictures")); //TODO: config
	}

}