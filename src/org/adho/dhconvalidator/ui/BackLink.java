package org.adho.dhconvalidator.ui;

import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.BaseTheme;

public class BackLink extends Button {
	public BackLink() {
		super("Back");
		setStyleName(BaseTheme.BUTTON_LINK);
		addStyleName("logout-link");
		addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().getNavigator().navigateTo("");
			}
		});
	}

}
