/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.ui;

import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import org.adho.dhconvalidator.Messages;
import org.adho.dhconvalidator.user.User;

/**
 * A panel that displays the result of the Login.
 *
 * @author marco.petris@web.de
 */
public class LoginResultPanel extends CenterPanel {

  private Button btContinue;
  private Button btRetry;
  private Button logoutLink;

  /** Authentication succesfull */
  public LoginResultPanel() {
    this(null);
  }

  /** @param errorMessage authentication error */
  public LoginResultPanel(String errorMessage) {
    super(false);
    initComponents(errorMessage);
    initActions();
  }

  /** Setup behaviour. */
  private void initActions() {
    btRetry.addClickListener(
        new ClickListener() {

          @Override
          public void buttonClick(ClickEvent event) {
            UI.getCurrent().setContent(new LoginPanel());
          }
        });
  }

  /**
   * Setup UI.
   *
   * @param errorMessage
   */
  private void initComponents(String errorMessage) {
    logoutLink = new LogoutLink();
    logoutLink.setVisible(false);

    User user = (User) VaadinSession.getCurrent().getAttribute(SessionStorageKey.USER.name());

    // if authencication has been successful we open up a new tab to show the DHConvalidator
    // services
    btContinue = new Button(Messages.getString("LoginResultPanel.continue"));
    new BrowserWindowOpener(DHConvalidatorServices.class).extend(btContinue);

    btRetry = new Button(Messages.getString("LoginResultPanel.retry"));
    btRetry.setVisible(false);

    Label infoLabel = new Label("", ContentMode.HTML);
    if (errorMessage != null) {
      infoLabel.setValue(
          Messages.getString("LoginResultPanel.authenticationFailure", errorMessage));
      btContinue.setVisible(false);
      btRetry.setVisible(true);
    } else {
      infoLabel.setValue(
          Messages.getString("LoginResultPanel.greeting", user.getFirstName(), user.getLastName()));
      logoutLink.setVisible(true);
    }

    addCenteredComponent(logoutLink, Alignment.TOP_RIGHT);
    addCenteredComponent(infoLabel);
    addCenteredComponent(btContinue);
    addCenteredComponent(btRetry);
  }
}
