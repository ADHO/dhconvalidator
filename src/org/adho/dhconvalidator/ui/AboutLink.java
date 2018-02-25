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
 * The About Link.
 *
 * @author marco.petris@web.de
 */
public class AboutLink extends Button {

  public AboutLink() {
    super(Messages.getString("AboutLink.title"));
    setStyleName(BaseTheme.BUTTON_LINK);
    addStyleName("plain-link");
    addClickListener(
        new ClickListener() {

          @Override
          public void buttonClick(ClickEvent event) {
            UI.getCurrent().addWindow(new AboutWindow());
          }
        });
  }
}
