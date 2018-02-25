/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.ui;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import org.adho.dhconvalidator.Messages;

/**
 * The UI that handles the Login.
 *
 * @author marco.petris@web.de
 */
@Theme("dhconvalidator")
@PreserveOnRefresh
// @Push(value=PushMode.MANUAL, transport=Transport.LONG_POLLING)
public class DHConvalidatorLogin extends UI {
  @Override
  protected void init(VaadinRequest request) {
    Messages.setLocaleProvider(VaadinSessionLocaleProvider.INSTANCE);

    final LoginPanel loginPanel = new LoginPanel();

    setContent(loginPanel);
  }
}
