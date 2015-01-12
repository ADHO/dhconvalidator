package org.adho.dhconvalidator.conftool;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

public class DocumentToPaperMapper {

	public List<Paper> getPaperList(Document document) {
		
		List<Paper> result = new ArrayList<>();
		Elements paperElements =
			document.getRootElement().getChildElements("paper"); //$NON-NLS-1$
		
		for (int i=0; i<paperElements.size(); i++) {
			Element paperElement = paperElements.get(i);
			Integer paperId = 
				Integer.valueOf(paperElement.getFirstChildElement("paperID").getValue()); //$NON-NLS-1$
			
			String authors = 
					paperElement.getFirstChildElement("authors").getValue(); //$NON-NLS-1$
			String organisations = 
					paperElement.getFirstChildElement("organisations").getValue(); //$NON-NLS-1$
			String title = 
					paperElement.getFirstChildElement("title").getValue(); //$NON-NLS-1$
			String keywords =
					paperElement.getFirstChildElement("keywords").getValue(); //$NON-NLS-1$
			String topics =
					paperElement.getFirstChildElement("topics").getValue(); //$NON-NLS-1$
			String contributionType =
					paperElement.getFirstChildElement("contribution_type").getValue(); //$NON-NLS-1$
			
			result.add(
				new Paper(
					paperId, title, authors, organisations, 
					keywords, topics, contributionType));
		}
		
		return result;
	}
}
