/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.adho.dhconvalidator.Messages;
import org.adho.dhconvalidator.conversion.Converter;
import org.adho.dhconvalidator.conversion.SubmissionLanguage;
import org.adho.dhconvalidator.conversion.ZipFs;
import org.adho.dhconvalidator.conversion.input.InputConverter;
import org.adho.dhconvalidator.paper.Paper;
import org.adho.dhconvalidator.properties.PropertyKey;
import org.adho.dhconvalidator.user.User;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Table;

/**
 * A panel that displays the available papers for template generation.
 * @author marco.petris@web.de
 *
 */
public class PaperSelectionPanel extends CenterPanel implements View {
	
	private Table paperTable;
	private Button btGenerate;
	private InputConverter inputConverter;
	private Label postDownloadLabel;
	private ComboBox languages;

	/**
	 * @param inputConverter the InputConverter to be used for template generation
	 */
	public PaperSelectionPanel(InputConverter inputConverter) {
		super(true, ServicesViewName.templates);
		this.inputConverter = inputConverter;
		initComponents();
	}

	/**
	 * Load and display the current papers.
	 */
	private void initData() {
		postDownloadLabel.setVisible(false);
		
		paperTable.removeAllItems();
		try {
			List<Paper> papers = PropertyKey.getPaperProviderInstance().getPapers(
					(User)VaadinSession.getCurrent().getAttribute(SessionStorageKey.USER.name()));
			for (Paper paper : papers) {
				paperTable.addItem(new Object[] {paper.getTitle()}, paper);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			Notification.show(
				Messages.getString("PaperSelectionPanel.error1Title"),  //$NON-NLS-1$
				Messages.getString(
						"PaperSelectionPanel.conftoolerrormsg",//$NON-NLS-1$
						 e.getLocalizedMessage()),
				Type.ERROR_MESSAGE);
		}
		
	}

	/**
	 * Setup UI.
	 */
	private void initComponents() {
		Label info = new Label(
			Messages.getString("PaperSelectionPanel.hintMsg"), //$NON-NLS-1$
			ContentMode.HTML); 
		
		languages = 
				new ComboBox(Messages.getString("PaperSelectionPanel.language"), //$NON-NLS-1$
						Arrays.asList(SubmissionLanguage.values()));
		languages.setNullSelectionAllowed(false);
		languages.setValue(SubmissionLanguage.ENGLISH);
		
		paperTable = new Table(Messages.getString("PaperSelectionPanel.tableTitle")); //$NON-NLS-1$
		paperTable.setSelectable(true);
		paperTable.setMultiSelect(true);
		paperTable.setPageLength(4);
		paperTable.addContainerProperty("title", String.class, null); //$NON-NLS-1$
		paperTable.setColumnHeader(
			"title", 
			Messages.getString("PaperSelectionPanel.titleColumnTitle")); //$NON-NLS-1$ //$NON-NLS-2$
		paperTable.setWidth("100%"); //$NON-NLS-1$
		paperTable.setImmediate(true);
		
		btGenerate = new Button(
			Messages.getString(
				"PaperSelectionPanel.generateButtonCaption")); //$NON-NLS-1$
		StreamResource templateStreamResource = 
				new StreamResource(
						new StreamSource() {
							@Override
							public InputStream getStream() {
								return createTemplates();
							}
						}, "your_personal_dh_templates.zip" ); //$NON-NLS-1$
		
		templateStreamResource.setCacheTime(0);
		new FileDownloader(templateStreamResource).extend(btGenerate);
		
		addCenteredComponent(info);
		addCenteredComponent(languages);
		addCenteredComponent(paperTable);
		addCenteredComponent(btGenerate);
		
		postDownloadLabel = 
				new Label(
					Messages.getString("PaperSelectionPanel.postDownloadInfo",
						inputConverter.getTextEditorDescription(),
						PropertyKey.base_url.getValue()+"popup/DHConvalidatorServices#!converter"),
					ContentMode.HTML);
		postDownloadLabel.addStyleName("postDownloadInfoRedAndBold");
		postDownloadLabel.setVisible(false);
		
		addCenteredComponent(postDownloadLabel);
		
	}

	/**
	 * @return a zipped container with all generated templates.
	 */
	private InputStream createTemplates() {
		@SuppressWarnings("unchecked")
		Set<Paper> selection = (Set<Paper>) paperTable.getValue();
		
		if (selection.isEmpty()) {
			Notification.show(
				Messages.getString("PaperSelectionPanel.selectSubmissionTitle"),  //$NON-NLS-1$
				Messages.getString("PaperSelectionPanel.selectSubmissionMsg"),  //$NON-NLS-1$
				Type.HUMANIZED_MESSAGE);
			return null;
		}
		else {
			try {
				// generate and add a template for each paper
				ZipFs zipFs = new ZipFs();
				int idx = 1;
				for (Paper paper : selection) {
					
					String title = paper.getTitle().replaceAll("[^0-9a-zA-Z]", "_"); //$NON-NLS-1$ //$NON-NLS-2$ 
					
					if (title.length() > PropertyKey.maxfilenamelength.getValue(Converter.DEFAULT_MAX_FILE_LENGTH)) {
						title = title.substring(0, Converter.DEFAULT_MAX_FILE_LENGTH);
					}
					SubmissionLanguage submissionLanguage = 
							(SubmissionLanguage) languages.getValue();
					
					zipFs.putDocument(
						idx + "_" + title //$NON-NLS-1$
							+ "." +inputConverter.getFileExtension(),  //$NON-NLS-1$
						inputConverter.getPersonalizedTemplate(paper, submissionLanguage));
					idx++;
				}
				postDownloadLabel.setVisible(true);
//				UI.getCurrent().push();
				
				return new ByteArrayInputStream(zipFs.toZipData());
			} catch (IOException e) {
				e.printStackTrace();
				Notification.show(
						Messages.getString("PaperSelectionPanel.templateCreationErrorTitle"),  //$NON-NLS-1$
						Messages.getString("PaperSelectionPanel.templateCreationErrorMsg"), //$NON-NLS-1$
						Type.ERROR_MESSAGE);
				return null;
			}
		}
		
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		// reload data
		initData();
		Page.getCurrent().setTitle(Messages.getString("PaperSelectionPanel.title")); //$NON-NLS-1$
	}
}
