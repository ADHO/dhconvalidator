package org.adho.dhconvalidator.ui;

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

public class ServiceSelectionPanel extends CenterPanel implements View {
	
	private Button btOdtTemplate;
	private Button btDocxTemplate;
	private Button btConversionPanel;

	public ServiceSelectionPanel() {
		super(true);
		initComponents();
		initActions();
		
	}

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

	private void initComponents() {
		VerticalLayout templateGeneratorServiceContent = new VerticalLayout();
		templateGeneratorServiceContent.setSpacing(true);
		templateGeneratorServiceContent.setMargin(true);
		
		Panel templateGeneratorServicePanel = 
			new Panel("Template Generation Service", templateGeneratorServiceContent);
		templateGeneratorServicePanel.setWidth("500px");
		templateGeneratorServiceContent.addComponent(
				new Label("This service will generate a template for each of "
						+ "your accepted submissions.<br>"
				+ "Please use these templates to prepare your submissions for "
				+ "the Conversion<br>and Validation Service.",
				ContentMode.HTML));
		
		btOdtTemplate = new Button("Open Document (odt)");
		
		btOdtTemplate.setDescription(
			"If you want to use Libre Office, Open Office or similar choose the odt format.");
		templateGeneratorServiceContent.addComponent(btOdtTemplate);
		
		btDocxTemplate = new Button("Microsoft Office Document (docx)");
		btDocxTemplate.setDescription(
			"If you want to use Microsoft Office 2007 or later choose the docx format.");
		templateGeneratorServiceContent.addComponent(btDocxTemplate);
		
		VerticalLayout conversionAndValidationServiceContent = new VerticalLayout();
		
		conversionAndValidationServiceContent.setSpacing(true);
		conversionAndValidationServiceContent.setMargin(true);

		Panel conversionAndValidationServicePanel = 
				new Panel(
						"Conversion and Validation Service",
						conversionAndValidationServiceContent);
		conversionAndValidationServiceContent.addComponent(
			new Label("This service will convert your edited and template based documents<br>"
					+ " to compressed TEI packages which can be uploaded to ConfTool.", 
						ContentMode.HTML));
		btConversionPanel = new Button("Continue");
		conversionAndValidationServiceContent.addComponent(btConversionPanel);
		conversionAndValidationServiceContent.setComponentAlignment(
				btConversionPanel, Alignment.MIDDLE_CENTER);

		addCenteredComponent(templateGeneratorServicePanel); 
		addCenteredComponent(conversionAndValidationServicePanel);
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		Page.getCurrent().setTitle("DHConvalidator Service Selection");
	}

}
