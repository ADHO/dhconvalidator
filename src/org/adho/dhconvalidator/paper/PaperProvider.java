/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.paper;

import java.io.IOException;
import java.util.List;

import org.adho.dhconvalidator.user.User;

/**
 * Metadata provider for templates and conversion.
 * 
 * @author  marco.petris@web.de
 *
 */
public interface PaperProvider {
	/**
	 * @param user a User
	 * @return a list of all submmissions of the given user
	 * @throws IOException in case of any failure
	 */
	public List<Paper> getPapers(User user) throws IOException;

	/**
	 * @param user a User
	 * @param paperId the ConfTool paperID
	 * @return the Paper with the given ID or <code>null</code>
	 * @throws IOException  in case of any failure
	 */
	public Paper getPaper(User user, Integer paperId) throws IOException;
}

