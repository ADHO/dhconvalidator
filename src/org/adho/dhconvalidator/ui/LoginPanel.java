/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.ui;

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
import java.io.IOException;
import org.adho.dhconvalidator.Messages;
import org.adho.dhconvalidator.properties.PropertyKey;
import org.adho.dhconvalidator.user.User;
import org.adho.dhconvalidator.user.UserProvider;

/**
 * The Login box.
 *
 * @author marco.petris@web.de
 */
public class LoginPanel extends CenterPanel {

  private TextField userNameInput;
  private PasswordField passwordInput;
  private Button btLogin;

  public LoginPanel() {
    super(false);
    initComponents();
    initActions();
  }

  /** Setup behaviour. */
  private void initActions() {
    btLogin.addClickListener(
        new ClickListener() {
          @Override
          public void buttonClick(ClickEvent event) {
            authenticate(userNameInput.getValue(), passwordInput.getValue().toCharArray());
          }
        });
  }

  /**
   * Authentication via ConfTool.
   *
   * @param username
   * @param pass
   */
  protected void authenticate(String username, char[] pass) {
    UserProvider userProvider = PropertyKey.getUserProviderInstance();
    try {
      User user = userProvider.authenticate(username, pass);

      user = userProvider.getDetailedUser(user);

      VaadinSession.getCurrent().setAttribute(SessionStorageKey.USER.name(), user);
      UI.getCurrent().setContent(new LoginResultPanel());
    } catch (IOException e) {
      e.printStackTrace();
      UI.getCurrent().setContent(new LoginResultPanel(e.getLocalizedMessage()));
    } catch (UserProvider.AuthenticationException a) {
      a.printStackTrace();
      UI.getCurrent().setContent(new LoginResultPanel(a.getLocalizedMessage()));
    }
  }

  /** Setup UI. */
  private void initComponents() {

    userNameInput = new TextField(Messages.getString("LoginPanel.userName"), "");
    userNameInput.focus();
    passwordInput = new PasswordField(Messages.getString("LoginPanel.password"), "");

    btLogin = new Button(Messages.getString("LoginPanel.login"));
    btLogin.setClickShortcut(KeyCode.ENTER);

    Label caption = new Label(Messages.getString("LoginPanel.title"), ContentMode.HTML);
    caption.addStyleName("login-caption");
    addCenteredComponent(caption);
    addCenteredComponent(userNameInput);
    addCenteredComponent(passwordInput);
    addCenteredComponent(btLogin);
  }
}
