package org.adho.dhconvalidator.conftool;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

public class DocumentToUserMapper {

	public User getUser(Document document) {
		System.out.println(document.toXML());
		
		Elements userElements = 
				document.getRootElement().getChildElements("user");
		
		if (userElements.size() == 1) {
			
			Element userElement = userElements.get(0);
			Integer userId = 
				Integer.valueOf(
					userElement.getFirstChildElement("personID").getValue());
			String firstName = 
					userElement.getFirstChildElement("firstname").getValue();
			String lastName =  
					userElement.getFirstChildElement("name").getValue();
					
			return new User(userId, firstName, lastName);
		}
		
		return null;
	}

}
