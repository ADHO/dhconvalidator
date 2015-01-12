package org.adho.dhconvalidator.ui;

import org.adho.dhconvalidator.Messages;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class CenterPanel extends VerticalLayout {
	
	private VerticalLayout center;
	private HeaderPanel headerPanel;

	public CenterPanel(boolean showHeader) {
		initComponents(showHeader);
	}

	private void initComponents(boolean showHeader) {
		center = new VerticalLayout();
		center.setSpacing(true);
		setSizeFull();
		addComponent(center);
		center.setSizeUndefined();
		setComponentAlignment(center, Alignment.MIDDLE_CENTER);
		if (showHeader) {
			headerPanel = new HeaderPanel();
			center.addComponent(headerPanel);
			Label title = new Label(Messages.getString("CenterPanel.title")); //$NON-NLS-1$
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

	public BackLink getBackLink() {
		return headerPanel.getBackLink();
	}

	
}
