/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conversion;

/**
 * The TEI namespace.
 * 
 * @author marco.petris@web.de
 *
 */
public enum TeiNamespace {
	TEI("tei", "http://www.tei-c.org/ns/1.0"), //$NON-NLS-1$ //$NON-NLS-2$
	;
	private String name;
	private String uri;

	private TeiNamespace(String name, String uri) {
		this.name = name;
		this.uri = uri;
	}

	public String toUri() {
		return uri;
	}
	
	public String getName() {
		return name;
	}
}
