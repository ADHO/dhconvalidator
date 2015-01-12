/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.ui;

import org.adho.dhconvalidator.Messages;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * A panel that lets the user select the service to use.
 * 
 * @author marco.petris@web.de
 *
 */
public class ServiceSelectionPanel extends CenterPanel implements View {
	
	private Button btOdtTemplate;
	private Button btDocxTemplate;
	private Button btConversionPanel;

	public ServiceSelectionPanel() {
		super(true);
		initComponents();
		initActions();
		
	}

	/**
	 * Setup behaviour.
	 */
	private void initActions() {
		btOdtTemplate.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().getNavigator().navigateTo(
						ServicesViewName.odt.name());
			}
		});
		btConversionPanel.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().getNavigator().navigateTo(
						ServicesViewName.converter.name());
			}
		});
		btDocxTemplate.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().getNavigator().navigateTo(
						ServicesViewName.docx.name());
			}
		});
	}

	/**
	 * Setup UI.
	 */
	private void initComponents() {
		setHeightUndefined();
		
		getBackLink().setVisible(false);
		
		Label infoLabel = new Label(
			Messages.getString("ServiceSelectionPanel.intro"), //$NON-NLS-1$
			ContentMode.HTML);
		infoLabel.setWidth("600px"); //$NON-NLS-1$
		addCenteredComponent(infoLabel);
		
		VerticalLayout templateGeneratorServiceContent = new VerticalLayout();
		templateGeneratorServiceContent.setSpacing(true);
		templateGeneratorServiceContent.setMargin(true);
		
		Panel templateGeneratorServicePanel = 
			new Panel(Messages.getString("ServiceSelectionPanel.templateGenerationService"), //$NON-NLS-1$
					templateGeneratorServiceContent); 
		templateGeneratorServicePanel.setWidth("500px"); //$NON-NLS-1$
		templateGeneratorServiceContent.addComponent(
				new Label(Messages.getString("ServiceSelectionPanel.templateGenerationServiceInfo"), //$NON-NLS-1$
				ContentMode.HTML));
		
		btOdtTemplate = new Button(Messages.getString("ServiceSelectionPanel.odtButtonCaption")); //$NON-NLS-1$
		
		btOdtTemplate.setDescription(
			Messages.getString("ServiceSelectionPanel.odtButtonDescription")); //$NON-NLS-1$
		templateGeneratorServiceContent.addComponent(btOdtTemplate);
		
		btDocxTemplate = new Button(Messages.getString("ServiceSelectionPanel.docxButtonCaption")); //$NON-NLS-1$
		btDocxTemplate.setDescription(
			Messages.getString("ServiceSelectionPanel.docxButtonDescription")); //$NON-NLS-1$
		templateGeneratorServiceContent.addComponent(btDocxTemplate);
		
		VerticalLayout conversionAndValidationServiceContent = new VerticalLayout();
		
		conversionAndValidationServiceContent.setSpacing(true);
		conversionAndValidationServiceContent.setMargin(true);

		Panel conversionAndValidationServicePanel = 
				new Panel(
						Messages.getString("ServiceSelectionPanel.conversionService"), //$NON-NLS-1$
						conversionAndValidationServiceContent);
		conversionAndValidationServicePanel.setWidth("500px"); //$NON-NLS-1$
		conversionAndValidationServiceContent.addComponent(
			new Label(
				Messages.getString("ServiceSelectionPanel.conversionServiceInfo"), //$NON-NLS-1$
				ContentMode.HTML));
		btConversionPanel = new Button(Messages.getString("ServiceSelectionPanel.btContinue")); //$NON-NLS-1$
		conversionAndValidationServiceContent.addComponent(btConversionPanel);
		conversionAndValidationServiceContent.setComponentAlignment(
				btConversionPanel, Alignment.MIDDLE_CENTER);

		addCenteredComponent(templateGeneratorServicePanel); 
		addCenteredComponent(conversionAndValidationServicePanel);
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		Page.getCurrent().setTitle(Messages.getString("ServiceSelectionPanel.title")); //$NON-NLS-1$
	}

}
