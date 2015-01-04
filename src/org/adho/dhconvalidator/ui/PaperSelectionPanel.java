package org.adho.dhconvalidator.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.adho.dhconvalidator.conftool.ConfToolClient;
import org.adho.dhconvalidator.conftool.Paper;
import org.adho.dhconvalidator.conftool.User;
import org.adho.dhconvalidator.conversion.ZipFs;
import org.adho.dhconvalidator.conversion.input.InputConverter;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Table;

public class PaperSelectionPanel extends CenterPanel implements View {
	
	private Table paperTable;
	private Button btGenerate;
	private InputConverter inputConverter;

	public PaperSelectionPanel(InputConverter inputConverter) {
		super(true);
		this.inputConverter = inputConverter;
		initComponents();
	}

	private void initData() {
		paperTable.removeAllItems();
		try {
			List<Paper> papers = new ConfToolClient().getPapers(
					(User)VaadinSession.getCurrent().getAttribute(SessionStorageKey.USER.name()));
			for (Paper paper : papers) {
				paperTable.addItem(new Object[] {paper.getTitle()}, paper);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			Notification.show(
				"Error", "Unable to load papers from ConfTool: " + e.getLocalizedMessage(), 
				Type.ERROR_MESSAGE);
		}
		
	}

	private void initComponents() {
		Label info = new Label(
			"Please select one or more "
			+ "submissions to generate the templates:", ContentMode.HTML);
		
		paperTable = new Table("Your submissions");
		paperTable.setSelectable(true);
		paperTable.setMultiSelect(true);
		paperTable.setPageLength(4);
		paperTable.addContainerProperty("title", String.class, null);
		paperTable.setColumnHeader("title", "Title");
		paperTable.setWidth("100%");
		paperTable.setImmediate(true);
		
		btGenerate = new Button("Generate Templates");
		new FileDownloader(new StreamResource(
				new StreamSource() {
			
					@Override
					public InputStream getStream() {
						return createTemplates();
					}
				}, "your_personal_dh_templates.zip" )).extend(btGenerate);
		
		addCenteredComponent(info);
		addCenteredComponent(paperTable);
		addCenteredComponent(btGenerate);
	}

	protected InputStream createTemplates() {
		@SuppressWarnings("unchecked")
		Set<Paper> selection = (Set<Paper>) paperTable.getValue();
		
		if (selection.isEmpty()) {
			Notification.show(
				"Info", 
				"Please select a submission first!", 
				Type.HUMANIZED_MESSAGE);
			return null;
		}
		else {
			try {
				ZipFs zipFs = new ZipFs();
				int idx = 1;
				for (Paper paper : selection) {
					zipFs.putDocument(
						idx + "_" + paper.getTitle().replaceAll("[^0-9a-zA-Z]", "_")
							+ "." +inputConverter.getFileExtension(), 
						inputConverter.getPersonalizedTemplate(paper));
					idx++;
				}
				
				return new ByteArrayInputStream(zipFs.toZipData());
			} catch (IOException e) {
				e.printStackTrace();
				Notification.show(
						"Error", 
						"template creation failed",
						Type.ERROR_MESSAGE);
				return null;
			}
		}
		
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		initData();
		Page.getCurrent().setTitle("DHConvalidator Template Generation Service");
	}
}
