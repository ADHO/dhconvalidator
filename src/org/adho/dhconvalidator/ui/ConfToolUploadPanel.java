package org.adho.dhconvalidator.ui;

import org.adho.dhconvalidator.Messages;
import org.adho.dhconvalidator.properties.PropertyKey;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

public class ConfToolUploadPanel extends CenterPanel implements View {
	
	public ConfToolUploadPanel() {
		super(true);
		initComponents();
	}

	private void initComponents() {
		
		Label infoLabel = new Label(
			Messages.getString(
					"ConfToolUploadPanel.info", 
					PropertyKey.conftool_login_url.getValue()),
			ContentMode.HTML);
		infoLabel.setWidth("600px"); //$NON-NLS-1$
		addCenteredComponent(infoLabel);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// noop
	}
}
