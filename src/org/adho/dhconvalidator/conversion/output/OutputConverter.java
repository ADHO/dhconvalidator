/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conversion.output;

import java.io.IOException;

import nu.xom.Document;

import org.adho.dhconvalidator.conversion.oxgarage.ZipResult;
import org.adho.dhconvalidator.paper.Paper;
import org.adho.dhconvalidator.user.User;

/**
 * An Ouptput converter refines the TEI result of OxGarage.
 * 
 * @author marco.petris@web.de
 *
 */
public interface OutputConverter {
	/**
	 * @param document the TEI document
	 * @param user the user that initiated the conversion
	 * @param paper the paper that is being handled
	 * @throws IOException in case of any failure
	 */
	public void convert(Document document, User user, Paper paper) throws IOException;
	/**
	 * This method makes changes to the whole {@link ZipResult} like renaming/moving external resources.
	 * @param zipResult the zip package that is going to be returned
	 * @throws IOException in case of any failure
	 */
	public void convert(ZipResult zipResult) throws IOException;
}
