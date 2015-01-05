package org.adho.dhconvalidator.ui;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("dhconvalidator")
@PreserveOnRefresh
@Push
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
	
			Button btGetInputfile = new Button("Get the template based input document");
			content.addComponent(btGetInputfile);
			
			FileDownloader inputFileDownloader = new FileDownloader(new StreamResource(
				new StreamSource() {
					
					@Override
					public InputStream getStream() {
						return Thread.currentThread().getContextClassLoader().getResourceAsStream(
							"/org/adho/dhconvalidator/conversion/example/1_Digital_Humanities.odt");
					}
				}, "1_Digital_Humanities.odt"));
			inputFileDownloader.extend(btGetInputfile);
			
			Button btGetOutputfile = 
					new Button("Get the conversion result as compressed TEI");
			content.addComponent(btGetOutputfile);
			
			FileDownloader outputFileDownloader = new FileDownloader(new StreamResource(
				new StreamSource() {
					
					@Override
					public InputStream getStream() {
						return Thread.currentThread().getContextClassLoader().getResourceAsStream(
							"/org/adho/dhconvalidator/conversion/example/1_Digital_Humanities.dhc");
					}
				}, "1_Digital_Humanities.dhc"));
			outputFileDownloader.extend(btGetOutputfile);

			Label preview = new Label("", ContentMode.HTML);
			preview.addStyleName("tei-preview");
			preview.setWidth("800px");
			
			content.addComponent(preview);
			content.setComponentAlignment(preview, Alignment.MIDDLE_CENTER);
	
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
			IOUtils.copy(
				Thread.currentThread().getContextClassLoader().getResourceAsStream(
						"/org/adho/dhconvalidator/conversion/example/1_Digital_Humanities.html"), 
				buffer);
			
			preview.setValue(buffer.toString("UTF-8"));
			
			Page.getCurrent().setTitle("DHConvalidator Example");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
