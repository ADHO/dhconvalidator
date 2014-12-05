package org.adho.dhconvalidator.conftool;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

public class DocumentToPaperMapper {

	public List<Paper> getPaperList(Document document) {
		System.out.println(document.toXML());
		List<Paper> result = new ArrayList<>();
		Elements paperElements =
			document.getRootElement().getChildElements("paper");
		
		for (int i=0; i<paperElements.size(); i++) {
			Element paperElement = paperElements.get(i);
			Integer paperId = 
				Integer.valueOf(paperElement.getFirstChildElement("paperID").getValue());
			
			String authors = 
					paperElement.getFirstChildElement("authors").getValue();
			String organisations = 
					paperElement.getFirstChildElement("organisations").getValue();
			String title = 
					paperElement.getFirstChildElement("title").getValue();
			String keywords =
					paperElement.getFirstChildElement("keywords").getValue();
			result.add(new Paper(paperId, title, authors, organisations, keywords));
		}
		
		return result;
	}

	private List<String> makeAuthorNameList(String value) {

		
		return null;
	}

}
