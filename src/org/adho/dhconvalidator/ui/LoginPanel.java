package org.adho.dhconvalidator.ui;

import java.io.IOException;

import org.adho.dhconvalidator.conftool.ConfToolCacheProvider;
import org.adho.dhconvalidator.conftool.ConfToolClient;
import org.adho.dhconvalidator.conftool.ConfToolClient.AuthenticationException;
import org.adho.dhconvalidator.conftool.User;
import org.adho.dhconvalidator.properties.PropertyKey;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.VaadinSession;
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
		ConfToolClient confToolClient = 
				new ConfToolClient(
					PropertyKey.conftool_url.getValue(), 
					PropertyKey.conftool_shared_pass.getValue().toCharArray());
		try {
			User user = confToolClient.authenticate(username, pass);

			user = 
				ConfToolCacheProvider.INSTANCE.getConfToolCache().getDetailedUser(user);
			
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
	
		userNameInput = new TextField("E-mail or user name", "");
		userNameInput.focus();
		passwordInput = new PasswordField("Password", "");
		
		btLogin = new Button("Login");
		btLogin.setClickShortcut(KeyCode.ENTER);

		Label caption = new Label("ConfTool Authentication");
		caption.addStyleName("login-caption");
		addCenteredComponent(caption);
		addCenteredComponent(userNameInput);
		addCenteredComponent(passwordInput);
		addCenteredComponent(btLogin);		
	}

}
