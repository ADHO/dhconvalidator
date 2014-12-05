package org.adho.dhconvalidator.ui;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class LoginPanel extends VerticalLayout {
	public static interface LoginListener {
		public void authenticate(String user, char[] pass);
	}
	
	private TextField userNameInput;
	private PasswordField passwordInput;
	private Button btLogin;

	public LoginPanel(LoginListener loginListener) {
		initComponents();
		initActions(loginListener);
	}

	private void initActions(final LoginListener loginListener) {
		btLogin.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				loginListener.authenticate(
					userNameInput.getValue(), 
					passwordInput.getValue().toCharArray());
			}
		});
	}


	private void initComponents() {
	
		userNameInput = new TextField("E-mail or user name", "");
		userNameInput.setSizeUndefined();
		userNameInput.focus();
		passwordInput = new PasswordField("Password", "");
		passwordInput.setSizeUndefined();
		
		btLogin = new Button("Login");
		btLogin.setClickShortcut(KeyCode.ENTER);

		Label caption = new Label("ConfTool Authentication");
		caption.setSizeUndefined();
		caption.addStyleName("login-caption");
		VerticalLayout centerPanel = 
				new VerticalLayout(caption, userNameInput, passwordInput, btLogin);
		centerPanel.setWidth("100%");
		centerPanel.setSpacing(true);
		addComponent(centerPanel);
		
		setComponentAlignment(centerPanel, Alignment.MIDDLE_CENTER);
		centerPanel.setComponentAlignment(caption, Alignment.MIDDLE_CENTER);
		centerPanel.setComponentAlignment(userNameInput, Alignment.MIDDLE_CENTER);
		centerPanel.setComponentAlignment(passwordInput, Alignment.MIDDLE_CENTER);
		centerPanel.setComponentAlignment(btLogin, Alignment.MIDDLE_CENTER);		
	}

}
