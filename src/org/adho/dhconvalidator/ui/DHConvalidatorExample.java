package org.adho.dhconvalidator.ui;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.adho.dhconvalidator.Messages;
import org.apache.commons.io.IOUtils;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("dhconvalidator")
@PreserveOnRefresh
@Push(value=PushMode.MANUAL)
public class DHConvalidatorExample extends UI {

	@Override
	protected void init(VaadinRequest request) {
		try {
			VerticalLayout content = new VerticalLayout();
			content.setMargin(true);
			content.setSpacing(true);
			
			setContent(content);
			
			LogoutLink logoutLink = new LogoutLink();
			content.addComponent(logoutLink);
			content.setComponentAlignment(logoutLink, Alignment.TOP_RIGHT);
	
			Button btGetInputfile = 
				new Button(
					Messages.getString("DHConvalidatorExample.btInputCaption")); //$NON-NLS-1$
			content.addComponent(btGetInputfile);
			
			FileDownloader inputFileDownloader = new FileDownloader(new StreamResource(
				new StreamSource() {
					
					@Override
					public InputStream getStream() {
						return Thread.currentThread().getContextClassLoader().getResourceAsStream(
							"/org/adho/dhconvalidator/conversion/example/1_Digital_Humanities.odt"); //$NON-NLS-1$
					}
				}, "1_Digital_Humanities.odt")); //$NON-NLS-1$
			inputFileDownloader.extend(btGetInputfile);
			
			Button btGetOutputfile = 
					new Button(
						Messages.getString(
							"DHConvalidatorExample.btConversionResultCaption")); //$NON-NLS-1$
			content.addComponent(btGetOutputfile);
			
			FileDownloader outputFileDownloader = new FileDownloader(new StreamResource(
				new StreamSource() {
					
					@Override
					public InputStream getStream() {
						return Thread.currentThread().getContextClassLoader().getResourceAsStream(
							"/org/adho/dhconvalidator/conversion/example/1_Digital_Humanities.dhc"); //$NON-NLS-1$
					}
				}, "1_Digital_Humanities.dhc")); //$NON-NLS-1$
			outputFileDownloader.extend(btGetOutputfile);

			Label preview = new Label("", ContentMode.HTML); //$NON-NLS-1$
			preview.addStyleName("tei-preview"); //$NON-NLS-1$
			preview.setWidth("800px"); //$NON-NLS-1$
			
			content.addComponent(preview);
			content.setComponentAlignment(preview, Alignment.MIDDLE_CENTER);
	
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
			IOUtils.copy(
				Thread.currentThread().getContextClassLoader().getResourceAsStream(
						"/org/adho/dhconvalidator/conversion/example/1_Digital_Humanities.html"),  //$NON-NLS-1$
				buffer);
			
			preview.setValue(buffer.toString("UTF-8")); //$NON-NLS-1$

			Page.getCurrent().setTitle(Messages.getString("DHConvalidatorExample.title")); //$NON-NLS-1$
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
