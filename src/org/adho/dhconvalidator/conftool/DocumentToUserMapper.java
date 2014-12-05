package org.adho.dhconvalidator.conftool;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

public class DocumentToUserMapper {

	public List<User> getUserList(Document document) {
		System.out.println(document.toXML());
		
		Elements authorElements = 
				document.getRootElement().getChildElements("subsumed_author");
		List<User> result = new ArrayList<>();
		
		for (int i=0; i<authorElements.size(); i++) {
			Element authorElement = authorElements.get(i);
			Integer userId = 
				Integer.valueOf(
					authorElement.getFirstChildElement("personID").getValue());
			String firstName = 
					authorElement.getFirstChildElement("firstname").getValue();
			String lastName =  
					authorElement.getFirstChildElement("firstname").getValue();
					
			List<Integer> paperIds = 
				makeIntegerList(
					authorElement.getFirstChildElement("paperIDs").getValue());
		
			result.add(new User(userId, firstName, lastName, paperIds));
		}
		
		
		return result;
	}

	private List<Integer> makeIntegerList(String value) {
		List<Integer> result = new ArrayList<>();
		if (!value.isEmpty()) {
			String[] split = value.split(",");
			for (String splitValue : split) {
				result.add(Integer.valueOf(splitValue));
			}
		}
		return result;
	}

}
