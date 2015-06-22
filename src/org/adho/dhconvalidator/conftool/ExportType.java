/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conftool;

/**
 * DHConvalidator relevant ConfTool export types.
 * 
 * @author marco.petris@web.de
 *
 */
enum ExportType {
	/**
	 * all papers for a specific user 
	 */
	papers,
	/**
	 * details for a specific user or a list of users
	 */
	users, 
	/**
	 * list of accepted authors 
	 */
	subsumed_authors,
	;
}
