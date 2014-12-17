package org.adho.dhconvalidator.ui;

import org.adho.dhconvalidator.conftool.User;

import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

public class LoginResultPanel extends CenterPanel {

	private Button btContinue;
	private Button btRetry;
	private Button logoutLink;

	public LoginResultPanel() {
		this(null);
	}
	
	public LoginResultPanel(String errorMessage) {
		initComponents(errorMessage);
		initActions();
	}

	private void initActions() {
		btRetry.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().setContent(new LoginPanel());
			}
		});
	}

	private void initComponents(String errorMessage) {
		logoutLink = new LogoutLink();
		logoutLink.setVisible(false);

		User user = 
			(User)VaadinSession.getCurrent().getAttribute(SessionStorageKey.USER.name());
		
		btContinue = new Button("Continue");
		new BrowserWindowOpener(DHConvalidatorServices.class).extend(btContinue);
		
		btRetry = new Button("Retry");
		btRetry.setVisible(false);
		
		Label infoLabel = new Label("", ContentMode.HTML);
		if (errorMessage != null) {
			infoLabel.setValue(
				"Authentication failed!<br>Reason:<br>" + errorMessage);
			btContinue.setVisible(false);
			btRetry.setVisible(true);
		}
		else {
			infoLabel.setValue(
				"Hi " + user.getFirstName() + " " + user.getLastName() + ",<br>"
				+ "the authentication was successful, please continue to the DHConvalidator!");
			logoutLink.setVisible(true);
		}
		
		addCenteredComponent(logoutLink, Alignment.TOP_RIGHT);
		addCenteredComponent(infoLabel);
		addCenteredComponent(btContinue);
		addCenteredComponent(btRetry);
	}
}
