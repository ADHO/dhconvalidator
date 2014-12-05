package org.adho.dhconvalidator.ui;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class ServiceSelectionPanel extends VerticalLayout {
	
	private Button btOdtTemplate;
	private Button btDocxTemplate;
	private Button btConversionPanel;

	public ServiceSelectionPanel() {
		initComponents();
		initActions();
		
	}

	private void initActions() {
		btOdtTemplate.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				
			}
		});
		
	}

	private void initComponents() {
		setSpacing(true);
		VerticalLayout templateGeneratorServiceContent = new VerticalLayout();
		templateGeneratorServiceContent.setSpacing(true);
		templateGeneratorServiceContent.setMargin(true);
		
		Panel templateGeneratorServicePanel = 
			new Panel("Template Generation Service", templateGeneratorServiceContent);
//		templateGeneratorServicePanel.setSizeUndefined();
		btOdtTemplate = new Button("Open Document (odt)");
		
		btOdtTemplate.setDescription(
			"If you want to use Libre Office, Open Office or similar choose the odt format.");
		templateGeneratorServiceContent.addComponent(btOdtTemplate);
		
		btDocxTemplate = new Button("Microsoft Office Document (docx)");
		btDocxTemplate.setDescription(
			"If you want to use Microsoft Office choose the docx format.");
		templateGeneratorServiceContent.addComponent(btDocxTemplate);
		
		addComponent(templateGeneratorServicePanel);
		
		VerticalLayout conversionAndValidationServiceContent = new VerticalLayout();
		conversionAndValidationServiceContent.setSpacing(true);
		conversionAndValidationServiceContent.setMargin(true);

		Panel conversionAndValidationServicePanel = 
				new Panel(
						"Conversion and Validation Service",
						conversionAndValidationServiceContent);
//		conversionAndValidationServicePanel.setSizeUndefined();
		btConversionPanel = new Button("Continue");
		conversionAndValidationServiceContent.addComponent(btConversionPanel);
		conversionAndValidationServiceContent.setComponentAlignment(
				btConversionPanel, Alignment.MIDDLE_CENTER);
		addComponent(conversionAndValidationServicePanel);
		
	}

}
