package org.adho.dhconvalidator.ui;

import org.adho.dhconvalidator.Messages;

import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.BaseTheme;

public class AboutLink extends Button {
	
	public AboutLink() {
		super(Messages.getString("AboutLink.title")); //$NON-NLS-1$
		setStyleName(BaseTheme.BUTTON_LINK);
		addStyleName("plain-link"); //$NON-NLS-1$
		addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().addWindow(new AboutWindow());
			}
		});
	}

}
