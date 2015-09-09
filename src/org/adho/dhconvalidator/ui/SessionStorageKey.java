/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.ui;

/**
 * The keys used to store session values in {@link com.vaadin.server.VaadinSession#setAttribute(String, Object)}
 * @author marco.petris@web.de
 *
 */
public enum SessionStorageKey {
	/**
	 * The conversion result.
	 */
	ZIPRESULT,
	/**
	 * The authenticated user.
	 */
	USER,
	;
}
