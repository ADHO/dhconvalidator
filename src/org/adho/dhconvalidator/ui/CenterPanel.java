package org.adho.dhconvalidator.ui;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class CenterPanel extends VerticalLayout {
	
	private VerticalLayout center;

	public CenterPanel() {
		initComponents();
	}

	private void initComponents() {
		center = new VerticalLayout();
		center.setSpacing(true);
		setSizeFull();
		addComponent(center);
		center.setSizeUndefined();
		setComponentAlignment(center, Alignment.MIDDLE_CENTER);
	}

	public void addCenteredComponent(Component c) {
		center.addComponent(c);
	}
	
	public void addCenteredComponent(Component c, Alignment alignment) {
		center.addComponent(c);
		center.setComponentAlignment(c, alignment);
	}

}
