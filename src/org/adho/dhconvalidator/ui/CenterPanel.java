/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.ui;

import org.adho.dhconvalidator.Messages;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * A panel that displays its components centered and has an optional 
 * {@link HeaderPanel}.
 * @author marco.petris@web.de
 *
 */
public class CenterPanel extends VerticalLayout {
	
	private VerticalLayout center;
	private HeaderPanel headerPanel;

	/**
	 * @param showHeader <code>true</code>->display the {@link HeaderPanel}.
	 * @param backstepService the service to navigate to via back link 
	 */
	public CenterPanel(boolean showHeader, ServicesViewName backstepService) {
		initComponents(showHeader, backstepService);
	}
	
	public CenterPanel(boolean showHeader) {
		this(showHeader, null);
	}

	private void initComponents(boolean showHeader, ServicesViewName backstepService) {
		center = new VerticalLayout();
		center.setSpacing(true);
		setSizeFull();
		addComponent(center);
		center.setSizeUndefined();
		setComponentAlignment(center, Alignment.MIDDLE_CENTER);
		if (showHeader) {
			headerPanel = new HeaderPanel(backstepService);
			center.addComponent(headerPanel);
			Label title = new Label(getTitle());
			title.addStyleName("title-caption"); //$NON-NLS-1$
			center.addComponent(title);
			center.setComponentAlignment(title, Alignment.TOP_LEFT);
		}
	}

	public void addCenteredComponent(Component c) {
		center.addComponent(c);
	}
	
	public void addCenteredComponent(Component c, Alignment alignment) {
		center.addComponent(c);
		center.setComponentAlignment(c, alignment);
	}

	/**
	 * @return the backlink instance or <code>null</code>
	 */
	public BackLink getBackLink() {
		if (headerPanel == null) {
			return null;
		}
		return headerPanel.getBackLink();
	}

	protected String getTitle() {
		return Messages.getString("CenterPanel.title");  //$NON-NLS-1$
	}
	
}
