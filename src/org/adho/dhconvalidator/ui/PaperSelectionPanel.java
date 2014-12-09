package org.adho.dhconvalidator.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.adho.dhconvalidator.conftool.ConfToolCacheProvider;
import org.adho.dhconvalidator.conftool.Paper;
import org.adho.dhconvalidator.conftool.User;
import org.adho.dhconvalidator.conversion.ZipFs;
import org.adho.dhconvalidator.conversion.input.InputConverter;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Table;

public class PaperSelectionPanel extends CenterPanel {
	
	private Table paperTable;
	private Button btGenerate;
	private InputConverter inputConverter;

	public PaperSelectionPanel(InputConverter inputConverter) {
		this.inputConverter = inputConverter;
		initComponents();
		initActions();
		initData();
	}

	private void initData() {
		List<Paper> papers = ConfToolCacheProvider.INSTANCE.getConfToolCache().getPapers(
				(User)VaadinSession.getCurrent().getAttribute(SessionStorageKey.USER.name()));
		for (Paper paper : papers) {
			paperTable.addItem(new Object[] {paper.getTitle()}, paper);
		}
		
	}

	private void initActions() {
//		btGenerate.addClickListener(new ClickListener() {
//			
//			@Override
//			public void buttonClick(ClickEvent event) {
//				@SuppressWarnings("unchecked")
//				Set<Paper> selection = (Set<Paper>) paperTable.getValue();
//				if (selection.isEmpty()) {
//					Notification.show(
//						"Info", 
//						"Please select a submission first!", 
//						Type.HUMANIZED_MESSAGE);
//				}
//				else {
//					System.out.println(paperTable.getValue());
//				}
//			}
//		});
	}

	private void initComponents() {
		LogoutLink logoutLink = new LogoutLink();
		
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
		
		addCenteredComponent(logoutLink, Alignment.TOP_RIGHT);
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
}
