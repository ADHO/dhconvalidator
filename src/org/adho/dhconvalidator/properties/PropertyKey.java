package org.adho.dhconvalidator.properties;

import org.adho.dhconvalidator.ui.PropertyProvider;

public enum PropertyKey {
	conftool_shared_pass,
	conftool_url,
	oxgarage_url,
	tei_pictures_location,
	tei_media_location,
	publicationStmt, 
	encodingDesc,
	version,
	;
	
	public String getValue() {
		return PropertyProvider.getProperties().getProperty(this.name());
	}
}
