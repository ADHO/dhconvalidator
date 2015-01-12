package org.adho.dhconvalidator.conversion;

public enum Type {
	ODT("odt%3Aapplication%3Avnd.oasis.opendocument.text/", "odt"), //$NON-NLS-1$ //$NON-NLS-2$
	TEI("TEI%3Atext%3Axml/", "tei"), //$NON-NLS-1$ //$NON-NLS-2$
	DOCX("docx%3Aapplication%3Avnd.openxmlformats-officedocument.wordprocessingml.document/", "docx"), //$NON-NLS-1$ //$NON-NLS-2$
	XHTML("xhtml%3Aapplication%3Axhtml%2Bxml/", "xhmtl"), //$NON-NLS-1$ //$NON-NLS-2$
	;
	private String identifier;
	private String extension;
	
	private Type(String identifier, String extension) {
		this.identifier = identifier;
		this.extension = extension;
	}

	public String getIdentifier() {
		return identifier;
	}
	
	public String getExtension() {
		return extension;
	}
}
