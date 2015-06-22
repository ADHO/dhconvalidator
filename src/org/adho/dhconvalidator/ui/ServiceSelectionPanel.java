/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.ui;

import org.adho.dhconvalidator.Messages;
import org.adho.dhconvalidator.user.User;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * A panel that lets the user select the service to use.
 * 
 * @author marco.petris@web.de
 *
 */
public class ServiceSelectionPanel extends CenterPanel implements View {
	
	private Button btTemplateGeneration;
	private Button btConversion;
	private Button btConfTool;

	public ServiceSelectionPanel() {
		super(true);
		initComponents();
		initActions();
		
	}

	/**
	 * Setup behaviour.
	 */
	private void initActions() {
		btConversion.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().getNavigator().navigateTo(
						ServicesViewName.converter.name());
			}
		});

		btTemplateGeneration.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().getNavigator().navigateTo(
						ServicesViewName.templates.name());
			}
		});
		
		btConfTool.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().getNavigator().navigateTo(
						ServicesViewName.conftoolupload.name());
			}
		});
	}

	/**
	 * Setup UI.
	 */
	private void initComponents() {
		User user = (User) VaadinSession.getCurrent().getAttribute(SessionStorageKey.USER.name());
		if (user.isAdmin()) {
			UserSwitchPanel userSwitchPanel = new UserSwitchPanel();
			addCenteredComponent(userSwitchPanel, Alignment.TOP_RIGHT);
		}
		
		Label infoLabel = new Label(
			Messages.getString("ServiceSelectionPanel.intro"), //$NON-NLS-1$
			ContentMode.HTML);
		infoLabel.setWidth("600px"); //$NON-NLS-1$
		addCenteredComponent(infoLabel);
		
		VerticalLayout option1Panel = new VerticalLayout();
		option1Panel.setSpacing(true);
		option1Panel.setMargin(new MarginInfo(true, false, true, false));
		addCenteredComponent(option1Panel);
		
		Label option1Label = new Label(
			Messages.getString("ServiceSelectionPanel.option1Info"),  //$NON-NLS-1$
			ContentMode.HTML);
		option1Panel.addComponent(option1Label);
		
		btTemplateGeneration = new Button(Messages.getString("ServiceSelectionPanel.option1"));  //$NON-NLS-1$
		option1Panel.addComponent(btTemplateGeneration);
		
		VerticalLayout option2Panel = new VerticalLayout();
		option2Panel.setSpacing(true);
		addCenteredComponent(option2Panel);
		
		Label option2Label = new Label(
			Messages.getString("ServiceSelectionPanel.option2Info"),  //$NON-NLS-1$
			ContentMode.HTML);
		option2Panel.addComponent(option2Label);
		
		btConversion = new Button(Messages.getString("ServiceSelectionPanel.option2"));  //$NON-NLS-1$
		option2Panel.addComponent(btConversion);	
		
		VerticalLayout option3Panel = new VerticalLayout();
		option3Panel.setSpacing(true);
		option3Panel.setMargin(new MarginInfo(true, false, true, false));
		addCenteredComponent(option3Panel);
		
		Label option3Label = new Label(
			Messages.getString("ServiceSelectionPanel.option3Info"),  //$NON-NLS-1$
			ContentMode.HTML);
		option3Panel.addComponent(option3Label);
		
		btConfTool = new Button(Messages.getString("ServiceSelectionPanel.option3"));  //$NON-NLS-1$
		option3Panel.addComponent(btConfTool);	
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		Page.getCurrent().setTitle(Messages.getString("ServiceSelectionPanel.title")); //$NON-NLS-1$
	}

}
