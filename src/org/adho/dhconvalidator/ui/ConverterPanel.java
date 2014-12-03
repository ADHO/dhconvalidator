package org.adho.dhconvalidator.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.adho.dhconvalidator.conversion.ConversionPath;
import org.adho.dhconvalidator.conversion.Converter;
import org.adho.dhconvalidator.conversion.oxgarage.ZipResult;

import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.StartedListener;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;

public class ConverterPanel extends VerticalLayout {
	
	private Upload upload;
	private ByteArrayOutputStream uploadContent;
	private String filename;
	private ProgressBar progressBar;
	private RichTextArea logArea;
	private Label preview;
	private HorizontalSplitPanel resultPanel;
	private Label resultCaption;
	
	public ConverterPanel() {
		initComponents();
		initActions();
	}

	private void initActions() {
		upload.addSucceededListener(new SucceededListener() {
			
			@Override
			public void uploadSucceeded(SucceededEvent event) {
				try {
					
					byte[] uploadData = uploadContent.toByteArray();
					if (uploadData.length == 0) {
						Notification.show(
							"Info", 
							"Please select a file first!", 
							Type.TRAY_NOTIFICATION);
					}
					else {
						
						Converter converter =
								new Converter(
									"http://85.214.78.116:8080/ege-webservice/");
	//								"http://www.tei-c.org/ege-webservice/"); //TODO: config							
						
						
						ZipResult zipResult = converter.convert(
							uploadData, 
							ConversionPath.getConvertionPathByFilename(filename));
						VaadinSession.getCurrent().setAttribute(
								SessionStorageKey.ZIPRESULT.name(), zipResult);
						System.out.println(converter.getContentAsXhtml());
						preview.setValue(converter.getContentAsXhtml());
						resultCaption.setValue("Preview and Conversion log " + filename );
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				progressBar.setVisible(false);
				UI.getCurrent().setPollInterval(-1);	
			}
		});
		upload.addStartedListener(new StartedListener() {
			
			@Override
			public void uploadStarted(StartedEvent event) {
				progressBar.setVisible(true);
				UI.getCurrent().setPollInterval(500);
			}
		});

		upload.addFailedListener(new FailedListener() {
			
			@Override
			public void uploadFailed(FailedEvent event) {
				resultCaption.setValue("Preview and Conversion log for file " + filename );
				progressBar.setVisible(false);
				UI.getCurrent().setPollInterval(-1);
			}
		});
	}

	private void initComponents() {
		setSizeFull();
		setSpacing(true);
		HorizontalLayout inputPanel = new HorizontalLayout();
		addComponent(inputPanel);
		
		upload = new Upload(
			"Please upload your .docx file, .odt file or latex package .zip file",
			new Receiver() {
				@Override
				public OutputStream receiveUpload(String filename,
						String mimeType) {
					ConverterPanel.this.filename = filename;
					ConverterPanel.this.uploadContent = new ByteArrayOutputStream(); 
					return ConverterPanel.this.uploadContent;
				}
			});

		inputPanel.addComponent(upload);
		
		progressBar = new ProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setVisible(false);
		inputPanel.addComponent(progressBar);
		inputPanel.setComponentAlignment(progressBar, Alignment.MIDDLE_CENTER);
	
		resultCaption = new Label("Preview and Conversion log");
		addComponent(resultCaption);
		
		resultPanel = new HorizontalSplitPanel();
		addComponent(resultPanel);
		resultPanel.setSizeFull();
		
		logArea = new RichTextArea();
		logArea.setSizeFull();
		logArea.setReadOnly(true);
		resultPanel.addComponent(logArea);
		
		preview = new Label("", ContentMode.HTML);
		resultPanel.addComponent(preview);
		
		setExpandRatio(resultPanel, 1.0f);
		
	}

}
