/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.ui;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;

/**
 * A header with logout-, back- and about-links.
 * 
 * @author marco.petris@web.de
 *
 */
public class HeaderPanel extends HorizontalLayout {
	
	private BackLink backLink;

	public HeaderPanel() {
		initComponents();
	}

	/**
	 * Setup UI.
	 */
	private void initComponents() {
		setSpacing(true);
		
		setWidth("100%"); //$NON-NLS-1$

		backLink = new BackLink();
		addComponent(backLink);
		setComponentAlignment(backLink, Alignment.TOP_LEFT);
		
		AboutLink aboutLink = new AboutLink();
		addComponent(aboutLink);
		setComponentAlignment(aboutLink, Alignment.TOP_RIGHT);
		setExpandRatio(aboutLink, 1.0f);

		LogoutLink logoutLink = new LogoutLink();
		addComponent(logoutLink);
		setComponentAlignment(logoutLink, Alignment.TOP_RIGHT);
				
	}
	
	public BackLink getBackLink() {
		return backLink;
	}

}
