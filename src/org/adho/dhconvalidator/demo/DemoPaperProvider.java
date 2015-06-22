/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.demo;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.adho.dhconvalidator.paper.Paper;
import org.adho.dhconvalidator.paper.PaperProvider;
import org.adho.dhconvalidator.user.User;

/**
 * Provides a single paper for demo purposes.
 * 
 * @author marco.petris@web.de
 * 
 */
public class DemoPaperProvider implements PaperProvider {

	@Override
	public List<Paper> getPapers(User user) throws IOException {
		return Collections.singletonList(new Paper(
				1, "DHConvalidator - Test", Collections.singletonList(
					new User(
							user.getFirstName(), 
							user.getLastName(), 
							"University of Hamburg", 
							user.getEmail())), 
				"Digital Humanities", "Software Testing", "Long Paper"));
	}

	@Override
	public Paper getPaper(User user, Integer paperId) throws IOException {
		return new Paper(
				paperId, 
				"DHConvalidator - Test", 
				Collections.singletonList(new User(
						user.getFirstName(), 
						user.getLastName(), 
						"University of Hamburg", 
						user.getEmail())), 
				"Digital Humanities", "Software Testing", "Long Paper");
	}

}
