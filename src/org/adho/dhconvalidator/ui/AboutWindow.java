/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.ui;

import org.adho.dhconvalidator.Messages;
import org.adho.dhconvalidator.properties.PropertyKey;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * The About box.
 * @author marco.petris@web.de
 *
 */
public class AboutWindow extends Window {
	
	public AboutWindow() {
		super(Messages.getString("AboutWindow.title")); //$NON-NLS-1$
		initComponents();
	}

	private void initComponents() {
		setModal(true);
		setHeight("100px"); //$NON-NLS-1$
		setWidth("300px"); //$NON-NLS-1$
		center();
		VerticalLayout content = new VerticalLayout();
		content.setMargin(true);
		content.setSizeFull();
		Label aboutLabel = new Label(
				Messages.getString("AboutWindow.info",  //$NON-NLS-1$
						PropertyKey.version.getValue()),
						ContentMode.HTML);
		content.addComponent(aboutLabel);
		content.setComponentAlignment(aboutLabel, Alignment.MIDDLE_CENTER);
		
		setContent(content);
		
	}

}
