/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conftool;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

/**
 * Maps a ConfTool User result document to a {@link User}.
 * @author marco.petris@web.de
 *
 */
public class DocumentToUserMapper {

	public User getUser(Document document) {
		Elements userElements = 
				document.getRootElement().getChildElements("user"); //$NON-NLS-1$
		
		if (userElements.size() == 1) {
			
			Element userElement = userElements.get(0);
			Integer userId = 
				Integer.valueOf(
					userElement.getFirstChildElement("personID").getValue()); //$NON-NLS-1$
			String firstName = 
					userElement.getFirstChildElement("firstname").getValue(); //$NON-NLS-1$
			String lastName =  
					userElement.getFirstChildElement("name").getValue(); //$NON-NLS-1$
					
			return new User(userId, firstName, lastName);
		}
		
		return null;
	}

}
