package org.adho.dhconvalidator.ui;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

@Theme("dhconvalidator")
@PreserveOnRefresh
@Push
public class DHConvalidatorTabUI extends UI {

	@Override
	protected void init(VaadinRequest request) {
		if ((VaadinSession.getCurrent().getAttribute(SessionStorageKey.USER.name())) == null) {
			setContent(new LoginPanel());
		}
		else {
			setContent(new ServiceSelectionPanel());
		}
	}

}
