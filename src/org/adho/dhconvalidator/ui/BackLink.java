/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.ui;

import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.BaseTheme;
import org.adho.dhconvalidator.Messages;

/**
 * The 'back' link.
 *
 * @author marco.petris@web.de
 */
public class BackLink extends Button {
  public BackLink(final ServicesViewName backstepService) {
    super(Messages.getString("BackLink.title")); // $NON-NLS-1$
    setStyleName(BaseTheme.BUTTON_LINK);
    addStyleName("plain-link"); // $NON-NLS-1$
    addClickListener(
        new ClickListener() {

          @Override
          public void buttonClick(ClickEvent event) {
            UI.getCurrent()
                .getNavigator()
                .navigateTo((backstepService == null) ? "" : backstepService.name()); // $NON-NLS-1$
          }
        });
  }
}
