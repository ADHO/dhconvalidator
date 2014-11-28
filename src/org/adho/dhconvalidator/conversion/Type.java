package org.adho.dhconvalidator.conversion;

public enum Type {
	ODT("odt%3Aapplication%3Avnd.oasis.opendocument.text/"),
	TEI("TEI%3Atext%3Axml/"),
	DOCX("docx%3Aapplication%3Avnd.openxmlformats-officedocument.wordprocessingml.document/"),
	XHTML("xhtml%3Aapplication%3Axhtml%2Bxml/"),
	;
	private String identifier;

	private Type(String identifier) {
		this.identifier = identifier;
	}
	
	public String getIdentifier() {
		return identifier;
	}
}
