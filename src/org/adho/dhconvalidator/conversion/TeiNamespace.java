package org.adho.dhconvalidator.conversion;

public enum TeiNamespace {
	TEI("tei", "http://www.tei-c.org/ns/1.0"),
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
