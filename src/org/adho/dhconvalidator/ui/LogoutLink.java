/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.adho.dhconvalidator.Messages;

import com.vaadin.server.Page;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.BaseTheme;

/**
 * A logout link.
 * 
 * @author marco.petris@web.de
 *
 */
public class LogoutLink extends Button {
	
	public LogoutLink() {
		super(Messages.getString("LogoutLink.title")); //$NON-NLS-1$
		setStyleName(BaseTheme.BUTTON_LINK);
		addStyleName("plain-link"); //$NON-NLS-1$
		addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				List<Page> pages = new ArrayList<>();
				// keep the pages...
				for (UI ui : VaadinSession.getCurrent().getUIs()) {
					Page page = ui.getPage();
					if (page != null){
						pages.add(page);
					}
				}
					
				VaadinSession.getCurrent().close();

				//... to notify them of the session close
				for (Page p : pages) {
					try {
						p.reload();
					}
					catch (Exception e) {
						Logger.getLogger(""); //$NON-NLS-1$
					}
				}			
			}
		});
	}

}
