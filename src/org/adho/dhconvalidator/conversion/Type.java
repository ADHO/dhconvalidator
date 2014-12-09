package org.adho.dhconvalidator.conversion;

public enum Type {
	ODT("odt%3Aapplication%3Avnd.oasis.opendocument.text/", "odt"),
	TEI("TEI%3Atext%3Axml/", "tei"),
	DOCX("docx%3Aapplication%3Avnd.openxmlformats-officedocument.wordprocessingml.document/", "docx"),
	XHTML("xhtml%3Aapplication%3Axhtml%2Bxml/", "xhmtl"),
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
