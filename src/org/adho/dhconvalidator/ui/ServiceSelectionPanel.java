package org.adho.dhconvalidator.ui;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class ServiceSelectionPanel extends CenterPanel implements View {
	
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
		LogoutLink logoutLink = new LogoutLink();

		VerticalLayout templateGeneratorServiceContent = new VerticalLayout();
		templateGeneratorServiceContent.setSpacing(true);
		templateGeneratorServiceContent.setMargin(true);
		
		Panel templateGeneratorServicePanel = 
			new Panel("Template Generation Service", templateGeneratorServiceContent);

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
		
		btConversionPanel = new Button("Continue");
		conversionAndValidationServiceContent.addComponent(btConversionPanel);
		conversionAndValidationServiceContent.setComponentAlignment(
				btConversionPanel, Alignment.MIDDLE_CENTER);

		addCenteredComponent(logoutLink, Alignment.TOP_RIGHT);
		addCenteredComponent(templateGeneratorServicePanel); 
		addCenteredComponent(conversionAndValidationServicePanel);
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		//noop
	}

}
