package org.adho.dhconvalidator.ui;

import org.adho.dhconvalidator.Messages;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class TemplateGeneratorPanel extends CenterPanel implements View {
	
	private Button btOdtTemplate;
	private Button btDocxTemplate;

	public TemplateGeneratorPanel() {
		super(true);
		initComponents();
		initActions();
	}

	private void initActions() {
		btDocxTemplate.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().getNavigator().navigateTo(
						ServicesViewName.docx.name());
			}
		});
		
		btOdtTemplate.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().getNavigator().navigateTo(
						ServicesViewName.odt.name());
			}
		});
	}

	private void initComponents() {
		Label infoLabel = new Label(
			Messages.getString("TemplateGeneratorPanel.intro"), //$NON-NLS-1$
			ContentMode.HTML);
		infoLabel.setWidth("600px"); //$NON-NLS-1$
		addCenteredComponent(infoLabel);
		
		btOdtTemplate = new Button(Messages.getString("TemplateGeneratorPanel.odtButtonCaption")); //$NON-NLS-1$
		
		btOdtTemplate.setDescription(
			Messages.getString("TemplateGeneratorPanel.odtButtonDescription")); //$NON-NLS-1$
		addCenteredComponent(btOdtTemplate);
		
		btDocxTemplate = new Button(Messages.getString("TemplateGeneratorPanel.docxButtonCaption")); //$NON-NLS-1$
		btDocxTemplate.setDescription(
			Messages.getString("TemplateGeneratorPanel.docxButtonDescription")); //$NON-NLS-1$
		addCenteredComponent(btDocxTemplate);

	}
	
	@Override
	protected String getTitle() {
		return Messages.getString("TemplateGeneratorPanel.title");
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// noop
	}
}

