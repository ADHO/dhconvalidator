package org.adho.dhconvalidator.ui;

import java.io.IOException;

import org.adho.dhconvalidator.Messages;
import org.adho.dhconvalidator.conftool.ConfToolClient;
import org.adho.dhconvalidator.conftool.ConfToolClient.AuthenticationException;
import org.adho.dhconvalidator.conftool.User;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

public class LoginPanel extends CenterPanel {

	private TextField userNameInput;
	private PasswordField passwordInput;
	private Button btLogin;

	public LoginPanel() {
		super(false);
		initComponents();
		initActions();
	}

	private void initActions() {
		btLogin.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				authenticate(
					userNameInput.getValue(), 
					passwordInput.getValue().toCharArray());
			}
		});
	}


	protected void authenticate(String username, char[] pass) {
		ConfToolClient confToolClient = new ConfToolClient();
		try {
			User user = confToolClient.authenticate(username, pass);

			user = 
				confToolClient.getDetailedUser(user);
			
			VaadinSession.getCurrent().setAttribute(
				SessionStorageKey.USER.name(), user);
			UI.getCurrent().setContent(new LoginResultPanel());
		} catch (IOException e) {
			e.printStackTrace();
			UI.getCurrent().setContent(new LoginResultPanel(e.getLocalizedMessage()));
		} catch (AuthenticationException a) {
			a.printStackTrace();
			UI.getCurrent().setContent(new LoginResultPanel(a.getLocalizedMessage()));
		}
	}

	private void initComponents() {
	
		userNameInput = new TextField(Messages.getString("LoginPanel.userName"), ""); //$NON-NLS-1$ //$NON-NLS-2$
		userNameInput.focus();
		passwordInput = new PasswordField(Messages.getString("LoginPanel.password"), ""); //$NON-NLS-1$ //$NON-NLS-2$
		
		btLogin = new Button(Messages.getString("LoginPanel.login")); //$NON-NLS-1$
		btLogin.setClickShortcut(KeyCode.ENTER);

		Label caption = new Label(Messages.getString("LoginPanel.title"), ContentMode.HTML); //$NON-NLS-1$
		caption.addStyleName("login-caption"); //$NON-NLS-1$
		addCenteredComponent(caption);
		addCenteredComponent(userNameInput);
		addCenteredComponent(passwordInput);
		addCenteredComponent(btLogin);		
	}

}
