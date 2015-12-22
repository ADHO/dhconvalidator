/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conversion;

/**
 * Types that are supported either as input or output by the DHConvalidator.
 * 
 * @author marco.petris@web.de
 *
 */
public enum Type {
	ODT("odt%3Aapplication%3Avnd.oasis.opendocument.text/", "odt"), //$NON-NLS-1$ //$NON-NLS-2$
	TEI("TEI%3Atext%3Axml/", "tei"), //$NON-NLS-1$ //$NON-NLS-2$
	DOCX("docx%3Aapplication%3Avnd.openxmlformats-officedocument.wordprocessingml.document/", "docx"), //$NON-NLS-1$ //$NON-NLS-2$
	XHTML("xhtml%3Aapplication%3Axhtml%2Bxml/", "xhmtl"), //$NON-NLS-1$ //$NON-NLS-2$
	;
	private String identifier;
	private String extension;
	
	/**
	 * @param identifier OxGarage identifier
	 * @param extension default file extension
	 */
	private Type(String identifier, String extension) {
		this.identifier = identifier;
		this.extension = extension;
	}

	/**
	 * @return OxGarage identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * @return default file extension
	 */
	public String getExtension() {
		return extension;
	}
	
	/**
	 * @param ext extension to test
	 * @return <code>true</code> if one Type has the specified extension
	 */
	public static boolean hasExtension(String ext) {
		for (Type type : Type.values()) {
			if (type.getExtension().equals(ext)) {
				return true;
			}
		}
		
		return false;
	}
}
